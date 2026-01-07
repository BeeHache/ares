package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.mapper.UserMapper;
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
    ResponseEntity<UserDTO> getUser(@AuthenticationPrincipal User principal) {
        User user = userService.getUserByUserDetails(principal);
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/import")
    ResponseEntity<Void> importOPML(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User principal) {
        multipartFileValidator.validateMultipartFile(file);

        final User user = userService.getUserByUserDetails(principal);
        opmlService.importFile(file).forEach(feed -> {
            feed.getUsers().add(user);
            user.getFeeds().add(feed);
        });
        userService.saveUser(user);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/import")
    ResponseEntity<Void> importOPML(@RequestParam("url") String url, @AuthenticationPrincipal User principal) {
        urlValidator.validateURL(url);

        final User user = userService.getUserByUserDetails(principal);
        opmlService.importFeed(url).forEach(feed -> {
            feed.getUsers().add(user);
            user.getFeeds().add(feed);
        });
        userService.saveUser(user);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/addfeed")
    ResponseEntity<FeedDTO> addFeed(@RequestParam("link") String link, @AuthenticationPrincipal @NonNull User principal){
        urlValidator.validateURL(link);

        User user = userService.getUserByUserDetails(principal);
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        FeedDTO feedDTO = feedMapper.toDTO(feed);
        return ResponseEntity.ok(feedDTO);
    }
}
