package br.com.viniciusfinancas.financas.repositories;

import br.com.viniciusfinancas.financas.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
}
