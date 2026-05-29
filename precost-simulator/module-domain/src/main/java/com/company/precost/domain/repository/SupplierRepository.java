package com.company.precost.domain.repository;

import com.company.precost.domain.entity.Supplier;
import com.company.precost.domain.enums.ActiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByStatus(ActiveStatus status);
}
