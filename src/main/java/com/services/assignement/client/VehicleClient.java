package com.services.assignement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;
import java.util.List;

@FeignClient(name = "vehicle-service", url = "http://localhost:8081/vehicles")
public interface VehicleClient {

    @GetMapping("/{id}")
    VehicleResponse getVehicleById(@PathVariable("id") UUID id);

    @GetMapping("/{id}/documents")
    List<DocumentResponse> getVehicleDocuments(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/assign")
    void assignVehicle(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/release")
    void releaseVehicle(@PathVariable("id") UUID id, @RequestBody ReleaseVehicleRequest request);
    
    // DTOs record
    record VehicleResponse(UUID id, String plate, Double currentKm, String operationalStatus) {}
    record DocumentResponse(String documentType, String legalStatus) {}
    record ReleaseVehicleRequest(Double finalKm) {}
}
