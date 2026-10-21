package com.hostelhub.complaint.entity;

import com.hostelhub.hostel.entity.Hostel;
import com.hostelhub.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "hostel_complaints",
        indexes = {
                @Index(name = "idx_complaint_hostel_status",
                        columnList = "hostel_id,status"),
                @Index(name = "idx_complaint_hostler",
                        columnList = "hostler_id,created_at")
        }
)
public class Complaint {

    public enum Category {
        FOOD, ROOM, WATER, ELECTRICITY, CLEANLINESS, PLUMBING, OTHER
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostler_id", nullable = false)
    private User hostler;

    @Column(nullable = false, length = 150)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "admin_note", length = 2000)
    private String adminNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = Status.PENDING;
        if (priority == null) priority = Priority.MEDIUM;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Hostel getHostel() { return hostel; }
    public void setHostel(Hostel hostel) { this.hostel = hostel; }
    public User getHostler() { return hostler; }
    public void setHostler(User hostler) { this.hostler = hostler; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
