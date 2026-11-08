"""Rebuild the Marionette Crossbar's held model: Java model, painted atlas and portable BBModel.

A bone-and-hematic-iron marionette control: a grip for the fist, a main bar pointing forward, a front (hand) bar and
a rear (foot) bar that rock independently, and five living-vein strings hanging from the tips. The vein strings
are rendered separately so they can pulse while a puppet is out. Model space is the usual y-down entity space
with the grip at the origin; MarionetteCrossbarItemRenderer orients it for each display context.
"""
import json
import puppet_rig
from puppet_rig import geometry, noise
from build_faculty import (SHARED, ASSETS, joint, box, tone, metal, blockbench, paint, texture_entry)

ATLAS = 64
# Blood-side fittings are hematic iron (dark red-black), never brass, bronze or copper.
PALETTE = dict(SHARED, he0='#1e1014', he1='#3c2026', he2='#643a40')
MODELS = puppet_rig.ROOT / 'src/main/java/com/vincenthuto/hemomancy/client/model/item'
TEXTURE = ASSETS / 'textures/item/marionette_crossbar_model.png'
BBMODEL = ASSETS / 'models/item/bbmodel/MarionetteCrossbarModel.bbmodel'
TOP = 'mesh.getRoot()'
SEGMENTS = 3
SEGMENT_LENGTH = 3
# Tip order is the puppet assignment order: the first puppet takes the head string at the front of the bar.
STRINGS = [('head_string', 'control', (0, 1, -7.5)),
           ('left_hand_string', 'front_bar', (6.5, 1, 0)),
           ('right_hand_string', 'front_bar', (-6.5, 1, 0)),
           ('left_foot_string', 'rear_bar', (5, 1, 0)),
           ('right_foot_string', 'rear_bar', (-5, 1, 0))]


def crossbar():
    parts = []
    j = lambda *a, **k: joint(parts, *a, **k)
    j('grip', TOP, (0, 0, 0), [
        box((-1, -2, -1), (2, 6, 2), 'bone'),
        box((-1.5, 3.5, -1.5), (3, 1, 3), 'hematic_iron')])
    j('control', 'grip', (0, -3, 0), [
        box((-1, -1, -8), (2, 2, 14), 'bone'),
        box((-1.5, -1.5, -1.5), (3, 3, 3), 'vein_wrap'),
        box((-.5, -2.5, -.5), (1, 1, 1), 'hematic_iron')])
    # The cross bars sit half a pixel proud of the main bar, lashed on top of it: equal heights would leave
    # coplanar faces fighting where the bars cross.
    j('front_bar', 'control', (0, 0, -5.5), [
        box((-6, -1.5, -1), (12, 2, 2), 'bone'),
        box((-7, -2, -1.5), (1, 3, 3), 'hematic_iron'),
        box((6, -2, -1.5), (1, 3, 3), 'hematic_iron')])
    j('rear_bar', 'control', (0, 0, 4.5), [
        box((-4.5, -1.5, -1), (9, 2, 2), 'bone'),
        box((-5.5, -2, -1.5), (1, 3, 3), 'hematic_iron'),
        box((4.5, -2, -1.5), (1, 3, 3), 'hematic_iron')])
    for name, parent, pivot in STRINGS:
        previous = parent
        for i in range(SEGMENTS):
            cubes = [box((-.25, 0, -.25), (.5, SEGMENT_LENGTH, .5), 'vein')]
            if i == SEGMENTS - 1:
                cubes.append(box((-.75, SEGMENT_LENGTH, -.75), (1.5, 1.5, 1.5), 'knot'))
            j(f'{name}_{i}', previous, pivot if i == 0 else (0, SEGMENT_LENGTH, 0), cubes)
            previous = f'{name}_{i}'
    return parts


def materials():
    def bone(side, px, py, w, h, gx, gy):
        r = noise(gx, gy, 31)
        # Grain runs along the bar; worn ends darken toward the iron caps.
        if r < .12:
            return 'i0'
        return tone(side, py, h, 'i2', 'i1', 'i0', r)

    def vein_wrap(side, px, py, w, h, gx, gy):
        if (px + py) % 3 == 0:
            return 'r3'  # a vein winding round the bar
        return 'r1' if side in ('north', 'up', 'west') else 'r0'

    def vein(side, px, py, w, h, gx, gy):
        return 'r3' if py % 2 == 0 else 'r2'

    def knot(side, px, py, w, h, gx, gy):
        if side == 'up':
            return 'r2'
        return 'r0' if side == 'down' or py == h - 1 else 'r1'

    return {'bone': bone, 'hematic_iron': metal('he2', 'he1', 'he0'), 'vein_wrap': vein_wrap, 'vein': vein, 'knot': knot}


JAVA = r'''package com.vincenthuto.hemomancy.client.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar.CrossbarMotionRules;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Authored by tools/circus/build_marionette_crossbar.py; regenerate the model, atlas and BBModel together.
public final class MarionetteCrossbarModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("marionette_crossbar"), "main");
	/** Strings in puppet assignment order: head first, then the hand bar, then the foot bar. */
	public static final String[] STRINGS = { %STRINGS% };
	public static final int SEGMENTS = %SEGMENTS%;
	/** The knot at the end of each string, in its last segment's pixels; puppet threads continue from here. */
	private static final float KNOT_Y = %KNOT_Y%F;
	private final ModelPart root;
	private final ModelPart grip;
	private final ModelPart control;
	private final ModelPart frontBar;
	private final ModelPart rearBar;
	private final ModelPart[][] strings = new ModelPart[STRINGS.length][SEGMENTS];
	private final ModelPart[][] stringParents = new ModelPart[STRINGS.length][];

	public MarionetteCrossbarModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
		grip = root.getChild("grip");
		control = grip.getChild("control");
		frontBar = control.getChild("front_bar");
		rearBar = control.getChild("rear_bar");
		for (int i = 0; i < STRINGS.length; i++) {
			ModelPart bar = i == 0 ? control : i < 3 ? frontBar : rearBar;
			stringParents[i] = bar == control ? new ModelPart[] { grip, control } : new ModelPart[] { grip, control, bar };
			ModelPart segment = bar;
			for (int s = 0; s < SEGMENTS; s++) {
				segment = segment.getChild(STRINGS[i] + "_" + s);
				strings[i][s] = segment;
			}
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        /* GEOMETRY */
	}

	/** Poses the bar and lets each string hang against the bar's tilt before it swings and flicks. */
	public void pose(CrossbarMotionRules.Pose pose, float time) {
		root.getAllParts().forEach(ModelPart::resetPose);
		grip.y -= pose.rise();
		control.xRot = pose.pitch();
		control.zRot = pose.roll();
		frontBar.zRot = pose.frontRock();
		rearBar.zRot = pose.rearRock();
		for (int i = 0; i < STRINGS.length; i++) {
			float barRock = i == 0 ? 0.0F : i < 3 ? pose.frontRock() : pose.rearRock();
			for (int s = 0; s < SEGMENTS; s++) {
				float lag = (float) Math.sin(time * 0.2F - s * 0.6F - i * 1.3F);
				ModelPart segment = strings[i][s];
				segment.xRot = (s == 0 ? -pose.pitch() : 0.0F) + pose.threadSwing() * lag
						- pose.threadFlick() * (s == 0 ? 0.9F : 0.4F);
				segment.zRot = (s == 0 ? -pose.roll() - barRock : 0.0F) + pose.threadSwing() * 0.6F * lag;
			}
		}
	}

	/** The bone-and-hematic-iron frame; strings are drawn separately so they can pulse. */
	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		for (ModelPart[] string : strings) string[0].visible = false;
		grip.render(poseStack, buffer, packedLight, packedOverlay, color);
		for (ModelPart[] string : strings) string[0].visible = true;
	}

	public void renderStrings(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		for (int i = 0; i < STRINGS.length; i++) {
			poseStack.pushPose();
			for (ModelPart parent : stringParents[i]) parent.translateAndRotate(poseStack);
			strings[i][0].render(poseStack, buffer, packedLight, packedOverlay, color);
			poseStack.popPose();
		}
	}

	/** Where string {@code index}'s knot sits in the current frame, after the latest {@link #pose}. */
	public Vector3f knot(PoseStack poseStack, int index) {
		poseStack.pushPose();
		for (ModelPart parent : stringParents[index]) parent.translateAndRotate(poseStack);
		for (ModelPart segment : strings[index]) segment.translateAndRotate(poseStack);
		Vector3f knot = poseStack.last().pose().transformPosition(0.0F, KNOT_Y / 16.0F, 0.0F, new Vector3f());
		poseStack.popPose();
		return knot;
	}
}
'''


def build():
    parts = crossbar()
    previous_atlas, puppet_rig.ATLAS = puppet_rig.ATLAS, ATLAS
    try:
        statements, materials_by_uv = geometry(parts)
    finally:
        puppet_rig.ATLAS = previous_atlas
    java = (JAVA.replace('%STRINGS%', ', '.join(f'"{name}"' for name, _, _ in STRINGS))
            .replace('%SEGMENTS%', str(SEGMENTS)).replace('%KNOT_Y%', str(SEGMENT_LENGTH + .75)))
    (MODELS / 'MarionetteCrossbarModel.java').write_text(java.replace('/* GEOMETRY */', statements.lstrip()),
                                                         encoding='utf-8')
    uv_origins = list(materials_by_uv.keys())
    model = blockbench('MarionetteCrossbarModel', parts, uv_origins, [], atlas=ATLAS)
    paint(model['elements'], materials_by_uv, materials(), PALETTE, atlas=ATLAS).save(TEXTURE)
    model['textures'] = [texture_entry(TEXTURE, {'index': 0, 'name': 'MarionetteCrossbarModel'}, atlas=ATLAS,
                                       relative_dir='../../../textures/item/')]
    BBMODEL.write_text(json.dumps(model, indent=2) + '\n', encoding='utf-8')
    print(f'MarionetteCrossbarModel: {len(parts)} parts, {len(model["elements"])} cubes, {ATLAS}px atlas')


if __name__ == '__main__':
    build()
