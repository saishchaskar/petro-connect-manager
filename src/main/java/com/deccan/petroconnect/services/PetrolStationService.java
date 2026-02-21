package com.deccan.petroconnect.services;

import com.deccan.petroconnect.entity.PetrolStation;
import com.deccan.petroconnect.entity.User;
import com.deccan.petroconnect.repository.PetrolStationRepository;
import com.deccan.petroconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetrolStationService {

    @Autowired
    private PetrolStationRepository petrolStationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public PetrolStation getStationForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        PetrolStation station = user.getPetrolStation();
        if (station == null) {
            // This case should not happen for a logged-in user post-registration.
            throw new RuntimeException("No petrol station associated with user: " + username);
        }
        return station;
    }

    @Transactional
    public PetrolStation updateStationConfiguration(String username, String configurationJson) {
        PetrolStation station = getStationForUser(username);
        station.setConfiguration(configurationJson);
        return petrolStationRepository.save(station);
    }
}