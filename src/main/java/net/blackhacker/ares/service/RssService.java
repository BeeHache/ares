package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedImage;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.utils.DateTimeReformatter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class RssService {

    final private URLFetchService urlFetchService;

    static final private RssReader rssReader = new RssReader();


    public RssService(URLFetchService urlFetchService) {
        this.urlFetchService = urlFetchService;
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
        try {
            feed.setLink(new URI(channel.getLink()).toURL());

        } catch(Exception e) {

        }
        if  (channel.getImage().isPresent()) {
            try {
                FeedImage feedImage = new FeedImage();
                feedImage.setImageUrl(new URI(channel.getImage().get().getUrl()).toURL());

            } catch (Exception e) {

            }
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
                    feedItem.setLink(new URI(rssItem.getLink().get()).toURL());
                }catch(Exception e) {

                }
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
                                enclosure.setUrl(new URI(rssEnclosure.getUrl()).toURL());
                            } catch(Exception e) {

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
        }).toList();

        feed.getFeedItems().addAll(feedItems);
        // if ANY feedItem has an enclosure then this feed a podcast
        feed.setPodcast(feedItems.stream().anyMatch(
                feedItem -> !feedItem.getEnclosures().isEmpty()));

        return true;
    }

    private Optional<FeedDTO> buildFeedDTO(URL url) {
        FeedDTO feedDto = new  FeedDTO();
        try {
            List<Item> rssItems = parseRss(url.toString());

            if (rssItems.isEmpty()) {
                return Optional.empty();
            }

            // Get channel info from 1st item
            Channel channel = rssItems.get(0).getChannel();
            feedDto.setTitle(channel.getTitle());
            feedDto.setDescription(channel.getDescription());
            feedDto.setLink(channel.getLink());
            if  (channel.getImage().isPresent()) {
                feedDto.setImageUrl(channel.getImage().get().getUrl());
            }

            List<FeedItemDTO> feedItems = rssItems.stream().map(rssItem -> {

                final FeedItemDTO feedItemDTO = new FeedItemDTO();

                if (rssItem.getTitle().isPresent()) {
                    feedItemDTO.setTitle(rssItem.getTitle().get());
                }
                if (rssItem.getDescription().isPresent()) {
                    feedItemDTO.setDescription(rssItem.getDescription().get());
                }
                if (rssItem.getLink().isPresent()) {
                    feedItemDTO.setLink(rssItem.getLink().get());
                }
                if(rssItem.getPubDate().isPresent()){
                    String pubdate = rssItem.getPubDate().get();
                    feedItemDTO.setDate(DateTimeReformatter.reformat(pubdate));
                }

                //  Loop through each enclosure and add it to the FeedItem
                rssItem.getEnclosures().forEach(
                        enclosure -> {
                            try {
                                EnclosureDTO enclosureDTO = new EnclosureDTO();
                                enclosureDTO.setUrl(enclosure.getUrl());
                                if(enclosure.getLength().isPresent()) {
                                    enclosureDTO.setLength(enclosure.getLength().get());
                                }
                                enclosureDTO.setType(enclosure.getType());
                                feedItemDTO.getEnclosures().add(enclosureDTO);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                );

                if (rssItem.getUpdated().isPresent()){
                    feedItemDTO.setDate(DateTimeReformatter.reformat(rssItem.getUpdated().get()));
                }

                return feedItemDTO;
            }).toList();

            feedDto.getItems().addAll(feedItems);
            // if ANY feedItem has an enclosure then this feed a podcast
            feedDto.setIsPodcast(feedItems.stream()
                    .anyMatch(feedItemDTO -> !feedItemDTO.getEnclosures().isEmpty()));

            return Optional.of(feedDto);

        } catch (Exception e) {
            throw new ServiceException(String.format("Couldn't build FeedDTO from url: %s: %s",url.toString(), e.getMessage()));
        }
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

}
