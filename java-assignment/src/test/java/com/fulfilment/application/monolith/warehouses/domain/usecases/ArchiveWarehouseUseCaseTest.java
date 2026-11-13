package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.InMemoryWarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private ArchiveWarehouseUseCase archiveUseCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    archiveUseCase = new ArchiveWarehouseUseCase(store);
  }

  @Test
  void testArchiveWarehouseSuccessfully() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "MWH.001";
    w.location = "ZWOLLE-001";
    w.capacity = 30;
    w.stock = 10;
    store.create(w);

    archiveUseCase.archive(w);
    assertNotNull(w.archivedAt);
    assertNull(store.findByBusinessUnitCode("MWH.001"));
  }

  @Test
  void testArchiveNonExistentOrNullWarehouseThrows() {
    assertThrows(WebApplicationException.class, () -> archiveUseCase.archive(null));

    Warehouse missing = new Warehouse();
    missing.businessUnitCode = "MWH.MISSING";
    assertThrows(WebApplicationException.class, () -> archiveUseCase.archive(missing));
  }
}