package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
     LocationGateway locationGateway = new LocationGateway();

    // when
     Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
     assertEquals(location.identification, "ZWOLLE-001");
     assertEquals(1, location.maxNumberOfWarehouses);
     assertEquals(40, location.maxCapacity);
  }
  
  @Test
  public void testWhenResolveNonExistingOrNullLocationShouldReturnNull() {
    LocationGateway locationGateway = new LocationGateway();
    assertNull(locationGateway.resolveByIdentifier("NON-EXISTING-999"));
    assertNull(locationGateway.resolveByIdentifier(null));
  }
}
