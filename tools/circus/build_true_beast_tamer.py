"""The Beast Tamer's true self (2026-11-17): who the Beast Tamer is in their own Chamber of Will, walking the dark
apron outside the cage of the Empty Menagerie.

The same person as the physical Beast Tamer (tools/circus/build_faculty.py): brown hair, the crimson frogged coat with
its long tails, the fur mantle, tall boots and the whip. In here the mantle has grown into a whole beast's pelt, its
skull riding over their crown with lit eyes and the hide falling down their back; the gold frogging and epaulettes are
bone, teeth and claws; the coat tails hang long and torn to the calf; a broken shackle of hematic iron rings the left
wrist and the cage keys hang at the belt; and the whip has grown out of the right wrist as a living crimson vein. For a
move that lash reaches across the ring as a glowing strand of their blood (`renderLash`), and the beasts they once caged
come out of the dark as spectral heads of red light (`renderBeasts`). No brass, bronze, copper or gilt.

The rig is jointed: hips, chest, neck, head; shoulder, elbow, wrist, four two-segment fingers and a thumb a side; hip,
knee and ankle. The root is the feet at model y 24. About 44 pixels tall, drawn 1.5 times over (`BeastTamerMoves.SCALE`),
so a texel is about a tenth of a block. Skin and cloth are shaded as round forms lit from above (`lit`), never in
columns.
"""
import math

import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'c0', 'c1', 'c2', 'c3', 'r0', 'r1', 'r2', 'r3', 'r4', 'b0', 'b1', 'b2', 'h0', 'h1', 'h2',
         's0', 's1', 's2', 's3', 's4', 'u0', 'u1', 'u2', 'u3', 'u4', 'l0', 'l1', 'l2', 'fe0', 'fe1', 'fe2',
         'i0', 'i1', 'i2', 'v0', 'v1', 'g0', 'g1', 'w']
PALETTE = ['#141017', '#1d1922', '#2a2531', '#3a3342', '#4d4556',
           '#3a0e1a', '#5e1424', '#86202f', '#a8303d', '#c8484f',
           '#8f8270', '#bfb398', '#e9e1cc',
           '#2e170f', '#5e3020', '#7a4430',
           '#6e4a38', '#96684e', '#b8876a', '#d4a283', '#e6bc9c',
           '#3a2f28', '#5a4a3c', '#7c6a54', '#a08a6c', '#c2ab88',
           '#2a1a14', '#46301f', '#6a4a30',
           '#2a2528', '#4a4246', '#7a6e72',
           '#8f8470', '#b8ab8e', '#d8ccae',
           '#4a0c16', '#7e1a28', '#ff4a3a', '#ff9a7a', '#ece6da']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0'], I['g1']}

# The front of the head, 8 by 8: the physical Tamer's face, the pupils lit and a scar across the left cheek.
FACE = [
    'hHhhhhHh',
    'hhhhhhhh',
    'hSssssSh',
    'sbbssbbs',
    'swgssgws',
    'hsvnnssh',
    'hhsmmshh',
    'hhdssdhh',
]
FACE_KEYS = {'h': 'h1', 'H': 'h2', 'S': 's4', 's': 's3', 'b': 'h0', 'w': 'w', 'g': 'g0', 'v': 'v1', 'n': 's1',
             'm': 's0', 'd': 's1'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('hips', 'root', (0, -16, 0), [box((-4.5, -3, -2.5), (9, 4, 5), 'breeches')])
    joint('belt', 'hips', (0, -3, 0), [box((-5, -1, -3), (10, 2, 6), 'belt')])
    joint('buckle', 'belt', (0, 0, -3), [box((-1.5, -1.25, -.5), (3, 2.5, .5), 'bone')])
    # The cage keys on a ring at the left hip.
    joint('keys', 'belt', (4.5, 1, -1), [
        box((-.5, 0, -1), (1, 2, 2), 'iron'),
        box((-.25, 2, -.75), (.5, 3, .5), 'iron'),
        box((-.25, 2, .25), (.5, 2.5, .5), 'iron')], (0, 0, -10))
    joint('chest', 'hips', (0, -3, 0), [box((-5, -12, -3), (10, 12, 6), 'coat')])
    # The mantle grown into a pelt: heaped over the shoulders, falling down the back.
    joint('mantle', 'chest', (0, -12, 0), [
        box((-6.5, -1.5, -3.5), (13, 4, 7.5), 'fur'),
        box((-5.5, 2.5, 2), (11, 7, 2), 'fur')])
    joint('neck', 'chest', (0, -12, -.5), [box((-1.5, -2, -1.5), (3, 2, 3), 'skin')])
    joint('head', 'neck', (0, -2, 0), [box((-4, -8, -4), (8, 8, 8), 'head')])
    # Their hair gathered at the nape, under the pelt.
    joint('tail_of_hair', 'head', (0, -3, 4), [box((-1.5, 0, -.5), (3, 5, 1.5), 'hair')], (15, 0, 0))
    # The beast's skull over the crown, its muzzle over their brow, the hide running down from it.
    joint('skull', 'head', (0, -8, 0), [
        box((-4.5, -2.5, -3.5), (9, 3, 8), 'skull'),
        box((-2.5, -1.75, -7.5), (5, 2.5, 4), 'skull')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_fang', 'skull', (sign * 1.5, .75, -7), [box((-.5, 0, -.5), (1, 2, 1), 'tooth')])
        joint(side + '_pelt_ear', 'skull', (sign * 3.5, -2.5, 0), [box((-1, -2.5, -.5), (2, 2.5, 1.5), 'fur')],
              (0, 0, sign * 12))
    joint('hide', 'skull', (0, -1, 4.5), [box((-4.5, 0, -.5), (9, 9, 1.5), 'fur')], (12, 0, 0))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_upper', 'chest', (sign * 6.5, -11, 0), [box((-2, -1.5, -2), (4, 8, 4), 'sleeve')])
        # Epaulettes of bone hung with claws.
        joint(side + '_epaulette', side + '_upper', (0, -1.5, 0), [
            box((-2.5, -1, -2.5), (5, 1.5, 5), 'bone'),
            box((-2.5, .5, -2.5), (5, 1.5, 5), 'claw')])
        joint(side + '_fore', side + '_upper', (0, 6.5, 0), [
            box((-1.75, 0, -1.75), (3.5, 6, 3.5), 'sleeve'),
            box((-2.25, 4, -2.25), (4.5, 2, 4.5), 'wrist' if sign < 0 else 'cuff')])
        if sign > 0:
            # The broken shackle on the left wrist, a link of its chain still hanging.
            joint('shackle', side + '_fore', (0, 3, 0), [box((-2.5, -.75, -2.5), (5, 1.5, 5), 'iron')])
            joint('shackle_link', 'shackle', (2, .75, 0), [box((-.5, 0, -.5), (1, 2, 1), 'iron')])
        joint(side + '_hand', side + '_fore', (0, 6, 0), [box((-1, 0, -1.75), (2, 3, 3.5), 'hand')])
        for digit, z in enumerate((-1.5, -.75, 0, .75)):
            name = side + '_finger_' + str(digit)
            length = 2 if digit in (1, 2) else 1.75
            joint(name, side + '_hand', (0, 3, z + .25), [box((-.5, 0, -.25), (1, length, .75), 'hand')])
            joint(name + '_tip', name, (0, length, 0), [box((-.5, 0, -.25), (1, 1.5, .75), 'hand')])
        joint(side + '_thumb', side + '_hand', (-sign * .75, .5, -1.75), [box((-.5, 0, -.5), (1, 2, 1), 'hand')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 2, 0), [box((-.5, 0, -.5), (1, 1.5, 1), 'hand')])
        joint(side + '_thigh', 'hips', (sign * 2.25, 1, 0), [box((-2, 0, -2), (4, 7, 4), 'breeches')])
        joint(side + '_shin', side + '_thigh', (0, 7, 0), [
            box((-2, 0, -2.25), (4, 6, 4.5), 'boot'),
            box((-2.5, -.5, -2.75), (5, 2, 5.5), 'boot_cuff')])
        joint(side + '_foot', side + '_shin', (0, 6, 0), [box((-2, 0, -4), (4, 2, 5.5), 'boot')])
        # The coat's long tails, torn to the calf, and its short front skirts.
        joint(side + '_tail', 'hips', (sign * 2.25, 1, 2.5), [box((-2, 0, -.5), (4, 13, 1), 'coat_tail')],
              (10, 0, -sign * 5))
        joint(side + '_skirt', 'hips', (sign * 2.5, 1, -2.75), [box((-2, 0, -.5), (4, 5, 1), 'coat_tail')],
              (-6, 0, -sign * 4))
    # The whip in the right fist: its handle forward, a short length of living lash hanging from it.
    joint('whip_handle', 'right_hand', (0, 2, 0), [box((-.75, -.75, -5), (1.5, 1.5, 6), 'whip_handle')])
    parent = 'whip_handle'
    for i in range(4):
        joint('lash_' + str(i), parent, (0, 0, -5) if i == 0 else (0, 4, 0), [box((-.5, 0, -.5), (1, 4, 1), 'lash')])
        parent = 'lash_' + str(i)
    # Drawn only for a move, never with the body: one segment of the reaching lash, and a caged beast's head.
    joint('lash_ghost', 'mesh.getRoot()', (0, 0, 0), [box((-1.5, 0, -1.5), (3, 16, 3), 'lash_glow')])
    joint('beast', 'mesh.getRoot()', (0, 0, 0), [
        box((-4.5, -4, -4), (9, 8, 8), 'beast'),
        box((-4.5, -5, -4.5), (9, 1.5, 3), 'beast_brow')])
    joint('beast_mane', 'beast', (0, 0, 1), [
        box((-7, -7, -1.5), (14, 14, 3), 'mane'),
        box((-6, -6, 1.5), (12, 12, 4), 'mane')])
    joint('beast_muzzle', 'beast', (0, 1, -4), [
        box((-3, -2, -5), (6, 4, 5), 'beast'),
        box((-1.5, -2.5, -5.5), (3, 1.5, 1.5), 'beast_nose')])
    for side, sign in (('right', -1), ('left', 1)):
        joint('beast_' + side + '_fang', 'beast_muzzle', (sign * 2, 2, -4), [box((-.5, 0, -.5), (1, 2, 1), 'tooth')])
        joint('beast_' + side + '_ear', 'beast', (sign * 3.5, -4, 0), [box((-1, -2.5, -.75), (2, 2.5, 1.5), 'mane')])
    joint('beast_jaw', 'beast', (0, 3, -3), [box((-2.5, 0, -5.5), (5, 2, 5.5), 'beast_jaw')])
    return parts


def lit(side, px, py, w, h, gx, gy, salt=0):
    """A round form lit from above: light on top, each face darkest at its edges, a breath of grain."""
    if side == 'up':
        v = .82
    elif side == 'down':
        v = .1
    else:
        vertical = 1 - py / max(1, h - 1)
        across = 1 - abs((px + .5) / w * 2 - 1)
        v = .22 + .38 * vertical + .32 * across ** .6
        if side == 'north':
            v += .05
        elif side == 'south':
            v -= .08
    return v + (noise(gx, gy, 11 + salt) - .5) * .12


def ramp(v, names, limits=(.28, .45, .62, .78)):
    for limit, name in zip(limits, names):
        if v < limit:
            return I[name]
    return I[names[min(len(names) - 1, len(limits))]]


SKIN = ('s0', 's1', 's2', 's3', 's4')
COAT = ('r0', 'r1', 'r2', 'r3', 'r4')
FUR = ('u0', 'u1', 'u2', 'u3', 'u4')
BONE = ('b0', 'b0', 'b1', 'b2', 'b2')
IRON = ('fe0', 'fe1', 'fe1', 'fe2', 'fe2')
LEATHER = ('l0', 'l1', 'l1', 'l2', 'l2')
BOOT = ('ink', 'c0', 'c1', 'c2', 'c3')
CREAM = ('i0', 'i0', 'i1', 'i2', 'i2')


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    r = noise(gx, gy, 31)
    if material == 'skin':
        return ramp(v, SKIN)
    if material == 'head':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if side == 'up':
            return I['h2'] if r > .7 else I['h1']
        if side == 'down':
            return I['s1']
        if side == 'south':
            return I['h0'] if py == h - 1 or r < .2 else I['h1']
        # The sides: hair over the top rows and the back, an ear, the jaw's skin forward.
        toward = w - 1 - px if side == 'east' else px
        if py < 2 or toward >= w - 3:
            return I['h0'] if r < .25 else I['h1']
        if py in (3, 4) and toward in (3, 4):
            return I['s1']
        return ramp(v + .05, SKIN)
    if material == 'hair':
        return ramp(v - .1, ('h0', 'h0', 'h1', 'h2', 'h2'))
    if material in ('coat', 'sleeve'):
        if material == 'coat' and side == 'north':
            if px in (w // 2 - 1, w // 2) and py < h - 1:
                # The placket buttoned in bone.
                return I['b2'] if py % 3 == 1 else I['r0']
            if py in (2, 5, 8) and 1 <= px <= w - 2:
                # Frogging gone to bone: a row of teeth across the chest.
                return I['b1'] if (px + py) % 2 == 0 else I['b0']
        if r < .05 and side not in ('up', 'down'):
            return I['v1']
        return ramp(v, COAT)
    if material == 'coat_tail':
        if side not in ('up', 'down') and py >= h - 2:
            # The torn hem: ragged and stained dark.
            return I['ink'] if (px + (py == h - 1)) % 2 == 0 or r < .3 else I['r0']
        return ramp(v - .05, COAT)
    if material in ('fur', 'mane'):
        if side == 'down':
            return I['u0']
        # Tufts: a streak of light or dark down the hair's fall, never a straight column.
        streak = noise(gx // 2, gy // 3, 17)
        if streak > .8:
            v += .15
        elif streak < .2:
            v -= .15
        if material == 'fur' and side not in ('up',) and py == h - 1:
            return I['u0'] if px % 2 else I['u1']
        return ramp(v, FUR)
    if material == 'skull':
        if side == 'north' and h >= 3 and w >= 9 and py == 1 and px in (1, 2, w - 3, w - 2):
            # The beast's eye sockets, lit.
            return I['g1'] if px in (2, w - 3) else I['g0']
        if side == 'north' and w == 5 and py == h - 1:
            return I['ink'] if px % 2 else I['b0']
        if r < .08:
            return I['b0']
        return ramp(v + .05, BONE)
    if material in ('tooth', 'claw'):
        if material == 'claw' and side not in ('up', 'down'):
            # A fringe of claws hanging from the epaulette.
            return I['b2'] if px % 2 == 0 and py < h - 1 else (I['ink'] if px % 2 else I['b1'])
        return I['b2'] if side in ('up', 'north') else I['b1']
    if material == 'bone':
        return ramp(v + .05, BONE)
    if material == 'belt':
        if side in ('up', 'down') or py in (0, h - 1):
            return I['l0']
        return I['l1'] if r > .25 else I['l0']
    if material == 'iron':
        if side not in ('up', 'down') and py == h - 1 and h >= 2:
            # Their blood seeping where the shackle grips.
            return I['g0'] if px % 3 == 0 and r > .3 else I['v0']
        return ramp(v, IRON)
    if material == 'cuff':
        return ramp(v + .1, BONE)
    if material == 'wrist':
        # Where the lash grows out of them: the vein lit through the skin.
        if side not in ('up', 'down') and (px + py) % 3 == 0:
            return I['g0']
        return I['v1'] if r > .4 else I['v0']
    if material == 'hand':
        if side not in ('up', 'down') and r < .06:
            return I['v1']
        return ramp(v, SKIN)
    if material == 'breeches':
        return ramp(v, CREAM)
    if material == 'boot':
        if side == 'north' and py == 0:
            return I['c3']
        return ramp(v - .05, BOOT)
    if material == 'boot_cuff':
        return ramp(v - .05, LEATHER)
    if material == 'whip_handle':
        if side in ('north', 'south'):
            return I['b1']
        return ramp(v, LEATHER)
    if material == 'lash':
        if side not in ('up', 'down') and py % 3 == 1:
            return I['g0']
        return I['v1'] if v > .5 else I['v0']
    if material == 'lash_glow':
        # Lit blood: red through, a hot glint now and then along it.
        return I['g1'] if py % 5 == 0 and px == w // 2 else I['g0']
    if material in ('beast', 'beast_brow', 'beast_jaw'):
        if material == 'beast' and side == 'north' and w == 9 and py in (2, 3) and px in (1, 2, 6, 7):
            # The beast's eyes.
            return I['g1'] if py == 2 and px in (2, 6) else I['g0']
        if material == 'beast_jaw' and side == 'up':
            return I['r0']
        return ramp(v, ('u1', 'u2', 'u3', 'u4', 'u4'))
    if material == 'beast_nose':
        return I['ink'] if side != 'up' else I['u0']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.BeastTamerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_beast_tamer.py; regenerate the model, atlas and BBModel together.
/**
 * The Beast Tamer's true self: the same person, the pelt of a beast over their shoulders and its skull on their crown,
 * walking the dark outside the cage with the whip ready in the right fist and the left hand open toward the ring. Each
 * intervention is a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up: the lash drawn back and cracked
 * down its lane, twirled overhead and cracked down to light the hoop, flicked over the Hound to goad it, and both arms
 * flung up to call the beasts out of the dark. What reaches into the ring is drawn by {@link #renderLash} (the lash, a
 * strand of their blood, from the fist to the gesture's synced point) and {@link #renderBeasts}.
 */
public final class CircusTrueBeastTamerModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_beast_tamer"), "main");
    /** Segments of the drawn lash, and how far past the mark the whipcrack runs, in blocks. */
    private static final int SEGMENTS = 28;
    private final ModelPart root, hips, chest, neck, head, handle, ghostLash, beast, beastJaw;
    private final ModelPart[] uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2],
            thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final ModelPart[] lash = new ModelPart[4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's lash (model pixels, fist to tip) and how strongly it shows; and each beast's place, turn and showing. */
    private final Vec3[] lashPoints = new Vec3[SEGMENTS + 1];
    private float lashWeight;
    private final Vec3[] beastAt = new Vec3[BeastTamerMoves.BEASTS];
    private final float[] beastYaw = new float[BeastTamerMoves.BEASTS], beastWeight = new float[BeastTamerMoves.BEASTS],
            beastGape = new float[BeastTamerMoves.BEASTS];

    public CircusTrueBeastTamerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        chest = hips.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = hips.getChild(side + "_tail");
        }
        handle = hands[0].getChild("whip_handle");
        ModelPart segment = handle;
        for (int i = 0; i < lash.length; i++) {
            segment = segment.getChild("lash_" + i);
            lash[i] = segment;
        }
        ghostLash = bakedRoot.getChild("lash_ghost");
        beast = bakedRoot.getChild("beast");
        beastJaw = beast.getChild("beast_jaw");
        for (int i = 0; i <= SEGMENTS; i++) lashPoints[i] = Vec3.ZERO;
        for (int i = 0; i < beastAt.length; i++) beastAt[i] = Vec3.ZERO;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CircusFacultyEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        movement = Mth.clamp(movement, 0.0F, 1.0F);
        float breath = Mth.sin(age * .06F);
        float stride = limbSwing * .6662F;
        // A watchful walk: long strides, the coat tails swinging, the shoulders easy.
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * movement;
            thighs[i].xRot = swing * .62F;
            shins[i].xRot = Math.max(0.0F, -Mth.cos(stride + i * Mth.PI + .9F)) * .9F * movement;
            feet[i].xRot = -thighs[i].xRot * .5F - shins[i].xRot * .6F;
            tails[i].xRot += movement * .35F + Math.abs(swing) * .15F + breath * .02F;
            uppers[i].xRot = -swing * .35F;
            uppers[i].zRot = -sign * .1F;
        }
        hips.y += Math.abs(Mth.sin(stride)) * -.6F * movement;
        chest.xRot += .04F + breath * .015F;
        chest.yRot += Mth.cos(stride) * .05F * movement;
        // At rest: the whip down at the right side, the left hand open toward the ring.
        uppers[0].xRot += -.15F;
        fores[0].xRot = -.55F;
        curl(0, 1.35F);
        uppers[1].xRot += -.6F;
        uppers[1].zRot = -.05F;
        fores[1].xRot = -.45F;
        hands[1].zRot = -.2F;
        curl(1, .25F + .2F * HemoEasing.smoothstep(.85F, 1.0F, Mth.sin(age * .023F)));
        for (int k = 0; k < lash.length; k++)
            lash[k].xRot = (k == 0 ? 1.2F : .12F) + Mth.sin(age * .09F - k * .8F) * .12F + movement * .1F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        lashWeight = 0.0F;
        for (int i = 0; i < beastWeight.length; i++) beastWeight[i] = 0.0F;
        if (gesture >= 0) gesture(gesture, since, target, age);
        for (ModelPart segment : lash) segment.visible = lashWeight < .35F;
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .05F);
            tips[i][f].zRot = sign * amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    private static int windup(int gesture) {
        return switch (gesture) {
            case BeastTamerMoves.GESTURE_CRACK -> BeastTamerMoves.CRACK_WINDUP;
            case BeastTamerMoves.GESTURE_HOOP -> BeastTamerMoves.HOOP_WINDUP;
            case BeastTamerMoves.GESTURE_GOAD -> BeastTamerMoves.GOAD_WINDUP;
            default -> BeastTamerMoves.BEASTS_WINDUP;
        };
    }

    /** The whip's crack takes the last three ticks of the wind-up, accelerating: 0 until then, 1 on the blow. */
    private static float crack(float since, float windup) {
        float t = Mth.clamp((since - (windup - 3.0F)) / 3.0F, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, Vec3 target, float age) {
        float w = windup(gesture);
        float end = gesture == BeastTamerMoves.GESTURE_BEASTS
                ? BeastTamerMoves.beastLands(BeastTamerMoves.BEASTS - 1) + BeastTamerMoves.BEAST_RUN
                : gesture == BeastTamerMoves.GESTURE_HOOP ? w + 10.0F : w + BeastTamerMoves.HELD;
        float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(end, end + BeastTamerMoves.RECOVER, since));
        if (weight <= 0.0F) return;
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since);
        float tension = HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - HemoEasing.smoothstep(w - 3.0F, w, since));
        float tremble = Mth.sin(since * 3.3F) * tension;
        float down = crack(since, w);
        Vec3 hand = handleTip();
        Vec3 mark = model(target);
        if (gesture == BeastTamerMoves.GESTURE_BEASTS) {
            // Both arms flung up and wide, the head thrown back: calling them out of the dark. Each leap a jab of the fists.
            float jab = 0.0F;
            for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
                float s = since - BeastTamerMoves.beastLands(i);
                jab = Math.max(jab, s >= 0.0F && s < 6.0F ? 1.0F - s / 6.0F : 0.0F);
            }
            for (int i = 0; i < 2; i++) {
                float sign = i == 0 ? -1 : 1;
                uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, -2.55F + .5F * jab + .06F * tremble);
                uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * (.75F - .25F * jab));
                fores[i].xRot = Mth.lerp(weight, fores[i].xRot, -.25F - .4F * jab);
            }
            curl(1, Mth.lerp(weight, .3F, .05F + .9F * jab));
            head.xRot += weight * (-.45F + .25F * jab);
            chest.xRot += weight * (-.18F + .2F * jab);
            hand = handleTip();
            lashWeight = weight;
            for (int s = 0; s <= SEGMENTS; s++) {
                float f = s / (float) SEGMENTS;
                // The lash raised high and snaking over their head.
                lashPoints[s] = hand.add(Mth.sin(age * .25F + f * 5.0F) * 7.0F * f, -42.0F * f, Mth.cos(age * .2F + f * 4.0F) * 5.0F * f);
            }
            beasts(since, target);
            return;
        }
        float back = gather * (1.0F - down);
        boolean hoop = gesture == BeastTamerMoves.GESTURE_HOOP;
        // The whip arm: hauled back overhead through the gather (twirling it, for the hoop), trembling at the top, then
        // cracked forward and down in three ticks; held, then a slow recovery.
        float up = hoop ? -2.9F : Mth.lerp(gather, -.6F, -2.75F);
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, up + .05F * tremble, gesture == BeastTamerMoves.GESTURE_GOAD ? -1.5F : -1.05F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, .22F);
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, hoop ? -.3F : Mth.lerp(gather, -.55F, -1.6F), -.1F));
        if (hoop) fores[0].zRot += Mth.sin(since * .9F) * .45F * back * weight;
        chest.yRot += weight * (.38F * back - .32F * down);
        chest.xRot += weight * (-.12F * back + .24F * down);
        head.xRot += weight * .12F * down;
        // The left arm out for balance, the stance set: right foot back.
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -.4F - .6F * back);
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, -.75F);
        curl(1, Mth.lerp(weight, .3F, .1F));
        thighs[0].xRot += weight * .35F;
        thighs[1].xRot -= weight * .3F;
        shins[0].xRot += weight * .25F;
        feet[0].xRot = -thighs[0].xRot * .5F - shins[0].xRot * .6F;
        feet[1].xRot = -thighs[1].xRot * .5F - shins[1].xRot * .6F;
        hand = handleTip();
        lashWeight = weight;
        // Where the lash lands: down its lane past the mark, onto the hoop, or cracked in the air over the Hound.
        Vec3 flat = new Vec3(mark.x - hand.x, 0.0D, mark.z - hand.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, -1.0D) : flat.normalize();
        Vec3 tip = switch (gesture) {
            case BeastTamerMoves.GESTURE_CRACK -> mark.add(flat.scale(BeastTamerMoves.CRACK_OVERSHOOT * 16.0D / BeastTamerMoves.SCALE));
            case BeastTamerMoves.GESTURE_GOAD -> mark.add(0.0D, -2.2D * 16.0D / BeastTamerMoves.SCALE, 0.0D);
            default -> mark;
        };
        boolean air = gesture == BeastTamerMoves.GESTURE_GOAD;
        float retract = HemoEasing.smoothstep(end, end + BeastTamerMoves.RECOVER, since);
        for (int s = 0; s <= SEGMENTS; s++) {
            float f = s / (float) SEGMENTS;
            Vec3 drawn = hoop
                    // Twirled overhead in a widening spiral.
                    ? hand.add(Mth.cos(since * .9F + f * 2.6F) * (5.0F + 20.0F * f), -6.0F - 4.0F * f,
                            Mth.sin(since * .9F + f * 2.6F) * (5.0F + 20.0F * f))
                    // Drawn back over the shoulder and coiled behind, swaying with the breath of the gather.
                    : hand.add(0.0D, -5.0F - 12.0F * f + Mth.sin(f * 6.0F + since * .4F) * 2.5F * f, 8.0F + 28.0F * f);
            Vec3 laid = hand.lerp(tip, f);
            laid = air ? laid.add(0.0D, -Mth.sin(f * Mth.PI) * 8.0F, 0.0D)
                    // Down to the floor fast from the fist, then along it.
                    : new Vec3(laid.x, tip.y + (hand.y - tip.y) * Math.pow(1.0F - f, 3.0F), laid.z);
            // The whip unrolls: its base goes first, its tip last.
            float unroll = Mth.clamp(down * 1.6F - f * .6F, 0.0F, 1.0F);
            Vec3 point = drawn.lerp(laid, unroll);
            lashPoints[s] = hand.add(point.subtract(hand).scale(1.0F - .85F * retract));
        }
    }

    /** Each beast: crouched in the dark, then bounding down its lane, jaws wide, then fading at its end. */
    private void beasts(float since, Vec3 target) {
        for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
            float s = since - BeastTamerMoves.beastLands(i);
            float run = BeastTamerMoves.BEAST_RUN;
            if (s < -10.0F || s > run + 6.0F) continue;
            Vec3 dir = BeastTamerMoves.beastDir(target, i);
            Vec3 world;
            if (s < 0.0F) {
                world = BeastTamerMoves.beastFrom(target, i).add(0.0D, .7D, 0.0D);
                beastWeight[i] = HemoEasing.smoothstep(-10.0F, -2.0F, s) * .75F;
                beastGape[i] = .15F;
            } else {
                float t = Math.min(s, run);
                world = BeastTamerMoves.beastAt(target, i, t).add(0.0D, 1.0D + Math.sin(Math.PI * t / run) * 1.1D, 0.0D);
                beastWeight[i] = 1.0F - HemoEasing.smoothstep(run, run + 6.0F, s);
                beastGape[i] = s <= run ? 1.0F : 1.0F - (s - run) / 6.0F;
            }
            beastAt[i] = model(world);
            Vec3 ahead = model(world.add(dir)).subtract(beastAt[i]);
            beastYaw[i] = (float) Math.atan2(-ahead.x, -ahead.z);
        }
    }

    /** Where the whip's handle ends in the fist, in this model's pixels. */
    private Vec3 handleTip() {
        PoseStack stack = new PoseStack();
        for (ModelPart part : new ModelPart[]{root, hips, chest, uppers[0], fores[0], hands[0], handle})
            part.translateAndRotate(stack);
        Vector3f p = stack.last().pose().transformPosition(0.0F, 0.0F, -5.0F / 16.0F, new Vector3f());
        return new Vec3(p.x * 16.0D, p.y * 16.0D, p.z * 16.0D);
    }

    /** Draws the reaching lash: a strand of their blood from the fist to this frame's tip, tapering, faded with its move. */
    public void renderLash(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (lashWeight <= .01F) return;
        int alpha = (int) (Mth.clamp(lashWeight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        for (int s = 0; s < SEGMENTS; s++) {
            Vec3 a = lashPoints[s], d = lashPoints[s + 1].subtract(a);
            double length = d.length();
            if (length < 1.0E-3D) continue;
            Vec3 dir = d.scale(1.0D / length);
            ghostLash.setPos((float) a.x, (float) a.y, (float) a.z);
            ghostLash.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            float taper = Mth.lerp(s / (float) SEGMENTS, 1.0F, .35F);
            ghostLash.xScale = ghostLash.zScale = taper;
            ghostLash.yScale = (float) (length / 16.0D) * 1.05F;
            ghostLash.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** Draws the beasts out of the dark: each a caged animal's head in red light, its eyes lit, its jaws open. */
    public void renderBeasts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
            if (beastWeight[i] <= .01F) continue;
            beast.setPos((float) beastAt[i].x, (float) beastAt[i].y, (float) beastAt[i].z);
            beast.setRotation(0.0F, beastYaw[i], 0.0F);
            beast.xScale = beast.yScale = beast.zScale = 1.3F;
            beastJaw.xRot = beastGape[i] * .75F;
            int alpha = (int) (Mth.clamp(beastWeight[i], 0.0F, 1.0F) * ((color >>> 24) & 255));
            beast.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / BeastTamerMoves.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 256
    return build_rig('circus_true_beast_tamer', 'CircusTrueBeastTamerModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
