package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.frontend.Main;
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

public class LoginPanel extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginPanel() {
        setTitle("Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2));

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Senha:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Não tem cadastro? Clique aqui");

        add(loginButton);
        add(registerButton);

        // Evento de Login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });

        // Evento para abrir tela de Registro
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fecha a tela de login
                new RegisterPanel(); // Abre a tela de registro
            }
        });

        setVisible(true);
    }

    private void realizarLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try {
            URL url = new URL("http://localhost:8080/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Criando JSON para envio
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) { // Login bem-sucedido
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = br.readLine();
                JSONObject jsonResponse = new JSONObject(response);
                String token = jsonResponse.getString("token");
                int userId = jsonResponse.getInt("id");  // Obtendo o ID do usuário

                // Salvar token e id no sistema
                TokenStorage.setToken(token);
                TokenStorage.setUserId(userId); // Armazenar o ID do usuário

            // Exibir o token e o ID para teste no console
            System.out.println("Token armazenado: " + token);
            System.out.println("ID do usuário: " + userId);

                JOptionPane.showMessageDialog(this, "Login realizado com sucesso!");
                dispose(); // Fecha a tela de login
                new Dashboard(); // Abre a tela principal da aplicação
            } else {
                JOptionPane.showMessageDialog(this, "Falha no login. Verifique suas credenciais.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar-se à API.");
        }
    }

}
