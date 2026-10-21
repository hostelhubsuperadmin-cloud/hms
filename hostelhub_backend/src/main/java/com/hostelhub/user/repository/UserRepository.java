
package com.hostelhub.user.repository;

import java.util.Optional;

import com.hostelhub.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT COUNT(DISTINCT u.id)
            FROM User u
            JOIN u.roles r
            WHERE r.name = :roleName
              AND u.status = :status
            """)
    long countUsersWithRoleAndStatus(
            @Param("roleName") String roleName,
            @Param("status") String status
    );
}
