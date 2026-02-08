package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedImage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class OpmlService {

    private final URLFetchService urlFetchService;

    OpmlService(URLFetchService urlFetchService) {
        this.urlFetchService = urlFetchService;
    }

    public Collection<Feed> importFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new ServiceException("File is empty");
        }

        try {
            try (InputStream is = file.getInputStream()) {
                return parseOPML(is);
            }
        } catch (Exception e){
            throw new ServiceException("Couldn't parse from url " + file.getOriginalFilename(), e);
        }
    }

    public Collection<Feed> importFeed(String urlString) {

        ResponseEntity<String> response = urlFetchService.fetchString(urlString);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new ServiceException("Problem fetching " + urlString);
        }

        try {
            return parseOPML(response.getBody());
        } catch (Exception e){
            throw new ServiceException("Couldn't parse url " + urlString, e);
        }
    }

    private Collection<Feed> parseOPML(String opmlString) throws Exception{
        try(InputStream is = new ByteArrayInputStream(opmlString.getBytes(StandardCharsets.UTF_8))){
            return parseOPML(is);
        }
    }


    private Collection<Feed> parseOPML(byte[] bytes) throws Exception{
        try(InputStream is = new ByteArrayInputStream(bytes)){
            return parseOPML(is);
        }
    }

    /**
     * Resursively walks the Outline tree build a collection of feeds
     * @param outlines
     * @return A Collection of Feeds
     */
    private Collection<Feed>  opmlWalker(Collection<Outline> outlines){
        Collection<Feed> feeds = new ArrayList<>();
        for (Outline outlineItem : outlines){
            try {
                String xmlUrl = outlineItem.getAttribute("xmlUrl");
                String imageUrl = outlineItem.getAttribute("imageUrl");
                if (xmlUrl != null && !xmlUrl.isEmpty()) {
                    Feed feed = new Feed();
                    feed.setUrl(new URI(xmlUrl).toURL());
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        FeedImage feedImage = new FeedImage();
                        feedImage.setImageUrl(new URI(imageUrl).toURL());
                        feedImage.setFeed(feed);
                        feed.setFeedImage(feedImage);
                    }
                    feeds.add(feed);
                }
                if (outlineItem.getSubElements() != null && !outlineItem.getSubElements().isEmpty()) {
                    feeds.addAll(opmlWalker(outlineItem.getSubElements()));
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return feeds;
    }

    private Collection<Feed> parseOPML(InputStream inputStream) throws Exception{
        Opml opml = new OpmlParser().parse(inputStream);
        List<Outline> ols = opml.getBody().getOutlines();
        return opmlWalker(ols);
    }
}
