"""Rebuild the Gorebound Hulk's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'GoreboundHulkModel.bbmodel'
PALETTE = ['#17121a', '#2b2229', '#4a4048', '#7d7377', '#4a1d26', '#6f2a33',
           '#9a3b40', '#c45a52', '#e08a6e', '#b9a184', '#e6d5b0', '#3e2b25']


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('pelvis', 'root', (0, -12, 0), [
        box((-5, -2, -3), (10, 4, 6), 'trunks'),
        box((-4.5, -9, -3), (9, 7, 6), 'abdomen')])
    joint('belt', 'pelvis', (0, -2.5, 0), [box((-5.5, -1.5, -3.5), (11, 3, 7), 'belt')])
    joint('buckle', 'belt', (0, 0, -3.5), [
        box((-2, -1.5, -.75), (4, 3, 1), 'bone'),
        box((-1, -1, -1.25), (2, 2, 1), 'iron')])
    # Hunched barrel chest; the hump behind the small head carries the trapezius mass.
    joint('chest', 'pelvis', (0, -8, 0), [
        box((-7, -11, -4), (14, 11, 8), 'flesh'),
        box((-6, -13, -1), (12, 3, 6), 'muscle')], (16, 0, 0))
    for side, sign in [('left', -1), ('right', 1)]:
        joint(side + '_pectoral', 'chest', (sign * 3.5, -8, -4), [
            box((-3, -2.5, -1), (6, 5, 2), 'muscle')], (0, 0, -sign * 6))
        joint(side + '_lat', 'chest', (sign * 6.5, -5, 1), [
            box((-1.5, -4, -2), (3, 8, 4), 'muscle')], (0, 0, sign * 12))
        joint(side + '_strap', 'chest', (sign * 4, -6, -5), [
            box((-1, -5, -.5), (2, 11, 1), 'strap')], (0, 0, -sign * 6))
        joint(side + '_trap', 'chest', (sign * 3.5, -11, 0), [
            box((-3, -2, -3), (6, 3, 6), 'muscle')], (0, 0, sign * 20))
    joint('neck', 'chest', (0, -12, -2.5), [box((-2, -2, -2), (4, 3, 4), 'tendon')])
    # A small carved skull: iron temple bolts and a circus moustache plate.
    joint('head', 'neck', (0, -2, -.5), [
        box((-2.5, -5, -2.5), (5, 5, 5), 'skull'),
        box((-3, -4, -3.5), (6, 1, 1), 'bone'),
        box((-3.5, -3, -.5), (1, 1, 1), 'iron'),
        box((2.5, -3, -.5), (1, 1, 1), 'iron'),
        box((-2.5, -1.5, -3.25), (5, 1, 1), 'moustache')], (-10, 0, 0))
    joint('jaw', 'head', (0, 0, -.5), [
        box((-2, 0, -3), (4, 2, 4), 'jaw'),
        box((-2, -1, -3.5), (1, 1, 1), 'bone'),
        box((1, -1, -3.5), (1, 1, 1), 'bone')], (4, 0, 0))
    for side, sign in [('left', -1), ('right', 1)]:
        joint(side + '_arm', 'chest', (sign * 8, -9, 0), [
            box((-2.5, -1, -2.5), (5, 7, 5), 'flesh'),
            box((-3, -2, -3), (6, 3, 6), 'muscle'),
            box((-2, 1, -3.5), (4, 4, 1), 'muscle')], (-12, 0, -sign * 10))
        # Oversized forearms and fists: the Strongman's promise.
        joint(side + '_forearm', side + '_arm', (0, 6, 0), [
            box((-3.5, 0, -3.5), (7, 7, 7), 'flesh'),
            box((-3, -1, -3), (6, 1, 6), 'tendon')], (-22, 0, sign * 4))
        joint(side + '_binding', side + '_forearm', (0, 5, 0), [
            box((-4, 0, -4), (8, 2, 8), 'iron')])
        joint(side + '_hand', side + '_forearm', (0, 7, 0), [
            box((-4, 0, -3.5), (8, 4, 7), 'fist')], (10, 0, 0))
        for digit in range(4):
            joint(side + '_finger_' + str(digit), side + '_hand', ((-3, -1, 1, 3)[digit], 3, -3.5), [
                box((-1, 0, -1.5), (2, 3, 2), 'knuckle')], (30, 0, 0))
        joint(side + '_thumb', side + '_hand', (-sign * 4, 1, -2), [
            box((-1, 0, -1), (2, 3, 2), 'fist')], (20, 0, sign * 25))
        # Short, bowed legs under the striped trunks.
        # The ankle pivot sits 2px above the sole so the planted boot test reads y = 24.
        joint(side + '_thigh', 'pelvis', (sign * 3, 0, 0), [
            box((-2.5, 0, -2.5), (5, 5, 5), 'flesh'),
            box((-3, 0, -3), (6, 2, 6), 'trunks')])
        joint(side + '_shin', side + '_thigh', (0, 5, 0), [
            box((-2, 0, -2), (4, 5, 4), 'flesh'),
            box((-1.5, -.5, -2.5), (3, 2, 1), 'bone')])
        joint(side + '_foot', side + '_shin', (0, 5, 0), [
            box((-2.5, -1, -4), (5, 3, 6), 'boot'),
            box((-2, 0, -4.5), (4, 2, 1), 'iron')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 7)
    if material in ('flesh', 'fist'):
        if side == 'down': return 4
        if side == 'up': return 7 if r > .6 else 6
        if py == h - 1: return 4
        # Stitched seams down the front and the spine of large flesh pieces.
        if (side == 'north' and w >= 7 and px == w // 3) or (side == 'south' and w >= 12 and px == w // 2):
            return 10 if py % 2 == 0 else 4
        if material == 'fist' and side == 'north' and py == 0: return 9
        if py == 0: return 7 if side in ('north', 'up') else 6
        streak = (px + py // 3) % 4 == 0
        if r < .1: return 4
        if side in ('north', 'up'): return 7 if streak else 6
        return 6 if streak else 5
    if material == 'muscle':
        if side == 'down': return 4
        if py == 0 or side == 'up': return 8 if r > .75 else 7
        if py == h - 1: return 5
        if side not in ('north', 'up'): return 6 if py % 3 == 1 else 5
        return 7 if py % 3 == 1 else 6
    if material == 'abdomen':
        if side == 'north':
            if px == w // 2: return 4
            if py % 3 == 2: return 5
            return 8 if py % 3 == 0 and 0 < px < w - 1 else 7
        if py == h - 1: return 4
        return 6 if r > .15 else 5
    if material == 'tendon':
        if side in ('up', 'down'): return 7
        return 8 if (px + py) % 2 == 0 else 7
    if material == 'knuckle':
        if side == 'up' or py == 0: return 9
        return 7 if side == 'north' else 6
    if material == 'trunks':
        if side == 'down': return 1
        if py == h - 1: return 0
        return 1 if px // 3 % 2 == 0 else 9
    if material in ('belt', 'strap'):
        if side in ('up', 'down'): return 11
        if side == 'north' and (py == 0 or py == h - 1): return 9 if px % 3 == 0 else 11
        if material == 'belt' and side == 'north' and px % 4 == 1: return 3
        return 11
    if material == 'boot':
        if side == 'down' or py == h - 1: return 0
        if side == 'north' and py == 0 and px % 2 == 1: return 9
        return 11 if r > .2 else 1
    if material == 'iron':
        if side == 'up' or py == 0: return 3
        if side == 'down' or py == h - 1: return 1
        if side != 'up' and h >= 2 and px % 3 == 1: return 3
        return 2
    if material == 'bone':
        if side == 'down' or (py == h - 1 and h > 1): return 9
        if side in ('east', 'west', 'south') and py > 0: return 9
        return 10
    if material == 'moustache':
        if side == 'north': return 1 if px in (0, w - 1) else 2
        return 1
    if material == 'jaw':
        if side == 'up': return 4
        if side == 'north': return 10
        if side == 'down': return 9
        return 2 if back_edge(side, px, w) else 9
    if material == 'skull':
        if side == 'north':
            face = ['99a99', '00a00', '07a70', 'aa0aa', '9aaa9']
            return {'0': 0, '7': 7, '9': 9, 'a': 10}[face[py][px]]
        if side == 'up':
            return 9 if (px + py) % 3 == 0 else 10
        if side == 'south':
            return 9 if py < 2 or r < .3 else 10
        if side == 'down': return 9
        if py == 2 and px == w // 2: return 0
        return 9 if back_edge(side, px, w) or py == h - 1 else 10
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_gorebound_hulk.py; regenerate the model, atlas and BBModel together.
public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "gorebound_hulk"), "main");
    private final ModelPart root, chest, head, jaw;
    private final ModelPart[] arms = new ModelPart[2], forearms = new ModelPart[2], hands = new ModelPart[2];
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2];

    public GoreboundHulkModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        ModelPart pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        head = chest.getChild("neck").getChild("head");
        jaw = head.getChild("jaw");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "left" : "right";
            arms[i] = chest.getChild(side + "_arm");
            forearms[i] = arms[i].getChild(side + "_forearm");
            hands[i] = forearms[i].getChild(side + "_hand");
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
    public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        float windup = entity.getStrikeWindup();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                windup > 0 ? windup + partialTick : 0, attackTime);
    }

    /** Shared runtime and authoring pose; windup is the existing ten-tick strike counter. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch,
            float windup, float strike) {
        root.getAllParts().forEach(ModelPart::resetPose);
        movement = Mth.clamp(movement, 0, 1);
        float lift = Mth.clamp(windup / 8, 0, 1);
        lift = lift * lift * (3 - 2 * lift);
        float recovery = Mth.clamp(strike * 2.5F, 0, 1);
        float impact = Mth.sin(Mth.clamp(strike, 0, 1) * Mth.PI);
        chest.xRot += -lift * .18F + impact * .14F;
        head.yRot += Mth.clamp(yaw, -45, 45) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD * .5F;
        jaw.xRot += Mth.sin(age * .09F) * .025F + lift * .05F;
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? -1 : 1;
            float gait = Mth.cos(limbSwing * .6662F + i * Mth.PI) * movement;
            thighs[i].xRot = gait * .45F;
            shins[i].xRot = Math.max(0, -gait) * .55F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            arms[i].xRot -= lift * 1.1F;
            forearms[i].xRot -= lift * .65F;
            if (windup <= 0 && strike > 0) {
                arms[i].xRot = Mth.lerp(recovery, -1.25F, arms[i].xRot);
                forearms[i].xRot = Mth.lerp(recovery, -.85F, forearms[i].xRot);
            }
            arms[i].xRot -= gait * .12F * (1 - lift);
            arms[i].zRot -= side * lift * .12F;
            hands[i].xRot -= lift * .12F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    return build_rig('gorebound_hulk', 'GoreboundHulkModel', anatomy(), JAVA, shade, PALETTE)


if __name__ == '__main__':
    build()
