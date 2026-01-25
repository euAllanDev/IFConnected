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

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 🔑 RowMapper CORRETO (inclui password)
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password")); // 🔥 ESSENCIAL
        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        long campusId = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusId);
        }

        return user;
    };

    // --- BUSCAS ---

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

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users", userRowMapper);
    }

    // --- SAVE ---

    public User save(User user) {
        String sql = """
            INSERT INTO users (username, email, password, bio, profile_image_url, campus_id)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Long id = jdbc.queryForObject(
                    sql,
                    Long.class,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getBio(),
                    user.getProfileImageUrl(),
                    user.getCampusId()
            );

            user.setId(id);
            return user;

        } catch (DuplicateKeyException e) {
            throw new RuntimeException(
                    "Erro: O e-mail '" + user.getEmail() + "' já está cadastrado."
            );
        }
    }

    // --- UPDATE ---

    public User update(User user) {
        String sql = """
            UPDATE users
            SET username = ?, email = ?, bio = ?, profile_image_url = ?, campus_id = ?
            WHERE id = ?
        """;

        jdbc.update(
                sql,
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
        jdbc.update(
                "UPDATE users SET campus_id = ? WHERE id = ?",
                campusId,
                userId
        );
    }

    // --- SUGESTÕES ---

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
            AND id NOT IN (
                SELECT followed_id FROM follows WHERE follower_id = ?
            )
            LIMIT 10
        """, inSql);

        List<Object> args = new ArrayList<>(nearbyCampusIds);
        args.add(myId);
        args.add(myId);

        return jdbc.query(sql, userRowMapper, args.toArray());
    }
}
