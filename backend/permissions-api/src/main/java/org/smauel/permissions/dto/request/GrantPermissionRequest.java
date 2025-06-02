package org.smauel.permissions.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantPermissionRequest {
    @NotBlank(message = "Permission name is required") private String permissionName;

    @NotNull(message = "Granted by is required") private Long grantedBy;

    private LocalDateTime expiresAt;
}
