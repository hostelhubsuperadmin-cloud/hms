package com.hostelhub.allocation.service;

import com.hostelhub.allocation.dto.AllocationResponse;
import com.hostelhub.allocation.dto.CheckInRequest;
import com.hostelhub.allocation.dto.CheckOutRequest;
import com.hostelhub.allocation.dto.CreateAllocationRequest;
import com.hostelhub.allocation.entity.Allocation;
import com.hostelhub.allocation.repository.AllocationRepository;
import com.hostelhub.bed.dto.BedResponse;
import com.hostelhub.bed.entity.Bed;
import com.hostelhub.bed.entity.BedStatus;
import com.hostelhub.bed.repository.BedRepository;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hostler.entity.HostlerProfile;
import com.hostelhub.hostler.repository.HostlerProfileRepository;
import com.hostelhub.hostler.service.HostelAccessService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AllocationService {

    private static final List<Allocation.Status> BLOCKING_STATUSES =
            List.of(
                    Allocation.Status.RESERVED,
                    Allocation.Status.ACTIVE
            );

    private final AllocationRepository allocationRepository;
    private final BedRepository bedRepository;
    private final HostelRepository hostelRepository;
    private final HostlerProfileRepository hostlerRepository;
    private final HostelAccessService accessService;

    public AllocationService(
            AllocationRepository allocationRepository,
            BedRepository bedRepository,
            HostelRepository hostelRepository,
            HostlerProfileRepository hostlerRepository,
            HostelAccessService accessService
    ) {
        this.allocationRepository = allocationRepository;
        this.bedRepository = bedRepository;
        this.hostelRepository = hostelRepository;
        this.hostlerRepository = hostlerRepository;
        this.accessService = accessService;
    }

    public AllocationResponse allocate(
            String authenticatedEmail,
            Long hostelId,
            CreateAllocationRequest request,
            Long authenticatedUserId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Hostel not found.")
                );

        if (hostel.getStatus() != HostelStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Beds can only be allocated in an active hostel."
            );
        }

        HostlerProfile hostler = hostlerRepository
                .findByIdAndHostel_Id(
                        request.hostlerId(),
                        hostelId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Hostler does not belong to this hostel."
                        )
                );

        if (!"ACTIVE".equals(hostler.getStatus())) {
            throw new IllegalArgumentException(
                    "An inactive hostler cannot receive a bed allocation."
            );
        }

        LocalDate today = LocalDate.now();

        if (request.startDate().isBefore(today)) {
            throw new IllegalArgumentException(
                    "The planned check-in date cannot be in the past."
            );
        }

        if (request.plannedCheckoutDate() != null
                && request.plannedCheckoutDate().isBefore(
                request.startDate()
        )) {
            throw new IllegalArgumentException(
                    "Planned check-out cannot be before check-in."
            );
        }

        Bed bed = bedRepository.findByIdForUpdate(
                request.bedId()
        ).orElseThrow(() ->
                new EntityNotFoundException("Bed not found.")
        );

        requireBedBelongsToHostel(bed, hostelId);

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "The selected bed is not available."
            );
        }

        if (allocationRepository.existsByBedIdAndStatusIn(
                bed.getId(),
                BLOCKING_STATUSES
        )) {
            throw new IllegalArgumentException(
                    "The selected bed is already reserved or occupied."
            );
        }

        Allocation allocation = new Allocation();
        allocation.setHostelId(hostelId);
        allocation.setHostlerId(hostler.getId());
        allocation.setBedId(bed.getId());
        allocation.setAllocatedBy(authenticatedUserId);
        allocation.setStartDate(request.startDate());
        allocation.setPlannedCheckoutDate(
                request.plannedCheckoutDate()
        );
        allocation.setStatus(Allocation.Status.RESERVED);

        try {
            return toResponse(
                    allocationRepository.saveAndFlush(allocation)
            );
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "This bed was reserved by another request. Refresh available beds and try again."
            );
        }
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> list(
            String authenticatedEmail,
            Long hostelId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        return allocationRepository
                .findByHostelIdOrderByIdDesc(hostelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BedResponse> listAvailableBeds(
            String authenticatedEmail,
            Long hostelId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        return bedRepository.findAvailableBedsForHostel(
                        hostelId,
                        BedStatus.AVAILABLE,
                        BLOCKING_STATUSES
                )
                .stream()
                .map(BedResponse::from)
                .toList();
    }

    public AllocationResponse checkIn(
            String authenticatedEmail,
            Long hostelId,
            Long allocationId,
            CheckInRequest request
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        Allocation allocation = findAllocation(
                hostelId,
                allocationId
        );

        if (allocation.getStatus() != Allocation.Status.RESERVED) {
            throw new IllegalArgumentException(
                    "Only a reserved allocation can be checked in."
            );
        }

        LocalDate date = request.checkInDate();

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-in cannot be recorded for a future date."
            );
        }

        if (date.isBefore(allocation.getStartDate())) {
            throw new IllegalArgumentException(
                    "Check-in cannot be earlier than the planned check-in date."
            );
        }

        if (allocation.getPlannedCheckoutDate() != null
                && date.isAfter(allocation.getPlannedCheckoutDate())) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be after the planned check-out date."
            );
        }

        Bed bed = bedRepository.findByIdForUpdate(
                allocation.getBedId()
        ).orElseThrow(() ->
                new EntityNotFoundException("Allocated bed not found.")
        );

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "This bed is not available for check-in."
            );
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        allocation.setStartDate(date);
        allocation.setStatus(Allocation.Status.ACTIVE);

        return toResponse(
                allocationRepository.saveAndFlush(allocation)
        );
    }

    public AllocationResponse checkOut(
            String authenticatedEmail,
            Long hostelId,
            Long allocationId,
            CheckOutRequest request
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        Allocation allocation = findAllocation(
                hostelId,
                allocationId
        );

        if (allocation.getStatus() != Allocation.Status.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only an active allocation can be checked out."
            );
        }

        LocalDate date = request.checkOutDate();

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-out cannot be recorded for a future date."
            );
        }

        if (date.isBefore(allocation.getStartDate())) {
            throw new IllegalArgumentException(
                    "Check-out cannot be before check-in."
            );
        }

        Bed bed = bedRepository.findByIdForUpdate(
                allocation.getBedId()
        ).orElseThrow(() ->
                new EntityNotFoundException("Allocated bed not found.")
        );

        allocation.setEndDate(date);
        allocation.setStatus(Allocation.Status.COMPLETED);

        bed.setStatus(BedStatus.AVAILABLE);

        bedRepository.save(bed);

        return toResponse(
                allocationRepository.saveAndFlush(allocation)
        );
    }

    public void cancelReservation(
            String authenticatedEmail,
            Long hostelId,
            Long allocationId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        Allocation allocation = findAllocation(
                hostelId,
                allocationId
        );

        if (allocation.getStatus() != Allocation.Status.RESERVED) {
            throw new IllegalArgumentException(
                    "Only a reserved allocation can be cancelled. Check out an active hostler instead."
            );
        }

        allocation.setStatus(Allocation.Status.CANCELLED);
        allocationRepository.save(allocation);
    }

    private Allocation findAllocation(
            Long hostelId,
            Long allocationId
    ) {
        return allocationRepository
                .findByIdAndHostelId(allocationId, hostelId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Allocation not found in this hostel."
                        )
                );
    }

    private void requireBedBelongsToHostel(
            Bed bed,
            Long hostelId
    ) {
        var room = bed.getRoom();

        var floor = room.getFlat() != null
                ? room.getFlat().getFloor()
                : room.getFloor();

        Long actualHostelId = floor.getBlock().getHostel().getId();

        if (!Objects.equals(actualHostelId, hostelId)) {
            throw new IllegalArgumentException(
                    "The selected bed does not belong to this hostel."
            );
        }
    }

    private AllocationResponse toResponse(Allocation allocation) {
        HostlerProfile hostler = hostlerRepository
                .findByIdAndHostel_Id(
                        allocation.getHostlerId(),
                        allocation.getHostelId()
                )
                .orElse(null);

        Bed bed = bedRepository.findById(
                allocation.getBedId()
        ).orElse(null);

        return new AllocationResponse(
                allocation.getId(),
                allocation.getHostelId(),

                allocation.getHostlerId(),
                hostler == null
                        ? "Unknown hostler"
                        : hostler.getUser().getFullName(),
                hostler == null
                        ? ""
                        : hostler.getUser().getEmail(),

                allocation.getBedId(),
                bed == null ? "" : bed.getBedNumber(),
                bed == null ? "" : bed.getRoom().getRoomNumber(),

                allocation.getAllocatedBy(),
                allocation.getStatus().name(),

                allocation.getStartDate(),
                allocation.getPlannedCheckoutDate(),
                allocation.getEndDate()
        );
    }
}
