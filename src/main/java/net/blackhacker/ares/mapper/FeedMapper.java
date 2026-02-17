package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FeedMapper implements ModelDTOMapper<Feed, FeedDTO> {
    final private FeedItemMapper feedItemMapper;

    public FeedMapper(FeedItemMapper feedItemMapper) {
        this.feedItemMapper = feedItemMapper;
    }

    @Override
    public FeedDTO toDTO(Feed feed) {
        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setTitle(feed.getTitle());
        feedDTO.setDescription(feed.getDescription());
        if (feed.getLink()!=null) {
            feedDTO.setLink(feed.getLink().toString());
        }
        feedDTO.setIsPodcast(feed.isPodcast());
        feedDTO.getItems().addAll(feed.getFeedItems().stream().map(feedItemMapper::toDTO).toList());
        return feedDTO;
    }

    @Override
    public Feed toModel(FeedDTO feedDTO) {
        Feed feed = new Feed();
        feed.setId(feedDTO.getId());
        feed.setTitle(feedDTO.getTitle());
        feed.setDescription(feedDTO.getDescription());
        try {
            feed.setLink(new URI(feedDTO.getLink()).toURL());
        } catch(Exception e) {
            log.error(e.getMessage());
        }
        feed.setPodcast(feedDTO.getIsPodcast());
        feed.getFeedItems().addAll(feedDTO.getItems().stream().map(feedItemDTO -> {
            FeedItem fi = feedItemMapper.toModel(feedItemDTO);
            fi.setFeed(feed);
            return fi;
        }).toList());
        return feed;
    }
}
