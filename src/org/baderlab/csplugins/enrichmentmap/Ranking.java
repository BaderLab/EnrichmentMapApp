package org.baderlab.csplugins.enrichmentmap;

/**
 * Created by
 * User: risserlin
 * Date: May 1, 2009
 * Time: 10:42:44 AM
 */
public class Ranking {

    private String Name;

    private Double Score;

    private Integer Rank;

    public Ranking(String name, Double score) {
        Name = name;
        Score = score;
    }

    public Ranking(String name, Double score, Integer rank) {
        Name = name;
        Score = score;
        Rank = rank;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Double getScore() {
        return Score;
    }

    public void setScore(Double score) {
        Score = score;
    }

    public Integer getRank() {
        return Rank;
    }

    public void setRank(Integer rank) {
        Rank = rank;
    }
}
