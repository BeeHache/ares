package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.utils.DateTimeReformatter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Service
public class RssService {

    final private URLFetchService urlFetchService;
    final private FeedItemRepository feedItemRepository;

    static final private RssReader rssReader = new RssReader();


    public RssService(URLFetchService urlFetchService, FeedItemRepository feedItemRepository) {
        this.urlFetchService = urlFetchService;
        this.feedItemRepository = feedItemRepository;
    }


    public Feed feedFromUrl(@NonNull String urlString) {
        return feedFromUrl( new Feed(), urlString);
    }

    public Feed feedFromUrl(@NonNull Feed feed, @NonNull String urlString) {
        try {
            feed.setUrl(new URI(urlString).toURL());
            if (updateFeed(feed)) {
                return feed;
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public boolean updateFeed(@NonNull Feed feed) {
        if (feed.getUrl() == null) {
            return false;
        }

        List<Item> rssItems = parseRss(feed.getUrl().toString());

        if (rssItems.isEmpty()) {
            return false;
        }

        // Get channel info from 1st item
        Channel channel = rssItems.get(0).getChannel();
        feed.setTitle(channel.getTitle());
        feed.setDescription(channel.getDescription());
        
        // Handle relative links
        String channelLink = channel.getLink();
        if (channelLink != null) {
            try {
                URL resolvedLink = resolveUrl(feed.getUrl(), channelLink);
                feed.setLink(resolvedLink);
            } catch (Exception e) {
                log.warn("Could not resolve feed link: {}", channelLink, e);
            }
        }

        if  (channel.getImage().isPresent()) {
            feed.setImageUrlFromString(channel.getImage().get().getUrl());
        }

        List<FeedItem> feedItems = rssItems.stream().map(rssItem -> {

            final FeedItem feedItem = new FeedItem();
            feedItem.setFeed(feed);

            if (rssItem.getTitle().isPresent()) {
                feedItem.setTitle(rssItem.getTitle().get());
            }
            if (rssItem.getDescription().isPresent()) {
                feedItem.setDescription(rssItem.getDescription().get());
            }
            if (rssItem.getLink().isPresent()) {
                try {
                    URL resolvedItemLink = resolveUrl(feed.getUrl(), rssItem.getLink().get());
                    feedItem.setLink(resolvedItemLink);
                } catch(Exception e) {
                    log.warn("Could not resolve item link: {}", rssItem.getLink().orElse("null"));
                }
            }
            
            // If link is still null, we can't save this item. Return null and filter later.
            if (feedItem.getLink() == null) {
                return null;
            }

            if(rssItem.getPubDate().isPresent()){
                String pubdate = rssItem.getPubDate().get();
                feedItem.setDate(DateTimeReformatter.parse(pubdate));
            }

            //  Loop through each enclosure and add it to the FeedItem
            rssItem.getEnclosures().forEach(
                    rssEnclosure -> {
                        try {
                            Enclosure enclosure = new Enclosure();
                            try {
                                URL resolvedEnclosureUrl = resolveUrl(feed.getUrl(), rssEnclosure.getUrl());
                                enclosure.setUrl(resolvedEnclosureUrl);
                            } catch(Exception e) {
                                log.warn("Could not resolve enclosure link: {}", rssEnclosure.getUrl());
                            }
                            if(rssEnclosure.getLength().isPresent()) {
                                enclosure.setLength(rssEnclosure.getLength().get());
                            }
                            enclosure.setType(rssEnclosure.getType());
                            feedItem.getEnclosures().add(enclosure);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
            );

            if (rssItem.getUpdated().isPresent()){
                feedItem.setDate(ZonedDateTime.parse(rssItem.getUpdated().get(), DateTimeFormatter.ISO_DATE_TIME));
            }

            return feedItem;
        }).filter(fi -> {
            return Objects.nonNull(fi) &&
                    feedItemRepository.findByLink(fi.getLink()).isEmpty(); // the link doesnot
        }).toList();

        if (!feedItems.isEmpty()) {
            //Feed items are sorted by date newest to oldest.
            //The pubdate of the feed is that of the newest item.
            feed.getFeedItems().addAll(feedItems);
            FeedItem fi = feed.getFeedItems().iterator().next();
            feed.setPubdate(fi.getDate());
        }

        // if ANY feedItem has an enclosure then this feed a podcast
        feed.setPodcast(feedItems.stream().anyMatch(
                feedItem -> !feedItem.getEnclosures().isEmpty()));

        return true;
    }

    private List<Item> parseRss(String urlString) {
        return parseRss(urlString,null);
    }

    private List<Item> parseRss(String urlString, Map<String, String> headers) {
        ResponseEntity<byte[]> response = urlFetchService.fetchBytes(urlString, headers);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Problem fetching {}: {}", urlString, response.getStatusCode());
            return List.of();
        }
        if (response.getBody() == null) {
            log.warn("Response Body of {} is empty",urlString);
            return List.of();
        }

        return rssReader.read(new ByteArrayInputStream(response.getBody())).toList();
    }

    private URL resolveUrl(URL base, String relative) throws MalformedURLException, URISyntaxException {
        if (relative == null) return null;
        URI relativeUri = new URI(relative);
        if (relativeUri.isAbsolute()) {
            return relativeUri.toURL();
        }
        return base.toURI().resolve(relativeUri).toURL();
    }
}
