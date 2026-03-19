package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.mapper.AccountMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountService accountService;
    private final UserService userService;
    private final FeedService feedService;
    private final FeedMapper feedMapper;
    private final AccountMapper accountMapper;


    public AdminController(AccountService accountService,
                           UserService userService,
                           FeedService feedService,
                           FeedMapper feedMapper,
                           AccountMapper accountMapper) {
        this.accountService = accountService;
        this.userService = userService;
        this.feedService = feedService;
        this.feedMapper = feedMapper;
        this.accountMapper = accountMapper;
    }

    @GetMapping("/hello")
    public Map<String, String> helloAdmin() {
        return Map.of("message", "Hello, Admin!");
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userService.userCount());
        stats.put("totalFeeds", feedService.feedCount());
        stats.put("totalArticles", feedService.feedItemsCount());
        return stats;
    }

    @GetMapping("/accounts")
    public Page<AccountDTO> getAccounts(@PageableDefault(size = 20) Pageable pageable) {
        return accountService.getAccounts(pageable).map(accountMapper::toDTO);
    }

    @GetMapping("/users")
    public Page<User> getUsers(@PageableDefault(size = 20) Pageable pageable) {
        return userService.getUsers(pageable);
    }

    @PostMapping("/users/{id}/lock")
    public void lockUser(@PathVariable("id") Long id) {
        Optional<Account> optionalAccount = accountService.getAccount(id);
        if (optionalAccount.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        Account account = optionalAccount.get();
        account.lockAccount();
        accountService.saveAccount(account);
    }

    @PostMapping("/users/{id}/unlock")
    public void unlockUser(@PathVariable("id") Long id) {
        Optional<Account> optionalAccount = accountService.getAccount(id);
        if (optionalAccount.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        Account account = optionalAccount.get();
        account.enableAccount();
        accountService.saveAccount(account);
    }

    @GetMapping("/feeds")
    public Page<FeedDTO> getFeeds(@PageableDefault(size = 20) Pageable pageable) {
        return feedService.findAllFeeds(pageable).map(feed -> {
            //set the subscription count for each feed
            feed.setSubscribers(feedService.feedSubscriberCount(feed.getId()));
            if (feed.getSubscribers() == null) {
                feed.setSubscribers(0L);
            }
            return feed;
        }).map(feedMapper::toDTO);
    }

    @DeleteMapping("/feeds/{id}")
    public void deleteFeed(@PathVariable("id") UUID id) {
        if (!feedService.feedExists(id)) {
            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found");
        }
        feedService.deleteFeed(id);
    }

    @PostMapping("/feeds/{id}/refresh")
    public void refreshFeed(@PathVariable("id") UUID id) {
        if (!feedService.feedExists(id)) {
           throw  new ResponseStatusException(HttpStatus.NOT_FOUND, "Feed not found");
        }
        feedService.updateFeed(id);
    }
}
