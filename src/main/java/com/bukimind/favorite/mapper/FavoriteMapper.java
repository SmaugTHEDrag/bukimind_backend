package com.bukimind.favorite.mapper;

import com.bukimind.favorite.dto.FavoriteDTO;
import com.bukimind.favorite.entity.Favorite;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
    FavoriteDTO toDTO(Favorite save);

    List<FavoriteDTO> toListDTO(List<Favorite> byUserId);
}
