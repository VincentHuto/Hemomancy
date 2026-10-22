"""The Stained Priestess: Annetta's headless body, worn by the infection after the tick bursts.

The box rig is only her corpse: robe tatters, dead-grey limbs, the neck stump and a dark knot core where her head
was. Everything alive on her (wings, the thorn-and-tentacle head, the tendrils wrapping and dragging from her)
is procedural, drawn by StainedPriestessTendrilLayer from PriestessTendrilRules.

Emits StainedPriestessModel.java and the textures:
  stained_priestess.png          the corpse
  stained_priestess_tendril.png  tube strip for every tendril (u around, v along, tiles)
  stained_priestess_webbing.png  translucent veined membrane between the wing fingers
  stained_priestess_eye.png      additive almond eye for the three eyes

    python tools/model_export/stained_priestess.py            # write model + textures
    python tools/model_export/stained_priestess.py --preview  # also write build/stained_priestess_preview.png

Palette: blood, vessel red, infected yellow-orange, fungal grey and bone. Her copper is gone; nothing Unstained
survives on this body except the blue running through the lancea she pulls from the pool.
"""
import argparse
from pathlib import Path
import math

from PIL import Image
from annetta_kit import (MODEL_DIR, ROOT, TEXTURE_DIR, Rig, add, blood, entity_java_source, fungus, h, infected_ichor,
                         layout, mix, paint_atlases, preview_sheet, quads, robe_white, smooth)

import annetta_kit

# Block texel density: she renders at 1.2x, so one texture pixel per model unit is ~13 px per block, the nearest
# whole density to the mod's 16 px per block blocks and items. The tendril, webbing and eye sprites wrap
# procedural geometry, so they are box-filtered down to sizes that land near the same density.
TEXEL_SCALE = 1
annetta_kit.SCALE = TEXEL_SCALE
TENDRIL_SIZE = (8, 32)
WEBBING_SIZE = (16, 32)
EYE_SIZE = (16, 8)

JAVA = MODEL_DIR / 'StainedPriestessModel.java'
ATLAS_WIDTH = 128


# ---------------------------------------------------------------- rig

def priestess():
    """Her corpse as a jointed body: the pelvis is the hinge everything hangs from, a two-piece spine (waist and
    chest) bends above it, and arms and legs break at elbow and knee. Each segment overlaps the next past its
    pivot, so a bend closes over the joint instead of opening a gap. `body` is the chest's unrotated frame, kept
    at the neck so the tendril layer's wing and spill coordinates stay where they were."""
    r = Rig('stained_priestess', 'StainedPriestessModel', 'stained_priestess')

    r.part('hips', 'root', (0, 12, 0))
    r.cube('hips', (-4.3, -2.6, -2.3, 8.6, 4.6, 4.6), 'robe_ruin')
    for i, (x, z, length) in enumerate([(-3.2, -2.4, 9.5), (-0.4, -2.5, 11.5), (2.4, -2.4, 8.0),
                                        (-2.0, 2.0, 10.5), (1.6, 2.0, 12.0)]):
        name = f'robe_tatters_{i}'
        r.part(name, 'hips', (x, -2.0, z))
        r.cube(name, (-1.1, 0.0, -0.25, 2.2, length, 0.5), 'tatter')

    r.part('torso', 'hips', (0, 0, 0))
    r.cube('torso', (-3.7, -6.5, -1.9, 7.4, 7.0, 3.8), 'robe_ruin')
    r.cube('torso', (-3.95, -4.2, -2.15, 7.9, 1.0, 4.3), 'robe_ruin')
    r.part('chest', 'torso', (0, -5, 0))
    r.part('body', 'chest', (0, -7, 0))
    r.cube('body', (-4.0, 0.0, -2.0, 8.0, 8.0, 4.0), 'robe_ruin')
    r.cube('body', (-1.6, 1.0, 1.95, 3.2, 4.0, 0.3), 'wound')

    r.part('neck_stump', 'body', (0, -0.2, 0))
    r.cube('neck_stump', (-2.0, -1.6, -2.0, 4.0, 1.6, 4.0), 'stump')

    # A dark knot where her head was; the tendril layer boils around it and sets the eyes on its face.
    r.part('thorn_head', 'neck_stump', (0, -1.6, 0))
    r.cube('thorn_head', (-2.2, -5.4, -2.5, 4.4, 4.6, 4.4), 'thorn_core')

    for side, x in (('right', -5.5), ('left', 5.5)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        r.part(arm, 'body', (x, 2, 0))
        r.cube(arm, (-1.5, -2.0, -1.5, 3.0, 7.6, 3.0), 'dead_flesh')
        r.cube(arm, (-1.75, -2.25, -1.75, 3.5, 3.5, 3.5), 'robe_ruin')
        r.part(fore, arm, (0, 5, 0))
        r.cube(fore, (-1.4, -1.1, -1.4, 2.8, 5.7, 2.8), 'dead_flesh')
        r.cube(fore, (-1.25, 4.6, -1.25, 2.5, 1.7, 2.5), 'hand')

    for side, x in (('right', -1.9), ('left', 1.9)):
        leg, shin = f'{side}_leg', f'{side}_shin'
        r.part(leg, 'hips', (x, 0, 0))
        r.cube(leg, (-1.8, 0.0, -1.8, 3.6, 6.6, 3.6), 'dead_flesh')
        r.part(shin, leg, (0, 6, 0))
        r.cube(shin, (-1.7, -0.8, -1.7, 3.4, 6.8, 3.4), 'dead_flesh')

    # The lancea's grip; the spear layer draws it here and both hands are solved onto its shaft.
    r.part('spear', 'body')
    return r


# ---------------------------------------------------------------- paint

def soaked(t, rgb):
    """How far the blood has run from the stump down her robes: a wet bib, runnels, drying at the edges."""
    s = smooth(t['gx'], t['gy'] * 0.35, t['gz'], 2.5, 7)
    wet = 1.15 - t['gy'] * 0.075 + (s - 0.5) * 0.9
    if smooth(t['gx'] * 3.0, 0, t['gz'] * 3.0, 2.0, 11) > 0.62:
        wet += 0.55                   # runnels down from the neck
    wet = max(0.0, min(1.0, wet))
    if 0.25 < wet < 0.4:
        return mix(rgb, (92, 40, 36), 0.55)  # dried rim
    return mix(rgb, blood(t), wet)


def paint(cube, t):
    m = cube['material']
    if m in ('robe_ruin', 'tatter'):
        rgb = soaked(t, add(robe_white(t), -18))
        if m == 'tatter' and t['fv'] > 0.85:
            rgb = mix(rgb, (52, 6, 10), 0.6)
        return rgb + (255,)
    if m == 'dead_flesh':
        rgb = mix((178, 162, 156), (140, 124, 122), smooth(t['gx'], t['gy'], t['gz'], 2.0, 3) * 0.8 + t['n'] * 0.2)
        if abs(smooth(t['gx'], t['gy'], t['gz'], 1.5, 5) - 0.5) < 0.035:
            rgb = (92, 42, 58)        # veins gone dark under grey skin
        return soaked(t, rgb) + (255,)
    if m == 'hand':
        # Fingers gone black where the infection pooled first.
        rgb = mix((96, 70, 74), (34, 18, 24), min(1.0, t['fv'] * 1.3) * 0.8 + t['n'] * 0.2)
        return rgb + (255,)
    if m in ('stump', 'wound'):
        rgb = blood(t) if t['n'] > 0.3 else infected_ichor(t)
        return rgb + (255,)
    if m == 'thorn_core':
        return mix((52, 10, 18), (20, 4, 8), smooth(t['gx'], t['gy'], t['gz'], 1.2, 4) * 0.7 + t['n'] * 0.3) + (255,)
    raise ValueError('unpainted material ' + m)


# ---------------------------------------------------------------- procedural tendril textures

def tendril_strip():
    """Wraps a tendril tube: u around the circumference, v along the length (tiles vertically)."""
    w, hgt = 32, 128
    image = Image.new('RGBA', (w, hgt))
    for py in range(hgt):
        for px in range(w):
            u, v = px / w, py / hgt
            wave = smooth(px * 0.9, py * 0.35, 0, 2.2, 5)
            rgb = mix((132, 22, 34), (64, 8, 18), wave)
            if abs(math.sin(u * math.pi * 6 + math.sin(v * math.tau * 2) * 0.8)) < 0.16:
                rgb = mix(rgb, (40, 4, 12), 0.6)          # twisted vessel seams
            if 0.18 < u < 0.26:
                rgb = add(rgb, 34)                         # wet highlight running the length
            band = abs(math.sin(v * math.tau * 3 + smooth(px, 0, 0, 3.0, 9) * 2))
            if band < 0.12 and smooth(px * 2, py, 0, 1.5, 7) > 0.45:
                rgb = mix(infected_ichor(dict(n=h(px, py))), (240, 196, 96), 0.25)   # infected node rings
            elif smooth(px * 1.5, py, 3, 1.2, 12) > 0.84:
                rgb = mix(rgb, fungus(dict(n=h(px, py))), 0.7)                       # fungal crust
            image.putpixel((px, py), rgb + (255,))
    return image


def webbing():
    """Membrane between finger tendrils: u across the gap, v from root (0) to torn edge (1).

    Veins run out from the root and fork toward the edge; capillaries cross between them; the skin thins and tears
    near the trailing edge.
    """
    size = 64
    image = Image.new('RGBA', (size, size))
    for py in range(size):
        for px in range(size):
            u, v = px / size, py / size
            sway = 0.05 * math.sin(v * 7.0 + u * 3.0)
            main = abs(math.sin((u + sway) * math.pi * 3.0)) < 0.07 + 0.04 * (1 - v)
            fork = v > 0.45 and abs(math.sin((u - sway) * math.pi * 6.0 + v * 2.0)) < 0.05
            capillary = abs(math.sin(v * math.pi * 9.0 + u * 4.0 + smooth(px, py, 0, 6.0, 31) * 3)) < 0.035
            tear = smooth(px * 0.8, py * 1.2, 0, 4.0, 23) < 0.05 + 0.5 * max(0.0, v - 0.55) ** 1.5 * 3
            if tear and not main:
                continue
            body = smooth(px * 0.5, py * 0.5, 0, 5.0, 29)
            rgb = mix((176, 40, 54), (110, 16, 30), body * 0.6 + v * 0.4)
            alpha = 196 - int(80 * v)
            if main or fork:
                rgb, alpha = mix((72, 6, 18), (120, 14, 28), v), 240
            elif capillary:
                rgb, alpha = mix(rgb, (88, 10, 22), 0.5), alpha + 20
            image.putpixel((px, py), rgb + (max(0, min(255, alpha)),))
    return image


def eye_sprite():
    """Additive almond eye: black is invisible, so only the iris and its rim glow."""
    w, hgt = 32, 16
    image = Image.new('RGBA', (w, hgt), (0, 0, 0, 255))
    for py in range(hgt):
        for px in range(w):
            x, y = (px + 0.5) / w * 2 - 1, (py + 0.5) / hgt * 2 - 1
            d = x * x + (y / (0.62 * (1 - 0.35 * x * x))) ** 2
            if d > 1.0:
                continue
            if abs(x) < 0.07 + 0.05 * (1 - abs(y)):
                continue                                   # the slit stays dark
            k = 1.0 - d
            rgb = mix((150, 70, 6), (255, 226, 96), min(1.0, k * 1.6))
            if d > 0.82:
                rgb = (255, 244, 170)                      # hot rim
            image.putpixel((px, py), rgb + (255,))
    return image


# ---------------------------------------------------------------- Java

EXTRA_FIELDS = '''

	public enum WingPose { FOLDED, SPREAD, SHIELD }

	/** Review-client override for the tendril wings; null in play. */
	public static WingPose reviewWingPose;'''

# The animation timelines live as plain Java beside this script so they read and diff as Java.
EXTRA_METHODS = (Path(__file__).with_name('stained_priestess_anim.java')).read_text(encoding='utf-8')

EXTRA_IMPORTS = """import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
"""


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = priestess()
    size = layout(rig, ATLAS_WIDTH)
    (base,) = paint_atlases(rig, size, [paint])
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURE_DIR / 'stained_priestess.png')
    tendril_strip().resize(TENDRIL_SIZE, Image.BOX).save(TEXTURE_DIR / 'stained_priestess_tendril.png')
    webbing().resize(WEBBING_SIZE, Image.BOX).save(TEXTURE_DIR / 'stained_priestess_webbing.png')
    eye_sprite().resize(EYE_SIZE, Image.BOX).save(TEXTURE_DIR / 'stained_priestess_eye.png')
    JAVA.write_text(entity_java_source(rig, size, 'StainedPriestessEntity', 'stained_priestess.py', armed=True,
                                       extra_fields=EXTRA_FIELDS, extra_methods=EXTRA_METHODS,
                                       extra_imports=EXTRA_IMPORTS), encoding='utf-8')
    print(f'priestess: {sum(len(p["cubes"]) for p in rig.parts)} cubes, atlas {size[0]}x{size[1]}')
    if args.preview:
        q = quads(rig)
        preview_sheet([(q, base, 0.0, 0.1), (q, base, math.pi, 0.1)], ROOT / 'build/stained_priestess_preview.png')


if __name__ == '__main__':
    main()
