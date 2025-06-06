package org.smauel.permissions.exception;

public class RoleAlreadyAssignedException extends RuntimeException {
    public RoleAlreadyAssignedException(Long userId, String roleName) {
        super(String.format("Role '%s' is already assigned to user with ID %d", roleName, userId));
    }
}
