"""Rebuild the authored 256x128 Mortarbound UV palette texture."""
from pathlib import Path
from PIL import Image, ImageDraw
import random

OUT = Path(__file__).resolve().parents[2] / "src/main/resources/assets/hemomancy/textures/entity/mortarbound.png"
rng = random.Random(3197)
im = Image.new("RGBA", (256, 128), (12, 18, 23, 255))
d = ImageDraw.Draw(im)

def patch(box, colors, marks=80):
    x0, y0, x1, y1 = box
    d.rectangle((x0, y0, x1 - 1, y1 - 1), fill=colors[0])
    for _ in range(marks):
        x = rng.randrange(x0, x1)
        y = rng.randrange(y0, y1)
        d.rectangle((x, y, min(x1 - 1, x + rng.randrange(1, 3)),
                     min(y1 - 1, y + rng.randrange(1, 3))), fill=rng.choice(colors[1:]))

sculk = [(16, 31, 37), (23, 47, 52), (10, 23, 31), (34, 65, 66), (45, 82, 83)]
ink = [(6, 11, 18), (12, 20, 27), (22, 33, 37), (3, 8, 14)]
ivory = [(195, 199, 186), (231, 226, 206), (157, 170, 164), (120, 142, 144)]
glow = [(51, 153, 160), (111, 217, 209), (37, 108, 121)]

# Torso, veins, neck, head, face, and small cyan eye slits.
patch((0, 0, 38, 32), sculk, 260)
patch((40, 0, 66, 13), [(30, 82, 89), (44, 117, 123), (57, 145, 148)], 80)
patch((68, 0, 92, 15), sculk, 65)
patch((96, 0, 134, 22), ink, 160)
patch((136, 0, 154, 9), ink, 30)
patch((156, 0, 168, 8), glow, 35)

# Pale inkcap and dark dissolving brim.
patch((0, 36, 54, 58), ivory, 250)
patch((56, 36, 122, 58), ivory, 210)
patch((122, 36, 168, 52), ink, 110)
for x in range(56, 122, 3):
    d.line((x, 50, x + rng.randrange(-1, 2), 57), fill=rng.choice(ink), width=1)

# Distinct left and right limbs, lower body, and hanging ink.
for box in ((0, 64, 20, 90), (24, 64, 46, 78), (52, 64, 72, 90),
            (76, 64, 98, 78), (102, 64, 138, 82)):
    patch(box, sculk, 105)
for box in ((140, 64, 148, 84), (150, 64, 156, 94), (158, 64, 164, 96)):
    patch(box, ink, 45)

# The collapsed form is an almost black growth on the wall.
patch((176, 0, 228, 41), ink, 270)
patch((176, 42, 188, 76), ink, 55)
patch((188, 42, 200, 72), ink, 55)
patch((200, 42, 224, 60), ink, 55)
for _ in range(100):
    x = rng.randrange(176, 228)
    y = rng.randrange(0, 76)
    d.point((x, y), fill=rng.choice(((26, 72, 79), (39, 112, 119), (57, 143, 145))))

# The emerged lower body stays rooted in a darker, sculk-covered wall scar.
patch((0, 98, 32, 112), sculk, 100)
patch((36, 98, 78, 106), sculk, 110)
for _ in range(35):
    x = rng.randrange(36, 78)
    y = rng.randrange(98, 106)
    d.point((x, y), fill=rng.choice(glow))

OUT.parent.mkdir(parents=True, exist_ok=True)
im.save(OUT)
print(OUT)
