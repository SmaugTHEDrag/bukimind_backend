package com.bukimind.user.entity;

import jakarta.persistence.*;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

/**
 * Application user.
 *
 * Authentication (password, login, tokens) belongs to Keycloak; this table only
 * stores application level profile data. The link to Keycloak is
 * {@link #keycloakUserId} (the JWT "sub" claim), which is the stable identity --
 * usernames and emails can change in Keycloak, the subject does not.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // values allowed by the users.status check constraint
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "keycloak_user_id", nullable = false, unique = true, length = 255)
    private String keycloakUserId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = STATUS_ACTIVE;

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

//    // One user can have many favorites;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Favorite> favorite;
//
//    // One user can have many blogs;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Blog> blogs;
//
//    // One user can like many blogs;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<BlogLike> blogLikes;
//
//    // One user can comment on many blogs;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<BlogComment> blogComments;
//
//    // One user can comment on many reviews;
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Review> reviews;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = STATUS_ACTIVE;
        }
    }
}
