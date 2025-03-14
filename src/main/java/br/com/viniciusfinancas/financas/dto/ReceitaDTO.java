package br.com.viniciusfinancas.financas.dto;

import java.time.Instant;

public record ReceitaDTO(String titulo, Double valor,Instant data,  Long usuarioId, String status) {
}
