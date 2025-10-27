package com.doncey.admin;

import javax.swing.*;
import java.awt.*;

public class PlayersPanel extends JPanel {
    
    public PlayersPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);        
        JLabel label = new JLabel("PESTAÑA JUGADORES", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 24));
        add(label, BorderLayout.CENTER);
    }
}