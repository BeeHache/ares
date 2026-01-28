package net.blackhacker.ares.dto;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

public interface FeedTitleDTO extends Serializable {
    String getId();
    String getTitle();
    String getImageUrl();
}
