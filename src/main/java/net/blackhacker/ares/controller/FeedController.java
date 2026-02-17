package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.projection.FeedSummaryProjection;
import net.blackhacker.ares.projection.FeedTitleProjection;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.FeedImage;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.CacheService;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.OpmlService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;
    private final OpmlService opmlService;
    private final URLValidator urlValidator;
    private final FeedMapper feedMapper;
    private final MultipartFileValidator multipartFileValidator;
    private final TransactionTemplate transactionTemplate;

    public FeedController(FeedService feedService,  UserService userService,
                          OpmlService opmlService, URLValidator urlValidator,
                          MultipartFileValidator multipartFileValidator,
                          TransactionTemplate transactionTemplate,
                          JmsTemplate jmsTemplate,
                          FeedMapper feedMapper) {
        this.feedService = feedService;
        this.userService = userService;
        this.opmlService = opmlService;
        this.urlValidator = urlValidator;
        this.feedMapper = feedMapper;
        this.multipartFileValidator = multipartFileValidator;
        this.transactionTemplate = transactionTemplate;
    }

    @GetMapping("/titles")
    public ResponseEntity<Collection<FeedTitleProjection>> getFeedTitles(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Collection<FeedTitleProjection> titles = feedService.getFeedTitles(user.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(titles);
    }
    @GetMapping("/summaries")
    public ResponseEntity<Collection<FeedSummaryProjection>> getFeedSummary(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Collection<FeedSummaryProjection> summaries = feedService.getFeedSummaries(user.getId());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(summaries);
    }

    @GetMapping
    public ResponseEntity<Collection<FeedDTO>> getFeeds(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Collection<FeedDTO> feeds = user.getFeeds().stream().map(Feed::getDto).toList();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(feeds);
    }

    @GetMapping("/{id}")
    @Cacheable(value = CacheService.FEED_DTOS_CACHE, unless = "#result=null")
    public FeedDTO getFeed(@PathVariable("id") UUID id) {
        Optional<Feed> ofeed = feedService.getFeedById(id);
        if (ofeed.isEmpty()) {
            throw new ControllerException(HttpStatus.NOT_FOUND,  String.format("Feed with id %s not found", id));
        }
        ofeed.get().getFeedItems().clear();
        return feedMapper.toDTO(ofeed.get());
    }

    @GetMapping("/{id}/items/{pageNumber}")
    public Collection<FeedItemDTO> getFeedItems(@PathVariable("id") UUID feedId, @PathVariable("pageNumber")int pageNumber) {
        return feedService.getFeedItems(feedId, pageNumber);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") UUID id) {
        Optional<FeedImage> oFeedImage = feedService.getFeedImageById(id);
        if (oFeedImage.isEmpty()) {
            throw new ControllerException(HttpStatus.NOT_FOUND,  String.format("Feed with id %s not found", id));
        }

        FeedImage feedImage = oFeedImage.get();
        return ResponseEntity
            .ok()
            .contentType(feedImage.getContentType())
            .body(feedImage.getContent());
    }

    @PostMapping("/import")
    ResponseEntity<Void> importOpmlFromFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Account account) {
        multipartFileValidator.validateMultipartFile(file);
        final User user = userService.getUserByAccount(account).get();
        Collection<Feed> feeds = feedService.saveFeeds(opmlService.importFile(file));
        feeds.forEach(feed -> {
            userService.subscribeUserToFeed(user, feed);
        });
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    FeedDTO addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull Account account){
        urlValidator.validateURL(link);

        User user = userService.getUserByAccount(account).get();
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        return feedMapper.toDTO(feed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable("id") UUID id, @AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Optional<Feed> feed = feedService.getFeedById(id);

        if (feed.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Feed foundFeed = feed.get();

        user.getFeeds().remove(foundFeed);
        feedService.saveFeed(foundFeed);
        userService.saveUser(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public Collection<FeedItemProjection> search(@RequestParam("q") String query) {
        return feedService.searchItems(query);
    }
}
