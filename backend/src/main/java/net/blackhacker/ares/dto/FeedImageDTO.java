package net.blackhacker.ares.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@RedisHash("feed_images")
public class FeedImageDTO implements Serializable {
    @Id
    private UUID id;

    private String imageUrl;
    private String contentType;
    private byte[] content;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long ttl = 1L;
}
