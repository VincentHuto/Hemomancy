"""Rebuild the Crimson Troupe faculty: Java models, painted variant atlases and portable BBModels.

Strongman, Beast Tamer, Threadkeeper and Retired Understudy share CircusFacultyEntity and the
CircusFacultyModel base. Each figure keeps the vanilla humanoid joints (head, body, arms, legs) so
walking, swings and held items still work; costume props are child parts posed from the act state.

puppet_rig.geometry() packs the box UVs and writes createBodyLayer(); this script writes the
Blockbench file itself because the circus NPC files use Blockbench's mirrored X axis (see the
Fire Eater), then paints both variants from the same material map.
"""
from pathlib import Path
import base64
import json
import math
import uuid
from PIL import Image
from puppet_rig import ROOT, ATLAS, geometry, noise, front_edge, back_edge

ASSETS = ROOT / 'src/main/resources/assets/hemomancy'
MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
TEXTURES = ASSETS / 'textures/entity/npc/harbinger/circus'
BBMODELS = ASSETS / 'models/entity/bbmodel/npc/harbinger/circus'
TOP = 'mesh.getRoot()'

SHARED = {
    'ink': '#141017', 'c0': '#1d1922', 'c1': '#2a2531', 'c2': '#3a3342', 'c3': '#4d4556',
    'r0': '#4a1424', 'r1': '#6e1a2e', 'r2': '#962637', 'r3': '#c03a47',
    'i0': '#a89a7e', 'i1': '#d2c4a2', 'i2': '#ece2c6',
    'g0': '#6e5020', 'g1': '#a8802e', 'g2': '#dcb24e',
    's0': '#8a604a', 's1': '#b58468', 's2': '#d4a283', 's3': '#e6bc9c',
    'w': '#ece6da', 'p': '#1e1820',
    'l0': '#3a2418', 'l1': '#5a3a26', 'l2': '#7c5536',
    'fe0': '#2b2a30', 'fe1': '#4a4950', 'fe2': '#75747c',
}


def joint(parts, name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
    parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))


def box(origin, size, material):
    return dict(origin=origin, size=size, material=material)


def tone(side, py, h, light, mid, dark, r=0.5):
    """Three-tone cloth lit from above: lit tops, shadowed hems and a little weave noise."""
    if side == 'up':
        return light
    if side == 'down':
        return dark
    if py == 0 and h >= 3:
        return light
    if py == h - 1 and h >= 4:
        return dark
    if r < .07:
        return dark
    if r > .95:
        return light
    return mid


def from_front(side, px, w):
    """Columns from the front edge on a side strip (east strips meet the front at their last column)."""
    return w - 1 - px if side == 'east' else px


def head_shader(face, keys, hair, bald=False, ear=True):
    """Front from a pixel grid; sides, back and crown from the hair or scalp keys."""
    h_light, h_mid, h_dark = hair

    def shade(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 7)
        if side == 'north':
            return keys[face[py][px]]
        if side == 'down':
            return 's0'
        if bald:
            if side == 'up':
                return 's3' if (px + py) % 5 == 0 and r > .4 else 's2'
            if ear and side in ('east', 'west') and py in (3, 4) and from_front(side, px, w) in (3, 4):
                return 's0'
            return 's1' if py < h - 1 else 's0'
        if side == 'up':
            return h_light if r > .8 else h_mid
        if side == 'south':
            return h_dark if py == h - 1 or r < .15 else h_mid
        dist = from_front(side, px, w)
        if py < 2 or dist >= w - 3:
            return h_dark if r < .2 else h_mid
        if ear and py in (3, 4) and dist in (3, 4):
            return 's0'
        return 's1'
    return shade


def limb_shader(light, mid, dark, cuff=None, hand=0, hand_keys=('s2', 's1', 's0'), stripe=None):
    """Sleeves and trouser legs: optional cuff row band and skin hands on the lowest rows."""
    def shade(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 11)
        if hand and (py >= h - hand or side == 'down') and side != 'up':
            hl, hm, hd = hand_keys
            return hd if side == 'down' else (hl if side in ('north', 'west') else hm)
        if cuff and py in cuff[0] and side != 'up':
            return cuff[1] if side in ('north', 'west') else cuff[2]
        if stripe and side in ('north', 'south', 'east', 'west') and px % 3 == 1 and py < h - 1:
            return stripe
        return tone(side, py, h, light, mid, dark, r)
    return shade


def plain(light, mid, dark, salt=3):
    def shade(side, px, py, w, h, gx, gy):
        return tone(side, py, h, light, mid, dark, noise(gx, gy, salt))
    return shade


def metal(light, mid, dark):
    def shade(side, px, py, w, h, gx, gy):
        if side == 'up' or py == 0:
            return light
        if side == 'down' or (py == h - 1 and h > 2):
            return dark
        return mid if side in ('north', 'west') else dark
    return shade


# --------------------------------------------------------------------------------------------
# Threadkeeper: a lean ticket clerk turned accountant. Eyeshade, spectacles, sleeve garters,
# a ledger in hand and the Crossbar thread spindle at the belt.

THREADKEEPER_FACE = [
    'hhhhhhhh',
    'hssssssh',
    'sbbssbbs',
    'gwpggpwg',
    'sggssggs',
    'sssnnsss',
    'ssmmmmss',
    'kssssssk',
]


def threadkeeper():
    parts = []
    j = lambda *a, **k: joint(parts, *a, **k)
    j('head', TOP, (0, 0, 0), [box((-4, -8, -4), (8, 8, 8), 'head')])
    j('eyeshade', 'head', (0, 0, 0), [box((-4.5, -8, -4.5), (9, 2, 9), 'visor_band')])
    j('visor', 'eyeshade', (0, -6.5, -4.5), [box((-4, -.5, -3), (8, .5, 3), 'visor')], (14, 0, 0))
    j('hat', TOP, (0, 0, 0))
    j('body', TOP, (0, 0, 0), [
        box((-4, 0, -2), (8, 12, 4), 'frock'),
        box((-3, 1, -2.5), (6, 6, .5), 'waistcoat')])
    j('collar', 'body', (0, 0, 0), [box((-3, -1.5, -2.5), (6, 2, 5), 'collar')])
    j('belt', 'body', (0, 0, 0), [box((-4.5, 7, -2.5), (9, 1, 5), 'belt')])
    # The Crossbar spindle hangs at the right of the belt, flanged like a ticket-roll.
    j('spindle', 'body', (-2.5, 9.5, -3), [
        box((-.5, -1.5, -.5), (1, 3, 1), 'thread'),
        box((-1, -2, -1), (2, .5, 2), 'brass'),
        box((-1, 1.5, -1), (2, .5, 2), 'brass')])
    for side, sign in (('right', -1), ('left', 1)):
        j(side + '_coat_tail', 'body', (sign * 2, 11, 2.5), [
            box((-1.5, 0, -.5), (3, 6, 1), 'frock')], (6, 0, -sign * 3))
    j('right_arm', TOP, (-5, 2, 0), [box((-2, -2, -1.5), (3, 12, 3), 'sleeve')])
    j('right_garter', 'right_arm', (0, 0, 0), [box((-2.5, 2.5, -2), (4, 1, 4), 'garter')])
    j('left_arm', TOP, (5, 2, 0), [box((-1, -2, -1.5), (3, 12, 3), 'sleeve')])
    j('left_garter', 'left_arm', (0, 0, 0), [box((-1.5, 2.5, -2), (4, 1, 4), 'garter')])
    j('ledger', 'left_arm', (0, 9, 0), [box((2, -2, -2.5), (1.5, 4, 5), 'ledger')])
    j('right_leg', TOP, (-1.9, 12, 0), [
        box((-1.5, 0, -1.5), (3, 10, 3), 'trousers'),
        box((-1.5, 10, -2.5), (3, 2, 4), 'shoe')])
    j('left_leg', TOP, (1.9, 12, 0), [
        box((-1.5, 0, -1.5), (3, 10, 3), 'trousers'),
        box((-1.5, 10, -2.5), (3, 2, 4), 'shoe')])
    return parts


def threadkeeper_materials():
    keys = {'h': 'h1', 's': 's2', 'b': 'h0', 'g': 'g1', 'w': 'w', 'p': 'p', 'n': 's1', 'm': 's0', 'k': 's1'}

    def frock(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 5)
        if side == 'north' and w == 8 and px in (1, 6) and py < 7:
            return 'g1'  # gold lapel piping
        if side == 'north' and w == 8 and 2 <= px <= 5 and py == 0:
            return 'i2'
        return tone(side, py, h, 'c3', 'c2', 'c1', r)

    def waistcoat(side, px, py, w, h, gx, gy):
        if side == 'north':
            if px == 2 and py % 2 == 1:
                return 'g2'
            if (px, py) in ((3, 3), (4, 3), (5, 4)):
                return 'g1'  # watch chain
            return 'r1' if py < h - 1 else 'r0'
        return 'r0'

    def thread(side, px, py, w, h, gx, gy):
        return 't1' if (py + px) % 2 == 0 else 't0'

    def visor(side, px, py, w, h, gx, gy):
        if side == 'up':
            return 'v1' if px % 3 else 'v2'
        if side == 'down':
            return 'v0'
        return 'v2'

    def ledger(side, px, py, w, h, gx, gy):
        if side in ('east', 'west'):
            if px in (0, w - 1) and py in (0, h - 1):
                return 'g2'
            return 'r1' if py > 0 else 'r2'
        return 'i1' if side in ('north', 'up') else 'i0'

    def garter(side, px, py, w, h, gx, gy):
        return 'g2' if side == 'up' else ('r2' if px % 2 == 0 else 'r1')

    return {
        'head': head_shader(THREADKEEPER_FACE, keys, ('h1', 'h1', 'h0')),
        'visor_band': lambda side, px, py, w, h, gx, gy: None if side in ('up', 'down') else ('c2' if py == 0 else 'c1'),
        'visor': visor,
        'frock': frock,
        'waistcoat': waistcoat,
        'collar': plain('i2', 'i1', 'i0'),
        'belt': lambda side, px, py, w, h, gx, gy: 'g2' if side == 'north' and px in (4, 5) else 'l0',
        'thread': thread,
        'brass': metal('g2', 'g1', 'g0'),
        'sleeve': limb_shader('c3', 'c2', 'c1', cuff=((9,), 'i2', 'i1'), hand=2),
        'garter': garter,
        'ledger': ledger,
        'trousers': limb_shader('c2', 'c1', 'c0', stripe='c3'),
        'shoe': metal('c3', 'ink', 'ink'),
    }


THREADKEEPER_JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusThreadkeeperModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_threadkeeper"), "main");
	private final ModelPart spindle;

	public CircusThreadkeeperModel(ModelPart root) {
		super(root, 1.5F);
		spindle = body.getChild("spindle");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		// The spindle pays out slowly at rest and runs while a Crossbar is under inspection.
		spindle.yRot = ageInTicks * (onStage(state) ? 0.45F : 0.06F);
		if (onStage(state)) {
			head.xRot = 0.5F;
			leftArm.xRot = -1.05F;
			leftArm.yRot = 0.35F;
			leftArm.zRot = 0.0F;
			rightArm.xRot = -0.85F + Mth.sin(ageInTicks * 0.3F) * 0.12F;
			rightArm.yRot = -0.3F;
			rightArm.zRot = 0.0F;
		}
	}
}
'''

# --------------------------------------------------------------------------------------------
# Retired Understudy: stooped, faded harlequin diamonds, a knitted shawl, a cane, and the
# half-mask they wore for other people's roles pushed up onto the brow until the act.

UNDERSTUDY_FACE = [
    'HHHHHHHH',
    'HHssssHH',
    'HssssssH',
    'sbbssbbs',
    'swpsspws',
    'slssssls',
    'sssnnsss',
    'kslmmlsk',
]
HALF_MASK = [
    'giiiiig',
    'iEiiiEi',
    'gtiiiig',
]


def understudy():
    parts = []
    j = lambda *a, **k: joint(parts, *a, **k)
    j('head', TOP, (0, 0, 0), [box((-4, -8, -4), (8, 8, 8), 'head')])
    j('half_mask', 'head', (0, -4.5, -4), [box((-3.5, -2, -1), (7, 3, 1), 'half_mask')])
    j('hat', TOP, (0, 0, 0))
    j('body', TOP, (0, 0, 0), [box((-4, 0, -2), (8, 12, 4), 'costume')])
    j('shawl', 'body', (0, 0, 0), [
        box((-4.5, -.5, -2.5), (9, 3, 5), 'shawl'),
        box((-4, 2.5, 2), (8, 4, .5), 'shawl_fringe')])
    for side, sign in (('right', -1), ('left', 1)):
        j(side + '_shawl_tail', 'body', (sign * 2.5, 2.5, -2.5), [
            box((-1, 0, -.5), (2, 7, .5), 'shawl_fringe')], (-4, 0, 0))
    j('right_arm', TOP, (-5, 2, 0), [box((-2, -2, -1.5), (3, 12, 3), 'sleeve')])
    j('left_arm', TOP, (5, 2, 0), [box((-1, -2, -1.5), (3, 12, 3), 'sleeve')])
    # The cane is planted from the left hand; its crook tucks back into the fist.
    j('cane', 'left_arm', (.5, 9.5, -2), [
        box((-.5, 0, -.5), (1, 12.5, 1), 'cane'),
        box((-.5, -1, -.5), (1, 1, 2.5), 'cane_crook')])
    j('right_leg', TOP, (-1.9, 12, 0), [
        box((-1.5, 0, -1.5), (3, 10, 3), 'tights'),
        box((-1.5, 10, -2.5), (3, 2, 4), 'slipper')])
    j('left_leg', TOP, (1.9, 12, 0), [
        box((-1.5, 0, -1.5), (3, 10, 3), 'tights'),
        box((-1.5, 10, -2.5), (3, 2, 4), 'slipper')])
    return parts


def understudy_materials():
    keys = {'H': 'h1', 's': 's2', 'b': 'h0', 'w': 'w', 'p': 'p', 'l': 's1', 'n': 's1', 'm': 's0', 'k': 's1'}
    mask_keys = {'g': 'g1', 'i': 'm1', 'E': 'ink', 't': 'r2'}

    def diamonds(side, px, py, w, h, gx, gy):
        # Faded harlequin lattice; each face keeps its own diamonds so seams stay tidy.
        if side in ('up', 'down'):
            return 'e0'
        u, v = (px + .5) / 4, (py + .5) / 6
        lit = (math.floor(u + v) + math.floor(u - v)) % 2 == 0
        r = noise(gx, gy, 13)
        if r < .05:
            return 'k1'  # worn thin
        if lit:
            return 'd1' if py < h - 1 else 'd0'
        return 'e1' if py < h - 1 else 'e0'

    def sleeve(side, px, py, w, h, gx, gy):
        if py >= h - 2 or side == 'down':
            return 's0' if side == 'down' else ('s2' if side in ('north', 'west') else 's1')
        if py == h - 3:
            return 'm0'  # ruffled cuff
        return diamonds(side, px, py, w, h, gx, gy)

    def shawl(side, px, py, w, h, gx, gy):
        if side == 'up':
            return 'k2' if (px + py) % 2 else 'k1'
        return 'k1' if (px + py) % 2 else 'k0'

    def fringe(side, px, py, w, h, gx, gy):
        if py >= h - 1:
            return 'f' if px % 2 == 0 else 'k0'
        return shawl(side, px, py, w, h, gx, gy)

    def half_mask(side, px, py, w, h, gx, gy):
        if side == 'north':
            return mask_keys[HALF_MASK[py][px]]
        return 'g1' if side == 'up' else 'm0'

    return {
        'head': head_shader(UNDERSTUDY_FACE, keys, ('h1', 'h1', 'h0')),
        'half_mask': half_mask,
        'costume': diamonds,
        'shawl': shawl,
        'shawl_fringe': fringe,
        'sleeve': sleeve,
        'cane': lambda side, px, py, w, h, gx, gy: 'g1' if py == h - 1 and h > 4 else ('l2' if side in ('north', 'west', 'up') else 'l1'),
        'cane_crook': plain('l2', 'l1', 'l0'),
        'tights': limb_shader('e1', 'e0', 'c3'),
        'slipper': plain('d1', 'd0', 'r0'),
    }


UNDERSTUDY_JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusUnderstudyModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_understudy"), "main");
	private final ModelPart halfMask;
	private final ModelPart rightShawlTail;
	private final ModelPart leftShawlTail;

	public CircusUnderstudyModel(ModelPart root) {
		super(root, 1.5F);
		halfMask = head.getChild("half_mask");
		rightShawlTail = body.getChild("right_shawl_tail");
		leftShawlTail = body.getChild("left_shawl_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		// An old stoop: shoulders forward, the cane hand barely swinging.
		body.xRot = 0.12F;
		head.z = -0.8F;
		rightArm.z -= 0.6F;
		leftArm.z -= 0.6F;
		leftArm.xRot *= 0.3F;
		float sway = Mth.sin(ageInTicks * 0.07F) * 0.04F;
		rightShawlTail.xRot = -0.07F - limbSwingAmount * 0.3F + sway;
		leftShawlTail.xRot = -0.07F - limbSwingAmount * 0.3F - sway;
		halfMask.resetPose();
		if (state == ActState.PERFORM) {
			// The mask comes down: for the length of the act they are someone else.
			rightArm.xRot = -2.1F;
			rightArm.yRot = -0.35F;
			head.xRot = 0.1F;
			return;
		}
		halfMask.y -= 3.5F;
		halfMask.xRot = -0.35F;
		if (state == ActState.SETUP) {
			rightArm.xRot = -1.7F;
			rightArm.yRot = -0.3F;
		}
	}
}
'''

# --------------------------------------------------------------------------------------------
# Strongman: broad shoulders, a small bald head and handlebar moustache, a striped one-strap
# leotard, wristbands and a barbell that only appears for the act.

STRONGMAN_FACE = [
    'SSSSSSS',
    'sssssss',
    'sssssss',
    'sbbsbbs',
    'swpspws',
    'ssknkss',
    'ksssssk',
]


def strongman():
    parts = []
    j = lambda *a, **k: joint(parts, *a, **k)
    j('head', TOP, (0, 0, 0), [box((-3.5, -7, -3.5), (7, 7, 7), 'head')])
    j('moustache', 'head', (0, 0, 0), [
        box((-2.5, -2, -4), (5, 1, .5), 'moustache'),
        box((-3.5, -2.5, -4), (1, 1, .5), 'moustache'),
        box((2.5, -2.5, -4), (1, 1, .5), 'moustache')])
    j('hat', TOP, (0, 0, 0))
    j('body', TOP, (0, 0, 0), [
        box((-5, 0, -2.5), (10, 8, 5), 'chest'),
        box((-4, 8, -2), (8, 4, 4), 'trunks')])
    j('belt', 'body', (0, 0, 0), [
        box((-4.5, 7.5, -2.5), (9, 2, 5), 'belt'),
        box((-1.5, 7.5, -3), (3, 2, .5), 'brass')])
    # Held at chest height in the definition; the model re-seats it in the hands every frame.
    j('barbell', 'body', (0, 4, -10), [
        box((-14, -.5, -.5), (28, 1, 1), 'bar'),
        box((-13.5, -2.5, -2.5), (2, 5, 5), 'plate'),
        box((11.5, -2.5, -2.5), (2, 5, 5), 'plate')])
    for side, sign in (('right', -1), ('left', 1)):
        x0 = -3.5 if sign < 0 else -1.5
        j(side + '_arm', TOP, (sign * 6.5, 2, 0), [
            box((x0, -2.5, -2.5), (5, 6, 5), 'bicep'),
            box((x0 + .5, 3.5, -2), (4, 6, 4), 'forearm')])
        j(side + '_wristband', side + '_arm', (0, 0, 0), [
            box((x0, 6.5, -2.5), (5, 2, 5), 'wristband')])
        j(side + '_leg', TOP, (sign * 2.5, 12, 0), [
            box((-2.5, 0, -2.5), (5, 7, 5), 'thigh'),
            box((-2, 7, -2), (4, 3, 4), 'tights'),
            box((-2.5, 10, -3), (5, 2, 5.5), 'boot')])
    return parts


def strongman_materials():
    keys = {'S': 's3', 's': 's2', 'b': 'h0', 'w': 'w', 'p': 'p', 'n': 's1', 'k': 's1'}

    def stripes(py):
        return 'st0' if py % 3 else 'st1'

    def chest(side, px, py, w, h, gx, gy):
        # One strap over the left shoulder; skin above the leotard on the right.
        if side == 'up':
            return 'st0' if px >= w - 3 else 's3'
        if side == 'down':
            return 'st0'
        strap_col = (side == 'north' and px >= w - 3) or (side == 'south' and px <= 2) or side == 'east'
        if py >= 3 or (strap_col and side in ('north', 'south')):
            return stripes(py)
        if side == 'north' and py == 2 and px in (2, 6):
            return 's1'  # chest shading
        return 's2' if side in ('north', 'west') else 's1'

    def skin(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 17)
        if side == 'up':
            return 's3'
        if side == 'down':
            return 's0'
        if r < .08:
            return 's1'
        return 's2' if side in ('north', 'west') or py < 2 else 's1'

    def wristband(side, px, py, w, h, gx, gy):
        if side in ('up', 'down'):
            return 'l0'
        return 'g2' if px % 2 == 0 and py == 0 else 'l1'

    def plate(side, px, py, w, h, gx, gy):
        if side in ('east', 'west'):
            ring = px in (0, w - 1) or py in (0, h - 1)
            return 'r2' if ring else ('fe1' if (px, py) == (2, 2) else 'fe0')
        return 'r1'

    return {
        'head': head_shader(STRONGMAN_FACE, keys, ('s3', 's2', 's1'), bald=True),
        'moustache': plain('h1', 'h0', 'h0'),
        'chest': chest,
        'trunks': lambda side, px, py, w, h, gx, gy: stripes(py) if side not in ('up', 'down') else 'st0',
        'belt': lambda side, px, py, w, h, gx, gy: 'l1' if side in ('north', 'west') and py == 0 else 'l0',
        'brass': metal('g2', 'g1', 'g0'),
        'bar': metal('fe2', 'fe1', 'fe0'),
        'plate': plate,
        'bicep': skin,
        'forearm': skin,
        'wristband': wristband,
        'thigh': limb_shader('c3', 'c2', 'c1'),
        'tights': plain('c2', 'c1', 'c0'),
        'boot': lambda side, px, py, w, h, gx, gy: ('i1' if side == 'north' and px % 2 == 0 and py == 0 else
                                                    tone(side, py, h, 'l2', 'l1', 'l0')),
    }


STRONGMAN_JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusStrongmanModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_strongman"), "main");
	/** Shoulder pivot to the centre of the fist. */
	private static final float GRIP = 10.0F;
	private final ModelPart barbell;

	public CircusStrongmanModel(ModelPart root) {
		super(root, 2.5F);
		barbell = body.getChild("barbell");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		barbell.visible = onStage(state);
		if (!onStage(state)) {
			// A wide, heavy stance between acts.
			rightArm.zRot += 0.12F;
			leftArm.zRot -= 0.12F;
			return;
		}
		float lift = state == ActState.PERFORM ? -2.95F + Mth.sin(ageInTicks * 0.2F) * 0.12F : -1.35F;
		rightArm.xRot = leftArm.xRot = lift;
		rightArm.yRot = leftArm.yRot = 0.0F;
		rightArm.zRot = leftArm.zRot = 0.0F;
		head.xRot = state == ActState.PERFORM ? -0.3F : 0.15F;
		barbell.y = rightArm.y + Mth.cos(lift) * GRIP;
		barbell.z = rightArm.z + Mth.sin(lift) * GRIP;
	}
}
'''

# --------------------------------------------------------------------------------------------
# Beast Tamer: a crimson frogged coat with long tails, gold epaulettes, a fur mantle, tall boots
# and a whip coiled at the belt until the act, when the lash comes out of the hand.

BEAST_TAMER_FACE = [
    'hhhhhhhh',
    'hhhhhhhh',
    'hssssssh',
    'sbbssbrs',
    'swpsspws',
    'hssnnsrh',
    'hhsmmshh',
    'hhsssshh',
]
LASH = 3


def beast_tamer():
    parts = []
    j = lambda *a, **k: joint(parts, *a, **k)
    j('head', TOP, (0, 0, 0), [box((-4, -8, -4), (8, 8, 8), 'head')])
    j('hat', TOP, (0, 0, 0))
    j('body', TOP, (0, 0, 0), [box((-4, 0, -2), (8, 12, 4), 'coat')])
    j('mantle', 'body', (0, 0, 0), [
        box((-5, -.5, -3), (10, 3, 6), 'fur'),
        box((-3, 2.5, 2), (6, 2, 1.5), 'fur')])
    j('belt', 'body', (0, 0, 0), [box((-4.5, 7, -2.5), (9, 1.5, 5), 'belt')])
    j('whip_coil', 'body', (2.5, 9, -2.5), [box((-1.5, -1.5, -.5), (3, 3, 1), 'coil')])
    for side, sign in (('right', -1), ('left', 1)):
        j(side + '_coat_tail', 'body', (sign * 2, 11, 2.5), [
            box((-2, 0, -.5), (4, 8, 1), 'coat_tail')], (8, 0, -sign * 4))
    j('right_arm', TOP, (-5, 2, 0), [box((-3, -2, -2), (4, 12, 4), 'sleeve')])
    j('right_epaulette', 'right_arm', (0, 0, 0), [box((-3.5, -2.5, -2.5), (5, 1.5, 5), 'epaulette')])
    j('whip_handle', 'right_arm', (-1, 10, 0), [box((-.5, -1, -.5), (1, 3, 1), 'whip_handle')])
    parent = 'whip_handle'
    for i in range(LASH):
        j('whip_lash_' + str(i), parent, (0, 2 if i == 0 else 3, 0), [
            box((-.25, 0, -.25), (.5, 3, .5), 'lash')])
        parent = 'whip_lash_' + str(i)
    j('left_arm', TOP, (5, 2, 0), [box((-1, -2, -2), (4, 12, 4), 'sleeve')])
    j('left_epaulette', 'left_arm', (0, 0, 0), [box((-1.5, -2.5, -2.5), (5, 1.5, 5), 'epaulette')])
    for side, sign in (('right', -1), ('left', 1)):
        j(side + '_leg', TOP, (sign * 1.9, 12, 0), [
            box((-2, 0, -2), (4, 6, 4), 'breeches'),
            box((-2, 6, -2.5), (4, 6, 4.5), 'boot')])
    return parts


def beast_tamer_materials():
    keys = {'h': 'h1', 's': 's2', 'b': 'h0', 'r': 's0', 'w': 'w', 'p': 'p', 'n': 's1', 'm': 's0'}

    def coat(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 19)
        if side == 'north':
            if px in (3, 4) and py < h - 1:
                return 'g2' if py % 2 == 0 else 'r0'  # buttoned placket
            if py in (2, 4, 6) and 1 <= px <= 6:
                return 'g1'  # frogging
        return tone(side, py, h, 'r3', 'r2', 'r1', r)

    def coat_tail(side, px, py, w, h, gx, gy):
        if py == h - 1 and side != 'up':
            return 'g1'
        return tone(side, py, h, 'r2', 'r1', 'r0', noise(gx, gy, 21))

    def fur(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 23)
        if side == 'down':
            return 'u0'
        if side == 'up':
            return 'u3' if r > .7 else 'u2'
        if py == h - 1:
            return 'u0' if px % 2 else 'u1'  # ragged hem
        return 'u3' if r > .85 else ('u1' if r < .3 else 'u2')

    def epaulette(side, px, py, w, h, gx, gy):
        if side == 'up':
            return 'g2' if (px + py) % 3 else 'g1'
        if side == 'down':
            return 'g0'
        return 'g1' if px % 2 == 0 else 'g0'  # fringe

    def coil(side, px, py, w, h, gx, gy):
        if side in ('north', 'south'):
            return 'l0' if (px, py) == (1, 1) else ('l2' if (px + py) % 2 == 0 else 'l1')
        return 'l1'

    return {
        'head': head_shader(BEAST_TAMER_FACE, keys, ('h2', 'h1', 'h0')),
        'coat': coat,
        'coat_tail': coat_tail,
        'fur': fur,
        'belt': lambda side, px, py, w, h, gx, gy: 'g2' if side == 'north' and px in (4, 5) else 'ink',
        'coil': coil,
        'sleeve': limb_shader('r3', 'r2', 'r1', cuff=((7, 8), 'g2', 'g1'), hand=3, hand_keys=('l2', 'l1', 'l0')),
        'epaulette': epaulette,
        'whip_handle': plain('l2', 'l1', 'l0'),
        'lash': plain('l1', 'l0', 'l0'),
        'breeches': lambda side, px, py, w, h, gx, gy: ('g1' if side in ('east', 'west') and px == w // 2 else
                                                        tone(side, py, h, 'i2', 'i1', 'i0', noise(gx, gy, 29))),
        'boot': lambda side, px, py, w, h, gx, gy: ('c3' if side == 'north' and py == 0 else
                                                    tone(side, py, h, 'c2', 'c0', 'ink')),
    }


BEAST_TAMER_JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusBeastTamerModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_beast_tamer"), "main");
	private static final int LASH_SEGMENTS = %LASH%;
	private final ModelPart whipCoil;
	private final ModelPart whipHandle;
	private final ModelPart[] lash = new ModelPart[LASH_SEGMENTS];
	private final ModelPart rightCoatTail;
	private final ModelPart leftCoatTail;

	public CircusBeastTamerModel(ModelPart root) {
		super(root, 2.0F);
		whipCoil = body.getChild("whip_coil");
		whipHandle = rightArm.getChild("whip_handle");
		ModelPart segment = whipHandle;
		for (int i = 0; i < LASH_SEGMENTS; i++) {
			segment = segment.getChild("whip_lash_" + i);
			lash[i] = segment;
		}
		rightCoatTail = body.getChild("right_coat_tail");
		leftCoatTail = body.getChild("left_coat_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		float flare = limbSwingAmount * 0.45F + Mth.sin(ageInTicks * 0.08F) * 0.03F;
		rightCoatTail.xRot = rightCoatTail.getInitialPose().xRot + flare;
		leftCoatTail.xRot = leftCoatTail.getInitialPose().xRot + flare;
		boolean act = onStage(state);
		whipCoil.visible = !act;
		whipHandle.visible = act;
		if (!act) return;
		boolean crack = state == ActState.PERFORM;
		rightArm.xRot = crack ? -2.6F + Mth.sin(ageInTicks * 0.35F) * 0.25F : -1.9F;
		rightArm.yRot = 0.0F;
		rightArm.zRot = 0.0F;
		// The free hand stays open toward the beast.
		leftArm.xRot = -0.5F;
		leftArm.zRot = -0.25F;
		head.xRot = -0.1F;
		for (int i = 0; i < LASH_SEGMENTS; i++) {
			lash[i].xRot = crack ? 0.35F + Mth.sin(ageInTicks * 0.35F - i * 0.9F) * 0.55F : 0.25F;
		}
	}
}
'''.replace('%LASH%', str(LASH))

# --------------------------------------------------------------------------------------------

FACULTY = [
    dict(id='threadkeeper', model='CircusThreadkeeperModel', parts=threadkeeper, materials=threadkeeper_materials,
         java=THREADKEEPER_JAVA,
         palette={'h0': '#2a2020', 'h1': '#4a3a34', 'v0': '#1c3127', 'v1': '#2b4c3b', 'v2': '#467559',
                  't0': '#a8802e', 't1': '#dcb24e'},
         variant={'v0': '#3a1020', 'v1': '#5e1a2b', 'v2': '#8a2838', 't0': '#6e1a2e', 't1': '#c03a47'}),
    dict(id='understudy', model='CircusUnderstudyModel', parts=understudy, materials=understudy_materials,
         java=UNDERSTUDY_JAVA,
         palette={'h0': '#5d5a61', 'h1': '#a6a3a8', 'd0': '#7a3a44', 'd1': '#9a4c56', 'e0': '#454a5a',
                  'e1': '#5c6376', 'k0': '#6d675e', 'k1': '#8b8478', 'k2': '#a39c8f', 'm0': '#bfb398',
                  'm1': '#e9e1cc', 'f': '#962637',
                  's0': '#8f6d5c', 's1': '#b4917c', 's2': '#ceac95', 's3': '#e0c3ad'},
         variant={'m0': '#2e2a33', 'm1': '#46414d', 'f': '#a8802e'}),
    dict(id='strongman', model='CircusStrongmanModel', parts=strongman, materials=strongman_materials,
         java=STRONGMAN_JAVA,
         palette={'h0': '#1e1414', 'h1': '#3a261e', 'st0': '#962637', 'st1': '#e2d6b6',
                  's0': '#7c523c', 's1': '#a46e50', 's2': '#c48b68', 's3': '#d9a582'},
         variant={'st1': '#dcb24e', 'h0': '#5a2a16', 'h1': '#7c4022'}),
    dict(id='beast_tamer', model='CircusBeastTamerModel', parts=beast_tamer, materials=beast_tamer_materials,
         java=BEAST_TAMER_JAVA,
         palette={'h0': '#2e170f', 'h1': '#5e3020', 'h2': '#7a4430',
                  'u0': '#4a3d33', 'u1': '#6e5c4a', 'u2': '#8f7a60', 'u3': '#b39c7c'},
         variant={'u0': '#38383e', 'u1': '#58585f', 'u2': '#7c7c84', 'u3': '#a6a6ac'}),
]


def stable_uuid(*names):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, 'hemomancy:' + ':'.join(names)))


def faces(u, v, w, h, d):
    r = lambda values: [round(n, 4) for n in values]
    return {
        'north': {'uv': r([u + d, v + d, u + d + w, v + d + h]), 'texture': 0},
        'east': {'uv': r([u, v + d, u + d, v + d + h]), 'texture': 0},
        'south': {'uv': r([u + d + w + d, v + d, u + d + w + d + w, v + d + h]), 'texture': 0},
        'west': {'uv': r([u + d + w, v + d, u + d + w + d, v + d + h]), 'texture': 0},
        'up': {'uv': r([u + d + w, v + d, u + d, v]), 'texture': 0},
        'down': {'uv': r([u + d + w + w, v, u + d + w, v + d]), 'texture': 0},
    }


def blockbench(name, parts, uv_origins, textures, atlas=ATLAS):
    """Fire Eater conventions: Blockbench X is mirrored, Y is measured up from the feet."""
    by_name = {part['name']: part for part in parts}

    def world(part):
        x, y, z = part['pivot']
        parent = by_name.get(part['parent'])
        if parent:
            px, py, pz = world(parent)
            return x + px, y + py, z + pz
        return x, y, z

    r = lambda n: round(n, 4) + 0.0
    elements, groups = [], {}
    cube_index = 0
    for index, part in enumerate(parts):
        wx, wy, wz = world(part)
        origin = [r(-wx), r(24 - wy), r(wz)]
        children = []
        for cube in part['cubes']:
            (ox, oy, oz), (sx, sy, sz) = cube['origin'], cube['size']
            u, v = uv_origins[cube_index]
            cube_index += 1
            element_uuid = stable_uuid(name, part['name'], str(cube_index))
            elements.append({
                'name': f'{part["name"]}_{u}_{v}_{element_uuid[:8]}', 'rescale': False, 'locked': False,
                'from': [r(-(wx + ox + sx)), r(24 - (wy + oy + sy)), r(wz + oz)],
                'to': [r(-(wx + ox)), r(24 - (wy + oy)), r(wz + oz + sz)],
                'autouv': 0, 'color': index % 8, 'origin': origin, 'uv_offset': [u, v],
                'faces': faces(u, v, sx, sy, sz), 'type': 'cube', 'uuid': element_uuid})
            children.append(element_uuid)
        group = {'name': part['name'], 'origin': origin, 'color': index % 8, 'uuid': stable_uuid(name, part['name']),
                 'export': True, 'mirror_uv': False, 'isOpen': True, 'locked': False, 'visibility': True,
                 'autouv': 0, 'children': children}
        rx, ry, rz = part['rotation']
        if any(part['rotation']):
            group['rotation'] = [r(-rx), r(-ry), r(rz)]
        groups[part['name']] = group
    outliner = []
    for part in parts:
        parent = groups.get(part['parent'])
        (parent['children'] if parent else outliner).append(groups[part['name']])
    xs = [n for e in elements for n in (e['from'][0], e['to'][0])]
    ys = [n for e in elements for n in (e['from'][1], e['to'][1])]
    return {
        'meta': {'format_version': '4.0', 'model_format': 'modded_entity', 'box_uv': True},
        'name': name, 'model_identifier': name, 'modded_entity_version': '1.17', 'modded_entity_flip_y': True,
        'visible_box': [r((max(xs) - min(xs)) / 16), r((max(ys) - min(ys)) / 16), 0],
        'variable_placeholders': '', 'variable_placeholder_buttons': [], 'timeline_setups': [],
        'resolution': {'width': atlas, 'height': atlas},
        'elements': elements, 'outliner': outliner, 'textures': textures,
    }


def texture_entry(file, model, atlas=ATLAS, relative_dir='../../../../../../textures/entity/npc/harbinger/circus/'):
    return {
        'name': file.name, 'relative_path': relative_dir + file.name,
        'folder': '', 'namespace': '', 'id': str(model['index']), 'group': '', 'scope': 0,
        'width': atlas, 'height': atlas, 'uv_width': atlas, 'uv_height': atlas, 'particle': False,
        'use_as_default': model['index'] == 0, 'layers_enabled': False, 'sync_to_project': '',
        'file_format': 'png', 'render_mode': 'default', 'render_sides': 'auto', 'wrap_mode': 'limited',
        'pbr_channel': 'color', 'fps': 7, 'frame_time': 1, 'frame_order_type': 'loop', 'frame_order': '',
        'frame_interpolate': False, 'visible': True, 'internal': True, 'saved': True,
        'uuid': stable_uuid(model['name'], file.name),
        'source': 'data:image/png;base64,' + base64.b64encode(file.read_bytes()).decode('ascii'),
    }


def paint(elements, materials_by_uv, shaders, palette, atlas=ATLAS):
    colors = {key: tuple(bytes.fromhex(value[1:])) + (255,) for key, value in palette.items()}
    image = Image.new('RGBA', (atlas, atlas))
    for element in elements:
        shade = shaders[materials_by_uv[tuple(element['uv_offset'])]]
        for side, face in element['faces'].items():
            u, v, s, t = face['uv']
            left, top = math.floor(min(u, s)), math.floor(min(v, t))
            w, h = math.ceil(max(u, s)) - left, math.ceil(max(v, t)) - top
            for py in range(h):
                for px in range(w):
                    key = shade(side, px, py, w, h, left + px, top + py)
                    if key is not None:  # None leaves the face open, e.g. the eyeshade's crown
                        image.putpixel((left + px, top + py), colors[key])
    return image


def build_one(spec):
    parts = spec['parts']()
    for part in parts:
        # Cubes stay on the quarter-pixel grid; pivots may keep vanilla values such as the 1.9 hips.
        for values in [c[k] for c in part['cubes'] for k in ('origin', 'size')]:
            assert all(abs(n * 4 - round(n * 4)) < 1e-6 for n in values), (part['name'], values)
    statements, materials_by_uv = geometry(parts)
    # geometry() packs cubes in declaration order; keep that order for the Blockbench elements.
    uv_origins = list(materials_by_uv.keys())
    source = MODELS / (spec['model'] + '.java')
    source.write_text(spec['java'].replace('/* GEOMETRY */', statements.lstrip()), encoding='utf-8')

    shaders = spec['materials']()
    palette = dict(SHARED, **spec['palette'])
    files = [TEXTURES / f'circus_{spec["id"]}_0.png', TEXTURES / f'circus_{spec["id"]}_1.png']
    model = blockbench(spec['model'], parts, uv_origins, [])
    paint(model['elements'], materials_by_uv, shaders, palette).save(files[0])
    paint(model['elements'], materials_by_uv, shaders, dict(palette, **spec['variant'])).save(files[1])
    model['textures'] = [texture_entry(file, {'index': i, 'name': spec['model']}) for i, file in enumerate(files)]
    output = BBMODELS / (spec['model'] + '.bbmodel')
    output.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
    print(f'{spec["model"]}: {len(parts)} parts, {len(model["elements"])} cubes, {ATLAS}px atlas')


def build():
    for spec in FACULTY:
        build_one(spec)


if __name__ == '__main__':
    build()
