package com.company.precost.domain.repository;

import com.company.precost.domain.entity.Bom;
import com.company.precost.domain.enums.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BomRepository extends JpaRepository<Bom, Long> {

    Optional<Bom> findByMaterialCodeAndPlantCodeAndGradeCode(String materialCode, Plant plantCode, String gradeCode);

    List<Bom> findByPlantCode(Plant plantCode);

    List<Bom> findByMaterialCode(String materialCode);
}
