package com.example.elder.web;

import com.example.elder.domain.Role;

public class RoleResolver {
    public static Role resolve(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new ForbiddenException("missing_X_Role");
        }
        try {
            return Role.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException("invalid_role: " + roleHeader);
        }
    }
}

