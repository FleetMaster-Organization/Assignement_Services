package com.services.assignement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HistoryItemDTO {
    private String type; // "ASIGNACION" or "MANTENIMIENTO"
    private UUID id;
    private LocalDateTime date;
    private String description;
    private String conductor;
    private Double km;
}
