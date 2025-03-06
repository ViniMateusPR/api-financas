package br.com.viniciusfinancas.financas.infra.security;

import br.com.viniciusfinancas.financas.domain.user.User;
import br.com.viniciusfinancas.financas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
@Component
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repository;
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = this.repository.findByName(name)
                .orElseThrow(()->new UsernameNotFoundException("User Not Found"));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
