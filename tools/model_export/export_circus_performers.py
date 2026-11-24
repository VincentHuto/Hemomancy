"""Bake the physical Crimson Troupe's editable Blockbench projects into Java rigs and PNGs."""

import argparse
import base64
import io
import json
import re

from PIL import Image

from export_choir_keeper_java import ASSETS, ROOT, export_mesh
from export_excoriated_java import native_rotation

PROJECTS = ASSETS / "models/entity/bbmodel/npc/harbinger/circus"
TEXTURES = ASSETS / "textures/entity/npc/harbinger/circus"
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc"
ROLES = {"Acrobat": "acrobat", "FireEater": "fire_eater",
         "KnifeThrower": "knife_thrower", "StiltWalker": "stilt_walker"}

# A second dye lot for the same garments. Skin, hair, bone and stage props keep their colors.
DYES = dict(zip(
    ("381924", "59232c", "832d39", "b3444a", "d46b63", "281923", "44232c",
     "602b37", "873b45", "ab5554", "33282e", "48363c", "61454b", "845b5c", "a27972"),
    ("351922", "54282e", "79313b", "a6494b", "c77165", "291c29", "40273a",
     "553447", "754557", "9b6166", "34272b", "4c383a", "654c4b", "89645d", "a68474"),
))
DYE_PIXELS = {bytes.fromhex(a): bytes.fromhex(b) for a, b in DYES.items()}


def named(group, name):
    return next(child for child in group["children"]
                if isinstance(child, dict) and child["name"] == name)


def separate(group, name, origin, cubes, match):
    children = [child for child in group["children"] if isinstance(child, str)
                and match(cubes[child]["name"])]
    assert children, name
    group["children"] = [child for child in group["children"] if child not in children]
    part = {"name": name, "origin": origin, "children": children}
    group["children"].append(part)
    return part


def prepare(project, role):
    """Expose the humanoid API's seven siblings while keeping the authored limb chains."""
    root, = project["outliner"]
    waist = named(root, "Waist")
    chest = named(waist, "Chest")
    head = named(chest, "Head")
    arms = [named(chest, side + " shoulder") for side in ("Right", "Left")]
    legs = [named(waist, side + " hip") for side in ("Right", "Left")]
    waist["children"] = [child for child in waist["children"] if child not in legs]
    chest["children"] = [child for child in chest["children"] if child not in [head, *arms]]
    root["children"] = [waist, head, *arms, *legs,
                        {"name": "hat", "origin": head["origin"], "children": []}]
    cubes = {cube["uuid"]: cube for cube in project["elements"]}
    root["name"], waist["name"], head["name"] = "root", "body", "head"
    # HumanoidModel renders the root's children directly, so each starts in
    # Minecraft's 24-pixel ground frame instead of inheriting a root offset.
    root["origin"] = [0, 24, 0]
    for side, arm, leg in zip(("right", "left"), arms, legs):
        arm["name"] = side + "_arm"
        leg["name"] = side + "_leg"
        named(leg, side.title() + " knee")["name"] = side + "_calf"

    if role == "Acrobat":
        separate(waist, "sash", [3.5, 12, -2], cubes,
                 lambda label: label in ("Loose sash", "Split rigging panel"))
        for side, x in (("right", 2), ("left", -2)):
            ribbon = next(cube for cube in cubes.values()
                          if cube["name"] == "Tail of head ribbon" and cube["origin"][0] == x)
            head["children"].remove(ribbon["uuid"])
            head["children"].append({"name": side + "_ribbon", "origin": ribbon["origin"],
                                     "children": [ribbon["uuid"]]})
    elif role == "FireEater":
        separate(chest, "gorget", [0, 21, -4], cubes,
                 lambda label: label in ("Hematic iron gorget", "Riveted gorget tab"))
        separate(head, "cowl_tail", [-2, 30, 3], cubes, lambda label: label == "Cowl scarf tail")
    elif role == "KnifeThrower":
        separate(head, "side_cowl", [2, 29, 0], cubes, lambda label: label == "Swept side hood")
        left_hand = named(named(arms[1], "Left elbow"), "Left hand")
        fan = named(left_hand, "Throwing daggers")
        fan["name"] = "knife_fan"
        # Copy the middle prop at its own grip for the three free-flying knives.
        selected = [cubes[child] for child in fan["children"] if cubes[child]["name"].startswith("Dagger 2 ")]
        for i in range(3):
            prop = {"name": f"juggle_knife_{i}", "origin": selected[0]["origin"], "children": []}
            for j, source in enumerate(selected):
                cube = {**source, "uuid": f"juggle_{i}_{j}", "rotation": [0, 0, 0]}
                project["elements"].append(cube)
                prop["children"].append(cube["uuid"])
            root["children"].append(prop)
    else:
        hat = named(head, "Top Hat")
        separate(hat, "feather", [3, 53, 2], cubes, lambda label:
                 label in ("Feather quill", "Feather vane", "Bone feather socket"))
        for side, x in (("right", 2), ("left", -2)):
            panels = [cube for cube in cubes.values() if cube["name"] in
                      ("Striped coat tail", "Scarlet tail lining") and cube["origin"][0] == x]
            for panel in panels:
                waist["children"].remove(panel["uuid"])
            waist["children"].append({"name": side + "_tail", "origin": [x, 34, 1],
                                      "children": [panel["uuid"] for panel in panels]})

    def reflect_x(node):
        x, y, z = node["origin"]
        node["origin"] = [-x, y, z]
        if "rotation" in node:
            # Convert the authored Euler order, then express the X-reflected rotation in exporter coordinates.
            pitch, yaw, roll = native_rotation(node["rotation"])
            node["rotation"] = [-pitch, -yaw, roll]

    def visit(group):
        reflect_x(group)
        group["name"] = re.sub(r"[^a-z0-9]+", "_", group["name"].lower())
        for child in group["children"]:
            if isinstance(child, dict):
                visit(child)

    # The authoring rig labels the viewer's right as Right. Convert that side to
    # Minecraft's anatomical right together with its props, face paint and pivots.
    visit(root)
    for cube in project["elements"]:
        reflect_x(cube)
        x0, x1 = cube["from"][0], cube["to"][0]
        cube["from"] = [-x1, *cube["from"][1:]]
        cube["to"] = [-x0, *cube["to"][1:]]
        faces = cube["faces"]
        faces["east"], faces["west"] = faces["west"], faces["east"]
        cube["faces"] = {face: {**data, "uv": [u1, v0, u0, v1]}
                         for face, data in faces.items() for u0, v0, u1, v1 in [data["uv"]]}


def assets(role, texture):
    project = json.loads((PROJECTS / f"Circus{role}Model.bbmodel").read_text(encoding="utf-8"))
    primary = base64.b64decode(project["textures"][0]["source"].split(",", 1)[1], validate=True)
    atlas = Image.open(io.BytesIO(primary)).convert("RGBA")
    assert atlas.size == (128, 128)
    pixels = bytearray(atlas.tobytes())
    for offset in range(0, len(pixels), 4):
        color = bytes(pixels[offset:offset + 3])
        pixels[offset:offset + 3] = DYE_PIXELS.get(color, color)
    dyed = Image.frombytes("RGBA", atlas.size, bytes(pixels))
    alternate = io.BytesIO()
    dyed.save(alternate, format="PNG")
    prepare(project, role)
    mesh = export_mesh(project, package="com.vincenthuto.hemomancy.client.model.entity.npc",
                       mesh_name=f"Circus{role}Mesh",
                       source=f"tools/model_export/export_circus_performers.py from Circus{role}Model.bbmodel")
    return {JAVA / f"Circus{role}Mesh.java": mesh.encode(),
            TEXTURES / f"{texture}_0.png": primary,
            TEXTURES / f"{texture}_1.png": alternate.getvalue()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Check that runtime artifacts match the editable assets.")
    args = parser.parse_args()
    for role, texture in ROLES.items():
        for path, data in assets(role, texture).items():
            if args.check:
                if not path.is_file() or path.read_bytes() != data:
                    raise SystemExit(f"Regenerate {path.relative_to(ROOT)}")
            else:
                path.write_bytes(data)
    print("Checked all four Crimson Troupe meshes and atlases." if args.check else
          "Exported all four Crimson Troupe meshes and atlases.")


if __name__ == "__main__":
    main()

