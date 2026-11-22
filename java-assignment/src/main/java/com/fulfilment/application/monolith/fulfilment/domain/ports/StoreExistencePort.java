package com.fulfilment.application.monolith.fulfilment.domain.ports;

public interface StoreExistencePort {
  boolean existsById(Long storeId);
}