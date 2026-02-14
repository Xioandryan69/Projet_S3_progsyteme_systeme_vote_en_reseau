package reseaux;

import data.DataStore;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.*;

/**
 * Classe pour traiter la logique de vote
 * Gère l'authentification, la validation et l'enregistrement des votes
 */
public class VoteService {

    private static final String VOTES_FILE = "src/data/votes_records.txt";

    public static boolean authentifierUtilisateur(String code) {
        return DataStore.electeurs.containsKey(code);
    }

    public static boolean aDejaVote(String code) {
        Electeur electeur = DataStore.electeurs.get(code);
        return electeur != null && electeur.isAVote();
    }

    public static boolean candidatExiste(int candidatId) {
        return DataStore.candidats.containsKey(candidatId);
    }

    public static boolean isStatutValable(String code) {
        Electeur electeur = DataStore.electeurs.get(code);
        return electeur != null && electeur.isEstValable();
    }

    public static boolean enregistrerVote(String electeurCode, int candidatId) 
    {
        Electeur electeur = DataStore.electeurs.get(electeurCode);
        Candidat candidat = DataStore.candidats.get(candidatId);

        if (electeur == null || candidat == null) {
            return false;
        }

        if (electeur.isAVote()) { // ne concerne que le vote classique
            return false;
        }

        if (!DataStore.voteOuvert) {
            return false;
        }

        Vote vote = new Vote(electeur.getId(), candidatId);
        DataStore.votes.add(vote);

        electeur.setAVote(true);

        sauvegarderVote(electeur.getId(), candidat.getNom());

        return true;
    }

    private static void sauvegarderVote(String electeurId, String candidatNom) 
    {
        try (FileWriter writer = new FileWriter(VOTES_FILE, true)) 
        {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String ligne = timestamp + " - Électeur: " + electeurId + ", Candidat: " + candidatNom + "\n";
            writer.write(ligne);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du vote: " + e.getMessage());
        }
    }

    public static String obtenirResultats() {
        StringBuilder resultats = new StringBuilder();
        for (Candidat candidat : DataStore.candidats.values()) 
        {
            long count = DataStore.votes.stream()
                    .filter(v -> v.getCandidatId() == candidat.getId())
                    .count();
            resultats.append(candidat.getNom()).append(":").append(count).append("|");
        }
        if (resultats.length() > 0) resultats.setLength(resultats.length() - 1);
        return resultats.toString();
    }

    public static String obtenirListeCandidats() 
    {
        StringBuilder liste = new StringBuilder();
        for (Candidat candidat : DataStore.candidats.values()) 
        {
            liste.append(candidat.getId()).append(":").append(candidat.getNom()).append("|");
        }
        if (liste.length() > 0) liste.setLength(liste.length() - 1);
        return liste.toString();
    }
}