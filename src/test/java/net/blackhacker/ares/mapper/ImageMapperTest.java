package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.ImageDTO;
import net.blackhacker.ares.model.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImageMapperTest {

    private ImageMapper imageMapper;

    @BeforeEach
    void setUp() {
        imageMapper = new ImageMapper();
    }

    @Test
    void toDTO_shouldMapImageToImageDTO() {
        // Arrange
        Image image = new Image();
        image.setContentType("image/png");
        image.setData(new byte[]{1, 2, 3});

        // Act
        ImageDTO dto = imageMapper.toDTO(image);

        // Assert
        assertNotNull(dto);
        assertEquals("image/png", dto.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, dto.getData());
    }

    @Test
    void toDTO_shouldReturnNull_whenImageIsNull() {
        assertNull(imageMapper.toDTO(null));
    }

    @Test
    void toModel_shouldMapImageDTOToImage() {
        // Arrange
        ImageDTO dto = new ImageDTO();
        dto.setContentType("image/jpeg");
        dto.setData(new byte[]{4, 5, 6});

        // Act
        Image image = imageMapper.toModel(dto);

        // Assert
        assertNotNull(image);
        assertEquals("image/jpeg", image.getContentType());
        assertArrayEquals(new byte[]{4, 5, 6}, image.getData());
    }

    @Test
    void toModel_shouldReturnNull_whenImageDTOIsNull() {
        assertNull(imageMapper.toModel(null));
    }
}
