package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.event.LastRiteHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Lumenlace strain that hides a still host in dim, wet, or drowning light while
 * preserving sprinting Sepia Wake and the shared Last Rite mantles.
 */
public class LumenlaceMorphlingItem extends MorphlingItem {

	private static final String CAMOUFLAGE_STILL_SINCE_KEY = "CamouflageStillSince";
	private static final int INK_MANTLE_REPRIEVE_COOLDOWN = 12000;

	public LumenlaceMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.lumenlace.binomial";
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.LUX;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.DUCTILIS;
	}

	@Override
	public boolean tryUse(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
        if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(playerIn)) return false;
        if (playerIn instanceof net.minecraft.server.level.ServerPlayer server
                && com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.isMarionetteChannel(server))
            com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(server, false);
        try (var schoolAbility = MorphlingCombat.scope(this, playerIn, itemStack, null)) {

		return triggerPrimalLastLightMantle(playerIn, itemStack, true);

        }
    }

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {

		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		MorphlingItem.applyPassiveEffect(player, stack, EffectInit.morphling_lumenlace,
				EffectInit.luminous_dissipation, amplifier);

		runCamouflage(player, stack, maturity);

		if (maturity >= 2 && !player.level().isClientSide && player.isSprinting()) {
			if (player.tickCount % 20 == 0) {
				double radius = 4.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
				int exposureDuration = 30 + (maturity - 2) * 10;
				for (Monster mob : hostiles) {
					MorphlingCombat.afflict(this, player, mob, exposureDuration);
				}
			}
		}

        }
    }

	private void runCamouflage(Player player, ItemStack stack, int maturity) {
		if (player.level().isClientSide) {
			return;
		}
		int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
		boolean eligible = LumenlaceCamouflageRules.isEligible(maturity, player.isInWaterOrBubble(), lightLevel);
		boolean still = LumenlaceCamouflageRules.isStillEnough(player.getDeltaMovement().lengthSqr())
				&& !player.isSprinting();
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		long now = player.level().getGameTime();
		if (!eligible || !still) {
			tag.remove(CAMOUFLAGE_STILL_SINCE_KEY);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			return;
		}
		if (!tag.contains(CAMOUFLAGE_STILL_SINCE_KEY)) {
			tag.putLong(CAMOUFLAGE_STILL_SINCE_KEY, now);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			return;
		}
		if (LumenlaceCamouflageRules.shouldCamouflage(now, tag.getLong(CAMOUFLAGE_STILL_SINCE_KEY))) {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
					60, 0, true, false, true));
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
        onEquippedHurt(player, stack, source, amount, true);
    }

    @Override
    public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount, boolean allowReactions) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, source)) {

		int maturity = MorphlingItem.getMaturityLevel(stack);

		if (MorphlingItem.isPrimal(stack) && player.getHealth() <= 0) {
			if (triggerPrimalLastLightMantle(player, stack, allowReactions)) {
				return;
			}
		}

		if (maturity >= 4 && player.getHealth() <= 0
				&& LastRiteHelper.canFire(player, LastRiteHelper.INK_MANTLE_ID)) {
			long lastReprieve = getLastAbilityTick(stack, "InkMantleReprieve");
			long now = player.level().getGameTime();
			if (now - lastReprieve >= INK_MANTLE_REPRIEVE_COOLDOWN) {
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
					if (!volume.isActive()) return;
					double bloodCost = 500.0;
					if (volume.getBloodVolume() > bloodCost) {
						volume.drain(bloodCost);
						player.setHealth(8.0f);
						player.invulnerableTime = 40;
						if (player instanceof ServerPlayer serverPlayer) {
							BloodVolumeEvents.syncVolume(serverPlayer, volume);
						}
						setLastAbilityTick(stack, "InkMantleReprieve", now);
						LastRiteHelper.consume(player, LastRiteHelper.INK_MANTLE_ID);
					}
				});
			}
		}

        }
    }

	private boolean triggerPrimalLastLightMantle(Player player, ItemStack stack, boolean allowReactions) {
		if (!LastRiteHelper.canFire(player, LastRiteHelper.LAST_LIGHT_ID)) return false;
		if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "LastLightMantle",
				750.0, 12000, 700, 1)) return false;
		LastRiteHelper.consume(player, LastRiteHelper.LAST_LIGHT_ID);
		player.removeAllEffects();
		player.setHealth(Math.max(player.getHealth(), 12.0f));
		player.invulnerableTime = 80;
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
				160, 1, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
				240, 2, true, false, true));
		if (!allowReactions) return true;
		AABB area = player.getBoundingBox().inflate(8.0);
		for (Monster mob : player.level().getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			MorphlingCombat.afflict(this, player, mob, 160);
		}
		return true;
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Sepia Wake (Illuminate hostiles while sprinting)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Low-Light Camouflage (Stillness hides you in dark or water)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ink Mantle Reprieve (Prevent death using blood)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Last-Light Mantle (Primal rescue mantle cleanses and prevents death)", 5, currentMaturity));
		return list;
	}
}
