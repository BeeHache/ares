package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.UserDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.mapper.UserMapper;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import net.blackhacker.ares.service.UtilsService;
import net.blackhacker.ares.validation.MultipartFileValidator;
import net.blackhacker.ares.validation.URLValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final FeedService feedService;
    private final UtilsService utilsService;
    private final UserMapper userMapper;
    private final FeedMapper feedMapper;
    private final MultipartFileValidator multipartFileValidator;
    private final URLValidator urlValidator;


    public UserController(UserService userService, FeedService feedService, UtilsService utilsService,
                          UserMapper userMapper, FeedMapper feedMapper, MultipartFileValidator multipartFileValidator,
                          URLValidator urlValidator){
        this.userService = userService;
        this.feedService = feedService;
        this.utilsService = utilsService;
        this.userMapper = userMapper;
        this.feedMapper = feedMapper;
        this.multipartFileValidator = multipartFileValidator;
        this.urlValidator = urlValidator;
    }

    @GetMapping("/")
    UserDTO getUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        return userMapper.toDTO(user);
    }

    @PostMapping()
    ResponseEntity<Void> importOPML(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        multipartFileValidator.validateMultipartFile(file);

        final User user = userService.getUserByUserDetails(userDetails);
        utilsService.opml(file).handleAsync((feeds, throwable)->{
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
            if (feeds == null) {
                return null;
            }
            user.getFeeds().addAll(feeds);
            userService.saveUser(user);
            return feeds;
        });
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/addfeed")
    ResponseEntity<FeedDTO> addFeed(@RequestParam("link") String link, @AuthenticationPrincipal UserDetails userDetails){
        urlValidator.validateURL(link);

        User user = userService.getUserByUserDetails(userDetails);
        Feed feed = feedService.addFeed(link);
        user.getFeeds().add(feed);
        userService.saveUser(user);
        FeedDTO feedDTO = feedMapper.toDTO(feed);
        return ResponseEntity.ok(feedDTO);
    }
}
