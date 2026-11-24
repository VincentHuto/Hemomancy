"""Paint the Synaptic Loadouts screen in the Dendritic Distributor's materials."""

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

from dendritic_distributor import NERVE, SPECIAL
from fixture_atlas import ROOT, clamp, paint

ASSET = ROOT / "src/main/resources/assets/hemomancy/textures/gui/synaptic_loadouts.png"
SIZE = (404, 238)
SOCKETS = [(77, 61), (119, 49), (160, 41), (202, 38), (244, 41), (285, 49), (327, 61)]
IRON_DARK = "#211925"
IRON_EDGE = "#817378"
BONE = "#b19c77"


def material(image, kind, box, shade, shape=None):
    x0, y0, x1, y1 = box
    width, height = x1 - x0 + 1, y1 - y0 + 1
    tw, th = (width + 1) // 2, (height + 1) // 2
    grain = Image.new("RGBA", (tw, th))
    grain.putdata([
        clamp(paint(kind, "north", x, y, tw, th, "synaptic_gui", SPECIAL)[:3], shade) + (255,)
        for y in range(th) for x in range(tw)
    ])
    grain = Image.blend(grain, grain.filter(ImageFilter.BoxBlur(1)), .75)
    if kind == "ganglion":
        grain = Image.blend(grain, Image.new("RGBA", grain.size, (86, 60, 46, 255)), .7)
    face = grain.resize((tw * 2, th * 2), Image.Resampling.NEAREST).crop((0, 0, width, height))
    if shape:
        mask = Image.new("L", face.size)
        ImageDraw.Draw(mask).polygon([(x - x0, y - y0) for x, y in shape], fill=255)
        face.putalpha(mask)
    image.alpha_composite(face, (x0, y0))


def rivet(draw, x, y):
    draw.rectangle((x - 1, y - 1, x + 1, y + 1), fill=IRON_DARK)
    draw.point((x - 1, y - 1), fill=IRON_EDGE)


def bay(image, draw, x, center):
    case = [(x + 9, 80), (x + 162, 80), (x + 172, 90), (x + 172, 175), (x + 162, 184),
            (x + 9, 184), (x, 175), (x, 90)]
    material(image, "iron", (x, 80, x + 172, 184), 5, case)
    draw.line(case + [case[0]], fill=IRON_EDGE)
    well = [(x + 12, 84), (x + 159, 84), (x + 167, 92), (x + 167, 172), (x + 159, 179),
            (x + 12, 179), (x + 5, 172), (x + 5, 92)]
    material(image, "lacquer_deep", (x + 5, 84, x + 167, 179), -31, well)
    draw.line(well + [well[0]], fill="#9a6a58")
    draw.line((x + 17, 99, x + 155, 99), fill="#6c363a")
    for dx in (13, 159):
        for y in (89, 176):
            rivet(draw, x + dx, y)
    # The memory wheel is cut into the mounting face.
    for r, color in [(41, "#29121e"), (37, "#5f3837"), (34, "#271720"), (22, "#4d292e")]:
        draw.regular_polygon((center, 139, r), 12, rotation=15, outline=color)
    draw.regular_polygon((center, 139, 11), 8, fill="#301822", outline="#765749")
    draw.regular_polygon((center, 139, 5), 6, fill="#4b353a", outline="#a27b5a")
    for dx, dy in ((0, -42), (0, 41), (-42, 0), (42, 0)):
        draw.regular_polygon((center + dx, 139 + dy, 3), 4, fill="#82564b")


def nerve(draw, path, width):
    draw.line([(x, y + 2) for x, y in path], fill="#241622", width=width + 2)
    draw.line(path, fill="#655140", width=width)
    draw.line([(x, y - 1) for x, y in path], fill="#9c8160", width=1)


def paint_screen():
    image = Image.new("RGBA", SIZE)
    draw = ImageDraw.Draw(image)
    shell = [(13, 0), (390, 0), (403, 13), (403, 225), (391, 237), (12, 237), (0, 225), (0, 13)]
    material(image, "iron", (0, 0, 403, 237), -6, shell)
    draw.line(shell + [shell[0]], fill=IRON_DARK, width=2)
    draw.line([(5, 16), (16, 4), (386, 4), (398, 16)], fill=IRON_EDGE)
    recess = [(17, 7), (386, 7), (397, 19), (397, 218), (386, 230), (17, 230), (6, 218), (6, 19)]
    material(image, "wood", (6, 7, 397, 230), -18, recess)
    draw.line(recess + [recess[0]], fill="#0e0911", width=2)
    # Wax membrane, branching into seven boutons above the iron instrument bays.
    membrane = [(35, 75), (50, 56), (87, 40), (149, 25), (254, 25), (316, 40),
                (353, 56), (368, 75), (322, 85), (82, 85)]
    material(image, "ganglion", (35, 25, 368, 85), -70, membrane)
    draw.line(membrane + [membrane[0]], fill="#5b4242")
    draw.line([(50, 73), (118, 64), (167, 72), (202, 80), (237, 72), (286, 64), (354, 73)],
              fill="#542d36")
    nerve(draw, [(101, 85), (123, 77), (180, 74), (202, 73), (224, 74), (281, 77), (303, 85)], 6)
    for index, (x, y) in enumerate(SOCKETS):
        elbow = (202 + (x - 202) * 2 // 3, y + (73 - y) // 2 + 5)
        nerve(draw, [(202, 73), elbow, (x, y)], 5)
        if index != 3:
            branch_x = elbow[0] + (-10 if x < 202 else 10)
            draw.line([elbow, (branch_x, elbow[1] + 3), (branch_x, elbow[1] + 6)], fill="#8a674e")
    draw.regular_polygon((202, 73, 10), 8, fill="#6b4b39", outline=BONE)
    draw.regular_polygon((202, 73, 5), 6, fill=clamp(NERVE, -30), outline="#46313a")
    nerve(draw, [(202, 81), (202, 175), (193, 185), (193, 188)], 8)
    for y in (103, 154, 174):
        draw.rectangle((198, y - 3, 206, y + 3), fill="#746452", outline="#b39673")

    bay(image, draw, 15, 101)
    bay(image, draw, 216, 302)
    # Central transfer coupler. Its arrows are cut through the plate.
    draw.regular_polygon((202, 129, 13), 8, fill=IRON_DARK, outline=IRON_EDGE)
    draw.line([(195, 125), (208, 125), (205, 122)], fill="#e1bd77", width=2)
    draw.line([(209, 133), (196, 133), (199, 136)], fill="#d2a7a0", width=2)

    for x, right in ((12, 205), (212, 391)):
        draw.rectangle((x, 188, right, 227), fill=IRON_DARK, outline="#774e50")
        draw.line((x + 4, 226, right - 4, 226), fill="#66515a")
    for x in (10, 393):
        for y in (13, 76, 183, 224):
            rivet(draw, x, y)
    return image


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--preview", type=Path)
    args = parser.parse_args()
    image = paint_screen()
    if args.check:
        with Image.open(ASSET) as saved:
            if saved.size != image.size or saved.convert("RGBA").tobytes() != image.tobytes():
                raise SystemExit("Regenerate the Synaptic Loadouts GUI texture")
    else:
        image.save(ASSET, optimize=True)
    if args.preview:
        sheet = Image.new("RGBA", SIZE, "#120b12")
        sheet.alpha_composite(image)
        sheet.resize((SIZE[0] * 2, SIZE[1] * 2), Image.Resampling.NEAREST).save(args.preview)


if __name__ == "__main__":
    main()
