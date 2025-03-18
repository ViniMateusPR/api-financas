package br.com.viniciusfinancas.financas.controllers;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.domain.user.User;
import br.com.viniciusfinancas.financas.dto.DespesaDTO;
import br.com.viniciusfinancas.financas.dto.ReceitaDTO;
import br.com.viniciusfinancas.financas.enumPackage.DespesaStatus;
import br.com.viniciusfinancas.financas.enumPackage.ReceitaStatus;
import br.com.viniciusfinancas.financas.repositories.DespesaRepository;
import br.com.viniciusfinancas.financas.repositories.ReceitaRepository;
import br.com.viniciusfinancas.financas.repositories.UserRepository;
import br.com.viniciusfinancas.financas.service.DespesaService;
import br.com.viniciusfinancas.financas.service.ReceitaService;
import br.com.viniciusfinancas.financas.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    private final UserRepository repository;
    private final UserService userService;
    private final ReceitaRepository receitaRepository;
    private final ReceitaService receitaService;
    private final DespesaRepository despesaRepository;
    private final DespesaService despesaService;

    // Construtor do controlador
    public UserController(UserRepository repository, UserService userService, ReceitaRepository receitaRepository, ReceitaService receitaService, DespesaRepository despesaRepository, DespesaService despesaService) {
        this.repository = repository;
        this.userService = userService;
        this.receitaRepository = receitaRepository;
        this.receitaService = receitaService;
        this.despesaRepository = despesaRepository;
        this.despesaService = despesaService;
    }

    // Endpoint para verificar se o controlador está funcionando
    @GetMapping
    public ResponseEntity<String> getUser() {
        return ResponseEntity.ok("sucesso!");
    }

    @PostMapping("/enviarDespesa")
    public ResponseEntity<String> addDespesa(@RequestBody DespesaDTO despesaDTO) {
        // Verificação se o campo userId foi enviado
        if (despesaDTO.usuarioId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("O campo usuarioId é obrigatório.");
        }

        // Buscar o usuário pelo ID
        Optional<User> usuarioOptional = repository.findById(despesaDTO.usuarioId());  // Altere para usuarioId
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não encontrado.");
        }

        User usuario = usuarioOptional.get();

        // Criar uma nova despesa
        Despesa despesa = new Despesa();
        despesa.setTitulo(despesaDTO.titulo());
        despesa.setValor(despesaDTO.valor());
        despesa.setUsuario(usuario); // Associando o usuário à despesa

        // Verificar e atribuir o status, caso o campo status seja nulo ou inválido
        DespesaStatus status = DespesaStatus.PENDENTE; // Status padrão
        if (despesaDTO.status() != null) {
            try {
                status = DespesaStatus.valueOf(despesaDTO.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Status inválido. Os valores permitidos são: PAGA, PENDENTE, AGENDADA, ATRASADA.");
            }
        }
        despesa.setStatus(status);

        // Preencher a data automaticamente com a data/hora atual em Brasília
        Instant now = Instant.now();
        despesa.setData(now);

        // Salvar a despesa no banco de dados
        despesaRepository.save(despesa);

        // Formatar a data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("America/Sao_Paulo"));
        String dataFormatada = formatter.format(now);

        // Retornar resposta de sucesso com a data formatada
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Despesa criada com sucesso. Data: " + dataFormatada);
    }

    // Endpoint para enviar uma nova receita
    @PostMapping("/enviarReceita")
    public ResponseEntity<String> addReceita(@RequestBody ReceitaDTO receitaDTO) {
        // Verificar se o campo userId não é nulo
        if (receitaDTO.usuarioId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("O campo userId é obrigatório.");
        }

        // Buscar o usuário pelo ID
        Optional<User> usuarioOptional = repository.findById(Long.valueOf(receitaDTO.usuarioId()));

        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não encontrado.");
        }

        User usuario = usuarioOptional.get();

        // Criar uma nova receita
        Receita receita = new Receita();
        receita.setTitulo(receitaDTO.titulo());
        receita.setValor(receitaDTO.valor());
        receita.setUsuario(usuario); // Associando o usuário à receita

        // Preencher a data automaticamente com a data/hora atual em Brasília
        Instant now = Instant.now();
        receita.setData(now);

        // Caso tenha um status vindo do DTO, podemos setá-lo, caso contrário, o padrão será PENDENTE
        if (receitaDTO.status() != null) {
            receita.setStatus(ReceitaStatus.valorDoDescricao(receitaDTO.status()));  // Converte o status pela descrição
        }

        // Salvar a receita no banco de dados
        receitaRepository.save(receita);

        // Formatar a data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.of("America/Sao_Paulo"));
        String dataFormatada = formatter.format(now);

        // Retornar resposta de sucesso com a data formatada
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Receita criada com sucesso. Data: " + dataFormatada);
    }

    @GetMapping("/listarDespesas")
    public List<Despesa> listarDespesas(@RequestParam("userId") Long userId) {
        return despesaRepository.findByUsuarioId(userId);
    }

    @GetMapping("/listarReceitas")
    public List<Receita> listarReceitas(@RequestParam("userId") Long userId){
        return receitaRepository.findReceitasByUsuarioId(userId);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> findByAll(@PathVariable Long id){
        User user = userService.procurarPorId(id);
        return ResponseEntity.ok().body(user);
    }

    // Endpoint para buscar um usuário pelo email
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email) {
        Optional<User> userOptional = repository.findByEmail(email);

        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userOptional.get()); // Retorna o usuário encontrado
        } else {
            return ResponseEntity.notFound().build(); // Retorna 404 caso não encontre o usuário
        }
    }

    @PutMapping("/editarDespesa/{id}")
    public ResponseEntity<Despesa> editarDespesa(@PathVariable Long id, @RequestBody Despesa despesaAtualizada){
        Despesa despesaExistente = despesaRepository.findById(id)
                .orElse(null);
        if (despesaExistente == null) {
            return ResponseEntity.notFound().build();
        }

        despesaExistente.setTitulo(despesaAtualizada.getTitulo());
        despesaExistente.setValor(despesaAtualizada.getValor());
        despesaExistente.setStatus(despesaAtualizada.getStatus());

        Despesa despesaSalva = despesaRepository.save(despesaExistente);
        System.out.println("Despesa salva: "+despesaSalva);

        return ResponseEntity.ok(despesaSalva);
    }

    @PutMapping("/editarReceitas/{id}")
    public ResponseEntity<Receita> editarReceita(@PathVariable Long id, @RequestBody Receita receitaAtualizada) {
        // Log dos dados recebidos
        System.out.println("ID da receita recebida: " + id);
        System.out.println("Dados da receita recebida: " + receitaAtualizada);

        // Verifica se a receita existe
        Receita receitaExistente = receitaRepository.findById(id)
                .orElse(null); // Se não encontrar, retorna null

        if (receitaExistente == null) {
            // Se não encontrar a receita, retorna Not Found
            return ResponseEntity.notFound().build();
        }

        // Se a receita for encontrada, atualiza os dados
        receitaExistente.setTitulo(receitaAtualizada.getTitulo());
        receitaExistente.setValor(receitaAtualizada.getValor());
        receitaExistente.setStatus(receitaAtualizada.getStatus());  // Certifique-se que o campo 'status' está sendo mapeado

        // Salva a receita atualizada no banco de dados
        Receita receitaSalva = receitaRepository.save(receitaExistente);

        // Log dos dados após a atualização
        System.out.println("Receita salva: " + receitaSalva);

        // Retorna a receita atualizada
        return ResponseEntity.ok(receitaSalva);
    }

    @DeleteMapping("/deletarReceita/{id}")
    public ResponseEntity<String> deletarReceita(@PathVariable Long id) {
        try {
            boolean isDeleted = receitaService.deleteReceita(id);

            if (isDeleted) {
                return ResponseEntity.ok("Receita excluída com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Receita não encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir a receita: " + e.getMessage());
        }
    }

    @DeleteMapping("/deletarDespesa/{id}")
    public ResponseEntity<String> deletarDespesa(@PathVariable Long id){
        try {
            boolean isDeleted = despesaService.deleteDespesa(id);

            if (isDeleted) {
                return ResponseEntity.ok("Despesa excluída com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Receita não encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir a receita: " + e.getMessage());
        }
    }


}
