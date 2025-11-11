package com.doncey.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel de administración de un jugador específico
 * Permite crear y eliminar frutas y cocodrilos para ese jugador
 */
public class AdminPlayerPanel extends JPanel {
    
    private Integer playerId;
    private PlayersPanel playersPanel;
    
    // Colores
    private static final Color BG_PRIMARY = new Color(15, 15, 15);
    private static final Color BG_SECONDARY = new Color(35, 35, 35);
    private static final Color WHITE_LIGHT = new Color(240, 240, 240);
    private static final Color WHITE_PURE = new Color(255, 255, 255);
    private static final Color GRAY_MEDIUM = new Color(100, 100, 100);
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    private static final Color COLOR_ACCENT = new Color(100, 150, 255);
    
    // Fuentes
    private static final Font FONT_TITLE = new Font("Courier New", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Courier New", Font.BOLD, 13);
    private static final Font FONT_BUTTON = new Font("Courier New", Font.BOLD, 12);
    private static final Font FONT_LIST = new Font("Courier New", Font.PLAIN, 12);
    
    // Componentes
    private JList<String> fruitsList;
    private JList<String> crocodilesList;
    private JButton deleteFruitButton;
    private DefaultListModel<String> fruitsModel;
    private DefaultListModel<String> crocodilesModel;
    
    /**
     * Constructor del panel de administración
     * 
     * @param playerId ID del jugador a administrar
     * @param playersPanel Referencia al panel de jugadores para volver
     */
    public AdminPlayerPanel(Integer playerId, PlayersPanel playersPanel) {
        this.playerId = playerId;
        this.playersPanel = playersPanel;
        
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
    }
    
    /**
     * Crea el panel de encabezado
     * Contiene el título con el ID del jugador y el botón volver
     * 
     * @return Panel de encabezado configurado
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));
        
        JLabel titleLabel = new JLabel("ADMINISTRACIÓN JUGADOR #" + playerId);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(WHITE_PURE);
        
        JButton backButton = createButton("[ VOLVER ]", WHITE_PURE, e -> playersPanel.showPlayersList());
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(backButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crea el panel de contenido principal
     * Organiza las secciones de frutas y cocodrilos lado a lado
     * 
     * @return Panel de contenido configurado
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));
        
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 30, 0));
        mainContent.setBackground(BG_PRIMARY);
        mainContent.setOpaque(false);
        
        mainContent.add(createFruitsSection());
        mainContent.add(createCrocodilesSection());
        
        panel.add(mainContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crea la sección de frutas
     * Incluye botones de creación, lista y botón eliminar
     * 
     * @return Panel de frutas configurado
     */
    private JPanel createFruitsSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("FRUTAS");
        titleLabel.setFont(FONT_LABEL);
        titleLabel.setForeground(WHITE_PURE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JPanel createButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        createButtonsPanel.setBackground(BG_SECONDARY);
        createButtonsPanel.setOpaque(false);
        
        createButtonsPanel.add(createSmallButton("[ + MANGO ]", e -> showItemDialog("MANGO", "FRUTA")));
        createButtonsPanel.add(createSmallButton("[ + BANANO ]", e -> showItemDialog("BANANO", "FRUTA")));
        createButtonsPanel.add(createSmallButton("[ + MANZANA ]", e -> showItemDialog("MANZANA", "FRUTA")));
        
        fruitsModel = new DefaultListModel<>();
        fruitsList = new JList<>(fruitsModel);
        fruitsList.setFont(FONT_LIST);
        fruitsList.setBackground(new Color(25, 25, 25));
        fruitsList.setForeground(WHITE_LIGHT);
        fruitsList.setSelectionBackground(COLOR_ACCENT);
        fruitsList.setSelectionForeground(BG_PRIMARY);
        fruitsList.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane fruitsScroll = new JScrollPane(fruitsList);
        fruitsScroll.setBackground(new Color(20, 20, 20));
        fruitsScroll.setBorder(BorderFactory.createLineBorder(WHITE_LIGHT, 1));
        fruitsScroll.setOpaque(false);
        fruitsScroll.getViewport().setOpaque(false);
        
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        deletePanel.setBackground(BG_SECONDARY);
        deletePanel.setOpaque(false);
        deletePanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        deleteFruitButton = createSmallButton("[ - ELIMINAR ]", e -> deleteFruit());
        deleteFruitButton.setEnabled(false);
        deletePanel.add(deleteFruitButton);
        
        fruitsList.addListSelectionListener(e -> {
            deleteFruitButton.setEnabled(fruitsList.getSelectedIndex() != -1);
        });
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_SECONDARY);
        centerPanel.setOpaque(false);
        centerPanel.add(createButtonsPanel, BorderLayout.NORTH);
        centerPanel.add(fruitsScroll, BorderLayout.CENTER);
        centerPanel.add(deletePanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crea la sección de cocodrilos
     * Incluye botones de creación y lista de cocodrilos
     * 
     * @return Panel de cocodrilos configurado
     */
    private JPanel createCrocodilesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("COCODRILOS");
        titleLabel.setFont(FONT_LABEL);
        titleLabel.setForeground(WHITE_PURE);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JPanel createButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        createButtonsPanel.setBackground(BG_SECONDARY);
        createButtonsPanel.setOpaque(false);
        
        createButtonsPanel.add(createSmallButton("[ + ROJO ]", e -> showItemDialog("ROJO", "COCODRILO")));
        createButtonsPanel.add(createSmallButton("[ + AZUL ]", e -> showItemDialog("AZUL", "COCODRILO")));
        
        crocodilesModel = new DefaultListModel<>();
        crocodilesList = new JList<>(crocodilesModel);
        crocodilesList.setFont(FONT_LIST);
        crocodilesList.setBackground(new Color(25, 25, 25));
        crocodilesList.setForeground(WHITE_LIGHT);
        crocodilesList.setSelectionBackground(COLOR_ACCENT);
        crocodilesList.setSelectionForeground(BG_PRIMARY);
        crocodilesList.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane crocodilesScroll = new JScrollPane(crocodilesList);
        crocodilesScroll.setBackground(new Color(20, 20, 20));
        crocodilesScroll.setBorder(BorderFactory.createLineBorder(WHITE_LIGHT, 1));
        crocodilesScroll.setOpaque(false);
        crocodilesScroll.getViewport().setOpaque(false);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_SECONDARY);
        centerPanel.setOpaque(false);
        centerPanel.add(createButtonsPanel, BorderLayout.NORTH);
        centerPanel.add(crocodilesScroll, BorderLayout.CENTER);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crea un botón con estilo personalizado
     * 
     * @param text Texto del botón
     * @param color Color del botón
     * @param action Acción al hacer click
     * @return Botón configurado
     */
    private JButton createButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(color);
        button.setBackground(BG_SECONDARY);
        button.setBorder(BorderFactory.createLineBorder(color, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setMargin(new Insets(8, 16, 8, 16));
        button.addActionListener(action);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(color);
                button.setForeground(BG_PRIMARY);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BG_SECONDARY);
                button.setForeground(color);
            }
        });
        
        return button;
    }
    
    /**
     * Crea un botón pequeño con estilo personalizado
     * 
     * @param text Texto del botón
     * @param action Acción al hacer click
     * @return Botón pequeño configurado
     */
    private JButton createSmallButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(WHITE_LIGHT);
        button.setBackground(BG_PRIMARY);
        button.setBorder(BorderFactory.createLineBorder(WHITE_LIGHT, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.addActionListener(action);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(WHITE_LIGHT);
                button.setForeground(BG_PRIMARY);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(BG_PRIMARY);
                button.setForeground(WHITE_LIGHT);
            }
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(COLOR_ACCENT);
                }
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(WHITE_LIGHT);
                }
            }
        });
        
        return button;
    }
    
    /**
     * Muestra un diálogo genérico para ingresar altura X y Y
     * Se utiliza para frutas y cocodrilos
     * 
     * @param itemType Tipo de elemento (MANGO, ROJO, etc)
     * @param category Categoría (FRUTA o COCODRILO)
     */
    private void showItemDialog(String itemType, String category) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel labelX = new JLabel("Altura X:");
        JTextField fieldX = new JTextField(5);
        JLabel labelY = new JLabel("Altura Y:");
        JTextField fieldY = new JTextField(5);
        
        panel.add(labelX);
        panel.add(fieldX);
        panel.add(labelY);
        panel.add(fieldY);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Crear " + category + ": " + itemType,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                int heightX = Integer.parseInt(fieldX.getText());
                int heightY = Integer.parseInt(fieldY.getText());
                
                if (heightX <= 0 || heightY <= 0) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Las alturas deben ser números positivos",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                if (category.equals("FRUTA")) {
                    addFruit(itemType, heightX, heightY);
                } else if (category.equals("COCODRILO")) {
                    addCrocodile(itemType, heightX, heightY);
                }
                
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Por favor ingresa números válidos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    
    /**
     * Agrega una fruta a la lista
     * 
     * @param type Tipo de fruta
     * @param heightX Altura X
     * @param heightY Altura Y
     */
    private void addFruit(String type, int heightX, int heightY) {
        String fruitEntry = type + " (X:" + heightX + " Y:" + heightY + ")";
        fruitsModel.addElement(fruitEntry);
    }
    
    // Elimina la fruta seleccionada de la lista
    private void deleteFruit() {
        int selectedIndex = fruitsList.getSelectedIndex();
        if (selectedIndex != -1) {
            fruitsModel.remove(selectedIndex);
            deleteFruitButton.setEnabled(false);
        }
    }
    
    /**
     * Agrega un cocodrilo a la lista
     * 
     * @param type Tipo de cocodrilo
     * @param heightX Altura X
     * @param heightY Altura Y
     */
    private void addCrocodile(String type, int heightX, int heightY) {
        String crocodileEntry = type + " (X:" + heightX + " Y:" + heightY + ")";
        crocodilesModel.addElement(crocodileEntry);
    }
    
    /**
     * Retorna el ID del jugador
     * 
     * @return ID del jugador
     */
    public Integer getPlayerId() {
        return playerId;
    }
}