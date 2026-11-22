package com.fulfilment.application.monolith.fulfilment.domain.ports;

public interface ProductExistencePort {
  boolean existsById(Long productId);
}