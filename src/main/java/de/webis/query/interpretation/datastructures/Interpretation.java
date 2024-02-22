package de.webis.query.interpretation.datastructures;

import java.util.*;
import java.util.regex.Pattern;

public class Interpretation {
    private int id;
    private List<String> interpretation;
    private double relevance;

    private final List<String> containedEntities;
    private final Set<String> contextWords;

    private static final Pattern WIKI_URL_PATTERN = Pattern.compile("^http(s)?://en.wikipedia.org/wiki/(.)*");

    public Interpretation() {
        containedEntities = new ArrayList<>();
        contextWords = new HashSet<>();
    }

    public Interpretation(List<String> interpretation) {
        containedEntities = new ArrayList<>();
        contextWords = new HashSet<>();
        setInterpretation(interpretation);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(List<String> interpretation) {
        this.interpretation = interpretation;

        contextWords.clear();
        containedEntities.clear();
        for (String part : interpretation) {
            if (WIKI_URL_PATTERN.matcher(part).matches()) {
                containedEntities.add(part);
            } else {
                contextWords.addAll(Arrays.asList(part.split("\\s")));
            }
        }
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public List<String> getContainedEntities() {
        return containedEntities;
    }

    public Set<String> getContextWords() {
        return contextWords;
    }

    public double getScore() {
        return getRelevance();
    }

    @Override
    public int hashCode() {
        return String.join("|", interpretation)
                .replaceAll("http(s)?://en.wikipedia.org/wiki/", "")
                .trim().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Interpretation)){
            return false;
        }

        return this.hashCode() == obj.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%-200s | %2.4f", String.join(" | ", interpretation), relevance);
    }
}
