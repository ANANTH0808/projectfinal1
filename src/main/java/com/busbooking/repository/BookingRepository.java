package com.busbooking.repository;

import com.busbooking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.passenger JOIN FETCH b.bus WHERE LOWER(b.passenger.email) = LOWER(:email) AND b.status = 'CONFIRMED'")
    Iterable<Booking> findByPassengerEmail(@Param("email") String email);
    boolean existsByPassengerId(Long passengerId);

    void deleteByPassengerId(Long passengerId);

    boolean existsByBusId(Long busId);

    void deleteByBusId(Long busId);
}