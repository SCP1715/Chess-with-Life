# Self-play по правилам «Шахмат с жизнями»

Это описательный исследовательский прогон, не измерение Elo и не обучение NNUE.
Старые серии не объединялись с этой выборкой. A/B-пары имеют общий дебютный префикс, поэтому префиксные события не являются независимыми наблюдениями.

Движок SHA-256: `68018a585b4f8ba1d7a064fd4b00f8786b28cb8d28fa111821bec73c213cbb40`.
Фактически накопленное время A/B/C (сек): `{"A": 14818.065999999844, "B": 6192.719000000051, "C": 3883.5269999999723}`.
A/B-префиксы: совпало 167/175; расхождений: 8.
Проверка позиций с последним королём под атакой: 1707; результаты в `king-threat-continuations.jsonl`.

## A

Попыток 2291; завершено 2021; остановлено 270; ошибок 0.
Результаты: `{"BLACK_WIN": 1000, "WHITE_WIN": 971, "DRAW_AGREEMENT": 6, "DRAW_REPETITION": 19, "DRAW_FIFTY_MOVES": 25}`.
Медианная длина завершённой партии: 127 полуходов (знаменатель: 2021).
Уникальные полные последовательности: 453; уникальные дебюты: 451.
Вызовов draw-policy: 344657; позиций с доступным Claim: 21273; фактических Claim: 44.
Действия при доступном Claim: `{"DECLINE_DRAW": 20, "MOVE": 21229, "CLAIM_REPETITION": 19, "CLAIM_FIFTY_MOVES": 25}`; оценка относительно порога: `{"above_threshold": 21275, "at_or_below_threshold": 18}`.
Входящее предложение / свой ограничитель: `{"none": 341288, "pending": 3369}` / `{"clear": 248338, "latched": 96319}`.
Доля партий с событиями (% от всех попыток): `{"CASTLING": 76.037, "DRAW_OFFERED": 99.258, "DRAW_DECLINED": 98.996, "LOW_MATERIAL_POSITION": 75.164, "PROMOTION": 57.704, "KING_LEFT_UNDER_ATTACK": 70.188, "LAST_OPPONENT_KING_CAPTURED": 86.032, "KING_SELF_CAPTURE": 34.57, "DRAW_CLAIM_AVAILABLE_UNUSED": 14.579, "DRAW_ACCEPTED": 0.262, "KING_PROMOTION_AVAILABLE_OTHER_CHOSEN": 0.96, "KING_PROMOTION": 3.012, "DEBT_RESPONSE": 3.012, "DRAW_CLAIM_REPETITION": 0.829, "DRAW_CLAIM_FIFTY_MOVES": 1.091}`.
Событий на 1000 записанных игровых полуходов: `{"CASTLING": 7.165, "DRAW_OFFERED": 9.971, "DRAW_DECLINED": 9.953, "LOW_MATERIAL_POSITION": 5.446, "PROMOTION": 6.328, "KING_LEFT_UNDER_ATTACK": 4.99, "LAST_OPPONENT_KING_CAPTURED": 5.834, "KING_SELF_CAPTURE": 3.001, "DRAW_CLAIM_AVAILABLE_UNUSED": 62.831, "DRAW_ACCEPTED": 0.018, "KING_PROMOTION_AVAILABLE_OTHER_CHOSEN": 0.065, "KING_PROMOTION": 0.222, "DEBT_RESPONSE": 0.222, "DRAW_CLAIM_REPETITION": 0.056, "DRAW_CLAIM_FIFTY_MOVES": 0.074}`.
События: `{"CASTLING": 2421, "DRAW_OFFERED": 3369, "DRAW_DECLINED": 3363, "LOW_MATERIAL_POSITION": 1840, "PROMOTION": 2138, "KING_LEFT_UNDER_ATTACK": 1686, "LAST_OPPONENT_KING_CAPTURED": 1971, "KING_SELF_CAPTURE": 1014, "DRAW_CLAIM_AVAILABLE_UNUSED": 21229, "DRAW_ACCEPTED": 6, "KING_PROMOTION_AVAILABLE_OTHER_CHOSEN": 22, "KING_PROMOTION": 75, "DEBT_RESPONSE": 75, "DRAW_CLAIM_REPETITION": 19, "DRAW_CLAIM_FIFTY_MOVES": 25}`.

## B

Попыток 176; завершено 141; остановлено 34; ошибок 1.
Результаты: `{"DRAW_REPETITION": 12, "DRAW_FIFTY_MOVES": 1, "BLACK_WIN": 59, "WHITE_WIN": 69}`.
Медианная длина завершённой партии: 133 полуходов (знаменатель: 141).
Уникальные полные последовательности: 109; уникальные дебюты: 108.
Вызовов draw-policy: 27094; позиций с доступным Claim: 2202; фактических Claim: 13.
Действия при доступном Claim: `{"CLAIM_REPETITION": 12, "MOVE": 2189, "CLAIM_FIFTY_MOVES": 1}`; оценка относительно порога: `{"at_or_below_threshold": 9, "above_threshold": 2193}`.
Входящее предложение / свой ограничитель: `{"none": 26924, "pending": 170}` / `{"clear": 19712, "latched": 7382}`.
Доля партий с событиями (% от всех попыток): `{"CASTLING": 88.068, "DRAW_OFFERED": 92.614, "DRAW_DECLINED": 92.614, "KING_SELF_CAPTURE": 37.5, "LOW_MATERIAL_POSITION": 82.386, "DRAW_CLAIM_REPETITION": 6.818, "DRAW_CLAIM_AVAILABLE_UNUSED": 22.159, "PROMOTION": 45.455, "DRAW_CLAIM_FIFTY_MOVES": 0.568, "KING_LEFT_UNDER_ATTACK": 51.705, "LAST_OPPONENT_KING_CAPTURED": 72.727, "KING_PROMOTION": 6.25, "DEBT_RESPONSE": 6.25}`.
Событий на 1000 записанных игровых полуходов: `{"CASTLING": 8.632, "DRAW_OFFERED": 6.026, "DRAW_DECLINED": 6.026, "KING_SELF_CAPTURE": 2.844, "LOW_MATERIAL_POSITION": 5.382, "DRAW_CLAIM_REPETITION": 0.406, "DRAW_CLAIM_AVAILABLE_UNUSED": 74.1, "PROMOTION": 3.893, "DRAW_CLAIM_FIFTY_MOVES": 0.034, "KING_LEFT_UNDER_ATTACK": 3.385, "LAST_OPPONENT_KING_CAPTURED": 4.333, "KING_PROMOTION": 0.372, "DEBT_RESPONSE": 0.372}`.
События: `{"CASTLING": 255, "DRAW_OFFERED": 178, "DRAW_DECLINED": 178, "KING_SELF_CAPTURE": 84, "LOW_MATERIAL_POSITION": 159, "DRAW_CLAIM_REPETITION": 12, "DRAW_CLAIM_AVAILABLE_UNUSED": 2189, "PROMOTION": 115, "DRAW_CLAIM_FIFTY_MOVES": 1, "KING_LEFT_UNDER_ATTACK": 100, "LAST_OPPONENT_KING_CAPTURED": 128, "KING_PROMOTION": 11, "DEBT_RESPONSE": 11}`.

## C

Попыток 116; завершено 107; остановлено 9; ошибок 0.
Результаты: `{"DRAW_FIFTY_MOVES": 72, "WHITE_WIN": 17, "DRAW_REPETITION": 13, "BLACK_WIN": 5}`.
Медианная длина завершённой партии: 449 полуходов (знаменатель: 107).
Уникальные полные последовательности: 35; уникальные дебюты: 35.
Вызовов draw-policy: 28480; позиций с доступным Claim: 23096; фактических Claim: 85.
Действия при доступном Claim: `{"MOVE": 23011, "CLAIM_FIFTY_MOVES": 72, "CLAIM_REPETITION": 13}`; оценка относительно порога: `{"above_threshold": 23084, "at_or_below_threshold": 12}`.
Входящее предложение / свой ограничитель: `{"none": 28458, "pending": 22}` / `{"clear": 27553, "latched": 927}`.
Доля партий с событиями (% от всех попыток): `{"DRAW_CLAIM_AVAILABLE_UNUSED": 97.414, "DRAW_CLAIM_FIFTY_MOVES": 62.069, "KING_SELF_CAPTURE": 7.759, "LOW_MATERIAL_POSITION": 14.655, "DRAW_OFFERED": 18.966, "DRAW_DECLINED": 18.966, "PROMOTION": 17.241, "LAST_OPPONENT_KING_CAPTURED": 18.966, "DRAW_CLAIM_REPETITION": 11.207, "KING_LEFT_UNDER_ATTACK": 6.034}`.
Событий на 1000 записанных игровых полуходов: `{"DRAW_CLAIM_AVAILABLE_UNUSED": 811.647, "DRAW_CLAIM_FIFTY_MOVES": 2.54, "KING_SELF_CAPTURE": 0.317, "LOW_MATERIAL_POSITION": 0.67, "DRAW_OFFERED": 0.776, "DRAW_DECLINED": 0.776, "PROMOTION": 0.741, "LAST_OPPONENT_KING_CAPTURED": 0.776, "DRAW_CLAIM_REPETITION": 0.459, "KING_LEFT_UNDER_ATTACK": 0.247}`.
События: `{"DRAW_CLAIM_AVAILABLE_UNUSED": 23011, "DRAW_CLAIM_FIFTY_MOVES": 72, "KING_SELF_CAPTURE": 9, "LOW_MATERIAL_POSITION": 19, "DRAW_OFFERED": 22, "DRAW_DECLINED": 22, "PROMOTION": 21, "LAST_OPPONENT_KING_CAPTURED": 22, "DRAW_CLAIM_REPETITION": 13, "KING_LEFT_UNDER_ATTACK": 7}`.

Исходы исходных партий на 300 полуходах: `{"MAX_PLIES": 116}`; исходы/остановки после продолжения указаны отдельно выше.

## Артефакты

Для каждого профиля доступны полные сжатые журналы `games/*.jsonl.gz`, сводка, CSV незавершённых партий, журнал событий, диагностика решений ничьей и эпизоды пешек. `paired-openings.csv` фиксирует проверку воспроизведения префиксов.
