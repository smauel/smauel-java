package org.smauel.permissions.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionDto {
    private Long id;
    private Long userId;
    private PermissionDto permission;
    private RoleDto role;
    private LocalDateTime expiresAt;
    private LocalDateTime grantedAt;
    private Long grantedBy;
}
