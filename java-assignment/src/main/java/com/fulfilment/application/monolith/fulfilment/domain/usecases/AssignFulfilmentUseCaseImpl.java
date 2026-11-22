package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssociation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.AssignFulfilmentUseCase;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import com.fulfilment.application.monolith.fulfilment.event.FulfilmentChangedEvent;
import com.fulfilment.application.monolith.fulfilment.validator.FulfilmentValidator;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class AssignFulfilmentUseCaseImpl implements AssignFulfilmentUseCase {

  @Inject FulfilmentStore fulfilmentStore;

  @Inject Instance<FulfilmentValidator> validators;

  @Inject Event<FulfilmentChangedEvent> fulfilmentChangedEvent;

  @Override
  @Transactional
  public FulfilmentAssociation assign(
      String warehouseBusinessUnitCode, Long storeId, Long productId) {
    List<FulfilmentAssociation> existingAssociations = fulfilmentStore.findAll();

    List<FulfilmentValidator> orderedValidators =
        validators.stream()
            .sorted(Comparator.comparingInt(this::resolvePriority))
            .toList();

    for (FulfilmentValidator validator : orderedValidators) {
      validator.validate(warehouseBusinessUnitCode, storeId, productId, existingAssociations);
    }

    FulfilmentAssociation existing =
        fulfilmentStore.findExact(warehouseBusinessUnitCode, storeId, productId);
    if (existing != null) {
      return existing;
    }

    FulfilmentAssociation created =
        fulfilmentStore.save(
            new FulfilmentAssociation(warehouseBusinessUnitCode, storeId, productId));
    fulfilmentChangedEvent.fire(
        new FulfilmentChangedEvent(created, FulfilmentChangedEvent.Action.ASSIGNED));
    return created;
  }

  private int resolvePriority(FulfilmentValidator validator) {
    Class<?> targetClass =
        validator.getClass().isSynthetic() || validator.getClass().getName().contains("_ClientProxy")
            ? validator.getClass().getSuperclass()
            : validator.getClass();
    Priority priority = targetClass.getAnnotation(Priority.class);
    return priority != null ? priority.value() : 100;
  }
}