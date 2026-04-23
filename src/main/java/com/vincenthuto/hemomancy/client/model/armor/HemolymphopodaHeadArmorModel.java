package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
 * Empty HumanoidModel used to suppress vanilla armor rendering for the
 * Hemolymphopoda Headpiece. The actual helmet rendering is done by
 * {@link com.vincenthuto.hemomancy.client.render.layer.player.HemolymphopodaHeadpieceLayer}.
 */
public class HemolymphopodaHeadArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("hemolymphopoda_headpiece"), "main");

	public static final Lazy<HemolymphopodaHeadArmorModel<LivingEntity>> HELMET = Lazy.of(
			() -> new HemolymphopodaHeadArmorModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION)));

	public HemolymphopodaHeadArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
	}

	public static LayerDefinition createHeadLayer() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		// Replace head with an empty part so the vanilla armor layer renders nothing
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		// Intentionally empty – HemolymphopodaHeadpieceLayer handles all rendering.
	}
}
