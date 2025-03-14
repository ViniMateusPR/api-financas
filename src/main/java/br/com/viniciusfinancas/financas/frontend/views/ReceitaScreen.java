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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        JButton voltarButton = new JButton("Voltar ao Dashboard");

        // Painel para os botões
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3)); // Alinha os botões na horizontal
        buttonPanel.add(adicionarButton);
        buttonPanel.add(verButton);
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

                String[] columnNames = {"ID", "Título", "Valor", "Data", "Status", "Usuário", "Ação"};
                Object[][] data = new Object[receitas.length][7];

                for (int i = 0; i < receitas.length; i++) {
                    data[i][0] = receitas[i].getId();
                    data[i][1] = receitas[i].getTitulo();
                    data[i][2] = receitas[i].getValor();
                    data[i][3] = receitas[i].getData().toString();
                    data[i][4] = Objects.toString(receitas[i].getStatus(), "N/A");
                    data[i][5] = (receitas[i].getUsuario() != null) ? receitas[i].getUsuario().getName() : "Desconhecido";
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
                table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox(), receitas, table));

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
        private JButton editButton;
        private JButton closeButton;
        private String label;
        private boolean isPushed;
        private Receita[] receitas;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, Receita[] receitas, JTable table) {
            super(checkBox);
            this.receitas = receitas;
            this.table = table;

            editButton = new JButton();
            editButton.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/pencil.png"));
            editButton.setPreferredSize(new Dimension(30, 30));
            editButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        exibirFormularioEdicaoReceita(receitas[row]);
                    }
                    fireEditingStopped();
                }
            });

            closeButton = new JButton();
            closeButton.setIcon(new ImageIcon("C:/Users/vinic/OneDrive/Importante/close.png"));
            closeButton.setPreferredSize(new Dimension(30, 30));
            closeButton.addActionListener(new ActionListener() {
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
                            Long receitaId = (Long) table.getValueAt(row, 0); // Supondo que o ID está na primeira coluna

                            // Chamar o método para excluir a receita
                            excluirReceita(receitaId);

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
            int rowHeight = Math.max(editButton.getPreferredSize().height, closeButton.getPreferredSize().height);
            table.setRowHeight(rowHeight);

            JPanel panel = new JPanel();
            panel.add(editButton);
            panel.add(closeButton);
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }
    }


    private void exibirFormularioEdicaoReceita(Receita receita) {
        JFrame receitaFrame = new JFrame("Editar Receita");
        receitaFrame.setSize(400, 300);
        receitaFrame.setLayout(new GridLayout(4, 2));
        receitaFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextField tituloField = new JTextField(receita.getTitulo());
        JTextField valorField = new JTextField(String.valueOf(receita.getValor()));

        String[] statusOptions = {"RECEBIDA", "PENDENTE", "AGENDADA", "ATRASADA"};
        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setSelectedItem(receita.getStatus());

        receitaFrame.add(new JLabel("Título:"));
        receitaFrame.add(tituloField);

        receitaFrame.add(new JLabel("Valor:"));
        receitaFrame.add(valorField);

        receitaFrame.add(new JLabel("Status:"));
        receitaFrame.add(statusComboBox);

        JButton enviarButton = new JButton("Salvar Alterações");
        receitaFrame.add(enviarButton);

        // Ação ao clicar no botão "Salvar Alterações"
        enviarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String titulo = tituloField.getText();
                String valor = valorField.getText();
                String status = (String) statusComboBox.getSelectedItem();

                try {
                    editarReceita(receita.getId(), titulo, valor, status);
                    receitaFrame.dispose(); // Fecha a tela após salvar
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(receitaFrame, "Erro ao salvar a receita.");
                }
            }
        });

        receitaFrame.setVisible(true);
    }

    private void editarReceita(Long receitaId, String titulo, String valor, String status) throws Exception {
        int userId = TokenStorage.getUserId();  // Recuperando o ID do usuário a partir do token

        // Criar o JSON para enviar os dados da receita (sem o 'id' na body)
        JSONObject receitaJson = new JSONObject();
        receitaJson.put("titulo", titulo);
        receitaJson.put("valor", valor);
        receitaJson.put("usuarioId", userId);  // Confirme que o nome do campo é 'usuarioId'
        receitaJson.put("status", status);

        // Log para ver o JSON que está sendo enviado
        System.out.println("JSON enviado para o servidor: " + receitaJson.toString());

        // Modifique a URL para incluir o 'id' da receita
        URL url = new URL("http://localhost:8080/user/editarReceitas/" + receitaId);
        System.out.println("URL da requisição: " + url);  // Log da URL

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");

        // Recuperar o token de autenticação
        String token = TokenStorage.getToken();
        System.out.println("Token utilizado: " + token);  // Log do token

        conn.setRequestProperty("Authorization", "Bearer " + token);

        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(receitaJson.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("Código de resposta: " + responseCode);  // Log do código de resposta HTTP

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Sucesso ao editar a receita
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            System.out.println("Resposta do servidor: " + response);  // Log da resposta do servidor
            JOptionPane.showMessageDialog(null, "Receita editada com sucesso!");
        } else {
            // Exibe a resposta de erro para ajudar na depuração
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String errorResponse = br.readLine();
            System.err.println("Erro na edição da receita: " + errorResponse);  // Log de erro no cliente
            JOptionPane.showMessageDialog(null, "Erro ao editar a receita. " + errorResponse);
        }
    }

    private void excluirReceita(Long receitaId) {
        try {
            URL url = new URL("http://localhost:8080/user/deletarReceita/" + receitaId); // URL do seu endpoint
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                JOptionPane.showMessageDialog(null, "Receita excluída com sucesso!");
            } else {
                JOptionPane.showMessageDialog(null, "Erro ao excluir receita.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro na comunicação com o servidor.");
        }
    }



}
