package com.ifconnected.security;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // <--- ESSA ANOTAÇÃO É O QUE RESOLVE O ERRO DO BEAN
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Busca o usuário no seu repositório JDBC pelo email
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
        }

        // 2. Retorna o UserLoginInfo (que implementa UserDetails)
        // Isso permite que o Spring Security valide a senha e as permissões
        return new UserLoginInfo(user);
    }
}