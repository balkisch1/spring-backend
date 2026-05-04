package com.example.hotel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "chambres")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeChambre type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixParNuit;

    @Column(nullable = false)
    private Integer capacite;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Version
    private Integer version; // Optimistic Locking

    @OneToMany(mappedBy = "chambre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LigneReservation> lignesReservation;

    public enum TypeChambre {
        SIMPLE, DOUBLE, SUITE, DELUXE, PENTHOUSE
    }
}