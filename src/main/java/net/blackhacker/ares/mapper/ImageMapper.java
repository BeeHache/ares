package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Image;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper implements ModelDTOMapper<Image, ImageDTO>{
    @Override
    public ImageDTO toDTO(Image image) {
        if (image == null) return null;

        ImageDTO dto = new ImageDTO();
        dto.setId(image.getId());
        dto.setContentType(image.getContentType());
        dto.setData(image.getData());
        return dto;
    }

    @Override
    public Image toModel(ImageDTO imageDTO) {
        if (imageDTO == null) return null;
        Image image = new Image();
        image.setId(imageDTO.getId());
        image.setContentType(imageDTO.getContentType());
        image.setData(imageDTO.getData());
        return image;
    }
}
