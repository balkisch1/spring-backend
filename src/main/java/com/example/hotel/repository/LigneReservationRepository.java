package com.example.hotel.repository;

import com.example.hotel.entity.LigneReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneReservationRepository extends JpaRepository<LigneReservation, Long> {
}