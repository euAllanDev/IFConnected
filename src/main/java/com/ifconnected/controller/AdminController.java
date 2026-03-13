package com.ifconnected.controller;

import com.ifconnected.model.DTO.DashboardDTO;
import com.ifconnected.model.DTO.LoginDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.security.TokenService;
import com.ifconnected.security.UserLoginInfo;
import com.ifconnected.service.AdminService;
import com.ifconnected.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;


    public AdminController(AdminService adminService, UserService userService,  AuthenticationManager authenticationManager, TokenService tokenService) {
        this.adminService = adminService;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginDTO data) {
        // 1. Autentica
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // 2. Verifica se é realmente Admin
        UserLoginInfo userLoginInfo = (UserLoginInfo) auth.getPrincipal();
        if (!"ADMIN".equalsIgnoreCase(userLoginInfo.getUser().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Perfil sem privilégios administrativos.");
        }

        // 3. Gera Token (pode ser o mesmo ou um token com tempo de expiração menor)
        String token = tokenService.generateToken(userLoginInfo);

        // 4. Retorna
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", new UserResponseDTO(userLoginInfo.getUser()));

        // Log de auditoria (importante para painel admin)
        System.out.println("🚨 ADMIN LOGADO: " + data.getEmail() + " | IP: " + "IP_DO_CLIENTE");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam Long userId) {
        User user = userService.getUserEntityById(userId);

        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso restrito.");
        }

        return ResponseEntity.ok(adminService.getStats());
    }
}