package org.smauel.permissions.repository;

import java.util.List;
import java.util.Optional;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.enums.PermissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    List<Permission> findByResource(String resource);

    List<Permission> findByTypeAndResource(PermissionType type, String resource);
}
