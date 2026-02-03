package com.deccan.petroconnect.repositories;

import com.deccan.petroconnect.entities.DsrEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DsrEntryRepository extends JpaRepository<DsrEntry, Long> {
    Optional<DsrEntry> findByDateAndShiftType(LocalDate date, String shiftType);
    List<DsrEntry> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
}