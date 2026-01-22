package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Image;
import net.blackhacker.ares.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = new ImageService(imageRepository);
    }

    @Test
    void getImage_shouldReturnImage_whenImageExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Image image = new Image();
        image.setId(id);
        when(imageRepository.findById(id)).thenReturn(Optional.of(image));

        // Act
        Optional<Image> result = imageService.getImage(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void getImage_shouldReturnNull_whenImageDoesNotExist() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(imageRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Image> result = imageService.getImage(id);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveImage_shouldReturnSavedImage() {
        // Arrange
        Image image = new Image();
        when(imageRepository.save(image)).thenReturn(image);

        // Act
        Image result = imageService.saveImage(image);

        // Assert
        assertNotNull(result);
        assertEquals(image, result);
    }

    @Test
    void deleteImage_shouldCallDeleteById() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        imageService.deleteImage(id);

        // Assert
        verify(imageRepository, times(1)).deleteById(id);
    }

    @Test
    void updateImage_shouldUpdateExistingImage_whenImageExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Image existingImage = new Image();
        existingImage.setId(id);
        existingImage.setContentType("image/png");
        existingImage.setData(new byte[]{1, 2, 3});

        Image newImage = new Image();
        newImage.setContentType("image/jpeg");
        newImage.setData(new byte[]{4, 5, 6});

        when(imageRepository.findById(id)).thenReturn(Optional.of(existingImage));
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        UUID id = UUID.randomUUID();
        Image newImage = new Image();
        newImage.setContentType("image/jpeg");
        newImage.setData(new byte[]{4, 5, 6});

        when(imageRepository.findById(id)).thenReturn(Optional.empty());
        when(imageRepository.save(newImage)).thenReturn(newImage);

        // Act
        Image result = imageService.updateImage(id, newImage);

        // Assert
        assertNotNull(result);
        assertEquals(newImage, result);
    }
}
