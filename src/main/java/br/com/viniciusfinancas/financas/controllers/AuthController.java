package br.com.viniciusfinancas.financas.controllers;

import br.com.viniciusfinancas.financas.domain.user.User;
import br.com.viniciusfinancas.financas.dto.LoginRequestDTO;
import br.com.viniciusfinancas.financas.dto.RegisterRequestDTO;
import br.com.viniciusfinancas.financas.dto.ResponseDTO;
import br.com.viniciusfinancas.financas.infra.security.TokenService;
import br.com.viniciusfinancas.financas.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(UserRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body){
        User user = this.repository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User not found"));
        if(passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body){
        Optional<User> user = this.repository.findByEmail(body.email());

        if (user.isEmpty()) {
            // Criação de novo usuário
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());

            // Salvar o novo usuário no banco de dados
            this.repository.save(newUser);

            // Gerar o token JWT
            String token = this.tokenService.generateToken(newUser);

            // Retornar a resposta com os dados do usuário e o token
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
        }

        // Retornar erro caso o email já esteja em uso
        return ResponseEntity.badRequest().body("Este email já está em uso.");
    }


}
