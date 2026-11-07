"""Author the Scarlet Vanity's mirror: the tall frame standing on the back of the desk, its upper cell.

The desk (models/block/scarlet_vanity.json) is the lower cell and is hand-authored; this script owns the
upper cell (models/block/scarlet_vanity_mirror.json) and the item model, which is the desk with the mirror
stacked on it and a dark pane where the glass goes.

The frame is blood wood with hematic iron at the posts, ears, crest and side scrolls (no copper or brass on
Harbinger furniture). Its top is an arch stepped one pixel column at a time: low at the shoulders, rising to a
crest at the centre and to scrolled ears at the corners. The glass itself is not modelled: ScarletVanityRenderer
draws it as one quad that the vanity_mirror shader cuts to GLASS_TOP below, so the table there and the shader's
GLASS_TOP must stay the same, and so must the glass rectangle in VanityMirrorRules.

Coordinates are block pixels for facing=north (the vanity's front is -z), authored in the desk's frame (the desk
top is y 13, its backsplash ends at y 16) and shifted down 16 for the upper cell. Run from the repository root:
    python tools/model_export/scarlet_vanity_mirror.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODELS = ROOT / "src/main/resources/assets/hemomancy/models"

# The glass: x 3..13, from y 17 up to its column's top. One top per pixel column, x = 3 .. 12.
GLASS_X = (3, 13)
GLASS_BOTTOM = 17.0
GLASS_TOP = [29.0, 28.5, 28.5, 29.5, 30.5, 30.5, 29.5, 28.5, 28.5, 29.0]

# Depths: the frame's face, the glass (drawn by the renderer), the backing board, the back.
FRAME_FRONT = 13.5
BOARD_FRONT = 14.4
BACK = 15.0
POST_TOP = 30.5

TEXTURES = {
    "particle": "hemomancy:block/blood_wood_planks",
    "dark": "hemomancy:block/blood_wood_planks",
    "trim": "hemomancy:block/chiseled_hematic_iron_block",
}


def face_uvs(lo, hi):
    """Each face's UV read straight off the box, one texel per pixel, as Blockbench's auto UV does."""
    x1, y1, z1 = lo
    x2, y2, z2 = hi
    # Side faces read the cell the element starts in.
    floor = (y1 // 16) * 16
    top, bottom = max(0.0, 16.0 - (y2 - floor)), 16.0 - (y1 - floor)
    return {
        "north": [16 - x2, top, 16 - x1, bottom],
        "south": [x1, top, x2, bottom],
        "east": [16 - z2, top, 16 - z1, bottom],
        "west": [z1, top, z2, bottom],
        "up": [x1, z1, x2, z2],
        "down": [x1, 16 - z2, x2, 16 - z1],
    }


def box(name, lo, hi, texture, rotation=None):
    faces = {side: {"uv": [round(c, 4) for c in uv], "texture": "#" + texture}
             for side, uv in face_uvs(lo, hi).items()}
    element = {"name": name, "from": list(lo), "to": list(hi), "faces": faces}
    if rotation:
        element["rotation"] = rotation
    return element


def mirror_elements():
    """The frame in the desk's frame (y 16 is the top of the desk's backsplash)."""
    out = []
    x0, x1 = GLASS_X
    # The bottom rail sits on the backsplash, a little proud of the frame.
    out.append(box("bottom_rail", (2, 16, 13.25), (14, GLASS_BOTTOM, 15.25), "dark"))
    out.append(box("bottom_bead", (2.5, GLASS_BOTTOM, 13.25), (13.5, GLASS_BOTTOM + 0.25, 13.5), "trim"))
    # The posts, up to the ears.
    out.append(box("left_post", (2, GLASS_BOTTOM, FRAME_FRONT), (3, POST_TOP, BACK), "trim"))
    out.append(box("right_post", (13, GLASS_BOTTOM, FRAME_FRONT), (14, POST_TOP, BACK), "trim"))
    # The ears: a scroll knob over each post, curling outward.
    out.append(box("left_ear", (1.5, POST_TOP, 13.75), (3.25, POST_TOP + 1, 14.75), "trim"))
    out.append(box("right_ear", (12.75, POST_TOP, 13.75), (14.5, POST_TOP + 1, 14.75), "trim"))
    # The top rail and the backing board, one pixel column at a time.
    for i, top in enumerate(GLASS_TOP):
        x = x0 + i
        out.append(box(f"top_rail_{i}", (x, top, FRAME_FRONT), (x + 1, top + 1, BACK), "dark"))
        out.append(box(f"board_{i}", (x, GLASS_BOTTOM, BOARD_FRONT), (x + 1, top, BACK), "dark"))
    # The crest over the centre columns.
    centre = max(GLASS_TOP) + 1
    out.append(box("crest", (6.5, centre, 13.75), (9.5, centre + 0.5, 14.75), "trim"))
    out.append(box("crest_finial", (7.5, centre + 0.5, 13.75), (8.5, 32, 14.75), "trim"))
    # The side scrolls bracing the posts on the backsplash: an upright and a knob turned on its corner.
    for side, (sx0, sx1) in (("left", (1, 2)), ("right", (14, 15))):
        out.append(box(f"{side}_scroll", (sx0, 16, 13.75), (sx1, 19.5, 14.75), "trim"))
        cx = (sx0 + sx1) / 2
        out.append(box(f"{side}_scroll_knob", (cx - 0.6, 19.5, 13.65), (cx + 0.6, 20.7, 14.85), "trim",
                       {"angle": 45, "axis": "z", "origin": [cx, 20.1, 14.25]}))
    return out


def shifted(elements, dy):
    out = []
    for element in elements:
        moved = json.loads(json.dumps(element))
        moved["from"][1] += dy
        moved["to"][1] += dy
        if "rotation" in moved:
            moved["rotation"]["origin"][1] += dy
        out.append(moved)
    return out


def write(path, model):
    path.write_text(json.dumps(model, indent="\t") + "\n", encoding="utf-8")
    print("wrote", path.relative_to(ROOT))


def main():
    mirror = mirror_elements()
    write(MODELS / "block/scarlet_vanity_mirror.json", {
        "credit": "Generated by tools/model_export/scarlet_vanity_mirror.py",
        "parent": "block/block",
        "ambientocclusion": False,
        "render_type": "cutout",
        "textures": TEXTURES,
        "elements": shifted(mirror, -16),
    })

    desk = json.loads((MODELS / "block/scarlet_vanity.json").read_text(encoding="utf-8"))
    textures = dict(desk["textures"])
    textures.update(TEXTURES)
    textures["particle"] = desk["textures"]["particle"]
    textures["pane"] = "hemomancy:block/black_terracotta"
    # The item has no renderer to draw the glass, so it gets a dark pane in its place.
    pane = box("pane", (GLASS_X[0], GLASS_BOTTOM, BOARD_FRONT - 0.05), (GLASS_X[1], min(GLASS_TOP), BOARD_FRONT),
               "pane")
    write(MODELS / "item/scarlet_vanity.json", {
        "credit": "Generated by tools/model_export/scarlet_vanity_mirror.py: the desk with its mirror",
        "parent": "block/block",
        "ambientocclusion": False,
        "render_type": "cutout",
        "textures": textures,
        "elements": desk["elements"] + mirror + [pane],
        "display": {
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 0], "scale": [0.25, 0.25, 0.25]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, -1.5, 0], "scale": [0.3, 0.3, 0.3]},
            "ground": {"translation": [0, 2, 0], "scale": [0.18, 0.18, 0.18]},
            "gui": {"rotation": [30, -135, 0], "translation": [0, -3.5, 0], "scale": [0.42, 0.42, 0.42]},
            "fixed": {"translation": [0, -4, 0], "scale": [0.4, 0.4, 0.4]},
        },
    })


if __name__ == "__main__":
    main()
