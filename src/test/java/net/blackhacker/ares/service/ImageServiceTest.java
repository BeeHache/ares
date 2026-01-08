package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.ImageRepositry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepositry imageRepositry;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepositry);
    }

    @Test
    void getImage_shouldReturnImage_whenImageExists() {
        // Arrange
        Long id = 1L;
        Image image = new Image();
        image.setId(id);
        when(imageRepositry.findById(id)).thenReturn(Optional.of(image));

        // Act
        Image result = imageService.getImage(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getImage_shouldReturnNull_whenImageDoesNotExist() {
        // Arrange
        Long id = 1L;
        when(imageRepositry.findById(id)).thenReturn(Optional.empty());

        // Act
        Image result = imageService.getImage(id);

        // Assert
        assertNull(result);
    }

    @Test
    void saveImage_shouldReturnSavedImage() {
        // Arrange
        Image image = new Image();
        when(imageRepositry.save(image)).thenReturn(image);

        // Act
        Image result = imageService.saveImage(image);

        // Assert
        assertNotNull(result);
        assertEquals(image, result);
    }

    @Test
    void deleteImage_shouldCallDeleteById() {
        // Arrange
        Long id = 1L;

        // Act
        imageService.deleteImage(id);

        // Assert
        verify(imageRepositry, times(1)).deleteById(id);
    }

    @Test
    void updateImage_shouldUpdateExistingImage_whenImageExists() {
        // Arrange
        Long id = 1L;
        Image existingImage = new Image();
        existingImage.setId(id);
        existingImage.setContentType("image/png");
        existingImage.setData(new byte[]{1, 2, 3});

        Image newImage = new Image();
        newImage.setContentType("image/jpeg");
        newImage.setData(new byte[]{4, 5, 6});

        when(imageRepositry.findById(id)).thenReturn(Optional.of(existingImage));
        when(imageRepositry.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Image result = imageService.updateImage(id, newImage);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(newImage.getContentType(), result.getContentType());
        assertArrayEquals(newImage.getData(), result.getData());
    }

    @Test
    void updateImage_shouldSaveNewImage_whenImageDoesNotExist() {
        // Arrange
        Long id = 1L;
        Image newImage = new Image();
        newImage.setContentType("image/jpeg");
        newImage.setData(new byte[]{4, 5, 6});

        when(imageRepositry.findById(id)).thenReturn(Optional.empty());
        when(imageRepositry.save(newImage)).thenReturn(newImage);

        // Act
        Image result = imageService.updateImage(id, newImage);

        // Assert
        assertNotNull(result);
        assertEquals(newImage, result);
    }
}
