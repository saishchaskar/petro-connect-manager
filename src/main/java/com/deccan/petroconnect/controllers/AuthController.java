package com.deccan.petroconnect.controllers;

import com.deccan.petroconnect.dto.LoginRequest;
import com.deccan.petroconnect.dto.RegisterRequest;
import com.deccan.petroconnect.entity.User;
import com.deccan.petroconnect.repository.UserRepository;
import com.deccan.petroconnect.entity.PetrolStation;
import com.deccan.petroconnect.repository.PetrolStationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate; // Import

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetrolStationRepository petrolStationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerStationAndUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // Check if user already exists
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, String>() {{
                    put("error", "Username already exists");
                }});
            }

            // 1. Create and save the Petrol Station
            PetrolStation station = new PetrolStation();
            station.setStationName(registerRequest.getStationName());
            station.setStationCode(registerRequest.getStationCode());
            station.setDealerName(registerRequest.getDealerName());
            station.setContactNumber(registerRequest.getContactNumber());
            station.setEmail(registerRequest.getEmail());
            station.setAddress(registerRequest.getAddress());
            station.setCreatedAt(java.time.LocalDate.now());
            PetrolStation savedStation = petrolStationRepository.save(station);

            // 2. Create and save the User
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setPetrolStation(savedStation);
            userRepository.save(user);

            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("message", "Registration successful!");
            }});
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new HashMap<String, String>() {{
                put("error", e.getMessage());
            }});
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            System.out.println("Login attempt for user: " + loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("Authentication successful for: " + loginRequest.getUsername());

            // Create a simple auth token
            String authToken = UUID.randomUUID().toString();
            session.setAttribute("auth_token", authToken);
            session.setAttribute("username", loginRequest.getUsername());

            User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
            boolean isConfigured = user.getPetrolStation() != null;

            Map<String, Object> response = new HashMap<>();
            response.put("token", authToken);
            response.put("username", loginRequest.getUsername());
            response.put("station_configured", isConfigured);

            if (isConfigured && user.getPetrolStation().getCreatedAt() != null) {
                response.put("station_created_at", user.getPetrolStation().getCreatedAt().toString());
            } else {
                response.put("station_created_at", LocalDate.now().toString());

            }

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials for user: " + loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new HashMap<String, String>() {{
                put("error", "Invalid username or password");
            }});
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Authentication failed: " + e.getMessage());
            }});
        }
    }
}