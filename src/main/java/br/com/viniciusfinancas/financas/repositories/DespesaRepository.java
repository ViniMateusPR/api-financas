package br.com.viniciusfinancas.financas.repositories;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    List<Despesa> findByUsuarioId(Long userId);

    Optional<Despesa> findById(Long id);

    void deleteById(Long id);

    List<Despesa> findByUsuarioIdAndDataBetween(Long usuarioId, LocalDate dataInicio, LocalDate dataFim);
}
