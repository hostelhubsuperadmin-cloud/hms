package com.hostelhub.hostler.service;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.entity.HostelStatus;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hostler.dto.CreateHostlerApplicationRequest;
import com.hostelhub.hostler.dto.HostlerApplicationResponse;
import com.hostelhub.hostler.entity.HostlerApplication;
import com.hostelhub.hostler.entity.HostlerApplicationStatus;
import com.hostelhub.hostler.repository.HostlerApplicationRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HostlerApplicationService {

    private final HostlerApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final HostelRepository hostelRepository;

    public HostlerApplicationService(
            HostlerApplicationRepository applicationRepository,
            UserRepository userRepository,
            HostelRepository hostelRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.hostelRepository = hostelRepository;
    }

    /**
     * Submit a new hostel application for the authenticated user.
     */
    public HostlerApplicationResponse createApplication(
            String authenticatedEmail,
            CreateHostlerApplicationRequest request) {

        User user = findUserByEmail(authenticatedEmail);

        Hostel hostel = hostelRepository.findById(request.hostelId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Hostel not found."));

        // Only active hostels accept applications.
        if (hostel.getStatus() != HostelStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Applications can only be submitted to active hostels."
            );
        }

        // Reject a future date of birth.
        if (request.dateOfBirth() != null
                && request.dateOfBirth().isAfter(
                java.time.LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Date of birth cannot be in the future."
            );
        }

        // Prevent multiple pending applications by the same user
        // for the same hostel.
        boolean alreadyPending =
                applicationRepository
                        .existsByUser_IdAndHostel_IdAndStatus(
                                user.getId(),
                                hostel.getId(),
                                HostlerApplicationStatus.PENDING
                        );

        if (alreadyPending) {
            throw new IllegalArgumentException(
                    "You already have a pending application for this hostel."
            );
        }

        HostlerApplication application = new HostlerApplication();

        application.setUser(user);
        application.setHostel(hostel);
        application.setDateOfBirth(request.dateOfBirth());
        application.setGender(trimToNull(request.gender()));
        application.setGuardianName(trimToNull(request.guardianName()));
        application.setGuardianPhone(trimToNull(request.guardianPhone()));
        application.setEmergencyContact(
                trimToNull(request.emergencyContact()));
        application.setAddressLine(trimToNull(request.addressLine()));
        application.setCity(trimToNull(request.city()));
        application.setState(trimToNull(request.state()));
        application.setPostalCode(trimToNull(request.postalCode()));
        application.setRequestedMoveInDate(
                request.requestedMoveInDate());
        application.setApplicantNotes(
                trimToNull(request.applicantNotes()));

        application.setStatus(HostlerApplicationStatus.PENDING);

        try {
            HostlerApplication saved =
                    applicationRepository.saveAndFlush(application);

            return HostlerApplicationResponse.from(saved);

        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException(
                    "Unable to submit the application. "
                            + "A pending application may already exist."
            );
        }
    }

    /**
     * Retrieve all applications belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<HostlerApplicationResponse> getMyApplications(
            String authenticatedEmail) {

        User user = findUserByEmail(authenticatedEmail);

        return applicationRepository
                .findAllByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(HostlerApplicationResponse::from)
                .toList();
    }

    /**
     * Retrieve one application, only if it belongs to the authenticated user.
     */
    @Transactional(readOnly = true)
    public HostlerApplicationResponse getMyApplication(
            String authenticatedEmail,
            Long applicationId) {

        User user = findUserByEmail(authenticatedEmail);

        HostlerApplication application =
                applicationRepository
                        .findByIdAndUser_Id(applicationId, user.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Application not found."
                                ));

        return HostlerApplicationResponse.from(application);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found."
                        ));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
