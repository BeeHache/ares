package net.blackhacker.ares.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

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


    public UserController(UserService userService, FeedService feedService, OpmlService opmlService,
                          UserMapper userMapper, FeedMapper feedMapper, MultipartFileValidator multipartFileValidator,
                          URLValidator urlValidator){
        this.userService = userService;
        this.feedService = feedService;
        this.opmlService = opmlService;
        this.userMapper = userMapper;
        this.feedMapper = feedMapper;
        this.multipartFileValidator = multipartFileValidator;
        this.urlValidator = urlValidator;
    }

    @GetMapping("/")
    ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal Account account) {
        User user = userService.getUserByAccount(account);
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/import")
    ResponseEntity<Void> importOPML(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal Account account) {
        multipartFileValidator.validateMultipartFile(file);

        final User user = userService.getUserByAccount(account);
        opmlService.importFile(file).forEach(feed -> {
            feed.getUsers().add(user);
            user.getFeeds().add(feed);
        });
        userService.saveUser(user);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/import")
    ResponseEntity<Void> importOPML(@RequestParam("url") String url, @AuthenticationPrincipal Account account) {
        urlValidator.validateURL(url);

        User user = userService.getUserByAccount(account);
        opmlService.importFeed(url).forEach(feed -> {
            feed.getUsers().add(user);
            user.getFeeds().add(feed);
        });
        userService.saveUser(user);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/addfeed")
    ResponseEntity<FeedDTO> addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull Account account){
        urlValidator.validateURL(link);

        User user = userService.getUserByAccount(account);
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        FeedDTO feedDTO = feedMapper.toDTO(feed);
        return ResponseEntity.ok(feedDTO);
    }

    @GetMapping("/feeds")
    public ResponseEntity<Collection<FeedDTO>> getFeed(@AuthenticationPrincipal Account account) {

        User user = userService.getUserByAccount(account);
        Collection<FeedDTO> feeds = user.getFeeds().stream().map(feedMapper::toDTO).toList();
        return ResponseEntity.ok(feeds);
    }

    @DeleteMapping("/feeds/{id}")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long id, @AuthenticationPrincipal Account account) {
        User user = userService.getUserByAccount(account);
        Feed feed = feedService.getFeedById(id);

        if (feed == null){
            return ResponseEntity.notFound().build();
        }

        user.getFeeds().remove(feed);
        feed.getUsers().remove(user);
        feedService.saveFeed(feed);
        userService.saveUser(user);
        return ResponseEntity.ok().build();
    }
}
