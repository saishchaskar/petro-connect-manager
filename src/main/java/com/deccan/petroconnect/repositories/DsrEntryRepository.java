package com.deccan.petroconnect.repositories;

import com.deccan.petroconnect.entities.DsrEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DsrEntryRepository extends JpaRepository<DsrEntry, Long> {
    Optional<DsrEntry> findByDate(LocalDate date);
}