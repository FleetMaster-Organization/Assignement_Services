package com.services.assignement.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;
    
    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;
    
    @Column(name = "driver_name", nullable = false)
    private String driverName;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private Double initialKm;

    private Double finalKm;
}
