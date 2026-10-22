"""Unstained armor: white lacquered plate over white robes, patinated copper fittings, pale silver trim, glass helm.

Emits UnstainedArmorModel.java (four layer definitions plus robe animation) and both armor textures, painted at
the mod's block density (one texel per model unit):
layer_1 carries the helm, cuirass and sabatons; layer_2 carries the belted tabards and leg harness.

    python tools/model_export/unstained_armor.py            # write model + textures
    python tools/model_export/unstained_armor.py --preview  # also write build/unstained_armor_preview.png

java_model_to_bbmodel.mjs reads one layer per class, so there is no generated Blockbench project for this set.

The Guardians' glass helm means they do not breathe the air the infected breathe: a caged dome of clear glass with
copper censer vents at the ears and a crescent finial for the Lady. No blood red, no brass.
"""
from pathlib import Path
import argparse
import math

import numpy as np
from PIL import Image

from unstained_weapons import add, ceil, f, mix, rot_matrix
from vestment_of_the_final_molt import (MANNEQUIN, MANNEQUIN_COLOURS, SCALE, Rig, camel, paint_atlas, quads)

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/armor/UnstainedArmorModel.java'
TEXTURES = ASSETS / 'textures/models/armor'
ATLAS_WIDTH = 128

STANDARD = (('head', None), ('hat', None), ('body', None), ('right_arm', (-5, 2, 0)), ('left_arm', (5, 2, 0)),
            ('right_leg', (-1.9, 12, 0)), ('left_leg', (1.9, 12, 0)))


def base_rig():
    r = Rig()
    for name, pivot in STANDARD:
        r.part(name, None, pivot or (0, 0, 0))
    return r


# ---------------------------------------------------------------- rigs

def helmet():
    r = base_rig()
    # Clear glass dome in tiers, read round, over a pale silver gorget.
    r.cube('head', (-4.6, -8.75, -4.15, 9.2, 7.6, 8.3), 'glass')
    r.cube('head', (-4.15, -8.75, -4.6, 8.3, 7.6, 9.2), 'glass')
    r.cube('head', (-3.85, -9.5, -3.85, 7.7, 0.75, 7.7), 'glass')
    r.cube('head', (-2.75, -10.0, -2.75, 5.5, 0.5, 5.5), 'glass')
    r.cube('head', (-4.85, -1.25, -4.85, 9.7, 1.75, 9.7), 'gorget', hole=4.6)
    r.cube('head', (-4.8, -6.0, -4.8, 9.6, 0.5, 9.6), 'brow', hole=4.6)
    # Silver cage ribs over the dome.
    r.cube('head', (-0.4, -9.6, -4.9, 0.8, 3.6, 0.3), 'silver')
    r.cube('head', (-0.4, -9.6, 4.6, 0.8, 8.4, 0.3), 'silver')
    r.mirrored('head', (4.6, -8.9, -0.4, 0.3, 7.65, 0.8), 'silver')
    r.cube('head', (-0.4, -10.3, -3.0, 0.8, 0.3, 6.0), 'silver')
    r.cube('head', (-3.0, -10.3, -0.4, 6.0, 0.3, 0.8), 'silver')
    r.mirrored('head', (3.85, -9.75, -0.4, 0.3, 1.0, 0.8), 'silver')
    # Copper censer vents at the ears and a breathing grille under the chin.
    r.mirrored('head', (4.7, -4.75, -1.5, 0.6, 2.75, 3.0), 'vent')
    r.cube('head', (-1.5, -1.6, -5.2, 3.0, 1.25, 0.5), 'grille')
    # Rebreather intakes at the nape: the hoses from the back canisters seat here.
    r.mirrored('head', (1.35, -2.4, 4.6, 1.1, 1.3, 0.9), 'port')
    # Crescent finial for Our Lady of Still Waters.
    r.cube('head', (-0.4, -11.25, -0.4, 0.8, 1.0, 0.8), 'verdigris')
    r.cube('head', (-2.0, -14.0, 0, 4.0, 3.0, 0), 'crescent')
    return r


def chestplate():
    r = base_rig()
    r.cube('body', (-4.3, -0.3, -2.3, 8.6, 12.4, 4.6), 'linen')
    r.cube('body', (-4.65, 0.0, -2.9, 9.3, 7.75, 5.8), 'cuirass')
    r.cube('body', (-4.4, -0.75, -2.7, 8.8, 1.0, 5.4), 'band', hole=4.3)
    r.cube('body', (-4.55, 7.75, -2.8, 9.1, 1.5, 5.6), 'lame')
    r.cube('body', (-4.4, 9.25, -2.7, 8.8, 1.5, 5.4), 'lame')
    r.mirrored('body', (3.0, -0.25, 2.75, 1.25, 1.25, 0.75), 'verdigris')
    # Rebreather: twin canisters of still water and a copper-salt scrubber on a silver frame; corrugated hoses
    # rise to the helm's nape intakes so the wearer breathes only what the water and salts have cleaned.
    r.cube('body', (-3.5, 0.5, 2.95, 7.0, 8.5, 0.4), 'frame')
    for x in (1.9, -1.9):
        r.cube('body', (x - 1.25, 1.75, 3.8, 2.5, 6.5, 1.8), 'canister')
        r.cube('body', (x - 0.9, 1.75, 3.45, 1.8, 6.5, 2.5), 'canister')
        r.cube('body', (x - 1.45, 0.75, 3.25, 2.9, 1.0, 2.9), 'cap')
        r.cube('body', (x - 1.45, 8.25, 3.25, 2.9, 1.0, 2.9), 'cap')
        r.cube('body', (x - 1.4, 4.75, 3.3, 2.8, 0.5, 2.8), 'band', hole=0)
        # Each hose loops back off the cap, then climbs into the helm's intake.
        r.cube('body', (x - 0.4, 0.0, 4.2, 0.8, 0.75, 0.8), 'hose')
        r.cube('body', (x - 0.4, -0.75, 4.2, 0.8, 0.8, 2.3), 'hose')
        r.cube('body', (x - 0.4, -2.25, 5.7, 0.8, 1.5, 0.8), 'hose')
        r.cube('body', (x - 0.4, -2.25, 5.2, 0.8, 0.8, 0.5), 'hose')
    r.cube('body', (-0.6, 1.25, 3.5, 1.2, 6.25, 1.6), 'scrubber')
    r.cube('body', (-0.9, 1.0, 3.4, 1.8, 0.5, 1.8), 'cap')
    # The cape hangs between back and frame; only its lower half, below the canisters, swings free.
    r.part('upper_cape', 'body', (0, 0.25, 2.92))
    r.cube('upper_cape', (-4.75, 0, 0, 9.5, 9.0, 0), 'cape', drop=0)
    r.part('cape', 'upper_cape', (0, 9.0, 0), (5, 0, 0))
    r.cube('cape', (-4.75, 0, 0, 9.5, 8.5, 0), 'cape', drop=9.0)

    r.cube('left_arm', (-1.3, -2.25, -2.3, 4.6, 6.75, 4.6), 'linen', side=1)
    r.cube('right_arm', (-3.3, -2.25, -2.3, 4.6, 6.75, 4.6), 'linen', side=-1)
    r.cube('left_arm', (-1.4, 4.5, -2.4, 4.8, 5.0, 4.8), 'vambrace', side=1)
    r.cube('right_arm', (-3.4, 4.5, -2.4, 4.8, 5.0, 4.8), 'vambrace', side=-1)
    r.part('left_pauldron', 'left_arm', (1.0, -2.5, 0), (0, 0, 10))
    r.part('right_pauldron', 'right_arm', (-1.0, -2.5, 0), (0, 0, -10))
    r.mirrored('left_pauldron', (-1.75, -1.5, -2.75, 5.0, 0.75, 5.5), 'pauldron')
    r.mirrored('left_pauldron', (-2.5, -0.75, -3.25, 6.0, 2.0, 6.5), 'pauldron')
    r.mirrored('left_pauldron', (-1.75, 1.25, -3.4, 5.75, 1.25, 6.8), 'lame')
    r.mirrored('left_pauldron', (-1.0, 2.5, -3.3, 5.25, 1.25, 6.6), 'lame')
    return r


def leggings():
    r = base_rig()
    r.cube('body', (-4.5, 10.25, -2.55, 9.0, 1.75, 5.1), 'sash')
    r.cube('body', (-1.0, 10.0, -2.95, 2.0, 2.25, 0.5), 'buckle')
    # A stoppered vial of still water at the hip, carried for rinsing, never for drinking.
    r.cube('body', (2.9, 10.6, -3.9, 1.25, 2.25, 1.25), 'vial')
    r.cube('body', (2.8, 10.1, -4.0, 1.45, 0.6, 1.45), 'verdigris')
    r.part('front_tabard', 'body', (0, 11.5, -2.6))
    r.cube('front_tabard', (-2.75, 0, -0.5, 5.5, 8.0, 0.5), 'tabard')
    r.part('back_tabard', 'body', (0, 11.5, 2.6))
    r.cube('back_tabard', (-2.75, 0, 0, 5.5, 8.0, 0.5), 'tabard')
    for leg, side in (('left_leg', 1), ('right_leg', -1)):
        r.cube(leg, (-2.2, 0, -2.2, 4.4, 10.0, 4.4), 'linen', side=side)
        r.cube(leg, (-2.4, 0.25, -2.45, 4.8, 4.75, 4.9), 'cuisse', side=side)
        r.cube(leg, (-1.75, 4.5, -2.9, 3.5, 2.25, 0.75), 'poleyn', side=side)
    return r


def boots():
    r = base_rig()
    for leg, side in (('left_leg', 1), ('right_leg', -1)):
        r.cube(leg, (-2.45, 6.25, -2.45, 4.9, 4.25, 4.9), 'greave', side=side)
        r.cube(leg, (-2.6, 9.75, -2.6, 5.2, 0.6, 5.2), 'band', hole=2.45)
        r.cube(leg, (-2.55, 10.25, -3.0, 5.1, 1.75, 5.4), 'sabaton', side=side)
        r.cube(leg, (-2.3, 10.75, -3.6, 4.6, 1.25, 0.6), 'lame', side=side)
    return r


# ---------------------------------------------------------------- paint

PALETTE = dict(
    plate=(236, 234, 226), plate_shade=(212, 212, 208), silver=(204, 212, 222), silver_bright=(232, 237, 243),
    silver_dark=(140, 150, 168), verdigris=(86, 156, 136), verdigris_dark=(56, 112, 98),
    verdigris_light=(148, 204, 178), linen=(228, 223, 206), robe=(240, 238, 231), glass=(176, 218, 228),
    water=(112, 200, 206),
)


def verdigris(t):
    n = t['n']
    if n < 0.22:
        return PALETTE['verdigris_dark']
    if n > 0.86:
        return PALETTE['verdigris_light']
    return PALETTE['verdigris']


def edge_rows(t):
    """0 on the top/bottom pixel rows of a side face, 1 on the next, else None."""
    if not t['side']:
        return None
    k = min(t['v'], t['fh'] - 1 - t['v'])
    return k if k < 2 else None


def plate(t, rim=True):
    """White lacquered plate: a soft sheen down the middle of each face, a silver rim with copper rivets."""
    fw = max(t['fw'], 1)
    centre = abs((t['u'] + 0.5) / fw - 0.5) * 2
    rgb = mix(PALETTE['plate'], PALETTE['plate_shade'], centre ** 2 * 0.8)
    if t['side'] and abs(t['u'] + 0.5 - fw * 0.35) < 0.75:
        rgb = add(rgb, 10)
    rgb = add(rgb, (t['n'] - 0.5) * 6)
    k = edge_rows(t)
    if rim and k == 0:
        rgb = PALETTE['silver']
        if t['u'] % 10 == 5 and t['v'] == 0:
            rgb = PALETTE['verdigris']
    elif rim and k == 1:
        rgb = add(rgb, -14)
    return rgb


def linen(t):
    rgb = PALETTE['linen']
    if t['v'] % 3 == 0:
        rgb = (214, 208, 190)                 # weft
    elif (t['u'] + t['v'] // 3) % 5 == 0:
        rgb = add(rgb, 7)
    return add(rgb, (t['n'] - 0.5) * 6)


def robe_cloth(t, a, b, width, length, scallop=True):
    """White robe cloth with a silver border and a still-water ripple worked above the hem."""
    hem = length - (0.45 * abs(math.sin(a * math.pi / 1.4)) if scallop else 0)
    if b > hem:
        return (0, 0, 0, 0)
    rgb = add(PALETTE['robe'], math.sin(a * 1.3) * 6 + (t['n'] - 0.5) * 6)
    if b > hem - 0.5 or abs(a) > width / 2 - 0.45:
        rgb = PALETTE['silver']
    elif hem - 2.2 < b < hem - 0.9:
        wave = b - (hem - 1.55) - 0.3 * math.sin(a * 2.2)
        if abs(wave) < 0.22:
            rgb = PALETTE['verdigris']
    return rgb + (255,)


def crescent(a, b):
    """Silver crescent, horns up, centred in a 4x3 plane (a across, b down from the top)."""
    outer = math.hypot(a, (b - 1.0) * 1.1) < 1.75
    inner = math.hypot(a, (b - 0.35) * 1.1) < 1.45
    return outer and not inner and b > 0.2


def paint(cube, t):
    material, face = cube['material'], t['face']
    lx, ly, lz = t['lx'], t['ly'], t['lz']
    hole = cube.get('hole')
    if hole and face in ('up', 'down') and abs(lx) < hole and abs(lz) < hole:
        return (0, 0, 0, 0)                       # ring: the head or leg passes through
    if material == 'glass':
        if face == 'down':
            return (0, 0, 0, 0)
        # Clear glass, thickening toward each face edge, with two highlight streaks.
        d = min(t['u'], t['v'], t['fw'] - 1 - t['u'], t['fh'] - 1 - t['v'])
        alpha = 150 if d == 0 else 70
        rgb = PALETTE['glass']
        along = t['gx'] + t['gz'] - t['fy'] * 0.6
        if t['side'] and (abs(along - 2.2) < 0.3 or abs(along - 3.3) < 0.18):
            rgb, alpha = (244, 250, 252), 160
        return rgb + (alpha,)
    if material == 'silver':
        rgb = add(PALETTE['silver_bright'] if t['u'] == 0 else PALETTE['silver'], (t['n'] - 0.5) * 8)
        return rgb + (255,)
    if material == 'brow':
        rgb = PALETTE['silver_bright'] if t['v'] == 0 else PALETTE['silver']
        if t['side'] and t['u'] % 7 == 3:
            rgb = PALETTE['verdigris']
        return rgb + (255,)
    if material in ('gorget', 'band'):
        rgb = verdigris(t) if material == 'band' else PALETTE['silver']
        if material == 'gorget':
            k = edge_rows(t)
            if k == 0:
                rgb = PALETTE['silver_bright'] if t['v'] == 0 else PALETTE['silver_dark']
            elif t['side'] and t['u'] % 5 == 2 and t['v'] == 2:
                rgb = PALETTE['verdigris']
        return rgb + (255,)
    if material == 'vent':
        # Pierced copper rosette: the censer salts the breath before it reaches the wearer.
        rgb = verdigris(t)
        if face in ('east', 'west'):
            c = math.hypot(lz, ly + 3.375)
            if c < 1.0 and int((ly + 6) * SCALE) % 2 == 1 and abs(lz) < 0.85:
                rgb = (34, 60, 54)                # slits
            elif 1.1 <= c < 1.4:
                rgb = PALETTE['silver']
        return rgb + (255,)
    if material == 'grille':
        rgb = verdigris(t)
        if face == 'north' and t['u'] % 2 == 1 and 0 < t['v'] < t['fh'] - 1:
            rgb = (34, 60, 54)
        return rgb + (255,)
    if material == 'verdigris':
        return verdigris(t) + (255,)
    if material == 'port':
        rgb = verdigris(t)
        if face == 'south':
            d = min(t['u'], t['v'], t['fw'] - 1 - t['u'], t['fh'] - 1 - t['v'])
            rgb = PALETTE['silver'] if d == 0 else (34, 60, 54) if d >= 1 else rgb
        return rgb + (255,)
    if material == 'frame':
        rgb = PALETTE['silver']
        if t['side'] and (t['u'] in (0, t['fw'] - 1) or t['v'] in (0, t['fh'] - 1)):
            rgb = PALETTE['silver_bright']
        elif t['side'] and t['v'] % 6 == 3 and t['u'] % 4 == 1:
            rgb = PALETTE['verdigris']
        return rgb + (255,)
    if material == 'canister':
        if face in ('up', 'down'):
            return (0, 0, 0, 0)
        # Still water fills the canister below a meniscus; air and glass above it.
        level = 3.5
        d = min(t['u'], t['fw'] - 1 - t['u'])
        if ly > level:
            rgb, alpha = PALETTE['water'], 200
            if t['n'] > 0.93:
                rgb = (210, 240, 244)             # a rising bubble
        elif ly > level - 0.5:
            rgb, alpha = (200, 236, 240), 220
        else:
            rgb, alpha = PALETTE['glass'], 90
        if d == 0:
            alpha = max(alpha, 170)
            rgb = mix(rgb, (240, 248, 250), 0.4)
        return rgb + (alpha,)
    if material == 'cap':
        rgb = verdigris(t)
        if t['side'] and t['v'] == 0:
            rgb = PALETTE['verdigris_light']
        elif t['side'] and t['u'] % 3 == 1 and t['v'] == t['fh'] - 1:
            rgb = PALETTE['silver']
        return rgb + (255,)
    if material == 'hose':
        # Corrugated copper hose.
        _, _, _, w, hgt, d = cube['box']
        along = ly if hgt >= d else lz
        rgb = PALETTE['verdigris_dark'] if int(along * SCALE) % 2 else verdigris(t)
        return rgb + (255,)
    if material == 'scrubber':
        rgb = verdigris(t)
        if t['side'] and face == 'south' and t['v'] % 2 == 1 and 0 < t['u'] < t['fw'] - 1:
            rgb = (34, 60, 54)                    # salt-bed louvres
        return rgb + (255,)
    if material == 'crescent':
        return (PALETTE['silver_bright'] if lx < 0 else PALETTE['silver']) + (255,) \
            if crescent(lx, ly + 14.0) else (0, 0, 0, 0)
    if material == 'linen':
        return linen(t) + (255,)
    if material in ('cuirass', 'pauldron', 'vambrace', 'cuisse', 'greave', 'sabaton', 'lame', 'poleyn'):
        rgb = plate(t)
        if material == 'cuirass' and face == 'north':
            # The Lady's crescent cradling a single tear, inlaid on the breast.
            a, b = lx, ly - 1.5
            if crescent(a, b):
                rgb = PALETTE['silver']
            elif math.hypot(a, (b - 1.35) * 0.8) < 0.5:
                rgb = PALETTE['water']
            elif abs(a) < 0.3 and 4.6 < ly < 7.25:
                rgb = PALETTE['silver']           # the keel below the emblem
        if material == 'lame' and t['side'] and t['v'] == t['fh'] - 1:
            rgb = PALETTE['silver_dark']
        if material == 'pauldron' and face == 'up':
            rgb = add(rgb, 6)
        if material == 'vambrace' and t['side'] and t['fh'] // 2 - 1 <= t['v'] <= t['fh'] // 2:
            rgb = verdigris(t)                    # strap
        if material == 'greave' and t['side'] and face == 'north' and abs(t['u'] + 0.5 - t['fw'] / 2) < 1:
            rgb = PALETTE['silver']               # ridge down the shin
        if material == 'poleyn' and face == 'north':
            c = math.hypot(t['u'] + 0.5 - t['fw'] / 2, t['v'] + 0.5 - t['fh'] / 2)
            rgb = PALETTE['verdigris'] if c < 1.2 else PALETTE['silver'] if c < 2.0 else rgb
        return rgb + (255,)
    if material == 'cape':
        b = ly + cube['drop']
        rgba = robe_cloth(t, lx, b, 9.5, 17.5)
        if rgba[3] == 0:
            return rgba
        rgb = add(rgba[:3], math.sin(lx * 1.6 + 0.5) * 7) if rgba[:3] != PALETTE['silver'] else rgba[:3]
        if crescent(lx, (b - 10.0) * 0.85):
            rgb = PALETTE['silver']
        elif math.hypot(lx, (b - 11.5) * 0.8) < 0.55:
            rgb = PALETTE['water']
        return rgb + (255,)
    if material == 'tabard':
        if face in ('north', 'south'):
            return robe_cloth(t, lx, ly, 5.5, 8.0)
        return PALETTE['silver'] + (255,) if ly < 7.6 else (0, 0, 0, 0)
    if material == 'sash':
        rgb = add(PALETTE['robe'], (t['n'] - 0.5) * 6)
        if t['side'] and t['v'] % 3 == 0:
            rgb = (214, 212, 202)
        if t['side'] and t['u'] % 8 == 4 and t['v'] == 1:
            rgb = PALETTE['verdigris']
        return rgb + (255,)
    if material == 'buckle':
        d = min(t['u'], t['v'], t['fw'] - 1 - t['u'], t['fh'] - 1 - t['v'])
        rgb = PALETTE['silver_bright'] if d == 0 else PALETTE['silver']
        if face == 'north' and d >= 1 and t['u'] == t['fw'] // 2:
            rgb = PALETTE['silver_dark']
        return rgb + (255,)
    if material == 'vial':
        rgb, alpha = PALETTE['water'], 200
        if t['u'] == 0 or t['v'] < 1:
            rgb, alpha = (236, 248, 250), 220
        return rgb + (alpha,)
    raise KeyError(material)


# ---------------------------------------------------------------- layout

def layout(rigs):
    u = v = row = 0
    for rig in rigs:
        for part in rig.parts:
            for cube in part['cubes']:
                _, _, _, w, hgt, d = cube['box']
                cw, ch = ceil(2 * (w + d)) + 1, ceil(hgt + d) + 1
                if u + cw > ATLAS_WIDTH:
                    u, v, row = 0, v + row, 0
                cube['uv'] = (u, v)
                u += cw
                row = max(row, ch)
    height = 32
    while height < v + row:
        height *= 2
    return ATLAS_WIDTH, height


def paint_layer(rigs, size):
    image = Image.new('RGBA', (size[0] * SCALE, size[1] * SCALE))
    for rig in rigs:
        image.alpha_composite(paint_atlas(rig, size, paint))
    return image


# ---------------------------------------------------------------- Java

def part_lines(rig):
    lines = []
    for part in rig.parts:
        chain = 'CubeListBuilder.create()'
        for cube in part['cubes']:
            u, v = cube['uv']
            chain += (f'\n\t\t\t\t.texOffs({u}, {v}).addBox({", ".join(f(c) for c in cube["box"])}, '
                      f'new CubeDeformation(0.0F))')
        parent = 'root' if part['parent'] is None else camel(part['parent'])
        rot, pivot = part['rotation'], ', '.join(f(p) for p in part['pivot'])
        if any(rot):
            pose = f'PartPose.offsetAndRotation({pivot}, {", ".join(f(math.radians(a)) for a in rot)})'
        elif any(part['pivot']):
            pose = f'PartPose.offset({pivot})'
        else:
            pose = 'PartPose.ZERO'
        has_children = any(p['parent'] == part['name'] for p in rig.parts)
        decl = f'PartDefinition {camel(part["name"])} = ' if has_children else ''
        lines.append(f'\t\t{decl}{parent}.addOrReplaceChild("{part["name"]}", {chain}, {pose});')
    return '\n\n'.join(lines)


def layer_method(name, rig, size):
    return f'''	private static LayerDefinition {name}() {{
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

{part_lines(rig)}

		return LayerDefinition.create(mesh, {size[0]}, {size[1]});
	}}
'''


def java_source(rigs, sizes):
    chest = rigs['chest']
    rest = lambda rig, name, axis: f(math.radians(next(p for p in rig.parts if p['name'] == name)['rotation']['xyz'.index(axis)]))
    methods = '\n'.join(layer_method(f'create{key.capitalize()}Layer', rigs[key], sizes[key])
                        for key in ('helmet', 'chest', 'legs', 'boots'))
    return f'''package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * Unstained plate: glass helm, white cuirass and cape, belted tabards, sabatons. Generated by
 * tools/model_export/unstained_armor.py; edit the rigs there and re-run it.
 */
public class UnstainedArmorModel<T extends LivingEntity> extends HumanoidModel<T> {{

	public static final ModelLayerLocation UNSTAINED_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_helmet"), "main");
	public static final ModelLayerLocation UNSTAINED_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_chest"), "main");
	public static final ModelLayerLocation UNSTAINED_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_leggings"), "main");
	public static final ModelLayerLocation UNSTAINED_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_boots"), "main");

	public static final Lazy<UnstainedArmorModel<LivingEntity>> helmet = Lazy.of(() -> new UnstainedArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_HELMET_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> chest = Lazy.of(() -> new UnstainedArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_CHEST_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> legs = Lazy.of(
			() -> new UnstainedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_LEGS_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> boots = Lazy.of(
			() -> new UnstainedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_FEET_LAYER)));

	// Present only in the chest or leggings layer; null elsewhere.
	private final ModelPart cape;
	private final ModelPart frontTabard;
	private final ModelPart backTabard;

	public UnstainedArmorModel(ModelPart root) {{
		super(root, RenderType::entityTranslucent);
		this.cape = this.body.hasChild("upper_cape") ? this.body.getChild("upper_cape").getChild("cape") : null;
		this.frontTabard = this.body.hasChild("front_tabard") ? this.body.getChild("front_tabard") : null;
		this.backTabard = this.body.hasChild("back_tabard") ? this.body.getChild("back_tabard") : null;
	}}

	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {{
		return createHelmetLayer();
	}}

	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {{
		return switch (slot) {{
			case LEGS -> createLegsLayer();
			case FEET -> createBootsLayer();
			default -> createChestLayer();
		}};
	}}

{methods}
	/**
	 * The cape lifts behind a striding wearer and the tabards swing clear of the legs. Called after the wearer's pose
	 * has been copied onto the standard parts.
	 */
	public void animateRobes(float limbSwingAmount, float ageInTicks) {{
		float stride = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		float breath = Mth.sin(ageInTicks * 0.07F) * 0.02F;
		if (this.cape != null) {{
			this.cape.xRot = {rest(chest, 'cape', 'x')} + stride * 0.45F + breath;
		}}
		if (this.frontTabard != null) {{
			this.frontTabard.xRot = Math.min(Math.min(this.leftLeg.xRot, this.rightLeg.xRot), 0.0F) * 0.9F;
			this.backTabard.xRot = Math.max(Math.max(this.leftLeg.xRot, this.rightLeg.xRot), 0.0F) * 0.9F
					+ stride * 0.1F;
		}}
	}}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {{
		this.hat.visible = false;
		renderPart(this.head, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.body, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.rightArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.leftArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.rightLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.leftLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
	}}

	private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {{
		if (part.visible) {{
			part.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		}}
	}}
}}
'''


# ---------------------------------------------------------------- preview

def render(quad_list, tex, yaw, pitch, size=420):
    """Like unstained_weapons.render_quads, but blends translucent texels (glass, water) over the opaque pass."""
    tex = np.asarray(tex.convert('RGBA'), np.float32) / 255
    color = np.zeros((size, size, 3), np.float32) + np.array([30, 32, 40]) / 255
    depth = np.full((size, size), np.inf, np.float32)
    cy, sy, cp, sp = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)
    right, view = np.array([cy, 0, sy]), np.array([-sy * cp, sp, cy * cp])
    up = np.cross(view, right)
    allpts = np.concatenate([q[0] for q in quad_list]) * np.array([1, -1, 1])
    center = (allpts.min(axis=0) + allpts.max(axis=0)) / 2
    scale = size * 0.9 / max(np.ptp(allpts @ right), np.ptp(allpts @ up))
    light = np.array([0.3, 0.9, -0.5]) / np.linalg.norm([0.3, 0.9, -0.5])
    th, tw = tex.shape[:2]
    for opaque in (True, False):
        for pts, uvs in quad_list:
            p = pts * np.array([1, -1, 1]) - center
            n = np.cross(p[1] - p[0], p[2] - p[0])
            if np.linalg.norm(n) < 1e-9:
                n = np.cross(p[2] - p[0], p[3] - p[0])
            if np.linalg.norm(n) < 1e-9:
                continue
            shade = 0.6 + 0.4 * abs(n / np.linalg.norm(n) @ light)
            sx, sy_, dz = p @ right * scale + size / 2, -(p @ up) * scale + size / 2, p @ view
            for tri in ((0, 1, 2), (0, 2, 3)):
                xs, ys, zs, uv = sx[list(tri)], sy_[list(tri)], dz[list(tri)], uvs[list(tri)]
                x0, x1 = max(int(xs.min()), 0), min(int(math.ceil(xs.max())), size - 1)
                y0, y1 = max(int(ys.min()), 0), min(int(math.ceil(ys.max())), size - 1)
                det = (ys[1] - ys[2]) * (xs[0] - xs[2]) + (xs[2] - xs[1]) * (ys[0] - ys[2])
                if x0 > x1 or y0 > y1 or abs(det) < 1e-9:
                    continue
                gx, gy = np.meshgrid(np.arange(x0, x1 + 1) + .5, np.arange(y0, y1 + 1) + .5)
                l0 = ((ys[1] - ys[2]) * (gx - xs[2]) + (xs[2] - xs[1]) * (gy - ys[2])) / det
                l1 = ((ys[2] - ys[0]) * (gx - xs[2]) + (xs[0] - xs[2]) * (gy - ys[2])) / det
                l2 = 1 - l0 - l1
                inside = (l0 >= -1e-4) & (l1 >= -1e-4) & (l2 >= -1e-4)
                z = l0 * zs[0] + l1 * zs[1] + l2 * zs[2]
                tu = np.clip((l0 * uv[0, 0] + l1 * uv[1, 0] + l2 * uv[2, 0]).astype(int), 0, tw - 1)
                tv = np.clip((l0 * uv[0, 1] + l1 * uv[1, 1] + l2 * uv[2, 1]).astype(int), 0, th - 1)
                texel = tex[tv, tu]
                region = (slice(y0, y1 + 1), slice(x0, x1 + 1))
                a = texel[..., 3]
                if opaque:
                    ok = inside & (a > 0.97) & (z < depth[region])
                    depth[region][ok] = z[ok]
                    color[region][ok] = texel[..., :3][ok] * shade
                else:
                    ok = inside & (a > 0.01) & (a <= 0.97) & (z < depth[region])
                    blend = a[ok][:, None]
                    color[region][ok] = color[region][ok] * (1 - blend) + texel[..., :3][ok] * shade * blend
    return Image.fromarray((np.clip(color, 0, 1) * 255).astype('uint8'))


def preview(rigs, layer1, layer2):
    tex = Image.new('RGBA', (layer1.width, layer1.height + layer2.height + 8))
    tex.paste(layer1, (0, 0))
    tex.paste(layer2, (0, layer1.height))
    row = layer1.height + layer2.height
    for i, colour in enumerate(MANNEQUIN_COLOURS):
        for px in range(i * 4, i * 4 + 4):
            for py in range(row, row + 8):
                tex.putpixel((px, py), colour + (255,))
    full = (quads(rigs['helmet'], (), row) + quads(rigs['chest']) + quads(rigs['boots'])
            + quads(rigs['legs'], v_offset=layer1.height))
    tiles = [render(full, tex, 0.0, 0.0), render(full, tex, math.pi, 0.0), render(full, tex, 0.6, 0.2),
             render(full, tex, math.pi * 0.8, 0.25), render(full, tex, math.pi / 2, 0.0)]
    head = quads(rigs['helmet'], ('body', 'right_arm', 'left_arm', 'right_leg', 'left_leg'), row)
    tiles += [render(head, tex, 0.5, 0.25), render(head, tex, math.pi / 2, 0.0)]
    legs_only = quads(rigs['legs'], ('head', 'right_arm', 'left_arm'), row, v_offset=layer1.height) \
        + quads(rigs['boots'])
    tiles.append(render(legs_only, tex, 0.5, 0.2))
    size, cols = 420, 4
    sheet = Image.new('RGB', (size * cols, size * math.ceil(len(tiles) / cols)), (30, 32, 40))
    for i, tile in enumerate(tiles):
        sheet.paste(tile, ((i % cols) * size, (i // cols) * size))
    out = ROOT / 'build/unstained_armor_preview.png'
    out.parent.mkdir(exist_ok=True)
    sheet.save(out)
    print('preview ->', out)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rigs = dict(helmet=helmet(), chest=chestplate(), legs=leggings(), boots=boots())
    size1 = layout([rigs['helmet'], rigs['chest'], rigs['boots']])
    size2 = layout([rigs['legs']])
    layer1 = paint_layer([rigs['helmet'], rigs['chest'], rigs['boots']], size1)
    layer2 = paint_layer([rigs['legs']], size2)
    layer1.save(TEXTURES / 'unstained_layer_1.png')
    layer2.save(TEXTURES / 'unstained_layer_2.png')
    sizes = dict(helmet=size1, chest=size1, boots=size1, legs=size2)
    JAVA.write_text(java_source(rigs, sizes), encoding='utf-8')
    for key, rig in rigs.items():
        print(f'{key}: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {sizes[key][0]}x{sizes[key][1]}')
    if args.preview:
        preview(rigs, layer1, layer2)


if __name__ == '__main__':
    main()
