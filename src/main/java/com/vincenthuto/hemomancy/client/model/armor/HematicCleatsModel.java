package com.vincenthuto.hemomancy.client.model.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

/**
 * Fitted Hematic Cleats (CN-10), drawn over whatever boots carry them: per foot a hematic iron sole plate just proud of
 * a boot's outer shell, a sinew band round the boot, two bone spurs off the toe angled at the ground, one off the heel,
 * one splayed off each side of the sole, and two bone studs under it. No shell of its own; the boot's own model shows
 * through. Texture {@code textures/models/armor/hematic_cleats.png}, 64 x 32, from
 * {@code tools/model_export/living_land_sprites.py}.
 */
public class HematicCleatsModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(Hemomancy.rloc("hematic_cleats"), "main");
	public static final Lazy<HematicCleatsModel<LivingEntity>> spurs = Lazy.of(
			() -> new HematicCleatsModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER)));

	public HematicCleatsModel(ModelPart root) {
		super(root, RenderType::armorCutoutNoCull);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
		foot(root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F)));
		foot(root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F)));
		return LayerDefinition.create(mesh, 64, 32);
	}

	/** Boots are drawn inflated by a pixel, so a foot's outer shell runs to about 3 px either side of its centre. */
	private static void foot(PartDefinition leg) {
		leg.addOrReplaceChild("sinew", CubeListBuilder.create().texOffs(24, 0)
				.addBox(-2.0F, 9.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(1.15F, 0.05F, 1.15F)), PartPose.ZERO);
		leg.addOrReplaceChild("sole", CubeListBuilder.create().texOffs(0, 10)
				.addBox(-2.5F, 11.3F, -3.6F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.85F, 0.0F, 0.45F)), PartPose.ZERO);
		// Two toe spurs angled at the ground, one heel spur, a spur splayed off each side of the sole, two studs under it.
		leg.addOrReplaceChild("toe_spur_inner", CubeListBuilder.create().texOffs(0, 18)
				.addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-1.6F, 11.8F, -3.9F, 0.3F, 0.18F, 0.0F));
		leg.addOrReplaceChild("toe_spur_outer", CubeListBuilder.create().texOffs(0, 18)
				.addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(1.6F, 11.8F, -3.9F, 0.3F, -0.18F, 0.0F));
		leg.addOrReplaceChild("heel_spur", CubeListBuilder.create().texOffs(12, 18)
				.addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 10.8F, 2.9F, -0.5F, 0.0F, 0.0F));
		leg.addOrReplaceChild("side_spur_left", CubeListBuilder.create().texOffs(30, 18)
				.addBox(-3.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.2F, 11.4F, -0.5F, 0.0F, 0.0F, -0.4F));
		leg.addOrReplaceChild("side_spur_right", CubeListBuilder.create().texOffs(30, 18)
				.addBox(0.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.2F, 11.4F, -0.5F, 0.0F, 0.0F, 0.4F));
		leg.addOrReplaceChild("studs", CubeListBuilder.create().texOffs(24, 18)
				.addBox(-1.6F, 12.2F, -2.6F, 1.0F, 1.0F, 1.0F)
				.texOffs(24, 18).addBox(0.6F, 12.2F, 0.4F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
	}
}
