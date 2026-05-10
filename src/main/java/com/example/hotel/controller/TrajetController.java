package com.example.hotel.controller;

import com.example.hotel.entity.Trajet;
import com.example.hotel.service.TrajetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    // GET /api/trajets?villeDepart=Paris&villeArrivee=Lyon&dateDepart=...&type=AVION
    @GetMapping
    public ResponseEntity<Page<Trajet>> searchTrajets(
            @RequestParam(required = false) String villeDepart,
            @RequestParam(required = false) String villeArrivee,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDepart,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(
                trajetService.searchTrajets(villeDepart, villeArrivee, dateDepart, type,
                        PageRequest.of(page, size))
        );
    }

    // GET /api/trajets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Trajet> getTrajet(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.getTrajetById(id));
    }

    // POST /api/trajets/admin - ADMIN ONLY
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Trajet> createTrajet(@RequestBody Trajet trajet) {
        return ResponseEntity.ok(trajetService.createTrajet(trajet));
    }

    // PUT /api/trajets/admin/{id} - ADMIN ONLY
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Trajet> updateTrajet(@PathVariable Long id, @RequestBody Trajet trajet) {
        return ResponseEntity.ok(trajetService.updateTrajet(id, trajet));
    }

    // DELETE /api/trajets/admin/{id} - ADMIN ONLY
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrajet(@PathVariable Long id) {
        trajetService.deleteTrajet(id);
        return ResponseEntity.noContent().build();
    }
}