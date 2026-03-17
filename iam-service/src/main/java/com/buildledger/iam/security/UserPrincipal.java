package com.buildledger.iam.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

/**
 * Holds the authenticated user context extracted from JWT claims.
 * Used as the principal object in Spring Security's SecurityContext.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private final Long userId;
    private final String email;
    private final String role;

    @Override
    public String getName() {
        return email;
    }
}
