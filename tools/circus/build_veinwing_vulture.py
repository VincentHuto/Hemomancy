"""Rebuild the Veinwing Vulture's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'VeinwingVultureModel.bbmodel'
PALETTE = ['#130f14', '#2b2026', '#463440', '#6e2433', '#a3303c', '#d65a4a',
           '#e6d3b0', '#b19e82', '#c98f86', '#8e5a58', '#3c2026', '#643a40']  # 10-11: hematic iron, never brass


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 15, 0))
    # A static torso joint carries the hunched forward lean; animated parts stay on root.
    joint('torso', 'root', (0, 1, 0), [
        box((-2, -6, -2), (4, 7, 4), 'chest'),
        box((-3, -7, 0), (6, 3, 3), 'feather'),
        box((-.5, -5, -3), (1, 5, 1), 'bone')], (25, 0, 0))
    joint('ruff', 'root', (0, -6, -.5), [
        box((-2.5, -1, -2.5), (5, 2, 5), 'ruff'),
        box((-2, -2, -2), (4, 1, 4), 'ruff')])
    joint('head', 'root', (0, -7, -2.5), [
        box((-1, -2, -1), (2, 3, 2), 'skin'),
        box((-1.5, -5, -3), (3, 3, 3), 'skull'),
        box((-1, -4, -5), (2, 2, 2), 'beak')])
    joint('beak_hook', 'head', (0, -2.5, -5), [box((-.5, 0, -1), (1, 2, 1), 'hook')], (16, 0, 0))
    for side, sign in [('left', 1), ('right', -1)]:
        lo = lambda length: 0 if sign > 0 else -length
        joint(side + '_wing', 'root', (sign * 2.5, -4, 0), [
            box((-1, -1, -1), (2, 2, 2), 'hematic_iron'),
            box((lo(6), -.5, -.5), (6, 1, 1), 'spar'),
            box((lo(6) + (1 if sign > 0 else -1), .5, 0), (6, 5, 1), 'sail')])
        joint(side + '_forewing', side + '_wing', (sign * 6, 0, 0), [
            box((-1, -1, -1), (2, 2, 2), 'hematic_iron'),
            box((lo(9), -.5, -.5), (9, 1, 1), 'spar')], (0, -sign * 6, -sign * 17))
        for i, length in enumerate((6, 8, 10)):
            joint(f'{side}_membrane_{i}', side + '_forewing', (sign * (.5 + i * 3), .5, 0), [
                box((lo(3), 0, 0), (3, length, 1), 'blade'),
                box((sign * 2 if sign > 0 else -3, -.5, -.5), (1, length + 1, 1), 'vein')],
                (0, 0, -sign * (5 + i * 6)))
        joint(side + '_talon', 'root', (sign * 1.25, 2, 0), [
            box((-.5, 0, -.5), (1, 3, 1), 'skin'),
            box((-1.5, 3, -2.5), (1, 1, 3), 'talon'),
            box((.5, 3, -2.5), (1, 1, 3), 'talon'),
            box((-.5, 3, .5), (1, 1, 2), 'talon')])
    joint('tail', 'root', (0, 2, 2), [box((-2, 0, -.5), (4, 4, 1), 'blade')])
    joint('tail_cord', 'tail', (0, 3.5, 0), [box((-.5, 0, -.5), (1, 4, 1), 'vein')], (11, 0, 0))
    joint('tail_needle', 'tail_cord', (0, 3.5, 0), [
        box((-.5, 0, -.5), (1, 3, 1), 'needle')], (17, 0, 0))
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 53)
    if material == 'chest':
        # Bare breast with painted ribs; feathered flanks and back.
        if side == 'north':
            if px in (0, w - 1): return 1
            return 6 if py % 2 == 1 and 0 < py < h - 1 else (8 if r > .25 else 9)
        if side == 'down': return 9
        return 1 if (px + (py // 2) * 2) % 4 == 3 or py % 2 == 1 else 2
    if material in ('feather', 'ruff'):
        if side == 'down': return 1
        if material == 'ruff' and side != 'up': return 3 if py == h - 1 else 2
        return 1 if (px + (py // 2) * 2) % 4 == 3 or py % 2 == 1 else 2
    if material == 'blade':
        if side in ('up', 'down', 'east', 'west'): return 1
        if py >= h - 2: return 5 if py == h - 1 else 3
        return 1 if px == 0 or r < .12 else 2
    if material == 'sail':
        if side != 'north' and side != 'south': return 3
        if py == h - 1: return 1
        return 4 if px % 3 == 0 or (px + py) % 4 == 0 else 3
    if material == 'vein':
        if side in ('up', 'down'): return 3
        return 5 if py % 3 == 0 else 4
    if material == 'spar':
        if px % 4 == 3: return 10
        return 6 if side in ('up', 'north') else 7
    if material == 'hematic_iron':
        return 11 if side == 'up' or py == 0 else 10
    if material == 'needle':
        return 11 if py < h - 1 else 6
    if material == 'skull':
        if side in ('east', 'west'):
            if py == 1 and front_edge(side, px, w): return 5
            return 6 if py < h - 1 else 7
        if side == 'up': return 6 if r > .2 else 7
        if side == 'north': return 0 if py == 1 and px != w // 2 else 6
        return 7
    if material == 'beak':
        if side == 'up' or py == 0: return 6
        return 7
    if material == 'hook':
        return 11 if side == 'up' else 10
    if material == 'skin':
        if side in ('up', 'down'): return 9
        return 8 if py % 2 == 0 else 9
    if material == 'bone':
        return 6 if side in ('north', 'up') else 7
    if material == 'talon':
        if side == 'north': return 0
        return 7 if side != 'up' else 6
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_veinwing_vulture.py; regenerate the model, atlas and BBModel together.
public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftForewing;
	private final ModelPart rightForewing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.leftForewing = this.leftWing.getChild("left_forewing");
		this.rightForewing = this.rightWing.getChild("right_forewing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float flap = Mth.sin(ageInTicks * 0.55F) * 0.45F;
		this.root.y = 15.0F + Mth.sin(ageInTicks * 0.16F) * 0.6F;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.7F;
		this.leftWing.zRot = 0.25F + flap;
		this.rightWing.zRot = -0.25F - flap;
		this.leftWing.yRot = -0.18F;
		this.rightWing.yRot = 0.18F;
		this.leftForewing.zRot = -0.3F - flap * 0.35F;
		this.rightForewing.zRot = 0.3F + flap * 0.35F;
		this.tail.xRot = 0.18F + Mth.sin(ageInTicks * 0.22F) * 0.08F;
		this.leftTalon.xRot = Mth.cos(ageInTicks * 0.18F) * 0.12F;
		this.rightTalon.xRot = -this.leftTalon.xRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
'''


def build():
    return build_rig('veinwing_vulture', 'VeinwingVultureModel', anatomy(), JAVA, shade, PALETTE)


if __name__ == '__main__':
    build()
