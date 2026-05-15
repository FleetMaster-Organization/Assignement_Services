package com.services.assignement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;
import java.util.List;

@FeignClient(name = "driver-service", url = "http://localhost:8082/drivers")
public interface DriverClient {

    @GetMapping("/{id}")
    DriverResponse getDriverById(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/assign")
    void assignDriver(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/release")
    void releaseDriver(@PathVariable("id") UUID id);
    
    // DTOs record
    record DriverResponse(UUID idDriver, String firstName, String lastName, String employmentStatus, String operationalStatus, List<LicenseResponse> licenses) {}
    record LicenseResponse(String licenseStatus, boolean publicService) {}
}
