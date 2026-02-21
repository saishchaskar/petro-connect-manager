package com.deccan.petroconnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "petrol_stations")
public class PetrolStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stationName;
    private String stationCode;
    private String dealerName;
    private String contactNumber;
    private String email;
    private String address;
    private LocalDate createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private String configuration;
}