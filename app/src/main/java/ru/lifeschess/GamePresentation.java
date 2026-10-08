package ru.lifeschess;

final class GamePresentation {
    private GamePresentation() { }

    static int boardCoordinate(int coordinate, boolean flipped) {
        return flipped ? 7 - coordinate : coordinate;
    }

    static float whiteShare(int score, boolean mate) {
        int boundedScore = Math.max(-3000, Math.min(3000, score));
        if (mate) boundedScore = Integer.compare(score, 0) * 3000;
        return (float) (1.0 / (1.0 + Math.exp(-boundedScore / 400.0)));
    }
}
