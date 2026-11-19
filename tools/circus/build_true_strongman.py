"""The Strongman's true self (2026-11-17, second build): the colossus who wades round his ring in his Chamber of Will,
seen from the waist up. It replaces the first true self (a scaled humanoid with no elbows, hands or neck, its muscle
painted in vertical seams that read as licorice).

The same man as the physical Strongman, made monumental: bald, a heavy brow, the handlebar moustache, the striped
one-shoulder leotard and the wide belt. What his true self adds is iron: hematic iron grown out of him at the
shoulders, hooped round the forearms like plates threaded on a bar and plated over the backs of his hands, with his
blood seeping red where iron meets flesh, and pupils lit red. Skin is shaded as round forms (light from above, each
face darkening toward its edges), never in columns.

The rig is jointed: waist, chest, neck, head; shoulder, upper arm, forearm (elbow), hand (wrist), four two-segment
fingers and a two-segment thumb a side. The waist is the root at model y 24, the entity's own feet, so the ordeal
sinks the entity to put his waist at the apron. Authored at 1.75 times the density of a 16-pixel block for the scale
it is drawn at (see `OrdealFlight.SCALE`).
"""
import math

import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'h0', 'h1', 's0', 's1', 's2', 's3', 's4', 'st0', 'st1', 'st2', 'b0', 'b1',
         'fe0', 'fe1', 'fe2', 'v0', 'v1', 'g0', 'l0', 'l1']
PALETTE = ['#140d0f', '#1e1414', '#3a261e', '#5a3626', '#7c523c', '#a46e50', '#c48b68', '#dcaa86',
           '#5e1420', '#962637', '#b8394a', '#bfb398', '#e9e1cc',
           '#2a2528', '#4a4246', '#7a6e72', '#4a0c16', '#7e1a28', '#ff4a3a', '#2a1a14', '#46301f']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0']}

# The front of the head, 14 by 14 (the brow, nose and moustache are their own boxes over it).
FACE = [
    'ssssssssssssss',
    'sssSSSSSSSSsss',
    'ssSSSSSSSSSSss',
    'mssssssssssssm',
    'mhhhhhsshhhhhm',
    'mDDDDmssmDDDDm',
    'mDeepDmmDpeeDm',
    'mdDDdmssmdDDdm',
    'msmdssSSssdmsm',
    'mssssdSSdssssm',
    'mssddDnnDddssm',
    'mdhhhhhhhhhhdm',
    'mdDkkkkkkkkDdm',
    'dddmmmmmmmmddd',
]
FACE_KEYS = {'S': 's4', 's': 's3', 'm': 's2', 'd': 's1', 'D': 's0', 'h': 'h0', 'e': 'b1', 'p': 'g0', 'n': 's0',
             'k': 'ink'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('belly', 'root', (0, 0, 0), [
        box((-13, -15, -8), (26, 15, 16), 'leotard'),
        box((-14, -4, -9), (28, 4, 18), 'belt')])
    joint('buckle', 'belly', (0, -2, -9), [
        box((-4, -2.5, -1), (8, 5, 1), 'iron'),
        box((-2, -1.5, -1.5), (4, 3, .5), 'bone')])
    joint('chest', 'belly', (0, -15, 0), [
        box((-16, -16, -9), (32, 16, 18), 'torso'),
        box((-18, -20, -8), (36, 4, 15), 'skin')])
    # The one-shoulder leotard's strap, front and back, from his right hip up over his left shoulder.
    joint('strap', 'chest', (2, -6, -12.25), [box((-2.5, -18, -.5), (5, 28, 1), 'leotard')], (0, 0, 35))
    joint('back_strap', 'chest', (2, -6, 9.25), [box((-2.5, -18, -.5), (5, 28, 1), 'leotard')], (0, 0, 35))
    # A short, thick neck: the head sits down between the traps.
    joint('neck', 'chest', (0, -21, -1), [box((-6, -4, -6), (12, 5, 12), 'skin')])
    joint('head', 'neck', (0, -3, -1), [box((-7, -14, -7), (14, 14, 14), 'head')])
    joint('brow', 'head', (0, -10, -7), [box((-7.25, -1, -1.5), (14.5, 2, 1.5), 'brow')])
    joint('nose', 'head', (0, -6, -7), [box((-1.5, -2.5, -1.75), (3, 5, 1.75), 'nose')])
    joint('moustache', 'head', (0, -3.5, -7), [
        box((-5, -1, -1.5), (10, 2, 1.5), 'moustache'),
        box((5, -1.5, -1.5), (3, 1.5, 1.5), 'moustache'),
        box((-8, -1.5, -1.5), (3, 1.5, 1.5), 'moustache'),
        box((7, -4, -1.5), (1.5, 2.5, 1.5), 'moustache'),
        box((-8.5, -4, -1.5), (1.5, 2.5, 1.5), 'moustache')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_ear', 'head', (sign * 7, -7, 0), [box((0 if sign > 0 else -1, -2.5, -1.5), (1, 5, 3), 'skin')])
        joint(side + '_pec', 'chest', (sign * 8, -11, -9), [box((-8, -5, -3), (16, 10, 3), 'pec')])
        joint(side + '_lat', 'chest', (sign * 15, -8, 1), [box((-2, -7, -6), (4, 14, 11), 'skin')], (0, 0, sign * 8))
        joint(side + '_trap', 'chest', (sign * 5, -20, 0), [box((-6, -4, -6), (12, 5, 12), 'skin')], (0, 0, sign * 20))
        joint(side + '_shoulder', 'chest', (sign * 18, -17, 0), [box((-6, -5, -6), (12, 11, 12), 'arm')])
        # The iron grown out of him: a pauldron over each shoulder, hoops round the forearms, plate on the hands.
        joint(side + '_pauldron', side + '_shoulder', (sign * 1, -4, 0), [box((-7, -3, -7), (14, 5, 14), 'iron')],
              (0, 0, sign * 18))
        joint(side + '_upper', side + '_shoulder', (sign * 1, 4, 0), [
            box((-5, 0, -5), (10, 15, 10), 'arm'),
            box((-4, 2, -6.5), (8, 9, 1.5), 'arm')])
        joint(side + '_fore', side + '_upper', (0, 15, 0), [
            box((-5.5, 0, -5.5), (11, 13, 11), 'arm'),
            box((-6, 2, -6), (12, 2, 12), 'hoop'),
            box((-6, 6.5, -6), (12, 2, 12), 'hoop'),
            box((-5, 10.5, -5), (10, 3, 10), 'wrap')])
        # The palm faces in toward the body; the back of the hand, plated, faces out.
        joint(side + '_hand', side + '_fore', (0, 13.5, 0), [
            box((-2.5, 0, -4.5), (5, 7, 9), 'hand'),
            box((2.5 if sign > 0 else -3.5, 1, -4), (1, 5, 8), 'iron')])
        for digit, (z, length) in enumerate(((-4.25, 4), (-2, 4.75), (.25, 4.5), (2.5, 3.75))):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 7, z + 1), [box((-2, 0, -1), (4, length, 2), 'hand')])
            joint(name + '_tip', name, (0, length, 0), [box((-1.75, 0, -1), (3.5, 3.25, 2), 'hand')])
        joint(side + '_thumb', side + '_hand', (-sign * 2, 2, -4.5), [box((-1.25, 0, -1.25), (2.5, 4, 2.5), 'hand')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 4, 0), [box((-1, 0, -1), (2, 3, 2), 'hand')])
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


def skin(v):
    for limit, name in ((.24, 's0'), (.4, 's1'), (.57, 's2'), (.75, 's3')):
        if v < limit:
            return I[name]
    return I['s4']


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material == 'skin':
        return skin(v)
    if material == 'pec':
        if side == 'north' and py == h - 1:
            return I['s0']
        if side == 'north' and py == 0:
            return I['s4']
        return skin(v + .05)
    if material in ('arm', 'hand'):
        # The arms he reaches into the ring with burn: blood lit red in a net of veins under flushed skin.
        if side not in ('up', 'down') and h > 3:
            for k in range(3 if w > 6 else 1):
                column = int(w * (.2 + .3 * k) + w * .12 * math.sin(py * .5 + k * 2.1 + (gx // 16) * 1.7))
                if px == column and noise(gx, gy, 3 + k) > .1:
                    return I['g0']
                if abs(px - column) == 1 and noise(gx, gy, 6 + k) > .45:
                    return I['v1']
        if material == 'hand' and side not in ('up', 'down') and py == 0:
            return I['g0'] if px % 2 == 0 else I['v1']
        flushed = skin(v)
        return I['v0'] if flushed == I['s0'] else I['v1'] if flushed == I['s1'] and noise(gx, gy, 9) > .5 else flushed
    if material == 'head':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if side == 'up':
            # A bald crown, a sheen toward the front.
            return I['s4'] if py < h // 2 and noise(gx, gy, 5) > .35 else skin(.7 + (noise(gx, gy, 6) - .5) * .2)
        if side == 'south':
            return I['s1'] if py == h - 1 else skin(v)
        return skin(v)
    if material == 'brow':
        if side == 'north':
            return I['s2'] if 6 <= px < w - 6 else (I['h1'] if py == 0 else I['h0'])
        if side == 'up':
            return I['s3']
        if side == 'down':
            return I['ink']
        return I['h0']
    if material == 'nose':
        if side == 'up':
            return I['s4']
        if side == 'down':
            return I['s0']
        if side == 'north':
            if py == h - 1:
                return I['s0'] if px != w // 2 else I['s1']
            return I['s4'] if py < 2 else I['s3']
        return I['s2']
    if material == 'moustache':
        if side == 'up' or (side == 'north' and py == 0):
            return I['h1']
        return I['h1'] if noise(gx, gy, 8) > .8 else I['h0']
    if material in ('leotard', 'torso'):
        if material == 'torso':
            # The leotard covers the chest only below a line that climbs toward his left shoulder; skin above it.
            if side in ('north', 'south'):
                top = 9 + int(4 * px / w) + (1 if noise(gx, gy, 9) > .7 else 0)
            elif side in ('east', 'west'):
                top = 10
            else:
                top = 99 if side == 'up' else 0
            if py < top:
                return skin(v)
            if py == top:
                return I['st0']
        if noise(gx, gy, 12) < .05 and side not in ('up', 'down'):
            return I['s1']
        if side in ('up', 'down'):
            return I['st0']
        if py % 6 < 4:
            return I['st2'] if v > .72 else I['st1'] if v > .4 else I['st0']
        return I['b1'] if v > .5 else I['b0']
    if material == 'belt':
        if side in ('up', 'down') or py in (0, h - 1):
            return I['l0']
        if side == 'north' and py in (1, h - 2) and px % 2 == 0:
            return I['b0']
        return I['l1'] if noise(gx, gy, 14) > .25 else I['l0']
    if material in ('iron', 'hoop'):
        if material == 'hoop' and side not in ('up', 'down') and py in (0, h - 1):
            # His blood seeping where the iron grips the flesh.
            return I['g0'] if px % 3 == 0 and noise(gx, gy, 15) > .3 else I['v0']
        if side == 'up' or py == 0:
            return I['fe2']
        if side == 'down' or py == h - 1:
            return I['fe0']
        if h >= 4 and py == 1 and px % 4 == 2:
            return I['fe2']
        if noise(gx, gy, 16) < .08:
            return I['fe0']
        return I['fe1'] if v < .7 else I['fe2']
    if material == 'wrap':
        if side == 'down':
            return I['b0']
        return I['b0'] if (px + py) % 4 == 0 or v < .35 else I['b1']
    if material == 'bone':
        return I['b1'] if side in ('north', 'up') else I['b0']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealFlight;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_strongman.py; regenerate the model, atlas and BBModel together.
/**
 * The Strongman's true self: a colossus seen from the waist up, wading round his ring. At rest he leans over the ring
 * with his hands held out over it, fingers half closed, breathing, his head following the fighter. Each intervention is
 * a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up. His own arms stay outside the ring and make
 * the motion where he stands; spectral arms, larger copies of his own drawn glowing ({@link #renderGhosts}), manifest
 * over the ring and do it there, at a synced point: a fist held over the weight's mark and driven down on it, both fists
 * brought down on the rim nearest him for the heave, a hand riding the bar across the ring, and both hands taking hold of
 * the fighter and hauling for the line.
 */
public final class CircusTrueStrongmanModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_strongman"), "main");
    /** His arm's lengths in pixels: upper arm and forearm, then the hand with its fingers. */
    private static final float ARM = 28.0F, HAND = 13.0F;
    private final ModelPart root, chest, neck, head;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;

    public CircusTrueStrongmanModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        chest = root.getChild("belly").getChild("chest");
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
        }
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
        pose(age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        float breath = Mth.sin(age * .055F);
        // Leaning in over the ring, the chest rising and falling.
        chest.xRot += .14F + breath * .02F;
        neck.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD - .14F;
        // Every so often the hands close and open, as if weighing the ring.
        float clench = HemoEasing.smoothstep(.8F, 1.0F, Mth.sin(age * .021F));
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            shoulders[i].y -= breath * .35F;
            uppers[i].xRot = -.6F + breath * .03F;
            uppers[i].zRot = -sign * .32F;
            fores[i].xRot = -1.0F;
            fores[i].zRot = sign * .12F;
            hands[i].yRot = sign * 1.35F;
            hands[i].xRot = .25F;
            curl(i, .35F + clench * .9F);
        }
        ghostWeight[0] = ghostWeight[1] = 0.0F;
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.25F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** This frame's spectral arms: how strongly each shows, where its fist lands (model pixels), how closed it is. */
    private final float[] ghostWeight = new float[2], ghostFist = new float[2];
    private final Vec3[] ghostTarget = {Vec3.ZERO, Vec3.ZERO};

    /**
     * A blow's beats, in ticks from the wind-up's start: a slow gather to {@link #GATHERED}, a trembling hold, the drop
     * (accelerating, a few ticks long) landing on {@link CircusFacultyEntity#GESTURE_IMPACT}, a hit-stop on the floor,
     * then a heavy recovery.
     */
    private static final float GATHERED = 14.0F, DROP = 2.5F, HELD = 8.0F, RECOVER = 18.0F;

    /** 0 until the drop, then 1 at impact, accelerating like a falling weight. */
    private static float drop(float since) {
        float t = Mth.clamp((since - (CircusFacultyEntity.GESTURE_IMPACT - DROP)) / DROP, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 through the gather and the blow, falling to 0 over the recovery after the hit-stop. */
    private static float held(float since) {
        float after = CircusFacultyEntity.GESTURE_IMPACT + HELD;
        return 1.0F - HemoEasing.smoothstep(after, after + RECOVER, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        float gather = HemoEasing.smoothstep(0.0F, GATHERED, since);
        // The tremble of a held weight at the top of the gather, before it falls.
        float tension = HemoEasing.smoothstep(GATHERED - 3.0F, GATHERED, since)
                * (1.0F - HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT - DROP, CircusFacultyEntity.GESTURE_IMPACT, since));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float down = drop(since);
        float weight = HemoEasing.smoothstep(0.0F, 6.0F, since) * held(since);
        // The blow lands hard: the body pitches into it, then settles.
        float slam = down * held(since);
        switch (gesture) {
            case CircusFacultyEntity.GESTURE_DROP -> {
                // A fist hauled up over the mark, trembling, then dropped on it like the weight it is.
                lift(0, weight, gather, down, tremble);
                ghost(0, target.add(0.0D, Mth.lerp(down, 5.5D + .5D * gather + .15D * tremble, .4D), 0.0D), weight,
                        .5F + 1.0F * Math.max(gather, down));
                chest.xRot += -.12F * gather * (1.0F - down) + .28F * slam;
                head.xRot += .1F * slam;
            }
            case CircusFacultyEntity.GESTURE_SLAM -> {
                // Both fists over the rim nearest him, hauled up, then brought down together to start the wave.
                Vec3 toward = new Vec3(at.x - target.x, 0.0D, at.z - target.z).normalize();
                for (int i = 0; i < 2; i++) {
                    lift(i, weight, gather, down, tremble);
                    Vec3 rim = target.add(toward.yRot((i == 0 ? 1 : -1) * .4F).scale(OrdealMoves.HEAVE_START));
                    ghost(i, rim.add(0.0D, Mth.lerp(down, 6.0D + .5D * gather + .15D * tremble, .4D), 0.0D), weight,
                            .6F + .9F * Math.max(gather, down));
                }
                chest.xRot += -.16F * gather * (1.0F - down) + .34F * slam;
                head.xRot += .14F * slam;
            }
            case CircusFacultyEntity.GESTURE_SWEEP -> {
                // A hand pressed down on the bar where it will start, then hauled across the ring with it.
                float sweep = Mth.clamp((since - CircusFacultyEntity.GESTURE_IMPACT) / OrdealMoves.SWEEP_TICKS, 0.0F, 1.0F);
                float hold = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(
                        CircusFacultyEntity.GESTURE_IMPACT + OrdealMoves.SWEEP_TICKS, CircusFacultyEntity.GESTURE_IMPACT
                                + OrdealMoves.SWEEP_TICKS + 12.0F, since));
                // The sweep starts slow under the bar's weight and whips through the far side.
                float swept = sweep * sweep * (3.0F - 2.0F * sweep);
                uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, -1.35F + .1F * tremble);
                uppers[1].yRot = Mth.lerp(hold, 0.0F, Mth.lerp(swept, 1.0F, -1.0F));
                fores[1].xRot = Mth.lerp(hold, fores[1].xRot, -.25F);
                curl(1, Mth.lerp(hold, .35F, 1.3F));
                chest.yRot += hold * Mth.lerp(swept, .3F, -.3F);
                float along = -(float) OrdealMoves.SWEEP_REACH + 2.0F * (float) OrdealMoves.SWEEP_REACH * swept;
                double side = Mth.clamp(at.z - target.z, -8.0D, 8.0D);
                ghost(1, target.add(along, Mth.lerp(gather, 2.5D, .8D), side), hold, 1.3F);
            }
            case CircusFacultyEntity.GESTURE_PULL -> {
                // Both hands shoot out for the fighter, close on them at the strike, and haul in hard jerks.
                float out = HemoEasing.smoothstep(0.0F, 8.0F, since);
                float grip = HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT - 2.0F, CircusFacultyEntity.GESTURE_IMPACT, since);
                float hold = out * (1.0F - HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT + 60.0F,
                        CircusFacultyEntity.GESTURE_IMPACT + 72.0F, since));
                // Each haul a jerk: a sharp pull, then a breath.
                float jerk = Math.max(0.0F, Mth.sin(since * .55F)) * grip;
                Vec3 hauled = target.lerp(at, (.15F + .2F * jerk) * grip).add(0.0D, 1.2D, 0.0D);
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(grip, -1.5F, -.9F - .25F * jerk));
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, Mth.lerp(grip, -.15F, -1.6F - .2F * jerk));
                    curl(i, Mth.lerp(hold, .35F, .2F + 1.3F * grip));
                    Vec3 offset = new Vec3(i == 0 ? -1.2D : 1.2D, 0.0D, 0.0D).yRot(-bodyYaw * Mth.DEG_TO_RAD);
                    ghost(i, hauled.add(offset), hold, .2F + 1.3F * grip);
                }
                chest.xRot += .12F * out * (1.0F - grip) - .14F * jerk * hold;
            }
            default -> {
            }
        }
    }

    /** His own arm hauled up overhead through the gather, trembling at the top, brought down hard at the drop. */
    private void lift(int i, float weight, float gather, float down, float tremble) {
        float up = Mth.lerp(gather, -.6F, -2.9F + .05F * tremble);
        uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, up, -1.0F));
        uppers[i].zRot *= 1.0F - .6F * weight;
        fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, Mth.lerp(gather, -1.0F, -1.3F), -.25F));
        curl(i, Mth.lerp(weight, .35F, .4F + 1.1F * Math.max(gather, down)));
    }

    private void ghost(int i, Vec3 world, float weight, float fist) {
        ghostWeight[i] = weight;
        ghostTarget[i] = model(world);
        ghostFist[i] = fist;
    }

    /** The spectral arm's size against his own, and its length then (upper arm, forearm and hand), in pixels. */
    private static final float GHOST_SIZE = 1.6F, GHOST_LENGTH = (ARM + HAND) * GHOST_SIZE;

    /**
     * Draws the spectral arms: a copy of his own arm, larger, manifesting in the air over the ring and driving its fist
     * onto this frame's target from above and from his side, faded in with the gesture. His real arms stay where they are.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < 2; i++) {
            float weight = ghostWeight[i];
            if (weight <= .01F) continue;
            Vec3 target = ghostTarget[i];
            Vec3 toward = new Vec3(-target.x, 0.0D, -target.z);
            toward = toward.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : toward.normalize();
            // From above and from his side of the ring: in model space up is -y.
            Vec3 dir = toward.scale(-.5D).add(0.0D, .85D, 0.0D).normalize();
            Vec3 origin = target.subtract(dir.scale(GHOST_LENGTH));
            PartPose upper = uppers[i].storePose(), fore = fores[i].storePose(), hand = hands[i].storePose();
            float scale = uppers[i].xScale;
            uppers[i].setPos((float) origin.x, (float) origin.y, (float) origin.z);
            uppers[i].setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            uppers[i].xScale = uppers[i].yScale = uppers[i].zScale = GHOST_SIZE;
            fores[i].setRotation(0.0F, 0.0F, 0.0F);
            hands[i].setRotation(0.0F, hands[i].yRot, 0.0F);
            float[] curled = {fingers[i][0].zRot};
            curl(i, ghostFist[i]);
            int alpha = (int) (Mth.clamp(weight, 0.0F, 1.0F) * ((color >>> 24) & 255));
            uppers[i].render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
            uppers[i].loadPose(upper);
            fores[i].loadPose(fore);
            hands[i].loadPose(hand);
            uppers[i].xScale = uppers[i].yScale = uppers[i].zScale = scale;
            curl(i, Math.abs(curled[0]));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / OrdealFlight.SCALE);
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
    return build_rig('circus_true_strongman', 'CircusTrueStrongmanModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
