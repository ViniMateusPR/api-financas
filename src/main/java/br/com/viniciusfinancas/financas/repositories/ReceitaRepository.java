package br.com.viniciusfinancas.financas.repositories;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

  //  List<Receita> findByUser(User usuario);

  Optional<Receita> findById(Long id);


  void deleteById(Long id);

  List<Receita> findByUsuarioId(Long userId);

    @Query("SELECT r FROM Receita r WHERE r.usuario.email = :email")
    List<Receita> findByEmail(@Param("email") String email);
    @Query("SELECT r FROM Receita r JOIN FETCH r.usuario WHERE r.usuario.id = :usuarioId")
    List<Receita> findReceitasByUsuarioId(@Param("usuarioId") Long usuarioId);
}
