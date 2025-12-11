package com.ifconnected.service;

import com.ifconnected.repository.jdbc.FollowRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

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
            return "Você não segue este usuário.";
        }

        followRepository.unfollowUser(followerId, followedId);
        return "Você deixou de seguir este usuário.";
    }

    public List<Long> getFollowing(Long userId) {
        return followRepository.getFollowingIds(userId);
    }

    public List<Long> getFollowers(Long userId) {
        return followRepository.getFollowerIds(userId);
    }
}
