package net.blackhacker.ares.service;

import be.ceau.opml.OpmlParseException;
import be.ceau.opml.OpmlParser;
import be.ceau.opml.entity.Opml;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.repository.FeedRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
public class UtilsService {

    private final FeedMapper feedMapper;
    private final FeedRepository feedRepository;

    public UtilsService(FeedMapper feedMapper, FeedRepository feedRepository){
        this.feedMapper = feedMapper;
        this.feedRepository = feedRepository;
    }


    public FeedDTO readFeedDTOUrl(String url) {
        try {
            List<Item> items = new RssReader().read(url).toList();
            return feedMapper.toDTO(items);
        } catch (IOException e) {
                throw new ServiceException("Can't read from " + url, e);
        }
    }

    public Feed readFeedUrl(String url) {
        try {
            List<Item> items = new RssReader().read(url).toList();
            return feedMapper.toModel(items);
        } catch (IOException e) {
            throw new ServiceException("Can't read from " + url, e);
        }
    }

    @Async
    public CompletableFuture<Collection<Feed>> opml(MultipartFile opmlFile) {
        if (opmlFile == null || opmlFile.isEmpty()) {
            throw new ServiceException("File is empty");
        }
        try {
            List<Feed> feeds = new ArrayList<>();
            Opml opml = new OpmlParser().parse(opmlFile.getInputStream());
            for (be.ceau.opml.entity.Outline outline : opml.getBody().getOutlines()) {
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

                feeds.add(feed);
            }
            return CompletableFuture.completedFuture(feeds);
        } catch (IOException e) {
            throw new ServiceException("Couldn't read file " + opmlFile.getOriginalFilename(), e);
        } catch (OpmlParseException e) {
            throw new ServiceException("Couldn't parse file " + opmlFile.getOriginalFilename(), e);
        }
    }
}
