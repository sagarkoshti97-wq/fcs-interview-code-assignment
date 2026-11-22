package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import com.fulfilment.application.monolith.fulfilment.domain.exception.FulfilmentNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.ports.RemoveFulfilmentUseCase;
import com.fulfilment.application.monolith.fulfilment.event.FulfilmentChangedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class RemoveFulfilmentUseCaseImpl implements RemoveFulfilmentUseCase {

  @Inject FulfilmentStore fulfilmentStore;

  @Inject Event<FulfilmentChangedEvent> fulfilmentChangedEvent;

  @Override
  @Transactional
  public void remove(Long id) {
    FulfilmentAssociation existing = fulfilmentStore.findById(id);
    if (existing == null) {
      throw new FulfilmentNotFoundException("Fulfilment association not found.");
    }
    fulfilmentStore.deleteById(id);
    fulfilmentChangedEvent.fire(
        new FulfilmentChangedEvent(existing, FulfilmentChangedEvent.Action.REMOVED));
  }
}