package com.pding.paymentservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserDetailsImpl implements UserDetails {
    private final Long userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // You can return null or an empty string here since you won't need a password for JWT-based authentication
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Implement the remaining UserDetails methods as needed

    @Override
    public boolean isAccountNonExpired() {
        return true; // You can customize this as needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // You can customize this as needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // You can customize this as needed
    }

    @Override
    public boolean isEnabled() {
        return true; // You can customize this as needed
    }
}