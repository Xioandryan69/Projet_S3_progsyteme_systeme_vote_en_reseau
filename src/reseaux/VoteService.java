package reseaux;

import data.DataStore;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe pour traiter la logique de vote Gère l'authentification, la validation
 * et l'enregistrement des votes
 */
public class VoteService {

    private static final String VOTES_FILE = "src/data/votes_records.txt";

    /**
     * Authentifier un utilisateur par code électeur
     */
    public static boolean authentifierUtilisateur(String code) {
        return DataStore.electeurs.containsKey(code);
    }

    /**
     * Vérifier si l'utilisateur a déjà voté
     */
    public static boolean aDejaVote(String code) {
        Electeur electeur = DataStore.electeurs.get(code);
        return electeur != null && electeur.isAVote();
    }

    /**
     * Vérifier si un candidat existe
     */
    public static boolean candidatExiste(int candidatId) {
        return DataStore.candidats.containsKey(candidatId);
    }

    /**
     * Enregistrer un vote
     */
        Electeur electeur = DataStore.electeurs.get(electeurCode);
        Candidat candidat = DataStore.candidats.get(candidatId);

        if (electeur == null || candidat == null) {
            return false;
        }

        if (electeur.isAVote()) {
            return false;
        }

        if (!DataStore.voteOuvert) {
            return false;
        }

        // Créer le vote
        Vote vote = new Vote(electeur.getId(), candidatId);
        DataStore.votes.add(vote);

        // Marquer l'électeur comme ayant voté
        electeur.setAVote(true);

        // Enregistrer dans le fichier
        sauvegarderVote(electeur.getId(), candidat.getNom());

        return true;
    }

    /**
     * Sauvegarder le vote dans un fichier
     */
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String ligne = timestamp + " - Électeur: " + electeurId + ", Candidat: " + candidatNom + "\n";
            writer.write(ligne);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du vote: " + e.getMessage());
        }
    }

    /**
     * Obtenir les résultats des votes
     */
    public static String obtenirResultats() {
        StringBuilder resultats = new StringBuilder();

            long count = DataStore.votes.stream()
                    .filter(v -> v.getCandidatId() == candidat.getId())
                    .count();
            resultats.append(candidat.getNom()).append(":").append(count).append("|");
        }

        // Supprimer le dernier délimiteur
            resultats.setLength(resultats.length() - 1);
        }

        return resultats.toString();
    }

    /**
     * Obtenir la liste des candidats formatée
     */
        StringBuilder liste = new StringBuilder();

            liste.append(candidat.getId()).append(":").append(candidat.getNom()).append("|");
        }

        // Supprimer le dernier délimiteur
        if (liste.length() > 0) {
            liste.setLength(liste.length() - 1);
        }

        return liste.toString();
    }
}
