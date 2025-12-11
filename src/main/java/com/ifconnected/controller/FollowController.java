package com.ifconnected.controller;

import com.ifconnected.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follow")
@CrossOrigin("*")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{followerId}/{followedId}")
    public ResponseEntity<String> follow(
            @PathVariable Long followerId,
            @PathVariable Long followedId) {

        return ResponseEntity.ok(followService.follow(followerId, followedId));
    }

    @DeleteMapping("/{followerId}/{followedId}")
    public ResponseEntity<String> unfollow(
            @PathVariable Long followerId,
            @PathVariable Long followedId) {

        return ResponseEntity.ok(followService.unfollow(followerId, followedId));
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<Long>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<Long>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }
}
