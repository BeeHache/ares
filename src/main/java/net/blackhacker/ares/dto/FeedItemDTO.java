package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FeedItemDTO implements Serializable, Comparable<FeedItemDTO> {
    private String title;
    private String description;
    private String link;
    private String date;
    private Collection<EnclosureDTO> enclosures = new ArrayList<>();

    @Override
    public int compareTo(@NonNull FeedItemDTO o) {
        if (this.date == null) {
            return o.date == null ? 0 : -1;
        }
        if (o.date == null) {
            return 1;
        }
        return this.date.compareToIgnoreCase(o.date);
    }
}
