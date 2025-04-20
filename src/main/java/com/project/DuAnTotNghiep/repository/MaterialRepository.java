package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByCode(String code);
    List<Material> findAllByDeleteFlagFalse();
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    Page<Material> findAllByDeleteFlagFalse(Pageable pageable);
}