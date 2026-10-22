"""The Mycophant, the Spawn Mother: the hivemind's brood cultivator and the Apotheos path's final gate.

Emits MycophantModel.java plus three 2x painted atlases:
  mycophant.png       crimson and maroon flesh, dark chitin, rust and ochre, olive fungus at the seams, ivory claws,
                      a pale bone skull, a pink tongue and the hyphae lattice of the phase-2 wings (alpha cut out)
  mycophant_glow.png  amber eye pits, the pulp's eyes, sac cavities, egg pockets, vent and siphon mouths (emissive)
  mycophant_sac.png   the great gestation sac's translucent amber shell (its own translucent layer)

    python tools/model_export/mycophant.py            # write model + textures
    python tools/model_export/mycophant.py --preview  # also write build/mycophant_preview*.png

The geometry is a transcription of the locked three.js blockout (tools/model_export/reference/spawn-mother-v8.html):
the helpers below take that file's coordinates (pixels at world scale, Y up, the face toward -Z, three.js XYZ euler
radians) and convert them to Minecraft model space at half size (it renders at 2x), Y down, ZYX degrees, with the
creature's right on model -X. Big forms only: plating, seams, fungus and veins are painted, never modelled.

She rests on a plated belly and is dragged by four crab claws; above it a raised thorax carries a grasping hand
(left) and a feeding siphon (right), a tall armoured neck and a slim beaked goat skull ringed by a frilled-lizard
collar. The skull is two halves that swing apart at half health over an Ebrietas pulp face, and hyphae wings grow from
the shoulders. Behind, an S-curved hump of shell plates runs down into a swollen abdomen of sac cavities and petal vents.
"""
import argparse
import math
from pathlib import Path

import numpy as np

from annetta_kit import (ROOT, ASSETS, Rig, add, camel, clamp, f, h, layout, mix, paint_atlases, preview_sheet, quads,
                         smooth)

import annetta_kit

# Block texel density: it renders at 2x, so two texture pixels per model unit is 16 px per block.
TEXEL_SCALE = 2
annetta_kit.SCALE = TEXEL_SCALE

MODEL_DIR = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/endgame'
TEXTURE_DIR = ASSETS / 'textures/entity/boss/endgame'
JAVA = MODEL_DIR / 'MycophantModel.java'
ATLAS_WIDTH = 256


# ---------------------------------------------------------------- blockout -> model conversion

def r_xyz(rx, ry, rz):
    """three.js Euler 'XYZ' (radians) as a matrix: Rx @ Ry @ Rz."""
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    mx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    my = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    mz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return mx @ my @ mz


FLIP = np.diag([-1.0, -1.0, 1.0])


def model_rotation(rx, ry, rz):
    """Blockout XYZ radians -> Minecraft ZYX degrees in the flipped model frame."""
    m = FLIP @ r_xyz(rx, ry, rz) @ FLIP
    y = math.asin(max(-1.0, min(1.0, -m[2][0])))
    x = math.atan2(m[2][1], m[2][2])
    z = math.atan2(m[1][0], m[0][0])
    return tuple(round(math.degrees(a), 4) for a in (x, y, z))


class Builder:
    """Mirrors the blockout's grp()/box() calls, writing parts and cubes into a Rig."""

    def __init__(self, rig):
        self.rig = rig
        self.rot_groups = {}
        self.counter = {}

    def grp(self, parent, name, x=0.0, y=0.0, z=0.0, rx=0.0, ry=0.0, rz=0.0):
        self.rig.part(name, parent, (round(-x / 2, 4), round(-y / 2, 4), round(z / 2, 4)), model_rotation(rx, ry, rz))
        return name

    def box(self, parent, w, hgt, d, material, x=0.0, y=0.0, z=0.0, rx=0.0, ry=0.0, rz=0.0, **extra):
        if abs(rx) > 1e-6 or abs(ry) > 1e-6 or abs(rz) > 1e-6:
            key = (parent, round(rx, 5), round(ry, 5), round(rz, 5))
            sub = self.rot_groups.get(key)
            if sub is None:
                n = self.counter.get(parent, 0)
                self.counter[parent] = n + 1
                sub = self.grp(parent, f'{parent}_r{n}', 0, 0, 0, rx, ry, rz)
                self.rot_groups[key] = sub
            x, y, z = r_xyz(rx, ry, rz).T @ np.array([x, y, z], float)
            parent = sub
        self.rig.cube(parent, (round(-x / 2 - w / 4, 4), round(-y / 2 - hgt / 4, 4), round(z / 2 - d / 4, 4),
                               round(w / 2, 4), round(hgt / 2, 4), round(d / 2, 4)), material, **extra)


def rnd_stream(seed):
    """The blockout's Park-Miller generator, so per-part jitter matches it where it matters."""
    state = [seed]

    def rnd():
        state[0] = (state[0] * 16807) % 2147483647
        return (state[0] - 1) / 2147483646
    return rnd


# ---------------------------------------------------------------- the rig

CHAINS = {}      # name -> list of part names, for the Java arrays the animation drives


def spawn_mother():
    rig = Rig('mycophant', 'MycophantModel', 'mycophant')
    b = Builder(rig)
    rnd = rnd_stream(31)
    b.grp('root', 'ground', 0, -48.0, 0)                  # model y = 24 at the floor
    body = b.grp('ground', 'body', 0, -18, 0)

    # the belly she rests on and is hauled along on
    chassis = b.grp(body, 'chassis')
    b.box(chassis, 44, 16, 48, 'crimson2', 0, 26, 2)
    b.box(chassis, 34, 14, 10, 'crimson', 0, 28, -24, -0.3, 0, 0)

    # four crab claws: shoulder, upper, raised knee, forearm, pincer
    for name, sx, sy, sz, L1, L2, k in (('front', 18, 34, -18, 26, 28, 1.0), ('second', 23, 32, 0, 24, 26, 0.85)):
        for side, tag in ((-1, 'l'), (1, 'r')):
            kk = k * (1.05 if (name == 'front' and side == 1) else 1.0)
            p = f'claw_{name}_{tag}'
            sh = b.grp(chassis, p + '_upper', side * sx, sy, sz)
            b.box(sh, 10 * kk, 10 * kk, 10 * kk, 'chitin', 0, 0, 0)
            b.box(sh, 7.5 * kk, L1, 7.5 * kk, 'crimson2', 0, -L1 / 2, 0)
            el = b.grp(sh, p + '_fore', 0, -L1, 0)
            b.box(el, 9 * kk, 6, 9 * kk, 'chitin', 0, 0, 0)
            b.box(el, 6.5 * kk, L2, 6.5 * kk, 'crimson', 0, -L2 / 2, 0)
            end = b.grp(el, p + '_hand', 0, -L2, 0)
            b.box(end, 9 * kk, 10 * kk, 7 * kk, 'crimson2', 0, -5 * kk, 0)
            fx = b.grp(end, p + '_fixed', -2 * kk, -10 * kk, 0)
            b.box(fx, 3.6 * kk, 13 * kk, 4.6 * kk, 'chitin', 0, -6.5 * kk, 0)
            b.box(fx, 2.4 * kk, 4 * kk, 3 * kk, 'ivory', 0, -13.5 * kk, 0)
            dc = b.grp(end, p + '_dactyl', 2.2 * kk, -10 * kk, 0)
            b.box(dc, 3.2 * kk, 11 * kk, 4.2 * kk, 'crimson', 0, -5.5 * kk, 0)
            b.box(dc, 2.2 * kk, 3.4 * kk, 2.8 * kk, 'ivory', 0, -11.5 * kk, 0)

    # abdomen: five swelling-then-tapering masses, sac cavities, petal vents, trailing tendrils
    abdomen = b.grp(body, 'abdomen', 0, 31, 18)
    for i, (w, hg, d, x, y, z) in enumerate([(38, 24, 20, 0, 4, 10), (48, 30, 22, 0, 6, 30), (44, 28, 18, 0, 4, 49),
                                             (32, 20, 14, 0, 0, 64), (18, 12, 10, 0, -4, 75)]):
        b.box(abdomen, w, hg, d, 'crimson' if i % 2 else 'crimson2', x, y, z, plate=i)
    for x, y, z, side, n in ((-24.4, 6, 28, -1, 3), (24.4, 6, 28, 1, 3), (-22.4, 4, 48, -1, 2), (22.4, 4, 48, 1, 2)):
        b.box(abdomen, 2, 13, 15, 'dark', x, y, z)
        for i, sz in enumerate([6, 5, 4.5][:n]):
            b.box(abdomen, sz, sz, sz, 'egg', x + side * (1 + sz * 0.2), y + (i - 1) * 3, z + (i - (n - 1) / 2) * 4.5)
    vents = []
    for vi, (x, y, z, rx, rz, r) in enumerate(((-22, -6, 60, 0, math.pi / 2 + 0.5, 5), (22, -6, 60, 0, -math.pi / 2 - 0.5, 5),
                                               (0, -8, 74, 2.3, 0, 4.5))):
        g = b.grp(abdomen, f'vent_{vi}', x, y, z, rx, 0, rz)
        b.box(g, r * 1.6, 2, r * 1.6, 'dark', 0, -0.8, 0)
        b.box(g, r * 1.1, 1, r * 1.1, 'vent_glow', 0, -1.4, 0)
        petals = []
        for i in range(5):
            a = i / 5 * math.pi * 2
            spoke = b.grp(g, f'vent_{vi}_spoke_{i}', math.cos(a) * r, 0, math.sin(a) * r, 0, -a, 0)
            hinge = b.grp(spoke, f'vent_{vi}_petal_{i}')
            b.box(hinge, r * 1.1, 1.4, r * 0.9, 'rust' if i % 2 else 'crimson', -r * 0.5, 0.2, 0)
            petals.append(hinge)
        vents.append(petals)
    CHAINS['vent_petals'] = vents
    tails = []
    for i, x in enumerate((-3, 0, 3)):
        p = b.grp(abdomen, f'tail_{i}_0', x, -2, 79, 1.2 + i * 0.1, 0, x * 0.08)
        chain = [p]
        for k in range(6):
            t = 1.8 - k * 0.2
            b.box(p, t, 4, t, 'pink2' if k % 2 else 'crimson', 0, 2, 0)
            if k < 5:
                p = b.grp(p, f'tail_{i}_{k + 1}', 0, 4, 0)
                chain.append(p)
        tails.append(chain)
    CHAINS['tails'] = tails

    # thorax: core, shoulder hump, shoulders, one chest plate, one egg pocket
    thorax = b.grp(body, 'thorax', 0, 38, -8)
    b.box(thorax, 32, 30, 24, 'crimson', 0, 16, -2, plate=1)
    b.box(thorax, 26, 14, 20, 'crimson2', 0, 40, -4)
    b.box(thorax, 26, 12, 4, 'chitin', 0, 27, -15.2, 0.1, 0, 0)
    b.box(thorax, 9, 9, 2, 'dark', 0, 12, -14.6)
    for dx, dy, z in ((0, 1.5, 4), (-2, -2, 3), (2.2, -1.5, 2.6)):
        b.box(thorax, z, z, z * 0.8, 'egg', dx, 12 + dy, -16)
    for sd in (-1, 1):
        b.box(thorax, 16, 26, 18, 'chitin', sd * 20, 38, -6, 0, 0, sd * -0.12)
        b.box(thorax, 14, 14, 16, 'crimson2', sd * 13, 46, -7, 0, 0, sd * -0.35)

    # the great gestation sac: tall oval shell (its own translucent layer) around the egg mass
    sac = b.grp(body, 'sac', 0, 35, -25)
    shell = b.grp(sac, 'sac_shell')
    s = 0.66
    for w, hg, d in ((30, 46, 22), (36, 38, 24), (24, 54, 18), (40, 26, 22), (20, 40, 28)):
        b.box(shell, w * s, hg * s, d * s, 'shell', 0, 4 * s, 0)
    egg_rnd = rnd_stream(77)
    for i in range(14):
        sz = (5 + egg_rnd() * 5) * s
        b.box(sac, sz, sz, sz * 0.9, 'egg' if i % 2 else 'yolk', (egg_rnd() - .5) * 20 * s, (4 + (egg_rnd() - .5) * 32) * s,
              (egg_rnd() - .5) * 9 * s)
    for i, (x, y, z) in enumerate(((-6, 12, -5), (7, -2, -4), (-4, -12, -5))):
        e = b.grp(sac, f'embryo_{i}', x * s, y * s, z * s, 0, i * 0.8, i * 0.7)
        b.box(e, 7 * s, 5 * s, 5 * s, 'embryo', 0, 2 * s, 0)
        b.box(e, 5 * s, 6 * s, 5 * s, 'embryo', 3 * s, -2 * s, 0)
        b.box(e, 4 * s, 4 * s, 4 * s, 'embryo', -s, -4 * s, 0)
    b.box(sac, 20 * s, 8 * s, 14 * s, 'crimson2', 0, 30 * s, 6 * s)

    # the back: a solid core under nine plates following an S-curve from the hump into the abdomen
    b.box(body, 34, 30, 38, 'maroon', 0, 46, 12)
    b.box(body, 30, 22, 22, 'maroon', 0, 68, 2, 0.4, 0, 0)
    for i in range(9):
        t = i / 8
        sm = t * t * (3 - 2 * t)
        slope = math.atan(6 * t * (1 - t))
        W = 46 - math.sin(math.pi * t) * 14
        g = b.grp(body, f'back_plate_{i}', 0, 90 - 40 * sm, -6 + 40 * t, slope, 0, 0)
        b.box(g, W, 6, 11, 'crimson2' if i % 2 else 'crimson', 0, 0, 0, plate=2)
        if i in (2, 5):
            ts = 0.9
            tg = b.grp(g, f'vent_trumpet_{0 if i == 2 else 1}', (-1 if i == 2 else 1) * 11, 3, 0, -0.4, 0, 0)
            b.box(tg, 5 * ts, 3 * ts, 5 * ts, 'maroon', 0, 1.5 * ts, 0)
            b.box(tg, 5.5 * ts, 4 * ts, 5.5 * ts, 'rust', 0, 5 * ts, 0)
            b.box(tg, 8.5 * ts, 1.6 * ts, 8.5 * ts, 'ochre', 0, 7.6 * ts, 0)
            b.box(tg, 5 * ts, 1 * ts, 5 * ts, 'vent_glow', 0, 8 * ts, 0)
        for sd in (-1, 1):
            sk = b.grp(g, f'back_plate_{i}_skirt_{"l" if sd < 0 else "r"}', sd * (W / 2 - 1.2), -2, 0, -slope, 0,
                       sd * 0.2 * math.sin(math.pi * t))
            b.box(sk, 3, 20 + math.sin(math.pi * t) * 6, 10.5, 'chitin' if i % 2 else 'crimson2', 0, -11, 0)

    # upper arms: a grasping hand (left) and a feeding siphon (right)
    for side, kind in ((-1, 'hand'), (1, 'siphon')):
        p = f'arm_{kind}'
        sh = b.grp(thorax, p + '_upper', side * 24, 46, -10)
        b.box(sh, 11, 11, 11, 'chitin', 0, 0, 0)
        b.box(sh, 7, 22, 7, 'crimson2', 0, -11, 0)
        el = b.grp(sh, p + '_fore', 0, -22, 0)
        b.box(el, 8, 5, 8, 'chitin', 0, 0, 0)
        b.box(el, 6, 20, 6, 'crimson', 0, -10, 0)
        wrist = b.grp(el, p + '_wrist', 0, -20, 0)
        if kind == 'hand':
            b.box(wrist, 8, 7, 6, 'chitin', 0, -3.5, 0)
            fingers = []
            for fi, x in enumerate((-2.6, 0, 2.6)):
                f1 = b.grp(wrist, f'finger_{fi}_0', x, -7, -1)
                b.box(f1, 2.2, 9, 2.2, 'crimson2', 0, -4.5, 0)
                f2 = b.grp(f1, f'finger_{fi}_1', 0, -9, 0)
                b.box(f2, 2, 7, 2, 'crimson', 0, -3.5, 0)
                f3 = b.grp(f2, f'finger_{fi}_2', 0, -7, 0)
                b.box(f3, 1.5, 6, 1.5, 'ivory', 0, -3, 0)
                fingers.append([f1, f2, f3])
            CHAINS['fingers'] = fingers
            t1 = b.grp(wrist, 'thumb_0', -side * 4.5, -4, -2)
            b.box(t1, 3, 8, 3, 'chitin', 0, -4, 0)
            t2 = b.grp(t1, 'thumb_1', 0, -8, 0)
            b.box(t2, 2.4, 9, 2.4, 'ivory', 0, -4.5, 0)
            cradled = b.grp(wrist, 'cradled_fungling', 0, -13, -4)
            b.box(cradled, 8, 7, 7, 'fung', 0, 0, 0)
            b.box(cradled, 13, 4, 12, 'fung_cap', 0, 5, 0)
        else:
            b.box(wrist, 6.4, 4, 6.4, 'chitin', 0, -2, 0)
            siphon = []
            pp = b.grp(wrist, 'siphon_0', 0, -4, 0)
            for k in range(12):
                t = 4 - k * 0.15
                b.box(pp, t, 4, t, 'pink' if k % 2 else 'crimson', 0, -2, 0)
                siphon.append(pp)
                pp = b.grp(pp, f'siphon_{k + 1}', 0, -4, 0)
            b.box(pp, 6, 1.6, 6, 'ochre', 0, -0.8, 0)
            b.box(pp, 3.4, 1, 3.4, 'siphon_mouth', 0, -1.4, 0)
            siphon.append(pp)
            CHAINS['siphon'] = siphon

    # phase-2 wings: a dripping lattice of hyphae (spar chains carrying painted vanes)
    wing_spars = []
    for side, tag in ((-1, 'l'), (1, 'r')):
        pivot = b.grp(thorax, f'wing_{tag}', side * 18, 40, 8)
        spars = []
        for si, a0 in enumerate((1.25, 0.95, 0.65, 0.35, 0.05, -0.25)):
            segs = 8 - abs(si - 2)
            # each spar is a chain of segments laid along +x (blockout) for the right wing, mirrored for the left
            p = b.grp(pivot, f'wing_{tag}_{si}_0', 0, 0, si * 0.9, 0, 0, a0 if side > 0 else math.pi - a0)
            chain = [p]
            for k in range(segs):
                L = 10
                thick = max(0.9, 2.6 - k * 0.22)
                b.box(p, L + 0.8, thick, thick, 'ochre' if k > segs - 3 else ('crimson' if k % 2 else 'membrane'), L / 2, 0, 0)
                if si < 5:
                    vane = 2.4 + k * 2.9                       # the gap to the next spar widens toward the tip
                    b.box(p, L, vane, 0.2, 'vane', L / 2, -side * vane / 2, 0)
                if k % 2 and k > 2:
                    b.box(p, 1.2, 4, 1.2, 'ivory', L, side * 2, 0, 0, 0, -1.2 * side)
                if k < segs - 1:
                    p = b.grp(p, f'wing_{tag}_{si}_{k + 1}', L, 0, 0, 0, 0, -0.085 if side > 0 else 0.085)
                    chain.append(p)
            spars.append(chain)
        wing_spars.append(spars)
    CHAINS['wings'] = wing_spars

    # the neck: a tall armoured column carrying the head high above the front body
    neck = b.grp(thorax, 'neck', 0, 50, -10)
    b.box(neck, 15, 12, 13, 'crimson', 0, 4, -1, -0.15, 0, 0)
    b.box(neck, 13, 14, 11, 'crimson2', 0, 14, -4, -0.25, 0, 0)
    b.box(neck, 11, 10, 10, 'crimson', 0, 23, -7, -0.35, 0, 0)
    head = b.grp(neck, 'head', 0, 28, -12)

    # the skull: long, narrow, goat-like, beaked, two halves that crack apart in phase 2
    skull = [(12, 22, 13, 'crimson2', 0, -2, -4, 0.35, 0, 0), (13.5, 6, 12, 'bone', 0.8, 8, -3, 0.25, 0, 0.05),
             (11, 5, 11, 'ivory', -0.5, 4, -8, 0.45, 0, -0.04), (8, 12, 7, 'bone', 0, -6, -12, 0.5, 0, 0),
             (5, 7, 5, 'ivory', 0, -13, -15, 0.6, 0, 0)]
    for sd, tag in ((-1, 'l'), (1, 'r')):
        px, py, pz = sd * 6.5, 2, 3
        half = b.grp(head, f'skull_half_{tag}', px, py, pz)
        inner = b.grp(half, f'skull_half_{tag}_inner', -px, -py, -pz)
        for i, (w, hg, d, mat, x, y, z, rx, ry, rz) in enumerate(skull):
            b.box(inner, w / 2 - i * 0.07, hg - i * 0.11, d - i * 0.09, mat, x + sd * w / 4, y, z, rx, ry, rz)
        b.box(inner, 0.5, 20.6, 11.3, 'wound', -sd * 0.12, -2, -4.3, 0.35, 0, 0)   # the torn inner face of the half
        if sd > 0:
            b.box(inner, 3, 6, 3, 'bone', 5.5, 9, 0, -0.3, 0, -0.5)
        else:
            b.box(inner, 2.4, 4.5, 2.4, 'bone', -6, 6, 2, -0.2, 0, 0.7)
        pits = ([(-6.6, 2, -8, ((0, 0, 2.2), (0.8, 2.4, 1.2), (-0.4, -2.2, 1.4))), (-5.6, -6, -12, ((0, 0, 1.2),))]
                if sd < 0 else
                [(6.6, 0, -9, ((0, 0, 1.6), (0.5, 2.2, 1.0), (0.2, -1.8, 0.9), (0.9, 3.8, 0.8))),
                 (5, -8, -13, ((0, 0, 1.4), (0.4, -1.8, 0.8)))])
        for x, y, z, ps in pits:
            b.box(inner, 2.2, 6.7, 5.7, 'dark', x + sd * 0.45, y, z, 0.35, 0, 0)
            for dz, dy, sz in ps:
                b.box(inner, 1.2, sz, sz, 'pit', x + sd * 1.15, y + dy, z + dz - 1, 0.35, 0, 0)

    # the pulp inside: an Ebrietas flower-face of eyes, hollow tubes and tentacles
    pulp = b.grp(head, 'pulp', 0, -2, -5, 0.35, 0, 0)
    b.box(pulp, 11, 22, 3, 'membrane', 0, 0, 0)
    b.box(pulp, 7, 14, 2, 'pulp_core', 0, 1, -1.2)
    for i in range(30):
        sz = 0.8 + rnd() * 1.4
        mat = ('pulp_eye', 'dark', 'membrane', 'pulp_eye_green')[i % 4]
        b.box(pulp, sz, sz, 1, mat, (rnd() - .5) * 9, (rnd() - .5) * 19, -1.6 - rnd() * 0.6)
    for i in range(18):
        a = i / 18 * math.pi * 2
        tg = b.grp(pulp, f'pulp_tube_{i}', math.sin(a) * 6.5, math.cos(a) * 12, -1, -math.cos(a) * 0.5 - 0.2,
                   math.sin(a) * 0.5, 0)
        L = 4 + rnd() * 4
        b.box(tg, 2.2, 2.2, L, 'ochre' if i % 2 else 'rust', 0, 0, -L / 2)
        b.box(tg, 2.5, 2.5, 0.8, 'ivory', 0, 0, -L)
    pulp_tents = []
    for i, (x, y) in enumerate(((-2, 3), (2, 4), (0, -2), (-3, -6), (3, -5), (0, 8))):
        p = b.grp(pulp, f'pulp_tentacle_{i}_0', x, y, -1.5, -1.3, 0, (rnd() - .5) * 0.8)
        chain = [p]
        for k in range(6):
            b.box(p, 1.4 - k * 0.14, 3, 1.4 - k * 0.14, 'pink' if k % 2 else 'pink2', 0, 1.5, 0)
            if k < 5:
                p = b.grp(p, f'pulp_tentacle_{i}_{k + 1}', 0, 3, 0)
                chain.append(p)
        pulp_tents.append(chain)
    CHAINS['pulp_tentacles'] = pulp_tents

    # the vertical mouth under the cranium: two mandibles that part sideways, a thick tongue
    maw = b.grp(head, 'maw', 0, -14, -9)
    b.box(maw, 4, 12, 5, 'dark', 0, -3, 0, 0.3, 0, 0)
    for s2, tag in ((-1, 'l'), (1, 'r')):
        m = b.grp(maw, f'mandible_{tag}', s2 * 2.6, 1, 0)
        b.box(m, 3, 13, 5, 'crimson2', s2 * 1.2, -5, 0, 0.3, 0, 0)
        b.box(m, 1, 12, 1, 'ivory', -s2 * 0.2, -5, -2.4, 0.3, 0, 0)
        b.box(m, 1.2, 5, 1.2, 'bone', s2 * 2.4, -10, -1, 0.4, 0, s2 * 0.4)
    tongue = []
    p = b.grp(maw, 'tongue_0', 0, -6, -2, 0.6, 0, 0)
    for k in range(10):
        t = 4.6 - k * 0.32
        b.box(p, t, 4.4, t * 0.75, 'pink' if k % 2 else 'pink2', 0, -2.2, 0)
        tongue.append(p)
        if k < 9:
            p = b.grp(p, f'tongue_{k + 1}', 0, -4.4, 0)
    CHAINS['tongue'] = tongue

    # the frilled-lizard collar ringing the face
    collar = b.grp(head, 'collar', 0, -3, -7, 0.35, 0, 0)
    hinges = []
    for row, n in ((0, 16), (1, 12)):
        for i in range(n):
            a = -2.55 + 5.1 * i / (n - 1) + (0.2 if row else 0.0)
            spoke = b.grp(collar, f'collar_{row}_{i}_spoke', 0, 0, row * 2.5, 0, 0, -a)
            hinge = b.grp(spoke, f'collar_{row}_{i}', 0, 10 + row, 0)
            length = (15 if row else 20) + math.cos(a) * 7 + rnd() * 4
            b.box(hinge, 7.5, length, 1, 'rust' if row else 'membrane', 0, length / 2, 0)
            b.box(hinge, 1.6, length + 5, 1.8, 'bone', 3.4, (length + 5) / 2, -0.3)
            b.box(hinge, 7.8, 2, 1.3, 'amber' if i % 2 else 'ochre', 0, length - 1, -0.1)
            hinges.append(hinge)
    b.box(collar, 16, 16, 3, 'crimson', 0, 0, 2)
    CHAINS['collar'] = hinges

    # facial tentacles: two long lateral whips, three short under the mouth, three reaching forward
    face = []
    for ti, (x, y, z, n, t0, kind) in enumerate(((-6.5, -2, -7, 12, 3.4, 'whip'), (6.8, -4, -6, 11, 3.2, 'whip'),
                                                 (-2.5, -17, -14, 5, 1.8, 'short'), (1, -18, -15, 4, 1.6, 'short'),
                                                 (3.5, -16, -13, 5, 1.7, 'short'), (-4.5, -10, -12, 7, 2.0, 'fwd'),
                                                 (4.5, -11, -12, 6, 2.0, 'fwd'), (-1, -8, -14, 6, 1.8, 'fwd'))):
        p = b.grp(head, f'face_tentacle_{ti}_0', x, y, z)
        chain = [p]
        L = 7.5 if kind == 'whip' else 3.6
        for k in range(n):
            t = max(0.7, t0 - k * (t0 - 0.8) / n)
            b.box(p, t, L, t, 'crimson' if k % 2 else 'pink2', 0, -L / 2, 0)
            if k < n - 1:
                p = b.grp(p, f'face_tentacle_{ti}_{k + 1}', 0, -L, 0)
                chain.append(p)
        b.box(p, 0.7, 3, 0.7, 'amber', 0, -L - 1.5, 0)
        face.append(chain)
    CHAINS['face_tentacles'] = face
    return rig


# ---------------------------------------------------------------- painting

BASE = {
    'crimson': (110, 20, 22), 'crimson2': (138, 30, 26), 'rust': (168, 70, 30), 'maroon': (58, 12, 14),
    'dark': (28, 6, 6), 'olive': (122, 116, 40), 'chartreuse': (168, 168, 50), 'ochre': (192, 138, 42),
    'amber': (255, 176, 48), 'ivory': (230, 216, 176), 'bone': (216, 192, 138), 'pink': (176, 74, 90),
    'pink2': (138, 48, 70), 'membrane': (154, 26, 34), 'chitin': (82, 18, 18), 'egg': (255, 168, 40),
    'yolk': (255, 200, 72), 'embryo': (200, 90, 24), 'pit': (255, 186, 64), 'vent_glow': (255, 200, 72),
    'siphon_mouth': (255, 200, 72), 'pulp_core': (208, 64, 64), 'pulp_eye': (255, 176, 48),
    'pulp_eye_green': (200, 224, 72), 'fung': (208, 96, 42), 'fung_cap': (198, 42, 30), 'shell': (255, 176, 64),
    'vane': (154, 30, 30), 'wound': (86, 14, 18),
}
FUNGAL = (126, 120, 42)
FUNGAL_LIGHT = (176, 168, 58)


def paint(cube, t):
    m = cube['material']
    base = BASE[m]
    n = t['n']
    gx, gy, gz = t['gx'], t['gy'], t['gz']
    if m == 'vane':
        # the lattice: hyphae along and across the vane, open between; drips off the lower edge
        u, v = t['u'], t['v']
        if u % 5 == 0 or v % 4 == 0 or (v == t['fh'] - 1 and u % 3 == 0):
            return add(mix((168, 34, 30), (214, 150, 52), smooth(gx, gy, gz, 4.0, 3)), (n - 0.5) * 14) + (255,)
        return None
    if m in ('egg', 'yolk', 'pit', 'vent_glow', 'siphon_mouth', 'pulp_eye', 'pulp_eye_green', 'amber'):
        rim = t['fu'] < 0.18 or t['fu'] > 0.82 or t['fv'] < 0.18 or t['fv'] > 0.82
        return add(base, -40 if rim else (n - 0.5) * 18) + (255,)
    rgb = add(base, (n - 0.5) * 16)
    lump = smooth(gx, gy, gz, 3.0, 1)
    rgb = mix(rgb, add(rgb, -22), lump * 0.6)
    if m in ('crimson', 'crimson2', 'maroon', 'chitin'):
        # plate seams and the fungus that grows out of them
        # plate boundaries follow ridges of world-space noise, so seams run organically across cubes
        seam = abs(smooth(gx, gy, gz, 2.4, 11) - 0.5) < 0.035 or abs(smooth(gx, gy, gz, 4.1, 13) - 0.5) < 0.02
        if seam:
            rgb = add(rgb, -30)
            if smooth(gx, gy, gz, 2.0, 9) > 0.55:
                rgb = mix(FUNGAL, FUNGAL_LIGHT, n)
        elif t['face'] == 'up' and smooth(gx, gy, gz, 2.5, 5) > 0.72:
            rgb = mix(FUNGAL, FUNGAL_LIGHT, n)
        if m == 'chitin' and n > 0.92:
            rgb = add(rgb, -24)                    # pits in the shell
        if m in ('crimson', 'crimson2') and abs(smooth(gx, gy, gz, 1.6, 4) - 0.5) < 0.03:
            rgb = (176, 40, 44)                    # a vein running under the skin
    elif m in ('ivory', 'bone'):
        stain = smooth(gx, gy, gz, 2.0, 7)
        if stain > 0.7:
            rgb = mix(rgb, (150, 40, 34), (stain - 0.7) * 2.0)
        elif stain < 0.22:
            rgb = mix(rgb, (126, 120, 52), (0.22 - stain) * 2.5)
        if t['side'] and t['v'] % 3 == 0:
            rgb = add(rgb, -12)                    # growth lines in the bone
    elif m in ('pink', 'pink2'):
        if t['v'] % 3 == 0:
            rgb = add(rgb, -18)                    # muscle rings
    elif m == 'membrane':
        if abs(smooth(gx, gy, gz, 1.2, 2) - 0.5) < 0.05:
            rgb = (204, 64, 52)
    elif m == 'wound':
        # the torn inside of a skull half: wet tissue in long strands, a pale marrow rind where it tore from the bone
        if t['fu'] < 0.08 or t['fu'] > 0.92 or t['fv'] < 0.06 or t['fv'] > 0.94:
            rgb = add(mix(BASE['bone'], (150, 40, 34), 0.45), (n - 0.5) * 20)
        elif abs(smooth(gx, gy * 0.35, gz, 1.4, 21) - 0.5) < 0.06:
            rgb = add((168, 52, 56), (n - 0.5) * 18)
        elif smooth(gx, gy, gz, 1.8, 23) > 0.68:
            rgb = mix(rgb, (52, 6, 10), 0.6)
    elif m == 'fung_cap' and n > 0.8:
        rgb = (232, 200, 120)
    return tuple(int(clamp(c, 0, 255)) for c in rgb) + (255,)


GLOW = {
    'pit': (255, 170, 40), 'pulp_eye': (255, 160, 30), 'pulp_eye_green': (190, 230, 60), 'egg': (255, 150, 30),
    'yolk': (255, 190, 60), 'vent_glow': (255, 196, 70), 'siphon_mouth': (255, 196, 70), 'embryo': (150, 60, 10),
    'amber': (200, 120, 20),
}


def glow(cube, t):
    m = cube['material']
    if m not in GLOW:
        return None
    rim = t['fu'] < 0.15 or t['fu'] > 0.85 or t['fv'] < 0.15 or t['fv'] > 0.85
    rgb = add(GLOW[m], -70 if rim else (t['n'] - 0.5) * 30)
    return tuple(int(clamp(c, 0, 255)) for c in rgb) + (255,)


def sac(cube, t):
    if cube['material'] != 'shell':
        return None
    vein = abs(smooth(t['gx'], t['gy'], t['gz'], 1.8, 6) - 0.5) < 0.04
    rim = t['fu'] < 0.06 or t['fu'] > 0.94 or t['fv'] < 0.06 or t['fv'] > 0.94
    if vein:
        return (140, 30, 26, 190)
    rgb = add((255, 176, 70), (t['n'] - 0.5) * 30)
    return tuple(int(clamp(c, 0, 255)) for c in rgb) + (150 if rim else 96,)


# ---------------------------------------------------------------- Java

def subtree_sizes(rig):
    children = {}
    for part in rig.parts:
        children.setdefault(part['parent'], []).append(part['name'])
    sizes = {}

    def size(name):
        if name not in sizes:
            sizes[name] = 1 + sum(size(c) for c in children.get(name, []))
        return sizes[name]
    for part in rig.parts:
        size(part['name'])
    return children, sizes


def part_line(rig, part, parent_var, declare):
    chain = 'CubeListBuilder.create()'
    for cube in part['cubes']:
        u, v = cube['uv']
        box = ', '.join(f(c) for c in cube['box'])
        chain += f'\n\t\t\t\t.texOffs({u}, {v}).addBox({box}, new CubeDeformation(0.0F))'
    rot = part['rotation']
    pivot = ', '.join(f(p) for p in part['pivot'])
    if any(abs(r) > 1e-9 for r in rot):
        pose = f'PartPose.offsetAndRotation({pivot}, {", ".join(f(math.radians(a)) for a in rot)})'
    elif any(part['pivot']):
        pose = f'PartPose.offset({pivot})'
    else:
        pose = 'PartPose.ZERO'
    decl = f'PartDefinition {camel(part["name"])} = ' if declare else ''
    return f'\t\t{decl}{parent_var}.addOrReplaceChild("{part["name"]}", {chain}, {pose});'


def body_methods(rig, split_at=24):
    """createBodyLayer split into one builder method per big subtree, keeping each under the JVM method limit."""
    by_name = {p['name']: p for p in rig.parts}
    children, sizes = subtree_sizes(rig)
    methods = []

    def emit(name, lines):
        for child in children.get(name, []):
            has_children = bool(children.get(child))
            lines.append(part_line(rig, by_name[child], camel(name), has_children))
            if not has_children:
                continue
            if sizes[child] >= split_at:
                method = 'build' + camel(child)[0].upper() + camel(child)[1:]
                lines.append(f'\t\t{method}({camel(child)});')
                inner = []
                emit(child, inner)
                methods.append((method, child, inner))
            else:
                emit(child, lines)

    top = [part_line(rig, by_name['root'], 'mesh', True)]
    emit('root', top)
    return top, methods


def java_source(rig, size, extra_fields, extra_ctor, extra_methods):
    top, methods = body_methods(rig)
    fields = '\n'.join(f'\tprotected final ModelPart {camel(p["name"])};' for p in rig.parts)
    ctor = '\n'.join(
        f'\t\tthis.{camel(p["name"])} = '
        + ('root.getChild("root")' if p['parent'] is None else f'this.{camel(p["parent"])}.getChild("{p["name"]}")')
        + ';' for p in rig.parts)
    helpers = '\n\n'.join(
        f'\tprivate static void {name}(PartDefinition {camel(part)}) {{\n' + '\n'.join(lines) + '\n\t}'
        for name, part, lines in methods)
    body = '\n'.join(top)
    return f'''package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantIntroRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantMotionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantSplitRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantState;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantYieldRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperMoveTimeline;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Generated by tools/model_export/mycophant.py; edit the rig there (and mycophant_anim.java) and re-run it. */
public class MycophantModel extends HierarchicalModel<MycophantEntity> {{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("{rig.layer}"), "main");

{fields}{extra_fields}

	public MycophantModel(ModelPart root) {{
{ctor}{extra_ctor}
	}}

	public static LayerDefinition createBodyLayer() {{
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition mesh = meshdefinition.getRoot();

{body}

		return LayerDefinition.create(meshdefinition, {size[0]}, {size[1]});
	}}

{helpers}

	@Override
	public ModelPart root() {{
		return this.root;
	}}
{extra_methods}}}
'''


def array(name, parts):
    return f'new ModelPart[]{{{", ".join("this." + camel(p) for p in parts)}}}'


def extra_java():
    fields = ['', '	private final ModelPart[][] ventPetals;', '	private final ModelPart[][] tails;',
              '	private final ModelPart[][] fingers;', '	private final ModelPart[] siphon;',
              '	private final ModelPart[][] wingSpars;', '	private final ModelPart[][] pulpTentacles;',
              '	private final ModelPart[] tongue;', '	private final ModelPart[] collarHinges;',
              '	private final ModelPart[][] faceTentacles;']
    ctor = ['']
    ctor.append('		this.ventPetals = new ModelPart[][]{' + ', '.join(array('', c) for c in CHAINS['vent_petals']) + '};')
    ctor.append('		this.tails = new ModelPart[][]{' + ', '.join(array('', c) for c in CHAINS['tails']) + '};')
    ctor.append('		this.fingers = new ModelPart[][]{' + ', '.join(array('', c) for c in CHAINS['fingers']) + '};')
    ctor.append('		this.siphon = ' + array('', CHAINS['siphon']) + ';')
    spars = [chain for wing in CHAINS['wings'] for chain in wing]
    ctor.append('		this.wingSpars = new ModelPart[][]{' + ', '.join(array('', c) for c in spars) + '};')
    ctor.append('		this.pulpTentacles = new ModelPart[][]{' + ', '.join(array('', c) for c in CHAINS['pulp_tentacles']) + '};')
    ctor.append('		this.tongue = ' + array('', CHAINS['tongue']) + ';')
    ctor.append('		this.collarHinges = ' + array('', CHAINS['collar']) + ';')
    ctor.append('		this.faceTentacles = new ModelPart[][]{' + ', '.join(array('', c) for c in CHAINS['face_tentacles']) + '};')
    return '\n'.join(fields), '\n'.join(ctor)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = spawn_mother()
    names = [camel(p['name']) for p in rig.parts]
    clashes = {n for n in names if names.count(n) > 1}
    if clashes:
        raise ValueError(f'camelCase part name clash: {sorted(clashes)}')
    size = layout(rig, ATLAS_WIDTH)
    base, glow_image, sac_image = paint_atlases(rig, size, [paint, glow, sac])
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'mycophant.png')
    glow_image.save(TEXTURE_DIR / 'mycophant_glow.png')
    sac_image.save(TEXTURE_DIR / 'mycophant_sac.png')
    fields, ctor = extra_java()
    methods = (Path(__file__).with_name('mycophant_anim.java')).read_text(encoding='utf-8')
    JAVA.write_text(java_source(rig, size, fields, ctor, methods), encoding='utf-8')
    cubes = sum(len(p['cubes']) for p in rig.parts)
    print(f'mycophant: {len(rig.parts)} parts, {cubes} cubes, atlas {size[0]}x{size[1]} '
          f'({size[0] * TEXEL_SCALE}x{size[1] * TEXEL_SCALE} px)')
    if args.preview:
        hidden = ('pulp', 'wing_l', 'wing_r')
        q = quads(rig, hidden=hidden)
        preview_sheet([(q, base, 0.0, 0.05), (q, base, math.pi / 2, 0.05), (q, base, math.pi, 0.15),
                       (q, base, 0.7, 0.3)], ROOT / 'build/mycophant_preview.png')
        opened = {'skull_half_l': (0, 70, 30), 'skull_half_r': (0, -70, -30)}
        q2 = quads(rig, overrides=opened)
        preview_sheet([(q2, base, 0.0, 0.05), (q2, base, 0.7, 0.3)], ROOT / 'build/mycophant_preview_p2.png', cols=2)


if __name__ == '__main__':
    main()
