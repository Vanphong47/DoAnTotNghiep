package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long> {
    Page<ProductDetail> getProductDetailsByProductId(Long id, Pageable pageable);

    ProductDetail getProductDetailByProduct(Product product);
    List<ProductDetail> getProductDetailByProductId(Long productId);

    ProductDetail findByBarcodeContainingIgnoreCase(String barcode);

    boolean existsByBarcode(String barcode);
    @Modifying
    @Transactional
    @Query(value = "insert into bill_detail(moment_price, quantity, bill_id, product_detail_id) values (:momentPrice, :quantity, :billId, :productDetailId)", nativeQuery = true)
    void addNewProductDetail(@Param("momentPrice") Long momentPrice, @Param("quantity") Integer quantity, @Param("billId") Integer billId, @Param("productDetailId") Integer productDetailId);
}