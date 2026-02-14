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

            public static int creerPoll(String titre, List<String> optionsLibelles, LocalDateTime deadline)
            {
                int pollId = DataStore.getNextPollId();
            
                List<Option> options = new ArrayList<>();
                int optId = 1;
            
                for (String lib : optionsLibelles)
                {
                    options.add(new Option(optId++, lib.trim()));
                }
            
                // ✅ On passe la deadline au constructeur
                Poll poll = new Poll(pollId, titre, options, true, deadline);
            
                DataStore.ajouterPoll(poll);
            
                return pollId;
            }

    public static String listerPolls() 
    {
        if (DataStore.polls.isEmpty()) return "";
        return DataStore.polls.values().stream()
                .map(p -> p.getId() + ":" + p.getTitre())
                .collect(Collectors.joining("|"));
    }

    public static String getOptions(int pollId) 
    {
        Poll poll = DataStore.polls.get(pollId);
        if (poll == null) return "";
        return poll.getOptions().stream()
                .map(o -> o.getId() + ":" + o.getLibelle())
                .collect(Collectors.joining("|"));
    }

    public static boolean enregistrerVote(String electeurCode, int pollId, int optionId)
    {
        Electeur electeur = DataStore.electeurs.get(electeurCode);
    
        // ❌ électeur inexistant
        if (electeur == null)
        {
            return false;
        }
    
        // ❌ électeur non valide
        if (!electeur.isEstValable())
        {
            return false;
        }
    
        Poll poll = DataStore.polls.get(pollId);
    
        // ❌ sondage inexistant ou fermé
        if (poll == null || !poll.isOuvert())
        {
            return false;
        }
    
        // ❌ deadline dépassée
        if (!poll.estEncoreValide())
        {
            return false;
        }
    
        // ❌ déjà voté dans CE sondage
        boolean dejaVote = DataStore.pollVotes.stream()
                .anyMatch(v -> v.getElecteurId().equals(electeur.getId())
                        && v.getPollId() == pollId);
    
        if (dejaVote)
        {
            return false;
        }
    
        // ❌ option inexistante
        boolean optionExiste = poll.getOptions().stream()
                .anyMatch(o -> o.getId() == optionId);
    
        if (!optionExiste)
        {
            return false;
        }
    
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    
        PollVote vote = new PollVote(
                electeur.getId(),
                pollId,
                optionId,
                timestamp
        );
    
        DataStore.ajouterPollVote(vote);
    
        return true;
    }
    public static String getResultats(int pollId) {
        Map<Integer, Long> count = DataStore.pollVotes.stream()
                .filter(v -> v.getPollId() == pollId)
                .collect(Collectors.groupingBy(PollVote::getOptionId, Collectors.counting()));

        Poll poll = DataStore.polls.get(pollId);
        if (poll == null) return "";

        return poll.getOptions().stream()
                .map(o -> o.getLibelle() + ":" + count.getOrDefault(o.getId(), 0L))
                .collect(Collectors.joining("|"));
    }

    public static boolean aDejaVote(String electeurCode, int pollId) {
        Electeur electeur = DataStore.electeurs.get(electeurCode);
        if (electeur == null) return false;
        return DataStore.pollVotes.stream()
                .anyMatch(v -> v.getElecteurId().equals(electeur.getId())
                        && v.getPollId() == pollId);
    }
}