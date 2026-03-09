package com.ifconnected.controller;

import com.ifconnected.model.DTO.DashboardDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.service.AdminService;
import com.ifconnected.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
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