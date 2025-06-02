package org.smauel.permissions.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.smauel.permissions.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUserId(Long userId);

    @Query("SELECT up FROM UserPermission up " + "JOIN FETCH up.permission "
            + "LEFT JOIN FETCH up.role "
            + "WHERE up.userId = :userId "
            + "AND (up.expiresAt IS NULL OR up.expiresAt > :now)")
    List<UserPermission> findActivePermissionsByUserId(Long userId, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserPermission up " + "JOIN up.permission p "
            + "WHERE up.userId = :userId AND p.name = :permissionName "
            + "AND (up.expiresAt IS NULL OR up.expiresAt > CURRENT_TIMESTAMP)")
    boolean existsByUserIdAndPermissionName(Long userId, String permissionName);
}
