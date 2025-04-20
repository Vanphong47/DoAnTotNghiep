package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.utils.QRCodeService;
import com.project.DuAnTotNghiep.dto.Product.ProductDetailDto;
import com.project.DuAnTotNghiep.dto.Product.ProductDto;
import com.project.DuAnTotNghiep.dto.Product.ProductSearchDto;
import com.project.DuAnTotNghiep.dto.Product.SearchProductDto;
import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.entity.ProductDetail;
import com.project.DuAnTotNghiep.entity.ProductDiscount;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.ProductDetailRepository;
import com.project.DuAnTotNghiep.repository.ProductDiscountRepository;
import com.project.DuAnTotNghiep.repository.ProductRepository;
import com.project.DuAnTotNghiep.repository.Specification.ProductSpecification;
import com.project.DuAnTotNghiep.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Autowired
    private ProductDiscountRepository productDiscountRepository;

    @Override
    public Page<Product> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<ProductSearchDto> getAll(Pageable pageable) {
        return productRepository.getAll(pageable);
    }

    @Override
    public Product save(Product product) throws IOException {
        // Check if the product has an ID (i.e., it's an existing product)
        if (product.getId() != null) {
            // Fetch the existing product from the database
            Product existingProduct = productRepository.findById(product.getId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            // Retain the existing code (don't update it)
            product.setCode(existingProduct.getCode());
        } else {
            // New product: generate a new code
            String code = product.getCode();

            if (code == null || code.trim().isEmpty()) {
                Product productCurrent = productRepository.findTopByOrderByIdDesc();
                Long nextCode = (productCurrent == null) ? 1 : productCurrent.getId() + 1;
                code = "SP" + String.format("%04d", nextCode);

                while (productRepository.existsByCode(code)) {
                    nextCode++;
                    code = "SP" + String.format("%04d", nextCode);
                }
            } else {
                code = code.trim();
                if (productRepository.existsByCode(code)) {
                    throw new ShopApiException(HttpStatus.CONFLICT, "Product code already exists: " + code);
                }
            }
            product.setCode(code);
        }

        Double minPrice = Double.valueOf(1000000000);
        for (ProductDetail productDetail : product.getProductDetails()) {
            if (productDetail.getPrice() < minPrice) {
                minPrice = productDetail.getPrice();
            }
            QRCodeService.generateQRCode(productDetail.getBarcode(), productDetail.getBarcode());
        }

        product.setPrice(minPrice);
        product.setDeleteFlag(false);
        product.setCreateDate(product.getCreateDate() != null ? product.getCreateDate() : LocalDateTime.now());
        product.setUpdatedDate(LocalDateTime.now());
        return productRepository.save(product);
    }


    @Override
    public Product delete(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        product.setDeleteFlag(true);
        return productRepository.save(product);
    }

    @Override
    public Product getProductByCode(String code) {
        List<Product> products = productRepository.findByCode(code);

        if (products.isEmpty()) {
            throw new NotFoundException("No product found with code: " + code);
        } else if (products.size() == 1) {
            return products.get(0);
        } else {
            throw new ShopApiException(HttpStatus.CONFLICT, "Multiple products found with code: " + code);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        return productRepository.existsByCode(code);
    }

    @Override
    public Page<Product> search(String productName, Pageable pageable) {
        return productRepository.searchProductName(productName, pageable);
    }

    @Override
    public Page<ProductSearchDto> listSearchProduct(String maSanPham, String tenSanPham, Long nhanHang, Long chatLieu, Long theLoai, Integer trangThai, Pageable pageable) {
        return productRepository.listSearchProduct(maSanPham, tenSanPham, nhanHang, chatLieu, theLoai, trangThai, pageable);
    }

    @Override
    public Page<Product> getAllByStatus(int status, Pageable pageable) {
        return productRepository.findAllByStatusAndDeleteFlag(status, false, pageable);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Page<ProductDto> searchProduct(SearchProductDto searchDto, Pageable pageable) {
        Specification<Product> spec = new ProductSpecification(searchDto);
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::convertToDto);
    }

    @Override
    public Page<ProductDto> getAllProductApi(Pageable pageable) {
        Page<Product> productPage = productRepository.findAllByDeleteFlagFalse(pageable);
        return productPage.map(this::convertToDto);
    }

    @Override
    public ProductDto getProductByBarcode(String barcode) {
        ProductDetail productDetail = productDetailRepository.findByBarcodeContainingIgnoreCase(barcode);
        if (productDetail == null) {
            throw new ShopApiException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm có mã vạch: " + barcode);
        }
        Product product = productDetail.getProduct();
        return convertToDto(product);
    }

    @Override
    public List<ProductDto> getAllProductNoPaginationApi(SearchProductDto searchRequest) {
        Specification<Product> spec = new ProductSpecification(searchRequest);
        List<Product> products = productRepository.findAll(spec);
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public ProductDto getByProductDetailId(Long detailId) {
        Product product = productRepository.findByProductDetail_Id(detailId);
        if (product == null) {
            throw new NotFoundException("No product found with detail ID: " + detailId);
        }
        return convertToDto(product);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        return List.of();
    }

    private ProductDto convertToDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setCode(product.getCode());
        productDto.setName(product.getName());
        productDto.setCategoryName(product.getCategory().getName());
        productDto.setImageUrl(product.getImage().get(0).getLink());
        productDto.setDescription(product.getDescribe());
        productDto.setCreateDate(product.getCreateDate());
        productDto.setUpdatedDate(product.getUpdatedDate());

        List<ProductDetailDto> productDetailDtoList = new ArrayList<>();
        Double priceMin = Double.valueOf(1000000000);
        for (ProductDetail productDetail : product.getProductDetails()) {
            if (productDetail.getPrice() < priceMin) {
                priceMin = productDetail.getPrice();
            }
            ProductDetailDto productDetailDto = new ProductDetailDto();
            productDetailDto.setId(productDetail.getId());
            productDetailDto.setProductId(product.getId());
            productDetailDto.setColor(productDetail.getColor());
            productDetailDto.setSize(productDetail.getSize());
            productDetailDto.setPrice(productDetail.getPrice());
            productDetailDto.setQuantity(productDetail.getQuantity());
            productDetailDto.setBarcode(productDetail.getBarcode());
            ProductDiscount productDiscount = productDiscountRepository.findValidDiscountByProductDetailId(productDetail.getId());
            if (productDiscount != null) {
                productDto.setDiscounted(true);
                productDetailDto.setDiscountedPrice(productDiscount.getDiscountedAmount());
                if (productDiscount.getDiscountedAmount() < priceMin) {
                    priceMin = productDiscount.getDiscountedAmount();
                }
            }
            productDetailDtoList.add(productDetailDto);
        }
        productDto.setPriceMin(priceMin);
        productDto.setProductDetailDtos(productDetailDtoList);
        return productDto;
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return productRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return productRepository.existsByNameAndIdNot(name, id);
    }

}
