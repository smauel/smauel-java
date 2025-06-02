package org.smauel.permissions.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequest {
    @NotBlank(message = "Permission name is required") private String name;

    private String description;

    @NotNull(message = "Permission type is required") private PermissionType type;

    private String resource;

    @NotNull(message = "Action is required") private Action action;
}
