package br.com.viniciusfinancas.financas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FinanceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Método para buscar a soma das receitas
    public double getTotalReceitas(Long userId) {
        String sql = "SELECT SUM(valor) FROM receita WHERE usuario_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, Double.class);
    }

    // Método para buscar a soma das despesas
    public double getTotalDespesas(Long userId) {
        String sql = "SELECT SUM(valor) FROM despesa WHERE usuario_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, Double.class);
    }
}