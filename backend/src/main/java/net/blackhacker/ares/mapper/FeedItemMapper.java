package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class FeedItemMapper implements ModelDTOMapper<FeedItem, FeedItemDTO> {

    final private EnclosureMapper enclosureMapper;

   public FeedItemMapper(EnclosureMapper enclosureMapper) {
       this.enclosureMapper =  enclosureMapper;
   }

    @Override
    public FeedItemDTO toDTO(FeedItem feedItem) {
       FeedItemDTO feedItemDTO = new FeedItemDTO();
       if (feedItem.getTitle() != null) {
           feedItemDTO.setTitle(feedItem.getTitle());
       }

       if (feedItem.getDescription() != null) {
           feedItemDTO.setDescription(feedItem.getDescription());
       }

       if(feedItem.getLink()!=null){
           feedItemDTO.setLink(feedItem.getLink().toString());
       }

       if (feedItem.getDate() != null) {
           feedItemDTO.setDate(feedItem.getDate().format(DateTimeFormatter.ISO_INSTANT));
       }
       feedItemDTO.getEnclosures().addAll(
               feedItem.getEnclosures().stream().map(enclosureMapper::toDTO).toList());

        return feedItemDTO;
    }

    @Override
    public FeedItem toModel(FeedItemDTO feedItemDTO) {
       FeedItem feedItem = new FeedItem();
       feedItem.setTitle(feedItemDTO.getTitle());
       feedItem.setDescription(feedItemDTO.getDescription());
       try {
           feedItem.setLink(new URI(feedItemDTO.getLink()).toURL());
       } catch(Exception e) {
           log.info(e.getMessage());
       }
       if (feedItemDTO.getDate() != null) {
           try {
               feedItem.setDate(Instant.parse(feedItemDTO.getDate()).atZone(ZoneId.of("UTC")));
           } catch(Exception e) {
               log.info("Error parsing date: {}", feedItemDTO.getDate());
           }
       }

       feedItem.getEnclosures().addAll(
               feedItemDTO.getEnclosures().stream().map(enclosureMapper::toModel).toList()
       );

        return feedItem;
    }
}
