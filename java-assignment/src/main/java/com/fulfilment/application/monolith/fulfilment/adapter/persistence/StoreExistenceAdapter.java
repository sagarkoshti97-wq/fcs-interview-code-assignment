package com.fulfilment.application.monolith.fulfilment.adapter.persistence;

import com.fulfilment.application.monolith.fulfilment.domain.ports.StoreExistencePort;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreExistenceAdapter implements StoreExistencePort {

  @Override
  public boolean existsById(Long storeId) {
    return storeId != null && Store.findById(storeId) != null;
  }
}