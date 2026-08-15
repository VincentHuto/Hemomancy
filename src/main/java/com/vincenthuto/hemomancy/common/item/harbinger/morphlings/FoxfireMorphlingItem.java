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
 * Foxfire strain that hides a still host in dim, wet, or drowning light while
 * preserving sprinting Sepia Wake and the shared Last Rite mantles.
 */
public class FoxfireMorphlingItem extends MorphlingItem {

	private static final String CAMOUFLAGE_STILL_SINCE_KEY = "CamouflageStillSince";
	private static final int INK_MANTLE_REPRIEVE_COOLDOWN = 12000;

	public FoxfireMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.foxfire.binomial";
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
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		triggerPrimalLastLightMantle(playerIn, itemStack);
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		if (!player.hasEffect(EffectInit.luminous_dissipation)) {
			player.addEffect(new MobEffectInstance(EffectInit.luminous_dissipation,
					100, amplifier, false, false, true));
		}

		runCamouflage(player, stack, maturity);

		if (maturity >= 2 && !player.level().isClientSide && player.isSprinting()) {
			if (player.tickCount % 20 == 0) {
				double radius = 4.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
				int blindDuration = 30 + (maturity - 2) * 10;
				for (Monster mob : hostiles) {
					mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
							blindDuration, 0, true, true, true));
				}
			}
		}
	}

	private void runCamouflage(Player player, ItemStack stack, int maturity) {
		if (player.level().isClientSide) {
			return;
		}
		int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
		boolean eligible = FoxfireCamouflageRules.isEligible(maturity, player.isInWaterOrBubble(), lightLevel);
		boolean still = FoxfireCamouflageRules.isStillEnough(player.getDeltaMovement().lengthSqr())
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
		if (FoxfireCamouflageRules.shouldCamouflage(now, tag.getLong(CAMOUFLAGE_STILL_SINCE_KEY))) {
			player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
					60, 0, true, false, true));
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		if (MorphlingItem.isPrimal(stack) && player.getHealth() - amount <= 0) {
			if (triggerPrimalLastLightMantle(player, stack)) {
				return;
			}
		}

		if (maturity >= 4 && player.getHealth() - amount <= 0
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

	private boolean triggerPrimalLastLightMantle(Player player, ItemStack stack) {
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
		AABB area = player.getBoundingBox().inflate(8.0);
		for (Monster mob : player.level().getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
					160, 0, true, true, true));
			mob.addEffect(new MobEffectInstance(MobEffects.CONFUSION,
					160, 0, true, true, true));
		}
		return true;
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Sepia Wake (Blind hostiles while sprinting)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Low-Light Camouflage (Stillness hides you in dark or water)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ink Mantle Reprieve (Prevent death using blood)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Last-Light Mantle (Primal rescue mantle cleanses and prevents death)", 5, currentMaturity));
		return list;
	}
}
