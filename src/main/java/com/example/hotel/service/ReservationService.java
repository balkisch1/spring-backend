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

    /**
     * Créer une réservation avec gestion des transactions et Optimistic Locking.
     * Si 2 clients réservent la même chambre simultanément, une exception sera levée.
     */
    @Transactional(rollbackFor = Exception.class)
    public Reservation creerReservation(Long clientId, List<Long> chambreIds,
                                        List<LocalDate> datesArrivee, List<LocalDate> datesDepart,
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

        for (int i = 0; i < chambreIds.size(); i++) {
            Chambre chambre = chambreRepository.findById(chambreIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

            // Vérifier disponibilité (optimistic locking via @Version)
            List<Chambre> disponibles = chambreRepository.findChambresDisponibles(
                    datesArrivee.get(i), datesDepart.get(i)
            );

            boolean estDisponible = disponibles.stream()
                    .anyMatch(c -> c.getId().equals(chambre.getId()));

            if (!estDisponible) {
                throw new RuntimeException("Chambre " + chambre.getNumero() + " non disponible pour cette période");
            }

            long nombreNuits = ChronoUnit.DAYS.between(datesArrivee.get(i), datesDepart.get(i));
            if (nombreNuits <= 0) throw new RuntimeException("Dates invalides");

            BigDecimal sousTotal = chambre.getPrixParNuit().multiply(BigDecimal.valueOf(nombreNuits));

            LigneReservation ligne = LigneReservation.builder()
                    .reservation(reservation)
                    .chambre(chambre)
                    .dateArrivee(datesArrivee.get(i))
                    .dateDepart(datesDepart.get(i))
                    .nombreNuits((int) nombreNuits)
                    .prixUnitaire(chambre.getPrixParNuit())
                    .sousTotal(sousTotal)
                    .build();

            reservation.getLignesReservation().add(ligne);
            total = total.add(sousTotal);
        }

        reservation.setPrixTotal(total);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirmerReservation(Long id) {
        Reservation reservation = getReservationById(id);
        reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation annulerReservation(Long id) {
        Reservation reservation = getReservationById(id);
        if (reservation.getStatut() == Reservation.StatutReservation.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une réservation terminée");
        }
        reservation.setStatut(Reservation.StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }

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
            put("revenuTotal", reservationRepository.totalRevenu());
        }};
    }
}