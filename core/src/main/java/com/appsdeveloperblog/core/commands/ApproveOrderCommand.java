package com.appsdeveloperblog.core.commands;

import java.util.UUID;

public record ApproveOrderCommand(
        UUID orderId
) {
}
