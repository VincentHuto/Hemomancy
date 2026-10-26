package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperEveningStarAnimState;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheEveningStarModel;
import com.vincenthuto.hemomancy.client.model.item.LivingFlailModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.item.harbinger.LivingFlailRenderHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
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
	private static final float SICKLE_PITCH = 80.0F;
	private static final float ALONG_FOREARM = 90.0F;
	private static final double SICKLE_CHOKE = 0.4D;
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
		// the model's replayed clocks, updated this frame before its layers draw
		VesperEveningStarAnimState state = VesperEveningStarAnimState.of(entity);
		float dissolve = entity.isAwaitingAbsorption()
				? VesperCombatRules.weaponDissolveProgress(state.downedTime()) : 0.0F;
		if (dissolve >= 1.0F) return;
		float morph = entity.isRaging() ? 1.0F : Math.min(1.0F, state.stanceTime() / 30.0F);
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

	/** While a thrown hook is out on its tether, the right hand is empty. */
	private static boolean isSickleHookReleased(VesperTheEveningStarEntity entity, boolean leftHand) {
		if (leftHand) return false;
		VesperWeaponAction action = entity.getWeaponAction();
		int tick = entity.getActionTick();
		return switch (action) {
			case SICKLE_HOOK, SICKLE_HOOK_REEL, SICKLE_ANCHOR_HOOK -> tick >= action.impactTick()
					&& tick <= action.lastImpactTick();
			case SICKLE_HARVEST -> tick >= 14 && tick <= 30;
			default -> false;
		};
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

	/**
	 * The sickle's own hand transform is written for a player's arm frame, so give it that frame at his hand: the
	 * grip then sits in the fist with the handle across the palm and the curve reaching forward, as a player holds it.
	 */
	private static void applySickleGrip(PoseStack poseStack, boolean leftHand) {
		// The left anchor sits up the sleeve for the claws; a sickle belongs in the fist, mirroring the right hand.
		if (leftHand) poseStack.translate(-0.05D, 0.36D, -0.37D);
		// Tip the handle forward off the forearm so the blade hooks down ahead of him in a reaping guard, and hold
		// it by the butt rather than just under the blade.
		poseStack.mulPose(Axis.XP.rotationDegrees(SICKLE_PITCH));
		poseStack.translate(0.0D, 0.0D, -SICKLE_CHOKE);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
	}

	private static void applyWeaponGrip(PoseStack poseStack, EnumBloodTendency tendency, boolean leftHand) {
		poseStack.mulPose(Axis.XP.rotationDegrees(switch (tendency) {
			// The spear and the crossbow lie along the forearm, so a thrust or a shot points where the arm points.
			case DUCTILIS -> -90.0F + ALONG_FOREARM;
			case LUX -> -98.0F + ALONG_FOREARM;
			case TENEBRIS -> leftHand?72.0F:-72.0f;
			case FLAMMEUS -> -98.0F;
			default -> -90.0F;
		}));
		poseStack.mulPose(Axis.YP.rotationDegrees(VesperWeaponGripRules.yawDegrees(tendency, leftHand)));
		if (tendency == EnumBloodTendency.CONGEATIO) poseStack.translate(0.0D, 0.05D, -0.12D);
	}

}
