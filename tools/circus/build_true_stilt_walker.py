"""The Stilt Walker's true self (2026-11-17): the professor as they are in their own mind, walking round the Scarlet
Mummer's ring in their Chamber of Will, about twelve blocks tall.

The same performer as the physical Stilt Walker: the long dark coat with its tails, the crimson waistcoat, the top hat
with its band and plume, the dark face with a band of red eyes. What the true self changes is the legs: no boots
strapped to stilts any more. The trousers end at the knee, and below the knee they ARE stilt, lacquered blood-wood with
two hematic iron bands and an iron ferrule, grown out of raw flesh that seeps red at the join. Long arms in bone-white
gloves with long fingers; two cords hanging down the back.

The rig is jointed: hips, waist, chest, neck, head and hat; thigh and stilt-shin (the knee) a side; shoulder, upper arm,
forearm (elbow), hand (wrist), four two-segment fingers and a two-segment thumb a side; two coat tails. The root is the
stilts' tips at model y 24, the entity's own feet; drawn `OrdealStiltWalk.SCALE` (1.6) times over, a texel is a tenth
of a block. Round forms are shaded lit from above, each face darker toward its edges, never in columns.
"""
import puppet_rig
from puppet_rig import ROOT, build as build_rig, noise

puppet_rig.MODELS = ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc'
puppet_rig.TEXTURES = 'textures/entity/npc/harbinger/circus/'
puppet_rig.BBMODELS = ROOT / 'src/main/resources/assets/hemomancy/models/entity/bbmodel/npc/harbinger/circus'

NAMES = ['ink', 'c0', 'c1', 'c2', 'c3', 'r0', 'r1', 'r2', 'r3', 'b0', 'b1', 'b2',
         'w0', 'w1', 'w2', 'w3', 'fe0', 'fe1', 'fe2', 'v0', 'v1', 'g0', 'm0', 'm1', 'm2']
PALETTE = ['#120d12', '#18151d', '#221e28', '#2f2a36', '#443d4c',
           '#4a0c1a', '#741427', '#9e1c33', '#c4324a',
           '#a89a7e', '#d6caac', '#efe6d0',
           '#3a1216', '#58191d', '#7a2626', '#9c3a30',
           '#2a2528', '#4a4246', '#7a6e72',
           '#4a0c16', '#7e1a28', '#ff4a3a',
           '#0f0c11', '#1d1820', '#2c2530']
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0']}

# The front of the head, 8 by 10: a long dark face, a band of crimson across the eyes with two burning slits.
FACE = [
    'dddddddd',
    'dmmmmmmd',
    'rrrrrrrr',
    'rgGrrGgr',
    'drrrrrrd',
    'mmmssmmm',
    'mmmssmmm',
    'dmmmmmmd',
    'dmkkkkmd',
    'ddmmmmdd',
]
FACE_KEYS = {'d': 'm0', 'm': 'm1', 's': 'm2', 'r': 'r1', 'g': 'r3', 'G': 'g0', 'k': 'ink'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('hips', 'root', (0, -62, 0), [box((-6, -2, -3.5), (12, 5, 7), 'trouser')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_thigh', 'hips', (sign * 3.5, 1, 0), [box((-2.25, 0, -2.25), (4.5, 22, 4.5), 'trouser')])
        # Below the knee the leg is stilt: raw flesh at the join, then lacquered wood, two iron bands, a ferrule.
        joint(side + '_shin', side + '_thigh', (0, 22, 0), [
            box((-2.5, -1, -2.5), (5, 4, 5), 'join'),
            box((-1.5, 3, -1.5), (3, 30, 3), 'stilt'),
            box((-1.75, 10, -1.75), (3.5, 1.5, 3.5), 'band'),
            box((-1.75, 23, -1.75), (3.5, 1.5, 3.5), 'band'),
            box((-1, 33, -1), (2, 4, 2), 'stilt'),
            box((-1.25, 37, -1.25), (2.5, 2, 2.5), 'iron')])
    joint('waist', 'hips', (0, -2, 0), [
        box((-5, -11, -3), (10, 11, 6), 'waistcoat'),
        box((-5.5, -11, 1), (11, 12, 2.5), 'coat'),
        box((-5.75, -11, -3.25), (1.75, 12, 4.5), 'coat'),
        box((4, -11, -3.25), (1.75, 12, 4.5), 'coat')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_tail', 'waist', (sign * 2.75, 0, 3), [box((-2.5, 0, -.5), (5, 24, 1), 'coat')], (6, 0, sign * 4))
    joint('chest', 'waist', (0, -11, 0), [
        box((-7, -12, -3.5), (14, 12, 7), 'coat'),
        box((-3, -11, -4), (6, 11, 1), 'waistcoat')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_lapel', 'chest', (sign * 3.5, -11, -4), [box((-1, 0, -.5), (2, 9, 1), 'lapel')], (0, 0, -sign * 10))
        joint(side + '_cord', 'chest', (sign * 4, -10, 3.5), [box((-.25, 0, -.25), (.5, 20, .5), 'cord')])
    joint('neck', 'chest', (0, -12, 0), [box((-1.5, -3, -1.5), (3, 3, 3), 'skin')])
    joint('head', 'neck', (0, -3, 0), [box((-4, -10, -4), (8, 10, 8), 'head')])
    joint('hat', 'head', (0, -10, 0), [
        box((-6.5, -1, -6.5), (13, 1, 13), 'brim'),
        box((-4.5, -15, -4.5), (9, 14, 9), 'hat'),
        box((-4.75, -4, -4.75), (9.5, 2.5, 9.5), 'hatband')], (-5, 0, 4))
    joint('plume', 'hat', (2.5, -13, 2), [box((-.5, -8, -1), (1, 8, 4), 'plume')], (-22, 0, 10))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_upper', 'chest', (sign * 8.5, -10, 0), [
            box((-2.5, -1.5, -2.5), (5, 3, 5), 'coat'),
            box((-1.75, 0, -1.75), (3.5, 16, 3.5), 'sleeve')])
        joint(side + '_fore', side + '_upper', (0, 16, 0), [
            box((-1.5, 0, -1.5), (3, 13, 3), 'sleeve'),
            box((-1.75, 11, -1.75), (3.5, 3, 3.5), 'cuff')])
        # The palm faces in toward the body.
        joint(side + '_hand', side + '_fore', (0, 14, 0), [box((-.75, 0, -1.75), (1.5, 4, 3.5), 'glove')])
        for digit, z in enumerate((-1.5, -.75, 0, .75)):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 4, z + .25), [box((-.5, 0, -.25), (1, 3, .75), 'glove')])
            joint(name + '_tip', name, (0, 3, 0), [box((-.5, 0, -.25), (1, 2, .75), 'glove')])
        joint(side + '_thumb', side + '_hand', (0, 1, -1.75), [box((-.5, 0, -.5), (1, 2.5, 1), 'glove')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 2.5, 0), [box((-.5, 0, -.5), (1, 1.75, 1), 'glove')])
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


def tone(v, names):
    """Picks from dark to light along `names` by the lit value."""
    steps = len(names)
    index = min(steps - 1, max(0, int(v * steps)))
    return I[names[index]]


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material in ('coat', 'sleeve', 'trouser'):
        # Dark wool, its folds catching the light from above; a few loose crimson threads in the weave.
        if noise(gx, gy, 21) < .015 and side not in ('up', 'down'):
            return I['r0']
        return tone(v - (.08 if material == 'trouser' else 0), ['ink', 'c0', 'c1', 'c2', 'c3'])
    if material == 'lapel':
        return tone(v + .1, ['c1', 'c2', 'c3', 'c3'])
    if material == 'waistcoat':
        if side == 'north' and px == w // 2 and py % 3 == 1:
            return I['b1']
        return tone(v, ['r0', 'r1', 'r2', 'r3'])
    if material == 'cuff':
        return tone(v, ['r0', 'r1', 'r2', 'r3'])
    if material == 'glove':
        return tone(v, ['b0', 'b0', 'b1', 'b2'])
    if material == 'join':
        # Where the flesh becomes stilt: raw, and seeping where it grips the wood.
        if side not in ('up', 'down') and noise(gx, gy, 31) < .22:
            return I['g0']
        return tone(v, ['v0', 'v1', 'r1', 'r2'])
    if material == 'stilt':
        # Lacquered blood-wood, mottled, with now and then a vein of their blood showing through the lacquer.
        if side not in ('up', 'down') and noise(gx, gy, 33) < .03:
            return I['g0']
        if noise(gx, gy, 34) < .06:
            return I['w0']
        return tone(v, ['w0', 'w1', 'w2', 'w3'])
    if material in ('band', 'iron'):
        if side == 'up' or py == 0:
            return I['fe2']
        if side == 'down' or py == h - 1:
            return I['fe0']
        return I['fe1'] if v < .7 else I['fe2']
    if material == 'skin':
        return tone(v, ['m0', 'm1', 'm2'])
    if material == 'head':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if side in ('east', 'west') and py in (2, 3, 4):
            return I['r1'] if py != 3 else I['r0']
        return tone(v, ['m0', 'm1', 'm2'])
    if material == 'hat':
        return tone(v - .1, ['ink', 'c0', 'c1', 'c2'])
    if material == 'brim':
        return I['c2'] if side == 'up' else I['ink'] if side == 'down' else I['c0']
    if material == 'hatband':
        # Crimson silk with a bone thread along its top edge; never gold.
        if side not in ('up', 'down') and py == 0:
            return I['b0']
        return tone(v, ['r0', 'r1', 'r2'])
    if material == 'plume':
        return tone(v, ['r0', 'r1', 'b0', 'b1'])
    if material == 'cord':
        return I['r2'] if (gy // 2) % 2 == 0 else I['b0']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealStiltWalk;
import com.vincenthuto.hemomancy.common.encounter.ordeal.StiltWalkerMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_stilt_walker.py; regenerate the model, atlas and BBModel together.
/**
 * The Stilt Walker's true self: about twelve blocks tall, their legs stilt from the knee down, walking round the
 * Mummer's ring one long stride at a time (the synced stride count drives the legs). Each intervention is a gesture
 * ({@link CircusStiltWalkerEntity#gesture}) timed from its wind-up; their own body makes the motion where it stands,
 * and spectral copies of it, drawn by {@link #renderGhosts}, do it in the ring: a stilt-leg planted like a spear, two
 * walking four footfalls across, two stilts laid across the ring and spun, and their whole shadow falling.
 */
public final class CircusTrueStiltWalkerModel extends EntityModel<CircusStiltWalkerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_stilt_walker"), "main");
    /** A leg's length in pixels, thigh and stilt-shin; and a stilt-shin's alone. */
    private static final float LEG = 61.0F, SHIN = 39.0F;
    /** The model's height from the stilts' tips to the plume, in pixels. */
    private static final float HEIGHT = 121.0F;
    private final ModelPart root, hips, waist, chest, neck, head, hat;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], tails = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2],
            thumbTips = new ModelPart[2], cords = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;

    public CircusTrueStiltWalkerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        waist = hips.getChild("waist");
        chest = waist.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        hat = head.getChild("hat");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            tails[i] = waist.getChild(side + "_tail");
            cords[i] = chest.getChild(side + "_cord");
            uppers[i] = chest.getChild(side + "_upper");
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
    public void setupAnim(CircusStiltWalkerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(age, yaw, pitch, entity.stride(partialTick), entity.gesture(), entity.gestureAge(partialTick),
                entity.gestureAt(), entity.gestureAim());
    }

    /** The whole pose: the walk from {@code stride}, then gesture {@code gesture} (-1 at rest) {@code since} ticks in. */
    public void pose(float age, float yaw, float pitch, float stride, int gesture, float since, Vec3 target, Vec3 aim) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        ghostLegs[0] = ghostLegs[1] = ghostSpin = ghostShadow = 0.0F;
        float weight = gesture >= 0 ? gestureWeight(gesture, since) : 0.0F;
        walk(age, stride, 1.0F - weight);
        neck.yRot += Mth.clamp(yaw, -75, 75) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -75, 75) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -15, 55) * Mth.DEG_TO_RAD * .8F;
        neck.xRot += Mth.clamp(pitch, -15, 55) * Mth.DEG_TO_RAD * .2F;
        if (gesture >= 0 && weight > 0.0F) gesture(gesture, since, weight, target, aim, age);
    }

    /** How strongly a gesture holds the body: in over six ticks, out over a heavy recovery after the move ends. */
    private static float gestureWeight(int gesture, float since) {
        float end = switch (gesture) {
            case CircusStiltWalkerEntity.GESTURE_PLANT -> StiltWalkerMoves.PLANT_WINDUP + StiltWalkerMoves.PLANT_HOLD;
            case CircusStiltWalkerEntity.GESTURE_STRIDE -> StiltWalkerMoves.strideLands(StiltWalkerMoves.STRIDE_STEPS - 1) + 6;
            case CircusStiltWalkerEntity.GESTURE_SPIN -> StiltWalkerMoves.SPIN_WINDUP + StiltWalkerMoves.SPIN_TICKS;
            default -> StiltWalkerMoves.SHADOW_WINDUP + StiltWalkerMoves.SHADOW_REST;
        };
        return HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(end, end + 18.0F, since));
    }

    /**
     * One stride: the swinging stilt lifted from behind, its knee bending, and planted in front as the body passes over
     * the other; the hips dip where both are splayed, the coat tails and arms swing against it.
     */
    private void walk(float age, float stride, float amount) {
        int step = Mth.floor(stride);
        float e = HemoEasing.smoothstep(stride - step);
        float lift = Mth.sin(e * Mth.PI);
        int swing = Math.floorMod(step, 2);
        float reach = .28F * amount;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            if (i == swing) {
                thighs[i].xRot = Mth.lerp(e, reach, -reach) - .3F * lift * amount;
                shins[i].xRot = .55F * lift * amount;
            } else {
                thighs[i].xRot = Mth.lerp(e, -reach, reach);
            }
            thighs[i].zRot = sign * .03F;
            uppers[i].xRot = -thighs[i].xRot * .6F;
            uppers[i].zRot = -sign * (.12F + .03F * Mth.sin(age * .05F + i));
            fores[i].xRot = -.25F;
            hands[i].yRot = sign * 1.2F;
            curl(i, .45F);
            tails[i].xRot += .12F * lift * amount + .04F * Mth.sin(age * .07F + i * 1.7F);
            tails[i].zRot += .05F * Mth.sin(age * .05F + i);
        }
        hips.y += (1.0F - lift) * 2.4F * amount;
        float toward = swing == 0 ? 1 : -1;
        hips.zRot += toward * .035F * lift * amount;
        chest.zRot -= toward * .05F * lift * amount;
        chest.xRot += .05F + .02F * Mth.sin(age * .05F);
        head.zRot += toward * .02F * lift * amount;
        for (ModelPart cord : cords) cord.zRot = Mth.sin(age * .06F + cord.x) * .08F;
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

    /** This frame's spectral limbs: each ghost leg's weight and the point its tip stands on (model pixels). */
    private final float[] ghostLegs = new float[2];
    private final Vec3[] legTips = {Vec3.ZERO, Vec3.ZERO};
    /** The spun stilts: their weight, the ring's centre and the direction they lie along (model space). */
    private float ghostSpin;
    private Vec3 spinCentre = Vec3.ZERO, spinDir = new Vec3(1.0D, 0.0D, 0.0D);
    /** The long shadow: its weight, where it stands (model pixels), the way it falls (model space), how far it has tipped. */
    private float ghostShadow, shadowTip;
    private Vec3 shadowFoot = Vec3.ZERO, shadowDir = new Vec3(0.0D, 0.0D, -1.0D);

    /** 0 until {@code length} ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float drop(float since, float at, float length) {
        float t = Mth.clamp((since - (at - length)) / length, 0.0F, 1.0F);
        return t * t;
    }

    private void gesture(int gesture, float since, float weight, Vec3 target, Vec3 aim, float age) {
        switch (gesture) {
            case CircusStiltWalkerEntity.GESTURE_PLANT -> {
                // Their right leg hauled up high toward the ring, trembling at the top, then driven down like a spear;
                // its spectral copy comes down out of the dark and stands in the floor where it struck.
                float w = StiltWalkerMoves.PLANT_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since), down = drop(since, w, 2.5F);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 6.0F, w - 3.0F, since) * (1.0F - down);
                thighs[0].xRot = Mth.lerp(weight, thighs[0].xRot, Mth.lerp(down, -1.55F + .05F * tremble, -.7F));
                shins[0].xRot = Mth.lerp(weight, shins[0].xRot, Mth.lerp(down, .9F * gather, 0.0F));
                thighs[1].xRot = Mth.lerp(weight, thighs[1].xRot, .12F * gather);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * Mth.lerp(down, 1.25F, .7F));
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, -.2F);
                    curl(i, Mth.lerp(weight, .45F, .1F));
                }
                chest.xRot += weight * (-.18F * gather * (1.0F - down) + .3F * down);
                hips.xRot += weight * .12F * down;
                head.xRot += weight * .15F * down;
                ghostLeg(0, target.add(0.0D, Mth.lerp(down, 7.0D + .2D * tremble, 0.0D) * (1.0F - gather * .25F), 0.0D), weight);
            }
            case CircusStiltWalkerEntity.GESTURE_STRIDE -> {
                // Their body rocks with each footfall as the two spectral legs walk the chord, one planted while the
                // other swings over to the next footfall.
                for (int leg = 0; leg < 2; leg++) {
                    Vec3 tip = strideTip(leg, since, target, aim);
                    if (tip != null) ghostLeg(leg, tip, weight);
                }
                float beat = 0.0F;
                for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++)
                    beat = Math.max(beat, 1.0F - Mth.clamp(Math.abs(since - StiltWalkerMoves.strideLands(k)) / 4.0F, 0.0F, 1.0F));
                int next = Mth.clamp((int) ((since - StiltWalkerMoves.STRIDE_WINDUP) / StiltWalkerMoves.STRIDE_GAP) + 1, 0,
                        StiltWalkerMoves.STRIDE_STEPS - 1);
                int lifting = next % 2;
                thighs[lifting].xRot = Mth.lerp(weight, thighs[lifting].xRot, -.75F * (1.0F - beat));
                shins[lifting].xRot = Mth.lerp(weight, shins[lifting].xRot, .7F * (1.0F - beat));
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * .6F);
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, (i == lifting ? .5F : -.5F));
                }
                chest.xRot += weight * (.2F + .15F * beat);
                hips.y += weight * 1.5F * beat;
            }
            case CircusStiltWalkerEntity.GESTURE_SPIN -> {
                // Up onto the right stilt, the left swung out level, arms wide; wound back through the gather, then a
                // whole pirouette while the spectral stilts in the ring turn half a circle.
                float w = StiltWalkerMoves.SPIN_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 2.0F, since);
                float turning = HemoEasing.smoothstep(HemoEasing.clamp01((since - w) / StiltWalkerMoves.SPIN_TICKS));
                thighs[0].xRot = Mth.lerp(weight, thighs[0].xRot, 0.0F);
                shins[0].xRot = Mth.lerp(weight, shins[0].xRot, 0.0F);
                thighs[1].xRot = Mth.lerp(weight, thighs[1].xRot, 0.0F);
                thighs[1].zRot = Mth.lerp(weight, thighs[1].zRot, -1.45F * gather);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, -sign * 1.5F);
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, 0.0F);
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, 0.0F);
                    curl(i, Mth.lerp(weight, .45F, .05F));
                    tails[i].zRot += sign * .9F * turning * (1.0F - turning) * 4.0F * weight;
                }
                root.x += 3.5F * weight;
                root.yRot += weight * (.7F * gather * (1.0F - turning) - Mth.TWO_PI * turning);
                hips.y -= 1.5F * gather * weight;
                ghostSpin = weight * Math.max(gather, 1.0F - HemoEasing.smoothstep(w + StiltWalkerMoves.SPIN_TICKS,
                        w + StiltWalkerMoves.SPIN_TICKS + 10.0F, since));
                double angle = StiltWalkerMoves.spinAngle(Math.atan2(aim.z, aim.x), Math.max(0.0F, since - w));
                spinCentre = model(target.add(0.0D, .5D, 0.0D));
                spinDir = model(target.add(Math.cos(angle), .5D, Math.sin(angle))).subtract(spinCentre).normalize();
            }
            case CircusStiltWalkerEntity.GESTURE_FALL -> {
                // They lean out over the ring and push their own shadow over: it stands up at the rim, sways, and
                // falls across the ring like a felled pole.
                float w = StiltWalkerMoves.SHADOW_WINDUP;
                float lean = HemoEasing.smoothstep(0.0F, w, since);
                float shove = drop(since, w, 4.0F);
                chest.xRot += weight * (.35F * lean + .2F * shove);
                hips.xRot += weight * .1F * lean;
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(shove, -1.3F * lean, -1.75F));
                    uppers[i].zRot = Mth.lerp(weight, uppers[i].zRot, (i == 0 ? 1 : -1) * .12F);
                    fores[i].xRot = Mth.lerp(weight, fores[i].xRot, -.15F);
                    curl(i, Mth.lerp(weight, .45F, .05F));
                }
                ghostShadow = weight * HemoEasing.smoothstep(0.0F, 8.0F, since);
                shadowTip = StiltWalkerMoves.shadowFall(since) + (since < w ? Mth.sin(since * .4F) * .04F * (1.0F - lean) : 0.0F);
                shadowFoot = model(target);
                shadowDir = model(target.add(aim)).subtract(shadowFoot).multiply(1.0D, 0.0D, 1.0D).normalize();
            }
            default -> {
            }
        }
    }

    /**
     * Where spectral leg {@code leg} stands on the stride: the leg for footfall k is k % 2; it comes down out of the dark
     * onto its first footfall, then swings over to its next one while the other leg is planted.
     */
    private static Vec3 strideTip(int leg, float since, Vec3 start, Vec3 chord) {
        Vec3 planted = null;
        for (int k = leg; k < StiltWalkerMoves.STRIDE_STEPS; k += 2) {
            float lands = StiltWalkerMoves.strideLands(k);
            Vec3 step = StiltWalkerMoves.strideStep(start, chord, k);
            float from = k < 2 ? 0.0F : StiltWalkerMoves.strideLands(k - 1);
            if (since >= lands) {
                planted = step;
                continue;
            }
            if (since < from) return planted;
            float t = Mth.clamp((since - from) / Math.max(1.0F, lands - from), 0.0F, 1.0F);
            float fall = t * t;
            if (planted == null) return step.add(0.0D, 8.0D * (1.0F - fall), 0.0D);
            return planted.lerp(step, HemoEasing.smoothstep(t)).add(0.0D, 4.0D * Mth.sin(t * Mth.PI), 0.0D);
        }
        return planted;
    }

    private void ghostLeg(int i, Vec3 world, float weight) {
        ghostLegs[i] = weight;
        legTips[i] = model(world);
    }

    /** The spectral leg against their own, and its length then, in pixels; the spun stilts' and the shadow's sizes. */
    private static final float GHOST_LEG = 1.35F, GHOST_LEG_LENGTH = LEG * GHOST_LEG;
    private static final float GHOST_STILT = (float) (StiltWalkerMoves.RIM * 16.0D / OrdealStiltWalk.SCALE / SHIN);
    private static final float GHOST_SHADOW = (float) ((StiltWalkerMoves.SHADOW_LENGTH + .5D) * 16.0D / OrdealStiltWalk.SCALE / HEIGHT);

    /**
     * Draws the spectral limbs in {@code legTint}: copies of their leg reaching into the ring from above and from their
     * side, the tip standing on this frame's target; the two stilts spun across the ring; and, in {@code shadowTint},
     * their whole shadow standing at the rim and falling. Their real body stays where it is.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int legTint, int shadowTint) {
        for (int i = 0; i < 2; i++) {
            if (ghostLegs[i] <= .01F) continue;
            Vec3 tip = legTips[i];
            Vec3 toward = new Vec3(-tip.x, 0.0D, -tip.z);
            toward = toward.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : toward.normalize();
            // From above and from their side of the ring: in model space up is -y.
            Vec3 dir = toward.scale(-.45D).add(0.0D, .89D, 0.0D).normalize();
            Vec3 origin = tip.subtract(dir.scale(GHOST_LEG_LENGTH));
            PartPose thigh = thighs[i].storePose(), shin = shins[i].storePose();
            thighs[i].setPos((float) origin.x, (float) origin.y, (float) origin.z);
            aim(thighs[i], dir);
            thighs[i].xScale = thighs[i].yScale = thighs[i].zScale = GHOST_LEG;
            shins[i].setRotation(0.0F, 0.0F, 0.0F);
            thighs[i].render(pose, consumer, light, overlay, tint(legTint, ghostLegs[i]));
            thighs[i].loadPose(thigh);
            shins[i].loadPose(shin);
            thighs[i].xScale = thighs[i].yScale = thighs[i].zScale = 1.0F;
        }
        if (ghostSpin > .01F) {
            // One stilt out each way from the centre, knee to knee: together the ring's whole width.
            for (int i = 0; i < 2; i++) {
                PartPose shin = shins[i].storePose();
                shins[i].setPos((float) spinCentre.x, (float) spinCentre.y, (float) spinCentre.z);
                aim(shins[i], spinDir.scale(i == 0 ? -1.0D : 1.0D));
                shins[i].xScale = shins[i].zScale = GHOST_STILT * .9F;
                shins[i].yScale = GHOST_STILT;
                shins[i].render(pose, consumer, light, overlay, tint(legTint, ghostSpin));
                shins[i].loadPose(shin);
                shins[i].xScale = shins[i].yScale = shins[i].zScale = 1.0F;
            }
        }
        if (ghostShadow > .01F) {
            PartPose body = root.storePose();
            root.setPos((float) shadowFoot.x, (float) shadowFoot.y, (float) shadowFoot.z);
            root.setRotation(shadowTip, (float) Math.atan2(-shadowDir.x, -shadowDir.z), 0.0F);
            root.xScale = root.yScale = root.zScale = GHOST_SHADOW;
            root.render(pose, consumer, light, overlay, tint(shadowTint, ghostShadow));
            root.loadPose(body);
            root.xScale = root.yScale = root.zScale = 1.0F;
        }
    }

    /** Turns a part so its own +y (down its length) runs along {@code dir}, in its parent's space. */
    private static void aim(ModelPart part, Vec3 dir) {
        part.setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
    }

    private static int tint(int color, float weight) {
        int alpha = (int) (Mth.clamp(weight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / OrdealStiltWalk.SCALE);
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
    return build_rig('circus_true_stilt_walker', 'CircusTrueStiltWalkerModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
