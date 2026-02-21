package com.deccan.petroconnect.controllers;

import com.deccan.petroconnect.dto.UpdateConfigRequest;
import com.deccan.petroconnect.entity.PetrolStation;
import com.deccan.petroconnect.services.PetrolStationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/station")
public class PetrolStationController {

    @Autowired
    private PetrolStationService petrolStationService;

    @GetMapping("/config")
    public ResponseEntity<PetrolStation> getStationConfig(@RequestParam("username") String username) {
        try {
            PetrolStation station = petrolStationService.getStationForUser(username);
            return ResponseEntity.ok(station);
        } catch (Exception e) {
            // If user or station not found, this will be caught.
            // The frontend expects a 404 if not configured.
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateStationConfig(@RequestBody UpdateConfigRequest request, @RequestParam("username") String username) {
        try {
            petrolStationService.updateStationConfiguration(username, request.getConfiguration());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Configuration updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update configuration: " + e.getMessage()));
        }
    }
}