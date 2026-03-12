package com.ifconnected.controller;

import com.ifconnected.model.DTO.*;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.*;
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
    private final AuthenticationManager authenticationManager; // ✅ Necessário

    public AuthController(UserService userService,
                          GoogleAuthService googleAuthService,
                          TokenService tokenService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginDTO loginData) {
        // 1. O Spring Security valida a senha criptografada aqui
        var authToken = new UsernamePasswordAuthenticationToken(loginData.getEmail(), loginData.getPassword());
        var authentication = authenticationManager.authenticate(authToken);

        // 2. Se chegou aqui, a senha é válida. Pegamos o UserLoginInfo (que é o Principal)
        UserLoginInfo userLoginInfo = (UserLoginInfo) authentication.getPrincipal();

        // 3. Geramos o Token
        String jwtToken = tokenService.generateToken(userLoginInfo);

        // 4. Retornamos Token + Usuário (UserResponseDTO)
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
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