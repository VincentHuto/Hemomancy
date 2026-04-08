package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Tick morphling that emanates hemorrhagic venom, periodically damaging
 * nearby hostile mobs. Unlike the leeches (which focus on blood volume
 * sustain and life steal), the tick is a disease vector — it inflicts
 * persistent area damage and debuffs on enemies rather than directly
 * healing the player. Prefers MORTEM (death/decay feeds the parasite's
 * venom) with TENEBRIS as secondary (lurking darkness harbors ticks).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Engorge — on kill, gain temporary armor bonus as the
 *   tick feeds and swells (stacking, short duration)
 * - Mature (3): Blood Fever — gain attack speed when there are bleeding/
 *   damaged hostiles nearby (the tick grows frenzied near wounded prey)
 * - Apex (4): Pandemic Burst — when taking heavy damage (>6), release a
 *   disease cloud that applies Wither + Weakness to all nearby hostiles
 */
public class TickMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Pandemic Burst triggers (25 seconds). */
	private static final int PANDEMIC_BURST_COOLDOWN = 500;

	public TickMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.MORTEM;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Hemorrhagic Venom (AoE damage to nearby hostiles)
		if (!player.hasEffect(EffectInit.hemorrhagic_venom.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.hemorrhagic_venom.get(),
					100, maturity, false, true, true));
		}

		// Mature (3+): Blood Fever — attack speed near wounded hostiles
		if (maturity >= 3 && !player.level().isClientSide) {
			if (player.tickCount % 20 == 0) { // Check every second
				double radius = 8.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> nearbyHostiles = player.level().getEntitiesOfClass(Monster.class, area);

				// Count hostiles that are at less than full health (bleeding)
				long bleedingCount = nearbyHostiles.stream()
						.filter(m -> m.getHealth() < m.getMaxHealth())
						.count();

				if (bleedingCount > 0 && !player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
					int speedAmp = (int) Math.min(bleedingCount - 1, 2); // Speed I-III based on count
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
							30, speedAmp, true, false, true));
				}
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Engorge — temporary armor bonus on kill
		if (maturity >= 2 && !player.level().isClientSide) {
			int armorDuration = 100 + (maturity - 2) * 60; // 5s at Developing, 8s Mature, 11s Apex
			int armorAmplifier = Math.min(maturity - 2, 2); // Resistance I-III
			// Use Resistance to simulate armor gain from feeding
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
					armorDuration, armorAmplifier, true, true, true));
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Pandemic Burst — disease cloud on heavy damage
		if (maturity >= 4 && amount >= 6.0f) {
			long lastBurst = getLastAbilityTick(stack, "PandemicBurst");
			long now = player.level().getGameTime();
			if (now - lastBurst >= PANDEMIC_BURST_COOLDOWN) {
				setLastAbilityTick(stack, "PandemicBurst", now);

				double radius = 8.0;
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
				for (Monster mob : hostiles) {
					mob.addEffect(new MobEffectInstance(MobEffects.WITHER,
							100, 1, true, true, true)); // Wither II for 5s
					mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
							100, 0, true, true, true)); // Weakness I for 5s
				}
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Engorge (Resistance on kill from feeding)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Blood Fever (Speed near wounded hostiles)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Pandemic Burst (AoE Wither + Weakness on heavy hit)", 4, currentMaturity));
		return list;
	}

}
