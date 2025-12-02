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
        // Inicialização simples da tabela (num app real usaria Flyway)
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(255), email VARCHAR(255))");
        this.jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS follows (follower_id INT, followed_id INT, PRIMARY KEY(follower_id, followed_id))");
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email")
    );

    public User save(User user) {
        String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, user.getUsername(), user.getEmail());
        user.setId(id);
        return user;
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