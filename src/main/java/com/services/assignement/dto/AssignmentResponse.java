package com.services.assignement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssignmentResponse {
    private UUID id;
    private UUID vehicleId;
    private String vehiclePlate;
    private UUID driverId;
    private String driverName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double initialKm;
    private Double finalKm;
}
