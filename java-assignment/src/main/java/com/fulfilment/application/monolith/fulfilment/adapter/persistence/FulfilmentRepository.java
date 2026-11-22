package com.fulfilment.application.monolith.fulfilment.adapter.persistence;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FulfilmentRepository implements FulfilmentStore {

  @Inject DbFulfilmentPanacheRepository panacheRepository;

  @Override
  public List<FulfilmentAssociation> findAll() {
    return panacheRepository.listAll().stream().map(DbFulfilmentAssociation::toDomain).toList();
  }

  @Override
  public FulfilmentAssociation findById(Long id) {
    DbFulfilmentAssociation entity = panacheRepository.findById(id);
    return entity != null ? entity.toDomain() : null;
  }

  @Override
  public FulfilmentAssociation findExact(
      String warehouseBusinessUnitCode, Long storeId, Long productId) {
    DbFulfilmentAssociation entity =
        panacheRepository
            .find(
                "warehouseBusinessUnitCode = ?1 and storeId = ?2 and productId = ?3",
                warehouseBusinessUnitCode,
                storeId,
                productId)
            .firstResult();
    return entity != null ? entity.toDomain() : null;
  }

  @Override
  public List<FulfilmentAssociation> findByStoreAndProduct(Long storeId, Long productId) {
    return panacheRepository.list("storeId = ?1 and productId = ?2", storeId, productId).stream()
        .map(DbFulfilmentAssociation::toDomain)
        .toList();
  }

  @Override
  public List<FulfilmentAssociation> findByStoreId(Long storeId) {
    return panacheRepository.list("storeId", storeId).stream()
        .map(DbFulfilmentAssociation::toDomain)
        .toList();
  }

  @Override
  public List<FulfilmentAssociation> findByWarehouseBusinessUnitCode(
      String warehouseBusinessUnitCode) {
    return panacheRepository.list("warehouseBusinessUnitCode", warehouseBusinessUnitCode).stream()
        .map(DbFulfilmentAssociation::toDomain)
        .toList();
  }

  @Override
  public FulfilmentAssociation save(FulfilmentAssociation association) {
    DbFulfilmentAssociation entity = DbFulfilmentAssociation.fromDomain(association);
    panacheRepository.persist(entity);
    return entity.toDomain();
  }

  @Override
  public void deleteById(Long id) {
    panacheRepository.deleteById(id);
  }

  @ApplicationScoped
  public static class DbFulfilmentPanacheRepository
      implements PanacheRepository<DbFulfilmentAssociation> {}
}