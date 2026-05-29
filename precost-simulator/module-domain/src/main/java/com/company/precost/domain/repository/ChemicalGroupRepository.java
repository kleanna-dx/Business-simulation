package com.company.precost.domain.repository;

import com.company.precost.domain.entity.ChemicalGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChemicalGroupRepository extends JpaRepository<ChemicalGroup, String> {

    boolean existsByGroupCode(String groupCode);
}
