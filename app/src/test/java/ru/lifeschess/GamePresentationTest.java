package ru.lifeschess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GamePresentationTest {
    @Test public void flippingRotatesBothBoardAxes() {
        assertEquals(7, GamePresentation.boardCoordinate(0, true));
        assertEquals(0, GamePresentation.boardCoordinate(7, true));
        assertEquals(3, GamePresentation.boardCoordinate(3, true));
        assertEquals(0, GamePresentation.boardCoordinate(0, false));
    }

    @Test public void evaluationBarIsBalancedAtEqualScoreAndMovesForWhiteAdvantage() {
        assertEquals(.5f, GamePresentation.whiteShare(0, false), .001f);
        assertTrue(GamePresentation.whiteShare(300, false) > .5f);
        assertTrue(GamePresentation.whiteShare(-300, false) < .5f);
    }

    @Test public void mateScoresReachTheCorrectExtreme() {
        assertTrue(GamePresentation.whiteShare(100, true) > .99f);
        assertTrue(GamePresentation.whiteShare(-100, true) < .01f);
    }
}
