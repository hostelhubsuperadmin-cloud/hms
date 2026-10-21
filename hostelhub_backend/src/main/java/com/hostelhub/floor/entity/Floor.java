
package com.hostelhub.floor.entity;

import com.hostelhub.block.entity.Block;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "floors",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_floors_block_number",
                columnNames = {"block_id", "floor_number"}
        ),
        indexes = {
                @Index(name = "idx_floors_block_id", columnList = "block_id"),
                @Index(
                        name = "idx_floors_block_status",
                        columnList = "block_id,status"
                )
        }
)
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FloorStatus status = FloorStatus.ACTIVE;

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

    public Block getBlock() { return block; }
    public void setBlock(Block block) { this.block = block; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public FloorStatus getStatus() { return status; }
    public void setStatus(FloorStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

