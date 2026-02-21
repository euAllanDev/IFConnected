package com.ifconnected.controller;

import com.ifconnected.model.DTO.GoogleLoginDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.GoogleAuthService;
import com.ifconnected.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final GoogleAuthService googleAuthService;
    private final TokenService tokenService;

    public AuthController(UserService userService, GoogleAuthService googleAuthService, TokenService tokenService) {
        this.userService = userService;
        this.googleAuthService = googleAuthService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody User loginData) {
        return userService.login(loginData.getEmail());
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