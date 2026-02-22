package net.blackhacker.ares.events;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FeedSavedEvent implements Serializable {
    private UUID feedId;
}
