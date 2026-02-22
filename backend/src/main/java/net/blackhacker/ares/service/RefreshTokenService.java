package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.RefreshToken;
import net.blackhacker.ares.repository.crud.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RefreshTokenService {

    @Value("${security.jwt.refresh_expiration_ms: 86400000}") // default 24 hrs
    private Long refreshTokenDurationMs;

    final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findById(token);
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteById(token);
    }

    public RefreshToken generateToken(Account account){
        RefreshToken rt = new RefreshToken(account.getUsername(), refreshTokenDurationMs);
        refreshTokenRepository.save(rt);
        return rt;
    }
}
