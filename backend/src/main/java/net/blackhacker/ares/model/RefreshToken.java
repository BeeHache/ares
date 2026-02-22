package net.blackhacker.ares.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@RedisHash("RefreshTokens")
public class RefreshToken implements Serializable {
    @Id
    private String token;
    private String username;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long ttl;

    public RefreshToken(String username, Long ttl) {
        this.username = username;
        this.ttl = ttl;
        this.token = UUID.randomUUID().toString().replaceAll("-", "");
    }
}
