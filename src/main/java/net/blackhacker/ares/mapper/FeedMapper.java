package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FeedMapper implements ModelDTOMapper<Feed, FeedDTO> {

    private final FeedItemMapper feedItemMapper;
    private final ImageMapper imageMapper;


    public FeedMapper(FeedItemMapper feedItemMapper, ImageMapper imageMapper)
    {
        this.feedItemMapper = feedItemMapper;
        this.imageMapper = imageMapper;
    }


    @Override
    public FeedDTO toDTO(Feed feed) {

        if (feed == null) return null;
        FeedDTO dto = new FeedDTO();
        dto.setTitle(feed.getTitle());
        dto.setDescription(feed.getDescription());
        dto.setLink(feed.getLink());
        dto.setImage(imageMapper.toDTO(feed.getImage()));
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
        feed.setImage(imageMapper.toModel(dto.getImage()));
        feed.setPodcast(dto.isPodcast());
        feed.setLastModified(dto.getLastModified());

        if (dto.getItems() != null) {
            feed.setItems(dto.getItems().stream()
                    .map(feedItemMapper::toModel)
                    .collect(Collectors.toList()));
        }

        return feed;
    }
}
