package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lignes_reservation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LigneReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // ── Mode Hôtel ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chambre_id")
    private Chambre chambre;

    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private Integer nombreNuits;

    // ── Mode Transport ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    private Integer nombrePlaces;

    // ── Commun ─────────────────────────────────────────────────
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeLigne typeLigne;

    public enum TypeLigne {
        HOTEL, TRANSPORT
    }
}