package com.fulfilment.application.monolith.fulfilment.validator;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import java.util.List;

public interface FulfilmentValidator {

  void validate(
      String warehouseBusinessUnitCode,
      Long storeId,
      Long productId,
      List<FulfilmentAssociation> existingAssociations);
}