package com.appsdeveloperblog.core.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductReservedEvent(
        UUID orderId,
        UUID productId,
        Integer productQuantity,
        BigDecimal productPrice
) {
}
