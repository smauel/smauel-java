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
public class UserRoleAssignmentDto {
    private Long id;
    private Long userId;
    private RoleDto role;
    private LocalDateTime assignedAt;
    private Long assignedBy;
    private LocalDateTime expiresAt;
}
