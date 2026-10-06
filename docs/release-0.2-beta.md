# Chess with Life — 0.2 beta

## Русский

«Шахматы с жизнями» — офлайн-вариант шахмат для двух игроков за одним
Android-устройством. Можно играть вдвоём или против встроенного адаптированного
движка Fairy-Stockfish. Движок работает локально; приложению не нужны сеть,
учётная запись или разрешения. Минимальная версия Android — 4.4 (API 19).

В версии 0.2 beta подключён нативный Fairy-Stockfish с поиском по правилам
варианта. Можно выбрать сторону игрока и один из бюджетов поиска: 1 000,
2 000, 5 000, 10 000, 25 000 или 50 000 узлов на решение. Эти числа не являются
рейтингом Elo. Оценку позиции за белых можно включить отдельно; по умолчанию
она выключена. Настройки сохраняются на устройстве. Интерфейс доступен на
русском и английском языках.

Также доступны редактор позиции, выбор превращения, обязательное взятие короля,
короткая, длинная и вертикальная рокировки, предложение договорной ничьей и
заявления ничьей по предусмотренным основаниям.

Экран «О программе» указывает, что встроенный движок основан на Fairy-Stockfish,
и содержит ссылки на оригинальный проект, адаптированный форк и лицензию движка
GNU GPL-3.0.

Исходная ревизия адаптированного движка для этого выпуска: `ae608671`.

### Полные правила

Цель игры — взять все короли соперника. Игра рассчитана на двух игроков за
одним устройством. Сеть и учётные записи не используются.

Ходят по очереди. Фигуры перемещаются по обычной геометрии шахмат; путь
скользящих фигур должен быть свободен. Пешка может сделать первый ход на две
клетки, брать по диагонали и превращаться на последней горизонтали. Взятие на
проходе доступно сразу после двойного хода пешки.

Бой короля не запрещает ход. Игрок сам отвечает за безопасность своих королей.
Король может взять фигуру своей стороны, в том числе другого короля; ходивший
король остаётся на поле назначения. Только короли могут брать собственные
фигуры. Шахи не объявляются, атакованные короли не подсвечиваются; шах, мат и
пат не являются ограничениями или автоматическими исходами.

Если после хода пешки хотя бы один ранее существовавший король этой стороны
находится под боем, при превращении дополнительно доступен король. Превращение
в короля создаёт долг: следующий игрок обязан взять любой король создавшей долг
стороны. Безопасность короля обязанного игрока не проверяется. Если долг
исполнен, игра не завершилась и взятие создало нового короля, возникает
встречный долг. Взятие последнего короля соперника немедленно завершает игру;
после этого долг не исполняется.

Разрешены короткая и длинная рокировки, а также вертикальная рокировка.
Король и ладья должны не ходить, а клетки между ними должны быть свободны.
Проверка атакованных полей не выполняется. Ладья после превращения считается
не ходившей, пока сама не сделает ход.

Игрок, начинающий свой ход без короля, проигрывает. Троекратное повторение и
50 ходов без взятия или хода пешкой дают право заявить ничью, но сами по себе
партию не останавливают. Договорная ничья завершается только после предложения
одной стороны и подтверждения другой.

Если на доске остались ровно два короля — по одному каждого цвета — и они не
стоят на соседних клетках, включая диагональ, игрок, которому принадлежит ход,
может заявить ничью. Это не автоматическое завершение партии. Любая третья
фигура исключает это основание.

Редактор позволяет создавать необычные позиции без автоматической расстановки
или исправления фигур. Чтобы начать игру из редактора, у стороны хода должен
быть король; если задан активный долг, должен существовать допустимый способ
исполнить обязательное взятие короля.

## English

**Chess with Life** is an offline chess variant for two players sharing one
Android device. Players can play each other or face the built-in adapted
Fairy-Stockfish engine. The engine runs locally; the app needs no network,
account, or permissions. Minimum Android version: 4.4 (API 19).

Version 0.2 beta integrates native Fairy-Stockfish search for this variant.
Choose a side and a search budget of 1,000, 2,000, 5,000, 10,000, 25,000, or
50,000 nodes per engine decision. These values are not Elo ratings. Optional
position evaluation is shown from White's perspective and is disabled by
default. Settings are stored on the device. The interface is available in
Russian and English.

The app also provides a position editor, promotion choices, mandatory king
capture, kingside, queenside, and vertical castling, draw offers, and draw claims
under the conditions described below.

The About screen credits Fairy-Stockfish as the basis of the built-in engine
and links to the original project, the adapted fork, and the engine GPL-3.0
license.

Engine source revision used for this release: `ae608671`.

### Complete rules

The goal is to capture every opponent king. The game is for two players sharing
one device; it uses no network or accounts.

Players take turns. Pieces move according to standard chess geometry, and
sliding pieces need a clear path. A pawn may advance two squares on its first
move, captures diagonally, and promotes on the last rank. En passant is
available immediately after a pawn's two-square move.

An attacked king does not restrict a move. Players are responsible for their
own kings. A king may capture a friendly piece, including another king; the
moving king remains on the destination square. Only kings may capture friendly
pieces. Check is not announced, attacked kings are not highlighted, and check,
checkmate, and stalemate do not restrict play or end the game automatically.

If, after a pawn move, at least one previously existing king of that side is
attacked, promotion to a king becomes available. Promoting to a king creates a
debt: the next player must capture any king of the side that created the debt.
The capturing player's own king safety is not checked. If the debt is paid, the
game has not ended, and the capture created a new king, a counter-debt is
created. Capturing the opponent's last king ends the game immediately; no debt
is then carried out.

Kingside, queenside, and vertical castling are allowed. The king and rook must
not have moved, and the squares between them must be empty. Attacked squares
are not checked. A promoted rook is considered unmoved until it moves.

A player who begins a turn with no king loses. Threefold repetition and 50
moves without a capture or pawn move allow a draw claim, but do not end the game
automatically. An agreed draw ends the game only after one player offers and the
opponent accepts.

If exactly two kings remain, one of each color, and they are not on adjacent
squares (including diagonally), the player to move may claim a draw. This is
not automatic. Any third piece removes this draw condition.

The editor allows unusual positions without automatically arranging or
correcting pieces. To start from an edited position, the side to move must have
a king; if an active debt is set, at least one legal way to capture the required
king must exist.
