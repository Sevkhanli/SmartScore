package az.edu.itbrains.SmartScore.security;

import az.edu.itbrains.SmartScore.models.User;
import az.edu.itbrains.SmartScore.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Təsdiqlənmə yoxlaması CustomUserDetails-in isEnabled() metoduna keçirildi
        return new CustomUserDetails(user);
    }
}
