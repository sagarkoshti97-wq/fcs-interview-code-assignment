package com.fulfilment.application.monolith.fulfilment.adapter.persistence;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfilment_association")
public class DbFulfilmentAssociation {

  @Id
  @SequenceGenerator(
      name = "fulfilmentAssociationSeq",
      sequenceName = "fulfilment_association_seq",
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fulfilmentAssociationSeq")
  public Long id;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public Long productId;

  public DbFulfilmentAssociation() {}

  public FulfilmentAssociation toDomain() {
    return new FulfilmentAssociation(
        this.id, this.warehouseBusinessUnitCode, this.storeId, this.productId);
  }

  public static DbFulfilmentAssociation fromDomain(FulfilmentAssociation domain) {
    DbFulfilmentAssociation entity = new DbFulfilmentAssociation();
    entity.id = domain.id;
    entity.warehouseBusinessUnitCode = domain.warehouseBusinessUnitCode;
    entity.storeId = domain.storeId;
    entity.productId = domain.productId;
    return entity;
  }
}