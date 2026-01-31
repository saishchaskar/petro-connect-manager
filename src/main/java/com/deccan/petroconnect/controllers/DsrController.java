package com.deccan.petroconnect.controllers;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.services.DsrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dsr")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DsrController {
    
    @Autowired
    private DsrService dsrService;

    @PostMapping
    public ResponseEntity<?> createEntry(@RequestBody DsrEntry entry) {
        try {
            DsrEntry saved = dsrService.saveEntry(entry);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, String>() {{
                put("error", e.getMessage());
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to save DSR entry: " + e.getMessage());
            }});
        }
    }

    @GetMapping
    public ResponseEntity<List<DsrEntry>> getEntries() {
        return ResponseEntity.ok(dsrService.getAllEntries());
    }
}