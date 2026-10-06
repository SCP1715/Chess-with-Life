# Профиль C

Попыток: 116; завершено: 107; прервано: 9; технических ошибок: 0.
Исходы завершённых партий: {"DRAW_FIFTY_MOVES": 72, "WHITE_WIN": 17, "DRAW_REPETITION": 13, "BLACK_WIN": 5}.
Медианная длина завершённой партии: 449 полуходов (знаменатель 107 завершённых партий).
Уникальных полных последовательностей: 35; уникальных 16-полуходовых префиксов: 35.

Показатели событий указаны как количество событий; знаменатели — в summary.json.

Вызовов draw-policy: 28480; с доступным Claim: 23096; уникальных партий-позиций с Claim: 23096; фактических Claim: 85.
Действия при доступном Claim: `{"MOVE": 23011, "CLAIM_FIFTY_MOVES": 72, "CLAIM_REPETITION": 13}`; оценка относительно порога: `{"above_threshold": 23084, "at_or_below_threshold": 12}` (это корреляция, не объяснение мотива движка).
Входящее предложение / свой ограничитель: `{"none": 28458, "pending": 22}` / `{"clear": 27553, "latched": 927}`.
Доля партий с событиями по типам: `{"DRAW_CLAIM_AVAILABLE_UNUSED": 97.414, "DRAW_CLAIM_FIFTY_MOVES": 62.069, "KING_SELF_CAPTURE": 7.759, "LOW_MATERIAL_POSITION": 14.655, "DRAW_OFFERED": 18.966, "DRAW_DECLINED": 18.966, "PROMOTION": 17.241, "LAST_OPPONENT_KING_CAPTURED": 18.966, "DRAW_CLAIM_REPETITION": 11.207, "KING_LEFT_UNDER_ATTACK": 6.034}` (% от всех попыток профиля).
Событий на 1000 записанных игровых полуходов: `{"DRAW_CLAIM_AVAILABLE_UNUSED": 811.647, "DRAW_CLAIM_FIFTY_MOVES": 2.54, "KING_SELF_CAPTURE": 0.317, "LOW_MATERIAL_POSITION": 0.67, "DRAW_OFFERED": 0.776, "DRAW_DECLINED": 0.776, "PROMOTION": 0.741, "LAST_OPPONENT_KING_CAPTURED": 0.776, "DRAW_CLAIM_REPETITION": 0.459, "KING_LEFT_UNDER_ATTACK": 0.247}`.
