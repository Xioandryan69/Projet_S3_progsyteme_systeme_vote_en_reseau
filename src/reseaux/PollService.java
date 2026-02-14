package reseaux;

import data.DataStore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import model.*;

public class PollService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Crée un nouveau sondage.
     * @param titre Titre du sondage
     * @param optionsLibelles Liste des libellés des options
     * @return l'ID du sondage créé
     */
    public static int creerPoll(String titre, List<String> optionsLibelles) 
    {
        // Récupérer le prochain ID disponible depuis DataStore
        int pollId = DataStore.getNextPollId();

        List<Option> options = new ArrayList<>();
        int optId = 1;
        for (String lib : optionsLibelles) 
        {
            options.add(new Option(optId++, lib.trim()));
        }

      
        Poll poll = new Poll(pollId, titre, options, true);

        DataStore.ajouterPoll(poll);

        return pollId;
    }

    /**
     * Retourne la liste des sondages au format : "id:titre|id2:titre2|..."
     */
    public static String listerPolls() 
    {
        if (DataStore.polls.isEmpty()) 
        {
            return "";
        }
        return DataStore.polls.values().stream()
                .map(p -> p.getId() + ":" + p.getTitre())
                .collect(Collectors.joining("|"));
    }

 
    public static String getOptions(int pollId) 
    {
        Poll poll = DataStore.polls.get(pollId);
        if (poll == null) 
        {
            return "";
        }
        return poll.getOptions().stream()
                .map(o -> o.getId() + ":" + o.getLibelle())
                .collect(Collectors.joining("|"));
    }

    /**
     * Enregistre le vote d'un électeur pour une option donnée dans un sondage.
     * @param electeurCode code de l'électeur
     * @param pollId identifiant du sondage
     * @param optionId identifiant de l'option choisie
     * @return true si le vote est accepté, false sinon
     */
    public static boolean enregistrerVote(String electeurCode, int pollId, int optionId) 
    {
        // Vérifier l'électeur
        Electeur electeur = DataStore.electeurs.get(electeurCode);
        if (electeur == null) 
        {
            return false;
        }

        // Vérifier le sondage
        Poll poll = DataStore.polls.get(pollId);
        if (poll == null || !poll.isOuvert()) {
            return false;
        }

        // Vérifier que l'électeur n'a pas déjà voté pour ce sondage
        boolean dejaVote = DataStore.pollVotes.stream()
                .anyMatch(v -> v.getElecteurId().equals(electeur.getId())
                        && v.getPollId() == pollId);
        if (dejaVote) {
            return false;
        }

        // Vérifier que l'option existe dans ce sondage
        boolean optionExiste = poll.getOptions().stream().anyMatch(o -> o.getId() == optionId);
        if (!optionExiste) 
        {
            return false;
        }

        // Créer le vote avec timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        PollVote vote = new PollVote(electeur.getId(), pollId, optionId, timestamp);

        // Ajouter et persister
        DataStore.ajouterPollVote(vote);

        return true;
    }

    /**
     * Retourne les résultats d'un sondage au format : "libelleOption:nombreVotes|..."
     */
    public static String getResultats(int pollId) {
        // Compter les votes par option pour ce sondage
        Map<Integer, Long> count = DataStore.pollVotes.stream()
                .filter(v -> v.getPollId() == pollId)
                .collect(Collectors.groupingBy(PollVote::getOptionId, Collectors.counting()));

        Poll poll = DataStore.polls.get(pollId);
        if (poll == null) {
            return "";
        }

        return poll.getOptions().stream()
                .map(o -> o.getLibelle() + ":" + count.getOrDefault(o.getId(), 0L))
                .collect(Collectors.joining("|"));
    }

    /**
     * Vérifie si un électeur a déjà voté dans un sondage donné.
     */
    public static boolean aDejaVote(String electeurCode, int pollId) {
        Electeur electeur = DataStore.electeurs.get(electeurCode);
        if (electeur == null) return false;
        return DataStore.pollVotes.stream()
                .anyMatch(v -> v.getElecteurId().equals(electeur.getId())
                        && v.getPollId() == pollId);
    }
}