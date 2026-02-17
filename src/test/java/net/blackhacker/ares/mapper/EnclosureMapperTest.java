package net.blackhacker.ares.mapper;

import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.model.Enclosure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class EnclosureMapperTest {

    private EnclosureMapper enclosureMapper;

    @BeforeEach
    void setUp() {
        enclosureMapper = new EnclosureMapper();
    }

    @Test
    void toDTO_shouldMapEnclosureToDTO() throws Exception {
        Enclosure enclosure = new Enclosure();
        enclosure.setUrl(new URI("http://example.com/file.mp3").toURL());
        enclosure.setLength(1024L);
        enclosure.setType("audio/mpeg");

        EnclosureDTO dto = enclosureMapper.toDTO(enclosure);

        assertNotNull(dto);
        assertEquals("http://example.com/file.mp3", dto.getUrl());
        assertEquals(1024L, dto.getLength());
        assertEquals("audio/mpeg", dto.getType());
    }

    @Test
    void toModel_shouldMapDTOToEnclosure() throws Exception {
        EnclosureDTO dto = new EnclosureDTO();
        dto.setUrl("http://example.com/file.mp3");
        dto.setLength(1024L);
        dto.setType("audio/mpeg");

        Enclosure enclosure = enclosureMapper.toModel(dto);

        assertNotNull(enclosure);
        assertEquals("http://example.com/file.mp3", enclosure.getUrl().toString());
        assertEquals(1024L, enclosure.getLength());
        assertEquals("audio/mpeg", enclosure.getType());
    }

    @Test
    void toModel_shouldReturnNull_whenUrlIsInvalid() {
        EnclosureDTO dto = new EnclosureDTO();
        dto.setUrl("invalid-url");

        Enclosure enclosure = enclosureMapper.toModel(dto);

        assertNull(enclosure);
    }
}
