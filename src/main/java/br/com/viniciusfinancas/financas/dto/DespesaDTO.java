package br.com.viniciusfinancas.financas.dto;

import java.time.Instant;

public record DespesaDTO(String titulo, Double valor, Instant data, Integer userId, String status) {
}
