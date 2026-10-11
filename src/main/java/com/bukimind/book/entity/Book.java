package com.bukimind.book.entity;

import com.bukimind.favorite.entity.Favorite;
import com.bukimind.review.entity.Review;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bookTitle", nullable=false)
    private String title;

    @Column(name = "authorName",length = 100, nullable=false)
    private String author;

    @Column(name = "imageURL")
    private String image;

    @Column(nullable=false)
    private String category;

    @Column(name = "bookDescription", columnDefinition = "TEXT")
    private String description;

    @Column(name = "bookPDFURL")
    private String pdf;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // One book can be favorite by many user;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorite;

    // One book can have many reviews;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;
}
