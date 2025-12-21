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

    // RowMapper: Transforma dados do banco em Objeto Java
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("bio"),
                rs.getString("profile_image_url")
        );

        // Mapeia o campus_id se existir (evita NullPointer)
        long campusId = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusId);
        }
        return user;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        initializeTable(); // Garante que a tabela existe ao iniciar
    }

    // Cria a tabela automaticamente se não existir (Padrão e Seguro)
    private void initializeTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL UNIQUE,
                bio TEXT,
                profile_image_url TEXT,
                campus_id BIGINT
            )
        """;
        jdbc.execute(sql);
    }

    // Adicione esse método na classe UserRepository
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, email);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // Usuário não existe
        }
    }

    public User save(User user) {
        String sql = """
            INSERT INTO users (username, email, bio, profile_image_url, campus_id)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
        """;

        try {
            Long newId = jdbc.queryForObject(sql, Long.class,
                    user.getUsername(),
                    user.getEmail(),
                    user.getBio(),
                    user.getProfileImageUrl(),
                    user.getCampusId()
            );

            user.setId(newId);
            return user;

        } catch (DuplicateKeyException e) {
            // Aqui capturamos o erro do Postgres e lançamos um erro mais amigável
            throw new RuntimeException("Erro: O e-mail '" + user.getEmail() + "' já está cadastrado no sistema.");
        }
    }



    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbc.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null; // Ou lançar uma exceção personalizada
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

    // Atualiza apenas o Campus (Método auxiliar rápido)
    public void updateCampus(Long userId, Long campusId) {
        String sql = "UPDATE users SET campus_id = ? WHERE id = ?";
        jdbc.update(sql, campusId, userId);
    }

    // Busca usuários por lista de Campi
    public List<Long> findUserIdsByCampusIds(List<Long> campusIds) {
        if (campusIds.isEmpty()) return List.of();

        String inSql = String.join(",", Collections.nCopies(campusIds.size(), "?"));
        String sql = String.format("SELECT id FROM users WHERE campus_id IN (%s)", inSql);

        return jdbc.queryForList(sql, Long.class, campusIds.toArray());
    }

    // Sugestão de amigos (Pessoas dos campi vizinhos que eu NÃO sigo)
    public List<User> findSuggestions(Long myId, List<Long> nearbyCampusIds) {
        if (nearbyCampusIds.isEmpty()) return List.of();

        String inSql = String.join(",", Collections.nCopies(nearbyCampusIds.size(), "?"));

        // Seleciona users dos campi vizinhos EXCETO eu mesmo E quem eu já sigo
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

    // ... outros métodos ...

    // Listar todos os usuários
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, userRowMapper);
    }

}