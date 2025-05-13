package com.busbooking.service;

import com.busbooking.model.Booking;
import com.busbooking.model.Bus;
import com.busbooking.model.Passenger;
import com.busbooking.repository.BookingRepository;
import com.busbooking.repository.BusRepository;
import com.busbooking.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Transactional(readOnly = true)
    public Iterable<Booking> findByUsername(String username) {
        logger.debug("Finding bookings for username: {}", username);
        try {
            Iterable<Booking> bookings = bookingRepository.findByPassengerEmail(username);
            bookings.forEach(booking -> logger.debug("Found booking ID: {} for email: {}, passenger: {}, bus: {}",
                    booking.getId(),
                    booking.getPassenger() != null ? booking.getPassenger().getEmail() : "null",
                    booking.getPassenger() != null ? booking.getPassenger().getName() : "null",
                    booking.getBus() != null ? booking.getBus().getSource() + " to " + booking.getBus().getDestination() : "null"));
            return bookings;
        } catch (Exception e) {
            logger.error("Error querying bookings for username: {}", username, e);
            throw e;
        }
    }

    @Transactional
    public void createBooking(Long busId, String name, String email, String phone) {
        String normalizedEmail = email.toLowerCase();
        logger.debug("Creating booking for bus ID: {}, passenger: {}, email: {}", busId, name, normalizedEmail);
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalStateException("Bus not found"));
        if (bus.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No seats available on this bus");
        }
        Passenger passenger = passengerRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    Passenger newPassenger = new Passenger();
                    newPassenger.setName(name);
                    newPassenger.setEmail(normalizedEmail);
                    newPassenger.setPhone(phone);
                    logger.debug("Creating new passenger with email: {}", normalizedEmail);
                    return passengerRepository.save(newPassenger);
                });
        Booking booking = new Booking();
        booking.setBus(bus);
        booking.setPassenger(passenger);
        booking.setBookingDate(LocalDateTime.now());
        bus.setAvailableSeats(bus.getAvailableSeats() - 1);
        busRepository.save(bus);
        bookingRepository.save(booking);
        logger.debug("Booking created with ID: {}", booking.getId());
    }

    @Transactional
    public void cancelBooking(Long id) {
        logger.debug("Cancelling booking ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
        Bus bus = booking.getBus();
        bus.setAvailableSeats(bus.getAvailableSeats() + 1);
        busRepository.save(bus);
        bookingRepository.deleteById(id);
        logger.debug("Booking ID: {} cancelled", id);
    }

    public boolean existsByPassengerId(Long passengerId) {
        logger.debug("Checking if bookings exist for passenger ID: {}", passengerId);
        return bookingRepository.existsByPassengerId(passengerId);
    }

    @Transactional
    public void deleteByPassengerId(Long passengerId) {
        logger.debug("Deleting bookings for passenger ID: {}", passengerId);
        bookingRepository.deleteByPassengerId(passengerId);
    }
}