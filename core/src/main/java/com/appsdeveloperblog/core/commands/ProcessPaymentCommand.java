package com.appsdeveloperblog.core.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
        UUID orderId,
        UUID productId,
        Integer productQuantity,
        BigDecimal productPrice
) {
}
