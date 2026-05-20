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
 * Opened-monolith throne model.  The silhouette is intentionally huge in model
 * space: a rear void-black slab, two side pylons, a recessed seat-wound, and
 * crown teeth that read as a split Sanguine Monolith rather than a chair.
 */
public class CovenantThroneModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("modelcovenantthrone"), "main");

	private final ModelPart rearWall;
	private final ModelPart leftPylon;
	private final ModelPart rightPylon;
	private final ModelPart seatWound;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart centerCrown;
	private final ModelPart leftCrown;
	private final ModelPart rightCrown;
	private final ModelPart leftRib;
	private final ModelPart rightRib;

	public CovenantThroneModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.rearWall = root.getChild("rearWall");
		this.leftPylon = root.getChild("leftPylon");
		this.rightPylon = root.getChild("rightPylon");
		this.seatWound = root.getChild("seatWound");
		this.leftArm = root.getChild("leftArm");
		this.rightArm = root.getChild("rightArm");
		this.centerCrown = root.getChild("centerCrown");
		this.leftCrown = root.getChild("leftCrown");
		this.rightCrown = root.getChild("rightCrown");
		this.leftRib = root.getChild("leftRib");
		this.rightRib = root.getChild("rightRib");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("rearWall",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-14.0F, -48.0F, 6.0F, 28.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("leftPylon",
				CubeListBuilder.create()
						.texOffs(0, 54)
						.addBox(-24.0F, -32.0F, -6.0F, 6.0F, 32.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rightPylon",
				CubeListBuilder.create()
						.texOffs(60, 54)
						.addBox(18.0F, -32.0F, -6.0F, 6.0F, 32.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("seatWound",
				CubeListBuilder.create()
						.texOffs(88, 0)
						.addBox(-8.0F, -8.0F, -7.0F, 16.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("leftArm",
				CubeListBuilder.create()
						.texOffs(88, 28)
						.addBox(-18.0F, -18.0F, -7.0F, 8.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rightArm",
				CubeListBuilder.create()
						.texOffs(88, 60)
						.addBox(10.0F, -18.0F, -7.0F, 8.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("centerCrown",
				CubeListBuilder.create()
						.texOffs(0, 122)
						.addBox(-5.0F, -58.0F, 6.0F, 10.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("leftCrown",
				CubeListBuilder.create()
						.texOffs(30, 122)
						.addBox(-22.0F, -40.0F, 6.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rightCrown",
				CubeListBuilder.create()
						.texOffs(56, 122)
						.addBox(14.0F, -40.0F, 6.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("leftRib",
				CubeListBuilder.create()
						.texOffs(84, 92)
						.addBox(-11.0F, -38.0F, 4.0F, 4.0F, 30.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		root.addOrReplaceChild("rightRib",
				CubeListBuilder.create()
						.texOffs(104, 92)
						.addBox(7.0F, -38.0F, 4.0F, 4.0F, 30.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		rearWall.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		leftPylon.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightPylon.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		seatWound.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		centerCrown.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		leftCrown.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightCrown.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		leftRib.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightRib.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
