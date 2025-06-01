package com.appsdeveloperblog.core.events;

import java.util.UUID;

public record OrderRejectedEvent(
        UUID orderId
) {
}
