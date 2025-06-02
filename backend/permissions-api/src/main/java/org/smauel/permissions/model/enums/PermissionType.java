package org.smauel.permissions.model.enums;

/**
 * Types of permissions that can be assigned to users
 */
public enum PermissionType {
    SYSTEM, // System-level permissions (admin access, etc.)
    RESOURCE, // Resource-specific permissions (access to specific entities)
    FEATURE // Feature-based permissions (access to specific features/functions)
}
