package me.sathish.event_service.security;

import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class EventserviceconfigUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(final String username) {
        if ("authUser".equals(username)) {
            final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(UserRoles.AUTH_USER));
            return User.withUsername(username)
                    .password("{bcrypt}$2a$10$FMzmOkkfbApEWxS.4XzCKOR7EbbiwzkPEyGgYh6uQiPxurkpzRMa6")
                    .authorities(authorities)
                    .build();
        }
        throw new UsernameNotFoundException("User " + username + " not found");
    }

}
