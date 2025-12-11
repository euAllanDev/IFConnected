package com.ifconnected.repository.jdbc;

import com.ifconnected.dto.UserSummaryDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FollowRepository {

    private final JdbcTemplate jdbc;

    public FollowRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ============================
    // Ações de seguir / deixar de seguir
    // ============================

    public void followUser(Long followerId, Long followedId) {
        jdbc.update("SELECT follow_user(?, ?)", followerId, followedId);
    }

    public void unfollowUser(Long followerId, Long followedId) {
        jdbc.update("SELECT unfollow_user(?, ?)", followerId, followedId);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        Boolean result = jdbc.queryForObject(
                "SELECT is_following(?, ?)",
                Boolean.class,
                followerId,
                followedId
        );
        return result != null && result;
    }

    // ============================
    // IDs de seguidores / seguindo
    // ============================

    public List<Long> getFollowingIds(Long userId) {
        return jdbc.queryForList(
                "SELECT id FROM get_following_ids(?)",
                Long.class,
                userId
        );
    }

    public List<Long> getFollowerIds(Long userId) {
        return jdbc.queryForList(
                "SELECT id FROM get_follower_ids(?)",
                Long.class,
                userId
        );
    }

    // ============================
    // Contadores
    // ============================

    public Long countFollowers(Long userId) {
        return jdbc.queryForObject(
                "SELECT count_followers(?)",
                Long.class,
                userId
        );
    }

    public Long countFollowing(Long userId) {
        return jdbc.queryForObject(
                "SELECT count_following(?)",
                Long.class,
                userId
        );
    }

    // ============================
    // Listas com detalhes completos
    // ============================

    public List<UserSummaryDTO> getFollowersDetails(Long userId) {

        String sql = """
            SELECT u.id, u.username, u.profile_image_url, u.bio
            FROM follows f
            JOIN users u ON u.id = f.follower_id
            WHERE f.followed_id = ?
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> new UserSummaryDTO(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("profile_image_url"),
                        rs.getString("bio")
                ),
                userId
        );
    }

    public List<UserSummaryDTO> getFollowingDetails(Long userId) {

        String sql = """
            SELECT u.id, u.username, u.profile_image_url, u.bio
            FROM follows f
            JOIN users u ON u.id = f.followed_id
            WHERE f.follower_id = ?
        """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> new UserSummaryDTO(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("profile_image_url"),
                        rs.getString("bio")
                ),
                userId
        );
    }
}
