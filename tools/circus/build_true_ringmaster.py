"""The Ringmaster's true self (2026-11-18): the shape he comes down from the rafters in, at half his health, in the
finale in his Chamber of Will. He keeps fighting in it, so it walks, strikes and kneels like a man.

The same showman as the physical Ringmaster, as he is in his own mind: the black top hat with its crimson band, the
crimson tailcoat with black lacquer lapels and cuffs and long split tails, black trousers striped red, and the staff
crowned with its topper (a bone hoop round a blood core). What the true self adds: the face under the hat is a bone
mask split by a grin of teeth, its sockets lit red; the coat hangs open on a ribcage with his blood lit inside it;
the hands are bare grey flesh netted with lit veins, the nails grown to bone; the epaulettes are hematic iron with a
bone fringe (no brass, no gilt); and three red threads run up from his shoulders and collar into the dark, held
plumb whatever he does, the puppet master on strings of his own. Cloth and lacquer are shaded as round forms lit from
above (each face darker toward its edges), never in vertical stripes.

The rig is jointed: hips, knees, ankles; waist, chest, a short neck the head sits low on, head; shoulders, elbows,
wrists, four two-segment fingers and a two-segment thumb a side. The feet are the root at model y 24. About 1.25
times a man's size in pixels, drawn 1.5 times over (`CircusTrueRingmasterRenderer.TRUE_SCALE`): near four blocks to
the hat's brim. The staff is held across the right fist along the hand's z axis, so the renderer can stand it plumb
(planted) or let it ride the arm (a blow).
"""
import math

import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise, front_edge

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'k0', 'k1', 'k2', 'k3', 'r0', 'r1', 'r2', 'r3', 'b0', 'b1', 'b2', 'fe0', 'fe1', 'fe2',
         'f0', 'f1', 'f2', 'f3', 'v0', 'v1', 'g0', 'g1']
PALETTE = ['#08060a', '#120d11', '#1f171c', '#33272d', '#4f3d45',
           '#3a0610', '#6a0d1d', '#94162b', '#bf2a3c',
           '#8d8170', '#bfb398', '#e9e1cc',
           '#2a2528', '#4a4246', '#7a6e72',
           '#2b1a1e', '#4a2c31', '#6e4a4c', '#94716c',
           '#4a0c16', '#7e1a28', '#ff3a2e', '#ffa080']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0'], I['g1']}

# The front of the head, 8 by 8: a bone mask, a crack across it, red-lit sockets and a grin of teeth from ear to ear.
FACE = [
    'bBBBBBBb',
    'BBBcBBBB',
    'BKKBcKKB',
    'BKEBBEKB',
    'bBKBBBKb',
    'vrrrrrrv',
    'TtTtTtTt',
    'vTrTrTrv',
]
FACE_KEYS = {'B': 'b1', 'b': 'b0', 'c': 'v1', 'K': 'ink', 'E': 'g0', 'v': 'v0', 'r': 'r1', 'T': 'b2', 't': 'ink'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -18, 0), [box((-4.5, -3, -2.5), (9, 4, 5), 'waist')])
    joint('buckle', 'pelvis', (0, -2.5, -2.75), [box((-1, -.5, -.25), (2, 1.5, .5), 'bone')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_thigh', 'pelvis', (sign * 2.25, 0, 0), [box((-2, 0, -2), (4, 8, 4), 'trousers')])
        joint(side + '_shin', side + '_thigh', (0, 8, 0), [
            box((-1.5, 0, -1.5), (3, 8, 3), 'trousers'),
            box((-2, 3, -2), (4, 5, 4), 'boot')])
        joint(side + '_foot', side + '_shin', (0, 8, 0), [
            box((-2, 0, -4.5), (4, 2, 6), 'boot'),
            box((-1.5, .75, -6), (3, 1.25, 1.5), 'boot')])
        # The tailcoat is cut away in front; its two tails hang long behind, to the calf.
        joint(side + '_tail', 'pelvis', (sign * 2.25, 0, 2.5), [box((-2.25, 0, -.5), (4.5, 14, 1), 'tail')],
              (6, 0, -sign * 3))
    joint('chest', 'pelvis', (0, -3, 0), [
        box((-4.5, -10, -2.5), (9, 10, 5), 'torso'),
        box((-5, -10, -2.75), (10, 3, 5.5), 'coat')])
    # The open coat's ribcage: a sternum and three pairs of ribs over his lit blood.
    joint('sternum', 'chest', (0, -7, -2.5), [box((-.5, 0, -.5), (1, 5.5, .5), 'bone')])
    for k, y in enumerate((-6.5, -5, -3.5)):
        for side, sign in (('right', -1), ('left', 1)):
            joint(side + '_rib_' + str(k), 'chest', (sign * .5, y, -2.5),
                  [box((0 if sign > 0 else -2.5, 0, -.5), (2.5, .5, .5), 'bone')], (0, 0, sign * 14))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_lapel', 'chest', (sign * 3, -7, -2.75), [box((-1, 0, -.5), (2, 7, .5), 'lapel')],
              (0, 0, -sign * 8))
    # The high collar stands up behind the head, crimson without, black within.
    joint('collar', 'chest', (0, -10, 1.75), [box((-5, -5, -.5), (10, 5, 1), 'collar')], (-14, 0, 0))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_collar', 'chest', (sign * 4.75, -10, 0), [box((-.5, -4, -2), (1, 4, 4), 'collar')],
              (0, 0, sign * 10))
    # A short neck: the head sits low between the collar's wings.
    joint('neck', 'chest', (0, -10, -.5), [box((-2, -2, -2), (4, 2, 4), 'flesh')])
    joint('head', 'neck', (0, -1.5, 0), [box((-4, -8, -4), (8, 8, 8), 'head')])
    joint('hat', 'head', (0, -8, 0), [
        box((-6, -1, -6), (12, 1, 12), 'brim'),
        box((-4, -10, -4), (8, 9, 8), 'hat'),
        box((-4.5, -11.5, -4.5), (9, 1.5, 9), 'hat'),
        box((-4.25, -3.5, -4.25), (8.5, 2, 8.5), 'band')], (-4, 0, -5))
    joint('brooch', 'hat', (0, -2.5, -4.25), [box((-1, -1, -.5), (2, 2, .5), 'brooch')])
    # His own strings, from the shoulders and the collar up into the dark; the model holds them plumb.
    joint('strings', 'chest', (0, -9, 0), [
        box((-4, -56, -.25), (.5, 56, .5), 'thread'),
        box((3.5, -56, -.25), (.5, 56, .5), 'thread'),
        box((-.25, -60, 2), (.5, 58, .5), 'thread')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_shoulder', 'chest', (sign * 4.5, -9, 0), [
            box((-2.5, -1.5, -2.5), (5, 1.5, 5), 'iron'),
            box((-2.75, 0, -2.75), (5.5, 1.5, 5.5), 'fringe')])
        joint(side + '_upper', side + '_shoulder', (sign * .75, 0, 0), [box((-1.5, 0, -1.5), (3, 6.5, 3), 'sleeve')])
        joint(side + '_fore', side + '_upper', (0, 6.5, 0), [
            box((-1.25, 0, -1.25), (2.5, 5.5, 2.5), 'sleeve'),
            box((-1.75, 3.5, -1.75), (3.5, 2, 3.5), 'lacquer')])
        # The palm faces in toward the body; bare fingers, the nails grown out to bone.
        joint(side + '_hand', side + '_fore', (0, 5.5, 0), [box((-.75, 0, -2), (1.5, 2.5, 4), 'hand')])
        for digit, (z, length) in enumerate(((-1.5, 1.5), (-.5, 1.75), (.5, 1.75), (1.5, 1.5))):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 2.5, z), [box((-.5, 0, -.5), (1, length, 1), 'hand')])
            joint(name + '_tip', name, (0, length, 0), [box((-.5, 0, -.5), (1, 1.25, 1), 'claw')])
        joint(side + '_thumb', side + '_hand', (-sign * .75, .5, -1.75), [box((-.5, 0, -.5), (1, 1.5, 1), 'hand')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 1.5, 0), [box((-.5, 0, -.5), (1, 1.25, 1), 'claw')])
    # The staff, across the right fist on the palm side along the hand's z: the topper end at -z, the ferrule at +z.
    joint('staff', 'right_hand', (1.25, 1.75, 0), [
        box((-.5, -.5, -8), (1, 1, 30), 'shaft'),
        box((-.75, -.75, -2.5), (1.5, 1.5, 5), 'grip'),
        box((-.75, -.75, 21), (1.5, 1.5, 1.5), 'iron')])
    # The topper: a bone hoop standing in the staff's plane round the blood core, on a collar of iron.
    joint('topper', 'staff', (0, 0, -8), [
        box((-1, -1, -1), (2, 2, 1.5), 'iron'),
        box((-3, -.5, -8), (6, 1, 1), 'hoop'),
        box((-3, -.5, -2), (6, 1, 1), 'hoop'),
        box((-3, -.5, -7), (1, 1, 5), 'hoop'),
        box((2, -.5, -7), (1, 1, 5), 'hoop')])
    joint('core', 'topper', (0, 0, -4.5), [box((-1.25, -.75, -1.25), (2.5, 1.5, 2.5), 'core')], (0, 45, 0))
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


def ramp(v, names):
    """Picks a shade from dark to light by value."""
    step = 1.0 / len(names)
    return I[names[min(len(names) - 1, max(0, int(v / step)))]]


CRIMSON = ('r0', 'r1', 'r1', 'r2', 'r2', 'r3')
LACQUER = ('ink', 'k0', 'k1', 'k2', 'k3')
FLESH = ('f0', 'f1', 'f2', 'f3')


def cloth(v, gx, gy, py, h):
    """The crimson coat: old blood soaking up from the hem."""
    if py >= h - 2 and noise(gx, gy, 21) > .45 - .2 * (py - h + 2):
        return I['v0']
    return ramp(v, CRIMSON)


def veined(v, px, py, w, h, gx, gy):
    """Dead grey flesh netted with lit veins."""
    if w > 1 and h > 2:
        column = int(w * .5 + w * .3 * math.sin(py * .7 + (gx // 4) * 1.9))
        if px == column and noise(gx, gy, 3) > .35:
            return I['g0'] if noise(gx, gy, 4) > .55 else I['v1']
    return ramp(v * .95, FLESH)


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material in ('coat', 'sleeve'):
        if material == 'sleeve' and side in ('up', 'down'):
            return I['r0']
        return cloth(v, gx, gy, py, h if material == 'coat' else 99)
    if material == 'torso':
        if side != 'north':
            return cloth(v, gx, gy, py, h)
        # The coat open on the chest: crimson at the sides, the cavity between them dark with his blood, lit at heart.
        if px < 2 or px >= w - 2:
            return ramp(v, CRIMSON)
        cx, cy = (px - 3.5), (py - 4.5)
        if cx * cx + cy * cy < 1.6:
            return I['g1'] if cx * cx + cy * cy < .6 else I['g0']
        if cx * cx + cy * cy < 4.5 and noise(gx, gy, 5) > .3:
            return I['v1']
        return I['v0'] if noise(gx, gy, 6) > .35 else I['r0']
    if material == 'waist':
        if py == 0 and side not in ('up', 'down'):
            return ramp(v * .8, LACQUER)
        if side == 'north' and 2 <= px < w - 2:
            return ramp(v * .7, LACQUER)
        return cloth(v, gx, gy, py, 99)
    if material == 'tail':
        if side == 'north':
            return ramp(v * .6, LACQUER)
        if side in ('up', 'down'):
            return I['r0']
        return cloth(v, gx, gy, py, h)
    if material == 'collar':
        if side == 'north':
            return ramp(v * .6, LACQUER)
        if side == 'up':
            return I['b1']
        return ramp(v, CRIMSON)
    if material in ('lapel', 'lacquer', 'boot', 'hat', 'brim'):
        if material == 'lapel' and front_edge(side, px, w):
            return I['r3']
        if material == 'lapel' and side == 'north' and px == w - 1:
            return I['r2']
        if side == 'up':
            return I['k3'] if noise(gx, gy, 7) > .5 else I['k2']
        if side == 'down':
            return I['ink']
        # A lacquer sheen: one bright line high on each face.
        if material in ('hat', 'boot') and py == 1 and noise(gx, gy, 8) > .25:
            return I['k3']
        return ramp(v * .8, LACQUER)
    if material == 'trousers':
        if side in ('east', 'west') and px == w // 2:
            return I['r2'] if v > .45 else I['r1']
        return ramp(v * .75, LACQUER)
    if material == 'band':
        if side in ('up', 'down'):
            return I['r0']
        if py == 0:
            return I['b0'] if px % 2 == 0 else I['r1']
        return ramp(v, CRIMSON)
    if material == 'brooch':
        if side == 'north':
            # A lidless eye set in bone, lit.
            return I['g1'] if (px, py) == (1, 0) else I['g0'] if (px, py) == (0, 1) else I['b1']
        return I['b0']
    if material == 'iron':
        if side == 'up' or py == 0:
            return I['fe2']
        if side == 'down' or py == h - 1:
            return I['fe0']
        if noise(gx, gy, 16) < .1:
            return I['v1']
        return I['fe1'] if v < .7 else I['fe2']
    if material == 'fringe':
        if side in ('up', 'down'):
            return I['fe0']
        return I['b1'] if px % 2 == 0 else I['ink']
    if material in ('bone', 'hoop'):
        if side == 'down':
            return I['b0']
        if material == 'hoop' and noise(gx, gy, 17) > .85:
            return I['v1']
        return I['b2'] if side == 'up' or v > .7 else I['b1'] if v > .4 else I['b0']
    if material == 'flesh':
        return veined(v * .8, px, py, w, h, gx, gy)
    if material == 'head':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if front_edge(side, px, w) and py < 5:
            return I['b0']
        if side == 'up':
            return I['f0']
        return veined(v * .7, px, py, w, h, gx, gy)
    if material == 'hand':
        if side == 'down':
            return I['f0']
        return veined(v, px, py, w, h, gx, gy)
    if material == 'claw':
        if side == 'up':
            return I['f1']
        return I['b2'] if py == h - 1 else I['b1'] if v > .45 else I['b0']
    if material == 'shaft':
        # Hematic iron, a vein of his blood wound round it.
        if side not in ('up', 'down') and (gy + gx) % 7 == 0:
            return I['g0'] if noise(gx, gy, 18) > .6 else I['v1']
        return I['fe1'] if noise(gx, gy, 19) > .3 else I['fe0']
    if material == 'grip':
        return I['b1'] if (py + px) % 2 == 0 else I['b0']
    if material == 'core':
        return I['g1'] if side == 'north' and px in (1, 2) and py == 0 else I['g0']
    if material == 'thread':
        return I['g0']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_ringmaster.py; regenerate the model, atlas and BBModel together.
/**
 * The Ringmaster's true self, worn by his finale copy from the reveal on: the showman in his top hat and tailcoat, a
 * bone mask grinning under the brim, the coat open on his lit ribs, his own red strings running up into the dark. He
 * keeps fighting in it. At rest he stands leaning on the staff planted beside him, the free hand's fingers working;
 * walking, he strides with knees and ankles, the tails swinging and the staff swung like a cane. Each move replays the
 * server's synced move tick ({@link CircusRingmasterEntity#moveTime}) like his physical model's, but with the staff's
 * weight: the topper hauled back over the shoulder and cracked down in front of him. He presents himself through the
 * reveal (staff high, the free arm thrown wide), and at the kneel sinks onto one knee, the staff planted, the head bowed.
 */
public final class CircusTrueRingmasterModel extends EntityModel<CircusRingmasterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_ringmaster"), "main");
    /** The staff's own turn on the fist through a blow: the topper leads, behind the head raised, ahead of him landed. */
    private static final float STAFF_BLOW = 1.1F;
    /** How far the kneel lowers his hips, in pixels: the near thigh upright, its shin along the floor. */
    private static final float KNEEL_DROP = 8.5F;
    private final ModelPart root, pelvis, chest, neck, head, strings, staff, topper, core;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2],
            shins = new ModelPart[2], feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];

    public CircusTrueRingmasterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        strings = chest.getChild("strings");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            shoulders[i] = chest.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = pelvis.getChild(side + "_tail");
        }
        staff = hands[0].getChild("staff");
        topper = staff.getChild("topper");
        core = topper.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CircusRingmasterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = AnimationTime.partialTick();
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.move(), entity.moveTime(partialTick),
                entity.finalePhase(), entity.finaleTick() + partialTick, entity.hasTopper());
    }

    /**
     * The whole pose: {@code moveTime} is the ticks into the running move, {@code phaseTime} the ticks into the
     * finale's phase.
     */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, Move move, float moveTime,
            FinaleRules.FinalePhase phase, float phaseTime, boolean hasTopper) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        topper.visible = hasTopper;
        float breath = Mth.sin(age * .05F);
        // A looming stoop, the chest rising and falling, the head cocked a little too far and drifting back.
        chest.xRot += .07F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        head.zRot += .08F + Mth.sin(age * .031F) * .05F;
        // The staff arm: forearm out before him, the fist closed on the shaft. The free hand hangs, its fingers working.
        uppers[0].xRot = -.25F + breath * .02F;
        uppers[0].zRot = .3F;
        fores[0].xRot = -1.15F;
        curl(0, 1.45F);
        uppers[1].xRot = .05F;
        uppers[1].zRot = -.14F;
        fores[1].xRot = -.35F;
        curl(1, .45F + .25F * Mth.sin(age * .09F) * Mth.sin(age * .023F));
        for (int i = 0; i < 2; i++) tails[i].xRot += Mth.sin(age * .05F + i) * .04F;
        // Walking: a long stride bending at the knee and ankle, the hips dipping, the tails swinging behind.
        float step = Mth.clamp(movement * 4.0F, 0.0F, 1.0F);
        float stride = limbSwing * .6662F;
        pelvis.y += Math.abs(Mth.sin(stride)) * .7F * step;
        for (int i = 0; i < 2; i++) {
            float swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot += swing * .6F * step;
            shins[i].xRot += Math.max(0.0F, Mth.sin(stride + i * Mth.PI)) * .9F * step;
            feet[i].xRot = -(thighs[i].xRot + shins[i].xRot) * .6F;
            tails[i].xRot += Math.max(0.0F, -swing) * .45F * step + .15F * step;
        }
        uppers[1].xRot += Mth.cos(stride) * .4F * step;
        float cane = -Mth.cos(stride) * .35F * step;
        pelvis.yRot += Mth.cos(stride) * .06F * step;
        float blow = 0.0F;
        BossMoveTimeline timeline = move.timeline();
        if (timeline != null) {
            float commit = timeline.commitTick(), total = timeline.total();
            switch (move) {
                case KNIFE_THROW -> throwKnife(moveTime, commit, total);
                case STAFF_STRIKE, SLEIGHT -> blow = staffStrike(moveTime, commit, total);
                case KNIFE_BOARD -> knifeBoard(moveTime, commit);
                case SPOTLIGHT -> spotlight(moveTime, commit, total, age);
                case CUE -> blow = cue(moveTime, commit, total);
                case STRINGS_CUT -> pullStrings(moveTime, commit, total);
                default -> {
                }
            }
        }
        if (phase == FinaleRules.FinalePhase.REVEAL) present(phaseTime, age);
        if (phase == FinaleRules.FinalePhase.KNEEL || phase == FinaleRules.FinalePhase.ENDED) {
            blow = 0.0F;
            kneel(HemoEasing.smoothstep(0.0F, FinaleRules.KNEEL_SETTLE_TICKS, phaseTime), breath);
        }
        // The staff stands plumb in the fist (planted, or swung like a cane) except through a blow, when it rides the arm.
        Vector3f plumb = local(new Vector3f(0.0F, Mth.cos(cane), -Mth.sin(cane)), pelvis, chest, shoulders[0], uppers[0],
                fores[0], hands[0]);
        staff.xRot = Mth.lerp(blow, (float) -Math.asin(Mth.clamp(plumb.y, -1.0F, 1.0F)), STAFF_BLOW);
        staff.yRot = Mth.lerp(blow, (float) Mth.atan2(plumb.x, plumb.z), 0.0F);
        float pulse = 1.0F + Mth.sin(age * .11F) * .06F;
        core.xScale = core.yScale = core.zScale = pulse;
        // His own strings hang plumb from the rigging whatever his body does, swaying a little.
        Vector3f down = local(new Vector3f(Mth.sin(age * .02F) * .04F, 1.0F, Mth.cos(age * .017F) * .04F).normalize(),
                pelvis, chest);
        strings.xRot = (float) Math.acos(Mth.clamp(down.y, -1.0F, 1.0F));
        strings.yRot = (float) Mth.atan2(down.x, down.z);
    }

    /** A model-space direction in the frame of the last of {@code chain} (root first), undoing each joint's turn. */
    private static Vector3f local(Vector3f direction, ModelPart... chain) {
        Quaternionf turn = new Quaternionf();
        for (ModelPart part : chain) turn.mul(new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot));
        return turn.conjugate().transform(direction);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** 0 until {@code lead} ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float drop(float since, float at, float lead) {
        float t = Mth.clamp((since - (at - lead)) / lead, 0.0F, 1.0F);
        return t * t;
    }

    /**
     * The staff strike: the topper hauled back over his shoulder through the wind-up, the body rearing, a tremble at the
     * top, then cracked down in front of him on the commit tick, the body pitched into it, a hold, and the recovery.
     * Returns how fully the staff rides the arm.
     */
    private float staffStrike(float ticks, float commit, float total) {
        float raise = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 2.0F);
        float tension = HemoEasing.smoothstep(commit - 4.0F, commit - 2.0F, ticks) * (1.0F - down);
        float tremble = Mth.sin(ticks * 3.1F) * tension;
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(raise, -1.0F, -3.0F) + .04F * tremble, -.7F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, -.1F);
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.4F, -.15F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -.9F * raise, .4F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.5F, -.25F));
        chest.xRot += weight * Mth.lerp(down, -.22F * raise, .32F);
        head.xRot += weight * Mth.lerp(down, -.2F * raise, .15F);
        // He steps into the blow: the right foot forward, the left knee bending.
        thighs[0].xRot += weight * Mth.lerp(down, .1F, -.5F);
        shins[0].xRot += weight * down * .3F;
        thighs[1].xRot += weight * down * .25F;
        shins[1].xRot += weight * down * .35F;
        for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
        return weight;
    }

    /** The free arm drawn back behind the shoulder through the wind-up, then whipped out and down as the knife leaves. */
    private void throwKnife(float ticks, float commit, float total) {
        float wind = HemoEasing.smoothstep(0.0F, commit * .6F, ticks);
        float whip = drop(ticks, commit, 2.0F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(whip, Mth.lerp(wind, -.4F, -2.8F), -.8F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(whip, -.35F, .2F));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(whip, -1.3F * wind, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(whip, 1.2F, .05F)));
        chest.yRot += weight * Mth.lerp(whip, .35F * wind, -.3F);
        head.yRot -= weight * Mth.lerp(whip, .2F * wind, -.15F);
    }

    /** Both hands drawn back and spread, setting knives in a board, then thrown forward. */
    private void knifeBoard(float ticks, float commit) {
        float draw = HemoEasing.smoothstep(0.0F, commit, ticks);
        float out = drop(ticks, commit, 2.0F) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, commit + 8.0F, ticks));
        float weight = draw * (1.0F - HemoEasing.smoothstep(commit + 2.0F, commit + 8.0F, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(out, -2.5F, -1.4F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(out, -.9F, .1F));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(out, -.8F, -.05F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(out, 1.1F, .1F)));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, .55F);
        chest.xRot += weight * Mth.lerp(out, -.15F, .2F);
    }

    /** The staff lifted high and held for the whole of the light, the free hand pointing down at the lit pool. */
    private void spotlight(float ticks, float commit, float total, float age) {
        float hold = HemoEasing.smoothstep(0.0F, commit * .6F, ticks) * (1.0F - HemoEasing.smoothstep(total - 6.0F, total, ticks));
        float sway = Mth.sin(age * .15F) * .05F;
        uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, -2.8F + sway);
        uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, .2F);
        fores[0].xRot = Mth.lerp(hold, fores[0].xRot, -.2F);
        uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, -1.2F);
        uppers[1].yRot = Mth.lerp(hold, uppers[1].yRot, -.3F);
        fores[1].xRot = Mth.lerp(hold, fores[1].xRot, -.1F);
        curl(1, Mth.lerp(hold, .45F, .1F));
        head.xRot += .25F * hold;
    }

    /** The Cue: the staff raised like a baton through the count, the free arm high, then brought down hard on the beat. */
    private float cue(float ticks, float commit, float total) {
        float count = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(count, -1.0F, -3.0F), -.5F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.3F, -.2F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -2.4F * count, -.6F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.9F, -.3F));
        curl(1, Mth.lerp(weight, .45F, .05F));
        head.xRot += weight * Mth.lerp(down, -.3F * count, .1F);
        chest.xRot += weight * down * .28F;
        return weight;
    }

    /** Strings Cut: both fists close and haul back on a thread through the wind-up, then the borrowed act lands. */
    private void pullStrings(float ticks, float commit, float total) {
        float haul = HemoEasing.smoothstep(0.0F, commit, ticks);
        float release = drop(ticks, commit + 1.5F, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 3.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(release, -1.5F + .4F * haul, -1.9F));
        uppers[1].yRot = Mth.lerp(weight, uppers[1].yRot, -.5F * (1.0F - release));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(release, -1.2F * haul, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(release, 1.5F, .1F)));
        chest.xRot += weight * Mth.lerp(release, -.25F * haul, .3F);
    }

    /** The reveal: the staff raised high, the free arm thrown wide to the stands, the head back. */
    private void present(float ticks, float age) {
        float on = HemoEasing.smoothstep(0.0F, 14.0F, ticks)
                * (1.0F - HemoEasing.smoothstep(FinaleRules.REVEAL_TICKS - 12.0F, FinaleRules.REVEAL_TICKS, ticks));
        float sway = Mth.sin(age * .07F);
        uppers[0].xRot = Mth.lerp(on, uppers[0].xRot, -2.6F + .05F * sway);
        uppers[0].zRot = Mth.lerp(on, uppers[0].zRot, .25F);
        fores[0].xRot = Mth.lerp(on, fores[0].xRot, -.3F);
        uppers[1].xRot = Mth.lerp(on, uppers[1].xRot, -.4F);
        uppers[1].zRot = Mth.lerp(on, uppers[1].zRot, -1.25F + .06F * sway);
        fores[1].xRot = Mth.lerp(on, fores[1].xRot, -.15F);
        curl(1, Mth.lerp(on, .45F, .1F));
        chest.xRot -= .15F * on;
        head.xRot -= .3F * on;
    }

    /** The kneel: onto the right knee, the left foot planted before him, the staff planted, the head bowed over it. */
    private void kneel(float k, float breath) {
        pelvis.y += KNEEL_DROP * k;
        pelvis.yRot *= 1.0F - k;
        thighs[0].xRot = Mth.lerp(k, thighs[0].xRot, .12F);
        shins[0].xRot = Mth.lerp(k, shins[0].xRot, 1.45F);
        feet[0].xRot = Mth.lerp(k, feet[0].xRot, -.25F);
        thighs[1].xRot = Mth.lerp(k, thighs[1].xRot, -1.45F);
        thighs[1].zRot = Mth.lerp(k, thighs[1].zRot, -.08F);
        shins[1].xRot = Mth.lerp(k, shins[1].xRot, 1.42F);
        feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
        for (int i = 0; i < 2; i++) tails[i].xRot = Mth.lerp(k, tails[i].xRot, .55F);
        chest.xRot = Mth.lerp(k, chest.xRot, .32F + breath * .03F);
        chest.yRot *= 1.0F - k;
        head.xRot = Mth.lerp(k, head.xRot, .65F);
        head.yRot *= 1.0F - k;
        neck.yRot *= 1.0F - k;
        uppers[0].xRot = Mth.lerp(k, uppers[0].xRot, -.55F);
        uppers[0].zRot = Mth.lerp(k, uppers[0].zRot, .05F);
        fores[0].xRot = Mth.lerp(k, fores[0].xRot, -.95F);
        // The free hand on the raised knee.
        uppers[1].xRot = Mth.lerp(k, uppers[1].xRot, -.75F);
        uppers[1].zRot = Mth.lerp(k, uppers[1].zRot, -.05F);
        uppers[1].yRot = Mth.lerp(k, uppers[1].yRot, 0.0F);
        fores[1].xRot = Mth.lerp(k, fores[1].xRot, -.2F);
        curl(1, Mth.lerp(k, .45F, .9F));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 256
    return build_rig('circus_true_ringmaster', 'CircusTrueRingmasterModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
