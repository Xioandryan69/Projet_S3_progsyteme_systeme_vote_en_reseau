package data;

import model.*;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {

    // ------------------------------------------------------------
    // Données existantes (vote classique)
    // ------------------------------------------------------------
    public static Map<String, Electeur> electeurs = new HashMap<>();
    public static Map<Integer, Candidat> candidats = new HashMap<>();
    public static List<Vote> votes = new ArrayList<>();
    public static boolean voteOuvert = true;

    // ------------------------------------------------------------
    // NOUVELLES DONNÉES : sondages
    // ------------------------------------------------------------
    public static Map<Integer, Poll> polls = new HashMap<>();      // clé = pollId
    public static List<PollVote> pollVotes = new ArrayList<>();    // tous les votes de sondage

    // Pour générer des ID de sondage uniques
    private static int nextPollId = 1;

    // Chemins des fichiers JSON
    private static final String ELECTEURS_FILE = "src/data/electeurs.json";
    private static final String CANDIDATS_FILE = "src/data/user.json";
    private static final String POLLS_FILE     = "src/data/polls.json";
    private static final String POLL_VOTES_FILE = "src/data/poll_votes.json";

    // ------------------------------------------------------------
    // CHARGEMENT GLOBAL (appelé au démarrage du serveur)
    // ------------------------------------------------------------
    public static void chargerDonnees() {
        chargerElecteurs();
        chargerCandidats();
        chargerPolls();          // ← NOUVEAU
        chargerPollVotes();      // ← NOUVEAU
    }

    // ------------------------------------------------------------
    // Méthodes existantes (inchangées)
    // ------------------------------------------------------------
    private static void chargerElecteurs() {
        try {
            JsonArray array = JsonParser.parseReader(new FileReader(ELECTEURS_FILE)).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                Electeur el = new Electeur(
                        o.get("id").getAsString(),
                        o.get("code").getAsString()
                );
                electeurs.put(el.getCode(), el);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement électeurs : " + e.getMessage());
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

    // ------------------------------------------------------------
    // NOUVELLES MÉTHODES : Sondages (POLLS)
    // ------------------------------------------------------------

    /**
     * Charge tous les sondages depuis polls.json.
     * Si le fichier n'existe pas, crée le sondage par défaut (ID=1) avec les candidats existants.
     */
    public static void chargerPolls() {
        File file = new File(POLLS_FILE);
        if (!file.exists()) {
            creerPollParDefaut();
            sauvegarderPolls();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();

                int id = o.get("id").getAsInt();
                String titre = o.get("titre").getAsString();
                boolean ouvert = o.get("ouvert").getAsBoolean();

                // Lecture des options
                List<Option> options = new ArrayList<>();
                JsonArray optArray = o.getAsJsonArray("options");
                for (JsonElement optElem : optArray) {
                    JsonObject optObj = optElem.getAsJsonObject();
                    int optId = optObj.get("id").getAsInt();
                    String libelle = optObj.get("libelle").getAsString();
                    options.add(new Option(optId, libelle));
                }

                Poll poll = new Poll(id, titre, options, ouvert);
                polls.put(id, poll);
            }

            // Mise à jour du prochain ID disponible
            if (!polls.isEmpty()) {
                nextPollId = polls.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
            } else {
                nextPollId = 1;
            }

        } catch (Exception e) {
            System.out.println("Erreur chargement sondages : " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, on recrée le sondage par défaut
            creerPollParDefaut();
            sauvegarderPolls();
        }
    }


    private static void creerPollParDefaut() 
    {
        List<Option> options = new ArrayList<>();
        int optId = 1;
        for (Candidat c : candidats.values()) 
        {
            options.add(new Option(optId++, c.getNom()));
        }
        Poll defaultPoll = new Poll(1, "Élection par défaut", options, true);
        polls.put(1, defaultPoll);
        nextPollId = 2; // prochain ID disponible
        System.out.println("[DataStore] Sondage par défaut créé (ID=1).");
    }


    public static void sauvegarderPolls() 
    {
        JsonArray array = new JsonArray();

        for (Poll poll : polls.values()) 
        {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", poll.getId());
            obj.addProperty("titre", poll.getTitre());
            obj.addProperty("ouvert", poll.isOuvert());

            JsonArray optArray = new JsonArray();
            for (Option opt : poll.getOptions()) {
                JsonObject optObj = new JsonObject();
                optObj.addProperty("id", opt.getId());
                optObj.addProperty("libelle", opt.getLibelle());
                optArray.add(optObj);
            }
            obj.add("options", optArray);
            array.add(obj);
        }

        try (FileWriter writer = new FileWriter(POLLS_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(array, writer);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde polls.json : " + e.getMessage());
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
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement votes de sondage : " + e.getMessage());
            e.printStackTrace();
            pollVotes = new ArrayList<>();
        }
    }

   
    public static void sauvegarderPollVotes() 
    {
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
        sauvegarderPollVotes();
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
}