"""The Acrobat's true self (2026-11-17): the performer as they are in their own mind, always aloft. Hung by the knees
from a trapeze in the rigging over their ring in the Chamber of Will, watching the fight upside down.

The same masked performer as the physical Acrobat: the black face with lit red eyes, the band across the brow (bone
here, not the physical one's ochre), the two ribbons from the back of the head, the charcoal suit with its crimson
sash front and coat tails. Long-limbed. What the true self adds is their craft grown into them: scarlet tendons laced
in a cross-lacing up the forearms and shins (the ones they put on their Vultures by hand), crimson streamers from the
wrists, hematic iron rings at the wrists and a harness ring at the chest, and a lacquered trapeze bar on two lines of
their own blood, lit, running up into the dark. Shaded as round forms lit from above; the lacing crosses, it never
runs in seams down a limb.

The rig is jointed: the trapeze (bar and lines) is the root at the entity's own position; a `hang` joint at the bar
swings the body about it; the pelvis hangs upside down from the knees, and the waist, chest, neck and head, shoulder,
elbow and wrist with four two-segment fingers and a two-segment thumb, hip, knee and ankle are each their own joint.
`ghost_line` is a unit line drawn only by the spectral rigging (the model's renderGhosts). Drawn twice life size
(`AcrobatRigging.SCALE`).
"""
import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'c0', 'c1', 'c2', 'c3', 'r0', 'r1', 'r2', 'r3', 'b0', 'b1', 'fe0', 'fe1', 'fe2', 'g0', 'l0']
PALETTE = ['#0a0a0e', '#16161d', '#23232c', '#34333f', '#4a4958', '#450713', '#860f24', '#b51d35', '#d4434f',
           '#bfb398', '#e9e1cc', '#2a2528', '#4a4246', '#7a6e72', '#ff4a3a', '#5e1420']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0']}

# The front of the head, 7 by 7: a black mask, the band row (its own box over it), lit red eyes.
FACE = [
    'mmmmmmm',
    'mdddddm',
    'mmmmmmm',
    'mppmppm',
    'mdkmkdm',
    'mmmmmmm',
    'ddddddd',
]


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    # The trapeze: the bar at the entity's own position, its two lines running up into the dark.
    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('bar', 'root', (0, 0, 0), [box((-10, -.75, -.75), (20, 1.5, 1.5), 'lacquer')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_line', 'root', (sign * 9.25, 0, 0), [box((-.5, -96, -.5), (1, 96, 1), 'line')])
    # The spectral rigging's unit line, drawn only by renderGhosts.
    joint('ghost_line', 'root', (0, 0, 0), [box((-.5, 0, -.5), (1, 16, 1), 'line')])
    # The body swings about the bar; the pelvis hangs upside down 10 below it, the thighs reaching up to the bar.
    joint('hang', 'root', (0, 0, 0))
    joint('body', 'hang', (0, 10, 0), [box((-3.5, -1, -2), (7, 4, 4), 'suit')], (0, 0, 180))
    joint('waist', 'body', (0, -1, 0), [box((-3, -6, -1.75), (6, 6, 3.5), 'suit')])
    joint('chest', 'waist', (0, -6, 0), [
        box((-4, -7, -2.25), (8, 7, 4.5), 'suit'),
        box((-2.5, -6.5, -2.75), (5, 5.5, .5), 'sash'),
        box((-1.5, -5.25, -3.25), (3, 3, .5), 'iron')])
    joint('neck', 'chest', (0, -7, 0), [box((-1, -2, -1), (2, 2, 2), 'mask')])
    joint('head', 'neck', (0, -2, 0), [
        box((-3.5, -7, -3), (7, 7, 6), 'head'),
        box((-3.75, -6.25, -3.25), (7.5, 1, 6.5), 'band')])
    for side, sign in (('right', -1), ('left', 1)):
        # The two ribbons from the back of the head, each in two lengths.
        joint(side + '_tail', 'head', (sign * 1.75, -5.5, 3), [box((-.5, -.5, 0), (1, 1, 5), 'ribbon')],
              (25, sign * 15, 0))
        joint(side + '_tail_end', side + '_tail', (0, 0, 5), [box((-.75, -.75, 0), (1.5, 1.5, 4), 'ribbon')], (20, 0, 0))
        # The arm: shoulder, elbow, wrist; the palm toward the body, four two-segment fingers and a thumb.
        joint(side + '_upper', 'chest', (sign * 4.75, -6, 0), [
            box((-1.25, -1, -1.25), (2.5, 7, 2.5), 'suit'),
            box((-1.75, -1.5, -1.75), (3.5, 2, 3.5), 'sash')])
        joint(side + '_fore', side + '_upper', (0, 6, 0), [
            box((-1.25, 0, -1.25), (2.5, 6, 2.5), 'laced'),
            box((-1.5, 4, -1.5), (3, 1.5, 3), 'iron')])
        joint(side + '_streamer', side + '_fore', (0, 5, 1.25), [box((-.25, 0, 0), (.5, 8, 1), 'ribbon')], (12, 0, 0))
        joint(side + '_hand', side + '_fore', (0, 6, 0), [box((-1, 0, -1.25), (2, 2, 2.5), 'glove')])
        for digit, z in enumerate((-.75, -.25, .25, .75)):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 2, z), [box((-.25, 0, -.25), (.5, 1.25, .5), 'glove')])
            joint(name + '_tip', name, (0, 1.25, 0), [box((-.25, 0, -.25), (.5, 1, .5), 'glove')])
        joint(side + '_thumb', side + '_hand', (0, .5, -1.25), [box((-.25, 0, -.25), (.5, 1, .5), 'glove')], (-30, 0, 0))
        joint(side + '_thumb_tip', side + '_thumb', (0, 1, 0), [box((-.25, 0, -.25), (.5, .75, .5), 'glove')])
        # The leg: hip, knee, ankle. Hung, the thighs reach up to the bar and the shins hook over it.
        joint(side + '_thigh', 'body', (sign * 1.75, 3, 0), [box((-1.5, 0, -1.5), (3, 8, 3), 'suit')])
        joint(side + '_shin', side + '_thigh', (0, 8, 0), [box((-1.25, 0, -1.25), (2.5, 7, 2.5), 'laced')], (125, 0, 0))
        joint(side + '_foot', side + '_shin', (0, 7, 0), [box((-1, 0, -2.25), (2, 1.5, 3.25), 'slipper')])
        joint(side + '_coat', 'body', (sign * 1.5, 2, 2), [box((-1.25, 0, -.25), (2.5, 5, .5), 'suit')], (150, 0, 0))
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


def charcoal(v):
    for limit, name in ((.22, 'ink'), (.36, 'c0'), (.55, 'c1'), (.74, 'c2')):
        if v < limit:
            return I[name]
    return I['c3']


def crimson(v):
    for limit, name in ((.3, 'r0'), (.55, 'r1'), (.78, 'r2')):
        if v < limit:
            return I[name]
    return I['r3']


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material in ('suit', 'mask'):
        return charcoal(v)
    if material == 'glove':
        return charcoal(v - .1)
    if material == 'slipper':
        return crimson(v - .15)
    if material == 'laced':
        # Scarlet tendons laced in a cross over the suit: diagonals both ways, a lit knot where they cross.
        if side not in ('up', 'down'):
            a, b = (px + py) % 4, (px - py) % 4
            if a == 0 and b == 0:
                return I['g0'] if noise(gx, gy, 4) > .4 else I['r3']
            if a == 0 or b == 0:
                return crimson(v + .1)
        return charcoal(v)
    if material in ('sash', 'ribbon'):
        if material == 'sash' and side == 'north' and px in (0, w - 1):
            return I['r0']
        return crimson(v)
    if material == 'head':
        if side == 'north':
            key = FACE[py][px]
            if key == 'p':
                return I['g0']
            if key == 'k':
                return I['ink']
            if key == 'd':
                return charcoal(v - .2)
            return charcoal(v)
        return charcoal(v)
    if material == 'band':
        if side == 'down':
            return I['b0']
        return I['b1'] if v > .55 and noise(gx, gy, 7) > .25 else I['b0']
    if material == 'iron':
        if side == 'up' or py == 0:
            return I['fe2']
        if side == 'down' or py == h - 1:
            return I['fe0']
        return I['fe1'] if v < .7 else I['fe2']
    if material == 'lacquer':
        if side == 'up':
            return I['r2']
        if side == 'down':
            return I['l0']
        return I['r1'] if py == 0 else I['l0'] if noise(gx, gy, 3) > .2 else I['r0']
    if material == 'line':
        # The blood the lines are made of, lit through: a burning core, a darker rim at the ends of each texel run.
        return I['g0'] if (gy + gx) % 5 else I['r3']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatOrdealHits;
import com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatRigging;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_acrobat.py; regenerate the model, atlas and BBModel together.
/**
 * The Acrobat's true self: hung by the knees from a trapeze in the rigging over their ring, watching upside down, the
 * body swaying about the bar and the head following the fighter. Each intervention is a gesture
 * ({@link CircusAcrobatEntity#gesture}) timed from its wind-up: their own body makes the motion on the trapeze, and
 * their rigging, made of their blood, does it in the ring ({@link #renderGhosts}): a spectral trapeze hauled back and
 * let go through the fighter's spot, a double of themselves rolling in the dark and plummeting onto the mark, a snare
 * line paid out from their hand to a loop round the fighter and hauled, and a net of lines falling. When they let go
 * and fly across the ring they tumble, and the trapeze is left behind.
 */
public final class CircusTrueAcrobatModel extends EntityModel<CircusAcrobatEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_acrobat"), "main");
    private final ModelPart root, bar, hang, body, waist, chest, neck, head, ghostLine;
    private final ModelPart[] lines = new ModelPart[2], tails = new ModelPart[2], tailEnds = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], streamers = new ModelPart[2],
            thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it hangs and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's gesture, for the spectral rigging. */
    private int gesture = -1;
    private float since;
    private Vec3 target = Vec3.ZERO;

    public CircusTrueAcrobatModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        bar = root.getChild("bar");
        ghostLine = root.getChild("ghost_line");
        hang = root.getChild("hang");
        body = hang.getChild("body");
        waist = body.getChild("waist");
        chest = waist.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            lines[i] = root.getChild(side + "_line");
            tails[i] = head.getChild(side + "_tail");
            tailEnds[i] = tails[i].getChild(side + "_tail_end");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            streamers[i] = fores[i].getChild(side + "_streamer");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = body.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
        ghostLine.visible = false;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CircusAcrobatEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        // Flying across the ring (let go of one trapeze, caught by the far one) reads from the body's own speed.
        double speed = Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        float flying = HemoEasing.smoothstep(.3F, .5F, (float) speed);
        pose(age, yaw, pitch, flying, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float yaw, float pitch, float flying, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        ghostLine.visible = false;
        bar.visible = lines[0].visible = lines[1].visible = true;
        this.gesture = gesture;
        this.since = since;
        this.target = target;
        float breath = Mth.sin(age * .06F);
        // Hanging easy: a slow sway about the bar, the chest breathing, the arms loose toward the floor.
        hang.xRot += Mth.sin(age * .09F) * .06F;
        hang.zRot += Mth.sin(age * .057F) * .03F;
        chest.xRot += breath * .03F;
        // Upside down the head's turn and tip run the other way.
        head.yRot -= Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        neck.yRot -= Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.xRot -= Mth.clamp(pitch, -60, 60) * Mth.DEG_TO_RAD * .8F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            uppers[i].xRot = Mth.PI + Mth.sin(age * .07F + i) * .05F;
            uppers[i].zRot = sign * .2F;
            fores[i].xRot = -.25F;
            hands[i].yRot = sign * .3F;
            curl(i, .35F + .15F * Mth.sin(age * .05F + i * 2));
            thighs[i].zRot = -sign * .08F;
            feet[i].xRot = .35F;
            tails[i].xRot += Mth.sin(age * .2F + i) * .12F;
            tailEnds[i].xRot += Mth.sin(age * .2F + i + .8F) * .15F;
            streamers[i].xRot += Mth.sin(age * .17F + i * 1.7F) * .1F;
        }
        if (gesture >= 0) gesture(gesture, since, target);
        if (flying > 0.0F) fly(age, flying);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = -sign * amount * (1.0F + f * .05F);
            tips[i][f].zRot = -sign * amount * 1.2F;
        }
        thumbs[i].zRot = sign * amount * .5F;
        thumbTips[i].zRot = -sign * amount * .4F;
    }

    /** Let go and flying across: tucked and tumbling about the hips, the trapeze left hanging behind. */
    private void fly(float age, float flying) {
        hang.xRot = Mth.lerp(flying, hang.xRot, age * .55F);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].xRot = Mth.lerp(flying, thighs[i].xRot, -1.9F);
            shins[i].xRot = Mth.lerp(flying, shins[i].xRot, 2.3F);
            uppers[i].xRot = Mth.lerp(flying, uppers[i].xRot, -1.2F);
            uppers[i].zRot = Mth.lerp(flying, uppers[i].zRot, sign * .3F);
            fores[i].xRot = Mth.lerp(flying, fores[i].xRot, -1.6F);
            curl(i, Mth.lerp(flying, .4F, 1.2F));
        }
        boolean held = flying < .5F;
        bar.visible = held;
        lines[0].visible = lines[1].visible = held;
    }

    /** The wind-up of each move, in ticks; its blow lands on it. */
    private static float windup(int gesture) {
        return switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> AcrobatOrdealHits.SWING_WINDUP;
            case CircusAcrobatEntity.GESTURE_DROP -> AcrobatOrdealHits.DROP_WINDUP;
            case CircusAcrobatEntity.GESTURE_SNARE -> AcrobatOrdealHits.SNARE_WINDUP;
            default -> AcrobatOrdealHits.NET_WINDUP;
        };
    }

    /** 0 until two and a half ticks before {@code at}, then 1 at {@code at}, accelerating. */
    private static float drop(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 through the gather and the blow, falling to 0 over a heavy recovery after a hit-stop. */
    private static float held(float since, float at, float hold) {
        return 1.0F - HemoEasing.smoothstep(at + hold, at + hold + 16.0F, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        float w = windup(gesture);
        float gather = HemoEasing.smoothstep(0.0F, w - 6.0F, since);
        float tension = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since) * (1.0F - HemoEasing.smoothstep(w - 2.5F, w, since));
        float tremble = Mth.sin(since * 3.3F) * tension;
        float down = drop(since, w);
        switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> {
                // Pumping the trapeze higher and higher, arms hauled back, then both thrown out over the ring.
                float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * held(since, w + AcrobatOrdealHits.SWING_TICKS, 4.0F);
                hang.xRot += weight * (Mth.sin(since * .38F) * .45F * gather * (1.0F - down) + .35F * down);
                chest.xRot += weight * (-.35F * gather * (1.0F - down) + .45F * down);
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - .8F + .05F * tremble, Mth.PI + 1.35F));
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, -.9F, -.1F));
                    curl(i, Mth.lerp(weight, .4F, Mth.lerp(down, 1.3F, .1F)));
                }
            }
            case CircusAcrobatEntity.GESTURE_DROP -> {
                // Curled up to the bar, trembling, then snapped straight down: the dive their double makes.
                float weight = HemoEasing.smoothstep(0.0F, 5.0F, since) * held(since, w, 6.0F);
                float curlUp = gather * (1.0F - down);
                waist.xRot += weight * (1.1F * curlUp - .2F * down);
                chest.xRot += weight * (.7F * curlUp + .05F * tremble);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - 2.2F * curlUp, Mth.PI + .15F));
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, sign * Mth.lerp(down, .15F, .9F));
                    curl(i, Mth.lerp(weight, .4F, 1.2F * curlUp));
                }
            }
            case CircusAcrobatEntity.GESTURE_SNARE -> {
                // The right arm wound back and cast overhand, the line paying out; on the blow both hands haul.
                float weight = HemoEasing.smoothstep(0.0F, 4.0F, since) * held(since, w + 8.0F, 2.0F);
                float cast = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since);
                float haul = HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since);
                float jerk = Math.max(0.0F, Mth.sin((since - w) * .9F)) * haul;
                uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(cast, Mth.PI - 1.4F + .05F * tremble, Mth.PI + 1.2F));
                uppers[0].xRot = Mth.lerp(haul, uppers[0].xRot, Mth.PI - .9F - .3F * jerk);
                uppers[1].xRot = Mth.lerp(haul, uppers[1].xRot, Mth.PI - .7F - .3F * jerk);
                fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(cast, -1.2F, -.1F));
                for (int i = 0; i < 2; i++) {
                    fores[i].xRot = Mth.lerp(haul, fores[i].xRot, -1.5F);
                    curl(i, Mth.lerp(weight, .4F, .4F + .9F * Math.max(cast * (i == 0 ? 1 : 0), haul)));
                }
                chest.xRot += weight * (-.2F * (1.0F - cast) + .25F * cast - .35F * haul);
                hang.xRot += -.2F * jerk;
            }
            case CircusAcrobatEntity.GESTURE_NET -> {
                // Both arms spread wide, the net held between them, then flung down over the ring.
                float weight = HemoEasing.smoothstep(0.0F, 6.0F, since) * held(since, w, 8.0F);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, sign * Mth.lerp(down, 1.3F + .05F * tremble, .25F));
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, Mth.PI - .4F, Mth.PI + 1.0F));
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, -.6F, 0.0F));
                    curl(i, Mth.lerp(weight, .4F, Mth.lerp(down, 1.1F, .05F)));
                }
                chest.xRot += weight * (-.3F * gather * (1.0F - down) + .4F * down);
                hang.xRot += weight * .25F * down;
            }
            default -> {
            }
        }
    }

    /**
     * Draws the Acrobat's rigging in the ring for this frame's gesture: lines and bars of their blood, and their double.
     * Faded in with the gesture and out after its blow.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (gesture < 0) return;
        float w = windup(gesture);
        switch (gesture) {
            case CircusAcrobatEntity.GESTURE_SWING -> swingGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_DROP -> dropGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_SNARE -> snareGhost(pose, consumer, light, overlay, color, w);
            case CircusAcrobatEntity.GESTURE_NET -> netGhost(pose, consumer, light, overlay, color, w);
            default -> {
            }
        }
    }

    /** The spectral trapeze: hauled back over the lane's near end, trembling, then let go through the fighter's spot. */
    private void swingGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float end = w + AcrobatOrdealHits.SWING_TICKS;
        float alpha = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(end + 2.0F, end + 10.0F, since));
        if (alpha <= .01F) return;
        Vec3 d = swingDirection();
        Vec3 across = new Vec3(-d.z, 0.0D, d.x);
        double along = since < w ? -AcrobatOrdealHits.SWING_HALF_LENGTH : AcrobatOrdealHits.swingAlong(since - w);
        float tremble = since < w ? Mth.sin(since * 3.3F) * HemoEasing.smoothstep(w - 9.0F, w - 3.0F, since) * .12F : 0.0F;
        Vec3 barAt = target.add(d.scale(along)).add(0.0D, AcrobatOrdealHits.barHeight(along) + (since < w ? .5D + tremble : 0.0D), 0.0D);
        trapeze(pose, consumer, light, overlay, color, alpha, barAt, across);
    }

    /** A trapeze at {@code barAt} across {@code across}: the bar and its two lines up into the dark. */
    private void trapeze(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float alpha, Vec3 barAt,
            Vec3 across) {
        Vec3 left = barAt.add(across.scale(1.15D)), right = barAt.subtract(across.scale(1.15D));
        Vec3 m = model(barAt), ml = model(left);
        Vec3 dir = ml.subtract(m);
        PartPose kept = bar.storePose();
        bar.setPos((float) m.x, (float) m.y, (float) m.z);
        bar.setRotation(0.0F, (float) Math.atan2(-dir.z, dir.x), 0.0F);
        bar.xScale = bar.yScale = bar.zScale = 1.4F;
        boolean shown = bar.visible;
        bar.visible = true;
        bar.render(pose, consumer, light, overlay, tint(color, alpha));
        bar.visible = shown;
        bar.loadPose(kept);
        bar.xScale = bar.yScale = bar.zScale = 1.0F;
        for (Vec3 foot : new Vec3[]{left, right}) line(pose, consumer, light, overlay, color, alpha, foot.add(0.0D, 14.0D, 0.0D), foot, 1.3F);
    }

    /** Their double, rolling slowly in the dark over the mark, then plummeting onto it head first. */
    private void dropGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(0.0F, 5.0F, since) * (1.0F - HemoEasing.smoothstep(w + 1.0F, w + 8.0F, since));
        if (alpha <= .01F) return;
        double height = since < w ? AcrobatOrdealHits.dropHeight(since) : 0.0D;
        float plummet = HemoEasing.smoothstep(w - 5.0F, w - 3.0F, since);
        Vec3 m = model(target.add(0.0D, height + .2D, 0.0D));
        PartPose[] kept = body.getAllParts().map(ModelPart::storePose).toArray(PartPose[]::new);
        // Tucked and turning while it hangs in the dark; stretched straight, head down, as it falls.
        float tuck = 1.0F - plummet;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].setRotation(-1.8F * tuck, 0.0F, -sign * .1F);
            shins[i].setRotation(2.2F * tuck, 0.0F, 0.0F);
            uppers[i].setRotation(Mth.lerp(plummet, -1.0F, Mth.PI), 0.0F, sign * Mth.lerp(plummet, .3F, .1F));
            fores[i].setRotation(Mth.lerp(plummet, -1.7F, 0.0F), 0.0F, 0.0F);
        }
        waist.setRotation(.5F * tuck, 0.0F, 0.0F);
        chest.setRotation(.4F * tuck, 0.0F, 0.0F);
        body.setPos((float) m.x, (float) m.y, (float) m.z);
        body.setRotation(Mth.lerp(plummet, since * .45F, 0.0F), 0.0F, Mth.lerp(plummet, 0.0F, Mth.PI));
        body.xScale = body.yScale = body.zScale = 1.05F;
        body.render(pose, consumer, light, overlay, tint(color, alpha));
        body.xScale = body.yScale = body.zScale = 1.0F;
        int k = 0;
        for (ModelPart part : (Iterable<ModelPart>) body.getAllParts()::iterator) part.loadPose(kept[k++]);
    }

    /** The snare: a line paid out from their hand down to a loop round the fighter's feet, then hauled taut. */
    private void snareGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(w - 9.0F, w - 6.0F, since) * (1.0F - HemoEasing.smoothstep(w + 8.0F, w + 12.0F, since));
        if (alpha <= .01F) return;
        Vec3 hand = handWorld(0);
        Vec3 loop = target.add(0.0D, .15D, 0.0D);
        // Paying out: the line's end runs from the hand to the loop through the cast.
        float out = HemoEasing.smoothstep(w - 8.0F, w - 2.0F, since);
        float haul = HemoEasing.smoothstep(w, w + 3.0F, since);
        Vec3 end = hand.lerp(loop, out).lerp(hand, .35D * haul).add(0.0D, 1.2D * haul, 0.0D);
        line(pose, consumer, light, overlay, color, alpha, hand, end, 1.0F + .5F * haul);
        // The loop itself, cinching on the blow.
        double radius = AcrobatOrdealHits.SNARE_RADIUS * (1.0D - .7D * haul);
        Vec3 centre = loop.lerp(end, haul);
        int segments = 12;
        for (int s = 0; s < segments; s++) {
            double a0 = Math.PI * 2.0D * s / segments, a1 = Math.PI * 2.0D * (s + 1) / segments;
            line(pose, consumer, light, overlay, color, alpha * out, centre.add(Math.cos(a0) * radius, 0.0D, Math.sin(a0) * radius),
                    centre.add(Math.cos(a1) * radius, 0.0D, Math.sin(a1) * radius), .9F);
        }
    }

    /** The net: a square of lines seven blocks across falling from high over the mark, slow and then fast. */
    private void netGhost(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float w) {
        float alpha = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 24.0F, since));
        if (alpha <= .01F) return;
        float t = Mth.clamp(since / w, 0.0F, 1.0F);
        double height = .15D + AcrobatOrdealHits.NET_HEIGHT * (1.0D - t * t);
        double half = AcrobatOrdealHits.NET_RADIUS;
        Vec3 centre = target.add(0.0D, height, 0.0D);
        // A sag in the middle as it falls: the knots lag the edges.
        double sag = since < w ? .8D * t : 0.0D;
        for (int k = -3; k <= 3; k++) {
            double o = half * k / 3.0D;
            for (int s = -3; s < 3; s++) {
                double a = half * s / 3.0D, b = half * (s + 1) / 3.0D;
                double ya = -sag * (1.0D - (a * a + o * o) / (2.0D * half * half)), yb = -sag * (1.0D - (b * b + o * o) / (2.0D * half * half));
                line(pose, consumer, light, overlay, color, alpha, centre.add(a, ya, o), centre.add(b, yb, o), .8F);
                line(pose, consumer, light, overlay, color, alpha, centre.add(o, ya, a), centre.add(o, yb, b), .8F);
            }
        }
    }

    /** The swing's direction, as the server lays it: out from the ring's centre through the mark. */
    private Vec3 swingDirection() {
        Vec3 forward = new Vec3(-Math.sin(bodyYaw * Mth.DEG_TO_RAD), 0.0D, Math.cos(bodyYaw * Mth.DEG_TO_RAD));
        Vec3 centre = new Vec3(at.x, target.y, at.z).add(forward.scale(AcrobatRigging.RADIUS));
        Vec3 out = new Vec3(target.x - centre.x, 0.0D, target.z - centre.z);
        if (out.length() >= 1.5D) return out.normalize();
        return forward;
    }

    /** One line of the rigging from {@code a} to {@code b} in the world, {@code thickness} pixels across. */
    private void line(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color, float alpha, Vec3 a, Vec3 b,
            float thickness) {
        // A line passing by the eye would fill the view as a streak: it fades out within a few blocks of the camera.
        alpha *= HemoEasing.smoothstep(NEAR_FADE_START, NEAR_FADE_END, (float) cameraDistance(a, b));
        if (alpha <= .01F) return;
        Vec3 ma = model(a), mb = model(b);
        Vec3 d = mb.subtract(ma);
        double length = d.length();
        if (length < 1.0E-3D) return;
        d = d.scale(1.0D / length);
        ghostLine.setPos((float) ma.x, (float) ma.y, (float) ma.z);
        ghostLine.setRotation((float) Math.asin(Mth.clamp(d.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-d.x, d.y));
        ghostLine.xScale = ghostLine.zScale = thickness;
        ghostLine.yScale = (float) (length / 16.0D);
        ghostLine.visible = true;
        ghostLine.render(pose, consumer, light, overlay, tint(color, alpha));
        ghostLine.visible = false;
        ghostLine.xScale = ghostLine.yScale = ghostLine.zScale = 1.0F;
    }

    /** Within this many blocks of the camera a rigging line is gone; past the second it is whole. */
    private static final float NEAR_FADE_START = 2.5F, NEAR_FADE_END = 6.0F;

    /** The camera's nearest distance to the segment a to b, in world blocks. */
    private static double cameraDistance(Vec3 a, Vec3 b) {
        Vec3 eye = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 ab = b.subtract(a);
        double t = ab.lengthSqr() < 1.0E-6D ? 0.0D : Mth.clamp(eye.subtract(a).dot(ab) / ab.lengthSqr(), 0.0D, 1.0D);
        return eye.distanceTo(a.add(ab.scale(t)));
    }

    /**
     * The colour faded by {@code alpha}: in alpha for the translucent pass, and in the colour itself for the glow pass,
     * which is added light and ignores alpha.
     */
    private static int tint(int color, float alpha) {
        float a = Mth.clamp(alpha, 0.0F, 1.0F);
        int r = (int) (((color >> 16) & 255) * a), g = (int) (((color >> 8) & 255) * a), b = (int) ((color & 255) * a);
        return ((int) (a * ((color >>> 24) & 255)) << 24) | (r << 16) | (g << 8) | b;
    }

    /** Where hand {@code i} is in the world this frame, through the hanging body's joints. */
    private Vec3 handWorld(int i) {
        PoseStack stack = new PoseStack();
        root.translateAndRotate(stack);
        hang.translateAndRotate(stack);
        body.translateAndRotate(stack);
        waist.translateAndRotate(stack);
        chest.translateAndRotate(stack);
        uppers[i].translateAndRotate(stack);
        fores[i].translateAndRotate(stack);
        hands[i].translateAndRotate(stack);
        Vector3f p = stack.last().pose().transformPosition(0.0F, 2.0F / 16.0F, 0.0F, new Vector3f());
        return world(new Vec3(p.x * 16.0D, p.y * 16.0D, p.z * 16.0D));
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / AcrobatRigging.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    /** The inverse of {@link #model}: a point in this model's pixels in the world. */
    private Vec3 world(Vec3 model) {
        double x = -model.x / 16.0D, y = -(model.y / 16.0D - 1.501D), z = model.z / 16.0D;
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double wx = x * cos - z * sin, wz = x * sin + z * cos;
        return at.add(new Vec3(wx, y, wz).scale(AcrobatRigging.SCALE));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 256
    return build_rig('circus_true_acrobat', 'CircusTrueAcrobatModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
