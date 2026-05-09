package me.sathish.event_service.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class EventserviceconfigSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // creates hashes with {bcrypt} prefix
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(final HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/**", "/actuator/**")
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .httpBasic(basic -> basic.realmName("basic realm"))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain uiFilterChain(final HttpSecurity http) throws Exception {
        return http.cors(withDefaults())
                .csrf(withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/webjars/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.loginPage("/login").failureUrl("/login?loginError=true"))
                .logout(logout ->
                        logout.logoutSuccessUrl("/?logoutSuccess=true").deleteCookies("JSESSIONID"))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login?loginRequired=true")))
                .build();
    }
}
