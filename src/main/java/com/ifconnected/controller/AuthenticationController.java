package com.ifconnected.controller;

import com.ifconnected.model.DTO.LoginDTO;
import com.ifconnected.model.DTO.LoginResponseDTO;
import com.ifconnected.model.DTO.RegisterDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Autenticação (login/registro)")
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            UserService userService,
            TokenService tokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    // 🔐 LOGIN
    @Operation(
            summary = "Login do usuário",
            description = "Autentica com email e senha e retorna um JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginDTO data) {

        var authToken = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var authentication = authenticationManager.authenticate(authToken);

        UserLoginInfo principal = (UserLoginInfo) authentication.getPrincipal();

        String token = tokenService.generateToken(principal.getEmail());

        return ResponseEntity.ok(
                new LoginResponseDTO(token, principal.getId(), principal.getDisplayUsername())
        );
    }


    // 🧾 REGISTRO
    @Operation(
            summary = "Registro de usuário",
            description = "Cria um usuário novo. Email e username devem ser únicos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {

        // evita estourar exception do loadUserByUsername
        if (userService.isEmailRegistered(data.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("E-mail já cadastrado");
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
