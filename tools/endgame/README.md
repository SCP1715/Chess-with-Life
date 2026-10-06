# LifeChess exact endgame analysis

This is an offline research tool. It does not change the Android rules, the
engine evaluation, draw policy, or APK. Table results do not end a game.

## Implemented coverage

Version 1 solves `MODEL_UNBOUNDED` positions with exactly one king per side,
zero pawns, no castling rights, no en-passant square, and no king-capture debt:

| Class | Complete material assignments | Valid states |
|---|---:|---:|
| KK | 1 | 8,064 |
| KBK, bishop White | 1 | 499,968 |
| KBK, bishop Black | 1 | 499,968 |
| KNK, knight White | 1 | 499,968 |
| KNK, knight Black | 1 | 499,968 |
| KRK, rook White | 1 | 499,968 |
| KRK, rook Black | 1 | 499,968 |
| KQK, queen White | 1 | 499,968 |
| KQK, queen Black | 1 | 499,968 |
| KBKB, one bishop per color | 1 | 30,498,048 |

Kings may move into attack, capture adjacent enemy kings for an immediate win,
and capture their own lone minor. There is no check/checkmate filtering. A
minor cannot capture a friendly piece. Capturing the only minor transitions to
the already-solved KK table. In KBKB, any bishop capture or king capture of a
bishop transitions to the corresponding one-bishop KBK table; kings may also
capture their own bishop. The table graph includes both sides to move and every
non-overlapping square assignment.

`MODEL_FIFTY` and `MODEL_HISTORY` are not generally solved. One narrow proof is
implemented for an exact `MODEL_UNBOUNDED` DRAW: if the other model only adds
voluntary terminal-draw actions without removing ordinary moves, its value is
still DRAW. `audit_c1.py` applies this transfer to the archived C-1 state after
checking the full board, Claim50/ClaimRep and pending-offer facts. Do not apply
this shortcut to positions with a base WIN/LOSS or to a changed action set.
Unsupported rights, debt, pawns, or more than four occupied squares are never
stripped from a position to force a lookup. Other uncomputed ≤4-piece material
classes remain `UNKNOWN`/`IN_PROGRESS`.

## Proof and table format

`solve.cpp` builds the complete finite transition graph, performs retrograde
WDL propagation, and assigns the unresolved closed draw region only after the
WIN/LOSS attractor is exhausted. `DTLK` is the number of plies to capture the
last opposing king when the winner minimizes and the loser maximizes distance
without changing WDL. All WDL-preserving actions and all secondary-optimal
actions are derived by `table_io.py` from the saved table.

`verify_tables.py` is a separate checker with its own board representation and
move generator. It checks every valid state in KK/KBK/KNK/KRK/KQK for WDL,
the closed draw region, and exact DTLK recurrence. The 30-million-state KBKB
table is checked by the separately implemented `verify_kbkb.cpp`, which
independently enumerates every forward action and dependency edge. The binary
format is little-endian, tagged `LCTB1`, rules version 1.0, and model
`MODEL_UNBOUNDED`.

## Reproduce / resume

From the repository root in PowerShell, with MSYS2 g++ and Python available:

```powershell
g++ -std=c++17 -O3 -DNDEBUG -Wall -Wextra -pedantic `
  tools/endgame/solve.cpp -o playtest-results/endgame-analysis-20261006/lifechess_tb_solve.exe
.\playtest-results\endgame-analysis-20261006\lifechess_tb_solve.exe `
  playtest-results/endgame-analysis-20261006/tables ALL
python tools/endgame/verify_tables.py playtest-results/endgame-analysis-20261006/tables
g++ -std=c++17 -O3 -DNDEBUG -Wall -Wextra -pedantic `
  tools/endgame/verify_kbkb.cpp -o playtest-results/endgame-analysis-20261006/verify_kbkb.exe
.\playtest-results\endgame-analysis-20261006\verify_kbkb.exe `
  playtest-results/endgame-analysis-20261006/tables
python tools/endgame/summarize_tables.py playtest-results/endgame-analysis-20261006/tables `
  playtest-results/endgame-analysis-20261006/table-statistics.json
python tools/endgame/audit_c1.py `
  playtest-results/endgame-analysis-20261006/c1-night-actual.json `
  playtest-results/endgame-analysis-20261006/tables
python -c "import sys,unittest; sys.path.insert(0,'.'); suite=unittest.defaultTestLoader.loadTestsFromName('tools.test_lifechess_endgame'); result=unittest.TextTestRunner(verbosity=2).run(suite); sys.exit(not result.wasSuccessful())"
```

The solver accepts `KK`, `KBK`, `KNK`, `KRK`, `KQK`, `KBKB`, or one
color-specific name such as `KQK-WHITE`. Completed table files are reused on
restart; each table is written to a temporary sibling and renamed only when
complete. KBKB uses an implicit predecessor generator to avoid retaining its
hundreds of millions of edges in memory. `ALL` computes or resumes every
implemented class; it does not imply that unsupported material has been solved:

```powershell
.\playtest-results\endgame-analysis-20261006\lifechess_tb_solve.exe `
  playtest-results/endgame-analysis-20261006/tables ALL
```

Extraction of the night archive is separate. It streams one source log at a
time and uses a temporary per-game disk spool to avoid retaining long histories
in RAM. It currently restarts from the beginning if interrupted:

```powershell
python tools/endgame/extract_night_positions.py `
  playtest-results/night-20261006 `
  playtest-results/endgame-analysis-20261006/tables `
  playtest-results/endgame-analysis-20261006
python tools/endgame/audit_named_games.py `
  playtest-results/night-20261006 `
  playtest-results/endgame-analysis-20261006/tables `
  playtest-results/endgame-analysis-20261006/named-games.md
```

The extractor reads each extracted A/B/C gzip log once to EOF (including gzip
CRC checking), emits all action endpoints with ≤4 pieces, preserves the full
logged states and source links, and annotates each model separately. The three
RAR volumes are one multi-volume archive, not three samples; the standalone
night archive is not merged into the older `series-20261006-v2` experiment.
`index_c_continuations.py` records C's 116 shared starting states separately;
the first recorded continuation action is ply 301 and 85 shared boundary
positions contain at most four pieces. These are not new independent samples.

The remaining three-/four-piece classes, pawn/promotion/debt states, and the
claim-aware `MODEL_FIFTY`/`MODEL_HISTORY` graphs are still unsupported.

## Current results

See `playtest-results/endgame-analysis-20261006/coverage-manifest.json`,
`table-statistics.json`, `report.md`, and `named-games.md`. `coverage-manifest.json` records
the intentionally incomplete overall coverage and input/source revisions.
The `.lctb` files are research artifacts and should not be bundled into the APK
or mass-added to Git without approval.
