package com.hostelhub.hostel.controller;

import com.hostelhub.bed.entity.BedStatus;
import com.hostelhub.bed.repository.BedRepository;
import com.hostelhub.hostel.dto.PublicHostelResponse;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/public/hostels")
public class PublicHostelController {

    private final HostelRepository hostelRepository;
    private final BedRepository bedRepository;

    public PublicHostelController(
            HostelRepository hostelRepository,
            BedRepository bedRepository
    ) {
        this.hostelRepository = hostelRepository;
        this.bedRepository = bedRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PublicHostelResponse> getPublicHostels(
            @RequestParam(required = false) String city
    ) {
        return hostelRepository
                .findAllByStatusOrderByNameAsc(HostelStatus.ACTIVE)
                .stream()
                .filter(hostel ->
                        city == null
                                || city.isBlank()
                                || hostel.getCity().equalsIgnoreCase(city.trim())
                )
                .map(this::toPublicResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PublicHostelResponse getPublicHostel(
            @PathVariable Long id
    ) {
        Hostel hostel = hostelRepository.findById(id)
                .filter(item -> item.getStatus() == HostelStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Public hostel not found"
                ));

        return toPublicResponse(hostel);
    }

    @GetMapping("/{id}/availability")
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(
            @PathVariable Long id
    ) {
        Hostel hostel = hostelRepository.findById(id)
                .filter(item -> item.getStatus() == HostelStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Public hostel not found"
                ));

        long total = totalBeds(hostel.getId());
        long available = availableBeds(hostel.getId());

        return new AvailabilityResponse(
                hostel.getId(),
                total,
                available,
                total - available
        );
    }

    private PublicHostelResponse toPublicResponse(Hostel hostel) {
        return PublicHostelResponse.from(
                hostel,
                totalBeds(hostel.getId()),
                availableBeds(hostel.getId())
        );
    }

    private long totalBeds(Long hostelId) {
        return bedRepository.countByRoom_Floor_Block_Hostel_Id(hostelId)
                + bedRepository.countByRoom_Flat_Floor_Block_Hostel_Id(
                hostelId
        );
    }

    private long availableBeds(Long hostelId) {
        return bedRepository
                .countByRoom_Floor_Block_Hostel_IdAndStatus(
                        hostelId, BedStatus.AVAILABLE
                )
                + bedRepository
                .countByRoom_Flat_Floor_Block_Hostel_IdAndStatus(
                        hostelId, BedStatus.AVAILABLE
                );
    }

    public record AvailabilityResponse(
            Long hostelId,
            long totalBeds,
            long availableBeds,
            long occupiedOrUnavailableBeds
    ) {}
}
