"""Bake the Bloodlicker Blockbench rig and atlas into native Java assets."""
from base64 import b64decode
import json

from export_choir_keeper_java import ASSETS, ROOT, export_mesh, resolve_outliner
from export_excoriated_java import native_rotation

SOURCE = ROOT / "docs/art/bloodlicker/bloodlicker_redesign.bbmodel"
PROJECT = ASSETS / "models/entity/bbmodel/BloodlickerModel.bbmodel"
TEXTURE = ASSETS / "textures/entity/bloodlicker/model_bloodlicker.png"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/BloodlickerMesh.java"

PARTS = {
    "Bloodlicker": "root",
    "Hunched thorax": "thorax",
    "Engorged blood crop": "crop",
    "Head": "head",
    "Anchor siphon": "siphon",
    "wing": "wing",
    "grasping arm": "arm",
    "forearm": "forearm",
    "middle leg": "middle_leg",
    "hind leg": "hind_leg",
    "hind shin": "hind_shin",
}


def prepare_group(group):
    name = group["name"]
    if name.startswith(("Left ", "Right ")):
        side, part = name.split(" ", 1)
        group["name"] = f"{side.lower()}_{PARTS[part]}"
    else:
        group["name"] = PARTS[name]
    group["rotation"] = native_rotation(group.get("rotation", (0, 0, 0)))
    for child in group["children"]:
        if isinstance(child, dict):
            prepare_group(child)


def main():
    source = SOURCE.read_bytes()
    project = json.loads(source)
    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    if png != SOURCE.with_suffix(".png").read_bytes():
        raise ValueError("Blockbench and standalone atlases differ")
    resolve_outliner(project)
    root, = project["outliner"]
    prepare_group(root)
    for cube in project["elements"]:
        cube["rotation"] = native_rotation(cube.get("rotation", (0, 0, 0)))
    JAVA.write_text(export_mesh(
        project,
        mesh_name="BloodlickerMesh",
        source="tools/model_export/export_bloodlicker_java.py from bloodlicker_redesign.bbmodel",
    ), encoding="utf-8")
    PROJECT.write_bytes(source)
    TEXTURE.write_bytes(png)


if __name__ == "__main__":
    main()

