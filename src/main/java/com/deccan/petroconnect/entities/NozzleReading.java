package com.deccan.petroconnect.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "nozzle_readings")
public class NozzleReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nozzle_id")
    private String nozzleId;

    @Column(name = "starting_reading")
    private Double startingReading;

    @Column(name = "ending_reading")
    private Double endingReading;

    // Constructors
    public NozzleReading() {
    }

    public NozzleReading(String nozzleId, Double startingReading, Double endingReading) {
        this.nozzleId = nozzleId;
        this.startingReading = startingReading;
        this.endingReading = endingReading;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNozzleId() {
        return nozzleId;
    }

    public void setNozzleId(String nozzleId) {
        this.nozzleId = nozzleId;
    }

    public Double getStartingReading() {
        return startingReading;
    }

    public void setStartingReading(Double startingReading) {
        this.startingReading = startingReading;
    }

    public Double getEndingReading() {
        return endingReading;
    }

    public void setEndingReading(Double endingReading) {
        this.endingReading = endingReading;
    }
}
