package com.ifconnected.security;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Busca no Banco
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + email);
        }

        // 2. CORREÇÃO: Retorna o nosso UserLoginInfo personalizado
        // Agora o "cast" lá no TokenService vai funcionar!
        return new UserLoginInfo(user);
    }
}