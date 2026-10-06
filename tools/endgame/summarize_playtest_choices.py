"""Summarize observed actions only where the parent is table-covered.

Reads the compressed episode stream line-by-line and never interprets a
multi-piece position as an endgame-table result.
"""

import argparse
import gzip
import json
import re
from collections import Counter, defaultdict
from pathlib import Path


PARENT = re.compile(
    rb'"role":"before".*?"MODEL_UNBOUNDED":\{"status":"(WIN|DRAW|LOSS)","dtlk":([0-9]+)',
    re.DOTALL)
CHOSEN = re.compile(
    rb'"selectedActionValue":\{"status":"(WIN|DRAW|LOSS)","dtlk":([0-9]+)')
PROFILE = re.compile(rb'"profile":"([ABC])"')
GAME = re.compile(rb'"gameId":([0-9]+)')
PLY = re.compile(rb'"ply":([0-9]+)')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("episode_file", type=Path)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()
    counts = Counter()
    by_profile = defaultdict(Counter)
    by_parent = Counter()
    bytes_read = 0
    with gzip.open(args.episode_file, "rb") as stream:
        for line_no, line in enumerate(stream, 1):
            bytes_read += len(line)
            parent = PARENT.search(line)
            chosen = CHOSEN.search(line)
            if not parent or not chosen:
                continue
            p_wdl, p_dtlk = parent.group(1).decode("ascii"), int(parent.group(2))
            c_wdl, c_dtlk = chosen.group(1).decode("ascii"), int(chosen.group(2))
            profile_match = PROFILE.search(line)
            profile = profile_match.group(1).decode("ascii") if profile_match else "?"
            counts["coveredObservedMoves"] += 1
            by_profile[profile]["coveredObservedMoves"] += 1
            by_parent[p_wdl] += 1
            if p_wdl == "WIN" and c_wdl != "WIN":
                key = "WIN_TO_" + c_wdl
                counts[key] += 1
                by_profile[profile][key] += 1
            elif p_wdl == "DRAW" and c_wdl == "LOSS":
                counts["DRAW_TO_LOSS"] += 1
                by_profile[profile]["DRAW_TO_LOSS"] += 1
            elif p_wdl == c_wdl == "WIN" and c_dtlk > p_dtlk:
                counts["WIN_PRESERVED_BUT_SLOWER"] += 1
                by_profile[profile]["WIN_PRESERVED_BUT_SLOWER"] += 1
            elif p_wdl == c_wdl == "LOSS" and c_dtlk < p_dtlk:
                counts["LOSS_PRESERVED_BUT_SHORTER"] += 1
                by_profile[profile]["LOSS_PRESERVED_BUT_SHORTER"] += 1
            if line_no % 5000 == 0:
                print(f"episode rows read={line_no:,} uncompressed={bytes_read:,} bytes", flush=True)
    result = {"source": str(args.episode_file),
              "model": "MODEL_UNBOUNDED",
              "perspective": "mover at parent; selected child outcome normalized back to mover",
              "counts": dict(counts), "parentWdlCounts": dict(by_parent),
              "byProfile": {k: dict(v) for k, v in by_profile.items()},
              "meaning": {
                  "WIN_TO_DRAW_OR_LOSS": "observed move abandoned a table-proved win",
                  "DRAW_TO_LOSS": "observed move changed table-proved draw to loss",
                  "WIN_PRESERVED_BUT_SLOWER": "same WDL but selected line takes more plies than DTLK",
                  "LOSS_PRESERVED_BUT_SHORTER": "same loss but failed to maximize DTLK",
                  "notMeasured": "MODEL_FIFTY/MODEL_HISTORY decisions; positions outside computed tables; alternate branches"}}
    args.output_file.parent.mkdir(parents=True, exist_ok=True)
    args.output_file.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n",
                                encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
