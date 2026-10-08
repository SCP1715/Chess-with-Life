package ru.lifeschess.engine;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BotDifficultyTest {
    @Test public void offeredLevelsAreOrderedAndWithinTheEngineRange() {
        int[] levels = BotDifficulty.levels();
        assertArrayEquals(new int[]{500, 800, 1_000, 1_200, 1_350,
                1_600, 1_900, 2_200, 2_500, 2_850}, levels);
        for (int i = 0; i < levels.length; i++) {
            assertTrue(levels[i] >= 500 && levels[i] <= 2_850);
            if (i > 0) assertTrue(levels[i] > levels[i - 1]);
        }
    }

    @Test public void defaultDifficultyHasAVisibleSelection() {
        assertEquals(2, BotDifficulty.selectionFor(BotDifficulty.DEFAULT_CONDITIONAL_ELO));
    }

    @Test public void unknownSavedDifficultyFallsBackToDefault() {
        assertEquals(2, BotDifficulty.selectionFor(999));
    }
}
