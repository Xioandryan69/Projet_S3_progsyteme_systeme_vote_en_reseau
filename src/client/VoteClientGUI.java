package client;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import com.formdev.flatlaf.FlatLightLaf;
import reseaux.Message;
import reseaux.Protocol;

public class VoteClientGUI {
    private static final Color C_PRIMARY    = new Color(41,  128, 185);
    private static final Color C_SUCCESS    = new Color(39,  174,  96);
    private static final Color C_DANGER     = new Color(192,  57,  43);
    private static final Color C_WARN       = new Color(211,  84,   0);
    private static final Color C_BG         = new Color(245, 246, 250);
    private static final Color C_PANEL      = Color.WHITE;
    private static final Color C_SEL_BLUE   = new Color(52,  152, 219);
    private static final Color C_TEXT_MUTED = new Color(120, 120, 130);

    private static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font F_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_MONO   = new Font("Consolas",  Font.PLAIN, 12);

    private final JFrame     frame;
    private final JTextField serverField;
    private final JTextField portField;
    private final JTextField codeField;
    private final JButton    connectButton;
    private final JButton    loginButton;
    private final JButton    disconnectButton;
    private final JTextArea  logArea;

    private final JLabel userLabel;
    private final JLabel statusDot;
    private final JLabel statusLabel;

    private final DefaultListModel<String> candidatsModel;
    private final JList<String>            candidatsList;
    private final JButton refreshCandidatsButton;
    private final JButton voteButton;
    private final JButton resultsButton;

    private final DefaultListModel<String> pollsModel;
    private final JList<String>            pollsList;
    private final JButton refreshPollsButton;
    private final JButton createPollButton;
    private final JButton selectPollButton;
    private final JButton votePollButton;
    private final JButton resultsPollButton;
    private final JButton resultsAllButton;

    private final DefaultListModel<String> optionsModel;
    private final JList<String>            optionsList;
    private final JLabel selectedPollLabel;
    private final JLabel pollTypeLabel;

    private final Map<Integer, String> pollsMap = new LinkedHashMap<>();

    private Socket         socket;
    private BufferedReader in;
    private PrintWriter    out;
    private String         electeurCode;
    private String         electeurName     = "";
    private boolean        connecte         = false;
    private boolean        authentifie      = false;
    private int            currentPollId    = -1;
    private String         currentPollTitle = "";

    private static final String IP_HOTE = "192.168.78.90";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public VoteClientGUI() {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); }
        catch (Exception ignored) {}
        UIManager.put("Button.font",       F_NORMAL);
        UIManager.put("Label.font",        F_NORMAL);
        UIManager.put("TextField.font",    F_NORMAL);
        UIManager.put("TextArea.font",     F_NORMAL);
        UIManager.put("TabbedPane.font",   F_BOLD);
        UIManager.put("List.font",         F_NORMAL);
        UIManager.put("Button.arc",        10);
        UIManager.put("Component.arc",      8);
        UIManager.put("TextComponent.arc",  8);

        frame = new JFrame("  Système de Vote en Réseau");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(920, 700);
        frame.setMinimumSize(new Dimension(760, 560));
        frame.setLocationRelativeTo(null);

        serverField = styledTextField(IP_HOTE, 14);
        portField   = styledTextField("5000",    6);
        codeField   = styledTextField("",       18);
        codeField.setToolTipText("Entrez votre code électeur puis cliquez S'authentifier");

        connectButton    = primaryBtn("Se connecter",   C_PRIMARY);
        loginButton      = primaryBtn("S'authentifier", C_SUCCESS);
        disconnectButton = primaryBtn("Déconnexion",    C_DANGER);

        statusDot   = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        statusLabel = new JLabel("Déconnecté");
        statusLabel.setFont(F_BOLD);
        userLabel   = new JLabel("—");
        userLabel.setFont(F_NORMAL);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(F_MONO);
        logArea.setBackground(new Color(250, 251, 255));
        logArea.setForeground(new Color(35, 35, 50));
        logArea.setMargin(new Insets(6, 10, 6, 10));

        candidatsModel = new DefaultListModel<>();
        candidatsList  = styledList(candidatsModel, C_SEL_BLUE);
        pollsModel     = new DefaultListModel<>();
        pollsList      = styledList(pollsModel, C_SUCCESS);
        optionsModel   = new DefaultListModel<>();
        optionsList    = styledList(optionsModel, C_WARN);

        refreshCandidatsButton = primaryBtn("Charger candidats",  C_PRIMARY);
        voteButton             = primaryBtn("Voter",              C_SUCCESS);
        resultsButton          = primaryBtn("Résultats",          C_WARN);

        refreshPollsButton = primaryBtn("Actualiser",            C_PRIMARY);
        createPollButton   = primaryBtn("+ Créer un sondage",    C_SUCCESS);
        selectPollButton   = primaryBtn("Sélectionner",           new Color(100, 100, 200));
        votePollButton     = primaryBtn("Voter (sondage)",         C_SUCCESS);
        resultsPollButton  = primaryBtn("Résultats (ce sondage)", C_WARN);
        resultsAllButton   = primaryBtn("Résultats actuels",      C_PRIMARY);

        selectedPollLabel = new JLabel("Aucun sondage sélectionné");
        selectedPollLabel.setFont(F_BOLD);
        selectedPollLabel.setForeground(C_PRIMARY);
        pollTypeLabel = new JLabel("");
        pollTypeLabel.setFont(F_SMALL);
        pollTypeLabel.setForeground(C_TEXT_MUTED);

        pollsList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateUiState(); });

        wireListeners();
        frame.setContentPane(buildRoot());
        updateUiState();

        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { deconnecter(); }
        });
    }

    private void wireListeners() {
        connectButton.addActionListener(e    -> connecter());
        loginButton.addActionListener(e      -> authentifier());
        disconnectButton.addActionListener(e -> deconnecter());
        refreshCandidatsButton.addActionListener(e -> chargerCandidats());
        voteButton.addActionListener(e             -> voterClassique());
        resultsButton.addActionListener(e          -> afficherResultatsClassiques());
        refreshPollsButton.addActionListener(e -> chargerListeSondages());
        createPollButton.addActionListener(e   -> creerSondage());
        selectPollButton.addActionListener(e   -> selectionnerSondage());
        votePollButton.addActionListener(e     -> voterSondage());
        resultsPollButton.addActionListener(e  -> afficherResultatsSondage());
        resultsAllButton.addActionListener(e   -> afficherResultatsTousSondages());
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);
        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setBackground(C_BG);
        body.setBorder(new EmptyBorder(10, 14, 12, 14));

        JPanel topRow = new JPanel(new GridLayout(1, 2, 10, 0));
        topRow.setOpaque(false);
        topRow.add(buildConnectionCard());
        topRow.add(buildLoginCard());

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("Vote classique", buildClassicPanel());
        tabs.addTab("Sondages",       buildPollPanel());

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(titledBorder("Journal des opérations"));
        logScroll.setPreferredSize(new Dimension(0, 145));

        body.add(topRow,    BorderLayout.NORTH);
        body.add(tabs,      BorderLayout.CENTER);
        body.add(logScroll, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(C_PRIMARY);
        bar.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("  SYSTÈME DE VOTE EN RÉSEAU");
        title.setFont(F_TITLE);
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(userLabel);
        right.add(new JLabel(" "));
        right.add(statusDot);
        right.add(statusLabel);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildConnectionCard() {
        JPanel card = card("Connexion au serveur");
        GridBagConstraints c = gbc();

        // ── Bloc Serveur ──────────────────────────────────────────
        JPanel serverBlock = new JPanel(new BorderLayout(0, 3));
        serverBlock.setOpaque(false);

        JLabel serverSub = new JLabel("IP ou nom d'hôte");
        serverSub.setFont(F_SMALL);
        serverSub.setForeground(C_TEXT_MUTED);

        serverField.setToolTipText("Adresse IP ou nom d'hôte du serveur · ex: 192.168.1.10");
        serverField.setPreferredSize(new Dimension(185, 30));
        applyNeutralBorder(serverField);
        serverField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean touched = false;
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { touched = true; applyValidatedBorder(serverField, !serverField.getText().trim().isEmpty()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { if (touched) applyValidatedBorder(serverField, !serverField.getText().trim().isEmpty()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });

        serverBlock.add(serverSub,   BorderLayout.NORTH);
        serverBlock.add(serverField, BorderLayout.CENTER);

        // ── Bloc Port ─────────────────────────────────────────────
        JPanel portBlock = new JPanel(new BorderLayout(0, 3));
        portBlock.setOpaque(false);

        JLabel portSub = new JLabel("1 – 65535");
        portSub.setFont(F_SMALL);
        portSub.setForeground(C_TEXT_MUTED);

        portField.setToolTipText("Numéro de port TCP du serveur (défaut : 5000)");
        portField.setPreferredSize(new Dimension(72, 30));
        portField.setHorizontalAlignment(JTextField.CENTER);

        // Filtrer : chiffres uniquement
        ((javax.swing.text.AbstractDocument) portField.getDocument())
            .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int off, String str,
                        javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (str != null && str.matches("\\d*")) super.insertString(fb, off, str, a);
                }
                @Override
                public void replace(FilterBypass fb, int off, int len, String str,
                        javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                    if (str != null && str.matches("\\d*")) super.replace(fb, off, len, str, a);
                }
            });

        applyNeutralBorder(portField);
        portField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean touched = false;
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { touched = true; applyValidatedBorder(portField, isPortValid()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { if (touched) applyValidatedBorder(portField, isPortValid()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
        });

        portBlock.add(portSub,   BorderLayout.NORTH);
        portBlock.add(portField, BorderLayout.CENTER);

        // ── Boutons empilés ───────────────────────────────────────
        JPanel btnBlock = new JPanel(new GridLayout(2, 1, 0, 5));
        btnBlock.setOpaque(false);
        btnBlock.add(connectButton);
        btnBlock.add(disconnectButton);

        // ── Assemblage ────────────────────────────────────────────
        c.gridx = 0; c.weightx = 0;   card.add(boldLbl("Serveur"), c);
        c.gridx = 1; c.weightx = 1.0; card.add(serverBlock,        c);
        c.gridx = 2; c.weightx = 0;   card.add(boldLbl("Port"),    c);
        c.gridx = 3; c.weightx = 0;   card.add(portBlock,          c);
        c.gridx = 4; c.weightx = 0;   card.add(btnBlock,           c);
        return card;
    }

    /** Bordure grise neutre (état initial). */
    private void applyNeutralBorder(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
            new EmptyBorder(4, 6, 4, 6)
        ));
    }

    /** Bordure verte si valide, rouge sinon. */
    private void applyValidatedBorder(JTextField tf, boolean valid) {
        Color col = valid ? new Color(39, 174, 96) : new Color(192, 57, 43);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(col, 2),
            new EmptyBorder(3, 6, 3, 6)
        ));
    }

    /** Retourne true si le port dans portField est valide (1–65535). */
    private boolean isPortValid() {
        try {
            int v = Integer.parseInt(portField.getText().trim());
            return v >= 1 && v <= 65535;
        } catch (NumberFormatException e) { return false; }
    }

    private JPanel buildLoginCard() {
        JPanel card = card("Authentification");
        GridBagConstraints c = gbc();
        c.gridx = 0; card.add(boldLbl("Code électeur :"), c);
        c.gridx = 1; c.weightx = 1; card.add(codeField, c);
        c.gridx = 2; c.weightx = 0; card.add(loginButton, c);
        return card;
    }

    private JPanel buildClassicPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(10, 0, 10, 0));
        JPanel listCard = new JPanel(new BorderLayout());
        listCard.setBackground(C_PANEL);
        listCard.setBorder(titledBorder("Candidats disponibles"));
        listCard.add(new JScrollPane(candidatsList), BorderLayout.CENTER);
        JPanel btns = new JPanel(new GridLayout(0, 1, 0, 8));
        btns.setOpaque(false);
        btns.add(refreshCandidatsButton);
        btns.add(voteButton);
        btns.add(resultsButton);
        JPanel btnsWrap = new JPanel(new BorderLayout());
        btnsWrap.setOpaque(false);
        btnsWrap.add(btns, BorderLayout.NORTH);
        btnsWrap.setBorder(new EmptyBorder(0, 8, 0, 0));
        p.add(listCard,  BorderLayout.CENTER);
        p.add(btnsWrap,  BorderLayout.EAST);
        return p;
    }

    private JPanel buildPollPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(10, 0, 10, 0));
        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setBackground(C_PANEL);
        left.setBorder(titledBorder("Sondages disponibles"));
        left.add(new JScrollPane(pollsList), BorderLayout.CENTER);
        JPanel lb = new JPanel(new GridLayout(1, 2, 6, 0));
        lb.setOpaque(false);
        lb.add(refreshPollsButton);
        lb.add(createPollButton);
        left.add(lb, BorderLayout.SOUTH);
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(C_PANEL);
        right.setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        info.add(selectedPollLabel);
        info.add(pollTypeLabel);
        JPanel selRow = new JPanel(new BorderLayout(8, 0));
        selRow.setOpaque(false);
        selRow.setBorder(new EmptyBorder(0, 0, 6, 0));
        selRow.add(info, BorderLayout.CENTER);
        selRow.add(selectPollButton, BorderLayout.EAST);
        JScrollPane optScroll = new JScrollPane(optionsList);
        optScroll.setBorder(titledBorder("Options"));
        JPanel rb = new JPanel(new GridLayout(0, 1, 0, 6));
        rb.setOpaque(false);
        rb.add(votePollButton);
        rb.add(resultsPollButton);
        rb.add(resultsAllButton);
        right.add(selRow,    BorderLayout.NORTH);
        right.add(optScroll, BorderLayout.CENTER);
        right.add(rb,        BorderLayout.SOUTH);
        p.add(left);
        p.add(right);
        return p;
    }

    private void connecter() {
        if (connecte) { logI("Déjà connecté."); return; }
        String server  = serverField.getText().trim();
        String portStr = portField.getText().trim();
        if (server.isEmpty()) { showErr("Entrez l'adresse du serveur.", "Champ manquant"); return; }
        int port;
        try {
            port = Integer.parseInt(portStr);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showErr("Port invalide : '" + portStr + "'.\nEntrez un entier entre 1 et 65535.", "Port invalide");
            return;
        }
        logI("Connexion à " + server + ":" + port + " …");
        new Thread(() -> {
            try {
                socket = new Socket(server, port);
                in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out    = new PrintWriter(socket.getOutputStream(), true);
                connecte    = true;
                authentifie = false;
                logI("Connecté à " + server + ":" + port);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> showErr(
                    "Impossible de joindre " + server + ":" + port + "\n\nRaison : " + e.getMessage(), "Erreur de connexion"));
                logE("Connexion échouée : " + e.getMessage());
            }
            SwingUtilities.invokeLater(this::updateUiState);
        }).start();
    }

    private void authentifier() {
        if (!connecte) { showErr("Connectez-vous d'abord au serveur.", "Non connecté"); return; }
        String code = codeField.getText().trim();
        if (code.isEmpty()) { showErr("Le code électeur est vide.", "Code manquant"); return; }
        electeurCode = code;
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.LOGIN, electeurCode).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.LOGIN_SUCCESS)) {
                    authentifie = true;
                    String[] parts = resp.split("\\|", 3);
                    electeurName = (parts.length >= 2 && !parts[1].trim().isEmpty()) ? parts[1].trim() : electeurCode;
                    logI("Authentification réussie. Bienvenue, " + electeurName + " !");
                    JOptionPane.showMessageDialog(frame, "Bienvenue, " + electeurName + " !\nVous pouvez maintenant voter.", "Authentification réussie", JOptionPane.INFORMATION_MESSAGE);
                } else if (resp.contains(Protocol.CODE_INVALIDE)) {
                    logE("Code invalide : '" + electeurCode + "'");
                    showErr("Le code '" + electeurCode + "' est invalide.\nVérifiez votre code.", "Code invalide");
                } else if (resp.contains(Protocol.DEJA_VOTE)) {
                    logW("Vous avez déjà voté (classique).");
                    JOptionPane.showMessageDialog(frame, "Vous avez déjà voté (vote classique).", "Déjà voté", JOptionPane.WARNING_MESSAGE);
                } else {
                    logE("Réponse inattendue : " + resp);
                    showErr("Réponse inattendue du serveur :\n" + resp, "Erreur serveur");
                }
                updateUiState();
            });
        }).start();
    }

    private void deconnecter() {
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        connecte = false; authentifie = false; electeurName = "";
        currentPollId = -1; currentPollTitle = ""; pollsMap.clear();
        SwingUtilities.invokeLater(() -> {
            selectedPollLabel.setText("Aucun sondage sélectionné"); pollTypeLabel.setText(""); updateUiState();
        });
        logI("Déconnecté du serveur.");
    }

    private String envoyerMessage(String message) {
        try {
            if (out == null || in == null) { resetConn(); return "ERROR|SESSION_FERMEE"; }
            out.println(message);
            String r = in.readLine();
            if (r == null) { resetConn(); return "ERROR|SESSION_FERMEE"; }
            return r;
        } catch (IOException e) { resetConn(); logE("Erreur réseau : " + e.getMessage()); return "ERROR|" + e.getMessage(); }
    }

    private void resetConn() { connecte = false; authentifie = false; SwingUtilities.invokeLater(this::updateUiState); }

    private void chargerCandidats() {
        if (!connecte) { showErr("Connectez-vous d'abord.", "Non connecté"); return; }
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.GET_CANDIDATS).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.CANDIDATS_LIST)) {
                    String list = resp.substring(Protocol.CANDIDATS_LIST.length() + 1);
                    Map<Integer,String> candidats = parsePairs(list);
                    candidatsModel.clear();
                    candidats.forEach((id, nom) -> candidatsModel.addElement(id + " — " + nom));
                    logI(candidats.size() + " candidat(s) chargé(s)."); updateUiState();
                } else { logE("Erreur chargement candidats : " + resp); }
            });
        }).start();
    }

    private void voterClassique() {
        if (!authentifie) { showErr("Authentifiez-vous d'abord.", "Non authentifié"); return; }
        String sel = candidatsList.getSelectedValue();
        if (sel == null) { showErr("Sélectionnez un candidat.", "Aucune sélection"); return; }
        String idStr = sel.split("—")[0].trim();
        String nom   = sel.contains("—") ? sel.split("—", 2)[1].trim() : sel;
        if (JOptionPane.showConfirmDialog(frame, "Confirmer votre vote pour : « " + nom + " » ?",
                "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.VOTE, electeurCode, idStr).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.VOTE_ACCEPTED)) { logI("Vote enregistré pour « " + nom + " »."); JOptionPane.showMessageDialog(frame, "Vote enregistré.", "Vote enregistré", JOptionPane.INFORMATION_MESSAGE); }
                else if (resp.contains(Protocol.DEJA_VOTE)) { logW("Déjà voté."); JOptionPane.showMessageDialog(frame, "Vous avez déjà voté.", "Déjà voté", JOptionPane.WARNING_MESSAGE); }
                else if (resp.contains(Protocol.CANDIDAT_INVALIDE)) { logE("Candidat invalide."); showErr("Candidat invalide.", "Erreur"); }
                else { logE("Vote refusé : " + resp); showErr("Vote refusé :\n" + resp, "Vote refusé"); }
            });
        }).start();
    }

    private void afficherResultatsClassiques() {
        if (!connecte) { showErr("Connectez-vous d'abord.", "Non connecté"); return; }
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.GET_RESULTS).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.RESULTS)) { showResults("Résultats — Vote classique", resp.substring(Protocol.RESULTS.length() + 1)); }
                else { logE("Erreur résultats : " + resp); showErr("Impossible de récupérer les résultats.\n" + resp, "Erreur"); }
            });
        }).start();
    }

    private void chargerListeSondages() {
        if (!connecte) { showErr("Connectez-vous d'abord.", "Non connecté"); return; }
        refreshPollsButton.setEnabled(false);
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.GET_POLLS).toString());
            SwingUtilities.invokeLater(() -> {
                refreshPollsButton.setEnabled(true);
                if (resp.startsWith(Protocol.POLLS_LIST)) {
                    String list = resp.substring(Protocol.POLLS_LIST.length() + 1);
                    pollsModel.clear(); pollsMap.clear(); currentPollId = -1; currentPollTitle = "";
                    selectedPollLabel.setText("Aucun sondage sélectionné"); pollTypeLabel.setText("");
                    if (!list.isEmpty()) { for (String p : list.split("\\|")) { p = p.trim(); if (!p.isEmpty()) { pollsModel.addElement(p); parsePollEntry(p); } } }
                    logI(pollsModel.size() + " sondage(s) disponible(s).");
                } else { logE("Erreur sondages : " + resp); showErr("Impossible de charger la liste.\n" + resp, "Erreur"); }
                updateUiState();
            });
        }).start();
    }

    private void parsePollEntry(String entry) {
        String[] p = entry.split(":", 3);
        try {
            int id = Integer.parseInt(p[0].trim());
            String titre = p.length >= 2 ? p[p.length - 1].trim() : entry;
            int p1 = titre.lastIndexOf('('), p2 = titre.lastIndexOf(')');
            if (p1 != -1 && p2 > p1) titre = titre.substring(0, p1).trim();
            pollsMap.put(id, titre);
        } catch (NumberFormatException ignored) {}
    }

    private void selectionnerSondage() {
        int idx = pollsList.getSelectedIndex();
        if (idx == -1) { showErr("Sélectionnez un sondage dans la liste.", "Aucune sélection"); return; }
        String entry = pollsModel.get(idx);
        String[] parts = entry.split(":", 3);
        try {
            currentPollId = Integer.parseInt(parts[0].trim());
            String titre = "", type = "-";
            if (parts.length == 3) { type = parts[1].trim(); titre = parts[2].trim(); }
            else if (parts.length == 2) { titre = parts[1].trim(); int p1 = titre.lastIndexOf('('), p2 = titre.lastIndexOf(')'); if (p1 != -1 && p2 > p1) { type = titre.substring(p1+1, p2).trim(); titre = titre.substring(0, p1).trim(); } }
            else { titre = entry; }
            currentPollTitle = titre;
            selectedPollLabel.setText("Sondage actif : " + currentPollTitle);
            pollTypeLabel.setText("Type : " + (type.isEmpty() ? "-" : type));
            chargerOptionsSondage(currentPollId); updateUiState();
        } catch (NumberFormatException ex) { showErr("Format de sondage invalide.", "Erreur"); }
    }

    private void chargerOptionsSondage(int pollId) {
        if (!connecte) return;
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.GET_OPTIONS, String.valueOf(pollId)).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.OPTIONS_LIST)) {
                    Map<Integer,String> opts = parsePairs(resp.substring(Protocol.OPTIONS_LIST.length() + 1));
                    optionsModel.clear();
                    opts.forEach((id, label) -> optionsModel.addElement(id + " — " + label));
                    logI(opts.size() + " option(s) chargée(s)."); updateUiState();
                } else { logE("Erreur options : " + resp); }
            });
        }).start();
    }

    private void creerSondage() {
        if (!authentifie) { showErr("Authentifiez-vous d'abord.", "Non authentifié"); return; }
        JTextField titreField = styledTextField("", 22);
        JTextArea  optsArea   = new JTextArea(6, 22);
        optsArea.setFont(F_NORMAL); optsArea.setLineWrap(true); optsArea.setWrapStyleWord(true);
        JPanel dlg = new JPanel(new GridBagLayout());
        dlg.setBackground(C_PANEL); dlg.setBorder(new EmptyBorder(6, 6, 6, 6));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6); g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; dlg.add(boldLbl("Titre du sondage :"), g);
        g.gridx = 1; g.weightx = 1; dlg.add(titreField, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0; dlg.add(boldLbl("Options (1 par ligne) :"), g);
        g.gridx = 1; g.weighty = 1; g.fill = GridBagConstraints.BOTH; dlg.add(new JScrollPane(optsArea), g);
        if (JOptionPane.showConfirmDialog(frame, dlg, "Créer un sondage", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) { showErr("Le titre est vide.", "Titre manquant"); return; }
        List<String> opts = new ArrayList<>();
        for (String l : optsArea.getText().split("\n")) if (!l.trim().isEmpty()) opts.add(l.trim());
        if (opts.isEmpty()) { showErr("Ajoutez au moins une option.", "Options manquantes"); return; }
        List<String> parts = new ArrayList<>(Arrays.asList(Protocol.CREATE_POLL, electeurCode, titre));
        parts.addAll(opts);
        Message msg = new Message(parts.toArray(new String[0]));
        new Thread(() -> {
            String resp = envoyerMessage(msg.toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.CREATE_POLL_SUCCESS)) { String[] rp = resp.split("\\|"); logI("Sondage créé (ID=" + (rp.length >= 2 ? rp[1] : "?") + ")."); chargerListeSondages(); }
                else { logE("Création refusée : " + resp); showErr("Création refusée :\n" + resp, "Échec"); }
            });
        }).start();
    }

    private void voterSondage() {
        if (!authentifie) { showErr("Authentifiez-vous d'abord.", "Non authentifié"); return; }
        if (currentPollId == -1) { showErr("Sélectionnez d'abord un sondage.", "Aucun sondage"); return; }
        String sel = optionsList.getSelectedValue();
        if (sel == null) { showErr("Sélectionnez une option.", "Aucune sélection"); return; }
        String idStr = sel.split("—")[0].trim();
        String nom   = sel.contains("—") ? sel.split("—", 2)[1].trim() : sel;
        if (JOptionPane.showConfirmDialog(frame, "Confirmer votre vote pour : « " + nom + " »\ndans « " + currentPollTitle + " » ?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.VOTE_POLL, electeurCode, String.valueOf(currentPollId), idStr).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.VOTE_ACCEPTED)) { logI("Vote enregistré."); JOptionPane.showMessageDialog(frame, "Vote enregistré pour « " + nom + " ».", "Vote enregistré", JOptionPane.INFORMATION_MESSAGE); }
                else if (resp.contains(Protocol.DEJA_VOTE)) { logW("Déjà voté."); JOptionPane.showMessageDialog(frame, "Vous avez déjà voté dans ce sondage.", "Déjà voté", JOptionPane.WARNING_MESSAGE); }
                else { logE("Vote refusé : " + resp); showErr("Vote refusé :\n" + resp, "Vote refusé"); }
            });
        }).start();
    }

    private void afficherResultatsSondage() {
        if (!connecte) { showErr("Connectez-vous d'abord.", "Non connecté"); return; }
        if (currentPollId == -1) { showErr("Sélectionnez un sondage.", "Aucun sondage"); return; }
        new Thread(() -> {
            String resp = envoyerMessage(new Message(Protocol.GET_POLL_RESULTS, String.valueOf(currentPollId)).toString());
            SwingUtilities.invokeLater(() -> {
                if (resp.startsWith(Protocol.POLL_RESULTS)) { showResults("Résultats — « " + currentPollTitle + " »", resp.substring(Protocol.POLL_RESULTS.length() + 1)); }
                else { logE("Erreur résultats sondage : " + resp); showErr("Impossible de récupérer les résultats.\n" + resp, "Erreur"); }
            });
        }).start();
    }

    private void afficherResultatsTousSondages() {
        if (!connecte) { showErr("Connectez-vous d'abord.", "Non connecté"); return; }
        if (pollsMap.isEmpty()) { showErr("Aucun sondage connu.\nActualisez la liste.", "Aucun sondage"); return; }
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, String> e : pollsMap.entrySet()) {
                String resp = envoyerMessage(new Message(Protocol.GET_POLL_RESULTS, String.valueOf(e.getKey())).toString());
                sb.append("=== ").append(e.getValue()).append(" (ID ").append(e.getKey()).append(") ===\n");
                if (resp.startsWith(Protocol.POLL_RESULTS)) { for (String item : resp.substring(Protocol.POLL_RESULTS.length() + 1).split("\\|")) if (!item.trim().isEmpty()) sb.append("  • ").append(item.trim()).append("\n"); }
                else { sb.append("  (données non disponibles)\n"); }
                sb.append("\n");
            }
            String content = sb.toString();
            SwingUtilities.invokeLater(() -> showResults("Résultats actuels — Tous les sondages", content));
        }).start();
    }

    private void updateUiState() {
        if (authentifie) {
            statusDot.setForeground(C_SUCCESS); statusLabel.setText("Authentifié"); statusLabel.setForeground(Color.WHITE);
            userLabel.setText("  " + electeurName); userLabel.setForeground(Color.WHITE);
        } else if (connecte) {
            statusDot.setForeground(new Color(255, 200, 50)); statusLabel.setText("Connecté"); statusLabel.setForeground(Color.WHITE);
            userLabel.setText("  —"); userLabel.setForeground(new Color(200, 215, 240));
        } else {
            statusDot.setForeground(C_DANGER); statusLabel.setText("Déconnecté"); statusLabel.setForeground(new Color(255, 180, 170));
            userLabel.setText("  —"); userLabel.setForeground(new Color(180, 195, 220));
        }
        connectButton.setEnabled(!connecte); disconnectButton.setEnabled(connecte);
        loginButton.setEnabled(connecte && !authentifie); codeField.setEnabled(connecte && !authentifie);
        refreshCandidatsButton.setEnabled(connecte); voteButton.setEnabled(connecte && authentifie && !candidatsModel.isEmpty()); resultsButton.setEnabled(connecte);
        refreshPollsButton.setEnabled(connecte); createPollButton.setEnabled(connecte && authentifie);
        selectPollButton.setEnabled(connecte && pollsList.getSelectedIndex() != -1);
        votePollButton.setEnabled(connecte && authentifie && currentPollId != -1);
        resultsPollButton.setEnabled(connecte && currentPollId != -1); resultsAllButton.setEnabled(connecte && !pollsMap.isEmpty());
    }

    private void logI(String m) { log("[" + LocalTime.now().format(TIME_FMT) + "] " + m); }
    private void logE(String m) { log("[" + LocalTime.now().format(TIME_FMT) + "] X  " + m); }
    private void logW(String m) { log("[" + LocalTime.now().format(TIME_FMT) + "] !  " + m); }
    private void log(String msg) { SwingUtilities.invokeLater(() -> { logArea.append(msg + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }); }
    private void showErr(String msg, String title) { JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE); }
    private void showResults(String title, String raw) {
        StringBuilder sb = new StringBuilder();
        for (String item : raw.split("\\|")) if (!item.trim().isEmpty()) sb.append("   • ").append(item.trim()).append("\n");
        JTextArea ta = new JTextArea(sb.length() > 0 ? sb.toString() : "Aucun résultat disponible.");
        ta.setEditable(false); ta.setFont(F_MONO); ta.setMargin(new Insets(10, 14, 10, 14));
        JScrollPane sp = new JScrollPane(ta); sp.setPreferredSize(new Dimension(480, 340));
        JOptionPane.showMessageDialog(frame, sp, title, JOptionPane.PLAIN_MESSAGE);
    }

    private Map<Integer, String> parsePairs(String list) {
        Map<Integer, String> map = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) return map;
        for (String item : list.split("\\|")) { item = item.trim(); if (item.isEmpty()) continue; String[] p = item.split(":", 2); if (p.length == 2) { try { map.put(Integer.parseInt(p[0].trim()), p[1].trim()); } catch (NumberFormatException ignored) {} } }
        return map;
    }

    private JButton primaryBtn(String text, Color bg) {
        JButton b = new JButton(text); b.setFont(F_BOLD); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }

    private JTextField styledTextField(String text, int cols) { JTextField tf = new JTextField(text, cols); tf.setFont(F_NORMAL); return tf; }

    private <T> JList<T> styledList(DefaultListModel<T> model, Color selColor) {
        JList<T> list = new JList<>(model); list.setFont(F_NORMAL);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(selColor); list.setSelectionForeground(Color.WHITE); list.setFixedCellHeight(28); return list;
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(C_PANEL);
        Border line = BorderFactory.createLineBorder(new Color(210, 220, 240), 1);
        Border left = new MatteBorder(0, 4, 0, 0, C_PRIMARY);
        Border inner = new EmptyBorder(8, 12, 8, 12);
        p.setBorder(new CompoundBorder(new EmptyBorder(4, 4, 4, 4), new CompoundBorder(line, new CompoundBorder(left, inner))));
        return p;
    }

    private GridBagConstraints gbc() { GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.fill = GridBagConstraints.HORIZONTAL; c.gridy = 0; c.weightx = 0; return c; }

    private TitledBorder titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(210, 215, 230), 1), title);
        tb.setTitleFont(F_BOLD); tb.setTitleColor(C_PRIMARY); return tb;
    }

    private JLabel boldLbl(String text) { JLabel l = new JLabel(text); l.setFont(F_BOLD); return l; }

    public void show() { frame.setVisible(true); }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new VoteClientGUI().show()); }
}