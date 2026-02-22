package net.blackhacker.ares.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private CacheService cacheService;

    @Test
    void evictSingleCacheValue_shouldEvict_whenCacheExists() {
        String cacheName = "testCache";
        String key = "key";

        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        cacheService.evictSingleCacheValue(cacheName, key);

        verify(cache).evict(key);
    }

    @Test
    void evictSingleCacheValue_shouldDoNothing_whenCacheDoesNotExist() {
        String cacheName = "nonExistentCache";
        String key = "key";

        when(cacheManager.getCache(cacheName)).thenReturn(null);

        cacheService.evictSingleCacheValue(cacheName, key);

        verify(cache, never()).evict(any());
    }

    @Test
    void evictAllCacheValues_shouldClear_whenCacheExists() {
        String cacheName = "testCache";

        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        cacheService.evictAllCacheValues(cacheName);

        verify(cache).clear();
    }

    @Test
    void evictAllCacheValues_shouldDoNothing_whenCacheDoesNotExist() {
        String cacheName = "nonExistentCache";

        when(cacheManager.getCache(cacheName)).thenReturn(null);

        cacheService.evictAllCacheValues(cacheName);

        verify(cache, never()).clear();
    }
}
