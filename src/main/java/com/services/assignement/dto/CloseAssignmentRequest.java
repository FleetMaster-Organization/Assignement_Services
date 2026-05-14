package com.services.assignement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.UUID;

@Data
public class CloseAssignmentRequest {

    @NotNull(message = "El kilometraje final es obligatorio")
    @PositiveOrZero(message = "El kilometraje debe ser positivo")
    private Double finalKm;

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID userId;
}
