"""The Dagger Thrower's true self (2026-11-17): the figure who paces the throwing gallery over their ring in their
Chamber of Will, as they are in their own mind.

The same hooded figure as the physical Dagger Thrower: black hood and long black coat, the red side cowl, the crimson
waistcoat, red eyes in the dark of the hood. What their true self adds is knives: a bandolier of them across the
chest, a belt of them at the waist, a fan of five in the throwing hand, and bone clasps where the physical coat has
gilt. Long-fingered gloved hands. Cloth and leather are shaded as round forms lit from above (each face darker toward
its edges), never in vertical stripes.

The rig is jointed: hips, knees, ankles; waist, chest, neck, head; shoulders, elbows, wrists, four two-segment
fingers and a two-segment thumb a side. The feet are the root at model y 24. Authored at 1.6 times a block's density
and drawn 2.2 times over (`KnifeOrdealMoves.SCALE`), so a texel is about a seventh of a block. A spectral blade, its
own part beside the body, is drawn by the ghost layer wherever the gesture's blades are.
"""
import math

import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'c0', 'c1', 'c2', 'c3', 'r0', 'r1', 'r2', 'b0', 'b1', 'fe0', 'fe1', 'fe2', 'st0', 'st1', 'st2',
         'g0', 'g1', 'l0', 'l1']
PALETTE = ['#08080b', '#0f1014', '#1b1b22', '#292831', '#3d3b4a', '#450713', '#860f24', '#b51d35', '#bfb398',
           '#e9e1cc', '#2a2528', '#4a4246', '#7a6e72', '#5e6874', '#9ca9b8', '#e1e9ed', '#ff2a2a', '#ff9a7a',
           '#2a1a14', '#46301f']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0'], I['g1']}

# The front of the head, 9 by 10, in the dark of the hood: two red eyes and a pale line of a mouth.
FACE = [
    'kkkkkkkkk',
    'kkdddddkk',
    'kdDDDDDdk',
    'kDDDDDDDk',
    'kDeeDeeDk',
    'kDppDppDk',
    'kDDDmDDDk',
    'kDDmmmDDk',
    'kDDsssDDk',
    'kkDDDDDkk',
]
FACE_KEYS = {'k': 'ink', 'd': 'c1', 'D': 'c0', 'e': 'g1', 'p': 'g0', 'm': 'c2', 's': 'c3'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    def knife(name, parent, pivot, rotation, length=5):
        # A throwing knife: grip and pommel, a bone guard, the blade running out along +y.
        joint(name, parent, pivot, [
            box((-.5, -2.5, -.5), (1, 2.5, 1), 'grip'),
            box((-1, 0, -.5), (2, .5, 1), 'clasp'),
            box((-.5, .5, -.25), (1, length, .5), 'blade')], rotation)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -30, 0), [
        box((-6, -4, -3.5), (12, 5, 7), 'coat'),
        box((-6.5, -2, -4), (13, 2, 8), 'strap')])
    # The belt of knives at the waist, handles up.
    for k, x in enumerate((-4.5, -2.5, 2.5, 4.5)):
        knife('belt_knife_' + str(k), 'pelvis', (x, -1.5, -4.25), (0, 0, (k - 1.5) * 6), 3)
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_thigh', 'pelvis', (sign * 3.25, 0, 0), [box((-2.5, 0, -2.5), (5, 15, 5), 'trousers')])
        joint(side + '_shin', side + '_thigh', (0, 15, 0), [
            box((-2.25, 0, -2.25), (4.5, 12, 4.5), 'boot'),
            box((-2.75, 0, -2.75), (5.5, 2, 5.5), 'boot')])
        joint(side + '_foot', side + '_shin', (0, 12, 0), [box((-2.25, 0, -5), (4.5, 3, 7), 'boot')])
        # The coat's tails, long behind the knees.
        joint(side + '_tail', 'pelvis', (sign * 2.75, 0, 3.25), [box((-3, 0, -.5), (6, 18, 1), 'coat')], (8, 0, -sign * 5))
    joint('chest', 'pelvis', (0, -4, 0), [
        box((-7, -16, -4), (14, 16, 8), 'coat'),
        box((-4.5, -14.5, -4.5), (9, 14, .5), 'waistcoat')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_lapel', 'chest', (sign * 3.5, -15, -4.25), [box((-1, 0, -.5), (2, 10, 1), 'lapel')], (0, 0, -sign * 10))
    joint('clasps', 'chest', (0, -11, -4.75), [
        box((-.5, 0, -.25), (1, 1, .5), 'clasp'),
        box((-.5, 3, -.25), (1, 1, .5), 'clasp'),
        box((-.5, 6, -.25), (1, 1, .5), 'clasp')])
    # The bandolier, right hip to left shoulder, front and back, its knives handle-up along it.
    joint('bandolier', 'chest', (0, -8, -4.75), [box((-1.5, -11, -.5), (3, 22, 1), 'strap')], (0, 0, 38))
    for k in range(5):
        knife('bandolier_knife_' + str(k), 'bandolier', (0, -8 + 4 * k, -.75), (0, 0, 0), 3)
    joint('back_bandolier', 'chest', (0, -8, 4.75), [box((-1.5, -11, -.5), (3, 22, 1), 'strap')], (0, 0, -38))
    joint('neck', 'chest', (0, -16, 0), [box((-2, -3, -2), (4, 3, 4), 'skin')])
    joint('head', 'neck', (0, -3, 0), [box((-4.5, -10, -4.5), (9, 10, 9), 'face')])
    # The hood: a shell open at the front, its point falling behind.
    joint('hood', 'head', (0, 0, 0), [
        box((-5, -11, -5), (10, 2, 10), 'hood'),
        box((-5, -9, -5), (1, 10, 10), 'hood'),
        box((4, -9, -5), (1, 10, 10), 'hood'),
        box((-4, -9, 4), (8, 10, 1), 'hood'),
        box((-4, -9.5, -5.5), (8, 1.5, 1), 'hood')])
    joint('hood_tail', 'hood', (0, -9, 5), [box((-2.5, 0, -1), (5, 8, 2), 'hood')], (22, 0, 0))
    # The red side cowl over their left ear, as the physical one wears it.
    joint('cowl', 'hood', (-4.5, -9.5, 0), [
        box((-2.5, -1, -4), (5, 3, 8), 'cowl'),
        box((-2, 2, -3), (2, 5, 6), 'cowl')], (0, 0, 22))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_shoulder', 'chest', (sign * 7.5, -14, 0), [box((-2.5, -2, -3), (5, 5, 6), 'coat')])
        joint(side + '_upper', side + '_shoulder', (sign * .5, 2, 0), [box((-2, 0, -2), (4, 13, 4), 'coat')])
        joint(side + '_fore', side + '_upper', (0, 13, 0), [
            box((-1.75, 0, -1.75), (3.5, 11, 3.5), 'coat'),
            box((-2.25, 8.5, -2.25), (4.5, 2.5, 4.5), 'cuff')])
        # The palm faces in toward the body; long fingers.
        joint(side + '_hand', side + '_fore', (0, 11, 0), [box((-1.25, 0, -2), (2.5, 4, 4), 'glove')])
        for digit, (z, length) in enumerate(((-1.5, 3), (-.5, 3.5), (.5, 3.25), (1.5, 2.75))):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 4, z), [box((-.5, 0, -.5), (1, length, 1), 'glove')])
            joint(name + '_tip', name, (0, length, 0), [box((-.5, 0, -.5), (1, 2.5, 1), 'glove')])
        joint(side + '_thumb', side + '_hand', (-sign * 1, 1, -1.75), [box((-.5, 0, -.5), (1, 2.5, 1), 'glove')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 2.5, 0), [box((-.5, 0, -.5), (1, 2, 1), 'glove')])
    # The fan of five knives in the throwing hand.
    joint('fan', 'right_hand', (0, 3.5, -.5))
    for k in range(5):
        knife('fan_knife_' + str(k), 'fan', (0, 0, 0), (0, 0, (k - 2) * 16), 6)
    # The spectral blade, beside the body: drawn only by the ghost layer, a block and more of red light.
    joint('spectral_blade', 'mesh.getRoot()', (0, 0, 0), [
        box((-.75, -4, -.75), (1.5, 4, 1.5), 'grip'),
        box((-2.5, 0, -1), (5, 1, 2), 'clasp'),
        box((-1, 1, -.5), (2, 12, 1), 'ghost'),
        box((-.5, 13, -.25), (1, 2, .5), 'ghost')])
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


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material in ('coat', 'trousers', 'skin'):
        return ramp(v * .9, ('ink', 'c0', 'c1', 'c2', 'c3'))
    if material == 'hood':
        return ramp(v * .85, ('ink', 'c0', 'c1', 'c2', 'c3'))
    if material == 'boot':
        if side not in ('up', 'down') and py == 0 and h == 2:
            return I['c3']
        return ramp(v * .8, ('ink', 'c0', 'c1', 'c2'))
    if material == 'glove':
        return ramp(v * .85, ('ink', 'c0', 'c1', 'c2', 'c3'))
    if material in ('waistcoat', 'cuff', 'cowl', 'lapel'):
        return ramp(v, ('r0', 'r0', 'r1', 'r1', 'r2'))
    if material == 'clasp':
        return I['b1'] if side in ('north', 'up') else I['b0']
    if material == 'strap':
        if side not in ('up', 'down') and py % 4 == 0:
            return I['fe1']
        return I['l1'] if v > .5 else I['l0']
    if material == 'grip':
        return I['l1'] if py % 2 == 0 else I['l0']
    if material == 'blade':
        if side == 'north' or side == 'south':
            return I['st2'] if px == 0 else I['st1']
        return I['st0']
    if material == 'ghost':
        # The spectral blade: pale steel with its edges burning.
        if side in ('north', 'south') and (px == 0 or px == w - 1):
            return I['g0']
        if side in ('east', 'west') and py % 3 == 0:
            return I['g1']
        return I['b1'] if side in ('north', 'up') else I['st2']
    if material == 'face':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        return I['ink']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.KnifeOrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_knife_thrower.py; regenerate the model, atlas and BBModel together.
/**
 * The Dagger Thrower's true self: the hooded figure pacing the throwing gallery over their ring, a side-step that
 * crosses the feet, the knife hand loose with its fan, head on the fighter. Each intervention is a gesture
 * ({@link CircusKnifeThrowerEntity#gesture}) timed from its wind-up: the arm gathers slowly back, whips through in a few
 * accelerating ticks on the release, holds its follow-through, recovers. Their real arm throws where they stand; the
 * spectral blades that cross the ring ({@link #renderGhosts}) fly from the hand to the gesture's synced point and the
 * marks the moves work out from it ({@link KnifeOrdealMoves}), and stay in the board until the move is done.
 */
public final class CircusTrueKnifeThrowerModel extends EntityModel<CircusKnifeThrowerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_knife_thrower"), "main");
    private static final int MAX_BLADES = 16;
    private final ModelPart root, pelvis, chest, neck, head, fan, blade;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2],
            shins = new ModelPart[2], feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's spectral blades: where each is (model pixels), the way its point goes, how strongly it shows. */
    private int blades;
    private final Vec3[] bladeAt = new Vec3[MAX_BLADES], bladeDir = new Vec3[MAX_BLADES];
    private final float[] bladeAlpha = new float[MAX_BLADES];

    public CircusTrueKnifeThrowerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        blade = bakedRoot.getChild("spectral_blade");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
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
        fan = hands[0].getChild("fan");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CircusKnifeThrowerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
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
        blades = 0;
        fan.visible = true;
        float breath = Mth.sin(age * .06F);
        chest.xRot += .04F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        // At rest: weight on the left leg, the knife hand low and loose with its fan, the other hand at the waist.
        pelvis.zRot += .04F;
        thighs[0].zRot += .07F;
        thighs[1].zRot -= .02F;
        uppers[0].xRot = -.2F + breath * .02F;
        uppers[0].zRot = .14F;
        fores[0].xRot = -.5F;
        hands[0].yRot = -.4F;
        uppers[1].xRot = .05F;
        uppers[1].zRot = -.22F;
        fores[1].xRot = -1.05F;
        curl(0, .9F);
        curl(1, .45F + .1F * Mth.sin(age * .08F));
        for (int i = 0; i < 2; i++) tails[i].xRot += Mth.sin(age * .05F + i) * .04F;
        // Pacing the gallery: a side-step that crosses the feet, the body dipping on each step.
        float step = Mth.clamp(movement * 5.0F, 0.0F, 1.0F);
        float phase = limbSwing * 1.1F;
        root.y += Math.abs(Mth.sin(phase)) * .8F * step;
        for (int i = 0; i < 2; i++) {
            float swing = Mth.sin(phase + i * Mth.PI);
            thighs[i].zRot += swing * .22F * step;
            shins[i].xRot += Math.max(0.0F, swing) * .45F * step;
            thighs[i].xRot -= Math.max(0.0F, swing) * .3F * step;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            tails[i].zRot -= swing * .08F * step;
        }
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** 0 until 2.5 ticks before {@code at}, then 1 at {@code at}, accelerating like a whip. */
    private static float whip(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, Vec3 target) {
        switch (gesture) {
            case KnifeOrdealMoves.GESTURE_FAN -> fanThrow(since, target);
            case KnifeOrdealMoves.GESTURE_RING -> ringThrow(since, target);
            case KnifeOrdealMoves.GESTURE_PIN -> pinThrow(since, target);
            case KnifeOrdealMoves.GESTURE_BOARD -> boardThrow(since, target);
            default -> {
            }
        }
    }

    /** The fan: the knife arm drawn back over the shoulder, the other thrown out at the mark, then the whip across. */
    private void fanThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.FAN_WINDUP;
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since);
        float tension = HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - whip(since, w));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float snap = whip(since, w);
        float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(w + 8.0F, w + 22.0F, since));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(snap, Mth.lerp(gather, -.4F, -2.7F) + .05F * tremble, -.7F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, Mth.lerp(snap, .5F, -.4F));
        uppers[0].yRot = Mth.lerp(weight, 0.0F, Mth.lerp(snap, .45F * gather, -.45F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(snap, Mth.lerp(gather, -.5F, -1.5F), -.1F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -1.45F);
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, -.1F);
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, -.1F);
        curl(1, Mth.lerp(weight, .45F, .05F));
        chest.yRot += weight * Mth.lerp(snap, .45F * gather, -.35F);
        chest.xRot += weight * Mth.lerp(snap, -.12F * gather, .24F);
        pelvis.yRot += weight * Mth.lerp(snap, .15F * gather, -.12F);
        thighs[1].xRot -= .4F * weight * snap;
        shins[1].xRot += .35F * weight * snap;
        thighs[0].xRot += .3F * weight * snap;
        for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
        // The fan leaves the hand on the whip and is back in it once they recover.
        fan.visible = since < w || since > w + 26.0F;
        if (since < w) return;
        // Five blades down the five lanes, from the hand at chest height across the ring and out past it.
        Vec3 origin = new Vec3(at.x, target.y, at.z);
        Vec3 hand = hand(0);
        double reach = Math.hypot(target.x - origin.x, target.z - origin.z);
        float flown = (since - w) / KnifeOrdealMoves.FLIGHT;
        float alpha = 1.0F - HemoEasing.smoothstep(1.5F, 3.0F, flown);
        if (alpha <= 0.0F) return;
        for (int lane = 0; lane < KnifeOrdealMoves.FAN_LANES; lane++) {
            Vec3 dir = KnifeOrdealMoves.fanDirection(origin, target, lane);
            Vec3 end = model(origin.add(dir.scale(reach)).add(0.0D, 1.2D, 0.0D));
            Vec3 path = end.subtract(hand);
            ghost(hand.add(path.scale(flown)), path, alpha);
        }
    }

    /** The ring: both arms flung up and out to throw the ring of blades high, held wide, then swept in together. */
    private void ringThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.RING_WINDUP;
        float fling = HemoEasing.smoothstep(0.0F, 4.0F, since);
        float close = whip(since, w);
        float tension = HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - close);
        float tremble = Mth.sin(since * 3.1F) * tension;
        float weight = fling * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 22.0F, since));
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(close, -2.3F + .05F * tremble, -1.35F));
            uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, Mth.lerp(close, -sign * 1.05F, sign * .3F));
            fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(close, -.3F, -.2F));
            curl(i, Mth.lerp(weight, .5F, Mth.lerp(close, .02F, 1.3F)));
        }
        chest.xRot += weight * (-.14F * fling * (1.0F - close) + .26F * close);
        head.xRot += .12F * weight * close;
        // The blades fall out of the dark point-first into a ring round the mark, then slide in together.
        float land = KnifeOrdealMoves.FLIGHT + 4.0F;
        float fall = Mth.clamp((since - 4.0F) / (land - 4.0F), 0.0F, 1.0F);
        if (since < 4.0F) return;
        float sliding = Math.max(0.0F, since - w) / KnifeOrdealMoves.RING_CLOSE;
        float alpha = 1.0F - HemoEasing.smoothstep(1.0F, 1.6F, sliding);
        if (alpha <= 0.0F) return;
        double radius = KnifeOrdealMoves.RING_RADIUS * (1.0D - Math.min(1.0D, sliding));
        for (int k = 0; k < KnifeOrdealMoves.RING_BLADES; k++) {
            Vec3 foot = KnifeOrdealMoves.ringBlade(target, k, radius);
            if (sliding <= 0.0F) {
                // Point-down, leaning out a little, falling then standing in the board.
                Vec3 lean = new Vec3(foot.x - target.x, 0.0D, foot.z - target.z).normalize().scale(.25D);
                Vec3 down = model(foot).subtract(model(foot.add(lean.x, 1.0D, lean.z)));
                Vec3 standing = model(foot.add(0.0D, 6.0D * (1.0F - fall * fall), 0.0D));
                ghost(standing.subtract(down.normalize().scale(11.0D)), down, alpha);
            } else {
                // Sliding in along the floor at shin height, points toward the mark.
                Vec3 in = model(target.add(0.0D, .4D, 0.0D)).subtract(model(foot.add(0.0D, .4D, 0.0D)));
                Vec3 shin = model(foot.add(0.0D, .4D, 0.0D));
                ghost(shin.subtract(in.lengthSqr() < 1.0E-4D ? Vec3.ZERO : in.normalize().scale(8.0D)), in, alpha);
            }
        }
    }

    /** The pin: one heavy blade raised high overhead, then hurled down in an arc onto the mark. */
    private void pinThrow(float since, Vec3 target) {
        float w = KnifeOrdealMoves.PIN_WINDUP, release = w - KnifeOrdealMoves.PIN_FLIGHT;
        float raise = HemoEasing.smoothstep(0.0F, release - 2.0F, since);
        float tension = HemoEasing.smoothstep(release - 4.0F, release - 1.0F, since) * (1.0F - whip(since, release));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float snap = whip(since, release);
        float weight = HemoEasing.smoothstep(0.0F, 4.0F, since) * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 20.0F, since));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(snap, Mth.lerp(raise, -.3F, -3.0F) + .05F * tremble, -.55F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, Mth.lerp(snap, .1F, -.2F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(snap, -.3F, -.05F));
        curl(0, Mth.lerp(weight, .9F, Mth.lerp(snap, 1.35F, .1F)));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, -1.1F);
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, -.3F);
        chest.xRot += weight * (-.24F * raise * (1.0F - snap) + .38F * snap);
        thighs[1].xRot -= .35F * weight * snap;
        shins[1].xRot += .3F * weight * snap;
        feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
        fan.visible = false;
        Vec3 hand = hand(0);
        Vec3 mark = model(target);
        float alpha = 1.0F - HemoEasing.smoothstep(w + KnifeOrdealMoves.PIN_STUCK - 6.0F, w + KnifeOrdealMoves.PIN_STUCK, since);
        if (since < release) {
            // The blade manifests in the raised hand, point up.
            ghost(hand, new Vec3(0.0D, -1.0D, 0.0D), HemoEasing.smoothstep(1.0F, 6.0F, since));
            return;
        }
        if (alpha <= 0.0F) return;
        float t = Mth.clamp((since - release) / KnifeOrdealMoves.PIN_FLIGHT, 0.0F, 1.0F);
        if (t < 1.0F) {
            // A high lob: up out of the hand, over, and down point-first.
            Vec3 lift = new Vec3(0.0D, -16.0D * 4.0D * t * (1.0F - t), 0.0D);
            Vec3 now = hand.lerp(mark, t).add(lift);
            Vec3 later = hand.lerp(mark, Math.min(1.0F, t + .1F)).add(new Vec3(0.0D, -16.0D * 4.0D * (t + .1F) * (.9F - t), 0.0D));
            ghost(now, later.subtract(now), alpha);
            return;
        }
        Vec3 down = new Vec3(mark.x - hand.x, 0.0D, mark.z - hand.z).normalize().scale(.4D).add(0.0D, 1.0D, 0.0D);
        ghost(mark.subtract(down.normalize().scale(10.0D)), down, alpha);
    }

    /** The board: the knife arm raised and sighting, then six quick flicks round the outline and a last hard throw. */
    private void boardThrow(float since, Vec3 target) {
        int last = KnifeOrdealMoves.BULLSEYE_AT;
        float ready = HemoEasing.smoothstep(0.0F, 12.0F, since) * (1.0F - HemoEasing.smoothstep(last + 4.0F, last + 20.0F, since));
        float back = 0.0F, snap = 0.0F;
        for (int k = 0; k <= KnifeOrdealMoves.BOARD_KNIVES; k++) {
            float release = (k < KnifeOrdealMoves.BOARD_KNIVES ? KnifeOrdealMoves.boardLanding(k) : last) - KnifeOrdealMoves.FLIGHT;
            float big = k == KnifeOrdealMoves.BOARD_KNIVES ? 1.8F : 1.0F;
            float d = since - release;
            if (d < -3.0F || d > 4.0F) continue;
            back = Math.max(back, big * HemoEasing.smoothstep(-3.0F, -.8F, d) * (1.0F - whip(d, 0.0F)));
            snap = Math.max(snap, big * (d < 0.0F ? whip(d, 0.0F) : 1.0F - HemoEasing.smoothstep(0.0F, 4.0F, d)));
        }
        uppers[0].xRot = Mth.lerp(ready, uppers[0].xRot, -1.75F - .55F * back + .6F * snap);
        uppers[0].zRot = Mth.lerp(ready, uppers[0].zRot, .2F);
        fores[0].xRot = Mth.lerp(ready, fores[0].xRot, -.75F - .6F * back + .55F * snap);
        uppers[1].xRot = Mth.lerp(ready, uppers[1].xRot, -.85F);
        uppers[1].zRot = Mth.lerp(ready, uppers[1].zRot, .15F);
        fores[1].xRot = Mth.lerp(ready, fores[1].xRot, -1.35F);
        chest.yRot += ready * (.2F + .1F * back - .12F * snap);
        chest.xRot += ready * (.06F * snap);
        head.xRot += ready * .1F;
        // Each blade from the hand to its place on the outline, the last to the bullseye; all stay until the act ends.
        Vec3 hand = hand(0);
        float alpha = 1.0F - HemoEasing.smoothstep(last + KnifeOrdealMoves.BOARD_LINGER - 6.0F, last + KnifeOrdealMoves.BOARD_LINGER, since);
        if (alpha <= 0.0F) return;
        for (int k = 0; k <= KnifeOrdealMoves.BOARD_KNIVES; k++) {
            boolean bullseye = k == KnifeOrdealMoves.BOARD_KNIVES;
            float landing = bullseye ? last : KnifeOrdealMoves.boardLanding(k);
            float t = (since - (landing - KnifeOrdealMoves.FLIGHT)) / KnifeOrdealMoves.FLIGHT;
            if (t < 0.0F) continue;
            Vec3 spot = model(bullseye ? target : KnifeOrdealMoves.boardSpot(target, k));
            if (t < 1.0F) {
                ghost(hand.lerp(spot, t * t), spot.subtract(hand), alpha);
            } else {
                Vec3 down = new Vec3(spot.x - hand.x, 0.0D, spot.z - hand.z).normalize().scale(.5D).add(0.0D, 1.0D, 0.0D);
                ghost(spot.subtract(down.normalize().scale(10.0D)), down, alpha);
            }
        }
    }

    /** A hand's palm this frame, in model pixels. */
    private Vec3 hand(int i) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : new ModelPart[]{root, pelvis, chest, shoulders[i], uppers[i], fores[i], hands[i]})
            part.translateAndRotate(pose);
        Vector3f palm = pose.last().pose().transformPosition(new Vector3f(0.0F, 4.0F / 16.0F, 0.0F)).mul(16.0F);
        return new Vec3(palm.x, palm.y, palm.z);
    }

    private void ghost(Vec3 position, Vec3 direction, float alpha) {
        if (blades >= MAX_BLADES || direction.lengthSqr() < 1.0E-6D) return;
        bladeAt[blades] = position;
        bladeDir[blades] = direction.normalize();
        bladeAlpha[blades] = Mth.clamp(alpha, 0.0F, 1.0F);
        blades++;
    }

    /** Draws this frame's spectral blades, each its own part laid along its flight with its point leading. */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < blades; i++) {
            if (bladeAlpha[i] <= .01F) continue;
            Vec3 dir = bladeDir[i], p = bladeAt[i];
            blade.setPos((float) p.x, (float) p.y, (float) p.z);
            blade.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            int alpha = (int) (bladeAlpha[i] * ((color >>> 24) & 255));
            blade.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / KnifeOrdealMoves.SCALE);
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
    return build_rig('circus_true_knife_thrower', 'CircusTrueKnifeThrowerModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
