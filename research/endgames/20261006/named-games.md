# Первичный аудит выбранных партий

Таблицы дают доказательство только для загруженных беспешечных классов; позиции с большим числом фигур отмечены как UNKNOWN/OUT_OF_SCOPE.

## A-976

Лог: `A/games/game-000976.jsonl.gz`; seed `20261982`; parent `None`; итог `WHITE_WIN` / `WHITE_WIN`, длина 245 игровых полуходов.

### ply 201: {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'KING'}

До (13 фигур): `[('a8', 'br', '1'), ('b7', 'wp', '1'), ('c7', 'wr', '1'), ('e7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('g4', 'br', '1'), ('c3', 'wp', '1'), ('g3', 'wk', '1'), ('a2', 'bp', '1')]`; ход `WHITE`, долг `None`, полуходы `0`, повторы `190`.

После (12 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('e7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('g4', 'br', '1'), ('c3', 'wp', '1'), ('g3', 'wk', '1'), ('a2', 'bp', '1')]`; ход `BLACK`, долг `WHITE`, короли под боем `[{'king': 'g3', 'kingId': 'wk1', 'attacker': 'g4', 'attackerId': 'br1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 754 nodes 68 nps 68000 tbhits 0 time 1 pv g3g4 a2a1q
info depth 2 seldepth 2 multipv 1 score cp 754 nodes 149 nps 149000 tbhits 0 time 1 pv g3g4 a2a1q
info depth 3 seldepth 3 multipv 1 score cp 754 nodes 204 nps 204000 tbhits 0 time 1 pv g3g4 a2a1q f4e5
info depth 4 seldepth 4 multipv 1 score cp 761 nodes 282 nps 282000 tbhits 0 time 1 pv g3g4 a8g8 g4f4
info depth 5 seldepth 5 multipv 1 score cp 846 nodes 367 nps 367000 tbhits 0 time 1 pv g3g4 a8g8 g4f4 a2a1b
info depth 6 seldepth 6 multipv 1 score cp 938 nodes 512 nps 512000 tbhits 0 time 1 pv g3g4 a8g8 g4f4 a2a1n
info depth 7 seldepth 7 multipv 1 score cp 1025 nodes 1205 nps 602500 tbhits 0 time 2 pv g3g4 a8g8 g4f4 a2a1r
info depth 8 seldepth 9 multipv 1 score cp 792 nodes 4792 nps 599000 tbhits 0 time 8 pv b7a8k g4g3 e7e1 g3f3 f4e5 f6f5 c7a7
info depth 9 seldepth 11 multipv 1 score cp 783 nodes 10000 nps 500000 tbhits 0 time 20 pv b7a8k g4g3
```

candidateMoves: `['b7a8k']`; events: `[{'type': 'PROMOTION', 'side': 'WHITE', 'ply': 201, 'from': 'b7', 'to': 'a8', 'piece': 'KING'}, {'type': 'KING_PROMOTION', 'side': 'WHITE', 'ply': 201, 'square': 'a8', 'debtCreated': True, 'debtTarget': 'WHITE', 'newKingId': 'WK2'}, {'type': 'KING_LEFT_UNDER_ATTACK', 'side': 'WHITE', 'ply': 201, 'kingSquaresBefore': ['g3'], 'kingSquaresAfter': ['g3'], 'createdDebt': True, 'endedByLastKingCapture': False}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'g3', 'to': 'f2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'g2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'h2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'f3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'h3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'f4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'g4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g3', 'to': 'h4', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 202: {'type': 'MOVE', 'from': 'g4', 'to': 'g3', 'moveKind': 'NORMAL', 'promotion': None}

До (12 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('e7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('g4', 'br', '1'), ('c3', 'wp', '1'), ('g3', 'wk', '1'), ('a2', 'bp', '1')]`; ход `BLACK`, долг `WHITE`, полуходы `0`, повторы `191`.

После (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('e7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a2', 'bp', '1')]`; ход `WHITE`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[{'type': 'MOVE', 'from': 'g4', 'to': 'g3', 'moveKind': 'NORMAL', 'promotion': None}]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -783 nodes 9 nps 9000 tbhits 0 time 1 pv g4g3 e7e1
info depth 2 seldepth 2 multipv 1 score cp -783 nodes 17 nps 17000 tbhits 0 time 1 pv g4g3 e7e1
info depth 3 seldepth 3 multipv 1 score cp -807 nodes 31 nps 31000 tbhits 0 time 1 pv g4g3 e7e1 g3g8 a8b7 f6f5
info depth 4 seldepth 4 multipv 1 score cp -807 nodes 46 nps 46000 tbhits 0 time 1 pv g4g3 e7e1 g3g8 a8b7
info depth 5 seldepth 5 multipv 1 score cp -802 nodes 91 nps 91000 tbhits 0 time 1 pv g4g3 e7e1 g3g2 f4e5 f6f5
info depth 6 seldepth 6 multipv 1 score cp -802 nodes 135 nps 135000 tbhits 0 time 1 pv g4g3 e7e1 g3g2 f4e5 f6f5 e1a1
info depth 7 seldepth 7 multipv 1 score cp -803 nodes 214 nps 107000 tbhits 0 time 2 pv g4g3 e7e1 g3g2 f4e5 f6f5 e1a1 g2g8 a8b7 g8g2 b7c6
info depth 8 seldepth 9 multipv 1 score cp -803 nodes 460 nps 230000 tbhits 0 time 2 pv g4g3 e7e1 g3g2 f4e5 f6f5 e1a1 g2g8 a8b7 g8g2
info depth 9 seldepth 13 multipv 1 score cp -1176 nodes 5235 nps 402692 tbhits 0 time 13 pv g4g3 e7f7 a2a1k f7f6 g3f3 f4e5 a1b2 f6h6 f3c3
info depth 10 seldepth 12 multipv 1 score cp -1339 nodes 9986 nps 453909 tbhits 0 time 22 pv g4g3 e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5 g2f2 f6f7
info depth 11 seldepth 12 multipv 1 score cp -1339 nodes 10004 nps 454727 tbhits 0 time 22 pv g4g3 e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5 g2f2 f6f7
```

candidateMoves: `['g4g3']`; events: `[{'type': 'DEBT_RESPONSE', 'side': 'BLACK', 'ply': 202, 'targetSide': 'WHITE', 'from': 'g4', 'to': 'g3', 'targetKingIds': ['WK1', 'WK2'], 'legalTargetKingCaptures': 1, 'capturedTargetKing': True, 'capturedKingSquare': 'g3', 'capturedKingId': 'WK1', 'capturingPiece': 'ROOK', 'capturerKingsAttackedBefore': False, 'capturerKingsAttackedAfter': False, 'chainContinues': False}]`.

Полный набор ходов королём стороны хода: `[]`.

### ply 203: {'type': 'MOVE', 'from': 'e7', 'to': 'f7', 'moveKind': 'NORMAL', 'promotion': None}

До (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('e7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a2', 'bp', '1')]`; ход `WHITE`, долг `None`, полуходы `0`, повторы `192`.

После (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('f7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a2', 'bp', '1')]`; ход `BLACK`, долг `None`, короли под боем `[{'king': 'f6', 'kingId': 'bk1', 'attacker': 'f7', 'attackerId': 'wr1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 1339 nodes 69 nps 34500 tbhits 0 time 2 pv e7f7
info depth 2 seldepth 2 multipv 1 score cp 1339 nodes 102 nps 51000 tbhits 0 time 2 pv e7f7 a2a1k f7f6
info depth 3 seldepth 3 multipv 1 score cp 1339 nodes 136 nps 68000 tbhits 0 time 2 pv e7f7 a2a1k f7f6
info depth 4 seldepth 4 multipv 1 score cp 1339 nodes 172 nps 86000 tbhits 0 time 2 pv e7f7 a2a1k f7f6 g3g8
info depth 5 seldepth 5 multipv 1 score cp 1339 nodes 218 nps 109000 tbhits 0 time 2 pv e7f7 a2a1k f7f6 g3g8 a8b7
info depth 6 seldepth 6 multipv 1 score cp 1339 nodes 272 nps 136000 tbhits 0 time 2 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g2
info depth 7 seldepth 7 multipv 1 score cp 1339 nodes 347 nps 173500 tbhits 0 time 2 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5
info depth 8 seldepth 11 multipv 1 score cp 1356 nodes 679 nps 226333 tbhits 0 time 3 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5
info depth 9 seldepth 11 multipv 1 score cp 1370 nodes 1122 nps 374000 tbhits 0 time 3 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5 g2f2
info depth 10 seldepth 12 multipv 1 score cp 1378 nodes 2704 nps 386285 tbhits 0 time 7 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g2 f4e5 g2f2 b7c6 f2f3 c6d5 a1b2
info depth 11 seldepth 16 multipv 1 score cp 1387 nodes 8940 nps 447000 tbhits 0 time 20 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g1 b7c6 a1b2 c6d5 g1f1 f4e5 b2c2 f6f7
info depth 12 seldepth 16 multipv 1 score cp 1387 nodes 10006 nps 454818 tbhits 0 time 22 pv e7f7 a2a1k f7f6 g3g8 a8b7 g8g1 b7c6 a1b2 c6d5 g1f1 f4e5 b2c2 f6f7
```

candidateMoves: `['e7f7']`; events: `[]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'a8', 'to': 'a7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'a8', 'to': 'b7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'a8', 'to': 'b8', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 204: {'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'KING'}

До (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('f7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a2', 'bp', '1')]`; ход `BLACK`, долг `None`, полуходы `1`, повторы `193`.

После (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('f7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a1', 'bk', '0')]`; ход `WHITE`, долг `BLACK`, короли под боем `[{'king': 'f6', 'kingId': 'bk1', 'attacker': 'f7', 'attackerId': 'wr1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -1387 nodes 27 nps 27000 tbhits 0 time 1 pv a2a1k f7f6
info depth 2 seldepth 2 multipv 1 score cp -1387 nodes 54 nps 54000 tbhits 0 time 1 pv a2a1k f7f6
info depth 3 seldepth 3 multipv 1 score cp -1387 nodes 82 nps 82000 tbhits 0 time 1 pv a2a1k f7f6 g3g8
info depth 4 seldepth 4 multipv 1 score cp -1387 nodes 113 nps 113000 tbhits 0 time 1 pv a2a1k f7f6 g3g8 a8b7
info depth 5 seldepth 5 multipv 1 score cp -1387 nodes 148 nps 148000 tbhits 0 time 1 pv a2a1k f7f6 g3g8 a8b7 g8g1
info depth 6 seldepth 6 multipv 1 score cp -1387 nodes 188 nps 188000 tbhits 0 time 1 pv a2a1k f7f6 g3g8 a8b7 g8g1 b7c6
info depth 7 seldepth 7 multipv 1 score cp -1400 nodes 294 nps 294000 tbhits 0 time 1 pv a2a1k f7f6 g3g8 a8b7 g8g1 b7c6 g1f1
info depth 8 seldepth 11 multipv 1 score cp -1402 nodes 512 nps 512000 tbhits 0 time 1 pv a2a1k f7f6 g3g8 a8b7 g8g1 b7c6 g1f1 f4e5 a1b2
info depth 9 seldepth 13 multipv 1 score cp -1433 nodes 1687 nps 421750 tbhits 0 time 4 pv a2a1k f7f6 g3g8 a8b7 g8g1 b7c6 g1f1 f4e5 a1b2 c6d5 b2c2
info depth 10 seldepth 15 multipv 1 score cp -1467 nodes 10009 nps 526789 tbhits 0 time 19 pv a2a1k f7f6
```

candidateMoves: `['a2a1k']`; events: `[{'type': 'PROMOTION', 'side': 'BLACK', 'ply': 204, 'from': 'a2', 'to': 'a1', 'piece': 'KING'}, {'type': 'KING_PROMOTION', 'side': 'BLACK', 'ply': 204, 'square': 'a1', 'debtCreated': True, 'debtTarget': 'BLACK', 'newKingId': 'BK2'}, {'type': 'KING_LEFT_UNDER_ATTACK', 'side': 'BLACK', 'ply': 204, 'kingSquaresBefore': ['f6'], 'kingSquaresAfter': ['f6'], 'createdDebt': True, 'endedByLastKingCapture': False}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'f6', 'to': 'e5', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'f5', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'g5', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'e6', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'g6', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'e7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'f7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'f6', 'to': 'g7', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 205: {'type': 'MOVE', 'from': 'f7', 'to': 'f6', 'moveKind': 'NORMAL', 'promotion': None}

До (11 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('f7', 'wr', '1'), ('f6', 'bk', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a1', 'bk', '0')]`; ход `WHITE`, долг `BLACK`, полуходы `0`, повторы `194`.

После (10 фигур): `[('a8', 'wk', '0'), ('c7', 'wr', '1'), ('f6', 'wr', '1'), ('d5', 'bp', '1'), ('f5', 'wp', '1'), ('d4', 'wp', '1'), ('f4', 'wb', '1'), ('c3', 'wp', '1'), ('g3', 'br', '1'), ('a1', 'bk', '0')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[{'type': 'MOVE', 'from': 'f7', 'to': 'f6', 'moveKind': 'NORMAL', 'promotion': None}]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 1467 nodes 2 nps 2000 tbhits 0 time 1 pv f7f6
info depth 2 seldepth 2 multipv 1 score cp 1468 nodes 5 nps 5000 tbhits 0 time 1 pv f7f6 g3g8
info depth 3 seldepth 3 multipv 1 score cp 1468 nodes 12 nps 12000 tbhits 0 time 1 pv f7f6 g3g8 a8b7
info depth 4 seldepth 4 multipv 1 score cp 1469 nodes 26 nps 26000 tbhits 0 time 1 pv f7f6 g3g8 a8b7 g8e8
info depth 5 seldepth 5 multipv 1 score cp 1476 nodes 40 nps 40000 tbhits 0 time 1 pv f7f6 g3g8 a8b7 g8e8 f6b6
info depth 6 seldepth 6 multipv 1 score cp 1467 nodes 93 nps 93000 tbhits 0 time 1 pv f7f6 g3g8 a8b7 g8e8 f6b6 e8h8
info depth 7 seldepth 7 multipv 1 score cp 1476 nodes 136 nps 136000 tbhits 0 time 1 pv f7f6 g3g8 a8b7 g8e8 b7c6 e8d8
info depth 8 seldepth 10 multipv 1 score cp 1484 nodes 358 nps 179000 tbhits 0 time 2 pv f7f6 g3g8 a8b7 g8g1 f6b6 g1f1 f4e5
info depth 9 seldepth 11 multipv 1 score cp 1462 nodes 1819 nps 454750 tbhits 0 time 4 pv f7f6 g3g8 a8b7 a1b2 f6g6 g8f8 f5f6 b2c2 f6f7
info depth 10 seldepth 12 multipv 1 score cp 1476 nodes 2838 nps 473000 tbhits 0 time 6 pv f7f6 g3g8 a8b7 a1b2 f6g6 g8f8 f5f6 b2c2 f6f7 c2b3
info depth 11 seldepth 14 multipv 1 score cp 1485 nodes 10016 nps 455272 tbhits 0 time 22 pv f7f6
```

candidateMoves: `['f7f6']`; events: `[{'type': 'DEBT_RESPONSE', 'side': 'WHITE', 'ply': 205, 'targetSide': 'BLACK', 'from': 'f7', 'to': 'f6', 'targetKingIds': ['BK1', 'BK2'], 'legalTargetKingCaptures': 1, 'capturedTargetKing': True, 'capturedKingSquare': 'f6', 'capturedKingId': 'BK1', 'capturingPiece': 'ROOK', 'capturerKingsAttackedBefore': False, 'capturerKingsAttackedAfter': False, 'chainContinues': False}]`.

Полный набор ходов королём стороны хода: `[]`.

### ply 233: {'type': 'MOVE', 'from': 'c7', 'to': 'c8', 'moveKind': 'NORMAL', 'promotion': 'QUEEN'}

До (9 фигур): `[('b7', 'wk', '1'), ('c7', 'wp', '1'), ('d7', 'wr', '1'), ('b6', 'wr', '1'), ('f6', 'wp', '1'), ('e5', 'wb', '1'), ('d4', 'wp', '1'), ('e4', 'bk', '1'), ('f2', 'br', '1')]`; ход `WHITE`, долг `None`, полуходы `1`, повторы `222`.

После (9 фигур): `[('c8', 'wq', '0'), ('b7', 'wk', '1'), ('d7', 'wr', '1'), ('b6', 'wr', '1'), ('f6', 'wp', '1'), ('e5', 'wb', '1'), ('d4', 'wp', '1'), ('e4', 'bk', '1'), ('f2', 'br', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 2452 nodes 41 nps 41000 tbhits 0 time 1 pv c7c8q
info depth 2 seldepth 2 multipv 1 score cp 2452 nodes 78 nps 78000 tbhits 0 time 1 pv c7c8q e4f3
info depth 3 seldepth 3 multipv 1 score cp 2452 nodes 117 nps 117000 tbhits 0 time 1 pv c7c8q e4f3 f6f7
info depth 4 seldepth 4 multipv 1 score cp 2613 nodes 161 nps 161000 tbhits 0 time 1 pv c7c8q e4f3 f6f7
info depth 5 seldepth 5 multipv 1 score cp 2452 nodes 281 nps 281000 tbhits 0 time 1 pv c7c8q e4f3 f6f7 f3g2 d4d5
info depth 6 seldepth 6 multipv 1 score cp 2541 nodes 363 nps 181500 tbhits 0 time 2 pv c7c8q e4f3 f6f7 f3g2
info depth 7 seldepth 7 multipv 1 score cp 2467 nodes 634 nps 317000 tbhits 0 time 2 pv c7c8q e4f3 f6f7 f3e3 b6f6 f2b2 b7c6
info depth 8 seldepth 10 multipv 1 score cp 2475 nodes 926 nps 308666 tbhits 0 time 3 pv c7c8q e4f3 f6f7 f3e4 b6b3 f2f1 c8c2
info depth 9 seldepth 10 multipv 1 score cp 2515 nodes 1215 nps 405000 tbhits 0 time 3 pv c7c8q e4f3 f6f7 f3e3 b6f6 f2b2 b7c6
info depth 10 seldepth 11 multipv 1 score cp 2546 nodes 2184 nps 436800 tbhits 0 time 5 pv c7c8q e4f3 f6f7 f3g4 b6f6 f2f6 e5f6
info depth 11 seldepth 12 multipv 1 score cp 2576 nodes 4345 nps 482777 tbhits 0 time 9 pv c7c8q e4f3 f6f7 f3g2 d7c7 f2f1 d4d5
info depth 12 seldepth 12 multipv 1 score cp 2626 nodes 9628 nps 534888 tbhits 0 time 18 pv c7c8q e4f3 f6f7 f3e3 b6b3 e3d2 b3b2
info depth 13 seldepth 12 multipv 1 score cp 2626 nodes 10000 nps 526315 tbhits 0 time 19 pv c7c8q e4f3 f6f7 f3e3 b6b3 e3d2 b3b2
```

candidateMoves: `['c7c8q']`; events: `[{'type': 'PROMOTION', 'side': 'WHITE', 'ply': 233, 'from': 'c7', 'to': 'c8', 'piece': 'QUEEN'}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'b7', 'to': 'c8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'a6', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'b6', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'c6', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'a7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'c7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'b7', 'to': 'b8', 'moveKind': 'NORMAL', 'promotion': None}]`.

Для превращения на ply 201 доступны варианты: `[{'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'KING'}, {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'QUEEN'}, {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'ROOK'}, {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'BISHOP'}, {'type': 'MOVE', 'from': 'b7', 'to': 'a8', 'moveKind': 'NORMAL', 'promotion': 'KNIGHT'}]`.

Для превращения на ply 204 доступны варианты: `[{'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'KING'}, {'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'QUEEN'}, {'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'ROOK'}, {'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'BISHOP'}, {'type': 'MOVE', 'from': 'a2', 'to': 'a1', 'moveKind': 'NORMAL', 'promotion': 'KNIGHT'}]`.

Финальная позиция: 6 фигур; короли `[('b7', 'wk1')]`; toMove `BLACK`, result `WHITE_WIN`.

King IDs/counters: `{'9': 'WK2'}` / `{'w': 2, 'b': 2}`.

## A-186

Лог: `A/games/game-000186.jsonl.gz`; seed `20261192`; parent `None`; итог `BLACK_WIN` / `BLACK_WIN`, длина 122 игровых полуходов.

### ply 113: {'type': 'MOVE', 'from': 'e4', 'to': 'd3', 'moveKind': 'NORMAL', 'promotion': None}

До (13 фигур): `[('g8', 'bk', '1'), ('e6', 'bp', '1'), ('f6', 'wp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('b4', 'br', '1'), ('c4', 'wn', '1'), ('d4', 'wp', '1'), ('e4', 'wk', '1'), ('f4', 'bq', '1'), ('d3', 'wq', '1')]`; ход `WHITE`, долг `None`, полуходы `11`, повторы `113`.

После (12 фигур): `[('g8', 'bk', '1'), ('e6', 'bp', '1'), ('f6', 'wp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('b4', 'br', '1'), ('c4', 'wn', '1'), ('d4', 'wp', '1'), ('f4', 'bq', '1'), ('d3', 'wk', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -1961 nodes 66 nps 66000 tbhits 0 time 1 pv e4d3 b4b3 d3c4 f5d4
info depth 2 seldepth 2 multipv 1 score cp -2048 nodes 106 nps 106000 tbhits 0 time 1 pv e4d3 f4g3
info depth 3 seldepth 3 multipv 1 score cp -1961 nodes 148 nps 74000 tbhits 0 time 2 pv e4d3 b4b3 d3c4
info depth 4 seldepth 4 multipv 1 score cp -2036 nodes 258 nps 129000 tbhits 0 time 2 pv e4d3 f4g3 d3c2 g3g2
info depth 5 seldepth 5 multipv 1 score cp -1961 nodes 421 nps 210500 tbhits 0 time 2 pv e4d3 b4b3 d3c4 f5d4
info depth 6 seldepth 6 multipv 1 score cp -1961 nodes 815 nps 271666 tbhits 0 time 3 pv e4d3 b4b3 d3c4 f5d4
info depth 7 seldepth 7 multipv 1 score cp -1957 nodes 1413 nps 353250 tbhits 0 time 4 pv e4d3 f4g3 d3c2
info depth 8 seldepth 9 multipv 1 score cp -2046 nodes 2616 nps 523200 tbhits 0 time 5 pv e4d3 b4b3 d3c4 b3a3 c4b5 f5d4 b5c5 f4e5
info depth 9 seldepth 11 multipv 1 score cp -2192 nodes 4626 nps 578250 tbhits 0 time 8 pv e4d3 b4b3 d3c4 b3a3 c4b5 f5d4 b5c4 f4e5
info depth 10 seldepth 13 multipv 1 score cp -2292 nodes 6177 nps 561545 tbhits 0 time 11 pv e4d3 b4b3 d3c4 b3a3 c4b5 f5d4 b5c4 f4e5 f6f7 g8f7 c4c5 h6h5 c5c4 h5h4
info depth 11 seldepth 17 multipv 1 score mate -5 nodes 8920 nps 594666 tbhits 0 time 15 pv e4d3 b4b3 d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 12 seldepth 11 multipv 1 score mate -5 nodes 9470 nps 591875 tbhits 0 time 16 pv e4d3 b4b3 d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 13 seldepth 11 multipv 1 score mate -5 nodes 10004 nps 588470 tbhits 0 time 17 pv e4d3 b4b3 d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
```

candidateMoves: `['e4d3']`; events: `[{'type': 'KING_SELF_CAPTURE', 'side': 'WHITE', 'ply': 113, 'from': 'e4', 'to': 'd3', 'kingId': 'WK1', 'capturedType': 'QUEEN', 'capturedPieceId': None, 'kingAttackedBefore': True, 'kingAttackedAfter': False, 'safeNonSelfCaptureDestinations': [], 'hasSafeNonSelfCapture': False}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'e4', 'to': 'd3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'e3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'f3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'd4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'f4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'd5', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'e5', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e4', 'to': 'f5', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 115: {'type': 'MOVE', 'from': 'd3', 'to': 'c4', 'moveKind': 'NORMAL', 'promotion': None}

До (12 фигур): `[('g8', 'bk', '1'), ('e6', 'bp', '1'), ('f6', 'wp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('c4', 'wn', '1'), ('d4', 'wp', '1'), ('f4', 'bq', '1'), ('b3', 'br', '1'), ('d3', 'wk', '1')]`; ход `WHITE`, долг `None`, полуходы `1`, повторы `115`.

После (11 фигур): `[('g8', 'bk', '1'), ('e6', 'bp', '1'), ('f6', 'wp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('c4', 'wk', '1'), ('d4', 'wp', '1'), ('f4', 'bq', '1'), ('b3', 'br', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score mate -4 nodes 19 nps 19000 tbhits 0 time 1 pv d3c4
info depth 2 seldepth 2 multipv 1 score mate -4 nodes 42 nps 42000 tbhits 0 time 1 pv d3c4 b3c3
info depth 3 seldepth 3 multipv 1 score mate -4 nodes 70 nps 70000 tbhits 0 time 1 pv d3c4 b3c3 c4b5
info depth 4 seldepth 4 multipv 1 score mate -4 nodes 100 nps 100000 tbhits 0 time 1 pv d3c4 b3c3 c4b5 f4f1
info depth 5 seldepth 5 multipv 1 score mate -4 nodes 142 nps 71000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4
info depth 6 seldepth 6 multipv 1 score mate -4 nodes 184 nps 92000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6
info depth 7 seldepth 7 multipv 1 score mate -4 nodes 234 nps 117000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5
info depth 8 seldepth 8 multipv 1 score mate -4 nodes 288 nps 144000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 9 seldepth 9 multipv 1 score mate -4 nodes 348 nps 174000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 10 seldepth 9 multipv 1 score mate -4 nodes 428 nps 214000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 11 seldepth 9 multipv 1 score mate -4 nodes 524 nps 262000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 12 seldepth 9 multipv 1 score mate -4 nodes 626 nps 313000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 13 seldepth 9 multipv 1 score mate -4 nodes 744 nps 372000 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 14 seldepth 9 multipv 1 score mate -4 nodes 887 nps 443500 tbhits 0 time 2 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 15 seldepth 9 multipv 1 score mate -4 nodes 1378 nps 459333 tbhits 0 time 3 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 16 seldepth 9 multipv 1 score mate -4 nodes 1760 nps 440000 tbhits 0 time 4 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 17 seldepth 9 multipv 1 score mate -4 nodes 2865 nps 573000 tbhits 0 time 5 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 18 seldepth 9 multipv 1 score mate -4 nodes 4568 nps 571000 tbhits 0 time 8 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 19 seldepth 9 multipv 1 score mate -4 nodes 6533 nps 593909 tbhits 0 time 11 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 20 seldepth 9 multipv 1 score mate -4 nodes 9216 nps 658285 tbhits 0 time 14 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
info depth 21 seldepth 9 multipv 1 score mate -4 nodes 10002 nps 666800 tbhits 0 time 15 pv d3c4 b3c3 c4b5 f4f1 b5a4 f1a6 a4a5 a6a5
```

candidateMoves: `['d3c4']`; events: `[{'type': 'KING_SELF_CAPTURE', 'side': 'WHITE', 'ply': 115, 'from': 'd3', 'to': 'c4', 'kingId': 'WK1', 'capturedType': 'KNIGHT', 'capturedPieceId': None, 'kingAttackedBefore': True, 'kingAttackedAfter': False, 'safeNonSelfCaptureDestinations': ['c2', 'e2'], 'hasSafeNonSelfCapture': True}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'd3', 'to': 'c2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'd2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'e2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'c3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'e3', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'c4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'd4', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'e4', 'moveKind': 'NORMAL', 'promotion': None}]`.

Проверка вариантов c2/e2 по legalActions: `[{'type': 'MOVE', 'from': 'd3', 'to': 'c2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'd3', 'to': 'e2', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 122: {'type': 'MOVE', 'from': 'a6', 'to': 'a4', 'moveKind': 'NORMAL', 'promotion': None}

До (11 фигур): `[('g8', 'bk', '1'), ('f7', 'wp', '1'), ('a6', 'bq', '1'), ('e6', 'bp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('a4', 'wk', '1'), ('d4', 'wp', '1'), ('c3', 'br', '1')]`; ход `BLACK`, долг `None`, полуходы `0`, повторы `122`.

После (10 фигур): `[('g8', 'bk', '1'), ('f7', 'wp', '1'), ('e6', 'bp', '1'), ('h6', 'bp', '1'), ('d5', 'bn', '1'), ('e5', 'wp', '1'), ('f5', 'bn', '1'), ('a4', 'bq', '1'), ('d4', 'wp', '1'), ('c3', 'br', '1')]`; ход `WHITE`, долг `None`, короли под боем `[{'king': 'g8', 'kingId': 'bk1', 'attacker': 'f7', 'attackerId': 'wp1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score mate 1 nodes 60 nps 60000 tbhits 0 time 1 pv a6a4
info depth 2 seldepth 2 multipv 1 score mate 1 nodes 108 nps 108000 tbhits 0 time 1 pv a6a4
info depth 3 seldepth 2 multipv 1 score mate 1 nodes 156 nps 156000 tbhits 0 time 1 pv a6a4
info depth 4 seldepth 2 multipv 1 score mate 1 nodes 204 nps 204000 tbhits 0 time 1 pv a6a4
info depth 5 seldepth 2 multipv 1 score mate 1 nodes 252 nps 252000 tbhits 0 time 1 pv a6a4
info depth 6 seldepth 2 multipv 1 score mate 1 nodes 300 nps 300000 tbhits 0 time 1 pv a6a4
info depth 7 seldepth 2 multipv 1 score mate 1 nodes 348 nps 174000 tbhits 0 time 2 pv a6a4
info depth 8 seldepth 2 multipv 1 score mate 1 nodes 396 nps 198000 tbhits 0 time 2 pv a6a4
info depth 9 seldepth 2 multipv 1 score mate 1 nodes 444 nps 222000 tbhits 0 time 2 pv a6a4
info depth 10 seldepth 2 multipv 1 score mate 1 nodes 492 nps 246000 tbhits 0 time 2 pv a6a4
info depth 11 seldepth 2 multipv 1 score mate 1 nodes 540 nps 270000 tbhits 0 time 2 pv a6a4
info depth 12 seldepth 2 multipv 1 score mate 1 nodes 588 nps 294000 tbhits 0 time 2 pv a6a4
info depth 13 seldepth 2 multipv 1 score mate 1 nodes 636 nps 318000 tbhits 0 time 2 pv a6a4
info depth 14 seldepth 2 multipv 1 score mate 1 nodes 684 nps 342000 tbhits 0 time 2 pv a6a4
info depth 15 seldepth 2 multipv 1 score mate 1 nodes 732 nps 366000 tbhits 0 time 2 pv a6a4
info depth 16 seldepth 2 multipv 1 score mate 1 nodes 780 nps 390000 tbhits 0 time 2 pv a6a4
info depth 17 seldepth 2 multipv 1 score mate 1 nodes 828 nps 414000 tbhits 0 time 2 pv a6a4
info depth 18 seldepth 2 multipv 1 score mate 1 nodes 876 nps 438000 tbhits 0 time 2 pv a6a4
info depth 19 seldepth 2 multipv 1 score mate 1 nodes 924 nps 462000 tbhits 0 time 2 pv a6a4
info depth 20 seldepth 2 multipv 1 score mate 1 nodes 972 nps 486000 tbhits 0 time 2 pv a6a4
info depth 21 seldepth 2 multipv 1 score mate 1 nodes 1020 nps 510000 tbhits 0 time 2 pv a6a4
info depth 22 seldepth 2 multipv 1 score mate 1 nodes 1068 nps 534000 tbhits 0 time 2 pv a6a4
info depth 23 seldepth 2 multipv 1 score mate 1 nodes 1116 nps 558000 tbhits 0 time 2 pv a6a4
info depth 24 seldepth 2 multipv 1 score mate 1 nodes 1164 nps 582000 tbhits 0 time 2 pv a6a4
info depth 25 seldepth 2 multipv 1 score mate 1 nodes 1212 nps 606000 tbhits 0 time 2 pv a6a4
info depth 26 seldepth 2 multipv 1 score mate 1 nodes 1260 nps 630000 tbhits 0 time 2 pv a6a4
info depth 27 seldepth 2 multipv 1 score mate 1 nodes 1308 nps 654000 tbhits 0 time 2 pv a6a4
info depth 28 seldepth 2 multipv 1 score mate 1 nodes 1356 nps 678000 tbhits 0 time 2 pv a6a4
info depth 29 seldepth 2 multipv 1 score mate 1 nodes 1404 nps 468000 tbhits 0 time 3 pv a6a4
info depth 30 seldepth 2 multipv 1 score mate 1 nodes 1452 nps 484000 tbhits 0 time 3 pv a6a4
info depth 31 seldepth 2 multipv 1 score mate 1 nodes 1500 nps 500000 tbhits 0 time 3 pv a6a4
info depth 32 seldepth 2 multipv 1 score mate 1 nodes 1548 nps 516000 tbhits 0 time 3 pv a6a4
info depth 33 seldepth 2 multipv 1 score mate 1 nodes 1596 nps 532000 tbhits 0 time 3 pv a6a4
info depth 34 seldepth 2 multipv 1 score mate 1 nodes 1644 nps 548000 tbhits 0 time 3 pv a6a4
info depth 35 seldepth 2 multipv 1 score mate 1 nodes 1692 nps 564000 tbhits 0 time 3 pv a6a4
info depth 36 seldepth 2 multipv 1 score mate 1 nodes 1740 nps 580000 tbhits 0 time 3 pv a6a4
info depth 37 seldepth 2 multipv 1 score mate 1 nodes 1788 nps 596000 tbhits 0 time 3 pv a6a4
info depth 38 seldepth 2 multipv 1 score mate 1 nodes 1836 nps 612000 tbhits 0 time 3 pv a6a4
info depth 39 seldepth 2 multipv 1 score mate 1 nodes 1884 nps 628000 tbhits 0 time 3 pv a6a4
info depth 40 seldepth 2 multipv 1 score mate 1 nodes 1932 nps 644000 tbhits 0 time 3 pv a6a4
info depth 41 seldepth 2 multipv 1 score mate 1 nodes 1980 nps 660000 tbhits 0 time 3 pv a6a4
info depth 42 seldepth 2 multipv 1 score mate 1 nodes 2028 nps 507000 tbhits 0 time 4 pv a6a4
info depth 43 seldepth 2 multipv 1 score mate 1 nodes 2076 nps 519000 tbhits 0 time 4 pv a6a4
info depth 44 seldepth 2 multipv 1 score mate 1 nodes 2124 nps 531000 tbhits 0 time 4 pv a6a4
info depth 45 seldepth 2 multipv 1 score mate 1 nodes 2172 nps 543000 tbhits 0 time 4 pv a6a4
info depth 46 seldepth 2 multipv 1 score mate 1 nodes 2220 nps 555000 tbhits 0 time 4 pv a6a4
info depth 47 seldepth 2 multipv 1 score mate 1 nodes 2268 nps 567000 tbhits 0 time 4 pv a6a4
info depth 48 seldepth 2 multipv 1 score mate 1 nodes 2316 nps 579000 tbhits 0 time 4 pv a6a4
info depth 49 seldepth 2 multipv 1 score mate 1 nodes 2364 nps 591000 tbhits 0 time 4 pv a6a4
info depth 50 seldepth 2 multipv 1 score mate 1 nodes 2412 nps 603000 tbhits 0 time 4 pv a6a4
info depth 51 seldepth 2 multipv 1 score mate 1 nodes 2460 nps 615000 tbhits 0 time 4 pv a6a4
info depth 52 seldepth 2 multipv 1 score mate 1 nodes 2508 nps 627000 tbhits 0 time 4 pv a6a4
info depth 53 seldepth 2 multipv 1 score mate 1 nodes 2556 nps 639000 tbhits 0 time 4 pv a6a4
info depth 54 seldepth 2 multipv 1 score mate 1 nodes 2604 nps 651000 tbhits 0 time 4 pv a6a4
info depth 55 seldepth 2 multipv 1 score mate 1 nodes 2652 nps 663000 tbhits 0 time 4 pv a6a4
info depth 56 seldepth 2 multipv 1 score mate 1 nodes 2700 nps 675000 tbhits 0 time 4 pv a6a4
info depth 57 seldepth 2 multipv 1 score mate 1 nodes 2748 nps 687000 tbhits 0 time 4 pv a6a4
info depth 58 seldepth 2 multipv 1 score mate 1 nodes 2796 nps 699000 tbhits 0 time 4 pv a6a4
info depth 59 seldepth 2 multipv 1 score mate 1 nodes 2844 nps 711000 tbhits 0 time 4 pv a6a4
info depth 60 seldepth 2 multipv 1 score mate 1 nodes 2892 nps 723000 tbhits 0 time 4 pv a6a4
info depth 61 seldepth 2 multipv 1 score mate 1 nodes 2940 nps 735000 tbhits 0 time 4 pv a6a4
info depth 62 seldepth 2 multipv 1 score mate 1 nodes 2988 nps 747000 tbhits 0 time 4 pv a6a4
info depth 63 seldepth 2 multipv 1 score mate 1 nodes 3036 nps 607200 tbhits 0 time 5 pv a6a4
info depth 64 seldepth 2 multipv 1 score mate 1 nodes 3084 nps 616800 tbhits 0 time 5 pv a6a4
info depth 65 seldepth 2 multipv 1 score mate 1 nodes 3132 nps 626400 tbhits 0 time 5 pv a6a4
info depth 66 seldepth 2 multipv 1 score mate 1 nodes 3180 nps 636000 tbhits 0 time 5 pv a6a4
info depth 67 seldepth 2 multipv 1 score mate 1 nodes 3228 nps 645600 tbhits 0 time 5 pv a6a4
info depth 68 seldepth 2 multipv 1 score mate 1 nodes 3276 nps 655200 tbhits 0 time 5 pv a6a4
info depth 69 seldepth 2 multipv 1 score mate 1 nodes 3324 nps 664800 tbhits 0 time 5 pv a6a4
info depth 70 seldepth 2 multipv 1 score mate 1 nodes 3372 nps 674400 tbhits 0 time 5 pv a6a4
info depth 71 seldepth 2 multipv 1 score mate 1 nodes 3420 nps 684000 tbhits 0 time 5 pv a6a4
info depth 72 seldepth 2 multipv 1 score mate 1 nodes 3468 nps 693600 tbhits 0 time 5 pv a6a4
info depth 73 seldepth 2 multipv 1 score mate 1 nodes 3516 nps 703200 tbhits 0 time 5 pv a6a4
info depth 74 seldepth 2 multipv 1 score mate 1 nodes 3564 nps 712800 tbhits 0 time 5 pv a6a4
info depth 75 seldepth 2 multipv 1 score mate 1 nodes 3612 nps 722400 tbhits 0 time 5 pv a6a4
info depth 76 seldepth 2 multipv 1 score mate 1 nodes 3660 nps 732000 tbhits 0 time 5 pv a6a4
info depth 77 seldepth 2 multipv 1 score mate 1 nodes 3708 nps 741600 tbhits 0 time 5 pv a6a4
info depth 78 seldepth 2 multipv 1 score mate 1 nodes 3756 nps 751200 tbhits 0 time 5 pv a6a4
info depth 79 seldepth 2 multipv 1 score mate 1 nodes 3804 nps 760800 tbhits 0 time 5 pv a6a4
info depth 80 seldepth 2 multipv 1 score mate 1 nodes 3852 nps 770400 tbhits 0 time 5 pv a6a4
info depth 81 seldepth 2 multipv 1 score mate 1 nodes 3900 nps 780000 tbhits 0 time 5 pv a6a4
info depth 82 seldepth 2 multipv 1 score mate 1 nodes 3948 nps 789600 tbhits 0 time 5 pv a6a4
info depth 83 seldepth 2 multipv 1 score mate 1 nodes 3996 nps 799200 tbhits 0 time 5 pv a6a4
info depth 84 seldepth 2 multipv 1 score mate 1 nodes 4044 nps 808800 tbhits 0 time 5 pv a6a4
info depth 85 seldepth 2 multipv 1 score mate 1 nodes 4092 nps 682000 tbhits 0 time 6 pv a6a4
info depth 86 seldepth 2 multipv 1 score mate 1 nodes 4140 nps 690000 tbhits 0 time 6 pv a6a4
info depth 87 seldepth 2 multipv 1 score mate 1 nodes 4188 nps 698000 tbhits 0 time 6 pv a6a4
info depth 88 seldepth 2 multipv 1 score mate 1 nodes 4236 nps 706000 tbhits 0 time 6 pv a6a4
info depth 89 seldepth 2 multipv 1 score mate 1 nodes 4284 nps 714000 tbhits 0 time 6 pv a6a4
info depth 90 seldepth 2 multipv 1 score mate 1 nodes 4332 nps 722000 tbhits 0 time 6 pv a6a4
info depth 91 seldepth 2 multipv 1 score mate 1 nodes 4380 nps 730000 tbhits 0 time 6 pv a6a4
info depth 92 seldepth 2 multipv 1 score mate 1 nodes 4428 nps 738000 tbhits 0 time 6 pv a6a4
info depth 93 seldepth 2 multipv 1 score mate 1 nodes 4476 nps 746000 tbhits 0 time 6 pv a6a4
info depth 94 seldepth 2 multipv 1 score mate 1 nodes 4524 nps 754000 tbhits 0 time 6 pv a6a4
info depth 95 seldepth 2 multipv 1 score mate 1 nodes 4572 nps 762000 tbhits 0 time 6 pv a6a4
info depth 96 seldepth 2 multipv 1 score mate 1 nodes 4620 nps 770000 tbhits 0 time 6 pv a6a4
info depth 97 seldepth 2 multipv 1 score mate 1 nodes 4668 nps 778000 tbhits 0 time 6 pv a6a4
info depth 98 seldepth 2 multipv 1 score mate 1 nodes 4716 nps 786000 tbhits 0 time 6 pv a6a4
info depth 99 seldepth 2 multipv 1 score mate 1 nodes 4764 nps 794000 tbhits 0 time 6 pv a6a4
info depth 100 seldepth 2 multipv 1 score mate 1 nodes 4812 nps 802000 tbhits 0 time 6 pv a6a4
info depth 101 seldepth 2 multipv 1 score mate 1 nodes 4860 nps 810000 tbhits 0 time 6 pv a6a4
info depth 102 seldepth 2 multipv 1 score mate 1 nodes 4908 nps 818000 tbhits 0 time 6 pv a6a4
info depth 103 seldepth 2 multipv 1 score mate 1 nodes 4956 nps 826000 tbhits 0 time 6 pv a6a4
info depth 104 seldepth 2 multipv 1 score mate 1 nodes 5004 nps 834000 tbhits 0 time 6 pv a6a4
info depth 105 seldepth 2 multipv 1 score mate 1 nodes 5052 nps 842000 tbhits 0 time 6 pv a6a4
info depth 106 seldepth 2 multipv 1 score mate 1 nodes 5100 nps 850000 tbhits 0 time 6 pv a6a4
info depth 107 seldepth 2 multipv 1 score mate 1 nodes 5148 nps 735428 tbhits 0 time 7 pv a6a4
info depth 108 seldepth 2 multipv 1 score mate 1 nodes 5196 nps 742285 tbhits 0 time 7 pv a6a4
info depth 109 seldepth 2 multipv 1 score mate 1 nodes 5244 nps 749142 tbhits 0 time 7 pv a6a4
info depth 110 seldepth 2 multipv 1 score mate 1 nodes 5292 nps 756000 tbhits 0 time 7 pv a6a4
info depth 111 seldepth 2 multipv 1 score mate 1 nodes 5340 nps 762857 tbhits 0 time 7 pv a6a4
info depth 112 seldepth 2 multipv 1 score mate 1 nodes 5388 nps 769714 tbhits 0 time 7 pv a6a4
info depth 113 seldepth 2 multipv 1 score mate 1 nodes 5436 nps 776571 tbhits 0 time 7 pv a6a4
info depth 114 seldepth 2 multipv 1 score mate 1 nodes 5484 nps 783428 tbhits 0 time 7 pv a6a4
info depth 115 seldepth 2 multipv 1 score mate 1 nodes 5532 nps 790285 tbhits 0 time 7 pv a6a4
info depth 116 seldepth 2 multipv 1 score mate 1 nodes 5580 nps 797142 tbhits 0 time 7 pv a6a4
info depth 117 seldepth 2 multipv 1 score mate 1 nodes 5628 nps 804000 tbhits 0 time 7 pv a6a4
info depth 118 seldepth 2 multipv 1 score mate 1 nodes 5676 nps 810857 tbhits 0 time 7 pv a6a4
info depth 119 seldepth 2 multipv 1 score mate 1 nodes 5724 nps 817714 tbhits 0 time 7 pv a6a4
info depth 120 seldepth 2 multipv 1 score mate 1 nodes 5772 nps 824571 tbhits 0 time 7 pv a6a4
info depth 121 seldepth 2 multipv 1 score mate 1 nodes 5820 nps 831428 tbhits 0 time 7 pv a6a4
info depth 122 seldepth 2 multipv 1 score mate 1 nodes 5868 nps 838285 tbhits 0 time 7 pv a6a4
info depth 123 seldepth 2 multipv 1 score mate 1 nodes 5916 nps 845142 tbhits 0 time 7 pv a6a4
info depth 124 seldepth 2 multipv 1 score mate 1 nodes 5964 nps 852000 tbhits 0 time 7 pv a6a4
info depth 125 seldepth 2 multipv 1 score mate 1 nodes 6012 nps 858857 tbhits 0 time 7 pv a6a4
info depth 126 seldepth 2 multipv 1 score mate 1 nodes 6060 nps 865714 tbhits 0 time 7 pv a6a4
info depth 127 seldepth 2 multipv 1 score mate 1 nodes 6108 nps 872571 tbhits 0 time 7 pv a6a4
info depth 128 seldepth 2 multipv 1 score mate 1 nodes 6156 nps 879428 tbhits 0 time 7 pv a6a4
info depth 129 seldepth 2 multipv 1 score mate 1 nodes 6204 nps 886285 tbhits 0 time 7 pv a6a4
info depth 130 seldepth 2 multipv 1 score mate 1 nodes 6252 nps 893142 tbhits 0 time 7 pv a6a4
info depth 131 seldepth 2 multipv 1 score mate 1 nodes 6300 nps 787500 tbhits 0 time 8 pv a6a4
info depth 132 seldepth 2 multipv 1 score mate 1 nodes 6348 nps 793500 tbhits 0 time 8 pv a6a4
info depth 133 seldepth 2 multipv 1 score mate 1 nodes 6396 nps 799500 tbhits 0 time 8 pv a6a4
info depth 134 seldepth 2 multipv 1 score mate 1 nodes 6444 nps 805500 tbhits 0 time 8 pv a6a4
info depth 135 seldepth 2 multipv 1 score mate 1 nodes 6492 nps 811500 tbhits 0 time 8 pv a6a4
info depth 136 seldepth 2 multipv 1 score mate 1 nodes 6540 nps 817500 tbhits 0 time 8 pv a6a4
info depth 137 seldepth 2 multipv 1 score mate 1 nodes 6588 nps 823500 tbhits 0 time 8 pv a6a4
info depth 138 seldepth 2 multipv 1 score mate 1 nodes 6636 nps 829500 tbhits 0 time 8 pv a6a4
info depth 139 seldepth 2 multipv 1 score mate 1 nodes 6684 nps 835500 tbhits 0 time 8 pv a6a4
info depth 140 seldepth 2 multipv 1 score mate 1 nodes 6732 nps 841500 tbhits 0 time 8 pv a6a4
info depth 141 seldepth 2 multipv 1 score mate 1 nodes 6780 nps 847500 tbhits 0 time 8 pv a6a4
info depth 142 seldepth 2 multipv 1 score mate 1 nodes 6828 nps 853500 tbhits 0 time 8 pv a6a4
info depth 143 seldepth 2 multipv 1 score mate 1 nodes 6876 nps 859500 tbhits 0 time 8 pv a6a4
info depth 144 seldepth 2 multipv 1 score mate 1 nodes 6924 nps 865500 tbhits 0 time 8 pv a6a4
info depth 145 seldepth 2 multipv 1 score mate 1 nodes 6972 nps 871500 tbhits 0 time 8 pv a6a4
info depth 146 seldepth 2 multipv 1 score mate 1 nodes 7020 nps 877500 tbhits 0 time 8 pv a6a4
info depth 147 seldepth 2 multipv 1 score mate 1 nodes 7068 nps 883500 tbhits 0 time 8 pv a6a4
info depth 148 seldepth 2 multipv 1 score mate 1 nodes 7116 nps 889500 tbhits 0 time 8 pv a6a4
info depth 149 seldepth 2 multipv 1 score mate 1 nodes 7164 nps 895500 tbhits 0 time 8 pv a6a4
info depth 150 seldepth 2 multipv 1 score mate 1 nodes 7212 nps 901500 tbhits 0 time 8 pv a6a4
info depth 151 seldepth 2 multipv 1 score mate 1 nodes 7260 nps 907500 tbhits 0 time 8 pv a6a4
info depth 152 seldepth 2 multipv 1 score mate 1 nodes 7308 nps 913500 tbhits 0 time 8 pv a6a4
info depth 153 seldepth 2 multipv 1 score mate 1 nodes 7356 nps 919500 tbhits 0 time 8 pv a6a4
info depth 154 seldepth 2 multipv 1 score mate 1 nodes 7404 nps 925500 tbhits 0 time 8 pv a6a4
info depth 155 seldepth 2 multipv 1 score mate 1 nodes 7452 nps 828000 tbhits 0 time 9 pv a6a4
info depth 156 seldepth 2 multipv 1 score mate 1 nodes 7500 nps 833333 tbhits 0 time 9 pv a6a4
info depth 157 seldepth 2 multipv 1 score mate 1 nodes 7548 nps 838666 tbhits 0 time 9 pv a6a4
info depth 158 seldepth 2 multipv 1 score mate 1 nodes 7596 nps 844000 tbhits 0 time 9 pv a6a4
info depth 159 seldepth 2 multipv 1 score mate 1 nodes 7644 nps 849333 tbhits 0 time 9 pv a6a4
info depth 160 seldepth 2 multipv 1 score mate 1 nodes 7692 nps 854666 tbhits 0 time 9 pv a6a4
info depth 161 seldepth 2 multipv 1 score mate 1 nodes 7740 nps 860000 tbhits 0 time 9 pv a6a4
info depth 162 seldepth 2 multipv 1 score mate 1 nodes 7788 nps 865333 tbhits 0 time 9 pv a6a4
info depth 163 seldepth 2 multipv 1 score mate 1 nodes 7836 nps 870666 tbhits 0 time 9 pv a6a4
info depth 164 seldepth 2 multipv 1 score mate 1 nodes 7884 nps 876000 tbhits 0 time 9 pv a6a4
info depth 165 seldepth 2 multipv 1 score mate 1 nodes 7932 nps 881333 tbhits 0 time 9 pv a6a4
info depth 166 seldepth 2 multipv 1 score mate 1 nodes 7980 nps 886666 tbhits 0 time 9 pv a6a4
info depth 167 seldepth 2 multipv 1 score mate 1 nodes 8028 nps 892000 tbhits 0 time 9 pv a6a4
info depth 168 seldepth 2 multipv 1 score mate 1 nodes 8076 nps 897333 tbhits 0 time 9 pv a6a4
info depth 169 seldepth 2 multipv 1 score mate 1 nodes 8124 nps 902666 tbhits 0 time 9 pv a6a4
info depth 170 seldepth 2 multipv 1 score mate 1 nodes 8172 nps 908000 tbhits 0 time 9 pv a6a4
info depth 171 seldepth 2 multipv 1 score mate 1 nodes 8220 nps 913333 tbhits 0 time 9 pv a6a4
info depth 172 seldepth 2 multipv 1 score mate 1 nodes 8268 nps 918666 tbhits 0 time 9 pv a6a4
info depth 173 seldepth 2 multipv 1 score mate 1 nodes 8316 nps 924000 tbhits 0 time 9 pv a6a4
info depth 174 seldepth 2 multipv 1 score mate 1 nodes 8364 nps 929333 tbhits 0 time 9 pv a6a4
info depth 175 seldepth 2 multipv 1 score mate 1 nodes 8412 nps 934666 tbhits 0 time 9 pv a6a4
info depth 176 seldepth 2 multipv 1 score mate 1 nodes 8460 nps 940000 tbhits 0 time 9 pv a6a4
info depth 177 seldepth 2 multipv 1 score mate 1 nodes 8508 nps 945333 tbhits 0 time 9 pv a6a4
info depth 178 seldepth 2 multipv 1 score mate 1 nodes 8556 nps 950666 tbhits 0 time 9 pv a6a4
info depth 179 seldepth 2 multipv 1 score mate 1 nodes 8604 nps 956000 tbhits 0 time 9 pv a6a4
info depth 180 seldepth 2 multipv 1 score mate 1 nodes 8652 nps 961333 tbhits 0 time 9 pv a6a4
info depth 181 seldepth 2 multipv 1 score mate 1 nodes 8700 nps 966666 tbhits 0 time 9 pv a6a4
info depth 182 seldepth 2 multipv 1 score mate 1 nodes 8748 nps 972000 tbhits 0 time 9 pv a6a4
info depth 183 seldepth 2 multipv 1 score mate 1 nodes 8796 nps 879600 tbhits 0 time 10 pv a6a4
info depth 184 seldepth 2 multipv 1 score mate 1 nodes 8844 nps 884400 tbhits 0 time 10 pv a6a4
info depth 185 seldepth 2 multipv 1 score mate 1 nodes 8892 nps 889200 tbhits 0 time 10 pv a6a4
info depth 186 seldepth 2 multipv 1 score mate 1 nodes 8940 nps 894000 tbhits 0 time 10 pv a6a4
info depth 187 seldepth 2 multipv 1 score mate 1 nodes 8988 nps 898800 tbhits 0 time 10 pv a6a4
info depth 188 seldepth 2 multipv 1 score mate 1 nodes 9036 nps 903600 tbhits 0 time 10 pv a6a4
info depth 189 seldepth 2 multipv 1 score mate 1 nodes 9084 nps 908400 tbhits 0 time 10 pv a6a4
info depth 190 seldepth 2 multipv 1 score mate 1 nodes 9132 nps 913200 tbhits 0 time 10 pv a6a4
info depth 191 seldepth 2 multipv 1 score mate 1 nodes 9180 nps 918000 tbhits 0 time 10 pv a6a4
info depth 192 seldepth 2 multipv 1 score mate 1 nodes 9228 nps 922800 tbhits 0 time 10 pv a6a4
info depth 193 seldepth 2 multipv 1 score mate 1 nodes 9276 nps 927600 tbhits 0 time 10 pv a6a4
info depth 194 seldepth 2 multipv 1 score mate 1 nodes 9324 nps 932400 tbhits 0 time 10 pv a6a4
info depth 195 seldepth 2 multipv 1 score mate 1 nodes 9372 nps 937200 tbhits 0 time 10 pv a6a4
info depth 196 seldepth 2 multipv 1 score mate 1 nodes 9420 nps 942000 tbhits 0 time 10 pv a6a4
info depth 197 seldepth 2 multipv 1 score mate 1 nodes 9468 nps 946800 tbhits 0 time 10 pv a6a4
info depth 198 seldepth 2 multipv 1 score mate 1 nodes 9516 nps 951600 tbhits 0 time 10 pv a6a4
info depth 199 seldepth 2 multipv 1 score mate 1 nodes 9564 nps 956400 tbhits 0 time 10 pv a6a4
info depth 200 seldepth 2 multipv 1 score mate 1 nodes 9612 nps 961200 tbhits 0 time 10 pv a6a4
info depth 201 seldepth 2 multipv 1 score mate 1 nodes 9660 nps 966000 tbhits 0 time 10 pv a6a4
info depth 202 seldepth 2 multipv 1 score mate 1 nodes 9708 nps 970800 tbhits 0 time 10 pv a6a4
info depth 203 seldepth 2 multipv 1 score mate 1 nodes 9756 nps 975600 tbhits 0 time 10 pv a6a4
info depth 204 seldepth 2 multipv 1 score mate 1 nodes 9804 nps 980400 tbhits 0 time 10 pv a6a4
info depth 205 seldepth 2 multipv 1 score mate 1 nodes 9852 nps 985200 tbhits 0 time 10 pv a6a4
info depth 206 seldepth 2 multipv 1 score mate 1 nodes 9900 nps 990000 tbhits 0 time 10 pv a6a4
info depth 207 seldepth 2 multipv 1 score mate 1 nodes 9948 nps 904363 tbhits 0 time 11 pv a6a4
info depth 208 seldepth 2 multipv 1 score mate 1 nodes 9996 nps 908727 tbhits 0 time 11 pv a6a4
info depth 209 seldepth 2 multipv 1 score mate 1 nodes 10003 nps 909363 tbhits 0 time 11 pv a6a4
```

candidateMoves: `['a6a4']`; events: `[{'type': 'LAST_OPPONENT_KING_CAPTURED', 'side': 'BLACK', 'ply': 122, 'square': 'a4', 'kingId': 'WK1', 'result': 'BLACK_WIN'}, {'type': 'KING_LEFT_UNDER_ATTACK', 'side': 'BLACK', 'ply': 122, 'kingSquaresBefore': ['g8'], 'kingSquaresAfter': ['g8'], 'createdDebt': False, 'endedByLastKingCapture': True}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'g8', 'to': 'f7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g8', 'to': 'g7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g8', 'to': 'h7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g8', 'to': 'f8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g8', 'to': 'h8', 'moveKind': 'NORMAL', 'promotion': None}]`.

Финальная позиция: 10 фигур; короли `[('g8', 'bk1')]`; toMove `WHITE`, result `BLACK_WIN`.

King IDs/counters: `{'6': 'BK1'}` / `{'w': 1, 'b': 1}`.

## B-29

Лог: `B/games/game-000029.jsonl.gz`; seed `21261035`; parent `None`; итог `BLACK_WIN` / `BLACK_WIN`, длина 86 игровых полуходов.

### ply 48: {'type': 'MOVE', 'from': 'd6', 'to': 'h2', 'moveKind': 'NORMAL', 'promotion': None}

До (24 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('d6', 'bq', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wr', '1'), ('g1', 'wk', '1')]`; ход `BLACK`, долг `None`, полуходы `0`, повторы `48`.

После (24 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wr', '1'), ('g1', 'wk', '1')]`; ход `WHITE`, долг `None`, короли под боем `[{'king': 'g1', 'kingId': 'wk1', 'attacker': 'h2', 'attackerId': 'bq1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 449 nodes 50 nps 50000 tbhits 0 time 1 pv d6h2
info depth 2 seldepth 2 multipv 1 score cp 676 nodes 101 nps 101000 tbhits 0 time 1 pv d6h2 g1f1
info depth 3 seldepth 3 multipv 1 score cp 707 nodes 164 nps 164000 tbhits 0 time 1 pv d6h2 g1f1 f6f5
info depth 4 seldepth 4 multipv 1 score cp 761 nodes 229 nps 229000 tbhits 0 time 1 pv d6h2 g1f1 f6f5
info depth 5 seldepth 5 multipv 1 score cp 761 nodes 328 nps 164000 tbhits 0 time 2 pv d6h2 g1f1 e8c8 d2a2
info depth 6 seldepth 6 multipv 1 score cp 811 nodes 496 nps 248000 tbhits 0 time 2 pv d6h2 g1f1 f6f5 d3c2
info depth 7 seldepth 7 multipv 1 score cp 885 nodes 720 nps 360000 tbhits 0 time 2 pv d6h2 g1f1 f6f5 d3c2
info depth 8 seldepth 9 multipv 1 score cp 1176 nodes 1966 nps 393200 tbhits 0 time 5 pv d6h2 g1f1 f6f5
info depth 9 seldepth 11 multipv 1 score cp 968 nodes 7331 nps 385842 tbhits 0 time 19 pv d6h2 g1f1 h2h1 e2g1 c6d4 c3d4 g4g3
info depth 10 seldepth 10 multipv 1 score cp 1038 nodes 7881 nps 414789 tbhits 0 time 19 pv d6h2 g1f1 h2h1 e2g1 c6d4 c3d4
info depth 11 seldepth 15 multipv 1 score cp 1063 nodes 14845 nps 390657 tbhits 0 time 38 pv d6h2 g1f1 g4g3 e2g1 h2d2 c1d2 c6d4 c3d4 g3g2 f1f2
info depth 12 seldepth 17 multipv 1 score cp 1084 nodes 30486 nps 385898 tbhits 0 time 79 pv d6h2 g1f1 g4g3 e2g1 c6d4 c3d4 h2d2 c1d2 h4d4 d2c3 d4d3 c3f6 e8f7 f6g5 a6a5
info depth 13 seldepth 18 multipv 1 score cp 1107 nodes 58845 nps 394932 tbhits 0 time 149 pv d6h2 g1f1 g4g3 e2g1 c6d4 c3d4 h2d2 c1d2 h4d4 f1e2 a8d8 d2c3 d4d3 c3f6 e8f7
info depth 14 seldepth 21 multipv 1 score cp 1087 nodes 100031 nps 401730 tbhits 0 time 249 pv d6h2
```

candidateMoves: `['d6h2']`; events: `[]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'e8', 'to': 'd7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e8', 'to': 'e7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e8', 'to': 'f7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e8', 'to': 'd8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e8', 'to': 'f8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e8', 'to': 'c8', 'moveKind': 'CASTLE_QUEEN', 'promotion': None}]`.

### ply 48: {'type': 'OFFER_DRAW'}

До (24 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wr', '1'), ('g1', 'wk', '1')]`; ход `WHITE`, долг `None`, полуходы `1`, повторы `49`.

После (24 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wr', '1'), ('g1', 'wk', '1')]`; ход `WHITE`, долг `None`, короли под боем `[{'king': 'g1', 'kingId': 'wk1', 'attacker': 'h2', 'attackerId': 'bq1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -1087 nodes 44 nps 44000 tbhits 0 time 1 pv g1f1
info depth 2 seldepth 2 multipv 1 score cp -1087 nodes 87 nps 87000 tbhits 0 time 1 pv g1f1 g4g3
info depth 3 seldepth 3 multipv 1 score cp -1088 nodes 132 nps 132000 tbhits 0 time 1 pv g1f1 g4g3 e2g1
info depth 4 seldepth 4 multipv 1 score cp -838 nodes 192 nps 96000 tbhits 0 time 2 pv g1f1 g4g3
info depth 5 seldepth 5 multipv 1 score cp -1065 nodes 363 nps 181500 tbhits 0 time 2 pv g1f1 g4g3 e2g1 c6d8 d2c2 h2c2 d3c2 d8e6
info depth 6 seldepth 6 multipv 1 score cp -1065 nodes 473 nps 236500 tbhits 0 time 2 pv g1f1 g4g3 e2g1 c6d8 d2c2 h2c2
info depth 7 seldepth 7 multipv 1 score cp -1065 nodes 600 nps 300000 tbhits 0 time 2 pv g1f1 g4g3 e2g1 c6d8 d2c2 h2c2 d3c2
info depth 8 seldepth 11 multipv 1 score cp -946 nodes 1121 nps 373666 tbhits 0 time 3 pv g1f1 g4g3 e2g1 h2d2 c1d2
info depth 9 seldepth 11 multipv 1 score cp -1065 nodes 1765 nps 441250 tbhits 0 time 4 pv g1f1 g4g3 e2g1 c6d8 d2c2 h2c2 d3c2 d8e6 b1a1 g3g2 f1f2
info depth 10 seldepth 15 multipv 1 score cp -1062 nodes 8495 nps 404523 tbhits 0 time 21 pv g1f1 g4g3 e2g1 a6a5 b4a5 c6a5 b1b4 h2d2 c1d2 e8c8 d2e1
info depth 11 seldepth 15 multipv 1 score cp -1094 nodes 19992 nps 425361 tbhits 0 time 47 pv g1f1 g4g3 d2e3 g3g2 f1e1 h4h3 d3g6 e8d8
info depth 12 seldepth 13 multipv 1 score cp -936 nodes 24051 nps 429482 tbhits 0 time 56 pv g1f1 g4g3 e2g1 e8f8 d2e1 g3g2 f1f2
info depth 13 seldepth 17 multipv 1 score cp -1070 nodes 78045 nps 394166 tbhits 0 time 198 pv g1f1 g4g3 e2g1 a6a5 b4b5 c6d4 c3d4 h2d2 c1d2 h4d4 d2c3 d4d3 c3f6 e7f5 f6g5
info depth 14 seldepth 17 multipv 1 score cp -1070 nodes 100030 nps 390742 tbhits 0 time 256 pv g1f1 g4g3 e2g1 a6a5 b4b5 c6d4 c3d4 h2d2 c1d2 h4d4 d2c3 d4d3 c3f6 e7f5 f6g5
```

candidateMoves: `None`; events: `[{'type': 'DRAW_OFFERED', 'side': 'WHITE', 'ply': 48, 'stateResult': 'NONE'}]`.

### ply 49: {'type': 'MOVE', 'from': 'g1', 'to': 'f1', 'moveKind': 'NORMAL', 'promotion': None}

До (24 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wr', '1'), ('g1', 'wk', '1')]`; ход `WHITE`, долг `None`, полуходы `1`, повторы `49`.

После (23 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wk', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -1070 nodes 41 nps 20500 tbhits 0 time 2 pv g1f1
info depth 2 seldepth 2 multipv 1 score cp -1070 nodes 83 nps 41500 tbhits 0 time 2 pv g1f1 g4g3
info depth 3 seldepth 3 multipv 1 score cp -1070 nodes 127 nps 63500 tbhits 0 time 2 pv g1f1 g4g3 e2g1
info depth 4 seldepth 4 multipv 1 score cp -838 nodes 181 nps 90500 tbhits 0 time 2 pv g1f1 g4g3
info depth 5 seldepth 5 multipv 1 score cp -946 nodes 394 nps 197000 tbhits 0 time 2 pv g1f1 g4g3 e2g1 h2d2 c1d2
info depth 6 seldepth 6 multipv 1 score cp -976 nodes 537 nps 268500 tbhits 0 time 2 pv g1f1 g4g3 e2g1 e8f8 b1b2 h2d2 b2d2
info depth 7 seldepth 7 multipv 1 score cp -969 nodes 782 nps 260666 tbhits 0 time 3 pv g1f1 g4g3 e2g1 e8f8 b1b2 h2d2 b2d2
info depth 8 seldepth 10 multipv 1 score cp -1007 nodes 1442 nps 288400 tbhits 0 time 5 pv g1f1 g4g3 e2g1 a6a5 b4b5 c6d8 b1b2 h2d2 b2d2
info depth 9 seldepth 13 multipv 1 score cp -1066 nodes 3209 nps 356555 tbhits 0 time 9 pv g1f1 g4g3 e2g1 a6a5 b4b5 c6d8 d2e3 d8e6 b1b2 g3g2 f1e1
info depth 10 seldepth 15 multipv 1 score cp -946 nodes 5885 nps 452692 tbhits 0 time 13 pv g1f1 g4g3 e2g1 h2d2 c1d2
info depth 11 seldepth 17 multipv 1 score cp -1073 nodes 19159 nps 435431 tbhits 0 time 44 pv g1f1 g4g3 e2g1 a6a5 b4a5 h2d2 c1d2 c6a5 b1e1 c7c5 d3b5
info depth 12 seldepth 17 multipv 1 score cp -1157 nodes 73408 nps 394666 tbhits 0 time 186 pv g1f1 g4g3 e2g1 c6d4 c3d4 h4d4 b1b2 g3g2 f1f2 a8d8 d3g6 e7g6 d2h6 h2h6 c1h6 d4d1
info depth 13 seldepth 22 multipv 1 score cp -982 nodes 89848 nps 387275 tbhits 0 time 232 pv g1f1 g4g3 e2g1 h2d2 c1d2 c6d8
info depth 14 seldepth 22 multipv 1 score cp -982 nodes 100113 nps 391066 tbhits 0 time 256 pv g1f1 g4g3 e2g1 h2d2 c1d2 c6d8
```

candidateMoves: `['g1f1']`; events: `[{'type': 'KING_SELF_CAPTURE', 'side': 'WHITE', 'ply': 49, 'from': 'g1', 'to': 'f1', 'kingId': 'WK1', 'capturedType': 'ROOK', 'capturedPieceId': None, 'kingAttackedBefore': True, 'kingAttackedAfter': False, 'safeNonSelfCaptureDestinations': [], 'hasSafeNonSelfCapture': False}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'g1', 'to': 'f1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g1', 'to': 'h1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g1', 'to': 'f2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g1', 'to': 'g2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'g1', 'to': 'h2', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 49: {'type': 'DECLINE_DRAW'}

До (23 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wk', '1')]`; ход `BLACK`, долг `None`, полуходы `0`, повторы `50`.

После (23 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('h4', 'br', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('h2', 'bq', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('f1', 'wk', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp 982 nodes 49 nps 49000 tbhits 0 time 1 pv g4g3
info depth 2 seldepth 2 multipv 1 score cp 992 nodes 96 nps 96000 tbhits 0 time 1 pv g4g3 e2g1
info depth 3 seldepth 3 multipv 1 score cp 992 nodes 210 nps 210000 tbhits 0 time 1 pv g4g3 e2g1 a6a5
info depth 4 seldepth 4 multipv 1 score cp 991 nodes 432 nps 216000 tbhits 0 time 2 pv g4g3 d2e3 g3g2 f1e1
info depth 5 seldepth 5 multipv 1 score cp 991 nodes 709 nps 354500 tbhits 0 time 2 pv g4g3 d2e3 g3g2 f1e1 h4h3
info depth 6 seldepth 6 multipv 1 score cp 991 nodes 1130 nps 376666 tbhits 0 time 3 pv g4g3 d2e3 g3g2 f1e1 h4h3 d3g6
info depth 7 seldepth 7 multipv 1 score cp 991 nodes 1770 nps 442500 tbhits 0 time 4 pv g4g3 d2e3 g3g2 f1e1 h4h3 d3g6 e8d8
info depth 8 seldepth 8 multipv 1 score cp 992 nodes 2204 nps 551000 tbhits 0 time 4 pv g4g3 e2g1 a6a5 d2e1 c6b4 c3b4
info depth 9 seldepth 10 multipv 1 score cp 1051 nodes 2669 nps 533800 tbhits 0 time 5 pv g4g3 e2g1 a6a5 b4b5 c6d8
info depth 10 seldepth 10 multipv 1 score cp 1051 nodes 4218 nps 602571 tbhits 0 time 7 pv g4g3 e2g1 a6a5 b4b5 c6d8 b1b2 h2d2 b2d2 d8e6 d3c4 e6f4
info depth 11 seldepth 15 multipv 1 score cp 1092 nodes 10733 nps 466652 tbhits 0 time 23 pv g4g3 e2g1 a6a5 d3b5 h2d2 c1d2 a5b4 c3b4
info depth 12 seldepth 15 multipv 1 score cp 1122 nodes 28928 nps 438303 tbhits 0 time 66 pv g4g3 e2g1 c6d4 c3d4 h4d4 b1b3 a8d8 d2c2 d4d3 c2d3 d8d3 b3d3
info depth 13 seldepth 16 multipv 1 score cp 1134 nodes 66858 nps 425847 tbhits 0 time 157 pv g4g3 e2g1 c6d4 c3d4 h4d4 d2e3 d4d3 e3d3 e8c8 d3d8 c8d8 b1b2 g3g2 f1f2 h2h4 f2e2
info depth 14 seldepth 19 multipv 1 score cp 1142 nodes 100019 nps 392231 tbhits 0 time 255 pv g4g3
```

candidateMoves: `None`; events: `[{'type': 'DRAW_DECLINED', 'side': 'BLACK', 'ply': 49, 'stateResult': 'NONE'}]`.

### ply 55: {'type': 'MOVE', 'from': 'e1', 'to': 'd2', 'moveKind': 'NORMAL', 'promotion': None}

До (23 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('f3', 'bq', '1'), ('d2', 'wq', '1'), ('e2', 'wn', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('e1', 'wk', '1'), ('h1', 'br', '1')]`; ход `WHITE`, долг `None`, полуходы `5`, повторы `55`.

После (22 фигур): `[('a8', 'br', '0'), ('e8', 'bk', '0'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('e7', 'bn', '1'), ('a6', 'bp', '1'), ('b6', 'bp', '1'), ('c6', 'bn', '1'), ('f6', 'bp', '1'), ('g6', 'bp', '1'), ('a4', 'wp', '1'), ('b4', 'wp', '1'), ('d4', 'wp', '1'), ('g4', 'bp', '1'), ('c3', 'wp', '1'), ('d3', 'wb', '1'), ('f3', 'bq', '1'), ('d2', 'wk', '1'), ('e2', 'wn', '1'), ('b1', 'wr', '1'), ('c1', 'wb', '0'), ('h1', 'br', '1')]`; ход `BLACK`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score cp -2646 nodes 34 nps 34000 tbhits 0 time 1 pv e1d2
info depth 2 seldepth 2 multipv 1 score cp -2646 nodes 73 nps 73000 tbhits 0 time 1 pv e1d2 h1d1
info depth 3 seldepth 3 multipv 1 score cp -2646 nodes 113 nps 113000 tbhits 0 time 1 pv e1d2 h1d1 d2d1
info depth 4 seldepth 4 multipv 1 score cp -2646 nodes 160 nps 160000 tbhits 0 time 1 pv e1d2 h1d1 d2d1 f3d3
info depth 5 seldepth 5 multipv 1 score cp -2646 nodes 211 nps 211000 tbhits 0 time 1 pv e1d2 h1d1 d2d1 f3d3 d1e1
info depth 6 seldepth 6 multipv 1 score cp -2646 nodes 275 nps 275000 tbhits 0 time 1 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1
info depth 7 seldepth 7 multipv 1 score cp -2646 nodes 350 nps 350000 tbhits 0 time 1 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2
info depth 8 seldepth 11 multipv 1 score cp -2646 nodes 480 nps 480000 tbhits 0 time 1 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2 e7d5 c1d2 b1d3 d2e1
info depth 9 seldepth 15 multipv 1 score cp -2061 nodes 764 nps 382000 tbhits 0 time 2 pv e1d2 g4g3
info depth 10 seldepth 11 multipv 1 score cp -2246 nodes 6475 nps 462500 tbhits 0 time 14 pv e1d2 g4g3 b4b5 a6b5 c1a3 g3g2 a3e7 e8e7 b1b3 b5a4 b3a3 g2g1r e2g1
info depth 11 seldepth 15 multipv 1 score cp -2636 nodes 11427 nps 439500 tbhits 0 time 26 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2 e7d5 c1d2 b1f5 f2e1 f5d3 a4a5 g4g3
info depth 12 seldepth 18 multipv 1 score cp -2577 nodes 19787 nps 449704 tbhits 0 time 44 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2 b1c2 f2f1 c2a4 c1f4
info depth 13 seldepth 19 multipv 1 score cp -2638 nodes 70110 nps 432777 tbhits 0 time 162 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2 e7d5 a4a5 b1d3 c1f4 d3c2 a5b6 c7b6 f4d6 d5c3
info depth 14 seldepth 19 multipv 1 score cp -2638 nodes 100031 nps 415066 tbhits 0 time 241 pv e1d2 h1d1 d2d1 f3d3 d1e1 d3b1 e1f2 e7d5 a4a5 b1d3 c1f4 d3c2 a5b6 c7b6 f4d6 d5c3
```

candidateMoves: `['e1d2']`; events: `[{'type': 'KING_SELF_CAPTURE', 'side': 'WHITE', 'ply': 55, 'from': 'e1', 'to': 'd2', 'kingId': 'WK1', 'capturedType': 'QUEEN', 'capturedPieceId': None, 'kingAttackedBefore': True, 'kingAttackedAfter': False, 'safeNonSelfCaptureDestinations': ['d1'], 'hasSafeNonSelfCapture': True}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'e1', 'to': 'd1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'f1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'd2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'e2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'f2', 'moveKind': 'NORMAL', 'promotion': None}]`.

B-29 сравнение ходов короля e1: `[{'type': 'MOVE', 'from': 'e1', 'to': 'd1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'd2', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 84: {'type': 'MOVE', 'from': 'd4', 'to': 'c2', 'moveKind': 'NORMAL', 'promotion': None}

До (17 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('a6', 'bp', '1'), ('b6', 'wp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('d4', 'bn', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('e1', 'wk', '1'), ('g1', 'wn', '1')]`; ход `BLACK`, долг `None`, полуходы `0`, повторы `84`.

После (17 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('a6', 'bp', '1'), ('b6', 'wp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('c2', 'bn', '1'), ('e1', 'wk', '1'), ('g1', 'wn', '1')]`; ход `WHITE`, долг `None`, короли под боем `[{'king': 'e1', 'kingId': 'wk1', 'attacker': 'c2', 'attackerId': 'bn1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score mate 2 nodes 53 nps 53000 tbhits 0 time 1 pv d4c2
info depth 2 seldepth 2 multipv 1 score mate 2 nodes 122 nps 122000 tbhits 0 time 1 pv d4c2 e1f2
info depth 3 seldepth 3 multipv 1 score mate 2 nodes 190 nps 190000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 4 seldepth 4 multipv 1 score mate 2 nodes 258 nps 258000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 5 seldepth 4 multipv 1 score mate 2 nodes 326 nps 326000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 6 seldepth 4 multipv 1 score mate 2 nodes 394 nps 394000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 7 seldepth 4 multipv 1 score mate 2 nodes 462 nps 462000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 8 seldepth 4 multipv 1 score mate 2 nodes 530 nps 530000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 9 seldepth 4 multipv 1 score mate 2 nodes 599 nps 599000 tbhits 0 time 1 pv d4c2 e1f2 g3f2
info depth 10 seldepth 4 multipv 1 score mate 2 nodes 672 nps 336000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 11 seldepth 4 multipv 1 score mate 2 nodes 741 nps 370500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 12 seldepth 4 multipv 1 score mate 2 nodes 810 nps 405000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 13 seldepth 4 multipv 1 score mate 2 nodes 879 nps 439500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 14 seldepth 4 multipv 1 score mate 2 nodes 948 nps 474000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 15 seldepth 4 multipv 1 score mate 2 nodes 1017 nps 508500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 16 seldepth 4 multipv 1 score mate 2 nodes 1086 nps 543000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 17 seldepth 4 multipv 1 score mate 2 nodes 1155 nps 577500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 18 seldepth 4 multipv 1 score mate 2 nodes 1224 nps 612000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 19 seldepth 4 multipv 1 score mate 2 nodes 1293 nps 646500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 20 seldepth 4 multipv 1 score mate 2 nodes 1362 nps 681000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 21 seldepth 4 multipv 1 score mate 2 nodes 1431 nps 715500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 22 seldepth 4 multipv 1 score mate 2 nodes 1500 nps 750000 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 23 seldepth 4 multipv 1 score mate 2 nodes 1569 nps 784500 tbhits 0 time 2 pv d4c2 e1f2 g3f2
info depth 24 seldepth 4 multipv 1 score mate 2 nodes 1640 nps 546666 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 25 seldepth 4 multipv 1 score mate 2 nodes 1711 nps 570333 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 26 seldepth 4 multipv 1 score mate 2 nodes 1780 nps 593333 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 27 seldepth 4 multipv 1 score mate 2 nodes 1851 nps 617000 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 28 seldepth 4 multipv 1 score mate 2 nodes 1922 nps 640666 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 29 seldepth 4 multipv 1 score mate 2 nodes 1991 nps 663666 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 30 seldepth 4 multipv 1 score mate 2 nodes 2062 nps 687333 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 31 seldepth 4 multipv 1 score mate 2 nodes 2133 nps 711000 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 32 seldepth 4 multipv 1 score mate 2 nodes 2202 nps 734000 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 33 seldepth 4 multipv 1 score mate 2 nodes 2273 nps 757666 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 34 seldepth 4 multipv 1 score mate 2 nodes 2344 nps 781333 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 35 seldepth 4 multipv 1 score mate 2 nodes 2413 nps 804333 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 36 seldepth 4 multipv 1 score mate 2 nodes 2484 nps 828000 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 37 seldepth 4 multipv 1 score mate 2 nodes 2555 nps 851666 tbhits 0 time 3 pv d4c2 e1f2 g3f2
info depth 38 seldepth 4 multipv 1 score mate 2 nodes 2626 nps 656500 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 39 seldepth 4 multipv 1 score mate 2 nodes 2695 nps 673750 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 40 seldepth 4 multipv 1 score mate 2 nodes 2766 nps 691500 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 41 seldepth 4 multipv 1 score mate 2 nodes 2837 nps 709250 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 42 seldepth 4 multipv 1 score mate 2 nodes 2906 nps 726500 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 43 seldepth 4 multipv 1 score mate 2 nodes 2977 nps 744250 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 44 seldepth 4 multipv 1 score mate 2 nodes 3048 nps 762000 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 45 seldepth 4 multipv 1 score mate 2 nodes 3117 nps 779250 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 46 seldepth 4 multipv 1 score mate 2 nodes 3188 nps 797000 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 47 seldepth 4 multipv 1 score mate 2 nodes 3259 nps 814750 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 48 seldepth 4 multipv 1 score mate 2 nodes 3328 nps 832000 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 49 seldepth 4 multipv 1 score mate 2 nodes 3399 nps 849750 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 50 seldepth 4 multipv 1 score mate 2 nodes 3470 nps 867500 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 51 seldepth 4 multipv 1 score mate 2 nodes 3539 nps 884750 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 52 seldepth 4 multipv 1 score mate 2 nodes 3610 nps 902500 tbhits 0 time 4 pv d4c2 e1f2 g3f2
info depth 53 seldepth 4 multipv 1 score mate 2 nodes 3681 nps 736200 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 54 seldepth 4 multipv 1 score mate 2 nodes 3750 nps 750000 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 55 seldepth 4 multipv 1 score mate 2 nodes 3821 nps 764200 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 56 seldepth 4 multipv 1 score mate 2 nodes 3892 nps 778400 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 57 seldepth 4 multipv 1 score mate 2 nodes 3963 nps 792600 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 58 seldepth 4 multipv 1 score mate 2 nodes 4032 nps 806400 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 59 seldepth 4 multipv 1 score mate 2 nodes 4103 nps 820600 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 60 seldepth 4 multipv 1 score mate 2 nodes 4174 nps 834800 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 61 seldepth 4 multipv 1 score mate 2 nodes 4243 nps 848600 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 62 seldepth 4 multipv 1 score mate 2 nodes 4314 nps 862800 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 63 seldepth 4 multipv 1 score mate 2 nodes 4385 nps 877000 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 64 seldepth 4 multipv 1 score mate 2 nodes 4454 nps 890800 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 65 seldepth 4 multipv 1 score mate 2 nodes 4525 nps 905000 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 66 seldepth 4 multipv 1 score mate 2 nodes 4596 nps 919200 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 67 seldepth 4 multipv 1 score mate 2 nodes 4665 nps 933000 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 68 seldepth 4 multipv 1 score mate 2 nodes 4736 nps 947200 tbhits 0 time 5 pv d4c2 e1f2 g3f2
info depth 69 seldepth 4 multipv 1 score mate 2 nodes 4807 nps 801166 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 70 seldepth 4 multipv 1 score mate 2 nodes 4876 nps 812666 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 71 seldepth 4 multipv 1 score mate 2 nodes 4947 nps 824500 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 72 seldepth 4 multipv 1 score mate 2 nodes 5018 nps 836333 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 73 seldepth 4 multipv 1 score mate 2 nodes 5087 nps 847833 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 74 seldepth 4 multipv 1 score mate 2 nodes 5158 nps 859666 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 75 seldepth 4 multipv 1 score mate 2 nodes 5229 nps 871500 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 76 seldepth 4 multipv 1 score mate 2 nodes 5300 nps 883333 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 77 seldepth 4 multipv 1 score mate 2 nodes 5369 nps 894833 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 78 seldepth 4 multipv 1 score mate 2 nodes 5440 nps 906666 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 79 seldepth 4 multipv 1 score mate 2 nodes 5511 nps 918500 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 80 seldepth 4 multipv 1 score mate 2 nodes 5580 nps 930000 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 81 seldepth 4 multipv 1 score mate 2 nodes 5651 nps 941833 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 82 seldepth 4 multipv 1 score mate 2 nodes 5722 nps 953666 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 83 seldepth 4 multipv 1 score mate 2 nodes 5791 nps 965166 tbhits 0 time 6 pv d4c2 e1f2 g3f2
info depth 84 seldepth 4 multipv 1 score mate 2 nodes 5862 nps 837428 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 85 seldepth 4 multipv 1 score mate 2 nodes 5933 nps 847571 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 86 seldepth 4 multipv 1 score mate 2 nodes 6002 nps 857428 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 87 seldepth 4 multipv 1 score mate 2 nodes 6073 nps 867571 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 88 seldepth 4 multipv 1 score mate 2 nodes 6144 nps 877714 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 89 seldepth 4 multipv 1 score mate 2 nodes 6213 nps 887571 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 90 seldepth 4 multipv 1 score mate 2 nodes 6284 nps 897714 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 91 seldepth 4 multipv 1 score mate 2 nodes 6355 nps 907857 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 92 seldepth 4 multipv 1 score mate 2 nodes 6424 nps 917714 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 93 seldepth 4 multipv 1 score mate 2 nodes 6495 nps 927857 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 94 seldepth 4 multipv 1 score mate 2 nodes 6566 nps 938000 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 95 seldepth 4 multipv 1 score mate 2 nodes 6635 nps 947857 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 96 seldepth 4 multipv 1 score mate 2 nodes 6706 nps 958000 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 97 seldepth 4 multipv 1 score mate 2 nodes 6777 nps 968142 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 98 seldepth 4 multipv 1 score mate 2 nodes 6848 nps 978285 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 99 seldepth 4 multipv 1 score mate 2 nodes 6917 nps 988142 tbhits 0 time 7 pv d4c2 e1f2 g3f2
info depth 100 seldepth 4 multipv 1 score mate 2 nodes 6988 nps 873500 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 101 seldepth 4 multipv 1 score mate 2 nodes 7059 nps 882375 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 102 seldepth 4 multipv 1 score mate 2 nodes 7128 nps 891000 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 103 seldepth 4 multipv 1 score mate 2 nodes 7199 nps 899875 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 104 seldepth 4 multipv 1 score mate 2 nodes 7270 nps 908750 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 105 seldepth 4 multipv 1 score mate 2 nodes 7339 nps 917375 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 106 seldepth 4 multipv 1 score mate 2 nodes 7410 nps 926250 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 107 seldepth 4 multipv 1 score mate 2 nodes 7481 nps 935125 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 108 seldepth 4 multipv 1 score mate 2 nodes 7550 nps 943750 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 109 seldepth 4 multipv 1 score mate 2 nodes 7621 nps 952625 tbhits 0 time 8 pv d4c2 e1f2 g3f2
info depth 110 seldepth 4 multipv 1 score mate 2 nodes 7692 nps 854666 tbhits 0 time 9 pv d4c2 e1f2 g3f2
info depth 111 seldepth 4 multipv 1 score mate 2 nodes 7761 nps 862333 tbhits 0 time 9 pv d4c2 e1f2 g3f2
info depth 112 seldepth 4 multipv 1 score mate 2 nodes 7832 nps 870222 tbhits 0 time 9 pv d4c2 e1f2 g3f2
info depth 113 seldepth 4 multipv 1 score mate 2 nodes 7903 nps 878111 tbhits 0 time 9 pv d4c2 e1f2 g3f2
info depth 114 seldepth 4 multipv 1 score mate 2 nodes 7972 nps 885777 tbhits 0 time 9 pv d4c2 e1f2 g3f2
info depth 115 seldepth 4 multipv 1 score mate 2 nodes 8043 nps 804300 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 116 seldepth 4 multipv 1 score mate 2 nodes 8114 nps 811400 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 117 seldepth 4 multipv 1 score mate 2 nodes 8185 nps 818500 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 118 seldepth 4 multipv 1 score mate 2 nodes 8254 nps 825400 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 119 seldepth 4 multipv 1 score mate 2 nodes 8325 nps 832500 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 120 seldepth 4 multipv 1 score mate 2 nodes 8396 nps 839600 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 121 seldepth 4 multipv 1 score mate 2 nodes 8465 nps 846500 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 122 seldepth 4 multipv 1 score mate 2 nodes 8536 nps 853600 tbhits 0 time 10 pv d4c2 e1f2 g3f2
info depth 123 seldepth 4 multipv 1 score mate 2 nodes 8607 nps 782454 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 124 seldepth 4 multipv 1 score mate 2 nodes 8676 nps 788727 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 125 seldepth 4 multipv 1 score mate 2 nodes 8747 nps 795181 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 126 seldepth 4 multipv 1 score mate 2 nodes 8818 nps 801636 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 127 seldepth 4 multipv 1 score mate 2 nodes 8887 nps 807909 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 128 seldepth 4 multipv 1 score mate 2 nodes 8958 nps 814363 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 129 seldepth 4 multipv 1 score mate 2 nodes 9029 nps 820818 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 130 seldepth 4 multipv 1 score mate 2 nodes 9098 nps 827090 tbhits 0 time 11 pv d4c2 e1f2 g3f2
info depth 131 seldepth 4 multipv 1 score mate 2 nodes 9169 nps 764083 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 132 seldepth 4 multipv 1 score mate 2 nodes 9240 nps 770000 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 133 seldepth 4 multipv 1 score mate 2 nodes 9309 nps 775750 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 134 seldepth 4 multipv 1 score mate 2 nodes 9380 nps 781666 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 135 seldepth 4 multipv 1 score mate 2 nodes 9451 nps 787583 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 136 seldepth 4 multipv 1 score mate 2 nodes 9522 nps 793500 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 137 seldepth 4 multipv 1 score mate 2 nodes 9591 nps 799250 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 138 seldepth 4 multipv 1 score mate 2 nodes 9662 nps 805166 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 139 seldepth 4 multipv 1 score mate 2 nodes 9733 nps 811083 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 140 seldepth 4 multipv 1 score mate 2 nodes 9802 nps 816833 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 141 seldepth 4 multipv 1 score mate 2 nodes 9873 nps 822750 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 142 seldepth 4 multipv 1 score mate 2 nodes 9944 nps 828666 tbhits 0 time 12 pv d4c2 e1f2 g3f2
info depth 143 seldepth 4 multipv 1 score mate 2 nodes 10013 nps 770230 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 144 seldepth 4 multipv 1 score mate 2 nodes 10084 nps 775692 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 145 seldepth 4 multipv 1 score mate 2 nodes 10155 nps 781153 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 146 seldepth 4 multipv 1 score mate 2 nodes 10224 nps 786461 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 147 seldepth 4 multipv 1 score mate 2 nodes 10295 nps 791923 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 148 seldepth 4 multipv 1 score mate 2 nodes 10366 nps 797384 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 149 seldepth 4 multipv 1 score mate 2 nodes 10435 nps 802692 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 150 seldepth 4 multipv 1 score mate 2 nodes 10506 nps 808153 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 151 seldepth 4 multipv 1 score mate 2 nodes 10577 nps 813615 tbhits 0 time 13 pv d4c2 e1f2 g3f2
info depth 152 seldepth 4 multipv 1 score mate 2 nodes 10646 nps 760428 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 153 seldepth 4 multipv 1 score mate 2 nodes 10717 nps 765500 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 154 seldepth 4 multipv 1 score mate 2 nodes 10788 nps 770571 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 155 seldepth 4 multipv 1 score mate 2 nodes 10859 nps 775642 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 156 seldepth 4 multipv 1 score mate 2 nodes 10928 nps 780571 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 157 seldepth 4 multipv 1 score mate 2 nodes 10999 nps 785642 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 158 seldepth 4 multipv 1 score mate 2 nodes 11070 nps 790714 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 159 seldepth 4 multipv 1 score mate 2 nodes 11139 nps 795642 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 160 seldepth 4 multipv 1 score mate 2 nodes 11210 nps 800714 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 161 seldepth 4 multipv 1 score mate 2 nodes 11281 nps 805785 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 162 seldepth 4 multipv 1 score mate 2 nodes 11350 nps 810714 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 163 seldepth 4 multipv 1 score mate 2 nodes 11421 nps 815785 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 164 seldepth 4 multipv 1 score mate 2 nodes 11492 nps 820857 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 165 seldepth 4 multipv 1 score mate 2 nodes 11561 nps 825785 tbhits 0 time 14 pv d4c2 e1f2 g3f2
info depth 166 seldepth 4 multipv 1 score mate 2 nodes 11632 nps 775466 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 167 seldepth 4 multipv 1 score mate 2 nodes 11703 nps 780200 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 168 seldepth 4 multipv 1 score mate 2 nodes 11772 nps 784800 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 169 seldepth 4 multipv 1 score mate 2 nodes 11843 nps 789533 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 170 seldepth 4 multipv 1 score mate 2 nodes 11914 nps 794266 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 171 seldepth 4 multipv 1 score mate 2 nodes 11983 nps 798866 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 172 seldepth 4 multipv 1 score mate 2 nodes 12054 nps 803600 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 173 seldepth 4 multipv 1 score mate 2 nodes 12125 nps 808333 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 174 seldepth 4 multipv 1 score mate 2 nodes 12196 nps 813066 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 175 seldepth 4 multipv 1 score mate 2 nodes 12265 nps 817666 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 176 seldepth 4 multipv 1 score mate 2 nodes 12336 nps 822400 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 177 seldepth 4 multipv 1 score mate 2 nodes 12407 nps 827133 tbhits 0 time 15 pv d4c2 e1f2 g3f2
info depth 178 seldepth 4 multipv 1 score mate 2 nodes 12476 nps 779750 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 179 seldepth 4 multipv 1 score mate 2 nodes 12547 nps 784187 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 180 seldepth 4 multipv 1 score mate 2 nodes 12618 nps 788625 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 181 seldepth 4 multipv 1 score mate 2 nodes 12687 nps 792937 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 182 seldepth 4 multipv 1 score mate 2 nodes 12758 nps 797375 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 183 seldepth 4 multipv 1 score mate 2 nodes 12829 nps 801812 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 184 seldepth 4 multipv 1 score mate 2 nodes 12898 nps 806125 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 185 seldepth 4 multipv 1 score mate 2 nodes 12969 nps 810562 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 186 seldepth 4 multipv 1 score mate 2 nodes 13040 nps 815000 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 187 seldepth 4 multipv 1 score mate 2 nodes 13109 nps 819312 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 188 seldepth 4 multipv 1 score mate 2 nodes 13180 nps 823750 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 189 seldepth 4 multipv 1 score mate 2 nodes 13251 nps 828187 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 190 seldepth 4 multipv 1 score mate 2 nodes 13320 nps 832500 tbhits 0 time 16 pv d4c2 e1f2 g3f2
info depth 191 seldepth 4 multipv 1 score mate 2 nodes 13391 nps 787705 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 192 seldepth 4 multipv 1 score mate 2 nodes 13462 nps 791882 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 193 seldepth 4 multipv 1 score mate 2 nodes 13531 nps 795941 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 194 seldepth 4 multipv 1 score mate 2 nodes 13602 nps 800117 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 195 seldepth 4 multipv 1 score mate 2 nodes 13673 nps 804294 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 196 seldepth 4 multipv 1 score mate 2 nodes 13744 nps 808470 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 197 seldepth 4 multipv 1 score mate 2 nodes 13813 nps 812529 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 198 seldepth 4 multipv 1 score mate 2 nodes 13884 nps 816705 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 199 seldepth 4 multipv 1 score mate 2 nodes 13955 nps 820882 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 200 seldepth 4 multipv 1 score mate 2 nodes 14024 nps 824941 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 201 seldepth 4 multipv 1 score mate 2 nodes 14095 nps 829117 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 202 seldepth 4 multipv 1 score mate 2 nodes 14166 nps 833294 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 203 seldepth 4 multipv 1 score mate 2 nodes 14235 nps 837352 tbhits 0 time 17 pv d4c2 e1f2 g3f2
info depth 204 seldepth 4 multipv 1 score mate 2 nodes 14306 nps 794777 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 205 seldepth 4 multipv 1 score mate 2 nodes 14377 nps 798722 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 206 seldepth 4 multipv 1 score mate 2 nodes 14446 nps 802555 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 207 seldepth 4 multipv 1 score mate 2 nodes 14517 nps 806500 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 208 seldepth 4 multipv 1 score mate 2 nodes 14588 nps 810444 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 209 seldepth 4 multipv 1 score mate 2 nodes 14657 nps 814277 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 210 seldepth 4 multipv 1 score mate 2 nodes 14728 nps 818222 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 211 seldepth 4 multipv 1 score mate 2 nodes 14799 nps 822166 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 212 seldepth 4 multipv 1 score mate 2 nodes 14868 nps 826000 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 213 seldepth 4 multipv 1 score mate 2 nodes 14939 nps 829944 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 214 seldepth 4 multipv 1 score mate 2 nodes 15010 nps 833888 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 215 seldepth 4 multipv 1 score mate 2 nodes 15081 nps 837833 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 216 seldepth 4 multipv 1 score mate 2 nodes 15150 nps 841666 tbhits 0 time 18 pv d4c2 e1f2 g3f2
info depth 217 seldepth 4 multipv 1 score mate 2 nodes 15221 nps 801105 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 218 seldepth 4 multipv 1 score mate 2 nodes 15292 nps 804842 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 219 seldepth 4 multipv 1 score mate 2 nodes 15361 nps 808473 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 220 seldepth 4 multipv 1 score mate 2 nodes 15432 nps 812210 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 221 seldepth 4 multipv 1 score mate 2 nodes 15503 nps 815947 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 222 seldepth 4 multipv 1 score mate 2 nodes 15572 nps 819578 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 223 seldepth 4 multipv 1 score mate 2 nodes 15643 nps 823315 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 224 seldepth 4 multipv 1 score mate 2 nodes 15714 nps 827052 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 225 seldepth 4 multipv 1 score mate 2 nodes 15783 nps 830684 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 226 seldepth 4 multipv 1 score mate 2 nodes 15854 nps 834421 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 227 seldepth 4 multipv 1 score mate 2 nodes 15925 nps 838157 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 228 seldepth 4 multipv 1 score mate 2 nodes 15994 nps 841789 tbhits 0 time 19 pv d4c2 e1f2 g3f2
info depth 229 seldepth 4 multipv 1 score mate 2 nodes 16065 nps 803250 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 230 seldepth 4 multipv 1 score mate 2 nodes 16136 nps 806800 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 231 seldepth 4 multipv 1 score mate 2 nodes 16205 nps 810250 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 232 seldepth 4 multipv 1 score mate 2 nodes 16276 nps 813800 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 233 seldepth 4 multipv 1 score mate 2 nodes 16347 nps 817350 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 234 seldepth 4 multipv 1 score mate 2 nodes 16418 nps 820900 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 235 seldepth 4 multipv 1 score mate 2 nodes 16487 nps 824350 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 236 seldepth 4 multipv 1 score mate 2 nodes 16558 nps 827900 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 237 seldepth 4 multipv 1 score mate 2 nodes 16629 nps 831450 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 238 seldepth 4 multipv 1 score mate 2 nodes 16698 nps 834900 tbhits 0 time 20 pv d4c2 e1f2 g3f2
info depth 239 seldepth 4 multipv 1 score mate 2 nodes 16769 nps 798523 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 240 seldepth 4 multipv 1 score mate 2 nodes 16840 nps 801904 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 241 seldepth 4 multipv 1 score mate 2 nodes 16913 nps 805380 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 242 seldepth 4 multipv 1 score mate 2 nodes 16993 nps 809190 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 243 seldepth 4 multipv 1 score mate 2 nodes 17076 nps 813142 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 244 seldepth 4 multipv 1 score mate 2 nodes 17165 nps 817380 tbhits 0 time 21 pv d4c2 e1f2 g3f2
info depth 245 seldepth 4 multipv 1 score mate 2 nodes 17285 nps 823095 tbhits 0 time 21 pv d4c2 e1f2 g3f2
```

candidateMoves: `['d4c2']`; events: `[]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'c8', 'to': 'b7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'c7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'd7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'b8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'd8', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 85: {'type': 'MOVE', 'from': 'b6', 'to': 'c7', 'moveKind': 'NORMAL', 'promotion': None}

До (17 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'bp', '0'), ('a6', 'bp', '1'), ('b6', 'wp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('c2', 'bn', '1'), ('e1', 'wk', '1'), ('g1', 'wn', '1')]`; ход `WHITE`, долг `None`, полуходы `1`, повторы `85`.

После (16 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'wp', '1'), ('a6', 'bp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('c2', 'bn', '1'), ('e1', 'wk', '1'), ('g1', 'wn', '1')]`; ход `BLACK`, долг `None`, короли под боем `[{'king': 'e1', 'kingId': 'wk1', 'attacker': 'c2', 'attackerId': 'bn1'}]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score mate -1 nodes 21 nps 21000 tbhits 0 time 1 pv b6c7
info depth 2 seldepth 2 multipv 1 score mate -1 nodes 41 nps 41000 tbhits 0 time 1 pv b6c7 c2e1
info depth 3 seldepth 3 multipv 1 score mate -1 nodes 61 nps 61000 tbhits 0 time 1 pv b6c7 c2e1
info depth 4 seldepth 3 multipv 1 score mate -1 nodes 81 nps 81000 tbhits 0 time 1 pv b6c7 c2e1
info depth 5 seldepth 3 multipv 1 score mate -1 nodes 101 nps 101000 tbhits 0 time 1 pv b6c7 c2e1
info depth 6 seldepth 3 multipv 1 score mate -1 nodes 121 nps 121000 tbhits 0 time 1 pv b6c7 c2e1
info depth 7 seldepth 3 multipv 1 score mate -1 nodes 141 nps 141000 tbhits 0 time 1 pv b6c7 c2e1
info depth 8 seldepth 3 multipv 1 score mate -1 nodes 161 nps 161000 tbhits 0 time 1 pv b6c7 c2e1
info depth 9 seldepth 3 multipv 1 score mate -1 nodes 181 nps 181000 tbhits 0 time 1 pv b6c7 c2e1
info depth 10 seldepth 3 multipv 1 score mate -1 nodes 201 nps 100500 tbhits 0 time 2 pv b6c7 c2e1
info depth 11 seldepth 3 multipv 1 score mate -1 nodes 221 nps 110500 tbhits 0 time 2 pv b6c7 c2e1
info depth 12 seldepth 3 multipv 1 score mate -1 nodes 241 nps 120500 tbhits 0 time 2 pv b6c7 c2e1
info depth 13 seldepth 3 multipv 1 score mate -1 nodes 261 nps 130500 tbhits 0 time 2 pv b6c7 c2e1
info depth 14 seldepth 3 multipv 1 score mate -1 nodes 281 nps 140500 tbhits 0 time 2 pv b6c7 c2e1
info depth 15 seldepth 3 multipv 1 score mate -1 nodes 301 nps 150500 tbhits 0 time 2 pv b6c7 c2e1
info depth 16 seldepth 3 multipv 1 score mate -1 nodes 321 nps 160500 tbhits 0 time 2 pv b6c7 c2e1
info depth 17 seldepth 3 multipv 1 score mate -1 nodes 341 nps 170500 tbhits 0 time 2 pv b6c7 c2e1
info depth 18 seldepth 3 multipv 1 score mate -1 nodes 361 nps 180500 tbhits 0 time 2 pv b6c7 c2e1
info depth 19 seldepth 3 multipv 1 score mate -1 nodes 381 nps 190500 tbhits 0 time 2 pv b6c7 c2e1
info depth 20 seldepth 3 multipv 1 score mate -1 nodes 401 nps 200500 tbhits 0 time 2 pv b6c7 c2e1
info depth 21 seldepth 3 multipv 1 score mate -1 nodes 421 nps 210500 tbhits 0 time 2 pv b6c7 c2e1
info depth 22 seldepth 3 multipv 1 score mate -1 nodes 441 nps 220500 tbhits 0 time 2 pv b6c7 c2e1
info depth 23 seldepth 3 multipv 1 score mate -1 nodes 461 nps 230500 tbhits 0 time 2 pv b6c7 c2e1
info depth 24 seldepth 3 multipv 1 score mate -1 nodes 481 nps 240500 tbhits 0 time 2 pv b6c7 c2e1
info depth 25 seldepth 3 multipv 1 score mate -1 nodes 501 nps 250500 tbhits 0 time 2 pv b6c7 c2e1
info depth 26 seldepth 3 multipv 1 score mate -1 nodes 521 nps 260500 tbhits 0 time 2 pv b6c7 c2e1
info depth 27 seldepth 3 multipv 1 score mate -1 nodes 541 nps 270500 tbhits 0 time 2 pv b6c7 c2e1
info depth 28 seldepth 3 multipv 1 score mate -1 nodes 561 nps 280500 tbhits 0 time 2 pv b6c7 c2e1
info depth 29 seldepth 3 multipv 1 score mate -1 nodes 581 nps 290500 tbhits 0 time 2 pv b6c7 c2e1
info depth 30 seldepth 3 multipv 1 score mate -1 nodes 601 nps 300500 tbhits 0 time 2 pv b6c7 c2e1
info depth 31 seldepth 3 multipv 1 score mate -1 nodes 621 nps 310500 tbhits 0 time 2 pv b6c7 c2e1
info depth 32 seldepth 3 multipv 1 score mate -1 nodes 641 nps 320500 tbhits 0 time 2 pv b6c7 c2e1
info depth 33 seldepth 3 multipv 1 score mate -1 nodes 661 nps 330500 tbhits 0 time 2 pv b6c7 c2e1
info depth 34 seldepth 3 multipv 1 score mate -1 nodes 681 nps 340500 tbhits 0 time 2 pv b6c7 c2e1
info depth 35 seldepth 3 multipv 1 score mate -1 nodes 701 nps 350500 tbhits 0 time 2 pv b6c7 c2e1
info depth 36 seldepth 3 multipv 1 score mate -1 nodes 721 nps 360500 tbhits 0 time 2 pv b6c7 c2e1
info depth 37 seldepth 3 multipv 1 score mate -1 nodes 741 nps 370500 tbhits 0 time 2 pv b6c7 c2e1
info depth 38 seldepth 3 multipv 1 score mate -1 nodes 761 nps 380500 tbhits 0 time 2 pv b6c7 c2e1
info depth 39 seldepth 3 multipv 1 score mate -1 nodes 781 nps 390500 tbhits 0 time 2 pv b6c7 c2e1
info depth 40 seldepth 3 multipv 1 score mate -1 nodes 801 nps 400500 tbhits 0 time 2 pv b6c7 c2e1
info depth 41 seldepth 3 multipv 1 score mate -1 nodes 821 nps 410500 tbhits 0 time 2 pv b6c7 c2e1
info depth 42 seldepth 3 multipv 1 score mate -1 nodes 841 nps 420500 tbhits 0 time 2 pv b6c7 c2e1
info depth 43 seldepth 3 multipv 1 score mate -1 nodes 861 nps 430500 tbhits 0 time 2 pv b6c7 c2e1
info depth 44 seldepth 3 multipv 1 score mate -1 nodes 881 nps 440500 tbhits 0 time 2 pv b6c7 c2e1
info depth 45 seldepth 3 multipv 1 score mate -1 nodes 901 nps 450500 tbhits 0 time 2 pv b6c7 c2e1
info depth 46 seldepth 3 multipv 1 score mate -1 nodes 921 nps 460500 tbhits 0 time 2 pv b6c7 c2e1
info depth 47 seldepth 3 multipv 1 score mate -1 nodes 941 nps 470500 tbhits 0 time 2 pv b6c7 c2e1
info depth 48 seldepth 3 multipv 1 score mate -1 nodes 961 nps 480500 tbhits 0 time 2 pv b6c7 c2e1
info depth 49 seldepth 3 multipv 1 score mate -1 nodes 981 nps 490500 tbhits 0 time 2 pv b6c7 c2e1
info depth 50 seldepth 3 multipv 1 score mate -1 nodes 1001 nps 500500 tbhits 0 time 2 pv b6c7 c2e1
info depth 51 seldepth 3 multipv 1 score mate -1 nodes 1021 nps 510500 tbhits 0 time 2 pv b6c7 c2e1
info depth 52 seldepth 3 multipv 1 score mate -1 nodes 1041 nps 520500 tbhits 0 time 2 pv b6c7 c2e1
info depth 53 seldepth 3 multipv 1 score mate -1 nodes 1061 nps 530500 tbhits 0 time 2 pv b6c7 c2e1
info depth 54 seldepth 3 multipv 1 score mate -1 nodes 1081 nps 540500 tbhits 0 time 2 pv b6c7 c2e1
info depth 55 seldepth 3 multipv 1 score mate -1 nodes 1101 nps 550500 tbhits 0 time 2 pv b6c7 c2e1
info depth 56 seldepth 3 multipv 1 score mate -1 nodes 1121 nps 373666 tbhits 0 time 3 pv b6c7 c2e1
info depth 57 seldepth 3 multipv 1 score mate -1 nodes 1141 nps 380333 tbhits 0 time 3 pv b6c7 c2e1
info depth 58 seldepth 3 multipv 1 score mate -1 nodes 1161 nps 387000 tbhits 0 time 3 pv b6c7 c2e1
info depth 59 seldepth 3 multipv 1 score mate -1 nodes 1181 nps 393666 tbhits 0 time 3 pv b6c7 c2e1
info depth 60 seldepth 3 multipv 1 score mate -1 nodes 1201 nps 400333 tbhits 0 time 3 pv b6c7 c2e1
info depth 61 seldepth 3 multipv 1 score mate -1 nodes 1221 nps 407000 tbhits 0 time 3 pv b6c7 c2e1
info depth 62 seldepth 3 multipv 1 score mate -1 nodes 1241 nps 413666 tbhits 0 time 3 pv b6c7 c2e1
info depth 63 seldepth 3 multipv 1 score mate -1 nodes 1261 nps 420333 tbhits 0 time 3 pv b6c7 c2e1
info depth 64 seldepth 3 multipv 1 score mate -1 nodes 1281 nps 427000 tbhits 0 time 3 pv b6c7 c2e1
info depth 65 seldepth 3 multipv 1 score mate -1 nodes 1301 nps 433666 tbhits 0 time 3 pv b6c7 c2e1
info depth 66 seldepth 3 multipv 1 score mate -1 nodes 1321 nps 440333 tbhits 0 time 3 pv b6c7 c2e1
info depth 67 seldepth 3 multipv 1 score mate -1 nodes 1341 nps 447000 tbhits 0 time 3 pv b6c7 c2e1
info depth 68 seldepth 3 multipv 1 score mate -1 nodes 1361 nps 453666 tbhits 0 time 3 pv b6c7 c2e1
info depth 69 seldepth 3 multipv 1 score mate -1 nodes 1381 nps 460333 tbhits 0 time 3 pv b6c7 c2e1
info depth 70 seldepth 3 multipv 1 score mate -1 nodes 1401 nps 467000 tbhits 0 time 3 pv b6c7 c2e1
info depth 71 seldepth 3 multipv 1 score mate -1 nodes 1421 nps 473666 tbhits 0 time 3 pv b6c7 c2e1
info depth 72 seldepth 3 multipv 1 score mate -1 nodes 1441 nps 480333 tbhits 0 time 3 pv b6c7 c2e1
info depth 73 seldepth 3 multipv 1 score mate -1 nodes 1461 nps 487000 tbhits 0 time 3 pv b6c7 c2e1
info depth 74 seldepth 3 multipv 1 score mate -1 nodes 1481 nps 493666 tbhits 0 time 3 pv b6c7 c2e1
info depth 75 seldepth 3 multipv 1 score mate -1 nodes 1501 nps 500333 tbhits 0 time 3 pv b6c7 c2e1
info depth 76 seldepth 3 multipv 1 score mate -1 nodes 1521 nps 507000 tbhits 0 time 3 pv b6c7 c2e1
info depth 77 seldepth 3 multipv 1 score mate -1 nodes 1541 nps 513666 tbhits 0 time 3 pv b6c7 c2e1
info depth 78 seldepth 3 multipv 1 score mate -1 nodes 1561 nps 520333 tbhits 0 time 3 pv b6c7 c2e1
info depth 79 seldepth 3 multipv 1 score mate -1 nodes 1581 nps 527000 tbhits 0 time 3 pv b6c7 c2e1
info depth 80 seldepth 3 multipv 1 score mate -1 nodes 1601 nps 533666 tbhits 0 time 3 pv b6c7 c2e1
info depth 81 seldepth 3 multipv 1 score mate -1 nodes 1621 nps 540333 tbhits 0 time 3 pv b6c7 c2e1
info depth 82 seldepth 3 multipv 1 score mate -1 nodes 1641 nps 547000 tbhits 0 time 3 pv b6c7 c2e1
info depth 83 seldepth 3 multipv 1 score mate -1 nodes 1661 nps 553666 tbhits 0 time 3 pv b6c7 c2e1
info depth 84 seldepth 3 multipv 1 score mate -1 nodes 1681 nps 560333 tbhits 0 time 3 pv b6c7 c2e1
info depth 85 seldepth 3 multipv 1 score mate -1 nodes 1701 nps 567000 tbhits 0 time 3 pv b6c7 c2e1
info depth 86 seldepth 3 multipv 1 score mate -1 nodes 1721 nps 573666 tbhits 0 time 3 pv b6c7 c2e1
info depth 87 seldepth 3 multipv 1 score mate -1 nodes 1741 nps 580333 tbhits 0 time 3 pv b6c7 c2e1
info depth 88 seldepth 3 multipv 1 score mate -1 nodes 1761 nps 587000 tbhits 0 time 3 pv b6c7 c2e1
info depth 89 seldepth 3 multipv 1 score mate -1 nodes 1781 nps 593666 tbhits 0 time 3 pv b6c7 c2e1
info depth 90 seldepth 3 multipv 1 score mate -1 nodes 1801 nps 600333 tbhits 0 time 3 pv b6c7 c2e1
info depth 91 seldepth 3 multipv 1 score mate -1 nodes 1821 nps 607000 tbhits 0 time 3 pv b6c7 c2e1
info depth 92 seldepth 3 multipv 1 score mate -1 nodes 1841 nps 613666 tbhits 0 time 3 pv b6c7 c2e1
info depth 93 seldepth 3 multipv 1 score mate -1 nodes 1861 nps 620333 tbhits 0 time 3 pv b6c7 c2e1
info depth 94 seldepth 3 multipv 1 score mate -1 nodes 1881 nps 627000 tbhits 0 time 3 pv b6c7 c2e1
info depth 95 seldepth 3 multipv 1 score mate -1 nodes 1901 nps 633666 tbhits 0 time 3 pv b6c7 c2e1
info depth 96 seldepth 3 multipv 1 score mate -1 nodes 1921 nps 640333 tbhits 0 time 3 pv b6c7 c2e1
info depth 97 seldepth 3 multipv 1 score mate -1 nodes 1941 nps 647000 tbhits 0 time 3 pv b6c7 c2e1
info depth 98 seldepth 3 multipv 1 score mate -1 nodes 1961 nps 653666 tbhits 0 time 3 pv b6c7 c2e1
info depth 99 seldepth 3 multipv 1 score mate -1 nodes 1981 nps 660333 tbhits 0 time 3 pv b6c7 c2e1
info depth 100 seldepth 3 multipv 1 score mate -1 nodes 2001 nps 667000 tbhits 0 time 3 pv b6c7 c2e1
info depth 101 seldepth 3 multipv 1 score mate -1 nodes 2021 nps 673666 tbhits 0 time 3 pv b6c7 c2e1
info depth 102 seldepth 3 multipv 1 score mate -1 nodes 2041 nps 510250 tbhits 0 time 4 pv b6c7 c2e1
info depth 103 seldepth 3 multipv 1 score mate -1 nodes 2061 nps 515250 tbhits 0 time 4 pv b6c7 c2e1
info depth 104 seldepth 3 multipv 1 score mate -1 nodes 2081 nps 520250 tbhits 0 time 4 pv b6c7 c2e1
info depth 105 seldepth 3 multipv 1 score mate -1 nodes 2101 nps 525250 tbhits 0 time 4 pv b6c7 c2e1
info depth 106 seldepth 3 multipv 1 score mate -1 nodes 2121 nps 530250 tbhits 0 time 4 pv b6c7 c2e1
info depth 107 seldepth 3 multipv 1 score mate -1 nodes 2141 nps 535250 tbhits 0 time 4 pv b6c7 c2e1
info depth 108 seldepth 3 multipv 1 score mate -1 nodes 2161 nps 540250 tbhits 0 time 4 pv b6c7 c2e1
info depth 109 seldepth 3 multipv 1 score mate -1 nodes 2181 nps 545250 tbhits 0 time 4 pv b6c7 c2e1
info depth 110 seldepth 3 multipv 1 score mate -1 nodes 2201 nps 550250 tbhits 0 time 4 pv b6c7 c2e1
info depth 111 seldepth 3 multipv 1 score mate -1 nodes 2221 nps 555250 tbhits 0 time 4 pv b6c7 c2e1
info depth 112 seldepth 3 multipv 1 score mate -1 nodes 2241 nps 560250 tbhits 0 time 4 pv b6c7 c2e1
info depth 113 seldepth 3 multipv 1 score mate -1 nodes 2261 nps 565250 tbhits 0 time 4 pv b6c7 c2e1
info depth 114 seldepth 3 multipv 1 score mate -1 nodes 2281 nps 570250 tbhits 0 time 4 pv b6c7 c2e1
info depth 115 seldepth 3 multipv 1 score mate -1 nodes 2301 nps 575250 tbhits 0 time 4 pv b6c7 c2e1
info depth 116 seldepth 3 multipv 1 score mate -1 nodes 2321 nps 580250 tbhits 0 time 4 pv b6c7 c2e1
info depth 117 seldepth 3 multipv 1 score mate -1 nodes 2341 nps 585250 tbhits 0 time 4 pv b6c7 c2e1
info depth 118 seldepth 3 multipv 1 score mate -1 nodes 2361 nps 590250 tbhits 0 time 4 pv b6c7 c2e1
info depth 119 seldepth 3 multipv 1 score mate -1 nodes 2381 nps 595250 tbhits 0 time 4 pv b6c7 c2e1
info depth 120 seldepth 3 multipv 1 score mate -1 nodes 2401 nps 600250 tbhits 0 time 4 pv b6c7 c2e1
info depth 121 seldepth 3 multipv 1 score mate -1 nodes 2421 nps 605250 tbhits 0 time 4 pv b6c7 c2e1
info depth 122 seldepth 3 multipv 1 score mate -1 nodes 2441 nps 610250 tbhits 0 time 4 pv b6c7 c2e1
info depth 123 seldepth 3 multipv 1 score mate -1 nodes 2461 nps 615250 tbhits 0 time 4 pv b6c7 c2e1
info depth 124 seldepth 3 multipv 1 score mate -1 nodes 2481 nps 620250 tbhits 0 time 4 pv b6c7 c2e1
info depth 125 seldepth 3 multipv 1 score mate -1 nodes 2501 nps 625250 tbhits 0 time 4 pv b6c7 c2e1
info depth 126 seldepth 3 multipv 1 score mate -1 nodes 2521 nps 630250 tbhits 0 time 4 pv b6c7 c2e1
info depth 127 seldepth 3 multipv 1 score mate -1 nodes 2541 nps 635250 tbhits 0 time 4 pv b6c7 c2e1
info depth 128 seldepth 3 multipv 1 score mate -1 nodes 2561 nps 640250 tbhits 0 time 4 pv b6c7 c2e1
info depth 129 seldepth 3 multipv 1 score mate -1 nodes 2581 nps 645250 tbhits 0 time 4 pv b6c7 c2e1
info depth 130 seldepth 3 multipv 1 score mate -1 nodes 2601 nps 650250 tbhits 0 time 4 pv b6c7 c2e1
info depth 131 seldepth 3 multipv 1 score mate -1 nodes 2621 nps 655250 tbhits 0 time 4 pv b6c7 c2e1
info depth 132 seldepth 3 multipv 1 score mate -1 nodes 2641 nps 660250 tbhits 0 time 4 pv b6c7 c2e1
info depth 133 seldepth 3 multipv 1 score mate -1 nodes 2661 nps 665250 tbhits 0 time 4 pv b6c7 c2e1
info depth 134 seldepth 3 multipv 1 score mate -1 nodes 2681 nps 670250 tbhits 0 time 4 pv b6c7 c2e1
info depth 135 seldepth 3 multipv 1 score mate -1 nodes 2701 nps 675250 tbhits 0 time 4 pv b6c7 c2e1
info depth 136 seldepth 3 multipv 1 score mate -1 nodes 2721 nps 680250 tbhits 0 time 4 pv b6c7 c2e1
info depth 137 seldepth 3 multipv 1 score mate -1 nodes 2741 nps 685250 tbhits 0 time 4 pv b6c7 c2e1
info depth 138 seldepth 3 multipv 1 score mate -1 nodes 2761 nps 690250 tbhits 0 time 4 pv b6c7 c2e1
info depth 139 seldepth 3 multipv 1 score mate -1 nodes 2781 nps 695250 tbhits 0 time 4 pv b6c7 c2e1
info depth 140 seldepth 3 multipv 1 score mate -1 nodes 2801 nps 700250 tbhits 0 time 4 pv b6c7 c2e1
info depth 141 seldepth 3 multipv 1 score mate -1 nodes 2821 nps 705250 tbhits 0 time 4 pv b6c7 c2e1
info depth 142 seldepth 3 multipv 1 score mate -1 nodes 2841 nps 710250 tbhits 0 time 4 pv b6c7 c2e1
info depth 143 seldepth 3 multipv 1 score mate -1 nodes 2861 nps 715250 tbhits 0 time 4 pv b6c7 c2e1
info depth 144 seldepth 3 multipv 1 score mate -1 nodes 2881 nps 720250 tbhits 0 time 4 pv b6c7 c2e1
info depth 145 seldepth 3 multipv 1 score mate -1 nodes 2901 nps 725250 tbhits 0 time 4 pv b6c7 c2e1
info depth 146 seldepth 3 multipv 1 score mate -1 nodes 2921 nps 730250 tbhits 0 time 4 pv b6c7 c2e1
info depth 147 seldepth 3 multipv 1 score mate -1 nodes 2941 nps 735250 tbhits 0 time 4 pv b6c7 c2e1
info depth 148 seldepth 3 multipv 1 score mate -1 nodes 2961 nps 592200 tbhits 0 time 5 pv b6c7 c2e1
info depth 149 seldepth 3 multipv 1 score mate -1 nodes 2981 nps 596200 tbhits 0 time 5 pv b6c7 c2e1
info depth 150 seldepth 3 multipv 1 score mate -1 nodes 3001 nps 600200 tbhits 0 time 5 pv b6c7 c2e1
info depth 151 seldepth 3 multipv 1 score mate -1 nodes 3021 nps 604200 tbhits 0 time 5 pv b6c7 c2e1
info depth 152 seldepth 3 multipv 1 score mate -1 nodes 3041 nps 608200 tbhits 0 time 5 pv b6c7 c2e1
info depth 153 seldepth 3 multipv 1 score mate -1 nodes 3061 nps 612200 tbhits 0 time 5 pv b6c7 c2e1
info depth 154 seldepth 3 multipv 1 score mate -1 nodes 3081 nps 616200 tbhits 0 time 5 pv b6c7 c2e1
info depth 155 seldepth 3 multipv 1 score mate -1 nodes 3101 nps 620200 tbhits 0 time 5 pv b6c7 c2e1
info depth 156 seldepth 3 multipv 1 score mate -1 nodes 3121 nps 624200 tbhits 0 time 5 pv b6c7 c2e1
info depth 157 seldepth 3 multipv 1 score mate -1 nodes 3141 nps 628200 tbhits 0 time 5 pv b6c7 c2e1
info depth 158 seldepth 3 multipv 1 score mate -1 nodes 3161 nps 632200 tbhits 0 time 5 pv b6c7 c2e1
info depth 159 seldepth 3 multipv 1 score mate -1 nodes 3181 nps 636200 tbhits 0 time 5 pv b6c7 c2e1
info depth 160 seldepth 3 multipv 1 score mate -1 nodes 3201 nps 640200 tbhits 0 time 5 pv b6c7 c2e1
info depth 161 seldepth 3 multipv 1 score mate -1 nodes 3221 nps 644200 tbhits 0 time 5 pv b6c7 c2e1
info depth 162 seldepth 3 multipv 1 score mate -1 nodes 3241 nps 648200 tbhits 0 time 5 pv b6c7 c2e1
info depth 163 seldepth 3 multipv 1 score mate -1 nodes 3261 nps 652200 tbhits 0 time 5 pv b6c7 c2e1
info depth 164 seldepth 3 multipv 1 score mate -1 nodes 3281 nps 656200 tbhits 0 time 5 pv b6c7 c2e1
info depth 165 seldepth 3 multipv 1 score mate -1 nodes 3301 nps 660200 tbhits 0 time 5 pv b6c7 c2e1
info depth 166 seldepth 3 multipv 1 score mate -1 nodes 3321 nps 664200 tbhits 0 time 5 pv b6c7 c2e1
info depth 167 seldepth 3 multipv 1 score mate -1 nodes 3341 nps 668200 tbhits 0 time 5 pv b6c7 c2e1
info depth 168 seldepth 3 multipv 1 score mate -1 nodes 3361 nps 672200 tbhits 0 time 5 pv b6c7 c2e1
info depth 169 seldepth 3 multipv 1 score mate -1 nodes 3381 nps 676200 tbhits 0 time 5 pv b6c7 c2e1
info depth 170 seldepth 3 multipv 1 score mate -1 nodes 3401 nps 680200 tbhits 0 time 5 pv b6c7 c2e1
info depth 171 seldepth 3 multipv 1 score mate -1 nodes 3421 nps 684200 tbhits 0 time 5 pv b6c7 c2e1
info depth 172 seldepth 3 multipv 1 score mate -1 nodes 3441 nps 688200 tbhits 0 time 5 pv b6c7 c2e1
info depth 173 seldepth 3 multipv 1 score mate -1 nodes 3461 nps 692200 tbhits 0 time 5 pv b6c7 c2e1
info depth 174 seldepth 3 multipv 1 score mate -1 nodes 3481 nps 696200 tbhits 0 time 5 pv b6c7 c2e1
info depth 175 seldepth 3 multipv 1 score mate -1 nodes 3501 nps 700200 tbhits 0 time 5 pv b6c7 c2e1
info depth 176 seldepth 3 multipv 1 score mate -1 nodes 3521 nps 704200 tbhits 0 time 5 pv b6c7 c2e1
info depth 177 seldepth 3 multipv 1 score mate -1 nodes 3541 nps 708200 tbhits 0 time 5 pv b6c7 c2e1
info depth 178 seldepth 3 multipv 1 score mate -1 nodes 3561 nps 712200 tbhits 0 time 5 pv b6c7 c2e1
info depth 179 seldepth 3 multipv 1 score mate -1 nodes 3581 nps 716200 tbhits 0 time 5 pv b6c7 c2e1
info depth 180 seldepth 3 multipv 1 score mate -1 nodes 3601 nps 720200 tbhits 0 time 5 pv b6c7 c2e1
info depth 181 seldepth 3 multipv 1 score mate -1 nodes 3621 nps 724200 tbhits 0 time 5 pv b6c7 c2e1
info depth 182 seldepth 3 multipv 1 score mate -1 nodes 3641 nps 728200 tbhits 0 time 5 pv b6c7 c2e1
info depth 183 seldepth 3 multipv 1 score mate -1 nodes 3661 nps 732200 tbhits 0 time 5 pv b6c7 c2e1
info depth 184 seldepth 3 multipv 1 score mate -1 nodes 3681 nps 736200 tbhits 0 time 5 pv b6c7 c2e1
info depth 185 seldepth 3 multipv 1 score mate -1 nodes 3701 nps 740200 tbhits 0 time 5 pv b6c7 c2e1
info depth 186 seldepth 3 multipv 1 score mate -1 nodes 3721 nps 744200 tbhits 0 time 5 pv b6c7 c2e1
info depth 187 seldepth 3 multipv 1 score mate -1 nodes 3741 nps 748200 tbhits 0 time 5 pv b6c7 c2e1
info depth 188 seldepth 3 multipv 1 score mate -1 nodes 3761 nps 752200 tbhits 0 time 5 pv b6c7 c2e1
info depth 189 seldepth 3 multipv 1 score mate -1 nodes 3781 nps 756200 tbhits 0 time 5 pv b6c7 c2e1
info depth 190 seldepth 3 multipv 1 score mate -1 nodes 3801 nps 760200 tbhits 0 time 5 pv b6c7 c2e1
info depth 191 seldepth 3 multipv 1 score mate -1 nodes 3821 nps 764200 tbhits 0 time 5 pv b6c7 c2e1
info depth 192 seldepth 3 multipv 1 score mate -1 nodes 3841 nps 768200 tbhits 0 time 5 pv b6c7 c2e1
info depth 193 seldepth 3 multipv 1 score mate -1 nodes 3861 nps 772200 tbhits 0 time 5 pv b6c7 c2e1
info depth 194 seldepth 3 multipv 1 score mate -1 nodes 3881 nps 646833 tbhits 0 time 6 pv b6c7 c2e1
info depth 195 seldepth 3 multipv 1 score mate -1 nodes 3901 nps 650166 tbhits 0 time 6 pv b6c7 c2e1
info depth 196 seldepth 3 multipv 1 score mate -1 nodes 3921 nps 653500 tbhits 0 time 6 pv b6c7 c2e1
info depth 197 seldepth 3 multipv 1 score mate -1 nodes 3941 nps 656833 tbhits 0 time 6 pv b6c7 c2e1
info depth 198 seldepth 3 multipv 1 score mate -1 nodes 3961 nps 660166 tbhits 0 time 6 pv b6c7 c2e1
info depth 199 seldepth 3 multipv 1 score mate -1 nodes 3981 nps 663500 tbhits 0 time 6 pv b6c7 c2e1
info depth 200 seldepth 3 multipv 1 score mate -1 nodes 4001 nps 666833 tbhits 0 time 6 pv b6c7 c2e1
info depth 201 seldepth 3 multipv 1 score mate -1 nodes 4021 nps 670166 tbhits 0 time 6 pv b6c7 c2e1
info depth 202 seldepth 3 multipv 1 score mate -1 nodes 4041 nps 673500 tbhits 0 time 6 pv b6c7 c2e1
info depth 203 seldepth 3 multipv 1 score mate -1 nodes 4061 nps 676833 tbhits 0 time 6 pv b6c7 c2e1
info depth 204 seldepth 3 multipv 1 score mate -1 nodes 4081 nps 680166 tbhits 0 time 6 pv b6c7 c2e1
info depth 205 seldepth 3 multipv 1 score mate -1 nodes 4101 nps 683500 tbhits 0 time 6 pv b6c7 c2e1
info depth 206 seldepth 3 multipv 1 score mate -1 nodes 4121 nps 686833 tbhits 0 time 6 pv b6c7 c2e1
info depth 207 seldepth 3 multipv 1 score mate -1 nodes 4141 nps 690166 tbhits 0 time 6 pv b6c7 c2e1
info depth 208 seldepth 3 multipv 1 score mate -1 nodes 4161 nps 693500 tbhits 0 time 6 pv b6c7 c2e1
info depth 209 seldepth 3 multipv 1 score mate -1 nodes 4181 nps 696833 tbhits 0 time 6 pv b6c7 c2e1
info depth 210 seldepth 3 multipv 1 score mate -1 nodes 4201 nps 700166 tbhits 0 time 6 pv b6c7 c2e1
info depth 211 seldepth 3 multipv 1 score mate -1 nodes 4221 nps 703500 tbhits 0 time 6 pv b6c7 c2e1
info depth 212 seldepth 3 multipv 1 score mate -1 nodes 4241 nps 706833 tbhits 0 time 6 pv b6c7 c2e1
info depth 213 seldepth 3 multipv 1 score mate -1 nodes 4261 nps 710166 tbhits 0 time 6 pv b6c7 c2e1
info depth 214 seldepth 3 multipv 1 score mate -1 nodes 4281 nps 713500 tbhits 0 time 6 pv b6c7 c2e1
info depth 215 seldepth 3 multipv 1 score mate -1 nodes 4301 nps 716833 tbhits 0 time 6 pv b6c7 c2e1
info depth 216 seldepth 3 multipv 1 score mate -1 nodes 4321 nps 720166 tbhits 0 time 6 pv b6c7 c2e1
info depth 217 seldepth 3 multipv 1 score mate -1 nodes 4341 nps 723500 tbhits 0 time 6 pv b6c7 c2e1
info depth 218 seldepth 3 multipv 1 score mate -1 nodes 4361 nps 726833 tbhits 0 time 6 pv b6c7 c2e1
info depth 219 seldepth 3 multipv 1 score mate -1 nodes 4381 nps 730166 tbhits 0 time 6 pv b6c7 c2e1
info depth 220 seldepth 3 multipv 1 score mate -1 nodes 4401 nps 733500 tbhits 0 time 6 pv b6c7 c2e1
info depth 221 seldepth 3 multipv 1 score mate -1 nodes 4421 nps 736833 tbhits 0 time 6 pv b6c7 c2e1
info depth 222 seldepth 3 multipv 1 score mate -1 nodes 4441 nps 740166 tbhits 0 time 6 pv b6c7 c2e1
info depth 223 seldepth 3 multipv 1 score mate -1 nodes 4461 nps 743500 tbhits 0 time 6 pv b6c7 c2e1
info depth 224 seldepth 3 multipv 1 score mate -1 nodes 4481 nps 746833 tbhits 0 time 6 pv b6c7 c2e1
info depth 225 seldepth 3 multipv 1 score mate -1 nodes 4501 nps 750166 tbhits 0 time 6 pv b6c7 c2e1
info depth 226 seldepth 3 multipv 1 score mate -1 nodes 4521 nps 753500 tbhits 0 time 6 pv b6c7 c2e1
info depth 227 seldepth 3 multipv 1 score mate -1 nodes 4541 nps 756833 tbhits 0 time 6 pv b6c7 c2e1
info depth 228 seldepth 3 multipv 1 score mate -1 nodes 4561 nps 760166 tbhits 0 time 6 pv b6c7 c2e1
info depth 229 seldepth 3 multipv 1 score mate -1 nodes 4581 nps 763500 tbhits 0 time 6 pv b6c7 c2e1
info depth 230 seldepth 3 multipv 1 score mate -1 nodes 4601 nps 766833 tbhits 0 time 6 pv b6c7 c2e1
info depth 231 seldepth 3 multipv 1 score mate -1 nodes 4621 nps 770166 tbhits 0 time 6 pv b6c7 c2e1
info depth 232 seldepth 3 multipv 1 score mate -1 nodes 4641 nps 773500 tbhits 0 time 6 pv b6c7 c2e1
info depth 233 seldepth 3 multipv 1 score mate -1 nodes 4661 nps 776833 tbhits 0 time 6 pv b6c7 c2e1
info depth 234 seldepth 3 multipv 1 score mate -1 nodes 4681 nps 780166 tbhits 0 time 6 pv b6c7 c2e1
info depth 235 seldepth 3 multipv 1 score mate -1 nodes 4701 nps 783500 tbhits 0 time 6 pv b6c7 c2e1
info depth 236 seldepth 3 multipv 1 score mate -1 nodes 4721 nps 786833 tbhits 0 time 6 pv b6c7 c2e1
info depth 237 seldepth 3 multipv 1 score mate -1 nodes 4741 nps 790166 tbhits 0 time 6 pv b6c7 c2e1
info depth 238 seldepth 3 multipv 1 score mate -1 nodes 4761 nps 793500 tbhits 0 time 6 pv b6c7 c2e1
info depth 239 seldepth 3 multipv 1 score mate -1 nodes 4781 nps 796833 tbhits 0 time 6 pv b6c7 c2e1
info depth 240 seldepth 3 multipv 1 score mate -1 nodes 4801 nps 685857 tbhits 0 time 7 pv b6c7 c2e1
info depth 241 seldepth 3 multipv 1 score mate -1 nodes 4821 nps 688714 tbhits 0 time 7 pv b6c7 c2e1
info depth 242 seldepth 3 multipv 1 score mate -1 nodes 4841 nps 691571 tbhits 0 time 7 pv b6c7 c2e1
info depth 243 seldepth 3 multipv 1 score mate -1 nodes 4861 nps 694428 tbhits 0 time 7 pv b6c7 c2e1
info depth 244 seldepth 3 multipv 1 score mate -1 nodes 4882 nps 697428 tbhits 0 time 7 pv b6c7 c2e1
info depth 245 seldepth 3 multipv 1 score mate -1 nodes 4916 nps 702285 tbhits 0 time 7 pv b6c7 c2e1
```

candidateMoves: `['b6c7']`; events: `[{'type': 'KING_LEFT_UNDER_ATTACK', 'side': 'WHITE', 'ply': 85, 'kingSquaresBefore': ['e1'], 'kingSquaresAfter': ['e1'], 'createdDebt': False, 'endedByLastKingCapture': False}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'e1', 'to': 'd1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'f1', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'd2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'e2', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'e1', 'to': 'f2', 'moveKind': 'NORMAL', 'promotion': None}]`.

### ply 86: {'type': 'MOVE', 'from': 'c2', 'to': 'e1', 'moveKind': 'NORMAL', 'promotion': None}

До (16 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'wp', '1'), ('a6', 'bp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('c2', 'bn', '1'), ('e1', 'wk', '1'), ('g1', 'wn', '1')]`; ход `BLACK`, долг `None`, полуходы `0`, повторы `86`.

После (15 фигур): `[('c8', 'bk', '1'), ('e8', 'br', '1'), ('b7', 'bb', '1'), ('c7', 'wp', '1'), ('a6', 'bp', '1'), ('g6', 'bp', '1'), ('a5', 'wp', '1'), ('d5', 'bn', '1'), ('e5', 'wb', '1'), ('c4', 'bp', '1'), ('f4', 'bp', '1'), ('d3', 'bq', '1'), ('g3', 'bp', '1'), ('e1', 'bn', '1'), ('g1', 'wn', '1')]`; ход `WHITE`, долг `None`, короли под боем `[]`.

Записанные легальные взятия короля по долгу: `[]`.

Результат таблицы до: `None`; после: `None` (это отдельные позиции, а не перенос оценки назад).

Записанная информация движка:

```text
info depth 1 seldepth 1 multipv 1 score mate 1 nodes 43 nps 21500 tbhits 0 time 2 pv c2e1
info depth 2 seldepth 2 multipv 1 score mate 1 nodes 86 nps 43000 tbhits 0 time 2 pv c2e1
info depth 3 seldepth 2 multipv 1 score mate 1 nodes 129 nps 64500 tbhits 0 time 2 pv c2e1
info depth 4 seldepth 2 multipv 1 score mate 1 nodes 172 nps 86000 tbhits 0 time 2 pv c2e1
info depth 5 seldepth 2 multipv 1 score mate 1 nodes 215 nps 107500 tbhits 0 time 2 pv c2e1
info depth 6 seldepth 2 multipv 1 score mate 1 nodes 258 nps 129000 tbhits 0 time 2 pv c2e1
info depth 7 seldepth 2 multipv 1 score mate 1 nodes 301 nps 150500 tbhits 0 time 2 pv c2e1
info depth 8 seldepth 2 multipv 1 score mate 1 nodes 344 nps 172000 tbhits 0 time 2 pv c2e1
info depth 9 seldepth 2 multipv 1 score mate 1 nodes 387 nps 129000 tbhits 0 time 3 pv c2e1
info depth 10 seldepth 2 multipv 1 score mate 1 nodes 430 nps 143333 tbhits 0 time 3 pv c2e1
info depth 11 seldepth 2 multipv 1 score mate 1 nodes 473 nps 157666 tbhits 0 time 3 pv c2e1
info depth 12 seldepth 2 multipv 1 score mate 1 nodes 516 nps 172000 tbhits 0 time 3 pv c2e1
info depth 13 seldepth 2 multipv 1 score mate 1 nodes 559 nps 186333 tbhits 0 time 3 pv c2e1
info depth 14 seldepth 2 multipv 1 score mate 1 nodes 602 nps 200666 tbhits 0 time 3 pv c2e1
info depth 15 seldepth 2 multipv 1 score mate 1 nodes 645 nps 215000 tbhits 0 time 3 pv c2e1
info depth 16 seldepth 2 multipv 1 score mate 1 nodes 688 nps 229333 tbhits 0 time 3 pv c2e1
info depth 17 seldepth 2 multipv 1 score mate 1 nodes 731 nps 182750 tbhits 0 time 4 pv c2e1
info depth 18 seldepth 2 multipv 1 score mate 1 nodes 774 nps 193500 tbhits 0 time 4 pv c2e1
info depth 19 seldepth 2 multipv 1 score mate 1 nodes 817 nps 204250 tbhits 0 time 4 pv c2e1
info depth 20 seldepth 2 multipv 1 score mate 1 nodes 860 nps 215000 tbhits 0 time 4 pv c2e1
info depth 21 seldepth 2 multipv 1 score mate 1 nodes 903 nps 225750 tbhits 0 time 4 pv c2e1
info depth 22 seldepth 2 multipv 1 score mate 1 nodes 946 nps 236500 tbhits 0 time 4 pv c2e1
info depth 23 seldepth 2 multipv 1 score mate 1 nodes 989 nps 247250 tbhits 0 time 4 pv c2e1
info depth 24 seldepth 2 multipv 1 score mate 1 nodes 1032 nps 258000 tbhits 0 time 4 pv c2e1
info depth 25 seldepth 2 multipv 1 score mate 1 nodes 1075 nps 268750 tbhits 0 time 4 pv c2e1
info depth 26 seldepth 2 multipv 1 score mate 1 nodes 1118 nps 279500 tbhits 0 time 4 pv c2e1
info depth 27 seldepth 2 multipv 1 score mate 1 nodes 1161 nps 290250 tbhits 0 time 4 pv c2e1
info depth 28 seldepth 2 multipv 1 score mate 1 nodes 1204 nps 301000 tbhits 0 time 4 pv c2e1
info depth 29 seldepth 2 multipv 1 score mate 1 nodes 1247 nps 311750 tbhits 0 time 4 pv c2e1
info depth 30 seldepth 2 multipv 1 score mate 1 nodes 1290 nps 322500 tbhits 0 time 4 pv c2e1
info depth 31 seldepth 2 multipv 1 score mate 1 nodes 1333 nps 333250 tbhits 0 time 4 pv c2e1
info depth 32 seldepth 2 multipv 1 score mate 1 nodes 1376 nps 344000 tbhits 0 time 4 pv c2e1
info depth 33 seldepth 2 multipv 1 score mate 1 nodes 1419 nps 354750 tbhits 0 time 4 pv c2e1
info depth 34 seldepth 2 multipv 1 score mate 1 nodes 1462 nps 365500 tbhits 0 time 4 pv c2e1
info depth 35 seldepth 2 multipv 1 score mate 1 nodes 1505 nps 376250 tbhits 0 time 4 pv c2e1
info depth 36 seldepth 2 multipv 1 score mate 1 nodes 1548 nps 387000 tbhits 0 time 4 pv c2e1
info depth 37 seldepth 2 multipv 1 score mate 1 nodes 1591 nps 397750 tbhits 0 time 4 pv c2e1
info depth 38 seldepth 2 multipv 1 score mate 1 nodes 1634 nps 408500 tbhits 0 time 4 pv c2e1
info depth 39 seldepth 2 multipv 1 score mate 1 nodes 1677 nps 419250 tbhits 0 time 4 pv c2e1
info depth 40 seldepth 2 multipv 1 score mate 1 nodes 1720 nps 430000 tbhits 0 time 4 pv c2e1
info depth 41 seldepth 2 multipv 1 score mate 1 nodes 1763 nps 440750 tbhits 0 time 4 pv c2e1
info depth 42 seldepth 2 multipv 1 score mate 1 nodes 1806 nps 451500 tbhits 0 time 4 pv c2e1
info depth 43 seldepth 2 multipv 1 score mate 1 nodes 1849 nps 462250 tbhits 0 time 4 pv c2e1
info depth 44 seldepth 2 multipv 1 score mate 1 nodes 1892 nps 378400 tbhits 0 time 5 pv c2e1
info depth 45 seldepth 2 multipv 1 score mate 1 nodes 1935 nps 387000 tbhits 0 time 5 pv c2e1
info depth 46 seldepth 2 multipv 1 score mate 1 nodes 1978 nps 395600 tbhits 0 time 5 pv c2e1
info depth 47 seldepth 2 multipv 1 score mate 1 nodes 2021 nps 404200 tbhits 0 time 5 pv c2e1
info depth 48 seldepth 2 multipv 1 score mate 1 nodes 2064 nps 412800 tbhits 0 time 5 pv c2e1
info depth 49 seldepth 2 multipv 1 score mate 1 nodes 2107 nps 421400 tbhits 0 time 5 pv c2e1
info depth 50 seldepth 2 multipv 1 score mate 1 nodes 2150 nps 430000 tbhits 0 time 5 pv c2e1
info depth 51 seldepth 2 multipv 1 score mate 1 nodes 2193 nps 438600 tbhits 0 time 5 pv c2e1
info depth 52 seldepth 2 multipv 1 score mate 1 nodes 2236 nps 447200 tbhits 0 time 5 pv c2e1
info depth 53 seldepth 2 multipv 1 score mate 1 nodes 2279 nps 455800 tbhits 0 time 5 pv c2e1
info depth 54 seldepth 2 multipv 1 score mate 1 nodes 2322 nps 464400 tbhits 0 time 5 pv c2e1
info depth 55 seldepth 2 multipv 1 score mate 1 nodes 2365 nps 473000 tbhits 0 time 5 pv c2e1
info depth 56 seldepth 2 multipv 1 score mate 1 nodes 2408 nps 481600 tbhits 0 time 5 pv c2e1
info depth 57 seldepth 2 multipv 1 score mate 1 nodes 2451 nps 490200 tbhits 0 time 5 pv c2e1
info depth 58 seldepth 2 multipv 1 score mate 1 nodes 2494 nps 498800 tbhits 0 time 5 pv c2e1
info depth 59 seldepth 2 multipv 1 score mate 1 nodes 2537 nps 507400 tbhits 0 time 5 pv c2e1
info depth 60 seldepth 2 multipv 1 score mate 1 nodes 2580 nps 516000 tbhits 0 time 5 pv c2e1
info depth 61 seldepth 2 multipv 1 score mate 1 nodes 2623 nps 524600 tbhits 0 time 5 pv c2e1
info depth 62 seldepth 2 multipv 1 score mate 1 nodes 2666 nps 533200 tbhits 0 time 5 pv c2e1
info depth 63 seldepth 2 multipv 1 score mate 1 nodes 2709 nps 541800 tbhits 0 time 5 pv c2e1
info depth 64 seldepth 2 multipv 1 score mate 1 nodes 2752 nps 550400 tbhits 0 time 5 pv c2e1
info depth 65 seldepth 2 multipv 1 score mate 1 nodes 2795 nps 559000 tbhits 0 time 5 pv c2e1
info depth 66 seldepth 2 multipv 1 score mate 1 nodes 2838 nps 567600 tbhits 0 time 5 pv c2e1
info depth 67 seldepth 2 multipv 1 score mate 1 nodes 2881 nps 576200 tbhits 0 time 5 pv c2e1
info depth 68 seldepth 2 multipv 1 score mate 1 nodes 2924 nps 584800 tbhits 0 time 5 pv c2e1
info depth 69 seldepth 2 multipv 1 score mate 1 nodes 2967 nps 593400 tbhits 0 time 5 pv c2e1
info depth 70 seldepth 2 multipv 1 score mate 1 nodes 3010 nps 501666 tbhits 0 time 6 pv c2e1
info depth 71 seldepth 2 multipv 1 score mate 1 nodes 3053 nps 508833 tbhits 0 time 6 pv c2e1
info depth 72 seldepth 2 multipv 1 score mate 1 nodes 3096 nps 516000 tbhits 0 time 6 pv c2e1
info depth 73 seldepth 2 multipv 1 score mate 1 nodes 3139 nps 523166 tbhits 0 time 6 pv c2e1
info depth 74 seldepth 2 multipv 1 score mate 1 nodes 3182 nps 530333 tbhits 0 time 6 pv c2e1
info depth 75 seldepth 2 multipv 1 score mate 1 nodes 3225 nps 537500 tbhits 0 time 6 pv c2e1
info depth 76 seldepth 2 multipv 1 score mate 1 nodes 3268 nps 544666 tbhits 0 time 6 pv c2e1
info depth 77 seldepth 2 multipv 1 score mate 1 nodes 3311 nps 551833 tbhits 0 time 6 pv c2e1
info depth 78 seldepth 2 multipv 1 score mate 1 nodes 3354 nps 559000 tbhits 0 time 6 pv c2e1
info depth 79 seldepth 2 multipv 1 score mate 1 nodes 3397 nps 566166 tbhits 0 time 6 pv c2e1
info depth 80 seldepth 2 multipv 1 score mate 1 nodes 3440 nps 573333 tbhits 0 time 6 pv c2e1
info depth 81 seldepth 2 multipv 1 score mate 1 nodes 3483 nps 580500 tbhits 0 time 6 pv c2e1
info depth 82 seldepth 2 multipv 1 score mate 1 nodes 3526 nps 587666 tbhits 0 time 6 pv c2e1
info depth 83 seldepth 2 multipv 1 score mate 1 nodes 3569 nps 594833 tbhits 0 time 6 pv c2e1
info depth 84 seldepth 2 multipv 1 score mate 1 nodes 3612 nps 602000 tbhits 0 time 6 pv c2e1
info depth 85 seldepth 2 multipv 1 score mate 1 nodes 3655 nps 609166 tbhits 0 time 6 pv c2e1
info depth 86 seldepth 2 multipv 1 score mate 1 nodes 3698 nps 616333 tbhits 0 time 6 pv c2e1
info depth 87 seldepth 2 multipv 1 score mate 1 nodes 3741 nps 623500 tbhits 0 time 6 pv c2e1
info depth 88 seldepth 2 multipv 1 score mate 1 nodes 3784 nps 630666 tbhits 0 time 6 pv c2e1
info depth 89 seldepth 2 multipv 1 score mate 1 nodes 3827 nps 637833 tbhits 0 time 6 pv c2e1
info depth 90 seldepth 2 multipv 1 score mate 1 nodes 3870 nps 645000 tbhits 0 time 6 pv c2e1
info depth 91 seldepth 2 multipv 1 score mate 1 nodes 3913 nps 652166 tbhits 0 time 6 pv c2e1
info depth 92 seldepth 2 multipv 1 score mate 1 nodes 3956 nps 659333 tbhits 0 time 6 pv c2e1
info depth 93 seldepth 2 multipv 1 score mate 1 nodes 3999 nps 666500 tbhits 0 time 6 pv c2e1
info depth 94 seldepth 2 multipv 1 score mate 1 nodes 4042 nps 673666 tbhits 0 time 6 pv c2e1
info depth 95 seldepth 2 multipv 1 score mate 1 nodes 4085 nps 680833 tbhits 0 time 6 pv c2e1
info depth 96 seldepth 2 multipv 1 score mate 1 nodes 4128 nps 688000 tbhits 0 time 6 pv c2e1
info depth 97 seldepth 2 multipv 1 score mate 1 nodes 4171 nps 595857 tbhits 0 time 7 pv c2e1
info depth 98 seldepth 2 multipv 1 score mate 1 nodes 4214 nps 602000 tbhits 0 time 7 pv c2e1
info depth 99 seldepth 2 multipv 1 score mate 1 nodes 4257 nps 608142 tbhits 0 time 7 pv c2e1
info depth 100 seldepth 2 multipv 1 score mate 1 nodes 4300 nps 614285 tbhits 0 time 7 pv c2e1
info depth 101 seldepth 2 multipv 1 score mate 1 nodes 4343 nps 620428 tbhits 0 time 7 pv c2e1
info depth 102 seldepth 2 multipv 1 score mate 1 nodes 4386 nps 626571 tbhits 0 time 7 pv c2e1
info depth 103 seldepth 2 multipv 1 score mate 1 nodes 4429 nps 632714 tbhits 0 time 7 pv c2e1
info depth 104 seldepth 2 multipv 1 score mate 1 nodes 4472 nps 638857 tbhits 0 time 7 pv c2e1
info depth 105 seldepth 2 multipv 1 score mate 1 nodes 4515 nps 645000 tbhits 0 time 7 pv c2e1
info depth 106 seldepth 2 multipv 1 score mate 1 nodes 4558 nps 651142 tbhits 0 time 7 pv c2e1
info depth 107 seldepth 2 multipv 1 score mate 1 nodes 4601 nps 657285 tbhits 0 time 7 pv c2e1
info depth 108 seldepth 2 multipv 1 score mate 1 nodes 4644 nps 663428 tbhits 0 time 7 pv c2e1
info depth 109 seldepth 2 multipv 1 score mate 1 nodes 4687 nps 669571 tbhits 0 time 7 pv c2e1
info depth 110 seldepth 2 multipv 1 score mate 1 nodes 4730 nps 675714 tbhits 0 time 7 pv c2e1
info depth 111 seldepth 2 multipv 1 score mate 1 nodes 4773 nps 681857 tbhits 0 time 7 pv c2e1
info depth 112 seldepth 2 multipv 1 score mate 1 nodes 4816 nps 688000 tbhits 0 time 7 pv c2e1
info depth 113 seldepth 2 multipv 1 score mate 1 nodes 4859 nps 694142 tbhits 0 time 7 pv c2e1
info depth 114 seldepth 2 multipv 1 score mate 1 nodes 4902 nps 700285 tbhits 0 time 7 pv c2e1
info depth 115 seldepth 2 multipv 1 score mate 1 nodes 4945 nps 706428 tbhits 0 time 7 pv c2e1
info depth 116 seldepth 2 multipv 1 score mate 1 nodes 4988 nps 712571 tbhits 0 time 7 pv c2e1
info depth 117 seldepth 2 multipv 1 score mate 1 nodes 5031 nps 718714 tbhits 0 time 7 pv c2e1
info depth 118 seldepth 2 multipv 1 score mate 1 nodes 5074 nps 724857 tbhits 0 time 7 pv c2e1
info depth 119 seldepth 2 multipv 1 score mate 1 nodes 5117 nps 731000 tbhits 0 time 7 pv c2e1
info depth 120 seldepth 2 multipv 1 score mate 1 nodes 5160 nps 737142 tbhits 0 time 7 pv c2e1
info depth 121 seldepth 2 multipv 1 score mate 1 nodes 5203 nps 743285 tbhits 0 time 7 pv c2e1
info depth 122 seldepth 2 multipv 1 score mate 1 nodes 5246 nps 655750 tbhits 0 time 8 pv c2e1
info depth 123 seldepth 2 multipv 1 score mate 1 nodes 5289 nps 661125 tbhits 0 time 8 pv c2e1
info depth 124 seldepth 2 multipv 1 score mate 1 nodes 5332 nps 666500 tbhits 0 time 8 pv c2e1
info depth 125 seldepth 2 multipv 1 score mate 1 nodes 5375 nps 671875 tbhits 0 time 8 pv c2e1
info depth 126 seldepth 2 multipv 1 score mate 1 nodes 5418 nps 677250 tbhits 0 time 8 pv c2e1
info depth 127 seldepth 2 multipv 1 score mate 1 nodes 5461 nps 682625 tbhits 0 time 8 pv c2e1
info depth 128 seldepth 2 multipv 1 score mate 1 nodes 5504 nps 688000 tbhits 0 time 8 pv c2e1
info depth 129 seldepth 2 multipv 1 score mate 1 nodes 5547 nps 693375 tbhits 0 time 8 pv c2e1
info depth 130 seldepth 2 multipv 1 score mate 1 nodes 5590 nps 698750 tbhits 0 time 8 pv c2e1
info depth 131 seldepth 2 multipv 1 score mate 1 nodes 5633 nps 704125 tbhits 0 time 8 pv c2e1
info depth 132 seldepth 2 multipv 1 score mate 1 nodes 5676 nps 709500 tbhits 0 time 8 pv c2e1
info depth 133 seldepth 2 multipv 1 score mate 1 nodes 5719 nps 714875 tbhits 0 time 8 pv c2e1
info depth 134 seldepth 2 multipv 1 score mate 1 nodes 5762 nps 720250 tbhits 0 time 8 pv c2e1
info depth 135 seldepth 2 multipv 1 score mate 1 nodes 5805 nps 725625 tbhits 0 time 8 pv c2e1
info depth 136 seldepth 2 multipv 1 score mate 1 nodes 5848 nps 731000 tbhits 0 time 8 pv c2e1
info depth 137 seldepth 2 multipv 1 score mate 1 nodes 5891 nps 736375 tbhits 0 time 8 pv c2e1
info depth 138 seldepth 2 multipv 1 score mate 1 nodes 5934 nps 741750 tbhits 0 time 8 pv c2e1
info depth 139 seldepth 2 multipv 1 score mate 1 nodes 5977 nps 747125 tbhits 0 time 8 pv c2e1
info depth 140 seldepth 2 multipv 1 score mate 1 nodes 6020 nps 752500 tbhits 0 time 8 pv c2e1
info depth 141 seldepth 2 multipv 1 score mate 1 nodes 6063 nps 757875 tbhits 0 time 8 pv c2e1
info depth 142 seldepth 2 multipv 1 score mate 1 nodes 6106 nps 763250 tbhits 0 time 8 pv c2e1
info depth 143 seldepth 2 multipv 1 score mate 1 nodes 6149 nps 768625 tbhits 0 time 8 pv c2e1
info depth 144 seldepth 2 multipv 1 score mate 1 nodes 6192 nps 774000 tbhits 0 time 8 pv c2e1
info depth 145 seldepth 2 multipv 1 score mate 1 nodes 6235 nps 779375 tbhits 0 time 8 pv c2e1
info depth 146 seldepth 2 multipv 1 score mate 1 nodes 6278 nps 697555 tbhits 0 time 9 pv c2e1
info depth 147 seldepth 2 multipv 1 score mate 1 nodes 6321 nps 702333 tbhits 0 time 9 pv c2e1
info depth 148 seldepth 2 multipv 1 score mate 1 nodes 6364 nps 707111 tbhits 0 time 9 pv c2e1
info depth 149 seldepth 2 multipv 1 score mate 1 nodes 6407 nps 711888 tbhits 0 time 9 pv c2e1
info depth 150 seldepth 2 multipv 1 score mate 1 nodes 6450 nps 716666 tbhits 0 time 9 pv c2e1
info depth 151 seldepth 2 multipv 1 score mate 1 nodes 6493 nps 721444 tbhits 0 time 9 pv c2e1
info depth 152 seldepth 2 multipv 1 score mate 1 nodes 6536 nps 726222 tbhits 0 time 9 pv c2e1
info depth 153 seldepth 2 multipv 1 score mate 1 nodes 6579 nps 731000 tbhits 0 time 9 pv c2e1
info depth 154 seldepth 2 multipv 1 score mate 1 nodes 6622 nps 735777 tbhits 0 time 9 pv c2e1
info depth 155 seldepth 2 multipv 1 score mate 1 nodes 6665 nps 740555 tbhits 0 time 9 pv c2e1
info depth 156 seldepth 2 multipv 1 score mate 1 nodes 6708 nps 745333 tbhits 0 time 9 pv c2e1
info depth 157 seldepth 2 multipv 1 score mate 1 nodes 6751 nps 750111 tbhits 0 time 9 pv c2e1
info depth 158 seldepth 2 multipv 1 score mate 1 nodes 6794 nps 754888 tbhits 0 time 9 pv c2e1
info depth 159 seldepth 2 multipv 1 score mate 1 nodes 6837 nps 759666 tbhits 0 time 9 pv c2e1
info depth 160 seldepth 2 multipv 1 score mate 1 nodes 6880 nps 764444 tbhits 0 time 9 pv c2e1
info depth 161 seldepth 2 multipv 1 score mate 1 nodes 6923 nps 769222 tbhits 0 time 9 pv c2e1
info depth 162 seldepth 2 multipv 1 score mate 1 nodes 6966 nps 774000 tbhits 0 time 9 pv c2e1
info depth 163 seldepth 2 multipv 1 score mate 1 nodes 7009 nps 700900 tbhits 0 time 10 pv c2e1
info depth 164 seldepth 2 multipv 1 score mate 1 nodes 7052 nps 705200 tbhits 0 time 10 pv c2e1
info depth 165 seldepth 2 multipv 1 score mate 1 nodes 7095 nps 709500 tbhits 0 time 10 pv c2e1
info depth 166 seldepth 2 multipv 1 score mate 1 nodes 7138 nps 713800 tbhits 0 time 10 pv c2e1
info depth 167 seldepth 2 multipv 1 score mate 1 nodes 7181 nps 718100 tbhits 0 time 10 pv c2e1
info depth 168 seldepth 2 multipv 1 score mate 1 nodes 7224 nps 722400 tbhits 0 time 10 pv c2e1
info depth 169 seldepth 2 multipv 1 score mate 1 nodes 7267 nps 726700 tbhits 0 time 10 pv c2e1
info depth 170 seldepth 2 multipv 1 score mate 1 nodes 7310 nps 731000 tbhits 0 time 10 pv c2e1
info depth 171 seldepth 2 multipv 1 score mate 1 nodes 7353 nps 735300 tbhits 0 time 10 pv c2e1
info depth 172 seldepth 2 multipv 1 score mate 1 nodes 7396 nps 739600 tbhits 0 time 10 pv c2e1
info depth 173 seldepth 2 multipv 1 score mate 1 nodes 7439 nps 743900 tbhits 0 time 10 pv c2e1
info depth 174 seldepth 2 multipv 1 score mate 1 nodes 7482 nps 748200 tbhits 0 time 10 pv c2e1
info depth 175 seldepth 2 multipv 1 score mate 1 nodes 7525 nps 752500 tbhits 0 time 10 pv c2e1
info depth 176 seldepth 2 multipv 1 score mate 1 nodes 7568 nps 756800 tbhits 0 time 10 pv c2e1
info depth 177 seldepth 2 multipv 1 score mate 1 nodes 7611 nps 761100 tbhits 0 time 10 pv c2e1
info depth 178 seldepth 2 multipv 1 score mate 1 nodes 7654 nps 765400 tbhits 0 time 10 pv c2e1
info depth 179 seldepth 2 multipv 1 score mate 1 nodes 7697 nps 769700 tbhits 0 time 10 pv c2e1
info depth 180 seldepth 2 multipv 1 score mate 1 nodes 7740 nps 774000 tbhits 0 time 10 pv c2e1
info depth 181 seldepth 2 multipv 1 score mate 1 nodes 7783 nps 707545 tbhits 0 time 11 pv c2e1
info depth 182 seldepth 2 multipv 1 score mate 1 nodes 7826 nps 711454 tbhits 0 time 11 pv c2e1
info depth 183 seldepth 2 multipv 1 score mate 1 nodes 7869 nps 715363 tbhits 0 time 11 pv c2e1
info depth 184 seldepth 2 multipv 1 score mate 1 nodes 7912 nps 719272 tbhits 0 time 11 pv c2e1
info depth 185 seldepth 2 multipv 1 score mate 1 nodes 7955 nps 723181 tbhits 0 time 11 pv c2e1
info depth 186 seldepth 2 multipv 1 score mate 1 nodes 7998 nps 727090 tbhits 0 time 11 pv c2e1
info depth 187 seldepth 2 multipv 1 score mate 1 nodes 8041 nps 731000 tbhits 0 time 11 pv c2e1
info depth 188 seldepth 2 multipv 1 score mate 1 nodes 8084 nps 734909 tbhits 0 time 11 pv c2e1
info depth 189 seldepth 2 multipv 1 score mate 1 nodes 8127 nps 738818 tbhits 0 time 11 pv c2e1
info depth 190 seldepth 2 multipv 1 score mate 1 nodes 8170 nps 742727 tbhits 0 time 11 pv c2e1
info depth 191 seldepth 2 multipv 1 score mate 1 nodes 8213 nps 746636 tbhits 0 time 11 pv c2e1
info depth 192 seldepth 2 multipv 1 score mate 1 nodes 8256 nps 750545 tbhits 0 time 11 pv c2e1
info depth 193 seldepth 2 multipv 1 score mate 1 nodes 8299 nps 691583 tbhits 0 time 12 pv c2e1
info depth 194 seldepth 2 multipv 1 score mate 1 nodes 8342 nps 695166 tbhits 0 time 12 pv c2e1
info depth 195 seldepth 2 multipv 1 score mate 1 nodes 8385 nps 698750 tbhits 0 time 12 pv c2e1
info depth 196 seldepth 2 multipv 1 score mate 1 nodes 8428 nps 702333 tbhits 0 time 12 pv c2e1
info depth 197 seldepth 2 multipv 1 score mate 1 nodes 8471 nps 705916 tbhits 0 time 12 pv c2e1
info depth 198 seldepth 2 multipv 1 score mate 1 nodes 8514 nps 709500 tbhits 0 time 12 pv c2e1
info depth 199 seldepth 2 multipv 1 score mate 1 nodes 8557 nps 713083 tbhits 0 time 12 pv c2e1
info depth 200 seldepth 2 multipv 1 score mate 1 nodes 8600 nps 716666 tbhits 0 time 12 pv c2e1
info depth 201 seldepth 2 multipv 1 score mate 1 nodes 8643 nps 720250 tbhits 0 time 12 pv c2e1
info depth 202 seldepth 2 multipv 1 score mate 1 nodes 8686 nps 723833 tbhits 0 time 12 pv c2e1
info depth 203 seldepth 2 multipv 1 score mate 1 nodes 8729 nps 727416 tbhits 0 time 12 pv c2e1
info depth 204 seldepth 2 multipv 1 score mate 1 nodes 8772 nps 731000 tbhits 0 time 12 pv c2e1
info depth 205 seldepth 2 multipv 1 score mate 1 nodes 8815 nps 734583 tbhits 0 time 12 pv c2e1
info depth 206 seldepth 2 multipv 1 score mate 1 nodes 8858 nps 738166 tbhits 0 time 12 pv c2e1
info depth 207 seldepth 2 multipv 1 score mate 1 nodes 8901 nps 741750 tbhits 0 time 12 pv c2e1
info depth 208 seldepth 2 multipv 1 score mate 1 nodes 8944 nps 745333 tbhits 0 time 12 pv c2e1
info depth 209 seldepth 2 multipv 1 score mate 1 nodes 8987 nps 748916 tbhits 0 time 12 pv c2e1
info depth 210 seldepth 2 multipv 1 score mate 1 nodes 9030 nps 694615 tbhits 0 time 13 pv c2e1
info depth 211 seldepth 2 multipv 1 score mate 1 nodes 9073 nps 697923 tbhits 0 time 13 pv c2e1
info depth 212 seldepth 2 multipv 1 score mate 1 nodes 9116 nps 701230 tbhits 0 time 13 pv c2e1
info depth 213 seldepth 2 multipv 1 score mate 1 nodes 9159 nps 704538 tbhits 0 time 13 pv c2e1
info depth 214 seldepth 2 multipv 1 score mate 1 nodes 9202 nps 707846 tbhits 0 time 13 pv c2e1
info depth 215 seldepth 2 multipv 1 score mate 1 nodes 9245 nps 711153 tbhits 0 time 13 pv c2e1
info depth 216 seldepth 2 multipv 1 score mate 1 nodes 9288 nps 714461 tbhits 0 time 13 pv c2e1
info depth 217 seldepth 2 multipv 1 score mate 1 nodes 9331 nps 717769 tbhits 0 time 13 pv c2e1
info depth 218 seldepth 2 multipv 1 score mate 1 nodes 9374 nps 721076 tbhits 0 time 13 pv c2e1
info depth 219 seldepth 2 multipv 1 score mate 1 nodes 9417 nps 724384 tbhits 0 time 13 pv c2e1
info depth 220 seldepth 2 multipv 1 score mate 1 nodes 9460 nps 727692 tbhits 0 time 13 pv c2e1
info depth 221 seldepth 2 multipv 1 score mate 1 nodes 9503 nps 731000 tbhits 0 time 13 pv c2e1
info depth 222 seldepth 2 multipv 1 score mate 1 nodes 9546 nps 734307 tbhits 0 time 13 pv c2e1
info depth 223 seldepth 2 multipv 1 score mate 1 nodes 9589 nps 737615 tbhits 0 time 13 pv c2e1
info depth 224 seldepth 2 multipv 1 score mate 1 nodes 9632 nps 740923 tbhits 0 time 13 pv c2e1
info depth 225 seldepth 2 multipv 1 score mate 1 nodes 9675 nps 744230 tbhits 0 time 13 pv c2e1
info depth 226 seldepth 2 multipv 1 score mate 1 nodes 9718 nps 747538 tbhits 0 time 13 pv c2e1
info depth 227 seldepth 2 multipv 1 score mate 1 nodes 9761 nps 750846 tbhits 0 time 13 pv c2e1
info depth 228 seldepth 2 multipv 1 score mate 1 nodes 9804 nps 754153 tbhits 0 time 13 pv c2e1
info depth 229 seldepth 2 multipv 1 score mate 1 nodes 9847 nps 757461 tbhits 0 time 13 pv c2e1
info depth 230 seldepth 2 multipv 1 score mate 1 nodes 9890 nps 760769 tbhits 0 time 13 pv c2e1
info depth 231 seldepth 2 multipv 1 score mate 1 nodes 9933 nps 764076 tbhits 0 time 13 pv c2e1
info depth 232 seldepth 2 multipv 1 score mate 1 nodes 9976 nps 767384 tbhits 0 time 13 pv c2e1
info depth 233 seldepth 2 multipv 1 score mate 1 nodes 10019 nps 715642 tbhits 0 time 14 pv c2e1
info depth 234 seldepth 2 multipv 1 score mate 1 nodes 10062 nps 718714 tbhits 0 time 14 pv c2e1
info depth 235 seldepth 2 multipv 1 score mate 1 nodes 10105 nps 721785 tbhits 0 time 14 pv c2e1
info depth 236 seldepth 2 multipv 1 score mate 1 nodes 10148 nps 724857 tbhits 0 time 14 pv c2e1
info depth 237 seldepth 2 multipv 1 score mate 1 nodes 10191 nps 727928 tbhits 0 time 14 pv c2e1
info depth 238 seldepth 2 multipv 1 score mate 1 nodes 10234 nps 731000 tbhits 0 time 14 pv c2e1
info depth 239 seldepth 2 multipv 1 score mate 1 nodes 10277 nps 734071 tbhits 0 time 14 pv c2e1
info depth 240 seldepth 2 multipv 1 score mate 1 nodes 10320 nps 737142 tbhits 0 time 14 pv c2e1
info depth 241 seldepth 2 multipv 1 score mate 1 nodes 10363 nps 740214 tbhits 0 time 14 pv c2e1
info depth 242 seldepth 2 multipv 1 score mate 1 nodes 10406 nps 743285 tbhits 0 time 14 pv c2e1
info depth 243 seldepth 2 multipv 1 score mate 1 nodes 10449 nps 696600 tbhits 0 time 15 pv c2e1
info depth 244 seldepth 2 multipv 1 score mate 1 nodes 10492 nps 699466 tbhits 0 time 15 pv c2e1
info depth 245 seldepth 2 multipv 1 score mate 1 nodes 10535 nps 702333 tbhits 0 time 15 pv c2e1
```

candidateMoves: `['c2e1']`; events: `[{'type': 'LAST_OPPONENT_KING_CAPTURED', 'side': 'BLACK', 'ply': 86, 'square': 'e1', 'kingId': 'WK1', 'result': 'BLACK_WIN'}]`.

Полный набор ходов королём стороны хода: `[{'type': 'MOVE', 'from': 'c8', 'to': 'b7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'c7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'd7', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'b8', 'moveKind': 'NORMAL', 'promotion': None}, {'type': 'MOVE', 'from': 'c8', 'to': 'd8', 'moveKind': 'NORMAL', 'promotion': None}]`.

Финальная позиция: 15 фигур; короли `[('c8', 'bk1')]`; toMove `WHITE`, result `BLACK_WIN`.

King IDs/counters: `{'2': 'BK1'}` / `{'w': 1, 'b': 1}`.
