package net.blackhacker.ares.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;

import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FeedParser extends DefaultHandler {

    final static private String RSS = "rss";
    final static private String CHANNEL = "channel";
    final static private String ITEM = "item";
    final static private String TITLE = "title";
    final static private String DESCRIPTION = "description";
    final static private String LINK = "link";
    final static private String PUBDATE = "pubDate";
    final static private String IMAGE = "image";
    final static private String URL = "url";
    final static private String GUID = "guid";
    final static private String ENCLOSURE = "enclosure";

    final static private String FEED = "feed";
    final static private String ENTRY = "entry";
    final static private String PUBLISHED = "published";
    final static private String UPDATED = "updated";
    final static private String SUMMARY = "summary";
    final static private String CONTENT = "content";
    final static private String AUTHOR = "author";
    final static private String CATEGORY = "category";
    final static private String CONTENT_ENCODED = "content:encoded";

    final static private String  ITUNES_IMAGE = "itunes:image";

    @Getter
    @Setter
    private Feed feed;

    record StackItem(String tag, Object value) { }

    final private Stack<StackItem> stack = new Stack<>();
    final private StringBuilder chars = new StringBuilder();
    
    // Map to keep track of items for synchronization;
    private final Map<String, FeedItem> existingItemsByTitle = new HashMap<>();
    private final Map<String, FeedItem> existingItemsByGuid = new HashMap<>();

    final FeedItemRepository feedItemRepository;

    public FeedParser(FeedItemRepository feedItemRepository) {
        this.feedItemRepository = feedItemRepository;
    }

    public void parse(Feed feed, @NonNull InputStream is){
        this.feed = feed;
        this.stack.clear();
        this.chars.setLength(0);
        
        // Index existing items for fast lookup
        this.existingItemsByTitle.clear();
        this.existingItemsByGuid.clear();
        if (feed.getFeedItems() != null) {
            for (FeedItem item : feed.getFeedItems()) {
                if (item.getGuid() != null && !item.getGuid().isBlank()) {
                    existingItemsByGuid.put(item.getGuid().trim(), item);
                }
                if (item.getTitle() != null && !item.getTitle().isBlank()) {
                    existingItemsByTitle.put(item.getTitle().trim(), item);
                }
            }
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.newSAXParser().parse(is, this);
        } catch (Exception e) {
            log.error("Parsing error for feed {}: {}", feed.getUrl(), e.getMessage());
        }
    }


    private String getChars() {
        return chars.toString().trim();
    }

    private URL resolveUrl(String relative) {
        if (relative == null || relative.isBlank()) return null;
        String sanitized = relative.trim().replace(" ", "%20");
        try {
            URI uri = new URI(sanitized);
            if (uri.isAbsolute()) {
                return uri.toURL();
            }
            if (feed != null && feed.getUrl() != null) {
                return feed.getUrl().toURI().resolve(uri).toURL();
            }
        } catch (Exception e) {
            log.warn("Could not resolve URL: {} (Feed: {})", sanitized, (feed != null && feed.getUrl() != null) ? feed.getUrl() : "null");
        }
        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        chars.setLength(0);

        switch (qName) {
            case CHANNEL:
            case FEED:
                stack.push(new StackItem(CHANNEL, null));
                break;

            case ITEM:
            case ENTRY:
                stack.push(new StackItem(ITEM, null));
                break;

            case ENCLOSURE: {
                URL enclosureUrl = resolveUrl(attributes.getValue("url"));
                if (enclosureUrl != null) {
                    try {
                        Enclosure enclosure = new Enclosure();
                        enclosure.setUrl(enclosureUrl);
                        String length = attributes.getValue("length");
                        if (length != null) {
                            try {
                                enclosure.setLength(Long.parseLong(length.trim()));
                            } catch (NumberFormatException nfe) {}
                        }
                        enclosure.setType(attributes.getValue("type"));
                        stack.push(new StackItem(ENCLOSURE, enclosure));
                    } catch (Exception e) {
                        log.error("Failed to parse enclosure: {}", e.getMessage());
                    }
                }
                break;
            }

            case ITUNES_IMAGE:
                URL itunesImg = resolveUrl(attributes.getValue("href"));
                if (itunesImg != null) {
                    stack.push(new StackItem(IMAGE, itunesImg));
                }
                break;

            case LINK:
                String href = attributes.getValue("href");
                if (href != null && !href.isBlank()) {
                    URL link = resolveUrl(href);
                    if (link != null) {
                        stack.push(new StackItem(LINK, link));
                    }
                }
                break;

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case TITLE:
                stack.push(new StackItem(TITLE, getChars()));
                break;

            case DESCRIPTION:
            case CONTENT:
            case SUMMARY:
                stack.push(new StackItem(DESCRIPTION, getChars()));
                break;

            case CONTENT_ENCODED:
                stack.push(new StackItem(CONTENT_ENCODED, getChars()));
                break;

            case PUBDATE:
            case PUBLISHED:
            case UPDATED:
                stack.push(new StackItem(PUBDATE, DateTimeReformatter.parse(getChars())));
                break;

            case LINK:
                if (!getChars().isEmpty()) {
                    URL bodyLink = resolveUrl(getChars());
                    if (bodyLink != null) {
                        stack.push(new StackItem(LINK, bodyLink));
                    }
                }
                break;

            case URL: {
                URL url = resolveUrl(getChars());
                if (url != null) {
                    stack.push(new StackItem(URL, url));
                }
                break;
            }

            case IMAGE:
                if (!stack.isEmpty() && stack.peek().tag().equals(URL)) {
                    stack.push(new StackItem(IMAGE, stack.pop().value()));
                }
                break;

            case GUID:
                stack.push(new StackItem(GUID, getChars()));
                break;

            case ITEM:
            case ENTRY: {
                String title = null;
                String description = null;
                String contentEncoded = null;
                String guid = null;
                List<Enclosure> enclosures = new ArrayList<>();
                URL link = null;
                ZonedDateTime pubdate = null;

                while (!stack.isEmpty()) {
                    StackItem item = stack.pop();
                    if (item.tag().equals(ITEM)) break;

                    switch (item.tag()) {
                        case TITLE:
                            title = item.value().toString().trim();
                            break;
                        case DESCRIPTION:
                            description = item.value().toString().trim();
                            break;
                        case CONTENT_ENCODED:
                            contentEncoded = item.value().toString().trim();
                            break;
                        case GUID:
                            guid = item.value().toString().trim();
                            break;
                        case LINK:
                            link = (URL) item.value();
                            break;
                        case ENCLOSURE:
                            enclosures.add((Enclosure) item.value());
                            break;
                        case PUBDATE:
                            pubdate = (ZonedDateTime) item.value();
                            break;
                    }
                }

                if (title == null || title.isBlank()) {
                    break;
                }

                // SYNC LOGIC: Lookup existing item
                FeedItem feedItem = null;
                if (guid != null && !guid.isBlank()) feedItem = existingItemsByGuid.get(guid);
                if (feedItem == null) feedItem = existingItemsByTitle.get(title);

                if (feedItem != null) {
                    Optional<FeedItem> existing = feedItemRepository.findByFeedAndTitle(feed.getId(), title);
                    if (existing.isPresent()) {
                        feedItem = existing.get();
                    }
                }

                if (feedItem == null) {
                    feedItem = new FeedItem();

                    feedItem.setFeed(feed);
                    feed.getFeedItems().add(feedItem);

                    feedItem.setTitle(title);
                    feedItem.setGuid(guid);
                    feedItem.setLink(link);
                    feedItem.setDate(pubdate);
                    feedItem.setDescription(contentEncoded != null ? contentEncoded : description);

                    for (Enclosure enc : enclosures) {
                        if (!feedItem.getEnclosures().contains(enc)) {
                            feedItem.getEnclosures().add(enc);
                        }
                    }
                }
                // Update maps so subsequent occurrences are merged
                existingItemsByTitle.put(title, feedItem);
                if (guid != null && !guid.isBlank()) existingItemsByGuid.put(guid, feedItem);
                break;
            }

            case CHANNEL:
            case FEED: {
                String title = null;
                URL link = null;
                String description = null;
                ZonedDateTime pubdate = null;
                URL imageUrl = null;

                while (!stack.isEmpty()) {
                    StackItem item = stack.pop();
                    if (item.tag().equals(CHANNEL)) break;
                    
                    switch (item.tag()) {
                        case TITLE: title = item.value().toString().trim(); break;
                        case LINK: link = (URL) item.value(); break;
                        case DESCRIPTION: description = item.value().toString().trim(); break;
                        case PUBDATE: pubdate = (ZonedDateTime) item.value(); break;
                        case IMAGE: imageUrl = (URL) item.value(); break;
                    }
                }
                
                if (title != null) feed.setTitle(title);
                if (link != null) feed.setLink(link);
                if (description != null) feed.setDescription(description);
                if (pubdate != null) feed.setPubdate(pubdate);
                if (imageUrl != null) feed.setImageUrl(imageUrl);
                break;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars.append(ch, start, length);
    }
}
