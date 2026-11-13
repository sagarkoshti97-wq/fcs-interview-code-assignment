package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfilment_association")
public class FulfilmentAssociation extends PanacheEntity {

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public Long productId;

  public FulfilmentAssociation() {}

  public FulfilmentAssociation(String warehouseBusinessUnitCode, Long storeId, Long productId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.storeId = storeId;
    this.productId = productId;
  }
}