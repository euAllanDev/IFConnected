package com.ifconnected.controller;

import com.ifconnected.model.DTO.RegisterDTO;
import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.service.GeoFeedService;
import com.ifconnected.service.MinioService;
import com.ifconnected.service.NotificationService;
import com.ifconnected.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final MinioService minioService;
    private final GeoFeedService geoFeedService;
    private final NotificationService notificationService;

    public UserController(UserService userService, MinioService minioService,
                          GeoFeedService geoFeedService, NotificationService notificationService) {
        this.userService = userService;
        this.minioService = minioService;
        this.geoFeedService = geoFeedService;
        this.notificationService = notificationService;
    }

    @GetMapping("/users")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/users")
    public UserResponseDTO createUser(@RequestBody RegisterDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setBio(dto.getBio());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setCampusId(dto.getCampusId());
        user.setRole(dto.getRole());
        return userService.createUser(user);
    }

    @GetMapping("/users/{id}")
    public UserResponseDTO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/users/{id}/profile")
    public UserProfileDTO getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    @PutMapping("/users/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody UpdateUserDTO dto) {
        return userService.updateUser(id, dto);
    }

    @PostMapping(value = "/users/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDTO uploadProfilePhoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = minioService.uploadImage(file);
        return userService.updateProfileImage(id, imageUrl);
    }

    @PatchMapping("/users/{id}/campus")
    public void updateUserCampus(@PathVariable Long id, @RequestBody Long campusId) {
        userService.updateCampus(id, campusId);
    }

    // --- SOCIAL & FOLLOW ---

    @GetMapping("/users/{followerId}/isFollowing/{followedId}")
    public boolean isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.isFollowing(followerId, followedId);
    }

    @DeleteMapping("/users/{followerId}/follow/{followedId}")
    public void unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.unfollow(followerId, followedId);
    }

    @PostMapping("/users/{followerId}/follow/{followedId}")
    public void followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        userService.follow(followerId, followedId);
        User follower = userService.getUserEntityById(followerId);
        notificationService.createNotification(followedId, followerId, follower.getUsername(), "FOLLOW", null);
    }

    @GetMapping("/users/{id}/suggestions")
    public List<User> getSuggestions(@PathVariable Long id, @RequestParam(defaultValue = "50") double radiusKm) {
        User user = userService.getUserEntityById(id);
        if (user.getCampusId() == null) return new ArrayList<>();
        return geoFeedService.getPeopleYouMightKnow(id, user.getCampusId(), radiusKm);
    }
}