package client;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import reseaux.Message;
import reseaux.Protocol;

public class VoteClientGUI 
{

    private final JFrame frame;
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField codeField;
    private final JButton connectButton;
    private final JButton loginButton;
    private final JButton disconnectButton;
    private final JTextArea logArea;
    private final String ipHote = "192.168.78.90";

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String electeurCode;
    private boolean connecte = false;
    private boolean authentifie = false;

  
    private final DefaultListModel<String> candidatsModel;
    private final JList<String> candidatsList;
    private final JButton refreshCandidatsButton;
    private final JButton voteButton;
    private final JButton resultsButton;

    //onglet "Sondages"
    private final DefaultListModel<String> pollsModel;          // Liste des sondages
    private final JList<String> pollsList;
    private final JButton refreshPollsButton;
    private final JButton createPollButton;
    private final JButton selectPollButton;                    
    private final JButton votePollButton;                      // Voter dans le sondage courant
    private final JButton resultsPollButton;                   

    private final DefaultListModel<String> optionsModel;     
    private final JList<String> optionsList;
    private final JLabel selectedPollLabel;                    

    private int currentPollId = -1;                          
    private String currentPollTitle = "";

    public VoteClientGUI() 
    {
 
        frame = new JFrame("Système de vote - Client GUI");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(780, 600);
        frame.setLocationRelativeTo(null);

        serverField = new JTextField(ipHote, 15);
        portField   = new JTextField("5000", 10);
        codeField   = new JTextField(20);
        connectButton     = new JButton("Se connecter");
        loginButton       = new JButton("S'authentifier");
        disconnectButton  = new JButton("Déconnexion");

        logArea = new JTextArea(10, 70);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(750, 150));

        // --- Modèles pour les listes ---
        candidatsModel = new DefaultListModel<>();
        candidatsList  = new JList<>(candidatsModel);
        candidatsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        pollsModel     = new DefaultListModel<>();
        pollsList      = new JList<>(pollsModel);
        pollsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        optionsModel   = new DefaultListModel<>();
        optionsList    = new JList<>(optionsModel);
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //classique ---
        refreshCandidatsButton = new JButton("Charger candidats");
        voteButton             = new JButton("Voter");
        resultsButton          = new JButton("Résultats");

        // --- onglet Sondages ---
        refreshPollsButton = new JButton("Actualiser sondages");
        createPollButton   = new JButton("Créer un sondage");
        selectPollButton   = new JButton("Choisir ce sondage");
        votePollButton     = new JButton("Voter (sondage)");
        resultsPollButton  = new JButton("Résultats (sondage)");
        selectedPollLabel  = new JLabel("Aucun sondage sélectionné");

      
        connectButton.addActionListener(e -> connecter());
        loginButton.addActionListener(e -> authentifier());
        disconnectButton.addActionListener(e -> deconnecter());
     
        refreshCandidatsButton.addActionListener(e -> chargerCandidats());
        voteButton.addActionListener(e -> voterClassique());
        resultsButton.addActionListener(e -> afficherResultatsClassiques());

        refreshPollsButton.addActionListener(e -> chargerListeSondages());
        createPollButton.addActionListener(e -> creerSondage());
        selectPollButton.addActionListener(e -> 
        {
            int idx = pollsList.getSelectedIndex();
            if (idx != -1) 
            {
                String selection = pollsModel.get(idx);
                // format attendu : "id: titre"
                String[] parts = selection.split(":", 2);
                try {
                    currentPollId = Integer.parseInt(parts[0].trim());
                    currentPollTitle = parts[1].trim();
                    selectedPollLabel.setText("Sondage actif : " + currentPollTitle);
                    chargerOptionsSondage(currentPollId);
                    log("Sondage sélectionné : " + currentPollTitle);

                    updateUiState();
                } catch (NumberFormatException ex) {
                    log("Erreur : format de sondage invalide");
                }
            } else {
                log("Veuillez sélectionner un sondage dans la liste.");
            }
        });


        votePollButton.addActionListener(e -> voterSondage());
        resultsPollButton.addActionListener(e -> afficherResultatsSondage());

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panneau du haut : connexion + authentification
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        topPanel.add(buildConnectionPanel());
        topPanel.add(buildLoginPanel());

        // Panneau central avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Vote classique", buildClassicVotePanel());
        tabbedPane.addTab("Sondages", buildPollPanel());

        root.add(topPanel, BorderLayout.NORTH);
        root.add(tabbedPane, BorderLayout.CENTER);
        root.add(logScroll, BorderLayout.SOUTH);

        frame.setContentPane(root);
        updateUiState();

        frame.addWindowListener(new WindowAdapter() 
        {
            @Override
            public void windowClosing(WindowEvent e) {
                deconnecter();
            }
        });
    }

    // ------------------------------------------------------------
    // Construction des sous-panneaux
    // ------------------------------------------------------------
    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Connexion"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Serveur"), c);
        c.gridx = 1; c.weightx = 1; panel.add(serverField, c);
        c.gridx = 2; c.weightx = 0; panel.add(new JLabel("Port"), c);
        c.gridx = 3; c.weightx = 0.4; panel.add(portField, c);
        c.gridx = 4; c.weightx = 0; panel.add(connectButton, c);
        c.gridx = 5; panel.add(disconnectButton, c);
        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Authentification"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Code électeur"), c);
        c.gridx = 1; c.weightx = 1; panel.add(codeField, c);
        c.gridx = 2; c.weightx = 0; panel.add(loginButton, c);
        return panel;
    }

    private JPanel buildClassicVotePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Liste des candidats
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("Candidats"));
        center.add(new JScrollPane(candidatsList), BorderLayout.CENTER);

        // Boutons d'action
        JPanel actions = new JPanel(new GridLayout(3, 1, 5, 5));
        actions.setBorder(BorderFactory.createTitledBorder("Actions"));
        actions.add(refreshCandidatsButton);
        actions.add(voteButton);
        actions.add(resultsButton);

        panel.add(center, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildPollPanel() 
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(BorderFactory.createTitledBorder("Sondages disponibles"));
        left.add(new JScrollPane(pollsList), BorderLayout.CENTER);
        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        leftButtons.add(refreshPollsButton);
        leftButtons.add(createPollButton);
        left.add(leftButtons, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(BorderFactory.createTitledBorder("Options du sondage"));
        right.add(new JScrollPane(optionsList), BorderLayout.CENTER);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(selectPollButton);
        selectionPanel.add(selectedPollLabel);
        right.add(selectionPanel, BorderLayout.NORTH);

        JPanel rightButtons = new JPanel(new GridLayout(2, 1, 5, 5));
        rightButtons.add(votePollButton);
        rightButtons.add(resultsPollButton);
        right.add(rightButtons, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    //Communications réseau
    private void connecter() 
    {
        if (connecte) { log("Déjà connecté."); return; }
        String server = serverField.getText().trim();
        String portStr = portField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            log("Port invalide.");
            return;
        }
        new Thread(() -> {
            try {
                socket = new Socket(server, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                connecte = true;
                authentifie = false;
                log("Connecté à " + server + ":" + port);
            } catch (IOException e) {
                log("Erreur connexion : " + e.getMessage());
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void authentifier() 
    {
        if (!connecte) { log("Connectez-vous d'abord."); return; }
        String code = codeField.getText().trim();
        if (code.isEmpty()) { log("Code électeur vide."); return; }
        electeurCode = code;
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.LOGIN, electeurCode).toString());
            if (response.startsWith(Protocol.LOGIN_SUCCESS)) {
                authentifie = true;
                log("Authentification réussie.");
            } else if (response.contains(Protocol.CODE_INVALIDE)) {
                log("Code électeur invalide.");
            } else if (response.contains(Protocol.DEJA_VOTE)) {
                log("Vous avez déjà voté (vote classique).");
            } else {
                log("Erreur : " + response);
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void deconnecter() 
    {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
        connecte = false;
        authentifie = false;
        currentPollId = -1;
        currentPollTitle = "";
        selectedPollLabel.setText("Aucun sondage sélectionné");
        SwingUtilities.invokeLater(this::updateUiState);
        log("Déconnecté.");
    }

    private String envoyerMessage(String message) 
    {
        try {
            if (out == null || in == null) 
            {
                connecte = false;
                authentifie = false;
                SwingUtilities.invokeLater(this::updateUiState);
                return "ERROR|SESSION_FERMEE";
            }
    
            out.println(message);
            String response = in.readLine();
    
            if (response == null) 
            {
                // La connexion est probablement fermée côté serveur
                connecte = false;
                authentifie = false;
                SwingUtilities.invokeLater(this::updateUiState);
                return "ERROR|SESSION_FERMEE";
            }
    
            return response;
    
        } 
        catch (IOException e) 
        {
            connecte = false;
            authentifie = false;
            SwingUtilities.invokeLater(this::updateUiState);
            log("Erreur de communication : " + e.getMessage());
            return "ERROR|" + e.getMessage();
        }
    }


    private void chargerCandidats() {
        if (!connecte) { log("Connectez-vous d'abord."); return; }
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.GET_CANDIDATS).toString());
            if (response.startsWith(Protocol.CANDIDATS_LIST)) {
                String list = response.substring(Protocol.CANDIDATS_LIST.length() + 1);
                Map<Integer, String> candidats = parseCandidats(list);
                SwingUtilities.invokeLater(() -> {
                    candidatsModel.clear();
                    for (Map.Entry<Integer, String> entry : candidats.entrySet()) {
                        candidatsModel.addElement(entry.getKey() + " - " + entry.getValue());
                    }
                });
                log("Candidats chargés.");
            } else {
                log("Erreur chargement candidats : " + response);
            }
        }).start();
    }

    private void voterClassique() 
    {
        if (!authentifie) { log("Authentifiez-vous d'abord."); return; }
        String selection = candidatsList.getSelectedValue();
        if (selection == null || selection.isEmpty()) {
            log("Sélectionnez un candidat.");
            return;
        }
        String candidatIdStr = selection.split("-")[0].trim();
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.VOTE, electeurCode, candidatIdStr).toString());
            if (response.startsWith(Protocol.VOTE_ACCEPTED)) {
                log("Vote enregistré (classique).");
            } else if (response.contains(Protocol.CANDIDAT_INVALIDE)) {
                log("Candidat invalide.");
            } else {
                log("Vote refusé : " + response);
            }
        }).start();
    }

    private void afficherResultatsClassiques() 
    {
        if (!connecte) { log("Connectez-vous d'abord."); return; }
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.GET_RESULTS).toString());
            if (response.startsWith(Protocol.RESULTS)) {
                String resultats = response.substring(Protocol.RESULTS.length() + 1);
                log("Résultats classiques : " + resultats.replace("|", ", "));
            } else {
                log("Erreur résultats : " + response);
            }
        }).start();
    }

    private Map<Integer, String> parseCandidats(String list)
     {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) return map;
        String[] items = list.split("\\|");
        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split(":");
            if (parts.length == 2) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    map.put(id, parts[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    // ------------------------------------------------------------
    // Fonctionnalités des sondages
    // ------------------------------------------------------------
    private void chargerListeSondages() {

        if (!connecte) { 
            log("Connectez-vous d'abord."); 
            return; 
        }
    
        refreshPollsButton.setEnabled(false);
    
        new Thread(() -> {
    
            String response = envoyerMessage(
                    new Message(Protocol.GET_POLLS).toString()
            );
    
            if (response.startsWith(Protocol.POLLS_LIST)) {
    
                String list = response.substring(
                        Protocol.POLLS_LIST.length() + 1
                );
    
                SwingUtilities.invokeLater(() -> {
    
                    pollsModel.clear();
    
                    if (list.isEmpty()) {
                        pollsModel.addElement("Aucun sondage disponible");
                    } else {
                        String[] polls = list.split("\\|");
                        for (String p : polls) {
                            if (!p.trim().isEmpty()) {
                                pollsModel.addElement(p.trim());
                            }
                        }
                    }
    
                    // Réinitialisation propre
                    currentPollId = -1;
                    currentPollTitle = "";
                    selectedPollLabel.setText("Aucun sondage sélectionné");
    
                    refreshPollsButton.setEnabled(true);
                    updateUiState();
                });
    
                log("Liste des sondages reçue.");
    
            } else {
                SwingUtilities.invokeLater(() -> {
                    refreshPollsButton.setEnabled(true);
                    updateUiState();
                });
    
                log("Erreur chargement sondages : " + response);
            }
    
        }).start();
    }

    private void chargerOptionsSondage(int pollId) {

        if (!connecte) { 
            log("Connectez-vous d'abord."); 
            return; 
        }
    
        new Thread(() -> {
    
            String response = envoyerMessage(
                    new Message(
                            Protocol.GET_OPTIONS, 
                            String.valueOf(pollId)
                    ).toString()
            );
    
            if (response.startsWith(Protocol.OPTIONS_LIST)) {
    
                String list = response.substring(
                        Protocol.OPTIONS_LIST.length() + 1
                );
    
                Map<Integer, String> opts = parseOptions(list);
    
                SwingUtilities.invokeLater(() -> {
    
                    optionsModel.clear();
    
                    for (Map.Entry<Integer, String> entry : opts.entrySet()) {
                        optionsModel.addElement(
                                entry.getKey() + " - " + entry.getValue()
                        );
                    }
    
                    updateUiState();
                });
    
                log("Options du sondage #" + pollId + " chargées.");
    
            } else {
                log("Erreur chargement options : " + response);
            }
    
        }).start();
    }

    private Map<Integer, String> parseOptions(String list) {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) return map;
        String[] items = list.split("\\|");
        for (String item : items) {
            if (item.isEmpty()) continue;
            String[] parts = item.split(":");
            if (parts.length == 2) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    map.put(id, parts[1].trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return map;
    }

    private void creerSondage() {
        if (!authentifie) { log("Vous devez être authentifié pour créer un sondage."); return; }

        // Boîte de dialogue personnalisée
        JTextField titreField = new JTextField(20);
        JTextArea optionsArea = new JTextArea(5, 20);
        optionsArea.setLineWrap(true);
        JScrollPane scrollOpt = new JScrollPane(optionsArea);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Titre du sondage :"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; panel.add(titreField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Options (une par ligne) :"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;
        panel.add(scrollOpt, gbc);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Créer un sondage",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String titre = titreField.getText().trim();
        if (titre.isEmpty()) { log("Titre vide, création annulée."); return; }

        String[] options = optionsArea.getText().split("\n");
        List<String> optsList = new ArrayList<>();
        for (String opt : options) {
            String trimmed = opt.trim();
            if (!trimmed.isEmpty()) optsList.add(trimmed);
        }
        if (optsList.isEmpty()) { log("Aucune option saisie, création annulée."); return; }

      
        List<String> parts = new ArrayList<>();
        parts.add(Protocol.CREATE_POLL);
        parts.add(electeurCode);
        parts.add(titre);
        parts.addAll(optsList);
        Message msg = new Message(parts.toArray(new String[0]));

        new Thread(() -> {
            String response = envoyerMessage(msg.toString());
            
            if (response.startsWith(Protocol.CREATE_POLL_SUCCESS)) 
            {
                String[] respParts = response.split("\\|");
                if (respParts.length >= 2) {
                    String newId = respParts[1];
                    log("Sondage créé avec succès ! ID = " + newId);
                    // Rafraîchir la liste des sondages
                    chargerListeSondages();
                } else {
                    log("Réponse inattendue du serveur.");
                }
            } else if (response.startsWith(Protocol.CREATE_POLL_FAILED)) {
                log("Échec de la création du sondage : " + response);
            } else {
                log("Erreur : " + response);
            }
        }).start();
    }

    private void voterSondage() {
        if (!authentifie) { log("Authentifiez-vous d'abord."); return; }
        if (currentPollId == -1) { log("Aucun sondage sélectionné."); return; }
        String selection = optionsList.getSelectedValue();
        if (selection == null || selection.isEmpty()) {
            log("Sélectionnez une option dans le sondage.");
            return;
        }
        String optionIdStr = selection.split("-")[0].trim();
        new Thread(() -> {
            // VOTE_POLL|code|pollId|optionId
            String response = envoyerMessage(new Message(Protocol.VOTE_POLL,
                    electeurCode, String.valueOf(currentPollId), optionIdStr).toString());
            if (response.startsWith(Protocol.VOTE_ACCEPTED)) {
                log("Vote enregistré pour le sondage \"" + currentPollTitle + "\".");
            } else if (response.contains(Protocol.DEJA_VOTE)) {
                log("Vous avez déjà voté dans ce sondage.");
            } else if (response.contains(Protocol.CANDIDAT_INVALIDE)) 
            { // ou OPTION_INVALIDE
                log("Option invalide.");
            } else {
                log("Vote refusé : " + response);
            }
        }).start();
    }

    private void afficherResultatsSondage() {
        if (!connecte) { log("Connectez-vous d'abord."); return; }
        if (currentPollId == -1) { log("Aucun sondage sélectionné."); return; }
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.GET_POLL_RESULTS,
                    String.valueOf(currentPollId)).toString());
            if (response.startsWith(Protocol.POLL_RESULTS)) {
                String resultats = response.substring(Protocol.POLL_RESULTS.length() + 1);
                log("Résultats du sondage \"" + currentPollTitle + "\" : " +
                        resultats.replace("|", ", "));
            } else {
                log("Erreur résultats sondage : " + response);
            }
        }).start();
    }

    // ------------------------------------------------------------
    // Gestion de l'état de l'interface
    // ------------------------------------------------------------
    private void updateUiState() {
        // Connexion
        connectButton.setEnabled(!connecte);
        disconnectButton.setEnabled(connecte);
        loginButton.setEnabled(connecte && !authentifie);
        codeField.setEnabled(connecte && !authentifie);

        // Onglet Vote classique
        refreshCandidatsButton.setEnabled(connecte);
        voteButton.setEnabled(connecte && authentifie);
        resultsButton.setEnabled(connecte);

        // Onglet Sondages
        refreshPollsButton.setEnabled(connecte);
        createPollButton.setEnabled(connecte && authentifie);
        selectPollButton.setEnabled(connecte && !pollsModel.isEmpty());
        votePollButton.setEnabled(connecte && authentifie && currentPollId != -1);
        resultsPollButton.setEnabled(connecte && currentPollId != -1);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void show() {
        frame.setVisible(true);
    }

    // ------------------------------------------------------------
    // Point d'entrée
    // ------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VoteClientGUI().show());
    }
}