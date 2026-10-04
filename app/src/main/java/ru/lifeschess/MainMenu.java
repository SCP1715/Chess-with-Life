package ru.lifeschess;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class MainMenu {
    private MainMenu() { }
    static void render(Context context, LinearLayout root, boolean hasGame, Runnable newGame,
                       Runnable continueGame, Runnable editor, Runnable rules) {
        TextView title = new TextView(context); title.setText("Шахматы с жизнями"); title.setTextSize(24);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 12, 0, 20);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        add(context, root, "Новая игра", v -> newGame.run());
        if (hasGame) add(context, root, "Продолжить игру", v -> continueGame.run());
        add(context, root, "Редактор доски", v -> editor.run());
        add(context, root, "Правила", v -> rules.run());
    }
    private static void add(Context c, LinearLayout parent, String text, View.OnClickListener click) {
        Button button = new Button(c); button.setText(text); button.setOnClickListener(click);
        parent.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }
}
