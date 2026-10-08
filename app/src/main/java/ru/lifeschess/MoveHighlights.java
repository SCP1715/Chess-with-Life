package ru.lifeschess;

import java.util.HashSet;
import java.util.Set;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;

final class MoveHighlights {
    final Set<Integer> targets = new HashSet<>();
    final Set<Integer> captures = new HashSet<>();

    static MoveHighlights from(Board board, Iterable<Move> moves) {
        MoveHighlights highlights = new MoveHighlights();
        if (moves == null) return highlights;
        for (Move move : moves) {
            highlights.targets.add(move.to);
            if (board.at(move.to) != null || move.kind == Move.Kind.EN_PASSANT)
                highlights.captures.add(move.to);
        }
        return highlights;
    }
}
