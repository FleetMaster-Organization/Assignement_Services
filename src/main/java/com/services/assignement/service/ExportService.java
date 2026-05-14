package com.services.assignement.service;

import com.opencsv.CSVWriter;
import com.services.assignement.model.AssignmentEntity;
import com.services.assignement.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportService {

    private final AssignmentRepository assignmentRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportToCsv() throws IOException {
        List<AssignmentEntity> assignments = assignmentRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            String[] header = {"ID Asignacion", "Placa Vehiculo", "Nombre Conductor", "Fecha Inicio", "Fecha Fin", "Km Inicial", "Km Final", "Duracion (Horas)"};
            writer.writeNext(header);

            for (AssignmentEntity a : assignments) {
                String duration = a.getEndDate() != null ? 
                        String.valueOf(Duration.between(a.getStartDate(), a.getEndDate()).toHours()) : "N/A";
                
                String[] data = {
                    a.getId().toString(),
                    a.getVehicle().getPlate(),
                    a.getDriver().getName(),
                    a.getStartDate().format(DATE_FORMATTER),
                    a.getEndDate() != null ? a.getEndDate().format(DATE_FORMATTER) : "Activa",
                    a.getInitialKm().toString(),
                    a.getFinalKm() != null ? a.getFinalKm().toString() : "N/A",
                    duration
                };
                writer.writeNext(data);
            }
        }
        return out.toByteArray();
    }

    public byte[] exportToXlsx() throws IOException {
        List<AssignmentEntity> assignments = assignmentRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Asignaciones");
            
            // Estilo cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear cabecera
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID Asignacion", "Placa Vehiculo", "Nombre Conductor", "Fecha Inicio", "Fecha Fin", "Km Inicial", "Km Final", "Duracion (Horas)"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowIdx = 1;
            for (AssignmentEntity a : assignments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getId().toString());
                row.createCell(1).setCellValue(a.getVehicle().getPlate());
                row.createCell(2).setCellValue(a.getDriver().getName());
                row.createCell(3).setCellValue(a.getStartDate().format(DATE_FORMATTER));
                row.createCell(4).setCellValue(a.getEndDate() != null ? a.getEndDate().format(DATE_FORMATTER) : "Activa");
                row.createCell(5).setCellValue(a.getInitialKm());
                row.createCell(6).setCellValue(a.getFinalKm() != null ? a.getFinalKm() : 0.0);
                
                long hours = a.getEndDate() != null ? Duration.between(a.getStartDate(), a.getEndDate()).toHours() : 0;
                row.createCell(7).setCellValue(hours);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
