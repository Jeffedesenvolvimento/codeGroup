package br.com.codegroup.config;

import br.com.codegroup.model.User;
import br.com.codegroup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthConfig implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // mock fixo — ignora o banco
        if ("teste".equals(username)) {
            return User.builder()
                    .usuario("teste")
                    .senha("123456")
                    .build();
        }
        return userRepository.findByUsuario(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
