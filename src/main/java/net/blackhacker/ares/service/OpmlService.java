package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.OpmlWriter;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Head;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.utils.URLConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    private Collection<Feed> opmlWalker(Collection<Outline> outlines){
        Collection<Feed> feeds = new ArrayList<>();
        for (Outline outlineItem : outlines){
            try {
                String xmlUrl = outlineItem.getAttribute("xmlUrl");
                String htmlUrl = outlineItem.getAttribute("htmlUrl");
                String title = outlineItem.getAttribute("title");
                String text = outlineItem.getAttribute("text");
                String imageUrl = outlineItem.getAttribute("imageUrl");
                if (xmlUrl != null && !xmlUrl.isEmpty()) {
                    Feed feed = new Feed();
                    if (title != null) {
                        feed.setTitle(title);
                    } else if (text != null) {
                        feed.setTitle(text);
                    }
                    if (htmlUrl != null) {
                        feed.setLink(new URI(htmlUrl).toURL());
                    }
                    feed.setUrl(new URI(xmlUrl).toURL());
                    if(imageUrl != null){
                        ResponseEntity<byte[]> re = urlFetchService.fetchBytes(imageUrl);
                        MediaType contentType = re.getHeaders().getContentType();

                        if (re.getStatusCode() == HttpStatus.OK) {
                            Image image = new Image();
                            if (contentType != null) {
                                image.setContentType(contentType.toString());
                            }
                            if (re.hasBody()){
                                image.setData(re.getBody());
                            }

                            feed.setImage(image);
                        }
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

    public String generateOPML(Collection<Feed> feeds) {
        try {
        Head head = new Head("Ares Export", new Date().toString(),
                null,null,null,null,
                null,null,null,null,
                null,null,null);
        Body body = new Body(
            feeds.stream().map(feed -> {
                Map<String, String> attributes = new HashMap<>();
                attributes.put("text", feed.getTitle());
                attributes.put("title", feed.getTitle());
                attributes.put("type", "rss");
                attributes.put("xmlUrl", feed.getLink().toString());
                if (feed.getDescription() != null) {
                    attributes.put("description", feed.getDescription());
                }
                // Assuming htmlUrl is not stored in Feed, omitting it or could be same as link if appropriate
                return new Outline(attributes, Collections.emptyList());
            }).toList()
        );

        return new OpmlWriter().write(new Opml("2.0", head, body));
        } catch (Exception e){
            throw new ServiceException("Couldn't generate OPML", e);
        }
    }
}
