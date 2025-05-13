package com.busbooking.controller;

import com.busbooking.model.Passenger;
import com.busbooking.service.PassengerService;
import com.busbooking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/passengers")
public class PassengerController {

    private static final Logger logger = LoggerFactory.getLogger(PassengerController.class);

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String listPassengers(Model model) {
        logger.debug("Fetching all passengers");
        try {
            model.addAttribute("passengers", passengerService.findAll());
        } catch (Exception e) {
            logger.error("Error fetching passengers: {}", e.getMessage());
            model.addAttribute("errorMessage", "Failed to load passengers due to an internal error");
        }
        return "passengers";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("passenger", new Passenger());
        return "passenger-form";
    }

    @PostMapping("/add")
    public String addPassenger(@ModelAttribute Passenger passenger, RedirectAttributes redirectAttributes) {
        logger.debug("Adding passenger: {}", passenger.getEmail());
        try {
            passengerService.save(passenger);
            redirectAttributes.addFlashAttribute("message", "Passenger added successfully");
        } catch (Exception e) {
            logger.error("Error adding passenger: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add passenger: " + e.getMessage());
            return "redirect:/passengers/add";
        }
        return "redirect:/passengers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        logger.debug("Fetching passenger with ID: {}", id);
        return passengerService.findById(id)
                .map(passenger -> {
                    model.addAttribute("passenger", passenger);
                    return "passenger-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Passenger not found");
                    return "redirect:/passengers";
                });
    }

    @PostMapping("/edit")
    public String editPassenger(@ModelAttribute Passenger passenger, RedirectAttributes redirectAttributes) {
        logger.debug("Updating passenger: {}", passenger.getEmail());
        try {
            passengerService.save(passenger);
            redirectAttributes.addFlashAttribute("message", "Passenger updated successfully");
        } catch (Exception e) {
            logger.error("Error updating passenger: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update passenger: " + e.getMessage());
            return "redirect:/passengers/edit/" + passenger.getId();
        }
        return "redirect:/passengers";
    }

    @GetMapping("/delete/{id}")
    public String deletePassenger(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.debug("Deleting passenger with ID: {}", id);
        try {
            if (bookingService.existsByPassengerId(id)) {
                throw new IllegalStateException("Cannot delete passenger with active bookings");
            }
            passengerService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Passenger deleted successfully");
        } catch (IllegalStateException e) {
            logger.warn("Deletion failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting passenger: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete passenger due to an internal error");
        }
        return "redirect:/passengers";
    }
}