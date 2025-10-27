package com.doncey.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerPanel extends JPanel {
    
    private JTextArea logArea; // Área de logs
    private JLabel timeValueLabel; // Etiqueta de hora dinámica
    
    private static final Color BG_PRIMARY = new Color(15, 15, 15);              
    private static final Color BG_SECONDARY = new Color(35, 35, 35);            
    private static final Color WHITE_LIGHT = new Color(240, 240, 240);          
    private static final Color WHITE_PURE = new Color(255, 255, 255);           
    private static final Color GRAY_MEDIUM = new Color(100, 100, 100);          
    private static final Color GRAY_LIGHT = new Color(200, 200, 200);           
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);       
    
    // Fuentes
    private static final Font FONT_LABEL = new Font("Courier New", Font.BOLD, 13);
    private static final Font FONT_VALUE = new Font("Courier New", Font.BOLD, 15);
    private static final Font FONT_BUTTON = new Font("Courier New", Font.BOLD, 14);
    private static final Font FONT_LOGS = new Font("Consolas", Font.PLAIN, 14);
    
    // Constructor
    public ServerPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createLogsPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    // Header: Estado y Hora
    private JPanel createHeaderPanel() {
        // Panel principal
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(48, 32, 48, 32));
        
        // Panel de estado
        JPanel statusPanel = new JPanel(new GridLayout(1, 2, 50, 0));
        statusPanel.setBackground(BG_SECONDARY);
        statusPanel.setOpaque(false);
        statusPanel.add(createStatusWidget("ESTADO", "● ONLINE", WHITE_LIGHT));
        statusPanel.add(createStatusWidget("HORA", getCurrentTime(), GRAY_LIGHT));
        
        // Referencia para actualizar hora
        timeValueLabel = (JLabel) ((JPanel) statusPanel.getComponent(1)).getComponent(1);
        new Timer(1000, e -> timeValueLabel.setText(getCurrentTime())).start();
        
        // Agregar al panel principal
        panel.add(statusPanel, BorderLayout.WEST);
        panel.setBorder(BorderFactory.createMatteBorder(20, 20, 20, 0, BG_SECONDARY));
        return panel;
    }
    
    // Widget de status
    private JPanel createStatusWidget(String label, String value, Color color) {
        // Contenedor del widget de status
        JPanel widget = new JPanel();
        widget.setLayout(new BoxLayout(widget, BoxLayout.Y_AXIS));
        widget.setBackground(BG_SECONDARY);
        widget.setOpaque(false);
        
        // Etiquetas
        JLabel labelText = new JLabel(label);
        labelText.setFont(FONT_LABEL);
        labelText.setForeground(TEXT_SECONDARY);
        labelText.setOpaque(false);
        widget.add(labelText);

        JLabel valueText = new JLabel(value);
        valueText.setFont(FONT_VALUE);
        valueText.setForeground(color);
        valueText.setOpaque(false);
        widget.add(valueText);
        
        return widget;
    }
    
    // Panel de logs
    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        
        // Línea superior
        JPanel topDivider = new JPanel();
        topDivider.setBackground(WHITE_PURE);
        topDivider.setPreferredSize(new Dimension(0, 1));
        panel.add(topDivider, BorderLayout.NORTH);
        
        // Contenedor
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG_PRIMARY);
        container.setBorder(new EmptyBorder(20, 32, 20, 32));
        
        // Etiqueta superior
        JLabel topLabel = new JLabel("┌─ REGISTROS DE ACTIVIDAD ─────────────────────────────────────────┐");
        topLabel.setFont(FONT_LABEL);
        topLabel.setForeground(WHITE_PURE);
        topLabel.setOpaque(false);
        
        // Area de logs
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(FONT_LOGS);
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(WHITE_LIGHT);
        logArea.setCaretColor(WHITE_LIGHT);
        logArea.setLineWrap(true);
        logArea.setMargin(new Insets(16, 16, 16, 16));
        
        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setBackground(new Color(25, 25, 25));
        scrollBar.setForeground(GRAY_MEDIUM);
        scrollBar.setPreferredSize(new Dimension(12, 0));
        scrollBar.setUnitIncrement(5);
        
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        
        // Contenedor de logs
        JPanel logContainer = new JPanel(new BorderLayout());
        logContainer.setBackground(BG_PRIMARY);
        logContainer.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, WHITE_LIGHT));
        logContainer.add(scrollPane, BorderLayout.CENTER);
        
        // Estructura principal
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_PRIMARY);
        mainContainer.add(topLabel, BorderLayout.NORTH);
        mainContainer.add(logContainer, BorderLayout.CENTER);
        
        JLabel bottomLabel = new JLabel("└────────────────────────────────────────────────────────────────┘");
        bottomLabel.setFont(FONT_LABEL);
        bottomLabel.setForeground(WHITE_PURE);
        bottomLabel.setOpaque(false);
        bottomLabel.setBorder(new EmptyBorder(12, 0, 0, 0));
        mainContainer.add(bottomLabel, BorderLayout.SOUTH);
        
        container.add(mainContainer, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);
        
        // Línea inferior
        JPanel bottomDivider = new JPanel();
        bottomDivider.setBackground(WHITE_PURE);
        bottomDivider.setPreferredSize(new Dimension(0, 1));
        panel.add(bottomDivider, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Panel de botones
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        buttonPanel.setBackground(BG_SECONDARY);
        buttonPanel.setOpaque(false);
        
        buttonPanel.add(createButton("[ PURGAR REGISTROS ]", WHITE_PURE, GRAY_MEDIUM, e -> clearLogs()));
        buttonPanel.add(createButton("[ EXTRAER DATOS ]", WHITE_PURE, GRAY_MEDIUM, e -> downloadLogs()));
        
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createMatteBorder(10, 0, 10, 0, BG_SECONDARY));
        
        return panel;
    }
    
    // Botón con hover
    private JButton createButton(String text, Color primary, Color secondary, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(primary);
        button.setBackground(BG_SECONDARY);
        button.setBorder(BorderFactory.createLineBorder(primary, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setMargin(new Insets(10, 20, 10, 20));
        button.addActionListener(action);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(primary);
                button.setForeground(BG_PRIMARY);
                button.setBorder(BorderFactory.createLineBorder(secondary, 1));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BG_SECONDARY);
                button.setForeground(primary);
                button.setBorder(BorderFactory.createLineBorder(primary, 1));
            }
            public void mousePressed(java.awt.event.MouseEvent e) {
                button.setBackground(secondary);
                button.setForeground(WHITE_PURE);
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                button.setBackground(primary);
                button.setForeground(BG_PRIMARY);
            }
        });
        
        return button;
    }
    
    // Agregar log
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String formatted = message.startsWith(">") || message.startsWith("[") 
                ? message 
                : "> " + message;
            logArea.append(formatted + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    // Limpiar logs
    private void clearLogs() {
        if (JOptionPane.showConfirmDialog(this,
            "¿ESTÁ SEGURO DE PURGAR TODOS LOS REGISTROS?\nESTA ACCIÓN NO SE PUEDE DESHACER.",
            "CONFIRMACIÓN REQUERIDA",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            logArea.setText("");
            addLog("[" + getCurrentTime() + "] > Registros purgados con éxito");
        }
    }
    
    // Exportar logs
    private void downloadLogs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("EXTRAER DATOS DEL SISTEMA");
        fileChooser.setSelectedFile(new java.io.File("doncey_logs_" + System.currentTimeMillis() + ".txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                    writer.write("═══════════════════════════════════════════════════════\n");
                    writer.write("DONKEY KONG JR - LOGS\n");
                    writer.write("Exportado: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                    writer.write("═══════════════════════════════════════════════════════\n\n");
                    writer.write(logArea.getText());
                }
                addLog("[" + getCurrentTime() + "] > Datos extraídos: " + file.getName());
            } catch (Exception e) {
                addLog("[" + getCurrentTime() + "] > Error en extracción: " + e.getMessage());
            }
        }
    }
    
    // Obtener hora
    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}