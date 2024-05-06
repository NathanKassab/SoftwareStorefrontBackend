package me.bannock.capstone.backend.products.service.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<ProductModel, Long> {

    Optional<ProductModel> findProductModelByid(Long productId);

    List<ProductModel> findProductModelsByOwnerUid(long ownerUid);

    List<ProductModel> findProductModelsByDisabledIsFalseAndHiddenIsFalse();

}
