package model;

public class PollVote {
    private String electeurId;  
    private int pollId;
    private int optionId;

    public String getElecteurId() {
        return electeurId;
    }

    public void setElecteurId(String electeurId) {
        this.electeurId = electeurId;
    }

    public int getPollId() {
        return pollId;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public PollVote(String electeurId, int pollId, int optionId, String timestamp) {
        this.electeurId = electeurId;
        this.pollId = pollId;
        this.optionId = optionId;
        this.timestamp = timestamp;
    }

    private String timestamp;

}