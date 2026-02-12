package model;

public class Electeur {
    private String id;
    private String code;
    private boolean aVote;
    private boolean estValable;

    public Electeur(String id, String code) {
        this.id = id;
        this.code = code;
        this.aVote = false;
    }

    public Electeur(String id, String code, boolean aVote, boolean estValable) {
        this.id = id;
        this.code = code;
        this.aVote = aVote;
        this.estValable = estValable;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public boolean isAVote() {
        return aVote;
    }

    public void setAVote(boolean aVote) {
        this.aVote = aVote;
    }

    public boolean isEstValable() {
        return estValable;
    }

    public void setEstValable(boolean estValable) {
        this.estValable = estValable;
    }
}
