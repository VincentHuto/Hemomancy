"""Rebuild the Marrow Juggler's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'MarrowSpitterModel.bbmodel'
PALETTE = ['#17121a', '#2d1f2a', '#4b2232', '#74283a', '#a3384a', '#d0625e',
           '#8a7a6c', '#b8a48a', '#e4d4b4', '#f6ead2', '#6b6470', '#c9a24a']
MASKS = {
    'mourn': ['99999', '90909', '95959', '90009', '90909'],
    'laugh': ['99999', '90909', '99999', '05550', '90009'],
    'blank': ['99999', '90909', '99999', '99999', '98889'],
}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 24, 0))
    joint('body', 'root', (0, -13, 0), [
        box((-4, -9, -2.5), (8, 6, 5), 'doublet'),
        box((-3, -3, -2), (6, 3, 4), 'doublet')], (8, 0, 0))
    # Two pleated ruff layers, the second turned to scallop the silhouette.
    joint('collar', 'body', (0, -9, 0), [
        box((-4.5, -1, -3), (9, 2, 6), 'ruff'),
        box((-1, .5, -3.5), (2, 2, 1), 'bone')])
    joint('collar_fold', 'collar', (0, 0, 0), [box((-3.5, -1.5, -3.5), (7, 1, 7), 'ruff')], (0, 45, 0))
    joint('hood', 'body', (0, -10, -.5), [box((-3, -6, -2.5), (6, 6, 5), 'hood')], (-10, 0, 0))
    joint('mask_mourn', 'hood', (0, -3, -2.5), [box((-2.5, -2.5, -1), (5, 5, 1), 'mourn')])
    joint('mask_laugh', 'hood', (-3, -3, 0), [box((-2.5, -2.5, -1), (5, 5, 1), 'laugh')], (0, 70, -8))
    joint('mask_blank', 'hood', (3, -3, 0), [box((-2.5, -2.5, -1), (5, 5, 1), 'blank')], (0, -70, 8))
    for side, sign in [('left', -1), ('right', 1)]:
        joint('hood_point_' + side, 'hood', (sign * 2, -5.5, 0), [
            box((-1, -4, -1), (2, 4, 2), 'hood')], (0, 0, sign * 35))
        joint('hood_tip_' + side, 'hood_point_' + side, (0, -4, 0), [
            box((-.5, -3, -.5), (1, 3, 1), 'hood'),
            box((-1, -5, -1), (2, 2, 2), 'brass')], (0, 0, sign * 45))
    joint('skirt', 'body', (0, 0, 0), [box((-3.5, 0, -2.5), (7, 3, 5), 'robe')])
    for side, sign in [('left', -1), ('right', 1)]:
        for facing, z, tilt, length in [('front', -2.5, -8, 9), ('rear', 2.5, 8, 10)]:
            name = facing + '_' + side
            joint(name, 'skirt', (sign * 1.75, 2, z), [
                box((-1.5, 0, -.5), (3, length, 1), 'panel')], (tilt, 0, -sign * 8))
            joint(name + '_rag', name, (0, length, 0), [
                box((-1.5, 0, -.5), (3, 2, 1), 'rag')], (0, 0, -sign * 8))
        joint('side_' + side, 'skirt', (sign * 3.5, 2, 0), [
            box((-.5, 0, -2), (1, 9, 4), 'panel')], (0, 0, -sign * 14))
    joint('sash', 'skirt', (0, 1, -3), [box((-1, 0, -.5), (2, 9, 1), 'sash')], (-6, 0, 0))
    shoulders = [(4, -8, 0), (4, -5.5, .5), (3, -2.5, .5)]
    for i in range(6):
        side = -1 if i % 2 == 0 else 1
        tier = i // 2
        sx, sy, sz = shoulders[tier]
        joint('arm_' + str(i), 'body', (side * sx, sy, sz), [
            box((-.5, 0, -.5), (1, 5, 1), 'bone'),
            box((-1, -.5, -1), (2, 3, 2), 'sleeve')], (-18, 0, -side * [115, 75, 35][tier]))
        joint('forearm_' + str(i), 'arm_' + str(i), (0, 4.5, 0), [
            box((-.5, 0, -.5), (1, 4, 1), 'bone'),
            box((-1, 3, -1), (2, 1, 2), 'brass')], (-55, 0, 0))
        joint('hand_' + str(i), 'forearm_' + str(i), (0, 4, 0), [
            box((-1, 0, -.5), (2, 2, 1), 'bone')], (15, 0, 0))
        joint('fingers_' + str(i), 'hand_' + str(i), (0, 1.5, 0), [
            box((-1, 0, -.5), (.5, 1.5, .5), 'bone'),
            box((.5, 0, -.5), (.5, 1.5, .5), 'bone')], (-35, 0, 0))
        joint('thumb_' + str(i), 'hand_' + str(i), (side * -1, .5, -.25), [
            box((-.25, 0, -.25), (.5, 1, .5), 'bone')], (-25, 0, side * 30))
    for i in range(3):
        joint('knife_' + str(i), 'body', (0, -8, -6), [
            box((-.5, -5, -.25), (1, 4, .5), 'blade'),
            box((-1, -1, -.5), (2, 1, 1), 'brass'),
            box((-.5, 0, -.5), (1, 2, 1), 'grip'),
            box((-.5, 2, -.5), (1, 1, 1), 'brass')])
    return parts


def harlequin(px, py, dark=False):
    # Diamonds touching at their points; the corners between them form the second color.
    cx, cy = px % 6, py % 6
    if abs(2 * cx - 5) + abs(2 * cy - 5) > 5:
        return 0 if dark else 1
    return 2 if dark else 3


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 11)
    flank = side not in ('north', 'up')
    if material in ('doublet', 'sleeve'):
        if side == 'down': return 0
        if side == 'up': return 3 if (px + py) % 2 else 2
        if py == 0 and material == 'doublet': return 4
        return harlequin(px, py, flank)
    if material == 'robe':
        if side in ('up', 'down'): return 2
        return 3 if px % 3 != 2 else 2
    if material == 'panel':
        if side in ('up', 'down'): return 2
        if py >= h - 2: return harlequin(px, py, flank)
        fold = px % 3
        if fold == 0 and not flank: return 4
        return (3 if fold != 2 else 2) if not flank else 2
    if material == 'rag':
        if side in ('up', 'down'): return 1
        return 2 if (px + py) % 2 == 0 else 0
    if material == 'sash':
        if side in ('up', 'down'): return 3
        return 8 if py % 3 == 0 else 4
    if material == 'hood':
        if side == 'down': return 0
        if side == 'up': return 3 if r > .3 else 2
        if py == 0: return 3
        return 2 if r > .15 else 1
    if material == 'ruff':
        if side == 'down': return 6
        if side == 'up': return 9 if px % 2 == 0 else 8
        return 8 if px % 2 == 0 else 7
    if material in MASKS:
        if side == 'north':
            return {'0': 0, '5': 5, '8': 8, '9': 9}[MASKS[material][py][px]]
        return 7
    if material == 'bone':
        if side == 'down' or (py == h - 1 and h > 2): return 6
        return 8 if side in ('north', 'up') else 7
    if material == 'brass':
        if side == 'up' or py == 0: return 11
        return 7 if side != 'north' else 11
    if material == 'blade':
        if side == 'north': return 9 if px == 0 else 8
        return 10
    if material == 'grip':
        return 1 if py % 2 else 2
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Authored by tools/circus/build_marrow_juggler.py; regenerate the model, atlas and BBModel together.
public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "marrow_spitter"), "main");
    private final ModelPart root, body, hood;
    private final ModelPart[] arms = new ModelPart[6], forearms = new ModelPart[6], hands = new ModelPart[6];
    private final ModelPart[] knives = new ModelPart[3];
    private final ModelPart[] hems;

    public MarrowSpitterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        hood = body.getChild("hood");
        for (int i = 0; i < arms.length; i++) {
            arms[i] = body.getChild("arm_" + i);
            forearms[i] = arms[i].getChild("forearm_" + i);
            hands[i] = forearms[i].getChild("hand_" + i);
        }
        for (int i = 0; i < knives.length; i++) knives[i] = body.getChild("knife_" + i);
        ModelPart skirt = body.getChild("skirt");
        hems = new ModelPart[]{skirt.getChild("front_left"), skirt.getChild("front_right"),
                skirt.getChild("rear_left"), skirt.getChild("rear_right"),
                skirt.getChild("side_left"), skirt.getChild("side_right"), skirt.getChild("sash")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
    }

    @Override
    public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        setupPose(limbSwingAmount, age, yaw, pitch);
    }

    /** Poses the connected rig for both runtime rendering and authoring previews. */
    public void setupPose(float movement, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        root.y += -.75F + Mth.sin(age * .07F) * .35F;
        body.zRot += Mth.sin(age * .045F) * .025F;
        body.xRot += Mth.clamp(movement, 0, 1) * .06F;
        hood.yRot += Mth.clamp(yaw, -40, 40) * Mth.DEG_TO_RAD * .6F;
        hood.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD * .4F;
        for (int i = 0; i < hems.length; i++) {
            hems[i].xRot += Mth.sin(age * .065F + i * 1.1F) * .045F;
        }
        for (int i = 0; i < arms.length; i++) {
            int pair = i / 2;
            float side = i % 2 == 0 ? -1 : 1;
            float phase = age * .13F + pair * Mth.TWO_PI / 3;
            arms[i].zRot -= side * Mth.sin(phase) * .1F;
            arms[i].xRot += Mth.cos(phase + i % 2 * Mth.PI) * .08F;
            forearms[i].xRot -= Mth.cos(phase + i % 2 * Mth.PI) * .12F;
            hands[i].xRot += Mth.sin(phase) * .1F;
        }
        for (int i = 0; i < knives.length; i++) {
            float phase = age * .13F + i * Mth.TWO_PI / 3;
            float t = (1 - Mth.cos(phase)) * .5F;
            float arc = Mth.sin(phase);
            Vector3f left = palm(i * 2);
            Vector3f right = palm(i * 2 + 1);
            ModelPart knife = knives[i];
            knife.x = Mth.lerp(t, left.x, right.x);
            knife.y = Mth.lerp(t, left.y, right.y) - arc * arc * (7 + i * 1.5F);
            knife.z = Mth.lerp(t, left.z, right.z);
            knife.zRot = phase * 2;
        }
    }

    private Vector3f palm(int index) {
        PoseStack pose = new PoseStack();
        arms[index].translateAndRotate(pose);
        forearms[index].translateAndRotate(pose);
        hands[index].translateAndRotate(pose);
        // The knife pivot is its grip, so catching follows the bent hand exactly.
        return pose.last().pose().transformPosition(new Vector3f(0, .75F / 16, 0)).mul(16);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
'''


def build():
    return build_rig('marrow_spitter', 'MarrowSpitterModel', anatomy(), JAVA, shade, PALETTE)


if __name__ == '__main__':
    build()
