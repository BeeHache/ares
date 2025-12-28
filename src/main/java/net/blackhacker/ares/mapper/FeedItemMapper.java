package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.stereotype.Component;

@Component
public class FeedItemMapper implements  ModelDTOMapper<FeedItem, FeedItemDTO> {
    @Override
    public FeedItemDTO toDTO(FeedItem item) {
        if (item == null) return null;

        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setLink(item.getLink());
        dto.setImage(item.getImage());
        dto.setDate(item.getDate());
        return dto;
    }

    @Override
    public FeedItem toModel(FeedItemDTO dto) {
        if (dto == null) return null;

        FeedItem item = new FeedItem();
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLink(dto.getLink());
        item.setImage(dto.getImage());
        item.setDate(dto.getDate());
        return item;
    }
}
