package com.bukimind.user.dto;
import java.util.UUID;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
}
