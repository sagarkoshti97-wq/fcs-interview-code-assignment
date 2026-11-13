package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.LocalDateTime;
import java.util.List;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

	private final WarehouseStore warehouseStore;
	private final LocationResolver locationResolver;

	@Inject
	public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
		this.warehouseStore = warehouseStore;
		this.locationResolver = locationResolver;
	}

	public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
		this(warehouseStore, null);
	}

	@Override
	public void create(Warehouse warehouse) {
		if (warehouse == null || warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
			throw new WebApplicationException("Bussiness Unit code must be provided", 400);
		}

		// 1. Business Unit Code Verification
		Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
		if (existing != null && existing.archivedAt == null) {
			throw new WebApplicationException(
					"Warehouse with business unit code " + warehouse.businessUnitCode + " already exists.", 400);
		}

		// 2. Location Validation
		if (locationResolver != null) {
			Location location = locationResolver.resolveByIdentifier(warehouse.location);
			if (location == null) {
				throw new WebApplicationException("Invalid location: " + warehouse.location, 400);
			}

			List<Warehouse> warehousesAtLocation = warehouseStore.getAll().stream()
					.filter(w -> w.archivedAt == null && location.identification.equals(w.location)).toList();

			// 3. Warehouse Creation Feasibility
			if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
				throw new WebApplicationException(
						"Maximum number of warehouses reached for location " + warehouse.location, 400);
			}

			// 4. Capacity and Stock Validation against Location maxCapacity
			int currentTotalCapacity = warehousesAtLocation.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0)
					.sum();
			int requestedCapacity = warehouse.capacity != null ? warehouse.capacity : 0;

			if (currentTotalCapacity + requestedCapacity > location.maxCapacity) {
				throw new WebApplicationException(
						"Warehouse capacity exceeds maximum capacity for location " + warehouse.location, 400);
			}
		}

		// Validate capacity & stock relationship
		if (warehouse.capacity == null || warehouse.capacity <= 0) 
		{
			throw new WebApplicationException("Warehouse capacity must be greater than zero.", 400);
		}
		int stock = warehouse.stock != null ? warehouse.stock : 0;
		if (stock < 0 || stock > warehouse.capacity) 
		{
			throw new WebApplicationException("Warehouse stock cannot exceed warehouse capacity or be negative.", 400);
		}

		if (warehouse.createdAt == null) 
		{
			warehouse.createdAt = LocalDateTime.now();
		}

		// if all went well, create the warehouse
		warehouseStore.create(warehouse);
	}
}
