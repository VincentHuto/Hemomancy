package com.vincenthuto.hemomancy.model.item;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBloodArm<T extends LivingEntity> extends HumanoidModel<T> {
	
	public static final ModelLayerLocation blood_arm = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_arm"), "main");
	
	private final List<ModelPart> parts;
	public ModelPart leftSleeve;
	public ModelPart rightSleeve;
	public ModelPart leftPants;
	public ModelPart rightPants;
	public ModelPart jacket;
	public ModelPart cloak;
	public ModelPart ear;
	public final boolean slim;

	public ModelBloodArm(ModelPart part, boolean slim) {
		super(part, RenderType::entityTranslucent);
		this.slim = slim;
		this.ear = part.getChild("ear");
		this.cloak = part.getChild("cloak");
		this.leftSleeve = part.getChild("left_sleeve");
		this.rightSleeve = part.getChild("right_sleeve");
		this.leftPants = part.getChild("left_pants");
		this.rightPants = part.getChild("right_pants");
		this.jacket = part.getChild("jacket");

		this.parts = part.getAllParts().filter((p_170824_) -> {
			return !p_170824_.isEmpty();
		}).collect(ImmutableList.toImmutableList());
	}

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition grip = mesh.getRoot();
		grip.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F,
				1.0F, CubeDeformation.NONE), PartPose.ZERO);
		grip.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F,
				1.0F, CubeDeformation.NONE, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
 		

		grip.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F,
				12.0F, 4.0F, CubeDeformation.NONE), PartPose.offset(1.9F, 12.0F, 0.0F));
		grip.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F,
				12.0F, 4.0F, CubeDeformation.NONE.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		grip.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F,
				12.0F, 4.0F, CubeDeformation.NONE.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		grip.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F,
				12.0F, 4.0F, CubeDeformation.NONE.extend(0.25F)), PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);

	}

	public static MeshDefinition createMesh(CubeDeformation deform, boolean p_170827_) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deform, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("ear",
				CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, deform),
				PartPose.ZERO);
		partdefinition.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F,
				10.0F, 16.0F, 1.0F, deform, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
		if (p_170827_) {
			partdefinition.addOrReplaceChild("left_arm",
					CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform),
					PartPose.offset(5.0F, 2.5F, 0.0F));
			partdefinition.addOrReplaceChild("right_arm",
					CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform),
					PartPose.offset(-5.0F, 2.5F, 0.0F));
			partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F,
					-2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
			partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F,
					-2.0F, -2.0F, 3.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
		} else {
			partdefinition.addOrReplaceChild("left_arm",
					CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
					PartPose.offset(5.0F, 2.0F, 0.0F));
			partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F,
					-2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
			partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F,
					-2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
		}

		partdefinition.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deform),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F,
				-2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F,
				-2.0F, 4.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F,
				8.0F, 12.0F, 4.0F, deform.extend(0.25F)), PartPose.ZERO);
		return meshdefinition;
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return Iterables.concat(super.bodyParts(),
				ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
	}

	public void renderEars(PoseStack p_103402_, VertexConsumer p_103403_, int p_103404_, int p_103405_) {
		this.ear.copyFrom(this.head);
		this.ear.x = 0.0F;
		this.ear.y = 0.0F;
		this.ear.render(p_103402_, p_103403_, p_103404_, p_103405_);
	}

	public void renderCloak(PoseStack p_103412_, VertexConsumer p_103413_, int p_103414_, int p_103415_) {
		this.cloak.render(p_103412_, p_103413_, p_103414_, p_103415_);
	}

	@Override
	public void setupAnim(T p_103395_, float p_103396_, float p_103397_, float p_103398_, float p_103399_,
			float p_103400_) {
		super.setupAnim(p_103395_, p_103396_, p_103397_, p_103398_, p_103399_, p_103400_);
		this.leftPants.copyFrom(this.leftLeg);
		this.rightPants.copyFrom(this.rightLeg);
		this.leftSleeve.copyFrom(this.leftArm);
		this.rightSleeve.copyFrom(this.rightArm);
		this.jacket.copyFrom(this.body);
		if (p_103395_.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
			if (p_103395_.isCrouching()) {
				this.cloak.z = 1.4F;
				this.cloak.y = 1.85F;
			} else {
				this.cloak.z = 0.0F;
				this.cloak.y = 0.0F;
			}
		} else if (p_103395_.isCrouching()) {
			this.cloak.z = 0.3F;
			this.cloak.y = 0.8F;
		} else {
			this.cloak.z = -1.1F;
			this.cloak.y = -0.85F;
		}

	}

	@Override
	public void setAllVisible(boolean p_103419_) {
		super.setAllVisible(p_103419_);
		this.leftSleeve.visible = p_103419_;
		this.rightSleeve.visible = p_103419_;
		this.leftPants.visible = p_103419_;
		this.rightPants.visible = p_103419_;
		this.jacket.visible = p_103419_;
		this.cloak.visible = p_103419_;
		this.ear.visible = p_103419_;
	}

	@Override
	public void translateToHand(HumanoidArm p_103392_, PoseStack p_103393_) {
		ModelPart modelpart = this.getArm(p_103392_);
		if (this.slim) {
			float f = 0.5F * (p_103392_ == HumanoidArm.RIGHT ? 1 : -1);
			modelpart.x += f;
			modelpart.translateAndRotate(p_103393_);
			modelpart.x -= f;
		} else {
			modelpart.translateAndRotate(p_103393_);
		}

	}

	public ModelPart getRandomModelPart(Random p_103407_) {
		return this.parts.get(p_103407_.nextInt(this.parts.size()));
	}
}