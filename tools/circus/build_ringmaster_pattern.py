"""Rebuild the Ringmaster Pattern's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'RingmasterPatternModel.bbmodel'
PALETTE = ['#0c0a10', '#241a22', '#3d2a33', '#5c1622', '#8f1f2e', '#c43a3c',
           '#a07a2c', '#e0b84a', '#fff0a0', '#ece4d6', '#b9ae9e', '#4f4a55']
GLOWING = {8}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    top = 'mesh.getRoot()'
    # Nobody is inside the office: a void under the hat with two gold points of attention.
    joint('head', top, (0, 0, 0), [box((-2.5, -5, -2), (5, 5, 4), 'void')])
    joint('top_hat', 'head', (0, -5, 0), [
        box((-4, -1, -4), (8, 1, 8), 'brim'),
        box((-3, -9, -3), (6, 8, 6), 'hat'),
        box((-3.5, -3, -3.5), (7, 2, 7), 'band'),
        box((-1, -3, -4.5), (2, 2, 1), 'gold')], (0, 0, -4))
    joint('hat', top, (0, 0, 0))
    joint('body', top, (0, 0, 0), [
        box((-3, 0, -2), (6, 9, 4), 'coat'),
        box((-2, 1, -2.5), (4, 7, 1), 'vest'),
        box((-3.5, 9, -2), (7, 3, 4), 'coat'),
        box((-3, -2, -2.5), (6, 2, 5), 'collar')])
    for side, sign in [('left', 1), ('right', -1)]:
        joint(side + '_lapel', 'body', (sign * 1, 0, -2.5), [
            box((0 if sign > 0 else -2, 0, -.5), (2, 7, 1), 'lapel')], (0, 0, -sign * 11))
        joint(side + '_coattail', 'body', (sign * .5, 10, 1), [
            box((0 if sign > 0 else -3, 0, 0), (3, 10, 1), 'tail')], (9, 0, -sign * 6))
        joint(side + '_command_thread', 'body', (sign * 3.5, -1, 2.5), [
            box((-.5, 0, -.5), (1, 12, 1), 'thread')])
        joint(side + '_arm', top, (sign * 4, 2, 0), [
            box((-1, -2, -1), (2, 6, 2), 'coat'),
            box((-2, -3, -1.5), (4, 1, 3), 'gold'),
            box((-2, -2, -1.5), (4, 1, 3), 'fringe'),
            box((-1.5, 3, -1.5), (3, 1, 3), 'gold')])
        joint(side + '_forearm', side + '_arm', (0, 4, 0), [
            box((-1, 0, -1), (2, 4, 2), 'coat'),
            box((-1.5, 3, -1.5), (3, 1, 3), 'cuff'),
            box((-1, 4, -1), (2, 3, 2), 'glove')], (-26, 0, 0))
        joint(side + '_leg', top, (sign * 1.5, 12, 0), [box((-1, 0, -1), (2, 6, 2), 'trouser')])
        joint(side + '_shin', side + '_leg', (0, 6, 0), [
            box((-1, 0, -1), (2, 5, 2), 'boot'),
            box((-1, 5, -2), (2, 1, 3), 'boot')])
    # The conductor's baton, held up from the right glove.
    joint('baton', 'right_forearm', (0, 6, -.5), [
        box((-.5, 0, -.5), (1, 6, 1), 'baton'),
        box((-.5, 6, -.5), (1, 1, 1), 'tip')], (-120, 0, 0))
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 61)
    if material == 'void':
        if side == 'north' and py == 1 and px in (1, 3): return 8
        return 0
    if material in ('coat', 'tail'):
        if side == 'down': return 3
        if side == 'up' or py == 0: return 5
        if material == 'tail' and side == 'north' and px in (0, w - 1): return 7
        if py == h - 1: return 3
        if side not in ('north', 'up'): return 3 if px % 3 == 0 else 4
        return 4 if r > .1 else 3
    if material == 'collar':
        if side == 'up' or py == 0: return 7
        return 4 if side == 'north' else 3
    if material == 'vest':
        if side != 'north': return 1
        if px == w // 2 - 1 and py % 2 == 0: return 7
        return 6 if py % 2 == 1 else 1
    if material == 'lapel':
        if side == 'north': return 7 if px == 0 else 3
        return 3
    if material == 'gold':
        if side == 'up' or py == 0: return 7
        return 6
    if material == 'fringe':
        if side == 'up': return 6
        return 7 if px % 2 == 0 else 6
    if material in ('cuff', 'glove'):
        if side in ('north', 'up', 'west'): return 9
        return 10
    if material == 'hat':
        if side == 'up': return 2
        return 2 if px == 1 and side in ('north', 'east', 'west') else 1
    if material == 'brim':
        if side == 'up': return 1
        return 2 if side != 'down' else 0
    if material == 'band':
        return 3 if py == h - 1 or side in ('up', 'down') else 4
    if material == 'thread':
        if side in ('up', 'down'): return 5
        return 8 if py % 3 == 0 else 5
    if material == 'baton':
        return 2 if side in ('north', 'up') else 1
    if material == 'tip':
        return 8
    if material == 'trouser':
        if side in ('east', 'west') and px == 0: return 4
        return 1 if r > .15 else 2
    if material == 'boot':
        if side == 'down': return 0
        if py == 0 or (side == 'north' and px == 0): return 11
        return 1
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.RingmasterPatternEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_ringmaster_pattern.py; regenerate the model, atlas and BBModel together.
public class RingmasterPatternModel extends HumanoidModel<RingmasterPatternEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "ringmaster_pattern"), "main");
	private final ModelPart leftCoattail;
	private final ModelPart rightCoattail;

	public RingmasterPatternModel(ModelPart root) {
		super(root);
		leftCoattail = body.getChild("left_coattail");
		rightCoattail = body.getChild("right_coattail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(RingmasterPatternEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
		float step = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.18F;
		leftCoattail.xRot = 0.15F + step;
		rightCoattail.xRot = 0.15F - step;
		leftArm.zRot = -0.24F;
		rightArm.zRot = 0.24F;
		leftArm.getChild("left_forearm").xRot = -0.45F;
		rightArm.getChild("right_forearm").xRot = -0.45F;
	}
}
'''


def build():
    return build_rig('ringmaster_pattern', 'RingmasterPatternModel', anatomy(), JAVA, shade, PALETTE, glow=GLOWING)


if __name__ == '__main__':
    build()
