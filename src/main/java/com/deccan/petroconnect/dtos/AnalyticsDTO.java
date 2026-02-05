// src/main/java/com/deccan/petroconnect/dtos/AnalyticsDTO.java
package com.deccan.petroconnect.dtos;

import java.time.LocalDate;

public interface AnalyticsDTO {
    LocalDate getDate();
    Double getPetrolVolume(); // Litres
    Double getPetrolAmount(); // Revenue
    Double getDieselVolume();
    Double getDieselAmount();
    Double getTotalRevenue();
}