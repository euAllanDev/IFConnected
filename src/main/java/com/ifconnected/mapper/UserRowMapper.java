package com.ifconnected.mapper;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();

        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setBio(rs.getString("bio"));
        user.setProfileImageUrl(rs.getString("profile_image_url"));

        // Trata campus_id para aceitar nulo
        long campusIdVal = rs.getLong("campus_id");
        if (!rs.wasNull()) {
            user.setCampusId(campusIdVal);
        } else {
            user.setCampusId(null);
        }

        // Trata o Role (Papel)
        try {
            String role = rs.getString("role");
            if (role != null) {
                user.setRole(role);
            } else {
                user.setRole("STUDENT"); // Padrão
            }
        } catch (SQLException e) {
            user.setRole("STUDENT"); // Se a coluna não existir, assume padrão
        }

        return user;
    }
}