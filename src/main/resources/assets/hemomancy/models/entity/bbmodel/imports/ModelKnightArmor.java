// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class ModelKnightArmor<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modelknightarmor"), "main");
	private final ModelPart BeltR;
	private final ModelPart BeltL;
	private final ModelPart head;
	private final ModelPart Helmet;
	private final ModelPart body;
	private final ModelPart Mbelt;
	private final ModelPart MbeltL;
	private final ModelPart MbeltR;
	private final ModelPart Tabbard;
	private final ModelPart CloakAtL;
	private final ModelPart Backplate;
	private final ModelPart Cloak1;
	private final ModelPart Cloak2;
	private final ModelPart Cloak3;
	private final ModelPart CloakAtR;
	private final ModelPart Chestplate;
	private final ModelPart Frontcloth1;
	private final ModelPart Frontcloth2;
	private final ModelPart right_arm;
	private final ModelPart ShoulderR1;
	private final ModelPart GauntletR;
	private final ModelPart GauntletstrapR1;
	private final ModelPart GauntletstrapR2;
	private final ModelPart ShoulderR;
	private final ModelPart ShoulderR0;
	private final ModelPart ShoulderR2;
	private final ModelPart left_arm;
	private final ModelPart ShoulderL1;
	private final ModelPart GauntletL;
	private final ModelPart GauntletstrapL1;
	private final ModelPart GauntletstrapL2;
	private final ModelPart ShoulderL;
	private final ModelPart ShoulderL0;
	private final ModelPart ShoulderL2;
	private final ModelPart right_leg;
	private final ModelPart SidepanelR3;
	private final ModelPart SidepanelR2;
	private final ModelPart SidepanelR0;
	private final ModelPart SidepanelR1;
	private final ModelPart left_leg;
	private final ModelPart SidepanelL2;
	private final ModelPart SidepanelL0;
	private final ModelPart SidepanelL3;
	private final ModelPart SidepanelL1;

	public ModelKnightArmor(ModelPart root) {
		this.BeltR = root.getChild("BeltR");
		this.BeltL = root.getChild("BeltL");
		this.head = root.getChild("head");
		this.Helmet = this.head.getChild("Helmet");
		this.body = root.getChild("body");
		this.Mbelt = this.body.getChild("Mbelt");
		this.MbeltL = this.body.getChild("MbeltL");
		this.MbeltR = this.body.getChild("MbeltR");
		this.Tabbard = this.body.getChild("Tabbard");
		this.CloakAtL = this.body.getChild("CloakAtL");
		this.Backplate = this.body.getChild("Backplate");
		this.Cloak1 = this.body.getChild("Cloak1");
		this.Cloak2 = this.body.getChild("Cloak2");
		this.Cloak3 = this.body.getChild("Cloak3");
		this.CloakAtR = this.body.getChild("CloakAtR");
		this.Chestplate = this.body.getChild("Chestplate");
		this.Frontcloth1 = this.body.getChild("Frontcloth1");
		this.Frontcloth2 = this.body.getChild("Frontcloth2");
		this.right_arm = root.getChild("right_arm");
		this.ShoulderR1 = this.right_arm.getChild("ShoulderR1");
		this.GauntletR = this.right_arm.getChild("GauntletR");
		this.GauntletstrapR1 = this.right_arm.getChild("GauntletstrapR1");
		this.GauntletstrapR2 = this.right_arm.getChild("GauntletstrapR2");
		this.ShoulderR = this.right_arm.getChild("ShoulderR");
		this.ShoulderR0 = this.right_arm.getChild("ShoulderR0");
		this.ShoulderR2 = this.right_arm.getChild("ShoulderR2");
		this.left_arm = root.getChild("left_arm");
		this.ShoulderL1 = this.left_arm.getChild("ShoulderL1");
		this.GauntletL = this.left_arm.getChild("GauntletL");
		this.GauntletstrapL1 = this.left_arm.getChild("GauntletstrapL1");
		this.GauntletstrapL2 = this.left_arm.getChild("GauntletstrapL2");
		this.ShoulderL = this.left_arm.getChild("ShoulderL");
		this.ShoulderL0 = this.left_arm.getChild("ShoulderL0");
		this.ShoulderL2 = this.left_arm.getChild("ShoulderL2");
		this.right_leg = root.getChild("right_leg");
		this.SidepanelR3 = this.right_leg.getChild("SidepanelR3");
		this.SidepanelR2 = this.right_leg.getChild("SidepanelR2");
		this.SidepanelR0 = this.right_leg.getChild("SidepanelR0");
		this.SidepanelR1 = this.right_leg.getChild("SidepanelR1");
		this.left_leg = root.getChild("left_leg");
		this.SidepanelL2 = this.left_leg.getChild("SidepanelL2");
		this.SidepanelL0 = this.left_leg.getChild("SidepanelL0");
		this.SidepanelL3 = this.left_leg.getChild("SidepanelL3");
		this.SidepanelL1 = this.left_leg.getChild("SidepanelL1");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition BeltR = partdefinition.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = partdefinition.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Helmet = head.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Tabbard = body.addOrReplaceChild("Tabbard", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 1.2F, -3.5F, 6.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition CloakAtL = body.addOrReplaceChild("CloakAtL", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Cloak1 = body.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-9.0F, 0.0F, 0.0F, 9.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 1.3F, 4.2F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak2 = body.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-9.0F, 11.7F, -2.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 1.3F, 4.2F, 0.3069F, 0.0F, 0.0F));

		PartDefinition Cloak3 = body.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(-9.0F, 15.2F, -4.2F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 1.3F, 4.2F, 0.4466F, 0.0F, 0.0F));

		PartDefinition CloakAtR = body.addOrReplaceChild("CloakAtR", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Frontcloth1 = body.addOrReplaceChild("Frontcloth1", CubeListBuilder.create().texOffs(120, 39).addBox(-6.0F, 0.0F, 0.0F, 6.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -3.5F, -0.1047F, 0.0F, 0.0F));

		PartDefinition Frontcloth2 = body.addOrReplaceChild("Frontcloth2", CubeListBuilder.create().texOffs(100, 37).addBox(-6.0F, 7.5F, 1.8F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -3.5F, -0.3316F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderR1 = right_arm.addOrReplaceChild("ShoulderR1", CubeListBuilder.create().texOffs(0, 19).addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition GauntletR = right_arm.addOrReplaceChild("GauntletR", CubeListBuilder.create().texOffs(100, 26).mirror().addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR1 = right_arm.addOrReplaceChild("GauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR2 = right_arm.addOrReplaceChild("GauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR0 = right_arm.addOrReplaceChild("ShoulderR0", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR2 = right_arm.addOrReplaceChild("ShoulderR2", CubeListBuilder.create().texOffs(0, 11).mirror().addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderL1 = left_arm.addOrReplaceChild("ShoulderL1", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition GauntletL = left_arm.addOrReplaceChild("GauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL1 = left_arm.addOrReplaceChild("GauntletstrapL1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL2 = left_arm.addOrReplaceChild("GauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL0 = left_arm.addOrReplaceChild("ShoulderL0", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL2 = left_arm.addOrReplaceChild("ShoulderL2", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition SidepanelR3 = right_leg.addOrReplaceChild("SidepanelR3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition SidepanelR2 = right_leg.addOrReplaceChild("SidepanelR2", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition SidepanelR0 = right_leg.addOrReplaceChild("SidepanelR0", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition SidepanelR1 = right_leg.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition SidepanelL2 = left_leg.addOrReplaceChild("SidepanelL2", CubeListBuilder.create().texOffs(114, 5).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition SidepanelL0 = left_leg.addOrReplaceChild("SidepanelL0", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition SidepanelL3 = left_leg.addOrReplaceChild("SidepanelL3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition SidepanelL1 = left_leg.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(96, 7).addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		BeltR.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		BeltL.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}