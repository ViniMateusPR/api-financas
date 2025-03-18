package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.frontend.utils.InstantAdapter;
import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

public class ReceitaScreen extends JFrame {

    public ReceitaScreen() {
        setTitle("Receita");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JButton adicionarButton = new JButton("Adicionar Receita");
        JButton verButton = new JButton("Ver Receitas");
        JButton gerarRelatorioButton = new JButton("Gerar Relatório");
        JButton voltarButton = new JButton("Voltar ao Dashboard");

        // Painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4)); // Alinha os botões na horizontal
        buttonPanel.add(adicionarButton);
        buttonPanel.add(verButton);
        buttonPanel.add(gerarRelatorioButton);
        buttonPanel.add(voltarButton);


        add(buttonPanel, BorderLayout.CENTER);

        // Ação ao clicar no botão "Adicionar Receita"
        adicionarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exibirFormularioReceita();
            }
        });

        // Ação ao clicar no botão "Ver Receitas"
        verButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exibirReceitas();
            }
        });

        // Ação ao clicar no botão "Voltar ao Dashboard"
        voltarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new Dashboard(); // Chama a tela do Dashboard
            }
        });

        // Ação ao clicar no botão "Gerar Relatório"
        gerarRelatorioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gerarRelatorio(); // Função para gerar o relatório
            }
        });

        setVisible(true);
    }

    private void exibirFormularioReceita() {
        JFrame receitaFrame = new JFrame("Adicionar Receita");
        receitaFrame.setSize(400, 300);
        receitaFrame.setLayout(new GridLayout(4, 2));
        receitaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField tituloField = new JTextField();
        JTextField valorField = new JTextField();

        String[] statusOptions = {"RECEBIDA", "PENDENTE", "AGENDADA", "ATRASADA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);

        receitaFrame.add(new JLabel("Título:"));
        receitaFrame.add(tituloField);

        receitaFrame.add(new JLabel("Valor:"));
        receitaFrame.add(valorField);

        receitaFrame.add(new JLabel("Status:"));
        receitaFrame.add(statusComboBox);

        JButton enviarButton = new JButton("Enviar");
        receitaFrame.add(enviarButton);

        // Ação ao clicar no botão "Enviar"
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titulo = tituloField.getText();
                String valor = valorField.getText();
                String status = (String) statusComboBox.getSelectedItem();

                try {
                    // Envia os dados para a API
                    enviarReceita(titulo, valor, status);
                    receitaFrame.dispose(); // Fecha a tela após enviar
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(receitaFrame, "Erro ao enviar a receita.");
                    ex.printStackTrace();  // Adicionando o printStackTrace para melhor depuração
                }
            }
        });

        receitaFrame.setVisible(true);
    }

    private void enviarReceita(String titulo, String valor, String status) throws Exception {
        int userId = TokenStorage.getUserId();

        // Criando o JSON para enviar a receita
        JSONObject receitaJson = new JSONObject();
        receitaJson.put("titulo", titulo);
        receitaJson.put("valor", valor);
        receitaJson.put("usuarioId", userId);
        receitaJson.put("status", status);

        URL url = new URL("http://localhost:8080/user/enviarReceita");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String token = TokenStorage.getToken();
        conn.setRequestProperty("Authorization", "Bearer " + token);

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(receitaJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode == 201) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            JOptionPane.showMessageDialog(null, response);
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorResponse = br.readLine();
            JOptionPane.showMessageDialog(null, "Erro ao enviar a receita. " + errorResponse);
        }
    }

    private void exibirReceitas() {
        int userId = TokenStorage.getUserId();
        String token = TokenStorage.getToken();

        String url = "http://localhost:8080/user/listarReceitas?userId=" + userId;

        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Instant.class, new InstantAdapter())
                        .create();
                Receita[] receitas = gson.fromJson(response.toString(), Receita[].class);

                // Atualizando os nomes das colunas
                String[] columnNames = {"Título", "Valor", "Data", "Status", "Usuário", "Ação"};
                Object[][] data = new Object[receitas.length][6]; // Reduzindo para 6 colunas

                for (int i = 0; i < receitas.length; i++) {
                    data[i][0] = receitas[i].getTitulo();
                    data[i][1] = receitas[i].getValor();
                    data[i][2] = receitas[i].getData().toString();
                    data[i][3] = receitas[i].getStatus().getDescricao();
                    data[i][4] = receitas[i].getUsuario().getName();
                    // Cria um botão de edição com o ícone de lápis
                    data[i][5] = new JButton(new ImageIcon("C:/Users/vinic/OneDrive/Importante/pencil.png"));
                }

                DefaultTableModel model = new DefaultTableModel(data, columnNames);
                JTable table = new JTable(model) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 5; // Apenas a coluna de Ação (ícone de lápis) é editável
                    }
                };

                // Ajustando o comportamento da célula para exibir o botão corretamente
                table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
                table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox(), receitas, table));

                table.setFillsViewportHeight(true);
                JScrollPane scrollPane = new JScrollPane(table);

                // Exibindo a tabela em um JFrame
                JFrame tableFrame = new JFrame("Lista de Receitas");
                tableFrame.setSize(800, 400);
                tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                tableFrame.setLayout(new BorderLayout());
                tableFrame.add(scrollPane, BorderLayout.CENTER);
                tableFrame.setVisible(true);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = br.readLine();
                JOptionPane.showMessageDialog(this, "Erro ao carregar as receitas: " + errorResponse);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao fazer a requisição. Confira o console para detalhes.");
            e.printStackTrace();  // Log da exceção para depuração
        }
    }

    // Função para gerar o relatório
    public void gerarRelatorio() {
        int userId = TokenStorage.getUserId();
        String token = TokenStorage.getToken();

        String url = "http://localhost:8080/relatorios/excelReceita?userId=" + userId;
        try {
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Lê a resposta como um fluxo de bytes
                InputStream inputStream = conn.getInputStream();
                String filePath = "C:\\Users\\vinic\\OneDrive\\Importante\\excelTeste\\relatorio_receita.xlsx";  // Defina o caminho onde deseja salvar

                // Salva o arquivo no caminho especificado
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
            e.printStackTrace();  // Log da exceção para depuração
        }
    }



    // Classe que renderiza o botão de edição corretamente na célula
    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Classe que permite a edição do botão
    public static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private Receita[] receitas;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, Receita[] receitas, JTable table) {
            super(checkBox);
            this.receitas = receitas;
            this.table = table;
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        Receita receita = receitas[row];
                        JOptionPane.showMessageDialog(table, "Receita selecionada: " + receita.getTitulo());
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    Receita receita = receitas[row];
                    JOptionPane.showMessageDialog(table, "Receita: " + receita.getTitulo());
                }
            }
            isPushed = false;
            return label;
        }
    }
}
