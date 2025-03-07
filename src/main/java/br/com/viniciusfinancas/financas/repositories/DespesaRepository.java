package br.com.viniciusfinancas.financas.repositories;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DespesaRepository extends JpaRepository<Despesa, Long> {
    Optional<Despesa> findByTitulo(String titulo);
}
