package com.sathish.runevents.security;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
class SecurityUserDetailsService implements UserDetailsService {
    //    @Bean
    //    public InMemoryUserDetailsManager createUserDetailsManager() {
    //        UserDetails userDetails1 = createNewUser("in28minutes", "dummy");
    //        UserDetails userDetails2 = createNewUser("ranga", "dummydummy");
    //        return new InMemoryUserDetailsManager(userDetails1, userDetails2);
    //    }
    //
    //    private UserDetails createNewUser(String username, String password) {
    //        Function<String, String> passwordEncoder
    //                = input -> new BCryptPasswordEncoder().encode(input);
    //
    //        UserDetails userDetails = User.builder()
    //                .passwordEncoder(passwordEncoder)
    //                .username(username)
    //                .password(password)
    //                .roles("USER","ADMIN")
    //                .build();
    //        return userDetails;
    //    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        Optional<UserDetails> userDetails = Optional.of(new User(
                username,
                "{noop}Admin1234%",
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        return userDetails.get();
    }
}
