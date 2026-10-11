package com.bukimind.user.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String status;
    private String lastLoginAt;
    private String createdAt;
    private String updatedAt;
}

