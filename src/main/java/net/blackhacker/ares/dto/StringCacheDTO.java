package net.blackhacker.ares.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@RedisHash("StringCache")
public class StringCacheDTO implements Serializable {
    @Id
    private UUID id;
    private String string;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long ttl = 1L;
}
