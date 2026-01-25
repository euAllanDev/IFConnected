package com.ifconnected.mapper;

import com.ifconnected.model.JDBC.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {

        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .bio(rs.getString("bio"))
                .profileImageUrl(rs.getString("profile_image_url"))
                .campusId(rs.getLong("campus_id"))
                .build();
    }
}
