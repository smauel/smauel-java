package org.smauel.permissions.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.smauel.permissions.model.UserRoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleAssignmentRepository extends JpaRepository<UserRoleAssignment, Long> {
    List<UserRoleAssignment> findByUserId(Long userId);

    @Query("SELECT ura FROM UserRoleAssignment ura "
            + "JOIN FETCH ura.role r "
            + "WHERE ura.userId = :userId "
            + "AND (ura.expiresAt IS NULL OR ura.expiresAt > :now)")
    List<UserRoleAssignment> findActiveRoleAssignmentsByUserId(Long userId, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(ura) > 0 THEN true ELSE false END "
            + "FROM UserRoleAssignment ura "
            + "WHERE ura.userId = :userId AND ura.role.id = :roleId "
            + "AND (ura.expiresAt IS NULL OR ura.expiresAt > :now)")
    boolean existsActiveByUserIdAndRoleId(Long userId, Long roleId, LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(ura) > 0 THEN true ELSE false END "
            + "FROM UserRoleAssignment ura "
            + "JOIN ura.role r "
            + "JOIN r.permissions p "
            + "WHERE ura.userId = :userId AND p.name = :permissionName "
            + "AND (ura.expiresAt IS NULL OR ura.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasPermissionThroughRoles(Long userId, String permissionName);

    void deleteByUserIdAndRole_Id(Long userId, Long roleId);

    void deleteByUserId(Long userId);
}
