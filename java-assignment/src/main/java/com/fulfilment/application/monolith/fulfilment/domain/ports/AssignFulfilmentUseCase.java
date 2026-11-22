package com.fulfilment.application.monolith.fulfilment.domain.ports;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;

public interface AssignFulfilmentUseCase {

  FulfilmentAssociation assign(String warehouseBusinessUnitCode, Long storeId, Long productId);
}