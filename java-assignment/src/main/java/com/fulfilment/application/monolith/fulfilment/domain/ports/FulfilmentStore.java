package com.fulfilment.application.monolith.fulfilment.domain.ports;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import java.util.List;

public interface FulfilmentStore {

  List<FulfilmentAssociation> findAll();

  FulfilmentAssociation findById(Long id);

  FulfilmentAssociation findExact(String warehouseBusinessUnitCode, Long storeId, Long productId);

  List<FulfilmentAssociation> findByStoreAndProduct(Long storeId, Long productId);

  List<FulfilmentAssociation> findByStoreId(Long storeId);

  List<FulfilmentAssociation> findByWarehouseBusinessUnitCode(String warehouseBusinessUnitCode);

  FulfilmentAssociation save(FulfilmentAssociation association);

  void deleteById(Long id);
}