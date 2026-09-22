package com.example.techexchange.dto.request;

import com.example.techexchange.entity.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRoleUpdateRequest {

    @NotNull(message = "Роль обов'язкова")
    private UserRole role;
}
