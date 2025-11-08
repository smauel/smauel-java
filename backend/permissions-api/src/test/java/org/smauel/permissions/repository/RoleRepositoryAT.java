package org.smauel.permissions.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.smauel.permissions.model.Permission;
import org.smauel.permissions.model.Role;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest(showSql = false)
@DisplayName("Role Repository Acceptance Tests")
class RoleRepositoryAT {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create and save permissions
        Permission createUserPermission = permissionRepository.save(Permission.builder()
                .name("user:create")
                .description("Create a new user")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.CREATE)
                .build());

        Permission readUserPermission = permissionRepository.save(Permission.builder()
                .name("user:read")
                .description("Read user details")
                .type(PermissionType.RESOURCE)
                .resource("user")
                .action(Action.READ)
                .build());

        Permission readOrderPermission = permissionRepository.save(Permission.builder()
                .name("order:read")
                .description("Read order details")
                .type(PermissionType.FEATURE)
                .resource("order")
                .action(Action.READ)
                .build());

        // Create roles
        adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator role with full access")
                .permissions(Set.of(createUserPermission, readUserPermission, readOrderPermission))
                .build();

        userRole = Role.builder()
                .name("USER")
                .description("Standard user role with limited access")
                .permissions(Set.of(readUserPermission))
                .build();
    }

    @Test
    @DisplayName("save role should persist the role and assign an ID")
    void whenSaveRole_thenRoleIsPersistedWithId() {
        Role savedRole = roleRepository.save(adminRole);

        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isNotNull().isPositive();
        assertThat(savedRole.getName()).isEqualTo("ADMIN");

        Optional<Role> foundRoleOpt = roleRepository.findById(savedRole.getId());
        assertThat(foundRoleOpt).isPresent();
        assertThat(foundRoleOpt.get().getName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("save role with existing name should throw DataIntegrityViolationException")
    void whenSaveRoleWithExistingName_thenThrowsException() {
        roleRepository.save(adminRole);

        Role duplicateRole =
                Role.builder().name("ADMIN").description("A duplicate role").build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> roleRepository.saveAndFlush(duplicateRole),
                "Saving a role with a duplicate name should throw DataIntegrityViolationException");
    }

    @Test
    @DisplayName("save role with null name should throw DataIntegrityViolationException")
    void whenSaveRoleWithNullName_thenThrowsException() {
        Role roleWithNullName =
                Role.builder().name(null).description("A role with a null name").build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> roleRepository.saveAndFlush(roleWithNullName),
                "Saving a role with a null name should throw ConstraintViolationException");
    }

    @Test
    @DisplayName("findByName should return role when role with given name exists")
    void whenFindByName_thenReturnRole() {
        roleRepository.save(adminRole);

        Optional<Role> foundRoleOpt = roleRepository.findByName("ADMIN");

        assertThat(foundRoleOpt).isPresent();
        assertThat(foundRoleOpt.get().getDescription()).isEqualTo("Administrator role with full access");
    }

    @Test
    @DisplayName("findByIdWithPermissions should return role and its associated permissions")
    void whenFindByIdWithPermissions_thenReturnRoleWithPermissions() {
        Role savedAdminRole = roleRepository.save(adminRole);
        roleRepository.save(userRole);

        Optional<Role> foundRoleOpt = roleRepository.findByIdWithPermissions(savedAdminRole.getId());

        assertThat(foundRoleOpt).isPresent();
        Role foundRole = foundRoleOpt.get();
        assertThat(foundRole.getName()).isEqualTo("ADMIN");
        assertThat(foundRole.getPermissions()).hasSize(3);
        assertThat(foundRole.getPermissions())
                .extracting(Permission::getName)
                .containsExactlyInAnyOrder("user:create", "user:read", "order:read");
    }

    @Test
    @DisplayName("findByNameWithPermissions should return role and its associated permissions")
    void whenFindByNameWithPermissions_thenReturnRoleWithPermissions() {
        roleRepository.save(adminRole);
        roleRepository.save(userRole);

        Optional<Role> foundRoleOpt = roleRepository.findByNameWithPermissions("USER");

        assertThat(foundRoleOpt).isPresent();
        Role foundRole = foundRoleOpt.get();
        assertThat(foundRole.getName()).isEqualTo("USER");
        assertThat(foundRole.getPermissions()).hasSize(1);
        assertThat(foundRole.getPermissions().iterator().next().getName()).isEqualTo("user:read");
    }

    @Test
    @DisplayName("findByNameWithPermissions should return empty optional for non-existent role")
    void whenFindByNameWithPermissionsForNonExistentRole_thenReturnEmpty() {
        Optional<Role> foundRoleOpt = roleRepository.findByNameWithPermissions("NON_EXISTENT_ROLE");
        assertThat(foundRoleOpt).isNotPresent();
    }

    @Test
    @DisplayName("deleting a role should not delete associated permissions")
    void whenRoleIsDeleted_thenAssociatedPermissionsAreNotDeleted() {
        Role savedRole = roleRepository.save(adminRole);
        long permissionCountBeforeDelete = permissionRepository.count();
        assertThat(permissionCountBeforeDelete).isEqualTo(3);

        roleRepository.deleteById(savedRole.getId());
        roleRepository.flush();

        Optional<Role> deletedRoleOpt = roleRepository.findById(savedRole.getId());
        assertThat(deletedRoleOpt).isNotPresent();

        long permissionCountAfterDelete = permissionRepository.count();
        assertThat(permissionCountAfterDelete).isEqualTo(permissionCountBeforeDelete);
    }
}
