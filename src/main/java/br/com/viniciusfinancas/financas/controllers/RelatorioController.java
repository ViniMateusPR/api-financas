package br.com.viniciusfinancas.financas.controllers;

import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.repositories.ReceitaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final ReceitaRepository receitaRepository;

    public RelatorioController(ReceitaRepository receitaRepository) {
        this.receitaRepository = receitaRepository;
    }

    @GetMapping("/excelReceita")
    public ResponseEntity<byte[]> gerarRelatorioExcel(@RequestParam("userId") Long userId) throws IOException {
        // Consultar as receitas para o usuário logado
        List<Receita> receitas = receitaRepository.findByUsuarioId(userId);

        // Verifique no console os dados recuperados
        System.out.println("Receitas recuperadas para o usuário ID " + userId + ":");
        for (Receita receita : receitas) {
            System.out.println("ID: " + receita.getId() + ", Título: " + receita.getTitulo() + ", Valor: " + receita.getValor() + ", Data: " + receita.getData() + ", Status: " + receita.getStatus());
        }

        // Cria uma nova planilha Excel
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Planilha de Receitas (sem a coluna Total)
        Sheet receitaSheet = workbook.createSheet("Receitas");
        criarCabecalho(receitaSheet, new String[]{"Título", "Valor", "Data", "Status"});  // Sem a coluna "Total"
        preencherDadosReceitas(receitaSheet, receitas);

        // Calcular a soma dos valores
        double somaTotal = calcularSoma(receitas);

        // Adiciona a soma na última linha (fora do loop de receitas)
        Row totalRow = receitaSheet.createRow(receitas.size() + 1);  // Linha após a última receita
        totalRow.createCell(0).setCellValue("Total");
        totalRow.createCell(1).setCellValue(somaTotal);  // Coloca o valor total na segunda coluna
        // Deixe as outras células da linha em branco
        totalRow.createCell(2).setCellValue("");
        totalRow.createCell(3).setCellValue("");

        // Gerar arquivo em memória
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        // Retorna o arquivo gerado como um array de bytes
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_receitas.xlsx")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(byteArrayOutputStream.toByteArray());
    }

    private void criarCabecalho(Sheet sheet, String[] cabecalho) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cabecalho.length; i++) {
            headerRow.createCell(i).setCellValue(cabecalho[i]);
        }
    }

    private void preencherDadosReceitas(Sheet sheet, List<Receita> receitas) {
        int rowNum = 1;
        for (Receita receita : receitas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(receita.getTitulo());
            row.createCell(1).setCellValue(receita.getValor());
            row.createCell(2).setCellValue(receita.getData().toString());
            row.createCell(3).setCellValue(receita.getStatus().toString());
        }
    }

    // Função para calcular a soma total das receitas
    private double calcularSoma(List<Receita> receitas) {
        double somaTotal = 0;
        for (Receita receita : receitas) {
            somaTotal += receita.getValor();
        }
        return somaTotal;
    }
}
