package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.service.BillService;
import com.project.DuAnTotNghiep.service.ProductDetailService;
import com.project.DuAnTotNghiep.service.ProductService;
import com.project.DuAnTotNghiep.service.serviceImpl.ProductDetailServiceImpl;
import com.project.DuAnTotNghiep.service.serviceImpl.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-detail")
public class ProductDetailRestController {
    @Autowired
    private ProductDetailService productDetailService = new ProductDetailServiceImpl();
    private ProductService productService = new ProductServiceImpl();
    @Autowired
    private BillService billDetailService;

    @PostMapping("/add")
    public ResponseEntity<?> addProductToBill(@RequestParam Long billId,
                                              @RequestParam Long productDetailId,
                                              @RequestParam int quantity) {
        try {
            billDetailService.addProductToBill(billId, productDetailId, quantity);
            return ResponseEntity.ok("Product added and inventory updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
