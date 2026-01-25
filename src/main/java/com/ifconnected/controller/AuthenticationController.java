package com.ifconnected.controller;

import com.ifconnected.config.OpenApiConfig;
import com.ifconnected.model.DTO.LoginDTO;
import com.ifconnected.model.DTO.LoginResponseDTO;
import com.ifconnected.model.DTO.RegisterDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserPrincipal;
import com.ifconnected.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Autenticação e registro")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Operation(summary = "Login", description = "Autentica com email e senha e retorna um JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO data) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    data.email(),
                    data.password()
            );

            var authentication = authenticationManager.authenticate(authToken);

            // ✅ Principal agora é UserPrincipal
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = principal.getUser();

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

    @Operation(summary = "Registro", description = "Cria um novo usuário com senha criptografada.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {

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
