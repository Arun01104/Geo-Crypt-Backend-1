package com.Geo_Crypt.EPM.Config;

import com.Geo_Crypt.EPM.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Map your Role enum to ROLE_ authorities, e.g., ADMIN -> ROLE_ADMIN
        GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_" + u.getRole().name());

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword()) // already encoded
                .authorities(auth)
                .accountExpired(false).accountLocked(false)
                .credentialsExpired(false).disabled(false)
                .build();
    }

}
