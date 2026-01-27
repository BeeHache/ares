package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.model.FeedItem;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
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
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setLink(item.getLink().toString());
        dto.setImage(imageMapper.toDTO(item.getImage()));
        dto.setDate(item.getDate());
        dto.setEnclosures(item.getEnclosures().stream().map(enclosureMapper::toDTO).toList());
        return dto;
    }

    @Override
    public FeedItem toModel(FeedItemDTO dto) {
        if (dto == null) return null;

        FeedItem item = new FeedItem();
        item.setId(dto.getId());
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        try {
            item.setLink(new URI(dto.getLink()).toURL());
        } catch(Exception e) {
            log.error(e.getMessage(),e);
        }
        item.setImage(imageMapper.toModel(dto.getImage()));
        item.setDate(dto.getDate());
        item.setEnclosures(dto.getEnclosures().stream().map(enclosureMapper::toModel).toList());
        return item;
    }
}
