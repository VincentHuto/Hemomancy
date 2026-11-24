"""Generate the concept's native-pixel material tiles and reusable nine-slices."""
from pathlib import Path
from random import Random

from PIL import Image, ImageDraw

ROOT = Path(__file__).parent / "textures"
PALETTES = {
    "blood": ("#332326", "#855047", "#e6a781", "#d9bb92", "#614139"),
    "unstained": ("#282e39", "#555f79", "#b4c6ee", "#d2d6db", "#53617a"),
    "fungal": ("#302a1d", "#695733", "#eaba69", "#d8c398", "#625234"),
    "still": ("#202e30", "#426463", "#9acdc5", "#c3d4c8", "#47625d"),
    "circus": ("#30212c", "#814356", "#f3aa91", "#e0c49e", "#783b50"),
}


def rgb(value):
    return tuple(bytes.fromhex(value[1:]))


def shade(value, amount):
    return tuple(max(0, min(255, channel + amount)) for channel in rgb(value))


for theme, (dark, metal, bright, leaf, stitch) in PALETTES.items():
    target = ROOT / theme
    target.mkdir(parents=True, exist_ok=True)
    for name, color in [("cover", dark), ("leaf", leaf)]:
        rng = Random(417)
        tile = Image.new("RGB", (64, 64))
        draw = ImageDraw.Draw(tile)
        for y in range(64):
            for x in range(64):
                grain = rng.choices([-9, -5, -2, 0, 3, 6], [1, 5, 11, 30, 7, 2])[0]
                draw.point((x, y), fill=shade(color, grain))
        for _ in range(68):
            x, y = rng.randrange(64), rng.randrange(64)
            length = rng.randrange(1, 5)
            for bit in range(length):
                draw.point(((x + bit) % 64, (y + bit // 3) % 64), fill=shade(color, -10))
        tile.save(target / f"{name}.png")

    for name in ("frame", "row", "row_focus", "row_disabled", "slot"):
        size = 32 if name == "frame" else 16
        end = size - 1
        base = stitch if name == "slot" else dark
        rim = bright if name == "row_focus" else metal
        if name == "row_disabled":
            rim = shade(dark, 16)
        sprite = Image.new("RGBA", (size, size))
        draw = ImageDraw.Draw(sprite)
        draw.polygon([(2, 0), (end-2, 0), (end, 2), (end, end-2),
                      (end-2, end), (2, end), (0, end-2), (0, 2)],
                     fill="#160f13", outline="#160f13")
        draw.rectangle((2, 2, end-2, end-2), fill=base, outline=rim)
        draw.line((3, end-2, end-2, end-2, end-2, 3), fill=shade(dark, -12))
        if name == "frame":
            draw.rectangle((4, 4, end-4, end-4), outline=stitch)
            for x in (4, end-4):
                for y in (4, end-4):
                    draw.polygon([(x, y-2), (x+2, y), (x, y+2), (x-2, y)], fill=metal)
                    draw.point((x, y), fill=bright)
        sprite.save(target / f"{name}.png")
