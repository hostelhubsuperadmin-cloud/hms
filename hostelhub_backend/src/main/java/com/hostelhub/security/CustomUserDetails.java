package com.hostelhub.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.hostelhub.user.entity.User;

public class CustomUserDetails implements UserDetails {

    private final User user;
    private final Set<GrantedAuthority> authorities = new HashSet<>();

    public CustomUserDetails(User user) {
        this.user = user;

        user.getRoles().forEach(role -> {
            authorities.add(
                    new SimpleGrantedAuthority("ROLE_" + role.getName())
            );

            role.getPermissions().forEach(permission ->
                    authorities.add(
                            new SimpleGrantedAuthority(permission.getName())
                    )
            );
        });
    }

    public Long getId() {
        return user.getId();
    }

    public String getFullName() {
        return user.getFullName();
    }

    public Set<String> getRoleNames() {
        Set<String> names = new HashSet<>();
        user.getRoles().forEach(role -> names.add(role.getName()));
        return names;
    }

    public String getStatus() {
        return user.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"SUSPENDED".equals(user.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(user.getStatus());
    }
}