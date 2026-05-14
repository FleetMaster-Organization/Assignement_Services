package com.services.assignement.repository;

import com.services.assignement.model.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID>, JpaSpecificationExecutor<AssignmentEntity> {
    
    List<AssignmentEntity> findByVehicleIdOrderByStartDateDesc(UUID vehicleId);
}
