package com.doncey.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel principal de jugadores
 * Usa CardLayout para cambiar entre:
 * 1. Lista de jugadores
 * 2. Panel de administración de un jugador
 */
public class PlayersPanel extends JPanel {
    
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    private JPanel playersListPanel;
    private JList<String> playersList;
    private DefaultListModel<String> playersModel;
    private JLabel headerCountLabel;
    private List<Integer> playerIds; // Mantener track de IDs
    
    // Map para guardar paneles de administración por ID de jugador
    private Map<Integer, AdminPlayerPanel> adminPanelsMap;
    
    // AdminPlayerPanel actual
    private AdminPlayerPanel currentAdminPanel;
    
    // Colores
    private static final Color BG_PRIMARY = new Color(15, 15, 15);
    private static final Color BG_SECONDARY = new Color(35, 35, 35);
    private static final Color WHITE_LIGHT = new Color(240, 240, 240);
    private static final Color WHITE_PURE = new Color(255, 255, 255);
    private static final Color COLOR_ACCENT = new Color(100, 150, 255);
    
    // Fuentes
    private static final Font FONT_LABEL = new Font("Courier New", Font.BOLD, 13);
    private static final Font FONT_VALUE = new Font("Courier New", Font.BOLD, 15);
    private static final Font FONT_LIST = new Font("Courier New", Font.PLAIN, 14);
    
    public PlayersPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        
        // Inicializar datos
        playerIds = new ArrayList<>();
        playersModel = new DefaultListModel<>();
        adminPanelsMap = new HashMap<>();
        
        // CardLayout para cambiar entre vistas
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_PRIMARY);
        
        // Crear vista de lista de jugadores
        playersListPanel = createPlayersListView();
        cardPanel.add(playersListPanel, "LIST");
        
        // Agregar al panel principal
        add(cardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createPlayersListView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PRIMARY);
        
        // Header
        panel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Contenedor de la lista
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG_PRIMARY);
        container.setBorder(new EmptyBorder(20, 32, 20, 32));
        
        // Etiqueta superior
        JLabel topLabel = new JLabel("┌─ JUGADORES ACTIVOS ─────────────────────────────────────────────┐");
        topLabel.setFont(FONT_LABEL);
        topLabel.setForeground(WHITE_PURE);
        topLabel.setOpaque(false);
        topLabel.setBorder(new EmptyBorder(0, 0, 12, 0));
        
        // Lista de jugadores
        playersList = new JList<>(playersModel);
        playersList.setFont(FONT_LIST);
        playersList.setBackground(new Color(20, 20, 20));
        playersList.setForeground(WHITE_LIGHT);
        playersList.setSelectionBackground(COLOR_ACCENT);
        playersList.setSelectionForeground(BG_PRIMARY);
        playersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playersList.setBorder(new EmptyBorder(16, 16, 16, 16));
        
        // Listener para cuando se hace click en un jugador
        playersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && playersList.getSelectedIndex() != -1) {
                int selectedIndex = playersList.getSelectedIndex();
                Integer playerId = playerIds.get(selectedIndex);
                showAdminPanel(playerId);
            }
        });
        
        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(playersList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(new Color(20, 20, 20));
        scrollPane.setBorder(BorderFactory.createLineBorder(WHITE_LIGHT, 2));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setBackground(new Color(25, 25, 25));
        scrollBar.setForeground(new Color(100, 100, 100));
        scrollBar.setPreferredSize(new Dimension(12, 0));
        
        // Etiqueta inferior
        JLabel bottomLabel = new JLabel("└────────────────────────────────────────────────────────────────┘");
        bottomLabel.setFont(FONT_LABEL);
        bottomLabel.setForeground(WHITE_PURE);
        bottomLabel.setOpaque(false);
        bottomLabel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        // Estructura
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(BG_PRIMARY);
        listPanel.setOpaque(false);
        listPanel.add(topLabel, BorderLayout.NORTH);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(bottomLabel, BorderLayout.SOUTH);
        
        container.add(listPanel, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Crear el panel de header
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_SECONDARY);
        panel.setBorder(new EmptyBorder(20, 32, 20, 32));
        
        headerCountLabel = new JLabel("JUGADORES CONECTADOS: " + playersModel.getSize());
        headerCountLabel.setFont(FONT_VALUE);
        headerCountLabel.setForeground(WHITE_LIGHT);
        
        panel.add(headerCountLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Agregar un jugador a la lista
     * 
     * @param playerId ID del jugador que se conectó
     */
    public void addPlayer(Integer playerId) {
        SwingUtilities.invokeLater(() -> {
            if (!playerIds.contains(playerId)) {
                playerIds.add(playerId);
                playersModel.addElement("JUGADOR #" + playerId);
                updateHeaderCount();
            }
        });
    }
    
    /**
     * Eliminar un jugador de la lista
     * 
     * @param playerId ID del jugador que se desconectó
     */
    public void removePlayer(Integer playerId) {
        SwingUtilities.invokeLater(() -> {
            int index = playerIds.indexOf(playerId);
            if (index != -1) {
                playerIds.remove(index);
                playersModel.remove(index);
                updateHeaderCount();
                
                // Eliminar el panel del map
                adminPanelsMap.remove(playerId);
                
                // Si estábamos en el panel admin de este jugador, volver a la lista
                if (currentAdminPanel != null && currentAdminPanel.getPlayerId().equals(playerId)) {
                    showPlayersList();
                }
            }
        });
    }
    
    /**
     * Mostrar el panel de administración de un jugador
     * 
     * @param playerId ID del jugador 
     */
    private void showAdminPanel(Integer playerId) {
        // Verificar si ya existe un panel para este jugador
        if (adminPanelsMap.containsKey(playerId)) {
            // Recuperar el panel existente
            currentAdminPanel = adminPanelsMap.get(playerId);
            cardLayout.show(cardPanel, "ADMIN_" + playerId);
        } else {
            // Crear nuevo AdminPlayerPanel para este jugador
            currentAdminPanel = new AdminPlayerPanel(playerId, this);
            adminPanelsMap.put(playerId, currentAdminPanel);
            
            // Agregar al cardPanel
            cardPanel.add(currentAdminPanel, "ADMIN_" + playerId);
            cardLayout.show(cardPanel, "ADMIN_" + playerId);
        }
    }
    
    public void removeFruitFromAdminPanel(int fruitId) {
    if (currentAdminPanel != null) {
        currentAdminPanel.removeFruitById(fruitId);
    }
}

    // Volver a la lista de jugadores
    public void showPlayersList() {
        playersList.clearSelection();
        cardLayout.show(cardPanel, "LIST");
        currentAdminPanel = null;
    }
    
    private void updateHeaderCount() {
        if (headerCountLabel != null) {
            headerCountLabel.setText("JUGADORES CONECTADOS: " + playersModel.getSize());
        }
    }
}