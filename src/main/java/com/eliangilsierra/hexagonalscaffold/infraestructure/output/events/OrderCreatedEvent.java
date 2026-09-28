package com.eliangilsierra.hexagonalscaffold.infraestructure.output.events;

/**
 * The in-process stand-in for a message this scaffold would otherwise publish
 * to a broker. Deliberately minimal (just the id): a real event payload design
 * is a whole topic on its own, and isn't the point of this scaffold.
 */
public record OrderCreatedEvent(Long orderId) {
}
