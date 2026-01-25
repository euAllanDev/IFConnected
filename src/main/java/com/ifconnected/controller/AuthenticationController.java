package com.ifconnected.controller;

import com.ifconnected.model.DTO.LoginDTO;
import com.ifconnected.model.DTO.LoginResponseDTO;
import com.ifconnected.model.DTO.RegisterDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    // 🔐 LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO data) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    data.email(),
                    data.password()
            );

            var authentication = authenticationManager.authenticate(authToken);

            User user = (User) authentication.getPrincipal();
            String token = tokenService.generateToken(user);

            return ResponseEntity.ok(
                    new LoginResponseDTO(token, user.getId(), user.getUsername())
            );

        } catch (BadCredentialsException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou senha inválidos");
        }
    }

    // 🧾 REGISTRO
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {
        // Checagem de existência: NÃO use loadUserByUsername (ele lança exception)
        if (userService.isEmailRegistered(data.email())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("E-mail já cadastrado");
        }

        User newUser = new User();
        newUser.setUsername(data.username());
        newUser.setEmail(data.email());
        newUser.setPassword(data.password());
        newUser.setCampusId(data.campusId());

        userService.createUser(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
