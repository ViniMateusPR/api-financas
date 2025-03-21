package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Dashboard extends JFrame {

    public Dashboard() {
        setTitle("Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Adicionando a label de boas-vindas
        JLabel welcomeLabel = new JLabel("Bem-vindo ao seu Dashboard!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        add(welcomeLabel, BorderLayout.NORTH);

        // Criando o painel para o gráfico
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new GridLayout(1, 1)); // Apenas um gráfico centralizado

        // Adicionando o gráfico de receitas e despesas com duas colunas
        chartPanel.add(createDualColumnChartPanel());

        add(chartPanel, BorderLayout.CENTER);

        // Criando os botões
        JButton despesaButton = new JButton("Acessar Despesas");
        despesaButton.setFont(new Font("Arial", Font.PLAIN, 18));

        JButton receitaButton = new JButton("Acessar Receitas");
        receitaButton.setFont(new Font("Arial", Font.PLAIN, 18));

        // Criando o botão para gerar o relatório de receita líquida
        JButton relatorioReceitaLiquidaButton = new JButton("Gerar Relatório");
        relatorioReceitaLiquidaButton.setFont(new Font("Arial", Font.PLAIN, 18));

        // Criando o painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(despesaButton);
        buttonPanel.add(receitaButton); // Adicionando o botão "Acessar Receitas"
        buttonPanel.add(relatorioReceitaLiquidaButton); // Adicionando o botão para gerar o relatório

        add(buttonPanel, BorderLayout.SOUTH);

        // Criando o painel para o botão de logout no canto superior direito
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton logoutButton = new JButton("Sair");
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 16));
        logoutPanel.add(logoutButton);
        add(logoutPanel, BorderLayout.NORTH);

        // Ação do botão "Acessar Despesas"
        despesaButton.addActionListener(e -> abrirTelaDespesa());

        // Ação do botão "Acessar Receitas"
        receitaButton.addActionListener(e -> abrirTelaReceita());

        // Ação do botão "Sair"
        logoutButton.addActionListener(e -> logout());

        // Ação do botão "Gerar Relatório Receita Líquida"
        relatorioReceitaLiquidaButton.addActionListener(e -> gerarRelatorioReceitaLiquida());

        setVisible(true);
    }

    private void abrirTelaDespesa() {
        new DespesaScreen();
        dispose();
    }

    private void abrirTelaReceita() {
        new ReceitaScreen();
        dispose();
    }

    private void logout() {
        // Limpar o token de autenticação e retornar para a tela de login
        TokenStorage.clearToken();
        TokenStorage.clearUserId();

        // Redireciona para a tela de login (presumindo que você tenha uma classe LoginScreen)
        new LoginPanel();
        dispose();
    }

    // Método para fazer requisição HTTP e buscar o total das receitas
    private double fetchTotalReceitas() {
        String urlString = "http://localhost:8080/finance/totalReceitas?userId=" + TokenStorage.getUserId();
        return fetchTotal(urlString);
    }

    // Método para fazer requisição HTTP e buscar o total das despesas
    private double fetchTotalDespesas() {
        String urlString = "http://localhost:8080/finance/totalDespesas?userId=" + TokenStorage.getUserId();
        return fetchTotal(urlString);
    }

    // Método genérico para fazer a requisição e retornar o total (Receitas ou Despesas)
    private double fetchTotal(String urlString) {
        try {
            // Criando a URL para a requisição
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + TokenStorage.getToken());

            // Verificando a resposta da requisição
            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extraindo o valor do total da resposta
                String responseString = response.toString();
                int startIndex = responseString.indexOf(":") + 1;
                int endIndex = responseString.indexOf("}");
                String totalString = responseString.substring(startIndex, endIndex).trim();
                return Double.parseDouble(totalString);
            } else {
                System.out.println("Erro na requisição: " + connection.getResponseCode());
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Método para criar o painel do gráfico com duas colunas (Receitas e Despesas)
    private ChartPanel createDualColumnChartPanel() {
        // Criando o dataset para receitas e despesas
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Adicionando as receitas ao dataset
        dataset.addValue(fetchTotalReceitas(), "Receitas", "Mês");

        // Adicionando as despesas ao dataset
        dataset.addValue(fetchTotalDespesas(), "Despesas", "Mês");

        // Criando o gráfico de barras
        JFreeChart chart = ChartFactory.createBarChart(
                "Receitas e Despesas", // Título do gráfico
                "Categoria", // Eixo X
                "Valor", // Eixo Y
                dataset, // Dataset
                PlotOrientation.VERTICAL, // Orientação do gráfico
                true, // Mostrar legenda
                true, // Mostrar tooltips
                false // Sem URLs
        );

        // Personalizando as cores das barras
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        // A cor das barras para as receitas (verde)
        renderer.setSeriesPaint(0, Color.GREEN);

        // A cor das barras para as despesas (vermelho)
        renderer.setSeriesPaint(1, Color.RED);

        return new ChartPanel(chart);
    }

    // Método para gerar o relatório de receita líquida
    private void gerarRelatorioReceitaLiquida() {
        int userId = TokenStorage.getUserId();
        String token = TokenStorage.getToken();

        if (token == null || token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Erro: Token de autenticação não encontrado.");
            return;
        }

        if (userId <= 0) {
            JOptionPane.showMessageDialog(this, "Erro: ID do usuário inválido.");
            return;
        }

        String url = "http://localhost:8080/relatorios/excelReceitaLiquida?userId=" + userId;
        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = conn.getInputStream();
                if (inputStream == null) {
                    JOptionPane.showMessageDialog(this, "Erro: Nenhum dado recebido da API.");
                    return;
                }

                String filePath = "C:\\Users\\vinic\\OneDrive\\Importante\\excelTeste\\relatorio_receita_liquida.xlsx";

                try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = br.readLine();
                JOptionPane.showMessageDialog(this, "Erro ao gerar o relatório: " + errorResponse);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao fazer a requisição para gerar o relatório.");
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        new Dashboard();
    }
}
