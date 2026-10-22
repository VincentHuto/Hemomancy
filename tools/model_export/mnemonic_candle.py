"""Author the Mnemonic Candle: a pillar of poured Mnemonic Ambergris bound in Enthralling Filament.

The candle stands in a hematic iron drip dish on bone feet, with a finger ring, a spent stub beside it,
and the dish floor flooded with spilled ambergris. The wax is marbled whale-memory: grey-olive with
nacre streaks, dark inclusions and faint crimson memory veins, with fresher drips running down over
two crimson-and-slate thread bands. The lit model adds a crimson-cored flame and a molten pool, both
full-bright through NeoForge element light data.

Emits the unlit and lit block models, the painted atlas and an editable Blockbench project; the item
model parents the unlit block. Python 3 + Pillow + NumPy. `--preview` writes textured turnarounds to
build/model-previews/mnemonic_candle.png and mnemonic_candle_lit.png.

The main wick sits on the block's centre line so MnemonicCandleBlock's smoke stays on the flame under
every random blockstate rotation; FLAME_TIP is the smoke height it uses.
"""
import argparse
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, PALETTE, box, build, mirror_x, model_json, preview, separate, spin, write

ATLAS = 'mnemonic_candle'
FLAME_TIP = 15  # block pixels; MnemonicCandleBlock emits smoke here
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

WAX = (108, 100, 78)
NACRE = (166, 152, 112)
INCLUSION = (66, 62, 48)
MEMORY_VEIN = (122, 30, 38)
SLATE = (44, 62, 66)
THREAD_RED = (150, 30, 40)


def octagon_prism(name, x0, x1, z0, z1, y0, y1, chamfer, mat, **kw):
    """An octagonal prism as three non-overlapping slabs, so no two top faces share a plane region."""
    return [box(f'{name}_core', (x0, y0, z0 + chamfer), (x1, y1, z1 - chamfer), mat, **kw),
            box(f'{name}_front', (x0 + chamfer, y0, z0), (x1 - chamfer, y1, z0 + chamfer), mat, **kw),
            box(f'{name}_back', (x0 + chamfer, y0, z1 - chamfer), (x1 - chamfer, y1, z1), mat, **kw)]


def dish():
    e = []
    for lo, hi in [((7.25, 0, 3.5), (8.75, .5, 4.5)), ((7.25, 0, 11.5), (8.75, .5, 12.5)),
                   ((3.5, 0, 7.25), (4.5, .5, 8.75)), ((11.5, 0, 7.25), (12.5, .5, 8.75))]:
        e.append(box('foot', lo, hi, 'bone', skip=('up',)))
    e += octagon_prism('dish_floor', 3, 13, 3, 13, .5, 1.25, 1.5, 'iron')
    e.append(box('rim_front', (4.5, 1.25, 3), (11.5, 2.25, 3.5), 'ferric', skip=('down',)))
    e.append(box('rim_back', (4.5, 1.25, 12.5), (11.5, 2.25, 13), 'ferric', skip=('down',)))
    e.append(box('rim_west', (3, 1.25, 4.5), (3.5, 2.25, 11.5), 'ferric', skip=('down',)))
    e.append(box('rim_east', (12.5, 1.25, 4.5), (13, 2.25, 11.5), 'ferric', skip=('down',)))
    # Bevelled corners: a wall turned 45 degrees across each chamfer.
    for cx, cz, angle in [(3.93, 3.93, 45), (12.07, 3.93, -45), (3.93, 12.07, -45), (12.07, 12.07, 45)]:
        e.append(box('rim_corner', (cx - 1.06, 1.25, cz - .25), (cx + 1.06, 2.25, cz + .25), 'ferric',
                     skip=('down',), rotation=spin('y', angle, (cx, 1.75, cz))))
    # Finger ring on the east side.
    e.append(box('ring_arm', (13, .75, 7.5), (15.5, 1.25, 8.5), 'iron'))
    e.append(box('ring_outer', (15, 1.25, 7.5), (15.5, 3, 8.5), 'iron'))
    e.append(box('ring_inner', (14, 1.25, 7.5), (14.5, 3, 8.5), 'iron'))
    e.append(box('ring_top', (14, 3, 7.5), (15.5, 3.5, 8.5), 'ferric'))
    # Spilled ambergris flooding the dish floor.
    e.append(box('pool', (4.25, 1.25, 4.5), (11.75, 1.5, 11.25), 'pool', skip=('down',)))
    e.append(box('pool_tongue', (6, 1.25, 3.5), (10, 1.375, 4.5), 'pool', skip=('down',)))
    return e


def candle():
    e = octagon_prism('pillar', 6, 10, 6, 10, 1.5, 10.5, .5, 'wax', up='wax_top', skip=('down',))
    for i, (y0, y1) in enumerate([(3, 3.5), (7.25, 7.75)]):
        e += octagon_prism(f'thread_band{i}', 5.75, 10.25, 5.75, 10.25, y0, y1, .5, 'thread')
    e.append(box('thread_end', (8.75, 1.75, 5.5), (9, 3, 5.75), 'thread', skip=('south',)))
    e.append(box('thread_knot', (8.625, 1.5, 5.375), (9.125, 2, 5.75), 'thread', skip=('south', 'down')))
    # Fresher drips, dribbled over the thread after it was tied.
    for lo, hi, blob_lo, blob_hi in [
            ((6.75, 6, 5.625), (7.5, 10.5, 6), (6.625, 5.5, 5.5), (7.625, 6.25, 6)),
            ((8.5, 8.25, 5.625), (9, 10.5, 6), (8.375, 7.875, 5.5), (9.125, 8.5, 6)),
            ((5.625, 4, 7.25), (6, 10.5, 8), (5.5, 3.5, 7.125), (6, 4.25, 8.125)),
            ((10, 7.75, 7.75), (10.375, 10.5, 8.5), (10, 7.25, 7.625), (10.5, 8, 8.625))]:
        e.append(box('drip', lo, hi, 'drip', skip=('down',)))
        e.append(box('drip_blob', blob_lo, blob_hi, 'drip'))
    # Uneven lip of the crater.
    for lo, hi in [((6, 10.5, 6.5), (7, 11, 8)), ((9, 10.5, 8), (10, 11.25, 9.5)), ((7.5, 10.5, 9.5), (9, 10.875, 10))]:
        e.append(box('lip', lo, hi, 'drip', skip=('down',)))
    e.append(box('wick', (7.75, 10.5, 7.75), (8.25, 12, 8.25), 'wick', skip=('down',)))
    # Spent stub beside it.
    e.append(box('stub', (10.75, 1.5, 8.75), (12.25, 4.5, 10.5), 'wax', up='wax_top', skip=('down',)))
    e.append(box('stub_drip', (11, 2.75, 8.5), (11.5, 4.5, 8.75), 'drip', skip=('south', 'down')))
    e.append(box('stub_wick', (11.375, 4.5, 9.5), (11.625, 5.25, 9.75), 'wick', skip=('down',)))
    return e


def flame():
    planes = []
    for angle in (45, -45):
        planes.append(box('flame', (7, 11.5, 8), (9, FLAME_TIP, 8), 'flame',
                          skip=('east', 'west', 'up', 'down'), rotation=spin('y', angle, (8, 13, 8)), density=4,
                          extra=BRIGHT))
    planes.append(box('molten_pool', (6.25, 10.55, 6.5), (9.75, 10.6, 9.5), 'molten',
                      skip=('north', 'south', 'east', 'west', 'down'), density=2, extra=BRIGHT))
    return planes


# --- special faces --------------------------------------------------------------------------

def wax(face, lx, ly, w, h, key):
    """Marbled Mnemonic Ambergris: diagonal nacre streaks, dark inclusions, faint memory veins."""
    n = noise('wax', key, face, lx, ly)
    streak = noise('streak', key, face, lx + ly // 3, ly // 3)
    if noise('memory', key, face, lx, ly // 3) < .025:
        return MEMORY_VEIN + (255,)
    if streak > .84:
        return clamp(NACRE, int(n * 10) - 5) + (255,)
    if streak < .06 and n < .4:
        return INCLUSION + (255,)
    glow = 10 if ly < 2 else (-10 if ly >= h - 2 else 0)   # wax is paler where it thins at the lip
    return clamp(WAX, glow + int(n * 12) - 6) + (255,)


def wax_top(face, lx, ly, w, h, key):
    """Crater: a pale worn lip around a darker sunken pool."""
    if lx in (0, w - 1) or ly in (0, h - 1):
        return clamp(NACRE, int(noise('lip', key, lx, ly) * 10) - 10) + (255,)
    return clamp(WAX, -18 + int(noise('crater', key, lx, ly) * 10)) + (255,)


def drip(face, lx, ly, w, h, key):
    n = noise('drip', key, face, lx, ly)
    rgb = clamp((160, 148, 110), int(n * 10) - 5 + (18 if lx == 0 else 0) - (16 if ly == h - 1 and h > 2 else 0))
    return rgb + (255,)


def pool(face, lx, ly, w, h, key):
    n = noise('pool', key, face, lx, ly)
    if n > .93:
        return NACRE + (255,)
    return clamp((106, 98, 72), int(n * 10) - 5) + (255,)


def thread(face, lx, ly, w, h, key):
    """Enthralling Filament (puppeteering_thread) wound round and round: slate twist with a crimson strand."""
    phase = (lx + ly) % 3
    if phase == 0:
        return THREAD_RED + (255,)
    if phase == 1 and noise('glint', key, face, lx) > .7:
        return (196, 96, 104, 255)
    return clamp(SLATE, int(noise('thread', key, face, lx, ly) * 10) - 5) + (255,)


def wick(face, lx, ly, w, h, key):
    return ((26, 20, 22) if ly < 2 else (58, 50, 46)) + (255,)


def flame_tex(face, lx, ly, w, h, key):
    """A teardrop memory-flame: pale bone-gold envelope, crimson core, darker root."""
    u = (lx + .5) / w * 2 - 1
    v = (ly + .5) / h
    hw = (v / .7) ** .7 if v < .7 else math.sqrt(max(0, 1 - ((v - .7) / .3) ** 2))
    if abs(u) > hw * .98:
        return (0, 0, 0, 0)
    if abs(u) < hw * .5 and v > .45:
        return ((118, 20, 40) if v > .86 else (206, 44, 54)) + (255,)
    return ((255, 242, 206) if v < .3 else (244, 214, 150)) + (255,)


def molten(face, lx, ly, w, h, key):
    n = noise('molten', lx, ly)
    if (lx + 2 * ly) % 7 == 0 and n > .4:
        return (196, 70, 70, 255)   # memory threads stirring in the melt
    return clamp((206, 176, 112), int(n * 16) - 8) + (255,)


SPECIAL = dict(wax=wax, wax_top=wax_top, drip=drip, pool=pool, thread=thread, wick=wick, flame=flame_tex,
               molten=molten)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    body = dish() + candle()
    moved = separate(body)
    lit = flame()
    for el in body + lit:
        el['density'] = 1   # one texel per unit: the mod's block texel density
    atlas, models = build({'body': body, 'lit': lit}, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'
    unlit = model_json(models['body'], ATLAS, atlas.height, particle)
    lit_model = model_json(models['body'] + models['lit'], ATLAS, atlas.height, particle)
    write({'block/mnemonic_candle': unlit, 'block/mnemonic_candle_lit': lit_model}, 'mnemonic_candle', lit_model)
    print(f'Exported Mnemonic Candle: {len(body)} cubes (+{len(lit)} lit), {moved} faces separated, '
          f'128x{atlas.height} atlas')
    if args.preview:
        preview(unlit['elements'], ATLAS, 'Mnemonic Candle - unlit block model, actual atlas', 'mnemonic_candle')
        preview(lit_model['elements'], ATLAS, 'Mnemonic Candle - lit block model (flame + melt are full-bright)',
                'mnemonic_candle_lit')


if __name__ == '__main__':
    main()
