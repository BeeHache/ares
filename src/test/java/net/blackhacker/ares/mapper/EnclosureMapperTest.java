package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.model.Enclosure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class EnclosureMapperTest {

    private EnclosureMapper enclosureMapper;

    @BeforeEach
    void setUp() {
        enclosureMapper = new EnclosureMapper();
    }

    @Test
    void toDTO_shouldMapEnclosureToDTO() throws MalformedURLException {
        // Arrange
        Enclosure enclosure = new Enclosure();
        String urlString = "http://example.com/file.mp3";
        enclosure.setUrl(URI.create(urlString).toURL());
        enclosure.setLength(1024L);
        enclosure.setType("audio/mpeg");

        // Act
        EnclosureDTO dto = enclosureMapper.toDTO(enclosure);

        // Assert
        assertNotNull(dto);
        assertEquals(urlString, dto.getUrl());
        assertEquals(1024L, dto.getLength());
        assertEquals("audio/mpeg", dto.getType());
    }

    @Test
    void toDTO_shouldReturnNullWhenInputIsNull() {
        // Act
        EnclosureDTO dto = enclosureMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void toModel_shouldMapDTOToEnclosure() throws MalformedURLException {
        // Arrange
        EnclosureDTO dto = new EnclosureDTO();
        String urlString = "http://example.com/file.mp3";
        dto.setUrl(urlString);
        dto.setLength(2048L);
        dto.setType("video/mp4");

        // Act
        Enclosure enclosure = enclosureMapper.toModel(dto);

        // Assert
        assertNotNull(enclosure);
        assertEquals(new URL(urlString), enclosure.getUrl());
        assertEquals(2048L, enclosure.getLength());
        assertEquals("video/mp4", enclosure.getType());
    }

    @Test
    void toModel_shouldReturnNullWhenInputIsNull() {
        // Act
        Enclosure enclosure = enclosureMapper.toModel(null);

        // Assert
        assertNull(enclosure);
    }

    @Test
    void toModel_shouldHandleInvalidUrl() {
        // Arrange
        EnclosureDTO dto = new EnclosureDTO();
        dto.setUrl("invalid-url");
        dto.setLength(100L);
        dto.setType("text/plain");

        // Act
        Enclosure enclosure = enclosureMapper.toModel(dto);

        // Assert
        assertNotNull(enclosure);
        assertNull(enclosure.getUrl()); // URL should be null due to exception
        assertEquals(100L, enclosure.getLength());
        assertEquals("text/plain", enclosure.getType());
    }
}
