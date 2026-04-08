package com.vincenthuto.hemomancy.common.item.morphlings;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

/**
 * Fungal morphling that passively regenerates health by applying the
 * Mycorrhizal Mending effect while equipped. Maturity level scales the
 * healing amplifier. Prefers MORTEM (death/decay nourishes fungi) with
 * ANIMUS as secondary (life force aids regeneration).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Spore Cloud — when damaged, release blinding spores that
 *   inflict Blindness on the attacker
 * - Mature (3): Mycorrhizal Network — passively heal nearby allied players
 *   (symbiotic fungal healing link)
 * - Apex (4): Decomposer — killed mobs have their loot table rolled again,
 *   granting bonus item drops (fungal breakdown of organic matter)
 */
public class FungalMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Spore Cloud triggers (3 seconds). */
	private static final int SPORE_CLOUD_COOLDOWN = 60;

	public FungalMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.MORTEM;
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Base effect: Mycorrhizal Mending (health regen, amplifier = maturity)
		if (!player.hasEffect(EffectInit.mycorrhizal_mending.get())) {
			player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending.get(),
					100, maturity, false, true, true));
		}

		// Mature (3+): Mycorrhizal Network — heal nearby allied players
		if (maturity >= 3 && !player.level().isClientSide) {
			double radius = 8.0;
			AABB area = player.getBoundingBox().inflate(radius);
			List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, area,
					p -> p != player && p.getHealth() < p.getMaxHealth());
			float healPerAlly = 0.5f + (maturity - 3) * 0.5f; // 0.5 at Mature, 1.0 at Apex
			for (Player ally : nearbyPlayers) {
				ally.heal(healPerAlly);
			}
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Spore Cloud — blind the attacker
		if (maturity >= 2 && source.getEntity() instanceof LivingEntity attacker) {
			long lastSpore = getLastAbilityTick(stack, "SporeCloud");
			long now = player.level().getGameTime();
			if (now - lastSpore >= SPORE_CLOUD_COOLDOWN) {
				setLastAbilityTick(stack, "SporeCloud", now);

				int blindDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
				attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,
						blindDuration, 0, true, true, true));
				attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						blindDuration, 0, true, true, true));
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Decomposer — killed mobs drop bonus loot
		// Re-roll the mob's loot table for extra drops
		if (maturity >= 4 && player.level() instanceof ServerLevel serverLevel) {
			var lootTableId = victim.getLootTable();
			LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableId);
			LootParams.Builder lootParams = new LootParams.Builder(serverLevel)
					.withParameter(LootContextParams.THIS_ENTITY, victim)
					.withParameter(LootContextParams.ORIGIN, victim.position())
					.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
					.withParameter(LootContextParams.KILLER_ENTITY, player)
					.withParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
					.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player);
			List<ItemStack> bonusLoot = lootTable.getRandomItems(
					lootParams.create(LootContextParamSets.ENTITY));
			for (ItemStack drop : bonusLoot) {
				ItemEntity itemEntity = new ItemEntity(serverLevel,
						victim.getX(), victim.getY(), victim.getZ(), drop);
				serverLevel.addFreshEntity(itemEntity);
			}
		}
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Spore Cloud (Blind attacker on hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Mycorrhizal Network (Heal nearby allies)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Decomposer (Bonus loot from kills)", 4, currentMaturity));
		return list;
	}

}
