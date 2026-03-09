package com.ifconnected.service;

import com.ifconnected.exception.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioService minioService;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                       FollowRepository followRepository,
                       PostRepository postRepository,
                       PasswordEncoder passwordEncoder,
                       MinioService minioService,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
        this.minioService = minioService;
        this.notificationService = notificationService;
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
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        return new UserResponseDTO(user);
    }

    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return new UserResponseDTO(user);
    }

    public User getUserEntityById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }
        return user;
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UpdateUserDTO dto) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        if (dto.getUsername() != null) existingUser.setUsername(dto.getUsername());
        if (dto.getBio() != null) existingUser.setBio(dto.getBio());
        if (dto.getCampusId() != null) existingUser.setCampusId(dto.getCampusId());

        User updatedUser = userRepository.update(existingUser);
        return new UserResponseDTO(updatedUser);
    }

    @CacheEvict(value = "users", key = "#id")
    @Transactional
    public UserResponseDTO updateProfileImage(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo inválido"); // Vai dar 400 Bad Request
        }

        // O Service manda a foto pro MinIO
        String imageUrl = minioService.uploadImage(file);

        // O Service manda o repositório salvar a URL
        userRepository.updateProfileImage(id, imageUrl);

        User user = userRepository.findById(id);
        return new UserResponseDTO(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
    }

    @Transactional
    public void follow(Long followerId, Long followedId) {
        // 1. Salva no banco de relacionamentos (Postgres)
        followRepository.followUser(followerId, followedId);

        // 2. Dispara a notificação (Mongo)
        User follower = getUserEntityById(followerId);
        notificationService.createNotification(
                followedId,
                followerId,
                follower.getUsername(),
                "FOLLOW",
                null
        );
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
        if (user == null) throw new ResourceNotFoundException("Usuário não encontrado");

        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);
        long posts = postRepository.countByUserId(userId);

        return new UserProfileDTO(new UserResponseDTO(user), followers, following, posts);
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

}