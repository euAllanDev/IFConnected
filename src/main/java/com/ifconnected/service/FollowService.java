package com.ifconnected.service;

import com.ifconnected.dto.UserSummaryDTO;
import com.ifconnected.repository.jdbc.FollowRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    // ============================
    // Seguir / deixar de seguir
    // ============================

    public String follow(Long followerId, Long followedId) {

        if (followerId.equals(followedId)) {
            return "Você não pode seguir você mesmo.";
        }

        if (followRepository.isFollowing(followerId, followedId)) {
            return "Você já segue este usuário.";
        }

        followRepository.followUser(followerId, followedId);
        return "Agora você está seguindo este usuário!";
    }

    public String unfollow(Long followerId, Long followedId) {

        if (!followRepository.isFollowing(followerId, followedId)) {
            return "Você já não segue este usuário.";
        }

        followRepository.unfollowUser(followerId, followedId);
        return "Você deixou de seguir este usuário.";
    }

    // ============================
    // IDs básicos
    // ============================

    public List<Long> getFollowing(Long userId) {
        return followRepository.getFollowingIds(userId);
    }

    public List<Long> getFollowers(Long userId) {
        return followRepository.getFollowerIds(userId);
    }

    // ============================
    // Contadores
    // ============================

    public Long countFollowers(Long userId) {
        return followRepository.countFollowers(userId);
    }

    public Long countFollowing(Long userId) {
        return followRepository.countFollowing(userId);
    }

    // ============================
    // Dados completos
    // ============================

    public List<UserSummaryDTO> getFollowersDetails(Long userId) {
        return followRepository.getFollowersDetails(userId);
    }

    public List<UserSummaryDTO> getFollowingDetails(Long userId) {
        return followRepository.getFollowingDetails(userId);
    }

    // ============================
    // Check follow
    // ============================

    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.isFollowing(followerId, followedId);
    }
}
