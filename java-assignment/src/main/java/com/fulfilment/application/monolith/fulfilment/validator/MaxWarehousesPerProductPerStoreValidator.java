package com.fulfilment.application.monolith.fulfilment.validator;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentConstraintException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MaxWarehousesPerProductPerStoreValidator implements FulfilmentValidator {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;

  @Override
  public void validate(
      String warehouseBusinessUnitCode,
      Long storeId,
      Long productId,
      List<FulfilmentAssociation> existingAssociations) {
    Set<String> warehousesForProductInStore =
        existingAssociations.stream()
            .filter(a -> storeId.equals(a.storeId) && productId.equals(a.productId))
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());

    if (!warehousesForProductInStore.contains(warehouseBusinessUnitCode)
        && warehousesForProductInStore.size() >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new FulfilmentConstraintException(
          "Constraint violation: Product can be fulfilled by a maximum of 2 different Warehouses per Store.");
    }
  }
}