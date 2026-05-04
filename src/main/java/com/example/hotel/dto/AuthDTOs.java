package com.example.hotel.dto;

import com.example.hotel.entity.Chambre;
import com.example.hotel.entity.Client;
import com.example.hotel.entity.LigneReservation;
import com.example.hotel.entity.Reservation;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ── Auth DTOs ──────────────────────────────────────────────────
public class AuthDTOs {

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterRequest {
        private String email;
        private String password;
        private String nom;
        private String prenom;
        private String telephone;
    }

    @Data @Builder
    public static class AuthResponse {
        private String token;
        private String email;
        private String nom;
        private String prenom;
        private String role;
        private Long id;
    }
}

// ── Client DTO ─────────────────────────────────────────────────
@Data @Builder
class ClientDTO {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String telephone;
    private Client.Role role;

    public static ClientDTO from(Client c) {
        return ClientDTO.builder()
                .id(c.getId())
                .email(c.getEmail())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .telephone(c.getTelephone())
                .role(c.getRole())
                .build();
    }
}

// ── Chambre DTOs ───────────────────────────────────────────────
class ChambreDTOs {

    @Data @Builder
    public static class ChambreResponse {
        private Long id;
        private String numero;
        private Chambre.TypeChambre type;
        private BigDecimal prixParNuit;
        private Integer capacite;
        private String description;
        private String imageUrl;
        private Boolean disponible;

        public static ChambreResponse from(Chambre c) {
            return ChambreResponse.builder()
                    .id(c.getId())
                    .numero(c.getNumero())
                    .type(c.getType())
                    .prixParNuit(c.getPrixParNuit())
                    .capacite(c.getCapacite())
                    .description(c.getDescription())
                    .imageUrl(c.getImageUrl())
                    .disponible(c.getDisponible())
                    .build();
        }
    }

    @Data
    public static class ChambreRequest {
        private String numero;
        private Chambre.TypeChambre type;
        private BigDecimal prixParNuit;
        private Integer capacite;
        private String description;
        private String imageUrl;
    }

    @Data
    public static class DisponibiliteRequest {
        private LocalDate dateArrivee;
        private LocalDate dateDepart;
        private String type;
        private BigDecimal prixMax;
        private Integer capacite;
    }
}

// ── Reservation DTOs ───────────────────────────────────────────
class ReservationDTOs {

    @Data
    public static class LigneRequest {
        private Long chambreId;
        private LocalDate dateArrivee;
        private LocalDate dateDepart;
    }

    @Data
    public static class ReservationRequest {
        private List<LigneRequest> lignes;
        private String commentaire;
    }

    @Data @Builder
    public static class LigneResponse {
        private Long id;
        private Long chambreId;
        private String chambreNumero;
        private Chambre.TypeChambre chambreType;
        private LocalDate dateArrivee;
        private LocalDate dateDepart;
        private Integer nombreNuits;
        private BigDecimal prixUnitaire;
        private BigDecimal sousTotal;

        public static LigneResponse from(LigneReservation l) {
            return LigneResponse.builder()
                    .id(l.getId())
                    .chambreId(l.getChambre().getId())
                    .chambreNumero(l.getChambre().getNumero())
                    .chambreType(l.getChambre().getType())
                    .dateArrivee(l.getDateArrivee())
                    .dateDepart(l.getDateDepart())
                    .nombreNuits(l.getNombreNuits())
                    .prixUnitaire(l.getPrixUnitaire())
                    .sousTotal(l.getSousTotal())
                    .build();
        }
    }

    @Data @Builder
    public static class ReservationResponse {
        private Long id;
        private Long clientId;
        private String clientNom;
        private String clientEmail;
        private LocalDateTime dateCreation;
        private Reservation.StatutReservation statut;
        private BigDecimal prixTotal;
        private String commentaire;
        private List<LigneResponse> lignes;

        public static ReservationResponse from(Reservation r) {
            List<LigneResponse> lignes = r.getLignesReservation() == null ? List.of() :
                    r.getLignesReservation().stream().map(LigneResponse::from).toList();
            return ReservationResponse.builder()
                    .id(r.getId())
                    .clientId(r.getClient().getId())
                    .clientNom(r.getClient().getNom() + " " + r.getClient().getPrenom())
                    .clientEmail(r.getClient().getEmail())
                    .dateCreation(r.getDateCreation())
                    .statut(r.getStatut())
                    .prixTotal(r.getPrixTotal())
                    .commentaire(r.getCommentaire())
                    .lignes(lignes)
                    .build();
        }
    }
}

// ── Dashboard DTO ──────────────────────────────────────────────
@Data @Builder
class DashboardDTO {
    private Long totalReservations;
    private Long reservationsConfirmees;
    private Long reservationsEnAttente;
    private Long totalClients;
    private Long totalChambres;
    private Double revenuTotal;
}