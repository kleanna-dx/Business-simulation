package com.company.precost.domain.repository;

import com.company.precost.domain.entity.Material;
import com.company.precost.domain.enums.ActiveStatus;
import com.company.precost.domain.enums.MaterialCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 자재 마스터 Repository.
 */
public interface MaterialRepository extends JpaRepository<Material, String> {

    /** 코드/명 통합 검색 (자동완성) */
    @Query("select m from Material m " +
            "where (:q is null or m.materialCode like %:q% or m.materialName like %:q%) " +
            "and (:category is null or m.category = :category) " +
            "and m.status = :status")
    Page<Material> search(@Param("q") String q,
                          @Param("category") MaterialCategory category,
                          @Param("status") ActiveStatus status,
                          Pageable pageable);

    List<Material> findByStatus(ActiveStatus status);

    long countByCategoryAndStatus(MaterialCategory category, ActiveStatus status);

    boolean existsByMaterialCode(String materialCode);
}
