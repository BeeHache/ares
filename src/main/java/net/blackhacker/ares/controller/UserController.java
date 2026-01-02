package net.blackhacker.ares.controller;

import jakarta.servlet.http.HttpSession;
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
import net.blackhacker.ares.validation.UserDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private UtilsService utilsService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FeedMapper feedMapper;

    @Autowired
    private UserDTOValidator userDTOValidator;

    @Autowired
    private MultipartFileValidator multipartFileValidator;

    @Autowired
    private URLValidator urlValidator;


    @PostMapping("/login")
    void loginUser(@RequestBody UserDTO userDTO, HttpSession httpSession) {
        userDTOValidator.validateUserForLogin(userDTO);
        User user = userService.loginUser(userMapper.toModel(userDTO));
        httpSession.setAttribute("user", userMapper.toDTO(user));
    }

    @GetMapping("/logout")
    void logoutUser(HttpSession httpSession) {
        httpSession.invalidate();
    }

    @GetMapping("/")
    UserDTO getUser(HttpSession httpSession) {
        return userMapper.toDTO((User) httpSession.getAttribute("user"));
    }


    @PostMapping()
    ResponseEntity<Void> importOPML(@RequestParam("file") MultipartFile file, HttpSession httpSession) {
        UserDTO sessionUser = (UserDTO) httpSession.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.badRequest().build();
        }

        final User user = userService.getUserByEmail(sessionUser.getEmail());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        multipartFileValidator.validateMultipartFile(file);

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
    ResponseEntity<FeedDTO> addFeed(@RequestParam("link") String link, HttpSession httpSession){
        UserDTO userDTO = (UserDTO) httpSession.getAttribute("user");
        if (userDTO == null) {
            return ResponseEntity.badRequest().build();
        }

        urlValidator.validateURL(link);

        Feed feed = feedService.addFeed(link);
        Optional<User> userOption = userService.findByEmail(userDTO.getEmail());

        if (userOption.isEmpty()) return ResponseEntity.badRequest().build();

        User user = userOption.get();
        user.getFeeds().add(feed);
        userService.saveUser(user);
        FeedDTO feedDTO = feedMapper.toDTO(feed);
        return ResponseEntity.ok(feedDTO);
    }
}
