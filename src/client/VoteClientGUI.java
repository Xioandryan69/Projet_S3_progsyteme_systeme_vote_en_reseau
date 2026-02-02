package client;

import reseaux.Message;
import reseaux.Protocol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

public class VoteClientGUI {

    private final JFrame frame;
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField codeField;
    private final JButton connectButton;
    private final JButton loginButton;
    private final JButton refreshCandidatsButton;
    private final JButton voteButton;
    private final JButton resultsButton;
    private final JButton disconnectButton;
    private final DefaultListModel<String> candidatsModel;
    private final JList<String> candidatsList;
    private final JTextArea logArea;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String electeurCode;
    private boolean connecte = false;
    private boolean authentifie = false;

    public VoteClientGUI() {
        // Initialiser TOUS les composants AVANT de les utiliser
        frame = new JFrame("Système de vote - Client GUI");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(720, 520);
        frame.setLocationRelativeTo(null);

        // Fields
        serverField = new JTextField("localhost", 15);
        portField = new JTextField("5000", 10);
        codeField = new JTextField(20);

        // Buttons
        connectButton = new JButton("Se connecter");
        loginButton = new JButton("S'authentifier");
        refreshCandidatsButton = new JButton("Charger candidats");
        voteButton = new JButton("Voter");
        resultsButton = new JButton("Résultats");
        disconnectButton = new JButton("Déconnexion");

        // Candidates list
        candidatsModel = new DefaultListModel<>();
        candidatsList = new JList<>(candidatsModel);
        candidatsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Log area
        logArea = new JTextArea(10, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(700, 120));

        // Wire buttons APRÈS création
        connectButton.addActionListener(e -> connecter());
        loginButton.addActionListener(e -> authentifier());
        refreshCandidatsButton.addActionListener(e -> chargerCandidats());
        voteButton.addActionListener(e -> voter());
        resultsButton.addActionListener(e -> afficherResultats());
        disconnectButton.addActionListener(e -> deconnecter());

        // Maintenant construire les panneaux
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        topPanel.add(buildConnectionPanel());
        topPanel.add(buildLoginPanel());

        JPanel centerPanel = new JPanel(new BorderLayout(12, 12));
        centerPanel.add(buildCandidatesPanel(), BorderLayout.CENTER);
        centerPanel.add(buildActionsPanel(), BorderLayout.EAST);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(centerPanel, BorderLayout.CENTER);
        root.add(logScroll, BorderLayout.SOUTH);

        frame.setContentPane(root);

        updateUiState();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                deconnecter();
            }
        });
    }

    private JPanel buildConnectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Connexion"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Serveur"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        panel.add(serverField, c);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(new JLabel("Port"), c);
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.4;
        panel.add(portField, c);
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(connectButton, c);
        c.gridx = 5;
        c.gridy = 0;
        panel.add(disconnectButton, c);

        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Authentification"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        panel.add(new JLabel("Code électeur"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        panel.add(codeField, c);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(loginButton, c);

        return panel;
    }

    private JPanel buildCandidatesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Candidats"));
        panel.add(new JScrollPane(candidatsList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Actions"));
        panel.add(refreshCandidatsButton);
        panel.add(voteButton);
        panel.add(resultsButton);
        return panel;
    }

    private void connecter() {
        if (connecte) {
            log("Déjà connecté.");
            return;
        }
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
                log("Erreur connexion: " + e.getMessage());
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void authentifier() {
        if (!connecte) {
            log("Connectez-vous d'abord.");
            return;
        }
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            log("Code électeur vide.");
            return;
        }
        electeurCode = code;

        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.LOGIN, electeurCode).toString());
            if (response.startsWith(Protocol.LOGIN_SUCCESS)) {
                authentifie = true;
                log("Authentification réussie.");
            } else if (response.contains(Protocol.CODE_INVALIDE)) {
                log("Code électeur invalide.");
            } else if (response.contains(Protocol.DEJA_VOTE)) {
                log("Vous avez déjà voté.");
            } else {
                log("Erreur: " + response);
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void chargerCandidats() {
        if (!connecte) {
            log("Connectez-vous d'abord.");
            return;
        }
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
                log("Erreur chargement candidats: " + response);
            }
        }).start();
    }

    private void voter() {
        if (!authentifie) {
            log("Authentifiez-vous avant de voter.");
            return;
        }
        String selection = candidatsList.getSelectedValue();
        if (selection == null || selection.isEmpty()) {
            log("Sélectionnez un candidat.");
            return;
        }
        String candidatIdStr = selection.split("-")[0].trim();

        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.VOTE, electeurCode, candidatIdStr).toString());
            if (response.startsWith(Protocol.VOTE_ACCEPTED)) {
                log("Vote enregistré.");
            } else if (response.contains(Protocol.CANDIDAT_INVALIDE)) {
                log("Candidat invalide.");
            } else {
                log("Vote refusé: " + response);
            }
        }).start();
    }

    private void afficherResultats() {
        if (!connecte) {
            log("Connectez-vous d'abord.");
            return;
        }
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.GET_RESULTS).toString());
            if (response.startsWith(Protocol.RESULTS)) {
                String resultats = response.substring(Protocol.RESULTS.length() + 1);
                log("Résultats: " + resultats.replace("|", ", "));
            } else {
                log("Erreur résultats: " + response);
            }
        }).start();
    }

    private String envoyerMessage(String message) {
        try {
            out.println(message);
            String response = in.readLine();
            return response != null ? response : "";
        } catch (IOException e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private void deconnecter() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        connecte = false;
        authentifie = false;
        SwingUtilities.invokeLater(this::updateUiState);
        log("Déconnecté.");
    }

    private Map<Integer, String> parseCandidats(String list) {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) {
            return map;
        }
        String[] items = list.split("\\|");
        for (String item : items) {
            if (item.isEmpty()) {
                continue;
            }
            String[] parts = item.split(":");
            if (parts.length == 2) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    map.put(id, parts[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return map;
    }

    private void updateUiState() {
        connectButton.setEnabled(!connecte);
        disconnectButton.setEnabled(connecte);
        loginButton.setEnabled(connecte && !authentifie);
        refreshCandidatsButton.setEnabled(connecte);
        voteButton.setEnabled(connecte && authentifie);
        resultsButton.setEnabled(connecte);
        codeField.setEnabled(connecte && !authentifie);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VoteClientGUI().show());
    }
}
