package com.busbooking.service;

import com.busbooking.model.Bus;
import com.busbooking.repository.BusRepository;
import com.busbooking.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BusService {

    private static final Logger logger = LoggerFactory.getLogger(BusService.class);

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Iterable<Bus> findAll() {
        return busRepository.findAll();
    }

    public java.util.Optional<Bus> findById(Long id) {
        return busRepository.findById(id);
    }

    public void save(Bus bus) {
        if (bus.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        busRepository.save(bus);
    }

    @Transactional
    public void deleteById(Long id) {
        logger.debug("Attempting to delete bus with ID {}", id);
        try {
            bookingRepository.deleteByBusId(id);
            busRepository.deleteById(id);
            logger.info("Successfully deleted bus with ID {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete bus with ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    public Iterable<Bus> findBySourceAndDestination(String source, String destination) {
        return busRepository.findBySourceAndDestination(source, destination);
    }
}