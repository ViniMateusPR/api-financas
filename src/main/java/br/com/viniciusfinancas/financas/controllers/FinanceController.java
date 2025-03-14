package br.com.viniciusfinancas.financas.controllers;

import br.com.viniciusfinancas.financas.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    // Endpoint para pegar o total de receitas
    @GetMapping("/totalReceitas")
    public ResponseEntity<Map<String, Double>> getTotalReceitas(@RequestParam Long userId) {
        double total = financeService.getTotalReceitas(userId);
        Map<String, Double> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    // Endpoint para pegar o total de despesas
    @GetMapping("/totalDespesas")
    public ResponseEntity<Map<String, Double>> getTotalDespesas(@RequestParam Long userId) {
        double total = financeService.getTotalDespesas(userId);
        Map<String, Double> response = new HashMap<>();
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
}
