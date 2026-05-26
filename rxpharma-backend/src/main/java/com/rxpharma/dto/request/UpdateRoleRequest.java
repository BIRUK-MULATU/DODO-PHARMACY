package com.rxpharma.dto.request;

import com.rxpharma.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private User.Role role;
}