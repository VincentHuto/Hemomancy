// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class ModelLeaderArmor<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "modelleaderarmor"), "main");
	private final ModelPart head;
	private final ModelPart Helmet;
	private final ModelPart body;
	private final ModelPart CollarF;
	private final ModelPart CollarB;
	private final ModelPart CollarR;
	private final ModelPart CollarL;
	private final ModelPart BeltR;
	private final ModelPart Mbelt;
	private final ModelPart MbeltL;
	private final ModelPart MbeltR;
	private final ModelPart BeltL;
	private final ModelPart CloakTL;
	private final ModelPart Cloak3;
	private final ModelPart CloakTR;
	private final ModelPart Cloak1;
	private final ModelPart Cloak2;
	private final ModelPart Chestplate;
	private final ModelPart ChestOrnament;
	private final ModelPart ChestClothL;
	private final ModelPart ChestClothR;
	private final ModelPart Backplate;
	private final ModelPart LegClothR;
	private final ModelPart LegClothL;
	private final ModelPart right_arm;
	private final ModelPart GauntletR;
	private final ModelPart GauntletstrapR1;
	private final ModelPart GauntletstrapR2;
	private final ModelPart GauntletR2;
	private final ModelPart ShoulderR;
	private final ModelPart ShoulderR1;
	private final ModelPart ShoulderR2;
	private final ModelPart ShoulderR5;
	private final ModelPart ShoulderR3;
	private final ModelPart ShoulderR4;
	private final ModelPart left_arm;
	private final ModelPart GauntletL;
	private final ModelPart GauntletstrapL1;
	private final ModelPart GauntletstrapL2;
	private final ModelPart GauntletL2;
	private final ModelPart ShoulderL;
	private final ModelPart ShoulderL1;
	private final ModelPart ShoulderL2;
	private final ModelPart ShoulderL3;
	private final ModelPart ShoulderL5;
	private final ModelPart ShoulderL4;
	private final ModelPart right_leg;
	private final ModelPart BackpanelR1;
	private final ModelPart BackpanelR2;
	private final ModelPart BackpanelR3;
	private final ModelPart BackpanelR4;
	private final ModelPart left_leg;
	private final ModelPart BackpanelL1;
	private final ModelPart BackpanelL4;
	private final ModelPart BackpanelL2;
	private final ModelPart BackpanelL3;

	public ModelLeaderArmor(ModelPart root) {
		this.head = root.getChild("head");
		this.Helmet = this.head.getChild("Helmet");
		this.body = root.getChild("body");
		this.CollarF = this.body.getChild("CollarF");
		this.CollarB = this.body.getChild("CollarB");
		this.CollarR = this.body.getChild("CollarR");
		this.CollarL = this.body.getChild("CollarL");
		this.BeltR = this.body.getChild("BeltR");
		this.Mbelt = this.body.getChild("Mbelt");
		this.MbeltL = this.body.getChild("MbeltL");
		this.MbeltR = this.body.getChild("MbeltR");
		this.BeltL = this.body.getChild("BeltL");
		this.CloakTL = this.body.getChild("CloakTL");
		this.Cloak3 = this.body.getChild("Cloak3");
		this.CloakTR = this.body.getChild("CloakTR");
		this.Cloak1 = this.body.getChild("Cloak1");
		this.Cloak2 = this.body.getChild("Cloak2");
		this.Chestplate = this.body.getChild("Chestplate");
		this.ChestOrnament = this.body.getChild("ChestOrnament");
		this.ChestClothL = this.body.getChild("ChestClothL");
		this.ChestClothR = this.body.getChild("ChestClothR");
		this.Backplate = this.body.getChild("Backplate");
		this.LegClothR = this.body.getChild("LegClothR");
		this.LegClothL = this.body.getChild("LegClothL");
		this.right_arm = root.getChild("right_arm");
		this.GauntletR = this.right_arm.getChild("GauntletR");
		this.GauntletstrapR1 = this.right_arm.getChild("GauntletstrapR1");
		this.GauntletstrapR2 = this.right_arm.getChild("GauntletstrapR2");
		this.GauntletR2 = this.right_arm.getChild("GauntletR2");
		this.ShoulderR = this.right_arm.getChild("ShoulderR");
		this.ShoulderR1 = this.right_arm.getChild("ShoulderR1");
		this.ShoulderR2 = this.right_arm.getChild("ShoulderR2");
		this.ShoulderR5 = this.right_arm.getChild("ShoulderR5");
		this.ShoulderR3 = this.right_arm.getChild("ShoulderR3");
		this.ShoulderR4 = this.right_arm.getChild("ShoulderR4");
		this.left_arm = root.getChild("left_arm");
		this.GauntletL = this.left_arm.getChild("GauntletL");
		this.GauntletstrapL1 = this.left_arm.getChild("GauntletstrapL1");
		this.GauntletstrapL2 = this.left_arm.getChild("GauntletstrapL2");
		this.GauntletL2 = this.left_arm.getChild("GauntletL2");
		this.ShoulderL = this.left_arm.getChild("ShoulderL");
		this.ShoulderL1 = this.left_arm.getChild("ShoulderL1");
		this.ShoulderL2 = this.left_arm.getChild("ShoulderL2");
		this.ShoulderL3 = this.left_arm.getChild("ShoulderL3");
		this.ShoulderL5 = this.left_arm.getChild("ShoulderL5");
		this.ShoulderL4 = this.left_arm.getChild("ShoulderL4");
		this.right_leg = root.getChild("right_leg");
		this.BackpanelR1 = this.right_leg.getChild("BackpanelR1");
		this.BackpanelR2 = this.right_leg.getChild("BackpanelR2");
		this.BackpanelR3 = this.right_leg.getChild("BackpanelR3");
		this.BackpanelR4 = this.right_leg.getChild("BackpanelR4");
		this.left_leg = root.getChild("left_leg");
		this.BackpanelL1 = this.left_leg.getChild("BackpanelL1");
		this.BackpanelL4 = this.left_leg.getChild("BackpanelL4");
		this.BackpanelL2 = this.left_leg.getChild("BackpanelL2");
		this.BackpanelL3 = this.left_leg.getChild("BackpanelL3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Helmet = head.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition CollarF = body.addOrReplaceChild("CollarF", CubeListBuilder.create().texOffs(17, 31).addBox(-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarB = body.addOrReplaceChild("CollarB", CubeListBuilder.create().texOffs(17, 26).addBox(-4.5F, -1.5F, 7.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarR = body.addOrReplaceChild("CollarR", CubeListBuilder.create().texOffs(17, 11).addBox(4.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarL = body.addOrReplaceChild("CollarL", CubeListBuilder.create().texOffs(17, 11).addBox(-5.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition BeltR = body.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = body.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition CloakTL = body.addOrReplaceChild("CloakTL", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak3 = body.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 17.0F, -3.7F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.4466F, 0.0F, 0.0F));

		PartDefinition CloakTR = body.addOrReplaceChild("CloakTR", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak1 = body.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak2 = body.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 14.0F, -1.3F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3069F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ChestOrnament = body.addOrReplaceChild("ChestOrnament", CubeListBuilder.create().texOffs(76, 53).addBox(-2.5F, 3.0F, -4.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ChestClothL = body.addOrReplaceChild("ChestClothL", CubeListBuilder.create().texOffs(20, 47).mirror().addBox(-4.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663F, 0.0F, 0.0F));

		PartDefinition ChestClothR = body.addOrReplaceChild("ChestClothR", CubeListBuilder.create().texOffs(20, 47).addBox(1.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663F, 0.0F, 0.0F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LegClothR = body.addOrReplaceChild("LegClothR", CubeListBuilder.create().texOffs(20, 55).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 10.4F, -3.9F, -0.0349F, 0.0F, 0.0F));

		PartDefinition LegClothL = body.addOrReplaceChild("LegClothL", CubeListBuilder.create().texOffs(20, 55).mirror().addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 10.4F, -3.9F, -0.0349F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition GauntletR = right_arm.addOrReplaceChild("GauntletR", CubeListBuilder.create().texOffs(100, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR1 = right_arm.addOrReplaceChild("GauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR2 = right_arm.addOrReplaceChild("GauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletR2 = right_arm.addOrReplaceChild("GauntletR2", CubeListBuilder.create().texOffs(102, 37).addBox(4.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1676F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR1 = right_arm.addOrReplaceChild("ShoulderR1", CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR2 = right_arm.addOrReplaceChild("ShoulderR2", CubeListBuilder.create().texOffs(0, 19).addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR5 = right_arm.addOrReplaceChild("ShoulderR5", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR3 = right_arm.addOrReplaceChild("ShoulderR3", CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR4 = right_arm.addOrReplaceChild("ShoulderR4", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition GauntletL = left_arm.addOrReplaceChild("GauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL1 = left_arm.addOrReplaceChild("GauntletstrapL1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL2 = left_arm.addOrReplaceChild("GauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletL2 = left_arm.addOrReplaceChild("GauntletL2", CubeListBuilder.create().texOffs(102, 37).addBox(-5.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1676F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL1 = left_arm.addOrReplaceChild("ShoulderL1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL2 = left_arm.addOrReplaceChild("ShoulderL2", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL3 = left_arm.addOrReplaceChild("ShoulderL3", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL5 = left_arm.addOrReplaceChild("ShoulderL5", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL4 = left_arm.addOrReplaceChild("ShoulderL4", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelR1 = right_leg.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

		PartDefinition BackpanelR2 = right_leg.addOrReplaceChild("BackpanelR2", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition BackpanelR3 = right_leg.addOrReplaceChild("BackpanelR3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition BackpanelR4 = right_leg.addOrReplaceChild("BackpanelR4", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-2.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelL1 = left_leg.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

		PartDefinition BackpanelL4 = left_leg.addOrReplaceChild("BackpanelL4", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

		PartDefinition BackpanelL2 = left_leg.addOrReplaceChild("BackpanelL2", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition BackpanelL3 = left_leg.addOrReplaceChild("BackpanelL3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

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