package com.deccan.petroconnect.services;

import com.deccan.petroconnect.entities.DsrEntry;
import com.deccan.petroconnect.repositories.DsrEntryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class DsrService {

    @Autowired
    private DsrEntryRepository repository;

    public DsrEntry saveEntry(@Valid DsrEntry entry) {
        // If an entry for the date already exists, update it instead of creating a new one.
        Optional<DsrEntry> existing = repository.findByDate(entry.getDate());
        if (existing.isPresent()) {
            DsrEntry existingEntry = existing.get();
            // Preserve the ID to ensure we update the existing record
            entry.setId(existingEntry.getId());
            // Preserve the original creation timestamp
            entry.setCreatedAt(existingEntry.getCreatedAt());
        }
        return repository.save(entry);
    }

    public List<DsrEntry> getAllEntries() {
        return repository.findAll();
    }

    public Optional<DsrEntry> getEntryByDate(LocalDate date) {
        return repository.findByDate(date);
    }
}