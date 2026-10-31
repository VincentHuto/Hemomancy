"""Author Marimo's presence in the Lighthouse Garden's undercroft (Brined Court Track 10): Marimo Moss and Marimo's Heart.

- Marimo Moss: the great moss mass that fills the undercroft's lowest chamber and threads up its walls. Grey-green and
  soft, older and paler than living moss, with fine gold filaments of the Giving Light's lichen running through it.
  Four slow frames: the filaments brighten and fade as the mass breathes. Light 4.
- Marimo's Heart: the densest knot of the mass, where the filaments gather into pale sun-cored nodes. Four frames of a
  slow pulse. Light 9; its block entity draws the pulse's glow over it.

Both are 16 x 16 at 16 px per block, on the vanilla cube_all parent. Emits the textures and their .mcmeta, block
models, blockstates and item models. `--preview` writes build/model-previews/marimo_moss.png (each tiled 3 x 3).
"""
import argparse
import json
import math

from PIL import Image

from clairaudiograph import clamp, noise
from court_kit import MOSS, MOSS_HI, MOSS_LO, STONE, SUN, SUN_CORE, SUN_HI
from fixture_atlas import ASSETS, ROOT

# Marimo's moss is greyer than living moss: the old growth of the lowest tiers.
GREY_MOSS = tuple((a * 2 + b) // 3 for a, b in zip(MOSS, STONE))
GREY_MOSS_LO = tuple((a * 2 + b) // 3 for a, b in zip(MOSS_LO, STONE))
GREY_MOSS_HI = tuple((a * 3 + b) // 4 for a, b in zip(MOSS_HI, STONE))
FRAMES = 4


def lerp(a, b, t):
    return tuple(int(x + (y - x) * t) for x, y in zip(a, b))


def filament(x, y, seed):
    """Distance-like value to the nearest wandering gold filament (0 on it), tiling on 16."""
    best = 9.0
    for i in range(3):
        phase = noise(seed, i) * math.tau
        for step in range(16):
            fx = (step + i * 5) % 16
            fy = (int(8 + 6 * math.sin(step * .45 + phase)) + i * 5) % 16
            d = math.hypot(((x - fx + 8) % 16) - 8, ((y - fy + 8) % 16) - 8)
            best = min(best, d)
    return best


def moss(frame):
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    breath = .5 + .5 * math.sin(frame / FRAMES * math.tau)
    for y in range(16):
        for x in range(16):
            n = noise('marimo', x, y)
            clump = noise('marimo_clump', x // 2, y // 2)
            rgb = GREY_MOSS_HI if clump > .72 else GREY_MOSS_LO if clump < .22 else GREY_MOSS
            rgb = clamp(rgb, int(n * 14) - 7)
            if filament(x, y, 'marimo_fil') < .5 and noise('marimo_gap', x, y) > .5:
                rgb = lerp(rgb, SUN, .35 + .4 * breath)
            px[x, y] = rgb + (255,)
    return image


def heart(frame):
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    pulse = .5 + .5 * math.sin(frame / FRAMES * math.tau)
    nodes = ((4, 4), (11, 6), (7, 12), (14, 13))
    for y in range(16):
        for x in range(16):
            n = noise('mheart', x, y)
            rgb = clamp(GREY_MOSS if noise('mheart_c', x // 2, y // 2) > .3 else GREY_MOSS_LO, int(n * 12) - 6)
            if filament(x, y, 'mheart_fil') < .8:
                rgb = lerp(rgb, SUN_HI, .5 + .35 * pulse)
            for cx, cy in nodes:
                d = math.hypot(((x - cx + 8) % 16) - 8, ((y - cy + 8) % 16) - 8)
                if d < 1.0 + .4 * pulse:
                    rgb = SUN_CORE
                elif d < 2.0 + .4 * pulse:
                    rgb = lerp(rgb, SUN_HI, .6)
            px[x, y] = rgb + (255,)
    return image


def write(name, painter):
    strip = Image.new('RGBA', (16, 16 * FRAMES))
    for f in range(FRAMES):
        strip.paste(painter(f), (0, 16 * f))
    strip.save(ASSETS / f'textures/block/{name}.png')
    (ASSETS / f'textures/block/{name}.png.mcmeta').write_text(json.dumps(
        {'animation': {'frametime': 30, 'interpolate': True}}, indent=2) + '\n')
    (ASSETS / f'models/block/{name}.json').write_text(json.dumps(
        {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'hemomancy:block/{name}'}}, indent=2) + '\n')
    (ASSETS / f'blockstates/{name}.json').write_text(json.dumps(
        {'variants': {'': {'model': f'hemomancy:block/{name}'}}}, indent=2) + '\n')
    (ASSETS / f'models/item/{name}.json').write_text(json.dumps(
        {'parent': f'hemomancy:block/{name}'}, indent=2) + '\n')
    return strip


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    mass = write('marimo_moss', moss)
    core = write('marimo_heart', heart)
    print('Exported Marimo Moss and Marimo\'s Heart (4 frames each)')
    if args.preview:
        sheet = Image.new('RGBA', (16 * 7, 16 * 3), (30, 24, 28, 255))
        for x in range(3):
            for y in range(3):
                sheet.paste(mass.crop((0, 0, 16, 16)), (16 * x, 16 * y))
                sheet.paste(core.crop((0, 0, 16, 16)), (16 * (x + 4), 16 * y))
        out = ROOT / 'build/model-previews/marimo_moss.png'
        out.parent.mkdir(parents=True, exist_ok=True)
        sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).convert('RGB').save(out)
        print(out)


if __name__ == '__main__':
    main()
