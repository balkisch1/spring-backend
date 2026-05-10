package com.example.hotel.controller;

import com.example.hotel.entity.Reservation;
import com.example.hotel.service.ReservationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Data static class HotelRequest { private Long clientId; private List<LigneHotelDTO> lignes; private String commentaire; }
    @Data static class LigneHotelDTO { private Long chambreId; private String dateArrivee; private String dateDepart; }
    @Data static class TransportRequest { private Long clientId; private List<LigneTransportDTO> lignes; private String commentaire; }
    @Data static class LigneTransportDTO { private Long trajetId; private int nombrePlaces; }

    @PostMapping("/hotel")
    public ResponseEntity<Reservation> creerHotel(@RequestBody HotelRequest req) {
        List<ReservationService.LigneHotelRequest> lignes = req.getLignes().stream()
                .map(l -> new ReservationService.LigneHotelRequest(l.getChambreId(), LocalDate.parse(l.getDateArrivee()), LocalDate.parse(l.getDateDepart())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationService.creerReservationHotel(req.getClientId(), lignes, req.getCommentaire()));
    }

    @PostMapping("/transport")
    public ResponseEntity<Reservation> creerTransport(@RequestBody TransportRequest req) {
        List<ReservationService.LigneTransportRequest> lignes = req.getLignes().stream()
                .map(l -> new ReservationService.LigneTransportRequest(l.getTrajetId(), l.getNombrePlaces()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservationService.creerReservationTransport(req.getClientId(), lignes, req.getCommentaire()));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Reservation>> getReservationsClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.getReservationsClient(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @PutMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Reservation> confirmer(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmerReservation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Reservation> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.annulerReservation(id));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Reservation>> getAllReservations(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reservationService.getAllReservations(PageRequest.of(page, size)));
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getDashboard() {
        return ResponseEntity.ok(reservationService.getDashboardStats());
    }
}