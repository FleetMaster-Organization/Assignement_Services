package com.services.assignement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignmentRequest {

    @NotNull(message = "El ID del vehículo es obligatorio")
    private UUID vehicleId;

    @NotNull(message = "El ID del conductor es obligatorio")
    private UUID driverId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID userId;
}
