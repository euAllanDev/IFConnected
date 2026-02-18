package com.ifconnected.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // O fallback ":meu-segredo-super-secreto" garante que se a prop não existir, use este.
    @Value("${api.security.token.secret:meu-segredo-super-secreto}")
    private String secret;

    public String generateToken(UserLoginInfo userLoginInfo) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("ifconnected-api")
                    .withSubject(userLoginInfo.getEmail())
                    .withClaim("id", userLoginInfo.getId())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            System.out.println("Token gerado com sucesso para: " + userLoginInfo.getEmail());
            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String subject = JWT.require(algorithm)
                    .withIssuer("ifconnected-api")
                    .build()
                    .verify(token)
                    .getSubject();

            return subject; // Se chegou aqui, o token é válido e retorna o email
        } catch (JWTVerificationException exception) {
            // LOG IMPORTANTE: Se o 403 persistir, veja o erro no console do Java
            System.err.println("Falha na validação do Token: " + exception.getMessage());
            return "";
        }
    }

    private Instant genExpirationDate() {
        // Expira em 2 horas.
        // Verifique se o relógio do seu Windows está certo, caso contrário o token "nasce" expirado.
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}