package net.blackhacker.ares.service;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.model.Image;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RssService {

    final private URLFetchService urlFetchService;

    public RssService(URLFetchService urlFetchService) {
        this.urlFetchService = urlFetchService;
    }

    public FeedDTO feedDTOFromUrl(String urlString) {
        try {
            List<Item> rssItems = pareRss(urlString);
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
            List<Item> rssItems = pareRss(urlString);

            if (rssItems.isEmpty()) {
                return null;
            }

            Feed feed = new Feed();
            Channel channel = rssItems.get(0).getChannel();
            feed.setTitle(channel.getTitle());
            feed.setDescription(channel.getDescription());
            feed.setLink(channel.getLink());
            if (channel.getImage().isPresent()) {
                URL url = new URI(channel.getImage().get().getLink()).toURL();
                URLConnection connection = url.openConnection();
                String contentType = connection.getContentType();

                try(InputStream is = connection.getInputStream()){
                    byte[] bytes = is.readAllBytes();
                    Image image = new Image();
                    image.setData(bytes);
                    image.setContentType(contentType);
                    feed.setImage(image);
                }
            }
            List<FeedItem> feedItems = rssItems.stream().map(rssItem -> {
                FeedItem feedItem = new FeedItem();
                if (rssItem.getTitle().isPresent()) {
                    feedItem.setTitle(rssItem.getTitle().get());
                }
                if (rssItem.getDescription().isPresent()) {
                    feedItem.setDescription(rssItem.getDescription().get());
                }
                if (rssItem.getLink().isPresent()) {
                    feedItem.setLink(rssItem.getLink().get());
                }

                return feedItem;
            }).collect(Collectors.toList());
            feed.setItems(feedItems);
            return feed;

        } catch (Exception e) {
            throw new ServiceException("Can't read from " + urlString, e);
        }
    }

    private List<Item> pareRss(String urlString){
        byte[] bytes = urlFetchService.fetchImageBytes(urlString);
        return  new RssReader().read(new ByteArrayInputStream(bytes)).toList();
    }

}
