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
        TextView title = new TextView(context); title.setText("Полные правила"); title.setTextSize(24);
        title.setGravity(Gravity.CENTER); title.setPadding(0, 4, 0, 10);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        ScrollView scroll = new ScrollView(context);
        TextView text = new TextView(context); text.setTextSize(16); text.setTextColor(0xff242424);
        text.setText("Цель игры — взять все короли соперника. Игра рассчитана на двух игроков за одним устройством. Сеть и учётные записи не используются.\n\n"
                + "Ходят по очереди. Фигуры перемещаются по обычной геометрии шахмат; путь скользящих фигур должен быть свободен. Пешка может сделать первый ход на две клетки, брать по диагонали и превращаться на последней горизонтали. Взятие на проходе доступно сразу после двойного хода пешки.\n\n"
                + "Бой короля не запрещает ход. Следите за своими королями самостоятельно. Король может взять фигуру своей стороны, в том числе другого короля; ходивший король остаётся на поле. Шахи и атакованные короли не подсвечиваются.\n\n"
                + "Если после хода пешки хотя бы один король её стороны находится под боем, при превращении дополнительно доступен король. Это превращение создаёт долг: следующий игрок обязан взять любой король этой стороны. Проверка безопасности собственного короля не применяется. Если долг исполнен, но игра не закончилась, а взятие создало нового короля, возникает встречный долг. Взятие последнего короля завершает игру немедленно.\n\n"
                + "Разрешены короткая и длинная рокировки, а также вертикальная рокировка: король и ладья должны быть не ходившими и клетки между ними свободны. Проверка атакованных полей не выполняется. Превращённая ладья считается не ходившей, пока сама не сделает ход.\n\n"
                + "Если вы начали свой ход без короля — вы проиграли. Троекратное повторение и 50 ходов без взятия или хода пешкой дают право заявить ничью, но сами по себе партию не останавливают. Договорная ничья завершается только после предложения и подтверждения соперником.\n\n"
                + "Редактор позволяет создать необычную позицию без автоматической расстановки и исправления фигур. Для старта у ходящей стороны должен быть король; если задан долг, должен существовать хотя бы один разрешённый способ взять требуемого короля.");
        text.setPadding(4, 4, 4, 8); scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        Button button = new Button(context); button.setText("Назад"); button.setOnClickListener(v -> back.run());
        root.addView(button, new LinearLayout.LayoutParams(-1, -2));
    }
}
