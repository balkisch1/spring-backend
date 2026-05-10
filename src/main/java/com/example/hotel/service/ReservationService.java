package com.example.hotel.service;

import com.example.hotel.entity.*;
import com.example.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChambreRepository chambreRepository;
    private final ClientRepository clientRepository;
    private final TrajetRepository trajetRepository;
    private final NotificationService notificationService;

    // ── Records utilisés par le Controller ────────────────────────────────────
    public record LigneHotelRequest(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart) {}
    public record LigneTransportRequest(Long trajetId, int nombrePlaces) {}

    // ── HOTEL ─────────────────────────────────────────────────────────────────

    /**
     * Créer une réservation HÔTEL avec Optimistic Locking (@Version sur Chambre).
     * Si 2 clients réservent la même chambre simultanément, une OptimisticLockException
     * est levée et la transaction est rollback automatiquement.
     */
    @Transactional(rollbackFor = Exception.class)
    public Reservation creerReservationHotel(Long clientId,
                                             List<LigneHotelRequest> lignes,
                                             String commentaire) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Reservation reservation = Reservation.builder()
                .client(client)
                .statut(Reservation.StatutReservation.EN_ATTENTE)
                .commentaire(commentaire)
                .prixTotal(BigDecimal.ZERO)
                .lignesReservation(new ArrayList<>())
                .build();

        reservationRepository.save(reservation);
        BigDecimal total = BigDecimal.ZERO;

        for (LigneHotelRequest req : lignes) {
            Chambre chambre = chambreRepository.findById(req.chambreId())
                    .orElseThrow(() -> new RuntimeException("Chambre non trouvée : " + req.chambreId()));

            // Vérifier disponibilité via JPQL (Optimistic Locking via @Version sur Chambre)
            List<Chambre> dispo = chambreRepository.findChambresDisponibles(
                    req.dateArrivee(), req.dateDepart()
            );
            boolean disponible = dispo.stream().anyMatch(c -> c.getId().equals(chambre.getId()));
            if (!disponible)
                throw new RuntimeException("Chambre " + chambre.getNumero() + " non disponible pour cette période");

            long nuits = ChronoUnit.DAYS.between(req.dateArrivee(), req.dateDepart());
            if (nuits <= 0) throw new RuntimeException("Dates invalides");

            BigDecimal sousTotal = chambre.getPrixParNuit().multiply(BigDecimal.valueOf(nuits));

            reservation.getLignesReservation().add(LigneReservation.builder()
                    .reservation(reservation)
                    .chambre(chambre)
                    .dateArrivee(req.dateArrivee())
                    .dateDepart(req.dateDepart())
                    .nombreNuits((int) nuits)
                    .prixUnitaire(chambre.getPrixParNuit())
                    .sousTotal(sousTotal)
                    .typeLigne(LigneReservation.TypeLigne.HOTEL)
                    .build());

            total = total.add(sousTotal);
        }

        reservation.setPrixTotal(total);
        Reservation saved = reservationRepository.save(reservation);

        // 🔔 Notification automatique au client
        notificationService.notifierReservationCreee(saved);
        return saved;
    }

    // ── TRANSPORT ─────────────────────────────────────────────────────────────

    /**
     * Créer une réservation TRANSPORT avec Pessimistic Locking.
     * La ligne trajet est verrouillée en base pendant toute la transaction
     * pour éviter la survente de places (2 clients ne peuvent pas réserver
     * la même place en même temps).
     */
    @Transactional(rollbackFor = Exception.class)
    public Reservation creerReservationTransport(Long clientId,
                                                 List<LigneTransportRequest> lignes,
                                                 String commentaire) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        Reservation reservation = Reservation.builder()
                .client(client)
                .statut(Reservation.StatutReservation.EN_ATTENTE)
                .commentaire(commentaire)
                .prixTotal(BigDecimal.ZERO)
                .lignesReservation(new ArrayList<>())
                .build();

        reservationRepository.save(reservation);
        BigDecimal total = BigDecimal.ZERO;

        for (LigneTransportRequest req : lignes) {
            Trajet trajet = trajetRepository.findById(req.trajetId())
                    .orElseThrow(() -> new RuntimeException("Trajet non trouvé : " + req.trajetId()));

            if (!trajet.getActif())
                throw new RuntimeException("Ce trajet n'est plus actif");

            if (trajet.getPlacesDisponibles() < req.nombrePlaces())
                throw new RuntimeException("Seulement " + trajet.getPlacesDisponibles() + " place(s) disponible(s)");

            // Décrémentation atomique via @Modifying JPQL (Pessimistic Lock)
            int updated = trajetRepository.decrementersPlaces(trajet.getId(), req.nombrePlaces());
            if (updated == 0)
                throw new RuntimeException("Conflit concurrent : places insuffisantes, veuillez réessayer");

            BigDecimal sousTotal = trajet.getPrixParPlace().multiply(BigDecimal.valueOf(req.nombrePlaces()));

            reservation.getLignesReservation().add(LigneReservation.builder()
                    .reservation(reservation)
                    .trajet(trajet)
                    .nombrePlaces(req.nombrePlaces())
                    .prixUnitaire(trajet.getPrixParPlace())
                    .sousTotal(sousTotal)
                    .typeLigne(LigneReservation.TypeLigne.TRANSPORT)
                    .build());

            total = total.add(sousTotal);
        }

        reservation.setPrixTotal(total);
        Reservation saved = reservationRepository.save(reservation);

        // 🔔 Notification automatique au client
        notificationService.notifierReservationCreee(saved);
        return saved;
    }

    // ── ACTIONS COMMUNES ──────────────────────────────────────────────────────

    @Transactional
    public Reservation confirmerReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);
        Reservation saved = reservationRepository.save(reservation);

        // 🔔 Notification de confirmation
        notificationService.notifierReservationConfirmee(saved);
        return saved;
    }

    @Transactional
    public Reservation annulerReservation(Long id) {
        Reservation reservation = getReservationById(id);

        if (reservation.getStatut() == Reservation.StatutReservation.TERMINEE)
            throw new RuntimeException("Impossible d'annuler une réservation terminée");

        // Libérer les places transport si applicable (rollback métier)
        for (LigneReservation ligne : reservation.getLignesReservation()) {
            if (LigneReservation.TypeLigne.TRANSPORT.equals(ligne.getTypeLigne())
                    && ligne.getTrajet() != null) {
                trajetRepository.incrementerPlaces(ligne.getTrajet().getId(), ligne.getNombrePlaces());
            }
        }

        reservation.setStatut(Reservation.StatutReservation.ANNULEE);
        Reservation saved = reservationRepository.save(reservation);

        // 🔔 Notification d'annulation
        notificationService.notifierReservationAnnulee(saved);
        return saved;
    }

    // ── LECTURE ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée : " + id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservationsClient(Long clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> getAllReservations(Pageable pageable) {
        return reservationRepository.findAllByOrderByDateCreationDesc(pageable);
    }

    public Object getDashboardStats() {
        return new java.util.HashMap<>() {{
            put("totalReservations", reservationRepository.count());
            put("reservationsConfirmees", reservationRepository.countReservationsConfirmees());
            put("reservationsEnAttente", reservationRepository.countReservationsEnAttente());
            put("totalClients", clientRepository.count());
            put("totalChambres", chambreRepository.count());
            put("totalTrajets", trajetRepository.countTrajetsActifs());
            put("revenuTotal", reservationRepository.totalRevenu());
        }};
    }
}