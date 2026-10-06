package ru.lifeschess.engine;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LifeChessFenTest {
    @Test public void initialPositionPreservesVariantRightsAndExtensions() {
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQVkqv - 0 1 lc1:- lm1:0000000000000000",
                LifeChessFen.encode(GameState.initial()));
    }

    @Test public void drawResponseSearchChangesOnlyTheEncodedSideToMove() {
        GameState initial = GameState.initial();

        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQVkqv - 0 1 lc1:- lm1:0000000000000000",
                LifeChessFen.encode(initial, Side.BLACK));
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQVkqv - 0 1 lc1:- lm1:0000000000000000",
                LifeChessFen.encode(initial));
        assertEquals(Side.WHITE, initial.toMove);
    }

    @Test public void movedHomePawnIsEncodedInLifeChessBitboardExtension() {
        Board board = new Board().with(6, 4, new Piece(Side.WHITE, PieceType.PAWN, true))
                .with(7, 4, new Piece(Side.WHITE, PieceType.KING, false))
                .with(0, 4, new Piece(Side.BLACK, PieceType.KING, false));
        GameState state = new GameState(board, Side.WHITE, -1, null, 12,
                false, false, false, false, false, false,
                GameResult.NONE, null, null);

        assertTrue(LifeChessFen.encode(state).endsWith("lc1:- lm1:0000000000001000"));
    }
}
