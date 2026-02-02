package reseaux;

import data.DataStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestionnaire des sessions de vote Contrôle l'ouverture et la fermeture des
 * sessions
 */
public class SessionManager {

    private LocalDateTime heureOuverture;
    private LocalDateTime heureFermeture;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public SessionManager() {
        ouvrirSession();
    }

    /**
     * Ouvrir une session de vote
     */
    public void ouvrirSession() {
        heureOuverture = LocalDateTime.now();
        DataStore.voteOuvert = true;
        System.out.println("[SessionManager] Session de vote ouverte à "
                + heureOuverture.format(formatter));
    }

    /**
     * Fermer la session de vote
     */
    public void fermerSession() {
        heureFermeture = LocalDateTime.now();
        DataStore.voteOuvert = false;
        System.out.println("[SessionManager] Session de vote fermée à "
                + heureFermeture.format(formatter));
    }

    /**
     * Vérifier si la session est ouverte
     */
    public boolean estOuvert() {
        return DataStore.voteOuvert;
    }

    /**
     * Obtenir l'heure d'ouverture
     */
    public LocalDateTime getHeureOuverture() {
        return heureOuverture;
    }

    /**
     * Obtenir l'heure de fermeture
     */
    public LocalDateTime getHeureFermeture() {
        return heureFermeture;
    }

    /**
     * Obtenir les statistiques de session
     */
    public String getStatistiques() {
        StringBuilder stats = new StringBuilder();
        stats.append("Session ouverte: ").append(heureOuverture.format(formatter)).append("\n");
        if (heureFermeture != null) {
            stats.append("Session fermée: ").append(heureFermeture.format(formatter)).append("\n");
        }
        stats.append("État: ").append(estOuvert() ? "OUVERTE" : "FERMÉE").append("\n");
        stats.append("Votes enregistrés: ").append(DataStore.votes.size());

        return stats.toString();
    }
}
