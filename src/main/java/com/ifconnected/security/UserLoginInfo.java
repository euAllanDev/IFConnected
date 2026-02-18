package com.ifconnected.security;

import com.ifconnected.model.JDBC.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserLoginInfo implements UserDetails { // Adicionamos o implements de volta

    private final User user;

    public UserLoginInfo(User user) {
        this.user = user;
    }

    // --- Métodos Extras úteis para o seu código ---

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getDisplayUsername() {
        return user.getUsername();
    }

    public User getUser() {
        return user;
    }

    // --- MÉTODOS OBRIGATÓRIOS DO USERDETAILS (O que estava faltando) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pega o cargo do usuário (ADMIN ou STUDENT)
        String roleName = user.getRole() == null ? "STUDENT" : user.getRole();

        // O Spring Security exige que cargos comecem com "ROLE_"
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
        return user.getEmail(); // Usamos o e-mail como identificador de login
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