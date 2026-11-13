package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class FulfilmentService {

  @Inject WarehouseStore warehouseStore;

  @Inject ProductRepository productRepository;

  public List<FulfilmentAssociation> listAll() {
    return FulfilmentAssociation.listAll();
  }

  @Transactional
  public FulfilmentAssociation assign(
      String warehouseBuCode, Long storeId, Long productId) {
    if (warehouseBuCode == null || warehouseBuCode.isBlank() || storeId == null || productId == null) {
      throw new WebApplicationException(
          "Warehouse businessUnitCode, storeId, and productId are required.", 400);
    }

    // Verify entities exist
    var warehouse = warehouseStore.findByBusinessUnitCode(warehouseBuCode);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException(
          "Active warehouse " + warehouseBuCode + " does not exist.", 404);
    }
    if (Store.findById(storeId) == null) {
      throw new WebApplicationException("Store with id " + storeId + " does not exist.", 404);
    }
    if (productRepository.findById(productId) == null) {
      throw new WebApplicationException("Product with id " + productId + " does not exist.", 404);
    }

    // Check if the exact association already exists (idempotent check)
    FulfilmentAssociation existing =
        FulfilmentAssociation.find(
                "warehouseBusinessUnitCode = ?1 and storeId = ?2 and productId = ?3",
                warehouseBuCode,
                storeId,
                productId)
            .firstResult();
    if (existing != null) {
      return existing;
    }

    // Constraint 1: Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
    List<FulfilmentAssociation> productStoreAssocs =
        FulfilmentAssociation.list("storeId = ?1 and productId = ?2", storeId, productId);
    Set<String> warehousesForProductInStore =
        productStoreAssocs.stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());
    if (!warehousesForProductInStore.contains(warehouseBuCode)
        && warehousesForProductInStore.size() >= 2) {
      throw new WebApplicationException(
          "Constraint violation: Product can be fulfilled by a maximum of 2 different Warehouses per Store.",
          400);
    }

    // Constraint 2: Each Store can be fulfilled by a maximum of 3 different Warehouses
    List<FulfilmentAssociation> storeAssocs =
        FulfilmentAssociation.list("storeId = ?1", storeId);
    Set<String> warehousesForStore =
        storeAssocs.stream()
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toSet());
    if (!warehousesForStore.contains(warehouseBuCode) && warehousesForStore.size() >= 3) {
      throw new WebApplicationException(
          "Constraint violation: Store can be fulfilled by a maximum of 3 different Warehouses.",
          400);
    }

    // Constraint 3: Each Warehouse can store maximally 5 types of Products
    List<FulfilmentAssociation> warehouseAssocs =
        FulfilmentAssociation.list("warehouseBusinessUnitCode = ?1", warehouseBuCode);
    Set<Long> productsInWarehouse =
        warehouseAssocs.stream().map(a -> a.productId).collect(Collectors.toSet());
    if (!productsInWarehouse.contains(productId) && productsInWarehouse.size() >= 5) {
      throw new WebApplicationException(
          "Constraint violation: Warehouse can store maximally 5 types of Products.", 400);
    }

    FulfilmentAssociation association =
        new FulfilmentAssociation(warehouseBuCode, storeId, productId);
    association.persist();
    return association;
  }

  @Transactional
  public void remove(Long id) {
    FulfilmentAssociation entity = FulfilmentAssociation.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Fulfilment association not found.", 404);
    }
    entity.delete();
  }
}