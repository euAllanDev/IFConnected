package com.ifconnected.service;

import com.ifconnected.model.DTO.UpdateUserDTO;
import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.DTO.UserResponseDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import com.ifconnected.repository.mongo.PostRepository;
import com.ifconnected.security.UserPrincipal;
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

    // ✅ usado no register pra não disparar exception
    public boolean isEmailRegistered(String email) {
        return userRepository.findByEmail(email) != null;
    }

    // ✅ Spring Security usa isso no login
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com o email: " + email);
        }
        return new UserPrincipal(user);
    }

    // ✅ Register
    public UserResponseDTO createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        return toResponseDTO(saved);
    }

    // --- ENTIDADE (uso interno) ---

    @Cacheable(value = "users", key = "#id")
    public User getUserEntityById(Long id) {
        return userRepository.findById(id);
    }

    // --- DTO (uso externo/front) ---

    public UserResponseDTO getUserById(Long id) {
        User user = getUserEntityById(id);
        if (user == null) throw new RuntimeException("Usuário não encontrado");
        return toResponseDTO(user);
    }

    public List<UserResponseDTO> getAllUsersDTO() {
        return userRepository.findAll().stream().map(this::toResponseDTO).toList();
    }

    // ✅ update via DTO (seguro)
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

    // ✅ para upload de foto ou updates internos (entidade -> DTO)
    @CacheEvict(value = "users", key = "#user.id")
    public UserResponseDTO updateUserEntity(User user) {
        User updated = userRepository.update(user);
        return toResponseDTO(updated);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
    }

    // --- FOLLOW ---

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

    // --- PROFILE ---

    public UserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId);
        if (user == null) throw new RuntimeException("Usuário não encontrado");

        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);
        long posts = postRepository.countByUserId(userId);

        // ✅ seu UserProfileDTO agora usa UserResponseDTO
        return new UserProfileDTO(toResponseDTO(user), followers, following, posts);
    }

    // --- MAPPER ---

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),        // ✅ username normal (nome)
                user.getEmail(),           // ✅ email separado
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId()
        );
    }

    public UserResponseDTO toUserResponse(User user) {
        return toResponseDTO(user);
    }
}
