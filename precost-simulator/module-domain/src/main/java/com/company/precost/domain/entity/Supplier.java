package com.company.precost.domain.entity;

import com.company.precost.domain.enums.ActiveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 협력사 마스터.
 */
@Entity
@Table(name = "supplier")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Supplier {

    @Id
    @Column(name = "supplier_code", length = 20, nullable = false)
    private String supplierCode;

    @Column(name = "supplier_name", length = 200, nullable = false)
    private String supplierName;

    @Column(name = "biz_no", length = 20)
    private String bizNo;

    @Column(name = "contact_name", length = 50)
    private String contactName;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private ActiveStatus status;

    @Builder
    public Supplier(String supplierCode, String supplierName, String bizNo,
                    String contactName, String contactPhone, ActiveStatus status) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.bizNo = bizNo;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.status = status == null ? ActiveStatus.ACTIVE : status;
    }

    public void update(String supplierName, String bizNo, String contactName, String contactPhone) {
        this.supplierName = supplierName;
        this.bizNo = bizNo;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public void deactivate() {
        this.status = ActiveStatus.INACTIVE;
    }
}
