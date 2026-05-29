package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ProductGrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductGradeRepository extends JpaRepository<ProductGrade, String> {
}
