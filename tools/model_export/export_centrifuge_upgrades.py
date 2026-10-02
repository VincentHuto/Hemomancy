"""Export the two Centrifuge bodies; the existing renderer owns the moving rotor.

All authored geometry uses the quarter-unit grid. Materials and Blockbench
conversion reuse the station exporter instead of adding a texture pipeline.
"""

import argparse
import copy
import json
import uuid

from export_scriptorium_upgrades import ASSETS, MODELS, SOURCE, blockbench, texture_size


TEXTURES = {
    "iron": "hemomancy:block/hematic_iron_block",
    "pillar": "hemomancy:block/hematic_iron_pillar",
    "cap": "hemomancy:block/hematic_iron_pillar_top",
    "dark": "hemomancy:block/bold_chiseled_hematic_iron_block",
    "bone": "hemomancy:block/calcified_hyphae",
    "wood": "hemomancy:block/blood_wood_planks",
    "glass": "hemomancy:block/vial_glass",
    "blood": "hemomancy:block/sanguine_glass",
    "particle": "hemomancy:block/hematic_iron_block",
}
NAMES = ("vial_centrifuge_calibrated", "vial_centrifuge_fractionating")


def cube(name, start, end, material="iron"):
    x, y, z = [b - a for a, b in zip(start, end)]
    dimensions = {"north": (x, y), "south": (x, y), "east": (z, y),
                  "west": (z, y), "up": (x, z), "down": (x, z)}
    faces = {}
    for side, (width, height) in dimensions.items():
        key = "cap" if material == "pillar" and side in ("up", "down") else material
        _, _, tw, th = texture_size(TEXTURES[key])
        # Crop at native material density; never stretch a tiny bolt over a tile.
        faces[side] = {"texture": "#" + key, "uv": [0, 0, width * 16 / tw, height * 16 / th]}
    return {"name": name, "from": start, "to": end, "faces": faces}


def calibrated():
    parts = []
    def add(*args):
        parts.append(cube(*args))

    # Low octagonal footing retains the original central spindle and open rotor.
    add("foot_middle", [3, 0, 1], [13, 1.5, 15], "dark")
    add("foot_left", [1, 0, 3], [3, 1.5, 13], "dark")
    add("foot_right", [13, 0, 3], [15, 1.5, 13], "dark")
    add("foot_top", [3, 1.5, 3], [13, 2, 13])
    add("spindle_socket", [5, 2, 5], [11, 3, 11], "dark")
    add("spindle_column", [6.5, 3, 6.5], [9.5, 10.5, 9.5], "pillar")
    add("upper_bearing", [6, 10.5, 6], [10, 12, 10], "cap")
    for x in (3, 11.5):
        for z in (3, 11.5):
            add("brace_shoe", [x-.25, 1.5, z-.25], [x+1.75, 2.5, z+1.75])
            add("bearing_brace", [x, 2.5, z], [x+1.5, 7, z+1.5], "wood")
            add("brace_collar", [x-.25, 6.25, z-.25], [x+1.75, 7.25, z+1.75])
    # A square bearing cage below the sample sweep, with visible indexed edges.
    for z in (4.5, 10.5):
        add("bearing_rail", [4.5, 7.25, z], [11.5, 8, z+1])
    for x in (4.5, 10.5):
        add("bearing_crossrail", [x, 7.25, 5.5], [x+1, 8, 10.5])
    for x in (5, 6.5, 8, 9.5, 11):
        add("calibration_tick", [x, 8, 4.5], [x+.25, 8.5, 5], "bone")
    # Broad stepped dial reads from normal viewing distance, including in items.
    add("governor_mount", [5, 2.5, 2], [11, 8.5, 4], "dark")
    add("governor_face", [5.5, 3.5, 1.5], [10.5, 7.5, 2], "bone")
    add("governor_face_top", [6.5, 7.5, 1.5], [9.5, 8.5, 2], "bone")
    add("governor_face_bottom", [6.5, 2.5, 1.5], [9.5, 3.5, 2], "bone")
    add("governor_window", [6.25, 4, 1.25], [9.75, 7, 1.5], "blood")
    add("governor_needle", [7.75, 5.5, 1], [8.25, 7.5, 1.25], "bone")
    add("governor_hub", [7.25, 5, .75], [8.75, 6, 1.25], "cap")
    for x in (6.25, 9.25):
        add("dial_index", [x, 4.25, 1], [x+.5, 4.75, 1.25], "bone")
    add("front_foot_inlay", [4, .5, .75], [12, 1, 1], "bone")
    return parts


def fractionating():
    parts = calibrated()
    def add(*args):
        parts.append(cube(*args))

    # Paired receivers are lower than the lowest moving sample, with open faces.
    for label, x, fill_height in (("primary", 1.25, 7.25), ("secondary", 11.25, 5.75)):
        add(label + "_receiver_foot", [x-.25, 2, 7], [x+3.75, 2.75, 11], "dark")
        add(label + "_receiver_glass", [x, 2.75, 7.25], [x+3.5, 9, 10.75], "glass")
        add(label + "_receiver_contents", [x+.75, 3, 8], [x+2.75, fill_height, 10], "blood")
        add(label + "_receiver_lid", [x-.25, 9, 7], [x+3.75, 9.75, 11])
        add(label + "_receiver_neck", [x+1.25, 9.75, 8.25], [x+2.25, 10.5, 9.25], "bone")
        for edge in (x, x+3):
            add(label + "_receiver_strap", [edge, 2.75, 10.75], [edge+.5, 9, 11.25])
        add(label + "_return_pipe", [x+1.25, 9.75, 9.25], [x+2.25, 10.5, 14.75], "pillar")

    # Rear uprights sit outside the sweep; the crosshead is above all rotor parts.
    for label, x in (("primary", 2.5), ("secondary", 12.5)):
        add(label + "_manifold_shoe", [x-.5, 1.5, 13.5], [x+2, 3, 16], "dark")
        add(label + "_manifold_riser", [x, 3, 15], [x+1.5, 18, 16], "pillar")
        add(label + "_riser_inlay", [x+.5, 4, 14.75], [x+1, 16.5, 15], "bone")
        for y in (5, 10, 15.5):
            add(label + "_pipe_ferrule", [x-.25, y, 15], [x+1.75, y+.75, 16])
    add("split_manifold_crosshead", [2.5, 17.5, 14.5], [14, 18.5, 16], "dark")
    add("split_manifold_trim", [3, 18.5, 14.75], [13.5, 19, 15.75], "bone")
    add("splitter_crown_socket", [6, 17.25, 13.75], [10.5, 18, 16])
    add("splitter_crown_glass", [6.5, 18, 14], [10, 20, 15.75], "blood")
    add("splitter_crown_divider", [8, 18, 13.75], [8.5, 20, 14], "bone")
    add("splitter_crown_cap", [6, 20, 13.75], [10.5, 20.75, 16], "dark")
    add("splitter_crown_top", [7, 20.75, 14.25], [9.5, 21.5, 15.5], "cap")
    return parts


def model(parts):
    return {"credit": "Hemomancy - Alchemist centrifuge upgrades", "parent": "block/block",
            "ambientocclusion": False, "render_type": "cutout",
            "textures": copy.deepcopy(TEXTURES), "elements": parts}


def assets():
    return {NAMES[0]: model(calibrated()), NAMES[1]: model(fractionating())}


def editable(name, runtime):
    result = blockbench(name, runtime)
    groups = {"Calibrated body and governor": [], "Fractionation receivers and manifold": []}
    for element in result["elements"]:
        label = "Fractionation receivers and manifold" if element["name"].startswith(
            ("primary_", "secondary_", "split_", "splitter_")) else "Calibrated body and governor"
        groups[label].append(element["uuid"])
    result["outliner"] = [{"name": label, "origin": [8, 0, 8],
                          "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, name + label)),
                          "children": children} for label, children in groups.items() if children]
    return result


def blockstates():
    variants = {}
    for stage, name in enumerate(("vial_centrifuge", *NAMES)):
        for facing, angle in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
            entry = {"model": "hemomancy:block/" + name}
            if angle: entry["y"] = angle
            variants[f"facing={facing},stage={stage}"] = entry
    return {"variants": variants}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="verify exports without changing files")
    args = parser.parse_args()
    exports = {ASSETS / "blockstates/vial_centrifuge.json": blockstates()}
    for name, value in assets().items():
        exports[MODELS / (name + ".json")] = value
        exports[SOURCE / (name + ".bbmodel")] = editable(name, value)
    for path, value in exports.items():
        if args.check:
            assert json.loads(path.read_text(encoding="utf-8")) == value, f"Export drift: {path}"
        else:
            path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")
    print("Verified" if args.check else "Exported", "two Centrifuge bodies and twelve stage/facing variants")


if __name__ == "__main__":
    main()
