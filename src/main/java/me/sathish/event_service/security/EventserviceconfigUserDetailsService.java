package me.sathish.event_service.security;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.sathish.event_service.event_domain_user.EventDomainUser;
import me.sathish.event_service.event_domain_user.EventDomainUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class EventserviceconfigUserDetailsService implements UserDetailsService {

    private final EventDomainUserRepository eventDomainUserRepository;

    public EventserviceconfigUserDetailsService(
            final EventDomainUserRepository eventDomainUserRepository) {
        this.eventDomainUserRepository = eventDomainUserRepository;
    }

    @Override
    public EventserviceconfigUserDetails loadUserByUsername(final String username) {
        final EventDomainUser eventDomainUser = eventDomainUserRepository.findByUsernameIgnoreCase(username);
        if (eventDomainUser == null) {
            log.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User " + username + " not found");
        }
        final String role = UserRoles.AUTH_USER;
        final List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        return new EventserviceconfigUserDetails(eventDomainUser.getId(), username, eventDomainUser.getHash(), authorities);
    }

}
