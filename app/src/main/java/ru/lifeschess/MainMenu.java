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
                       Runnable computerGame, Runnable continueGame, Runnable editor, Runnable rules,
                       Runnable about, Runnable settings) {
        TextView title = new TextView(context); title.setText(R.string.app_name); title.setTextSize(24);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 12, 0, 20);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        add(context, root, R.string.menu_new_game, v -> newGame.run());
        add(context, root, R.string.menu_computer_game, v -> computerGame.run());
        if (hasGame) add(context, root, R.string.menu_continue_game, v -> continueGame.run());
        add(context, root, R.string.menu_editor, v -> editor.run());
        add(context, root, R.string.menu_rules, v -> rules.run());
        add(context, root, R.string.menu_about, v -> about.run());
        add(context, root, R.string.menu_computer_settings, v -> settings.run());
    }
    private static void add(Context c, LinearLayout parent, int text, View.OnClickListener click) {
        Button button = new Button(c); button.setText(text); button.setOnClickListener(click);
        parent.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }
}
