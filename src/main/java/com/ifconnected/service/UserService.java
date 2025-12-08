package com.ifconnected.service;

import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    // Cria usuário no Postgres
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // Busca usuário (Primeiro no Redis, se não achar, vai no Postgres)
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        System.out.println("Buscando usuário no banco de dados (não estava no cache)...");
        return userRepository.findById(id);
    }

    // --- MÉTODO CORRIGIDO ---
    // Atualiza usuário no Postgres e atualiza o Cache Redis com o novo valor
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        // Agora chamamos o método 'update' unificado, que já salva Bio e Foto
        return userRepository.update(user);
    }

    // Lógica de Seguir
    public void follow(Long followerId, Long followedId) {
        followRepository.followUser(followerId, followedId);
    }

    // Lista de quem o usuário segue (usado para o Feed de Amigos)
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.getFollowingIds(userId);
    }
}