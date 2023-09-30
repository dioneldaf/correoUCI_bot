package org.example;

import javax.swing.*;
import java.awt.*;

public class Runner extends JFrame {
    private final Bot bot;
    public Runner(Bot bot) {
        super("CorreoUCI_bot");
        setSize(200, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        this.bot = bot;

        JLabel label = new JLabel("Bot Running...");
        label.setFont(new Font("CaskaydiaCove Nerd Font", Font.BOLD, 20));
        JButton button = new JButton("STOP");
        button.setFont(new Font("Fira Code", Font.BOLD, 20));
        getContentPane().add(label, BorderLayout.CENTER);
        getContentPane().add(button, BorderLayout.SOUTH);

        button.addActionListener(k -> dispose());

        setVisible(true);
    }

    @Override
    public void dispose() {
        Helper.saveBin(bot.getBotUsers());
        System.exit(0);
    }
}
