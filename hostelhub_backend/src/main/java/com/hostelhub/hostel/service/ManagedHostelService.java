package com.hostelhub.hostel.service;

import com.hostelhub.hostel.dto.ManagedHostelResponse;
import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.staff.entity.HostelStaffAssignment;
import com.hostelhub.staff.repository.HostelStaffRepository;
import com.hostelhub.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ManagedHostelService {

    private final UserRepository userRepository;
    private final HostelAdminAssignmentRepository adminRepository;
    private final HostelStaffRepository staffRepository;

    public ManagedHostelService(
            UserRepository userRepository,
            HostelAdminAssignmentRepository adminRepository,
            HostelStaffRepository staffRepository
    ) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.staffRepository = staffRepository;
    }

    public List<ManagedHostelResponse> listForUser(String email) {
        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found."
                        )
                );

        Map<Long, ManagedHostelResponse> result =
                new LinkedHashMap<>();

        adminRepository.findAllByUser_Id(user.getId())
                .forEach(assignment -> {
                    var hostel = assignment.getHostel();

                    result.put(
                            hostel.getId(),
                            new ManagedHostelResponse(
                                    hostel.getId(),
                                    hostel.getName(),
                                    "HOSTEL_ADMIN"
                            )
                    );
                });

        List<HostelStaffAssignment> staffAssignments =
                staffRepository
                        .findAllByUser_EmailIgnoreCaseOrderByAssignedAtDesc(
                                email
                        );

        staffAssignments.stream()
                .filter(assignment ->
                        "WARDEN".equalsIgnoreCase(
                                assignment.getStaffRole()
                        )
                )
                .forEach(assignment -> {
                    var hostel = assignment.getHostel();

                    result.putIfAbsent(
                            hostel.getId(),
                            new ManagedHostelResponse(
                                    hostel.getId(),
                                    hostel.getName(),
                                    "WARDEN"
                            )
                    );
                });

        return List.copyOf(result.values());
    }
}
