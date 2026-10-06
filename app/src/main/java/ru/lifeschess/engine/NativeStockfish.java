package ru.lifeschess.engine;

/** JNI entry point for the in-process Fairy-Stockfish library. */
public final class NativeStockfish {
    static {
        System.loadLibrary("lifechess");
    }

    private NativeStockfish() { }

    public static native String nativeEngineVersion();
    public static native String nativeLegalMoves(String fen);
    public static native String nativeSearch(long requestId, long stateRevision, String gameId,
                                             String rootFen, String moves, int nodeBudget,
                                             boolean opponentOffered,
                                             int claimMask,
                                             boolean offerAlreadySent,
                                             boolean analysisMode);
    public static native void nativeCancel(long requestId);

    public static String legalMoves(GameState state) {
        return nativeLegalMoves(LifeChessFen.encode(state));
    }

    public static String search(long requestId, long stateRevision, String gameId,
                                GameState state, int nodeBudget, boolean opponentOfferedDraw,
                                boolean drawOfferAlreadySent, boolean analysisMode) {
        if (state == null || state.searchRootFen == null)
            return "ERROR|SEARCH_HISTORY_UNAVAILABLE";
        if (nodeBudget <= 0)
            return "ERROR|INVALID_SEARCH_NODE_BUDGET";
        String moves = String.join(" ", state.searchMoves);
        int claimMask = 0;
        if (ru.lifeschess.engine.DrawDetector.canClaimRepetition(state)) claimMask |= 1;
        if (ru.lifeschess.engine.DrawDetector.canClaimFiftyMoves(state)) claimMask |= 2;
        if (ru.lifeschess.engine.DrawDetector.canClaimBareKings(state)) claimMask |= 4;
        return nativeSearch(requestId, stateRevision, gameId, state.searchRootFen, moves,
                nodeBudget, opponentOfferedDraw,
                claimMask, drawOfferAlreadySent, analysisMode);
    }

    /** Searches from the bot's side to decide an agreement without taking a game turn. */
    public static String searchDrawResponse(long requestId, long stateRevision, String gameId,
                                            GameState state, Side botSide, int nodeBudget) {
        if (state == null || botSide == null)
            return "ERROR|DRAW_RESPONSE_STATE_UNAVAILABLE";
        if (nodeBudget <= 0)
            return "ERROR|INVALID_SEARCH_NODE_BUDGET";
        return nativeSearch(requestId, stateRevision, gameId,
                LifeChessFen.encode(state, botSide), "", nodeBudget, true, 0,
                state.drawOfferSentInCurrentNonWinningStretch, false);
    }
}
