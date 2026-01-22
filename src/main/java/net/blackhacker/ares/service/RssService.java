package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.model.Image;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RssService {

    final private URLFetchService urlFetchService;

    public RssService(URLFetchService urlFetchService) {
        this.urlFetchService = urlFetchService;
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
        try {
            List<Item> rssItems = parseRss(urlString);

            if (rssItems.isEmpty()) {
                return null;
            }

            final Feed feed = new Feed();
            Channel channel = rssItems.get(0).getChannel();
            feed.setTitle(channel.getTitle());
            feed.setDescription(channel.getDescription());
            feed.setLink(new URI(channel.getLink()).toURL());
            feed.setUrl(new URI(urlString).toURL());
            if (channel.getImage().isPresent()) {

                ResponseEntity<byte[]> response = urlFetchService.fetchBytes(channel.getImage().get().getUrl());
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
            Set<FeedItem> feedItems = rssItems.stream().map(rssItem -> {
                FeedItem feedItem = new FeedItem();
                feedItem.setFeed(feed);
                if (rssItem.getTitle().isPresent()) {
                    feedItem.setTitle(rssItem.getTitle().get());
                }
                if (rssItem.getDescription().isPresent()) {
                    feedItem.setDescription(rssItem.getDescription().get());
                }
                if (rssItem.getLink().isPresent()) {
                    feedItem.setLink(rssItem.getLink().get());
                }

                if (rssItem.getUpdatedAsZonedDateTime().isPresent()){
                    feedItem.setDate(rssItem.getUpdatedAsZonedDateTime().get());
                } else if (rssItem.getPubDateAsZonedDateTime().isPresent()) {
                    feedItem.setDate(rssItem.getPubDateAsZonedDateTime().get());
                }

                rssItem.getEnclosures().forEach(
                    enclosure -> {
                        try {
                            Enclosure enclosureModel = new Enclosure();
                            enclosureModel.setUrl(new URI(enclosure.getUrl()).toURL());
                            enclosureModel.setLength(enclosure.getLength().isPresent() ? enclosure.getLength().get() : null);
                            enclosureModel.setType(enclosure.getType());
                            enclosureModel.setFeedItem(feedItem);
                            feedItem.getEnclosures().add(enclosureModel);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                );

                return feedItem;
            }).collect(Collectors.toSet());
            feed.setItems(feedItems);
            return feed;

        } catch (Exception e) {
            throw new ServiceException("Can't read from " + urlString, e);
        }
    }

    private List<Item> parseRss(String urlString) {
        ResponseEntity<byte[]> response = urlFetchService.fetchBytes(urlString);
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
