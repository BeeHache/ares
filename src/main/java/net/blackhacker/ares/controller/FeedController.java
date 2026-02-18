package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.FeedItemDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.projection.FeedItemProjection;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.*;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final FeedPageService feedPageService;
    private final URLValidator urlValidator;
    private final FeedMapper feedMapper;
    private final MultipartFileValidator multipartFileValidator;

    public FeedController(FeedService feedService,  UserService userService, FeedPageService feedPageService,
                          OpmlService opmlService, URLValidator urlValidator,
                          MultipartFileValidator multipartFileValidator,
                          FeedMapper feedMapper) {
        this.feedService = feedService;
        this.userService = userService;
        this.opmlService = opmlService;
        this.feedPageService = feedPageService;
        this.urlValidator = urlValidator;
        this.feedMapper = feedMapper;
        this.multipartFileValidator = multipartFileValidator;
    }

    @GetMapping
    public Collection<FeedDTO> getFeeds(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        return user.getFeeds().stream().map(feedMapper::toDTO).toList();
    }

    @GetMapping("/{id}")
    @Cacheable(value = CacheService.FEED_DTOS_CACHE, unless = "#result=null")
    public FeedDTO getFeed(@PathVariable("id") UUID id) {
        Optional<Feed> ofeed = feedService.getFeedById(id);
        if (ofeed.isEmpty()) {
            throw new ControllerException(HttpStatus.NOT_FOUND,  String.format("Feed with id %s not found", id));
        }
        return feedMapper.toDTO(ofeed.get());
    }

    @GetMapping("/{id}/items/{pageNumber}")
    @Cacheable(value = CacheService.FEED_PAGE_CACHE, unless = "#result=null")
    public Collection<FeedItemDTO> getFeedItems(@PathVariable("id") UUID feedId, @PathVariable("pageNumber")int pageNumber) {
        feedPageService.storePageNumber(feedId, pageNumber);
        return feedService.getFeedItems(feedId, pageNumber);
    }

    @PostMapping("/import")
    void importOpmlFromFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Account account) {
        multipartFileValidator.validateMultipartFile(file);
        final User user = userService.getUserByAccount(account).get();
        Collection<Feed> feeds = feedService.saveFeeds(opmlService.importFile(file));
        feeds.forEach(feed -> {
            userService.subscribeUserToFeed(user, feed);
        });
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
    public void deleteFeed(@PathVariable("id") UUID id, @AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Optional<Feed> feed = feedService.getFeedById(id);

        if (feed.isEmpty()){
            throw new ControllerException(HttpStatus.NOT_FOUND,  String.format("Feed with id %s not found", id));
        }

        Feed foundFeed = feed.get();
        user.getFeeds().remove(foundFeed);
        userService.saveUser(user);
    }

    @GetMapping("/search")
    public Collection<FeedItemProjection> search(@RequestParam("q") String query) {
        return feedService.searchItems(query);
    }
}
