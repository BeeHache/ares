package net.blackhacker.ares.mapper;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Item;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedMapper implements ModelDTOMapper<Feed, FeedDTO> {

    private final FeedItemMapper feedItemMapper;

    public FeedMapper(FeedItemMapper feedItemMapper){
        this.feedItemMapper = feedItemMapper;
    }


    @Override
    public FeedDTO toDTO(Feed feed) {

        if (feed == null) return null;
        FeedDTO dto = new FeedDTO();
        dto.setTitle(feed.getTitle());
        dto.setDescription(feed.getDescription());
        dto.setLink(feed.getLink());
        dto.setImage(feed.getImage());
        dto.setPodcast(feed.isPodcast());
        dto.setLastModified(feed.getLastModified());
        if (feed.getItems() != null) {
            dto.setItems(feed.getItems().stream()
                    .map(feedItemMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        return dto;

    }

    @Override
    public Feed toModel(FeedDTO dto) {
        if (dto == null) return null;
        Feed feed = new Feed();
        feed.setTitle(dto.getTitle());
        feed.setDescription(dto.getDescription());
        feed.setLink(dto.getLink());
        feed.setImage(dto.getImage());
        feed.setPodcast(dto.isPodcast());
        feed.setLastModified(dto.getLastModified());

        if (dto.getItems() != null) {
            feed.setItems(dto.getItems().stream()
                    .map(feedItemMapper::toModel)
                    .collect(Collectors.toList()));
        }

        return feed;
    }

    public FeedDTO toDTO(List<Item> items){
        if (items == null || items.isEmpty()) {
            return null;
        }

        FeedDTO dto = new FeedDTO();
        Channel channel = items.getFirst().getChannel();
        dto.setTitle(channel.getTitle());
        dto.setDescription(channel.getDescription());
        dto.setLink(channel.getLink());
        if (channel.getImage().isPresent()) {
            dto.setImage(channel.getImage().get().getLink());
        }
        return dto;
    }

    public Feed toModel(List<Item> items){
        if (items == null || items.isEmpty()) {
            return null;
        }

        Feed feed = new Feed();
        Channel channel = items.getFirst().getChannel();
        feed.setTitle(channel.getTitle());
        feed.setDescription(channel.getDescription());
        feed.setLink(channel.getLink());
        if (channel.getImage().isPresent()) {
            feed.setImage(channel.getImage().get().getLink());
        }
        return feed;
    }
}
