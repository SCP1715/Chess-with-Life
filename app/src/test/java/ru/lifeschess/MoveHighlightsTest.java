package ru.lifeschess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.Piece;
import ru.lifeschess.engine.PieceType;
import ru.lifeschess.engine.Side;

public class MoveHighlightsTest {
    @Test public void occupiedDestinationIsMarkedAsCaptureEvenForFriendlyKingCapture() {
        int friendlyKing = Board.index(3, 3);
        int enemyQueen = Board.index(4, 4);
        Board board = new Board()
                .with(friendlyKing, new Piece(Side.WHITE, PieceType.KING, true))
                .with(enemyQueen, new Piece(Side.WHITE, PieceType.KING, true));

        MoveHighlights highlights = MoveHighlights.from(board,
                Arrays.asList(new Move(friendlyKing, enemyQueen)));

        assertTrue(highlights.targets.contains(enemyQueen));
        assertTrue(highlights.captures.contains(enemyQueen));
    }

    @Test public void enPassantIsCaptureEvenThoughDestinationIsEmpty() {
        int target = Board.index(2, 3);
        MoveHighlights highlights = MoveHighlights.from(new Board(),
                Arrays.asList(new Move(Board.index(3, 2), target, Move.Kind.EN_PASSANT)));

        assertEquals(1, highlights.targets.size());
        assertTrue(highlights.captures.contains(target));
    }

    @Test public void emptyMoveKeepsTheQuietMoveMarker() {
        int target = Board.index(4, 4);
        MoveHighlights highlights = MoveHighlights.from(new Board(),
                Arrays.asList(new Move(Board.index(3, 3), target)));

        assertTrue(highlights.targets.contains(target));
        assertFalse(highlights.captures.contains(target));
    }
}
