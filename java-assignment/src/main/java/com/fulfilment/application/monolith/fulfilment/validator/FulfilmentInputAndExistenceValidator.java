package com.fulfilment.application.monolith.fulfilment.validator;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentConstraintException;
import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.ProductExistencePort;
import com.fulfilment.application.monolith.fulfilment.domain.ports.StoreExistencePort;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
@Priority(1)
public class FulfilmentInputAndExistenceValidator implements FulfilmentValidator {

  @Inject WarehouseStore warehouseStore;

  @Inject StoreExistencePort storeExistencePort;

  @Inject ProductExistencePort productExistencePort;

  @Override
  public void validate(
      String warehouseBusinessUnitCode,
      Long storeId,
      Long productId,
      List<FulfilmentAssociation> existingAssociations) {
    if (warehouseBusinessUnitCode == null
        || warehouseBusinessUnitCode.isBlank()
        || storeId == null
        || productId == null) {
      throw new FulfilmentConstraintException(
          "Warehouse businessUnitCode, storeId, and productId are required.");
    }

    var warehouse = warehouseStore.findByBusinessUnitCode(warehouseBusinessUnitCode);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new FulfilmentNotFoundException(
          "Active warehouse " + warehouseBusinessUnitCode + " does not exist.");
    }

    if (!storeExistencePort.existsById(storeId)) {
      throw new FulfilmentNotFoundException("Store with id " + storeId + " does not exist.");
    }

    if (!productExistencePort.existsById(productId)) {
      throw new FulfilmentNotFoundException("Product with id " + productId + " does not exist.");
    }
  }
}