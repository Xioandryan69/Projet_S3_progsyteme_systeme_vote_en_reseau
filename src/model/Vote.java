package model;

public class Vote {
    private String electeurId;
    private int candidatId;

    public Vote(String electeurId, int candidatId) {
        this.electeurId = electeurId;
        this.candidatId = candidatId;
    }

    public String getElecteurId() {
        return electeurId;
    }

    public int getCandidatId() {
        return candidatId;
    }
}
