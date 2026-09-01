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

public class ModelRobe<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modelrobe"), "main");
	private final ModelPart BeltR;
	private final ModelPart BeltL;
	private final ModelPart head;
	private final ModelPart Hood1;
	private final ModelPart Hood2;
	private final ModelPart Hood3;
	private final ModelPart Hood4;
	private final ModelPart body;
	private final ModelPart Chestthing;
	private final ModelPart Mbelt;
	private final ModelPart MbeltB;
	private final ModelPart ClothchestL;
	private final ModelPart ClothchestR;
	private final ModelPart Book;
	private final ModelPart Scroll;
	private final ModelPart Backplate;
	private final ModelPart MbeltL;
	private final ModelPart MbeltR;
	private final ModelPart Chestplate;
	private final ModelPart FrontclothR1;
	private final ModelPart FrontclothR2;
	private final ModelPart FrontclothL1;
	private final ModelPart FrontclothL2;
	private final ModelPart ClothBackR1;
	private final ModelPart ClothBackR2;
	private final ModelPart ClothBackR3;
	private final ModelPart ClothBackL1;
	private final ModelPart ClothBackL2;
	private final ModelPart ClothBackL3;
	private final ModelPart left_leg;
	private final ModelPart SideclothL2;
	private final ModelPart SideclothL3;
	private final ModelPart Focipouch;
	private final ModelPart SideclothL1;
	private final ModelPart LegpanelL4;
	private final ModelPart LegpanelL5;
	private final ModelPart LegpanelL6;
	private final ModelPart SidepanelL1;
	private final ModelPart right_arm;
	private final ModelPart ShoulderplateR1;
	private final ModelPart ShoulderplateR2;
	private final ModelPart ShoulderplateR3;
	private final ModelPart ShoulderplateTopR;
	private final ModelPart RArm1;
	private final ModelPart RArm2;
	private final ModelPart RArm3;
	private final ModelPart ShoulderR;
	private final ModelPart left_arm;
	private final ModelPart ShoulderplateL1;
	private final ModelPart ShoulderplateL2;
	private final ModelPart ShoulderplateL3;
	private final ModelPart ShoulderplateTopL;
	private final ModelPart LArm1;
	private final ModelPart LArm2;
	private final ModelPart LArm3;
	private final ModelPart ShoulderL;
	private final ModelPart right_leg;
	private final ModelPart SideclothR1;
	private final ModelPart SideclothR2;
	private final ModelPart SideclothR3;
	private final ModelPart SidepanelR1;
	private final ModelPart LegpanelR6;
	private final ModelPart LegpanelR5;
	private final ModelPart LegpanelR4;

	public ModelRobe(ModelPart root) {
		this.BeltR = root.getChild("BeltR");
		this.BeltL = root.getChild("BeltL");
		this.head = root.getChild("head");
		this.Hood1 = this.head.getChild("Hood1");
		this.Hood2 = this.head.getChild("Hood2");
		this.Hood3 = this.head.getChild("Hood3");
		this.Hood4 = this.head.getChild("Hood4");
		this.body = root.getChild("body");
		this.Chestthing = this.body.getChild("Chestthing");
		this.Mbelt = this.body.getChild("Mbelt");
		this.MbeltB = this.body.getChild("MbeltB");
		this.ClothchestL = this.body.getChild("ClothchestL");
		this.ClothchestR = this.body.getChild("ClothchestR");
		this.Book = this.body.getChild("Book");
		this.Scroll = this.body.getChild("Scroll");
		this.Backplate = this.body.getChild("Backplate");
		this.MbeltL = this.body.getChild("MbeltL");
		this.MbeltR = this.body.getChild("MbeltR");
		this.Chestplate = this.body.getChild("Chestplate");
		this.FrontclothR1 = this.body.getChild("FrontclothR1");
		this.FrontclothR2 = this.body.getChild("FrontclothR2");
		this.FrontclothL1 = this.body.getChild("FrontclothL1");
		this.FrontclothL2 = this.body.getChild("FrontclothL2");
		this.ClothBackR1 = this.body.getChild("ClothBackR1");
		this.ClothBackR2 = this.body.getChild("ClothBackR2");
		this.ClothBackR3 = this.body.getChild("ClothBackR3");
		this.ClothBackL1 = this.body.getChild("ClothBackL1");
		this.ClothBackL2 = this.body.getChild("ClothBackL2");
		this.ClothBackL3 = this.body.getChild("ClothBackL3");
		this.left_leg = root.getChild("left_leg");
		this.SideclothL2 = this.left_leg.getChild("SideclothL2");
		this.SideclothL3 = this.left_leg.getChild("SideclothL3");
		this.Focipouch = this.left_leg.getChild("Focipouch");
		this.SideclothL1 = this.left_leg.getChild("SideclothL1");
		this.LegpanelL4 = this.left_leg.getChild("LegpanelL4");
		this.LegpanelL5 = this.left_leg.getChild("LegpanelL5");
		this.LegpanelL6 = this.left_leg.getChild("LegpanelL6");
		this.SidepanelL1 = this.left_leg.getChild("SidepanelL1");
		this.right_arm = root.getChild("right_arm");
		this.ShoulderplateR1 = this.right_arm.getChild("ShoulderplateR1");
		this.ShoulderplateR2 = this.right_arm.getChild("ShoulderplateR2");
		this.ShoulderplateR3 = this.right_arm.getChild("ShoulderplateR3");
		this.ShoulderplateTopR = this.right_arm.getChild("ShoulderplateTopR");
		this.RArm1 = this.right_arm.getChild("RArm1");
		this.RArm2 = this.right_arm.getChild("RArm2");
		this.RArm3 = this.right_arm.getChild("RArm3");
		this.ShoulderR = this.right_arm.getChild("ShoulderR");
		this.left_arm = root.getChild("left_arm");
		this.ShoulderplateL1 = this.left_arm.getChild("ShoulderplateL1");
		this.ShoulderplateL2 = this.left_arm.getChild("ShoulderplateL2");
		this.ShoulderplateL3 = this.left_arm.getChild("ShoulderplateL3");
		this.ShoulderplateTopL = this.left_arm.getChild("ShoulderplateTopL");
		this.LArm1 = this.left_arm.getChild("LArm1");
		this.LArm2 = this.left_arm.getChild("LArm2");
		this.LArm3 = this.left_arm.getChild("LArm3");
		this.ShoulderL = this.left_arm.getChild("ShoulderL");
		this.right_leg = root.getChild("right_leg");
		this.SideclothR1 = this.right_leg.getChild("SideclothR1");
		this.SideclothR2 = this.right_leg.getChild("SideclothR2");
		this.SideclothR3 = this.right_leg.getChild("SideclothR3");
		this.SidepanelR1 = this.right_leg.getChild("SidepanelR1");
		this.LegpanelR6 = this.right_leg.getChild("LegpanelR6");
		this.LegpanelR5 = this.right_leg.getChild("LegpanelR5");
		this.LegpanelR4 = this.right_leg.getChild("LegpanelR4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition BeltR = partdefinition.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = partdefinition.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Hood2 = head.addOrReplaceChild("Hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

		PartDefinition Hood3 = head.addOrReplaceChild("Hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition Hood4 = head.addOrReplaceChild("Hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestthing = body.addOrReplaceChild("Chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltB = body.addOrReplaceChild("MbeltB", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

		PartDefinition ClothchestL = body.addOrReplaceChild("ClothchestL", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ClothchestR = body.addOrReplaceChild("ClothchestR", CubeListBuilder.create().texOffs(108, 38).addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Book = body.addOrReplaceChild("Book", CubeListBuilder.create().texOffs(81, 16).addBox(-6.0F, 0.0F, 4.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7679F));

		PartDefinition Scroll = body.addOrReplaceChild("Scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-6.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.192F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition FrontclothR1 = body.addOrReplaceChild("FrontclothR1", CubeListBuilder.create().texOffs(108, 38).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -2.9F, -0.1047F, 0.0F, 0.0F));

		PartDefinition FrontclothR2 = body.addOrReplaceChild("FrontclothR2", CubeListBuilder.create().texOffs(108, 47).addBox(-3.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -2.9F, -0.3316F, 0.0F, 0.0F));

		PartDefinition FrontclothL1 = body.addOrReplaceChild("FrontclothL1", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.1047F, 0.0F, 0.0F));

		PartDefinition FrontclothL2 = body.addOrReplaceChild("FrontclothL2", CubeListBuilder.create().texOffs(108, 47).mirror().addBox(-3.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.3316F, 0.0F, 0.0F));

		PartDefinition ClothBackR1 = body.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(118, 16).mirror().addBox(-4.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackR2 = body.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(123, 9).addBox(-1.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackR3 = body.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(120, 12).mirror().addBox(-4.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL1 = body.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(118, 16).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackL2 = body.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(123, 9).mirror().addBox(-4.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL3 = body.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(120, 12).addBox(-3.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition SideclothL2 = left_leg.addOrReplaceChild("SideclothL2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2967F));

		PartDefinition SideclothL3 = left_leg.addOrReplaceChild("SideclothL3", CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition Focipouch = left_leg.addOrReplaceChild("Focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(-6.5F, 0.5F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothL1 = left_leg.addOrReplaceChild("SideclothL1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition LegpanelL4 = left_leg.addOrReplaceChild("LegpanelL4", CubeListBuilder.create().texOffs(76, 38).mirror().addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL5 = left_leg.addOrReplaceChild("LegpanelL5", CubeListBuilder.create().texOffs(76, 42).mirror().addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL6 = left_leg.addOrReplaceChild("LegpanelL6", CubeListBuilder.create().texOffs(82, 38).mirror().addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelL1 = left_leg.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(116, 25).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderplateR1 = right_arm.addOrReplaceChild("ShoulderplateR1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR2 = right_arm.addOrReplaceChild("ShoulderplateR2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR3 = right_arm.addOrReplaceChild("ShoulderplateR3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateTopR = right_arm.addOrReplaceChild("ShoulderplateTopR", CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition RArm1 = right_arm.addOrReplaceChild("RArm1", CubeListBuilder.create().texOffs(88, 39).addBox(-1.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RArm2 = right_arm.addOrReplaceChild("RArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RArm3 = right_arm.addOrReplaceChild("RArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderplateL1 = left_arm.addOrReplaceChild("ShoulderplateL1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL2 = left_arm.addOrReplaceChild("ShoulderplateL2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL3 = left_arm.addOrReplaceChild("ShoulderplateL3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateTopL = left_arm.addOrReplaceChild("ShoulderplateTopL", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition LArm1 = left_arm.addOrReplaceChild("LArm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-3.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LArm2 = left_arm.addOrReplaceChild("LArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LArm3 = left_arm.addOrReplaceChild("LArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition SideclothR1 = right_leg.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothR2 = right_leg.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2967F));

		PartDefinition SideclothR3 = right_leg.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SidepanelR1 = right_leg.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(116, 25).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition LegpanelR6 = right_leg.addOrReplaceChild("LegpanelR6", CubeListBuilder.create().texOffs(82, 38).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR5 = right_leg.addOrReplaceChild("LegpanelR5", CubeListBuilder.create().texOffs(76, 42).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR4 = right_leg.addOrReplaceChild("LegpanelR4", CubeListBuilder.create().texOffs(76, 38).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

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
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}