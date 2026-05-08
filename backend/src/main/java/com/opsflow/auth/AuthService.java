package com.opsflow.auth;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import com.opsflow.user.Role;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = findEnabledUserByEmail(request.email());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                getSortedRoleNames(user),
                "Login successful"
        );
    }

    public CurrentUserResponse getCurrentUser(String email) {
        AppUser user = findEnabledUserByEmail(email);

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                getSortedRoleNames(user)
        );
    }

    private AppUser findEnabledUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    private List<String> getSortedRoleNames(AppUser user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .sorted(Comparator.naturalOrder())
                .toList();
    }
}
