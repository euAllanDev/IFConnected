package com.ifconnected.security;

import com.ifconnected.repository.jdbc.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);

        if(token != null){
            try {
                var login = tokenService.validateToken(token); // Retorna o email

                if (login != null && !login.isEmpty()) {
                    // BUSCA USUÁRIO
                    var user = userRepository.findByEmail(login);

                    if (user != null) {
                        // Se chegou aqui, as permissões (getAuthorities) são aplicadas
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                new UserLoginInfo(user).getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ [Filtro] Usuário autenticado: " + login);
                    } else {
                        System.out.println("❌ [Filtro] Email válido no token, mas não encontrado no Banco: " + login);
                    }
                } else {
                    System.out.println("❌ [Filtro] Token inválido ou expirado.");
                }
            } catch (Exception e) {
                // Se houver erro de serialização do Redis, ele vai aparecer aqui!
                System.err.println("💥 [Filtro] Erro crítico na autenticação: " + e.getMessage());
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}