package ru.lifeschess.engine;

/** Уровни намеренного ослабления Fairy-Stockfish; шкала не откалибрована для варианта. */
public final class BotDifficulty {
    public static final int DEFAULT_CONDITIONAL_ELO = 1_000;
    private static final int[] LEVELS = {500, 800, 1_000, 1_200, 1_350,
            1_600, 1_900, 2_200, 2_500, 2_850};

    private BotDifficulty() { }

    public static int[] levels() { return LEVELS.clone(); }

    public static int selectionFor(int conditionalElo) {
        for (int i = 0; i < LEVELS.length; i++)
            if (LEVELS[i] == conditionalElo) return i;
        return 2;
    }
}
