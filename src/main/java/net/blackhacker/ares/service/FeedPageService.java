package net.blackhacker.ares.service;

import net.blackhacker.ares.model.FeedPageCache;
import net.blackhacker.ares.repository.crud.FeedPageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FeedPageService {
    final FeedPageRepository feedPageRepository;

    public FeedPageService(FeedPageRepository feedPageRepository) {
        this.feedPageRepository = feedPageRepository;
    }

    public void storePageNumber(UUID feedId, Integer pageNumber) {
        Optional<FeedPageCache> pageCache = feedPageRepository.findById(feedId);
        FeedPageCache feedPageCache;
        if (pageCache.isPresent()) {
            feedPageCache = pageCache.get();
            feedPageCache.setTotalPages(Math.max(feedPageCache.getTotalPages(), pageNumber));
        } else {
            feedPageCache = new FeedPageCache();
            feedPageCache.setTotalPages(pageNumber);
        }
        feedPageRepository.save(feedPageCache);
    }

    public void deletePageNumbers(UUID feedId) {
        feedPageRepository.deleteById(feedId);
    }

    public Optional<Integer> getTotalPages(UUID feedId) {
        Optional<FeedPageCache> pageCache = feedPageRepository.findById(feedId);
        return pageCache.map(FeedPageCache::getTotalPages);
    }
}
