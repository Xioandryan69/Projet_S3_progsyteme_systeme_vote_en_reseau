package reseaux;

import java.util.Arrays;

/**
 * Classe pour construire et parser les messages entre client et serveur Format:
 * COMMANDE|param1|param2|...
 */
public class Message {

    private String[] parts;

    /**
     * Constructeur pour parser un message reçu
     */
    public Message(String message) {
        this.parts = message.split("\\|");
    }

    /**
     * Constructeur pour créer un message
     */
    public Message(String... parts) {
        this.parts = parts;
    }

    /**
     * Obtenir la commande (premier élément)
     */
    public String getCommand() {
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * Obtenir un paramètre à l'index donné
     */
    public String getParam(int index) {
        return index < parts.length ? parts[index] : "";
    }

    /**
     * Obtenir tous les paramètres après la commande
     */
    public String[] getParams() {
        return Arrays.copyOfRange(parts, 1, parts.length);
    }

    /**
     * Convertir le message en chaîne pour l'envoi
     */
    @Override
    public String toString() {
        return String.join(Protocol.DELIMITER, parts);
    }

    /**
     * Vérifier si le message contient une commande spécifique
     */
    public boolean isCommand(String cmd) {
        return cmd.equals(getCommand());
    }

    /**
     * Obtenir le nombre de paramètres
     */
    public int getParamCount() {
        return Math.max(0, parts.length - 1);
    }
}
