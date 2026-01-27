package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.EnclosureRepository;
import net.blackhacker.ares.repository.FeedItemRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RssService {

    final private URLFetchService urlFetchService;
    final private FeedItemRepository feedItemRepository;
    final private EnclosureRepository enclosureRepository;
    final private DateTimeFormatter dateTimeFormatter;
    final private DateTimeFormatter feedDateFormatter;
    final private URLValidator urlValidator;


    public RssService(URLFetchService urlFetchService, FeedItemRepository feedItemRepository,
                      EnclosureRepository enclosureRepository, URLValidator urlValidator) {
        this.urlFetchService = urlFetchService;
        this.feedItemRepository = feedItemRepository;
        this.enclosureRepository = enclosureRepository;
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        feedDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
        this.urlValidator = urlValidator;
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
        try {
            List<Item> rssItems = parseRss(feed.getUrl().toString());

            if (rssItems.isEmpty()) {
                return false;
            }

            // Get channel info from 1st item
            Channel channel = rssItems.get(0).getChannel();

            if (!Objects.equals(feed.getTitle(), channel.getTitle())) {
                feed.setTitle(channel.getTitle());
                feedUpdated=true;
            }

            if (!Objects.equals(feed.getDescription(), channel.getDescription())) {
                feed.setDescription(channel.getDescription());
                feedUpdated = true;
            }

            if (!Objects.equals(feed.getLinkAsString(), channel.getLink())) {
                String channelLink = channel.getLink();

                try {
                    urlValidator.validateURL(channelLink);
                    feed.setLinkFromString(channelLink);
                    feedUpdated = true;
                } catch (Exception e) {
                    log.error("Invalid link url " + channelLink);
                }
            }

            // Fetch image data
            if (channel.getImage().isPresent()) {
                ZonedDateTime lastModified =  feed.getLastModified();
                Map<String,String> headers = new HashMap<>();
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

            Set<FeedItem> feedItems = rssItems.stream().map(rssItem -> {

                FeedItem newFeedItem = new FeedItem();

                if (rssItem.getLink().isEmpty()){
                    return newFeedItem;
                }

                URL feedItemLink;
                String feedItemLinkString= rssItem.getLink().get();
                if (!feedItemLinkString.startsWith("http")) {
                    feedItemLinkString = "http://" + feedItemLinkString;
                }
                try {
                    feedItemLink = new URI(feedItemLinkString).toURL();
                } catch (Exception e) {
                    log.error(String.format("Can't use %s as link : %s" , feedItemLinkString, e.getMessage()));
                    return newFeedItem;
                }

                FeedItem foundFeedItem = null;
                Optional<FeedItem> oFeedItem = feedItemRepository.findByLink(feedItemLink);
                if (oFeedItem.isPresent()) {
                    //Item already exists
                    foundFeedItem = oFeedItem.get();
                }

                final FeedItem feedItem = oFeedItem.isPresent() ? foundFeedItem : newFeedItem;

                feedItem.setFeed(feed);
                if (rssItem.getTitle().isPresent()) {
                    feedItem.setTitle(rssItem.getTitle().get());
                }
                if (rssItem.getDescription().isPresent()) {
                    feedItem.setDescription(rssItem.getDescription().get());
                }

                feedItem.setLink(feedItemLink);


                ///  Loop through each enclosure and add it to the FeedItem
                rssItem.getEnclosures().forEach(
                    enclosure -> {
                        try {
                            URL enclosureUrl = new URI(enclosure.getUrl()).toURL();
                            Optional<Enclosure> existingEnclosure = enclosureRepository.findByUrl(enclosureUrl);
                            Enclosure enclosureModel = existingEnclosure.orElseGet(() -> {
                                Enclosure newEnclosure = new Enclosure();
                                newEnclosure.setUrl(enclosureUrl);
                                newEnclosure.setLength(enclosure.getLength().isPresent() ? enclosure.getLength().get() : null);
                                newEnclosure.setType(enclosure.getType());
                                return newEnclosure;
                            });
                            enclosureModel.setFeedItem(feedItem);
                            feedItem.getEnclosures().add(enclosureModel);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                );

                if (rssItem.getUpdated().isPresent()){
                    feedItem.setDate(rssItem.getUpdatedAsZonedDateTime().get());
                } else if (rssItem.getPubDate().isPresent()) {
                    String pubdate = rssItem.getPubDate().get();
                    ZonedDateTime zdt = null;
                    try {
                        zdt = rssItem.getPubDateAsZonedDateTime().get();
                    } catch (Exception e) {
                        try {
                            zdt = ZonedDateTime.parse(pubdate, feedDateFormatter);
                        } catch (Exception pe) {
                            log.error(String.format("Could not parse date time '%s':%s", pubdate, pe.getMessage()));
                        }
                    }

                    feedItem.setDate(zdt);
                }

                return feedItem;
            }).filter(feedItem -> feedItem.getLink() != null).collect(Collectors.toSet());

            if (feed.getItems().addAll(feedItems)) {
                feedUpdated=true;
            }
        } catch (Exception e) {
            throw new ServiceException(String.format("Couldn't update feed :%s:%s",feed.getId(), e.getMessage()));
        }
        return feedUpdated;
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
