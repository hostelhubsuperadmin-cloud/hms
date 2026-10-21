package com.hostelhub.menu.entity;

import com.hostelhub.hostel.entity.Hostel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;

@Entity
@Table(
        name = "hostel_menu_items",
        indexes = {
                @Index(name = "idx_menu_hostel_day", columnList = "hostel_id,day_of_week"),
                @Index(name = "idx_menu_public", columnList = "hostel_id,day_of_week,is_available")
        }
)
public class HostelMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hostel_id", nullable = false)
    private Hostel hostel;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HostelMenuItem() {}

    public HostelMenuItem(
            Hostel hostel,
            DayOfWeek dayOfWeek,
            String mealType,
            String name,
            String description,
            BigDecimal price,
            boolean available
    ) {
        this.hostel = hostel;
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }

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
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public String getMealType() { return mealType; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(
            DayOfWeek dayOfWeek,
            String mealType,
            String name,
            String description,
            BigDecimal price,
            boolean available
    ) {
        this.dayOfWeek = dayOfWeek;
        this.mealType = mealType;
        this.name = name;
        this.description = description;
        this.price = price;
        this.available = available;
    }
}
