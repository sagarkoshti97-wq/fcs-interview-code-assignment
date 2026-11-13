package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

	private final WarehouseStore warehouseStore;

	public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
		this.warehouseStore = warehouseStore;
	}

	@Override
	public void archive(Warehouse warehouse) {
		if (warehouse == null || warehouse.businessUnitCode == null) {
			throw new WebApplicationException("Warehouse to archive must be specified.", 400);
		}

		Warehouse current = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
		if (current == null || current.archivedAt != null) {
			throw new WebApplicationException(
					"Active warehouse with business unit code " + warehouse.businessUnitCode + " not found.", 404);
		}

		current.archivedAt = LocalDateTime.now();
		warehouseStore.update(current);
	}
}