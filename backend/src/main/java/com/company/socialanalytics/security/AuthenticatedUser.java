package com.company.socialanalytics.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {
    private final CurrentUser currentUser;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
        this.authorities = currentUser.authorities().stream().map(SimpleGrantedAuthority::new).toList();
    }

    public CurrentUser currentUser() {
        return currentUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return currentUser.email();
    }

    public UUID id() {
        return currentUser.id();
    }

    public Set<String> roles() {
        return currentUser.authorities();
    }
}
