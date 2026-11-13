package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private ReplaceWarehouseUseCase replaceUseCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    replaceUseCase = new ReplaceWarehouseUseCase(store, new LocationGateway());

    Warehouse initial = new Warehouse();
    initial.businessUnitCode = "MWH.001";
    initial.location = "ZWOLLE-001";
    initial.capacity = 30;
    initial.stock = 15;
    store.create(initial);
  }

  @Test
  void testReplaceWarehouseSuccessfully() {
    Warehouse replacement = new Warehouse();
    replacement.businessUnitCode = "MWH.001";
    replacement.location = "ZWOLLE-001";
    replacement.capacity = 40;
    replacement.stock = 15;

    replaceUseCase.replace(replacement);

    Warehouse active = store.findByBusinessUnitCode("MWH.001");
    assertNotNull(active);
    assertEquals(40, active.capacity);
    assertEquals(15, active.stock);
  }

  @Test
  void testReplaceWarehouseValidationErrors() {
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(null));

    // Non-existent warehouse
    Warehouse missing = new Warehouse();
    missing.businessUnitCode = "MWH.404";
    missing.location = "ZWOLLE-001";
    missing.capacity = 30;
    missing.stock = 15;
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(missing));

    // Capacity cannot accommodate old stock (15)
    Warehouse smallCap = new Warehouse();
    smallCap.businessUnitCode = "MWH.001";
    smallCap.location = "ZWOLLE-001";
    smallCap.capacity = 10;
    smallCap.stock = 15;
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(smallCap));

    // Stock does not match old stock (15)
    Warehouse wrongStock = new Warehouse();
    wrongStock.businessUnitCode = "MWH.001";
    wrongStock.location = "ZWOLLE-001";
    wrongStock.capacity = 35;
    wrongStock.stock = 20;
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(wrongStock));

    // Invalid location
    Warehouse badLoc = new Warehouse();
    badLoc.businessUnitCode = "MWH.001";
    badLoc.location = "UNKNOWN";
    badLoc.capacity = 35;
    badLoc.stock = 15;
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(badLoc));

    // Exceeds location maxCapacity (ZWOLLE-001 maxCapacity is 40)
    Warehouse overCap = new Warehouse();
    overCap.businessUnitCode = "MWH.001";
    overCap.location = "ZWOLLE-001";
    overCap.capacity = 45;
    overCap.stock = 15;
    assertThrows(WebApplicationException.class, () -> replaceUseCase.replace(overCap));
  }
}