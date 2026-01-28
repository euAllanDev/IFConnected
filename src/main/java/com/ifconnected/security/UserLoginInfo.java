package com.ifconnected.security;

import com.ifconnected.model.JDBC.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserLoginInfo implements UserDetails {

    private final User user;

    public UserLoginInfo(User user) {
        this.user = user;
    }

    // --- Métodos Extras (Úteis para o Controller) ---

    public Long getId() {
        return user.getId();
    }

    public User getUser() {
        return user;
    }

    public String getEmail() {
        return user.getEmail();
    }

    // ✅ CORREÇÃO: Adicionado para resolver o erro no AuthenticationController
    public String getDisplayUsername() {
        return user.getUsername();
    }

    // --- Métodos Obrigatórios do UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName = user.getRole() == null ? "STUDENT" : user.getRole();
        // Garante o prefixo ROLE_ exigido pelo Spring Security
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // O Spring usa isso como identificador principal (Login)
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}