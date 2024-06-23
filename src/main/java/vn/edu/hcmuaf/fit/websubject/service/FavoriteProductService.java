package vn.edu.hcmuaf.fit.websubject.service;

import vn.edu.hcmuaf.fit.websubject.entity.FavoriteProduct;

import java.util.List;

public interface FavoriteProductService {
    List<FavoriteProduct> getAllFavoriteProducts();

    FavoriteProduct addFavorite(Integer productId);

    void deleteFavorite(Integer id);
}
