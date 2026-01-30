package net.blackhacker.ares.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Data
public class FeedItemDTO implements Serializable {
    private String title;
    private String description;
    private String link;
    private ZonedDateTime date;
    private Collection<EnclosureDTO> enclosures = new ArrayList<>();
}
