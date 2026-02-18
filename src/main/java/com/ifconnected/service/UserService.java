package com.ifconnected.service;

import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.mongo.PostRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       FollowRepository followRepository,
                       PostRepository postRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO createUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("STUDENT");
        }

        User savedUser = userRepository.save(user);
        return new UserResponseDTO(savedUser);
    }

    public UserResponseDTO login(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado.");
        }
        return new UserResponseDTO(user);
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        return new UserResponseDTO(user);
    }

    // --- ESSE É O MÉTODO QUE O CONTROLLER TÁ PEDINDO E NÃO ACHAVA ---
    public User getUserEntityById(Long id) {
        return userRepository.findById(id);
    }
    // ----------------------------------------------------------------

    // Atualizado para retornar lista de DTOs (corrigindo erro de tipo)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    // Atualizado para aceitar o DTO e o ID (corrigindo erro de argumentos)
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UpdateUserDTO dto) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        if (dto.getUsername() != null) existingUser.setUsername(dto.getUsername());
        if (dto.getBio() != null) existingUser.setBio(dto.getBio());
        if (dto.getCampusId() != null) existingUser.setCampusId(dto.getCampusId());

        User updatedUser = userRepository.update(existingUser);
        return new UserResponseDTO(updatedUser);
    }

    // --- MÉTODO NOVO QUE O CONTROLLER PEDE ---
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateProfileImage(Long id, String imageUrl) {
        userRepository.updateProfileImage(id, imageUrl);
        User user = userRepository.findById(id);
        return new UserResponseDTO(user);
    }
    // -----------------------------------------

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
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

    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.isFollowing(followerId, followedId);
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) throw new RuntimeException("Usuário não encontrado");

        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);
        long posts = postRepository.countByUserId(userId);

        // CORREÇÃO DO ERRO DE TIPO (Passo 4)
        return new UserProfileDTO(new UserResponseDTO(user), followers, following, posts);
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }


}