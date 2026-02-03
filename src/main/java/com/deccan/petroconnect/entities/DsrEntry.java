package com.deccan.petroconnect.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "dsr_entry", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date", "shift_type"})
})
public class DsrEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "shift_type", nullable = false)
    private String shiftType;

    // We use "TEXT" or "LONGTEXT" to store the large JSON blob from the frontend
    @Lob
    @Column(name = "json_data", columnDefinition = "TEXT")
    private String jsonData;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}