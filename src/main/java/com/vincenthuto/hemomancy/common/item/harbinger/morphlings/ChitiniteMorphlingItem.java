package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Chitinite morphling that grants damage resistance by applying the
 * Chitinous Bulwark effect while equipped. Maturity level scales the armor
 * toughness bonus (capped at amplifier 2 to prevent extreme stacking).
 * Prefers FERRIC (iron/metal strengthens the carapace) with
 * CONGEATIO as secondary (cold hardens chitin).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Carapace Thorns — reflect a percentage of melee damage
 *   back at attackers (chitin spines lacerate on contact)
 * - Mature (3): Ablative Plating — periodically grow Absorption hearts
 *   (golden hearts) that act as a regenerating damage buffer, absorbing
 *   hits before they reach real health (layered chitin regrowth)
 * - Apex (4): Ironhide — after taking a hit greater than 6 damage, gain brief
 *   invulnerability AND emit a retaliatory thorn burst that damages all
 *   nearby hostiles (hardened carapace shatters outward on impact)
 */
public class ChitiniteMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Ironhide triggers (3 seconds). */
	private static final int IRONHIDE_COOLDOWN = 60;
	/** Interval in ticks between Ablative Plating grants (8 seconds). */
	private static final int ABLATIVE_PLATING_INTERVAL = 160;
	private static final int PRIMAL_CARAPACE_DURATION = 120;

	public ChitiniteMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FERRIC;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.CONGEATIO;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "PrimalCarapace",
				500.0, 900, 260, 0)) return;
		CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putLong("PrimalCarapaceUntil", worldIn.getGameTime() + PRIMAL_CARAPACE_DURATION);
		tag.putFloat("PrimalCarapaceStored", 0.0f);
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		playerIn.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
				PRIMAL_CARAPACE_DURATION, 2, true, true, true));
		playerIn.setAbsorptionAmount(Math.min(playerIn.getAbsorptionAmount() + 8.0f, 16.0f));
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Chitinous Bulwark (armor toughness, amplifier capped at 2)
		int amplifier = Math.min(maturity, 2);
		if (!player.hasEffect(EffectInit.chitinous_bulwark)) {
			player.addEffect(new MobEffectInstance(EffectInit.chitinous_bulwark,
					100, maturity, false, true, true));
		}

		// Mature (3+): Ablative Plating — periodically grant Absorption hearts
		if (maturity >= 3 && !player.level().isClientSide) {
			long now = player.level().getGameTime();
			long lastPlating = getLastAbilityTick(stack, "AblativePlating");
			if (now - lastPlating >= ABLATIVE_PLATING_INTERVAL) {
				// Only grant if current absorption is low (don't over-stack)
				if (player.getAbsorptionAmount() < 4.0f) {
					setLastAbilityTick(stack, "AblativePlating", now);
					float absorptionAmount = 4.0f + (maturity - 3) * 2.0f; // 4 at Mature, 6 at Apex
					player.setAbsorptionAmount(
							Math.min(player.getAbsorptionAmount() + absorptionAmount, 8.0f));
				}
			}
		}

		if (MorphlingItem.isPrimal(stack) && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			float stored = tag.getFloat("PrimalCarapaceStored");
			long until = tag.getLong("PrimalCarapaceUntil");
			if (stored > 0 && until > 0 && player.level().getGameTime() > until) {
				double radius = 5.5;
				AABB area = player.getBoundingBox().inflate(radius);
				float burstDamage = Math.min(18.0f, 4.0f + stored * 0.65f);
				for (net.minecraft.world.entity.monster.Monster mob :
						player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, area)) {
					mob.hurt(player.damageSources().thorns(player), burstDamage);
					double dx = mob.getX() - player.getX();
					double dz = mob.getZ() - player.getZ();
					double dist = Math.sqrt(dx * dx + dz * dz);
					if (dist > 0) {
						mob.push(dx / dist * 0.9, 0.35, dz / dist * 0.9);
					}
				}
				tag.remove("PrimalCarapaceUntil");
				tag.remove("PrimalCarapaceStored");
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Carapace Thorns — reflect damage to melee attackers
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			float thornsPct = 0.20f + (maturity - 2) * 0.10f; // 20% at Developing, 30% Mature, 40% Apex
			float thornsDamage = amount * thornsPct;
			if (thornsDamage > 0.5f) {
				attacker.hurt(player.damageSources().thorns(player), thornsDamage);
			}
		}

		// Apex (4): Ironhide — invulnerability + retaliatory thorn burst
		if (maturity >= 4 && amount >= 6.0f) {
			long lastIronhide = getLastAbilityTick(stack, "Ironhide");
			long now = player.level().getGameTime();
			if (now - lastIronhide >= IRONHIDE_COOLDOWN) {
				setLastAbilityTick(stack, "Ironhide", now);
				// Brief invulnerability
				player.invulnerableTime = 30;

				// Retaliatory thorn burst: damage all nearby hostiles
				if (!player.level().isClientSide) {
					double radius = 4.0;
					AABB area = player.getBoundingBox().inflate(radius);
					List<net.minecraft.world.entity.monster.Monster> hostiles =
							player.level().getEntitiesOfClass(
									net.minecraft.world.entity.monster.Monster.class, area);
					float burstDamage = amount * 0.5f; // 50% of triggering hit
					for (net.minecraft.world.entity.monster.Monster mob : hostiles) {
						mob.hurt(player.damageSources().thorns(player), burstDamage);
						// Knockback from the burst
						double dx = mob.getX() - player.getX();
						double dz = mob.getZ() - player.getZ();
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist > 0) {
							mob.push(dx / dist * 0.5, 0.2, dz / dist * 0.5);
						}
					}
				}
			}
		}

		if (MorphlingItem.isPrimal(stack) && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag.getLong("PrimalCarapaceUntil") > player.level().getGameTime()) {
				tag.putFloat("PrimalCarapaceStored", tag.getFloat("PrimalCarapaceStored") + amount);
				stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Carapace Thorns (Reflect melee damage)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ablative Plating (Regenerating Absorption shield)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Ironhide (Invulnerability + thorn burst on heavy hit)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Primal Carapace (Staff active banks damage into ferric shard burst)", 5, currentMaturity));
		return list;
	}

}
