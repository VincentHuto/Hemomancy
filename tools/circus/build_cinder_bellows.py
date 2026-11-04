"""Rebuild the Cinder Bellows's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'CinderBellowsModel.bbmodel'
PALETTE = ['#1a1216', '#2e1f24', '#4a3236', '#6b4440', '#8f5a4c', '#b98d6c',
           '#e2cfa4', '#6e2228', '#a8383a', '#d9532c', '#f7953a', '#ffd98a']
EMBER = {9, 10, 11}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    # The coil leaves the trunk sideways and curls behind it; pivots sit on the ground plane,
    # so every segment's underside stays at y = 0 without per-segment offsets.
    widths = [8, 7, 7, 6, 6, 5, 5, 4, 3, 3, 2]
    heights = [7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2]
    turns = [90, -27, -27, -27, -27, -27, -27, -27, -27, -24, -18]
    for i, (w, h, turn) in enumerate(zip(widths, heights, turns)):
        heat = .08 + i * .045
        cubes = [box((-w / 2, -(h - 1), -2), (w, h - 1, 6), f'scale:{heat:.3f}')]
        if w > 3:
            cubes.append(box((-(w - 2) / 2, -h, -1.5), (w - 2, h, 5), f'scale:{heat:.3f}'))
        else:
            cubes = [box((-w / 2, -h, -2), (w, h, 6), f'scale:{heat:.3f}')]
        name = 'coil' if i == 0 else 'tail_' + str(i - 1)
        parent = 'root' if i == 0 else ('coil' if i == 1 else 'tail_' + str(i - 2))
        joint(name, parent, (0, 0, -3.5) if i == 0 else (0, 0, 2.5), cubes, (0, turn, 0))
    joint('tail_tip', 'tail_9', (0, 0, 3.5), [
        box((-.5, -1.5, -.5), (1, 1, 3), 'scale:0.600'),
        box((-.5, -2, 2), (1, 1, 1), 'ember')], (35, 0, 0))

    # Rising trunk: belly scutes run unbroken from the coil to the sternum.
    joint('pelvis', 'root', (0, -6, -3), [
        box((-3.5, -7, -3), (7, 8, 6), 'scale:0.080'),
        box((-2.5, -6, -3.5), (5, 7, 1), 'belly')], (-8, 0, 0))
    joint('abdomen', 'pelvis', (0, -6, 0), [
        box((-3, -7, -2.5), (6, 7, 5), 'scale:0.050'),
        box((-2, -6, -3), (4, 6, 1), 'belly'),
        box((-.5, -6, 2), (1, 5, 1), 'spine')], (12, 0, 0))
    joint('chest', 'abdomen', (0, -6, 0), [
        box((-3, -8, .5), (6, 7, 2), 'hide'),
        box((-4, -10, -1.5), (8, 2, 3), 'hide'),
        box((-.5, -9, 2.5), (1, 9, 1), 'spine'),
        box((-.5, -8, -3.5), (1, 6, 1), 'bone')], (5, 0, 0))
    joint('ruff', 'chest', (0, -10, 0), [box((-3, -1, -3), (6, 1, 6), 'ruff')])
    joint('ruff_fold', 'ruff', (0, -.5, 0), [box((-3, -1, -3), (6, 1, 6), 'ruff')], (0, 45, 0))
    for side, sign in [('left', -1), ('right', 1)]:
        # Pleated bellows lobes: leather plates over recessed ember folds.
        pleats = []
        for k in range(7):
            y = -3.5 + k
            if k % 2 == 0:
                pleats.append(box((-1.5, y, -2), (3, 1, 4), 'lung'))
            else:
                pleats.append(box((-1, y, -1.5), (2, 1, 3), 'seam'))
        joint(side + '_lung', 'chest', (sign * 1.75, -4, -.5), pleats)
        joint(side + '_clavicle', 'chest', (sign * .5, -9.5, -2), [
            box((-4 if sign < 0 else 0, -.5, -.5), (4, 1, 1), 'bone')], (0, 0, sign * 10))
        for i in range(4):
            front = 1 if i == 3 else 2
            joint(side + '_rib_' + str(i), 'chest', (sign * 3.5, -7.25 + i * 1.75, 1), [
                box((-.5, -.5, -4), (1, 1, 4), 'bone'),
                box((.5 if sign < 0 else -.5 - front, -.5, -4), (front, 1, 1), 'bone')], (20, 0, 0))
        joint(side + '_arm', 'chest', (sign * 4.5, -8.5, .5), [
            box((-1, 0, -1), (2, 6, 2), 'hide'),
            box((-1.5, -1, -1.5), (3, 2, 3), 'bone')], (15, 0, -sign * 12))
        joint(side + '_forearm', side + '_arm', (0, 6, 0), [
            box((-1, 0, -1), (2, 5, 2), 'hide'),
            box((-1.5, 4, -1.5), (3, 1, 3), 'bone')], (-75, 0, sign * 6))
        joint(side + '_hand', side + '_forearm', (0, 5, 0), [
            box((-1, 0, -.5), (2, 2, 1), 'hide')])
        joint(side + '_fingers', side + '_hand', (0, 2, 0), [
            box((-1, 0, -.5), (.5, 2, .5), 'bone'),
            box((-.25, 0, -.5), (.5, 2.5, .5), 'bone'),
            box((.5, 0, -.5), (.5, 2, .5), 'bone')], (25, 0, 0))
        joint(side + '_thumb', side + '_hand', (-sign * 1, .5, -.25), [
            box((-.25, 0, -.25), (.5, 1.5, .5), 'bone')], (0, 0, -sign * 25))
    # The Fire Breather's wick torch, counter-rotated upright in the curled hand.
    joint('torch', 'left_hand', (0, 1.5, -1), [
        box((-.5, -5, -.5), (1, 7, 1), 'wood'),
        box((-1, -7, -1), (2, 2, 2), 'ember')], (36, 0, -20))
    joint('neck', 'chest', (0, -10, -.5), [
        box((-1, -5, -1), (2, 5, 2), 'hide'),
        box((-.5, -5, -1.75), (1, 5, 1), 'throat'),
        box((-1.5, -2, -1.5), (3, 1, 3), 'bone'),
        box((-1.5, -4, -1.5), (3, 1, 3), 'bone')], (18, 0, 0))
    joint('head', 'neck', (0, -5, 0), [
        box((-2.5, -5, -2.5), (5, 5, 5), 'cranium'),
        box((-1.5, -2, -5.5), (3, 2, 3), 'muzzle'),
        box((-2.5, -5, -3.5), (5, 1, 1), 'brow'),
        box((-3, -2.5, -2.5), (1, 1, 2), 'bone'),
        box((2, -2.5, -2.5), (1, 1, 2), 'bone'),
        box((-.5, -6, -1.5), (1, 1, 4), 'spine')], (-25, 0, 0))
    joint('jaw', 'head', (0, 0, 1.5), [
        box((-1.5, 0, -7), (3, 1, 6), 'jaw'),
        box((-1, -1, -6.75), (2, 1, 1), 'teeth')], (6, 0, 0))
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    """Pick a palette index. Light falls from above; no blanket per-face outlines."""
    kind, _, arg = material.partition(':')
    r = noise(gx, gy)
    if kind == 'scale':
        heat = float(arg)
        if side == 'down':
            return 1
        offset = (py // 2 % 2) * 2
        cx, cy = (px + offset) % 4, py % 2
        if cy == 1 and cx == 3:
            if r < heat / 3: return 10
            if r < heat: return 9
            return 1
        if side != 'up' and py == h - 1:
            return 1 if r < .5 else 2
        if cy == 1: return 2
        if cx == 0: return 4
        return 3 if side != 'up' or r > .25 else 4
    if kind == 'belly':
        if side == 'north':
            if py % 2 == 1: return 4
            return 5 if 0 < px < w - 1 else 4
        return 3
    if kind == 'hide':
        if side == 'down': return 1
        if side == 'up' or py == 0: return 4
        if py == h - 1: return 2
        if r < .14: return 2
        if w >= 3 and side in ('north', 'south') and px == w // 2 and py % 3 == 1: return 4
        return 3
    if kind == 'bone':
        if side == 'down' or py == h - 1 and h > 1: return 5
        if side in ('east', 'west', 'south') and h > 1 and py > 0: return 5
        if r < .07: return 5
        return 6
    if kind == 'spine':
        band = py % 3
        if side in ('up', 'down'): return 6
        return 2 if band == 2 else (6 if band == 0 else 5)
    if kind == 'lung':
        if side == 'down': return 1
        if side == 'up': return 8
        if side == 'north' and px == w // 2: return 9
        return 7
    if kind == 'seam':
        if side == 'north': return 11 if px == w // 2 else 10
        return 9
    if kind == 'throat':
        if side == 'north': return 11 if r < .25 else 10
        return 9
    if kind == 'ruff':
        if side == 'down': return 7
        if side == 'up': return 8 if (px + py) // 2 % 2 == 0 else 7
        return 8 if px // 2 % 2 == 0 else 7
    if kind == 'wood':
        if side in ('up', 'down'): return 2
        return 3 if (px + py // 3) % 2 == 0 else 2
    if kind == 'ember':
        if side == 'down': return 9
        if side == 'up': return 11
        return 10 if py < h - 1 else 9
    if kind == 'teeth':
        if side == 'north': return 6 if px % 2 == 0 else 0
        return 5
    if kind == 'jaw':
        if side == 'up': return 9 if px == w // 2 else 0
        if side == 'north': return 6
        if side == 'down': return 5 if py >= h // 2 else 2
        if side in ('east', 'west'):
            return 2 if back_edge(side, px, w) else (6 if r > .2 else 5)
        return 5
    if kind == 'brow':
        if side == 'up': return 2 if r < .6 else 1
        if side == 'north': return 6
        return 5
    if kind == 'muzzle':
        if side == 'north': return (0 if px != w // 2 else 6) if py == 0 else (5 if px != w // 2 else 6)
        if side == 'up': return 5 if py == 0 else 6
        if side == 'down': return 5
        return 6 if py == 0 else 5
    if kind == 'cranium':
        if side == 'north':
            face = ['55655', '00600', '0b5b0', '66066', '56665']
            return {'0': 0, '5': 5, '6': 6, 'b': 10}[face[py][px]]
        if side == 'up':
            # Soot pools at the crown and back; the front rim stays bone.
            if py <= 1: return 1 if r < .55 else 2
            if py == 2: return 2 if r < .6 else 5
            return 5 if py == 3 else 6
        if side == 'south':
            if py == 0: return 1
            if py == 1: return 2 if r < .7 else 1
            return 2 if r < .25 else 5
        if side == 'down': return 5
        if py == 0: return 2 if r < .7 else 1
        if py == 1: return 2 if back_edge(side, px, w) or r < .35 else 5
        if back_edge(side, px, w): return 5
        if front_edge(side, px, w) and py in (2, 3): return 5
        return 6 if py < h - 1 else 5
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_cinder_bellows.py; regenerate the model, atlas and BBModel together.
public class CinderBellowsModel extends EntityModel<CinderBellowsEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "cinder_bellows"), "main");
    private final ModelPart root, pelvis, abdomen, chest, neck, head, jaw, tailTip;
    private final ModelPart[] tail = new ModelPart[10];
    private final ModelPart[] lungs, arms, forearms, hands;

    public CinderBellowsModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        abdomen = pelvis.getChild("abdomen");
        chest = abdomen.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        ModelPart parent = root.getChild("coil");
        for (int i = 0; i < tail.length; i++) {
            tail[i] = parent.getChild("tail_" + i);
            parent = tail[i];
        }
        tailTip = tail[9].getChild("tail_tip");
        lungs = new ModelPart[]{chest.getChild("left_lung"), chest.getChild("right_lung")};
        arms = new ModelPart[]{chest.getChild("left_arm"), chest.getChild("right_arm")};
        forearms = new ModelPart[]{arms[0].getChild("left_forearm"), arms[1].getChild("right_forearm")};
        hands = new ModelPart[]{forearms[0].getChild("left_hand"), forearms[1].getChild("right_hand")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(CinderBellowsEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        int cycle = entity.getBreathCycle();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
    }

    /** Ember heat for the glow layer: banked at rest, rising through the inhale, full during the breath. */
    public static float emberHeat(float cycle) {
        return Math.max(inhale(cycle) * .55F, exhale(cycle));
    }

    private static float inhale(float cycle) {
        return smooth(cycle <= 20 ? cycle / 20 : cycle <= 60 ? 1 : (100 - cycle) / 40);
    }

    private static float exhale(float cycle) {
        return smooth((cycle - 20) / 5) * (1 - smooth((cycle - 60) / 12));
    }

    /** Shared posing entry point for runtime rendering and authoring previews. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch, float cycle) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float inhale = inhale(cycle);
        float exhale = exhale(cycle);
        // Rear back while drawing breath, then lunge the neck into the cone.
        float draw = inhale * (1 - exhale);
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .075F + limbSwing * .6F;
        for (int i = 0; i < tail.length; i++) {
            // Local curvature propagates through the parents, as on Scarlet Serpent.
            tail[i].yRot += Mth.sin(wave - i * .65F) * (.035F + travel * .12F)
                    * (1 - exhale * .65F);
        }
        tailTip.xRot += Mth.sin(age * .11F) * .12F + inhale * .3F;
        pelvis.zRot += Mth.sin(wave) * travel * .035F;
        abdomen.xRot -= draw * .12F;
        chest.xRot += exhale * .12F - draw * .16F;
        chest.y -= inhale * .25F;
        for (ModelPart lung : lungs) {
            lung.xScale = 1 + inhale * .12F;
            lung.yScale = 1 + inhale * .06F;
            lung.zScale = 1 + inhale * .22F;
        }
        neck.xRot += exhale * .32F - draw * .18F;
        head.yRot += Mth.clamp(yaw, -35, 35) * Mth.DEG_TO_RAD;
        // The head counters the neck lunge so the open jaw stays aimed down the cone.
        head.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD - exhale * .3F + draw * .1F;
        jaw.xRot += exhale * .9F + inhale * .03F;
        for (int i = 0; i < arms.length; i++) {
            float sign = i == 0 ? -1 : 1;
            // Elbows haul back like bellows handles on the draw, then pump forward on the breath.
            arms[i].zRot -= sign * draw * .25F;
            arms[i].xRot += Mth.sin(age * .045F + i * Mth.PI) * .025F + draw * .35F - exhale * .45F;
            forearms[i].xRot -= exhale * .3F;
            hands[i].xRot += Mth.sin(age * .05F + i * Mth.PI) * .035F;
        }
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    return build_rig('cinder_bellows', 'CinderBellowsModel', anatomy(), JAVA, shade, PALETTE, glow=EMBER)


if __name__ == '__main__':
    build()
