"""Author the Blood Gutter: a deepslate-tile floor block with a blood channel cut into it (Hemorath's motif).

The top is vanilla deepslate tiles. A channel 6 px wide and 4 px deep runs from the centre out to each connected
side; it is lined in polished venous stone, rimmed with a 1 px hematic iron lip, and has a 2 px runnel down its
floor. Where the channel ends, turns or branches (and in a lone block) the centre is a sump: a pit 4 px deeper than
the channel floor under a hematic iron grate with a polished boss. In a straight run the centre simply carries the
channel floor and runnel on, so a run reads as one continuous channel. The channel's corners are studded with bone.
A side with no connection stays solid tile behind an iron lip.

Blockstate (multipart): `north/east/south/west` (connections) and `fill` 0-3.
  0 dry      dried residue in the runnels and a crust in any sump
  1 stained  a dark tacky film on the channel floor and a dark pool in any sump
  2 half     a blood surface halfway up the channel (y 14)
  3 full     the surface just under the iron lip (y 15)
The blood surface is the animated hemomancy:block/blood_still; the tiles, the venous lining and the blood use
world-aligned UVs so neighbouring gutters run on seamlessly, everything else is painted into the
`blood_gutter` atlas. Each direction's arm, closed side and fill is its own pre-turned model (no blockstate
rotation, so the world-aligned faces never turn).

Parts: blood_gutter_centre (always: base, corner tiles, lip posts); blood_gutter_sump (not a straight run) or
blood_gutter_straight_<ns|ew> (a straight run); blood_gutter_arm_<dir> (connected); blood_gutter_side_<dir>
(unconnected); and per fill n, blood_gutter_fill<n>_sump or blood_gutter_fill<n>_straight_<ns|ew> for the centre
plus blood_gutter_fill<n>_<dir> for each connected arm. "Not straight" is a multipart OR (STRAIGHT, NOT_STRAIGHT).
Also writes models/item/blood_gutter.json (a straight north-south run, half full), a Blockbench project and,
with `--preview`, build/model-previews/blood_gutter.png. 16 px per block. Python 3 + Pillow + NumPy.
"""
import argparse
import copy
import json

import numpy as np
from PIL import Image, ImageDraw

from clairaudiograph import clamp, noise, render
from export_scriptorium_upgrades import texture_image
from fixture_atlas import ASSETS, PALETTE, ROOT, box, build, write

ATLAS = 'blood_gutter'
DIRS = ('north', 'east', 'south', 'west')
TEXTURES = {'tiles': 'minecraft:block/deepslate_tiles', 'stone': 'hemomancy:block/polished_venous_stone',
            'blood': 'hemomancy:block/blood_still'}
WORLD = set(TEXTURES)

PIT = 8          # sump floor
FLOOR = 12       # channel floor
RUNNEL = 11      # runnel floor
LIP = 15         # the iron lip runs from here to the top
SURFACE = {2: 14.0, 3: 15.0}
FILM = .1        # residue and film float this far over what they lie on

TURN = {'north': 'east', 'east': 'south', 'south': 'west', 'west': 'north', 'up': 'up', 'down': 'down'}


def col(name, x0, z0, x1, z1, y0, y1, mat, **faces):
    return box(name, (x0, y0, z0), (x1, y1, z1), mat, **faces)


def lip_column(name, x0, z0, x1, z1, pit_face=None):
    """Venous stone under a 1 px iron lip; `pit_face` darkens that face's run below the channel floor."""
    e = []
    if pit_face:
        e.append(col(name + '_pit', x0, z0, x1, z1, PIT, FLOOR, 'stone', **{pit_face: 'pitwall'}))
        e.append(col(name + '_wall', x0, z0, x1, z1, FLOOR, LIP, 'stone'))
    else:
        e.append(col(name + '_wall', x0, z0, x1, z1, PIT, LIP, 'stone'))
    e.append(col(name + '_lip', x0, z0, x1, z1, LIP, 16, 'lip'))
    return e


# --- parts, authored for north ------------------------------------------------------------------

def centre():
    e = [box('base', (0, 0, 0), (16, PIT, 16), 'tiles', up='pit')]
    for x0, z0 in ((0, 0), (12, 0), (0, 12), (12, 12)):
        e.append(col('corner', x0, z0, x0 + 4, z0 + 4, PIT, 16, 'tiles'))
    for x0, z0 in ((4, 4), (11, 4), (4, 11), (11, 11)):
        e.append(col('post', x0, z0, x0 + 1, z0 + 1, PIT, LIP, 'stone'))
        e.append(col('post_cap', x0, z0, x0 + 1, z0 + 1, LIP, 16, 'lip', up='bone_stud'))
    return e


def sump():
    """Where the channel ends, turns or branches, the centre is a sump under a grate: a 1 px frame, a cross of bars
    and a polished boss, set flush with the channel floor. The pit below shows the base's dark top."""
    g = FLOOR - 1
    return [col('grate_frame', 5, 5, 11, 6, g, FLOOR, 'grate', skip=('north', 'east', 'west')),
          col('grate_frame', 5, 10, 11, 11, g, FLOOR, 'grate', skip=('south', 'east', 'west')),
          col('grate_frame', 5, 6, 6, 10, g, FLOOR, 'grate', skip=('west', 'north', 'south')),
          col('grate_frame', 10, 6, 11, 10, g, FLOOR, 'grate', skip=('east', 'north', 'south')),
          col('grate_bar', 7.5, 6, 8.5, 10, g + .25, FLOOR - .25, 'grate', skip=('north', 'south')),
          col('grate_bar', 6, 7.5, 7.5, 8.5, g + .25, FLOOR - .25, 'grate', skip=('west', 'east')),
          col('grate_bar', 8.5, 7.5, 10, 8.5, g + .25, FLOOR - .25, 'grate', skip=('west', 'east')),
          col('grate_boss', 7, 7, 9, 9, g, FLOOR, 'boss')]


def straight():
    """The centre of a straight north-south run: the channel floor and its runnel carried on through, no sump.
    Turned a quarter for an east-west run."""
    return [col('floor_w', 5, 5, 7, 11, PIT, FLOOR, 'stone'),
            col('floor_e', 9, 5, 11, 11, PIT, FLOOR, 'stone'),
            col('runnel', 7, 5, 9, 11, PIT, RUNNEL, 'stone')]


def arm():
    """The channel running from the sump out through the north edge."""
    e = lip_column('lip_w', 4, 0, 5, 4) + lip_column('lip_e', 11, 0, 12, 4)
    e += [col('floor_w', 5, 0, 7, 5, PIT, FLOOR, 'stone', south='pitwall'),
          col('floor_e', 9, 0, 11, 5, PIT, FLOOR, 'stone', south='pitwall'),
          col('runnel', 7, 0, 9, 5, PIT, RUNNEL, 'stone', south='pitwall')]
    return e


def side():
    """A closed side: solid tile behind an iron lip along the sump's edge."""
    return [col('tile', 4, 0, 12, 4, PIT, 16, 'tiles')] + lip_column('lip', 5, 4, 11, 5, pit_face='south')


def surface(name, x0, z0, x1, z1, y, end=None):
    """A blood surface over the channel; `end` also closes the volume on that open edge."""
    faces = {'up'} | ({end} if end else set())
    return box(name, (x0, FLOOR if end else PIT, z0), (x1, y, z1), 'blood',
               skip=[f for f in ('north', 'east', 'south', 'west', 'up', 'down') if f not in faces])


def flat(name, x0, z0, x1, z1, y, mat):
    return box(name, (x0, y, z0), (x1, y + .01, z1), mat, skip=('north', 'east', 'south', 'west', 'down'))


def fill_sump(level):
    if level == 0:
        return [flat('crust', 5, 5, 11, 11, PIT + FILM, 'crust')]
    if level == 1:
        return [flat('pool', 5, 5, 11, 11, PIT + 1.5, 'pool')]
    return [surface('surface', 5, 5, 11, 11, SURFACE[level])]


def fill_straight(level):
    """A straight run's centre wets like its arms: residue in the runnel, a film, or a surface over the channel."""
    if level == 0:
        return [flat('residue', 7, 5, 9, 11, RUNNEL + FILM, 'residue')]
    if level == 1:
        return [flat('film', 5, 5, 7, 11, FLOOR + FILM, 'film'), flat('film', 9, 5, 11, 11, FLOOR + FILM, 'film'),
                flat('film_runnel', 7, 5, 9, 11, RUNNEL + FILM, 'film')]
    return [box('surface', (5, FLOOR, 5), (11, SURFACE[level], 11), 'blood',
                skip=('north', 'east', 'south', 'west', 'down'))]


def fill_arm(level):
    if level == 0:
        return [flat('residue', 7, 0, 9, 5, RUNNEL + FILM, 'residue')]
    if level == 1:
        return [flat('film', 5, 0, 7, 5, FLOOR + FILM, 'film'), flat('film', 9, 0, 11, 5, FLOOR + FILM, 'film'),
                flat('film_runnel', 7, 0, 9, 5, RUNNEL + FILM, 'film')]
    return [surface('surface', 5, 0, 11, 5, SURFACE[level], end='north')]


# --- turning ------------------------------------------------------------------------------------

def turn(el, times):
    """Quarter-turns clockwise seen from above (north -> east), about the block centre."""
    out = copy.deepcopy(el)
    for _ in range(times):
        (x0, y0, z0), (x1, y1, z1) = out['lo'], out['hi']
        out['lo'], out['hi'] = [16 - z1, y0, x0], [16 - z0, y1, x1]
        out['faces'] = {TURN[k]: v for k, v in out['faces'].items()}
        out['skip'] = {TURN[s] for s in out['skip']}
    return out


def turned(elements, direction):
    return [turn(el, DIRS.index(direction)) for el in elements]


# --- painters -----------------------------------------------------------------------------------

def lip_tex(face, lx, ly, w, h, key):
    """Hematic iron: a cold sheen on top, darker on the drop, a rivet every four texels."""
    n = noise('lip', key, face, lx, ly)
    along = max(lx, ly) if face == 'up' else lx
    if face == 'up':
        rgb = clamp(PALETTE['iron'], 10 + int(n * 10) - 5)
        if along % 4 == 1:
            rgb = clamp(PALETTE['iron'], 30)
    else:
        rgb = clamp(PALETTE['iron'], -8 + int(n * 10) - 5)
    if noise('vein', key, face, lx, ly) < .04:
        rgb = (104, 26, 36)
    return rgb + (255,)


def bone_stud(face, lx, ly, w, h, key):
    """Aged bone, a shade under the fixture palette so the studs read as accents, not lights."""
    return clamp((184, 168, 138), int(noise('stud', key) * 12) - 6) + (255,)


def pit_tex(face, lx, ly, w, h, key):
    n = noise('pit', lx, ly)
    rgb = clamp((24, 6, 9), int(n * 8) - 4)
    if n > .9:
        rgb = (44, 10, 14)
    return rgb + (255,)


def pitwall(face, lx, ly, w, h, key):
    """The sump's sides: venous stone sinking into shadow."""
    n = noise('pitwall', key, face, lx, ly)
    shade = -ly * 26 // max(h, 1)
    rgb = clamp((70, 8, 14), shade + int(n * 12) - 6)
    if ly == 0:
        rgb = clamp((96, 12, 20), int(n * 8))
    return rgb + (255,)


def grate_tex(face, lx, ly, w, h, key):
    n = noise('grate', key, face, lx, ly)
    rgb = clamp(PALETTE['iron'], int(n * 12) - 6 + (10 if face == 'up' else -12))
    if face == 'up' and noise('vein', key, lx, ly) < .08:
        rgb = (100, 24, 34)
    return rgb + (255,)


def boss_tex(face, lx, ly, w, h, key):
    if face != 'up':
        return clamp(PALETTE['ferric'], -20) + (255,)
    return clamp(PALETTE['ferric'], (22 if (lx, ly) == (0, 0) else 6 if lx + ly == 1 else -8)) + (255,)


def residue(face, lx, ly, w, h, key):
    """Dried blood in the runnel: a broken brown-black streak."""
    n = noise('residue', key, lx, ly)
    if n < .45:
        return (0, 0, 0, 0)
    return ((46, 14, 12) if n > .75 else (62, 18, 16)) + (255,)


def crust(face, lx, ly, w, h, key):
    """The sump's floor caked in dried blood, crazed into flakes."""
    n = noise('crust', lx, ly)
    if (lx + ly * 2) % 5 == 0 and n > .4:
        return (22, 6, 6, 255)
    return clamp((58, 16, 14), int(n * 14) - 7) + (255,)


def film(face, lx, ly, w, h, key):
    """A dark tacky film: not yet dry, a few glossy points, the stone showing through in pinholes."""
    n = noise('film', key, lx, ly)
    if n < .1:
        return (0, 0, 0, 0)
    if n > .96:
        return (150, 34, 40, 255)
    return clamp((76, 8, 14), int(noise('film_tone', key, lx // 2, ly) * 18) - 9) + (255,)


def pool(face, lx, ly, w, h, key):
    """What drained into the sump: a still, dark pool with a glint."""
    n = noise('pool', lx, ly)
    if (lx, ly) in ((1, 1), (2, 1)):
        return (168, 42, 46, 255)
    return clamp((84, 6, 14), int(n * 12) - 6) + (255,)


SPECIAL = dict(lip=lip_tex, bone_stud=bone_stud, pit=pit_tex, pitwall=pitwall, grate=grate_tex, boss=boss_tex,
               residue=residue, crust=crust, film=film, pool=pool)


# --- emitting -----------------------------------------------------------------------------------

FACES = ('north', 'east', 'south', 'west', 'up', 'down')
CULL = {'down': (1, 0), 'up': (1, 16), 'north': (2, 0), 'south': (2, 16), 'west': (0, 0), 'east': (0, 16)}


def world_uv(face, lo, hi):
    """Vanilla's default UVs for a face, so world-textured faces line up across blocks."""
    x0, y0, z0 = lo
    x1, y1, z1 = hi
    return {'down': [x0, 16 - z1, x1, 16 - z0], 'up': [x0, z0, x1, z1],
            'north': [16 - x1, 16 - y1, 16 - x0, 16 - y0], 'south': [x0, 16 - y1, x1, 16 - y0],
            'west': [z0, 16 - y1, z1, 16 - y0], 'east': [16 - z1, 16 - y1, 16 - z0, 16 - y0]}[face]


def mat_of(el, face):
    return el['faces'].get(face, el['mat'])


def split(groups, world_textures=WORLD):
    """Atlas-only copies of every element (world faces skipped) for `build`, and the world faces' elements.
    `world_textures` are the materials drawn from their own texture with world-aligned UVs."""
    atlas_groups, world = {}, {}
    for g, elements in groups.items():
        atlas_groups[g], world[g] = [], []
        for el in elements:
            live = [f for f in FACES if f not in el['skip']]
            painted = [f for f in live if mat_of(el, f) not in world_textures]
            if painted:
                a = copy.deepcopy(el)
                a['skip'] = set(FACES) - set(painted)
                atlas_groups[g].append(a)
            faces = {}
            for f in live:
                m = mat_of(el, f)
                if m not in world_textures:
                    continue
                faces[f] = {'uv': [round(v, 4) for v in world_uv(f, el['lo'], el['hi'])], 'texture': '#' + m}
                axis, value = CULL[f]
                bound = el['lo'][axis] if f in ('down', 'north', 'west') else el['hi'][axis]
                if bound == value:
                    faces[f]['cullface'] = f
            if faces:
                world[g].append({'name': el['name'], 'from': [round(v, 4) for v in el['lo']],
                                 'to': [round(v, 4) for v in el['hi']], 'faces': faces})
    return atlas_groups, world


def model(elements, height):
    return {'parent': 'minecraft:block/block', 'render_type': 'minecraft:cutout', 'ambientocclusion': False,
            'texture_size': [128, height],
            'textures': dict({'atlas': f'hemomancy:block/{ATLAS}', 'particle': TEXTURES['tiles']}, **TEXTURES),
            'elements': elements}


STRAIGHT = {'ns': {'north': 'true', 'south': 'true', 'east': 'false', 'west': 'false'},
            'ew': {'north': 'false', 'south': 'false', 'east': 'true', 'west': 'true'}}
# Every state that is not a straight run: an end, a corner, a tee, a cross or a lone block.
NOT_STRAIGHT = [{'north': 'true', 'south': 'false'}, {'north': 'false', 'south': 'true'},
                {'east': 'true', 'west': 'false'}, {'east': 'false', 'west': 'true'},
                {d: 'true' for d in DIRS}, {d: 'false' for d in DIRS}]


def blockstate():
    model = lambda name: {'model': f'hemomancy:block/blood_gutter_{name}'}
    parts = [{'apply': model('centre')},
             {'when': {'OR': NOT_STRAIGHT}, 'apply': model('sump')}]
    for axis, when in STRAIGHT.items():
        parts.append({'when': dict(when), 'apply': model(f'straight_{axis}')})
    for d in DIRS:
        parts.append({'when': {d: 'true'}, 'apply': model(f'arm_{d}')})
        parts.append({'when': {d: 'false'}, 'apply': model(f'side_{d}')})
    for level in range(4):
        fill = {'fill': str(level)}
        parts.append({'when': {'OR': [dict(c, **fill) for c in NOT_STRAIGHT]}, 'apply': model(f'fill{level}_sump')})
        for axis, when in STRAIGHT.items():
            parts.append({'when': dict(when, **fill), 'apply': model(f'fill{level}_straight_{axis}')})
        for d in DIRS:
            parts.append({'when': {d: 'true', 'fill': str(level)}, 'apply': model(f'fill{level}_{d}')})
    return {'multipart': parts}


def straight_axis(connected):
    """'ns' or 'ew' for a straight run, else None."""
    c = set(connected)
    return 'ns' if c == {'north', 'south'} else 'ew' if c == {'east', 'west'} else None


def assemble(built, connected, level):
    """The element list one block state renders, for previews and the item."""
    axis = straight_axis(connected)
    out = list(built['centre']) + (built[f'straight_{axis}'] if axis else built['sump'])
    for d in DIRS:
        out += built[f'arm_{d}'] if d in connected else built[f'side_{d}']
    out += built[f'fill{level}_straight_{axis}'] if axis else built[f'fill{level}_sump']
    for d in connected:
        out += built[f'fill{level}_{d}']
    return out


# --- preview ------------------------------------------------------------------------------------

def textures_for_preview():
    out = {'atlas': np.array(Image.open(ASSETS / f'textures/block/{ATLAS}.png').convert('RGBA'))}
    for key, resource in TEXTURES.items():
        image = texture_image(resource).convert('RGBA')
        out[key] = np.array(image.crop((0, 0, 16, 16)))
    return out


def shifted(elements, dx=0, dy=0, dz=0):
    out = copy.deepcopy(elements)
    for el in out:
        for k, d in enumerate((dx, dy, dz)):
            el['from'][k] += d
            el['to'][k] += d
    return out


def preview(built):
    tex = textures_for_preview()
    singles = [('lone sump, dry', (), 0), ('straight N-S, stained', ('north', 'south'), 1),
               ('tee, half', ('north', 'east', 'west'), 2), ('cross, full', DIRS, 3),
               ('straight E-W, half', ('east', 'west'), 2), ('end, stained', ('north',), 1)]
    small, big = 300, 560
    sheet = Image.new('RGBA', (small * 6, 30 + small * 2 + big), (30, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for i, (label, conn, level) in enumerate(singles):
        els = shifted(assemble(built, conn, level), dy=-4)
        sheet.paste(render(els, tex, 25, 50, small, 13), (small * i, 30))
        draw.text((small * i + 8, 34), label, fill=(220, 200, 200))
        sheet.paste(render(els, tex, 200, 70, small, 13), (small * i, 30 + small))
    # A floor: a long straight run (dry end to half) into a tee, full to the east, half round a corner to the west.
    plain = [{'from': [0, 0, 0], 'to': [16, 16, 16],
              'faces': {f: {'uv': world_uv(f, (0, 0, 0), (16, 16, 16)), 'texture': '#tiles'} for f in FACES}}]
    scene = []
    layout = {(0, -3): (('south',), 0), (0, -2): (('north', 'south'), 0), (0, -1): (('north', 'south'), 1),
              (0, 0): (('north', 'south'), 2), (0, 1): (('north', 'east', 'west'), 2),
              (1, 1): (('east', 'west'), 3), (2, 1): (('west',), 3),
              (-1, 1): (('east', 'west'), 2), (-2, 1): (('east', 'south'), 2), (-2, 2): (('north',), 2)}
    for gx in range(-2, 3):
        for gz in range(-3, 3):
            conn, level = layout.get((gx, gz), (None, None))
            elements = assemble(built, conn, level) if conn else plain
            scene += shifted(elements, gx * 16, -12, gz * 16 + 8)
    for j, (yaw, pitch) in enumerate(((150, 40), (210, 62), (180, 88))):
        x = j * (small * 6 // 3)
        sheet.paste(render(scene, tex, yaw, pitch, big, 5.6), (x + 30, 30 + small * 2))
    draw.text((10, 8), 'Blood Gutter - fill 0 dry / 1 stained / 2 half / 3 full; bottom: a straight run into a tee in a deepslate tile floor',
              fill=(220, 200, 200))
    out = ROOT / 'build/model-previews/blood_gutter.png'
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert('RGB').save(out)
    print(out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'centre': centre(), 'sump': sump(), 'straight_ns': straight(), 'straight_ew': turned(straight(), 'east')}
    for d in DIRS:
        groups[f'arm_{d}'] = turned(arm(), d)
        groups[f'side_{d}'] = turned(side(), d)
    for level in range(4):
        groups[f'fill{level}_sump'] = fill_sump(level)
        groups[f'fill{level}_straight_ns'] = fill_straight(level)
        groups[f'fill{level}_straight_ew'] = turned(fill_straight(level), 'east')
        for d in DIRS:
            groups[f'fill{level}_{d}'] = turned(fill_arm(level), d)
    atlas_groups, world = split(groups)
    atlas, painted = build(atlas_groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    built = {g: world[g] + painted[g] for g in groups}

    outputs = {f'block/blood_gutter_{g}': model(built[g], atlas.height) for g in groups}
    item = model(assemble(built, ('north', 'south'), 2), atlas.height)
    for el in item['elements']:
        for f in el['faces'].values():
            f.pop('cullface', None)
    outputs['item/blood_gutter'] = item
    write(outputs, 'blood_gutter', model(assemble(built, DIRS, 2), atlas.height))
    (ASSETS / 'blockstates/blood_gutter.json').write_text(json.dumps(blockstate(), indent=2) + '\n')
    print(f'Exported Blood Gutter: {len(groups)} part models, 128x{atlas.height} atlas')
    if args.preview:
        preview(built)


if __name__ == '__main__':
    main()
