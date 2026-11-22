package com.fulfilment.application.monolith.fulfilment.domain.model;

public class FulfilmentAssociation {

  public Long id;
  public String warehouseBusinessUnitCode;
  public Long storeId;
  public Long productId;

  public FulfilmentAssociation() {}

  public FulfilmentAssociation(String warehouseBusinessUnitCode, Long storeId, Long productId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.storeId = storeId;
    this.productId = productId;
  }

  public FulfilmentAssociation(
      Long id, String warehouseBusinessUnitCode, Long storeId, Long productId) {
    this.id = id;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.storeId = storeId;
    this.productId = productId;
  }
}