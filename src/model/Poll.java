package model;

import java.util.*;

public class Poll
{
    private int id;

    public Poll(int id, String titre, List<Option> options, boolean ouvert) {
        this.id = id;
        this.titre = titre;
        this.options = options;
        this.ouvert = ouvert;
    }

    private String titre;
    private List<Option> options;
    private boolean ouvert; // par défaut true

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public void setOuvert(boolean ouvert) {
        this.ouvert = ouvert;
    }
}
