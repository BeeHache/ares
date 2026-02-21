package net.blackhacker.ares.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SubscriptionEvent implements Serializable {
    private Long userId;
    private UUID feedId;
}
