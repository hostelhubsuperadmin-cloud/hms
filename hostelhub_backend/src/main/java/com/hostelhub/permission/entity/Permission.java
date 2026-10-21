
package com.hostelhub.permission.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    protected Permission() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}