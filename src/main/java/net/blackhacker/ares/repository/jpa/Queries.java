package net.blackhacker.ares.repository.jpa;

public class Queries {
    final static public String FIND_MODIFIED_BEFORE = "SELECT f FROM Feed f WHERE f.lastModified < :dt OR f.lastModified IS NULL";
    final static public String  FIND_FEED_TITLES_BY_USERID =
            "SELECT f.id, f.dto ->> 'title' AS title, " +
                   "f.dto ->> 'imageUrl' AS imageUrl, " +
                   "f.dto ->> 'isPodcast' as isPodcast, " +
                   "f.dto -> 'items' -> 0 ->> 'date' as pubdate " +
            "FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id " +
            "WHERE s.user_id = :userid";

    final static public String  FIND_FEED_SUMMARIES_BY_USERID =
            "SELECT f.id, f.dto ->> 'title' AS title, " +
                    "f.dto ->> 'imageUrl' AS imageUrl, " +
                    "f.dto ->> 'isPodcast' as isPodcast, " +
                    "f.dto ->> 'description' as description, " +
                    "f.dto ->> 'link' as link, " +
                    "f.dto -> 'items' -> 0 ->> 'date' as pubdate " +
                    "FROM subscriptions s INNER JOIN feeds f ON s.feed_id = f.id " +
                    "WHERE s.user_id = :userid";

    final static public String GET_FEED_DTO_BY_ID = "SELECT f.dto FROM Feed f WHERE f.id=:feed_id";

    final static public String GET_FEED_IMAGE_BY_ID = "SELECT f.feedImage from Feed f where f.id=:feed_id";

}
