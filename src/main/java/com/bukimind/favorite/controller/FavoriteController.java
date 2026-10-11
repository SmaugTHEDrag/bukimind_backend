package com.bukimind.favorite.controller;

import com.bukimind.favorite.dto.FavoriteDTO;
import com.bukimind.favorite.dto.FavoriteRequestDTO;
import com.bukimind.favorite.service.IFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorite API", description = "APIs for managing user's favorite books")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @Operation(summary = "Get all favorite books of the current user")
    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getFavorites() {
        return ResponseEntity.ok(favoriteService.getAllFavorites());
    }

    @Operation(summary = "Add favorite to current user's favorites")
    @PostMapping
    public ResponseEntity<FavoriteDTO> addFavorite(@RequestBody @Valid FavoriteRequestDTO request) {
        return ResponseEntity.ok(favoriteService.addFavorite(request));
    }

    @Operation(summary = "Remove favorite from current user's favorites")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Integer bookId) {
        favoriteService.removeFavorite(bookId);
        return ResponseEntity.ok("Book removed from favorites");
    }

}

