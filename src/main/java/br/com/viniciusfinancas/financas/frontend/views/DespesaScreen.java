package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import br.com.viniciusfinancas.financas.frontend.utils.InstantAdapter;
import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class DespesaScreen extends JFrame {
    public DespesaScreen() {
        setTitle("Despesa");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Criando os botões
        JButton adicionarButton = new JButton("Adicionar Despesa");
        JButton verButton = new JButton("Ver Despesas");

        // Painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2)); // Alinha os botões na horizontal
        buttonPanel.add(adicionarButton);
        buttonPanel.add(verButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Ação ao clicar no botão "Adicionar Despesa"
        adicionarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exibe o formulário de adicionar despesa
                exibirFormularioDespesa();
            }
        });

        // Ação ao clicar no botão "Ver Despesas"
        verButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exibe a lista de despesas
                exibirDespesas();
            }
        });

        setVisible(true);
    }

    // Método para exibir o formulário de adicionar despesa
    private void exibirFormularioDespesa() {
        // Criando o JFrame para o formulário de despesa
        JFrame despesaFrame = new JFrame("Adicionar Despesa");
        despesaFrame.setSize(400, 300);
        despesaFrame.setLayout(new GridLayout(4, 2));
        despesaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Campos para entrada de dados
        JTextField tituloField = new JTextField();
        JTextField valorField = new JTextField();

        // Criando o JComboBox para o status
        String[] statusOptions = {"PENDENTE", "PAGA", "AGENDADA", "ATRASADA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);

        // Adicionando os campos
        despesaFrame.add(new JLabel("Título:"));
        despesaFrame.add(tituloField);

        despesaFrame.add(new JLabel("Valor:"));
        despesaFrame.add(valorField);

        despesaFrame.add(new JLabel("Status:"));
        despesaFrame.add(statusComboBox);

        JButton enviarButton = new JButton("Enviar");
        despesaFrame.add(enviarButton);

        // Ação ao clicar no botão "Enviar"
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titulo = tituloField.getText();
                String valor = valorField.getText();
                String status = (String) statusComboBox.getSelectedItem(); // Obtém o status selecionado

                try {
                    // Envia os dados para a API
                    enviarDespesa(titulo, valor, status);

                    // Fecha o formulário de despesa após enviar
                    despesaFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(despesaFrame, "Erro ao enviar despesa.");
                }
            }
        });

        despesaFrame.setVisible(true);
    }

    // Método para enviar a despesa para a API
    private void enviarDespesa(String titulo, String valor, String status) throws Exception {
        int userId = TokenStorage.getUserId();  // Recupera o ID do usuário armazenado

        // Criando o JSON para enviar a despesa
        JSONObject despesaJson = new JSONObject();
        despesaJson.put("titulo", titulo);
        despesaJson.put("valor", valor);  // Agora valor é Double
        despesaJson.put("usuarioId", userId);  // Certificando-se de passar a chave correta
        despesaJson.put("status", status);

        // Configurando a URL e conexão
        URL url = new URL("http://localhost:8080/user/enviarDespesa");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // Adicionando o token no cabeçalho
        String token = TokenStorage.getToken(); // Recupera o token armazenado
        conn.setRequestProperty("Authorization", "Bearer " + token);

        conn.setDoOutput(true);

        // Exibe o JSON enviado para depuração
        System.out.println("Enviando JSON: " + despesaJson.toString());

        OutputStream os = conn.getOutputStream();
        os.write(despesaJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        // Código de resposta HTTP
        int responseCode = conn.getResponseCode();
        System.out.println("Código de resposta HTTP: " + responseCode);  // Exibe o código de resposta

        if (responseCode == 201) { // Despesa criada com sucesso
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            JOptionPane.showMessageDialog(null, response);
        } else {
            // Leitura da resposta de erro
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorResponse = br.readLine();
            JOptionPane.showMessageDialog(null, "Erro ao enviar a despesa. " + errorResponse);
        }
    }

    // Método para exibir as despesas cadastradas
    private void exibirDespesas() {
        // Recuperando o ID do usuário logado do TokenStorage
        int userId = TokenStorage.getUserId();  // Recupera o ID do usuário armazenado
        String token = TokenStorage.getToken();  // Recupera o token armazenado

        // Exibindo as informações para depuração no console
        System.out.println("User ID: " + userId);
        System.out.println("Token: " + token);

        // Criando uma requisição GET para o endpoint que retorna as despesas filtradas por userId
        String url = "http://localhost:8080/user/listarDespesas?userId=" + userId;

        long startTime = System.currentTimeMillis();  // Iniciando o cronômetro para medir o tempo de resposta

        try {
            // Configurando a conexão HTTP
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");

            // Adicionando o token ao cabeçalho da requisição
            conn.setRequestProperty("Authorization", "Bearer " + token); // Adicionando o token ao cabeçalho da requisição

            // Verificando a resposta da requisição
            int responseCode = conn.getResponseCode();
            long endTime = System.currentTimeMillis();  // Tempo final da requisição
            long responseTime = endTime - startTime;  // Tempo de resposta da requisição

            // Exibindo o código de resposta no console
            System.out.println("Código de resposta: " + responseCode);
            System.out.println("Tempo de resposta: " + responseTime + " ms");

            if (responseCode == 200) { // Se a requisição foi bem-sucedida
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                // Usando Gson para converter o JSON em uma lista de objetos Despesa
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Instant.class, new InstantAdapter())  // Registrando o adaptador para Instant
                        .create();

                Despesa[] despesas = gson.fromJson(response.toString(), Despesa[].class);  // Convertendo o JSON para array de Despesas

                // Preparando os dados para a tabela
                String[] columnNames = {"ID", "Título", "Valor", "Data", "Status", "Usuário"};
                Object[][] data = new Object[despesas.length][6]; // Inicializa a matriz de dados

                for (int i = 0; i < despesas.length; i++) {
                    data[i][0] = despesas[i].getId();  // ID
                    data[i][1] = despesas[i].getTitulo();  // Título
                    data[i][2] = despesas[i].getValor();  // Valor
                    data[i][3] = despesas[i].getData().toString();  // Data convertida para String
                    data[i][4] = despesas[i].getStatus().toString();  // Status convertido para String
                    data[i][5] = despesas[i].getUsuario().getName();  // Nome do usuário
                }

                // Criando a tabela
                JTable table = new JTable(data, columnNames);
                JScrollPane scrollPane = new JScrollPane(table);

                // Exibindo a tabela em um JFrame
                JFrame tableFrame = new JFrame("Lista de Despesas");
                tableFrame.setSize(600, 400);
                tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                tableFrame.add(scrollPane);
                tableFrame.setVisible(true);

            } else {
                // Caso o código de resposta não seja 200 (OK), mostramos a mensagem de erro
                String errorResponse = null;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    if (br != null) {
                        errorResponse = br.readLine();  // Tentando ler a resposta de erro
                    }
                }

                if (errorResponse == null) {
                    errorResponse = "Erro desconhecido. Não foi possível obter a resposta do servidor.";
                }

                // Exibindo detalhes do erro no console
                System.err.println("Erro ao carregar as despesas:");
                System.err.println("Código de erro: " + responseCode);
                System.err.println("Mensagem de erro: " + errorResponse);

                // Exibindo o erro no JOptionPane
                JOptionPane.showMessageDialog(this, "Erro ao carregar as despesas: " + errorResponse);
            }
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();  // Tempo final da requisição em caso de erro
            long responseTime = endTime - startTime;  // Tempo de resposta da requisição em caso de erro

            // Exibindo detalhes do erro no console
            System.err.println("Erro ao fazer a requisição.");
            e.printStackTrace();  // Exibe o stack trace no console para ajudar a depurar
            System.err.println("Tempo de resposta até o erro: " + responseTime + " ms");

            // Exibindo uma mensagem no JOptionPane
            JOptionPane.showMessageDialog(this, "Erro ao fazer a requisição. Confira o console para detalhes.");
        }
    }



}