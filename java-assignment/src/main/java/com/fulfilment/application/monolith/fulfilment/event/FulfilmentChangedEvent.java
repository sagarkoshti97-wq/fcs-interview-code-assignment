package com.fulfilment.application.monolith.fulfilment.event;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;

public class FulfilmentChangedEvent {

  public enum Action { ASSIGNED, REMOVED }

  public final FulfilmentAssociation association;
  public final Action action;

  public FulfilmentChangedEvent(FulfilmentAssociation association, Action action) {
    this.association = association;
    this.action = action;
  }
}