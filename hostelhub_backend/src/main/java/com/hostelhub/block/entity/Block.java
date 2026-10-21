
package com.hostelhub.block.entity;

import com.hostelhub.hostel.entity.Hostel;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "blocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_blocks_hostel_code",
                columnNames = {"hostel_id", "code"}
        ),
        indexes = {
                @Index(name = "idx_blocks_hostel_id", columnList = "hostel_id"),
                @Index(
                        name = "idx_blocks_hostel_status",
                        columnList = "hostel_id,status"
                )
        }
)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlockStatus status = BlockStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Hostel getHostel() { return hostel; }
    public void setHostel(Hostel hostel) { this.hostel = hostel; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }
    public BlockStatus getStatus() { return status; }
    public void setStatus(BlockStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

