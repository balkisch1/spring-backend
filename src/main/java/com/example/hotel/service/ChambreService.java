package com.example.hotel.service;

import com.example.hotel.entity.Chambre;
import com.example.hotel.repository.ChambreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChambreService {

    private final ChambreRepository chambreRepository;

    public Page<Chambre> getAllChambres(Pageable pageable) {
        return chambreRepository.findAll(pageable);
    }

    public Chambre getChambreById(Long id) {
        return chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée : " + id));
    }

    public List<Chambre> getChambresDisponibles(LocalDate dateArrivee, LocalDate dateDepart) {
        return chambreRepository.findChambresDisponibles(dateArrivee, dateDepart);
    }

    public Page<Chambre> searchChambres(LocalDate dateArrivee, LocalDate dateDepart,
                                        String type, BigDecimal prixMax,
                                        Integer capacite, Pageable pageable) {
        Chambre.TypeChambre typeChambre = null;
        if (type != null && !type.isEmpty()) {
            typeChambre = Chambre.TypeChambre.valueOf(type.toUpperCase());
        }
        return chambreRepository.findChambresDisponiblesFiltered(
                dateArrivee, dateDepart, typeChambre, prixMax, capacite, pageable
        );
    }

    @Transactional
    public Chambre createChambre(Chambre chambre) {
        return chambreRepository.save(chambre);
    }

    @Transactional
    public Chambre updateChambre(Long id, Chambre updated) {
        Chambre chambre = getChambreById(id);
        chambre.setNumero(updated.getNumero());
        chambre.setType(updated.getType());
        chambre.setPrixParNuit(updated.getPrixParNuit());
        chambre.setCapacite(updated.getCapacite());
        chambre.setDescription(updated.getDescription());
        chambre.setImageUrl(updated.getImageUrl());
        chambre.setDisponible(updated.getDisponible());
        return chambreRepository.save(chambre);
    }

    @Transactional
    public void deleteChambre(Long id) {
        chambreRepository.deleteById(id);
    }
}