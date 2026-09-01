package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.PaleIntercessionEntity;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PaleIntercessionModel extends HierarchicalModel<PaleIntercessionEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("pale_intercession"), "main");
	private final ModelPart whole;

	public PaleIntercessionModel(ModelPart root) { this.whole = root.getChild("whole"); }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition whole = root.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
		PartDefinition robe = whole.addOrReplaceChild("robe", CubeListBuilder.create()
				.texOffs(0, 40).addBox(-5, -15, -3, 10, 15, 6, new CubeDeformation(0))
				.texOffs(48, 40).addBox(-6, -10, -3.5f, 12, 10, 7, new CubeDeformation(-.15f)), PartPose.ZERO);
		robe.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4, -22, -2.5f, 8, 8, 5, new CubeDeformation(0)), PartPose.ZERO);
		whole.addOrReplaceChild("veil", CubeListBuilder.create().texOffs(32, 0)
				.addBox(-4.5f, -30, -3.5f, 9, 9, 7, new CubeDeformation(.35f))
				.texOffs(64, 0).addBox(-3.5f, -28, -4, 7, 2, 1, new CubeDeformation(0)), PartPose.ZERO);
		whole.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 64)
				.addBox(-1.5f, 0, -1.5f, 3, 12, 3, new CubeDeformation(0)), PartPose.offset(5, -21, 0));
		whole.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(16, 64)
				.addBox(-1.5f, 0, -1.5f, 3, 12, 3, new CubeDeformation(0)), PartPose.offset(-5, -21, 0));
		whole.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(0, 84)
				.addBox(-6, -34, 1.5f, 12, 1, 1, new CubeDeformation(0))
				.texOffs(0, 88).addBox(-6, -24, 1.5f, 12, 1, 1, new CubeDeformation(0))
				.texOffs(0, 92).addBox(-6, -33, 1.5f, 1, 9, 1, new CubeDeformation(0))
				.texOffs(4, 92).addBox(5, -33, 1.5f, 1, 9, 1, new CubeDeformation(0)), PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override public ModelPart root() { return whole; }
	@Override public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
		whole.render(pose, consumer, light, overlay, color);
	}
	@Override public void setupAnim(PaleIntercessionEntity entity, float swing, float swingAmount, float age, float yaw, float pitch) {
		whole.getAllParts().forEach(ModelPart::resetPose);
		AnimationDefinition animation = switch (entity.getPresentation()) {
			case MANIFEST -> PaleIntercessionAnimations.MANIFEST;
			case GLIDE -> PaleIntercessionAnimations.GLIDE;
			case INTERPOSE -> PaleIntercessionAnimations.INTERPOSE;
			case STRIKE -> PaleIntercessionAnimations.STRIKE;
			case DISTORT -> PaleIntercessionAnimations.DISTORT;
			case DISSOLVE -> PaleIntercessionAnimations.DISSOLVE;
			case STILL -> PaleIntercessionAnimations.STILL;
		};
		animate(entity.presentationAnimationState, animation, age);
	}
}
