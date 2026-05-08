package com.opsflow.auth;

import java.util.List;

public record CurrentUserResponse(
        Long userId,
        String email,
        String displayName,
        List<String> roles
) {
}
