package model;

public class Electeur {
    private String id;
    private String code;
    private boolean aVote;

    public Electeur(String id, String code) {
        this.id = id;
        this.code = code;
        this.aVote = false;
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
}
