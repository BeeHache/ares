package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.utils.URLConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FeedMapper implements ModelDTOMapper<Feed, FeedDTO> {

    private final FeedItemMapper feedItemMapper;
    private final ImageMapper imageMapper;
    private final URLConverter urlConverter;


    public FeedMapper(FeedItemMapper feedItemMapper, ImageMapper imageMapper, URLConverter urlConverter)
    {
        this.feedItemMapper = feedItemMapper;
        this.imageMapper = imageMapper;
        this.urlConverter = urlConverter;
    }


    @Override
    public FeedDTO toDTO(Feed feed) {

        FeedDTO dto = new FeedDTO();
        if (feed != null) {
            dto.setId(feed.getId());
            dto.setTitle(feed.getTitle());
            dto.setDescription(feed.getDescription());
            dto.setLink(feed.getLink().toString());
            dto.setImage(imageMapper.toDTO(feed.getImage()));
            dto.setPodcast(feed.isPodcast());
            dto.setLastModified(feed.getLastModified());
            if (feed.getItems() != null) {
                dto.setItems(feed.getItems().stream()
                        .map(feedItemMapper::toDTO)
                        .collect(Collectors.toList()));
            }
        }
        return dto;

    }

    @Override
    public Feed toModel(FeedDTO dto) {
        if (dto == null) return null;
        Feed feed = new Feed();
        feed.setId(dto.getId());
        feed.setTitle(dto.getTitle());
        feed.setDescription(dto.getDescription());
        feed.setLink(urlConverter.convertToEntityAttribute(dto.getLink()));
        feed.setImage(imageMapper.toModel(dto.getImage()));
        feed.setPodcast(dto.isPodcast());
        feed.setLastModified(dto.getLastModified());

        if (dto.getItems() != null) {
            feed.setItems(dto.getItems().stream()
                    .map(feedItemMapper::toModel)
                    .collect(Collectors.toSet()));
        }

        return feed;
    }
}
