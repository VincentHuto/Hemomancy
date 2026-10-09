package com.vincenthuto.hemomancy.client.model.item;

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
	public static final String[] STRINGS = { "head_string", "left_hand_string", "right_hand_string", "left_foot_string", "right_foot_string" };
	public static final int SEGMENTS = 3;
	/** The knot at the end of each string, in its last segment's pixels; puppet threads continue from here. */
	private static final float KNOT_Y = 3.75F;
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
        PartDefinition grip = mesh.getRoot().addOrReplaceChild("grip", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(9, 0).addBox(-1.5F, 3.5F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition control = grip.addOrReplaceChild("control", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 14.0F).texOffs(0, 17).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F).texOffs(13, 17).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition front_bar = control.addOrReplaceChild("front_bar", CubeListBuilder.create().texOffs(18, 17).addBox(-6.0F, -1.5F, -1.0F, 12.0F, 2.0F, 2.0F).texOffs(47, 17).addBox(-7.0F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F).texOffs(56, 17).addBox(6.0F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 0.0F, -5.5F));
        PartDefinition rear_bar = control.addOrReplaceChild("rear_bar", CubeListBuilder.create().texOffs(0, 24).addBox(-4.5F, -1.5F, -1.0F, 9.0F, 2.0F, 2.0F).texOffs(23, 24).addBox(-5.5F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F).texOffs(32, 24).addBox(4.5F, -2.0F, -1.5F, 1.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 0.0F, 4.5F));
        PartDefinition head_string_0 = control.addOrReplaceChild("head_string_0", CubeListBuilder.create().texOffs(41, 24).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 1.0F, -7.5F));
        PartDefinition head_string_1 = head_string_0.addOrReplaceChild("head_string_1", CubeListBuilder.create().texOffs(46, 24).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition head_string_2 = head_string_1.addOrReplaceChild("head_string_2", CubeListBuilder.create().texOffs(51, 24).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F).texOffs(56, 24).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_hand_string_0 = front_bar.addOrReplaceChild("left_hand_string_0", CubeListBuilder.create().texOffs(0, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(6.5F, 1.0F, 0.0F));
        PartDefinition left_hand_string_1 = left_hand_string_0.addOrReplaceChild("left_hand_string_1", CubeListBuilder.create().texOffs(5, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_hand_string_2 = left_hand_string_1.addOrReplaceChild("left_hand_string_2", CubeListBuilder.create().texOffs(10, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F).texOffs(15, 31).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_hand_string_0 = front_bar.addOrReplaceChild("right_hand_string_0", CubeListBuilder.create().texOffs(24, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(-6.5F, 1.0F, 0.0F));
        PartDefinition right_hand_string_1 = right_hand_string_0.addOrReplaceChild("right_hand_string_1", CubeListBuilder.create().texOffs(29, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_hand_string_2 = right_hand_string_1.addOrReplaceChild("right_hand_string_2", CubeListBuilder.create().texOffs(34, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F).texOffs(39, 31).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_foot_string_0 = rear_bar.addOrReplaceChild("left_foot_string_0", CubeListBuilder.create().texOffs(48, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(5.0F, 1.0F, 0.0F));
        PartDefinition left_foot_string_1 = left_foot_string_0.addOrReplaceChild("left_foot_string_1", CubeListBuilder.create().texOffs(53, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_foot_string_2 = left_foot_string_1.addOrReplaceChild("left_foot_string_2", CubeListBuilder.create().texOffs(58, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F).texOffs(0, 36).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_foot_string_0 = rear_bar.addOrReplaceChild("right_foot_string_0", CubeListBuilder.create().texOffs(9, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(-5.0F, 1.0F, 0.0F));
        PartDefinition right_foot_string_1 = right_foot_string_0.addOrReplaceChild("right_foot_string_1", CubeListBuilder.create().texOffs(14, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_foot_string_2 = right_foot_string_1.addOrReplaceChild("right_foot_string_2", CubeListBuilder.create().texOffs(19, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F).texOffs(24, 36).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
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

	/** The bone-and-brass frame; strings are drawn separately so they can pulse. */
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
