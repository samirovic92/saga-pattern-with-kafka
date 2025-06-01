package com.appsdeveloperblog.core.events;

import java.util.UUID;

public record ProductReservationCancelledEvent(
        UUID orderId,
        UUID productId
) {
}
