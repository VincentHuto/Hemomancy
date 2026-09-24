"""Reject overlapping, coplanar faces in the Mortarbound's collapsed form."""
import json
from pathlib import Path

MODEL = Path(__file__).resolve().parents[2] / "src/main/resources/assets/hemomancy/models/entity/bbmodel/MortarboundModel.bbmodel"
elements = [e for e in json.loads(MODEL.read_text(encoding="utf-8"))["elements"]
            if e["name"].startswith("wall_growth_")]

def overlap(a, b, axis):
    other = [i for i in range(3) if i != axis]
    return all(min(a["to"][i], b["to"][i]) > max(a["from"][i], b["from"][i]) for i in other)

for index, first in enumerate(elements):
    for second in elements[index + 1:]:
        for axis in range(3):
            for face in ("from", "to"):
                if first[face][axis] == second[face][axis] and overlap(first, second, axis):
                    raise SystemExit(f"Coplanar overlap: {first['name']} and {second['name']} on {face}[{axis}]")

print(f"Checked {len(elements)} collapsed-form cuboids: no overlapping coplanar faces")
