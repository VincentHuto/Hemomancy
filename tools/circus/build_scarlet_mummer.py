"""Rebuild the Scarlet Mummer's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'ScarletMummerModel.bbmodel'
PALETTE = ['#140f14', '#2b1a22', '#551c2a', '#8a2333', '#c43a3f', '#f07a5a',
           '#e9c46a', '#f7f0e6', '#cbbfb3', '#8c8079', '#4a3d48', '#6e5a8a']
FACE = ['7777', '8008', '0640', '8008', '7777', '7337']


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    top = 'mesh.getRoot()'
    joint('head', top, (0, 0, 0), [
        box((-2, -6, -2), (4, 6, 4), 'face'),
        box((-1, 0, -1), (2, 1, 2), 'porcelain')])
    # Stepped scarlet cone; vanilla copies head rotation onto the hat part.
    joint('hat', top, (0, 0, 0), [
        box((-2.5, -7, -2.5), (5, 1, 5), 'cap'),
        box((-1.5, -9, -1.5), (3, 2, 3), 'cap'),
        box((-1, -11, -1), (2, 2, 2), 'cap'),
        box((-.5, -13, -.5), (1, 2, 1), 'yellow')])
    joint('body', top, (0, 0, 0), [
        box((-2, 1, -1.5), (4, 5, 3), 'doublet'),
        box((-1.5, 6, -1), (3, 4, 2), 'doublet'),
        box((-2.5, 10, -1.5), (5, 2, 3), 'skirt'),
        box((-.5, 1, -2), (1, 8, 1), 'buttons'),
        box((-3, 0, -2), (6, 1, 4), 'ruff')])
    for side, sign in [('left', 1), ('right', -1)]:
        # Eyespotted frill: folded along the back at rest, flared when performing.
        joint(side + '_gorget', 'body', (sign * 1.5, 1, 1), [
            box((-.5, -2, -.5), (1, 4, 1), 'rib')], (0, -sign * 32, -sign * 5))
        joint(side + '_upper_fan', side + '_gorget', (0, -1, 0), [
            box((0 if sign > 0 else -5, -7, 0), (5, 11, 1), 'fan'),
            box((4 if sign > 0 else -5, -6, -.5), (1, 9, 1), 'rib')], (0, 0, sign * 7))
        joint(side + '_outer_fan', side + '_upper_fan', (sign * 5, -3, 0), [
            box((0 if sign > 0 else -4, -2, 0), (4, 8, 1), 'fan'),
            box((3 if sign > 0 else -4, -1, -.5), (1, 6, 1), 'rib')], (0, 0, -sign * 10))
        joint(side + '_lower_fan', side + '_gorget', (0, 3, 0), [
            box((0 if sign > 0 else -4, 0, 0), (4, 6, 1), 'fan')], (0, 0, sign * 23))
        joint(side + '_arm', top, (sign * 3, 2, 0), [
            box((-1, -1, -1), (2, 6, 2), 'sleeve'),
            box((-1.5, 4, -1.5), (3, 1, 3), 'ruff')])
        joint(side + '_forearm', side + '_arm', (0, 5, 0), [
            box((-.5, 0, -.5), (1, 5, 1), 'sleeve'),
            box((-1, 5, -1), (2, 2, 2), 'glove'),
            box((-.5, 7, -.5), (1, 2, 1), 'glove')], (0, 0, -sign * 6))
        joint(side + '_leg', top, (sign * 1.5, 12, 0), [
            box((-1, 0, -1), (2, 6, 2), 'hose')])
        joint(side + '_shin', side + '_leg', (0, 6, 0), [
            box((-1, 0, -1), (2, 5, 2), 'hose'),
            box((-1, 5, -3), (2, 1, 4), 'shoe'),
            box((-.5, 4, -3.5), (1, 1, 1), 'shoe')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 23)
    flank = side not in ('north', 'up')
    if material == 'face':
        if side == 'north': return int(FACE[py][px])
        if side == 'down': return 9
        return 8 if py > 0 else 7
    if material == 'porcelain':
        return 8
    if material in ('cap', 'doublet'):
        if side == 'down': return 1
        if side == 'up': return 4
        if material == 'doublet' and px % 3 == 2: return 2
        return 3 if not flank else 2
    if material == 'skirt':
        if side in ('up', 'down'): return 2
        return 4 if px % 2 == 0 else 2
    if material == 'buttons':
        if side == 'north': return 6 if py % 2 == 0 else 2
        return 2
    if material == 'ruff':
        if side == 'down': return 9
        if side == 'up': return 7 if (px + py) % 2 == 0 else 8
        return 7 if px % 2 == 0 else 8
    if material == 'yellow':
        return 6
    if material == 'rib':
        if side in ('up', 'north'): return 8
        return 9 if py % 3 else 10
    if material == 'fan':
        if side in ('up', 'down') or (side in ('east', 'west')): return 2
        # Ocelli: dark pupil, yellow ring, scarlet halo, on a dark membrane with ribs.
        cx = (w - 1) // 2
        best = min(abs(px - cx) + abs(py - c) for c in range(2, h - 1, 5))
        if best == 0: return 0
        if best == 1: return 6
        if best == 2 and (px == cx or py % 5 == 2): return 4
        if px in (0, w - 1): return 1
        return 11 if r < .1 else 2
    if material == 'sleeve':
        if side == 'down': return 1
        return 3 if (py // 2) % 2 == 0 and not flank else 2
    if material == 'glove':
        if side == 'down': return 8
        return 7 if side in ('north', 'up', 'west') else 8
    if material == 'hose':
        if side in ('up', 'down'): return 1
        return 4 if py % 2 == 0 else 0
    if material == 'shoe':
        if side == 'up': return 3
        return 2 if side != 'down' else 0
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_scarlet_mummer.py; regenerate the model, atlas and BBModel together.
public class ScarletMummerModel extends HumanoidModel<ScarletMummerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "scarlet_mummer"), "main");

	private final ModelPart leftGorget;
	private final ModelPart rightGorget;

	public ScarletMummerModel(ModelPart root) {
		super(root);
		leftGorget = body.getChild("left_gorget");
		rightGorget = body.getChild("right_gorget");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flare = entity.isPerforming() ? 0.34F + Mth.sin(ageInTicks * 0.3F) * 0.05F : 0.08F;
		leftGorget.zRot = -flare;
		rightGorget.zRot = flare;
		leftGorget.yRot = entity.isPerforming() ? -0.08F : -1.15F;
		rightGorget.yRot = entity.isPerforming() ? 0.08F : 1.15F;
		if (entity.isPerforming()) {
			body.yRot = Mth.sin(ageInTicks * 0.22F) * 0.08F;
			leftArm.zRot -= 0.35F;
			rightArm.zRot += 0.35F;
		}
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
	}
}
'''


def build():
    return build_rig('scarlet_mummer', 'ScarletMummerModel', anatomy(), JAVA, shade, PALETTE)


if __name__ == '__main__':
    build()
