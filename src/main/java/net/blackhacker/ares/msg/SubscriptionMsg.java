package net.blackhacker.ares.msg;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SubscriptionMsg implements Serializable {
    private Long userId;
    private UUID feedId;
}
