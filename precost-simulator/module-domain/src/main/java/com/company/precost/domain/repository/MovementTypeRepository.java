package com.company.precost.domain.repository;

import com.company.precost.domain.entity.MovementTypeEntity;
import com.company.precost.domain.enums.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovementTypeRepository extends JpaRepository<MovementTypeEntity, MovementType> {
}
