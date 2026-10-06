# Исследовательские материалы LifeChess — 6 октября 2026

Это отдельная публикация экспериментальных данных, не релиз приложения и не
оценка Elo. Вложения содержат архивные партии self-play и таблицы результатов
для точно перечисленных классов окончаний. Отчёты, методики, ограничения и
небольшие примеры доступны в каталоге [`research`](README.md).

## Архивные серии

- `series-20261006-data.zip`: исходный 100-партийный прогон. В нём обнаружена
  ошибка общего ограничителя повторных предложений ничьей. Эпизоды сохраняют
  исследовательскую ценность, но частоты нельзя переносить на исправленный
  движок.
- `series-20261006-v2-data.zip`: новый отдельный 100-партийный прогон с
  независимым ограничителем для каждой стороны. Он предшествует добавлению
  заявления ничьей при двух несоседних голых королях; старые партии не
  пересчитывались под более позднее правило.
- `night-20261006-A.part1.rar`, `part2.rar`, `part3.rar`: три тома одного
  многотомного архива профиля A. Для распаковки обязательны все три.
- `night-20261006.rar`: профили B/C и пилот. Профиль C продолжает часть A/B
  партий; A и B используют совпадающие дебютные префиксы. Это зависимые
  траектории, не независимая статистическая выборка.

Все указанные архивы проверены WinRAR; для многотомного профиля A тест первой
части автоматически проверил связанные тома. Контрольные суммы вложений — в
`SHA256SUMS.txt`.

## Точный анализ окончаний

`lifechess-endgame-tables-20261006.zip` содержит только бинарные таблицы.
Они охватывают перечисленные в `research/endgames/20261006/coverage-manifest.json`
материальные классы и не являются общим решателем. Состояния с пешками,
превращениями, долгами, несколькими королями и многими специальными правами
остаются вне полного покрытия. Табличный DRAW не завершает партию в приложении.

## Воспроизводимость

Старые серии работали на Fairy-Stockfish ревизии
`9f778da667f6e07dae1e85d3e2ea204fc6dee94d`, собранной с локальным patch
SHA-256 `3168a204bc48a9269940b508dc1d771a7ef84438766cba787d4cd835cce96c70`.
Сам patch не сохранён; точную старую сборку нельзя восстановить только по
commit. Журналы можно заново анализировать, но повторить эксперимент тем же
бинарником по опубликованному исходнику нельзя. Новый адаптированный fork
Fairy-Stockfish — отдельная последующая ревизия и не подменяет происхождение
этих результатов.

---

# LifeChess research data — 6 October 2026

This is a separate publication of experimental data, not an app release or an
Elo rating. Assets contain archived self-play games and endgame tables for the
explicitly listed classes. Reports, methods, limitations, and small examples
are in the [`research`](README.md) directory.

## Archived runs

- `series-20261006-data.zip`: the original 100-game run. A shared draw-offer
  cooldown bug was found. Recorded episodes remain useful, but their
  frequencies must not be attributed to the corrected engine.
- `series-20261006-v2-data.zip`: a separate 100-game run with an independent
  cooldown per side. It predates the draw claim for two non-adjacent bare
  kings; these old games were not recalculated under the later rule.
- `night-20261006-A.part1.rar`, `part2.rar`, `part3.rar`: three required volumes
  of one multi-volume Profile A archive.
- `night-20261006.rar`: Profiles B/C and the pilot. Profile C continues some
  A/B games, and A/B share opening prefixes; these are dependent trajectories,
  not independent statistical samples.

All listed archives passed WinRAR integrity tests. Testing the first A volume
also checked its companion volumes. Asset checksums are in `SHA256SUMS.txt`.

## Exact endgame analysis

`lifechess-endgame-tables-20261006.zip` contains only binary tables. Coverage
is limited to the material classes listed in
`research/endgames/20261006/coverage-manifest.json`; this is not a general
solver. Pawns, promotions, debts, multiple kings, and many special rights are
not fully covered. A table DRAW is not an app game result.

## Reproducibility

The old runs used Fairy-Stockfish revision
`9f778da667f6e07dae1e85d3e2ea204fc6dee94d` with a local patch whose SHA-256 was
`3168a204bc48a9269940b508dc1d771a7ef84438766cba787d4cd835cce96c70`. The patch
source was not preserved, so the exact old binary cannot be rebuilt from the
commit alone. The logs can be reanalyzed, but reproducing the run with the same
binary from published source is not possible. The later adapted Fairy-Stockfish
fork is a separate revision and does not replace the provenance of these
results.
