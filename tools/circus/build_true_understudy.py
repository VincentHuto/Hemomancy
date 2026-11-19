"""The Retired Understudy's true self (2026-11-17): the Understudy as they are in their own mind, keeping the prompt
corner in the wings of their stage in their Chamber of Will.

The same person as the physical Understudy (grey hair, the harlequin diamonds, the knitted shawl, the cane, the
half-mask), but no longer stooped and no longer faded: standing straight and tall, the diamonds bright crimson and
bone, the shawl grown into a heavy crimson stage mantle with a knitted collar and a bone fringe, the cane grown into a
prompter's staff of blood wood banded in hematic iron with a cue light caged at its head, the long grey hair tied back,
and the half-mask worn down over the face for good: in here they play every part. The mask's eyes are lit red, and so
is the cue light. Skin and cloth are shaded as round forms lit from above, never in columns.

The rig is jointed: hips, torso, neck, head; shoulder, upper arm, forearm (elbow), hand (wrist), four two-segment
fingers and a thumb a side; thigh, shin (knee) and foot. The feet are the root at model y 24, the entity's own feet.
Authored at twice the density of a 16-pixel block (62 pixels tall) and drawn 1.65 times over
(`UnderstudyChoreography.SCALE`), about six and a half blocks tall. The same rig, posed on its own and drawn small and
translucent, is the Understudy's double that steps into the ring.
"""
import math

import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 's0', 's1', 's2', 's3', 's4', 'h0', 'h1', 'h2', 'r0', 'r1', 'r2', 'r3', 'b0', 'b1', 'b2',
         'k0', 'k1', 'k2', 'e0', 'e1', 'w0', 'w1', 'fe0', 'fe1', 'fe2', 'g0', 'g1']
PALETTE = ['#140d10', '#7c5a4c', '#9a7563', '#b8917c', '#cfab94', '#e2c4ae',  # skin
           '#6d6a72', '#a6a3a8', '#d4d2d6',  # silver hair
           '#3a0c16', '#5e1420', '#8e1f30', '#b83242',  # crimson
           '#9a8e76', '#cdbf9e', '#ece2c6',  # bone
           '#5d574e', '#7d766a', '#9e968a',  # knitted collar
           '#231c26', '#3a2f3c',  # tights
           '#2a1a14', '#46301f',  # blood wood
           '#2a2528', '#4a4246', '#7a6e72',  # hematic iron
           '#ff4a3a', '#ffe2c8']  # glow: crimson and the cue light's core
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0'], I['g1']}

# The half-mask over the upper face, 9 by 5: bone, its eyes cut and lit red, a crimson tear under the left eye.
MASK = [
    'bBBBBBBBb',
    'BkgkBkgkB',
    'BbbbBbbbB',
    'bBBbBbRBb',
    '.bBBBBRb.',
]
MASK_KEYS = {'B': 'b2', 'b': 'b1', 'k': 'ink', 'g': 'g0', 'R': 'r3', '.': 'b0'}
# The face below and round the mask, 10 by 11: hair at the brow, the mask's band over the eyes, a lined mouth and jaw.
FACE = [
    'HHHHHHHHHH',
    'HhhhhhhhhH',
    'Hsssssssss',
    'msssssssmm',
    'mssssssssm',
    'mssssssssm',
    'msssssSssm',
    'msssnnsssm',
    'mssdkkdssm',
    'mdsssssdsm',
    'ddmmmmmmdd',
]
FACE_KEYS = {'H': 'h1', 'h': 'h2', 's': 's3', 'S': 's4', 'm': 's2', 'd': 's1', 'n': 's2', 'k': 'r0'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('hips', 'root', (0, -26, 0), [box((-6, -3, -3.5), (12, 5, 7), 'costume')])
    joint('torso', 'hips', (0, -2, 0), [
        box((-6, -10, -3.5), (12, 9, 7), 'costume'),
        box((-7, -20, -4), (14, 10, 8), 'costume')])
    # The shawl grown into a stage mantle: a heavy knitted collar and the long crimson drape down the back.
    joint('collar', 'torso', (0, -19, 0), [box((-8.5, -1.5, -4.75), (17, 3, 9.5), 'knit')])
    joint('mantle', 'torso', (0, -19, 4.25), [box((-8, 0, 0), (16, 40, 1.5), 'mantle')], (6, 0, 0))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_drape', 'torso', (sign * 7.5, -18.5, 1), [box((-1, 0, -2.5), (2, 22, 5), 'mantle')], (0, 0, sign * 4))
    joint('neck', 'torso', (0, -20, 0), [box((-2, -3, -2), (4, 3, 4), 'skin')])
    joint('head', 'neck', (0, -3, 0), [box((-5, -11, -5), (10, 11, 10), 'head')])
    joint('mask', 'head', (0, -6, -5), [box((-4.5, -2.5, -1), (9, 5, 1), 'mask')])
    # Long grey hair tied back at the nape and hanging down between the shoulders.
    joint('hair_knot', 'head', (0, -6, 5), [box((-2, -2, -.5), (4, 4, 2), 'hair')])
    joint('hair_tail', 'hair_knot', (0, 2, .5), [box((-1.5, 0, -.75), (3, 12, 1.5), 'hair')], (8, 0, 0))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_shoulder', 'torso', (sign * 8, -18, 0), [box((-2.5, -2, -2.5), (5, 4, 5), 'costume')])
        joint(side + '_upper', side + '_shoulder', (0, 1, 0), [box((-2, 0, -2), (4, 10, 4), 'costume')])
        joint(side + '_fore', side + '_upper', (0, 10, 0), [
            box((-1.75, 0, -1.75), (3.5, 8, 3.5), 'costume'),
            box((-2.25, 6.5, -2.25), (4.5, 1.5, 4.5), 'ruff')])
        joint(side + '_hand', side + '_fore', (0, 8, 0), [box((-1.25, 0, -1.75), (2.5, 3, 3.5), 'skin')])
        for digit, (z, length) in enumerate(((-1.25, 1.75), (-.5, 2.25), (.25, 2), (1, 1.5))):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 3, z), [box((-.75, 0, -.25), (1.5, length, .75), 'skin')])
            joint(name + '_tip', name, (0, length, 0), [box((-.5, 0, -.25), (1, 1.5, .75), 'skin')])
        joint(side + '_thumb', side + '_hand', (-sign * 1, 1, -1.75), [box((-.5, 0, -.5), (1, 2, 1), 'skin')],
              (-25, 0, -sign * 25))
        joint(side + '_thumb_tip', side + '_thumb', (0, 2, 0), [box((-.25, 0, -.25), (.75, 1.5, .75), 'skin')])
        joint(side + '_thigh', 'hips', (sign * 3, 1, 0), [box((-2.25, 0, -2.25), (4.5, 12, 4.5), 'tights')])
        joint(side + '_shin', side + '_thigh', (0, 12, 0), [box((-2, 0, -2), (4, 11, 4), 'tights')])
        joint(side + '_foot', side + '_shin', (0, 11, 0), [box((-2, 0, -4), (4, 2, 6), 'slipper')])
    # The cane grown into the prompter's staff, planted from the left hand, the cue light caged at its head.
    joint('staff', 'left_hand', (0, 2, 0), [
        box((-.75, -34, -.75), (1.5, 58, 1.5), 'staff'),
        box((-1, -8, -1), (2, 1.5, 2), 'iron'),
        box((-1, 6, -1), (2, 1.5, 2), 'iron')])
    joint('lamp', 'staff', (0, -34, 0), [
        box((-2, -5, -2), (4, 1, 4), 'iron'),
        box((-1.25, -4, -1.25), (2.5, 3, 2.5), 'light'),
        box((-2, -1, -2), (4, 1, 4), 'iron'),
        box((-.5, -6.5, -.5), (1, 1.5, 1), 'iron')])
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


def ramp(v, names, limits):
    for limit, name in zip(limits, names):
        if v < limit:
            return I[name]
    return I[names[-1]]


def skin(v):
    return ramp(v, ('s0', 's1', 's2', 's3', 's4'), (.24, .4, .57, .75))


def crimson(v):
    return ramp(v, ('r0', 'r1', 'r2', 'r3'), (.3, .5, .72))


def bone(v):
    return ramp(v, ('b0', 'b1', 'b2'), (.38, .66))


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material == 'skin':
        return skin(v)
    if material == 'costume':
        # Harlequin diamonds, bright again: crimson and bone, each lit as part of the round form it lies on.
        u, t = (px + .5) / 3, (py + .5) / 4.5
        if side in ('up', 'down'):
            return crimson(v - .1)
        lattice = (math.floor(u + t) + math.floor(u - t)) % 2 == 0
        return bone(v) if lattice else crimson(v)
    if material == 'ruff':
        return bone(v + .1) if (px + py) % 2 == 0 else bone(v - .15)
    if material == 'knit':
        # A heavy knit: rows of stitches, lit from above.
        stitch = .08 if (px + py // 2) % 2 == 0 else -.08
        return ramp(v + stitch, ('k0', 'k1', 'k2'), (.4, .64))
    if material == 'mantle':
        # Crimson velvet in soft folds, a bone fringe at its hem.
        if side not in ('up', 'down') and py >= h - 2:
            return I['b2'] if px % 2 == 0 and py == h - 1 else I['b1']
        fold = .1 * math.sin(px * 1.3 + (gx // 7))
        return crimson(v + fold - .08)
    if material == 'head':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if side == 'up':
            # Silver hair drawn back hard from the brow.
            return I['h2'] if (px + gy) % 3 == 0 else I['h1']
        if side in ('east', 'west'):
            front = w - 1 - px if side == 'east' else px
            if py >= 4 and 2 <= front <= 6 and py < h - 1:
                return skin(v + .05) if not (py in (5, 6) and front in (3, 4)) else I['s1']
            return I['h1'] if noise(gx, gy, 3) > .3 else I['h0']
        if side == 'south':
            return I['h1'] if noise(gx, gy, 4) > .4 else I['h0']
        return I['s1']
    if material == 'mask':
        if side == 'north':
            return I[MASK_KEYS[MASK[py][px]]]
        return I['b1'] if side == 'up' else I['b0']
    if material == 'hair':
        if side == 'down':
            return I['h0']
        return I['h2'] if (px + py) % 4 == 0 and v > .5 else I['h1'] if v > .35 else I['h0']
    if material == 'tights':
        return ramp(v, ('e0', 'e1'), (.5,))
    if material == 'slipper':
        return crimson(v - .05)
    if material == 'staff':
        if side == 'up':
            return I['w1']
        return I['w1'] if v > .55 and noise(gx, gy, 5) > .3 else I['w0']
    if material == 'iron':
        if side == 'up' or py == 0:
            return I['fe2']
        if side == 'down' or (py == h - 1 and h > 1):
            return I['fe0']
        return I['fe1']
    if material == 'light':
        return I['g1'] if side in ('north', 'south', 'east', 'west') and 0 < py < h - 1 else I['g0']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.UnderstudyChoreography;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_understudy.py; regenerate the model, atlas and BBModel together.
/**
 * The Retired Understudy's true self: standing straight in the prompt corner of their stage, the mask down, the
 * mantle hanging, the prompter's staff planted at their side with its cue light lit, their head following the fighter.
 * Each intervention is a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up: the staff raised and the
 * light aimed (the cue, then the lock and the thrust at the burn), both hands hauling the fly rope down for the curtain,
 * a hand conducting through the reprise, and the staff's heel knocked three times on the boards for the trapdoors. Their
 * double ({@link #renderDouble}) is this same rig posed on its own, drawn life-sized in red light at the gesture's
 * synced point: stepping onto each reprise mark to strike it, and rising through the trapdoor under the fighter.
 */
public final class CircusTrueUnderstudyModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_understudy"), "main");
    private final ModelPart root, hips, torso, neck, head, mantle, hairTail, staff;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2], drapes = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final List<ModelPart> parts = new ArrayList<>();
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's double: how strongly it shows, where its feet stand (model pixels), how high it has risen, its pose. */
    private float doubleWeight, doubleRise;
    private Vec3 doubleAt = Vec3.ZERO;
    private int doubleGesture = -1;
    private float doubleSince;

    public CircusTrueUnderstudyModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        torso = hips.getChild("torso");
        mantle = torso.getChild("mantle");
        neck = torso.getChild("neck");
        head = neck.getChild("head");
        hairTail = head.getChild("hair_knot").getChild("hair_tail");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            drapes[i] = torso.getChild(side + "_drape");
            shoulders[i] = torso.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
        staff = hands[1].getChild("staff");
        root.getAllParts().forEach(parts::add);
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
        int gesture = entity.gesture();
        float since = entity.gestureAge(partialTick);
        doubleWeight = 0.0F;
        if (gesture == UnderstudyChoreography.GESTURE_BEAT || gesture == UnderstudyChoreography.GESTURE_KNOCK) {
            doubleGesture = gesture;
            doubleSince = since;
            doubleAt = model(entity.gestureAt());
            if (gesture == UnderstudyChoreography.GESTURE_BEAT) {
                doubleWeight = HemoEasing.smoothstep(0.0F, 2.0F, since) * (1.0F - HemoEasing.smoothstep(6.0F, 10.0F, since));
                doubleRise = 1.0F;
            } else {
                float knock = UnderstudyChoreography.KNOCKS[2] + 2.0F;
                doubleWeight = HemoEasing.smoothstep(10.0F, 14.0F, since) * (1.0F - HemoEasing.smoothstep(knock + 4.0F, knock + 12.0F, since));
                doubleRise = HemoEasing.smoothstep(12.0F, knock, since);
            }
        }
        pose(age, limbSwing, limbSwingAmount, yaw, pitch, gesture, since);
    }

    private void reset() {
        for (ModelPart part : parts) {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        }
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float limbSwing, float limbSwingAmount, float yaw, float pitch, int gesture, float since) {
        reset();
        float breath = Mth.sin(age * .06F);
        float walk = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
        float stride = limbSwing * .6662F;
        // Standing straight, chin up: no stoop in here.
        torso.xRot -= .03F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * .45F * walk;
            thighs[i].xRot = swing;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.2F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            shoulders[i].y -= breath * .25F;
            uppers[i].zRot = -sign * .1F;
            curl(i, .45F);
        }
        hips.y += Math.abs(Mth.sin(stride)) * -.6F * walk;
        // The right hand held a little out from the body, the fingers moving as if over lines; the staff planted.
        uppers[0].xRot = -.25F + Mth.sin(age * .05F) * .04F;
        fores[0].xRot = -.75F;
        hands[0].yRot = -.4F;
        curl(0, .35F + .2F * Mth.sin(age * .17F));
        uppers[1].xRot = -.3F;
        uppers[1].zRot = .12F;
        fores[1].xRot = -.25F;
        staff.xRot = .55F;
        curl(1, 1.2F);
        mantle.xRot += breath * .02F + walk * .15F;
        hairTail.xRot += breath * .02F + walk * .2F;
        for (int i = 0; i < 2; i++) drapes[i].xRot += walk * .08F;
        if (gesture >= 0) gesture(gesture, since);
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 4; f++) {
            fingers[i][f].xRot = amount * (1.0F + f * .08F);
            tips[i][f].xRot = amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .5F;
    }

    /** The beats of a blow: a slow gather, a fast drop of a few ticks onto the impact, a hit-stop, a heavy recovery. */
    private static float drop(float since, float impact) {
        float t = Mth.clamp((since - (impact - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private static float held(float since, float impact, float hold, float recover) {
        return 1.0F - HemoEasing.smoothstep(impact + hold, impact + hold + recover, since);
    }

    private void gesture(int gesture, float since) {
        switch (gesture) {
            case UnderstudyChoreography.GESTURE_CUE -> {
                // The staff lifted high and the cue light swung out over the ring, hunting with the light.
                float raise = HemoEasing.smoothstep(0.0F, 8.0F, since);
                staffRaised(raise, Mth.sin(since * .35F) * .12F * raise);
                torso.xRot += .08F * raise;
            }
            case UnderstudyChoreography.GESTURE_LOCK -> {
                // Held dead still on the settled light, trembling, then driven down at it for the burn.
                float impact = UnderstudyChoreography.LOCK;
                float tension = HemoEasing.smoothstep(impact - 6.0F, impact - 1.0F, since) * (1.0F - drop(since, impact));
                float down = drop(since, impact) * held(since, impact, 6.0F, 16.0F);
                float raise = held(since, impact, 6.0F, 16.0F);
                staffRaised(raise, Mth.sin(since * 3.1F) * .04F * tension - .5F * down);
                torso.xRot += .08F * raise + .22F * down;
                head.xRot += .12F * down;
                // The free hand thrown out after it.
                uppers[0].xRot = Mth.lerp(down, uppers[0].xRot, -1.6F);
                uppers[0].zRot = Mth.lerp(down, uppers[0].zRot, .5F);
                curl(0, Mth.lerp(down, .35F, -.2F));
            }
            case UnderstudyChoreography.GESTURE_CURTAIN -> {
                // Both hands reach up for the fly rope over the right shoulder, gather, then haul it down hand over hand.
                float impact = 24.0F;
                float reach = HemoEasing.smoothstep(0.0F, 14.0F, since);
                float tension = HemoEasing.smoothstep(impact - 5.0F, impact - 1.0F, since) * (1.0F - drop(since, impact));
                float haul = drop(since, impact);
                float hold = reach * held(since, impact, 40.0F, 14.0F);
                float hand = Math.max(0.0F, Mth.sin((since - impact) * .4F)) * haul;
                for (int i = 0; i < 2; i++) {
                    float top = -2.8F + (i == 0 ? .05F : -.1F) + Mth.sin(since * 3.1F) * .05F * tension;
                    float hauled = -1.1F - (i == 0 ? hand : 1.0F - hand) * .9F;
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(haul, top, hauled));
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, i == 0 ? .25F : -.45F);
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, -.3F);
                    curl(i, Mth.lerp(hold, .45F, 1.5F));
                }
                staff.xRot = Mth.lerp(hold, staff.xRot, 1.4F);
                torso.xRot += hold * (-.1F * (1.0F - haul) + .25F * haul);
                torso.yRot += hold * -.25F;
                hips.y += hold * haul * 1.5F;
                for (int i = 0; i < 2; i++) {
                    thighs[i].xRot -= hold * haul * .35F;
                    shins[i].xRot += hold * haul * .7F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
            }
            case UnderstudyChoreography.GESTURE_REPRISE -> {
                // The head bowed over the prompt, the free hand conducting each step as it is learned.
                float hold = HemoEasing.smoothstep(0.0F, 6.0F, since);
                float beat = (since % UnderstudyChoreography.BEAT) / UnderstudyChoreography.BEAT;
                float stroke = beat < .3F ? beat / .3F : 1.0F - (beat - .3F) / .7F;
                head.xRot += .3F * hold;
                uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, -1.5F + .5F * stroke);
                uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, .3F);
                fores[0].xRot = Mth.lerp(hold, fores[0].xRot, -.6F - .4F * stroke);
                curl(0, Mth.lerp(hold, .35F, .1F));
            }
            case UnderstudyChoreography.GESTURE_BEAT -> {
                // Each beat of the reprise given its downbeat, as the double strikes the mark.
                float impact = UnderstudyChoreography.LEAD;
                float lift = HemoEasing.smoothstep(0.0F, impact - 2.0F, since) * (1.0F - drop(since, impact));
                float down = drop(since, impact) * held(since, impact, 1.0F, 5.0F);
                head.xRot += .3F;
                uppers[0].xRot = -1.5F - .8F * lift + 1.2F * down;
                uppers[0].zRot = .3F;
                fores[0].xRot = -.6F - .5F * lift;
                torso.xRot += .1F * down;
                curl(0, .1F + 1.2F * down);
            }
            case UnderstudyChoreography.GESTURE_KNOCK -> {
                // The staff's heel lifted and knocked on the boards three times; then both arms flung wide as the doors drop.
                float lift = 0.0F;
                for (int knock : UnderstudyChoreography.KNOCKS) {
                    float before = HemoEasing.smoothstep(knock - 6.0F, knock - 2.0F, since);
                    lift = Math.max(lift, before * (1.0F - drop(since, knock)) * (since < knock + 1.0F ? 1.0F : 0.0F));
                }
                float drop = UnderstudyChoreography.KNOCKS[2] + 2.0F;
                float wide = HemoEasing.smoothstep(drop - 1.5F, drop, since) * held(since, drop, 8.0F, 14.0F);
                staff.xRot = Mth.lerp(Math.max(lift, wide * .5F), staff.xRot, .1F);
                uppers[1].xRot = Mth.lerp(lift, uppers[1].xRot, -1.0F);
                fores[1].xRot = Mth.lerp(lift, fores[1].xRot, -.9F);
                hips.y += lift * -.4F;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    if (i == 0) {
                        uppers[i].xRot = Mth.lerp(wide, uppers[i].xRot, -1.2F);
                        uppers[i].zRot = Mth.lerp(wide, uppers[i].zRot, -sign * 1.3F);
                        fores[i].xRot = Mth.lerp(wide, fores[i].xRot, -.1F);
                        curl(i, Mth.lerp(wide, .35F, -.25F));
                    }
                }
                torso.xRot += .12F * wide;
            }
            default -> {
            }
        }
    }

    /** The left arm lifting the staff overhead and out over the ring, the cue light at its head; {@code aim} tips it. */
    private void staffRaised(float raise, float aim) {
        uppers[1].xRot = Mth.lerp(raise, uppers[1].xRot, -2.75F + aim);
        uppers[1].zRot = Mth.lerp(raise, uppers[1].zRot, -.15F);
        fores[1].xRot = Mth.lerp(raise, fores[1].xRot, -.2F);
        staff.xRot = Mth.lerp(raise, staff.xRot, 1.25F);
        curl(1, 1.3F);
    }

    /** The double's own pose: stepping in and striking the mark, or rising through the trapdoor and stamping. */
    private void doublePose() {
        reset();
        float since = doubleSince;
        if (doubleGesture == UnderstudyChoreography.GESTURE_BEAT) {
            float impact = UnderstudyChoreography.LEAD;
            float lift = HemoEasing.smoothstep(0.0F, impact - 1.5F, since) * (1.0F - drop(since, impact));
            float down = drop(since, impact) * held(since, impact, 2.0F, 6.0F);
            torso.xRot += .25F * lift - .1F + .45F * down;
            uppers[0].xRot = -2.9F * lift - .9F * down;
            fores[0].xRot = -.4F * lift - .2F * down;
            uppers[1].xRot = -.6F * lift + .8F * down;
            uppers[1].zRot = .5F;
            thighs[0].xRot = -.7F * Math.max(lift, down);
            shins[0].xRot = .4F * Math.max(lift, down);
            thighs[1].xRot = .4F * Math.max(lift, down);
            hips.y += 2.5F * down;
            for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            curl(0, 1.4F);
            curl(1, .2F);
        } else {
            float stamp = UnderstudyChoreography.KNOCKS[2] + 2.0F;
            float raise = HemoEasing.smoothstep(stamp - 6.0F, stamp - 2.0F, since) * (1.0F - drop(since, stamp));
            thighs[0].xRot = -1.1F * raise;
            shins[0].xRot = 1.1F * raise;
            feet[0].xRot = -thighs[0].xRot - shins[0].xRot;
            for (int i = 0; i < 2; i++) {
                float sign = i == 0 ? -1 : 1;
                uppers[i].xRot = -1.0F - .6F * raise;
                uppers[i].zRot = -sign * .9F;
                curl(i, -.2F);
            }
            head.xRot -= .2F;
        }
        staff.visible = false;
        mantle.xRot += .3F;
    }

    /** The double's size against the true self's: a performer's own height on the boards. */
    private static final float DOUBLE_SIZE = 2.4F / (62.0F / 16.0F * UnderstudyChoreography.SCALE);

    /** Draws the double in red light at this frame's synced point, faced out to the house; the true self is untouched. */
    public void renderDouble(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (doubleWeight <= .01F) return;
        List<PartPose> saved = new ArrayList<>(parts.size());
        for (ModelPart part : parts) saved.add(part.storePose());
        boolean staffVisible = staff.visible;
        doublePose();
        pose.pushPose();
        // From the floor under the mark: the rise lifts it up through the boards.
        pose.translate(doubleAt.x / 16.0F, doubleAt.y / 16.0F + (1.0F - doubleRise) * 2.4F / UnderstudyChoreography.SCALE, doubleAt.z / 16.0F);
        pose.mulPose(Axis.YP.rotation((float) Math.atan2(-doubleAt.x, -doubleAt.z)));
        pose.scale(DOUBLE_SIZE, DOUBLE_SIZE, DOUBLE_SIZE);
        pose.translate(0.0F, -1.5F, 0.0F);
        int alpha = (int) (Mth.clamp(doubleWeight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        root.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        pose.popPose();
        for (int i = 0; i < parts.size(); i++) parts.get(i).loadPose(saved.get(i));
        staff.visible = staffVisible;
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip and lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / UnderstudyChoreography.SCALE);
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
    return build_rig('circus_true_understudy', 'CircusTrueUnderstudyModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
