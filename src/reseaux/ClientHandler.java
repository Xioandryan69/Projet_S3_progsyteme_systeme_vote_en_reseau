package reseaux;

import java.io.*;
import java.net.Socket;
import model.*;

/**
 * Classe pour gérer un client connecté
 * Exécutée dans un thread distinct pour chaque client
 * Responsable de la communication avec le client
 */
public class ClientHandler implements Runnable 
{
    private Socket socket;
    private Electeur electeur;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean actif = true;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() 
    {
        try {
            // Initialiser les flux de communication
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(
                    socket.getOutputStream(), true
            );

            System.out.println("[ClientHandler] Nouvelle connexion: " + 
                    socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

            String messageRecu;

            // Boucle de traitement des messages
            while (actif && (messageRecu = in.readLine()) != null) {
                System.out.println("[ClientHandler] Message reçu: " + messageRecu);

                // Parser le message et traiter la requête
                Message message = new Message(messageRecu);
                String reponse = RequestProcessor.traiterRequete(message);

                // Envoyer la réponse
                out.println(reponse);
                System.out.println("[ClientHandler] Réponse envoyée: " + reponse);
            }

            fermerConnexion();

        } catch (IOException e) {
            System.err.println("[ClientHandler] Erreur d'entrée/sortie: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ClientHandler] Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fermerConnexion();
        }
    }

    /**
     * Fermer la connexion du client
     */
    private void fermerConnexion() {
        actif = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[ClientHandler] Connexion fermée");
            }
        } catch (IOException e) {
            System.err.println("[ClientHandler] Erreur lors de la fermeture: " + e.getMessage());
        }
    }
}
