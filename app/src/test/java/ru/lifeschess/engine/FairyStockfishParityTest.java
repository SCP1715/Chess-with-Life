package ru.lifeschess.engine;

import org.junit.Assume;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/** Optional host-side differential tests; set LIFECHESS_ENGINE to the built engine. */
public class FairyStockfishParityTest {
    private static final String ENGINE = System.getenv("LIFECHESS_ENGINE");

    @Test public void legalMoveSetsMatchOnRuleControlPositions() throws Exception {
        Assume.assumeTrue("Set LIFECHESS_ENGINE to run Java/C++ parity checks",
                ENGINE != null && new File(ENGINE).isFile());

        assertParity(GameState.initial());
        assertParity(state(Side.BLACK, Side.WHITE,
                new String[]{"a8:WK", "h8:BK", "a4:BR", "a1:WK"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"a7:BR", "b7:WP", "h7:WK", "h6:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"e8:WR", "h8:BK", "e1:WK"},
                false, false, true, false, false, false));
    }

    @Test public void legalMoveSetsMatchOnEndgameTableControls() throws Exception {
        Assume.assumeTrue("Set LIFECHESS_ENGINE to run Java/C++ parity checks",
                ENGINE != null && new File(ENGINE).isFile());

        assertParity(state(Side.WHITE, null,
                new String[]{"e4:WK", "e5:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"a1:WK", "h8:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.BLACK, null,
                new String[]{"e6:WK", "e7:WB", "e8:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.BLACK, null,
                new String[]{"c7:WK", "b5:WN", "a8:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"e5:WK", "e7:WB", "e8:BK", "e6:BB"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"e1:WK", "e2:WB", "h8:BK"},
                false, false, false, false, false, false));
        assertParity(state(Side.WHITE, null,
                new String[]{"b7:WK", "f3:WB", "h2:BK", "a1:BB"},
                false, false, false, false, false, false));
    }

    @Test public void legalMoveSetsMatchAcrossSeededGameSequence() throws Exception {
        Assume.assumeTrue("Set LIFECHESS_ENGINE to run Java/C++ parity checks",
                ENGINE != null && new File(ENGINE).isFile());
        Random random = new Random(0x4c494645L);
        GameState state = GameState.initial();
        for (int ply = 0; ply < 48; ply++) {
            assertParityFromInitialHistory(state);
            List<GameAction> moves = new ArrayList<>();
            for (GameAction action : RuleEngine.legalActions(state))
                if (action.type == GameAction.Type.MOVE) moves.add(action);
            if (moves.isEmpty()) break;
            state = RuleEngine.applyAction(state, moves.get(random.nextInt(moves.size())));
        }
        assertEquals("Seeded parity sequence ended before 48 half-moves", 48, state.history.size());
    }

    private static void assertParity(GameState state) throws Exception {
        assertParity(state, "position fen " + fen(state));
    }

    private static void assertParityFromInitialHistory(GameState state) throws Exception {
        StringBuilder command = new StringBuilder("position startpos");
        StringBuilder moves = new StringBuilder();
        for (GameAction action : state.history) {
            if (action.type != GameAction.Type.MOVE) continue;
            if (moves.length() != 0) moves.append(' ');
            moves.append(actionKey(action));
        }
        if (moves.length() != 0) command.append(" moves ").append(moves);
        assertParity(state, command.toString());
    }

    private static void assertParity(GameState state, String positionCommand) throws Exception {
        Set<String> javaMoves = new HashSet<>();
        for (GameAction action : RuleEngine.legalActions(state))
            if (action.type == GameAction.Type.MOVE) javaMoves.add(actionKey(action));
        Set<String> cppMoves = queryPerftOne(positionCommand);
        assertEquals("Legal moves differ for " + fen(state), javaMoves, cppMoves);
    }

    private static Set<String> queryPerftOne(String positionCommand) throws Exception {
        Process process = new ProcessBuilder(ENGINE).redirectErrorStream(true).start();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
                     process.getOutputStream(), StandardCharsets.UTF_8))) {
            output.write("uci\n");
            output.flush();
            readThrough(input, "uciok");
            output.write("setoption name UCI_Variant value lifechess\n");
            output.write("isready\n");
            output.flush();
            readThrough(input, "readyok");
            output.write(positionCommand + "\n");
            output.write("go perft 1\n");
            output.flush();

            Set<String> moves = new HashSet<>();
            String line;
            while ((line = input.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.matches("[a-h][1-8][a-h][1-8][qrbnk]?: 1"))
                    moves.add(trimmed.substring(0, trimmed.indexOf(':')));
                if (trimmed.startsWith("Nodes searched:")) break;
            }
            output.write("quit\n");
            output.flush();
            if (!process.waitFor(10, TimeUnit.SECONDS))
                throw new AssertionError("Fairy-Stockfish did not exit after perft");
            return moves;
        } finally {
            process.destroyForcibly();
        }
    }

    private static void readThrough(BufferedReader input, String expected) throws Exception {
        String line;
        while ((line = input.readLine()) != null)
            if (line.trim().equals(expected)) return;
        throw new AssertionError("Fairy-Stockfish exited before " + expected);
    }

    private static String actionKey(GameAction action) {
        String value = square(action.from) + square(action.to);
        if (action.promotion != null) {
            switch (action.promotion) {
                case KING: value += "k"; break;
                case QUEEN: value += "q"; break;
                case ROOK: value += "r"; break;
                case BISHOP: value += "b"; break;
                case KNIGHT: value += "n"; break;
                default: throw new AssertionError("Invalid promotion type");
            }
        }
        return value;
    }

    private static String fen(GameState state) {
        StringBuilder value = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            if (row != 0) value.append('/');
            int empty = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = state.board.at(row, col);
                if (piece == null) { empty++; continue; }
                if (empty != 0) { value.append(empty); empty = 0; }
                char letter;
                switch (piece.type) {
                    case KING: letter = 'k'; break;
                    case QUEEN: letter = 'q'; break;
                    case ROOK: letter = 'r'; break;
                    case BISHOP: letter = 'b'; break;
                    case KNIGHT: letter = 'n'; break;
                    case PAWN: letter = 'p'; break;
                    default: throw new AssertionError("Unknown piece type");
                }
                value.append(piece.side == Side.WHITE ? Character.toUpperCase(letter) : letter);
            }
            if (empty != 0) value.append(empty);
        }
        value.append(state.toMove == Side.WHITE ? " w " : " b ");
        StringBuilder rights = new StringBuilder();
        if (state.whiteKingSide) rights.append('K');
        if (state.whiteQueenSide) rights.append('Q');
        if (state.whiteVertical) rights.append('V');
        if (state.blackKingSide) rights.append('k');
        if (state.blackQueenSide) rights.append('q');
        if (state.blackVertical) rights.append('v');
        value.append(rights.length() == 0 ? "-" : rights.toString()).append(' ');
        value.append(state.enPassantTarget < 0 ? "-" : square(state.enPassantTarget));
        value.append(' ').append(state.halfMovesSinceCaptureOrPawn).append(" 1 lc1:")
                .append(state.debtTargetKings == null ? "-"
                        : state.debtTargetKings == Side.WHITE ? "w" : "b");
        return value.toString();
    }

    private static String square(int index) {
        return "" + (char) ('a' + Board.col(index)) + (8 - Board.row(index));
    }

    private static GameState state(Side turn, Side debt, String[] pieces,
                                  boolean wk, boolean wq, boolean wv,
                                  boolean bk, boolean bq, boolean bv) {
        Board board = new Board();
        for (String entry : pieces) {
            String[] pair = entry.split(":");
            String cell = pair[1];
            Side side = cell.charAt(0) == 'W' ? Side.WHITE : Side.BLACK;
            PieceType type;
            switch (cell.charAt(1)) {
                case 'K': type = PieceType.KING; break;
                case 'Q': type = PieceType.QUEEN; break;
                case 'R': type = PieceType.ROOK; break;
                case 'B': type = PieceType.BISHOP; break;
                case 'N': type = PieceType.KNIGHT; break;
                case 'P': type = PieceType.PAWN; break;
                default: throw new AssertionError("Invalid fixture piece: " + cell);
            }
            board = board.with(index(pair[0]), new Piece(side, type, false));
        }
        return new GameState(board, turn, -1, debt, 0, wk, wq, wv, bk, bq, bv,
                GameResult.NONE, null, java.util.Collections.<String, Integer>emptyMap());
    }

    private static int index(String coordinate) {
        return (8 - (coordinate.charAt(1) - '0')) * 8 + coordinate.charAt(0) - 'a';
    }
}
