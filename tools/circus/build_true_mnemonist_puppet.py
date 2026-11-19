"""The Mnemonist Puppet's true self (2026-11-17): what the puppet is inside the Retired Understudy's Chamber of Will.
Memory made flesh. The physical puppet is a slim bisque doll with an egg face, long arms, a memory spool and loose
thread bindings; its true self is the same doll with the bisque burst. The shell has cracked open over the chest and
at every joint, and raw crimson memory-flesh shows through and binds the pieces together; the egg face is split, its
jaw hanging open on a dark hollow, the glass bead of its one eye lit red; three small pale faces, the faces it
remembers, are pinned to its chest; the spool on its back is twice the size, its wound core glowing, with four stiff
threads standing out of it. Taller, longer in the arm, three long fingers a hand. Shaded as round forms lit from
above, never in columns.

Its own jointed rig (pelvis, spine, neck, head and jaw; shoulder, upper arm, forearm, hand and three two-segment
fingers a side; thigh, shin and foot), so the model owns its walk and its ordeal moves (`OrdealMnemonistMoves`).
The feet are the root at model y 24. Drawn 1.3 times over.
"""
import puppet_rig
from puppet_rig import build as build_rig, noise

NAMES = ['ink', 'gap', 'iron', 'flange', 'bq0', 'bq1', 'bq2', 'bq3', 'f0', 'f1', 'f2', 'f3', 'face0', 'face1',
         'g0', 'g1', 'th']
PALETTE = ['#0d0b0e', '#1f1b20', '#3a3438', '#5a5052',
           '#8a847c', '#b2aca2', '#d6d0c4', '#ebe6dc',  # bisque
           '#3b0c14', '#5e1420', '#8e1f30', '#b83242',  # memory-flesh
           '#c9c0ae', '#9e9484',  # the remembered faces
           '#ff4a3a', '#ffd2b8', '#7d1a24']  # glow: the eye, the spool's core; thread
I = {name: i for i, name in enumerate(NAMES)}
GLOW = {I['g0'], I['g1']}

# The split egg face, 5 by 5: the hollow socket, the lit glass bead, a crack running down into the open jaw.
FACE = ['77677', '7k767', 'kk7g7', '76f77', '6f777']
FACE_KEYS = {'7': 'bq3', '6': 'bq2', 'k': 'ink', 'g': 'g0', 'f': 'f2'}
# A remembered face, 2 by 3 (two eyes over a mouth), pale and small.
SMALL_FACE = ['kk', 'aa', 'ka']


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -16, 0), [box((-1.5, -1, -1), (3, 2, 2), 'bisque')])
    joint('spine', 'pelvis', (0, -1, 0), [
        box((-.75, -3, -.75), (1.5, 3, 1.5), 'flesh'),
        box((-1.5, -4.5, -1), (3, 2, 2), 'bisque')])
    # The burst chest: shell plates sprung apart over a knot of crimson memory-flesh.
    joint('chest', 'spine', (0, -4, 0), [
        box((-2, -5, -1.5), (4, 5, 3), 'flesh'),
        box((-2.5, -5.5, -1.75), (2, 3, 3.5), 'bisque'),
        box((.5, -5, -1.75), (2, 2.5, 3.5), 'bisque'),
        box((-1, -1.5, -1.75), (2.5, 1.5, 3.5), 'bisque')])
    # The faces it remembers, pinned to its chest.
    for i, (x, y, tilt) in enumerate(((-1.25, -3.5, -12), (1, -2, 10), (-.25, -1, 4))):
        joint('face_' + str(i), 'chest', (x, y, -1.75), [box((-.75, -1, -.5), (1.5, 2, .5), 'small_face')], (0, 0, tilt))
    # The spool on its back, twice the size, its wound core lit; four stiff threads stand out of it.
    joint('spool', 'chest', (0, -3, 1.5), [
        box((-3, -2, 0), (6, 4, 1), 'flange'),
        box((-2, -3, 0), (4, 6, 1), 'flange'),
        box((-2, -2, 1), (4, 4, 2.5), 'wound'),
        box((-3, -2, 3.5), (6, 4, 1), 'flange'),
        box((-2, -3, 3.5), (4, 6, 1), 'flange'),
        box((-.5, -.5, 4.5), (1, 1, 1.5), 'iron')])
    for i, (x, y, rx, rz) in enumerate(((-1.5, -1.5, 40, -35), (1.5, -1.5, 40, 35), (-1, 1, 70, -20), (1, 1, 70, 20))):
        joint('thread_' + str(i), 'spool', (x, y, 2), [box((-.25, -6, -.25), (.5, 6, .5), 'thread')], (rx, 0, rz))
        joint('thread_' + str(i) + '_tip', 'thread_' + str(i), (0, -6, 0), [box((-.25, -5, -.25), (.5, 5, .5), 'thread')],
              (-20, 0, 0))
    joint('neck', 'chest', (0, -5.5, 0), [box((-.5, -2, -.5), (1, 2, 1), 'flesh')])
    # The egg split open: the skull, and its jaw hanging on a dark hollow.
    joint('head', 'neck', (0, -2, 0), [box((-2.5, -6, -2), (5, 5, 4), 'face')], (6, 0, 12))
    joint('jaw', 'head', (0, -1, 1), [box((-2, 0, -3), (4, 1.5, 3.5), 'bisque')], (18, 0, 0))
    for side, sign in (('right', -1), ('left', 1)):
        joint(side + '_shoulder', 'chest', (sign * 3, -4.5, 0), [box((-1, -1, -1), (2, 2, 2), 'bisque')])
        joint(side + '_upper', side + '_shoulder', (0, .5, 0), [
            box((-.5, 0, -.5), (1, 6, 1), 'bisque'),
            box((-.75, 5.5, -.75), (1.5, 1, 1.5), 'flesh')])
        joint(side + '_fore', side + '_upper', (0, 6.5, 0), [
            box((-.5, 0, -.5), (1, 6.5, 1), 'bisque'),
            box((-.75, 2.5, -.75), (1.5, 1, 1.5), 'flesh')])
        joint(side + '_hand', side + '_fore', (0, 6.5, 0), [box((-1, 0, -.5), (2, 1.5, 1), 'bisque')])
        for digit, x in enumerate((-.75, 0, .75)):
            name = side + '_finger_' + str(digit)
            joint(name, side + '_hand', (x, 1.5, 0), [box((-.25, 0, -.25), (.5, 2.5, .5), 'bisque')])
            joint(name + '_tip', name, (0, 2.5, 0), [box((-.25, 0, -.25), (.5, 2, .5), 'flesh')])
        joint(side + '_thigh', 'pelvis', (sign * 1, 0, 0), [
            box((-.5, 0, -.5), (1, 8, 1), 'bisque'),
            box((-.75, 7, -.75), (1.5, 1.5, 1.5), 'flesh')])
        joint(side + '_shin', side + '_thigh', (0, 8, 0), [box((-.5, 0, -.5), (1, 7, 1), 'bisque')])
        joint(side + '_foot', side + '_shin', (0, 7, 0), [box((-.5, 0, -2), (1, 1, 3), 'bisque')])
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
    return v + (noise(gx, gy, 23 + salt) - .5) * .12


def ramp(v, names, limits):
    for limit, name in zip(limits, names):
        if v < limit:
            return I[name]
    return I[names[-1]]


def bisque(v):
    return ramp(v, ('bq0', 'bq1', 'bq2', 'bq3'), (.3, .5, .7))


def flesh(v):
    return ramp(v, ('f0', 'f1', 'f2', 'f3'), (.3, .5, .72))


def shade(material, side, px, py, w, h, gx, gy):
    v = lit(side, px, py, w, h, gx, gy)
    r = noise(gx, gy, 41)
    if material == 'bisque':
        # Crazed bisque, the cracks opening on red flesh.
        if r < .06:
            return I['f2']
        if r > .985:
            return I['f1']
        return bisque(v)
    if material == 'flesh':
        if r < .08:
            return I['g0'] if side not in ('up', 'down') else I['f3']
        return flesh(v)
    if material == 'face':
        if side == 'north':
            return I[FACE_KEYS[FACE[py][px]]]
        if r < .05:
            return I['f2']
        return bisque(v)
    if material == 'small_face':
        if side == 'north':
            key = SMALL_FACE[min(py, 2)][min(px, 1)]
            return I['ink'] if key == 'k' else I['face0']
        return I['face1']
    if material == 'flange':
        return I['flange'] if side in ('north', 'south') else I['iron']
    if material == 'wound':
        return I['g1'] if (py + px) % 3 == 0 and side not in ('up', 'down') else I['g0'] if py % 2 == 0 else I['th']
    if material == 'thread':
        return I['g0'] if py % 3 == 0 else I['th']
    if material == 'iron':
        return I['flange'] if side == 'up' or py == 0 else I['iron']
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMnemonistMoves;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_mnemonist_puppet.py (the Mnemonist Puppet's true self, 2026-11-17); regenerate
// the model, atlas and BBModel together.
/**
 * The Mnemonist Puppet's true self: memory made flesh. A marionette's walk (long strides, the body hung from strings
 * and lifting on each step, the arms dangling loose and swinging late), the head cocked to listen, the spool's threads
 * standing out and stirring, and its ordeal moves ({@link OrdealMnemonistMoves}), each a gather, a fast blow and a
 * recovery: the recital's three blows walked forward, the threads cast from the spool, the echo let out of its open
 * jaw, and the lunge.
 */
public final class TrueMnemonistPuppetModel extends EntityModel<MnemonistPuppetEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_mnemonist_puppet"), "main");
    private final ModelPart root, pelvis, spine, chest, neck, head, jaw, spool;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2],
            threads = new ModelPart[4], threadTips = new ModelPart[4];
    private final ModelPart[][] fingers = new ModelPart[2][3], tips = new ModelPart[2][3];

    public TrueMnemonistPuppetModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        spine = pelvis.getChild("spine");
        chest = spine.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        spool = chest.getChild("spool");
        for (int t = 0; t < 4; t++) {
            threads[t] = spool.getChild("thread_" + t);
            threadTips[t] = threads[t].getChild("thread_" + t + "_tip");
        }
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            shoulders[i] = chest.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            for (int f = 0; f < 3; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.yScale = 1.0F;
        });
        float movement = Mth.clamp(limbSwingAmount * 1.4F, 0.0F, 1.0F);
        float stride = limbSwing * .55F;
        // Hung from strings: the body lifts on each long step and sways over the planted leg.
        pelvis.y -= Math.abs(Mth.sin(stride)) * 1.4F * movement + Mth.sin(age * .09F) * .25F;
        chest.zRot += Mth.cos(stride) * .12F * movement + Mth.sin(age * .05F) * .03F;
        chest.xRot += .12F + .1F * movement;
        head.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        // Listening: the cocked head twitches now and then.
        head.zRot += Mth.sin(age * .7F) * .08F * HemoEasing.smoothstep(.85F, 1.0F, Mth.sin(age * .043F));
        jaw.xRot += .1F * Mth.sin(age * .08F);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * movement;
            thighs[i].xRot = swing * .85F;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.3F + .1F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            // Dangling arms that swing late, as if only the strings move them.
            float late = Mth.cos(stride - .9F + i * Mth.PI + Mth.PI) * movement;
            uppers[i].xRot = .1F + late * .55F + Mth.sin(age * .06F + i) * .04F;
            uppers[i].zRot = -sign * (.12F + .05F * Mth.sin(age * .07F + i * 2));
            fores[i].xRot = -.15F - Math.max(0.0F, late) * .4F;
            curl(i, .25F + .1F * Mth.sin(age * .11F + i));
        }
        for (int t = 0; t < 4; t++) {
            threads[t].xRot += Mth.sin(age * .13F + t * 1.7F) * .1F;
            threadTips[t].xRot += Mth.sin(age * .19F + t) * .25F;
            threads[t].zRot += Mth.sin(age * .11F + t * 2.3F) * .08F;
        }
        spool.zRot = age * .03F;
        int move = entity.ordealMove();
        if (move >= 0) move(move, entity.ordealMoveAge(partialTick));
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 3; f++) {
            fingers[i][f].xRot = -amount * (1.0F + f * .1F);
            tips[i][f].xRot = -amount * 1.3F;
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    private static float strike(float since, float at, float back) {
        return HemoEasing.smoothstep(at - back, at - 2.0F, since) * (1.0F - fall(since, at));
    }

    private void move(int move, float since) {
        switch (move) {
            case OrdealMnemonistMoves.RECITAL -> {
                float w = OrdealMnemonistMoves.RECITAL_WINDUP, beat = OrdealMnemonistMoves.RECITAL_BEAT;
                float end = 1.0F - HemoEasing.smoothstep(w + 2 * beat + 4, w + 2 * beat + 16, since);
                float crouch = HemoEasing.smoothstep(0.0F, w - 2, since) * end;
                // Right, left, then both overhead: each arm cocked back through its gather and driven down on its beat.
                float r1 = strike(since, w, w), l1 = strike(since, w + beat, beat + 1), both = strike(since, w + 2 * beat, beat + 1);
                float d1 = fall(since, w) * (1 - fall(since, w + beat)), d2 = fall(since, w + beat) * (1 - fall(since, w + 2 * beat));
                float d3 = fall(since, w + 2 * beat) * end;
                uppers[0].xRot = Mth.lerp(crouch, uppers[0].xRot, -2.8F * Math.max(r1, both) - .6F * (d1 + d3) - .2F);
                uppers[1].xRot = Mth.lerp(crouch, uppers[1].xRot, -2.8F * Math.max(l1, both) - .6F * (d2 + d3) - .2F);
                for (int i = 0; i < 2; i++) {
                    fores[i].xRot = Mth.lerp(crouch, fores[i].xRot, -.5F);
                    curl(i, Mth.lerp(crouch, .25F, -.3F));
                }
                chest.xRot += crouch * .2F + .35F * (d1 + d2) + .5F * d3;
                chest.yRot += .35F * (d1 - d2);
                head.xRot -= .3F * Math.max(r1, both);
                jaw.xRot += .5F * d3;
                thighs[0].xRot -= .6F * crouch;
                shins[0].xRot += .5F * crouch;
                thighs[1].xRot += .35F * crouch;
                for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            }
            case OrdealMnemonistMoves.THREAD_CAST -> {
                float w = OrdealMnemonistMoves.THREAD_WINDUP;
                float end = 1.0F - HemoEasing.smoothstep(w + 6, w + 18, since);
                float wind = HemoEasing.smoothstep(0.0F, w - 2, since) * (1.0F - fall(since, w));
                float cast = fall(since, w) * end;
                // Arms flung wide and back, the spool thrust out; then everything thrown forward and the threads lash out.
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].xRot = Mth.lerp(wind + cast, uppers[i].xRot, Mth.lerp(cast, .9F, -1.7F));
                    uppers[i].zRot = Mth.lerp(wind + cast, uppers[i].zRot, -sign * Mth.lerp(cast, 1.2F, .3F));
                    curl(i, Mth.lerp(cast, .2F, -.4F));
                }
                chest.xRot += -.4F * wind + .45F * cast;
                head.xRot += -.3F * wind + .2F * cast;
                for (int t = 0; t < 4; t++) {
                    threads[t].xRot += -.5F * wind + 1.1F * cast;
                    threadTips[t].xRot += .6F * cast;
                    threads[t].yScale = 1.0F + 1.2F * cast;
                    threadTips[t].yScale = 1.0F + 1.4F * cast;
                }
            }
            case OrdealMnemonistMoves.ECHO -> {
                float w = OrdealMnemonistMoves.ECHO_WINDUP, gap = OrdealMnemonistMoves.ECHO_GAP;
                float end = 1.0F - HemoEasing.smoothstep(w + gap + 4, w + gap + 18, since);
                float draw = HemoEasing.smoothstep(0.0F, w - 2, since) * end;
                // The head thrown back, the jaw wide on its hollow; the cry and its echo jolt it twice.
                float cry = fall(since, w) * (1.0F - HemoEasing.smoothstep(w + 2, w + 6, since))
                        + fall(since, w + gap) * (1.0F - HemoEasing.smoothstep(w + gap + 2, w + gap + 6, since));
                head.xRot += -.7F * draw + .5F * cry;
                jaw.xRot += .9F * draw;
                chest.xRot += -.3F * draw + .3F * cry;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(draw, uppers[i].zRot, -sign * 1.0F);
                    uppers[i].xRot = Mth.lerp(draw, uppers[i].xRot, .3F);
                    curl(i, Mth.lerp(draw, .25F, -.5F));
                    thighs[i].xRot -= .3F * cry;
                    shins[i].xRot += .5F * cry;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                pelvis.y += 1.0F * cry;
            }
            case OrdealMnemonistMoves.LUNGE -> {
                float w = OrdealMnemonistMoves.LUNGE_WINDUP, run = 8.0F;
                float crouch = HemoEasing.smoothstep(0.0F, w - 2, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 14, since));
                float running = HemoEasing.smoothstep(w - 1, w + 1, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 4, since));
                chest.xRot += .55F * crouch;
                head.xRot -= .4F * crouch;
                pelvis.y += 2.0F * crouch * (1.0F - running);
                uppers[0].xRot = Mth.lerp(crouch, uppers[0].xRot, Mth.lerp(running, .9F, -1.9F));
                uppers[1].xRot = Mth.lerp(crouch, uppers[1].xRot, Mth.lerp(running, -.6F, -1.6F));
                curl(0, Mth.lerp(running, .3F, -.4F));
                curl(1, Mth.lerp(running, .3F, -.4F));
                for (int i = 0; i < 2; i++) {
                    float sprint = Mth.cos(since * 1.6F + i * Mth.PI) * running;
                    thighs[i].xRot += -.5F * crouch * (1.0F - running) + sprint * 1.0F;
                    shins[i].xRot += .8F * crouch * (1.0F - running) + Math.max(0.0F, -sprint) * 1.0F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                for (int t = 0; t < 4; t++) threads[t].xRot -= .6F * running;
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
    return build_rig('true_mnemonist_puppet', 'TrueMnemonistPuppetModel', anatomy(), JAVA, shade, PALETTE, glow=GLOW)


if __name__ == '__main__':
    build()
