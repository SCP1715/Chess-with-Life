package ru.lifeschess.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/** Versioned, lossless JSON representation for local engine/test exchanges. */
public final class LifeChessProtocol {
    public static final String NAME = "lifechess";
    public static final int VERSION = 2;
    private static final Pattern REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    public enum Operation { LEGAL_ACTIONS, APPLY_ACTION, SEARCH, EXPORT_STATE }
    public enum ResponseKind { LEGAL_ACTIONS, STATE_UPDATED, SEARCH_RESULT, DRAW_ACTION, GAME_OVER, CANCELLED, ERROR }

    public static final class Request {
        public final String requestId;
        public final long stateRevision;
        public final Operation operation;
        public final GameState state;
        public final GameAction action;

        public Request(String requestId, long stateRevision, Operation operation,
                       GameState state, GameAction action) {
            validateRequestId(requestId);
            if (stateRevision < 0) throw new IllegalArgumentException("Negative state revision");
            if (operation == null || state == null) throw new IllegalArgumentException("Operation and state required");
            if ((operation == Operation.APPLY_ACTION) != (action != null))
                throw new IllegalArgumentException("Only APPLY_ACTION carries an action");
            this.requestId = requestId;
            this.stateRevision = stateRevision;
            this.operation = operation;
            this.state = state;
            this.action = action;
        }
    }

    public static final class Snapshot {
        public final String requestId;
        public final long stateRevision;
        public final GameState state;

        private Snapshot(String requestId, long stateRevision, GameState state) {
            this.requestId = requestId;
            this.stateRevision = stateRevision;
            this.state = state;
        }
    }

    public static final class Response {
        public final String requestId;
        public final long stateRevision;
        public final ResponseKind kind;
        public final GameState state;
        public final List<GameAction> legalActions;
        public final GameAction selectedAction;
        public final String errorCode;
        public final String message;

        public Response(String requestId, long stateRevision, ResponseKind kind,
                        GameState state, List<GameAction> legalActions,
                        GameAction selectedAction, String errorCode, String message) {
            validateRequestId(requestId);
            if (stateRevision < 0 || kind == null) throw new IllegalArgumentException("Invalid response metadata");
            if (kind == ResponseKind.LEGAL_ACTIONS && legalActions == null)
                throw new IllegalArgumentException("LEGAL_ACTIONS response requires an action list");
            if (kind == ResponseKind.ERROR && (errorCode == null || message == null))
                throw new IllegalArgumentException("ERROR response requires code and message");
            this.requestId = requestId;
            this.stateRevision = stateRevision;
            this.kind = kind;
            this.state = state;
            this.legalActions = Collections.unmodifiableList(new ArrayList<>(
                    legalActions == null ? Collections.<GameAction>emptyList() : legalActions));
            this.selectedAction = selectedAction;
            this.errorCode = errorCode;
            this.message = message;
        }
    }

    private LifeChessProtocol() { }

    public static String encodeRequest(Request request) {
        StringBuilder out = new StringBuilder(4096);
        out.append('{');
        field(out, "protocol", NAME).append(',');
        numberField(out, "version", VERSION).append(',');
        field(out, "requestId", request.requestId).append(',');
        numberField(out, "stateRevision", request.stateRevision).append(',');
        field(out, "variant", NAME).append(',');
        field(out, "operation", request.operation.name()).append(',');
        out.append("\"state\":");
        appendState(out, request.state);
        out.append(",\"action\":");
        if (request.action == null) out.append("null");
        else appendAction(out, request.action);
        return out.append('}').toString();
    }

    public static Request decodeRequest(String json) {
        Map<String, Object> root = object(Json.parse(json), "request");
        requireProtocol(root);
        String requestId = string(root, "requestId");
        long revision = integer(root, "stateRevision");
        Operation operation = enumValue(Operation.class, string(root, "operation"), "operation");
        GameState state = decodeState(object(required(root, "state"), "state"));
        Object rawAction = required(root, "action");
        GameAction action = rawAction == null ? null : decodeAction(object(rawAction, "action"));
        return new Request(requestId, revision, operation, state, action);
    }

    public static String encodeSnapshot(String requestId, long revision, GameState state) {
        validateRequestId(requestId);
        if (revision < 0 || state == null) throw new IllegalArgumentException("Invalid snapshot metadata");
        StringBuilder out = new StringBuilder(4096);
        out.append('{');
        field(out, "protocol", NAME).append(',');
        numberField(out, "version", VERSION).append(',');
        field(out, "requestId", requestId).append(',');
        numberField(out, "stateRevision", revision).append(',');
        field(out, "variant", NAME).append(',');
        out.append("\"state\":");
        appendState(out, state);
        return out.append('}').toString();
    }

    public static Snapshot decodeSnapshot(String json) {
        Map<String, Object> root = object(Json.parse(json), "snapshot");
        requireProtocol(root);
        String requestId = string(root, "requestId");
        validateRequestId(requestId);
        long revision = integer(root, "stateRevision");
        if (revision < 0) throw new IllegalArgumentException("Negative state revision");
        return new Snapshot(requestId, revision, decodeState(object(required(root, "state"), "state")));
    }

    public static String encodeResponse(Response response) {
        StringBuilder out = new StringBuilder(4096);
        out.append('{');
        field(out, "protocol", NAME).append(',');
        numberField(out, "version", VERSION).append(',');
        field(out, "requestId", response.requestId).append(',');
        numberField(out, "stateRevision", response.stateRevision).append(',');
        field(out, "variant", NAME).append(',');
        field(out, "kind", response.kind.name()).append(',');
        out.append("\"state\":");
        if (response.state == null) out.append("null"); else appendState(out, response.state);
        out.append(",\"legalActions\":[");
        for (int i = 0; i < response.legalActions.size(); i++) {
            if (i != 0) out.append(',');
            appendAction(out, response.legalActions.get(i));
        }
        out.append("],\"selectedAction\":");
        if (response.selectedAction == null) out.append("null"); else appendAction(out, response.selectedAction);
        out.append(",\"errorCode\":");
        if (response.errorCode == null) out.append("null"); else quote(out, response.errorCode);
        out.append(",\"message\":");
        if (response.message == null) out.append("null"); else quote(out, response.message);
        return out.append('}').toString();
    }

    public static Response decodeResponse(String json) {
        Map<String, Object> root = object(Json.parse(json), "response");
        requireProtocol(root);
        String requestId = string(root, "requestId");
        long revision = integer(root, "stateRevision");
        ResponseKind kind = enumValue(ResponseKind.class, string(root, "kind"), "response kind");
        Object rawState = required(root, "state");
        GameState state = rawState == null ? null : decodeState(object(rawState, "state"));
        List<GameAction> actions = new ArrayList<>();
        for (Object action : array(required(root, "legalActions"), "legalActions"))
            actions.add(decodeAction(object(action, "legal action")));
        Object rawSelected = required(root, "selectedAction");
        GameAction selected = rawSelected == null ? null : decodeAction(object(rawSelected, "selectedAction"));
        Object rawCode = required(root, "errorCode");
        String code = rawCode == null ? null : asString(rawCode, "errorCode");
        Object rawMessage = required(root, "message");
        String message = rawMessage == null ? null : asString(rawMessage, "message");
        return new Response(requestId, revision, kind, state, actions, selected, code, message);
    }

    public static String encodeAction(GameAction action) {
        StringBuilder out = new StringBuilder(128);
        appendAction(out, action);
        return out.toString();
    }

    public static GameAction decodeAction(String json) {
        return decodeAction(object(Json.parse(json), "action"));
    }

    private static void requireProtocol(Map<String, Object> root) {
        if (!NAME.equals(string(root, "protocol")) || integer(root, "version") != VERSION
                || !NAME.equals(string(root, "variant")))
            throw new IllegalArgumentException("Unsupported LifeChess protocol, version, or variant");
    }

    private static void validateRequestId(String id) {
        if (id == null || !REQUEST_ID.matcher(id).matches())
            throw new IllegalArgumentException("Invalid requestId");
    }

    private static void appendState(StringBuilder out, GameState state) {
        out.append('{').append("\"board\":[");
        for (int i = 0; i < 64; i++) {
            if (i != 0) out.append(',');
            Piece p = state.board.at(i);
            if (p == null) quote(out, ".");
            else quote(out, (p.side == Side.WHITE ? "w" : "b") + pieceCode(p.type) + (p.hasMoved ? "1" : "0"));
        }
        out.append("],");
        field(out, "toMove", state.toMove.name()).append(',');
        out.append("\"enPassant\":");
        if (state.enPassantTarget < 0) out.append("null");
        else quote(out, squareName(state.enPassantTarget));
        out.append(',');
        nullableEnumField(out, "debtTargetKings", state.debtTargetKings).append(',');
        numberField(out, "halfMovesSinceCaptureOrPawn", state.halfMovesSinceCaptureOrPawn).append(',');
        field(out, "castlingRights", rights(state)).append(',');
        field(out, "result", state.result.name()).append(',');
        nullableEnumField(out, "drawOfferBy", state.drawOfferBy).append(',');
        out.append("\"drawOfferSentInCurrentNonWinningStretch\":")
                .append(state.drawOfferSentInCurrentNonWinningStretch).append(',');
        out.append("\"repetitions\":[");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : new TreeMap<>(state.repetitions).entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('{');
            field(out, "key", entry.getKey()).append(',');
            numberField(out, "count", entry.getValue());
            out.append('}');
        }
        out.append("],\"history\":[");
        for (int i = 0; i < state.history.size(); i++) {
            if (i != 0) out.append(',');
            appendAction(out, state.history.get(i));
        }
        out.append("],\"searchRootFen\":");
        if (state.searchRootFen == null) out.append("null");
        else quote(out, state.searchRootFen);
        out.append(",\"searchMoves\":[");
        for (int i = 0; i < state.searchMoves.size(); i++) {
            if (i != 0) out.append(',');
            quote(out, state.searchMoves.get(i));
        }
        out.append("]}");
    }

    private static GameState decodeState(Map<String, Object> value) {
        List<Object> cells = array(required(value, "board"), "board");
        if (cells.size() != 64) throw new IllegalArgumentException("Board must contain 64 squares");
        Board board = new Board();
        for (int i = 0; i < cells.size(); i++) {
            String cell = asString(cells.get(i), "board square");
            if (".".equals(cell)) continue;
            if (cell.length() != 3 || (cell.charAt(0) != 'w' && cell.charAt(0) != 'b')
                    || (cell.charAt(2) != '0' && cell.charAt(2) != '1'))
                throw new IllegalArgumentException("Invalid board square: " + cell);
            Side side = cell.charAt(0) == 'w' ? Side.WHITE : Side.BLACK;
            PieceType type = pieceType(cell.charAt(1));
            board = board.with(i, new Piece(side, type, cell.charAt(2) == '1'));
        }

        Side toMove = enumValue(Side.class, string(value, "toMove"), "toMove");
        Object epRaw = required(value, "enPassant");
        int ep = epRaw == null ? -1 : parseSquare(asString(epRaw, "enPassant"));
        Side debt = nullableEnum(value, "debtTargetKings", Side.class);
        int halfMoves = checkedInt(integer(value, "halfMovesSinceCaptureOrPawn"), "halfMovesSinceCaptureOrPawn");
        if (halfMoves < 0) throw new IllegalArgumentException("Negative half-move counter");
        String castling = string(value, "castlingRights");
        if (castling.length() != 6 || !castling.matches("[01]{6}"))
            throw new IllegalArgumentException("castlingRights must be six 0/1 flags");
        GameResult result = enumValue(GameResult.class, string(value, "result"), "result");
        Side offer = nullableEnum(value, "drawOfferBy", Side.class);
        Object latchRaw = required(value, "drawOfferSentInCurrentNonWinningStretch");
        if (!(latchRaw instanceof Boolean))
            throw new IllegalArgumentException("drawOfferSentInCurrentNonWinningStretch must be boolean");
        boolean drawOfferLatch = (Boolean) latchRaw;

        Map<String, Integer> repetitions = new HashMap<>();
        for (Object entryRaw : array(required(value, "repetitions"), "repetitions")) {
            Map<String, Object> entry = object(entryRaw, "repetition entry");
            String key = string(entry, "key");
            int count = checkedInt(integer(entry, "count"), "repetition count");
            if (count < 1 || repetitions.put(key, count) != null)
                throw new IllegalArgumentException("Invalid or duplicate repetition entry");
        }
        List<GameAction> history = new ArrayList<>();
        for (Object action : array(required(value, "history"), "history"))
            history.add(decodeAction(object(action, "history action")));
        Object rawSearchRoot = required(value, "searchRootFen");
        String searchRootFen = rawSearchRoot == null ? null : asString(rawSearchRoot, "searchRootFen");
        List<String> searchMoves = new ArrayList<>();
        for (Object move : array(required(value, "searchMoves"), "searchMoves"))
            searchMoves.add(asString(move, "search move"));
        if (searchRootFen == null && !searchMoves.isEmpty())
            throw new IllegalArgumentException("Search moves require a replay root");

        return new GameState(board, toMove, ep, debt, halfMoves,
                castling.charAt(0) == '1', castling.charAt(1) == '1', castling.charAt(2) == '1',
                castling.charAt(3) == '1', castling.charAt(4) == '1', castling.charAt(5) == '1',
                result, offer, repetitions, history, drawOfferLatch, searchRootFen, searchMoves);
    }

    private static void appendAction(StringBuilder out, GameAction action) {
        if (action == null) throw new IllegalArgumentException("Action required");
        out.append('{');
        field(out, "type", action.type.name());
        if (action.type == GameAction.Type.MOVE) {
            out.append(','); field(out, "from", squareName(action.from)).append(',');
            field(out, "to", squareName(action.to)).append(',');
            field(out, "moveKind", action.moveKind.name()).append(',');
            nullableEnumField(out, "promotion", action.promotion);
        }
        out.append('}');
    }

    private static GameAction decodeAction(Map<String, Object> value) {
        GameAction.Type type = enumValue(GameAction.Type.class, string(value, "type"), "action type");
        if (type != GameAction.Type.MOVE) return GameAction.procedure(type);
        int from = parseSquare(string(value, "from"));
        int to = parseSquare(string(value, "to"));
        Move.Kind kind = enumValue(Move.Kind.class, string(value, "moveKind"), "moveKind");
        PieceType promotion = nullableEnum(value, "promotion", PieceType.class);
        return GameAction.move(new Move(from, to, kind), promotion);
    }

    private static String rights(GameState s) {
        return "" + bit(s.whiteKingSide) + bit(s.whiteQueenSide) + bit(s.whiteVertical)
                + bit(s.blackKingSide) + bit(s.blackQueenSide) + bit(s.blackVertical);
    }

    private static char bit(boolean value) { return value ? '1' : '0'; }

    private static char pieceCode(PieceType type) {
        switch (type) {
            case KING: return 'k';
            case QUEEN: return 'q';
            case ROOK: return 'r';
            case BISHOP: return 'b';
            case KNIGHT: return 'n';
            case PAWN: return 'p';
            default: throw new IllegalArgumentException("Unsupported piece type");
        }
    }

    private static PieceType pieceType(char code) {
        switch (code) {
            case 'k': return PieceType.KING;
            case 'q': return PieceType.QUEEN;
            case 'r': return PieceType.ROOK;
            case 'b': return PieceType.BISHOP;
            case 'n': return PieceType.KNIGHT;
            case 'p': return PieceType.PAWN;
            default: throw new IllegalArgumentException("Unknown piece code: " + code);
        }
    }

    private static String squareName(int square) {
        if (!Board.isValid(square)) throw new IllegalArgumentException("Invalid square index: " + square);
        return String.valueOf((char) ('a' + Board.col(square))) + (8 - Board.row(square));
    }

    private static int parseSquare(String name) {
        if (name == null || !name.matches("[a-h][1-8]"))
            throw new IllegalArgumentException("Invalid square: " + name);
        return Board.index(8 - (name.charAt(1) - '0'), name.charAt(0) - 'a');
    }

    private static StringBuilder field(StringBuilder out, String key, String value) {
        quote(out, key).append(':'); quote(out, value);
        return out;
    }

    private static StringBuilder numberField(StringBuilder out, String key, long value) {
        quote(out, key).append(':').append(value);
        return out;
    }

    private static StringBuilder nullableEnumField(StringBuilder out, String key, Enum<?> value) {
        quote(out, key).append(':');
        if (value == null) out.append("null"); else quote(out, value.name());
        return out;
    }

    private static StringBuilder quote(StringBuilder out, String value) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') out.append('\\').append(c);
            else if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
            else out.append(c);
        }
        return out.append('"');
    }

    private static Object required(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) throw new IllegalArgumentException("Missing field: " + key);
        return map.get(key);
    }

    private static String string(Map<String, Object> map, String key) {
        return asString(required(map, key), key);
    }

    private static String asString(Object value, String name) {
        if (!(value instanceof String)) throw new IllegalArgumentException(name + " must be a string");
        return (String) value;
    }

    private static long integer(Map<String, Object> map, String key) {
        Object value = required(map, key);
        if (!(value instanceof Long)) throw new IllegalArgumentException(key + " must be an integer");
        return (Long) value;
    }

    private static int checkedInt(long value, String name) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE)
            throw new IllegalArgumentException(name + " is out of range");
        return (int) value;
    }

    private static Map<String, Object> object(Object value, String name) {
        if (!(value instanceof Map)) throw new IllegalArgumentException(name + " must be an object");
        @SuppressWarnings("unchecked") Map<String, Object> result = (Map<String, Object>) value;
        return result;
    }

    private static List<Object> array(Object value, String name) {
        if (!(value instanceof List)) throw new IllegalArgumentException(name + " must be an array");
        @SuppressWarnings("unchecked") List<Object> result = (List<Object>) value;
        return result;
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, String name) {
        try { return Enum.valueOf(type, value); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Invalid " + name + ": " + value); }
    }

    private static <T extends Enum<T>> T nullableEnum(Map<String, Object> map, String key, Class<T> type) {
        Object value = required(map, key);
        return value == null ? null : enumValue(type, asString(value, key), key);
    }

    /** Small strict JSON parser: the wire format has no external runtime dependency. */
    private static final class Json {
        private final String text;
        private int at;

        private Json(String text) {
            if (text == null) throw new IllegalArgumentException("JSON is null");
            this.text = text;
        }

        static Object parse(String text) {
            Json parser = new Json(text);
            Object result = parser.value();
            parser.space();
            if (parser.at != text.length()) throw parser.error("Trailing data");
            return result;
        }

        private Object value() {
            space();
            if (at >= text.length()) throw error("Unexpected end");
            char c = text.charAt(at);
            if (c == '{') return objectValue();
            if (c == '[') return arrayValue();
            if (c == '"') return stringValue();
            if (c == 't') { literal("true"); return Boolean.TRUE; }
            if (c == 'f') { literal("false"); return Boolean.FALSE; }
            if (c == 'n') { literal("null"); return null; }
            if (c == '-' || Character.isDigit(c)) return numberValue();
            throw error("Unexpected token");
        }

        private Map<String, Object> objectValue() {
            at++;
            Map<String, Object> result = new LinkedHashMap<>();
            space();
            if (take('}')) return result;
            do {
                space();
                if (at >= text.length() || text.charAt(at) != '"') throw error("Object key required");
                String key = stringValue();
                space(); require(':');
                Object value = value();
                if (result.containsKey(key)) throw error("Duplicate object key: " + key);
                result.put(key, value);
                space();
                if (take('}')) return result;
                require(',');
            } while (true);
        }

        private List<Object> arrayValue() {
            at++;
            List<Object> result = new ArrayList<>();
            space();
            if (take(']')) return result;
            do {
                result.add(value());
                space();
                if (take(']')) return result;
                require(',');
            } while (true);
        }

        private String stringValue() {
            require('"');
            StringBuilder result = new StringBuilder();
            while (at < text.length()) {
                char c = text.charAt(at++);
                if (c == '"') return result.toString();
                if (c == '\\') {
                    if (at >= text.length()) throw error("Incomplete escape");
                    char escaped = text.charAt(at++);
                    switch (escaped) {
                        case '"': case '\\': case '/': result.append(escaped); break;
                        case 'b': result.append('\b'); break;
                        case 'f': result.append('\f'); break;
                        case 'n': result.append('\n'); break;
                        case 'r': result.append('\r'); break;
                        case 't': result.append('\t'); break;
                        case 'u':
                            if (at + 4 > text.length()) throw error("Incomplete unicode escape");
                            try { result.append((char) Integer.parseInt(text.substring(at, at + 4), 16)); }
                            catch (NumberFormatException e) { throw error("Invalid unicode escape"); }
                            at += 4;
                            break;
                        default: throw error("Invalid escape");
                    }
                } else {
                    if (c < 0x20) throw error("Control character in string");
                    result.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private Long numberValue() {
            int start = at;
            if (take('-') && at == text.length()) throw error("Incomplete number");
            if (take('0')) {
                if (at < text.length() && Character.isDigit(text.charAt(at))) throw error("Leading zero");
            } else {
                digits();
            }
            if (take('.') || take('e') || take('E')) throw error("Only integer numbers are supported");
            try { return Long.parseLong(text.substring(start, at)); }
            catch (NumberFormatException e) { throw error("Integer out of range"); }
        }

        private void digits() {
            int start = at;
            while (at < text.length() && Character.isDigit(text.charAt(at))) at++;
            if (start == at) throw error("Digit required");
        }

        private void literal(String value) {
            if (!text.startsWith(value, at)) throw error("Invalid literal");
            at += value.length();
        }

        private boolean take(char c) {
            if (at < text.length() && text.charAt(at) == c) { at++; return true; }
            return false;
        }

        private void require(char c) {
            if (!take(c)) throw error("Expected '" + c + "'");
        }

        private void space() {
            while (at < text.length() && (text.charAt(at) == ' ' || text.charAt(at) == '\n'
                    || text.charAt(at) == '\r' || text.charAt(at) == '\t')) at++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at JSON offset " + at);
        }
    }
}
