package de.webis.query.interpretation.strategies;

import de.webis.query.interpretation.datastructures.Query;

import java.util.Set;

public interface TokenizationStrategy {
    Set<String> apply(Query query);
}
