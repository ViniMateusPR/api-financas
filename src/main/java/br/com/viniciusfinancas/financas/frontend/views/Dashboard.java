package br.com.viniciusfinancas.financas.frontend.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Dashboard extends JFrame {
    public Dashboard() {
        setTitle("Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Adicionando a label de boas-vindas
        JLabel welcomeLabel = new JLabel("Bem-vindo ao seu Dashboard!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        add(welcomeLabel, BorderLayout.CENTER);

        // Criando o botão "Despesa"
        JButton despesaButton = new JButton("Acessar Despesas");
        despesaButton.setFont(new Font("Arial", Font.PLAIN, 18));

        // Adicionando o botão ao painel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(despesaButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Ação do botão "Acessar Despesas"
        despesaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exibir a tela de Despesa
                abrirTelaDespesa();
            }
        });

        setVisible(true);
    }

    // Método para abrir a tela de Despesa
    private void abrirTelaDespesa() {
        // Cria a nova instância da tela de Despesa
        new DespesaScreen();
        // Fecha o Dashboard após abrir a tela de Despesa
        dispose();
    }
}
