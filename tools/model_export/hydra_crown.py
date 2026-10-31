"""Author the Hydra Crown: the growth that makes a vent chimney one of Hydra's heads.

Set on the magma mouth of every chimney in a Vent Sanctum (placed at runtime, since the chimneys are wherever the
terrain put them). A pale polyp ring round the open mouth, and a crown of eight trailing tentacles: four lean out
over the compass points, four stand at the ring's corners. Colour runs from polyp-pale at the root through amber and
flame orange to crimson at the tips, and the bands ripple up the tentacles: the texture is animated (eight frames,
interpolated), colour and motion only. Nothing burns. The tips give a low glow (light 10) that marks a head in the
dark at Hydrothermal depth.

No facing. The model reaches past its cell (x -3..19, y 0..26), as a crown should; Block.box(2, 0, 2, 14, 4, 14) is
the ring that collides. Emits block/hydra_crown, its animated atlas and .mcmeta, the blockstate and a Blockbench
project. `--preview` writes build/model-previews/hydra_crown.png from frame 0.
"""
import argparse
import json
import math

import numpy as np
from PIL import Image, ImageDraw

import carved_stone
from clairaudiograph import noise, render
from court_kit import HALF_BRIGHT, SPECIAL, flame_gradient
from fixture_atlas import ASSETS, ROOT, box, build, model_json, separate, spin, write

ATLAS = 'hydra_crown'
FRAMES = 8
FRAME = [0]


def tentacle(face, lx, ly, w, h, key):
    _, y, _ = carved_stone.world(key, face, lx, ly)
    phase = FRAME[0] / FRAMES * math.tau
    t = (y - 2) / 24 + .09 * math.sin(phase - y * .55)
    n = noise('tentacle', key, face, lx, ly)
    rgb = flame_gradient(t, n)
    if (int(y * 2) + FRAME[0]) % 6 == 0:
        rgb = tuple(min(255, c + 26) for c in rgb)          # a brighter band riding up the tentacle
    return rgb + (255,)


def ring():
    e = [box('ring_n', (3, 0, 3), (13, 3, 5.5), 'polyp'),
         box('ring_s', (3, 0, 10.5), (13, 3, 13), 'polyp'),
         box('ring_w', (3, 0, 5.5), (5.5, 3, 10.5), 'polyp'),
         box('ring_e', (10.5, 0, 5.5), (13, 3, 10.5), 'polyp'),
         box('lip_n', (4, 3, 4), (12, 4, 5.5), 'polyp'),
         box('lip_s', (4, 3, 10.5), (12, 4, 12), 'polyp'),
         box('lip_w', (4, 3, 5.5), (5.5, 4, 10.5), 'polyp'),
         box('lip_e', (10.5, 3, 5.5), (12, 4, 10.5), 'polyp'),
         box('throat', (5.5, 0, 5.5), (10.5, 0.5, 10.5), 'ember', extra=HALF_BRIGHT)]
    return e


def crown():
    e = []
    # The four leaning tentacles, two segments each: out over the rim, then trailing further out and up.
    for name, lo, hi, axis, angle, origin, tip_lo, tip_hi, tip_angle, tip_origin in (
            ('n', (7, 3, 3.5), (9, 13, 5.5), 'x', -22.5, (8, 3, 4.5), (7.25, 12, 1.5), (8.75, 22, 3.5), -45, (8, 12, 2.5)),
            ('s', (7, 3, 10.5), (9, 13, 12.5), 'x', 22.5, (8, 3, 11.5), (7.25, 12, 12.5), (8.75, 22, 14.5), 45, (8, 12, 13.5)),
            ('w', (3.5, 3, 7), (5.5, 13, 9), 'z', 22.5, (4.5, 3, 8), (1.5, 12, 7.25), (3.5, 22, 8.75), 45, (2.5, 12, 8)),
            ('e', (10.5, 3, 7), (12.5, 13, 9), 'z', -22.5, (11.5, 3, 8), (12.5, 12, 7.25), (14.5, 22, 8.75), -45, (13.5, 12, 8))):
        e.append(box('tentacle_' + name, lo, hi, 'tentacle', rotation=spin(axis, angle, origin), extra=HALF_BRIGHT))
        e.append(box('trail_' + name, tip_lo, tip_hi, 'tentacle', rotation=spin(axis, tip_angle / 2, tip_origin),
                     extra=HALF_BRIGHT))
    # The four corner tentacles stand taller and nearly straight, turned on the diagonal.
    for name, x, z in (('nw', 4, 4), ('ne', 11, 4), ('sw', 4, 11), ('se', 11, 11)):
        e.append(box('corner_' + name, (x, 3, z), (x + 1.5, 19, z + 1.5), 'tentacle',
                     rotation=spin('y', 45, (x + .75, 3, z + .75)), extra=HALF_BRIGHT))
        e.append(box('corner_tip_' + name, (x + .25, 19, z + .25), (x + 1.25, 26, z + 1.25), 'tentacle',
                     rotation=spin('y', 45, (x + .75, 19, z + .75)), extra=HALF_BRIGHT))
    return e


def frame_preview(elements, frame0):
    texture = np.array(frame0.convert('RGBA'))
    views = [(0, 10), (30, 25), (-35, 25), (90, 10), (180, 25), (145, 30), (-60, 55), (15, -20)]
    model = json.loads(json.dumps(elements))
    for el in model:
        el['from'][1] -= 6
        el['to'][1] -= 6
        if 'rotation' in el:
            el['rotation']['origin'][1] -= 6
    tiles = [render(model, {'atlas': texture}, yaw, pitch, 360, 12) for yaw, pitch in views]
    sheet = Image.new('RGBA', (360 * 4, 360 * 2 + 30), (30, 24, 28, 255))
    for i, t in enumerate(tiles):
        sheet.paste(t, (360 * (i % 4), 30 + 360 * (i // 4)))
    ImageDraw.Draw(sheet).text((10, 8), 'Hydra Crown - frame 0 of the animated atlas', fill=(220, 200, 200))
    out = ROOT / 'build/model-previews/hydra_crown.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = ring() + crown()
    moved = separate(body)
    groups = {'body': body}
    carved_stone.register(groups)
    special = dict(SPECIAL, tentacle=tentacle)
    frames = []
    models = None
    for f in range(FRAMES):
        FRAME[0] = f
        atlas, models = build(groups, special)
        frames.append(atlas)
    width, height = frames[0].size
    strip = Image.new('RGBA', (width, height * FRAMES), (0, 0, 0, 0))
    for f, image in enumerate(frames):
        strip.paste(image, (0, f * height))
    strip.save(ASSETS / f'textures/block/{ATLAS}.png')
    (ASSETS / f'textures/block/{ATLAS}.png.mcmeta').write_text(json.dumps(
        {'animation': {'frametime': 3, 'interpolate': True, 'width': width, 'height': height}}, indent=2) + '\n')
    model = model_json(models['body'], ATLAS, height, f'hemomancy:block/{ATLAS}')
    write({'block/hydra_crown': model}, 'hydra_crown', model)
    (ASSETS / 'blockstates/hydra_crown.json').write_text(
        json.dumps({'variants': {'': {'model': 'hemomancy:block/hydra_crown'}}}, indent=2) + '\n')
    print(f'Exported Hydra Crown: {len(body)} cubes, {moved} faces separated, {FRAMES} frames of 128x{height}')
    if args.preview:
        frame_preview(model['elements'], frames[0])


if __name__ == '__main__':
    main()
