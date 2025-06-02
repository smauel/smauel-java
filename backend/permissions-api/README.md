# Permissions API

The Permissions API is responsible for managing user permissions and roles on the platform. It provides endpoints for creating, updating, and deleting permissions, as well as assigning permissions to users directly or through roles.

## Features

- **Permission Management**: Create, read, update, and delete permissions
- **Role Management**: Create roles that bundle multiple permissions together
- **User Permission Assignment**: Grant permissions to users either directly or through roles
- **Permission Checking**: Verify if a user has a specific permission
- **Time-limited Permissions**: Support for permissions that expire after a set time

## API Endpoints

### Permissions

- `POST /api/v1/permissions` - Create a new permission
- `GET /api/v1/permissions` - Get all permissions
- `GET /api/v1/permissions/{id}` - Get permission by ID
- `GET /api/v1/permissions/name/{name}` - Get permission by name
- `GET /api/v1/permissions/resource/{resource}` - Get permissions by resource
- `GET /api/v1/permissions/type/{type}/resource/{resource}` - Get permissions by type and resource
- `DELETE /api/v1/permissions/{id}` - Delete a permission

### Roles

- `POST /api/v1/roles` - Create a new role
- `GET /api/v1/roles` - Get all roles
- `GET /api/v1/roles/{id}` - Get role by ID
- `GET /api/v1/roles/name/{name}` - Get role by name
- `PUT /api/v1/roles/{roleId}/permissions/{permissionId}` - Add a permission to a role
- `DELETE /api/v1/roles/{roleId}/permissions/{permissionId}` - Remove a permission from a role
- `DELETE /api/v1/roles/{id}` - Delete a role

### User Permissions

- `POST /api/v1/user-permissions/users/{userId}/permissions` - Grant a permission to a user
- `POST /api/v1/user-permissions/users/{userId}/roles` - Grant a role to a user
- `GET /api/v1/user-permissions/users/{userId}/permissions` - Get all permissions for a user
- `GET /api/v1/user-permissions/users/{userId}/permissions/{permissionName}/check` - Check if a user has a specific permission
- `DELETE /api/v1/user-permissions/{id}` - Revoke a specific user permission
- `DELETE /api/v1/user-permissions/users/{userId}` - Revoke all permissions from a user

## Data Model

- **Permission**: Represents a single permission that can be granted to a user
- **Role**: Represents a collection of permissions that can be assigned together
- **UserPermission**: Represents a permission granted to a user, either directly or through a role

## Integration with Users API

The Permissions API integrates with the Users API by referencing user IDs. When a user is created or deleted in the Users API, corresponding permissions should be managed appropriately.
