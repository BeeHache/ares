package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.utils.DateTimeReformatter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.net.*;
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

    public FeedDTO feedDTOFromUrl(URL url) {
        try {
            Optional<FeedDTO> optionalFeedDTO = buildFeedDTO(url);
            return optionalFeedDTO.orElse(null);
        } catch (Exception e) {
                throw new ServiceException("Can't read from " + url.toString(), e);
        }
    }

    public Feed feedFromUrl(String urlString) {
        return feedFromUrl( new Feed(), urlString);
    }

    public Feed feedFromUrl(@NonNull Feed feed, String urlString) {
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
        Optional<FeedDTO> optionalFeedDTOFeedDTO = buildFeedDTO(feed.getUrl());
        if (optionalFeedDTOFeedDTO.isEmpty()) {
            return false;
        }

        FeedDTO feedDTO = optionalFeedDTOFeedDTO.get();
        feedDTO.setId(feed.getId());
        feed.setDto(feedDTO);
        feed.setPodcast(feedDTO.getIsPodcast());
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
            // if ANY feedItem has an enclosure that this is a podcast
            feedDto.setIsPodcast(feedItems.stream().anyMatch(rssItem -> !rssItem.getEnclosures().isEmpty()));
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
