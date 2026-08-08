"""Create concept-oriented Blockbench variants without overwriting authored exports.

The existing Vesper exports are kept intact. These variants add named, editable
concept groups for the throne/mount, absorbed Morphling anatomy, eight tendency
sigils, and weapon-state callouts.
"""

from __future__ import annotations

import copy
import json
import math
import uuid
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
EXPORT_DIR = ROOT / "tools" / "model_export"
NAMESPACE = uuid.UUID("f0d86825-b6ea-4b62-bb7d-ff5b1c10f07c")


def uid(key: str) -> str:
    return str(uuid.uuid5(NAMESPACE, key))


def face_uv(from_, to, offset=(0, 0)):
    x0, y0 = offset
    width = abs(to[0] - from_[0])
    height = abs(to[1] - from_[1])
    depth = abs(to[2] - from_[2])
    return {
        "north": {"uv": [x0 + depth, y0 + depth, x0 + depth + width, y0 + depth + height], "texture": 0},
        "east": {"uv": [x0, y0 + depth, x0 + depth, y0 + depth + height], "texture": 0},
        "south": {"uv": [x0 + depth + width + depth, y0 + depth, x0 + depth + width * 2 + depth, y0 + depth + height], "texture": 0},
        "west": {"uv": [x0 + depth + width, y0 + depth, x0 + depth + width + depth, y0 + depth + height], "texture": 0},
        "up": {"uv": [x0 + depth + width, y0, x0 + depth, y0 + depth], "texture": 0},
        "down": {"uv": [x0 + depth + width * 2, y0 + depth, x0 + depth + width, y0 + depth * 2], "texture": 0},
    }


def cube(model_name, name, from_, to, origin, *, color=1, rotation=None, uv=(0, 0)):
    element = {
        "name": name,
        "rescale": False,
        "locked": False,
        "from": list(from_),
        "to": list(to),
        "autouv": 0,
        "color": color,
        "origin": list(origin),
        "uv_offset": list(uv),
        "faces": face_uv(from_, to, uv),
        "type": "cube",
        "uuid": uid(f"{model_name}:element:{name}"),
    }
    if rotation is not None:
        element["rotation"] = list(rotation)
    return element


def group(model_name, name, origin, children, *, color=1, rotation=None):
    node = {
        "name": name,
        "origin": list(origin),
        "color": color,
        "uuid": uid(f"{model_name}:group:{name}"),
        "export": True,
        "mirror_uv": False,
        "isOpen": True,
        "locked": False,
        "visibility": True,
        "autouv": 0,
        "children": children,
    }
    if rotation is not None:
        node["rotation"] = list(rotation)
    return node


def add_elements(model, elements):
    model["elements"].extend(elements)


def group_ref(model_name, group_name):
    return uid(f"{model_name}:group:{group_name}")


def find_group(nodes, name):
    for node in nodes:
        if isinstance(node, dict):
            if node.get("name") == name:
                return node
            found = find_group(node.get("children", []), name)
            if found is not None:
                return found
    return None


def apply_common_metadata(model, model_name):
    model["name"] = model_name
    model["model_identifier"] = model_name
    model["variable_placeholders"] = ""
    model["variable_placeholder_buttons"] = []
    model["timeline_setups"] = []


def build_crowned(source):
    model_name = "VesperTheCrownedRefusalConceptModel"
    model = copy.deepcopy(source)
    apply_common_metadata(model, model_name)

    upper = find_group(model["outliner"], "upperBody")
    lower = find_group(model["outliner"], "lowerBody")
    if upper is None or lower is None:
        raise ValueError("Crowned Refusal source must contain upperBody and lowerBody groups")

    throne_elements = [
        cube(model_name, "throne_platform", (-10, 23, -1), (10, 28, 9), (0, 25, 4), color=2, uv=(320, 0)),
        cube(model_name, "throne_back_core", (-8, 27, 5), (8, 48, 9), (0, 30, 7), color=3, uv=(352, 0)),
        cube(model_name, "throne_left_pillar", (-11, 27, 2), (-7, 50, 7), (-9, 30, 4), color=4, rotation=(0, 0, -8), uv=(384, 0)),
        cube(model_name, "throne_right_pillar", (7, 27, 2), (11, 50, 7), (9, 30, 4), color=5, rotation=(0, 0, 8), uv=(416, 0)),
        cube(model_name, "throne_crown_arch", (-12, 45, 3), (12, 50, 8), (0, 46, 5), color=6, uv=(448, 0)),
        cube(model_name, "throne_dorsal_fuse", (-7, 20, 6), (7, 32, 14), (0, 24, 8), color=7, uv=(320, 32)),
    ]
    throne_group = group(
        model_name,
        "conceptThrone_FusedDorsalSeat",
        (0, 25, 5),
        [element["uuid"] for element in throne_elements],
        color=2,
    )

    mount_elements = [
        cube(model_name, "mount_dorsal_carapace", (-14, 5, -4), (14, 21, 8), (0, 12, 2), color=3, uv=(320, 64)),
        cube(model_name, "mount_abdomen_plate", (-12, 1, 4), (12, 9, 19), (0, 6, 10), color=4, rotation=(8, 0, 0), uv=(352, 64)),
        cube(model_name, "mount_left_foreclaw", (-23, 6, -3), (-13, 14, 8), (-18, 10, 2), rotation=(0, 0, -18), color=5, uv=(384, 64)),
        cube(model_name, "mount_right_foreclaw", (13, 6, -3), (23, 14, 8), (18, 10, 2), rotation=(0, 0, 18), color=6, uv=(416, 64)),
        cube(model_name, "mount_tail_root", (-4, 11, 15), (4, 18, 27), (0, 13, 18), rotation=(-24, 0, 0), color=7, uv=(448, 64)),
        cube(model_name, "mount_tail_stinger", (-3, 26, 25), (3, 39, 31), (0, 26, 27), rotation=(-18, 0, 0), color=1, uv=(480, 64)),
    ]
    mount_group = group(
        model_name,
        "conceptMount_ApexScorpionMorphling",
        (0, 10, 6),
        [element["uuid"] for element in mount_elements],
        color=3,
    )

    add_elements(model, throne_elements + mount_elements)
    upper.setdefault("children", []).append(throne_group)
    lower.setdefault("children", []).append(mount_group)
    return model


def sigil_group(model_name, index, label, x, y, z, active=False):
    suffix = "active" if active else "dormant"
    name = f"sigil_{index + 1:02d}_{label}_{suffix}"
    color = 7 if active else (index % 8)
    elements = [
        cube(model_name, f"{name}_top", (x - 1, y + 4, z), (x + 1, y + 10, z + 1), (x, y + 7, z), color=color, uv=(index * 16, 192)),
        cube(model_name, f"{name}_bottom", (x - 1, y - 10, z), (x + 1, y - 4, z + 1), (x, y - 7, z), color=color, uv=(index * 16, 208)),
        cube(model_name, f"{name}_left", (x - 10, y - 1, z), (x - 4, y + 1, z + 1), (x - 7, y, z), color=color, uv=(index * 16, 224)),
        cube(model_name, f"{name}_right", (x + 4, y - 1, z), (x + 10, y + 1, z + 1), (x + 7, y, z), color=color, uv=(index * 16, 240)),
        cube(model_name, f"{name}_rune_a", (x - 1, y - 4, z - 1), (x + 1, y + 4, z), (x, y, z - 1), color=color, rotation=(0, 0, 45), uv=(index * 16, 256)),
        cube(model_name, f"{name}_rune_b", (x - 1, y - 4, z - 1), (x + 1, y + 4, z), (x, y, z - 1), color=color, rotation=(0, 0, -45), uv=(index * 16, 272)),
    ]
    return elements, group(model_name, name, (x, y, z), [element["uuid"] for element in elements], color=color)


def build_evening(source):
    model_name = "VesperTheEveningStarConceptModel"
    model = copy.deepcopy(source)
    apply_common_metadata(model, model_name)

    upper = find_group(model["outliner"], "upperBody")
    if upper is None:
        raise ValueError("Evening Star source must contain upperBody group")

    absorbed_elements = [
        cube(model_name, "absorbed_carapace_core", (-10, 14, 5), (10, 30, 10), (0, 22, 7), color=2, uv=(320, 0)),
        cube(model_name, "absorbed_spine_left", (-16, 19, 7), (-9, 38, 11), (-12, 28, 9), rotation=(0, 0, -20), color=3, uv=(352, 0)),
        cube(model_name, "absorbed_spine_right", (9, 19, 7), (16, 38, 11), (12, 28, 9), rotation=(0, 0, 20), color=4, uv=(384, 0)),
        cube(model_name, "absorbed_tail_mantle", (-4, 11, 10), (4, 35, 16), (0, 16, 12), rotation=(-28, 0, 0), color=5, uv=(416, 0)),
        cube(model_name, "absorbed_hook_left", (-21, 8, 4), (-14, 16, 9), (-18, 12, 6), rotation=(0, 0, -26), color=6, uv=(448, 0)),
        cube(model_name, "absorbed_hook_right", (14, 8, 4), (21, 16, 9), (18, 12, 6), rotation=(0, 0, 26), color=7, uv=(480, 0)),
    ]
    absorbed_group = group(model_name, "conceptAbsorbedMorphling_ExoskeletalMantle", (0, 22, 8), [e["uuid"] for e in absorbed_elements], color=2)

    sigil_specs = [
        ("animus", 0, 27, 16, True),
        ("flammeus", 18, 19, 16, False),
        ("ductilis", 23, 2, 16, False),
        ("lux", 18, -15, 16, False),
        ("mortem", 0, -23, 16, False),
        ("congeatio", -18, -15, 16, False),
        ("ferric", -23, 2, 16, False),
        ("tenebris", -18, 19, 16, False),
    ]
    sigil_elements = []
    sigil_nodes = []
    for index, (label, x, y, z, active) in enumerate(sigil_specs):
        elements, node = sigil_group(model_name, index, label, x, y, z, active)
        sigil_elements.extend(elements)
        sigil_nodes.append(node)
    sigils_group = group(model_name, "conceptTendencySigils_EightfoldHalo", (0, 2, 16), sigil_nodes, color=7)

    weapon_specs = [
        ("sickle", (-31, 20, 5), ("hooked blade", 2)),
        ("flail", (-31, 6, 5), ("segmented blood flail", 3)),
        ("spear", (31, 20, 5), ("living spear", 4)),
        ("axe", (31, 6, 5), ("living axe", 5)),
    ]
    weapon_elements = []
    weapon_nodes = []
    for label, (x, y, z), (_, color) in weapon_specs:
        parts = [
            cube(model_name, f"weapon_{label}_shaft", (x - 1, y - 6, z), (x + 1, y + 6, z + 1), (x, y, z), color=color, uv=(320, 320)),
            cube(model_name, f"weapon_{label}_head", (x - 6, y + 4, z - 1), (x + 6, y + 9, z + 1), (x, y + 6, z), color=color, rotation=(0, 0, 18 if x < 0 else -18), uv=(352, 320)),
        ]
        weapon_elements.extend(parts)
        weapon_nodes.append(group(model_name, f"weaponCallout_{label}", (x, y, z), [p["uuid"] for p in parts], color=color))
    weapon_group = group(model_name, "conceptWeaponMorphCallouts", (0, 12, 5), weapon_nodes, color=4)

    add_elements(model, absorbed_elements + sigil_elements + weapon_elements)
    upper.setdefault("children", []).extend([absorbed_group, sigils_group, weapon_group])
    return model


def write_model(source_path, target_path, builder):
    source = json.loads(source_path.read_text(encoding="utf-8"))
    target = builder(source)
    target_path.write_text(json.dumps(target, indent=2) + "\n", encoding="utf-8")


def main():
    crowned_source = EXPORT_DIR / "VesperTheCrownedRefusalModel.bbmodel"
    evening_source = EXPORT_DIR / "VesperTheEveningStarModel.bbmodel"
    write_model(crowned_source, EXPORT_DIR / "VesperTheCrownedRefusalConceptModel.bbmodel", build_crowned)
    write_model(evening_source, EXPORT_DIR / "VesperTheEveningStarConceptModel.bbmodel", build_evening)
    print("Created VesperTheCrownedRefusalConceptModel.bbmodel")
    print("Created VesperTheEveningStarConceptModel.bbmodel")


if __name__ == "__main__":
    main()
