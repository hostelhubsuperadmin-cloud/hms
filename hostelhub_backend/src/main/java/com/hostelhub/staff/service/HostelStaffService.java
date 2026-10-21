package com.hostelhub.staff.service;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.role.entity.Role;
import com.hostelhub.role.repository.RoleRepository;
import com.hostelhub.staff.dto.CreateHostelStaffRequest;
import com.hostelhub.staff.dto.HostelStaffResponse;
import com.hostelhub.staff.dto.UpdateHostelStaffRequest;
import com.hostelhub.staff.entity.HostelStaffAssignment;
import com.hostelhub.staff.repository.HostelStaffRepository;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;
import com.hostelhub.auth.service.AccountActivationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class HostelStaffService {

    private final HostelRepository hostelRepository;
    private final HostelAdminAssignmentRepository adminAssignmentRepository;
    private final HostelStaffRepository staffRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountActivationService accountActivationService;
    private final SecureRandom secureRandom = new SecureRandom();

    public HostelStaffService(
            HostelRepository hostelRepository,
            HostelAdminAssignmentRepository adminAssignmentRepository,
            HostelStaffRepository staffRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AccountActivationService accountActivationService) {

        this.hostelRepository = hostelRepository;
        this.adminAssignmentRepository = adminAssignmentRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountActivationService = accountActivationService;
    }

    @Transactional(readOnly = true)
    public List<HostelStaffResponse> getStaff(
            String adminEmail,
            Long hostelId) {

        requireAssignedHostelAdmin(adminEmail, hostelId);

        return staffRepository
                .findAllByHostel_IdOrderByAssignedAtDesc(hostelId)
                .stream()
                .map(HostelStaffResponse::from)
                .toList();
    }

    public HostelStaffResponse createStaff(
            String adminEmail,
            Long hostelId,
            CreateHostelStaffRequest request) {

        User admin = userRepository.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found."));

        requireAssignedHostelAdmin(adminEmail, hostelId);

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Hostel not found."));

        String roleName = request.role().trim()
                .toUpperCase(Locale.ROOT);

        if (!roleName.equals("WARDEN") && !roleName.equals("STAFF")) {
            throw new IllegalArgumentException(
                    "Only WARDEN and STAFF can be created here.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Role " + roleName
                                        + " is missing from the roles table."));

        // This account needs to activate/reset its password before use.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomSecret = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone() == null
                ? null : request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(randomSecret));
        user.markPendingActivation();
        user.addRole(role);

        User savedUser = userRepository.saveAndFlush(user);

        HostelStaffAssignment assignment =
                new HostelStaffAssignment(
                        hostel,
                        savedUser,
                        admin,
                        roleName
                );

        HostelStaffAssignment savedAssignment =
                staffRepository.saveAndFlush(assignment);

// Create a hashed, expiring activation token and send the email.
        accountActivationService.createAndSendActivationEmail(savedUser);

        return HostelStaffResponse.from(savedAssignment);
    }

    private void requireAssignedHostelAdmin(
            String adminEmail,
            Long hostelId) {

        boolean isAssigned =
                adminAssignmentRepository
                        .existsByUser_EmailIgnoreCaseAndHostel_Id(
                                adminEmail,
                                hostelId
                        );

        if (!isAssigned) {
            throw new org.springframework.security.access
                    .AccessDeniedException(
                    "You are not assigned to manage this hostel.");
        }
    }

    public HostelStaffResponse updateStaff(
            String adminEmail,
            Long hostelId,
            Long staffId,
            UpdateHostelStaffRequest request) {

        requireAssignedHostelAdmin(adminEmail, hostelId);

        HostelStaffAssignment assignment = staffRepository
                .findByIdAndHostel_Id(staffId, hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff assignment not found in this hostel."));

        User user = assignment.getUser();

        String email = request.email().trim()
                .toLowerCase(Locale.ROOT);

        userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Another account already uses this email.");
                });

        String newRoleName = request.role().trim()
                .toUpperCase(Locale.ROOT);

        if (!newRoleName.equals("WARDEN")
                && !newRoleName.equals("STAFF")) {
            throw new IllegalArgumentException(
                    "Only WARDEN and STAFF are allowed.");
        }

        Role newRole = roleRepository.findByName(newRoleName)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Role " + newRoleName
                                        + " is missing from the roles table."));

        String oldRoleName = assignment.getStaffRole();

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone() == null
                || request.phone().isBlank()
                ? null : request.phone().trim());

        if (!oldRoleName.equals(newRoleName)) {
            Role oldRole = roleRepository.findByName(oldRoleName)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Existing staff role is missing."));

            // Remove the old global role only if this user has no
            // other staff assignment using that role.
            boolean usedElsewhere =
                    staffRepository.existsByUser_IdAndStaffRoleAndHostel_IdNot(
                            user.getId(),
                            oldRoleName,
                            hostelId
                    );

            if (!usedElsewhere) {
                user.getRoles().remove(oldRole);
            }

            user.addRole(newRole);
            assignment.setStaffRole(newRoleName);
        }

        userRepository.save(user);

        return HostelStaffResponse.from(
                staffRepository.save(assignment)
        );
    }

    public void deleteStaff(
            String adminEmail,
            Long hostelId,
            Long staffId) {

        requireAssignedHostelAdmin(adminEmail, hostelId);

        HostelStaffAssignment assignment = staffRepository
                .findByIdAndHostel_Id(staffId, hostelId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Staff assignment not found in this hostel."));

        // Delete only the hostel assignment, not the User account.
        staffRepository.delete(assignment);
    }

}
