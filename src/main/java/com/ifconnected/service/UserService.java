package com.ifconnected.service;


import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository; // Injeção do novo repositório

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateProfile(Long userId, String bio, String imageUrl) {
        userRepository.updateProfile(userId, bio, imageUrl);
    }

    // Método auxiliar para o Feed (pegar quem eu sigo)
    public java.util.List<Long> getFollowingIds(Long userId) {
        return followRepository.getFollowingIds(userId);
    }


    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        System.out.println("Buscando usuário no Postgres (sem cache)...");
        return userRepository.findById(id);
    }

    public void follow(Long followerId, Long followedId) {
        // Agora usamos o FollowRepository dedicado
        followRepository.followUser(followerId, followedId);
    }
}