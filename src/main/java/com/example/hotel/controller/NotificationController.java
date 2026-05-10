package com.example.hotel.controller;

import com.example.hotel.entity.Notification;
import com.example.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long clientId) {
        return ResponseEntity.ok(notificationService.getNotificationsClient(clientId));
    }

    // GET /api/notifications/client/{clientId}/non-lues
    @GetMapping("/client/{clientId}/non-lues")
    public ResponseEntity<List<Notification>> getNonLues(@PathVariable Long clientId) {
        return ResponseEntity.ok(notificationService.getNonLues(clientId));
    }

    // GET /api/notifications/client/{clientId}/count
    @GetMapping("/client/{clientId}/count")
    public ResponseEntity<Map<String, Long>> countNonLues(@PathVariable Long clientId) {
        return ResponseEntity.ok(Map.of("count", notificationService.countNonLues(clientId)));
    }

    // PUT /api/notifications/{id}/lue
    @PutMapping("/{id}/lue")
    public ResponseEntity<Void> marquerLue(@PathVariable Long id) {
        notificationService.marquerLue(id);
        return ResponseEntity.ok().build();
    }

    // PUT /api/notifications/client/{clientId}/tout-lire
    @PutMapping("/client/{clientId}/tout-lire")
    public ResponseEntity<Void> marquerToutesLues(@PathVariable Long clientId) {
        notificationService.marquerToutesLues(clientId);
        return ResponseEntity.ok().build();
    }
}