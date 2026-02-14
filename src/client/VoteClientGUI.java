package client;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;


    private final JFrame frame;
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField codeField;
    private final JButton connectButton;
    private final JButton loginButton;
    private final JButton disconnectButton;
    private final JTextArea logArea;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String electeurCode;
    private boolean connecte = false;
    private boolean authentifie = false;

        frame = new JFrame("Système de vote - Client GUI");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        portField   = new JTextField("5000", 10);
        codeField   = new JTextField(20);
        connectButton     = new JButton("Se connecter");
        loginButton       = new JButton("S'authentifier");
        disconnectButton  = new JButton("Déconnexion");

        candidatsModel = new DefaultListModel<>();
        candidatsList  = new JList<>(candidatsModel);
        candidatsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        connectButton.addActionListener(e -> connecter());
        loginButton.addActionListener(e -> authentifier());
        refreshCandidatsButton.addActionListener(e -> chargerCandidats());

        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        topPanel.add(buildConnectionPanel());
        topPanel.add(buildLoginPanel());


        root.add(topPanel, BorderLayout.NORTH);
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

        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Authentification"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        return panel;
    }

        return panel;
    }

        return panel;
    }

    private void connecter() {
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

    private void authentifier() {
        String code = codeField.getText().trim();
        electeurCode = code;
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.LOGIN, electeurCode).toString());
            if (response.startsWith(Protocol.LOGIN_SUCCESS)) {
                authentifie = true;
                log("Authentification réussie.");
            } else if (response.contains(Protocol.CODE_INVALIDE)) {
                log("Code électeur invalide.");
            } else if (response.contains(Protocol.DEJA_VOTE)) {
            } else {
                log("Erreur : " + response);
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void chargerCandidats() {
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

        String selection = candidatsList.getSelectedValue();
        if (selection == null || selection.isEmpty()) {
            log("Sélectionnez un candidat.");
            return;
        }
        String candidatIdStr = selection.split("-")[0].trim();
        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.VOTE, electeurCode, candidatIdStr).toString());
            if (response.startsWith(Protocol.VOTE_ACCEPTED)) {
            } else if (response.contains(Protocol.CANDIDAT_INVALIDE)) {
                log("Candidat invalide.");
            } else {
                log("Vote refusé : " + response);
            }
        }).start();
    }

        new Thread(() -> {
            String response = envoyerMessage(new Message(Protocol.GET_RESULTS).toString());
            if (response.startsWith(Protocol.RESULTS)) {
                String resultats = response.substring(Protocol.RESULTS.length() + 1);
            } else {
                log("Erreur résultats : " + response);
            }
        }).start();
    }

    private Map<Integer, String> parseCandidats(String list) {
        Map<Integer, String> map = new LinkedHashMap<>();
        return map;
    }
        String[] items = list.split("\\|");
        for (String item : items) {
            String[] parts = item.split(":");
            if (parts.length == 2) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    map.put(id, parts[1].trim());
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