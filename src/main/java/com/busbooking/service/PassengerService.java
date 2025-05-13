package com.busbooking.service;

import com.busbooking.model.Passenger;
import com.busbooking.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class PassengerService {

    private static final Logger logger = LoggerFactory.getLogger(PassengerService.class);

    @Autowired
    private PassengerRepository passengerRepository;

    public Iterable<Passenger> findAll() {
        logger.debug("Fetching all passengers");
        return passengerRepository.findAll();
    }

    public Optional<Passenger> findById(Long id) {
        logger.debug("Finding passenger by ID: {}", id);
        return passengerRepository.findById(id);
    }

    public Optional<Passenger> findByEmail(String email) {
        logger.debug("Finding passenger by email: {}", email);
        return passengerRepository.findByEmail(email);
    }

    public Passenger save(Passenger passenger) {
        logger.debug("Saving passenger: {}", passenger.getEmail());
        return passengerRepository.save(passenger);
    }

    public void deleteById(Long id) {
        logger.debug("Deleting passenger with ID: {}", id);
        passengerRepository.deleteById(id);
    }
}