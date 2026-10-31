"""Author the Brined Court's two full-block surfaces (Track 7): Sunburst Crust and Kindled Tissue.

- Sunburst Crust: coastal rock crusted gold-orange with the Giving Light's lichen, in rosettes with pale thick
  centres. Lines the Lighthouse Garden's terraces and walls; light 10, so a crusted wall glows at night.
- Kindled Tissue: the body of Hydra at the Vent Sanctum, the mass that members who did not keep a head became. Flame-
  coloured grown tissue in folds, pale polyp nodules budding through, animated in four slow frames as it breathes.
  Light 4.

Both are 16 x 16 at 16 px per block, on the vanilla cube_all parent. Emits the textures (and Kindled Tissue's .mcmeta),
block models and blockstates. `--preview` writes build/model-previews/court_cubes.png (each texture tiled 3 x 3).
"""
import argparse
import json
import math

from PIL import Image

from clairaudiograph import clamp, noise
from court_kit import (CRIMSON, FLAME, AMBER, POLYP, POLYP_LO, SUN, SUN_HI, SUN_LO, STONE, STONE_LO)
from fixture_atlas import ASSETS, ROOT


def crust():
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    for y in range(16):
        for x in range(16):
            n = noise('crust_rock', x, y)
            rock = clamp(STONE if n > .3 else STONE_LO, int(n * 16) - 8)
            # Rosettes: lichen grows out from a few centres and thins at their rims.
            best = min(math.hypot(((x - cx + 8) % 16) - 8, ((y - cy + 8) % 16) - 8) / r
                       for cx, cy, r in ((4, 4, 5.2), (12, 6, 4.2), (6, 12, 4.6), (13, 13, 3.4), (1, 9, 2.6)))
            if best < .45:
                rgb = SUN_HI if noise('crust_core', x, y) > .35 else SUN
            elif best < 1.0 and noise('crust_rim', x, y) < 1.15 - best:
                rgb = clamp(SUN if noise('crust', x, y) > .3 else SUN_LO, int(noise('crust_n', x, y) * 14) - 7)
            else:
                rgb = rock
            px[x, y] = rgb + (255,)
    return image


def tissue(frame, frames):
    image = Image.new('RGBA', (16, 16))
    px = image.load()
    swell = math.sin(frame / frames * math.tau)
    for y in range(16):
        for x in range(16):
            fold = math.sin((x + y * .6) * .9 + noise('fold', x // 3, y // 3) * 2.4)
            t = .55 + .3 * fold + .08 * swell
            stops = [AMBER, FLAME, CRIMSON]
            t = max(0.0, min(1.0, t)) * 2
            i = min(int(t), 1)
            f = t - i
            rgb = tuple(int(a + (b - a) * f) for a, b in zip(stops[i], stops[i + 1]))
            rgb = clamp(rgb, int(noise('tissue', x, y) * 14) - 7)
            # Polyp nodules budding through the mass, swelling a little with each breath.
            for cx, cy in ((4, 3), (11, 9), (6, 12), (14, 2)):
                d = math.hypot(((x - cx + 8) % 16) - 8, ((y - cy + 8) % 16) - 8)
                if d < 1.4 + .35 * swell:
                    rgb = POLYP if d < .8 else POLYP_LO
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
    sunburst = crust()
    sunburst.save(ASSETS / 'textures/block/sunburst_crust.png')
    cube('sunburst_crust')
    frames = 4
    strip = Image.new('RGBA', (16, 16 * frames))
    for f in range(frames):
        strip.paste(tissue(f, frames), (0, 16 * f))
    strip.save(ASSETS / 'textures/block/kindled_tissue.png')
    (ASSETS / 'textures/block/kindled_tissue.png.mcmeta').write_text(json.dumps(
        {'animation': {'frametime': 20, 'interpolate': True}}, indent=2) + '\n')
    cube('kindled_tissue')
    print('Exported Sunburst Crust and Kindled Tissue (4 frames)')
    if args.preview:
        sheet = Image.new('RGBA', (16 * 7, 16 * 3), (30, 24, 28, 255))
        first = strip.crop((0, 0, 16, 16))
        for x in range(3):
            for y in range(3):
                sheet.paste(sunburst, (16 * x, 16 * y))
                sheet.paste(first, (16 * (x + 4), 16 * y))
        out = ROOT / 'build/model-previews/court_cubes.png'
        out.parent.mkdir(parents=True, exist_ok=True)
        sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).convert('RGB').save(out)
        print(out)


if __name__ == '__main__':
    main()
