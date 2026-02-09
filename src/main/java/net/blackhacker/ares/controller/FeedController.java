package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedTitleDTO;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final MultipartFileValidator multipartFileValidator;

    public FeedController(FeedService feedService,  UserService userService,
                          OpmlService opmlService, URLValidator urlValidator,
                          MultipartFileValidator multipartFileValidator,
                          TransactionTemplate transactionTemplate,
                          JmsTemplate jmsTemplate) {
        this.feedService = feedService;
        this.userService = userService;
        this.opmlService = opmlService;
        this.urlValidator = urlValidator;
        this.multipartFileValidator = multipartFileValidator;
    }

    @GetMapping("/titles")
    public Collection<FeedTitleDTO> getFeedTitles(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        return feedService.getFeedTitles(user.getId());
    }

    @GetMapping
    public ResponseEntity<Collection<FeedDTO>> getFeed(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Collection<FeedDTO> feeds = user.getFeeds().stream().map(Feed::getDto).toList();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(feeds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedDTO> getFeed(@PathVariable("id") UUID id) {
        Optional<FeedDTO> feedDTO = feedService.getFeedDTO(id);
        if (feedDTO.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(feedDTO.get());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") UUID id) {
        Optional<FeedImage> oFeedImage = feedService.getFeedImageById(id);
        if (oFeedImage.isEmpty()) {
            return ResponseEntity.notFound().build();
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
    ResponseEntity<FeedDTO> addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull Account account){
        urlValidator.validateURL(link);

        User user = userService.getUserByAccount(account).get();
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        return ResponseEntity.ok(feed.getDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable("id") UUID id, @AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Feed feed = feedService.getFeedById(id);

        if (feed == null){
            return ResponseEntity.notFound().build();
        }

        user.getFeeds().remove(feed);
        feedService.saveFeed(feed);
        userService.saveUser(user);
        return ResponseEntity.ok().build();
    }
}
