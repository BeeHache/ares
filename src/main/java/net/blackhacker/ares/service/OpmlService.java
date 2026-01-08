package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Opml;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.Image;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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

        byte[] bytes = urlFetchService.fetchImageBytes(urlString);

        try {
            return parseOPML(bytes);
        } catch (Exception e){
            throw new ServiceException("Couldn't parse url " + urlString, e);
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
            feed.setLink(attributes.get("xmlUrl"));

            String contentType = urlFetchService.getContentType(attributes.get("xmlUrl"));
            byte[] bytes = urlFetchService.fetchImageBytes(attributes.get("imageUrl"));
            Image image = new Image();
            image.setData(bytes);
            image.setContentType(contentType);
            feed.setImage(image);

            return feed;

        }).collect(Collectors.toList());

    }
}
