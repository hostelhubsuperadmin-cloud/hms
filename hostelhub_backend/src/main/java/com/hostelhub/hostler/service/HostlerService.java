package com.hostelhub.hostler.service;

import com.hostelhub.allocation.entity.Allocation;
import com.hostelhub.allocation.repository.AllocationRepository;
import com.hostelhub.auth.service.AccountActivationService;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hostler.dto.HostlerRequest;
import com.hostelhub.hostler.dto.HostlerResponse;
import com.hostelhub.hostler.entity.HostlerProfile;
import com.hostelhub.hostler.repository.HostlerProfileRepository;
import com.hostelhub.role.entity.Role;
import com.hostelhub.role.repository.RoleRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class HostlerService {

    private final HostlerProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final HostelRepository hostelRepository;
    private final RoleRepository roleRepository;
    private final AllocationRepository allocationRepository;
    private final HostelAccessService accessService;
    private final PasswordEncoder passwordEncoder;
    private final AccountActivationService activationService;

    private final SecureRandom secureRandom = new SecureRandom();

    public HostlerService(
            HostlerProfileRepository profileRepository,
            UserRepository userRepository,
            HostelRepository hostelRepository,
            RoleRepository roleRepository,
            AllocationRepository allocationRepository,
            HostelAccessService accessService,
            PasswordEncoder passwordEncoder,
            AccountActivationService activationService
    ) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.hostelRepository = hostelRepository;
        this.roleRepository = roleRepository;
        this.allocationRepository = allocationRepository;
        this.accessService = accessService;
        this.passwordEncoder = passwordEncoder;
        this.activationService = activationService;
    }

    @Transactional(readOnly = true)
    public List<HostlerResponse> list(
            String authenticatedEmail,
            Long hostelId,
            String query,
            String status
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        String normalizedQuery = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        String normalizedStatus = status == null || status.isBlank()
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);

        return profileRepository
                .findAllByHostel_IdOrderByCreatedAtDesc(hostelId)
                .stream()
                .filter(profile ->
                        normalizedStatus.isEmpty()
                                || profile.getStatus().equals(normalizedStatus)
                )
                .filter(profile -> {
                    if (normalizedQuery.isEmpty()) {
                        return true;
                    }

                    User user = profile.getUser();

                    return contains(user.getFullName(), normalizedQuery)
                            || contains(user.getEmail(), normalizedQuery)
                            || contains(user.getPhone(), normalizedQuery)
                            || contains(profile.getGuardianName(), normalizedQuery)
                            || contains(profile.getGuardianPhone(), normalizedQuery)
                            || contains(profile.getEmergencyContact(), normalizedQuery);
                })
                .map(HostlerResponse::from)
                .toList();
    }

    public HostlerResponse create(
            String authenticatedEmail,
            Long hostelId,
            HostlerRequest request
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Hostel not found.")
                );

        String email = normalizeEmail(request.email());

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        Role hostlerRole = roleRepository.findByName("HOSTLER")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "HOSTLER role is missing from the roles table."
                        )
                );

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String temporarySecret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(trimToNull(request.phone()));
        user.setPasswordHash(
                passwordEncoder.encode(temporarySecret)
        );
        user.markPendingActivation();
        user.addRole(hostlerRole);

        User savedUser = userRepository.saveAndFlush(user);

        HostlerProfile profile = new HostlerProfile();
        profile.setUser(savedUser);
        profile.setHostel(hostel);

        applyRequest(profile, savedUser, request);

        HostlerProfile savedProfile =
                profileRepository.saveAndFlush(profile);

        // The hostler receives an activation email and sets their own password.
        activationService.createAndSendActivationEmail(savedUser);

        return HostlerResponse.from(savedProfile);
    }

    public HostlerResponse update(
            String authenticatedEmail,
            Long hostelId,
            Long hostlerId,
            HostlerRequest request
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        HostlerProfile profile = profileRepository
                .findByIdAndHostel_Id(hostlerId, hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Hostler not found in this hostel."
                        )
                );

        User user = profile.getUser();
        String email = normalizeEmail(request.email());

        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Another account already uses this email."
                    );
                });

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(trimToNull(request.phone()));

        applyRequest(profile, user, request);

        userRepository.save(user);
        return HostlerResponse.from(
                profileRepository.saveAndFlush(profile)
        );
    }

    public void deactivate(
            String authenticatedEmail,
            Long hostelId,
            Long hostlerId
    ) {
        accessService.requireCanManageHostel(
                authenticatedEmail,
                hostelId
        );

        HostlerProfile profile = profileRepository
                .findByIdAndHostel_Id(hostlerId, hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Hostler not found in this hostel."
                        )
                );

        boolean hasOpenAllocation =
                allocationRepository.existsByHostlerIdAndStatusIn(
                        hostlerId,
                        List.of(
                                Allocation.Status.RESERVED,
                                Allocation.Status.ACTIVE
                        )
                );

        if (hasOpenAllocation) {
            throw new IllegalArgumentException(
                    "Check out or cancel this hostler's allocation before removing the hostler."
            );
        }

        // Keep the record for invoice and payment history.
        profile.setStatus("INACTIVE");
        profileRepository.save(profile);
    }

    private void applyRequest(
            HostlerProfile profile,
            User user,
            HostlerRequest request
    ) {
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setGender(trimToNull(request.gender()));
        profile.setGuardianName(trimToNull(request.guardianName()));
        profile.setGuardianPhone(trimToNull(request.guardianPhone()));
        profile.setEmergencyContact(trimToNull(request.emergencyContact()));
        profile.setAddressLine(trimToNull(request.addressLine()));
        profile.setCity(trimToNull(request.city()));
        profile.setState(trimToNull(request.state()));
        profile.setPostalCode(trimToNull(request.postalCode()));
        profile.setNotes(trimToNull(request.notes()));
    }

    private boolean contains(String value, String query) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
