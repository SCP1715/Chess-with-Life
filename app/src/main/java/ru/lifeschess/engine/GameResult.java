package ru.lifeschess.engine;

import java.io.Serializable;

public enum GameResult implements Serializable {
    NONE, WHITE_WIN, BLACK_WIN, DRAW_REPETITION, DRAW_FIFTY_MOVES, DRAW_AGREEMENT
}
