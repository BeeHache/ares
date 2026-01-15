package net.blackhacker.ares.controller;

import net.blackhacker.ares.service.EmailConfirmationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email-confirmation")
public class EmailConfirmationController {
    // Injects the EmailConfirmationService
    private final EmailConfirmationService emailConfirmationService;

    public EmailConfirmationController(EmailConfirmationService emailConfirmationService) {
        this.emailConfirmationService = emailConfirmationService;
    }


    @GetMapping("/{code}")
    public ResponseEntity<String> confirmEmail(@PathVariable("code") String code) {
        emailConfirmationService.confirmEmail(code);
        return ResponseEntity.ok("Email confirmed successfully.");
    }

}
