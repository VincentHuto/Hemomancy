package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Fungal morphling that passively regenerates health by applying the
 * Mycorrhizal Mending effect while equipped. Maturity level scales the
 * healing amplifier. Prefers MORTEM (death/decay nourishes fungi) with
 * ANIMUS as secondary (life force aids regeneration).
 *
 * Maturity bonuses (unique reactive abilities):
 * - Developing (2): Sporulation — when damaged, emit a burst of toxic spores
 *   that inflict Wither and Slowness on ALL nearby hostiles in a radius
 *   (not just the attacker — area denial through spore dispersal)
 * - Mature (3): Mycorrhizal Network — passively heal nearby allied players
 *   (symbiotic fungal healing link)
 * - Apex (4): Cordyceps Burst — killed mobs explode in a toxic fungal bloom
 *   that poisons and slows all nearby hostiles AND drops bonus loot
 *   (parasitic fungi consume the corpse and burst outward)
 */
public class FungalMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Sporulation triggers (3 seconds). */
	private static final int SPORULATION_COOLDOWN = 60;

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
		if (!player.hasEffect(EffectInit.mycorrhizal_mending)) {
			player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending,
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

		// Developing (2+): Sporulation — toxic spore burst hits ALL nearby hostiles
		if (maturity >= 2 && !player.level().isClientSide) {
			long lastSpore = getLastAbilityTick(stack, "Sporulation");
			long now = player.level().getGameTime();
			if (now - lastSpore >= SPORULATION_COOLDOWN) {
				setLastAbilityTick(stack, "Sporulation", now);

				double radius = 5.0 + (maturity - 2) * 1.0; // 5 at Developing, 6 Mature, 7 Apex
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);

				int witherDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
				int slowDuration = 30 + (maturity - 2) * 15; // 1.5s at Developing, 2.25s Mature, 3s Apex
				for (Monster mob : hostiles) {
					mob.addEffect(new MobEffectInstance(MobEffects.WITHER,
							witherDuration, 0, true, true, true));
					mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
							slowDuration, 0, true, true, true));
				}
			}
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Cordyceps Burst — toxic fungal explosion from the corpse
		// poisons and slows nearby hostiles AND drops bonus loot
		if (maturity >= 4 && player.level() instanceof ServerLevel serverLevel) {
			// Toxic burst: Poison + Slowness to all nearby hostiles
			double radius = 6.0;
			AABB area = victim.getBoundingBox().inflate(radius);
			List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class, area,
					m -> m != victim && m.isAlive());
			for (Monster mob : hostiles) {
				mob.addEffect(new MobEffectInstance(MobEffects.POISON,
						100, 1, true, true, true)); // Poison II for 5s
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						80, 0, true, true, true)); // Slowness for 4s
			}

			// Bonus loot: re-roll the mob's loot table
			var lootTableId = victim.getLootTable();
			LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableId);
			LootParams.Builder lootParams = new LootParams.Builder(serverLevel)
					.withParameter(LootContextParams.THIS_ENTITY, victim)
					.withParameter(LootContextParams.ORIGIN, victim.position())
					.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
					.withParameter(LootContextParams.ATTACKING_ENTITY, player)
					.withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
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
		list.add(MorphlingItem.maturityBonusLine("Sporulation (AoE toxic spores when hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Mycorrhizal Network (Heal nearby allies)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Cordyceps Burst (Kills explode, poison foes + bonus loot)", 4, currentMaturity));
		return list;
	}

}
