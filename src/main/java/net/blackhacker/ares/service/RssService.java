package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.validation.URLValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class RssService {

    final private URLFetchService urlFetchService;
    final private DateTimeFormatter dateTimeFormatter;
    final private DateTimeFormatter feedDateFormatter;
    final private URLValidator urlValidator;
    final private ObjectMapper objectMapper;


    public RssService(URLFetchService urlFetchService, URLValidator urlValidator,  ObjectMapper objectMapper) {
        this.urlFetchService = urlFetchService;
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        feedDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
        this.urlValidator = urlValidator;
        this.objectMapper = objectMapper;
    }

    public FeedDTO feedDTOFromUrl(String urlString) {
        try {
            List<Item> rssItems = parseRss(urlString);
            if (rssItems.isEmpty()) {
                return null;
            }

            FeedDTO feedDTO = new FeedDTO();
            Channel channel = rssItems.get(0).getChannel();
            feedDTO.setTitle(channel.getTitle());
            feedDTO.setDescription(channel.getDescription());
            feedDTO.setLink(channel.getLink());

            if (channel.getImage().isPresent()) {
                URL url = new URI(channel.getImage().get().getLink()).toURL();
                URLConnection connection = url.openConnection();
                String contentType = connection.getContentType();

                try(InputStream is = connection.getInputStream()){
                    byte[] bytes = is.readAllBytes();
                   ImageDTO imageDTO = new ImageDTO();
                   imageDTO.setData(bytes);
                   imageDTO.setContentType(contentType);
                   feedDTO.setImage(imageDTO);
                }
            }

            List<FeedItemDTO> feedItemDTOs = rssItems.stream().map(rssItem -> {
                FeedItemDTO feedItemDTO = new FeedItemDTO();
                if (rssItem.getTitle().isPresent()) {
                    feedItemDTO.setTitle(rssItem.getTitle().get());
                }
                if (rssItem.getDescription().isPresent()) {
                    feedItemDTO.setDescription(rssItem.getDescription().get());
                }
                if (rssItem.getLink().isPresent()) {
                    feedItemDTO.setLink(rssItem.getLink().get());
                }
                return feedItemDTO;
            }).toList();
            feedDTO.setItems(feedItemDTOs);
            return feedDTO;

        } catch (Exception e) {
                throw new ServiceException("Can't read from " + urlString, e);
        }
    }

    public Feed feedFromUrl(String urlString) {
        return feedFromUrl( new Feed(), urlString);
    }

    public Feed feedFromUrl(@NonNull Feed feed, String urlString) {
        try {
            feed.setUrl(new URI(urlString).toURL());
            updateFeed(feed);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return feed;
    }

    public boolean updateFeed(Feed feed) {
        boolean feedUpdated = false;
        FeedDTO feedDto = new  FeedDTO();
        try {
            List<Item> rssItems = parseRss(feed.getUrl().toString());

            if (rssItems.isEmpty()) {
                return false;
            }

            // Get channel info from 1st item
            Channel channel = rssItems.get(0).getChannel();
            feedDto.setId(feed.getId());
            feedDto.setTitle(channel.getTitle());
            feedDto.setDescription(channel.getDescription());
            feedDto.setLink(channel.getLink());

            if (false) {
                // Fetch image data
                if (channel.getImage().isPresent()) {
                    ZonedDateTime lastModified = feed.getLastModified();
                    Map<String, String> headers = new HashMap<>();
                    headers.put(HttpHeaders.IF_MODIFIED_SINCE, lastModified.format(dateTimeFormatter));

                    ResponseEntity<byte[]> response = urlFetchService.fetchBytes(channel.getImage().get().getUrl(), headers);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Image image = new Image();
                        image.setData(response.getBody());

                        MediaType mt = response.getHeaders().getContentType();
                        if (mt != null) {
                            image.setContentType(mt.toString());
                        }
                        feed.setImage(image);
                    }
                }
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

                ///  Loop through each enclosure and add it to the FeedItem
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
                    feedItemDTO.setDate(rssItem.getUpdatedAsZonedDateTime().get());
                }

                return feedItemDTO;
            }).toList();

            feedDto.getItems().addAll(feedItems);
            feed.setJsonData(objectMapper.writer().writeValueAsString(feedDto));
            return true;

        } catch (Exception e) {
            throw new ServiceException(String.format("Couldn't update feed :%s:%s",feed.getId(), e.getMessage()));
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

        return new RssReader().read(new ByteArrayInputStream(response.getBody())).toList();
    }

}
