"""Author the editable Choir Keeper Blockbench project and its pixel texture."""

import base64
import io
import json
import sys
import uuid
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
MODEL = ROOT / "src/main/resources/assets/hemomancy/models/entity/bbmodel/ChoirKeeperModel.bbmodel"
TEXTURE = ROOT / "src/main/resources/assets/hemomancy/textures/entity/choir_keeper.png"

if MODEL.exists() and "--overwrite" not in sys.argv:
    raise SystemExit("The Blockbench project already exists. Pass --overwrite to replace its current edits.")

image = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
draw = ImageDraw.Draw(image)


def tile(col, row, base, light, dark, seed=0):
    x, y = col * 16, row * 16
    draw.rectangle((x, y, x + 15, y + 15), fill=base)
    for i in range(16):
        if (i + seed) % 3 == 0:
            draw.line((x + i, y + 1, x + i - 2, y + 12), fill=light, width=1)
        if (i + seed) % 4 == 0:
            draw.line((x + i, y + 6, x + i + 2, y + 15), fill=dark, width=1)
    draw.line((x, y + 15, x + 15, y + 15), fill=dark)


tile(0, 0, "#252333", "#534860", "#171923")  # mantle
tile(1, 0, "#343044", "#61506b", "#211d2e")  # breast
tile(2, 0, "#1c2533", "#426074", "#101722")  # wings
tile(3, 0, "#262b3a", "#456071", "#141824")  # flight feathers
tile(4, 0, "#60495d", "#92788a", "#3c2c42")  # bare head
tile(5, 0, "#413744", "#766978", "#241e2b")  # beak
tile(6, 0, "#38333e", "#706577", "#211d27")  # legs
tile(7, 0, "#27293c", "#344e59", "#151724")  # tail feather

# Two versions of the Eye of Ender ocellus; the second is cracked and dim.
for col, cracked in ((0, False), (1, True)):
    x, y = col * 16, 16
    draw.rectangle((x, y, x + 15, y + 15), fill="#19202d")
    draw.ellipse((x + 1, y + 1, x + 14, y + 14), fill="#304958", outline="#597384")
    draw.ellipse((x + 3, y + 3, x + 12, y + 12), fill="#3a7865" if cracked else "#55c29a")
    draw.ellipse((x + 5, y + 4, x + 10, y + 11), fill="#75aa79" if cracked else "#b7e59a")
    draw.polygon([(x + 8, y + 3), (x + 10, y + 8), (x + 8, y + 13), (x + 6, y + 8)], fill="#10151d")
    if cracked:
        draw.line((x + 2, y + 3, x + 7, y + 6, x + 9, y + 11), fill="#131b24")
        draw.line((x + 12, y + 2, x + 9, y + 7, x + 12, y + 12), fill="#131b24")

tile(2, 1, "#25313b", "#3d5961", "#19202d")  # teal feather vane
tile(3, 1, "#372d4a", "#645078", "#211a31")  # purple feather vane
tile(4, 1, "#5c435f", "#a075aa", "#32263e")  # chorus fruit
tile(5, 1, "#734f82", "#c28cc2", "#452b57")  # fruit highlights
tile(6, 1, "#0c1519", "#263a3b", "#091015")  # claws
tile(7, 1, "#b4d990", "#e5ffc7", "#4c7959")  # living bird eye

elements = []


def is_quarter(value):
    return abs(value * 4 - round(value * 4)) < 1e-8


def snap_quarter(value):
    return round(value * 4) / 4


def group(name, origin, children, rotation=None):
    assert all(is_quarter(value) for value in origin), name
    result = {"name": name, "origin": origin, "color": 0,
              "uuid": str(uuid.uuid5(uuid.NAMESPACE_DNS, "choir_keeper_group_" + name)),
              "export": True, "mirror_uv": False, "isOpen": True,
              "locked": False, "visibility": True, "autouv": 0, "children": children}
    if rotation:
        result["rotation"] = rotation
    return result


def cube(name, lower, upper, uv=(0, 0), origin=None, rotation=None,
         front_uv=None, top_uv=None):
    assert all(is_quarter(value) for value in lower + upper), name
    assert all(high - low >= .25 for low, high in zip(lower, upper)), name
    x, y, z = lower
    X, Y, Z = upper
    u, v = uv[0] * 16, uv[1] * 16
    faces = {face: {"uv": [u, v, u + 16, v + 16], "texture": 0}
             for face in ("north", "east", "south", "west", "up", "down")}
    if front_uv is not None:
        f, g = front_uv[0] * 16, front_uv[1] * 16
        for face in ("north", "south"):
            faces[face] = {"uv": [f, g, f + 16, g + 16], "texture": 0}
    if top_uv is not None:
        f, g = top_uv[0] * 16, top_uv[1] * 16
        for face in ("up", "down"):
            faces[face] = {"uv": [f, g, f + 16, g + 16], "texture": 0}
    uid = str(uuid.uuid5(uuid.NAMESPACE_DNS, "choir_keeper_cube_" + name))
    result = {"name": name, "rescale": False, "locked": False,
              "from": lower, "to": upper, "autouv": 0, "color": 0,
              "origin": origin or [snap_quarter((x + X) / 2),
                                   snap_quarter((y + Y) / 2),
                                   snap_quarter((z + Z) / 2)],
              "uv_offset": [u, v], "faces": faces, "type": "cube", "uuid": uid}
    if rotation:
        result["rotation"] = rotation
    elements.append(result)
    return uid


body = [
    cube("narrow_mantle", [-2.5, 9.5, -1.75], [2.5, 17, 3.5], (0, 0)),
    cube("slim_breast", [-2, 10.5, -3.5], [2, 15, -1.5], (1, 0)),
    cube("tapered_rump", [-2, 9, 2.25], [2, 12, 5.25], (0, 0)),
]
neck = [cube("long_bare_neck_lower", [-1, 15.5, -3], [1, 20.75, -1.5], (4, 0), rotation=[-12, 0, 0]),
        cube("long_bare_neck_upper", [-.75, 19.25, -4.5], [.75, 23.25, -3], (4, 0), rotation=[-17, 0, 0])]
head = [cube("small_vulture_head", [-1.25, 22, -6], [1.25, 24.5, -3.25], (4, 0)),
        cube("beak_base", [-.75, 22.25, -7.75], [.75, 23.25, -5.75], (5, 0)),
        cube("hooked_beak_tip", [-.5, 21.25, -8.5], [.5, 22.75, -7.5], (5, 0), rotation=[-18, 0, 0]),
        cube("left_eye", [-1.5, 23, -5.25], [-1.25, 23.75, -4.5], (7, 1)),
        cube("right_eye", [1.25, 23, -5.25], [1.5, 23.75, -4.5], (7, 1))]

wings = []
for side, label in ((-1, "left"), (1, "right")):
    x0, x1 = (-3.25, -2) if side < 0 else (2, 3.25)
    parts = [cube(label + "_folded_wing", [x0, 11.25, -1.75], [x1, 16, 4.5], (2, 0), rotation=[0, 0, 6 * side])]
    for i in range(5):
        shift = i * .75
        parts.append(cube(f"{label}_flight_feather_{i}",
                          [x0 - (.5 if side < 0 else 0), 8.5 - i * .25, -1.25 + shift],
                          [x1 + (.5 if side > 0 else 0), 13.5 - i * .25, .25 + shift],
                          (3, 0), rotation=[-10, 0, side * (4 + i * 2)]))
    wings.append(group(label + "_wing", [side * 3, 15, 0], parts))

legs = []
for side, label in ((-1, "left"), (1, "right")):
    x = side * 1.5
    parts = [cube(label + "_thigh", [x - .75, 5.5, 1], [x + .75, 9.25, 2.5], (1, 0)),
             cube(label + "_shank", [x - .5, 1.5, 1.5], [x + .5, 6, 2.5], (6, 0))]
    for i, dx in enumerate((-.75, 0, .75)):
        parts.append(cube(f"{label}_front_talon_{i}",
                          [x + dx - .25, .5, -1], [x + dx + .25, 2, 2], (6, 1),
                          rotation=[-11, 0, 0]))
    parts.append(cube(label + "_rear_talon", [x - .25, .5, 2], [x + .25, 1.75, 4.25], (6, 1)))
    legs.append(group(label + "_leg", [x, 8.5, 1.75], parts))

# The resting tail angles down behind the body and keeps its eye feathers
# gathered. Each feather remains a separate bone for a later display pose.
tail = []
for i in range(15):
    angle = -21 + i * 3
    vane = [cube(f"tail_{i:02d}_shaft", [-.25, 8.5, 4.75],
                 [.25, 9, 23], (7, 0)),
            cube(f"tail_{i:02d}_vane", [-1, 8.5, 16.5],
                 [1, 9.25, 25], (2 if i % 2 else 3, 1)),
            cube(f"tail_{i:02d}_eye", [-1, 9.25, 20.75],
                 [1, 9.5, 24], (7, 0),
                 top_uv=(1 if i in (1, 7, 12) else 0, 1))]
    tail.append(group(f"eye_feather_{i:02d}", [0, 8.75, 4.75], vane,
                      rotation=[0, angle, 0]))

outliner = [group("body", [0, 12, 1], body + [
    group("neck", [0, 16.5, -2.5], neck + [group("head", [0, 21.5, -4], head)]),
    *wings, group("tail_fan", [0, 8.75, 4.75], tail,
                  rotation=[22.5, 0, 0])]), *legs]

TEXTURE.parent.mkdir(parents=True, exist_ok=True)
MODEL.parent.mkdir(parents=True, exist_ok=True)
image.save(TEXTURE)
buffer = io.BytesIO()
image.save(buffer, format="PNG")
source = "data:image/png;base64," + base64.b64encode(buffer.getvalue()).decode("ascii")
project = {
    "meta": {"format_version": "4.0", "model_format": "modded_entity", "box_uv": False},
    "name": "Choir Keeper", "model_identifier": "choir_keeper",
    "modded_entity_version": "1.21.1", "modded_entity_flip_y": False,
    "visible_box": [4, 3, 4], "resolution": {"width": 128, "height": 128},
    "elements": elements, "outliner": outliner,
    "textures": [{"path": str(TEXTURE), "name": TEXTURE.name,
                  "folder": "textures/entity", "namespace": "hemomancy",
                  "id": "0", "particle": False, "render_mode": "default",
                  "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
                  "frame_order": "", "visible": True, "internal": False,
                  "saved": True, "uuid": str(uuid.uuid5(uuid.NAMESPACE_DNS, "choir_keeper_texture")),
                  "source": source}],
}
MODEL.write_text(json.dumps(project, indent=2) + "\n", encoding="utf-8")
print(f"Wrote {MODEL} ({len(elements)} cubes) and {TEXTURE}")
