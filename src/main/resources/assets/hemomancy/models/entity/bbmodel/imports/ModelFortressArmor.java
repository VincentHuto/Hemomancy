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

public class ModelFortressArmor<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modelfortressarmor"), "main");
	private final ModelPart head;
	private final ModelPart Mask_0;
	private final ModelPart Mask_1;
	private final ModelPart Mask_2;
	private final ModelPart Goggles;
	private final ModelPart OrnamentL;
	private final ModelPart OrnamentL2;
	private final ModelPart OrnamentR;
	private final ModelPart OrnamentR2;
	private final ModelPart Helmet;
	private final ModelPart HelmetR;
	private final ModelPart HelmetL;
	private final ModelPart HelmetB;
	private final ModelPart capsthingy;
	private final ModelPart flapR;
	private final ModelPart flapL;
	private final ModelPart Gemornament;
	private final ModelPart Gem;
	private final ModelPart body;
	private final ModelPart BeltR;
	private final ModelPart Mbelt;
	private final ModelPart MbeltL;
	private final ModelPart MbeltR;
	private final ModelPart BeltL;
	private final ModelPart Chestplate;
	private final ModelPart Scroll;
	private final ModelPart Backplate;
	private final ModelPart Book;
	private final ModelPart right_arm;
	private final ModelPart ShoulderR;
	private final ModelPart GauntletR;
	private final ModelPart GauntletstrapR1;
	private final ModelPart GauntletstrapR2;
	private final ModelPart ShoulderplateRtop;
	private final ModelPart ShoulderplateR1;
	private final ModelPart ShoulderplateR2;
	private final ModelPart ShoulderplateR3;
	private final ModelPart left_arm;
	private final ModelPart ShoulderL;
	private final ModelPart GauntletL;
	private final ModelPart Gauntletstrapl1;
	private final ModelPart GauntletstrapL2;
	private final ModelPart ShoulderplateLtop;
	private final ModelPart ShoulderplateL1;
	private final ModelPart ShoulderplateL2;
	private final ModelPart ShoulderplateL3;
	private final ModelPart right_leg;
	private final ModelPart LegpanelR1;
	private final ModelPart LegpanelR2;
	private final ModelPart LegpanelR3;
	private final ModelPart LegpanelR4;
	private final ModelPart LegpanelR5;
	private final ModelPart LegpanelR6;
	private final ModelPart SidepanelR1;
	private final ModelPart SidepanelR2;
	private final ModelPart SidepanelR3;
	private final ModelPart BackpanelR1;
	private final ModelPart BackpanelR2;
	private final ModelPart BackpanelR3;
	private final ModelPart left_leg;
	private final ModelPart BackpanelL3;
	private final ModelPart LegpanelL1;
	private final ModelPart LegpanelL2;
	private final ModelPart LegpanelL3;
	private final ModelPart LegpanelL4;
	private final ModelPart LegpanelL5;
	private final ModelPart LegpanelL6;
	private final ModelPart SidepanelL1;
	private final ModelPart SidepanelL2;
	private final ModelPart SidepanelL3;
	private final ModelPart BackpanelL1;
	private final ModelPart BackpanelL2;

	public ModelFortressArmor(ModelPart root) {
		this.head = root.getChild("head");
		this.Mask_0 = this.head.getChild("Mask_0");
		this.Mask_1 = this.head.getChild("Mask_1");
		this.Mask_2 = this.head.getChild("Mask_2");
		this.Goggles = this.head.getChild("Goggles");
		this.OrnamentL = this.head.getChild("OrnamentL");
		this.OrnamentL2 = this.head.getChild("OrnamentL2");
		this.OrnamentR = this.head.getChild("OrnamentR");
		this.OrnamentR2 = this.head.getChild("OrnamentR2");
		this.Helmet = this.head.getChild("Helmet");
		this.HelmetR = this.head.getChild("HelmetR");
		this.HelmetL = this.head.getChild("HelmetL");
		this.HelmetB = this.head.getChild("HelmetB");
		this.capsthingy = this.head.getChild("capsthingy");
		this.flapR = this.head.getChild("flapR");
		this.flapL = this.head.getChild("flapL");
		this.Gemornament = this.head.getChild("Gemornament");
		this.Gem = this.head.getChild("Gem");
		this.body = root.getChild("body");
		this.BeltR = this.body.getChild("BeltR");
		this.Mbelt = this.body.getChild("Mbelt");
		this.MbeltL = this.body.getChild("MbeltL");
		this.MbeltR = this.body.getChild("MbeltR");
		this.BeltL = this.body.getChild("BeltL");
		this.Chestplate = this.body.getChild("Chestplate");
		this.Scroll = this.body.getChild("Scroll");
		this.Backplate = this.body.getChild("Backplate");
		this.Book = this.body.getChild("Book");
		this.right_arm = root.getChild("right_arm");
		this.ShoulderR = this.right_arm.getChild("ShoulderR");
		this.GauntletR = this.right_arm.getChild("GauntletR");
		this.GauntletstrapR1 = this.right_arm.getChild("GauntletstrapR1");
		this.GauntletstrapR2 = this.right_arm.getChild("GauntletstrapR2");
		this.ShoulderplateRtop = this.right_arm.getChild("ShoulderplateRtop");
		this.ShoulderplateR1 = this.right_arm.getChild("ShoulderplateR1");
		this.ShoulderplateR2 = this.right_arm.getChild("ShoulderplateR2");
		this.ShoulderplateR3 = this.right_arm.getChild("ShoulderplateR3");
		this.left_arm = root.getChild("left_arm");
		this.ShoulderL = this.left_arm.getChild("ShoulderL");
		this.GauntletL = this.left_arm.getChild("GauntletL");
		this.Gauntletstrapl1 = this.left_arm.getChild("Gauntletstrapl1");
		this.GauntletstrapL2 = this.left_arm.getChild("GauntletstrapL2");
		this.ShoulderplateLtop = this.left_arm.getChild("ShoulderplateLtop");
		this.ShoulderplateL1 = this.left_arm.getChild("ShoulderplateL1");
		this.ShoulderplateL2 = this.left_arm.getChild("ShoulderplateL2");
		this.ShoulderplateL3 = this.left_arm.getChild("ShoulderplateL3");
		this.right_leg = root.getChild("right_leg");
		this.LegpanelR1 = this.right_leg.getChild("LegpanelR1");
		this.LegpanelR2 = this.right_leg.getChild("LegpanelR2");
		this.LegpanelR3 = this.right_leg.getChild("LegpanelR3");
		this.LegpanelR4 = this.right_leg.getChild("LegpanelR4");
		this.LegpanelR5 = this.right_leg.getChild("LegpanelR5");
		this.LegpanelR6 = this.right_leg.getChild("LegpanelR6");
		this.SidepanelR1 = this.right_leg.getChild("SidepanelR1");
		this.SidepanelR2 = this.right_leg.getChild("SidepanelR2");
		this.SidepanelR3 = this.right_leg.getChild("SidepanelR3");
		this.BackpanelR1 = this.right_leg.getChild("BackpanelR1");
		this.BackpanelR2 = this.right_leg.getChild("BackpanelR2");
		this.BackpanelR3 = this.right_leg.getChild("BackpanelR3");
		this.left_leg = root.getChild("left_leg");
		this.BackpanelL3 = this.left_leg.getChild("BackpanelL3");
		this.LegpanelL1 = this.left_leg.getChild("LegpanelL1");
		this.LegpanelL2 = this.left_leg.getChild("LegpanelL2");
		this.LegpanelL3 = this.left_leg.getChild("LegpanelL3");
		this.LegpanelL4 = this.left_leg.getChild("LegpanelL4");
		this.LegpanelL5 = this.left_leg.getChild("LegpanelL5");
		this.LegpanelL6 = this.left_leg.getChild("LegpanelL6");
		this.SidepanelL1 = this.left_leg.getChild("SidepanelL1");
		this.SidepanelL2 = this.left_leg.getChild("SidepanelL2");
		this.SidepanelL3 = this.left_leg.getChild("SidepanelL3");
		this.BackpanelL1 = this.left_leg.getChild("BackpanelL1");
		this.BackpanelL2 = this.left_leg.getChild("BackpanelL2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_0 = head.addOrReplaceChild("Mask_0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_1 = head.addOrReplaceChild("Mask_1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_2 = head.addOrReplaceChild("Mask_2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Goggles = head.addOrReplaceChild("Goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition OrnamentL = head.addOrReplaceChild("OrnamentL", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentL2 = head.addOrReplaceChild("OrnamentL2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentR = head.addOrReplaceChild("OrnamentR", CubeListBuilder.create().texOffs(78, 8).addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentR2 = head.addOrReplaceChild("OrnamentR2", CubeListBuilder.create().texOffs(78, 8).addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition Helmet = head.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition HelmetR = head.addOrReplaceChild("HelmetR", CubeListBuilder.create().texOffs(21, 13).addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition HelmetL = head.addOrReplaceChild("HelmetL", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition HelmetB = head.addOrReplaceChild("HelmetB", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition capsthingy = head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition flapR = head.addOrReplaceChild("flapR", CubeListBuilder.create().texOffs(59, 10).addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, -0.5236F));

		PartDefinition flapL = head.addOrReplaceChild("flapL", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.5236F));

		PartDefinition Gemornament = head.addOrReplaceChild("Gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition Gem = head.addOrReplaceChild("Gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltR = body.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = body.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Scroll = body.addOrReplaceChild("Scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-6.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.192F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Book = body.addOrReplaceChild("Book", CubeListBuilder.create().texOffs(100, 8).addBox(-6.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7679F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletR = right_arm.addOrReplaceChild("GauntletR", CubeListBuilder.create().texOffs(100, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR1 = right_arm.addOrReplaceChild("GauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR2 = right_arm.addOrReplaceChild("GauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderplateRtop = right_arm.addOrReplaceChild("ShoulderplateRtop", CubeListBuilder.create().texOffs(110, 37).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR1 = right_arm.addOrReplaceChild("ShoulderplateR1", CubeListBuilder.create().texOffs(110, 45).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR2 = right_arm.addOrReplaceChild("ShoulderplateR2", CubeListBuilder.create().texOffs(94, 45).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR3 = right_arm.addOrReplaceChild("ShoulderplateR3", CubeListBuilder.create().texOffs(94, 45).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletL = left_arm.addOrReplaceChild("GauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Gauntletstrapl1 = left_arm.addOrReplaceChild("Gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL2 = left_arm.addOrReplaceChild("GauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderplateLtop = left_arm.addOrReplaceChild("ShoulderplateLtop", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL1 = left_arm.addOrReplaceChild("ShoulderplateL1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL2 = left_arm.addOrReplaceChild("ShoulderplateL2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL3 = left_arm.addOrReplaceChild("ShoulderplateL3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition LegpanelR1 = right_leg.addOrReplaceChild("LegpanelR1", CubeListBuilder.create().texOffs(0, 51).addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR2 = right_leg.addOrReplaceChild("LegpanelR2", CubeListBuilder.create().texOffs(8, 51).addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR3 = right_leg.addOrReplaceChild("LegpanelR3", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR4 = right_leg.addOrReplaceChild("LegpanelR4", CubeListBuilder.create().texOffs(0, 43).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR5 = right_leg.addOrReplaceChild("LegpanelR5", CubeListBuilder.create().texOffs(0, 47).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR6 = right_leg.addOrReplaceChild("LegpanelR6", CubeListBuilder.create().texOffs(6, 43).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelR1 = right_leg.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(0, 22).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition SidepanelR2 = right_leg.addOrReplaceChild("SidepanelR2", CubeListBuilder.create().texOffs(0, 31).addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition SidepanelR3 = right_leg.addOrReplaceChild("SidepanelR3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition BackpanelR1 = right_leg.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelR2 = right_leg.addOrReplaceChild("BackpanelR2", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelR3 = right_leg.addOrReplaceChild("BackpanelR3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelL3 = left_leg.addOrReplaceChild("BackpanelL3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL1 = left_leg.addOrReplaceChild("LegpanelL1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL2 = left_leg.addOrReplaceChild("LegpanelL2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL3 = left_leg.addOrReplaceChild("LegpanelL3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL4 = left_leg.addOrReplaceChild("LegpanelL4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL5 = left_leg.addOrReplaceChild("LegpanelL5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL6 = left_leg.addOrReplaceChild("LegpanelL6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelL1 = left_leg.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition SidepanelL2 = left_leg.addOrReplaceChild("SidepanelL2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition SidepanelL3 = left_leg.addOrReplaceChild("SidepanelL3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition BackpanelL1 = left_leg.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelL2 = left_leg.addOrReplaceChild("BackpanelL2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}