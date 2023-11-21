package org.example;

import javax.swing.*;
import java.awt.*;

public class Runner extends JFrame {
    private final Bot bot;
    public Runner(Bot bot) {
        super("CorreoUCI_bot");
        setSize(200, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());
        this.bot = bot;

        JLabel label = new JLabel("Bot Running...");
        label.setFont(new Font("Ink Free", Font.PLAIN, 20));
        label.setHorizontalAlignment(JLabel.CENTER);
        JButton button = new JButton("STOP");
        button.setFont(new Font("Ink Free", Font.PLAIN, 20));
        getContentPane().add(label);
        getContentPane().add(button);

        button.addActionListener(k -> dispose());

        setVisible(true);
    }

    @Override
    public void dispose() {
        Helper.saveBin(bot.getBotUsers());
        System.exit(0);
    }
}
