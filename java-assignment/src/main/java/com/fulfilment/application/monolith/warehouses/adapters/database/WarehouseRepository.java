package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

	@Override
	public List<Warehouse> getAll() {
//    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
		return this.find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
	}

	@Override
	@Transactional
	public void create(Warehouse warehouse) {
		DbWarehouse dbWarehouse = new DbWarehouse();
		dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
		dbWarehouse.location = warehouse.location;
		dbWarehouse.capacity = warehouse.capacity;
		dbWarehouse.stock = warehouse.stock;
		dbWarehouse.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
		dbWarehouse.archivedAt = warehouse.archivedAt;
		this.persist(dbWarehouse);

		// throw new UnsupportedOperationException("Unimplemented method 'create'");
	}

	@Override
	@Transactional
	public void update(Warehouse warehouse) {
		DbWarehouse dbWarehouse = this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
				.firstResult();
		if (dbWarehouse != null) {
			dbWarehouse.location = warehouse.location;
			dbWarehouse.capacity = warehouse.capacity;
			dbWarehouse.stock = warehouse.stock;
			dbWarehouse.archivedAt = warehouse.archivedAt;
			this.persist(dbWarehouse);
		}
//		throw new UnsupportedOperationException("Unimplemented method 'replace'");
	}

	@Override
	@Transactional
	public void remove(Warehouse warehouse) {
		DbWarehouse dbWarehouse = this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
				.firstResult();
		if (dbWarehouse != null) {
			this.delete(dbWarehouse);
		}
//		throw new UnsupportedOperationException("Unimplemented method 'remove'");
	}

	@Override
	public Warehouse findByBusinessUnitCode(String buCode) {
		if (buCode == null) {
			return null;
		}
		DbWarehouse dbWarehouse = this.find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
		return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;

//		throw new UnsupportedOperationException("Unimplemented method 'findById'");
	}

	public Warehouse findActiveByIdOrBusinessUnitCode(String idOrBuCode) 
	{
		if (idOrBuCode == null) 
		{
			return null;
		}
		try 
		{
			Long numericId = Long.parseLong(idOrBuCode);
			DbWarehouse byId = this.find("id = ?1 and archivedAt is null", numericId).firstResult();
			if (byId != null) 
			{
				return byId.toWarehouse();
			}
		} catch (NumberFormatException ignored)
		{
			// Not a numeric ID; fall through to businessUnitCode lookup
		}
		return findByBusinessUnitCode(idOrBuCode);
	}
}
