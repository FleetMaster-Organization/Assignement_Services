package com.services.assignement.controller;

import com.services.assignement.dto.*;
import com.services.assignement.service.AssignmentService;
import com.services.assignement.service.ExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final ExportService exportService;

    @PostMapping("/asignaciones")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(@Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.createAssignment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Asignación creada exitosamente"));
    }

    @PatchMapping("/asignaciones/{id}/cerrar")
    public ResponseEntity<ApiResponse<AssignmentResponse>> closeAssignment(
            @PathVariable UUID id,
            @Valid @RequestBody CloseAssignmentRequest request) {
        AssignmentResponse response = assignmentService.closeAssignment(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Asignación cerrada exitosamente"));
    }

    @GetMapping("/vehiculos/{id}/historial")
    public ResponseEntity<ApiResponse<List<HistoryItemDTO>>> getVehicleHistory(@PathVariable UUID id) {
        List<HistoryItemDTO> history = assignmentService.getVehicleHistory(id);
        return ResponseEntity.ok(ApiResponse.success(history, "Historial obtenido exitosamente"));
    }

    @GetMapping("/asignaciones/export")
    public ResponseEntity<byte[]> exportAssignments(@RequestParam(defaultValue = "CSV") String formato) throws IOException {
        byte[] data;
        String filename;
        MediaType mediaType;

        if ("XLSX".equalsIgnoreCase(formato)) {
            data = exportService.exportToXlsx();
            filename = "asignaciones.xlsx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            data = exportService.exportToCsv();
            filename = "asignaciones.csv";
            mediaType = MediaType.TEXT_PLAIN;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(data);
    }
}
