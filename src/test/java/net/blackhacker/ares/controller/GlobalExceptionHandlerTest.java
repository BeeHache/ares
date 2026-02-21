package net.blackhacker.ares.controller;

import net.blackhacker.ares.service.RegistrationException;
import net.blackhacker.ares.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/controller-exception")
        public void throwControllerException() {
            throw new ControllerException(HttpStatus.BAD_REQUEST, "Controller Error");
        }

        @GetMapping("/test/validation-exception")
        public void throwValidationException() {
            throw new ValidationException("Validation Error");
        }

        @GetMapping("/test/auth-exception")
        public void throwAuthException() {
            throw new BadCredentialsException("Bad Credentials");
        }

        @GetMapping("/test/registration-exception")
        public void throwRegistrationException() {
            throw new RegistrationException("Registration Error");
        }

        @GetMapping("/test/global-exception")
        public void throwGlobalException() {
            throw new RuntimeException("Unexpected Error");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleControllerException_shouldReturnStatusFromException() throws Exception {
        mockMvc.perform(get("/test/controller-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Controller Error"));
    }

    @Test
    void handleValidationException_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/validation-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Error"));
    }

    @Test
    void handleAuthenticationException_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/test/auth-exception"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void handleRegistrationException_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/registration-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Registration Error"));
    }

    @Test
    void handleGlobalException_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/global-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An internal server error occurred. Please try again later:Unexpected Error"));
    }
}
