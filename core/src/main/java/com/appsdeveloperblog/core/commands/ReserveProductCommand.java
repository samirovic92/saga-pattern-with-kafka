package com.appsdeveloperblog.core.commands;

import java.util.UUID;

public record ReserveProductCommand(
        UUID orderId,
        UUID productId,
        Integer productQuantity
) {
}
