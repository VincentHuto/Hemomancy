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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Pests morphling that damages nearby hostile mobs by applying the
 * Verminous Aura effect while equipped. Maturity level scales the damage
 * radius and damage dealt. Prefers FLAMMEUS (fervent heat drives the swarm)
 * with TENEBRIS as secondary (darkness harbors vermin).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Swarm Retaliation — when damaged, spawn tracking pest
 *   projectiles that hunt the attacker
 * - Mature (3): Infest — kills cause pest swarm to erupt from the corpse,
 *   automatically targeting nearby hostiles for chain-kill potential
 * - Apex (4): Plague Burst — when health drops below 25%, emit a massive
 *   AoE burst that withers all nearby hostiles
 */
public class PestsMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Swarm Retaliation triggers (1 second). */
	private static final int SWARM_RETALIATION_COOLDOWN = 20;
	/** Cooldown in ticks between Plague Burst triggers (30 seconds). */
	private static final int PLAGUE_BURST_COOLDOWN = 600;
	private static final int MAX_PRIMAL_SWARM = 8;

	public PestsMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.FLAMMEUS;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.TENEBRIS;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		int stored = tag.getInt("VerminCrownSwarm");
		if (stored <= 0) {
			playerIn.displayClientMessage(Component.literal("The Vermin Crown is quiet."), true);
			return;
		}
		if (!MorphlingItem.tryBeginPrimalAbility(playerIn, itemStack, "VerminCrown",
				300.0, 500, 180, 0)) return;
		AABB area = playerIn.getBoundingBox().inflate(18.0);
		List<Monster> hostiles = worldIn.getEntitiesOfClass(Monster.class, area, Monster::isAlive);
		int releases = Math.min(stored, MAX_PRIMAL_SWARM);
		for (int i = 0; i < releases; i++) {
			var pest = new com.vincenthuto.hemomancy.common.entity.projectile.TrackingPestsEntity(
					playerIn, false);
			if (!hostiles.isEmpty()) {
				pest.setTarget(hostiles.get(i % hostiles.size()));
			}
			worldIn.addFreshEntity(pest);
		}
		tag.putInt("VerminCrownSwarm", Math.max(0, stored - releases));
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Verminous Aura (AoE damage, amplifier = maturity)
		if (!player.hasEffect(EffectInit.verminous_aura)) {
			player.addEffect(new MobEffectInstance(EffectInit.verminous_aura,
					100, maturity, false, true, true));
		}

		// Apex (4): Plague Burst — emergency AoE Wither when health is critically low
		if (maturity >= 4 && !player.level().isClientSide) {
			if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
				long lastBurst = getLastAbilityTick(stack, "PlagueBurst");
				long now = player.level().getGameTime();
				if (now - lastBurst >= PLAGUE_BURST_COOLDOWN) {
					setLastAbilityTick(stack, "PlagueBurst", now);

					double radius = 8.0;
					AABB area = player.getBoundingBox().inflate(radius);
					List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);
					for (Monster mob : hostiles) {
						mob.addEffect(new MobEffectInstance(MobEffects.WITHER,
								100, 1, true, true, true));
						mob.hurt(player.damageSources().magic(), 6.0F);
					}
				}
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Swarm Retaliation — spawn tracking pests at attacker
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			long lastSwarm = getLastAbilityTick(stack, "SwarmRetaliation");
			long now = player.level().getGameTime();
			if (now - lastSwarm >= SWARM_RETALIATION_COOLDOWN) {
				setLastAbilityTick(stack, "SwarmRetaliation", now);

				int pestCount = 1 + (maturity - 2); // 1 at Developing, 2 at Mature, 3 at Apex
				for (int i = 0; i < pestCount; i++) {
					var pest = new com.vincenthuto.hemomancy.common.entity.projectile.TrackingPestsEntity(
							player, false);
					pest.setTarget(attacker);
					player.level().addFreshEntity(pest);
				}
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Mature (3+): Infest — kills cause pest swarm to erupt from corpse,
		// automatically targeting nearby hostiles for chain-kill potential
		if (maturity >= 3 && !player.level().isClientSide) {
			int pestCount = 2 + (maturity - 3); // 2 at Mature, 3 at Apex
			double searchRadius = 12.0;
			AABB area = victim.getBoundingBox().inflate(searchRadius);
			List<Monster> nearbyHostiles = player.level().getEntitiesOfClass(Monster.class, area,
					m -> m != victim && m.isAlive());

			if (!nearbyHostiles.isEmpty()) {
				for (int i = 0; i < Math.min(pestCount, nearbyHostiles.size()); i++) {
					var pest = new com.vincenthuto.hemomancy.common.entity.projectile.TrackingPestsEntity(
							player, false);
					// Spawn pests from the victim's corpse location
					pest.setPos(victim.getX(), victim.getY() + 0.5, victim.getZ());
					pest.setTarget(nearbyHostiles.get(i % nearbyHostiles.size()));
					player.level().addFreshEntity(pest);
				}
			}
		}

		if (MorphlingItem.isPrimal(stack) && !player.level().isClientSide) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			tag.putInt("VerminCrownSwarm", Math.min(MAX_PRIMAL_SWARM,
					tag.getInt("VerminCrownSwarm") + (victim.getMaxHealth() >= 20.0f ? 2 : 1)));
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Swarm Retaliation (Pests hunt your attacker)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Infest (Kills spawn pests targeting nearby foes)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Plague Burst (AoE Wither at low health)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Vermin Crown (Kills store swarms; staff releases hunters)", 5, currentMaturity));
		return list;
	}

}
