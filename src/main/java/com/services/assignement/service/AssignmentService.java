package com.services.assignement.service;

import com.services.assignement.client.DriverClient;
import com.services.assignement.client.VehicleClient;
import com.services.assignement.dto.AssignmentRequest;
import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.dto.CloseAssignmentRequest;
import com.services.assignement.dto.HistoryItemDTO;
import com.services.assignement.exception.BusinessException;
import com.services.assignement.exception.ResourceNotFoundException;
import com.services.assignement.mapper.AssignmentMapper;
import com.services.assignement.model.AssignmentEntity;
import com.services.assignement.repository.AssignmentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final VehicleClient vehicleClient;
    private final DriverClient driverClient;
    private final AssignmentMapper assignmentMapper;

    @Transactional(rollbackFor = Exception.class)
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        log.info("Iniciando asignación para vehículo {} y conductor {}", request.getVehicleId(), request.getDriverId());

        VehicleClient.VehicleResponse vehicle;
        try {
            vehicle = vehicleClient.getVehicleById(request.getVehicleId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }

        DriverClient.DriverResponse driver;
        try {
            driver = driverClient.getDriverById(request.getDriverId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Conductor no encontrado");
        }

        validateVehicle(vehicle);
        validateDriver(driver);

        // Actualizar estados remotos
        vehicleClient.assignVehicle(request.getVehicleId());
        driverClient.assignDriver(request.getDriverId());

        String driverName = driver.firstName() + " " + driver.lastName();

        // Crear asignación local
        AssignmentEntity assignment = AssignmentEntity.builder()
                .vehicleId(vehicle.id())
                .vehiclePlate(vehicle.plate())
                .driverId(driver.idDriver())
                .driverName(driverName)
                .createdByUserId(request.getUserId())
                .startDate(LocalDateTime.now())
                .initialKm(vehicle.currentKm())
                .build();

        AssignmentEntity saved = assignmentRepository.save(assignment);
        log.info("Asignación creada exitosamente con ID: {}", saved.getId());

        return assignmentMapper.toResponse(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public AssignmentResponse closeAssignment(UUID id, CloseAssignmentRequest request) {
        log.info("Cerrando asignación {}", id);

        AssignmentEntity assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));

        if (assignment.getEndDate() != null) {
            throw new BusinessException("La asignación ya se encuentra cerrada");
        }

        if (request.getFinalKm() < assignment.getInitialKm()) {
            throw new BusinessException("Error de lógica en odómetro: El kilometraje final no puede ser menor al inicial");
        }

        // Actualizar estados remotos (el release del vehículo actualiza el KM)
        vehicleClient.releaseVehicle(assignment.getVehicleId(), new VehicleClient.ReleaseVehicleRequest(request.getFinalKm()));
        driverClient.releaseDriver(assignment.getDriverId());

        // Cerrar asignación local
        assignment.setEndDate(LocalDateTime.now());
        assignment.setFinalKm(request.getFinalKm());

        AssignmentEntity saved = assignmentRepository.save(assignment);
        log.info("Asignación {} cerrada correctamente", saved.getId());

        return assignmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HistoryItemDTO> getVehicleHistory(UUID vehicleId) {
        log.info("Obteniendo historial para vehículo {}", vehicleId);
        
        List<AssignmentEntity> assignments = assignmentRepository.findByVehicleIdOrderByStartDateDesc(vehicleId);

        return assignments.stream().map(a -> HistoryItemDTO.builder()
                .type("ASIGNACION")
                .id(a.getId())
                .date(a.getStartDate())
                .description(String.format("Asignación a %s", a.getDriverName()))
                .conductor(a.getDriverName())
                .km(a.getInitialKm())
                .build())
                .collect(Collectors.toList());
    }

    private void validateVehicle(VehicleClient.VehicleResponse vehicle) {
        if (!"ACTIVO".equals(vehicle.operationalStatus())) {
            throw new BusinessException("El vehículo no está disponible (Estado actual: " + vehicle.operationalStatus() + ")");
        }

        List<VehicleClient.DocumentResponse> docs = vehicleClient.getVehicleDocuments(vehicle.id());
        boolean hasValidSoat = false;
        boolean hasValidTecno = false;

        for (VehicleClient.DocumentResponse doc : docs) {
            if ("VIGENTE".equals(doc.legalStatus())) {
                if ("SOAT".equals(doc.documentType())) hasValidSoat = true;
                if ("TECNOMECANICA".equals(doc.documentType())) hasValidTecno = true;
            }
        }

        if (!hasValidSoat) {
            throw new BusinessException("El SOAT del vehículo está vencido o no existe");
        }
        if (!hasValidTecno) {
            throw new BusinessException("La revisión Tecnomecánica está vencida o no existe");
        }
    }

    private void validateDriver(DriverClient.DriverResponse driver) {
        if (!"ACTIVO".equals(driver.employmentStatus())) {
            throw new BusinessException("El conductor no está activo laboralmente (Estado actual: " + driver.employmentStatus() + ")");
        }
        if (!"DISPONIBLE".equals(driver.operationalStatus())) {
            throw new BusinessException("El conductor no está disponible operativamente (Estado actual: " + driver.operationalStatus() + ")");
        }
        
        boolean hasValidLicense = driver.licenses().stream()
                .anyMatch(l -> "VIGENTE".equals(l.licenseStatus()));

        if (!hasValidLicense) {
            throw new BusinessException("La licencia del conductor está vencida o no existe");
        }
    }
}
