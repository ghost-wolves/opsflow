package com.opsflow.auth;

import java.util.List;

public record LoginResponse(
        Long userId,
        String email,
        String displayName,
        List<String> roles,
        String message
) {
}
