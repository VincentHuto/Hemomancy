"""Bake the Scarlet Serpent rig and its three biome atlases from the Blockbench project."""
from base64 import b64decode
from io import BytesIO
import json
import math

from PIL import Image

from export_choir_keeper_java import ASSETS, ROOT, export_mesh

PROJECT = ASSETS / "models/entity/bbmodel/ScarletSerpentModel.bbmodel"
TEXTURES = ASSETS / "textures/entity/scarlet_serpent"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/mob/animal/ScarletSerpentMesh.java"


def align_body_joints(body, elements):
    """Put each link's heading on its bone so animation can unwind the authored coil."""
    parent_yaw = 0
    parent_authored = parent_aligned = body["origin"]
    joint, = body["children"]
    while joint:
        cube, = (node for node in joint["children"] if isinstance(node, str))
        element = elements[cube]
        authored = joint["origin"]
        x, y, z = (coord - base for coord, base in zip(authored, parent_authored))
        angle = math.radians(parent_yaw)
        aligned = [
            round(parent_aligned[0] + x * math.cos(angle) - z * math.sin(angle), 5),
            parent_aligned[1] + y,
            round(parent_aligned[2] + x * math.sin(angle) + z * math.cos(angle), 5),
        ]
        yaw = element["rotation"][1]
        joint["origin"] = aligned
        joint["rotation"] = [0, (yaw - parent_yaw + 180) % 360 - 180, 0]
        element["rotation"] = [0, 0, 0]
        element["origin"] = aligned
        for key in ("from", "to"):
            element[key] = [new + coord - old for new, coord, old in zip(aligned, element[key], authored)]
        parent_yaw, parent_authored, parent_aligned = yaw, authored, aligned
        joint = next((node for node in joint["children"] if isinstance(node, dict)), None)


def variant(image, swamp):
    result = image.copy()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, a = image.getpixel((x, y))
            if not a:
                continue
            if r < 80:
                color = (r + 4, g + 1, max(0, b - 10)) if swamp else (r + 19, g + 3, b)
            elif r > 1.7 * g and r > 1.65 * b:
                color = (round(r * .66), round(r * .32), round(r * .22)) if swamp else (
                    min(255, r + 12), round(g * .79), round(b * .86))
            elif r > g > b and g > 1.45 * b:
                color = (round(r * .83), round(g * .84), round(b * .86)) if swamp else (
                    r, max(0, g - 8), max(0, b - 4))
            else:
                color = (r, g, b)
            result.putpixel((x, y), (*color, a))
    return result


def main():
    project = json.loads(PROJECT.read_text(encoding="utf-8"))
    root, = project["outliner"]
    body = next(node for node in root["children"] if node["name"] == "body_chain")
    align_body_joints(body, {cube["uuid"]: cube for cube in project["elements"]})
    JAVA.write_text(export_mesh(
        project,
        mesh_name="ScarletSerpentMesh",
        source="tools/model_export/export_scarlet_serpent_java.py",
    ), encoding="utf-8")

    texture, = project["textures"]
    png = b64decode(texture["source"].split(",", 1)[1], validate=True)
    (TEXTURES / "model_scarlet_serpent.png").write_bytes(png)
    with Image.open(BytesIO(png)) as atlas:
        variant(atlas, swamp=False).save(TEXTURES / "model_scarlet_serpent_desert.png")
        variant(atlas, swamp=True).save(TEXTURES / "model_scarlet_serpent_swamp.png")


if __name__ == "__main__":
    main()
