package br.com.viniciusfinancas.financas.frontend.views;

import br.com.viniciusfinancas.financas.frontend.utils.TokenStorage;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("email", email);
            json.put("password", password);

            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            // Se a resposta contiver o token
            String token = json.getString("token");

            // Salvar token no sistema
            TokenStorage.setToken(token);

            /* Exibir o token para teste no console
                System.out.println("Token armazenado: " + token);  */

            // Exibir o token para teste no console
            System.out.println("Token após registro: " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) { // Sucesso
                JOptionPane.showMessageDialog(this, "Registro realizado com sucesso!");
                dispose();
                new Dashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Falha no registro. Tente novamente.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar-se à API.");
        }
    }
}
