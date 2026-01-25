package com.ifconnected.service;

import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.mongo.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    // Construtor com injeção de dependências
    // @Lazy no PasswordEncoder evita ciclo de dependência se ocorrer
    public UserService(UserRepository userRepository,
                       FollowRepository followRepository,
                       PostRepository postRepository,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + username);
        }
        return user;
    }

    // --- MÉTODOS DE NEGÓCIO ---

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        // System.out.println("Buscando usuário no banco de dados...");
        return userRepository.findById(id);
    }

    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.update(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
    }

    // --- SEGUIR / FOLLOW ---

    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.isFollowing(followerId, followedId);
    }

    public void follow(Long followerId, Long followedId) {
        followRepository.followUser(followerId, followedId);
    }

    public void unfollow(Long followerId, Long followedId) {
        followRepository.unfollowUser(followerId, followedId);
    }

    public List<Long> getFollowingIds(Long userId) {
        return followRepository.getFollowingIds(userId);
    }

    // --- GERAL ---

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);
        long posts = postRepository.countByUserId(userId);

        return new UserProfileDTO(user, followers, following, posts);
    }
}