package com.deccan.petroconnect.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "dsr_entry")
public class DsrEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(precision = 12, scale = 3)
    private BigDecimal startingReading;
    
    @Column(precision = 12, scale = 3)
    private BigDecimal endingReading;
    
    @Column(precision = 12, scale = 3)
    private BigDecimal sales;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}