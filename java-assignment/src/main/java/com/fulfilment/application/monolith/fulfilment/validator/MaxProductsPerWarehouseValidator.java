package com.fulfilment.application.monolith.fulfilment.validator;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentConstraintException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class MaxProductsPerWarehouseValidator implements FulfilmentValidator {

  private static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

  @Override
  public void validate(
      String warehouseBusinessUnitCode,
      Long storeId,
      Long productId,
      List<FulfilmentAssociation> existingAssociations) {
    Set<Long> productsInWarehouse =
        existingAssociations.stream()
            .filter(a -> warehouseBusinessUnitCode.equals(a.warehouseBusinessUnitCode))
            .map(a -> a.productId)
            .collect(Collectors.toSet());

    if (!productsInWarehouse.contains(productId)
        && productsInWarehouse.size() >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
      throw new FulfilmentConstraintException(
          "Constraint violation: Warehouse can store maximally 5 types of Products.");
    }
  }
}