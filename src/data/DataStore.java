package data;

import model.*;
import com.google.gson.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class DataStore 
{
    // Données vote classique
    public static Map<String, Electeur> electeurs = new HashMap<>();
    public static Map<Integer, Candidat> candidats = new HashMap<>();
    public static List<Vote> votes = new ArrayList<>();
    public static boolean voteOuvert = true;

    // Données sondages
    public static Map<Integer, Poll> polls = new HashMap<>();
    public static List<PollVote> pollVotes = new ArrayList<>();
    private static int nextPollId = 1;

    private static final String ELECTEURS_FILE = "src/data/electeurs.json";
    private static final String CANDIDATS_FILE = "src/data/user.json";
    private static final String POLLS_FILE = "src/data/polls.json";
    private static final String POLL_VOTES_FILE = "src/data/poll_votes.json";

    // Charger toutes les données
    public static void chargerDonnees() 
    {
        chargerElecteurs();
        chargerCandidats();
        chargerPolls();
        chargerPollVotes();
    }

    private static void chargerElecteurs() {
        try {
            JsonArray array = JsonParser.parseReader(
                    new FileReader(ELECTEURS_FILE)
            ).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                boolean aVote = o.has("aVote") && o.get("aVote").getAsBoolean();

                Electeur el = new Electeur(
                        o.get("id").getAsString(),
                        o.get("code").getAsString(),
                        aVote,
                        o.get("estValable").getAsBoolean()
                );

                // Charger votes sondages
                if (o.has("votesSondages")) {
                    JsonArray sondages = o.getAsJsonArray("votesSondages");
                    for (JsonElement ve : sondages) {
                        el.ajouterSondageVote(ve.getAsInt());
                    }
                }

                electeurs.put(el.getCode(), el);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement électeurs");
            e.printStackTrace();
        }
    }

    private static void chargerCandidats() {
        try {
            JsonArray array = JsonParser.parseReader(new FileReader(CANDIDATS_FILE)).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                Candidat c = new Candidat(
                        o.get("id").getAsInt(),
                        o.get("nom").getAsString()
                );
                candidats.put(c.getId(), c);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement candidats : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----- Sondages -----
    private static void chargerPolls() {

        try (FileReader reader = new FileReader(POLLS_FILE)) {
    
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
    
            for (JsonElement element : array) {
    
                JsonObject o = element.getAsJsonObject();
    
                int id = o.get("id").getAsInt();
                String titre = o.get("titre").getAsString();
                boolean ouvert = o.get("ouvert").getAsBoolean();
    
                LocalDateTime deadline = null;
                if (o.has("deadline") && !o.get("deadline").isJsonNull()) {
                    deadline = LocalDateTime.parse(o.get("deadline").getAsString());
                }
    
                List<Option> options = new ArrayList<>();
                JsonArray optionsArray = o.getAsJsonArray("options");
    
                for (JsonElement optElement : optionsArray) {
                    JsonObject optObj = optElement.getAsJsonObject();
                    int optId = optObj.get("id").getAsInt();
                    String libelle = optObj.get("libelle").getAsString();
    
                    options.add(new Option(optId, libelle));
                }
    
                Poll poll = new Poll(id, titre, options, ouvert, deadline);
    
                // ✅ CORRECTION ICI
                polls.put(id, poll);
    
                // maintenir nextPollId cohérent
                if (id >= nextPollId) {
                    nextPollId = id + 1;
                }
            }
    
        } catch (Exception e) {
            System.out.println("Erreur chargement polls");
            e.printStackTrace();
        }
    }

    public static int getNextPollId() {
        return nextPollId++;
    }

    public static void chargerPollVotes() {
        File file = new File(POLL_VOTES_FILE);
        if (!file.exists()) {
            pollVotes = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                String electeurId = o.get("electeurId").getAsString();
                int pollId = o.get("pollId").getAsInt();
                int optionId = o.get("optionId").getAsInt();
                String timestamp = o.get("timestamp").getAsString();

                PollVote vote = new PollVote(electeurId, pollId, optionId, timestamp);
                pollVotes.add(vote);

                // mettre à jour l'électeur
                Electeur el = electeurs.get(electeurId);
                if (el != null) el.ajouterSondageVote(pollId);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement votes de sondage : " + e.getMessage());
            e.printStackTrace();
            pollVotes = new ArrayList<>();
        }
    }

    public static void sauvegarderPollVotes() {
        JsonArray array = new JsonArray();
        for (PollVote vote : pollVotes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("electeurId", vote.getElecteurId());
            obj.addProperty("pollId", vote.getPollId());
            obj.addProperty("optionId", vote.getOptionId());
            obj.addProperty("timestamp", vote.getTimestamp());
            array.add(obj);
        }

        try (FileWriter writer = new FileWriter(POLL_VOTES_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde poll_votes.json : " + e.getMessage());
        }
    }

    public static void ajouterPollVote(PollVote vote) {
        pollVotes.add(vote);
        Electeur el = electeurs.get(vote.getElecteurId());
        if (el != null) el.ajouterSondageVote(vote.getPollId());
        sauvegarderPollVotes();
    }

    public static void sauvegarderPolls() {

        JsonArray array = new JsonArray();
    
        // ✅ CORRECTION ICI
        for (Poll poll : polls.values()) {
    
            JsonObject obj = new JsonObject();
    
            obj.addProperty("id", poll.getId());
            obj.addProperty("titre", poll.getTitre());
            obj.addProperty("ouvert", poll.isOuvert());
    
            if (poll.getDeadline() != null) {
                obj.addProperty("deadline", poll.getDeadline().toString());
            }
    
            JsonArray optionsArray = new JsonArray();
    
            for (Option opt : poll.getOptions()) {
    
                JsonObject optObj = new JsonObject();
                optObj.addProperty("id", opt.getId());
                optObj.addProperty("libelle", opt.getLibelle());
    
                optionsArray.add(optObj);
            }
    
            obj.add("options", optionsArray);
            array.add(obj);
        }
    
        try (FileWriter writer = new FileWriter(POLLS_FILE)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(array, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ajouterPoll(Poll poll) {
        polls.put(poll.getId(), poll);
        sauvegarderPolls();
    }

    public static void reinitialiser() {
        electeurs.clear();
        candidats.clear();
        votes.clear();
        polls.clear();
        pollVotes.clear();
        voteOuvert = true;
        nextPollId = 1;
        chargerDonnees();
    }

    public static String getResultatsVoteClassique() {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Résultats Vote Classique ===\n");
    
        Map<Integer, Integer> compteur = new HashMap<>();
    
        for (Vote v : votes) {
            compteur.put(v.getCandidatId(),
                    compteur.getOrDefault(v.getCandidatId(), 0) + 1);
        }
    
        for (Candidat c : candidats.values()) {
            int nb = compteur.getOrDefault(c.getId(), 0);
            sb.append(c.getNom())
              .append(" : ")
              .append(nb)
              .append(" vote(s)\n");
        }
    
        return sb.toString();
    }


    public static String getResultatsSondages() {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Résultats Sondages ===\n\n");
    
        for (Poll poll : polls.values()) {
    
            sb.append("Sondage : ").append(poll.getTitre()).append("\n");
    
            if (poll.getDeadline() != null) {
                sb.append("Deadline : ")
                  .append(poll.getDeadline())
                  .append("\n");
            }
    
            sb.append("Statut : ")
              .append(poll.isOuvert() ? "En cours" : "Terminé")
              .append("\n");
    
            Map<Integer, Integer> compteur = new HashMap<>();
    
            for (PollVote vote : pollVotes) {
                if (vote.getPollId() == poll.getId()) {
                    compteur.put(
                        vote.getOptionId(),
                        compteur.getOrDefault(vote.getOptionId(), 0) + 1
                    );
                }
            }
    
            for (Option opt : poll.getOptions()) {
                int nb = compteur.getOrDefault(opt.getId(), 0);
                sb.append(" - ")
                  .append(opt.getLibelle())
                  .append(" : ")
                  .append(nb)
                  .append(" vote(s)\n");
            }
    
            sb.append("\n");
        }
    
        return sb.toString();
    }
}