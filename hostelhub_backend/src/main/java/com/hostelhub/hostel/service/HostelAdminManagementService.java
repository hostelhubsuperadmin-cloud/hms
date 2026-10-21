
package com.hostelhub.hostel.service;

import com.hostelhub.auth.service.AccountActivationService;
import com.hostelhub.hostel.dto.CreateHostelAdminRequest;
import com.hostelhub.hostel.dto.HostelAdminAssignmentResponse;
import com.hostelhub.hostel.dto.HostelAdminResponse;
import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hosteladmin.entity.HostelAdminAssignment;
import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.role.entity.Role;
import com.hostelhub.role.repository.RoleRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class HostelAdminManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HostelRepository hostelRepository;
    private final HostelAdminAssignmentRepository assignmentRepository;
    private final AccountActivationService activationService;
    private final PasswordEncoder passwordEncoder;

    public HostelAdminManagementService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HostelRepository hostelRepository,
            HostelAdminAssignmentRepository assignmentRepository,
            AccountActivationService activationService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hostelRepository = hostelRepository;
        this.assignmentRepository = assignmentRepository;
        this.activationService = activationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public HostelAdminResponse createAdmin(CreateHostelAdminRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account already exists for this email address"
            );
        }

        Role role = roleRepository.findByName("HOSTEL_ADMIN")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "HOSTEL_ADMIN role is missing"
                ));

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(
                request.phone() == null || request.phone().isBlank()
                        ? null
                        : request.phone().trim()
        );

        // The invited user sets their own password through the activation flow.
        // This temporary random password is never emailed or returned.
        user.setPasswordHash(
                passwordEncoder.encode(
                        java.util.UUID.randomUUID().toString()
                )
        );

        user.markPendingActivation();
        user.getRoles().add(role);

        User saved = userRepository.saveAndFlush(user);

        // Uses the existing one-time token and email implementation.
        activationService.createAndSendActivationEmail(saved);

        return toAdminResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HostelAdminResponse> listAdmins() {
        Role role = roleRepository.findByName("HOSTEL_ADMIN")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "HOSTEL_ADMIN role is missing"
                ));

        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(existing -> existing.getId().equals(role.getId())))
                .map(this::toAdminResponse)
                .toList();
    }

    @Transactional
    public HostelAdminAssignmentResponse assignAdmin(
            Long adminId,
            Long hostelId,
            String assignerEmail) {

        User assigner = userRepository.findByEmailIgnoreCase(assignerEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hostel Admin not found"
                ));

        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hostel not found"
                ));

        boolean hasAdminRole = admin.getRoles().stream()
                .anyMatch(role -> "HOSTEL_ADMIN".equals(role.getName()));

        if (!hasAdminRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected user does not have the HOSTEL_ADMIN role"
            );
        }

        if (assignmentRepository.existsByHostel_IdAndUser_Id(hostelId, adminId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This admin is already assigned to this hostel"
            );
        }

        HostelAdminAssignment saved = assignmentRepository.save(
                new HostelAdminAssignment(hostel, admin, assigner)
        );

        return toAssignmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<HostelAdminAssignmentResponse> myHostels(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        boolean isHostelAdmin = user.getRoles().stream()
                .anyMatch(role -> "HOSTEL_ADMIN".equals(role.getName()));

        if (!isHostelAdmin) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This endpoint is only for Hostel Admins"
            );
        }

        return assignmentRepository.findAllByUser_Id(user.getId()).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    private HostelAdminResponse toAdminResponse(User user) {
        return new HostelAdminResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.isEmailVerified()
        );
    }

    private HostelAdminAssignmentResponse toAssignmentResponse(
            HostelAdminAssignment assignment) {

        return new HostelAdminAssignmentResponse(
                assignment.getId(),
                assignment.getHostel().getId(),
                assignment.getHostel().getName(),
                assignment.getUser().getId(),
                assignment.getUser().getFullName(),
                assignment.getUser().getEmail(),
                assignment.getAssignedAt()
        );
    }
    @Transactional
    public void deleteAdmin(Long adminId, String currentUserEmail) {
        User currentUser = userRepository
                .findByEmailIgnoreCase(currentUserEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hostel Admin not found"
                ));

        if (currentUser.getId().equals(admin.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You cannot delete your own account through this operation"
            );
        }

        boolean hasHostelAdminRole = admin.getRoles().stream()
                .anyMatch(role -> "HOSTEL_ADMIN".equals(role.getName()));

        if (!hasHostelAdminRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected account is not a Hostel Admin"
            );
        }

        // Never delete an account that also has another role.
        if (admin.getRoles().size() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account has additional roles. Remove or transfer "
                            + "its responsibilities before deleting it."
            );
        }

        // Remove the admin's hostel assignments first.
        assignmentRepository.deleteAllByUser_Id(adminId);
        assignmentRepository.flush();

        // Preserve assignment records that still refer to this user
        // as the person who created them.
        if (assignmentRepository.existsByAssignedBy_Id(adminId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is referenced by other admin assignments. "
                            + "Transfer those references before deleting it."
            );
        }

        try {
            userRepository.delete(admin);
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account is referenced by other records. "
                            + "The account was not deleted; resolve the "
                            + "dependent records first.",
                    exception
            );
        }
    }
    @Transactional(readOnly = true)
    public List<HostelAdminAssignmentResponse> listAdminAssignments(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hostel Admin not found"
                ));

        boolean hasAdminRole = admin.getRoles().stream()
                .anyMatch(role -> "HOSTEL_ADMIN".equals(role.getName()));

        if (!hasAdminRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected user is not a Hostel Admin"
            );
        }

        return assignmentRepository.findAllByUser_Id(adminId)
                .stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Transactional
    public void unassignAdminFromHostel(Long adminId, Long hostelId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Hostel Admin not found"
                ));

        boolean hasAdminRole = admin.getRoles().stream()
                .anyMatch(role -> "HOSTEL_ADMIN".equals(role.getName()));

        if (!hasAdminRole) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected user is not a Hostel Admin"
            );
        }

        HostelAdminAssignment assignment = assignmentRepository
                .findByUser_IdAndHostel_Id(adminId, hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "This Hostel Admin is not assigned to the selected hostel"
                ));

        assignmentRepository.delete(assignment);
        assignmentRepository.flush();
    }
}

