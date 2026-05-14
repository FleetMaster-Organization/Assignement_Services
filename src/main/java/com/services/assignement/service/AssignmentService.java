package com.services.assignement.service;

import com.services.assignement.dto.AssignmentRequest;
import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.dto.CloseAssignmentRequest;
import com.services.assignement.dto.HistoryItemDTO;
import com.services.assignement.enums.DriverStatus;
import com.services.assignement.enums.VehicleStatus;
import com.services.assignement.exception.BusinessException;
import com.services.assignement.exception.ResourceNotFoundException;
import com.services.assignement.mapper.AssignmentMapper;
import com.services.assignement.model.AssignmentEntity;
import com.services.assignement.model.DriverEntity;
import com.services.assignement.model.VehicleEntity;
import com.services.assignement.repository.AssignmentRepository;
import com.services.assignement.repository.DriverRepository;
import com.services.assignement.repository.VehicleRepository;
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
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AssignmentMapper assignmentMapper;

    @Transactional(rollbackFor = Exception.class)
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        log.info("Iniciando asignación para vehículo {} y conductor {}", request.getVehicleId(), request.getDriverId());

        // 1. Bloqueo pesimista y validación de existencia (Orden: Vehículo -> Conductor para evitar deadlocks)
        VehicleEntity vehicle = vehicleRepository.findByIdWithLock(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

        DriverEntity driver = driverRepository.findByIdWithLock(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        // 2. Validaciones de negocio (REQ-22, REQ-23, REQ-24)
        validateVehicle(vehicle);
        validateDriver(driver);

        // 3. Actualizar estados
        vehicle.setStatus(VehicleStatus.ASIGNADO);
        driver.setStatus(DriverStatus.EN_RUTA);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        // 4. Crear asignación
        AssignmentEntity assignment = AssignmentEntity.builder()
                .vehicle(vehicle)
                .driver(driver)
                .createdByUserId(request.getUserId())
                .startDate(LocalDateTime.now())
                .initialKm(vehicle.getCurrentKm())
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

        // Bloquear entidades relacionadas
        VehicleEntity vehicle = vehicleRepository.findByIdWithLock(assignment.getVehicle().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        DriverEntity driver = driverRepository.findByIdWithLock(assignment.getDriver().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        // Actualizar estados
        vehicle.setStatus(VehicleStatus.DISPONIBLE);
        vehicle.setCurrentKm(request.getFinalKm());
        driver.setStatus(DriverStatus.ACTIVO);

        vehicleRepository.save(vehicle);
        driverRepository.save(driver);

        // Cerrar asignación
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
                .description(String.format("Asignación a %s", a.getDriver().getName()))
                .conductor(a.getDriver().getName())
                .km(a.getInitialKm())
                .build())
                .collect(Collectors.toList());
    }

    private void validateVehicle(VehicleEntity vehicle) {
        if (vehicle.getStatus() != VehicleStatus.DISPONIBLE) {
            throw new BusinessException("El vehículo no está disponible (Estado actual: " + vehicle.getStatus() + ")");
        }
        if (vehicle.getSoatExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El SOAT del vehículo está vencido");
        }
        if (vehicle.getTechnoExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La revisión Tecnomecánica está vencida");
        }
    }

    private void validateDriver(DriverEntity driver) {
        if (driver.getStatus() != DriverStatus.ACTIVO) {
            throw new BusinessException("El conductor no está activo (Estado actual: " + driver.getStatus() + ")");
        }
        if (driver.getLicenseExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("La licencia del conductor está vencida");
        }
    }
}
