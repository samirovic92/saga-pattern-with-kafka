package com.appsdeveloperblog.core.commands;

import java.util.UUID;

public record RejectOrderCommand(
        UUID orderId
) {
}
