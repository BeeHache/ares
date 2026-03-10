package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.jpa.FeedItemRepository;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final FeedService feedService;
    private final FeedMapper feedMapper;

    public AdminController(UserService userService,
                           FeedService feedService,
                           FeedItemRepository feedItemRepository,
                           AccountService accountService, FeedMapper feedMapper) {
        this.userService = userService;
        this.feedService = feedService;
        this.feedMapper = feedMapper;
    }

    @GetMapping("/hello")
    public Map<String, String> helloAdmin() {
        return Map.of("message", "Hello, Admin!");
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userService.getUserRepository().count());
        stats.put("totalFeeds", feedService.getFeedRepository().count());
        stats.put("totalArticles", feedService.getFeedItemRepository().count());
        return stats;
    }

    @GetMapping("/users")
    public Page<User> getUsers(@PageableDefault(size = 20) Pageable pageable) {
        return userService.getUserRepository().findAll(pageable);
    }

    @PostMapping("/users/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable Long id) {
        return userService.getUserRepository().findById(id)
                .map(user -> {
                    user.getAccount().lockAccount();
                    userService.saveUser(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        return userService.getUserRepository().findById(id)
                .map(user -> {
                    user.getAccount().enableAccount(); // Re-enable / unlock
                    userService.saveUser(user);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/feeds")
    public Page<FeedDTO> getFeeds(@PageableDefault(size = 20) Pageable pageable) {
        return feedService.findAllFeeds(pageable).map(feed -> {
            //set the subscription count for each feed
            feed.setSubscriptionCount(
                feedService.getFeedRepository().findSubscriptionCountByFeedId(feed.getId())
            );
            if (feed.getSubscriptionCount() == null) {
                feed.setSubscriptionCount(0L);
            }
            return feed;
        }).map(feedMapper::toDTO);
    }

    @DeleteMapping("/feeds/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable UUID id) {
        if (feedService.getFeedRepository().existsById(id)) {
            feedService.getFeedRepository().deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/feeds/{id}/refresh")
    public ResponseEntity<Void> refreshFeed(@PathVariable UUID id) {
        if (feedService.getFeedRepository().existsById(id)) {
            feedService.updateFeed(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
