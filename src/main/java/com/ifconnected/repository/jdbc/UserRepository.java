package com.ifconnected.repository.jdbc;

import com.ifconnected.model.JDBC.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    // --- 1. CORREÇÃO NO MAPPER (Para ler a senha quando fizer login) ---
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"), // <--- IMPORTANTE: Lendo a senha
                rs.getString("bio"),
                rs.getString("profile_image_url"),
                rs.getLong("campus_id"),
                rs.getString("role")      // <--- IMPORTANTE: Lendo a role
        );

        // Tratamento para campus_id nulo
        long campusId = rs.getLong("campus_id");
        if (rs.wasNull()) {
            user.setCampusId(null);
        } else {
            user.setCampusId(campusId);
        }
        return user;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        // Não precisa mais de initializeTable(), o Liquibase cuida disso.
    }

    // --- 2. CORREÇÃO NO SAVE (Para GRAVAR a senha no banco) ---
    public User save(User user) {
        // Adicionamos 'password' e 'role' no SQL
        String sql = """
            INSERT INTO users (username, email, password, bio, profile_image_url, campus_id, role)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Long newId = jdbc.queryForObject(sql, Long.class,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(), // <--- AQUI: Passando a senha criptografada
                    user.getBio(),
                    user.getProfileImageUrl(),
                    user.getCampusId(),
                    user.getRole()
            );

            user.setId(newId);
            return user;

        } catch (DuplicateKeyException e) {
            throw new RuntimeException("Erro: O e-mail '" + user.getEmail() + "' já está cadastrado.");
        }
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public User update(User user) {
        String sql = """
            UPDATE users 
            SET username = ?, email = ?, bio = ?, profile_image_url = ?, campus_id = ?
            WHERE id = ?
        """;

        jdbc.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCampusId(),
                user.getId()
        );

        return user;
    }

    public void updateCampus(Long userId, Long campusId) {
        String sql = "UPDATE users SET campus_id = ? WHERE id = ?";
        jdbc.update(sql, campusId, userId);
    }

    public void updateProfileImage(Long userId, String imageUrl) {
        String sql = "UPDATE users SET profile_image_url = ? WHERE id = ?";
        jdbc.update(sql, imageUrl, userId);
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, userRowMapper);
    }

    // Métodos auxiliares de Feed/Geo
    public List<Long> findUserIdsByCampusIds(List<Long> campusIds) {
        if (campusIds.isEmpty()) return List.of();
        String inSql = String.join(",", Collections.nCopies(campusIds.size(), "?"));
        String sql = String.format("SELECT id FROM users WHERE campus_id IN (%s)", inSql);
        return jdbc.queryForList(sql, Long.class, campusIds.toArray());
    }

    public List<User> findSuggestions(Long myId, List<Long> nearbyCampusIds) {
        if (nearbyCampusIds.isEmpty()) return List.of();
        String inSql = String.join(",", Collections.nCopies(nearbyCampusIds.size(), "?"));
        String sql = String.format("""
            SELECT * FROM users 
            WHERE campus_id IN (%s) 
            AND id != ? 
            AND id NOT IN (SELECT followed_id FROM follows WHERE follower_id = ?)
            LIMIT 10
        """, inSql);
        List<Object> args = new ArrayList<>(nearbyCampusIds);
        args.add(myId);
        args.add(myId);
        return jdbc.query(sql, userRowMapper, args.toArray());
    }
}