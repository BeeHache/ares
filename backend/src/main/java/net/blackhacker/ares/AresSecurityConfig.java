package net.blackhacker.ares;

import lombok.extern.slf4j.Slf4j;
import net.blackhacker.ares.security.CustomAccessDeniedHandler;
import net.blackhacker.ares.security.JwtAuthenticationEntryPoint;
import net.blackhacker.ares.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AresSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final CustomAccessDeniedHandler forbiddenHandler;

    private final List<URL> allowedOrigins;

    public AresSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                              JwtAuthenticationEntryPoint unauthorizedHandler,
                              CustomAccessDeniedHandler forbiddenHandler,
                              @Value("${spring.application.allowedOrigins}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.unauthorizedHandler = unauthorizedHandler;
        this.forbiddenHandler = forbiddenHandler;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(url -> {
            try {
                if (url.isEmpty()) {
                    return null;
                }
                return new URI(url).toURL();
            } catch (Exception e) {
                log.error("'{}' is not a valid URL", url);
                return null; // null get filtered out
            }
        }).filter(Objects::nonNull).toList();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler) // Handle 401
                        .accessDeniedHandler(forbiddenHandler)         // Handle 403
                )
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/features/**").hasRole("ADMIN")
                                .requestMatchers(
                                        "/api/login/**", "/api/register/**", "/").permitAll()
                                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> localhostOrigins = Arrays.asList(
                "http://localhost:4200",
                "http://localhost",
                "https://localhost"
        );

        configuration.setAllowedOrigins(Stream.concat(localhostOrigins.stream(), allowedOrigins.stream().map(URL::toString)).toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)  {
        return config.getAuthenticationManager();
    }
}
