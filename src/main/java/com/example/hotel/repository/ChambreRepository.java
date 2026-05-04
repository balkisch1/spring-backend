package com.example.hotel.repository;

import com.example.hotel.entity.Chambre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {

    // JPQL : Chambres disponibles pour une période donnée
    @Query("""
        SELECT c FROM Chambre c 
        WHERE c.disponible = true 
        AND c.id NOT IN (
            SELECT lr.chambre.id FROM LigneReservation lr 
            WHERE lr.reservation.statut IN ('EN_ATTENTE', 'CONFIRMEE')
            AND NOT (lr.dateDepart <= :dateArrivee OR lr.dateArrivee >= :dateDepart)
        )
    """)
    List<Chambre> findChambresDisponibles(
            @Param("dateArrivee") LocalDate dateArrivee,
            @Param("dateDepart") LocalDate dateDepart
    );

    // JPQL : Chambres disponibles avec filtres + pagination
    @Query("""
        SELECT c FROM Chambre c 
        WHERE c.disponible = true 
        AND (:type IS NULL OR c.type = :type)
        AND (:prixMax IS NULL OR c.prixParNuit <= :prixMax)
        AND (:capacite IS NULL OR c.capacite >= :capacite)
        AND c.id NOT IN (
            SELECT lr.chambre.id FROM LigneReservation lr 
            WHERE lr.reservation.statut IN ('EN_ATTENTE', 'CONFIRMEE')
            AND NOT (lr.dateDepart <= :dateArrivee OR lr.dateArrivee >= :dateDepart)
        )
    """)
    Page<Chambre> findChambresDisponiblesFiltered(
            @Param("dateArrivee") LocalDate dateArrivee,
            @Param("dateDepart") LocalDate dateDepart,
            @Param("type") Chambre.TypeChambre type,
            @Param("prixMax") BigDecimal prixMax,
            @Param("capacite") Integer capacite,
            Pageable pageable
    );

    List<Chambre> findByType(Chambre.TypeChambre type);

    Page<Chambre> findAll(Pageable pageable);
}
