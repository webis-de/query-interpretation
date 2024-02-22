package de.webis.query.interpretation.datastructures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Query {
    private String id;
    private String text;
    private Set<String> categories;
    private int difficulty;
    private List<Entity> annotations;

    public Query() {
        annotations = new ArrayList<>();
    }

    public Query(String text) {
        this.text = text;
        this.id = String.valueOf(text.hashCode());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public List<Entity> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Entity> annotations) {
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return text;
    }
}
