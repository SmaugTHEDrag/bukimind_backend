package com.bukimind.favorite.service;

import com.bukimind.favorite.dto.FavoriteDTO;
import com.bukimind.favorite.dto.FavoriteRequestDTO;
import com.bukimind.book.entity.Book;
import com.bukimind.favorite.entity.Favorite;
import com.bukimind.user.entity.User;
import com.bukimind.common.exception.ResourceNotFoundException;
import com.bukimind.common.security.CurrentUserService;
import com.bukimind.favorite.mapper.FavoriteMapper;
import com.bukimind.book.repository.IBookRepository;
import com.bukimind.favorite.repository.IFavoriteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FavoriteService implements IFavoriteService{

    private final IFavoriteRepository favoriteRepository;

    private final IBookRepository bookRepository;

    private final FavoriteMapper favoriteMapper;

    private final CurrentUserService currentUserService;

    @Override
    public List<FavoriteDTO> getAllFavorites() {
        User user = currentUserService.getCurrentUser();

        List<Favorite> favorites = favoriteRepository.findByUserId(user.getId());

        return favoriteMapper.toListDTO(favorites);
    }

    // Adds a book to the user's favorites.
    @Override
    public FavoriteDTO addFavorite(FavoriteRequestDTO request) {

        User user = currentUserService.getCurrentUser();

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // one user can favorite a book only once
        if (favoriteRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            throw new IllegalArgumentException("Book already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBook(book);

        return favoriteMapper.toDTO(favoriteRepository.save(favorite));
    }

    @Override
    public void removeFavorite(Integer bookId) {

        User user = currentUserService.getCurrentUser();

        Favorite favorite = favoriteRepository.findByUserIdAndBookId(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }
}

