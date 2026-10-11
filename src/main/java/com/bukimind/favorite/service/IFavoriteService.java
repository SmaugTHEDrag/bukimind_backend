package com.bukimind.favorite.service;

import com.bukimind.favorite.dto.FavoriteDTO;
import com.bukimind.favorite.dto.FavoriteRequestDTO;

import java.util.List;

public interface IFavoriteService {

    List<FavoriteDTO> getAllFavorites();

    // one user can favorite a book only once
    FavoriteDTO addFavorite(FavoriteRequestDTO favoriteRequestDTO);

    void removeFavorite(Integer bookId);

}

