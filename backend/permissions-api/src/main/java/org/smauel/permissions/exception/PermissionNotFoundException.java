package org.smauel.permissions.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(String permissionName) {
        super("Permission not found: " + permissionName);
    }

    public PermissionNotFoundException(Long permissionId) {
        super("Permission not found with id: " + permissionId);
    }
}
