"""Export Naeglerophaeon's Blockbench rig and its active and drained atlases."""

import base64
import io
import json
from pathlib import Path

from PIL import Image, ImageOps


ASSETS = Path(__file__).resolve().parents[2] / "src/main/resources/assets/hemomancy"
PROJECT = ASSETS / "models/entity/bbmodel/NaeglerophaeonRedesign.bbmodel"


def export():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    groups = {group["uuid"]: group for group in project["groups"]}

    def export_group(node):
        source = groups[node["uuid"]]
        assert source["rotation"] == [0, 0, 0], f"Expected an unrotated rest pivot: {source['name']}"
        return {
            "name": source["name"],
            "origin": source["origin"],
            "children": [export_group(child) if isinstance(child, dict) else child for child in node["children"]],
        }

    elements = [
        {
            **{key: cube[key] for key in ("uuid", "name", "from", "to", "origin", "rotation")},
            "faces": {
                side: {key: face[key] for key in ("uv", "rotation") if key in face}
                for side, face in cube["faces"].items()
                if face["texture"] == 0
            },
        }
        for cube in project["elements"]
    ]
    geometry = dict(resolution=project["resolution"], elements=elements,
                    outliner=[export_group(root) for root in project["outliner"]])
    (ASSETS / "entity_geometry/naeglerophaeon_cube_geometry.json").write_text(
        json.dumps(geometry, separators=(",", ":")) + "\n", encoding="utf-8"
    )

    texture, = project["textures"]
    png = base64.b64decode(texture["source"].partition(",")[2], validate=True)
    atlas = Image.open(io.BytesIO(png)).convert("RGBA")
    assert atlas.size == (project["resolution"]["width"], project["resolution"]["height"])
    destination = ASSETS / "textures/entity"
    (destination / "naeglerophaeon_redesign.png").write_bytes(png)
    drained = ImageOps.colorize(ImageOps.grayscale(atlas), "#241e26", "#a09a96").convert("RGBA")
    drained.putalpha(atlas.getchannel("A"))
    drained.save(destination / "naeglerophaeon_redesign_drained.png", optimize=True)


if __name__ == "__main__":
    export()
