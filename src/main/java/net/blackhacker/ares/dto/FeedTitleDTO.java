package net.blackhacker.ares.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface FeedTitleDTO extends Serializable {
    String getId();
    String getTitle();
    String getImageUrl();
    Boolean getIsPodcast();
}
