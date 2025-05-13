package com.busbooking.controller;

import com.busbooking.model.Bus;
import com.busbooking.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/buses")
public class BusController {

    private static final Logger logger = LoggerFactory.getLogger(BusController.class);

    @Autowired
    private BusService busService;

    @GetMapping
    public String listBuses(Model model) {
        model.addAttribute("buses", busService.findAll());
        return "buses";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("bus", new Bus());
        return "bus-form";
    }

    @PostMapping("/add")
    public String addBus(@ModelAttribute Bus bus, RedirectAttributes redirectAttributes) {
        try {
            busService.save(bus);
            redirectAttributes.addFlashAttribute("message", "Bus added successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/buses/add";
        }
        return "redirect:/buses";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return busService.findById(id)
                .map(bus -> {
                    model.addAttribute("bus", bus);
                    return "bus-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Bus not found");
                    return "redirect:/buses";
                });
    }

    @PostMapping("/edit")
    public String editBus(@ModelAttribute Bus bus, RedirectAttributes redirectAttributes) {
        try {
            busService.save(bus);
            redirectAttributes.addFlashAttribute("message", "Bus updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/buses/edit/" + bus.getId();
        }
        return "redirect:/buses";
    }

    @GetMapping("/delete/{id}")
    public String deleteBus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            busService.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Bus deleted successfully");
        } catch (IllegalStateException e) {
            logger.warn("Failed to delete bus with ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting bus with ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete bus due to an internal error");
        }
        return "redirect:/buses";
    }

    @GetMapping("/search")
    public String showSearchForm() {
        return "bus-search";
    }

    @PostMapping("/search")
    public String searchBuses(@RequestParam String source, @RequestParam String destination, Model model) {
        logger.debug("Searching buses from {} to {}", source, destination);
        try {
            Iterable<Bus> buses = busService.findBySourceAndDestination(source.trim(), destination.trim());
            model.addAttribute("buses", buses);
            if (!buses.iterator().hasNext()) {
                model.addAttribute("message", "No buses found for the specified route");
            }
        } catch (Exception e) {
            logger.error("Error searching buses: {}", e.getMessage());
            model.addAttribute("errorMessage", "Failed to search buses due to an internal error");
        }
        return "bus-search";
    }
}