package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    useCase = new CreateWarehouseUseCase(store, new LocationGateway());
  }

  @Test
  void testCreateWarehouseSuccessfully() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "MWH.100";
    w.location = "AMSTERDAM-001";
    w.capacity = 50;
    w.stock = 20;

    useCase.create(w);

    Warehouse saved = store.findByBusinessUnitCode("MWH.100");
    assertNotNull(saved);
    assertNotNull(saved.createdAt);
    assertEquals(50, saved.capacity);
  }

  @Test
  void testCreateWarehouseValidationFailures() {
    // Null or blank BU code
    assertThrows(WebApplicationException.class, () -> useCase.create(null));
    Warehouse blank = new Warehouse();
    blank.businessUnitCode = "  ";
    assertThrows(WebApplicationException.class, () -> useCase.create(blank));

    // Duplicate BU code
    Warehouse w1 = new Warehouse();
    w1.businessUnitCode = "MWH.DUP";
    w1.location = "ZWOLLE-001"; // max 1 warehouse, maxCapacity 40
    w1.capacity = 30;
    w1.stock = 10;
    useCase.create(w1);

    assertThrows(WebApplicationException.class, () -> useCase.create(w1));

    // Invalid location
    Warehouse badLoc = new Warehouse();
    badLoc.businessUnitCode = "MWH.BADLOC";
    badLoc.location = "INVALID-LOC";
    badLoc.capacity = 10;
    badLoc.stock = 5;
    assertThrows(WebApplicationException.class, () -> useCase.create(badLoc));

    // Max number of warehouses reached at ZWOLLE-001 (max = 1)
    Warehouse tooMany = new Warehouse();
    tooMany.businessUnitCode = "MWH.SECOND";
    tooMany.location = "ZWOLLE-001";
    tooMany.capacity = 5;
    tooMany.stock = 1;
    assertThrows(WebApplicationException.class, () -> useCase.create(tooMany));

    // Exceeds location max capacity at ZWOLLE-002 (maxCapacity = 50)
    Warehouse overLocCap = new Warehouse();
    overLocCap.businessUnitCode = "MWH.OVERCAP";
    overLocCap.location = "ZWOLLE-002";
    overLocCap.capacity = 60;
    overLocCap.stock = 10;
    assertThrows(WebApplicationException.class, () -> useCase.create(overLocCap));

    // Invalid capacity <= 0 or stock > capacity
    Warehouse badStock = new Warehouse();
    badStock.businessUnitCode = "MWH.BADSTOCK";
    badStock.location = "ZWOLLE-002";
    badStock.capacity = 20;
    badStock.stock = 25;
    assertThrows(WebApplicationException.class, () -> useCase.create(badStock));

    Warehouse zeroCap = new Warehouse();
    zeroCap.businessUnitCode = "MWH.ZEROCAP";
    zeroCap.location = "ZWOLLE-002";
    zeroCap.capacity = 0;
    zeroCap.stock = 0;
    assertThrows(WebApplicationException.class, () -> useCase.create(zeroCap));
  }

  static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> list = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return list.stream().filter(w -> w.archivedAt == null).toList();
    }

    @Override
    public void create(Warehouse warehouse) {
      list.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {}

    @Override
    public void remove(Warehouse warehouse) {
      list.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return list.stream()
          .filter(w -> w.archivedAt == null && buCode.equals(w.businessUnitCode))
          .findFirst()
          .orElse(null);
    }
  }
}