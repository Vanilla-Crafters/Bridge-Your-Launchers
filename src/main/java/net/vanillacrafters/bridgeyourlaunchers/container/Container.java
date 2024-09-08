package net.vanillacrafters.bridgeyourlaunchers.container;

import javax.swing.*;

public class Container {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setAlwaysOnTop(true);

        JOptionPane.showMessageDialog(
                frame,
                "That is a fabric mod please put this file into your mods folder",
                "Bridge your launchers",
                JOptionPane.WARNING_MESSAGE
        );
    }
}
