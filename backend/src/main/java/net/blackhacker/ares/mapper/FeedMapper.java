package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.model.Feed;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FeedMapper implements ModelDTOMapper<Feed, FeedDTO> {

    @Override
    public FeedDTO toDTO(Feed feed) {
        FeedDTO feedDTO = new FeedDTO();
        feedDTO.setId(feed.getId());
        feedDTO.setTitle(feed.getTitle());
        feedDTO.setDescription(feed.getDescription());
        if (feed.getUrl()!=null) {
            feedDTO.setUrl(feed.getUrl().toString());
        }
        if (feed.getLink()!=null) {
            feedDTO.setLink(feed.getLink().toString());
        }
        if (feed.getImageUrl()!=null){
            feedDTO.setImageUrl(feed.getImageUrl().toString());
        }
        feedDTO.setIsPodcast(feed.isPodcast());
        if (feed.getSubscribers() != null){
            feedDTO.setSubscribers(feed.getSubscribers());
        }

        if (!feed.getFeedItems().isEmpty()) {
            feedDTO.setPubdate(feed.getFeedItems().iterator().next().getDate().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return feedDTO;
    }

    @Override
    public Feed toModel(FeedDTO feedDTO) {
        Feed feed = new Feed();
        feed.setId(feedDTO.getId());
        feed.setTitle(feedDTO.getTitle());
        feed.setDescription(feedDTO.getDescription());
        feed.setUrlFromString(feedDTO.getUrl());
        feed.setLinkFromString(feedDTO.getLink());
        feed.setImageUrlFromString(feedDTO.getImageUrl());
        feed.setPodcast(feedDTO.getIsPodcast());
        return feed;
    }
}
