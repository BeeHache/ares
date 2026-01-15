package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    final FeedService feedService;
    final FeedMapper feedMapper;
    final UserService userService;

    public FeedController(FeedService feedService, FeedMapper feedMapper, UserService userService) {
        this.feedService = feedService;
        this.feedMapper = feedMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Collection<FeedDTO>> getFeed(@AuthenticationPrincipal Account principal) {

        User user = userService.getUserByAccount(principal);
        Collection<FeedDTO> feeds = user.getFeeds().stream().map(feedMapper::toDTO).toList();
        return ResponseEntity.ok(feeds);
    }
}
