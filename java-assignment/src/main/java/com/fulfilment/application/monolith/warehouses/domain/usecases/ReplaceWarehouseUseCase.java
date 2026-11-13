package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this(warehouseStore, null);
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null
        || newWarehouse.businessUnitCode == null
        || newWarehouse.businessUnitCode.isBlank()) {
      throw new WebApplicationException("Business unit code must be provided.", 400);
    }

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null || current.archivedAt != null) {
      throw new WebApplicationException(
          "Active warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " does not exist.",
          404);
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new WebApplicationException("New warehouse capacity must be greater than zero.", 400);
    }

    // 1. Capacity Accommodation: new capacity must accommodate previous warehouse stock
    if (current.stock != null && newWarehouse.capacity < current.stock) {
      throw new WebApplicationException(
          "New warehouse capacity cannot accommodate the stock from the warehouse being replaced.",
          400);
    }

    // 2. Stock Matching: confirm stock of new warehouse matches stock of previous warehouse
    if (!Objects.equals(newWarehouse.stock, current.stock)) {
      throw new WebApplicationException(
          "New warehouse stock must match the stock of the previous warehouse.", 400);
    }

    if (newWarehouse.stock != null && newWarehouse.stock > newWarehouse.capacity) {
      throw new WebApplicationException(
          "New warehouse stock cannot exceed its capacity.", 400);
    }

    // 3. Location Validation & Capacity Check (excluding the warehouse being archived)
    if (locationResolver != null) {
      Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
      if (location == null) {
        throw new WebApplicationException(
            "Invalid location: " + newWarehouse.location, 400);
      }

      List<Warehouse> otherWarehousesAtLocation =
          warehouseStore.getAll().stream()
              .filter(
                  w ->
                      w.archivedAt == null
                          && !w.businessUnitCode.equals(current.businessUnitCode)
                          && location.identification.equals(w.location))
              .toList();

      if (otherWarehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
        throw new WebApplicationException(
            "Maximum number of warehouses reached for location " + newWarehouse.location, 400);
      }

      int otherTotalCapacity =
          otherWarehousesAtLocation.stream()
              .mapToInt(w -> w.capacity != null ? w.capacity : 0)
              .sum();

      if (otherTotalCapacity + newWarehouse.capacity > location.maxCapacity) {
        throw new WebApplicationException(
            "New warehouse capacity exceeds maximum capacity for location "
                + newWarehouse.location,
            400);
      }
    }

    // Archive current warehouse and create the replacement warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}