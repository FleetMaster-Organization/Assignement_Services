package com.services.assignement.mapper;

import com.services.assignement.dto.AssignmentResponse;
import com.services.assignement.model.AssignmentEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

    AssignmentResponse toResponse(AssignmentEntity entity);

    List<AssignmentResponse> toResponseList(List<AssignmentEntity> entities);
}
