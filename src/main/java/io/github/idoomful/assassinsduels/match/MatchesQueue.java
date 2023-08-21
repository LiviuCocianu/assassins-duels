package io.github.idoomful.assassinsduels.match;

import java.util.LinkedList;
import java.util.Queue;

public class MatchesQueue {
    private final Queue<Match> matches = new LinkedList<>();

    public Queue<Match> getMatches() {
        return matches;
    }
}
