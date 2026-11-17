"""Author the ground network's blocks (core-loop Track 6): stains, gouges, living veins, the Lux trace, the vein node
and the catalyst feeder. Run from the repository root: python tools/model_export/ground_network.py

Marks (stain, gouge, vein, trace) are multiface blocks after vanilla glow lichen: one flat element per face, turned by
the blockstate. Their textures sit in the block's own 16 px grid and stay narrow and partly clear:

  blood_stain_<age>_<n>        three splashes (pool, arms, drops) per age: glossy red, a tacky maroon skin with a
                               raised rim, then a cracked brown crust round a clear middle; vanilla picks one by position
  blood_gouge                  a short trench nicked into the face, dark floor and a worn rim
  living_vein                  the same trench grown through with tissue, with fine capillaries to the edges
  lux_trace                    no geometry (the ground run renderer draws it); a particle texture only

A lit run's glow is drawn by GroundRunRenderer, not baked, so a dormant gouge is only its groove.

The vein node is a knot of tissue 10 x 9 x 10; the catalyst feeder a hematic iron hopper 12 x 12 x 12 with a red
lacquer band and an open mouth (no copper or brass on blood machines). Writes textures, block and item models and
blockstates under src/main/resources/assets/hemomancy. Python 3 + Pillow. Deterministic.
"""
import json
import math
import os
import random

from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "hemomancy")
FACES = ("north", "east", "south", "west", "up", "down")
TURN = {"north": {}, "east": {"y": 90}, "south": {"y": 180}, "west": {"y": 270}, "up": {"x": 270}, "down": {"x": 90}}
AGES = ("wet", "tacky", "dry")


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def save(img, name):
    path = os.path.join(ASSETS, "textures", "block", name + ".png")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def blend(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(len(a)))


# ── Textures ──

STAIN_VARIANTS = 3
STAIN_ALPHA = {"wet": 228, "tacky": 200, "dry": 160}


def splash_shape(variant):
    """One splash, shared by every age so a stain keeps its outline as it dries: a lumpy main pool off centre, three or
    four arms thrown out of it (1 to 2 px wide, ending in a drop), and a few satellite droplets. Returns a map of pixel
    to (depth, part): depth 0 at the pool's edge to 1 at its heart; part 'pool', 'arm' or 'drop'."""
    rnd = random.Random(7700 + variant * 131)
    cx, cy = 8.0 + rnd.uniform(-1.2, 1.2), 8.0 + rnd.uniform(-1.2, 1.2)
    base = rnd.uniform(3.6, 4.4)
    harmonics = [(k, rnd.uniform(0.08, 0.2) / k ** 0.5, rnd.uniform(0, 6.283)) for k in (2, 3, 5, 7)]

    def radius(theta):
        r = base
        for k, amp, phase in harmonics:
            r *= 1.0 + amp * math.cos(k * theta + phase)
        return r

    shape = {}
    for y in range(16):
        for x in range(16):
            px, py = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(px, py)
            theta = math.atan2(py, px)
            rr = radius(theta) + rnd.uniform(-0.45, 0.45)  # a ragged edge, never a clean circle
            if d < rr:
                shape[(x, y)] = (1.0 - d / rr, "pool")
    arms = rnd.randint(3, 4)
    start = rnd.uniform(0, 6.283)
    for i in range(arms):
        theta = start + i * 6.283 / arms + rnd.uniform(-0.5, 0.5)
        length = rnd.uniform(2.0, 4.5)
        r0 = radius(theta) - 0.5
        wide = rnd.random() < 0.5
        steps = int((length + 1) * 3)
        wobble = rnd.uniform(-0.25, 0.25)
        for s in range(steps):
            t = s / steps
            dist = r0 + t * length
            a = theta + wobble * t
            x, y = int(cx + math.cos(a) * dist), int(cy + math.sin(a) * dist)
            for ox, oy in ((0, 0),) + (((1, 0), (0, 1)) if wide and t < 0.5 else ()):
                key = (x + ox, y + oy)
                if 1 <= key[0] < 15 and 1 <= key[1] < 15 and key not in shape:
                    shape[key] = (0.22 * (1 - t), "arm")
        # The drop the arm ends in.
        ex, ey = cx + math.cos(theta + wobble) * (r0 + length + 0.8), cy + math.sin(theta + wobble) * (r0 + length + 0.8)
        for oy in (-1, 0, 1):
            for ox in (-1, 0, 1):
                x, y = int(ex + ox * 0.6), int(ey + oy * 0.6)
                if 1 <= x < 15 and 1 <= y < 15 and (x, y) not in shape and rnd.random() < 0.8:
                    shape[(x, y)] = (0.3, "drop")
    for _ in range(rnd.randint(2, 4)):
        theta = rnd.uniform(0, 6.283)
        dist = radius(theta) + rnd.uniform(2.0, 5.5)
        x, y = int(cx + math.cos(theta) * dist), int(cy + math.sin(theta) * dist)
        cells = [(x, y)] + ([(x + 1, y), (x, y + 1), (x + 1, y + 1)] if rnd.random() < 0.35 else [])
        for key in cells:
            if 1 <= key[0] < 15 and 1 <= key[1] < 15 and key not in shape:
                shape[key] = (0.25, "drop")
    return shape, (cx, cy)


def stain(age, variant):
    """The splash of {@code variant} at {@code age}: wet is glossy red with a highlight; tacky a dull maroon skin with a
    darker heart and a slightly brighter raised rim; dry a cracked brown crust round a mostly clear middle."""
    rnd = random.Random(611 + AGES.index(age) * 17 + variant * 1009)
    shape, (cx, cy) = splash_shape(variant)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    alpha = STAIN_ALPHA[age]
    cracks = [rnd.uniform(0, 6.283) for _ in range(rnd.randint(4, 6))]
    for (x, y), (depth, part) in shape.items():
        if age == "wet":
            col = blend((176, 18, 28), (108, 6, 12), min(1.0, depth * 1.1))
            a = alpha if part == "pool" else alpha - 12
        elif age == "tacky":
            if part == "pool" and depth < 0.2:
                col = (126, 28, 30)  # the raised rim catches a little light
            elif part == "pool":
                col = blend((92, 14, 18), (58, 6, 10), min(1.0, (depth - 0.2) * 1.6))
            else:
                col = (84, 12, 16)
            a = alpha + (10 if part == "pool" and depth < 0.2 else 0)
        else:
            theta = math.atan2(y + 0.5 - cy, x + 0.5 - cx)
            cracked = any(abs((theta - c + 3.1416) % 6.2832 - 3.1416) < 0.13 for c in cracks)
            if part == "pool" and depth > 0.38:
                if rnd.random() > 0.12:
                    continue  # the middle has flaked away; a few flecks are left
                col, a = (74, 38, 26), alpha - 40
            else:
                if cracked or rnd.random() < 0.12:
                    continue  # cracks and flaked gaps through the crust
                col = blend((96, 50, 34), (66, 32, 22), rnd.random())
                a = alpha
        px[x, y] = col + (max(0, min(255, a)),)
    if age == "wet":
        # A glossy highlight up and to the left of the pool's heart.
        hx, hy = int(cx - 1.5), int(cy - 1.5)
        for (x, y) in ((hx, hy), (hx + 1, hy), (hx, hy + 1)):
            if (x, y) in shape:
                px[x, y] = (214, 70, 76, 236)
    return img


def gouge(tissue=False):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    # A pit gouged into the middle of the face: a 4 px floor, its deepest 2 px darker, a 1 px worn rim round it.
    # A run reads as a line of pits; the ground run renderer joins them when the run is lit or alive.
    floor = (150, 30, 46) if tissue else (34, 12, 14)
    core = (198, 56, 68) if tissue else (14, 4, 6)
    rim = (104, 24, 34) if tissue else (96, 72, 64)
    for y in range(5, 11):
        for x in range(5, 11):
            edge = x in (5, 10) or y in (5, 10)
            corner = x in (5, 10) and y in (5, 10)
            if corner:
                continue
            px[x, y] = (rim + (150,)) if edge else (floor + (240,))
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        px[x, y] = core + (250,)
    if tissue:
        # A glossy fleck where the tissue catches the light.
        px[6, 6] = (232, 120, 128, 230)
    return img


def flesh(seed, base, dark, light):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            n = rnd.random()
            col = blend(dark, base, n) if n < 0.6 else blend(base, light, (n - 0.6) / 0.4)
            px[x, y] = col + (255,)
    # Veins across it.
    for _ in range(3):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for _ in range(10):
            px[x % 16, y % 16] = dark + (255,)
            x += rnd.choice((-1, 0, 1))
            y += rnd.choice((0, 1))
    return img


def iron(seed, band):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 46 + rnd.randrange(14)
            px[x, y] = (v, v - 4, v - 2, 255)
    for x in range(16):
        px[x, 0] = (78, 72, 74, 255)
        px[x, 15] = (28, 24, 26, 255)
        if band:
            px[x, 5] = (122, 14, 22, 255)
            px[x, 6] = (96, 10, 18, 255)
    return img


def mouth():
    img = iron(31, False)
    px = img.load()
    for y in range(3, 13):
        for x in range(3, 13):
            d = max(abs(x - 7.5), abs(y - 7.5))
            px[x, y] = (18, 12, 14, 255) if d < 3 else (60, 14, 20, 255)
    return img


# ── Models and blockstates ──

def face_model(name, texture, render_type):
    write_json(os.path.join(ASSETS, "models", "block", name + ".json"), {
        "ambientocclusion": False,
        "render_type": render_type,
        "textures": {"particle": texture, "mark": texture},
        "elements": [{
            "from": [0, 0, 0.1], "to": [16, 16, 0.1],
            "faces": {"north": {"uv": [16, 0, 0, 16], "texture": "#mark"},
                      "south": {"uv": [0, 0, 16, 16], "texture": "#mark"}}}]})


def multiface_state(name, model_for, extra_when=None):
    parts = []
    for when_extra, model in (extra_when or [({}, model_for)]):
        for face in FACES:
            applies = []
            for one in (model if isinstance(model, list) else [model]):
                apply = {"model": one}
                apply.update(TURN[face])
                if TURN[face]:
                    apply["uvlock"] = True
                applies.append(apply)
            when = {face: "true"}
            when.update(when_extra)
            parts.append({"when": when, "apply": applies if len(applies) > 1 else applies[0]})
    write_json(os.path.join(ASSETS, "blockstates", name + ".json"), {"multipart": parts})


def box_model(name, frm, to, side, top, bottom, render_type="minecraft:solid"):
    faces = {}
    for face in ("north", "east", "south", "west"):
        faces[face] = {"texture": "#side", "uv": [frm[0], 16 - to[1], to[0], 16 - frm[1]]}
    faces["up"] = {"texture": "#top", "uv": [frm[0], frm[2], to[0], to[2]]}
    faces["down"] = {"texture": "#bottom", "uv": [frm[0], frm[2], to[0], to[2]], "cullface": "down"}
    write_json(os.path.join(ASSETS, "models", "block", name + ".json"), {
        "parent": "minecraft:block/block",
        "render_type": render_type,
        "textures": {"particle": side, "side": side, "top": top, "bottom": bottom},
        "elements": [{"from": frm, "to": to, "faces": faces}]})
    write_json(os.path.join(ASSETS, "blockstates", name + ".json"), {"variants": {"": {"model": "hemomancy:block/" + name}}})
    write_json(os.path.join(ASSETS, "models", "item", name + ".json"), {"parent": "hemomancy:block/" + name})


def knot_model():
    """The vein node: a knot of tissue on four short roots, a smaller knot on its crown."""
    side, top = "hemomancy:block/vein_node_side", "hemomancy:block/vein_node_top"
    parts = [([4, 0, 4], [12, 7, 12]), ([5, 7, 5], [11, 9, 11]), ([6, 9, 6], [10, 10, 10]),
             ([1, 0, 6], [4, 2, 10]), ([12, 0, 6], [15, 2, 10]), ([6, 0, 1], [10, 2, 4]), ([6, 0, 12], [10, 2, 15])]
    elements = []
    for frm, to in parts:
        faces = {}
        for face in ("north", "south"):
            faces[face] = {"texture": "#side", "uv": [frm[0], 16 - to[1], to[0], 16 - frm[1]]}
        for face in ("east", "west"):
            faces[face] = {"texture": "#side", "uv": [frm[2], 16 - to[1], to[2], 16 - frm[1]]}
        faces["up"] = {"texture": "#top", "uv": [frm[0], frm[2], to[0], to[2]]}
        faces["down"] = {"texture": "#side", "uv": [frm[0], frm[2], to[0], to[2]]}
        elements.append({"from": frm, "to": to, "faces": faces})
    write_json(os.path.join(ASSETS, "models", "block", "vein_node.json"), {
        "parent": "minecraft:block/block", "textures": {"particle": side, "side": side, "top": top},
        "elements": elements})
    write_json(os.path.join(ASSETS, "blockstates", "vein_node.json"), {"variants": {"": {"model": "hemomancy:block/vein_node"}}})
    write_json(os.path.join(ASSETS, "models", "item", "vein_node.json"), {"parent": "hemomancy:block/vein_node"})


def main():
    # Three splashes per age; the blockstate lists all three, so each position picks one and a stain keeps its
    # outline as it dries.
    for age in AGES:
        for n in range(STAIN_VARIANTS):
            name = "blood_stain_%s_%d" % (age, n)
            save(stain(age, n), name)
            face_model(name, "hemomancy:block/" + name, "minecraft:translucent")
    multiface_state("blood_stain", None, [({"age": str(i)}, ["hemomancy:block/blood_stain_%s_%d" % (age, n)
                                                            for n in range(STAIN_VARIANTS)])
                                          for i, age in enumerate(AGES)])

    save(gouge(False), "blood_gouge")
    face_model("blood_gouge", "hemomancy:block/blood_gouge", "minecraft:cutout")
    multiface_state("blood_gouge", "hemomancy:block/blood_gouge")

    save(gouge(True), "living_vein")
    face_model("living_vein", "hemomancy:block/living_vein", "minecraft:cutout")
    multiface_state("living_vein", "hemomancy:block/living_vein")

    # The Lux trace has no geometry; its particle is the incandescent glow of the gouge's light.
    write_json(os.path.join(ASSETS, "models", "block", "lux_trace.json"),
               {"textures": {"particle": "minecraft:block/glowstone"}, "elements": []})
    write_json(os.path.join(ASSETS, "blockstates", "lux_trace.json"), {"variants": {"": {"model": "hemomancy:block/lux_trace"}}})

    save(flesh(41, (150, 36, 50), (92, 16, 26), (196, 72, 84)), "vein_node_side")
    save(flesh(43, (166, 44, 58), (104, 18, 30), (214, 92, 104)), "vein_node_top")
    knot_model()

    save(iron(29, True), "catalyst_feeder_side")
    save(mouth(), "catalyst_feeder_top")
    save(iron(33, False), "catalyst_feeder_bottom")
    box_model("catalyst_feeder", [2, 0, 2], [14, 12, 14], "hemomancy:block/catalyst_feeder_side",
              "hemomancy:block/catalyst_feeder_top", "hemomancy:block/catalyst_feeder_bottom")
    print("ground network assets written")


if __name__ == "__main__":
    main()
