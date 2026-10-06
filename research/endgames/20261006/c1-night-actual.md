# Поиск точной позиции C-1

Источник поиска: `C/games/game-A-000001.jsonl.gz`; его результаты не объединялись с другими сериями. Искомая расстановка: White Kb7/Bf3, Black Kh2/Ba1, белые ходят.

Совпадений: 1.

- `C/games/game-A-000001.jsonl.gz`: game `1`, запись 901, ply 1200 (after); clock 910; current repetition count 1; Claim50 `True`; ClaimRep `False`; repetition entries 939; rights `000000`; EP `-1`; debt `None`; parent `13`; result `None`/`MAX_PLIES`.

Результаты для этого состояния:

- `MODEL_UNBOUNDED`: **DRAW**, точно по полной таблице KBKB.
- `MODEL_FIFTY`: **DRAW**. Claim50 доступен, но добровольное заявление лишь
  добавляет действие с исходом DRAW и не отменяет обычные ходы.
- `MODEL_HISTORY`: **DRAW**. В текущем состоянии ClaimRep недоступен, активного
  предложения ничьей нет; будущие повторные заявления также добровольны и
  заканчиваются только ничьей.

Перенос доказательства здесь не основан на одном факте доступности Claim:
сначала полная таблица доказывает DRAW в `MODEL_UNBOUNDED`; стратегия,
удерживающая ничью, остаётся доступна в обоих claim-aware расширениях, а
дополнительное заявление само может дать только DRAW. Поэтому точное значение
конкретной C-1 позиции остаётся DRAW во всех трёх моделях. Проверяемый разбор
и предпосылки записаны в `c1-night-actual.json`; воспроизведение:
`python tools/endgame/audit_c1.py playtest-results/endgame-analysis-20261006/c1-night-actual.json playtest-results/endgame-analysis-20261006/tables`.
