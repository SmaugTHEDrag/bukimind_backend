package com.bukimind.favorite.repository;

import com.bukimind.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IFavoriteRepository extends JpaRepository<Favorite, Integer> {

    // all favorites of a user
    List<Favorite> findByUserId(Integer userId);

    // favorite entry for a user and book
    Optional<Favorite> findByUserIdAndBookId(Integer userId, Integer bookId);

    // check if user already favorited the book
    boolean existsByUserIdAndBookId(Integer userId, Integer bookId);
}
