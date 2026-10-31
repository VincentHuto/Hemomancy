"""The Bloom Warden: an Arbor elder of the undercroft's middle tiers, more moss than person, woken by a wrong done to the
Giving Light (Brined Court Track 10, Tier 2).

The Arbor's jointed body (court_arbor.py) gone further toward Marimo: Marimo's grey-green moss over most of the skin,
threaded with the Giving Light's gold filaments, the gold-orange lichen surviving in patches on the shoulders; a moss
mantle over the shoulders and back with a frond crest along it; frond spurs at the elbows it fights with; long hair
gone to moss and shoots; and heavy root masses down both shins that tear free when it uproots. The Sunburst canopy at
Primal comes from the strain layer, as on the Arbor. The eyes are open and gold. Nothing burns: the light is biological.

    python tools/model_export/court_bloom_warden.py [--preview]
"""
import argparse

from court_humanoid import PALETTES, add, flesh, h, hair_cap, humanoid, mix, paint_base, wrap_skirt, write

PAL = dict(PALETTES['giving_light'], skin=(120, 112, 88), eye=(252, 196, 72),
           hair=((86, 104, 62), (60, 74, 44), (116, 138, 76)))
SHOOTS = ((-3.0, -18), (-1.2, -6), (0.6, 6), (2.4, 14), (3.4, 24))


def rig():
    r = humanoid('bloom_warden', 'CourtBloomWardenModel')
    hair_cap(r, back=7.0)
    r.part('hair_fall', 'hair', (0, -2, 4.0), (10, 0, 0))
    r.cube('hair_fall', (-3.8, 0.0, -0.4, 7.6, 9.0, 0.9), 'hair')
    for i, (x, rot) in enumerate(SHOOTS):
        name = f'shoot_{i}'
        r.part(name, 'hair', (x, -8.0, 0.6), (-24, 0, rot))
        r.cube(name, (-0.4, -4.2, -0.4, 0.8, 4.2, 0.8), 'shoot')
        r.cube(name, (-1.2, -5.4, -0.3, 2.4, 1.4, 0.6), 'frond')
    # The moss mantle over the shoulders and down the back, its frond crest standing along the spine.
    r.part('mantle', 'body', (0, -10, 0))
    r.cube('mantle', (-4.6, -0.6, -2.6, 9.2, 3.2, 5.2), 'moss')
    r.cube('mantle', (-4.4, 2.4, 1.9, 8.8, 7.0, 0.9), 'moss')
    for i in range(4):
        name = f'crest_{i}'
        r.part(name, 'mantle', (-2.4 + i * 1.6, 0.2, 2.4), (-30 + i * 4, 0, -12 + i * 8))
        r.cube(name, (-0.5, -4.6 - (i % 2), -0.3, 1.0, 4.6 + (i % 2), 0.6), 'frond')
    # Frond spurs at the elbows: what it strikes with.
    for side, sign in (('right', -1), ('left', 1)):
        name = f'{side}_spur'
        r.part(name, f'{side}_forearm', (sign * 2.0, 1.0, 0.4), (10, 0, sign * 55))
        r.cube(name, (-0.5, -5.0, -0.5, 1.0, 5.0, 1.0), 'shoot')
        r.cube(name, (-1.3, -6.8, -0.3, 2.6, 2.0, 0.6), 'frond')
    wrap_skirt(r, 4.0, material='moss', front_split=True)
    # Root masses on the shins: thick strands that reach the ground while it is rooted.
    for side, sign in (('right', -1), ('left', 1)):
        r.cube(f'{side}_shin', (-2.4, 1.0, -2.4, 4.8, 4.0, 4.8), 'moss')
        for i, (x, length) in enumerate(((-1.4, 6.0), (0.0, 8.0), (1.4, 5.5), (0.6, 7.0))):
            name = f'{side}_root_{i}'
            r.part(name, f'{side}_shin', (x * sign, 1.0, -2.3 + (i // 3) * 4.4), (10 + 7 * i, 0, 9 * sign * (i - 1.5)))
            r.cube(name, (-0.45, 0.0, -0.45, 0.9, length, 0.9), 'root')
    return r


def moss(t):
    """Marimo's moss: grey-green, tufted, with gold filaments running through it."""
    n = h(t['gx'] * 1.3, t['gy'] * 1.3, t['gz'] * 1.3)
    rgb = mix((84, 102, 64), (120, 138, 84), n)
    if (t['u'] * 3 + t['v'] * 5) % 11 == 0:
        rgb = (232, 184, 80)
    elif n > 0.85:
        rgb = add(rgb, 18)
    return rgb


def lichen(t):
    n = h(t['gx'] * 0.7, t['gy'] * 0.7, t['gz'] * 0.7)
    rgb = mix((214, 132, 34), (240, 178, 62), n)
    return (252, 214, 112) if n > 0.85 else rgb


def paint(cube, t):
    m = cube['material']
    if m in ('torso', 'arm', 'leg'):
        n = h(t['gx'], t['gy'], t['gz'])
        if m == 'torso' and t['gy'] < 3.0 and n > 0.72:
            return lichen(t)
        if n < 0.82:
            return moss(t)
        return add(flesh(t, PAL['skin']), -8 if (t['u'] + t['v']) % 4 == 0 else 0)
    if m == 'moss':
        return moss(t)
    if m == 'shoot':
        return mix((112, 146, 52), (148, 180, 70), t['n'])
    if m == 'frond':
        return mix((236, 168, 52), (252, 206, 96), t['n'])
    if m == 'root':
        return mix((76, 60, 40), (108, 84, 54), t['n']) if t['v'] % 3 else (214, 150, 56)
    return paint_base(cube, t, PAL)


def glow(cube, t):
    m = cube['material']
    if m == 'frond' and t['n'] > 0.45:
        return (255, 206, 100)
    if m in ('moss', 'torso', 'arm', 'leg') and (t['u'] * 3 + t['v'] * 5) % 11 == 0 and h(t['gx'], t['gy'], t['gz']) < 0.82:
        return (252, 196, 90)
    if m == 'face' and t['face'] == 'north':
        px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
        side = px if px < 4 else 7 - px
        if py == 4 and side == 2:
            return (255, 214, 110)
    return None


POSES = [('rest', {}, ()),
         ('rooted', {'right_leg': (-5, 6, 0), 'left_leg': (-5, -6, 0), 'right_shin': (90, 0, 0),
                     'left_shin': (90, 0, 0), 'right_arm': (-150, 0, 20), 'left_arm': (-150, 0, -20)}, ()),
         ('lash', {'right_arm': (-40, 0, 80), 'left_arm': (-40, 0, -80), 'right_forearm': (-20, 0, 0),
                   'left_forearm': (-20, 0, 0), 'body': (20, 0, 0)}, ())]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    write(rig(), 'bloom_warden', 'court_bloom_warden.py', paint, glow, POSES, args.preview)


if __name__ == '__main__':
    main()
