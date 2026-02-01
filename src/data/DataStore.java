package data;

import model.*;
import com.google.gson.*;
import java.io.FileReader;
import java.util.*;

public class DataStore {

    public static Map<String, Electeur> electeurs = new HashMap<>();
    public static Map<Integer, Candidat> candidats = new HashMap<>();
    public static List<Vote> votes = new ArrayList<>();

    public static boolean voteOuvert = true;

    public static void chargerDonnees() {
        chargerElecteurs();
        chargerCandidats();
    }

    private static void chargerElecteurs() {
        try {
            JsonArray array = JsonParser.parseReader(
                    new FileReader("src/data/electeurs.json")
            ).getAsJsonArray();

            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                Electeur el = new Electeur(
                        o.get("id").getAsString(),
                        o.get("code").getAsString()
                );
                electeurs.put(el.getCode(), el);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement électeurs");
            e.printStackTrace();
        }
    }

    private static void chargerCandidats() {
        try {
            JsonArray array = JsonParser.parseReader(
                    new FileReader("src/data/user.json")
            ).getAsJsonArray();

            for (JsonElement e : array) {
                JsonObject o = e.getAsJsonObject();
                Candidat c = new Candidat(
                        o.get("id").getAsInt(),
                        o.get("nom").getAsString()
                );
                candidats.put(c.getId(), c);
            }
        } catch (Exception e) {
            System.out.println("Erreur chargement candidats");
            e.printStackTrace();
        }
    }
}
