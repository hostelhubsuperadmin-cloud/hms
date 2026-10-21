package com.hostelhub.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hostelhub.permission.entity.Permission;

public interface PermissionRepository
        extends JpaRepository<Permission, Long> {
}
