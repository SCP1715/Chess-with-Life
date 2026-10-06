"""Record C-profile shared boundary states separately from independent data."""

import argparse
import gzip
import hashlib
import json
from pathlib import Path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("c_games_directory", type=Path)
    parser.add_argument("output_file", type=Path)
    args = parser.parse_args()
    rows = []
    for path in sorted(args.c_games_directory.glob("game-*.jsonl.gz")):
        start = first_action = None
        with gzip.open(path, "rt", encoding="utf-8") as stream:
            for line in stream:
                row = json.loads(line)
                if row.get("record") == "START":
                    start = row
                elif row.get("record") == "ACTION":
                    first_action = row
                    break
        if start is None or first_action is None:
            raise ValueError(f"missing START or continuation action in {path}")
        state = first_action["before"]
        state_json = json.dumps(state, ensure_ascii=False, sort_keys=True,
                                separators=(",", ":"))
        rows.append({"profile": "C", "gameId": start.get("gameId"),
                     "parentGameId": start.get("parentGameId"),
                     "parentProfileGameId": start.get("parentProfileGameId"),
                     "sourceFile": str(path), "firstRecordedPly": first_action.get("ply"),
                     "firstActionNumber": first_action.get("actionNumber"),
                     "boundaryStatePieceCount": sum(cell != "." for cell in state["board"]),
                     "boundaryStateSha256": hashlib.sha256(state_json.encode("utf-8")).hexdigest(),
                     "sameAsCStartState": state == start.get("initialState"),
                     "independentObservation": False,
                     "reason": "C profile resumes the parent A/B game at ply 300; this is the shared boundary, not a new game state"})
    args.output_file.parent.mkdir(parents=True, exist_ok=True)
    args.output_file.write_text("\n".join(json.dumps(r, ensure_ascii=False)
                                             for r in rows) + "\n", encoding="utf-8")
    print(f"indexed {len(rows)} C continuation boundaries; "
          f"<=4-piece boundaries={sum(r['boundaryStatePieceCount'] <= 4 for r in rows)}")


if __name__ == "__main__":
    main()
