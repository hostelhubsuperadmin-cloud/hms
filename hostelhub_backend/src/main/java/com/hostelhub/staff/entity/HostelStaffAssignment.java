package com.hostelhub.staff.entity;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "hostel_staff_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_hostel_staff_hostel_user",
                columnNames = {"hostel_id", "user_id"}
        )
)
public class HostelStaffAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @Column(name = "staff_role", nullable = false, length = 20)
    private String staffRole;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    protected HostelStaffAssignment() {
    }

    public HostelStaffAssignment(
            Hostel hostel,
            User user,
            User assignedBy,
            String staffRole) {
        this.hostel = hostel;
        this.user = user;
        this.assignedBy = assignedBy;
        this.staffRole = staffRole;
    }

    @PrePersist
    void onCreate() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Hostel getHostel() {
        return hostel;
    }

    public User getUser() {
        return user;
    }

    public User getAssignedBy() {
        return assignedBy;
    }

    public String getStaffRole() {
        return staffRole;
    }
    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }
    public Instant getAssignedAt() {
        return assignedAt;
    }
}
