import json
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
MODEL_DIR = ROOT / "src/main/resources/assets/hemomancy/models/item"
TEXTURE_DIR = ROOT / "src/main/resources/assets/hemomancy/textures/item"

PALETTES = {
    "worn_vow": ((50, 23, 25), (112, 30, 34), (77, 73, 70), (151, 139, 116)),
    "barbed_fitting": ((37, 16, 19), (143, 22, 30), (67, 63, 61), (184, 64, 57)),
    "chitinite_fitting": ((48, 25, 18), (117, 42, 29), (153, 91, 38), (205, 148, 63)),
    "prismatic_fitting": ((41, 17, 39), (115, 30, 76), (113, 70, 143), (99, 192, 207)),
    "crimson_vestment": ((48, 15, 20), (142, 18, 31), (91, 54, 47), (192, 151, 103)),
    "monolithic_frame": ((24, 20, 22), (75, 19, 25), (54, 53, 57), (119, 112, 105)),
    "assumed_limb": ((53, 20, 23), (133, 32, 39), (151, 123, 91), (218, 194, 145)),
}

MATERIAL_UVS = {
    "shaft": [0, 0, 4, 4],
    "vein": [4, 0, 8, 4],
    "metal": [8, 0, 12, 4],
    "focus": [12, 0, 16, 4],
    "accent": [0, 4, 4, 8],
    "plate": [4, 4, 8, 8],
    "cloth": [8, 4, 12, 8],
    "dark": [12, 4, 16, 8],
}


def material_for_region(uv):
    if uv is None:
        return "plate"
    x1, y1, _, _ = uv
    if y1 >= 32:
        return "cloth"
    if x1 >= 48 and y1 >= 16:
        return "accent"
    if x1 >= 48:
        return "focus"
    if x1 >= 40:
        return "vein"
    if x1 >= 24:
        return "metal"
    if y1 >= 16:
        return "plate"
    if x1 >= 16:
        return "vein"
    return "shaft"


def cube(a, b, uv=None, rotation=None):
    material = material_for_region(uv)
    side_face = {"uv": MATERIAL_UVS[material], "texture": "#0"}
    cap_material = "dark" if material in ("shaft", "vein", "cloth", "plate") else material
    cap_face = {"uv": MATERIAL_UVS[cap_material], "texture": "#0"}
    result = {"from": list(a), "to": list(b), "faces": {
        "north": dict(side_face), "east": dict(side_face), "south": dict(side_face), "west": dict(side_face),
        "up": dict(cap_face), "down": dict(cap_face),
    }}
    if rotation:
        angle, origin, axis = rotation
        result["rotation"] = {"angle": angle, "axis": axis, "origin": list(origin)}
    return result


def shared_shaft():
    return [
        cube((7.25, -10, 7.25), (8.75, 5, 8.75), (0, 0, 8, 16)),
        cube((7.0, 4, 7.0), (9.0, 18, 9.0), (8, 0, 16, 16)),
        cube((7.3, 17, 7.3), (8.7, 24, 8.7), (16, 0, 24, 12)),
        cube((6.55, -5, 6.55), (9.45, -2.5, 9.45), (24, 0, 32, 8)),
        cube((6.65, 5, 6.65), (9.35, 7, 9.35), (24, 8, 32, 16)),
        cube((6.6, 14, 6.6), (9.4, 16, 9.4), (32, 0, 40, 8)),
        cube((6.9, 21.5, 6.9), (9.1, 24.5, 9.1), (32, 8, 40, 16)),
        cube((7.0, -9, 6.8), (7.45, 18, 7.2), (40, 0, 44, 16)),
    ]


def worn_vow():
    e = shared_shaft()
    e += [cube((3, 25, 7.25), (13, 26.5, 8.75)), cube((3, 31, 7.25), (13, 32.5, 8.75)),
          cube((2.5, 25, 7.25), (4, 32, 8.75)), cube((12, 25, 7.25), (13.5, 32, 8.75)),
          cube((6.4, 26, 6.4), (9.6, 31.2, 9.6), (48, 0, 56, 12)),
          cube((5, 23.5, 7), (11, 25, 9)), cube((6.2, -11, 6.2), (9.8, -9, 9.8))]
    return e


def barbed():
    e = shared_shaft()
    e += [cube((4, 23, 7), (6, 33, 9), rotation=(-22.5, (6, 24, 8), "z")),
          cube((10, 23, 7), (12, 33, 9), rotation=(22.5, (10, 24, 8), "z")),
          cube((1.5, 31, 7), (5.5, 33, 9), rotation=(-22.5, (5, 32, 8), "z")),
          cube((10.5, 31, 7), (14.5, 33, 9), rotation=(22.5, (11, 32, 8), "z")),
          cube((6.2, 25, 6.2), (9.8, 30, 9.8), (48, 0, 56, 12)),
          cube((4.3, 19, 7), (6, 23, 9), rotation=(-22.5, (6, 21, 8), "z")),
          cube((10, 16, 7), (11.7, 21, 9), rotation=(22.5, (10, 19, 8), "z"))]
    return e


def chitinite():
    e = shared_shaft()
    for y, width in ((2, 2.6), (8, 3.0), (14, 3.4), (20, 4.0)):
        e.append(cube((8-width, y, 6.5), (8+width, y+3.2, 9.5), (16, 16, 32, 24)))
    e += [cube((3.3, 23, 6.8), (6, 32, 9.2), rotation=(-22.5, (6, 24, 8), "z")),
          cube((10, 23, 6.8), (12.7, 32, 9.2), rotation=(22.5, (10, 24, 8), "z")),
          cube((6.1, 25.5, 6.1), (9.9, 30.5, 9.9), (48, 0, 56, 12)),
          cube((5.4, 30, 7), (7.2, 34, 9), rotation=(22.5, (6.5, 31, 8), "z")),
          cube((8.8, 30, 7), (10.6, 34, 9), rotation=(-22.5, (9.5, 31, 8), "z"))]
    return e


def prismatic():
    e = shared_shaft()
    e += [cube((6.2, 24, 6.2), (9.8, 30, 9.8), (48, 0, 56, 12)),
          cube((6.5, 29, 6.5), (9.5, 36, 9.5), (48, 16, 56, 32), rotation=(45, (8, 30, 8), "y")),
          cube((3.7, 26, 7), (6.2, 33, 9), (56, 16, 64, 32), rotation=(-22.5, (6, 27, 8), "z")),
          cube((9.8, 26, 7), (12.3, 33, 9), (56, 16, 64, 32), rotation=(22.5, (10, 27, 8), "z")),
          cube((4.5, 22, 7), (6.5, 27, 9), rotation=(-22.5, (6, 24, 8), "z")),
          cube((9.5, 22, 7), (11.5, 27, 9), rotation=(22.5, (10, 24, 8), "z")),
          cube((6.8, 10, 6.8), (9.2, 14, 9.2), (48, 16, 56, 32), rotation=(45, (8, 12, 8), "y"))]
    return e


def crimson_vestment():
    e = shared_shaft()
    e += [cube((3, 24, 7), (13, 26, 9)), cube((3, 33, 7), (13, 35, 9)),
          cube((2.5, 24, 7), (4, 35, 9)), cube((12, 24, 7), (13.5, 35, 9)),
          cube((6.2, 26, 6.2), (9.8, 32, 9.8), (48, 0, 56, 12)),
          cube((4, 34, 7), (6, 38, 9), rotation=(-22.5, (6, 34, 8), "z")),
          cube((10, 34, 7), (12, 38, 9), rotation=(22.5, (10, 34, 8), "z")),
          cube((7, 35, 7), (9, 40, 9)), cube((3.2, 14, 7.1), (5.2, 27, 7.8), (0, 32, 8, 64)),
          cube((10.8, 12, 8.2), (12.8, 27, 8.9), (8, 32, 16, 64))]
    return e


def monolithic_frame():
    e = shared_shaft()
    e += [cube((2.5, 22, 6), (13.5, 36, 10), (16, 16, 32, 32)),
          cube((4, 24, 5.5), (12, 34, 10.5), (32, 16, 48, 32)),
          cube((1, 24, 6.5), (3, 34, 9.5)), cube((13, 24, 6.5), (15, 34, 9.5)),
          cube((2, 35, 6.5), (5, 38, 9.5)), cube((11, 35, 6.5), (14, 38, 9.5)),
          cube((5.5, 20, 6.2), (10.5, 23, 9.8)), cube((6, -12, 6), (10, -9, 10))]
    return e


def assumed_limb():
    e = shared_shaft()
    e += [cube((5.8, 23, 6.4), (10.2, 29.5, 9.6), (48, 0, 56, 12)),
          cube((3, 28, 7), (6, 36, 9), rotation=(-22.5, (6, 29, 8), "z")),
          cube((5.5, 29, 7), (7.2, 38, 9), rotation=(-22.5, (7, 30, 8), "z")),
          cube((8.8, 29, 7), (10.5, 38, 9), rotation=(22.5, (9, 30, 8), "z")),
          cube((10, 28, 7), (13, 36, 9), rotation=(22.5, (10, 29, 8), "z")),
          cube((6.3, 25.5, 5.8), (9.7, 29, 10.2), (56, 0, 64, 12)),
          cube((6.4, 9, 6.4), (9.6, 12, 9.6)), cube((6.3, -11.5, 6.3), (9.7, -9, 9.7))]
    return e


BUILDERS = {"worn_vow": worn_vow, "barbed_fitting": barbed, "chitinite_fitting": chitinite,
            "prismatic_fitting": prismatic, "crimson_vestment": crimson_vestment,
            "monolithic_frame": monolithic_frame, "assumed_limb": assumed_limb}


def fit_to_standard_bounds(elements, base_elements):
    source_min = [min(element["from"][axis] for element in elements) for axis in range(3)]
    source_max = [max(element["to"][axis] for element in elements) for axis in range(3)]
    target_min = [min(element["from"][axis] for element in base_elements) for axis in range(3)]
    target_max = [max(element["to"][axis] for element in base_elements) for axis in range(3)]

    # Match the standard staff's height and width exactly. Preserve the variants'
    # slimmer depth and center it inside the standard model's deeper crown envelope.
    scale = [
        (target_max[0] - target_min[0]) / (source_max[0] - source_min[0]),
        (target_max[1] - target_min[1]) / (source_max[1] - source_min[1]),
        min(1.0, (target_max[2] - target_min[2]) / (source_max[2] - source_min[2])),
    ]
    source_center_z = (source_min[2] + source_max[2]) / 2
    target_center_z = (target_min[2] + target_max[2]) / 2

    def transform(value, axis):
        if axis == 2:
            return round(target_center_z + (value - source_center_z) * scale[axis], 4)
        return round(target_min[axis] + (value - source_min[axis]) * scale[axis], 4)

    for element in elements:
        element["from"] = [transform(value, axis) for axis, value in enumerate(element["from"])]
        element["to"] = [transform(value, axis) for axis, value in enumerate(element["to"])]
        if "rotation" in element:
            element["rotation"]["origin"] = [
                transform(value, axis) for axis, value in enumerate(element["rotation"]["origin"])
            ]
    return elements


def make_texture(name, colors):
    dark, blood, material, highlight = colors
    image = Image.new("RGBA", (64, 64), dark + (255,))
    draw = ImageDraw.Draw(image)

    def fill_tile(column, row, color):
        x, y = column * 16, row * 16
        draw.rectangle((x, y, x + 15, y + 15), fill=color + (255,))
        return x, y

    # Organic shaft: quiet vertical grain with one intentional living seam.
    x, y = fill_tile(0, 0, dark)
    draw.line((x + 3, y, x + 3, y + 15), fill=material + (255,))
    draw.line((x + 11, y, x + 11, y + 15), fill=tuple(max(0, c - 18) for c in material) + (255,))
    draw.line((x + 7, y + 1, x + 7, y + 14), fill=blood + (255,))
    draw.point((x + 8, y + 5), fill=highlight + (255,))

    # Living vein: broad central vessel with restrained paired branches.
    x, y = fill_tile(1, 0, dark)
    draw.rectangle((x + 6, y, x + 9, y + 15), fill=blood + (255,))
    for yy, direction in ((3, -1), (7, 1), (11, -1)):
        draw.line((x + 7, y + yy, x + 7 + direction * 5, y + yy + 3), fill=blood + (255,), width=2)
    draw.line((x + 8, y, x + 8, y + 15), fill=highlight + (255,))

    # Forged fitting: bordered plate, central seam, and four purposeful rivets.
    x, y = fill_tile(2, 0, material)
    draw.rectangle((x, y, x + 15, y + 15), outline=dark + (255,), width=2)
    draw.line((x + 7, y + 2, x + 7, y + 13), fill=dark + (255,), width=2)
    for px, py in ((3, 3), (12, 3), (3, 12), (12, 12)):
        draw.rectangle((x + px, y + py, x + px + 1, y + py + 1), fill=highlight + (255,))

    # Focal organ/gem: centered, symmetrical, and visually isolated.
    x, y = fill_tile(3, 0, dark)
    draw.rectangle((x + 5, y + 2, x + 10, y + 13), fill=blood + (255,))
    draw.rectangle((x + 3, y + 5, x + 12, y + 10), fill=blood + (255,))
    draw.rectangle((x + 6, y + 3, x + 9, y + 12), fill=highlight + (255,))
    draw.rectangle((x + 4, y + 6, x + 11, y + 9), outline=material + (255,))

    # Fitting accent: a clean heraldic chevron rather than noise.
    x, y = fill_tile(0, 1, dark)
    draw.line((x + 2, y + 4, x + 7, y + 10, x + 13, y + 3), fill=highlight + (255,), width=3)
    draw.line((x + 3, y + 11, x + 12, y + 11), fill=blood + (255,), width=2)

    # Structural plate: broad readable panels with edge wear.
    x, y = fill_tile(1, 1, material)
    draw.rectangle((x, y, x + 15, y + 15), outline=dark + (255,), width=2)
    draw.line((x + 4, y + 2, x + 4, y + 13), fill=highlight + (255,), width=2)
    draw.line((x + 11, y + 2, x + 11, y + 13), fill=dark + (255,), width=2)
    draw.line((x + 2, y + 8, x + 13, y + 8), fill=blood + (255,))

    # Cloth/sinew: vertical weave, hem, and a single centered ritual stitch.
    x, y = fill_tile(2, 1, blood)
    for xx in (2, 6, 10, 14):
        draw.line((x + xx, y, x + xx, y + 15), fill=dark + (255,))
    draw.line((x, y + 13, x + 15, y + 13), fill=highlight + (255,), width=2)
    draw.line((x + 7, y + 3, x + 7, y + 10), fill=material + (255,), width=2)

    # End-cap/shadow tile: deliberately quiet so cube ends do not look patterned.
    x, y = fill_tile(3, 1, dark)
    draw.rectangle((x + 1, y + 1, x + 14, y + 14), outline=material + (255,))
    draw.rectangle((x + 4, y + 4, x + 11, y + 11), outline=blood + (255,))
    image.save(TEXTURE_DIR / f"living_staff_{name}.png")


def main():
    base_model = json.loads((MODEL_DIR / "living_staff.json").read_text(encoding="utf-8"))
    for name, builder in BUILDERS.items():
        elements = fit_to_standard_bounds(builder(), base_model["elements"])
        model = {"credit": "Hemomancy fitting redesign",
                 "textures": {"0": f"hemomancy:item/living_staff_{name}", "particle": f"hemomancy:item/living_staff_{name}"},
                 "elements": elements, "display": base_model["display"]}
        (MODEL_DIR / f"living_staff_{name}.json").write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        make_texture(name, PALETTES[name])
    print(f"Generated {len(BUILDERS)} fitting models and textures")


if __name__ == "__main__":
    main()
