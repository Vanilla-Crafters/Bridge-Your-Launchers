package net.vanillacrafters.bridgeyourlaunchers.container;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class Container {
    private static void init() {
        FlatDarkLaf.setup();

        final JPanel panel = new JPanel();
        panel.add(new JButton("FlatDarkLaf button!"));
        panel.add(new JTextField("FlatDarkLaf text field!"));

        final JFrame frame = new JFrame("FlatDarkLaf test.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Container::init);
    }
}
