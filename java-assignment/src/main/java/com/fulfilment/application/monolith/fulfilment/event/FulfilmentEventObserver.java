package com.fulfilment.application.monolith.fulfilment.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import org.jboss.logging.Logger;

@ApplicationScoped
public class FulfilmentEventObserver {

  private static final Logger LOGGER = Logger.getLogger(FulfilmentEventObserver.class.getName());

  public void onFulfilmentChanged(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) FulfilmentChangedEvent event) 
  {
    LOGGER.infof(
        "Post-commit fulfilment event [%s]: warehouse=%s, storeId=%d, productId=%d",
        event.action,
        event.association.warehouseBusinessUnitCode,
        event.association.storeId,
        event.association.productId);
  }
}