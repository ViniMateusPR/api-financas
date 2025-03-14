package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.BarRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
        JLabel welcomeLabel = new JLabel("Bem-vindo ao seu Dashboard!" , SwingConstants.CENTER);
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

        // Criando o painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(despesaButton);
        buttonPanel.add(receitaButton); // Adicionando o novo botão

        add(buttonPanel, BorderLayout.SOUTH);

        // Ação do botão "Acessar Despesas"
        despesaButton.addActionListener(e -> abrirTelaDespesa());

        // Ação do botão "Acessar Receitas"
        receitaButton.addActionListener(e -> abrirTelaReceita());

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

    public static void main(String[] args) {
        new Dashboard();
    }
}
