package com.hostelhub.allocation.controller;

import com.hostelhub.allocation.dto.AllocationResponse;
import com.hostelhub.allocation.dto.CheckInRequest;
import com.hostelhub.allocation.dto.CheckOutRequest;
import com.hostelhub.allocation.dto.CreateAllocationRequest;
import com.hostelhub.allocation.service.AllocationService;
import com.hostelhub.bed.dto.BedResponse;
import com.hostelhub.security.AuthenticatedUserId;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels/{hostelId}")
@PreAuthorize("hasAnyRole('HOSTEL_ADMIN', 'WARDEN')")
public class AllocationController {

    private final AllocationService service;

    public AllocationController(AllocationService service) {
        this.service = service;
    }

    @GetMapping("/available-beds")
    public List<BedResponse> availableBeds(
            Authentication authentication,
            @PathVariable Long hostelId
    ) {
        return service.listAvailableBeds(
                authentication.getName(),
                hostelId
        );
    }

    @GetMapping("/allocations")
    public List<AllocationResponse> list(
            Authentication authentication,
            @PathVariable Long hostelId
    ) {
        return service.list(
                authentication.getName(),
                hostelId
        );
    }

    @PostMapping("/allocations")
    @ResponseStatus(HttpStatus.CREATED)
    public AllocationResponse allocate(
            Authentication authentication,
            @PathVariable Long hostelId,
            @Valid @RequestBody CreateAllocationRequest request
    ) {
        Long authenticatedUserId =
                AuthenticatedUserId.from(authentication);

        return service.allocate(
                authentication.getName(),
                hostelId,
                request,
                authenticatedUserId
        );
    }

    @PatchMapping("/allocations/{allocationId}/check-in")
    public AllocationResponse checkIn(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long allocationId,
            @Valid @RequestBody CheckInRequest request
    ) {
        return service.checkIn(
                authentication.getName(),
                hostelId,
                allocationId,
                request
        );
    }

    @PatchMapping("/allocations/{allocationId}/check-out")
    public AllocationResponse checkOut(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long allocationId,
            @Valid @RequestBody CheckOutRequest request
    ) {
        return service.checkOut(
                authentication.getName(),
                hostelId,
                allocationId,
                request
        );
    }

    @DeleteMapping("/allocations/{allocationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            Authentication authentication,
            @PathVariable Long hostelId,
            @PathVariable Long allocationId
    ) {
        service.cancelReservation(
                authentication.getName(),
                hostelId,
                allocationId
        );
    }
}
