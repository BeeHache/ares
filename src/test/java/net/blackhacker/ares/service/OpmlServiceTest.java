package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpmlServiceTest {

    @MockitoBean
    private URLFetchService urlFetchService;

    private OpmlService opmlService;

    @BeforeEach
    void setUp() {
        opmlService = new OpmlService(urlFetchService);
    }

    @Test
    void importFile_shouldThrowException_whenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "test.opml", "text/xml", new byte[0]);

        assertThrows(ServiceException.class, () -> opmlService.importFile(file));
    }

    @Test
    void importFile_shouldThrowException_whenFileIsNull() {
        assertThrows(ServiceException.class, () -> opmlService.importFile(null));
    }

    @Test
    void importFile_shouldParseOpmlAndReturnFeeds() {
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="1.0">
                    <head>
                        <title>Subscriptions</title>
                    </head>
                    <body>
                        <outline text="Tech" title="Tech" type="rss" xmlUrl="http://example.com/rss" htmlUrl="http://example.com" imageUrl="http://example.com/image.png"/>
                    </body>
                </opml>""";
        MockMultipartFile file = new MockMultipartFile("file", "test.opml", "text/xml", opmlContent.getBytes(StandardCharsets.UTF_8));

        when(urlFetchService.getContentType(anyString())).thenReturn("image/png");
        when(urlFetchService.fetchImageBytes(anyString())).thenReturn(new byte[]{1, 2, 3});

        Collection<Feed> result = opmlService.importFile(file);

        assertNotNull(result);
        assertEquals(1, result.size());
        Feed feed = result.iterator().next();
        assertEquals("Tech", feed.getTitle());
        assertEquals("http://example.com/rss", feed.getLink());
        assertNotNull(feed.getImage());
        assertEquals("image/png", feed.getImage().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, feed.getImage().getData());
    }

    @Test
    void importFeed_shouldParseOpmlFromUrl() {
        String opmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="1.0">
                    <head>
                        <title>Subscriptions</title>
                    </head>
                    <body>
                        <outline text="Tech" title="Tech" type="rss" xmlUrl="http://example.com/rss" htmlUrl="http://example.com" imageUrl="http://example.com/image.png"/>
                    </body>
                </opml>""";

        // Mock the fetch for the OPML file itself
        when(urlFetchService.fetchImageBytes("http://example.com/opml")).thenReturn(opmlContent.getBytes(StandardCharsets.UTF_8));
        
        // Mock the fetch for the image inside the OPML
        when(urlFetchService.getContentType("http://example.com/rss")).thenReturn("image/png");
        when(urlFetchService.fetchImageBytes("http://example.com/image.png")).thenReturn(new byte[]{1, 2, 3});

        Collection<Feed> result = opmlService.importFeed("http://example.com/opml");

        assertNotNull(result);
        assertEquals(1, result.size());
        Feed feed = result.iterator().next();
        assertEquals("Tech", feed.getTitle());
        assertEquals("http://example.com/rss", feed.getLink());
        assertNotNull(feed.getImage());
        assertEquals("image/png", feed.getImage().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, feed.getImage().getData());
    }

    @Test
    void importFile_shouldThrowServiceException_whenParsingFails() {
        MockMultipartFile file = new MockMultipartFile("file", "test.opml", "text/xml", "invalid xml".getBytes(StandardCharsets.UTF_8));

        assertThrows(ServiceException.class, () -> opmlService.importFile(file));
    }
}
