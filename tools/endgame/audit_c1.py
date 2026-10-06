"""Re-evaluate the archived C-1 full state against exact table evidence."""

import argparse
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from tools.endgame.reference import Model, Position, Wdl
from tools.endgame.table_io import (load_tables, prove_draw_under_optional_claims,
                                    query)


EXPECTED = {"b7": ("w", "K"), "f3": ("w", "B"),
            "h2": ("b", "K"), "a1": ("b", "B")}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("position_json", type=Path)
    parser.add_argument("table_directory", type=Path)
    args = parser.parse_args()
    entries = json.loads(args.position_json.read_text(encoding="utf-8"))
    if len(entries) != 1:
        raise ValueError("expected exactly one archived C-1 state")
    record = entries[0]
    position = Position.from_state(record["state"])
    board = {sq: (color, kind) for sq, color, kind in position.pieces}
    expected_squares = {
        (8 - int(square[1])) * 8 + ord(square[0]) - ord("a"): piece
        for square, piece in EXPECTED.items()}
    if board != expected_squares or position.turn != "w":
        raise ValueError("archived state does not match the specified C-1 position")
    tables = load_tables(args.table_directory)
    base = query(position, tables)
    if base != (Wdl.DRAW, 0):
        raise ValueError(f"unexpected MODEL_UNBOUNDED result for C-1: {base}")
    if record.get("claimFiftyAvailable") is not True:
        raise ValueError("archived C-1 state does not provide an available Claim50")
    if record.get("claimRepetitionAvailable") is not False:
        raise ValueError("archived C-1 repetition-claim evidence changed")
    if record["state"].get("drawOfferBy") is not None:
        raise ValueError("C-1 has a pending draw offer; this proof assumes none")
    fifty = prove_draw_under_optional_claims(position, tables, Model.FIFTY)
    history = prove_draw_under_optional_claims(position, tables, Model.HISTORY)
    if fifty is None or history is None or not fifty.exact or not history.exact:
        raise ValueError("could not transfer the proven draw to the archived claim models")

    # Both claim-aware models only add optional actions whose terminal payoff
    # is DRAW; they do not remove ordinary moves or force a claim. A strategy
    # that holds the base game to DRAW still does so, while an offered claim
    # itself cannot produce a WIN. Therefore the exact value remains DRAW.
    record["unbounded"] = "DRAW"
    record["fiftyModel"] = fifty.wdl.value
    record["historyModel"] = history.wdl.value
    record["modelProof"] = {
        "kind": "optional_draw_only_extension_of_proven_draw",
        "baseModel": Model.UNBOUNDED.value,
        "baseStatus": "DRAW",
        "claimFiftyAvailable": True,
        "claimRepetitionAvailable": False,
        "historyAwareStatus": "DRAW",
        "assumptions": [
            "claims are voluntary and end only in a draw",
            "claim actions do not remove or alter ordinary board moves",
            "there is no active external draw agreement in the archived state",
        ],
    }
    args.position_json.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + "\n",
                                  encoding="utf-8")
    print(json.dumps({key: record[key] for key in
                      ("unbounded", "fiftyModel", "historyModel", "modelProof")},
                     ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
