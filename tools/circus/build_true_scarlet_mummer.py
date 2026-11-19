"""The Scarlet Mummer's true self (2026-11-17): what the Mummer is inside the Stilt Walker's Chamber of Will. The
physical Mummer is a small pageant puppet, a porcelain face under a stepped scarlet cap, a doublet and ruff, and a
folded frill of eyespot fans it opens when it performs. Its true self is the pageant gone wild: taller and long in
the limb, a cracked porcelain mask with a dark face and a burning eye showing through the crack and a grin too wide,
a jester's cockscomb of three horns with bone baubles for its cap, long spidery fingers with claw tips, ribbons of
scarlet hanging from its hips, and the frill grown into a whole train of eyes: two three-part fans and a tall centre
plume, every eyespot's pupil lit. Folded along its back it is a cloak; opened, every eye in the ring looks at you.

The rig is jointed (knees, elbows, wrists, four two-segment fingers and a thumb a side, the fans in three parts, the
ribbons in two) with its feet at model y 24, drawn 1.35 times over (TrueScarletMummerRenderer).
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

PALETTE = ['#140f14', '#2b1a22', '#551c2a', '#8a2333', '#c43a3f', '#f07a5a',
           '#e9c46a', '#f7f0e6', '#cbbfb3', '#8c8079', '#4a3d48', '#6e5a8a',
           '#ffd34a', '#ff4a3a']
INK, D1, D2, RED, RED2, SALMON, YELLOW, WHITE, PORCELAIN, GREY, DUSK, VIOLET, EYE, BURN = range(14)
GLOW = {EYE, BURN}

# The mask, 5 by 7: porcelain cracked down its right side, the dark face under it with a burning eye; a grin too wide.
FACE = [
    'wwpww',
    'wpppc',
    'kyppb',
    'pppcd',
    'prrrd',
    'rkkkr',
    'pprpp',
]
FACE_KEYS = {'w': WHITE, 'p': PORCELAIN, 'c': INK, 'k': INK, 'y': EYE, 'b': BURN, 'd': D1, 'r': RED}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -13, 0), [box((-2.5, -1, -1.5), (5, 3, 3), 'doublet')])
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_thigh', 'pelvis', (sign * 1.5, 2, 0), [box((-1, 0, -1), (2, 6, 2), 'hose')])
        joint(side + '_shin', side + '_thigh', (0, 6, 0), [
            box((-1, 0, -1), (2, 5, 2), 'hose'),
            box((-1, 4, -3), (2, 1, 4), 'shoe'),
            box((-.5, 3, -3.5), (1, 1, 1), 'shoe')])
    for k, (x, z) in enumerate(((-2, -1.25), (-1, 1.25), (0, -1.25), (1, 1.25), (2, -1.25))):
        joint('ribbon_' + str(k), 'pelvis', (x, 1.5, z), [box((-.5, 0, -.25), (1, 6, .5), 'ribbon')], (0, 0, x * 6))
        joint('ribbon_' + str(k) + '_tip', 'ribbon_' + str(k), (0, 6, 0), [box((-.5, 0, -.25), (1, 4, .5), 'ribbon')])
    joint('torso', 'pelvis', (0, -1, 0), [box((-1.5, -5, -1), (3, 5, 2), 'doublet')])
    joint('chest', 'torso', (0, -5, 0), [
        box((-2.5, -6, -1.5), (5, 6, 3), 'doublet'),
        box((-.5, -5.5, -1.75), (1, 5, .25), 'buttons'),
        box((-3.5, -7, -2.5), (7, 1.5, 5), 'ruff')])
    joint('neck', 'chest', (0, -7, 0), [box((-.5, -1.5, -.5), (1, 1.5, 1), 'porcelain')])
    joint('head', 'neck', (0, -1.5, 0), [box((-2.5, -7, -2.5), (5, 7, 5), 'face')])
    joint('hat', 'head', (0, -7, 0), [box((-3, -1, -3), (6, 1, 6), 'cap')])
    for name, pivot, rotation in (('horn_centre', (0, -1, 1), (-25, 0, 0)), ('horn_right', (-2, -1, 0), (0, 0, -40)),
                                  ('horn_left', (2, -1, 0), (0, 0, 40))):
        joint(name, 'hat', pivot, [box((-1, -4, -1), (2, 4, 2), 'cap')], rotation)
        joint(name + '_tip', name, (0, -4, 0), [
            box((-.5, -3, -.5), (1, 3, 1), 'cap'),
            box((-.75, -4.5, -.75), (1.5, 1.5, 1.5), 'bone')], (rotation[0], 0, rotation[2]))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_upper', 'chest', (sign * 3, -5, 0), [
            box((-1.5, -1, -1.5), (3, 2.5, 3), 'puff'),
            box((-.75, 0, -.75), (1.5, 6, 1.5), 'sleeve')])
        joint(side + '_fore', side + '_upper', (0, 6, 0), [
            box((-.75, 0, -.75), (1.5, 6, 1.5), 'sleeve'),
            box((-1.25, 4.5, -1.25), (2.5, 1.5, 2.5), 'ruff')])
        joint(side + '_hand', side + '_fore', (0, 6, 0), [box((-.5, 0, -1), (1, 2, 2), 'glove')])
        for digit, z in enumerate((-.75, -.25, .25, .75)):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (0, 2, z), [box((-.25, 0, -.25), (.5, 3, .5), 'glove')])
            joint(name + '_tip', name, (0, 3, 0), [box((-.25, 0, -.25), (.5, 2, .5), 'claw')])
        joint(side + '_thumb', side + '_hand', (0, .5, -1), [box((-.25, 0, -.25), (.5, 2, .5), 'glove')],
              (-25, 0, -sign * 20))
        joint(side + '_thumb_tip', side + '_thumb', (0, 2, 0), [box((-.25, 0, -.25), (.5, 1.25, .5), 'claw')])
        # The train of eyes: three fans a side, rising from the shoulder blades.
        joint(side + '_fan', 'chest', (sign * 1, -4, 1.5), [box((0 if sign > 0 else -6, -12, 0), (6, 12, 1), 'fan')])
        joint(side + '_fan_outer', side + '_fan', (sign * 6, -8, 0), [box((0 if sign > 0 else -5, -9, 0), (5, 10, 1), 'fan')])
        joint(side + '_fan_tip', side + '_fan_outer', (sign * 5, -5, 0), [box((0 if sign > 0 else -4, -6, 0), (4, 8, 1), 'fan')])
    joint('train', 'chest', (0, -4, 2), [box((-2.5, -14, 0), (5, 14, 1), 'fan')])
    return parts


def lit(side, px, py, w, h, gx, gy):
    if side == 'up':
        return .82
    if side == 'down':
        return .1
    vertical = 1 - py / max(1, h - 1)
    across = 1 - abs((px + .5) / w * 2 - 1)
    v = .22 + .38 * vertical + .32 * across ** .6
    return v + (.05 if side == 'north' else -.08 if side == 'south' else 0) + (noise(gx, gy, 23) - .5) * .12


def pick(v, shades):
    return shades[min(len(shades) - 1, max(0, int(v * len(shades))))]


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    if material == 'face':
        if side == 'north':
            return FACE_KEYS[FACE[py][px]]
        if side == 'east' and noise(gx, gy, 5) < .3:
            return D1
        return pick(v, [GREY, PORCELAIN, PORCELAIN, WHITE])
    if material == 'porcelain':
        return pick(v, [GREY, PORCELAIN, WHITE])
    if material == 'doublet':
        # Scarlet in diamonds, harlequin fashion, darker toward every edge.
        if side not in ('up', 'down') and (px + py) % 4 == 0 and (px - py) % 4 == 0:
            return D2
        return pick(v, [D2, RED, RED, RED2])
    if material == 'buttons':
        return YELLOW if py % 2 == 0 else D2
    if material == 'ruff':
        if side == 'down':
            return GREY
        return WHITE if (px + py) % 2 == 0 and v > .45 else PORCELAIN if v > .3 else GREY
    if material == 'cap':
        return pick(v, [D2, RED, RED2, SALMON])
    if material == 'bone':
        return pick(v, [GREY, PORCELAIN, WHITE])
    if material == 'puff':
        return pick(v, [D2, RED, RED2])
    if material == 'sleeve':
        return pick(v, [D1, D2, RED])
    if material in ('glove',):
        return pick(v, [GREY, PORCELAIN, WHITE])
    if material == 'claw':
        return pick(v, [INK, D1, DUSK])
    if material == 'hose':
        return pick(v, [INK, D1, D2, DUSK])
    if material == 'shoe':
        return pick(v, [INK, D2, RED])
    if material == 'ribbon':
        if py == h - 1 and h > 4:
            return D2
        return pick(v + .1, [D2, RED, RED2, SALMON])
    if material == 'fan':
        if side in ('up', 'down', 'east', 'west'):
            return D2
        # Ocelli: a lit pupil, a yellow ring, a scarlet halo, on a dark membrane; ribbed at the outer edge.
        cx = (w - 1) // 2
        best = min(abs(px - cx) + abs(py - c) for c in range(2, h - 1, 4))
        if best == 0:
            return EYE
        if best == 1:
            return YELLOW
        if best == 2:
            return RED2
        if px in (0, w - 1):
            return D1
        return VIOLET if noise(gx, gy, 9) < .1 else D2 if v > .4 else D1
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.ScarletMummerOrdealMoves;
import com.vincenthuto.hemomancy.common.encounter.ordeal.StiltWalkerMoves;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_scarlet_mummer.py (the Mummer's true self, 2026-11-17); regenerate together.
/**
 * The Scarlet Mummer's true self in the Stilt Walker's Chamber: long-limbed, a cracked mask grinning, a train of
 * eyespot fans folded down its back like a cloak. It prances (high knees, a bob, a twitch of the head, arms flourishing)
 * and its ordeal moves ({@link ScarletMummerOrdealMoves}) are each a slow gather, a fast blow and a recovery: the
 * dazzle throws every fan open at once, the feint steps aside and lunges fingers first, the whirl spins twice with its
 * ribbons out, the entrance crouches, leaps tucked and lands spread.
 */
public class TrueScarletMummerModel extends EntityModel<ScarletMummerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_scarlet_mummer"), "main");
    private final ModelPart root, pelvis, torso, chest, neck, head, hat, train;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], uppers = new ModelPart[2],
            fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2],
            fans = new ModelPart[2], outers = new ModelPart[2], fanTips = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final ModelPart[] ribbons = new ModelPart[5], ribbonTips = new ModelPart[5];

    public TrueScarletMummerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        torso = pelvis.getChild("torso");
        chest = torso.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        hat = head.getChild("hat");
        train = chest.getChild("train");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            fans[i] = chest.getChild(side + "_fan");
            outers[i] = fans[i].getChild(side + "_fan_outer");
            fanTips[i] = outers[i].getChild(side + "_fan_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
        for (int k = 0; k < 5; k++) {
            ribbons[k] = pelvis.getChild("ribbon_" + k);
            ribbonTips[k] = ribbons[k].getChild("ribbon_" + k + "_tip");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        root.getAllParts().forEach(ModelPart::resetPose);
        float move = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        prance(limbSwing, move, age);
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        float flare = entity.isPerforming() ? .55F + .08F * Mth.sin(age * .3F) : .0F;
        fanned(flare, age);
        swipe(attackTime);
        if (entity.ordealMove() >= 0) move(entity.ordealMove(), entity.ordealMoveAge(partialTick), age);
    }

    /** A prance: knees high, a bob on every step, the arms flourishing, a twitch of the head now and then. */
    private void prance(float limbSwing, float move, float age) {
        float stride = limbSwing * .6662F;
        root.y -= Math.abs(Mth.sin(stride)) * 1.2F * move + Mth.sin(age * .09F) * .3F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot = swing * .9F * move - Math.max(0.0F, -swing) * .5F * move;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.3F * move + .05F;
            uppers[i].xRot = -swing * .5F * move - .2F;
            uppers[i].zRot = -sign * (.35F + .15F * Mth.sin(age * .11F + i * 2.0F));
            fores[i].xRot = -.6F - .25F * Mth.sin(age * .13F + i);
            hands[i].zRot = sign * .3F * Mth.sin(age * .17F + i);
            curl(i, .25F + .2F * Mth.sin(age * .07F + i));
        }
        chest.yRot += Mth.cos(stride) * .15F * move;
        chest.xRot += .1F;
        float twitch = Mth.sin(age * .13F) > .93F ? .4F : 0.0F;
        head.zRot += Mth.sin(age * .9F) * .04F + twitch;
        for (int k = 0; k < 5; k++) {
            ribbons[k].xRot += .1F * move + .06F * Mth.sin(age * .1F + k);
            ribbonTips[k].xRot += .12F * Mth.sin(age * .12F + k * 1.3F) + .2F * move;
        }
    }

    /** The fans: folded down along its back at 0, every eye open behind its head at 1, flung past open above 1. */
    private void fanned(float open, float age) {
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            fans[i].xRot = Mth.lerp(Math.min(open, 1.0F), -1.35F, -.15F);
            fans[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.1F, .45F + .25F * Math.max(0.0F, open - 1.0F));
            fans[i].yRot = sign * Mth.lerp(Math.min(open, 1.0F), -.5F, .1F);
            outers[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.5F, .35F) + sign * .03F * Mth.sin(age * .2F + i);
            fanTips[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.6F, .35F);
        }
        train.xRot = Mth.lerp(Math.min(open, 1.0F), -1.2F, -.2F);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .08F);
            tips[i][f].zRot = sign * amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .5F;
    }

    /** Its ordinary blow: the right hand raked across in front of it, claws first. */
    private void swipe(float attack) {
        if (attack <= 0.0F) return;
        float rake = Mth.sin(attack * Mth.PI);
        uppers[0].xRot = Mth.lerp(rake, uppers[0].xRot, -1.7F);
        uppers[0].yRot += rake * .9F - attack * 1.2F;
        fores[0].xRot = Mth.lerp(rake, fores[0].xRot, -.2F);
        curl(0, Mth.lerp(rake, .25F, .05F));
        chest.yRot += -rake * .4F;
    }

    /** 0 until {@code length} ticks before {@code at}, then 1 at {@code at}, accelerating into the blow. */
    private static float snap(float since, float at, float length) {
        float t = Mth.clamp((since - (at - length)) / length, 0.0F, 1.0F);
        return t * t;
    }

    private void move(int move, float since, float age) {
        switch (move) {
            case ScarletMummerOrdealMoves.DAZZLE -> {
                // The fans rise slowly, every eye opening, tremble held at the top, then fling open past full at the blow
                // with its arms thrown wide and its back arched; they settle as it recovers.
                float w = StiltWalkerMoves.DAZZLE_WINDUP;
                float rise = HemoEasing.smoothstep(0.0F, w - 3.0F, since), open = snap(since, w, 2.0F);
                float hold = 1.0F - HemoEasing.smoothstep(w + 5.0F, w + 16.0F, since);
                float tremble = Mth.sin(since * 3.3F) * HemoEasing.smoothstep(w - 6.0F, w - 2.0F, since) * (1.0F - open);
                fanned((.85F * rise + .5F * open + .02F * tremble) * hold, age);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, -sign * Mth.lerp(open, .6F * rise, 1.8F));
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, -.3F);
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, -.2F);
                    curl(i, Mth.lerp(hold * open, .25F, 0.0F));
                }
                chest.xRot += hold * (.2F * rise * (1.0F - open) - .35F * open);
                head.xRot += hold * (.15F * rise * (1.0F - open) - .3F * open);
            }
            case ScarletMummerOrdealMoves.FEINT -> {
                // The quick step aside, a coil low, then the lunge: right arm thrust out straight, fingers first.
                float w = StiltWalkerMoves.FEINT_WINDUP;
                float step = HemoEasing.smoothstep(0.0F, 4.0F, since) * (1.0F - HemoEasing.smoothstep(4.0F, 7.0F, since));
                float coil = HemoEasing.smoothstep(3.0F, w - 1.0F, since), lunge = snap(since, w + 1.0F, 2.0F);
                float hold = 1.0F - HemoEasing.smoothstep(w + StiltWalkerMoves.FEINT_DASH + 3.0F, w + StiltWalkerMoves.FEINT_DASH + 14.0F, since);
                pelvis.zRot += .35F * step;
                thighs[0].zRot += .5F * step;
                thighs[1].zRot -= .5F * step;
                float low = coil * (1.0F - lunge) * hold;
                root.y += 2.5F * low;
                thighs[0].xRot = Mth.lerp(hold, thighs[0].xRot, Mth.lerp(lunge, -.9F * coil, -1.2F));
                shins[0].xRot = Mth.lerp(hold, shins[0].xRot, Mth.lerp(lunge, 1.2F * coil, .3F));
                thighs[1].xRot = Mth.lerp(hold, thighs[1].xRot, Mth.lerp(lunge, .3F * coil, .9F));
                uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, Mth.lerp(lunge, .6F * coil, -1.65F));
                uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, Mth.lerp(lunge, .2F, -.05F));
                fores[0].xRot = Mth.lerp(hold, fores[0].xRot, Mth.lerp(lunge, -1.4F * coil, 0.0F));
                curl(0, Mth.lerp(hold * lunge, .25F, 0.0F));
                uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, .7F * lunge);
                uppers[1].zRot = Mth.lerp(hold, uppers[1].zRot, .9F * lunge);
                chest.xRot += hold * (.3F * coil * (1.0F - lunge) + .45F * lunge);
                chest.yRot += hold * (-.4F * coil * (1.0F - lunge) + .3F * lunge);
            }
            case ScarletMummerOrdealMoves.WHIRL -> {
                // A crouch with its arms gathered in, then two whole turns with arms, fans and ribbons out like blades.
                float w = StiltWalkerMoves.WHIRL_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 2.0F, since);
                float spin = HemoEasing.clamp01((since - w + 1.0F) / 9.0F);
                float out = HemoEasing.smoothstep(w - 2.0F, w, since) * (1.0F - HemoEasing.smoothstep(w + 9.0F, w + 16.0F, since));
                float hold = 1.0F - HemoEasing.smoothstep(w + 9.0F, w + 16.0F, since);
                root.y += 2.0F * gather * (1.0F - out) * hold;
                root.yRot += (-.6F * gather * (1.0F - spin) + Mth.TWO_PI * 2.0F * HemoEasing.smoothstep(spin)) * hold;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, -sign * Mth.lerp(out, .2F, 1.55F));
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(out, .5F * gather, 0.0F));
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, Mth.lerp(out, -1.6F * gather, 0.0F));
                    curl(i, Mth.lerp(out, .5F, 0.0F));
                }
                for (int k = 0; k < 5; k++) {
                    ribbons[k].zRot += (k - 2) * .45F * out;
                    ribbons[k].xRot += -1.2F * out * ((k % 2) * 2 - 1);
                }
                fanned(Mth.lerp(out, 0.0F, .8F), age);
            }
            case ScarletMummerOrdealMoves.ENTRANCE -> {
                // The grand entrance: a deep crouch with the fans half open, a tucked leap, a landing spread wide.
                float w = StiltWalkerMoves.ENTRANCE_WINDUP, f = StiltWalkerMoves.ENTRANCE_FLIGHT;
                float crouch = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since));
                float air = HemoEasing.smoothstep(w - 1.0F, w + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + f - 3.0F, w + f, since));
                float land = snap(since, w + f, 2.0F) * (1.0F - HemoEasing.smoothstep(w + f + 3.0F, w + f + 14.0F, since));
                root.y += 3.0F * crouch + 2.5F * land;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    thighs[i].xRot += -1.1F * crouch - 1.4F * air - .8F * land;
                    shins[i].xRot += 1.6F * crouch + 1.8F * air + 1.0F * land;
                    uppers[i].xRot += -2.6F * air + .4F * crouch;
                    uppers[i].zRot += -sign * (1.4F * land + .3F * air);
                }
                chest.xRot += .4F * crouch - .2F * air + .45F * land;
                fanned(.5F * crouch + .3F * air + 1.2F * land, age);
            }
            default -> {
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    puppet_rig.ATLAS = 128
    return build_rig('true_scarlet_mummer', 'TrueScarletMummerModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
