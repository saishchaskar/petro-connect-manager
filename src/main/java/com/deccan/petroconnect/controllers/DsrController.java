package com.deccan.petroconnect.controllers;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.services.DsrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.deccan.petroconnect.dtos.AnalyticsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dsr")
public class DsrController {

    @Autowired
    private DsrService dsrService;

    @GetMapping
    public ResponseEntity<?> getDsrShift(
            @RequestParam("date") String date,
            @RequestParam("shift") String shift) {
        DsrEntry entry = dsrService.getDsrEntry(LocalDate.parse(date), shift);
         
        if (entry == null) {
            // FIX: Return 200 OK with null body instead of 404
            return ResponseEntity.ok(null);
        }
        // Return the JSON content directly as it matches the frontend DsrShift interface
        return ResponseEntity.ok(entry.getJsonData());
    }

    @PostMapping
    public ResponseEntity<?> saveDsrShift(@RequestBody Map<String, Object> shiftData) {
        // The frontend sends the whole object. We extract date/shift for indexing
        String date = (String) shiftData.get("date");
        String shift = (String) shiftData.get("shiftType");
        
        dsrService.saveDsrEntry(LocalDate.parse(date), shift, shiftData);
        return ResponseEntity.ok().body("{\"message\": \"Saved successfully\"}");
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkShiftExists(
            @RequestParam("date") String date,
            @RequestParam("shift") String shift) {
        boolean exists = dsrService.exists(LocalDate.parse(date), shift);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/consolidated")
    public ResponseEntity<List<Map<String, Object>>> getConsolidatedReport(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        return ResponseEntity.ok(dsrService.getMonthlyReport(month, year));
    }

    @GetMapping("/analytics")
    public ResponseEntity<List<AnalyticsDTO>> getAnalytics(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(dsrService.getAnalyticsData(LocalDate.parse(startDate), LocalDate.parse(endDate)));
    }
        
    
}