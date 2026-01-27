package net.blackhacker.ares.controller;

import net.blackhacker.ares.Constants;
import net.blackhacker.ares.dto.StringCacheDTO;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.StringCacheRepository;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.OpmlService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.jspecify.annotations.NonNull;
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
    private final TransactionTemplate transactionTemplate;
    private final JmsTemplate jmsTemplate;
    private final StringCacheRepository stringCacheRepository;

    public FeedController(FeedService feedService,  UserService userService,
                          OpmlService opmlService, URLValidator urlValidator,
                          MultipartFileValidator multipartFileValidator,
                          TransactionTemplate transactionTemplate,
                          JmsTemplate jmsTemplate,
                          StringCacheRepository stringCacheRepository) {
        this.feedService = feedService;
        this.userService = userService;
        this.opmlService = opmlService;
        this.urlValidator = urlValidator;
        this.multipartFileValidator = multipartFileValidator;
        this.transactionTemplate = transactionTemplate;
        this.jmsTemplate = jmsTemplate;
        this.stringCacheRepository = stringCacheRepository;
    }

    @GetMapping
    public ResponseEntity<Collection<String>> getFeed(@AuthenticationPrincipal Account principal) {

        User user = userService.getUserByAccount(principal).get();
        Collection<String> feeds = user.getFeeds().stream().map(Feed::getJsonData).toList();
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(feeds);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getFeed(@PathVariable("id") UUID id) {


        Optional<StringCacheDTO> oJson = stringCacheRepository.findById(id);
        if (oJson.isPresent()) {
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(oJson.get().getString());
        }

        Feed feed = feedService.getFeedById(id);
        if (feed==null) {
            return ResponseEntity.notFound().build();
        }

        StringCacheDTO stringCacheDTO = new StringCacheDTO();
        stringCacheDTO.setId(id);
        stringCacheDTO.setString(feed.getJsonData());
        stringCacheRepository.save(stringCacheDTO);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(feed.getJsonData());
    }

    @PostMapping("/import")
    ResponseEntity<Void> importOpmlFromFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Account account) {
        multipartFileValidator.validateMultipartFile(file);
        final User user = userService.getUserByAccount(account).get();
        Collection<Feed> feeds = feedService.saveFeeds(opmlService.importFile(file));
        importOpml(user,feeds);
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    ResponseEntity<String> addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull Account account){
        urlValidator.validateURL(link);

        User user = userService.getUserByAccount(account).get();
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        return ResponseEntity.ok(feed.getJsonData());
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

    private void importOpml (final User user, Collection<Feed> feeds) {
        feeds.forEach(feed -> {
            transactionTemplate.executeWithoutResult(status -> {
                user.getFeeds().add(feed);
                userService.saveUser(user);
            });
            sendUpdateFeedMessage(feed.getId());
        });
    }

    private void sendUpdateFeedMessage(UUID feedId){
        jmsTemplate.convertAndSend(Constants.UPDATE_FEED_QUEUE, feedId);
    }
}
