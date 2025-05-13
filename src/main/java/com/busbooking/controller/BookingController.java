package com.busbooking.controller;

import com.busbooking.model.Booking;
import com.busbooking.model.Passenger;
import com.busbooking.service.BookingService;
import com.busbooking.service.BusService;
import com.busbooking.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BusService busService;

    @Autowired
    private PassengerService passengerService;

    @GetMapping
    public String listBookings(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, Model model) {
        if (user == null) {
            logger.warn("No authenticated user found");
            model.addAttribute("errorMessage", "Please log in to view bookings");
            model.addAttribute("bookings", Collections.emptyList());
            return "bookings";
        }
        logger.debug("Fetching bookings for user: {}", user.getUsername());
        try {
            Iterable<Booking> bookings = bookingService.findByUsername(user.getUsername().toLowerCase());
            model.addAttribute("bookings", bookings);
            bookings.forEach(booking -> {
                logger.debug("Booking ID: {}, Passenger: {}, Bus: {} to {}",
                        booking.getId(),
                        booking.getPassenger() != null ? booking.getPassenger().getEmail() : "null",
                        booking.getBus() != null ? booking.getBus().getSource() : "null",
                        booking.getBus() != null ? booking.getBus().getDestination() : "null");
            });
        } catch (org.hibernate.LazyInitializationException e) {
            logger.error("Lazy loading error fetching bookings: {}", e.getMessage());
            model.addAttribute("errorMessage", "Failed to load bookings due to a data access issue");
            model.addAttribute("bookings", Collections.emptyList());
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("Database error fetching bookings: {}", e.getMessage());
            model.addAttribute("errorMessage", "Failed to load bookings due to a database issue");
            model.addAttribute("bookings", Collections.emptyList());
        } catch (Exception e) {
            logger.error("Unexpected error fetching bookings: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Failed to load bookings due to an internal error");
            model.addAttribute("bookings", Collections.emptyList());
        }
        return "bookings";
    }

    @GetMapping("/book/{busId}")
    public String showBookForm(@PathVariable Long busId, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Showing booking form for bus ID: {}", busId);
        if (user == null) {
            logger.warn("No authenticated user found for booking form");
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to book a bus");
            return "redirect:/login";
        }
        logger.debug("Authenticated user: {}", user.getUsername());
        return busService.findById(busId)
                .map(bus -> {
                    // Validate bus data
                    boolean isValidPrice = (bus.getPrice() != null && bus.getPrice() >= 0) || (bus.getPrice() == null);
                    boolean isValidSeats = (bus.getAvailableSeats() != null && bus.getAvailableSeats() >= 0) || (bus.getAvailableSeats() == null);
                    if (bus.getSource() == null || bus.getDestination() == null || !isValidPrice || !isValidSeats) {
                        logger.warn("Invalid bus data: ID {}, Source: {}, Destination: {}, Price: {}, Seats: {}",
                                bus.getId(), bus.getSource(), bus.getDestination(), bus.getPrice(), bus.getAvailableSeats());
                        redirectAttributes.addFlashAttribute("errorMessage", "Invalid bus data");
                        return "redirect:/buses/search";
                    }
                    logger.debug("Bus found: ID {}, Route: {} to {}, Price: {}, Seats: {}",
                            bus.getId(), bus.getSource(), bus.getDestination(), bus.getPrice(), bus.getAvailableSeats());
                    model.addAttribute("bus", bus);
                    passengerService.findByEmail(user.getUsername().toLowerCase()).ifPresent(passenger -> {
                        logger.debug("Passenger found: {} ({})", passenger.getName(), passenger.getEmail());
                        model.addAttribute("passenger", passenger);
                    });
                    if (!model.containsAttribute("passenger")) {
                        logger.debug("No passenger found for email: {}", user.getUsername());
                        model.addAttribute("passenger", new Passenger());
                    }
                    return "book";
                })
                .orElseGet(() -> {
                    logger.warn("Bus not found: {}", busId);
                    redirectAttributes.addFlashAttribute("errorMessage", "Bus not found");
                    return "redirect:/buses/search";
                });
    }

    @PostMapping("/book")
    public String bookBus(@RequestParam Long busId, @RequestParam String name, @RequestParam String email, @RequestParam String phone, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user, RedirectAttributes redirectAttributes) {
        logger.debug("Processing booking for bus ID: {}, passenger: {}", busId, name);
        if (user == null) {
            logger.warn("No authenticated user found for booking");
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to book a bus");
            return "redirect:/login";
        }
        try {
            String bookingEmail = user.getUsername().toLowerCase(); // Enforce authenticated user's email
            logger.debug("Using email for booking: {}", bookingEmail);
            bookingService.createBooking(busId, name, bookingEmail, phone);
            redirectAttributes.addFlashAttribute("message", "Booking confirmed successfully");
            return "redirect:/bookings";
        } catch (IllegalStateException e) {
            logger.warn("Booking failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/book/" + busId;
        } catch (Exception e) {
            logger.error("Unexpected error during booking: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to book due to an internal error");
            return "redirect:/bookings/book/" + busId;
        }
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.debug("Cancelling booking ID: {}", id);
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("message", "Booking cancelled successfully");
        } catch (IllegalStateException e) {
            logger.warn("Cancellation failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error cancelling booking: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel booking due to an internal error");
        }
        return "redirect:/bookings";
    }
}