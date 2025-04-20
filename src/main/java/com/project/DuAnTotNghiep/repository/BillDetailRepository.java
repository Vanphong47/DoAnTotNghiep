package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.entity.BillDetail;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    Optional<BillDetail> findByBillAndProductDetail(Bill bill, ProductDetail productDetail);
}