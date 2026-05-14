package com.services.assignement.mapper;

import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.model.AssignmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehiclePlate", source = "vehicle.plate")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    AssignmentResponse toResponse(AssignmentEntity entity);

    List<AssignmentResponse> toResponseList(List<AssignmentEntity> entities);
}
