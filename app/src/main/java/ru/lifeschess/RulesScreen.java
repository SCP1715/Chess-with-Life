package ru.lifeschess;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class RulesScreen {
    private RulesScreen() { }
    static void render(Context context, LinearLayout root, Runnable back) {
        TextView title = new TextView(context); title.setText(R.string.rules_title); title.setTextSize(24);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 4, 0, 10);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(context);
        TextView text = new TextView(context); text.setTextSize(16); text.setTextColor(0xff242424);
        text.setText(R.string.rules_body);
        text.setPadding(4, 4, 4, 8); scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Button button = new Button(context); button.setText(R.string.action_back); button.setOnClickListener(v -> back.run());
        root.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }
}
