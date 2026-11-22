package com.fulfilment.application.monolith.fulfilment.validator;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentConstraintException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MaxWarehousesPerStoreValidator implements FulfilmentValidator {

  private static final int MAX_WAREHOUSES_PER_STORE = 3;

  @Override
  public void validate(
      String warehouseBusinessUnitCode,
      Long storeId,
      Long productId,
      List<FulfilmentAssociation> existingAssociations) {
    Set<String> warehousesForStore =
        existingAssociations.stream()
            .filter(a -> storeId.equals(a.storeId))
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());

    if (!warehousesForStore.contains(warehouseBusinessUnitCode)
        && warehousesForStore.size() >= MAX_WAREHOUSES_PER_STORE) {
      throw new FulfilmentConstraintException(
          "Constraint violation: Store can be fulfilled by a maximum of 3 different Warehouses.");
    }
  }
}