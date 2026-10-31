"""Author the Brined Court outposts' two full-block surfaces (Track 8): Sclerotium Shell and Current Tissue.

- Sclerotium Shell: the Still Bell's diving bell, grown, not forged. A hard frost blue-grey sclerotium rind laid in
  overlapping folds like a Hushbell's mantle, with darker seams and the odd pale bloom of frost. No light.
- Current Tissue: the Woven Current's pod walls. Violet-blue grown tissue run through with a branching vein net that
  carries a pale glow (light 6), animated in four slow frames as a pulse travels along the veins.

Both are 16 x 16 at 16 px per block, on the vanilla cube_all parent. Emits the textures (and Current Tissue's .mcmeta),
block models and blockstates. `--preview` writes build/model-previews/court_outpost_cubes.png (each tiled 3 x 3).
"""
import argparse
import json
import math

from PIL import Image

from clairaudiograph import clamp, noise
from court_outpost_kit import ROPE, ROPE_GLOW, ROPE_LO
from fixture_atlas import ASSETS, ROOT

RIND = (96, 112, 130)
RIND_HI = (132, 150, 168)
RIND_LO = (66, 78, 96)


def shell():
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            # Wavy overlapping folds, like a closed mantle: each fold's crest rises and falls across the block (one
            # full wave per 8 px, so the texture tiles), a pale lip on top and a deep crease under it. No straight
            # joints anywhere, so the rind never reads as masonry.
            wave = 1.6 * math.sin(x * math.tau / 8 + (y // 4) * 1.3) + .7 * math.sin(x * math.tau / 16 * 3)
            v = (y + wave) % 4
            n = noise('shell', x, y)
            # The rind is darker than the sleepers' mantle it was grown from, so they read against it.
            if v < .9:
                rgb = RIND_HI
            elif v > 3.1:
                rgb = clamp(RIND_LO, -12)
            elif v > 2.4:
                rgb = RIND_LO
            else:
                rgb = RIND
            if noise('frost', x // 2, y // 2) > .9 and v < 2.4:
                rgb = (176, 194, 210)
            px[x, y] = clamp(rgb, int(n * 6) - 3) + (255,)
    return image


def vein(x, y):
    """Distance-like value to a branching vein net (two trunks and their branches), 0 on a vein."""
    best = 9.0
    for ax, ay, bx, by in ((0, 4, 16, 9), (3, 16, 9, 0), (9, 6, 16, 2), (5, 11, 0, 15), (11, 9, 14, 16)):
        dx, dy = bx - ax, by - ay
        t = max(0.0, min(1.0, ((x - ax) * dx + (y - ay) * dy) / (dx * dx + dy * dy)))
        best = min(best, math.hypot(x - (ax + t * dx), y - (ay + t * dy)))
    return best


def tissue(frame, frames):
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            n = noise('ctissue', x, y)
            d = vein(x, y)
            rgb = clamp(ROPE if n > .35 else ROPE_LO, int(n * 12) - 6)
            if d < .75:
                # A pulse travelling along the veins: brightest where (x + y) matches the frame's phase.
                phase = math.sin((x + y) * .5 - frame / frames * math.tau)
                rgb = clamp(ROPE_GLOW, int(phase * 30) - 10)
            elif d < 1.4:
                rgb = clamp(rgb, 18)
            px[x, y] = rgb + (255,)
    return image


def cube(name):
    (ASSETS / f'models/block/{name}.json').write_text(json.dumps(
        {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'hemomancy:block/{name}'}}, indent=2) + '\n')
    (ASSETS / f'blockstates/{name}.json').write_text(json.dumps(
        {'variants': {'': {'model': f'hemomancy:block/{name}'}}}, indent=2) + '\n')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    sclerotium = shell()
    sclerotium.save(ASSETS / 'textures/block/sclerotium_shell.png')
    cube('sclerotium_shell')
    frames = 4
    strip = Image.new('RGBA', (16, 16 * frames))
    for f in range(frames):
        strip.paste(tissue(f, frames), (0, 16 * f))
    strip.save(ASSETS / 'textures/block/current_tissue.png')
    (ASSETS / 'textures/block/current_tissue.png.mcmeta').write_text(json.dumps(
        {'animation': {'frametime': 10, 'interpolate': True}}, indent=2) + '\n')
    cube('current_tissue')
    print('Exported Sclerotium Shell and Current Tissue (4 frames)')
    if args.preview:
        sheet = Image.new('RGBA', (16 * 7, 16 * 3), (30, 24, 28, 255))
        first = strip.crop((0, 0, 16, 16))
        for x in range(3):
            for y in range(3):
                sheet.paste(sclerotium, (16 * x, 16 * y))
                sheet.paste(first, (16 * (x + 4), 16 * y))
        out = ROOT / 'build/model-previews/court_outpost_cubes.png'
        out.parent.mkdir(parents=True, exist_ok=True)
        sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).convert('RGB').save(out)
        print(out)


if __name__ == '__main__':
    main()
