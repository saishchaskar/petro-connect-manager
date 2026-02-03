package com.deccan.petroconnect.services;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.entities.NozzleReading;
import com.deccan.petroconnect.repositories.DsrEntryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void saveDsrEntry(LocalDate date, String shift, Map<String, Object> data) {
        try {
            // Convert the entire frontend object to a JSON string
            String jsonContent = objectMapper.writeValueAsString(data);
            
            DsrEntry entry = dsrEntryRepository.findByDateAndShiftType(date, shift)
                    .orElse(new DsrEntry());
            
            entry.setDate(date);
            entry.setShiftType(shift);
            entry.setJsonData(jsonContent); 
            
            dsrEntryRepository.save(entry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing shift data", e);
        }
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