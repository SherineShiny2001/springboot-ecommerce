package com.gd.springecommerce.repository;

import com.gd.springecommerce.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.available = :quantity WHERE p.id = :id")
    void updateProductAvailability(Integer quantity, Long id);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findQuantityByIds(@Param("ids") List<Long> ids);
}
