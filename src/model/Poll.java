package model;

import java.time.LocalDateTime;
import java.util.List;

public class Poll {

    private int id;
    private String titre;
    private List<Option> options;
    private boolean ouvert;
    private LocalDateTime deadline;   // ✅ NOUVEAU

    public Poll(int id, String titre, List<Option> options, boolean ouvert, LocalDateTime deadline) {
        this.id = id;
        this.titre = titre;
        this.options = options;
        this.ouvert = ouvert;
        this.deadline = deadline;  // ✅
    }

    public int getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public List<Option> getOptions() {
        return options;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    // ✅ Vérifie si le sondage est encore valide
    public boolean estEncoreValide() {
        return deadline == null || LocalDateTime.now().isBefore(deadline);
    }
}