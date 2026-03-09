package com.ifconnected.controller;

import com.ifconnected.model.DTO.GoogleLoginDTO;
import com.ifconnected.model.DTO.LoginDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.GoogleAuthService;
import com.ifconnected.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;


    public AuthController(UserService userService, GoogleAuthService googleAuthService, TokenService tokenService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDTO data) {
        // Isso aqui é o que vai verificar se a senha está correta no banco
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // Se a senha estiver certa, pega o usuário do banco (UserLoginInfo)
        UserLoginInfo userLoginInfo = (UserLoginInfo) auth.getPrincipal();

        // Gera o JWT
        String token = tokenService.generateToken(userLoginInfo);

        // Monta a resposta
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", new UserResponseDTO(userLoginInfo.getUser()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@RequestBody GoogleLoginDTO dto) {
        User user = googleAuthService.authenticateGoogleUser(dto.token());
        String jwtToken = tokenService.generateToken(new UserLoginInfo(user));

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("user", new UserResponseDTO(user));

        return ResponseEntity.ok(response);
    }
}