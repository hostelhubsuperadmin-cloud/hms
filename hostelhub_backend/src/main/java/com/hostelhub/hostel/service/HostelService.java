package com.hostelhub.hostel.service;

import com.hostelhub.hostel.dto.CreateHostelRequest;
import com.hostelhub.hostel.dto.HostelResponse;
import com.hostelhub.hostel.dto.UpdateHostelRequest;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class HostelService {

    private final HostelRepository hostelRepository;

    public HostelService(HostelRepository hostelRepository) {
        this.hostelRepository = hostelRepository;
    }

    public HostelResponse create(CreateHostelRequest request) {
        String code = normalizeCode(request.code());

        if (hostelRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Hostel code already exists"
            );
        }

        Hostel hostel = new Hostel();
        hostel.setName(request.name().trim());
        hostel.setCode(code);
        hostel.setDescription(request.description());
        hostel.setAddressLine(request.addressLine().trim());
        hostel.setCity(request.city().trim());
        hostel.setState(request.state().trim());
        hostel.setCountry(request.country().trim());
        hostel.setPostalCode(request.postalCode());
        hostel.setContactPhone(request.contactPhone());
        hostel.setContactEmail(normalizeEmail(request.contactEmail()));
        hostel.setStatus(HostelStatus.DRAFT);

        return HostelResponse.from(hostelRepository.save(hostel));
    }

    @Transactional(readOnly = true)
    public List<HostelResponse> findAll() {
        return hostelRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(HostelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HostelResponse findById(Long id) {
        return HostelResponse.from(getHostel(id));
    }

    public HostelResponse update(Long id, UpdateHostelRequest request) {
        Hostel hostel = getHostel(id);
        String code = normalizeCode(request.code());

        hostelRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "Hostel code already exists"
                    );
                });

        hostel.setName(request.name().trim());
        hostel.setCode(code);
        hostel.setDescription(request.description());
        hostel.setAddressLine(request.addressLine().trim());
        hostel.setCity(request.city().trim());
        hostel.setState(request.state().trim());
        hostel.setCountry(request.country().trim());
        hostel.setPostalCode(request.postalCode());
        hostel.setContactPhone(request.contactPhone());
        hostel.setContactEmail(normalizeEmail(request.contactEmail()));

        return HostelResponse.from(hostelRepository.save(hostel));
    }

    public HostelResponse changeStatus(Long id, String status) {
        Hostel hostel = getHostel(id);

        try {
            hostel.setStatus(HostelStatus.valueOf(
                    status.trim().toUpperCase(Locale.ROOT)
            ));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be DRAFT, ACTIVE or INACTIVE"
            );
        }

        return HostelResponse.from(hostelRepository.save(hostel));
    }

    private Hostel getHostel(Long id) {
        return hostelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hostel not found"
                ));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank()
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }
}
