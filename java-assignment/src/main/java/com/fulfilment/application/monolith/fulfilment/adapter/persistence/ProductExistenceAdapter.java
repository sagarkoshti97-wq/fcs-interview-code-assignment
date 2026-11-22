package com.fulfilment.application.monolith.fulfilment.adapter.persistence;

import com.fulfilment.application.monolith.fulfilment.domain.ports.ProductExistencePort;
import com.fulfilment.application.monolith.products.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductExistenceAdapter implements ProductExistencePort {

  @Inject ProductRepository productRepository;

  @Override
  public boolean existsById(Long productId) {
    return productId != null && productRepository.findById(productId) != null;
  }
}