package com.vincenthuto.hemomancy.client.model.morphling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingAnimContext;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModel;
import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainModels;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.PrimalMorphlingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.Set;

/**
 * One anchor of a house strain, drawn from its generated model ({@link MorphlingStrainModels.Anchor}). Below Primal a
 * worn helmet wins over a head growth; at Primal a head replacement (Ink Cap's hood, Kinbud's polyp face) takes the
 * head and the helmet still renders over it.
 */
public final class MorphlingStrainAttachment extends MorphlingModelAttachment {
	private final MorphlingStrainModels.Anchor anchor;
	private MorphlingStrainModel model;

	public MorphlingStrainAttachment(MorphlingStrainModels.Anchor anchor) {
		this.anchor = anchor;
	}

	public MorphlingStrainModels.Anchor anchor() {
		return anchor;
	}

	public AttachmentPoint point() {
		return anchor.point();
	}

	private MorphlingStrainModel model() {
		if (model == null) {
			model = anchor.factory().apply(Minecraft.getInstance().getEntityModels().bakeLayer(anchor.layer()));
		}
		return model;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity,
			HumanoidModel<?> parentModel, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch, float alpha) {
		render(poseStack, buffer, packedLight, entity, parentModel, limbSwing, limbSwingAmount, partialTicks,
				ageInTicks, netHeadYaw, headPitch, alpha, PrimalMorphlingRules.APEX_LEVEL);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity,
			HumanoidModel<?> parentModel, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
			float netHeadYaw, float headPitch, float alpha, int maturity) {
		if (maturity < anchor.visibleFrom()) return;
		if (helmetCovers(entity, maturity)) return;
		int flags = entity instanceof Player player ? HemoCapabilityAccess.getEquippedMorphling(player).getActiveFlags() : 0;
		MorphlingAnimContext ctx = MorphlingAnimContext.of(entity, maturity, flags, ageInTicks);
		MorphlingStrainModel strainModel = model();
		strainModel.pose(ctx);
		float scale = maturity >= PrimalMorphlingRules.PRIMAL_LEVEL ? anchor.primalScale() : 1.0F;
		anchor.point().renderOnParts(poseStack, parentModel, () -> {
			poseStack.scale(scale, scale, scale);
			strainModel.draw(poseStack, buffer, packedLight, ctx);
		});
	}

	/** Below Primal a helmet covers head growths; a Primal head replacement draws under the helmet. */
	private boolean helmetCovers(LivingEntity entity, int maturity) {
		return anchor.point() == AttachmentPoint.HEAD && maturity < PrimalMorphlingRules.PRIMAL_LEVEL
				&& !entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
	}

	@Override
	public Set<AttachmentPoint> hiddenPlayerParts(int maturity) {
		return hiddenPlayerParts(maturity, 0);
	}

	@Override
	public Set<AttachmentPoint> hiddenPlayerParts(int maturity, int flags) {
		Set<AttachmentPoint> hidden = EnumSet.noneOf(AttachmentPoint.class);
		if (anchor.hideAt() >= 0 && maturity >= anchor.hideAt()) hidden.add(anchor.point());
		if (anchor.hideHeadFlags() != 0 && maturity >= PrimalMorphlingRules.PRIMAL_LEVEL
				&& (flags & anchor.hideHeadFlags()) != 0) {
			hidden.add(AttachmentPoint.HEAD);
		}
		return hidden;
	}

	/** Whether a hidden head part should keep the helmet: a Primal replacement wears it, a sealed bell does not. */
	public boolean keepsHelmet(int maturity, int flags) {
		return anchor.point() == AttachmentPoint.HEAD && anchor.hideAt() >= 0 && maturity >= anchor.hideAt();
	}
}
