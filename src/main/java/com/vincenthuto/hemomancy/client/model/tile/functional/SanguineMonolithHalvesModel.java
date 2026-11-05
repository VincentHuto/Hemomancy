package com.vincenthuto.hemomancy.client.model.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * The Sanguine Monolith split down its vertical centre: two 8 x 32 x 8 halves, each hinged at the outer bottom
 * edge of the slab so that {@link #setFall} swings them outward like a book falling open. Drawn in the same pose
 * as {@link SanguineMonolithModel} (rotated 180 degrees about X, so model y 24 is the base).
 */
public class SanguineMonolithHalvesModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("sanguine_monolith_halves"), "main");

	private final ModelPart left;
	private final ModelPart right;

	public SanguineMonolithHalvesModel(ModelPart root) {
		super(RenderType::entitySolid);
		this.left = root.getChild("left");
		this.right = root.getChild("right");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// Each half is hinged where it meets the ground on its own outer side. The boxes stop short of the centre;
		// the renderer fills the last 2.5 pixels with the jagged break, the same profile on both halves.
		root.addOrReplaceChild("left", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0.0F, -32.0F, -4.0F, 5.5F, 32.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-8.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("right", CubeListBuilder.create().texOffs(8, 0)
				.addBox(-5.5F, -32.0F, -4.0F, 5.5F, 32.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(8.0F, 24.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	/**
	 * Swings both halves outward by {@code radians} about their ground hinges. The model is drawn under a 180
	 * degree turn about X, which mirrors a rotation about Z: a negative zRot here leans the left half to the left.
	 */
	public void setFall(float radians) {
		left.zRot = -radians;
		right.zRot = radians;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		left.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		right.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
