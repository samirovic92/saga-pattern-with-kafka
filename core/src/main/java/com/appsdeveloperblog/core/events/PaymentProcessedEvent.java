package com.appsdeveloperblog.core.events;

import java.util.UUID;

public record PaymentProcessedEvent(
        UUID orderId,
        UUID paymentId
) {
}
