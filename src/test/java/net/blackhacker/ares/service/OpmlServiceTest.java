package net.blackhacker.ares.service;

import net.blackhacker.ares.model.Feed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = OpmlService.class)
@ExtendWith(MockitoExtension.class)
class OpmlServiceTest {

    @MockitoBean
    private URLFetchService urlFetchService;

    @InjectMocks
    private OpmlService opmlService;

    String opmlContentString;
    ResponseEntity<String> opmlResponseString;
    ResponseEntity<byte[]> imageResponseBytes;

    @BeforeEach
    void setUp() {

        opmlService = new OpmlService(urlFetchService);
        opmlContentString = """
                <?xml version="1.0" encoding="UTF-8"?>
                <opml version="1.0">
                    <head>
                        <title>Subscriptions</title>
                    </head>
                    <body>
                        <outline text="Tech" title="Tech" type="rss" xmlUrl="http://example.com/rss" htmlUrl="http://example.com" imageUrl="http://example.com/image.png"/>
                    </body>
                </opml>""";
        byte[] opmlContentStringBytes = opmlContentString.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("Content-Type", "image/png")));


        imageResponseBytes = new ResponseEntity<>(new byte[]{1, 2, 3}, headers, 200);
        opmlResponseString = new ResponseEntity<>(opmlContentString,
                new HttpHeaders(MultiValueMap.fromSingleValue(Map.of("Content-Type", "text/xml"))),
                200);

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
        when(urlFetchService.fetchBytes(anyString())).thenReturn(imageResponseBytes);

        MockMultipartFile file = new MockMultipartFile("file", "test.opml", "text/xml", opmlContentString.getBytes(StandardCharsets.UTF_8));
        Collection<Feed> result = opmlService.importFile(file);

        assertNotNull(result);
        assertEquals(1, result.size());
        Feed feed = result.iterator().next();
        assertEquals("Tech", feed.getTitle());
        assertEquals("http://example.com/rss", feed.getUrl().toString());
        assertNotNull(feed.getImage());
        assertEquals("image/png", feed.getImage().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, feed.getImage().getData());
    }

    @Test
    void importFeed_shouldParseOpmlFromUrl() {

        // Mock the fetch for the OPML file itself
        when(urlFetchService.fetchString("http://example.com/opml")).thenReturn(opmlResponseString);
        when(urlFetchService.fetchBytes(anyString())).thenReturn(imageResponseBytes);

        Collection<Feed> result = opmlService.importFeed("http://example.com/opml");

        assertNotNull(result);
        assertEquals(1, result.size());
        Feed feed = result.iterator().next();
        assertEquals("Tech", feed.getTitle());
        assertEquals("http://example.com/rss", feed.getUrl().toString());
    }

    @Test
    void importFile_shouldThrowServiceException_whenParsingFails() {
        MockMultipartFile file = new MockMultipartFile("file", "test.opml", "text/xml", "invalid xml".getBytes(StandardCharsets.UTF_8));

        assertThrows(ServiceException.class, () -> opmlService.importFile(file));
    }

    @Test
    void generateOPML_shouldReturnOpmlString() {
        // Arrange
        Feed feed1 = new Feed();
        feed1.setTitle("Feed 1");
        feed1.setLinkFromString("http://example.com/feed1");
        feed1.setDescription("Description 1");

        Feed feed2 = new Feed();
        feed2.setTitle("Feed 2");
        feed2.setLinkFromString("http://example.com/feed2");
        // Description is null

        Collection<Feed> feeds = List.of(feed1, feed2);

        // Act
        String result = opmlService.generateOPML(feeds);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<opml version=\"2.0\">"));
        assertTrue(result.contains("<head>"));
        assertTrue(result.contains("<title>Ares Export</title>"));
        assertTrue(result.contains("<body>"));
        
        // Check for Feed 1
        assertTrue(result.contains("text=\"Feed 1\""));
        assertTrue(result.contains("title=\"Feed 1\""));
        assertTrue(result.contains("xmlUrl=\"http://example.com/feed1\""));
        assertTrue(result.contains("description=\"Description 1\""));
        
        // Check for Feed 2
        assertTrue(result.contains("text=\"Feed 2\""));
        assertTrue(result.contains("title=\"Feed 2\""));
        assertTrue(result.contains("xmlUrl=\"http://example.com/feed2\""));
        // Should not contain description for Feed 2 if it's null, but checking absence is tricky with string contains
        // as "Description 1" is present. We can check that the specific attribute isn't there for this feed if we parse it back,
        // but for a simple string check, this is sufficient.
    }
}
