package model;

import java.util.HashSet;
import java.util.Set;

public class Electeur {
    private String id;
    private String code;
    private boolean aVote;             // pour le vote classique
    private boolean estValable;
    private Set<Integer> sondagesVotes; // IDs des sondages votés

    public Electeur(String id, String code) {
        this.id = id;
        this.code = code;
        this.aVote = false;
        this.sondagesVotes = new HashSet<>();
    }

    public Electeur(String id, String code, boolean aVote, boolean estValable) {
        this.id = id;
        this.code = code;
        this.aVote = aVote;
        this.estValable = estValable;
        this.sondagesVotes = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    // ----- Vote classique -----
    public boolean isAVote() {
        return aVote;
    }

    public void setAVote(boolean aVote) {
        this.aVote = aVote;
    }

    // ----- Statut -----
    public boolean isEstValable() {
        return estValable;
    }

    public void setEstValable(boolean estValable) {
        this.estValable = estValable;
    }

    // ----- Vote sondages -----
    public Set<Integer> getSondagesVotes() {
        return sondagesVotes;
    }

    public boolean aVoteSondage(int pollId) {
        return sondagesVotes.contains(pollId);
    }

    public void ajouterSondageVote(int pollId) {
        sondagesVotes.add(pollId);
    }
}