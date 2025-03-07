package br.com.viniciusfinancas.financas.service;

import br.com.viniciusfinancas.financas.domain.user.User;
import br.com.viniciusfinancas.financas.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Usando injeção via construtor (boa prática)
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User procurarPorId(Long id){
        Optional<User> user = userRepository.findById(id);
        return user.get();
    }



}
