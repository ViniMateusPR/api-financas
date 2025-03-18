package br.com.viniciusfinancas.financas.controllers;

import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.domain.user.Despesa;
import br.com.viniciusfinancas.financas.repositories.ReceitaRepository;
import br.com.viniciusfinancas.financas.repositories.DespesaRepository;
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
    private final DespesaRepository despesaRepository;

    public RelatorioController(ReceitaRepository receitaRepository, DespesaRepository despesaRepository) {
        this.receitaRepository = receitaRepository;
        this.despesaRepository = despesaRepository;
    }

    // Endpoint para gerar o relatório de receitas
    @GetMapping("/excelReceita")
    public ResponseEntity<byte[]> gerarRelatorioExcel(@RequestParam("userId") Long userId) throws IOException {
        List<Receita> receitas = receitaRepository.findByUsuarioId(userId);
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet receitaSheet = workbook.createSheet("Receitas");
        criarCabecalho(receitaSheet, new String[]{"Título", "Valor", "Data", "Status"});
        preencherDadosReceitas(receitaSheet, receitas);
        double somaTotal = calcularSoma(receitas);
        Row totalRow = receitaSheet.createRow(receitas.size() + 1);
        totalRow.createCell(0).setCellValue("Total");
        totalRow.createCell(1).setCellValue(somaTotal);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_receitas.xlsx")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(byteArrayOutputStream.toByteArray());
    }

    // Novo endpoint para gerar o relatório de despesas
    @GetMapping("/excelDespesa")
    public ResponseEntity<byte[]> gerarRelatorioExcelDespesa(@RequestParam("userId") Long userId) throws IOException {
        // Consultar as despesas para o usuário logado
        List<Despesa> despesas = despesaRepository.findByUsuarioId(userId);

        // Cria uma nova planilha Excel
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Planilha de Despesas
        Sheet despesaSheet = workbook.createSheet("Despesas");
        criarCabecalho(despesaSheet, new String[]{"Título", "Valor", "Data", "Status"});
        preencherDadosDespesas(despesaSheet, despesas);

        // Calcular a soma das despesas
        double somaTotal = calcularSomaDespesas(despesas);

        // Adiciona a soma na última linha
        Row totalRow = despesaSheet.createRow(despesas.size() + 1);
        totalRow.createCell(0).setCellValue("Total");
        totalRow.createCell(1).setCellValue(somaTotal);

        // Gerar arquivo em memória
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        // Retorna o arquivo gerado como um array de bytes
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_despesas.xlsx")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(byteArrayOutputStream.toByteArray());
    }

    // Endpoint para gerar o relatório de receita líquida
    @GetMapping("/excelReceitaLiquida")
    public ResponseEntity<byte[]> gerarRelatorioExcelLiquida(@RequestParam("userId") Long userId) throws IOException {
        // Consultar as receitas e despesas para o usuário logado
        List<Receita> receitas = receitaRepository.findByUsuarioId(userId);
        List<Despesa> despesas = despesaRepository.findByUsuarioId(userId);

        // Calcular a soma das receitas e despesas
        double somaReceitas = calcularSomaReceitas(receitas);
        double somaDespesas = calcularSomaDespesas(despesas);
        double receitaLiquida = somaReceitas - somaDespesas;

        // Cria uma nova planilha Excel
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Planilha de Receitas (sem a coluna Total)
        Sheet receitaSheet = workbook.createSheet("Receitas");
        criarCabecalho(receitaSheet, new String[]{"Título", "Valor", "Data", "Status"});  // Sem a coluna "Total"
        preencherDadosReceitas(receitaSheet, receitas);

        // Planilha de Despesas
        Sheet despesaSheet = workbook.createSheet("Despesas");
        criarCabecalho(despesaSheet, new String[]{"Título", "Valor", "Data", "Status"});  // Sem a coluna "Total"
        preencherDadosDespesas(despesaSheet, despesas);

        // Planilha de Resumo
        Sheet resumoSheet = workbook.createSheet("Resumo");
        Row resumoRow = resumoSheet.createRow(0);
        resumoRow.createCell(0).setCellValue("Total Receitas");
        resumoRow.createCell(1).setCellValue(somaReceitas);

        Row despesaRow = resumoSheet.createRow(1);
        despesaRow.createCell(0).setCellValue("Total Despesas");
        despesaRow.createCell(1).setCellValue(somaDespesas);

        Row liquidaRow = resumoSheet.createRow(2);
        liquidaRow.createCell(0).setCellValue("Receita Líquida");
        liquidaRow.createCell(1).setCellValue(receitaLiquida);

        // Gerar arquivo em memória
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        workbook.close();

        // Retorna o arquivo gerado como um array de bytes
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_receita_liquida.xlsx")
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

    private void preencherDadosDespesas(Sheet sheet, List<Despesa> despesas) {
        int rowNum = 1;
        for (Despesa despesa : despesas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(despesa.getTitulo());
            row.createCell(1).setCellValue(despesa.getValor());
            row.createCell(2).setCellValue(despesa.getData().toString());
            row.createCell(3).setCellValue(despesa.getStatus().toString());
        }
    }

    // Função para calcular a soma das receitas
    private double calcularSomaReceitas(List<Receita> receitas) {
        double somaTotal = 0;
        for (Receita receita : receitas) {
            somaTotal += receita.getValor();
        }
        return somaTotal;
    }

    // Função para calcular a soma das despesas
    private double calcularSomaDespesas(List<Despesa> despesas) {
        double somaTotal = 0;
        for (Despesa despesa : despesas) {
            somaTotal += despesa.getValor();
        }
        return somaTotal;
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
