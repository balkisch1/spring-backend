package com.example.hotel.repository;

import com.example.hotel.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Toutes les notifs d'un client, triées par date DESC
    @Query("SELECT n FROM Notification n WHERE n.client.id = :clientId ORDER BY n.dateCreation DESC")
    List<Notification> findByClientId(@Param("clientId") Long clientId);

    // Notifs non lues
    @Query("SELECT n FROM Notification n WHERE n.client.id = :clientId AND n.lue = false ORDER BY n.dateCreation DESC")
    List<Notification> findNonLuesByClientId(@Param("clientId") Long clientId);

    // Compter non lues
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.client.id = :clientId AND n.lue = false")
    Long countNonLues(@Param("clientId") Long clientId);

    // Marquer toutes comme lues
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true WHERE n.client.id = :clientId")
    void marquerToutesLues(@Param("clientId") Long clientId);
}