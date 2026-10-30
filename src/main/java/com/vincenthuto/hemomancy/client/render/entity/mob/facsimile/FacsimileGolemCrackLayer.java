package com.vincenthuto.hemomancy.client.render.entity.mob.facsimile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

/** The facsimile golem's damage: its hematic plates split along crimson fissures, deeper as it weakens. */
public class FacsimileGolemCrackLayer extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
	private static final ResourceLocation LOW = crack("low");
	private static final ResourceLocation MEDIUM = crack("medium");
	private static final ResourceLocation HIGH = crack("high");

	public FacsimileGolemCrackLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> parent) {
		super(parent);
	}

	private static ResourceLocation crack(String level) {
		return Hemomancy.rloc("textures/entity/facsimile/facsimile_golem_crackiness_" + level + ".png");
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, IronGolem golem, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (golem.isInvisible()) return;
		ResourceLocation texture = switch (golem.getCrackiness()) {
			case LOW -> LOW;
			case MEDIUM -> MEDIUM;
			case HIGH -> HIGH;
			case NONE -> null;
		};
		if (texture != null) {
			renderColoredCutoutModel(getParentModel(), texture, poseStack, buffer, packedLight, golem, -1);
		}
	}
}
