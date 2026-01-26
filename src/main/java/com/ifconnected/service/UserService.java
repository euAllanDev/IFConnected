package com.ifconnected.service;

import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.security.UserLoginInfo;
import org.springframework.cache.annotation.CacheEvict;
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

    public UserService(
            UserRepository userRepository,
            FollowRepository followRepository,
            PostRepository postRepository,
            @Lazy PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
        }
        return new UserLoginInfo(user);
    }

    public UserResponseDTO createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return toResponseDTO(saved);
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserEntityById(Long id) {
        return userRepository.findById(id);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = getUserEntityById(id);
        if (user == null) throw new RuntimeException("Usuário não encontrado");
        return toResponseDTO(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UpdateUserDTO dto) {
        User existing = userRepository.findById(id);
        if (existing == null) throw new RuntimeException("Usuário não encontrado");

        if (dto.username() != null && !dto.username().isBlank()) {
            existing.setUsername(dto.username().trim());
        }
        if (dto.bio() != null) {
            existing.setBio(dto.bio());
        }
        if (dto.campusId() != null) {
            existing.setCampusId(dto.campusId());
        }

        User updated = userRepository.update(existing);
        return toResponseDTO(updated);
    }

    @CacheEvict(value = "users", key = "#userId")
    public UserResponseDTO updateProfileImage(Long userId, String imageUrl) {
        User existing = userRepository.findById(userId);
        if (existing == null) throw new RuntimeException("Usuário não encontrado");

        userRepository.updateProfileImage(userId, imageUrl);

        // atualiza o objeto em memória (opcional, mas ajuda consistência)
        existing.setProfileImageUrl(imageUrl);
        return toResponseDTO(existing);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
    }

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

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) throw new RuntimeException("Usuário não encontrado");

        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);
        long posts = postRepository.countByUserId(userId);

        return new UserProfileDTO(toResponseDTO(user), followers, following, posts);
    }

    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId()
        );
    }
}
