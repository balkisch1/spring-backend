package com.example.hotel.config;

import com.example.hotel.entity.Chambre;
import com.example.hotel.entity.Client;
import com.example.hotel.repository.ChambreRepository;
import com.example.hotel.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ChambreRepository chambreRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (chambreRepository.count() == 0) {
            List<Chambre> chambres = List.of(
                    Chambre.builder().numero("101").type(Chambre.TypeChambre.SIMPLE)
                            .prixParNuit(new BigDecimal("80.00")).capacite(1)
                            .description("Chambre simple confortable avec vue sur le jardin.")
                            .imageUrl("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("102").type(Chambre.TypeChambre.SIMPLE)
                            .prixParNuit(new BigDecimal("90.00")).capacite(1)
                            .description("Chambre simple avec balcon et vue sur la ville.")
                            .imageUrl("https://images.unsplash.com/photo-1586023492125-27b2c045efd7?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("201").type(Chambre.TypeChambre.DOUBLE)
                            .prixParNuit(new BigDecimal("150.00")).capacite(2)
                            .description("Chambre double spacieuse avec grand lit et salle de bain moderne.")
                            .imageUrl("https://images.unsplash.com/photo-1618773928121-c32242e63f39?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("202").type(Chambre.TypeChambre.DOUBLE)
                            .prixParNuit(new BigDecimal("160.00")).capacite(2)
                            .description("Chambre double premium avec jacuzzi privé.")
                            .imageUrl("https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("301").type(Chambre.TypeChambre.SUITE)
                            .prixParNuit(new BigDecimal("280.00")).capacite(3)
                            .description("Suite luxueuse avec salon séparé et vue panoramique.")
                            .imageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("302").type(Chambre.TypeChambre.SUITE)
                            .prixParNuit(new BigDecimal("320.00")).capacite(4)
                            .description("Suite familiale avec 2 chambres et cuisine équipée.")
                            .imageUrl("https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("401").type(Chambre.TypeChambre.DELUXE)
                            .prixParNuit(new BigDecimal("450.00")).capacite(2)
                            .description("Chambre Deluxe avec terrasse privée et service en chambre 24h/24.")
                            .imageUrl("https://images.unsplash.com/photo-1631049552057-403cdb8f0658?w=600")
                            .disponible(true).build(),
                    Chambre.builder().numero("501").type(Chambre.TypeChambre.PENTHOUSE)
                            .prixParNuit(new BigDecimal("800.00")).capacite(6)
                            .description("Penthouse exclusif sur 2 niveaux avec piscine privée et butler.")
                            .imageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600")
                            .disponible(true).build()
            );
            chambreRepository.saveAll(chambres);
            System.out.println("✅ Chambres de démonstration créées");
        }

        if (clientRepository.count() == 0) {
            Client admin = Client.builder()
                    .email("admin@hotel.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nom("Admin")
                    .prenom("Hôtel")
                    .telephone("0600000000")
                    .role(Client.Role.ADMIN)
                    .build();

            Client client = Client.builder()
                    .email("client@hotel.com")
                    .password(passwordEncoder.encode("client123"))
                    .nom("Dupont")
                    .prenom("Jean")
                    .telephone("0612345678")
                    .role(Client.Role.CLIENT)
                    .build();

            clientRepository.saveAll(List.of(admin, client));
            System.out.println("✅ Comptes de démonstration créés");
            System.out.println("   Admin: admin@hotel.com / admin123");
            System.out.println("   Client: client@hotel.com / client123");
        }
    }
}