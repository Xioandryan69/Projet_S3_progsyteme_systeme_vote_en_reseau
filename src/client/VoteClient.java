package client;

import reseaux.Message;
import reseaux.Protocol;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client de vote Se connecte au serveur et permet aux utilisateurs de voter
 */
public class VoteClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private String electeurCode;
    private boolean connecte = false;

    public VoteClient(String adresseServeur, int port) {
        this.scanner = new Scanner(System.in);
        try {
            socket = new Socket(adresseServeur, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connecte = true;
            System.out.println("✓ Connecté au serveur: " + adresseServeur + ":" + port);
        } catch (IOException e) {
            System.err.println("✗ Impossible de se connecter au serveur: " + e.getMessage());
            connecte = false;
        }
    }

    public void demarrer() {
        if (!connecte) {
            System.out.println("Connexion échouée. Fermeture du client.");
            return;
        }

        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("   SYSTÈME DE VOTE EN RÉSEAU LOCAL");
        System.out.println("═══════════════════════════════════════════\n");

        // Étape 1: Authentification
        if (!authentifier()) {
            fermer();
            return;
        }

        // Étape 2: Menu de vote
        afficherMenu();

        fermer();
    }

    /**
     * Authentifier l'utilisateur
     */
    private boolean authentifier() {
        System.out.print("Entrez votre code électeur: ");
        electeurCode = scanner.nextLine().trim();

        if (electeurCode.isEmpty()) {
            System.out.println("✗ Code électeur invalide");
            return false;
        }

        Message msg = new Message(Protocol.LOGIN, electeurCode);
        String reponse = envoyerMessage(msg.toString());

        if (reponse.startsWith(Protocol.LOGIN_SUCCESS)) {
            System.out.println("✓ Authentification réussie!");
            return true;
        } else if (reponse.contains(Protocol.CODE_INVALIDE)) {
            System.out.println("✗ Code électeur invalide");
            return false;
        } else if (reponse.contains(Protocol.DEJA_VOTE)) {
            System.out.println("✗ Vous avez déjà voté");
            return false;
        } else {
            System.out.println("✗ Erreur: " + reponse);
            return false;
        }
    }

    /**
     * Afficher le menu de vote
     */
    private void afficherMenu() {
        boolean continuer = true;

        while (continuer) {
            System.out.println("\n───────────────────────────────────────────");
            System.out.println("1. Voter");
            System.out.println("2. Voir les candidats");
            System.out.println("3. Voir les résultats");
            System.out.println("4. Quitter");
            System.out.println("───────────────────────────────────────────");
            System.out.print("Choisir une option (1-4): ");

            String choix = scanner.nextLine().trim();

            switch (choix) {
                case "1":
                    voter();
                    break;
                case "2":
                    afficherCandidats();
                    break;
                case "3":
                    afficherResultats();
                    break;
                case "4":
                    continuer = false;
                    System.out.println("✓ Déconnexion...");
                    break;
                default:
                    System.out.println("✗ Option invalide");
            }
        }
    }

    /**
     * Afficher les candidats disponibles
     */
    private void afficherCandidats() {
        Message msg = new Message(Protocol.GET_CANDIDATS);
        String reponse = envoyerMessage(msg.toString());

        if (reponse.startsWith(Protocol.CANDIDATS_LIST)) {
            String candidatsList = reponse.substring(Protocol.CANDIDATS_LIST.length() + 1);
            String[] candidats = candidatsList.split("\\|");

            System.out.println("\n┌─ CANDIDATS DISPONIBLES ─────────────────┐");
            for (String candidat : candidats) {
                if (!candidat.isEmpty()) {
                    String[] parts = candidat.split(":");
                    if (parts.length == 2) {
                        System.out.println("│ ID: " + parts[0] + " - " + parts[1]);
                    }
                }
            }
            System.out.println("└─────────────────────────────────────────┘");
        } else {
            System.out.println("✗ Erreur lors de la récupération des candidats");
        }
    }

    /**
     * Voter pour un candidat
     */
    private void voter() {
        afficherCandidats();

        System.out.print("\nEntrez l'ID du candidat pour lequel voter: ");
        String candidatIdStr = scanner.nextLine().trim();

        try {
            int candidatId = Integer.parseInt(candidatIdStr);
            Message msg = new Message(Protocol.VOTE, electeurCode, String.valueOf(candidatId));
            String reponse = envoyerMessage(msg.toString());

            if (reponse.startsWith(Protocol.VOTE_ACCEPTED)) {
                System.out.println("✓ Vote enregistré avec succès!");
            } else if (reponse.contains(Protocol.CANDIDAT_INVALIDE)) {
                System.out.println("✗ Candidat invalide");
            } else {
                System.out.println("✗ Erreur: " + reponse);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ ID de candidat invalide");
        }
    }

    /**
     * Afficher les résultats du vote
     */
    private void afficherResultats() {
        Message msg = new Message(Protocol.GET_RESULTS);
        String reponse = envoyerMessage(msg.toString());

        if (reponse.startsWith(Protocol.RESULTS)) {
            String resultats = reponse.substring(Protocol.RESULTS.length() + 1);
            String[] votes = resultats.split("\\|");

            System.out.println("\n┌─ RÉSULTATS DU VOTE ─────────────────────┐");
            for (String vote : votes) {
                if (!vote.isEmpty()) {
                    String[] parts = vote.split(":");
                    if (parts.length == 2) {
                        System.out.println("│ " + parts[0] + ": " + parts[1] + " vote(s)");
                    }
                }
            }
            System.out.println("└─────────────────────────────────────────┘");
        } else {
            System.out.println("✗ Erreur lors de la récupération des résultats");
        }
    }

    /**
     * Envoyer un message au serveur et recevoir la réponse
     */
    private String envoyerMessage(String message) {
        try {
            out.println(message);
            String reponse = in.readLine();
            return reponse != null ? reponse : "";
        } catch (IOException e) {
            System.err.println("✗ Erreur de communication: " + e.getMessage());
            return "";
        }
    }

    /**
     * Fermer la connexion
     */
    private void fermer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        }
        scanner.close();
        System.out.println("✓ Connexion fermée");
    }

    public static void main(String[] args) {
        String adresseServeur = "localhost";
        int port = 5000;

        if (args.length >= 1) {
            adresseServeur = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide");
            }
        }

        VoteClient client = new VoteClient(adresseServeur, port);
        client.demarrer();
    }
}
