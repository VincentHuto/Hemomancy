"""Rebuild the Mnemonist Puppet's connected Java rig, painted atlas and portable BBModel.

Geometry, materials and animation live here; puppet_rig.py packs UVs, writes the Java model,
exports the Blockbench file and paints the atlas so all three stay in step.
"""
from puppet_rig import ROOT, BBMODELS, build as build_rig, noise, front_edge, back_edge

MODEL = BBMODELS / 'MnemonistPuppetModel.bbmodel'
PALETTE = ['#0d0b0e', '#1f1b20', '#3a3438', '#6f6a68', '#a9a39b', '#d6d0c4',
           '#ebe6dc', '#3b1418', '#5e1f22', '#7d2a2a', '#8c6a3a', '#c9a25a']
# A smooth egg: one hollow socket, one glass bead, a crack and a stitched-shut mouth.
FACE = ['55255', '52555', '505a5', '52555', '25155']
CHARS = {'0': 0, '1': 1, '2': 2, '4': 4, '5': 5, '7': 7, 'a': 10}


def anatomy():
    parts = []

    def joint(name, parent, pivot, cubes=(), rotation=(0, 0, 0)):
        parts.append(dict(name=name, parent=parent, pivot=pivot, rotation=rotation, cubes=list(cubes)))

    def box(origin, size, material):
        return dict(origin=origin, size=size, material=material)

    top = 'mesh.getRoot()'
    joint('head', top, (0, 0, 0))
    # The listening tilt lives on a child, so the humanoid head look never straightens it.
    joint('skull', 'head', (0, -1, 0), [
        box((-2.5, -6, -2), (5, 5, 4), 'face'),
        box((-1.5, -7, -1.5), (3, 1, 3), 'bisque'),
        box((-1.5, -1, -1.5), (3, 1, 3), 'bisque')], (6, 0, 12))
    joint('hat', top, (0, 0, 0))
    joint('body', top, (0, 0, 0), [
        box((-.5, -1, -.5), (1, 1, 1), 'gap'),
        box((-2, 0, -1.5), (4, 4, 3), 'bisque'),
        box((-1.5, 4, -1), (3, 2, 2), 'bisque'),
        box((-1.5, 6, -1), (3, 2, 2), 'bisque')])
    # Loose thread wraps, slightly askew: the wounds it keeps in order.
    for i, (y, tilt) in enumerate(((1, 5), (2.5, -4), (5, 6))):
        joint('binding_' + str(i), 'body', (0, y, 0), [
            box((-2.5, 0, -2), (5, .5, 4), 'thread')], (0, 0, tilt))
    # The spindle: chamfered disc flanges either side of a fat core of wound thread, axle out the back.
    joint('memory_spool', 'body', (0, 3.5, 1.5), [
        box((-2.5, -1.5, 0), (5, 3, 1), 'flange'),
        box((-1.5, -2.5, 0), (3, 5, 1), 'flange'),
        box((-1.5, -1.5, 1), (3, 3, 2), 'wound'),
        box((-2.5, -1.5, 3), (5, 3, 1), 'flange'),
        box((-1.5, -2.5, 3), (3, 5, 1), 'flange'),
        box((-.5, -.5, 4), (1, 1, 1), 'iron')])
    # The threads paying out of the spool are drawn live by MnemonistThreadLayer, not modelled here.
    for side, sign in [('left', 1), ('right', -1)]:
        # Arms long enough to hang past the knees.
        joint(side + '_arm', top, (sign * 3, 1, 0), [
            box((-1, -1, -1), (2, 2, 2), 'bisque'),
            box((-.5, 1, -.5), (1, 5, 1), 'bisque')])
        joint(side + '_forearm', side + '_arm', (0, 6.5, 0), [
            box((-.5, -.5, -.5), (1, 1, 1), 'gap'),
            box((-.5, .5, -.5), (1, 6, 1), 'bisque'),
            box((-1, 6.5, -.5), (2, 2, 1), 'bisque'),
            box((-1, 8.5, -.5), (.5, 3, .5), 'bisque'),
            box((-.25, 8.5, -.5), (.5, 4, .5), 'bisque'),
            box((.5, 8.5, -.5), (.5, 3, .5), 'bisque')], (0, 0, -sign * 3))
        # Long legs: hips at two thirds of the height.
        joint(side + '_leg', top, (sign * 1, 8, 0), [
            box((-.5, 0, -.5), (1, 7, 1), 'bisque'),
            box((-.5, 7, -.5), (1, 1, 1), 'gap')])
        joint(side + '_shin', side + '_leg', (0, 8, 0), [
            box((-.5, 0, -.5), (1, 7, 1), 'bisque'),
            box((-.5, 7, -2), (1, 1, 3), 'bisque')])
    return parts


def shade(material, side, px, py, w, h, gx, gy):
    r = noise(gx, gy, 43)
    if material in ('bisque', 'face'):
        if material == 'face' and side == 'north': return CHARS[FACE[py][px]]
        if side == 'down': return 3
        if r < .02: return 4  # faint crazing
        if r > .992: return 8  # an old stain
        if side == 'up': return 6
        if py == h - 1 and h > 2: return 4
        return 5 if side in ('north', 'west') else 4
    if material == 'gap':
        return 0 if side != 'up' else 1
    if material == 'thread':
        if side in ('up', 'down'): return 7
        return 8 if px % 3 else 7
    if material == 'flange':
        # Flat faces stay one tone so the crossed plates read as a single disc.
        return 2 if side in ('north', 'south') else 3
    if material == 'iron':
        return 3 if side == 'up' or py == 0 else 2
    if material == 'wound':
        return 8 if py % 2 == 0 else 7
    raise ValueError('Unknown material ' + material)


JAVA = r'''package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_mnemonist_puppet.py; regenerate the model, atlas and BBModel together.
public class MnemonistPuppetModel extends HumanoidModel<MnemonistPuppetEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "mnemonist_puppet"), "main");

	private final ModelPart memorySpool;

	public MnemonistPuppetModel(ModelPart root) {
		super(root);
		this.memorySpool = this.body.getChild("memory_spool");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	@Override
	public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
		// HumanoidModel resets limb heights to vanilla proportions; restore the short torso and long legs.
		float crouch = crouching ? 3.2F : 0;
		leftArm.y = leftArm.getInitialPose().y + crouch;
		rightArm.y = rightArm.getInitialPose().y + crouch;
		leftLeg.y = leftLeg.getInitialPose().y + (crouching ? .2F : 0);
		rightLeg.y = rightLeg.getInitialPose().y + (crouching ? .2F : 0);
		// The spindle turns slowly as the live threads pay out of its core.
		float pulse = Mth.sin(ageInTicks * 0.16F) * 0.04F;
		this.memorySpool.zRot = ageInTicks * 0.03F + pulse;
	}
}
'''


def build():
    return build_rig('mnemonist_puppet', 'MnemonistPuppetModel', anatomy(), JAVA, shade, PALETTE)


if __name__ == '__main__':
    build()
