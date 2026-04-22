package net.blackhacker.ares.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.model.Enclosure;
import net.blackhacker.ares.model.Feed;

import net.blackhacker.ares.model.FeedItem;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.repository.jpa.FeedRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
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

    final private FeedItemRepository feedItemRepository;
    final private FeedRepository feedRepository;
    final private TransactionTemplate transactionTemplate;

    @Getter
    @Setter
    private Feed feed;

    record Item(String tag, Object value) { }

    final private Stack<Item> stack = new Stack<>();
    final private StringBuilder chars = new StringBuilder();
    final private Set<String> seenTitles = new HashSet<>();
    final private Set<String> seenGuids = new HashSet<>();
    
    public FeedParser(FeedRepository feedRepository, FeedItemRepository feedItemRepository, TransactionTemplate transactionTemplate) {
        this.feedRepository = feedRepository;
        this.feedItemRepository = feedItemRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public void parse(Feed feed, @NonNull InputStream is){
        this.feed = feed;
        this.seenTitles.clear();
        this.seenGuids.clear();
        this.stack.clear();
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
                stack.push(new Item(CHANNEL, new Feed()));
                break;

            case ITEM:
            case ENTRY: {
                FeedItem feedItem = new FeedItem();
                stack.push(new Item(ITEM, feedItem));
                break;
            }

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
                        stack.push(new Item(ENCLOSURE, enclosure));
                    } catch (Exception e) {
                        log.error("Failed to parse enclosure: {}", e.getMessage());
                    }
                }
                break;
            }

            case ITUNES_IMAGE:
                URL itunesImg = resolveUrl(attributes.getValue("href"));
                if (itunesImg != null) {
                    stack.push(new Item(IMAGE, itunesImg));
                }
                break;

            case LINK:
                String href = attributes.getValue("href");
                if (href != null && !href.isBlank()) {
                    URL link = resolveUrl(href);
                    if (link != null) {
                        stack.push(new Item(LINK, link));
                    }
                }
                break;

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case TITLE:
                stack.push(new Item(TITLE, getChars()));
                break;

            case DESCRIPTION:
            case CONTENT:
            case SUMMARY:
                stack.push(new Item(DESCRIPTION, getChars()));
                break;

            case CONTENT_ENCODED:
                stack.push(new Item(CONTENT_ENCODED, getChars()));
                break;

            case PUBDATE:
            case PUBLISHED:
            case UPDATED:
                stack.push(new Item(PUBDATE, DateTimeReformatter.parse(getChars())));
                break;

            case LINK:
                if (!getChars().isEmpty()) {
                    URL bodyLink = resolveUrl(getChars());
                    if (bodyLink != null) {
                        stack.push(new Item(LINK, bodyLink));
                    }
                }
                break;

            case URL: {
                URL url = resolveUrl(getChars());
                if (url != null) {
                    stack.push(new Item(URL, url));
                }
                break;
            }

            case IMAGE:
                if (!stack.isEmpty() && stack.peek().tag().equals(URL)) {
                    stack.push(new Item(IMAGE, stack.pop().value()));
                }
                break;

            case GUID:
                stack.push(new Item(GUID, getChars()));
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
                    Item item = stack.pop();

                    switch (item.tag()) {
                        case TITLE: title = item.value().toString(); break;
                        case DESCRIPTION: description = item.value().toString(); break;
                        case CONTENT_ENCODED: contentEncoded = item.value().toString(); break;
                        case GUID: guid = item.value().toString(); break;
                        case LINK: link = (URL) item.value(); break;
                        case ENCLOSURE: enclosures.add((Enclosure) item.value()); break;
                        case PUBDATE: pubdate = (ZonedDateTime) item.value(); break;
                        case ITEM: {
                            FeedItem feedItem = (FeedItem) item.value();
                            if (title == null || title.isBlank()) {
                                log.warn("Skipping item with no title in feed {}", feed.getUrl());
                            } else {
                                // 1. Check In-Memory Duplicates (same session)
                                if (seenTitles.contains(title) || (guid != null && seenGuids.contains(guid))) {
                                    continue;
                                }

                                feedItem.setTitle(title);
                                feedItem.setGuid(guid);
                                feedItem.setLink(link);
                                feedItem.setDate(pubdate);
                                feedItem.setDescription(contentEncoded != null ? contentEncoded : description);
                                feedItem.getEnclosures().addAll(enclosures);

                                feedItemRepository.findByFeedAndTitle(feed.getId(), title).ifPresent(value -> feedItem.setId(value.getId()));
                                feedItem.setFeed(feed);
                                feed.getFeedItems().add(feedItem);
                                
                                // Mark as seen
                                seenTitles.add(title);
                                if (guid != null) seenGuids.add(guid);
                            }
                            break;
                        }
                    }
                    if (item.tag().equals(ITEM)) break;
                }
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
                    Item item = stack.pop();
                    if (item.tag().equals(CHANNEL)) {
                        break;
                    }
                    switch (item.tag()) {
                        case TITLE: title = item.value().toString(); break;
                        case LINK: link = (URL) item.value(); break;
                        case DESCRIPTION: description = item.value().toString(); break;
                        case PUBDATE: pubdate = (ZonedDateTime) item.value(); break;
                        case IMAGE: imageUrl = (URL) item.value(); break;
                    }
                }
                
                if (title != null) feed.setTitle(title);
                if (link != null) feed.setLink(link);
                if (description != null) feed.setDescription(description);
                if (pubdate != null) feed.setPubdate(pubdate);
                if (imageUrl != null) feed.setImageUrl(imageUrl);

                transactionTemplate.executeWithoutResult(status -> {
                    feedRepository.saveAndFlush(feed);
                });
                break;
            }

        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        chars.append(ch, start, length);
    }
}
