package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
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
 *   that inflict Necrosis on ALL nearby hostiles in a radius
 *   (not just the attacker — area denial through spore dispersal)
 * - Mature (3): Mycorrhizal Network — passively heal nearby allied players
 *   (symbiotic fungal healing link)
 * - Apex (4): Cordyceps Burst — killed mobs explode in a toxic fungal bloom
 *   that applies Necrosis to nearby hostiles and drops bonus loot
 *   (parasitic fungi consume the corpse and burst outward)
 */
public class GravecapMorphlingItem extends MorphlingItem {

	/** Cooldown in ticks between Sporulation triggers (3 seconds). */
	private static final int SPORULATION_COOLDOWN = 60;
	private static final int PRIMAL_MYCORRHIZA_COOLDOWN = 600;

	public GravecapMorphlingItem(Properties prop) {
		super(prop);
	}

	@Override
	protected String binomialKey() {
		return "morphling.hemomancy.gravecap.binomial";
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
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {

		int maturity = MorphlingItem.getMaturityLevel(stack);
		int amplifier = MorphlingItem.passiveAmplifier(player, stack, maturity);

		// Base effect: Gravecap Morphling (health regen, amplifier = maturity)
		MorphlingItem.applyPassiveEffect(player, stack, EffectInit.morphling_gravecap,
				EffectInit.mycorrhizal_mending, amplifier);

		// Mature (3+): Mycorrhizal Network — heal nearby allied players
		if (maturity >= 3 && !player.level().isClientSide) {
			int hyphal = SkillPointHelper.getHyphalCultivationLevel(player);
			double radius = 8.0 + hyphal;
			AABB area = player.getBoundingBox().inflate(radius);
			List<Player> nearbyPlayers = player.level().getEntitiesOfClass(Player.class, area,
					p -> p != player && p.getHealth() < p.getMaxHealth());
			float healPerAlly = 0.5f + (maturity - 3) * 0.5f + hyphal * 0.15f; // 0.5 at Mature, 1.0 at Apex
			for (Player ally : nearbyPlayers) {
				ally.heal(healPerAlly);
			}
		}

        }
    }

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, source)) {

		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Developing (2+): Sporulation — toxic spore burst hits ALL nearby hostiles
		if (maturity >= 2 && !player.level().isClientSide) {
			long lastSpore = getLastAbilityTick(stack, "Sporulation");
			long now = player.level().getGameTime();
			if (now - lastSpore >= SPORULATION_COOLDOWN) {
				setLastAbilityTick(stack, "Sporulation", now);

				int hyphal = SkillPointHelper.getHyphalCultivationLevel(player);
				double radius = 5.0 + (maturity - 2) * 1.0 + hyphal * 0.5; // 5 at Developing, 6 Mature, 7 Apex
				AABB area = player.getBoundingBox().inflate(radius);
				List<Monster> hostiles = player.level().getEntitiesOfClass(Monster.class, area);

				int necrosisDuration = 40 + (maturity - 2) * 20; // 2s at Developing, 3s Mature, 4s Apex
				for (Monster mob : hostiles) {
					MorphlingCombat.afflict(this, player, mob, necrosisDuration);
				}
			}
		}

        }
    }

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
        onEquippedKill(player, stack, victim, true);
    }

    @Override
    public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim, boolean allowReactions) {
        try (var schoolAbility = MorphlingCombat.scope(this, player, stack, player.damageSources().magic())) {

		int maturity = MorphlingItem.getMaturityLevel(stack);

		// Apex (4): Cordyceps Burst — toxic fungal explosion from the corpse
		// Necrosis spreads to nearby hostiles; the kill also drops bonus loot.
		if (maturity >= 4 && player.level() instanceof ServerLevel serverLevel) {
			// Toxic burst: Necrosis to all nearby hostiles
			int hyphal = SkillPointHelper.getHyphalCultivationLevel(player);
			double radius = 6.0 + hyphal * 0.5;
			AABB area = victim.getBoundingBox().inflate(radius);
			List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class, area,
					m -> m != victim && m.isAlive());
			for (Monster mob : hostiles) {
				if (allowReactions) MorphlingCombat.afflict(this, player, mob, 100);
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
			MorphlingItem.addHusbandryProgress(stack, 2);
		}

		if (MorphlingItem.isPrimal(stack) && player.level() instanceof ServerLevel serverLevel
				&& victim.getMaxHealth() >= 20.0f) {
			long now = serverLevel.getGameTime();
			long lastMycorrhiza = getLastAbilityTick(stack, "PrimalMycorrhiza");
			if (now - lastMycorrhiza >= PRIMAL_MYCORRHIZA_COOLDOWN) {
				setLastAbilityTick(stack, "PrimalMycorrhiza", now);
				MorphlingItem.applyMorphicStrain(player,
						PrimalMorphlingRules.morphicStrainDuration(220,
								SkillPointHelper.getPrimalMorphogenesisLevel(player)),
						0);

				double radius = 7.0 + SkillPointHelper.getHyphalCultivationLevel(player) * 0.75;
				AABB area = victim.getBoundingBox().inflate(radius);
				for (Player ally : serverLevel.getEntitiesOfClass(Player.class, area, Player::isAlive)) {
					ally.heal(3.0f);
					ally.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending,
							160, 1, true, true, true));
				}
				for (Monster mob : serverLevel.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
					if (allowReactions) MorphlingCombat.afflict(this, player, mob, 180);
				}
				if (serverLevel.random.nextFloat() < 0.12f + SkillPointHelper.getHyphalCultivationLevel(player) * 0.03f) {
					serverLevel.addFreshEntity(new ItemEntity(serverLevel,
							victim.getX(), victim.getY(), victim.getZ(),
							new ItemStack(ItemInit.spore_sac.get())));
				}
			}
		}

        }
    }

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		List<Component> list = new ArrayList<>();
		list.add(MorphlingItem.maturityBonusLine("Sporulation (AoE toxic spores when hit)", 2, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Mycorrhizal Network (Heal nearby allies)", 3, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Cordyceps Burst (Kills spread Necrosis + bonus loot)", 4, currentMaturity));
		list.add(MorphlingItem.maturityBonusLine("Primal Mycorrhiza (Elite kills seed a healing fungal patch)", 5, currentMaturity));
		return list;
	}

}
