package com.ifconnected.security;

import com.ifconnected.model.JDBC.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserLoginInfo implements UserDetails {

    private final User user;

    public UserLoginInfo(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    // ✅ username do perfil (handle)
    public String getDisplayUsername() {
        return user.getUsername();
    }

    // ✅ email real
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // ✅ Spring Security usa isso como "username" de autenticação
    // e aqui deve ser o EMAIL porque login é por email
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
