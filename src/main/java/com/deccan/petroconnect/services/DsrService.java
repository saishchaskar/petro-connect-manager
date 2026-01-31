package com.deccan.petroconnect.services;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.repositories.DsrEntryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class DsrService {

    @Autowired
    private DsrEntryRepository repository;

    public DsrEntry saveEntry(@Valid DsrEntry entry) {
        // Business invariant: ending >= starting
        // Use compareTo() for BigDecimal comparison (< returns negative, == returns 0, > returns positive)
        if (entry.getEndingReading() != null && entry.getStartingReading() != null &&
            entry.getEndingReading().compareTo(entry.getStartingReading()) < 0) {
            throw new IllegalArgumentException("Ending reading must be >= starting reading");
        }
        return repository.save(entry);
    }

    public List<DsrEntry> getAllEntries() {
        return repository.findAll();
    }
}