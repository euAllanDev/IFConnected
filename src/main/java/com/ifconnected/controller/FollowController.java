package com.ifconnected.controller;

import com.ifconnected.dto.UserSummaryDTO;
import com.ifconnected.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin("*")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // ============================
    // Seguir / deixar de seguir
    // ============================

    @PostMapping("/{followerId}/{followedId}")
    public ResponseEntity<String> follow(
            @PathVariable Long followerId,
            @PathVariable Long followedId
    ) {
        return ResponseEntity.ok(followService.follow(followerId, followedId));
    }

    @DeleteMapping("/{followerId}/{followedId}")
    public ResponseEntity<String> unfollow(
            @PathVariable Long followerId,
            @PathVariable Long followedId
    ) {
        return ResponseEntity.ok(followService.unfollow(followerId, followedId));
    }

    // ============================
    // IDs básicos
    // ============================

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<Long>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<Long>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    // ============================
    // Dados completos
    // ============================

    @GetMapping("/following/details/{userId}")
    public ResponseEntity<List<UserSummaryDTO>> getFollowingDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowingDetails(userId));
    }

    @GetMapping("/followers/details/{userId}")
    public ResponseEntity<List<UserSummaryDTO>> getFollowersDetails(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowersDetails(userId));
    }

    // ============================
    // Contadores
    // ============================

    @GetMapping("/count/followers/{userId}")
    public ResponseEntity<Long> countFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.countFollowers(userId));
    }

    @GetMapping("/count/following/{userId}")
    public ResponseEntity<Long> countFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.countFollowing(userId));
    }

    // ============================
    // Check follow
    // ============================

    @GetMapping("/is-following/{followerId}/{followedId}")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long followerId,
            @PathVariable Long followedId
    ) {
        return ResponseEntity.ok(followService.isFollowing(followerId, followedId));
    }
}
