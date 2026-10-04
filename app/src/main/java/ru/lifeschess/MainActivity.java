package ru.lifeschess;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import ru.lifeschess.engine.Board;
import ru.lifeschess.engine.DrawDetector;
import ru.lifeschess.engine.GameResult;
import ru.lifeschess.engine.GameState;
import ru.lifeschess.engine.Move;
import ru.lifeschess.engine.MoveGenerator;
import ru.lifeschess.engine.Piece;
import ru.lifeschess.engine.PieceType;
import ru.lifeschess.engine.RuleEngine;
import ru.lifeschess.engine.Side;

public final class MainActivity extends Activity {
    private static final String K_STATE = "game", K_EDITOR = "editor", K_SCREEN = "screen";
    private static final String K_HAS_GAME = "hasGame", K_HAS_EDITOR = "hasEditor";
    private static final String K_SELECTED = "selected", K_PENDING = "pending", K_EDITOR_SELECTED = "editorSelected";
    private static final int MENU = 0, GAME = 1, EDITOR = 2, RULES = 3;
    private int screen = MENU, selected = -1, editorSelected = -1;
    private boolean hasGame, hasEditor;
    private GameState state = GameState.initial();
    private BoardEditor editor = new BoardEditor();
    private Move pendingPromotion;
    private LinearLayout root;
    private ChessBoardView boardView;
    private TextView status;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Object saved = savedInstanceState.getSerializable(K_STATE);
            if (saved instanceof GameState) state = (GameState) saved;
            saved = savedInstanceState.getSerializable(K_EDITOR);
            if (saved instanceof BoardEditor) editor = (BoardEditor) saved;
            screen = savedInstanceState.getInt(K_SCREEN, MENU);
            hasGame = savedInstanceState.getBoolean(K_HAS_GAME, screen == GAME);
            hasEditor = savedInstanceState.getBoolean(K_HAS_EDITOR, screen == EDITOR);
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
        out.putInt(K_SELECTED, selected);
        out.putInt(K_EDITOR_SELECTED, editorSelected);
        if (pendingPromotion != null) out.putSerializable(K_PENDING, pendingPromotion);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (screen != MENU) {
            screen = MENU;
            selected = -1;
            showScreen();
            return;
        }
        super.onBackPressed();
    }

    private void showScreen() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(12), dp(12), dp(12));
        root.setBackgroundColor(0xfffaf8f2);
        setContentView(root);
        if (screen == MENU) showMenu();
        else if (screen == GAME) showGame();
        else if (screen == EDITOR) showEditor();
        else showRules();
    }

    private void showMenu() {
        MainMenu.render(this, root, hasGame,
                () -> { state = GameState.initial(); hasGame = true; selected = -1; pendingPromotion = null; screen = GAME; showScreen(); },
                () -> { screen = GAME; selected = -1; showScreen(); },
                () -> { if (!hasEditor) { editor = new BoardEditor(); hasEditor = true; } screen = EDITOR; showScreen(); },
                () -> { screen = RULES; showScreen(); });
    }

    private void showGame() {
        String turn = state.toMove == Side.WHITE ? "Белые" : "Чёрные";
        String debt = state.debtTargetKings == null ? "" : " · долг: взять короля " + sideName(state.debtTargetKings);
        GameScreen gameScreen = new GameScreen(this, state.board, selected,
                selected < 0 ? null : RuleEngine.legalMoves(state, selected), this::onGameSquare,
                outcomeText(state) == null ? "Ход: " + turn + debt : outcomeText(state));
        status = gameScreen.status; boardView = gameScreen.board;
        root.addView(gameScreen, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.VERTICAL);
        root.addView(buttons, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout row = horizontal(buttons);
        button(row, "Назад", v -> { screen = MENU; selected = -1; showScreen(); });
        if (state.drawOfferBy == null)
            button(row, "Предложить ничью", v -> { state = RuleEngine.offerDraw(state); showScreen(); });
        row = horizontal(buttons);
        button(row, "Сбросить на начальную позицию", v -> confirmResetGame());
        if (state.drawOfferBy != null && state.result == GameResult.NONE) {
            label(buttons, "Предложение от " + sideName(state.drawOfferBy) + ". Передайте устройство сопернику.");
            row = horizontal(buttons);
            button(row, "Принять", v -> { state = RuleEngine.respondDraw(state, true); showScreen(); });
            button(row, "Отклонить", v -> { state = RuleEngine.respondDraw(state, false); showScreen(); });
        }
        row = horizontal(buttons);
        Button repetition = button(row, "Заявить троекратное повторение", v -> {
            if (!DrawDetector.canClaimRepetition(state)) toast("Позиция ещё не повторилась трижды.");
            else { state = RuleEngine.claimDraw(state, true); showScreen(); }
        });
        repetition.setTextSize(12);
        Button fifty = button(row, "Заявить 50 ходов", v -> {
            if (!DrawDetector.canClaimFiftyMoves(state)) toast("Право заявления появится после 50 ходов без взятия и хода пешкой.");
            else { state = RuleEngine.claimDraw(state, false); showScreen(); }
        });
        fifty.setTextSize(12);
    }

    private void onGameSquare(int square) {
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
        new AlertDialog.Builder(this).setTitle("Выберите превращение")
                .setItems(labels, (dialog, which) -> completeMove(m, options.get(which)))
                .setOnCancelListener(dialog -> { pendingPromotion = null; showScreen(); }).show();
    }

    private void completeMove(Move m, PieceType promotion) {
        try { state = RuleEngine.startTurn(RuleEngine.play(state, m, promotion)); }
        catch (IllegalArgumentException ex) { toast(ex.getMessage()); }
        pendingPromotion = null; selected = -1; showScreen();
    }

    private void showEditor() {
        title(root, "Редактор позиции");
        label(root, "Выберите фигуру и нажмите клетку. Позиция не исправляется автоматически.");
        boardView = new ChessBoardView(this);
        boardView.setListener(square -> { editorSelected = square; editor.place(square); showScreen(); });
        root.addView(boardView, new LinearLayout.LayoutParams(-1, 0, 1));
        boardView.show(editor.board, editorSelected, null);
        ScrollView scroll = new ScrollView(this);
        LinearLayout controls = new LinearLayout(this); controls.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(controls); root.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout row = horizontal(controls);
        button(row, "Фигура: " + (editor.paletteErase ? "ластик" : sideName(editor.paletteSide) + " " + pieceName(editor.paletteType)), v -> choosePalette());
        button(row, "Стереть всё", v -> { editor.board = new Board(); editor.enPassant = -1; editor.debt = null; editorSelected = -1; showScreen(); });
        row = horizontal(controls);
        button(row, "Ходят: " + sideName(editor.turn), v -> { editor.turn = editor.turn.opposite(); showScreen(); });
        button(row, editor.moved ? "Новая фигура" : "Фигура не ходила", v -> {
            editor.moved = !editor.moved;
            if (editorSelected >= 0) {
                Piece p = editor.board.at(editorSelected);
                if (p != null) editor.board = editor.board.with(editorSelected, new Piece(p.side, p.type, editor.moved));
            }
            showScreen();
        });
        row = horizontal(controls);
        button(row, "Долг: " + (editor.debt == null ? "нет" : sideName(editor.debt)), v -> chooseDebt());
        button(row, "Рокировки и en passant", v -> chooseSpecialRights());
        row = horizontal(root);
        button(row, "Играть из текущей позиции", v -> startEditorGame());
        button(row, "Назад", v -> { screen = MENU; showScreen(); });
    }

    private void choosePalette() {
        String[] choices = new String[13]; int i = 0;
        choices[i++] = "Ластик";
        for (Side side : Side.values()) for (PieceType type : PieceType.values())
            choices[i++] = sideName(side) + " " + pieceName(type);
        new AlertDialog.Builder(this).setTitle("Фигура для размещения").setItems(choices, (d, which) -> {
            if (which == 0) editor.paletteErase = true;
            else { editor.paletteErase = false; int n = which - 1; editor.paletteSide = Side.values()[n / 6]; editor.paletteType = PieceType.values()[n % 6]; }
            showScreen();
        }).show();
    }

    private void chooseDebt() {
        String[] choices = {"Нет долга", "Взять короля белых", "Взять короля чёрных"};
        new AlertDialog.Builder(this).setTitle("Обязательное взятие").setItems(choices, (d, which) -> {
            editor.debt = which == 0 ? null : (which == 1 ? Side.WHITE : Side.BLACK); showScreen();
        }).show();
    }

    private void chooseSpecialRights() {
        String[] labels = {"Белая короткая рокировка", "Белая длинная рокировка", "Белая вертикальная рокировка",
                "Чёрная короткая рокировка", "Чёрная длинная рокировка", "Чёрная вертикальная рокировка"};
        boolean[] checked = {editor.wK, editor.wQ, editor.wV, editor.bK, editor.bQ, editor.bV};
        new AlertDialog.Builder(this).setTitle("Права рокировки").setMultiChoiceItems(labels, checked,
                (d, which, yes) -> checked[which] = yes).setPositiveButton("Готово", (d, w) -> {
            editor.wK=checked[0]; editor.wQ=checked[1]; editor.wV=checked[2];
            editor.bK=checked[3]; editor.bQ=checked[4]; editor.bV=checked[5]; askEnPassant();
        }).setNegativeButton("Отмена", null).show();
    }

    private void askEnPassant() {
        EditText input = new EditText(this); input.setSingleLine(); input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("например, e3; пусто — нет");
        new AlertDialog.Builder(this).setTitle("Поле en passant") .setView(input)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String text = input.getText().toString().trim().toLowerCase();
                    editor.enPassant = parseSquare(text); showScreen();
                }).setNegativeButton("Отмена", null).show();
    }

    private void startEditorGame() {
        GameState candidate = new GameState(editor.board, editor.turn, editor.enPassant, editor.debt, 0,
                editor.wK, editor.wQ, editor.wV, editor.bK, editor.bQ, editor.bV,
                GameResult.NONE, null, null);
        String error = RuleEngine.validateEditorStart(candidate);
        if (error != null) { toast(error); return; }
        state = DrawDetector.recordPosition(candidate); hasGame = true; screen = GAME; selected = -1; showScreen();
    }

    private void confirmResetGame() {
        new AlertDialog.Builder(this)
                .setTitle("Сбросить игру?")
                .setMessage("Текущая позиция будет заменена начальной расстановкой.")
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    state = GameState.initial(); hasGame = true; selected = -1; pendingPromotion = null;
                    screen = GAME; showScreen();
                })
                .show();
    }

    private void showRules() {
        RulesScreen.render(this, root, () -> { screen = MENU; showScreen(); });
    }

    private int parseSquare(String value) {
        if (value.length() != 2 || value.charAt(0) < 'a' || value.charAt(0) > 'h' || value.charAt(1) < '1' || value.charAt(1) > '8') return -1;
        return Board.index(8 - (value.charAt(1) - '0'), value.charAt(0) - 'a');
    }
    private String outcomeText(GameState s) {
        switch (s.result) {
            case WHITE_WIN: return "Победа белых — короли чёрных взяты.";
            case BLACK_WIN: return "Победа чёрных — короли белых взяты.";
            case DRAW_REPETITION: return "Ничья заявлена по троекратному повторению.";
            case DRAW_FIFTY_MOVES: return "Ничья заявлена по правилу 50 ходов.";
            case DRAW_AGREEMENT: return "Ничья по соглашению.";
            default: return null;
        }
    }
    private String sideName(Side side) { return side == Side.WHITE ? "белые" : "чёрные"; }
    private String pieceName(PieceType type) {
        switch (type) { case KING: return "король"; case QUEEN: return "ферзь"; case ROOK: return "ладья";
            case BISHOP: return "слон"; case KNIGHT: return "конь"; default: return "пешка"; }
    }
    private int dp(int n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private void title(LinearLayout layout, String text) { TextView t = label(layout, text); t.setTextSize(24); t.setGravity(Gravity.CENTER); t.setPadding(0, dp(4), 0, dp(10)); }
    private TextView label(android.view.ViewGroup parent, String text) {
        TextView t = new TextView(this); t.setText(text); t.setTextColor(0xff242424); t.setTextSize(16); t.setPadding(dp(4), dp(4), dp(4), dp(4));
        parent.addView(t, new android.view.ViewGroup.LayoutParams(-1, -2)); return t;
    }
    private LinearLayout horizontal(LinearLayout parent) { LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); parent.addView(row, new LinearLayout.LayoutParams(-1, -2)); return row; }
    private Button button(android.view.ViewGroup parent, String text, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(text); b.setTextSize(14); b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(0, -2, 1)); return b;
    }
    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_LONG).show(); }
}
