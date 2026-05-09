package com.opsflow.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AdminAccessController {

    @GetMapping("/api/admin/access-check")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, String> accessCheck() {
        return Map.of(
                "status", "AUTHORIZED",
                "scope", "MANAGER"
        );
    }
}
