package net.blackhacker.ares.controller;

import be.ceau.opml.OpmlWriteException;
import be.ceau.opml.OpmlWriter;
import be.ceau.opml.entity.Body;
import be.ceau.opml.entity.Head;
import be.ceau.opml.entity.Opml;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.OpmlService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController()
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final FeedService feedService;
    private final OpmlService opmlService;
    private final UserMapper userMapper;
    private final FeedMapper feedMapper;
    private final MultipartFileValidator multipartFileValidator;
    private final URLValidator urlValidator;

    public UserController(UserService userService,
                          FeedService feedService, OpmlService opmlService,
                          UserMapper userMapper, FeedMapper feedMapper,
                          MultipartFileValidator multipartFileValidator,
                          URLValidator urlValidator) {
        this.userService = userService;
        this.feedService = feedService;
        this.opmlService = opmlService;
        this.userMapper = userMapper;
        this.feedMapper = feedMapper;
        this.multipartFileValidator = multipartFileValidator;
        this.urlValidator = urlValidator;
    }

    @GetMapping("/")
    UserDTO getUser(@AuthenticationPrincipal Account account) {
        return userService.getUserByAccount(account).map(userMapper::toDTO).orElse(null);
    }

    @DeleteMapping("/")
    public void cancelAccount(@AuthenticationPrincipal Account account) {
        //add user to canceled_users table.
        userService.getUserByAccount(account).ifPresent(userService::cancelUser);
    }

    @PostMapping("/import")
    @Async
    void importOpmlFromFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Account account) {
        multipartFileValidator.validateMultipartFile(file);
        final User user = userService.getUserByAccount(account).get();
        Collection<Feed> feeds = feedService.saveFeeds(opmlService.importFile(file));
        subscribeUserToFeeds(user,feeds);
    }

    @PutMapping("/import")
    @Async
    void importOpmlFromUrl(@RequestParam("url") String url, @AuthenticationPrincipal Account account) {
        urlValidator.validateURL(url);
        final User user = userService.getUserByAccount(account).get();
        Collection<Feed> feeds = opmlService.importFeed(url);
        subscribeUserToFeeds(user,feeds);
    }

    private void subscribeUserToFeeds(final User user, Collection<Feed> feeds) {
        feeds.forEach(feed -> {
            userService.subscribeUserToFeed(user, feed);
        });
    }


    @GetMapping(value="/export")
    public ResponseEntity<Resource> exportFeeds(@AuthenticationPrincipal Account account)  {
        try {
            final User user = userService.getUserByAccount(account).get();

            Head head = new Head("Ares Feeds", null, null, null,
                    null, null, null, null, null, null,
                    null, null, null
            );

            Body body = new Body(user.getFeeds().stream().map(Feed::toOutline).toList());
            String xml = new OpmlWriter().write(new Opml("2.0", head, body));
            ByteArrayResource resource = new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .contentLength(xml.getBytes(StandardCharsets.UTF_8).length)
                    .body(resource);
        } catch (OpmlWriteException e) {
            throw new RuntimeException(e);
        }
    }


    @PutMapping("/addfeed")
    FeedDTO addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull Account account){
        urlValidator.validateURL(link);
        User user = userService.getUserByAccount(account).get();
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        return feedMapper.toDTO(feed);
    }

    @GetMapping("/feeds")
    public Collection<FeedDTO> getFeed(@AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        return user.getFeeds().stream().map(feedMapper::toDTO).toList();
    }

    @DeleteMapping("/feeds/{feedId}")
    public void unsubscribeFeed(@PathVariable("feedId") UUID feedId, @AuthenticationPrincipal Account principal) {
        User user = userService.getUserByAccount(principal).get();
        Optional<Feed> feed = feedService.getFeedById(feedId);

        if (feed.isEmpty()){
            throw new ControllerException(HttpStatus.NOT_FOUND,  String.format("Feed with id %s not found", feedId));
        }

        Feed foundFeed = feed.get();
        user.getFeeds().remove(foundFeed);
        userService.saveUser(user);
    }
}
