package ru.lifeschess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;
import java.util.Locale;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.DrawDetector;
import ru.lifeschess.engine.GameResult;
import ru.lifeschess.engine.GameState;
import ru.lifeschess.engine.GameAction;
import ru.lifeschess.engine.NativeStockfish;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.MoveGenerator;
import ru.lifeschess.engine.Piece;
import ru.lifeschess.engine.PieceType;
import ru.lifeschess.engine.RuleEngine;
import ru.lifeschess.engine.Side;

public final class MainActivity extends Activity {
    private static final String K_STATE = "game", K_EDITOR = "editor", K_SCREEN = "screen";
    private static final String K_HAS_GAME = "hasGame", K_HAS_EDITOR = "hasEditor";
    private static final String K_BOT_GAME = "botGame";
    private static final String K_SELECTED = "selected", K_PENDING = "pending", K_EDITOR_SELECTED = "editorSelected";
    private static final int MENU = 0, GAME = 1, EDITOR = 2, RULES = 3, ABOUT = 4;
    private int screen = MENU, selected = -1, editorSelected = -1;
    private boolean hasGame, hasEditor;
    private boolean botGame, botThinking;
    private Side botSide = Side.BLACK;
    private int botNodeBudget = 10_000;
    private Side activeBotSide = Side.BLACK;
    private int activeBotNodeBudget = 10_000;
    private boolean showEvaluation, activeShowEvaluation, analysisThinking;
    private TextView evaluationView;
    private GameState analysisState;
    private int botRequestToken;
    private long activeBotNativeRequestId;
    private final ExecutorService botExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private GameState state = GameState.initial();
    private String gameId = UUID.randomUUID().toString();
    private long stateRevision;
    private BoardEditor editor = new BoardEditor();
    private Move pendingPromotion;
    private LinearLayout root;
    private ChessBoardView boardView;
    private TextView status;

    @Override protected void attachBaseContext(Context base) {
        String language = base.getSharedPreferences("game-settings", MODE_PRIVATE)
                .getString("appLanguage", "ru");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.locale = locale;
        super.attachBaseContext(base.createConfigurationContext(configuration));
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("game-settings", MODE_PRIVATE);
        botSide = "WHITE".equals(preferences.getString("humanSide", "WHITE"))
                ? Side.BLACK : Side.WHITE;
        botNodeBudget = preferences.getInt("searchNodes", 10_000);
        showEvaluation = preferences.getBoolean("showEvaluation", false);
        if (savedInstanceState != null) {
            Object saved = savedInstanceState.getSerializable(K_STATE);
            if (saved instanceof GameState) state = (GameState) saved;
            saved = savedInstanceState.getSerializable(K_EDITOR);
            if (saved instanceof BoardEditor) editor = (BoardEditor) saved;
            screen = savedInstanceState.getInt(K_SCREEN, MENU);
            hasGame = savedInstanceState.getBoolean(K_HAS_GAME, screen == GAME);
            hasEditor = savedInstanceState.getBoolean(K_HAS_EDITOR, screen == EDITOR);
            botGame = savedInstanceState.getBoolean(K_BOT_GAME, false);
            gameId = savedInstanceState.getString("gameId", gameId);
            stateRevision = savedInstanceState.getLong("stateRevision", 0);
            activeBotSide = Side.values()[savedInstanceState.getInt("activeBotSide", Side.BLACK.ordinal())];
            activeBotNodeBudget = savedInstanceState.getInt("activeBotNodeBudget", botNodeBudget);
            activeShowEvaluation = savedInstanceState.getBoolean("activeShowEvaluation", showEvaluation);
            selected = savedInstanceState.getInt(K_SELECTED, -1);
            editorSelected = savedInstanceState.getInt(K_EDITOR_SELECTED, -1);
            saved = savedInstanceState.getSerializable(K_PENDING);
            if (saved instanceof Move) pendingPromotion = (Move) saved;
        }
        showScreen();
        if (pendingPromotion != null) {
            final Move restoreMove = pendingPromotion;
            root.post(() -> { if (pendingPromotion == restoreMove) showPromotionDialog(restoreMove); });
        }
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putSerializable(K_STATE, state);
        out.putSerializable(K_EDITOR, editor);
        out.putInt(K_SCREEN, screen);
        out.putBoolean(K_HAS_GAME, hasGame);
        out.putBoolean(K_HAS_EDITOR, hasEditor);
        out.putBoolean(K_BOT_GAME, botGame);
        out.putString("gameId", gameId);
        out.putLong("stateRevision", stateRevision);
        out.putInt("activeBotSide", activeBotSide.ordinal());
        out.putInt("activeBotNodeBudget", activeBotNodeBudget);
        out.putBoolean("activeShowEvaluation", activeShowEvaluation);
        out.putInt(K_SELECTED, selected);
        out.putInt(K_EDITOR_SELECTED, editorSelected);
        if (pendingPromotion != null) out.putSerializable(K_PENDING, pendingPromotion);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (screen != MENU) {
            cancelBotSearch();
            screen = MENU;
            selected = -1;
            showScreen();
            return;
        }
        super.onBackPressed();
    }

    @Override protected void onDestroy() {
        cancelBotSearch();
        botExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onPause() {
        cancelBotSearch();
        super.onPause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (screen == GAME) root.post(() -> { scheduleBotMove(); scheduleEvaluation(); });
    }

    private void showScreen() {
        stateRevision++;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.setBackgroundColor(0xfffaf8f2);
        setContentView(root);
        if (screen == MENU) showMenu();
        else if (screen == GAME) showGame();
        else if (screen == EDITOR) showEditor();
        else if (screen == RULES) showRules();
        else if (screen == ABOUT) showAbout();
        else showMenu();
        if (screen == MENU) addLanguageButton();
    }

    private void showMenu() {
        MainMenu.render(this, root, hasGame,
                () -> { cancelBotSearch(); botGame = false; gameId = UUID.randomUUID().toString(); state = GameState.initial(); hasGame = true; selected = -1; pendingPromotion = null; screen = GAME; showScreen(); },
                () -> startComputerGame(),
                () -> { screen = GAME; selected = -1; showScreen(); },
                () -> { cancelBotSearch(); botGame = false; if (!hasEditor) { editor = new BoardEditor(); hasEditor = true; } screen = EDITOR; showScreen(); },
                () -> { screen = RULES; showScreen(); },
                () -> { screen = ABOUT; showScreen(); }, this::showComputerSettings);
    }

    private void startComputerGame() {
        cancelBotSearch();
        botGame = true;
        gameId = UUID.randomUUID().toString();
        activeBotSide = botSide;
        activeBotNodeBudget = botNodeBudget;
        activeShowEvaluation = showEvaluation;
        state = GameState.initial();
        hasGame = true; selected = -1; pendingPromotion = null; screen = GAME; showScreen();
    }

    private void showComputerSettings() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(4), dp(20), 0);
        label(form, getString(R.string.settings_human_side));
        Spinner sideSpinner = new Spinner(this);
        sideSpinner.setAdapter(spinnerAdapter(new String[]{getString(R.string.side_white),
                getString(R.string.side_black)}));
        sideSpinner.setSelection(botSide == Side.BLACK ? 0 : 1);
        form.addView(sideSpinner, new LinearLayout.LayoutParams(-1, -2));
        label(form, getString(R.string.settings_search_depth));
        int[] budgets = {1_000, 2_000, 5_000, 10_000, 25_000, 50_000};
        int[] depthLabels = {R.string.depth_1000, R.string.depth_2000, R.string.depth_5000,
                R.string.depth_10000, R.string.depth_25000, R.string.depth_50000};
        String[] depths = new String[depthLabels.length];
        int selectedDepth = 3;
        for (int i = 0; i < depthLabels.length; i++) {
            depths[i] = getString(depthLabels[i]);
            if (budgets[i] == botNodeBudget) selectedDepth = i;
        }
        Spinner depthSpinner = new Spinner(this);
        depthSpinner.setAdapter(spinnerAdapter(depths));
        depthSpinner.setSelection(selectedDepth);
        form.addView(depthSpinner, new LinearLayout.LayoutParams(-1, -2));
        CheckBox evaluationCheck = new CheckBox(this);
        evaluationCheck.setText(R.string.settings_show_evaluation);
        evaluationCheck.setChecked(showEvaluation);
        form.addView(evaluationCheck, new LinearLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(this).setTitle(R.string.settings_title).setView(form)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    botSide = sideSpinner.getSelectedItemPosition() == 0 ? Side.BLACK : Side.WHITE;
                    botNodeBudget = budgets[depthSpinner.getSelectedItemPosition()];
                    showEvaluation = evaluationCheck.isChecked();
                    getSharedPreferences("game-settings", MODE_PRIVATE).edit()
                            .putString("humanSide", botSide == Side.BLACK ? "WHITE" : "BLACK")
                            .putInt("searchNodes", botNodeBudget)
                            .putBoolean("showEvaluation", showEvaluation).apply();
                    toast(getString(R.string.settings_saved));
                }).show();
    }

    private ArrayAdapter<String> spinnerAdapter(String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void addLanguageButton() {
        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        Button language = new Button(this);
        language.setText(R.string.language_button);
        language.setContentDescription(getString(R.string.language_button_description));
        language.setOnClickListener(v -> chooseLanguage());
        footer.addView(language, new LinearLayout.LayoutParams(-2, -2));
        root.addView(footer, new LinearLayout.LayoutParams(-1, -2));
    }

    private void chooseLanguage() {
        String[] languages = {getString(R.string.language_russian),
                getString(R.string.language_english)};
        String current = getSharedPreferences("game-settings", MODE_PRIVATE)
                .getString("appLanguage", "ru");
        new AlertDialog.Builder(this).setTitle(R.string.language_dialog_title)
                .setSingleChoiceItems(languages, "en".equals(current) ? 1 : 0,
                        (dialog, which) -> {
                            String selectedLanguage = which == 1 ? "en" : "ru";
                            getSharedPreferences("game-settings", MODE_PRIVATE).edit()
                                    .putString("appLanguage", selectedLanguage).apply();
                            dialog.dismiss();
                            recreate();
                        }).setNegativeButton(R.string.action_cancel, null).show();
    }

    private void showGame() {
        String turn = getString(state.toMove == Side.WHITE ? R.string.side_white_title
                : R.string.side_black_title);
        String debt = state.debtTargetKings == null ? "" : getString(R.string.debt_suffix,
                sideName(state.debtTargetKings));
        GameScreen gameScreen = new GameScreen(this, state.board, selected,
                selected < 0 ? null : RuleEngine.legalMoves(state, selected), this::onGameSquare,
                outcomeText(state) == null ? getString(R.string.turn_label, turn, debt)
                        : outcomeText(state));
        status = gameScreen.status; boardView = gameScreen.board;
        evaluationView = new TextView(this);
        evaluationView.setTextSize(14);
        evaluationView.setGravity(Gravity.CENTER);
        evaluationView.setVisibility(botGame && activeShowEvaluation ? View.VISIBLE : View.GONE);
        if (botGame && activeShowEvaluation && state.result == GameResult.NONE)
            evaluationView.setText(R.string.evaluation_calculating);
        root.addView(evaluationView, new LinearLayout.LayoutParams(-1, -2));
        root.addView(gameScreen, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.VERTICAL);
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout row = horizontal(buttons);
        button(row, R.string.action_back, v -> { cancelBotSearch(); screen = MENU; selected = -1; showScreen(); });
        if (state.drawOfferBy == null && !(botGame && state.toMove == activeBotSide))
            button(row, R.string.button_offer_draw, v -> {
                state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.OFFER_DRAW));
                showScreen();
            });
        row = horizontal(buttons);
        button(row, R.string.button_reset_game, v -> confirmResetGame());
        if (state.drawOfferBy != null && state.result == GameResult.NONE) {
            boolean botMustRespond = botGame && state.drawOfferBy != activeBotSide;
            label(buttons, botMustRespond ? getString(R.string.draw_computer_thinking)
                    : getString(R.string.draw_offer_from, sideName(state.drawOfferBy)));
            if (!botMustRespond) {
                row = horizontal(buttons);
                button(row, R.string.button_accept, v -> {
                    state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.ACCEPT_DRAW));
                    showScreen();
                });
                button(row, R.string.button_decline, v -> {
                    state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.DECLINE_DRAW));
                    showScreen();
                });
            }
        }
        row = horizontal(buttons);
        Button repetition = button(row, R.string.button_claim_repetition, v -> {
            if (!DrawDetector.canClaimRepetition(state)) toast(getString(R.string.error_repetition_unavailable));
            else { state = RuleEngine.applyAction(state,
                    GameAction.procedure(GameAction.Type.CLAIM_REPETITION)); showScreen(); }
        });
        repetition.setTextSize(12);
        Button fifty = button(row, R.string.button_claim_fifty, v -> {
            if (!DrawDetector.canClaimFiftyMoves(state)) toast(getString(R.string.error_fifty_unavailable));
            else { state = RuleEngine.applyAction(state,
                    GameAction.procedure(GameAction.Type.CLAIM_FIFTY_MOVES)); showScreen(); }
        });
        fifty.setTextSize(12);
        row = horizontal(buttons);
        Button bareKings = button(row, R.string.button_claim_bare_kings, v -> {
            if (!DrawDetector.canClaimBareKings(state)) toast(getString(R.string.error_draw_unavailable));
            else {
                GameAction claim = GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS);
                state = RuleEngine.applyAction(state, claim);
                showScreen();
            }
        });
        bareKings.setTextSize(12);
        scheduleBotMove();
        scheduleEvaluation();
    }

    private void onGameSquare(int square) {
        if (botGame && (state.toMove == activeBotSide
                || (state.drawOfferBy != null && state.drawOfferBy != activeBotSide))) return;
        if (state.result != GameResult.NONE || pendingPromotion != null) return;
        if (selected >= 0) {
            for (Move m : RuleEngine.legalMoves(state, selected)) if (m.to == square) {
                Piece moving = state.board.at(m.from);
                if (moving.type == PieceType.PAWN && Board.row(m.to) == moving.side.promotionRow()) {
                    pendingPromotion = m;
                    showPromotionDialog(m);
                } else completeMove(m, null);
                return;
            }
        }
        Piece p = state.board.at(square);
        selected = p != null && p.side == state.toMove ? square : -1;
        showScreen();
    }

    private void showPromotionDialog(Move m) {
        Piece pawn = state.board.at(m.from);
        List<PieceType> options = ru.lifeschess.engine.PromotionLogic.choices(
                state.board.with(m.from, null).with(m.to, pawn.moved()), pawn.side);
        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) labels[i] = pieceName(options.get(i));
        new AlertDialog.Builder(this).setTitle(R.string.promotion_title)
                .setItems(labels, (dialog, which) -> completeMove(m, options.get(which)))
                .setOnCancelListener(dialog -> { pendingPromotion = null; showScreen(); }).show();
    }

    private void completeMove(Move m, PieceType promotion) {
        try { state = RuleEngine.startTurn(RuleEngine.play(state, m, promotion)); }
        catch (IllegalArgumentException ex) { toast(ex.getMessage()); }
        pendingPromotion = null; selected = -1; showScreen();
    }

    private void scheduleBotMove() {
        boolean botMustRespondToDraw = botGame && state.drawOfferBy != null
                && state.drawOfferBy != activeBotSide && state.toMove != activeBotSide;
        if (!botGame || botThinking || screen != GAME || state.result != GameResult.NONE
                || (state.toMove != activeBotSide && !botMustRespondToDraw) || pendingPromotion != null
                || state.drawOfferBy == activeBotSide) return;

        cancelAnalysisOnly();

        final GameState requestedState = state;
        final int requestToken = ++botRequestToken;
        final long requestedRevision = stateRevision;
        final String requestedGameId = gameId;
        final boolean opponentOffered = requestedState.drawOfferBy != null
                && requestedState.drawOfferBy != activeBotSide;
        final boolean offerAlreadySent = requestedState.drawOfferSentInCurrentNonWinningStretch;
        botThinking = true;
        activeBotNativeRequestId = requestToken;
        if (status != null) status.setText(botMustRespondToDraw
                ? R.string.draw_computer_thinking : R.string.computer_thinking);
        botExecutor.execute(() -> {
            final String result;
            try {
                result = botMustRespondToDraw
                        ? NativeStockfish.searchDrawResponse(requestToken, requestedRevision,
                                requestedGameId, requestedState, activeBotSide, activeBotNodeBudget)
                        : NativeStockfish.search(requestToken, requestedRevision, requestedGameId,
                                requestedState, activeBotNodeBudget,
                                opponentOffered, offerAlreadySent, false);
            } catch (final RuntimeException | LinkageError error) {
                mainHandler.post(() -> finishBotSearch(requestToken, requestedRevision,
                        requestedGameId, requestedState,
                        null, getString(R.string.error_engine_start, error.getMessage())));
                return;
            }
            mainHandler.post(() -> finishBotSearch(requestToken, requestedRevision,
                    requestedGameId, requestedState, result, null));
        });
    }

    private void scheduleEvaluation() {
        if (!botGame || !activeShowEvaluation || state.result != GameResult.NONE
                || state.drawOfferBy != null || state.toMove == activeBotSide
                || analysisThinking || analysisState == state)
            return;
        final GameState requestedState = state;
        final long requestId = ++botRequestToken;
        final long requestedRevision = stateRevision;
        final String requestedGameId = gameId;
        analysisState = requestedState;
        analysisThinking = true;
        activeBotNativeRequestId = requestId;
        botExecutor.execute(() -> {
            final String result;
            try {
                result = NativeStockfish.search(requestId, requestedRevision, requestedGameId,
                        requestedState, activeBotNodeBudget,
                        false, false, true);
            } catch (final RuntimeException | LinkageError error) {
                mainHandler.post(() -> finishEvaluation(requestId, requestedRevision,
                        requestedGameId, requestedState, null));
                return;
            }
            mainHandler.post(() -> finishEvaluation(requestId, requestedRevision,
                    requestedGameId, requestedState, result));
        });
    }

    private void finishEvaluation(long requestId, long requestedRevision, String requestedGameId,
                                  GameState requestedState, String result) {
        if (requestId != botRequestToken) return;
        analysisThinking = false;
        activeBotNativeRequestId = 0;
        if (isFinishing() || screen != GAME || state != requestedState
                || stateRevision != requestedRevision || !gameId.equals(requestedGameId)
                || evaluationView == null || !activeShowEvaluation) {
            analysisState = null;
            if (!isFinishing() && screen == GAME && state == requestedState
                    && activeShowEvaluation) root.post(this::scheduleEvaluation);
            return;
        }
        if (result != null && !hasNativeRequestMetadata(result.split("\\|", -1),
                requestedGameId, requestId, requestedRevision)) {
            evaluationView.setText(R.string.evaluation_unavailable);
            return;
        }
        if (result == null || result.startsWith("ERROR|") || result.startsWith("CANCELLED|")) {
            evaluationView.setText(R.string.evaluation_unavailable);
            return;
        }
        String[] fields = result.split("\\|", -1);
        if (fields.length < 3) {
            evaluationView.setText(R.string.evaluation_unavailable);
            return;
        }
        try {
            int score = Integer.parseInt(fields[2]);
            if (requestedState.toMove == Side.BLACK) score = -score;
            String type = result.contains("scoreType=mate") ? getString(R.string.evaluation_mate)
                    : result.contains("scoreType=exact") ? getString(R.string.evaluation_exact_draw) : "cp";
            evaluationView.setText(getString(R.string.evaluation_white,
                    (score >= 0 ? "+" : "") + ("cp".equals(type)
                            ? String.format(Locale.ROOT, "%.2f", score / 100.0) : score), type));
        } catch (NumberFormatException ex) {
            evaluationView.setText(R.string.evaluation_unavailable);
        }
    }

    private void finishBotSearch(int token, long requestedRevision, String requestedGameId,
                                 GameState requestedState, String result, String error) {
        if (token != botRequestToken) return;
        botThinking = false;
        activeBotNativeRequestId = 0;
        if (isFinishing() || screen != GAME || state != requestedState
                || stateRevision != requestedRevision || !gameId.equals(requestedGameId)) {
            if (!isFinishing() && screen == GAME && state == requestedState
                    && gameId.equals(requestedGameId)) root.post(this::scheduleBotMove);
            return;
        }
        if (error != null) {
            toast(error);
            if (status != null) status.setText(R.string.error_search);
            return;
        }
        if (result == null || result.startsWith("ERROR|") || result.startsWith("CANCELLED|")) {
            toast(result == null ? getString(R.string.error_engine_empty) : result);
            if (status != null) status.setText(R.string.error_search);
            return;
        }

        String[] fields = result.split("\\|", -1);
        if (fields.length < 3) { toast(getString(R.string.error_engine_malformed, result)); showScreen(); return; }
        if (!hasNativeRequestMetadata(fields, requestedGameId, token, requestedRevision)) {
            toast(getString(R.string.error_engine_stale)); showScreen(); return;
        }
        String decision = fields[0];
        if ("claim_bare_kings".equals(decision)) {
            GameAction claim = GameAction.procedure(GameAction.Type.CLAIM_BARE_KINGS);
            if (!RuleEngine.legalActions(state).contains(claim)) {
                toast(getString(R.string.error_engine_bare_kings));
                showScreen();
                return;
            }
            state = RuleEngine.applyAction(state, claim);
            showScreen();
            return;
        }
        if ("claim_repetition".equals(decision) || "claim_fifty_moves".equals(decision)) {
            GameAction.Type selectedType = "claim_repetition".equals(decision)
                    ? GameAction.Type.CLAIM_REPETITION : GameAction.Type.CLAIM_FIFTY_MOVES;
            GameAction claim = GameAction.procedure(selectedType);
            if (!RuleEngine.legalActions(state).contains(claim)) claim = null;
            if (claim == null) { toast(getString(R.string.error_engine_draw_claim)); showScreen(); return; }
            state = RuleEngine.applyAction(state, claim);
            showScreen();
            return;
        }
        if ("accept".equals(decision)) {
            state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.ACCEPT_DRAW));
            showScreen();
            return;
        }
        if ("decline".equals(decision) && requestedState.toMove != activeBotSide) {
            state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.DECLINE_DRAW));
            showScreen();
            return;
        }
        if ("offer".equals(decision)) {
            state = RuleEngine.applyAction(state, GameAction.procedure(GameAction.Type.OFFER_DRAW))
                    .withDrawOfferLatch(true);
            showScreen();
            return;
        }
        if (!"continue".equals(decision) && !"decline".equals(decision)) {
            toast(getString(R.string.error_engine_action, decision));
            showScreen();
            return;
        }
        GameState candidateState = state;
        if ("decline".equals(decision) && state.drawOfferBy != null)
            candidateState = RuleEngine.applyAction(state,
                    GameAction.procedure(GameAction.Type.DECLINE_DRAW));

        GameAction action = actionFromUci(candidateState, fields[1]);
        if (action == null) {
            toast(getString(R.string.error_engine_move, fields[1]));
            showScreen();
            return;
        }
        try { Integer.parseInt(fields[2]); }
        catch (NumberFormatException ex) {
            toast(getString(R.string.error_engine_score));
            showScreen();
            return;
        }
        boolean resetOfferLatch = false;
        for (String field : fields) resetOfferLatch |= "resetOfferLatch=true".equals(field);
        if (resetOfferLatch) candidateState = candidateState.withDrawOfferLatch(false);
        state = RuleEngine.startTurn(RuleEngine.applyAction(candidateState, action));
        selected = -1;
        showScreen();
    }

    private GameAction actionFromUci(GameState current, String uci) {
        if (uci == null || uci.length() < 4) return null;
        int from = coordinate(uci.substring(0, 2));
        int to = coordinate(uci.substring(2, 4));
        if (from < 0 || to < 0) return null;
        PieceType promotion = null;
        if (uci.length() > 4) {
            switch (Character.toLowerCase(uci.charAt(4))) {
                case 'k': promotion = PieceType.KING; break;
                case 'q': promotion = PieceType.QUEEN; break;
                case 'r': promotion = PieceType.ROOK; break;
                case 'b': promotion = PieceType.BISHOP; break;
                case 'n': promotion = PieceType.KNIGHT; break;
                default: return null;
            }
        }
        for (GameAction action : RuleEngine.legalActions(current))
            if (action.type == GameAction.Type.MOVE && action.from == from && action.to == to
                    && action.promotion == promotion) return action;
        return null;
    }

    private int coordinate(String square) {
        if (square.length() != 2 || square.charAt(0) < 'a' || square.charAt(0) > 'h'
                || square.charAt(1) < '1' || square.charAt(1) > '8') return -1;
        return Board.index(8 - (square.charAt(1) - '0'), square.charAt(0) - 'a');
    }

    private void cancelBotSearch() {
        botRequestToken++;
        botThinking = false;
        analysisThinking = false;
        analysisState = null;
        long requestId = activeBotNativeRequestId;
        activeBotNativeRequestId = 0;
        try { if (requestId != 0) NativeStockfish.nativeCancel(requestId); }
        catch (RuntimeException | LinkageError ignored) { }
    }

    private void cancelAnalysisOnly() {
        analysisThinking = false;
        analysisState = null;
        long requestId = activeBotNativeRequestId;
        if (requestId != 0) {
            activeBotNativeRequestId = 0;
            try { NativeStockfish.nativeCancel(requestId); }
            catch (RuntimeException | LinkageError ignored) { }
        }
    }

    private boolean hasNativeRequestMetadata(String[] fields, String expectedGameId,
                                             long expectedRequestId, long expectedRevision) {
        String game = "gameId=" + expectedGameId;
        String request = "requestId=" + expectedRequestId;
        String revision = "stateRevision=" + expectedRevision;
        boolean hasGame = false, hasRequest = false, hasRevision = false;
        for (String field : fields) {
            hasGame |= game.equals(field);
            hasRequest |= request.equals(field);
            hasRevision |= revision.equals(field);
        }
        return hasGame && hasRequest && hasRevision;
    }

    private void showEditor() {
        title(root, R.string.editor_title);
        label(root, R.string.editor_instructions);
        boardView = new ChessBoardView(this);
        boardView.setListener(square -> { editorSelected = square; editor.place(square); showScreen(); });
        root.addView(boardView, new LinearLayout.LayoutParams(-1, 0, 1));
        boardView.show(editor.board, editorSelected, null);
        ScrollView scroll = new ScrollView(this);
        LinearLayout controls = new LinearLayout(this); controls.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(controls); root.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout row = horizontal(controls);
        button(row, getString(R.string.editor_piece, editor.paletteErase ? getString(R.string.editor_eraser)
                : sideName(editor.paletteSide) + " " + pieceName(editor.paletteType)), v -> choosePalette());
        button(row, R.string.editor_clear, v -> { editor.board = new Board(); editor.enPassant = -1; editor.debt = null; editorSelected = -1; showScreen(); });
        row = horizontal(controls);
        button(row, getString(R.string.editor_turn, sideName(editor.turn)), v -> { editor.turn = editor.turn.opposite(); showScreen(); });
        button(row, editor.moved ? R.string.editor_new_piece : R.string.editor_unmoved, v -> {
            editor.moved = !editor.moved;
            if (editorSelected >= 0) {
                Piece p = editor.board.at(editorSelected);
                if (p != null) editor.board = editor.board.with(editorSelected, new Piece(p.side, p.type, editor.moved));
            }
            showScreen();
        });
        row = horizontal(controls);
        button(row, getString(R.string.editor_debt, editor.debt == null
                ? getString(R.string.editor_no_debt) : sideName(editor.debt)), v -> chooseDebt());
        button(row, R.string.editor_special_rights, v -> chooseSpecialRights());
        row = horizontal(root);
        button(row, R.string.editor_play_position, v -> startEditorGame());
        button(row, R.string.action_back, v -> { screen = MENU; showScreen(); });
    }

    private void choosePalette() {
        String[] choices = new String[13]; int i = 0;
        choices[i++] = getString(R.string.editor_eraser_choice);
        for (Side side : Side.values()) for (PieceType type : PieceType.values())
            choices[i++] = sideName(side) + " " + pieceName(type);
        new AlertDialog.Builder(this).setTitle(R.string.editor_palette_title).setItems(choices, (d, which) -> {
            if (which == 0) editor.paletteErase = true;
            else { editor.paletteErase = false; int n = which - 1; editor.paletteSide = Side.values()[n / 6]; editor.paletteType = PieceType.values()[n % 6]; }
            showScreen();
        }).show();
    }

    private void chooseDebt() {
        String[] choices = {getString(R.string.editor_debt_none), getString(R.string.editor_debt_white),
                getString(R.string.editor_debt_black)};
        new AlertDialog.Builder(this).setTitle(R.string.editor_debt_title).setItems(choices, (d, which) -> {
            editor.debt = which == 0 ? null : (which == 1 ? Side.WHITE : Side.BLACK); showScreen();
        }).show();
    }

    private void chooseSpecialRights() {
        String[] labels = {getString(R.string.editor_castle_white_king),
                getString(R.string.editor_castle_white_queen), getString(R.string.editor_castle_white_vertical),
                getString(R.string.editor_castle_black_king), getString(R.string.editor_castle_black_queen),
                getString(R.string.editor_castle_black_vertical)};
        boolean[] checked = {editor.wK, editor.wQ, editor.wV, editor.bK, editor.bQ, editor.bV};
        new AlertDialog.Builder(this).setTitle(R.string.editor_castling_title).setMultiChoiceItems(labels, checked,
                (d, which, yes) -> checked[which] = yes).setPositiveButton(R.string.action_done, (d, w) -> {
            editor.wK=checked[0]; editor.wQ=checked[1]; editor.wV=checked[2];
            editor.bK=checked[3]; editor.bQ=checked[4]; editor.bV=checked[5]; askEnPassant();
        }).setNegativeButton(R.string.action_cancel, null).show();
    }

    private void askEnPassant() {
        EditText input = new EditText(this); input.setSingleLine(); input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.editor_en_passant_hint);
        new AlertDialog.Builder(this).setTitle(R.string.editor_en_passant_title).setView(input)
                .setPositiveButton(R.string.action_save, (d, w) -> {
                    String text = input.getText().toString().trim().toLowerCase();
                    editor.enPassant = parseSquare(text); showScreen();
                }).setNegativeButton(R.string.action_cancel, null).show();
    }

    private void startEditorGame() {
        GameState candidate = new GameState(editor.board, editor.turn, editor.enPassant, editor.debt, 0,
                editor.wK, editor.wQ, editor.wV, editor.bK, editor.bQ, editor.bV,
                GameResult.NONE, null, null);
        String error = RuleEngine.validateEditorStart(candidate);
        if (error != null) { toast(localizeEditorError(error)); return; }
        state = DrawDetector.recordPosition(candidate); gameId = UUID.randomUUID().toString();
        botGame = false; hasGame = true; screen = GAME; selected = -1; showScreen();
    }

    private void confirmResetGame() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.reset_confirm, (dialog, which) -> {
                    cancelBotSearch();
                    state = GameState.initial(); hasGame = true; selected = -1; pendingPromotion = null;
                    screen = GAME; showScreen();
                })
                .show();
    }

    private void showRules() {
        RulesScreen.render(this, root, () -> { screen = MENU; showScreen(); });
    }

    private void showAbout() {
        AboutScreen.render(this, root, () -> { screen = MENU; showScreen(); });
    }

    private int parseSquare(String value) {
        if (value.length() != 2 || value.charAt(0) < 'a' || value.charAt(0) > 'h' || value.charAt(1) < '1' || value.charAt(1) > '8') return -1;
        return Board.index(8 - (value.charAt(1) - '0'), value.charAt(0) - 'a');
    }
    private String outcomeText(GameState s) {
        switch (s.result) {
            case WHITE_WIN: return getString(R.string.result_white_win);
            case BLACK_WIN: return getString(R.string.result_black_win);
            case DRAW_REPETITION: return getString(R.string.result_draw_repetition);
            case DRAW_FIFTY_MOVES: return getString(R.string.result_draw_fifty);
            case DRAW_AGREEMENT: return getString(R.string.result_draw_agreement);
            case DRAW_BARE_KINGS: return getString(R.string.result_draw_bare_kings);
            default: return null;
        }
    }
    private String sideName(Side side) {
        return getString(side == Side.WHITE ? R.string.side_white_name : R.string.side_black_name);
    }
    private String pieceName(PieceType type) {
        switch (type) { case KING: return getString(R.string.piece_king); case QUEEN: return getString(R.string.piece_queen);
            case ROOK: return getString(R.string.piece_rook); case BISHOP: return getString(R.string.piece_bishop);
            case KNIGHT: return getString(R.string.piece_knight); default: return getString(R.string.piece_pawn); }
    }
    private String localizeEditorError(String error) {
        if (error.startsWith("У стороны, которой ходить"))
            return getString(R.string.editor_start_error_king);
        if (error.startsWith("Нельзя начать: активный долг"))
            return getString(R.string.editor_start_error_debt);
        return error;
    }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private void title(LinearLayout layout, String text) { TextView t = label(layout, text); t.setTextSize(24); t.setGravity(Gravity.CENTER); t.setPadding(0, dp(4), 0, dp(10)); }
    private void title(LinearLayout layout, int text) { title(layout, getString(text)); }
    private TextView label(android.view.ViewGroup parent, String text) {
        TextView t = new TextView(this); t.setText(text); t.setTextColor(0xff242424); t.setTextSize(16); t.setPadding(dp(4), dp(4), dp(4), dp(4));
        parent.addView(t, new android.view.ViewGroup.LayoutParams(-1, -2)); return t;
    }
    private TextView label(android.view.ViewGroup parent, int text) { return label(parent, getString(text)); }
    private LinearLayout horizontal(LinearLayout parent) { LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); parent.addView(row, new LinearLayout.LayoutParams(-1, -2)); return row; }
    private Button button(android.view.ViewGroup parent, String text, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(text); b.setTextSize(14); b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(0, -2, 1)); return b;
    }
    private Button button(android.view.ViewGroup parent, int text, View.OnClickListener listener) {
        return button(parent, getString(text), listener);
    }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
}
