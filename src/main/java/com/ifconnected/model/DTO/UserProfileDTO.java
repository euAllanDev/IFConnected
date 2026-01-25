package com.ifconnected.model.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfileDTO", description = "Resumo do perfil do usuário com contadores.")
public class UserProfileDTO {

    @Schema(description = "Dados públicos do usuário")
    private UserResponseDTO user;

    @Schema(example = "10")
    private int followersCount;

    @Schema(example = "5")
    private int followingCount;

    @Schema(example = "23")
    private long postCount;

    public UserProfileDTO() {}

    public UserProfileDTO(UserResponseDTO user, int followersCount, int followingCount, long postCount) {
        this.user = user;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.postCount = postCount;
    }

    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public long getPostCount() { return postCount; }
    public void setPostCount(long postCount) { this.postCount = postCount; }
}
