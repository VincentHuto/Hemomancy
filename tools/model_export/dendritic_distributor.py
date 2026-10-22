"""Author the Dendritic Distributor: a living neuron grown into a seven-position loadout selector.

A hematic iron dish holds a ganglion plexus whose roots creep over the rim. A twisting fascicle rises
from it in two white myelin internodes with bare nodes of Ranvier between them. The fascicle tapers
through an axon hillock into the soma. Around the soma a scalloped synaptic membrane spreads seven lobes, one per
remembered pattern (three by default, seven at full Synaptic Memory). A tapering, branching dendrite runs
down each lobe to a dormant synaptic bouton of Mnemonic Ambergris. The tissue is the yellowed white
of real nerve, with crimson kept to sparse capillaries. Apical dendrites fork twice above the
soma. No brass: the dish and the soma clamp are hematic iron.

The selection is not baked. DendriticDistributorRenderer draws three part models from the same atlas,
all authored for the centre slot (pointing -z) and turned about HUB:
  dendritic_distributor_selector    a myelinated axon that leaves the iron clamp and synapses on a bouton
  dendritic_distributor_bouton_lit  a full-bright yellow-white shell over the selected bouton
  dendritic_distributor_impulse     the action potential that runs out along the selected dendrite
The item model bakes the selector and lit shell at the centre slot.

Emits the block, part and item models, the painted atlas and an editable Blockbench project.
Python 3 + Pillow + NumPy. `--preview` writes build/model-previews/dendritic_distributor.png (the item
model, i.e. block plus selector).

Coordinates are block pixels for the north-facing state with the membrane facing -z. Keep HUB, SLOTS,
BOUTON_R and IMPULSE_* in step with DendriticSelectorRules and DendriticDistributorRenderer.
"""
import argparse
import math

from clairaudiograph import clamp, noise
from fixture_atlas import ASSETS, box, build, item_elements, model_json, preview, separate, spin, write
from mnemonic_candle import octagon_prism

ATLAS = 'dendritic_distributor'
BRIGHT = {'shade': False, 'neoforge_data': {'block_light': 15, 'sky_light': 15}}

HUB = (8, 9)                      # fan centre in x/z: trunk, soma, clamp and selector pivot share it
MEMBRANE_Y = 8                    # top of the synaptic membrane
MEMBRANE_R = 6                    # lobe tip radius
LOBE_DEPTH = 1.5                  # how far the membrane scallops in between lobes
HUB_R = 2.25                      # the membrane's round boss behind the soma
SLOTS = [-67.5, -45, -22.5, 0, 22.5, 45, 67.5]   # slot 0 first; MC element rotations allow 22.5 steps
BOUTON_R = 5
IMPULSE_Y = 8.875                 # centre height of the impulse bead as it rides the dendrite
IMPULSE_START, IMPULSE_END = 1.5, 4.5

NERVE = (230, 218, 178)          # nerve is a yellowed white, not pink
NERVE_DARK = (178, 160, 112)
CAPILLARY = (136, 22, 34)
MYELIN = (246, 243, 228)
FLESH = (212, 186, 116)          # ganglion and membrane: waxy yellow tissue
SOMA = (226, 200, 128)
GLOW_CORE = (255, 252, 226)      # the selection burns yellow-white, like the distributor's bolts
GLOW = (242, 204, 72)
WAX = (108, 100, 78)
NACRE = (166, 152, 112)


def heading(phi):
    """Unit x/z direction for an angle measured from -z, matching MC's right-handed y rotation."""
    a = math.radians(phi)
    return -math.sin(a), -math.cos(a)


def quarter(phi):
    """Split an angle into a quarter turn (0..3) and the +/-45 degree residual an element can carry."""
    k = round(phi / 90)
    return k % 4, phi - k * 90


def along(phi, r, centre=HUB):
    dx, dz = heading(phi)
    return centre[0] + dx * r, centre[1] + dz * r


def spoke(name, phi, r0, r1, y0, y1, thick, mat, centre=HUB, **kw):
    """A bar from radius r0 to r1 along `phi`, laid along the nearest axis and turned by the residual."""
    cx, cz = centre
    t = thick / 2
    k, rho = quarter(phi)
    lo, hi = [
        ((cx - t, y0, cz - r1), (cx + t, y1, cz - r0)),
        ((cx - r1, y0, cz - t), (cx - r0, y1, cz + t)),
        ((cx - t, y0, cz + r0), (cx + t, y1, cz + r1)),
        ((cx + r0, y0, cz - t), (cx + r1, y1, cz + t)),
    ][k]
    rotation = spin('y', rho, (cx, (y0 + y1) / 2, cz)) if rho else None
    return box(name, lo, hi, mat, rotation=rotation, **kw)


def bead(name, phi, r, y0, y1, size, mat, centre=HUB, **kw):
    """A square block centred on the `phi` ray at radius r, squared to that ray."""
    px, pz = along(phi, r, centre)
    h = size / 2
    rho = quarter(phi)[1]
    rotation = spin('y', rho, (px, (y0 + y1) / 2, pz)) if rho else None
    return box(name, (px - h, y0, pz - h), (px + h, y1, pz + h), mat, rotation=rotation, **kw)


def rounded(name, phi, r, y0, y1, size, mat, **kw):
    """A knob: a core block plus a wider, flatter girth so it reads round from every side."""
    girth = (y1 - y0) * .3
    return [bead(f'{name}_core', phi, r, y0, y1, size, mat, **kw),
            bead(f'{name}_girth', phi, r, y0 + girth, y1 - girth, size + .5, mat, **kw)]


# --- static block ---------------------------------------------------------------------------

def base():
    cx, cz = HUB
    e = octagon_prism('dish', 2.5, 13.5, 2.5, 13.5, 0, 1, 2, 'iron')
    e += octagon_prism('plexus', cx - 3.5, cx + 3.5, cz - 3.5, cz + 3.5, 1, 2.25, 2, 'ganglion', skip=('down',))
    e += octagon_prism('plexus_crown', cx - 2, cx + 2, cz - 2, cz + 2, 2.25, 3, 1, 'ganglion', skip=('down',))
    # Roots: over the plexus, down its flank, then thinning across the dish toward the rim.
    for phi, length in [(180, 4), (135, 4.25), (-135, 4), (90, 5), (-112.5, 4.75), (45, 5.5), (-22.5, 5.75)]:
        e.append(spoke('root', phi, 1, 3.5, 2.5, 3.25, 1, 'nerve', skip=('down',)))
        e.append(spoke('root_flank', phi, 3.25, 4, 1, 3, .875, 'nerve', skip=('down',)))
        e.append(spoke('root_run', phi, 3.75, length, 1, 1.5, .625, 'nerve', skip=('down',)))
        e.append(bead('root_tip', phi, length, 1, 1.375, .5, 'nerve', skip=('down',)))
    return e


def internode(name, y0, y1, turn):
    """A swollen myelin segment, octagonal in plan; alternate segments turn so the fascicle twists."""
    cx, cz = HUB
    rot = spin('y', turn, (cx, (y0 + y1) / 2, cz)) if turn else None
    return [box(f'{name}_core', (cx - 1, y0, cz - 1), (cx + 1, y1, cz + 1), 'myelin', rotation=rot),
            box(f'{name}_x', (cx - 1.25, y0 + .25, cz - .625), (cx + 1.25, y1 - .25, cz + .625), 'myelin',
                rotation=rot),
            box(f'{name}_z', (cx - .625, y0 + .25, cz - 1.25), (cx + .625, y1 - .25, cz + 1.25), 'myelin',
                rotation=rot)]


def fascicle():
    cx, cz = HUB
    e = [box('axon', (cx - .75, 3, cz - .75), (cx + .75, 7.75, cz + .75), 'nerve', skip=('down', 'up'))]
    e += internode('internode_low', 3.25, 4.75, 0)
    e += internode('internode_high', 5.125, 6.625, 22.5)
    # Axon hillock flaring up into the soma.
    e.append(box('hillock', (cx - 1, 7, cz - 1), (cx + 1, 7.75, cz + 1), 'nerve', skip=('down', 'up'),
                 rotation=spin('y', 45, (cx, 7.375, cz))))
    return e


def membrane():
    cx, cz = HUB
    lo = (cx - MEMBRANE_R, MEMBRANE_Y - .25, cz - MEMBRANE_R)
    hi = (cx + MEMBRANE_R, MEMBRANE_Y, cz + HUB_R)
    return [box('membrane', lo, hi, 'membrane', skip=('north', 'south', 'east', 'west'), density=4)]


def dendrite(phi, side):
    e = [spoke('dendrite', phi, 1.5, 3.375, MEMBRANE_Y, MEMBRANE_Y + .875, 1, 'nerve', skip=('down',)),
         spoke('dendrite_distal', phi, 3.25, BOUTON_R - .5, MEMBRANE_Y, MEMBRANE_Y + .625, .625, 'nerve',
               skip=('down',))]
    # One side twig per dendrite, alternating sides, budding on the membrane.
    fork = along(phi, 3.125)
    twig = phi + side * 22.5
    e.append(spoke('twig', twig, 0, 1.5, MEMBRANE_Y, MEMBRANE_Y + .375, .375, 'nerve', centre=fork, skip=('down',)))
    e.append(bead('twig_bud', twig, 1.5, MEMBRANE_Y, MEMBRANE_Y + .5, .5, 'nerve', centre=fork, skip=('down',)))
    e += rounded('bouton', phi, BOUTON_R, MEMBRANE_Y, MEMBRANE_Y + 1.25, 1.25, 'bouton', skip=('down',))
    return e


def soma():
    cx, cz = HUB
    return [box('soma_core', (cx - 1.5, MEMBRANE_Y, cz - 1.5), (cx + 1.5, 11, cz + 1.5), 'soma', skip=('down',)),
            box('soma_girth_x', (cx - 2, 8.75, cz - 1), (cx + 2, 10.25, cz + 1), 'soma', skip=('down',)),
            box('soma_girth_z', (cx - 1, 8.75, cz - 2), (cx + 1, 10.25, cz + 2), 'soma', skip=('down',)),
            box('soma_crown', (cx - 1, 11, cz - 1), (cx + 1, 11.5, cz + 1), 'soma', skip=('down',)),
            box('soma_lump', (cx + .75, 9.75, cz + .5), (cx + 2.25, 11, cz + 1.75), 'soma'),
            box('soma_lump', (cx - 2.25, 8.5, cz - .25), (cx - 1, 9.75, cz + 1.25), 'soma'),
            # The Harbinger's clamp: the one made thing on the soma, where the selector axon leaves it.
            box('clamp', (cx - 1.25, 11, cz - 1.25), (cx + 1.25, 11.75, cz + 1.25), 'ferric')]


def apical():
    """Apical dendrites rising from the back of the soma, forking twice."""
    cx = HUB[0]
    z0, z1 = 10.5, 11.25
    zc = (z0 + z1) / 2
    e = [box('apical', (cx - .375, 10.5, z0), (cx + .375, 13.5, z1), 'nerve', skip=('down',))]

    def tip(x, y, z, axis, angle, length):
        a = math.radians(angle)
        if axis == 'z':
            return x - length * math.sin(a), y + length * math.cos(a), z
        return x, y + length * math.cos(a), z + length * math.sin(a)

    for axis, angle in [('z', 22.5), ('z', -22.5), ('x', 22.5)]:
        e.append(box('apical_twig', (cx - .25, 13.5, zc - .25), (cx + .25, 15.25, zc + .25), 'nerve',
                     rotation=spin(axis, angle, (cx, 13.5, zc))))
        tx, ty, tz = tip(cx, 13.5, zc, axis, angle, 1.625)
        for sub in (angle - 22.5, angle + 22.5):
            if abs(sub) > 45:
                continue
            e.append(box('apical_sprig', (tx - .1875, ty, tz - .1875), (tx + .1875, ty + 1, tz + .1875), 'nerve',
                         rotation=spin(axis, sub, (tx, ty, tz)) if sub else None))
            bx, by, bz = tip(tx, ty, tz, axis, sub, 1)
            e.append(box('apical_bud', (bx - .25, by - .25, bz - .25), (bx + .25, by + .25, bz + .25), 'bouton'))
    return e


def distributor():
    e = base() + fascicle() + membrane() + soma() + apical()
    for i, phi in enumerate(SLOTS):
        e += dendrite(phi, 1 if i % 2 else -1)
    return e


# --- parts the renderer turns to the selected slot (authored for the centre slot, phi = 0) -------

def selector():
    """A myelinated axon leaving the clamp, arching out and down to synapse on the selected bouton."""
    cx, cz = HUB
    e = [box('selector_root', (cx - .375, 11.75, cz - .375), (cx + .375, 12.75, cz + .375), 'nerve'),
         spoke('selector_axon', 0, 0, 3.375, 12.25, 12.875, .625, 'nerve'),
         spoke('selector_myelin', 0, .75, 1.75, 12.125, 13, .875, 'myelin'),
         spoke('selector_myelin', 0, 2, 3, 12.125, 13, .875, 'myelin')]
    # The descent, turned 45 degrees about x so its top leans back toward the soma.
    zc = cz - 4
    e.append(box('selector_descent', (cx - .3125, 11.75 - 1.125, zc - .3125), (cx + .3125, 11.75 + 1.125, zc + .3125),
                 'nerve', rotation=spin('x', 45, (cx, 11.75, zc))))
    e.append(bead('selector_drop', 0, BOUTON_R, 9.75, 11.25, .625, 'nerve'))
    e.append(bead('selector_terminal', 0, BOUTON_R, 9.125, 9.875, .875, 'terminal', extra=BRIGHT))
    return e


def lit_shell():
    return rounded('bouton_lit', 0, BOUTON_R, MEMBRANE_Y - .0625, MEMBRANE_Y + 1.3125, 1.375, 'bouton_lit',
                   skip=('down',), extra=BRIGHT)


def impulse():
    """Authored at the soma end of the centre dendrite; the renderer slides it out along -z."""
    return [bead('impulse', 0, IMPULSE_START, IMPULSE_Y - .375, IMPULSE_Y + .375, .75, 'impulse', extra=BRIGHT)]


# --- special faces --------------------------------------------------------------------------

def nerve(face, lx, ly, w, h, key):
    """Peripheral nerve: pale glistening fibre, bands of Fontana across it, capillaries running along it."""
    along_v = h >= w
    run, across = (ly, lx) if along_v else (lx, ly)
    n = noise('nerve', key, face, lx, ly)
    if noise('vasa', key, face, across) < .06 and noise('vasa_gap', key, face, across, run // 3) < .7:
        return clamp(CAPILLARY, int(n * 16) - 8) + (255,)
    band = 10 if run % 3 == 0 else (-6 if run % 3 == 2 else 0)
    fibre = 8 if noise('fibre', key, face, across) > .6 else -4
    rgb = clamp(NERVE, band + fibre + int(n * 8) - 4)
    if across in (0, (w if along_v else h) - 1) and max(w, h) > 2:
        rgb = clamp(NERVE_DARK, int(n * 8))
    return rgb + (255,)


def myelin(face, lx, ly, w, h, key):
    """Myelin: pearly wrapped lamellae with a wet sheen near the top and a seam where each wrap closes."""
    n = noise('myelin', key, face, lx, ly)
    if face in ('up', 'down'):
        return clamp(MYELIN, -10 + int(n * 8)) + (255,)
    if lx == (key[1] * 3) % max(w, 1):
        return clamp(MYELIN, -40) + (255,)
    lamella = -14 if (ly + lx // 4) % 4 == 3 else 0
    return clamp(MYELIN, lamella + (18 if ly == 0 else -14 if ly == h - 1 else 0) + int(n * 8) - 4) + (255,)


def ganglion(face, lx, ly, w, h, key):
    """The root plexus: matted flesh threaded with nerve and a dense capillary mesh."""
    n = noise('ganglion', key, face, lx, ly)
    if noise('mesh', key, face, (lx + ly) // 2, (lx - ly) // 3) < .09:
        return clamp(CAPILLARY, int(n * 20) - 10) + (255,)
    if noise('thread', key, face, lx // 2, ly) > .85:
        return clamp(NERVE, int(n * 10) - 5) + (255,)
    return clamp(FLESH, int(n * 18) - 12 - (10 if ly == h - 1 and face not in ('up', 'down') else 0)) + (255,)


def soma_tex(face, lx, ly, w, h, key):
    """The cell body: dense, wet tissue under a web of capillaries, a pale wet glint on top."""
    n = noise('soma', key, face, lx, ly)
    if face == 'up' and (lx, ly) == (1, 1):
        return (255, 252, 236, 255)
    if noise('web', key, face, lx // 2 + ly, ly // 2) < .08 and n < .5:
        return (128, 20, 32, 255)
    if noise('nissl', key, face, lx, ly) > .92:
        return (150, 118, 56, 255)   # Nissl bodies
    return clamp(SOMA, int(n * 16) - 8 + (14 if ly < 2 else 0) - (16 if ly >= h - 2 else 0)) + (255,)


def bouton(face, lx, ly, w, h, key):
    """A dormant synaptic bouton of Mnemonic Ambergris: grey-olive wax, nacre flecks, one faint memory vein."""
    n = noise('bouton', key, face, lx, ly)
    if noise('memory', key, face, lx + ly) < .1:
        return (122, 30, 38, 255)
    if n > .86:
        return NACRE + (255,)
    return clamp(WAX, int(n * 14) - 7 + (14 if ly == 0 else 0) - (12 if ly == h - 1 else 0)) + (255,)


def bouton_lit(face, lx, ly, w, h, key):
    """The selected pattern: a bouton lit yellow-white from inside, hottest at its centre."""
    d = max(abs(lx - (w - 1) / 2), abs(ly - (h - 1) / 2)) / max(w / 2, 1)
    n = noise('lit', key, face, lx, ly)
    if d < .35:
        return GLOW_CORE + (255,)
    return clamp(GLOW, int(n * 14) - 7 - int(d * 30)) + (255,)


def terminal(face, lx, ly, w, h, key):
    return (GLOW if face != 'up' else clamp(GLOW, -50)) + (255,)


def impulse_tex(face, lx, ly, w, h, key):
    corner = lx in (0, w - 1) and ly in (0, h - 1)
    return (GLOW if corner else GLOW_CORE) + (255,)


def lobe_radius(phi):
    """Membrane edge: full at each slot, scalloped in between, closing to the hub past the last lobe."""
    if abs(phi) > SLOTS[-1] + 11.25:
        return HUB_R
    offset = (phi - SLOTS[0] + 11.25) % 22.5 - 11.25
    return MEMBRANE_R - LOBE_DEPTH * (1 - math.cos(math.radians(offset) * 16)) / 2


def membrane_tex(face, lx, ly, w, h, key):
    """The synaptic web, painted in world space on both faces (vanilla up: v0 north; down: v0 south)."""
    cx, cz = HUB
    x = cx - MEMBRANE_R + (lx + .5) * 2 * MEMBRANE_R / w
    t = (ly + .5) / h
    z = cz - MEMBRANE_R + (t if face == 'up' else 1 - t) * (MEMBRANE_R + HUB_R)
    r = math.hypot(x - cx, z - cz)
    phi = math.degrees(math.atan2(-(x - cx), -(z - cz)))
    edge = max(lobe_radius(phi), HUB_R)
    if r > edge:
        return (0, 0, 0, 0)
    n = noise('membrane', face, lx, ly)
    if r > edge - .3:
        return clamp(NERVE, int(n * 10) - 8) + (255,)          # thickened, pale lobe margin
    if r < 1.75:
        return clamp(FLESH, -20 + int(n * 8)) + (255,)
    # Capillaries fan out along each lobe, forking once halfway out.
    nearest = min(SLOTS, key=lambda s: abs(phi - s))
    off = (phi - nearest) * math.pi / 180 * r
    if abs(off) < .16:
        return clamp(CAPILLARY, int(n * 14)) + (255,)
    if r > 3.25 and abs(abs(off) - (r - 3.25) * .45) < .14:
        return clamp(FLESH, -60 + int(n * 10)) + (255,)
    if abs(abs(phi - nearest) - 11.25) * math.pi / 180 * r < .2:
        return clamp(FLESH, -70) + (255,)                       # crease between lobes
    shade = -16 if face == 'down' else 0
    return clamp(FLESH, shade + int(n * 14) - 7 - int((edge - r) * 3)) + (255,)


SPECIAL = dict(nerve=nerve, myelin=myelin, ganglion=ganglion, soma=soma_tex, bouton=bouton,
               bouton_lit=bouton_lit, terminal=terminal, impulse=impulse_tex, membrane=membrane_tex)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--preview', action='store_true')
    args = parser.parse_args()
    groups = {'block': distributor(), 'selector': selector(), 'lit': lit_shell(), 'impulse': impulse()}
    for elements in groups.values():
        for el in elements:
            el['density'] = 1   # one texel per unit: the mod's block texel density
    moved = separate(groups['block'])
    atlas, models = build(groups, SPECIAL)
    atlas.save(ASSETS / f'textures/block/{ATLAS}.png')
    particle = f'hemomancy:block/{ATLAS}'

    def part(name):
        return model_json(models[name], ATLAS, atlas.height, particle)

    block = part('block')
    item = model_json(item_elements((models['block'], 0), (models['selector'], 0), (models['lit'], 0)), ATLAS,
                      atlas.height, particle)
    write({'block/dendritic_distributor': block,
           'block/dendritic_distributor_selector': part('selector'),
           'block/dendritic_distributor_bouton_lit': part('lit'),
           'block/dendritic_distributor_impulse': part('impulse'),
           'item/dendritic_distributor': item}, 'dendritic_distributor', item)
    print(f'Exported Dendritic Distributor: {len(groups["block"])} cubes (+{len(groups["selector"])} selector), '
          f'{moved} faces separated, 128x{atlas.height} atlas')
    if args.preview:
        preview(item['elements'], ATLAS, 'Dendritic Distributor - block + selector at the centre slot',
                'dendritic_distributor')


if __name__ == '__main__':
    main()
