package com.example.hotel.service;

import com.example.hotel.entity.Trajet;
import com.example.hotel.repository.TrajetRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrajetService {

    private final TrajetRepository trajetRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<Trajet> searchTrajets(String villeDepart, String villeArrivee,
                                      LocalDateTime dateDepart, String type, Pageable pageable) {
        Trajet.TypeTransport typeTransport = null;
        if (type != null && !type.isEmpty()) {
            typeTransport = Trajet.TypeTransport.valueOf(type.toUpperCase());
        }
        return trajetRepository.findTrajetsDisponibles(
                villeDepart, villeArrivee, dateDepart, typeTransport, pageable
        );
    }

    @Transactional(readOnly = true)
    public Trajet getTrajetById(Long id) {
        return trajetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé : " + id));
    }

    /**
     * Réservation de places avec Pessimistic Locking pour éviter les doublons.
     * La ligne est verrouillée le temps de la transaction.
     */
    @Transactional(rollbackFor = Exception.class)
    public Trajet reserverPlaces(Long trajetId, int nombrePlaces) {
        // Pessimistic Lock : bloque la ligne en base pendant la transaction
        Trajet trajet = entityManager.find(Trajet.class, trajetId, LockModeType.PESSIMISTIC_WRITE);

        if (trajet == null) throw new RuntimeException("Trajet non trouvé");
        if (!trajet.getActif()) throw new RuntimeException("Ce trajet n'est plus actif");
        if (trajet.getPlacesDisponibles() < nombrePlaces) {
            throw new RuntimeException(
                    "Seulement " + trajet.getPlacesDisponibles() + " place(s) disponible(s), " +
                            "vous en demandez " + nombrePlaces
            );
        }

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - nombrePlaces);
        return trajetRepository.save(trajet);
    }

    @Transactional
    public void libererPlaces(Long trajetId, int nombrePlaces) {
        trajetRepository.incrementerPlaces(trajetId, nombrePlaces);
    }

    @Transactional
    public Trajet createTrajet(Trajet trajet) {
        trajet.setPlacesDisponibles(trajet.getCapaciteTotale());
        return trajetRepository.save(trajet);
    }

    @Transactional
    public Trajet updateTrajet(Long id, Trajet updated) {
        Trajet trajet = getTrajetById(id);
        trajet.setVilleDepart(updated.getVilleDepart());
        trajet.setVilleArrivee(updated.getVilleArrivee());
        trajet.setDateHeureDepart(updated.getDateHeureDepart());
        trajet.setDateHeureArrivee(updated.getDateHeureArrivee());
        trajet.setType(updated.getType());
        trajet.setCompagnie(updated.getCompagnie());
        trajet.setNumeroVol(updated.getNumeroVol());
        trajet.setPrixParPlace(updated.getPrixParPlace());
        trajet.setActif(updated.getActif());
        return trajetRepository.save(trajet);
    }

    @Transactional
    public void deleteTrajet(Long id) {
        trajetRepository.deleteById(id);
    }
}