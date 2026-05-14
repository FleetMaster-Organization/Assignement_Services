package com.services.assignement.model;

import com.services.assignement.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String plate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Column(nullable = false)
    private Double currentKm;

    @Column(nullable = false)
    private LocalDateTime soatExpiryDate;

    @Column(nullable = false)
    private LocalDateTime technoExpiryDate;
}
