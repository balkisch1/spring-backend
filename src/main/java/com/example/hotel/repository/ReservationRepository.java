package com.example.hotel.repository;

import com.example.hotel.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Réservations d'un client (avec JPQL)
    @Query("SELECT r FROM Reservation r WHERE r.client.id = :clientId ORDER BY r.dateCreation DESC")
    List<Reservation> findByClientId(@Param("clientId") Long clientId);

    // Toutes les réservations paginées (admin)
    Page<Reservation> findAllByOrderByDateCreationDesc(Pageable pageable);

    // Stats dashboard
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.statut = 'CONFIRMEE'")
    Long countReservationsConfirmees();

    @Query("SELECT SUM(r.prixTotal) FROM Reservation r WHERE r.statut = 'CONFIRMEE'")
    Double totalRevenu();

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.statut = 'EN_ATTENTE'")
    Long countReservationsEnAttente();
}