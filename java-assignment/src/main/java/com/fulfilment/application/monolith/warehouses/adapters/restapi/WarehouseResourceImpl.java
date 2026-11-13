package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

	@Inject
	WarehouseRepository warehouseRepository;

	@Inject
	CreateWarehouseOperation createWarehouseOperation;

	@Inject
	ArchiveWarehouseOperation archiveWarehouseOperation;

	@Inject
	ReplaceWarehouseOperation replaceWarehouseOperation;

	@Override
	public List<Warehouse> listAllWarehousesUnits() {
		return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
	}

	@Override
	@Transactional
	public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
		var domainWarehouse = toDomainWarehouse(data);
		createWarehouseOperation.create(domainWarehouse);
		return toWarehouseResponse(domainWarehouse);
	}

	@Override
	public Warehouse getAWarehouseUnitByID(String id) {
		var domainWarehouse = warehouseRepository.findActiveByIdOrBusinessUnitCode(id);
		if (domainWarehouse == null) {
			throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
		}
		return toWarehouseResponse(domainWarehouse);
	}

	@Override
	@Transactional
	public void archiveAWarehouseUnitByID(String id) {
		var domainWarehouse = warehouseRepository.findActiveByIdOrBusinessUnitCode(id);
		if (domainWarehouse == null) {
			throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
		}
		archiveWarehouseOperation.archive(domainWarehouse);
	}

	@Override
	@Transactional
	public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
		var domainWarehouse = toDomainWarehouse(data);
		domainWarehouse.businessUnitCode = businessUnitCode;
		replaceWarehouseOperation.replace(domainWarehouse);
		return toWarehouseResponse(domainWarehouse);
	}

	private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(Warehouse data) {
		var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
		warehouse.businessUnitCode = data.getBusinessUnitCode();
		warehouse.location = data.getLocation();
		warehouse.capacity = data.getCapacity();
		warehouse.stock = data.getStock();
		return warehouse;
	}

	private Warehouse toWarehouseResponse(
			com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
		var response = new Warehouse();
		response.setBusinessUnitCode(warehouse.businessUnitCode);
		response.setLocation(warehouse.location);
		response.setCapacity(warehouse.capacity);
		response.setStock(warehouse.stock);

		return response;
	}
}