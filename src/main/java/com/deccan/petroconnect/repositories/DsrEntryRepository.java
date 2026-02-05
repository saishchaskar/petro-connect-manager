// src/main/java/com/deccan/petroconnect/repositories/DsrEntryRepository.java
package com.deccan.petroconnect.repositories;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.dtos.AnalyticsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DsrEntryRepository extends JpaRepository<DsrEntry, Long> {
    Optional<DsrEntry> findByDateAndShiftType(LocalDate date, String shiftType);
    List<DsrEntry> findByDateBetweenOrderByDateAsc(LocalDate start, LocalDate end);

    // --- ANALYTICS QUERY ---
    // This extracts Nozzles from JSON, filters by Product Type, and Sums NetSale (Volume) and Amount
    @Query(value = """
        SELECT 
            d.date as date,
            SUM(CASE WHEN nozzle->>'productType' = 'Petrol' THEN CAST(nozzle->>'netSale' AS DOUBLE PRECISION) ELSE 0 END) as petrolVolume,
            SUM(CASE WHEN nozzle->>'productType' = 'Petrol' THEN CAST(nozzle->>'amount' AS DOUBLE PRECISION) ELSE 0 END) as petrolAmount,
            SUM(CASE WHEN nozzle->>'productType' = 'Diesel' THEN CAST(nozzle->>'netSale' AS DOUBLE PRECISION) ELSE 0 END) as dieselVolume,
            SUM(CASE WHEN nozzle->>'productType' = 'Diesel' THEN CAST(nozzle->>'amount' AS DOUBLE PRECISION) ELSE 0 END) as dieselAmount,
            SUM(CAST(nozzle->>'amount' AS DOUBLE PRECISION)) as totalRevenue
        FROM dsr_entry d
        CROSS JOIN LATERAL jsonb_array_elements(d.json_data->'nozzles') as nozzle
        WHERE d.date BETWEEN :startDate AND :endDate
        GROUP BY d.date
        ORDER BY d.date ASC
    """, nativeQuery = true)
    List<AnalyticsDTO> getAnalyticsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}