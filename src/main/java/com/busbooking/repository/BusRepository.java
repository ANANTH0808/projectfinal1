package com.busbooking.repository;

import com.busbooking.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    @Query("SELECT b FROM Bus b WHERE LOWER(b.source) = LOWER(:source) AND LOWER(b.destination) = LOWER(:destination)")
    List<Bus> findBySourceAndDestination(String source, String destination);
}