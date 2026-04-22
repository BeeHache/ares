package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.utils.FeedParser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.util.*;


@Slf4j
@Service
public class RssService {

    private final URLFetchService urlFetchService;
    private final FeedItemRepository feedItemRepository;
    private final ObjectProvider<FeedParser> feedParserProvider;


    public RssService(URLFetchService urlFetchService, 
                      FeedItemRepository feedItemRepository,
                      ObjectProvider<FeedParser> feedParserProvider) {
        this.urlFetchService = urlFetchService;
        this.feedItemRepository = feedItemRepository;
        this.feedParserProvider = feedParserProvider;
    }


    public Feed buildFeedFromUrl(@NonNull String urlString) {
        return buildFeedFromUrl( new Feed(), urlString);
    }

    public Feed buildFeedFromUrl(@NonNull Feed feed, @NonNull String urlString) {
        try {
            feed.setUrl(new URI(urlString).toURL());
            if (updateFeed(feed)) {
                return feed;
            }
        } catch(Exception e) {
            log.error("Failed to build feed from URL {}: {}", urlString, e.getMessage());
        }
        return null;
    }

    public boolean updateFeed(@NonNull Feed feed) {
        if (feed.getUrl() == null) {
            return false;
        }

        ResponseEntity<byte[]> response = urlFetchService.fetchBytes(feed.getUrl().toString(), null);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null){
            log.warn("Failed to fetch feed content from {}: {}", feed.getUrl(), response.getStatusCode());
            return false;
        }

        try {
            // Use a temporary feed object to parse into, preventing immediate pollution of the main object
            Feed tempFeed = new Feed();
            tempFeed.setId(feed.getId());
            tempFeed.setUrl(feed.getUrl());
            
            FeedParser parser = feedParserProvider.getObject();
            parser.parse(tempFeed, new ByteArrayInputStream(response.getBody()));

            // Update metadata only if the parser found data
            if (tempFeed.getTitle() != null) feed.setTitle(tempFeed.getTitle());
            if (tempFeed.getDescription() != null) feed.setDescription(tempFeed.getDescription());
            if (tempFeed.getLink() != null) feed.setLink(tempFeed.getLink());
            if (tempFeed.getImageUrl() != null) feed.setImageUrl(tempFeed.getImageUrl());
            feed.setPodcast(tempFeed.isPodcast());

            // Process items: only keep new ones
            List<FeedItem> newItems = tempFeed.getFeedItems().stream()
                    .filter(item -> !isDuplicate(feed, item))
                    .peek(item -> item.setFeed(feed))
                    .toList();

            if (!newItems.isEmpty()) {
                feed.getFeedItems().addAll(newItems);
                
                // Update pubdate to the newest item found in this batch
                newItems.stream()
                        .map(FeedItem::getDate)
                        .filter(Objects::nonNull)
                        .max(Comparator.naturalOrder())
                        .ifPresent(feed::setPubdate);
            }

            return true;
        } catch (Exception e) {
            log.error("Error parsing/updating feed {}: {}", feed.getUrl(), e.getMessage(), e);
            return false;
        }
    }

    private boolean isDuplicate(Feed feed, FeedItem item) {
        if (item.getGuid() != null && !item.getGuid().isBlank()) {
            return feedItemRepository.findByGuid(item.getGuid()).isPresent();
        }
        // Fallback: Check by Feed + Title for items without GUIDs
        return feedItemRepository.findByFeedAndTitle(feed.getId(), item.getTitle()).isPresent();
    }
}
