package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        // Atualiza a tabela se ela já existir (Adiciona colunas se faltarem)
        // Nota: Em produção usaria Flyway, aqui vamos no modo "gambiarra chique"
        createTableIfNotExists();
        try {
            this.jdbcTemplate.execute("ALTER TABLE users ADD COLUMN bio VARCHAR(500)");
            this.jdbcTemplate.execute("ALTER TABLE users ADD COLUMN profile_image_url VARCHAR(500)");
        } catch (Exception e) {
            // Ignora erro se as colunas já existirem
        }
    }

    private void createTableIfNotExists() {
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(255), email VARCHAR(255))");
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS follows (follower_id INT, followed_id INT, PRIMARY KEY(follower_id, followed_id))");
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        // Mapeia novos campos (pode vir null)
        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));
        return user;
    };

    public User save(User user) {
        String sql = "INSERT INTO users (username, email, bio, profile_image_url) VALUES (?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, user.getUsername(), user.getEmail(), user.getBio(), user.getProfileImageUrl());
        user.setId(id);
        return user;
    }

    // Novo método para atualizar perfil
    public void update(User user) {
        String sql = "UPDATE users SET bio = ?, profile_image_url = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getBio(), user.getProfileImageUrl(), user.getId());
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, id);
    }


    // Segunda entidade JDBC: Seguir usuário
    public void followUser(Long followerId, Long followedId) {
        String sql = "INSERT INTO follows (follower_id, followed_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, followerId, followedId);
    }
}