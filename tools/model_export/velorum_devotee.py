"""Velorum's woken devotee: one of the frozen congregation, taken up off the floor in the fight.

python tools/model_export/velorum_devotee.py [--preview]

The jointed Hemomancy NPC body (court_humanoid.humanoid(): elbows four pixels below the shoulder, knees halfway, inset
fillers so a bent joint closes), never the vanilla humanoid, dressed as the frozen devotee block is: the undyed grey-blue
robe as thin panels (front and back per leg, so it walks), the hood, the pale cord, sleeves, bare pallid frost-bitten
hands and feet, rime crust thickest on the shoulders, hood and hem, faint frost lines in the folds (glow). The palette is
frozen_devotee.py's, so the block and the mob are the same people. 16 px per block; the player's size.

Revision 2026-11-06 (Frozen Cruor and liquid dark): black-red veins run under the rime of the bare hands, feet and
face (straight runs and right-angle turns, a texel wide), dark in the base atlas and a dull red in the glow atlas, so
the woken carry the congregation's old blood. The shade atlases dress the same body as a shape of liquid dark for the
Procession's shades and the Eclipse's shapes: a glossy near-black with a slow violet-black sheen, the hood's and the
hem's edges torn open into tatters (alpha cut), two pale eye points and the cruor-red hands in the shade glow.

Writes:
  src/main/java/.../client/model/entity/boss/velorum/VelorumDevoteeModel.java   (+ velorum_devotee_anim.java)
  assets/hemomancy/textures/entity/boss/velorum/velorum_devotee.png              (+ _glow.png)
  assets/hemomancy/textures/entity/boss/velorum/velorum_devotee_shade.png        (+ _shade_glow.png)
  assets/hemomancy/models/entity/bbmodel/bosses/VelorumDevoteeModel.bbmodel
  build/velorum_devotee_preview.png                                               (--preview)
"""
import argparse
import math

import annetta_kit as kit
from annetta_kit import ROOT, ASSETS, add, mix, layout, paint_atlases, entity_java_source, quads, preview_sheet
from court_humanoid import humanoid, check_rig, bbmodel, weave

kit.SCALE = 1                   # one texture pixel per model unit: 16 px per block

try:                            # the block's palette, so block and mob are the same people
    from frozen_devotee import CLOTH, RIME, RIME_SHADOW, ICE, SKIN, FROSTBITE, HAIR
except Exception:               # the block generator's own imports are heavy; its constants as of 2026-11-05
    CLOTH, RIME, RIME_SHADOW, ICE = (52, 60, 86), (180, 208, 246), (138, 174, 232), (110, 160, 236)
    SKIN, FROSTBITE, HAIR = (178, 176, 190), (150, 168, 208), (200, 206, 218)

JAVA = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumDevoteeModel.java'
TEXTURES = ASSETS / 'textures/entity/boss/velorum'
BBMODEL = ASSETS / 'models/entity/bbmodel/bosses/VelorumDevoteeModel.bbmodel'
ANIM = ROOT / 'tools/model_export/velorum_devotee_anim.java'
CORD = (198, 196, 186)
# Frozen Cruor (design revision 2026-11-06, R.2): the veins under the rime, and the liquid dark of the shapes
VEIN = (52, 12, 18)
VEIN_GLOW = (70, 18, 24)
DARK = (7, 4, 12)
SHEEN = (26, 16, 48)
EYE = (214, 236, 255)
CRUOR_HAND = (110, 30, 40)


def h2(a, b, seed=0):
    return ((int(a) * 73856093) ^ (int(b) * 19349663) ^ (seed * 83492791)) % 1000 / 1000.0


def vein(cube, t):
    """A black-red vein a texel wide under the skin: a straight run down each side face of the bare hands and feet, with
    a right-angle branch every few texels, and two down the face from the temples. No curves, no web."""
    m = cube['material']
    if not t['side']:
        return False
    u, v, fw = t['u'], t['v'], t['fw']
    if m in ('arm', 'leg'):
        if cube.get('filler'):
            return False
        lower = cube.get('seg', 0.0) > 0.0
        if not lower and t['fv'] < 0.55:
            return False
        run = 1 + int(h2(t['gx'] * 2, t['gz'] * 2, 3) * max(1, fw - 2))
        if u == run:
            return True
        return v % 4 == 2 and run < u <= run + 2
    if m == 'face' and t['face'] in ('east', 'west'):
        return u in (2, fw - 3) and v >= 2 or (v == 5 and 2 <= u <= 4)
    return False


def devotee():
    r = humanoid('velorum_devotee', 'VelorumDevoteeModel')
    r.name, r.layer = 'velorum_devotee', 'velorum_devotee'
    # the hood, deep and plain, with its rime crust
    r.cube('head', (-4.5, -8.5, -4.5, 9.0, 1.5, 9.0), 'robe')
    for sign in (-1, 1):
        r.cube('head', (sign * 4.25 - 0.25 if sign > 0 else -4.5, -7.0, -4.5, 0.5, 7.0, 8.5), 'robe')
    # the hood's back hangs as its own drape off the crown, so it swings and settles behind the head
    r.part('hood_tail', 'head', (0, -7, 4))
    r.cube('hood_tail', (-4.5, 0.0, -0.25, 9.0, 7.5, 0.75), 'robe')
    r.cube('hood_tail', (-4.0, 7.5, -0.25, 8.0, 1.75, 0.5), 'robe')
    r.cube('head', (-4.75, -8.75, -4.75, 9.5, 0.5, 9.5), 'rime')
    # the robe over the body, the pale cord at the waist, rime on the shoulders
    r.cube('body', (-4.25, -10.25, -2.25, 8.5, 11.5, 4.5), 'robe')
    r.cube('body', (-4.5, 0.0, -2.5, 9.0, 1.0, 5.0), 'cord')
    r.cube('body', (-4.5, -10.5, -2.5, 9.0, 0.5, 5.0), 'rime')
    # the cord's knotted ends hang from the waist and swing as it walks
    r.part('cord_ends', 'body', (1.5, 1.0, -2.5))
    r.cube('cord_ends', (-0.75, 0.0, -0.25, 0.5, 5.0, 0.5), 'cord')
    r.cube('cord_ends', (0.25, 0.0, -0.25, 0.5, 4.0, 0.5), 'cord')
    # the sleeves hang loose over the arms; the bare hands show under them
    for side, sign in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        x0 = -3.25 if sign < 0 else -1.25
        r.cube(arm, (x0, -2.25, -2.25, 4.5, 6.5, 4.5), 'robe')
        r.cube(arm, (x0, -2.5, -2.5, 4.5, 0.5, 5.0), 'rime')
        r.cube(fore, (-2.25, 0.0, -2.25, 4.5, 4.0, 4.5), 'robe')
        # the sleeve's loose hem, its own part so it hangs and swings off the arm
        r.part(f'{side}_cuff', fore, (0, 3.25, 0))
        r.cube(f'{side}_cuff', (-2.5, 0.0, -2.5, 5.0, 1.5, 5.0), 'robe')
        # the robe as thin panels, front and back per leg, so it walks
        leg, shin = f'{side}_leg', f'{side}_shin'
        r.cube(leg, (-2.25, 0.0, -2.5, 4.5, 6.0, 0.5), 'robe')
        r.cube(leg, (-2.25, 0.0, 2.0, 4.5, 6.0, 0.5), 'robe')
        r.cube(shin, (-2.25, 0.0, -2.5, 4.5, 5.0, 0.5), 'robe')
        r.cube(shin, (-2.25, 0.0, 2.0, 4.5, 5.0, 0.5), 'robe')
        r.cube(shin, (-2.5, 4.5, -2.75, 5.0, 0.5, 5.5), 'rime')
    check_rig(r)
    return r


def paint(cube, t, glow=False):
    m = cube['material']
    lit = False
    if m == 'face':
        rgb = add(SKIN, (t['n'] - 0.5) * 8)
        if t['face'] == 'north':
            px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
            side = px if px < 4 else 7 - px
            if py == 4 and side in (1, 2):
                rgb = add(SKIN, -46)            # eyes closed under rime; a hostile's open glint is the glow
                lit = True
            elif py in (5, 6) and px in (3, 4):
                rgb = add(SKIN, -16)
            if py <= 1:
                rgb = mix(rgb, RIME, 0.6)
        else:
            rgb = add(HAIR, (t['n'] - 0.5) * 10)
    elif m in ('arm', 'leg'):
        # bare pallid frost-bitten hands and feet under the robe
        rgb = mix(add(SKIN, (t['n'] - 0.5) * 8), FROSTBITE, 0.35 + 0.3 * t['fv'])
    elif m == 'torso':
        rgb = add(CLOTH, -8)
    elif m == 'robe':
        rgb = weave(CLOTH, t)
        # rime crust in two-texel clumps over the cloth (the block figure is rimed all over; the mob keeps enough of it
        # that the swap from block to mob does not jump), thicker down the robe and thickest at the hem
        clump = ((t['u'] // 2) * 73856093 ^ (t['v'] // 2) * 19349663) % 100 / 100.0
        if t['side'] and clump < 0.12 + 0.3 * t['fv']:
            rgb = mix(rgb, RIME_SHADOW, 0.4 + 0.2 * t['n'])
        if t['side'] and t['fv'] > 0.8:
            rgb = mix(rgb, RIME_SHADOW, 0.6)
        if t['face'] == 'up':
            rgb = mix(rgb, RIME, 0.7)
        lit = t['side'] and t['n'] > 0.985          # faint frost lines in the folds
    elif m == 'cord':
        rgb = CORD if (t['u'] + t['v']) % 3 else add(CORD, -30)
    elif m == 'rime':
        rgb = mix(RIME, RIME_SHADOW, t['n'])
        if t['face'] == 'up':
            rgb = mix(rgb, (232, 242, 252), 0.5)
        lit = t['n'] > 0.92
    else:
        rgb = weave(CLOTH, t)
    veined = vein(cube, t)
    if glow:
        if veined:
            return (*VEIN_GLOW, 255)
        return (*ICE, 255) if lit and m != 'face' else ((214, 236, 255, 255) if lit else (0, 0, 0, 0))
    if veined:
        rgb = mix(rgb, VEIN, 0.55)
    return (*[max(0, min(255, int(c))) for c in rgb], 255)


def shade(cube, t, glow=False):
    """The same body as a shape of liquid dark: glossy near-black, a slow violet-black sheen, torn edges at the hood
    and the hem (the alpha cut, so the translucent type shows tatters), two pale eye points, the cruor-red hands."""
    m = cube['material']
    if glow:
        if m == 'face' and t['face'] == 'north':
            px, py = min(7, int(t['lx'] + 4)), min(7, int(t['ly'] + 8))
            side = px if px < 4 else 7 - px
            if py == 4 and side in (1, 2):
                return (*EYE, 255)
        if m == 'arm' and cube.get('seg', 0.0) > 0.0 and not cube.get('filler') and t['fv'] > 0.45:
            return (*CRUOR_HAND, 255)
        if vein(cube, t):
            return (*VEIN_GLOW, 255)
        # polish round 1: a shape stood unseen against the Eclipse's dark. The red seams under it light the lower part
        # of its robe a dull cruor, rising faintly up the cloth (this atlas is additive). Round 2: no rim on the cube
        # edges, which outlined every box like a wireframe
        if t['side'] and m in ('robe', 'rime'):
            under = max(0.0, (t['fv'] - 0.25) / 0.75)
            grain = 0.8 + 0.4 * kit.smooth(t['gx'], t['gy'], t['gz'], scale=2, seed=33)
            rgb = (104 * under * under * grain, 18 * under * under * grain, 26 * under * under * grain)
            if max(rgb) >= 4:
                return (*[min(255, int(c)) for c in rgb], 255)
        return (0, 0, 0, 0)
    sheen = kit.smooth(t['gx'], t['gy'], t['gz'], scale=3, seed=21)
    rgb = mix(DARK, SHEEN, 0.15 + 0.5 * sheen * sheen)
    if t['face'] == 'up':
        rgb = mix(rgb, SHEEN, 0.35)
    alpha = 235
    # torn edges: the robe's and the hood's lower rows fray into tatters, broad and few, never thin strands
    if m in ('robe', 'rime') and t['side']:
        tear = h2(t['u'] // 2, t['gz'] + t['gx'], 7)
        if t['fv'] > 0.78 and tear < (t['fv'] - 0.78) * 3.2:
            alpha = 0
        elif t['fv'] > 0.6:
            alpha = int(235 - 120 * (t['fv'] - 0.6) / 0.4)
    return (*[max(0, min(255, int(c))) for c in rgb], alpha)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    rig = devotee()
    size = layout(rig, 128)
    base, glow, dark, dark_glow = paint_atlases(rig, size, [paint, lambda c, t: paint(c, t, True), shade,
                                                            lambda c, t: shade(c, t, True)])
    TEXTURES.mkdir(parents=True, exist_ok=True)
    base.save(TEXTURES / 'velorum_devotee.png')
    glow.save(TEXTURES / 'velorum_devotee_glow.png')
    dark.save(TEXTURES / 'velorum_devotee_shade.png')
    dark_glow.save(TEXTURES / 'velorum_devotee_shade_glow.png')
    source = entity_java_source(rig, size, 'VelorumDevoteeEntity', 'velorum_devotee.py',
        package='com.vincenthuto.hemomancy.client.model.entity.boss.velorum',
        entity_package='com.vincenthuto.hemomancy.common.entity.boss.saint.velorum',
        extra_imports='import com.vincenthuto.hemomancy.client.render.AnimationTime;\n'
                      'import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumCongregationRules;\n'
                      'import com.vincenthuto.hemomancy.common.util.HemoEasing;\n\n'
                      'import static com.vincenthuto.hemomancy.common.util.HemoEasing.catmullRom;\n',
        extra_methods=ANIM.read_text(encoding='utf-8'))
    source = source.replace('import net.minecraft.world.entity.HumanoidArm;\n', '')
    JAVA.write_text(source, encoding='utf-8')
    bbmodel(rig, size, TEXTURES / 'velorum_devotee.png', BBMODEL)
    print(f'Velorum devotee: {sum(len(p["cubes"]) for p in rig.parts)} cubes; {len(rig.parts)} joints; atlas {base.size}')
    if args.preview:
        kneel = {'right_shin': (90, 0, 0), 'left_shin': (90, 0, 0)}
        reach = dict(kneel, right_arm=(-120, 0, 0))
        tiles = []
        for overrides in ({}, kneel, reach):
            q = quads(rig, overrides=overrides)
            tiles += [(q, base, 0.0, 0.1), (q, base, math.pi * 0.75, 0.15)]
        preview_sheet(tiles, ROOT / 'build/velorum_devotee_preview.png', cols=2)


if __name__ == '__main__':
    main()
