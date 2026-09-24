"""Verify the editable Mortarbound model has the intended large humanoid silhouette."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODEL = ROOT / "src/main/resources/assets/hemomancy/models/entity/bbmodel/MortarboundModel.bbmodel"
model = json.loads(MODEL.read_text(encoding="utf-8"))
parts = {part["name"] for part in model["outliner"]}
required = {"torso", "head", "cap", "left_arm", "right_arm", "wall_growth"}
if missing := required - parts:
    raise SystemExit(f"Missing distinct humanoid parts: {sorted(missing)}")

elements = model["elements"]
body = [e for e in elements if not e["name"].startswith("wall_growth_")]
width = max(e["to"][0] for e in body) - min(e["from"][0] for e in body)
height = max(e["to"][1] for e in body) - min(e["from"][1] for e in body)
if width < 32 or height < 48:
    raise SystemExit(f"Mortarbound needs a roughly 2x3 block profile; got {width}x{height} model units")

atlas_width = model["resolution"]["width"]
atlas_height = model["resolution"]["height"]
for element in elements:
    for face in element["faces"].values():
        u0, v0, u1, v1 = face["uv"]
        if min(u0, u1) < 0 or max(u0, u1) > atlas_width or min(v0, v1) < 0 or max(v0, v1) > atlas_height:
            raise SystemExit(f"UV outside {atlas_width}x{atlas_height} atlas: {element['name']}")

print(f"Mortarbound humanoid profile: {width}x{height} model units with separate head and arms")
