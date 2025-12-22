package net.blackhacker.ares.controller;

import jakarta.servlet.http.HttpServletRequest;
import net.blackhacker.ares.model.Feed;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController()
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping("/")
    Collection<Feed> getFeeds() {
        return null;

    }

}
