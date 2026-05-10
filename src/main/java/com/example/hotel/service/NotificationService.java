package com.example.hotel.service;

import com.example.hotel.entity.Client;
import com.example.hotel.entity.Notification;
import com.example.hotel.entity.Reservation;
import com.example.hotel.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Crée automatiquement une notification lors de la création d'une réservation.
     */
    @Transactional
    public void notifierReservationCreee(Reservation reservation) {
        Client client = reservation.getClient();
        String mode = determinerMode(reservation);

        Notification notif = Notification.builder()
                .client(client)
                .reservation(reservation)
                .type(Notification.TypeNotification.RESERVATION_CREEE)
                .titre("✅ Réservation #" + reservation.getId() + " créée")
                .message(String.format(
                        "Bonjour %s, votre réservation %s #%d a bien été enregistrée pour un montant de %.2f €. " +
                                "Elle est actuellement en attente de confirmation.",
                        client.getPrenom(), mode, reservation.getId(), reservation.getPrixTotal()
                ))
                .build();

        notificationRepository.save(notif);
        log.info("📧 Notification envoyée à {} pour réservation #{}", client.getEmail(), reservation.getId());
    }

    /**
     * Notification automatique lors de la confirmation par un admin.
     */
    @Transactional
    public void notifierReservationConfirmee(Reservation reservation) {
        Client client = reservation.getClient();

        Notification notif = Notification.builder()
                .client(client)
                .reservation(reservation)
                .type(Notification.TypeNotification.RESERVATION_CONFIRMEE)
                .titre("🎉 Réservation #" + reservation.getId() + " confirmée !")
                .message(String.format(
                        "Excellente nouvelle %s ! Votre réservation #%d est officiellement confirmée. " +
                                "Nous vous attendons avec impatience. Montant total : %.2f €.",
                        client.getPrenom(), reservation.getId(), reservation.getPrixTotal()
                ))
                .build();

        notificationRepository.save(notif);
    }

    /**
     * Notification lors de l'annulation d'une réservation.
     */
    @Transactional
    public void notifierReservationAnnulee(Reservation reservation) {
        Client client = reservation.getClient();

        Notification notif = Notification.builder()
                .client(client)
                .reservation(reservation)
                .type(Notification.TypeNotification.RESERVATION_ANNULEE)
                .titre("❌ Réservation #" + reservation.getId() + " annulée")
                .message(String.format(
                        "Bonjour %s, votre réservation #%d a été annulée. " +
                                "Si vous avez des questions, n'hésitez pas à nous contacter.",
                        client.getPrenom(), reservation.getId()
                ))
                .build();

        notificationRepository.save(notif);
    }

    /**
     * Notification de bienvenue lors de l'inscription.
     */
    @Transactional
    public void notifierBienvenue(Client client) {
        Notification notif = Notification.builder()
                .client(client)
                .type(Notification.TypeNotification.BIENVENUE)
                .titre("👋 Bienvenue " + client.getPrenom() + " !")
                .message(String.format(
                        "Bienvenue sur Grand Hôtel & Transport, %s ! " +
                                "Votre compte a été créé avec succès. " +
                                "Explorez nos chambres et nos trajets pour planifier votre prochain voyage.",
                        client.getPrenom()
                ))
                .build();

        notificationRepository.save(notif);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsClient(Long clientId) {
        return notificationRepository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNonLues(Long clientId) {
        return notificationRepository.findNonLuesByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Long countNonLues(Long clientId) {
        return notificationRepository.countNonLues(clientId);
    }

    @Transactional
    public void marquerLue(Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            n.setLue(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void marquerToutesLues(Long clientId) {
        notificationRepository.marquerToutesLues(clientId);
    }

    private String determinerMode(Reservation reservation) {
        if (reservation.getLignesReservation() == null || reservation.getLignesReservation().isEmpty()) return "";
        var premiereLigne = reservation.getLignesReservation().get(0);
        if (premiereLigne.getTypeLigne() != null) {
            return switch (premiereLigne.getTypeLigne()) {
                case HOTEL -> "hôtelière";
                case TRANSPORT -> "de transport";
            };
        }
        return "";
    }
}