package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
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
import java.io.BufferedReader;
import java.io.IOException;
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

        JButton adicionarButton = new JButton("Adicionar Despesa");
        JButton verButton = new JButton("Ver Despesas");
        JButton voltarButton = new JButton("Voltar ao Dashboard");

        // Painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3)); // Alinha os botões na horizontal
        buttonPanel.add(adicionarButton);
        buttonPanel.add(verButton);
        buttonPanel.add(voltarButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Ação ao clicar no botão "Adicionar Despesa"
        adicionarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exibirFormularioDespesa();
            }
        });

        // Ação ao clicar no botão "Ver Despesas"
        verButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exibirDespesas();
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

        setVisible(true);
    }

    private void exibirFormularioDespesa() {
        JFrame despesaFrame = new JFrame("Adicionar Despesa");
        despesaFrame.setSize(400, 300);
        despesaFrame.setLayout(new GridLayout(4, 2));
        despesaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField tituloField = new JTextField();
        JTextField valorField = new JTextField();

        String[] statusOptions = {"PAGA", "PENDENTE", "AGENDADA", "ATRASADA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);

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
                String status = (String) statusComboBox.getSelectedItem();

                try {
                    // Envia os dados para a API
                    enviarDespesa(titulo, valor, status);
                    despesaFrame.dispose(); // Fecha a tela após enviar
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(despesaFrame, "Erro ao enviar a despesa.");
                    ex.printStackTrace();  // Adicionando o printStackTrace para melhor depuração
                }
            }
        });

        despesaFrame.setVisible(true);
    }

    private void enviarDespesa(String titulo, String valor, String status) throws Exception {
        int userId = TokenStorage.getUserId();

        // Criando o JSON para enviar a despesa
        JSONObject despesaJson = new JSONObject();
        despesaJson.put("titulo", titulo);
        despesaJson.put("valor", valor);
        despesaJson.put("usuarioId", userId);
        despesaJson.put("status", status);

        URL url = new URL("http://localhost:8080/user/enviarDespesa");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String token = TokenStorage.getToken();
        conn.setRequestProperty("Authorization", "Bearer " + token);

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(despesaJson.toString().getBytes(StandardCharsets.UTF_8));
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
            JOptionPane.showMessageDialog(null, "Erro ao enviar a despesa. " + errorResponse);
        }
    }

    private void exibirDespesas() {
        int userId = TokenStorage.getUserId();
        String token = TokenStorage.getToken();

        String url = "http://localhost:8080/user/listarDespesas?userId=" + userId;

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
                Despesa[] despesas = gson.fromJson(response.toString(), Despesa[].class);

                String[] columnNames = {"ID", "Título", "Valor", "Data", "Status", "Usuário", "Ação"};
                Object[][] data = new Object[despesas.length][7];

                for (int i = 0; i < despesas.length; i++) {
                    data[i][0] = despesas[i].getId();
                    data[i][1] = despesas[i].getTitulo();
                    data[i][2] = despesas[i].getValor();
                    data[i][3] = despesas[i].getData().toString();
                    data[i][4] = despesas[i].getStatus().getDescricao();
                    data[i][5] = despesas[i].getUsuario().getName();
                    // Cria um botão de edição com o ícone de lápis
                    data[i][6] = new JButton(new ImageIcon("C:/Users/vinic/OneDrive/Importante/pencil.png"));
                }

                DefaultTableModel model = new DefaultTableModel(data, columnNames);
                JTable table = new JTable(model) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return column == 6; // Apenas a coluna de Ação (ícone de lápis) é editável
                    }
                };

                // Ajustando o comportamento da célula para exibir o botão corretamente
                table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
                table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), despesas, table));

                table.setFillsViewportHeight(true);
                JScrollPane scrollPane = new JScrollPane(table);

                // Exibindo a tabela em um JFrame
                JFrame tableFrame = new JFrame("Lista de Despesas");
                tableFrame.setSize(800, 400);
                tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                tableFrame.setLayout(new BorderLayout());
                tableFrame.add(scrollPane, BorderLayout.CENTER);
                tableFrame.setVisible(true);
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = br.readLine();
                JOptionPane.showMessageDialog(this, "Erro ao carregar as despesas: " + errorResponse);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao fazer a requisição. Confira o console para detalhes.");
            e.printStackTrace();  // Log da exceção para depuração
        }
    }

    private void exibirFormularioEdicaoDespesa(Despesa despesa) {
        JFrame editFrame = new JFrame("Editar Despesa");
        editFrame.setSize(400, 300);
        editFrame.setLayout(new GridLayout(4, 2));
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField tituloField = new JTextField(despesa.getTitulo());
        JTextField valorField = new JTextField(String.valueOf(despesa.getValor()));

        String[] statusOptions = {"PAGA", "PENDENTE", "AGENDADA", "ATRASADA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setSelectedItem(despesa.getStatus().getDescricao());

        editFrame.add(new JLabel("Título:"));
        editFrame.add(tituloField);

        editFrame.add(new JLabel("Valor:"));
        editFrame.add(valorField);

        editFrame.add(new JLabel("Status:"));
        editFrame.add(statusComboBox);

        JButton salvarButton = new JButton("Salvar Alterações");
        editFrame.add(salvarButton);

        salvarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titulo = tituloField.getText();
                String valor = valorField.getText();
                String status = (String) statusComboBox.getSelectedItem();

                try {
                    editarDespesa(despesa.getId(), titulo, valor, status);
                    editFrame.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(editFrame, "Erro ao editar a despesa.");
                    ex.printStackTrace();
                }
            }
        });

        editFrame.setVisible(true);
    }

    private void editarDespesa(Long despesaId, String titulo, String valor, String status) throws Exception {
        JSONObject despesaJson = new JSONObject();
        despesaJson.put("titulo", titulo);
        despesaJson.put("valor", valor);
        despesaJson.put("status", status);

        URL url = new URL("http://localhost:8080/user/editarDespesa/" + despesaId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");

        String token = TokenStorage.getToken();
        conn.setRequestProperty("Authorization", "Bearer " + token);

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(despesaJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            JOptionPane.showMessageDialog(null, "Despesa editada com sucesso");
        } else {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorResponse = br.readLine();
            JOptionPane.showMessageDialog(null, "Erro ao editar a despesa: " + errorResponse);
        }
    }

    private void excluirDespesa(int despesaId) throws Exception {
        URL url = new URL("http://localhost:8080/user/deletarDespesa/" + despesaId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("DELETE");
        conn.setDoOutput(true);

        String token = TokenStorage.getToken();
        conn.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = conn.getResponseCode();

        // Verifica se a resposta foi bem-sucedida (HTTP_OK)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            JOptionPane.showMessageDialog(null, "Despesa excluída com sucesso!");
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            JOptionPane.showMessageDialog(null, "Despesa não encontrada.");
        } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            JOptionPane.showMessageDialog(null, "Você não tem permissão para excluir esta despesa.");
        } else {
            // Para outros códigos de erro, mostre uma mensagem genérica
            JOptionPane.showMessageDialog(null, "Erro ao excluir despesa. Código de erro: " + responseCode);
        }
        // Fechar a conexão após a requisição
        conn.disconnect();
    }


    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton;
        private JButton closeButton;

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            editButton = new JButton();
            editButton.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/pencil.png"));
            editButton.setPreferredSize(new Dimension(30, 30));

            closeButton = new JButton();
            closeButton.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/close.png"));
            closeButton.setPreferredSize(new Dimension(30, 30));

            add(editButton);
            add(closeButton);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Ajustando a altura da linha para o tamanho dos botões
            int rowHeight = Math.max(editButton.getPreferredSize().height, closeButton.getPreferredSize().height);
            table.setRowHeight(rowHeight);
            return this;
        }
    }


    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private JButton deleteButton;  // Novo botão de exclusão
        private Despesa[] despesas;
        private String label;
        private JTable table;
        private int index;

        public ButtonEditor(JCheckBox checkBox, Despesa[] despesas, JTable table) {
            super(checkBox);
            this.despesas = despesas;
            this.table = table;
            button = new JButton();
            button.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/pencil.png"));
            button.setPreferredSize(new Dimension(30, 30));
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        exibirFormularioEdicaoDespesa(despesas[row]);
                    }
                    fireEditingStopped();
                }
            });


            deleteButton = new JButton();
            deleteButton.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/close.png"));
            deleteButton.setPreferredSize(new Dimension(30, 30));
            deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        // Exibir o pop-up de confirmação
                        int resposta = JOptionPane.showConfirmDialog(
                                null,
                                "Tem certeza que deseja excluir?",
                                "Confirmar Exclusão",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );

                        // Se a resposta for SIM (JOptionPane.YES_OPTION)
                        if (resposta == JOptionPane.YES_OPTION) {
                            // Recuperar o ID da receita (supondo que ele está em uma coluna específica da tabela)
                            Long despesaId = (Long) table.getValueAt(row, 0); // Supondo que o ID está na primeira coluna

                            // Chamar o método para excluir a receita
                            try {
                                excluirDespesa(Math.toIntExact(despesaId));
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }

                            // Atualizar a tabela (opcional, para refletir a exclusão)
                            ((DefaultTableModel) table.getModel()).removeRow(row);
                        }
                    }
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Ajuste da altura da linha
            int rowHeight = Math.max(button.getPreferredSize().height, deleteButton.getPreferredSize().height);
            table.setRowHeight(rowHeight);

            JPanel panel = new JPanel();
            panel.add(button);
            panel.add(deleteButton);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }

}
