package com.example.hotel.repository;

import com.example.hotel.entity.Trajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    // JPQL : Trajets disponibles avec places restantes
    @Query("""
        SELECT t FROM Trajet t
        WHERE t.actif = true
        AND t.placesDisponibles > 0
        AND (:villeDepart IS NULL OR LOWER(t.villeDepart) LIKE LOWER(CONCAT('%', :villeDepart, '%')))
        AND (:villeArrivee IS NULL OR LOWER(t.villeArrivee) LIKE LOWER(CONCAT('%', :villeArrivee, '%')))
        AND (:dateDepart IS NULL OR t.dateHeureDepart >= :dateDepart)
        AND (:type IS NULL OR t.type = :type)
        ORDER BY t.dateHeureDepart ASC
    """)
    Page<Trajet> findTrajetsDisponibles(
            @Param("villeDepart") String villeDepart,
            @Param("villeArrivee") String villeArrivee,
            @Param("dateDepart") LocalDateTime dateDepart,
            @Param("type") Trajet.TypeTransport type,
            Pageable pageable
    );

    // Mise à jour places (Pessimistic Locking via @Lock)
    @Modifying
    @Query("UPDATE Trajet t SET t.placesDisponibles = t.placesDisponibles - :nb WHERE t.id = :id AND t.placesDisponibles >= :nb")
    int decrementersPlaces(@Param("id") Long id, @Param("nb") int nb);

    @Modifying
    @Query("UPDATE Trajet t SET t.placesDisponibles = t.placesDisponibles + :nb WHERE t.id = :id")
    int incrementerPlaces(@Param("id") Long id, @Param("nb") int nb);

    List<Trajet> findByActifTrue();

    // Stats
    @Query("SELECT COUNT(t) FROM Trajet t WHERE t.actif = true AND t.placesDisponibles > 0")
    Long countTrajetsActifs();
}