package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BorrowedBloodReserve;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Deadman's Purse strain that turns combat feeding into borrowed-blood banking
 * instead of direct hit healing. Its mature lane still spends blood for an
 * emergency transfusion, and its primal covenant shares the feed bank with allies.
 */
public class DeadmansPurseMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Blood Transfusion triggers (10 seconds). */
	private static final int TRANSFUSION_COOLDOWN = 200;
	private static final int HEMOPHAGE_COVENANT_DURATION = 400;

	public DeadmansPurseMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.deadmans_purse.binomial";
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "HemophageCovenant",
				450.0, 1200, 300, 0)) return;
		CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putLong("HemophageCovenantUntil", worldIn.getGameTime() + HEMOPHAGE_COVENANT_DURATION);
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		playerIn.displayClientMessage(Component.literal("Hemophage Covenant opens.")
				.withStyle(net.minecraft.ChatFormatting.DARK_RED), true);
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		// Base effect: Sanguine Siphon (blood fill, amplifier = maturity)
		if (!player.hasEffect(EffectInit.sanguine_siphon)) {
			player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon,
					100, amplifier, false, false, true));
		}

		// Mature (3+): Blood Transfusion — auto-heal by spending blood volume when low
		if (maturity >= 3 && !player.level().isClientSide) {
			if (player.getHealth() <= player.getMaxHealth() * 0.3f && player.getHealth() > 0) {
				long lastTransfusion = getLastAbilityTick(stack, "BloodTransfusion");
				long now = player.level().getGameTime();
				if (now - lastTransfusion >= TRANSFUSION_COOLDOWN) {
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (!volume.isActive()) return;
						double bloodCost = 200.0; // Spend blood to heal
						if (volume.getBloodVolume() > bloodCost) {
							volume.drain(bloodCost);
							float transfusionHealing = 6.0f + (maturity - 3) * 3.0f;
							player.heal(transfusionHealing);
							BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
							setLastAbilityTick(stack, "BloodTransfusion", now);
						}
					});
				}
			}
		}
	}

	@Override
	public void onEquippedAttack(Player player, ItemStack stack, LivingEntity target, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		if (maturity >= 2 && !player.level().isClientSide) {
			double accepted = BorrowedBloodReserve.deposit(player,
					DeadmansPurseFeedBankRules.attackDeposit(maturity, amount));
			if (accepted > 0.0D) {
				MorphlingItem.addHusbandryProgress(stack, 1);
			}
		}

		if (MorphlingItem.isPrimal(stack) && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag.getLong("HemophageCovenantUntil") > player.level().getGameTime()) {
				float sharedHealing = Math.min(2.5f, amount * 0.18f);
				AABB area = player.getBoundingBox().inflate(12.0);
				for (Player ally : player.level().getEntitiesOfClass(Player.class, area, Player::isAlive)) {
					if (ally.getHealth() < ally.getMaxHealth()) {
						ally.heal(sharedHealing);
					}
				}
				BorrowedBloodReserve.deposit(player, Math.min(35.0D, amount * 8.0D));
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
					if (!volume.isActive()) return;
					volume.fill(Math.min(35.0, amount * 8.0));
					if (player instanceof ServerPlayer serverPlayer) {
						BloodVolumeEvents.syncVolume(serverPlayer, volume);
					}
				});
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		if (player.level().isClientSide) {
			return;
		}
		double accepted = BorrowedBloodReserve.deposit(player,
				DeadmansPurseFeedBankRules.killDeposit(maturity, victim.getMaxHealth()));
		if (accepted > 0.0D) {
			MorphlingItem.addHusbandryProgress(stack, 2);
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Feed Banking (Store strike blood as borrowed reserve)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Blood Transfusion (Emergency heal using blood)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Overkill Banking (Kills store corpse blood for later casts)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Hemophage Covenant (Staff active shares damage into blood and healing)", 5, currentMaturity));
		return list;
	}

}
