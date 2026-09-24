"""Check that the emerged lower body grows into the supporting wall."""
import json
from pathlib import Path

model_path = Path(__file__).resolve().parents[2] / "src/main/resources/assets/hemomancy/models/entity/bbmodel/MortarboundModel.bbmodel"
model = json.loads(model_path.read_text(encoding="utf-8"))
parts = {part["name"] for part in model["outliner"]}
required = {"tail", "wall_root", "wall_anchor"}
if missing := required - parts:
    raise SystemExit(f"Missing wall attachment parts: {sorted(missing)}")

def cuboids(part):
    return [element for element in model["elements"] if element["name"].startswith(part + "_")]

def intersects(a, b):
    return all(min(a["to"][axis], b["to"][axis]) > max(a["from"][axis], b["from"][axis])
               for axis in range(3))

tail = cuboids("tail")
root = cuboids("wall_root")
anchor = cuboids("wall_anchor")
if not tail or not root or not anchor:
    raise SystemExit("Tail, wall root, and wall anchor each need geometry")
if not any(intersects(a, b) for a in tail for b in root):
    raise SystemExit("The lower body does not overlap its wall root")
if not any(intersects(a, b) for a in root for b in anchor):
    raise SystemExit("The wall root does not overlap its wall anchor")
if not any(a["from"][2] < 8 < a["to"][2] for a in root):
    raise SystemExit("The root must cross the wall face eight model units behind the mob")
if not any(a["from"][2] < 8 < a["to"][2] for a in anchor):
    raise SystemExit("The anchor must sink into the wall")

print("Mortarbound lower body overlaps a root and anchor that enter the supporting wall")
