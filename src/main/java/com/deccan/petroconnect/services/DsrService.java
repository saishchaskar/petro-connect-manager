package com.deccan.petroconnect.services;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.entities.NozzleReading;
import com.deccan.petroconnect.repositories.DsrEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.deccan.petroconnect.dtos.AnalyticsDTO;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDate;
import java.util.*;

@Service
public class DsrService {

    @Autowired
    private DsrEntryRepository dsrEntryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DsrEntry getDsrEntry(LocalDate date, String shift) {
        return dsrEntryRepository.findByDateAndShiftType(date, shift).orElse(null);
    }

    @Transactional // Ensure both saves happen or neither
    public void saveDsrEntry(LocalDate date, String shift, Map<String, Object> data) {
        try {
            // 1. SAVE THE CURRENT SHIFT
            String jsonContent = objectMapper.writeValueAsString(data);
            DsrEntry entry = dsrEntryRepository.findByDateAndShiftType(date, shift)
                    .orElse(new DsrEntry());
            
            entry.setDate(date);
            entry.setShiftType(shift);
            entry.setJsonData(jsonContent); 
            dsrEntryRepository.save(entry);

            // 2. AUTO-UPDATE NEXT SHIFT (The Fix)
            propagateReadingsToNextShift(date, shift, data);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing shift data", e);
        }
    }

    private void propagateReadingsToNextShift(LocalDate currentDate, String currentShift, Map<String, Object> currentData) {
        // Logic to find the immediate next shift
        LocalDate nextDate;
        String nextShift;

        if ("Day".equalsIgnoreCase(currentShift)) {
            nextDate = currentDate;
            nextShift = "Night";
        } else {
            nextDate = currentDate.plusDays(1);
            nextShift = "Day";
        }

        // Fetch the next shift if it exists
        Optional<DsrEntry> nextEntryOpt = dsrEntryRepository.findByDateAndShiftType(nextDate, nextShift);
        
        if (nextEntryOpt.isPresent()) {
            try {
                DsrEntry nextEntry = nextEntryOpt.get();
                Map<String, Object> nextData = objectMapper.readValue(nextEntry.getJsonData(), Map.class);
                
                // Get the nozzle lists
                List<Map<String, Object>> currentNozzles = (List<Map<String, Object>>) currentData.get("nozzles");
                List<Map<String, Object>> nextNozzles = (List<Map<String, Object>>) nextData.get("nozzles");

                if (currentNozzles != null && nextNozzles != null) {
                    boolean dataChanged = false;

                    // Create a lookup map for the NEW ending readings
                    Map<String, Double> newOpeningReadings = new HashMap<>();
                    for (Map<String, Object> nozzle : currentNozzles) {
                        newOpeningReadings.put((String) nozzle.get("nozzleId"), getDouble(nozzle, "endingReading"));
                    }

                    // Loop through NEXT shift's nozzles and update their STARTING reading
                    for (Map<String, Object> nextNozzle : nextNozzles) {
                        String id = (String) nextNozzle.get("nozzleId");
                        
                        if (newOpeningReadings.containsKey(id)) {
                            Double correctOpening = newOpeningReadings.get(id);
                            Double existingOpening = getDouble(nextNozzle, "startingReading");

                            // Only update and save if values are different (prevents unnecessary writes)
                            if (!Objects.equals(correctOpening, existingOpening)) {
                                nextNozzle.put("startingReading", correctOpening);
                                
                                // OPTIONAL: Automatically recalculate 'readingDiff' for the next shift
                                // so the math stays correct immediately
                                Double nextEnding = getDouble(nextNozzle, "endingReading");
                                if (nextEnding > 0) {
                                    nextNozzle.put("readingDiff", nextEnding - correctOpening);
                                    // You could also recalculate 'netSale' and 'amount' here if needed
                                }
                                
                                dataChanged = true;
                            }
                        }
                    }

                    // Save the next shift if we made changes
                    if (dataChanged) {
                        nextEntry.setJsonData(objectMapper.writeValueAsString(nextData));
                        dsrEntryRepository.save(nextEntry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Log error but allow the main save to succeed
            }
        }
    }

    public List<AnalyticsDTO> getAnalyticsData(LocalDate start, LocalDate end) {
        return dsrEntryRepository.getAnalyticsBetweenDates(start, end);
    }

    public List<Map<String, Object>> getMonthlyReport(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        
        List<DsrEntry> entries = dsrEntryRepository.findByDateBetweenOrderByDateAsc(start, end);
        
        // Use a map to aggregate data for each day.
        Map<LocalDate, Map<String, Object>> dailyData = new LinkedHashMap<>();

        for (DsrEntry entry : entries) {
            try {
                if (entry.getJsonData() == null) continue;

                Map<String, Object> data = objectMapper.readValue(entry.getJsonData(), Map.class);
                LocalDate entryDate = entry.getDate();
                List<Map<String, Object>> nozzles = (List<Map<String, Object>>) data.get("nozzles");
                
                // Initialize row if date doesn't exist
                dailyData.putIfAbsent(entryDate, new HashMap<>() {{
                    put("date", entry.getDate().toString());
                    put("petrolVolume", 0.0);
                    put("petrolAmount", 0.0);
                    put("dieselVolume", 0.0);
                    put("dieselAmount", 0.0);
                }});

                Map<String, Object> dayRow = dailyData.get(entryDate);

                // Sum up volumes and amounts
                if (nozzles != null) {
                    for (Map<String, Object> n : nozzles) {
                        String type = (String) n.get("productType");
                        double netSale = getDouble(n, "netSale");
                        double amount = getDouble(n, "amount");

                        if ("Petrol".equalsIgnoreCase(type)) {
                            dayRow.put("petrolVolume", (double) dayRow.get("petrolVolume") + netSale);
                            dayRow.put("petrolAmount", (double) dayRow.get("petrolAmount") + amount);
                        } else if ("Diesel".equalsIgnoreCase(type)) {
                            dayRow.put("dieselVolume", (double) dayRow.get("dieselVolume") + netSale);
                            dayRow.put("dieselAmount", (double) dayRow.get("dieselAmount") + amount);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Calculate Rates and Final Totals
        List<Map<String, Object>> report = new ArrayList<>(dailyData.values());
        for (Map<String, Object> row : report) {
            double pVol = (double) row.get("petrolVolume");
            double pAmt = (double) row.get("petrolAmount");
            double dVol = (double) row.get("dieselVolume");
            double dAmt = (double) row.get("dieselAmount");

            row.put("petrolRate", pVol > 0 ? pAmt / pVol : 0.0);
            row.put("dieselRate", dVol > 0 ? dAmt / dVol : 0.0);
            row.put("totalAmount", pAmt + dAmt);
        }

        return report;
    }

    public List<NozzleReading> getOpeningReadingsForNewShift(LocalDate date, String shiftType) {
        // Logic: If current is Night, get Day. If Day, get yesterday's Evening.
        LocalDate prevDate = date;
        String prevShift;
        
        if ("Night".equalsIgnoreCase(shiftType)) {
            prevShift = "Day";
        } else { // For "Day" shift
            prevDate = date.minusDays(1);
            prevShift = "Evening"; // Assuming Evening is the last shift
        }

        // Find previous entry
        Optional<DsrEntry> prevEntry = dsrEntryRepository.findByDateAndShiftType(prevDate, prevShift);
        
        if (prevEntry.isPresent()) {
            try {
                String jsonData = prevEntry.get().getJsonData();
                if (jsonData != null) {
                    Map<String, Object> data = objectMapper.readValue(jsonData, Map.class);
                    List<Map<String, Object>> nozzles = (List<Map<String, Object>>) data.get("nozzles");
                    
                    if (nozzles != null) {
                        return nozzles.stream().map(nozzleData -> {
                            NozzleReading newNozzle = new NozzleReading();
                            newNozzle.setNozzleId((String) nozzleData.get("nozzleId"));
                            newNozzle.setStartingReading(getDouble(nozzleData, "endingReading"));
                            return newNozzle;
                        }).collect(java.util.stream.Collectors.toList());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>(); // Return empty or default 0 if no history
    }

    public boolean exists(LocalDate date, String shift) {
        return dsrEntryRepository.findByDateAndShiftType(date, shift).isPresent();
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0.0;
    }
}