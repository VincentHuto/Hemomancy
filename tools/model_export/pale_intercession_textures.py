"""Paints the Pale Intercession's base and emissive textures (128x128) for PaleIntercessionModel.

The model is Our Lady's small veiled defender: a bone-white robe with grey water-stain folds, a pale veil with a
silver band across the eyes, two thin arms and a square halo of pale silver behind the head. The emissive sheet
carries only what glows: the eye band, the halo and a faint still-water sheen down the robe seams.

Run from the repository root:  python tools/model_export/pale_intercession_textures.py
"""
from __future__ import annotations

import pathlib
import random

from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/resources/assets/hemomancy/textures/entity/pale_intercession"
SIZE = 128

# Still-water palette: bone, pale grey, silver and a verdigris-silver glow (Unstained, never blood-side).
BONE = (236, 232, 222)
BONE_SHADE = (214, 208, 196)
GREY = (176, 178, 176)
GREY_DEEP = (138, 142, 142)
SILVER = (206, 214, 218)
SILVER_BRIGHT = (232, 240, 244)
VEIL = (226, 228, 226)
VEIL_SHADE = (196, 200, 200)
GLOW = (190, 236, 232)
GLOW_SOFT = (150, 206, 204)

# Box UV layouts as (u, v, w, h, d) from PaleIntercessionModel.createBodyLayer.
BOXES = {
    "torso": (0, 0, 8, 8, 5),
    "veil": (32, 0, 9, 9, 7),
    "veil_band": (64, 0, 7, 2, 1),
    "robe": (0, 40, 10, 15, 6),
    "skirt": (48, 40, 12, 10, 7),
    "left_arm": (0, 64, 3, 12, 3),
    "right_arm": (16, 64, 3, 12, 3),
    "halo_top": (0, 84, 12, 1, 1),
    "halo_bottom": (0, 88, 12, 1, 1),
    "halo_left": (0, 92, 1, 9, 1),
    "halo_right": (4, 92, 1, 9, 1),
}


def faces(u: int, v: int, w: int, h: int, d: int) -> dict[str, tuple[int, int, int, int]]:
    """The six face rectangles (x0, y0, x1, y1) of a standard Minecraft box layout."""
    return {
        "top": (u + d, v, u + d + w, v + d),
        "bottom": (u + d + w, v, u + d + 2 * w, v + d),
        "right": (u, v + d, u + d, v + d + h),
        "front": (u + d, v + d, u + d + w, v + d + h),
        "left": (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "back": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


def shade(colour: tuple[int, int, int], amount: float) -> tuple[int, int, int, int]:
    r, g, b = colour
    return (max(0, min(255, int(r * amount))), max(0, min(255, int(g * amount))),
            max(0, min(255, int(b * amount))), 255)


def fill(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], colour: tuple[int, int, int],
         amount: float = 1.0) -> None:
    x0, y0, x1, y1 = rect
    if x1 > x0 and y1 > y0:
        draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=shade(colour, amount))


def folds(draw: ImageDraw.ImageDraw, rect: tuple[int, int, int, int], rng: random.Random,
          base: tuple[int, int, int], dark: tuple[int, int, int], density: float = 0.18) -> None:
    """Vertical water-stain folds: thin darker streaks that run down a face."""
    x0, y0, x1, y1 = rect
    for x in range(x0, x1):
        if rng.random() < density:
            top = y0 + rng.randint(0, max(0, (y1 - y0) // 3))
            for y in range(top, y1):
                if rng.random() < 0.85:
                    draw.point((x, y), fill=shade(dark, 0.96 + 0.08 * rng.random()))
    # A lighter hem line two pixels above the bottom edge.
    if y1 - y0 > 4:
        for x in range(x0, x1):
            if rng.random() < 0.6:
                draw.point((x, y1 - 2), fill=shade(base, 1.04))


def paint_box(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int, int], colour: tuple[int, int, int],
              dark: tuple[int, int, int], rng: random.Random, fold: bool) -> None:
    f = faces(*box)
    fill(draw, f["top"], colour, 1.06)
    fill(draw, f["bottom"], dark, 0.9)
    fill(draw, f["front"], colour, 1.0)
    fill(draw, f["back"], colour, 0.94)
    fill(draw, f["left"], colour, 0.9)
    fill(draw, f["right"], colour, 0.9)
    if fold:
        for name in ("front", "back", "left", "right"):
            folds(draw, f[name], rng, colour, dark)


def paint_base() -> Image.Image:
    rng = random.Random(20261102)
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    paint_box(draw, BOXES["torso"], BONE, BONE_SHADE, rng, True)
    paint_box(draw, BOXES["robe"], BONE, GREY, rng, True)
    paint_box(draw, BOXES["skirt"], BONE_SHADE, GREY_DEEP, rng, True)
    paint_box(draw, BOXES["left_arm"], BONE, GREY, rng, True)
    paint_box(draw, BOXES["right_arm"], BONE, GREY, rng, True)
    paint_box(draw, BOXES["veil"], VEIL, VEIL_SHADE, rng, True)
    # The veil's front carries no face: a darker hollow where eyes would be, under the band.
    front = faces(*BOXES["veil"])["front"]
    x0, y0, x1, y1 = front
    fill(draw, (x0 + 2, y0 + 2, x1 - 2, y0 + 4), VEIL_SHADE, 0.82)
    paint_box(draw, BOXES["veil_band"], SILVER, GREY_DEEP, rng, False)
    for key in ("halo_top", "halo_bottom", "halo_left", "halo_right"):
        paint_box(draw, BOXES[key], SILVER, GREY, rng, False)
    # A silver hem on the robe skirt.
    skirt = faces(*BOXES["skirt"])
    for name in ("front", "back", "left", "right"):
        sx0, sy0, sx1, sy1 = skirt[name]
        fill(draw, (sx0, sy1 - 1, sx1, sy1), SILVER, 1.0)
    return image


def paint_emissive() -> Image.Image:
    rng = random.Random(20261103)
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    # The eye band glows fully; the halo glows soft; the robe seams carry a faint still-water sheen.
    band = faces(*BOXES["veil_band"])
    for name in ("front", "left", "right", "top"):
        fill(draw, band[name], GLOW, 1.0)
    for key in ("halo_top", "halo_bottom", "halo_left", "halo_right"):
        for name, rect in faces(*BOXES[key]).items():
            fill(draw, rect, GLOW_SOFT, 0.75)
    for key in ("robe", "skirt"):
        for name in ("front", "back"):
            x0, y0, x1, y1 = faces(*BOXES[key])[name]
            for y in range(y0, y1):
                if rng.random() < 0.35:
                    draw.point((x0 + (x1 - x0) // 2, y), fill=(*GLOW_SOFT, 90))
    return image


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    paint_base().save(OUT / "base.png")
    paint_emissive().save(OUT / "emissive.png")
    print(f"wrote {OUT / 'base.png'} and {OUT / 'emissive.png'}")


if __name__ == "__main__":
    main()
