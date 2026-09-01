package com.vincenthuto.hemomancy.client.model.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition rearWall = partdefinition.addOrReplaceChild("rearWall", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -48.0F, 7.0F, 28.0F, 48.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leftPylon = partdefinition.addOrReplaceChild("leftPylon", CubeListBuilder.create().texOffs(0, 54).addBox(14.0F, -29.0F, -6.0F, 6.0F, 29.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rightPylon = partdefinition.addOrReplaceChild("rightPylon", CubeListBuilder.create().texOffs(60, 54).addBox(-20.0F, -28.0F, -6.0F, 6.0F, 28.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition seatWound = partdefinition.addOrReplaceChild("seatWound", CubeListBuilder.create().texOffs(88, 0).addBox(-8.0F, -8.0F, -7.0F, 16.0F, 8.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leftArm = partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(89, 29).addBox(8.0F, -14.0F, -4.0F, 8.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(89, 61).addBox(-16.0F, -14.0F, -4.0F, 8.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition centerCrown = partdefinition.addOrReplaceChild("centerCrown", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leftCrown = partdefinition.addOrReplaceChild("leftCrown", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rightCrown = partdefinition.addOrReplaceChild("rightCrown", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition leftRib = partdefinition.addOrReplaceChild("leftRib", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition rightRib = partdefinition.addOrReplaceChild("rightRib", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
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
