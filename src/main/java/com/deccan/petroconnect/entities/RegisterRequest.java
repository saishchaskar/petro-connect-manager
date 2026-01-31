package com.deccan.petroconnect.dto;

import lombok.Data;

// This DTO combines fields from both the station and the admin user for a single registration call.
@Data
public class RegisterRequest {
    // Station Details
    private String stationName;
    private String stationCode;
    private String dealerName;
    private String contactNumber;
    private String email;
    private String address;

    // Admin Account
    private String username;
    private String password;
}