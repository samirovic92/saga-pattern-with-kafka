package com.appsdeveloperblog.core.commands;

import java.util.UUID;

public record CancelProductReservationCommand(
        UUID orderId,
        UUID productId,
        Integer productQuantity
) {
}
