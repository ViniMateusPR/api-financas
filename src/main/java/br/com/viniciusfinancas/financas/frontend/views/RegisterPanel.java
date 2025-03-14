package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
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

public class RegisterPanel extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton backButton;

    public RegisterPanel() {
        setTitle("Registro de Usuário");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2));

        add(new JLabel("Nome:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Senha:"));
        passwordField = new JPasswordField();
        add(passwordField);

        registerButton = new JButton("Registrar");
        backButton = new JButton("Voltar para Login");

        add(registerButton);
        add(backButton);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarRegistro();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a tela de registro
                new LoginPanel(); // Volta para tela de login
            }
        });

        setVisible(true);
    }

    private void realizarRegistro() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            URL url = new URL("http://localhost:8080/auth/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Monta o JSON para o envio
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("email", email);
            json.put("password", password);

            // Envia o JSON para a API
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            // Lê a resposta da API
            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            if (responseCode == 200 || responseCode == 201) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }

                // Exibe a resposta para verificação
                System.out.println("Resposta da API: " + response.toString());

                // Tenta acessar o token no JSON da resposta
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("token")) {
                    String token = jsonResponse.getString("token");

                    // Armazenar o token localmente (exemplo com TokenStorage)
                    TokenStorage.setToken(token);

                    System.out.println("Token após registro: " + token);

                    // Exibir mensagem de sucesso e redirecionar
                    JOptionPane.showMessageDialog(this, "Registro realizado com sucesso!");
                    dispose(); // Fecha a tela de registro
                    new Dashboard(); // Redireciona para o Dashboard
                } else {
                    JOptionPane.showMessageDialog(this, "Token não encontrado na resposta.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Falha no registro. Tente novamente.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar-se à API.");
        }
    }

}
