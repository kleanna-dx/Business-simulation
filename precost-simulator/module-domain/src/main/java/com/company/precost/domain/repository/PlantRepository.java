package com.company.precost.domain.repository;

import com.company.precost.domain.entity.PlantEntity;
import com.company.precost.domain.enums.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<PlantEntity, Plant> {
}
