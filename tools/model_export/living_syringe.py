"""Export the Living Syringe item variants and their editable Blockbench models."""

import base64
import json
from pathlib import Path
import struct
import uuid
import zlib

ASSETS = Path(__file__).resolve().parents[2] / "src/main/resources/assets/hemomancy"
MODELS = ASSETS / "models/item"
NAME = "living_syringe"
SIDES = ("north", "east", "south", "west", "up", "down")

# Each material is a hand-pixelled 4x4 patch within the 16x16 atlas.
MATERIALS = {
    "iron": ("24212b 393540 544f5c 837885", "3322 2212 2112 1101"),
    "edge": ("49434e 665d65 8e7e7b b29a8b", "3332 2222 2212 2111"),
    "ivory": ("645048 9e8064 d0b38a f1ddaf", "3332 3222 2221 2110"),
    "sinew": ("29121f 4f2135 7f334b b75b62", "2332 1221 1211 0120"),
    "vein": ("460f25 781732 b82b47 f06869", "2331 1232 1221 0120"),
    "membrane": ("401426 702139 9c3d50 c76366", "1322 3221 1232 2110"),
    "socket": ("1c1923 2a2531 3e3442 65525e", "2112 1001 1001 2112"),
    "glass": ("233440 3c5863 658991 bbd8d4", "2322 2202 2102 2112"),
    "brass": ("483138 775145 b17a59 d9a879", "3322 3222 2212 2110"),
}
FILLED_GLASS = ("56172c 952440 d44359 ff8b80", "2322 3322 3212 2112")


def png_chunk(kind, data):
    return (struct.pack(">I", len(data)) + kind + data
            + struct.pack(">I", zlib.crc32(kind + data)))


def atlas(filled=False):
    pixels = bytearray(16 * 16 * 4)
    for index, (name, patch) in enumerate(MATERIALS.items()):
        colors, rows = FILLED_GLASS if filled and name == "glass" else patch
        palette = [bytes.fromhex(color) + b"\xff" for color in colors.split()]
        for y, row in enumerate(rows.split()):
            for x, color in enumerate(row):
                offset = ((index // 4 * 4 + y) * 16 + index % 4 * 4 + x) * 4
                pixels[offset:offset + 4] = palette[int(color)]
    raw = b"".join(b"\0" + pixels[i:i + 64] for i in range(0, len(pixels), 64))
    return (b"\x89PNG\r\n\x1a\n"
            + png_chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
            + png_chunk(b"IDAT", zlib.compress(raw, 9))
            + png_chunk(b"IEND", b""))


def cube(name, start, end, material, texture="#material", angle=0, pivot=None):
    index = list(MATERIALS).index(material)
    u, v = index % 4 * 4, index // 4 * 4
    element = {"name": name, "from": start, "to": end,
               "faces": {face: {"uv": [u, v, u + 4, v + 4], "texture": texture}
                         for face in SIDES}}
    if angle:
        element["rotation"] = {"angle": angle, "axis": "z", "origin": pivot}
    return element


HINGE = [6.5, 5.25, 4.25]
ROTOR_SEAT = [6.5, 9.25, 8]
BARREL_Z = 10.5  # The indexed vial is 2.5 units off the axle.

WRIST = {
    "pump and hinge": [
        cube("pump mantle", [4.25, 3.75, 7], [8.25, 5.25, 10.25], "membrane"),
        cube("pump outlet", [5.75, 4.25, 10.25], [7.25, 5.75, 11.25], "vein"),
        cube("fixed hinge knuckle", [3.25, 4.5, 3.75], [9.75, 5.75, 4.75], "iron"),
        cube("left hinge pin", [2.75, 4.75, 4], [3.25, 5.5, 4.5], "brass"),
        cube("right hinge pin", [9.75, 4.75, 4], [10.25, 5.5, 4.5], "brass"),
    ],
    "wrist roots": [
        cube("dorsal attachment", [4, 2.25, 9], [7.75, 5.5, 10.5], "membrane"),
        cube("palm attachment", [4, 2, 4.75], [7.5, 4.5, 6], "sinew"),
        cube("outer tendon wrap", [2.75, 2.25, 5.75], [4, 4.75, 9.5], "sinew"),
        cube("inner tendon wrap", [7.5, 2.5, 5.5], [8.75, 4.25, 9.75], "membrane"),
        cube("outer tendon root", [3.25, .75, 7.75], [4.25, 3.5, 9.25], "membrane",
             angle=22.5, pivot=[3.75, 2.75, 8.5]),
        cube("inner tendon root", [7, .75, 8.75], [8, 3.25, 10], "sinew",
             angle=-22.5, pivot=[7.5, 2.75, 9.25]),
        cube("palm root tip", [6.25, .5, 5.25], [7.25, 2.5, 6.25], "sinew",
             angle=22.5, pivot=[6.75, 2.25, 5.5]),
        cube("dorsal artery", [5, 1.75, 10.5], [6, 5.5, 11], "vein",
             angle=22.5, pivot=[5.5, 4, 10.5]),
        cube("outer wrist spur", [2.5, 3.25, 7.25], [3.25, 5.25, 8.5], "ivory",
             angle=-22.5, pivot=[3, 4.25, 8]),
    ],
}

CHAMBER = {
    "hinged axle": [
        cube("axle heel", [5.75, 5, 4.25], [7.25, 5.5, 8.75], "iron"),
        cube("axle collar", [6, 5.5, 7.5], [7, 5.75, 8.5], "brass"),
        cube("drum axle", [6.25, 5.75, 7.75], [6.75, 12.5, 8.25], "edge"),
        cube("rear pump collar", [5.75, 5.25, 8.75], [7.25, 5.75, 9.75], "sinew"),
        cube("rear inlet bridge", [5.5, 5.75, 9.5], [7.5, 6.5, 11.5], "socket"),
        cube("inlet seal", [6, 5.5, BARREL_Z - .75], [7, 5.75, BARREL_Z + .5], "ivory"),
        cube("front thrust bearing", [5.75, 12.25, 7.25], [7.25, 12.75, 8.75], "socket"),
        cube("fang feed neck", [5.75, 12.75, 7.75], [7.25, 13.5, 9.5], "sinew"),
        cube("fang shoulder", [5, 12.75, 9.5], [8, 13.5, 11.75], "sinew"),
        cube("left fang socket", [5.25, 12.5, 9.75], [5.75, 12.75, 11.25], "ivory"),
        cube("right fang socket", [7.25, 12.5, 9.75], [7.75, 12.75, 11.25], "ivory"),
    ],
    "hollow fang": [
        cube("barrel root", [4.75, 13.5, BARREL_Z - 1.25], [8.25, 14.75, BARREL_Z + 1.25], "membrane"),
        cube("chitin ferrule", [5.25, 14.5, BARREL_Z - 1], [7.75, 15.25, BARREL_Z + 1], "ivory"),
        cube("hollow fang", [6, 15.25, BARREL_Z - .5], [7, 17, BARREL_Z + .5], "ivory"),
        cube("fang tip", [6.25, 17, BARREL_Z - .25], [6.75, 18.25, BARREL_Z + .25], "ivory"),
    ],
}

VIAL = [
    cube("single vial seat", [5, 6.5, BARREL_Z - 1.25], [8, 7.25, BARREL_Z + 1.25], "brass"),
    cube("single vial glass", [5.5, 7.25, BARREL_Z - 1], [7.5, 11.25, BARREL_Z + 1], "glass", "#sample_1"),
    cube("single vial collar", [5, 11.25, BARREL_Z - 1.25], [8, 12.25, BARREL_Z + 1.25], "ivory"),
]


def speedloader(cx, cy):
    def part(name, bounds, material, texture="#material"):
        x1, y1, z1, x2, y2, z2 = bounds
        return cube(name, [cx + x1, cy + y1, z1], [cx + x2, cy + y2, z2], material, texture)

    # Contiguous bands form a faceted cylinder without overlapping face planes.
    rotor = []
    bands = [(-3.5, -2.75, 2.25), (-2.75, -1.75, 3.25), (-1.75, 1.75, 3.75),
             (1.75, 2.75, 3.25), (2.75, 3.5, 2.25)]
    for i, (bottom, top, half_width) in enumerate(bands):
        rotor.append(part(f"drum band {i}", [-half_width, bottom, 6.5, half_width, top, 7], "iron"))
        rotor.append(part(f"retainer band {i}", [-half_width + .25, bottom, 8.5, half_width - .25, top, 9.25], "edge"))

    rotor.extend([
        part("spindle", [-1.25, -1.25, 7, 1.25, 1.25, 8.5], "iron"),
        part("rear axle", [-1, -1, 5.5, 1, 1, 6.5], "edge"),
        part("rear axle stud", [-.5, -.5, 5, .5, .5, 5.5], "brass"),
        part("release knob collar", [-1, -1, 9.25, 1, 1, 10], "socket"),
        part("twist release stem", [-.5, -.5, 10, .5, .5, 10.75], "edge"),
        part("twist release grip", [-1, -.75, 10.75, 1, .75, 11.5], "brass"),
        part("twist release index", [-.25, -.5, 11.5, .25, .5, 11.75], "ivory"),
    ])

    # Start at twelve o'clock and fill clockwise around the release knob.
    sockets = [(0, 2.5), (1.75, 1.75), (2.5, 0), (1.75, -1.75),
               (0, -2.5), (-1.75, -1.75), (-2.5, 0), (-1.75, 1.75)]
    for i, (x, y) in enumerate(sockets, start=1):
        rotor.append(part(f"vial body {i}", [x - .75, y - .75, 7, x + .75, y + .75, 8.5],
                          "glass", f"#sample_{i}"))
        for side, z1, z2, z3 in (("front", 9.25, 9.5, 10.75), ("rear", 6.5, 6.25, 5.25)):
            rotor.append(part(f"{side} ferrule {i}", [x - .75, y - .75, min(z1, z2),
                              x + .75, y + .75, max(z1, z2)], "brass"))
            rotor.append(part(f"{side} vial {i}", [x - .75, y - .75, min(z2, z3),
                              x + .75, y + .75, max(z2, z3)], "glass", f"#sample_{i}"))
    return rotor


DISPLAY = {
    "thirdperson_righthand": {"rotation": [-74, 0, 0], "translation": [1.25, -1, -2.5], "scale": [.9] * 3},
    "thirdperson_lefthand": {"rotation": [-74, 0, 0], "translation": [1.25, -1, -2.5], "scale": [.9] * 3},
    "firstperson_righthand": {"rotation": [-58, -40, -12], "translation": [3.25, 4.25, 0], "scale": [.7] * 3},
    "firstperson_lefthand": {"rotation": [-58, -40, -12], "translation": [.5, 4, -.5], "scale": [.7] * 3},
    "ground": {"rotation": [0, 0, -25], "translation": [0, 3, 0], "scale": [.55] * 3},
    "gui": {"rotation": [13, -36, -24], "translation": [.25, -1, 0], "scale": [.8] * 3},
    "fixed": {"rotation": [0, 0, -20], "translation": [-.25, 0, 0], "scale": [.9] * 3},
}
RACK_DISPLAY = {
    "thirdperson_righthand": {"rotation": [0, 40, -10], "translation": [0, 2, 0], "scale": [.9] * 3},
    "thirdperson_lefthand": {"rotation": [0, 40, -10], "translation": [0, 2, 0], "scale": [.9] * 3},
    "firstperson_righthand": {"rotation": [-20, -24, -10], "translation": [0, 1, -1], "scale": [.85] * 3},
    "firstperson_lefthand": {"rotation": [-20, -24, -10], "translation": [0, 1, -1], "scale": [.85] * 3},
    "gui": {"rotation": [16, -19, 0], "scale": [1.35] * 3},
    "fixed": {"rotation": [10, -10, 0], "scale": [1.25] * 3},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [.8] * 3},
}
TEXTURES = {
    "material": f"hemomancy:item/{NAME}_materials",
    "particle": "#material",
    "sample_empty": "#material",
    "sample_full": f"hemomancy:item/{NAME}_filled",
}


def sample_textures(capacity, filled):
    return {f"sample_{i}": "#sample_full" if i <= filled else "#sample_empty"
            for i in range(1, capacity + 1)}


def elements(groups):
    return [element for group in groups.values() for element in group]


def write_json(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def blockbench(name, groups, filled, images, display=DISPLAY, group_origins=None):
    def stable_id(label):
        return str(uuid.uuid5(uuid.NAMESPACE_URL, f"hemomancy:{name}/{label}"))

    cubes = []
    outliner = []
    bb_groups = []
    for group, parts in groups.items():
        children = []
        for part in parts:
            element = {**part, "type": "cube", "uuid": stable_id(part["name"]), "origin": [8, 8, 8]}
            rotation = element.pop("rotation", None)
            if rotation:
                element["origin"] = rotation["origin"]
                element["rotation"] = [0, 0, rotation["angle"]]
            element["faces"] = {
                side: {**face, "texture": int(face["texture"].startswith("#sample_")
                                               and int(face["texture"].split("_")[-1]) <= filled)}
                for side, face in part["faces"].items()
            }
            cubes.append(element)
            children.append(element["uuid"])
        origin = (group_origins or {}).get(group, [8, 8, 8])
        bb_groups.append({"name": group, "uuid": stable_id(group), "origin": origin, "rotation": [0, 0, 0]})
        outliner.append({"uuid": stable_id(group), "children": children})
    textures = [
        {"name": file, "relative_path": f"../../../textures/item/{file}", "namespace": "hemomancy",
         "folder": "item", "id": str(index), "uuid": stable_id(file),
         "width": 16, "height": 16, "uv_width": 16, "uv_height": 16, "particle": index == 0,
         "render_mode": "default", "source": "data:image/png;base64," + base64.b64encode(data).decode()}
        for index, (file, data) in enumerate(images.items())
    ]
    write_json(MODELS / "bbmodel" / f"{name}.bbmodel", {
        "meta": {"format_version": "5.0", "model_format": "java_block", "box_uv": False},
        "name": name, "java_block_version": "1.9.0", "ambientocclusion": False,
        "front_gui_light": True, "resolution": {"width": 16, "height": 16},
        "elements": cubes, "groups": bb_groups, "outliner": outliner,
        "textures": textures, "display": display,
    })


def export():
    images = {f"{NAME}_materials.png": atlas(), f"{NAME}_filled.png": atlas(filled=True)}
    for name, image in images.items():
        (ASSETS / "textures/item" / name).write_bytes(image)

    root = {"ambientocclusion": False, "gui_light": "front", "textures": TEXTURES, "display": DISPLAY}
    write_json(MODELS / f"{NAME}.json", {
        "parent": "builtin/entity", "gui_light": "front", "display": DISPLAY,
        "textures": {"particle": TEXTURES["material"]},
    })
    folder = MODELS / NAME
    folder.mkdir(exist_ok=True)
    write_json(folder / "wrist.json", {**root, "elements": elements(WRIST)})
    write_json(folder / "chamber.json", {**root, "elements": elements(CHAMBER)})
    write_json(folder / "vial_empty.json", {
        **root, "elements": VIAL, "textures": {**TEXTURES, **sample_textures(1, 0)},
    })
    write_json(folder / "vial_filled.json", {
        "parent": f"hemomancy:item/{NAME}/vial_empty", "textures": sample_textures(1, 1),
    })

    standalone = {"speedloader": speedloader(8, 8)}
    detached_folder = MODELS / "vial_rack"
    detached_folder.mkdir(exist_ok=True)
    write_json(detached_folder / "empty.json", {
        **root, "display": RACK_DISPLAY, "elements": elements(standalone),
        "textures": {**TEXTURES, **sample_textures(8, 0)},
    })
    write_json(MODELS / "vial_rack.json", {
        "parent": "hemomancy:item/vial_rack/empty",
        "overrides": [{"predicate": {"hemomancy:filled_vials": count},
                       "model": f"hemomancy:item/vial_rack/filled_{count}"}
                      for count in range(1, 9)],
    })
    for count in range(1, 9):
        write_json(detached_folder / f"filled_{count}.json", {
            "parent": "hemomancy:item/vial_rack/empty", "textures": sample_textures(8, count),
        })

    mounted = []
    for element in standalone["speedloader"]:
        x, y, z = ROTOR_SEAT
        x1, y1, z1 = element["from"]
        x2, y2, z2 = element["to"]
        mounted.append({**element, "from": [x + x1 - 8, y - z2 + 8, z + y1 - 8],
                        "to": [x + x2 - 8, y - z1 + 8, z + y2 - 8]})

    body = {**WRIST, **CHAMBER}
    blockbench("vial_rack", standalone, 4, images, RACK_DISPLAY)
    hinge_origins = dict.fromkeys(CHAMBER, HINGE)
    blockbench(NAME, body, 0, images, group_origins=hinge_origins)
    blockbench(f"{NAME}_vial", {**body, "inline vial": VIAL}, 1, images,
               group_origins={**hinge_origins, "inline vial": HINGE})
    blockbench(f"{NAME}_rack", {**body, "inline drum": mounted}, 8, images,
               group_origins={**hinge_origins, "inline drum": ROTOR_SEAT})


if __name__ == "__main__":
    export()
