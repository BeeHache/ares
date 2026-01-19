package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.OpmlWriter;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Head;
import be.ceau.opml.entity.Opml;
import be.ceau.opml.entity.Outline;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.utils.URLConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpmlService {

    private final URLFetchService urlFetchService;
    private final URLConverter urlConverter;

    OpmlService(URLFetchService urlFetchService, URLConverter urlConverter) {
        this.urlFetchService = urlFetchService;
        this.urlConverter = urlConverter;
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

    private Collection<Feed> parseOPML(InputStream inputStream) throws Exception{
        Opml opml = new OpmlParser().parse(inputStream);

        return opml.getBody().getOutlines().stream().map(outline -> {

            Map<String, String> attributes = outline.getAttributes();

            Feed feed = new Feed();
            feed.setTitle(attributes.get("title"));
            feed.setDescription(attributes.get("description"));
            URL link = new URLConverter().convertToEntityAttribute(attributes.get("xmlUrl"));
            feed.setLink(link);

            ResponseEntity<byte[]> response = urlFetchService.fetchBytes(attributes.get("imageUrl"));
            if (response.getStatusCode().is2xxSuccessful()) {
                Image image = new Image();
                image.setData(response.getBody());
                image.setContentType(Objects.requireNonNull(response.getHeaders().getContentType()).toString());
                feed.setImage(image);
            }
            return feed;
        }).collect(Collectors.toList());
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
