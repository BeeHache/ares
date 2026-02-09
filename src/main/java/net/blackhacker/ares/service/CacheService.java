package net.blackhacker.ares.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    final static public String FEED_CACHE = "FEED_CACHE";
    final static public String FEED_DTOS_CACHE = "FEED_DTOS_CACHE";
    final static public String FEED_IMAGE_CACHE = "FEED_IMAGE_CACHE";
    final static public String FEED_TITLES_CACHE = "FEED_TITLES_CACHE";

    final static public String SUBSCRIPTION_CACHE = "SUBSCRIPTION_CACHE";

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictSingleCacheValue(String cacheName, Object cacheKey) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(cacheKey);
        }
    }

    public void evictAllCacheValues(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}