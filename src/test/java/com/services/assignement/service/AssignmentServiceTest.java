package com.services.assignement.service;

import com.services.assignement.client.DriverClient;
import com.services.assignement.client.VehicleClient;
import com.services.assignement.dto.AssignmentRequest;
import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.exception.BusinessException;
import com.services.assignement.mapper.AssignmentMapper;
import com.services.assignement.model.AssignmentEntity;
import com.services.assignement.repository.AssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private VehicleClient vehicleClient;
    @Mock
    private DriverClient driverClient;
    @Mock
    private AssignmentMapper assignmentMapper;

    @InjectMocks
    private AssignmentService assignmentService;

    private AssignmentRequest request;

    @BeforeEach
    void setUp() {
        request = new AssignmentRequest();
        request.setVehicleId(UUID.randomUUID());
        request.setDriverId(UUID.randomUUID());
        request.setUserId(UUID.randomUUID());
    }

    @Test
    void createAssignment_Success() {
        // Arrange
        VehicleClient.VehicleResponse vehicleResponse = new VehicleClient.VehicleResponse(request.getVehicleId(), "ABC-123", 1000.0, "ACTIVO");
        when(vehicleClient.getVehicleById(request.getVehicleId())).thenReturn(vehicleResponse);
        
        when(vehicleClient.getVehicleDocuments(request.getVehicleId())).thenReturn(List.of(
                new VehicleClient.DocumentResponse("SOAT", "VIGENTE"),
                new VehicleClient.DocumentResponse("TECNOMECANICA", "VIGENTE")
        ));

        DriverClient.DriverResponse driverResponse = new DriverClient.DriverResponse(
                request.getDriverId(), "John", "Doe", "ACTIVO", "DISPONIBLE", 
                List.of(new DriverClient.LicenseResponse("VIGENTE", true))
        );
        when(driverClient.getDriverById(request.getDriverId())).thenReturn(driverResponse);

        when(assignmentRepository.save(any(AssignmentEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assignmentMapper.toResponse(any())).thenReturn(new AssignmentResponse());

        // Act
        AssignmentResponse response = assignmentService.createAssignment(request);

        // Assert
        assertNotNull(response);
        verify(vehicleClient, times(1)).assignVehicle(request.getVehicleId());
        verify(driverClient, times(1)).assignDriver(request.getDriverId());
        verify(assignmentRepository, times(1)).save(any());
    }

    @Test
    void createAssignment_VehicleNotAvailable_ThrowsException() {
        // Arrange
        VehicleClient.VehicleResponse vehicleResponse = new VehicleClient.VehicleResponse(request.getVehicleId(), "ABC-123", 1000.0, "MANTENIMIENTO");
        when(vehicleClient.getVehicleById(request.getVehicleId())).thenReturn(vehicleResponse);
        
        DriverClient.DriverResponse driverResponse = new DriverClient.DriverResponse(
                request.getDriverId(), "John", "Doe", "ACTIVO", "DISPONIBLE", 
                List.of(new DriverClient.LicenseResponse("VIGENTE", true))
        );
        when(driverClient.getDriverById(request.getDriverId())).thenReturn(driverResponse);

        // Act & Assert
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(request));
    }

    @Test
    void createAssignment_ExpiredSoat_ThrowsException() {
        // Arrange
        VehicleClient.VehicleResponse vehicleResponse = new VehicleClient.VehicleResponse(request.getVehicleId(), "ABC-123", 1000.0, "ACTIVO");
        when(vehicleClient.getVehicleById(request.getVehicleId())).thenReturn(vehicleResponse);
        
        when(vehicleClient.getVehicleDocuments(request.getVehicleId())).thenReturn(List.of(
                new VehicleClient.DocumentResponse("SOAT", "VENCIDO"),
                new VehicleClient.DocumentResponse("TECNOMECANICA", "VIGENTE")
        ));

        DriverClient.DriverResponse driverResponse = new DriverClient.DriverResponse(
                request.getDriverId(), "John", "Doe", "ACTIVO", "DISPONIBLE", 
                List.of(new DriverClient.LicenseResponse("VIGENTE", true))
        );
        when(driverClient.getDriverById(request.getDriverId())).thenReturn(driverResponse);

        // Act & Assert
        assertThrows(BusinessException.class, () -> assignmentService.createAssignment(request));
    }
}
