package net.blackhacker.ares.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.utils.FeedParser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.*;


@Slf4j
@Service
public class RssService {

    private final URLFetchService urlFetchService;
    private final ObjectProvider<FeedParser> feedParserProvider;


    public RssService(URLFetchService urlFetchService,
                      ObjectProvider<FeedParser> feedParserProvider) {
        this.urlFetchService = urlFetchService;
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
            FeedParser parser = feedParserProvider.getObject();
            parser.parse(feed, new ByteArrayInputStream(response.getBody()));
            return true;
        } catch (Exception e) {
            log.error("Error parsing/updating feed {}: {}", feed.getUrl(), e.getMessage(), e);
            return false;
        }
    }
}
