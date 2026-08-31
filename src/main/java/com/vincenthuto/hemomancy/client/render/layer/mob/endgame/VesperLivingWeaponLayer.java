package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.item.hematic.LivingFlailRenderHelper;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class VesperLivingWeaponLayer
		extends RenderLayer<VesperTheEveningStarEntity, VesperTheEveningStarModel> {
	private final LivingFlailModel<VesperTheEveningStarEntity> flailModel;

	public VesperLivingWeaponLayer(RenderLayerParent<VesperTheEveningStarEntity, VesperTheEveningStarModel> parent) {
		super(parent);
		this.flailModel = new LivingFlailModel<>(Minecraft.getInstance().getEntityModels()
				.bakeLayer(LivingFlailModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		ItemStack weapon = entity.getLivingWeaponStack();
		if (weapon.isEmpty() || entity.isInvisible() || entity.isAwakening()) return;
		float dissolve = entity.isAwaitingAbsorption()
				? VesperCombatRules.weaponDissolveProgress(entity.getDownedTicks() + partialTick) : 0.0F;
		if (dissolve >= 1.0F) return;
		float morph = entity.isRaging() ? 1.0F : Math.min(1.0F, entity.getStanceTick() / 30.0F);
		float scale = 0.9F + morph * 0.65F;
		renderWeapon(poseStack, buffer, packedLight, entity, weapon, scale, false, dissolve);
		if (entity.isRaging() || entity.getActiveTendency() == EnumBloodTendency.TENEBRIS) {
			renderWeapon(poseStack, buffer, packedLight, entity, weapon, scale, true, dissolve);
		}
	}

	private void renderWeapon(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, ItemStack weapon, float scale, boolean leftHand, float dissolve) {
		if (isSickleHookReleased(entity, leftHand)) return;
		poseStack.pushPose();
		if (leftHand) getParentModel().translateToLeftWeapon(poseStack);
		else getParentModel().translateToWeapon(poseStack);
		if (weapon.getItem() instanceof LivingFlailItem) {
			renderFlail(poseStack, buffer, packedLight, entity, weapon, scale, leftHand, dissolve);
			poseStack.popPose();
			return;
		}
		if (entity.isRaging()) applySickleGrip(poseStack, leftHand);
		else applyWeaponGrip(poseStack, entity.getActiveTendency(), leftHand);
		poseStack.scale(scale, scale, scale);
		MultiBufferSource weaponBuffer = dissolve > 0.0F
				? ignored -> buffer.getBuffer(HemoRenderTypes.hermitFarewellDissolve(
						TextureAtlas.LOCATION_BLOCKS, entity.tickCount, dissolve, entity.getId() * 0.173F))
				: buffer;
		Minecraft.getInstance().getItemRenderer().renderStatic(weapon,
				leftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
				packedLight, OverlayTexture.NO_OVERLAY, poseStack, weaponBuffer, entity.level(), entity.getId());
		poseStack.popPose();
	}

	private static boolean isSickleHookReleased(VesperTheEveningStarEntity entity, boolean leftHand) {
		return entity.getWeaponAction() == VesperWeaponAction.SICKLE_HOOK
				&& !leftHand && entity.getActionTick() >= VesperWeaponAction.SICKLE_HOOK.impactTick();
	}

	private void renderFlail(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheEveningStarEntity entity, ItemStack weapon, float scale, boolean leftHand, float dissolve) {
		float side = leftHand ? -1.0F : 1.0F;
		poseStack.translate(side * 0.08F, 0.06F, -0.09F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 8.0F));
		poseStack.scale(scale * 0.9F, scale * 0.9F, scale * 0.9F);
		if (dissolve > 0.0F) {
			LivingFlailRenderHelper.renderHeldDissolving(flailModel, entity,
					leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, weapon, poseStack, buffer,
					packedLight, OverlayTexture.NO_OVERLAY, dissolve, entity.getId() * 0.173F, false);
		} else {
			LivingFlailRenderHelper.renderHeld(flailModel, entity,
					leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, weapon, poseStack, buffer,
					packedLight, OverlayTexture.NO_OVERLAY, false);
		}
	}

	private static void applyWeaponGrip(PoseStack poseStack, EnumBloodTendency tendency, boolean leftHand) {
		poseStack.mulPose(Axis.XP.rotationDegrees(switch (tendency) {
			case DUCTILIS -> -90.0F;
			case TENEBRIS -> leftHand?72.0F:-72.0f;
			case LUX, FLAMMEUS -> -98.0F;
			default -> -90.0F;
		}));
		poseStack.mulPose(Axis.YP.rotationDegrees(VesperWeaponGripRules.yawDegrees(tendency, leftHand)));
		if (tendency == EnumBloodTendency.CONGEATIO) poseStack.translate(0.0D, 0.05D, -0.12D);
	}

	private static void applySickleGrip(PoseStack poseStack, boolean leftHand) {
		float side = leftHand ? -1.0F : 1.0F;
		poseStack.translate(side * 0.045D, 0.04D, -0.10D);
		poseStack.mulPose(Axis.XP.rotationDegrees(-92.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(leftHand ? 0.0F : 180.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(side * 14.0F));
	}
}
