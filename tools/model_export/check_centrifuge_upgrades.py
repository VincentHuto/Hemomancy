"""Validate export parity, native UV density, hierarchy, and rotor clearance."""

import json
import math
import subprocess
import sys
from pathlib import Path

from export_centrifuge_upgrades import ASSETS, MODELS, SOURCE, NAMES
from export_scriptorium_upgrades import texture_size


def references(outliner):
    for node in outliner:
        if isinstance(node, str): yield node
        else: yield from references(node["children"])


def main():
    subprocess.run([sys.executable, str(Path(__file__).with_name(
        "export_centrifuge_upgrades.py")), "--check"], check=True)
    for name in NAMES:
        model = json.loads((MODELS / (name + ".json")).read_text(encoding="utf-8"))
        source = json.loads((SOURCE / (name + ".bbmodel")).read_text(encoding="utf-8"))
        assert 30 <= len(model["elements"]) <= 100, (name, "geometry budget")
        ids = [e["uuid"] for e in source["elements"]]
        refs = list(references(source["outliner"]))
        assert len(refs) == len(ids) == len(set(ids)) and set(refs) == set(ids), name + " outliner"
        assert all(t["source"].startswith("data:image/png;base64,") for t in source["textures"])
        for e in model["elements"]:
            lo, hi = e["from"], e["to"]
            assert all(v * 4 == round(v * 4) for v in lo + hi), e["name"] + " grid"
            assert all(b-a >= .25 for a, b in zip(lo, hi)), e["name"] + " dimensions"
            assert 0 <= lo[0] < hi[0] <= 16 and 0 <= lo[2] < hi[2] <= 16, e["name"] + " footprint"
            assert 0 <= lo[1] < hi[1] <= 22, e["name"] + " height"
            x, y, z = [b-a for a, b in zip(lo, hi)]
            sizes = {"north": (x, y), "south": (x, y), "east": (z, y),
                     "west": (z, y), "up": (x, z), "down": (x, z)}
            for direction, face in e["faces"].items():
                key = face["texture"][1:]
                resource = model["textures"][key].split(":")[1]
                assert (ASSETS / "textures" / (resource + ".png")).is_file(), resource
                u0, v0, u1, v1 = face["uv"]
                assert all(0 <= uv <= 16 for uv in face["uv"]), (e["name"], direction, "UV bounds")
                _, _, tw, th = texture_size(model["textures"][key])
                assert math.isclose(abs(u1-u0)*tw/16, sizes[direction][0])
                assert math.isclose(abs(v1-v0)*th/16, sizes[direction][1])
            # Nothing outside the central bearing may enter the spinning sample annulus.
            if hi[1] > 11 and lo[1] < 17:
                dx = max(lo[0]-8, 8-hi[0], 0)
                dz = max(lo[2]-8, 8-hi[2], 0)
                radius = math.hypot(dx, dz)
                central = max(abs(v-8) for v in (lo[0], hi[0], lo[2], hi[2])) <= 2
                assert radius >= 7.8 or central, (e["name"], "rotor sweep intrusion", radius)
        for e in source["elements"]:
            assert all(v*4 == round(v*4) for field in ("from", "to", "origin") for v in e[field])
        print(f"Validated {name}: {len(ids)} cubes, grid, native UVs, embedded textures, hierarchy, sweep clearance")
    states = json.loads((ASSETS / "blockstates/vial_centrifuge.json").read_text(encoding="utf-8"))["variants"]
    assert len(states) == 12
    for stage, name in enumerate(("vial_centrifuge", *NAMES)):
        for facing, rotation in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            state = states[f"facing={facing},stage={stage}"]
            assert state["model"] == "hemomancy:block/" + name and state.get("y", 0) == rotation
    print("Validated all 12 stage/facing variants; base model and shared moving rotor retained")


if __name__ == "__main__":
    main()
