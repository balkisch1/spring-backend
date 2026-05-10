package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trajets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String villeDepart;

    @Column(nullable = false)
    private String villeArrivee;

    @Column(nullable = false)
    private LocalDateTime dateHeureDepart;

    @Column(nullable = false)
    private LocalDateTime dateHeureArrivee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransport type;

    @Column(nullable = false)
    private String compagnie;

    private String numeroVol; // pour avion/train

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParPlace;

    @Column(nullable = false)
    private Integer capaciteTotale;

    @Column(nullable = false)
    private Integer placesDisponibles;

    @Column(nullable = false)
    private Boolean actif = true;

    @Version
    private Integer version; // Optimistic Locking

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneReservation> lignesReservation;

    public enum TypeTransport {
        AVION, TRAIN, BUS, FERRY
    }
}