package com.ifconnected.service;

import com.ifconnected.model.DTO.UserProfileDTO;
import com.ifconnected.model.JDBC.User;
import com.ifconnected.repository.jdbc.FollowRepository;
import com.ifconnected.repository.jdbc.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.ifconnected.repository.mongo.PostRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    public UserService(UserRepository userRepository,
                       FollowRepository followRepository,
                       PostRepository postRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
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

    @CacheEvict(value = "users", key = "#userId")
    public void updateCampus(Long userId, Long campusId) {
        userRepository.updateCampus(userId, campusId);
    }

    // Lógica de Seguir
    public void follow(Long followerId, Long followedId) {
        followRepository.followUser(followerId, followedId);
    }

    // Lista de quem o usuário segue (usado para o Feed de Amigos)
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.getFollowingIds(userId);
    }

    public User login(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) throw new RuntimeException("Usuário não encontrado.");
        return user;
    }


    // --- O MÉTODO QUE FALTAVA (Correção do Erro) ---
    // Monta o Perfil Completo juntando dados do Postgres e do Mongo
    public UserProfileDTO getUserProfile(Long userId) {
        // 1. Busca usuário
        // Nota: Chamamos userRepository direto para garantir dados frescos,
        // mas poderia ser getUserById(userId) se quiser usar cache.
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        // 2. Busca estatísticas no Postgres (FollowRepository)
        int followers = followRepository.countFollowers(userId);
        int following = followRepository.countFollowing(userId);

        // 3. Busca estatísticas no Mongo (PostRepository)
        long posts = postRepository.countByUserId(userId);

        // 4. Retorna o DTO montado
        return new UserProfileDTO(user, followers, following, posts);
    }

    // Expõe o repositório se necessário (opcional, mas o Controller pedia antes)
    public UserRepository getUserRepository() {
        return userRepository;
    }
}