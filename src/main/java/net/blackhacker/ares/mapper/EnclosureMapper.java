package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.model.Enclosure;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@Component
public class EnclosureMapper implements  ModelDTOMapper<Enclosure, EnclosureDTO> {
    @Override
    public EnclosureDTO toDTO(Enclosure enclosure) {
        if (enclosure == null) return null;
        EnclosureDTO enclosureDTO = new EnclosureDTO();
        enclosureDTO.setUrl(enclosure.getUrl().toString());
        enclosureDTO.setLength(enclosure.getLength());
        enclosureDTO.setType(enclosure.getType());
        return enclosureDTO;
    }

    @Override
    public Enclosure toModel(EnclosureDTO enclosureDTO) {
        if (enclosureDTO == null) return null;
        Enclosure enclosure = new Enclosure();

        try {
            enclosure.setUrl(new URI(enclosureDTO.getUrl()).toURL());
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            enclosure.setUrl(null);
            log.error("Invalid URL: {}", enclosureDTO.getUrl());
        }
        enclosure.setLength(enclosureDTO.getLength());
        enclosure.setType(enclosureDTO.getType());
        return enclosure;
    }
}
