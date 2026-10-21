package com.hostelhub.hostler.service;

import com.hostelhub.hostler.dto.HostlerApplicationResponse;
import com.hostelhub.hostler.dto.ReviewHostlerApplicationRequest;
import com.hostelhub.hostler.entity.HostlerApplication;
import com.hostelhub.hostler.entity.HostlerApplicationStatus;
import com.hostelhub.hostler.repository.HostlerApplicationRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class HostlerApplicationReviewService {

    private final HostlerApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public HostlerApplicationReviewService(
            HostlerApplicationRepository applicationRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<HostlerApplicationResponse> getApplications(
            HostlerApplicationStatus status) {

        List<HostlerApplication> applications;

        if (status == null) {
            applications = applicationRepository.findAll()
                    .stream()
                    .sorted((a, b) ->
                            b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        } else {
            applications =
                    applicationRepository
                            .findAllByStatusOrderByCreatedAtDesc(status);
        }

        return applications.stream()
                .map(HostlerApplicationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HostlerApplicationResponse getApplication(Long applicationId) {

        HostlerApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Hostler application not found."));

        return HostlerApplicationResponse.from(application);
    }

    public HostlerApplicationResponse reviewApplication(
            Long applicationId,
            String reviewerEmail,
            ReviewHostlerApplicationRequest request) {

        User reviewer = userRepository.findByEmailIgnoreCase(reviewerEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated reviewer not found."));

        HostlerApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Hostler application not found."));

        if (application.getStatus() != HostlerApplicationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending applications can be reviewed.");
        }

        HostlerApplicationStatus decision = request.status();

        if (decision != HostlerApplicationStatus.APPROVED
                && decision != HostlerApplicationStatus.REJECTED) {
            throw new IllegalArgumentException(
                    "Review decision must be APPROVED or REJECTED.");
        }

        application.setStatus(decision);
        application.setReviewedBy(reviewer);
        application.setReviewedAt(Instant.now());
        application.setReviewNotes(trimToNull(request.reviewNotes()));

        HostlerApplication saved =
                applicationRepository.saveAndFlush(application);

        return HostlerApplicationResponse.from(saved);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
