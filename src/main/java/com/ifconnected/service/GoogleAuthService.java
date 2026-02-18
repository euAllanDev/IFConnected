package com.ifconnected.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
public class GoogleAuthService {

    @Value("${google.client.id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final UserService userService;

    public GoogleAuthService(UserRepository userRepository, TokenService tokenService, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * Valida o token do Google e retorna a entidade User (buscada ou criada).
     * Mudei o retorno de String para User para o Controller ter acesso aos dados.
     */
    public User authenticateGoogleUser(String googleToken) {
        try {
            // 1. Validar Token do Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                throw new RuntimeException("Token do Google inválido.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // 2. Buscar ou Criar Usuário
            User user = userRepository.findByEmail(email);

            if (user == null) {
                user = new User();
                // Gera username amigável e único
                String safeName = name.toLowerCase().replace(" ", "_").replaceAll("[^a-z0-9_]", "");
                user.setUsername(safeName + "_" + UUID.randomUUID().toString().substring(0, 4));
                user.setEmail(email);
                user.setProfileImageUrl(pictureUrl);
                user.setBio("Estudante via Google");
                user.setRole("STUDENT");
                user.setCampusId(null); // Deixa nulo para o Front pedir a escolha do campus

                // Senha aleatória forte
                user.setPassword(UUID.randomUUID().toString());

                // Salva no banco (o userService cuida da criptografia da senha dummy)
                userService.createUser(user);

                // Busca o user recém-criado para ter o ID preenchido
                user = userRepository.findByEmail(email);
            }

            // 3. Retorna o objeto User completo
            return user;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro Google Auth: " + e.getMessage());
        }
    }
}