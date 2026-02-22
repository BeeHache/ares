package net.blackhacker.ares.mapper;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.dto.EnclosureDTO;
import net.blackhacker.ares.model.Enclosure;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class EnclosureMapper implements ModelDTOMapper<Enclosure, EnclosureDTO> {
    @Override
    public EnclosureDTO toDTO(Enclosure enclosure) {
        EnclosureDTO enclosureDTO = new EnclosureDTO();
        enclosureDTO.setUrl(enclosure.getUrl().toString());
        enclosureDTO.setLength(enclosure.getLength());
        enclosureDTO.setType(enclosure.getType());
        return enclosureDTO;
    }

    @Override
    public Enclosure toModel(EnclosureDTO enclosureDTO) {
        Enclosure enclosure = new Enclosure();
        try {
            enclosure.setUrl(new URI(enclosureDTO.getUrl()).toURL());
            enclosure.setLength(enclosureDTO.getLength());
            enclosure.setType(enclosureDTO.getType());
            return enclosure;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }


}
