"""Paint the Reliquary GUI atlas from the block fixture's materials.

The top 424 x 344 pixels are the open casket. The remaining strips hold the
isolated relic, drawer materials and socket rim for the screen's live layers.
"""

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

from fixture_atlas import ROOT, clamp, paint
from mnemonic_reliquary import SPECIAL

ASSET = ROOT / "src/main/resources/assets/hemomancy/textures/gui/mnemonic_reliquary.png"
W, H = 424, 512
ATLAS = Image.new("RGBA", (W, H))
D = ImageDraw.Draw(ATLAS)
NEAREST = Image.Resampling.NEAREST
DARK = "#180e16"
IRON_LIP = "#827682"
IRON_SHADOW = "#24202b"
BONE_DARK = "#665444"
BONE_LIGHT = "#cab999"
BLOOD = "#cc3546"


def material(kind, bounds, *, step=2, shade=0, shape=None, key="reliquary_gui"):
    """Fill a face with clustered texels using the same painter as the block atlas."""
    x0, y0, x1, y1 = bounds
    w, h = x1 - x0 + 1, y1 - y0 + 1
    tw, th = (w + step - 1) // step, (h + step - 1) // step
    image = Image.new("RGBA", (tw, th))
    pixels = []
    for y in range(th):
        for x in range(tw):
            r, g, b, alpha = paint(kind, "north", x, y, tw, th, key, SPECIAL)
            pixels.append(clamp((r, g, b), shade) + (alpha,))
    image.putdata(pixels)
    # Soften the sampled grain before scaling and masking, leaving inked details crisp.
    if kind != "sigil":
        strength = 0.4 if kind == "brain" else 0.72
        image = Image.blend(image, image.filter(ImageFilter.BoxBlur(1)), strength)
    image = image.resize((tw * step, th * step), NEAREST).crop((0, 0, w, h))
    if shape is not None:
        mask = Image.new("L", (w, h))
        ImageDraw.Draw(mask).polygon([(x - x0, y - y0) for x, y in shape], fill=255)
        image.putalpha(mask)
    ATLAS.alpha_composite(image, (x0, y0))


def stud(x, y):
    D.rectangle((x - 1, y - 1, x + 1, y + 2), fill=IRON_SHADOW)
    D.line((x - 1, y - 1, x + 1, y - 1), fill=IRON_LIP)
    D.point((x, y), fill="#5e535c")


def iron(bounds, *, shade=-4):
    x0, y0, x1, y1 = bounds
    material("iron", bounds, shade=shade)
    D.line((x0, y1, x1, y1), fill=IRON_SHADOW)
    D.line((x0, y0, x1, y0), fill=IRON_LIP)
    D.line((x1, y0 + 1, x1, y1), fill=IRON_SHADOW)


def lid():
    outline = [(0, 38), (22, 30), (58, 23), (94, 16), (130, 9), (293, 9),
               (329, 16), (365, 23), (401, 30), (423, 38), (423, 41), (0, 41)]
    D.polygon(outline, fill=DARK)
    lining = [(9, 38), (60, 28), (137, 11), (286, 11), (363, 28), (414, 38)]
    material("velvet", (9, 11, 414, 38), step=3, shade=-22, shape=lining)
    D.line(outline[:10], fill=BONE_DARK, width=2)
    D.line(lining[:6], fill="#ac8971", width=2)
    D.line((40, 34, 383, 34), fill="#380915", width=3)

    # The bone inlay is assembled from short cuts, like the carved liner on the block.
    glyphs = [(0, 0, 2, 4), (3, 2, 5, 4), (8, 1, 9, 5), (11, 0, 14, 1)]
    for start in (55, 75, 95, 115, 294, 314, 334, 354):
        for x0, y0, x1, y1 in glyphs:
            D.rectangle((start + x0, 27 + y0, start + x1, 27 + y1), fill="#9f846b")
    D.line((143, 11, 280, 11), fill="#611523")
    iron((128, 6, 295, 10), shade=4)
    material("bone", (126, 5, 131, 12), shade=-11)
    material("bone", (292, 5, 297, 12), shade=-11)
    D.polygon([(202, 9), (202, 6), (207, 6), (207, 2), (211, 0), (216, 2), (216, 6), (221, 6), (221, 9)],
              fill=BONE_LIGHT)
    D.rectangle((209, 4, 214, 7), fill="#5d1e2a")
    D.point((211, 4), fill=BLOOD)

    iron((0, 36, 423, 44))
    for x in (8, 48, 88, 174, 212, 250, 335, 375, 415):
        stud(x, 40)
    for x in (144, 279):
        iron((x - 9, 34, x + 9, 46), shade=12)
        D.line((x - 4, 36, x - 4, 43), fill="#9b8c94")
        D.line((x + 3, 36, x + 3, 43), fill=IRON_SHADOW)


def wall(x):
    material("wood", (x, 42, x + 103, 310), step=3, shade=-12)
    D.rectangle((x + 5, 46, x + 98, 306), fill=BONE_DARK)
    D.line((x + 5, 305, x + 98, 305), fill=BONE_LIGHT)
    material("lacquer_deep", (x + 7, 48, x + 96, 303), step=3, shade=-14)
    for rail_x in (x, x + 100):
        iron((rail_x, 45, rail_x + 3, 308))
        for y in (49, 104, 163, 222, 278, 304):
            stud(rail_x + 1, y)
    D.line((x + 7, 48, x + 96, 48), fill="#9d5f54")
    D.line((x + 7, 303, x + 96, 303), fill="#371b1d")


def cavity():
    # The opening stays translucent so the live veins show between the timber ribs.
    D.rectangle((104, 45, 319, 173), fill=(19, 8, 13, 95))
    for left, inner in ((104, 137), (319, 286)):
        lip = left + (6 if left == 104 else -6)
        points = [(left, 43), (inner, 53), (inner, 148), (lip, 173), (left, 173)]
        material("wood", (min(left, inner), 43, max(left, inner), 173), shade=-24, shape=points)
        D.line(points[1:4], fill="#5a3134", width=2)
        D.line((lip, 46, lip, 170), fill="#2d161d", width=2)
    D.polygon([(105, 173), (136, 149), (286, 149), (318, 173)], fill=(39, 15, 24, 217))
    D.line((136, 149, 286, 149), fill="#4c212c")
    D.line((132, 155, 290, 155), fill="#30131e")
    D.ellipse((160, 150, 263, 170), fill=(15, 3, 10, 210))
    cushion = [(162, 166), (169, 157), (183, 154), (240, 154), (253, 157),
               (261, 166), (257, 171), (167, 171)]
    material("velvet", (162, 154, 261, 171), step=2, shade=-2, shape=cushion)
    D.line([(165, 166), (178, 163), (242, 163), (258, 166)], fill="#a23648", width=2)
    D.line((169, 170, 254, 170), fill="#44101f", width=2)
    for x in range(170, 255, 5):
        D.point((x, 167), fill="#b98a77")
    for x in (165, 258):
        D.line((x, 166, x, 172), fill=BONE_LIGHT, width=2)


def vascular_plate():
    iron((104, 173, 319, 309))
    material("lacquer_deep", (108, 177, 315, 305), step=3, shade=-30)
    D.rectangle((110, 176, 313, 305), outline=BONE_DARK)
    D.rectangle((114, 189, 164, 302), fill="#211219", outline="#62373c")
    for y in (200, 223, 246, 269, 292):
        material("wood", (115, y - 9, 163, y + 9), step=3, shade=-22)
        D.line((116, y - 9, 162, y - 9), fill="#120b11")
        D.line((116, y + 9, 162, y + 9), fill="#61383b")
        D.point((161, y), fill="#926c61")
    for x in (110, 313):
        for y in (179, 301):
            stud(x, y)

    # Bone fretwork kept to the outer bevel, leaving clear space for region labels.
    for x in range(119, 307, 8):
        D.line([(x - 2, 304), (x, 302), (x + 2, 304)], fill="#9d785e")
    for x in (176, 307):
        D.line((x, 191, x, 299), fill="#593342")

    # The plate is etched rather than filled with a flat silhouette.
    contour = [(218, 188), (227, 192), (230, 200), (226, 210), (224, 214), (236, 219),
               (242, 226), (258, 246), (257, 251), (252, 251), (234, 233), (231, 243),
               (228, 257), (232, 270), (232, 294), (235, 300), (224, 300), (221, 278),
               (218, 270), (215, 278), (212, 300), (201, 300), (205, 294), (205, 270),
               (209, 257), (206, 243), (203, 233), (185, 251), (180, 251), (179, 246),
               (195, 226), (201, 219), (213, 214), (211, 210), (207, 200), (210, 192)]
    material("iron", (179, 188, 258, 300), shade=-17, shape=contour, step=2)
    D.line(contour + [contour[0]], fill="#9d795f", width=1)
    D.line([(219, 211), (219, 239), (216, 255), (219, 273), (228, 291)], fill="#9b5d5e")
    for y in (228, 233, 238, 243):
        D.line([(208, y - 3), (213, y - 1), (219, y), (225, y - 1), (230, y - 3)], fill="#76574e")
    D.line([(185, 245), (199, 224), (212, 219)], fill="#81514e")
    D.line([(225, 219), (238, 224), (252, 245)], fill="#81514e")
    D.line([(214, 260), (210, 280), (210, 295)], fill="#81514e")


def plinth():
    material("wood", (0, 313, 423, 330), step=3, shade=-1)
    D.line((0, 320, 423, 320), fill="#663735")
    D.line((0, 329, 423, 329), fill=DARK, width=2)
    iron((2, 308, 421, 315), shade=8)
    for x in (17, 87, 147, 277, 337, 407):
        stud(x, 312)
    material("sigil", (190, 316, 233, 328), step=1)
    for x, depth in ((63, 7), (139, 12), (282, 8), (318, 10)):
        D.polygon([(x - 5, 312), (x + 4, 312), (x + 2, 317), (x + 2, 312 + depth),
                   (x + 4, 314 + depth), (x - 2, 314 + depth), (x - 1, 319), (x - 3, 317)],
                  fill="#7e7357")
        D.line((x - 1, 315, x - 1, 312 + depth), fill="#a5936f")
    for x in (13, 378):
        toes = [(x, 330), (x + 29, 330), (x + 32, 335), (x + 32, 339), (x + 28, 343),
                (x + 23, 343), (x + 23, 339), (x + 18, 343), (x + 13, 343), (x + 13, 339),
                (x + 7, 343), (x + 2, 343), (x + 2, 339), (x, 339)]
        material("bone", (x, 330, x + 32, 343), shade=-6, shape=toes)
        D.line((x + 1, 331, x + 29, 331), fill="#9c826b")
        D.line((x + 4, 336, x + 27, 336), fill=BONE_LIGHT)


def relic():
    # Left hemisphere, painted as separate top, front and underside planes.
    # The right hemisphere shares its silhouette but has a different grain and light.
    top = [(4, 44), (12, 29), (26, 17), (45, 12), (57, 8), (77, 9), (85, 13),
           (83, 32), (70, 29), (56, 32), (44, 28), (31, 35), (17, 49)]
    face = [(4, 44), (17, 42), (31, 32), (44, 28), (56, 31), (70, 28), (83, 31),
            (84, 78), (71, 86), (59, 87), (47, 89), (27, 83), (14, 76), (6, 63)]
    lower = [(6, 63), (19, 69), (35, 74), (52, 79), (65, 78), (83, 72), (84, 83),
             (70, 96), (48, 96), (32, 91), (17, 84)]
    # The stem and cerebellum are seen between the hemispheres.
    material("brain", (65, 422, 108, 442), shade=-18, step=2,
             shape=[(70, 425), (81, 422), (98, 422), (108, 431), (105, 437), (94, 439),
                    (92, 442), (81, 442), (78, 437), (65, 435)])
    for side in (0, 1):
        def place(points):
            return [(173 - x if side else x, 344 + y) for x, y in points]

        bounds = (0, 348, 87, 442) if side == 0 else (86, 348, 173, 442)
        for plane, shade in ((lower, -33), (face, -8), (top, 13)):
            coords = place(plane)
            material("brain", bounds, shade=shade - side * 7, shape=coords, step=3, key=("relic", side))
        # Broader folds and a few buried vessels give the enlarged face a readable structure.
        for fold in ([(19, 49), (27, 43), (36, 41), (43, 46), (52, 46), (60, 41), (73, 40)],
                     [(12, 58), (24, 56), (32, 62), (44, 62), (49, 56), (63, 52), (81, 57)],
                     [(19, 72), (28, 68), (38, 74), (50, 75), (58, 70), (74, 67)],
                     [(36, 24), (47, 18), (58, 21), (66, 17), (79, 18)]):
            D.line(place(fold), fill="#663743", width=2)
            D.line(place([(x, y - 1) for x, y in fold]), fill="#cd9b8a")
        D.line(place([(28, 54), (36, 56), (42, 52), (49, 53), (53, 58)]), fill="#943139")
        D.line(place([(66, 63), (72, 60), (79, 62)]), fill="#b05250")


def fittings():
    material("lacquer_deep", (176, 344, 263, 355), shade=5)
    D.line((176, 344, 263, 344), fill="#a54c54")
    D.line((176, 355, 263, 355), fill="#895f55")
    material("velvet", (176, 356, 263, 379), shade=-39, step=2)
    D.line((176, 356, 263, 356), fill="#210c16")
    D.line((176, 379, 263, 379), fill="#532438")
    D.polygon([(178, 384), (193, 384), (195, 386), (195, 401), (193, 403),
               (178, 403), (176, 401), (176, 386)], fill="#8c735e")
    D.line([(176, 387), (178, 385), (193, 385)], fill=BONE_LIGHT)
    D.line([(177, 401), (193, 401), (194, 400)], fill=BONE_DARK)
    D.rectangle((178, 386, 193, 401), fill=(0, 0, 0, 0))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--preview", type=Path)
    args = parser.parse_args()
    wall(0)
    wall(320)
    cavity()
    lid()
    vascular_plate()
    plinth()
    relic()
    fittings()
    if args.check:
        with Image.open(ASSET) as saved:
            if saved.size != ATLAS.size or saved.convert("RGBA").tobytes() != ATLAS.tobytes():
                raise SystemExit("Regenerate the Mnemonic Reliquary atlas")
    else:
        ATLAS.save(ASSET, optimize=True)
    if args.preview:
        sheet = Image.new("RGBA", (424, 344), "#12070e")
        sheet.alpha_composite(ATLAS.crop((0, 0, 424, 344)))
        sheet.alpha_composite(ATLAS.crop((0, 344, 174, 448)), (125, 61))
        sheet.resize((848, 688), NEAREST).save(args.preview)


if __name__ == "__main__":
    main()
