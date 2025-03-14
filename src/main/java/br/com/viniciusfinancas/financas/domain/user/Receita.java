package br.com.viniciusfinancas.financas.domain.user;

import br.com.viniciusfinancas.financas.enumPackage.DespesaStatus;
import br.com.viniciusfinancas.financas.enumPackage.ReceitaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "receita", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", length = 255, nullable = false)
    private String titulo;

    @Column(name = "valor", nullable = false)
    private Double valor;

    @Column(name = "data")
    private Instant data;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)  // Chave estrangeira
    private User usuario;  // Relacionando a chave estrangeira com a tabela de usuário

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReceitaStatus status = ReceitaStatus.PENDENTE;
    // Getters e Setters

    public ReceitaStatus getStatus() {
        return status;
    }

    public void setStatus(ReceitaStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Instant getData() {
        return data;
    }

    public void setData(Instant data) {
        this.data = data;
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Receita{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", valor=" + valor +
                ", data=" + data +
                ", usuario=" + usuario +
                ", status=" + status +
                '}';
    }
}
