package com.example.hotel.controller;

import com.example.hotel.entity.Chambre;
import com.example.hotel.service.ChambreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/chambres")
@RequiredArgsConstructor
public class ChambreController {

    private final ChambreService chambreService;

    // GET /api/chambres?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<Chambre>> getAllChambres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(chambreService.getAllChambres(PageRequest.of(page, size)));
    }

    // GET /api/chambres/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Chambre> getChambre(@PathVariable Long id) {
        return ResponseEntity.ok(chambreService.getChambreById(id));
    }

    // GET /api/chambres/disponibles?dateArrivee=...&dateDepart=...
    @GetMapping("/disponibles")
    public ResponseEntity<List<Chambre>> getDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateArrivee,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart) {
        return ResponseEntity.ok(chambreService.getChambresDisponibles(dateArrivee, dateDepart));
    }

    // GET /api/chambres/search?dateArrivee=...&dateDepart=...&type=...&prixMax=...
    @GetMapping("/search")
    public ResponseEntity<Page<Chambre>> searchChambres(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateArrivee,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Integer capacite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                chambreService.searchChambres(dateArrivee, dateDepart, type, prixMax, capacite,
                        PageRequest.of(page, size))
        );
    }

    // POST /api/admin/chambres - ADMIN ONLY
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Chambre> createChambre(@RequestBody Chambre chambre) {
        return ResponseEntity.ok(chambreService.createChambre(chambre));
    }

    // PUT /api/admin/chambres/{id} - ADMIN ONLY
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Chambre> updateChambre(@PathVariable Long id, @RequestBody Chambre chambre) {
        return ResponseEntity.ok(chambreService.updateChambre(id, chambre));
    }

    // DELETE /api/admin/chambres/{id} - ADMIN ONLY
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteChambre(@PathVariable Long id) {
        chambreService.deleteChambre(id);
        return ResponseEntity.noContent().build();
    }
}