package com.services.assignement.service;

import com.services.assignement.dto.AssignmentRequest;
import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.enums.DriverStatus;
import com.services.assignement.enums.VehicleStatus;
import com.services.assignement.exception.BusinessException;
import com.services.assignement.mapper.AssignmentMapper;
import com.services.assignement.model.AssignmentEntity;
import com.services.assignement.model.DriverEntity;
import com.services.assignement.model.VehicleEntity;
import com.services.assignement.repository.AssignmentRepository;
import com.services.assignement.repository.DriverRepository;
import com.services.assignement.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private AssignmentMapper assignmentMapper;

    @InjectMocks
    private AssignmentService assignmentService;

    private VehicleEntity vehicle;
    private DriverEntity driver;
    private AssignmentRequest request;

    @BeforeEach
    void setUp() {
        vehicle = VehicleEntity.builder()
                .id(UUID.randomUUID())
                .plate("ABC-123")
                .status(VehicleStatus.DISPONIBLE)
                .currentKm(1000.0)
                .soatExpiryDate(LocalDateTime.now().plusDays(10))
                .technoExpiryDate(LocalDateTime.now().plusDays(10))
                .build();

        driver = DriverEntity.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .status(DriverStatus.ACTIVO)
                .licenseExpiryDate(LocalDateTime.now().plusDays(10))
                .build();

        request = new AssignmentRequest();
        request.setVehicleId(vehicle.getId());
        request.setDriverId(driver.getId());
        request.setUserId(UUID.randomUUID());
    }

    @Test
    void createAssignment_Success() {
        // Arrange
        when(vehicleRepository.findByIdWithLock(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(driverRepository.findByIdWithLock(driver.getId())).thenReturn(Optional.of(driver));
        when(assignmentRepository.save(any(AssignmentEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assignmentMapper.toResponse(any())).thenReturn(new AssignmentResponse());

        // Act
        AssignmentResponse response = assignmentService.createAssignment(request);

        // Assert
        assertNotNull(response);
        assertEquals(VehicleStatus.ASIGNADO, vehicle.getStatus());
        assertEquals(DriverStatus.EN_RUTA, driver.getStatus());
        verify(assignmentRepository, times(1)).save(any());
    }

    @Test
    void createAssignment_VehicleNotAvailable_ThrowsException() {
        // Arrange
        vehicle.setStatus(VehicleStatus.ASIGNADO);
        when(vehicleRepository.findByIdWithLock(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(driverRepository.findByIdWithLock(driver.getId())).thenReturn(Optional.of(driver));

        // Act & Assert
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(request));
    }

    @Test
    void createAssignment_ExpiredSoat_ThrowsException() {
        // Arrange
        vehicle.setSoatExpiryDate(LocalDateTime.now().minusDays(1));
        when(vehicleRepository.findByIdWithLock(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(driverRepository.findByIdWithLock(driver.getId())).thenReturn(Optional.of(driver));

        // Act & Assert
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(request));
    }
}
