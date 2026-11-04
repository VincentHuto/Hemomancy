"""Rebuild the Sanguine Hound's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'SanguineHoundModel.bbmodel'
PALETTE = ['#140e12', '#2a1b20', '#523039', '#79333e', '#a44549', '#d06a5c',
           '#8a7e74', '#d8c9a8', '#4c4a52', '#807a84', '#2f2a33', '#6b4a2e']
CUR_FLESH = {2: '#6e2030', 3: '#a3283a', 4: '#d4404a'}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    joint('root', 'mesh.getRoot()', (0, 0, 0))
    # Set-animal build: deep narrow keel, wasp waist, long back; stitched rupture seams mark the Curs.
    joint('body', 'root', (0, 13, 0), [
        box((-2, -3, -5), (4, 5, 5), 'chest'),
        box((-1.5, -2, 0), (3, 3, 4), 'muscle'),
        box((-2, -2.5, 3.5), (4, 4, 4), 'muscle'),
        box((-.5, -4, -5), (1, 1, 12), 'spine'),
        box((-2.5, -3.5, -3.5), (5, 6, 1), 'harness')])
    joint('neck', 'body', (0, -2, -4.5), [box((-1, -4, -1), (2, 4, 2), 'muscle')], (50, 0, 0))
    joint('head', 'root', (0, 9, -7.5), [
        box((-1.5, -1.5, -2), (3, 3, 3), 'skull'),
        box((-1, -.5, -5), (2, 2, 3), 'snout'),
        box((-1.5, -1, -4), (3, 3, 1), 'iron'),
        box((-.5, -2, -3), (1, 1, 2), 'iron')])
    joint('snout_tip', 'head', (0, .5, -5), [box((-.5, -1, -3), (1, 2, 3), 'snout')], (28, 0, 0))
    for side, sign in [('right', -1), ('left', 1)]:
        # Tall, upright, flat-topped ears.
        joint(side + '_ear', 'head', (sign * 1, -1.5, 0), [
            box((-.5, -5, -.5), (1, 5, 1), 'ear'),
            box((-.5, -6, -1), (1, 1, 2), 'ear')], (-10, 0, sign * 8))
        for end, z in [('front', -4), ('hind', 5)]:
            leg = end + '_' + side + '_leg'
            joint(leg, 'root', (sign * 1.5, 14, z), [box((-1, -1, -1), (2, 5, 2), 'muscle')])
            joint(end + '_' + side + '_shin', leg, (0, 4, 0), [
                box((-1, 0, -1), (2, 1, 2), 'iron'),
                box((-.5, 1, -.5), (1, 4, 1), 'bone'),
                box((-.5, 5, -1.5), (1, 1, 2), 'paw')])
    # Stiff tail ending in the Set-animal fork.
    joint('tail', 'root', (0, 11, 7.5), [box((-.5, -.5, 0), (1, 1, 6), 'spine')], (25, 0, 0))
    joint('tail_tip', 'tail', (0, 0, 6), [
        box((-1.5, -.5, 0), (3, 1, 1), 'bone'),
        box((-1.5, -.5, 1), (1, 1, 2), 'bone'),
        box((.5, -.5, 1), (1, 1, 2), 'bone')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 31)
    if material in ('muscle', 'chest'):
        if side == 'down': return 2
        if side == 'up': return 5 if r < .2 else 4
        if side in ('east', 'west'):
            # Exposed ribs on the chest; stitched rupture seams on the rest.
            if material == 'chest' and 1 <= py <= h - 3 and px % 2 == 0: return 7 if py < h - 3 else 6
            if material == 'muscle' and w >= 4 and px == w // 2: return 7 if py % 2 == 0 else 1
            if py == h - 1: return 2
            return 4 if py % 2 == 0 else 3
        if py == h - 1: return 2
        return 3 if r > .25 else 2
    if material == 'spine':
        if side == 'down': return 1
        return 7 if (px + py) % 2 == 0 else 1
    if material == 'harness':
        if side in ('up', 'down'): return 11
        if py == 0 or py == h - 1: return 1
        return 9 if px % 3 == 1 and py % 3 == 1 else 11
    if material == 'iron':
        if side == 'up' or py == 0: return 9
        return 8
    if material == 'skull':
        if side == 'up': return 7 if r > .15 else 6
        if side in ('east', 'west'):
            if py == 1 and front_edge(side, px, w): return 5
            if py == 1 and px == (w - 2 if side == 'east' else 1): return 0
            return 7 if py < h - 1 else 6
        if side == 'north': return 5 if py == 1 and px in (0, w - 1) else (0 if py == 1 else 7)
        return 6
    if material == 'snout':
        if side == 'north': return 0 if w == 1 else (7 if py == 0 else 3)
        if side == 'up': return 7 if r > .3 else 6
        if side == 'down': return 2
        if py == h - 1: return 2
        return 3
    if material == 'ear':
        if side == 'north': return 5
        return 3 if side != 'south' else 2
    if material == 'bone':
        return 7 if side in ('north', 'up', 'west') else 6
    if material == 'paw':
        if side == 'north': return 7 if px % 2 == 0 else 1
        return 1
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_sanguine_hound.py; regenerate the model, atlas and BBModel together.
public class SanguineHoundModel extends EntityModel<SanguineHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "sanguine_hound"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart hindLeftLeg;
	private final ModelPart hindRightLeg;
	private final ModelPart tail;
	private int color = -1;

	public SanguineHoundModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.frontLeftLeg = this.root.getChild("front_left_leg");
		this.frontRightLeg = this.root.getChild("front_right_leg");
		this.hindLeftLeg = this.root.getChild("hind_left_leg");
		this.hindRightLeg = this.root.getChild("hind_right_leg");
		this.tail = this.root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(SanguineHoundEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD;
		this.frontLeftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
		this.frontRightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.15F * limbSwingAmount;
		this.hindLeftLeg.xRot = this.frontRightLeg.xRot;
		this.hindRightLeg.xRot = this.frontLeftLeg.xRot;
		this.tail.yRot = Mth.sin(ageInTicks * 0.12F) * 0.24F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, color == -1 ? packedColor : color);
	}

	public void setColor(int color) {
		this.color = color;
	}
}
'''


def build():
    return build_rig('sanguine_hound', 'SanguineHoundModel', anatomy(), JAVA, shade, PALETTE, variants={'_cur': CUR_FLESH})


if __name__ == '__main__':
    build()
