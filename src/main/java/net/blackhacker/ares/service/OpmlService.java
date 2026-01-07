package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Opml;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpmlService {
    private final FeedMapper feedMapper;
    private final FeedRepository feedRepository;

    @Autowired
    public OpmlService(FeedMapper feedMapper, FeedRepository feedRepository){
        this.feedMapper = feedMapper;
        this.feedRepository = feedRepository;
    }

    public Collection<Feed> importFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new ServiceException("File is empty");
        }

        try {
            try (InputStream is = file.getInputStream()) {
                return readOPML(new OpmlParser().parse(is));
            }
        } catch (Exception e){
            throw new ServiceException("Couldn't parse from url " + file.getOriginalFilename(), e);
        }
    }

    public Collection<Feed> importFeed(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            try(InputStream is = url.openStream()){
                return readOPML(new OpmlParser().parse(is));
            }
        } catch (Exception e){
            throw new ServiceException("Couldn't parse url " + urlString, e);
        }
    }

    private Collection<Feed> readOPML(Opml opml){
        return opml.getBody().getOutlines().stream().map(outline -> {
            Map<String, String> attributes = outline.getAttributes();

            Feed feed = feedRepository.findByLink(attributes.get("xmlUrl"));
            if (feed == null) {
                FeedDTO dto = new FeedDTO();
                dto.setTitle(attributes.get("title"));
                dto.setDescription(attributes.get("description"));
                dto.setLink(attributes.get("xmlUrl"));
                dto.setImage(attributes.get("imageUrl"));
                feed = feedMapper.toModel(dto);
            }
            return feed;
        }).collect(Collectors.toList());
    }
}
