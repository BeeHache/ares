package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.stereotype.Component;

@Component
public class FeedItemMapper implements  ModelDTOMapper<FeedItem, FeedItemDTO> {

    final ImageMapper imageMapper;
    final EnclosureMapper enclosureMapper;


    public FeedItemMapper(ImageMapper imageMapper){
        this.imageMapper = imageMapper;
        this.enclosureMapper = new EnclosureMapper();
    }

    @Override
    public FeedItemDTO toDTO(FeedItem item) {
        if (item == null) return null;

        FeedItemDTO dto = new FeedItemDTO();
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setLink(item.getLink());
        dto.setImage(imageMapper.toDTO(item.getImage()));
        dto.setDate(item.getDate());
        dto.setEnclosures(item.getEnclosures().stream().map(enclosureMapper::toDTO).toList());
        return dto;
    }

    @Override
    public FeedItem toModel(FeedItemDTO dto) {
        if (dto == null) return null;

        FeedItem item = new FeedItem();
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setLink(dto.getLink());
        item.setImage(imageMapper.toModel(dto.getImage()));
        item.setDate(dto.getDate());
        item.setEnclosures(dto.getEnclosures().stream().map(enclosureMapper::toModel).toList());
        return item;
    }
}
