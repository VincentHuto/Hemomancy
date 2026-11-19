"""The Fire Breather's true self (2026-11-17): the professor as they are in their own Chamber of Will, standing on the
pulpit past the Scorch Ring's north rim and breathing into it.

The same masked performer as the physical Fire Breather (the black hood and mask with the band across the brow, the
red slit eyes, the cowl's two horns, the crimson throat frame and vest, the long coat-tails, the gauntlets and the
torch) made into what they have spent a life becoming: a flue. The cowl's horns are chimney pipes with fire at their
mouths, the throat frame is a grate with the furnace behind it, the vest opens on a ribbed iron grate over a chest that
burns, the mask's mouth is a lit slot that opens on the fire, and the coat-tails' hems are burning down. The brow band,
gilt on the physical body, is hematic iron with an ember line in it (no gilt on blood-side things).

The rig is jointed: pelvis, chest, neck, head and jaw; hip, knee; shoulder, elbow, wrist, three two-segment fingers and
a thumb a side. The torch stays in the right hand. Eighteen loose flame parts (`flame_0` to `flame_17`, off the root)
are the breath given a body: the model lays them along the breath's path each frame and the renderer draws them as
light. Feet at model y 24 (the entity's feet); drawn `FireEaterChoreography.SCALE` times over, so a texel is
about a fifth of a block.
"""
import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'c0', 'c1', 'c2', 'c3', 'r0', 'r1', 'r2', 'r3', 'fe0', 'fe1', 'fe2', 'b0', 'b1', 'l0', 'l1',
         'e0', 'e1', 'e2', 'e3', 'g']
PALETTE = ['#140d11', '#231a21', '#32272f', '#45373f', '#5b4a55', '#4a1424', '#6e1a2e', '#962637', '#c03a47',
           '#2a2528', '#4a4246', '#7a6e72', '#bfb398', '#e9e1cc', '#2a1a14', '#46301f',
           '#5e1a12', '#d9532c', '#f7953a', '#ffd98a', '#ff4a3a']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['e1'], I['e2'], I['e3'], I['g']}
FLAMES = 18

FACE = [
    'cccccccc',
    'cckkkkcc',
    'kkkkkkkk',
    'kggkkggk',
    'kkkkkkkk',
    'kkksekkk',
    'kmmmmmmk',
    'kkkkkkkk',
]
FACE_KEYS = {'c': 'c1', 'k': 'ink', 'g': 'g', 's': 'e0', 'e': 'e1', 'm': 'e2'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -20, 0), [
        box((-4.5, -3, -2.5), (9, 4, 5), 'coat'),
        box((-4.75, -1, -2.75), (9.5, 1.5, 5.5), 'belt')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_thigh', 'pelvis', (sign * 2.25, 1, 0), [box((-1.75, 0, -1.75), (3.5, 10, 3.5), 'trouser')])
        joint(side + '_shin', side + '_thigh', (0, 10, 0), [
            box((-1.5, 0, -1.5), (3, 6, 3), 'trouser'),
            box((-1.75, 5, -1.75), (3.5, 4, 3.5), 'boot'),
            box((-1.75, 8, -3.25), (3.5, 1, 1.5), 'boot')])
        # The coat-tails, their hems burning down, and the open coat's front skirts.
        joint(side + '_tail', 'pelvis', (sign * 2.25, 0, 2.5), [box((-2, 0, -.5), (4, 15, 1), 'tail')], (8, 0, -sign * 4))
        joint(side + '_skirt', 'pelvis', (sign * 3, 0, -2.5), [box((-1.5, 0, -.5), (3, 11, 1), 'tail')], (-6, 0, -sign * 5))
    joint('chest', 'pelvis', (0, -3, 0), [
        box((-4.5, -12, -2.75), (9, 12, 5.5), 'coat'),
        box((-3, -11, -3.25), (6, 8, .5), 'vest'),
        box((-2.5, -10.5, -3.5), (5, 7, .5), 'grate'),
        box((-5.5, -12.5, -3), (11, 2, 6), 'collar')])
    joint('neck', 'chest', (0, -12, 0), [box((-1.5, -3, -1.5), (3, 3, 3), 'throat')])
    joint('throat_frame', 'neck', (0, -1, 0), [box((-2.25, -.5, -2.25), (4.5, 1, 4.5), 'collar')])
    joint('head', 'neck', (0, -3, 0), [box((-4, -8, -4), (8, 8, 8), 'mask')])
    joint('band', 'head', (0, -6, 0), [box((-4.25, 0, -4.25), (8.5, 1, 8.5), 'band')])
    joint('jaw', 'head', (0, -1.5, 0), [box((-2.5, 0, -4.25), (5, 1.5, 4), 'jaw')])
    for side, sign in (('right', -1), ('left', 1)):
        # The cowl's horns are flues, fire at their mouths.
        joint(side + '_flue', 'head', (sign * 2.75, -7, 0), [
            box((-.75, -.75, 0), (1.5, 1.5, 6), 'flue'),
            box((-1, -4.5, 5), (2, 4.5, 2), 'flue'),
            box((-1.25, -6, 4.75), (2.5, 1.5, 2.5), 'flue_tip')], (18, sign * 14, 0))
        joint(side + '_upper', 'chest', (sign * 5.5, -11, 0), [
            box((-1.25, 0, -1.25), (2.5, 9, 2.5), 'sleeve'),
            box((-1.75, -1, -1.75), (3.5, 2.5, 3.5), 'collar')])
        joint(side + '_fore', side + '_upper', (0, 9, 0), [
            box((-1.25, 0, -1.25), (2.5, 8, 2.5), 'sleeve'),
            box((-1.5, 4.5, -1.5), (3, 3.5, 3), 'gauntlet')])
        joint(side + '_hand', side + '_fore', (0, 8, 0), [box((-1, 0, -1.25), (2, 2.5, 2.5), 'glove')])
        for digit, z in enumerate((-.75, 0, .75)):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 2.5, z), [box((-.5, 0, -.25), (1, 1.5, .5), 'glove')])
            joint(name + '_tip', name, (0, 1.5, 0), [box((-.5, 0, -.25), (1, 1.25, .5), 'glove')])
        joint(side + '_thumb', side + '_hand', (-sign * .75, .5, -1.25), [box((-.25, 0, -.25), (.5, 1.5, .5), 'glove')],
              (-20, 0, -sign * 25))
        joint(side + '_thumb_tip', side + '_thumb', (0, 1.5, 0), [box((-.25, 0, -.25), (.5, 1, .5), 'glove')])
    # The torch, held upright in the right fist.
    joint('torch', 'right_hand', (0, 2, -.25), [
        box((-.5, -6, -.5), (1, 9, 1), 'bone'),
        box((-1, -7, -1), (2, 1, 2), 'collar'),
        box((-1.5, -9.5, -1.5), (3, 2.5, 3), 'ember'),
        box((-1, -11.5, -1), (2, 2, 2), 'ember'),
        box((-.5, -13, -.5), (1, 1.5, 1), 'ember')], (90, 0, 0))
    for k in range(FLAMES):
        joint('flame_' + str(k), 'mesh.getRoot()', (0, 0, 0), [box((-3, -3, -3), (6, 6, 6), 'flame')])
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
    """The shade of a lit value along a dark-to-light ramp of palette names."""
    step = 1.0 / len(names)
    return I[names[min(len(names) - 1, max(0, int(v / step)))]]


COAT = ('ink', 'c0', 'c1', 'c2', 'c3')
CRIMSON = ('r0', 'r1', 'r2', 'r3')
IRON = ('fe0', 'fe1', 'fe1', 'fe2')
LEATHER = ('ink', 'l0', 'l1')


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material in ('coat', 'trouser', 'sleeve'):
        # Soot cracks with the fire showing through, here and there on the coat.
        if side not in ('up', 'down') and noise(gx, gy, 21) < .025:
            return I['e1']
        return ramp(v * .9, COAT)
    if material == 'tail':
        # The hem burning: the last rows lit, a charred row above them, ragged.
        burn = h - 1 - py + (1 if noise(gx, 0, 22) > .6 else 0)
        if side not in ('up', 'down'):
            if burn <= 0:
                return I['e3'] if noise(gx, gy, 23) > .5 else I['e2']
            if burn == 1:
                return I['e1'] if noise(gx, gy, 24) > .3 else I['e0']
            if burn == 2:
                return I['e0'] if noise(gx, gy, 25) > .5 else I['ink']
        return ramp(v * .9, COAT)
    if material == 'belt':
        return I['l1'] if v > .55 else I['l0']
    if material == 'boot':
        return ramp(v, LEATHER)
    if material == 'vest':
        return ramp(v, CRIMSON)
    if material == 'grate':
        if side == 'north':
            # Ribs of iron across the furnace in the chest, the breastbone down the middle.
            if py % 2 == 0 or px == w // 2:
                return I['fe2'] if py == 0 or (px == w // 2 and py % 2) else I['fe1']
            return I['e3'] if 0 < px < w - 1 and noise(gx, gy, 26) > .3 else I['e2']
        return ramp(v, IRON)
    if material in ('collar', 'gauntlet'):
        if noise(gx, gy, 27) < .06:
            return I['fe0']
        return ramp(v, IRON)
    if material == 'throat':
        if side == 'north':
            return I['e2'] if px == w // 2 else I['r2']
        return ramp(v, CRIMSON)
    if material == 'mask':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if side == 'up':
            # The crown of the hood, soot-black, cracked with fire.
            return I['e1'] if noise(gx, gy, 28) < .05 else ramp(v * .6, COAT)
        return ramp(v * .85, COAT)
    if material == 'band':
        if side in ('north', 'east', 'west', 'south'):
            return I['e1'] if (px + gx) % 5 == 0 else ramp(v, IRON)
        return ramp(v, IRON)
    if material == 'jaw':
        if side == 'up':
            return I['e3'] if 0 < px < w - 1 else I['e2']
        if side == 'north':
            return I['fe2'] if px % 2 == 0 else I['fe1']
        return ramp(v * .8, COAT)
    if material == 'flue':
        return ramp(v, IRON)
    if material == 'flue_tip':
        if side == 'up':
            return I['e3'] if 0 < px < w - 1 and 0 < py < h - 1 else I['e2']
        return I['e1'] if py == 0 else I['fe1']
    if material == 'glove':
        return ramp(v, LEATHER)
    if material == 'bone':
        return I['b1'] if side in ('north', 'up') or v > .55 else I['b0']
    if material == 'ember':
        if side == 'down':
            return I['e1']
        return I['e3'] if side == 'up' or noise(gx, gy, 29) > .5 else I['e2']
    if material == 'flame':
        # The breath's body: a hot heart, licks of orange and red at the edges.
        edge = min(px, py, w - 1 - px, h - 1 - py)
        r = noise(gx, gy, 30)
        if edge == 0:
            return I['e1'] if r > .3 else I['e0']
        if edge == 1:
            return I['e2'] if r > .2 else I['e1']
        return I['e3']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterChoreography;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterOrdealGeometry;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_fire_eater.py; regenerate the model, atlas and BBModel together.
/**
 * The Fire Breather's true self: the masked performer made a flue, standing on the pulpit past the ring and breathing
 * into it. At rest they stand square to the ring with the torch held up by the mouth, breathing slowly, the flues
 * smoking, the head following the fighter. Each intervention is a gesture ({@link CircusFireEaterEntity#gesture}) timed
 * from its wind-up, every one a slow fill, a fast loosing and a heavy recovery: the long breath (head back, chest
 * filled, then thrown forward over the torch), the spat embers (three sharp snaps of the head), the ring of fire (arms
 * raised wide, flung down and out as it lights, then drawn in as it closes) and the cinder rain (a crouch, then flung up
 * at the sky). The breath itself has a body ({@link #renderBreath}): eighteen flame parts laid along its path from the
 * mouth, drawn as light by the renderer.
 */
public final class CircusTrueFireEaterModel extends EntityModel<CircusFireEaterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_fire_eater"), "main");
    private static final int FLAMES = 18;
    /** A blow's hit-stop and recovery, in ticks. */
    private static final float HELD = 6.0F, RECOVER = 18.0F;
    private final ModelPart root, pelvis, chest, neck, head, jaw, torch;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], tails = new ModelPart[2], skirts = new ModelPart[2], flues = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][3], tips = new ModelPart[2][3];
    private final ModelPart[] flames = new ModelPart[FLAMES];
    /** This frame's body (to bring world points into model space) and its breath: where each flame is, and how big. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw, age;
    private final Vec3[] flameAt = new Vec3[FLAMES];
    private final float[] flameSize = new float[FLAMES];

    public CircusTrueFireEaterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            tails[i] = pelvis.getChild(side + "_tail");
            skirts[i] = pelvis.getChild(side + "_skirt");
            flues[i] = head.getChild(side + "_flue");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            for (int f = 0; f < 3; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
        torch = hands[0].getChild("torch");
        for (int k = 0; k < FLAMES; k++) {
            flames[k] = bakedRoot.getChild("flame_" + k);
            flameAt[k] = Vec3.ZERO;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CircusFireEaterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        this.age = age;
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        java.util.Arrays.fill(flameSize, 0.0F);
        float breath = Mth.sin(age * .06F);
        float walk = Mth.clamp(movement, 0.0F, 1.0F), stride = limbSwing * .6662F;
        chest.xRot += .06F + breath * .025F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        jaw.xRot += .06F + Math.max(0.0F, breath) * .05F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1, swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot = swing * .6F * walk;
            shins[i].xRot = Math.max(0.0F, -swing) * .9F * walk;
            tails[i].xRot += Mth.sin(age * .07F + i * 1.3F) * .05F + walk * .25F;
            skirts[i].xRot += Math.min(0.0F, thighs[i].xRot) * .8F + Mth.sin(age * .08F + i) * .03F;
            flues[i].xRot += Mth.sin(age * .09F + i * 2.0F) * .04F;
            uppers[i].xRot = i == 0 ? -.55F : -.12F - swing * .25F * walk;
            uppers[i].zRot = -sign * (i == 0 ? .05F : .12F);
            fores[i].xRot = i == 0 ? -1.35F : -.3F;
            curl(i, i == 0 ? 1.35F : .45F);
        }
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 3; f++) {
            fingers[i][f].xRot = -amount * (1.0F + f * .05F);
            tips[i][f].xRot = -amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .5F;
    }

    /** 0 until two and a half ticks before {@code at}, then 1 at {@code at}, accelerating like a thrown thing. */
    private static float loose(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 until {@code end} and the hit-stop after it, falling to 0 over the recovery. */
    private static float held(float since, float end) {
        return 1.0F - HemoEasing.smoothstep(end + HELD, end + HELD + RECOVER, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        switch (gesture) {
            case CircusFireEaterEntity.GESTURE_BREATH -> longBreath(since, target);
            case CircusFireEaterEntity.GESTURE_SPIT -> spit(since);
            case CircusFireEaterEntity.GESTURE_RING -> ring(since, target);
            case CircusFireEaterEntity.GESTURE_RAIN -> rain(since);
            default -> {
            }
        }
    }

    /** Head back and chest filled over the torch, a tremble at the top, then thrown forward into the breath. */
    private void longBreath(float since, Vec3 target) {
        float w = FireEaterChoreography.BREATH_IMPACT;
        float run = (float) Math.ceil(FireEaterOrdealGeometry.BREATH_LENGTH / FireEaterOrdealGeometry.BREATH_SPEED);
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since), down = loose(since, w), hold = held(since, w + run);
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        float fill = gather * (1.0F - down);
        chest.xRot += hold * (-.38F * fill + .5F * down);
        chest.xScale = chest.zScale = 1.0F + .1F * fill;
        neck.xRot += hold * (-.25F * fill + .15F * down);
        head.xRot += hold * (-.55F * fill + .02F * tremble + .3F * down);
        jaw.xRot += hold * (.12F * fill + .95F * down);
        // The torch brought up before the mouth; the free arm thrown back for balance as the breath goes.
        uppers[0].xRot = Mth.lerp(hold * gather, uppers[0].xRot, Mth.lerp(down, -2.2F, -1.55F));
        fores[0].xRot = Mth.lerp(hold * gather, fores[0].xRot, Mth.lerp(down, -.9F, -.35F));
        uppers[1].xRot = Mth.lerp(hold * gather, uppers[1].xRot, Mth.lerp(down, -.35F, .7F));
        uppers[1].zRot = Mth.lerp(hold * gather, uppers[1].zRot, -.5F);
        for (int i = 0; i < 2; i++) {
            thighs[i].xRot += -.3F * down * hold;
            shins[i].xRot += .45F * down * hold;
        }
        Vec3 mouth = mouth();
        if (since < w) {
            // The breath held in the mouth, swelling.
            held(mouth, .35F + .7F * gather);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + run + 2.0F, w + run + 8.0F, since);
        if (show <= 0.0F) return;
        Vec3 start = model(target);
        Vec3 direction = target.subtract(at).multiply(1.0D, 0.0D, 1.0D).normalize();
        double front = FireEaterOrdealGeometry.breathFront((int) (since - w) + 1);
        Vec3 tip = model(target.add(direction.scale(front)).add(0.0D, .5D, 0.0D));
        lay(mouth, start, tip, .5F, 1.8F, show);
    }

    /** Head drawn back, then three sharp snaps of the head and jaw, one for each ember spat. */
    private void spit(float since) {
        float gather = HemoEasing.smoothstep(0.0F, 12.0F, since);
        float last = FireEaterChoreography.SPIT_IMPACT + 2 * FireEaterChoreography.SPIT_GAP;
        float hold = held(since, last);
        float snap = 0.0F;
        for (int i = 0; i < 3; i++) {
            float leaves = FireEaterChoreography.SPIT_IMPACT + i * FireEaterChoreography.SPIT_GAP - FireEaterChoreography.SPIT_FLIGHT;
            snap = Math.max(snap, loose(since, leaves) * (1.0F - HemoEasing.smoothstep(leaves + 1.0F, leaves + 5.0F, since)));
        }
        head.xRot += hold * (-.35F * gather * (1.0F - snap) + .45F * snap);
        neck.xRot += hold * .2F * snap;
        jaw.xRot += hold * (.2F * gather + .75F * snap);
        chest.xRot += hold * (-.1F * gather + .18F * snap);
        uppers[0].xRot = Mth.lerp(hold * gather, uppers[0].xRot, -1.95F);
        fores[0].xRot = Mth.lerp(hold * gather, fores[0].xRot, -.85F);
        if (snap > .05F) held(mouth(), .5F * snap);
    }

    /** Arms raised wide, flung down and out as the ring lights, then drawn in as it closes, and held. */
    private void ring(float since, Vec3 target) {
        float w = FireEaterChoreography.RING_IMPACT, close = FireEaterOrdealGeometry.RING_CLOSE_TICKS;
        float raise = HemoEasing.smoothstep(0.0F, w - 6.0F, since), down = loose(since, w);
        float hold = held(since, w + close + FireEaterChoreography.RING_HOLD);
        float closing = HemoEasing.smoothstep(w + 4.0F, w + close, since);
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float z = Mth.lerp(down, 2.7F + .05F * tremble, Mth.lerp(closing, 1.35F, .45F));
            float x = Mth.lerp(down, -.25F, Mth.lerp(closing, -.25F, -1.35F));
            uppers[i].zRot = Mth.lerp(hold * raise, uppers[i].zRot, -sign * z);
            uppers[i].xRot = Mth.lerp(hold * raise, uppers[i].xRot, x);
            fores[i].xRot = Mth.lerp(hold * raise, fores[i].xRot, Mth.lerp(closing, -.15F, -.75F));
            curl(i, i == 0 ? 1.35F : Mth.lerp(hold * raise, .45F, Mth.lerp(down, .1F, .9F)));
        }
        chest.xRot += hold * (-.22F * raise * (1.0F - down) + .3F * down);
        head.xRot += hold * (-.3F * raise * (1.0F - down) + .35F * down);
        jaw.xRot += hold * (.95F * down * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 14.0F, since)) + .25F * down);
        Vec3 mouth = mouth();
        if (since < w) {
            held(mouth, .25F + .45F * raise);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 10.0F, since);
        if (show <= 0.0F) return;
        Vec3 toward = at.subtract(target).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 rim = model(target.add(toward.scale(FireEaterOrdealGeometry.RING_OPEN)).add(0.0D, .4D, 0.0D));
        lay(mouth, rim, rim, .6F, 2.2F, show);
    }

    /** A crouch with the head bowed and the arms hugged in, then flung up at the sky as the cinders go up. */
    private void rain(float since) {
        float w = FireEaterChoreography.RAIN_IMPACT;
        float down = loose(since, w), hold = held(since, w + 10.0F);
        float crouch = HemoEasing.smoothstep(0.0F, w - 5.0F, since) * (1.0F - down) * hold;
        float flung = down * hold;
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        pelvis.y += 4.0F * crouch;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].xRot += -.7F * crouch;
            shins[i].xRot += 1.3F * crouch;
            uppers[i].xRot = Mth.lerp(Math.max(crouch, flung), uppers[i].xRot, Mth.lerp(down, -.7F + .04F * tremble, -2.9F));
            uppers[i].zRot = Mth.lerp(Math.max(crouch, flung), uppers[i].zRot, Mth.lerp(down, sign * .35F, -sign * .55F));
            fores[i].xRot = Mth.lerp(Math.max(crouch, flung), fores[i].xRot, Mth.lerp(down, -1.6F, -.2F));
        }
        chest.xRot += .45F * crouch - .45F * flung;
        head.xRot += .4F * crouch - 1.0F * flung;
        jaw.xRot += 1.05F * flung;
        Vec3 mouth = mouth();
        if (since < w) {
            held(mouth, .3F + .6F * crouch);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 10.0F, since);
        if (show <= 0.0F) return;
        // Up: model space's -y, as high as the cinders go before they fall.
        Vec3 sky = mouth.add(0.0D, -10.0D / FireEaterChoreography.SCALE * 16.0D, 0.0D);
        lay(mouth, mouth.lerp(sky, .5D), sky, .8F, 2.8F, show);
    }

    /** The breath held in the mouth: two flames turning there, swelling with the fill. */
    private void held(Vec3 mouth, float size) {
        flameAt[0] = mouth;
        flameAt[1] = mouth.add(0.0D, -.5D, -.5D);
        flameSize[0] = size;
        flameSize[1] = size * .6F;
    }

    /**
     * Lays the flames from the mouth along a sagging arc to {@code bend} (where the breath meets the floor) and on to
     * {@code tip}, growing from {@code from} to {@code to} in size, all at {@code show}.
     */
    private void lay(Vec3 mouth, Vec3 bend, Vec3 tip, float from, float to, float show) {
        // In model space up is -y: the arc rises a little out of the mouth before it falls.
        Vec3 control = mouth.lerp(bend, .5D).add(0.0D, -8.0D, 0.0D);
        double arc = mouth.distanceTo(control) + control.distanceTo(bend), run = bend.distanceTo(tip), total = arc + run;
        if (total < 1.0E-3D) return;
        for (int k = 0; k < FLAMES; k++) {
            double along = (k + .5D) / FLAMES * total;
            Vec3 point;
            if (along <= arc) {
                double t = along / arc;
                point = mouth.scale((1 - t) * (1 - t)).add(control.scale(2 * t * (1 - t))).add(bend.scale(t * t));
            } else {
                point = bend.lerp(tip, (along - arc) / Math.max(1.0E-3D, run));
            }
            float flicker = 1.0F + .15F * Mth.sin(age * 1.7F + k * 2.3F);
            flameAt[k] = point;
            flameSize[k] = Mth.lerp(k / (float) (FLAMES - 1), from, to) * flicker * show;
        }
    }

    /** The mouth, in model pixels: the jaw's front through this frame's pose. */
    private Vec3 mouth() {
        PoseStack stack = new PoseStack();
        root.translateAndRotate(stack);
        pelvis.translateAndRotate(stack);
        chest.translateAndRotate(stack);
        neck.translateAndRotate(stack);
        head.translateAndRotate(stack);
        jaw.translateAndRotate(stack);
        Vector4f point = new Vector4f(0.0F, .05F, -.3F, 1.0F).mul(stack.last().pose());
        return new Vec3(point.x * 16.0F, point.y * 16.0F, point.z * 16.0F);
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / FireEaterChoreography.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    /** Draws this frame's breath: each flame turning where it lies, at its size; nothing when no breath shows. */
    public void renderBreath(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int k = 0; k < FLAMES; k++) {
            float size = flameSize[k];
            if (size <= .01F) continue;
            ModelPart flame = flames[k];
            flame.setPos((float) flameAt[k].x, (float) flameAt[k].y, (float) flameAt[k].z);
            flame.setRotation(age * .35F + k * 1.7F, age * .5F + k, k * .9F);
            flame.xScale = flame.yScale = flame.zScale = size;
            flame.render(pose, consumer, light, overlay, color);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 256
    return build_rig('circus_true_fire_eater', 'CircusTrueFireEaterModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
