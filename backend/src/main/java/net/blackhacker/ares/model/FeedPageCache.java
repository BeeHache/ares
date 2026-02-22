package net.blackhacker.ares.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@RedisHash("FeedPageCache")
public class FeedPageCache implements Serializable {
    @Id
    private UUID feedId;

    private Integer totalPages;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long ttl;
}
