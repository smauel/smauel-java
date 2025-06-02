package org.smauel.permissions.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.smauel.permissions.model.enums.Action;
import org.smauel.permissions.model.enums.PermissionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    private Long id;
    private String name;
    private String description;
    private PermissionType type;
    private String resource;
    private Action action;
    private LocalDateTime createdAt;
}
