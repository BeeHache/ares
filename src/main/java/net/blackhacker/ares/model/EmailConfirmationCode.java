package net.blackhacker.ares.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Data
@RedisHash("EmailConfirmationCodes")
public class EmailConfirmationCode implements Serializable {
    @Id
    private String code;
    private String email;

    @TimeToLive(unit = TimeUnit.DAYS)
    private Long ttl;

}
