package net.blackhacker.ares.controller;

import net.blackhacker.ares.dto.AccountDTO;
import net.blackhacker.ares.dto.FeedDTO;
import net.blackhacker.ares.dto.RoleDTO;
import net.blackhacker.ares.mapper.AccountMapper;
import net.blackhacker.ares.mapper.FeedMapper;
import net.blackhacker.ares.model.Account;
import net.blackhacker.ares.model.Role;
import net.blackhacker.ares.model.User;
import net.blackhacker.ares.repository.jpa.AccountRepository;
import net.blackhacker.ares.repository.jpa.RoleRepository;
import net.blackhacker.ares.service.AccountService;
import net.blackhacker.ares.service.FeedService;
import net.blackhacker.ares.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountService accountService;
    private final UserService userService;
    private final FeedService feedService;
    private final FeedMapper feedMapper;
    private final AccountMapper accountMapper;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;


    public AdminController(AccountService accountService,
                           UserService userService,
                           FeedService feedService,
                           FeedMapper feedMapper,
                           AccountMapper accountMapper,
                           RoleRepository roleRepository,
                           AccountRepository accountRepository) {
        this.accountService = accountService;
        this.userService = userService;
        this.feedService = feedService;
        this.feedMapper = feedMapper;
        this.accountMapper = accountMapper;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
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
    public Page<AccountDTO> getAccounts(
            @RequestParam(required = false) Account.AccountType type,
            @RequestParam(required = false) Boolean locked,
            @PageableDefault(size = 20) Pageable pageable) {
        return accountService.getAccountsFiltered(type, locked, pageable).map(accountMapper::toDTO);
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@RequestBody AccountDTO accountDTO) {
        if (accountDTO.getPassword() == null || accountDTO.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        
        Account account = accountMapper.toModel(accountDTO);
        
        // Map roles if provided
        if (accountDTO.getRoles() != null && !accountDTO.getRoles().isEmpty()) {
            Set<Role> roles = accountDTO.getRoles().stream()
                    .map(RoleDTO::getId)
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleId)))
                    .collect(Collectors.toSet());
            account.setRoles(roles);
        } else if (account.getType() == Account.AccountType.USER) {
            // Automatically assign USER role for USER type if no roles provided
            roleRepository.findByName("USER").ifPresent(role -> account.setRoles(Set.of(role)));
        }

        return accountMapper.toDTO(accountService.createAccount(account, accountDTO.getName()));
    }

    @PutMapping("/accounts/{id}/roles")
    public AccountDTO updateAccountRoles(@PathVariable("id") Long id, @RequestBody List<Long> roleIds) {
        Account account = accountService.getAccount(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleId)))
                .collect(Collectors.toSet());

        account.setRoles(roles);
        return accountMapper.toDTO(accountService.saveAccount(account));
    }

    @DeleteMapping("/accounts/{id}")
    public void deleteAccount(@PathVariable("id") Long id) {
        if (!accountRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
        accountRepository.deleteById(id);
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
