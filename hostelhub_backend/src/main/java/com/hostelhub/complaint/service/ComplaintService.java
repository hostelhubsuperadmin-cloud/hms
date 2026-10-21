package com.hostelhub.complaint.service;

import com.hostelhub.complaint.dto.ComplaintResponse;
import com.hostelhub.complaint.dto.CreateComplaintRequest;
import com.hostelhub.complaint.dto.UpdateComplaintRequest;
import com.hostelhub.complaint.entity.Complaint;
import com.hostelhub.complaint.repository.ComplaintRepository;
import com.hostelhub.hostel.repository.HostelRepository;
import com.hostelhub.hosteladmin.repository.HostelAdminAssignmentRepository;
import com.hostelhub.hostler.repository.HostlerProfileRepository;
import com.hostelhub.hostler.service.HostelAccessService;
import com.hostelhub.user.entity.User;
import com.hostelhub.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final HostlerProfileRepository hostlerProfiles;
    private final UserRepository users;
    private final HostelRepository hostels;
    private final HostelAccessService hostelAccess;

    public ComplaintService(
            ComplaintRepository complaints,
            HostlerProfileRepository hostlerProfiles,
            UserRepository users,
            HostelRepository hostels,
            HostelAccessService hostelAccess
    ) {
        this.complaints = complaints;
        this.hostlerProfiles = hostlerProfiles;
        this.users = users;
        this.hostels = hostels;
        this.hostelAccess = hostelAccess;
    }

    @Transactional
    public ComplaintResponse create(
            Long hostlerId,
            CreateComplaintRequest request
    ) {
        var profile = hostlerProfiles.findById(hostlerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "An active hostel resident profile is required."
                ));

        if (!"ACTIVE".equalsIgnoreCase(profile.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only active residents can submit complaints."
            );
        }

        Complaint complaint = new Complaint();
        complaint.setHostler(profile.getUser());
        complaint.setHostel(profile.getHostel());
        complaint.setSubject(request.subject().trim());
        complaint.setDescription(request.description().trim());
        complaint.setCategory(parseCategory(request.category()));

        Complaint.Priority priority = request.priority() == null
                || request.priority().isBlank()
                ? Complaint.Priority.MEDIUM
                : parsePriority(request.priority());

        complaint.setPriority(priority);
        complaint.setStatus(Complaint.Status.PENDING);

        return ComplaintResponse.from(complaints.save(complaint));
    }

    @Transactional
    public List<ComplaintResponse> getMyComplaints(Long hostlerId) {
        return complaints.findAllByHostler_IdOrderByCreatedAtDesc(hostlerId)
                .stream()
                .map(ComplaintResponse::from)
                .toList();
    }

    @Transactional
    public ComplaintResponse getMyComplaint(Long hostlerId, Long id) {
        Complaint complaint = complaints.findByIdAndHostler_Id(id, hostlerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Complaint not found."
                ));

        return ComplaintResponse.from(complaint);
    }

    @Transactional
    public List<ComplaintResponse> getHostelComplaints(
            String email,
            Long hostelId,
            String status
    ) {
        hostelAccess.requireCanManageHostel(email, hostelId);

        List<Complaint> records;

        if (status == null || status.isBlank()
                || "ALL".equalsIgnoreCase(status)) {
            records = complaints
                    .findAllByHostel_IdOrderByCreatedAtDesc(hostelId);
        } else {
            records = complaints
                    .findAllByHostel_IdAndStatusOrderByCreatedAtDesc(
                            hostelId, parseStatus(status)
                    );
        }

        return records.stream().map(ComplaintResponse::from).toList();
    }

    @Transactional
    public ComplaintResponse updateStatus(
            String email,
            Long hostelId,
            Long complaintId,
            UpdateComplaintRequest request
    ) {
        hostelAccess.requireCanManageHostel(email, hostelId);

        Complaint complaint = complaints
                .findByIdAndHostel_Id(complaintId, hostelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Complaint not found for this hostel."
                ));

        Complaint.Status nextStatus = parseStatus(request.status());
        complaint.setStatus(nextStatus);

        String note = request.adminNote();
        complaint.setAdminNote(
                note == null || note.isBlank() ? null : note.trim()
        );

        return ComplaintResponse.from(complaints.save(complaint));
    }

    private Complaint.Category parseCategory(String value) {
        try {
            return Complaint.Category.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid complaint category."
            );
        }
    }

    private Complaint.Priority parsePriority(String value) {
        try {
            return Complaint.Priority.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid complaint priority."
            );
        }
    }

    private Complaint.Status parseStatus(String value) {
        try {
            return Complaint.Status.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid complaint status."
            );
        }
    }
}
