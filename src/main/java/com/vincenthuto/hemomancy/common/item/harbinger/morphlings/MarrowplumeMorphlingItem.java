package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

/**
 * Marrowplume, Last Marrow's strain (Medullophyta ossivora): remains as a resource. Marrow Feeding heals the host;
 * Plume Cloud feeds on hostiles when the host is hurt; Root Network heals allies (or feeds the host alone); Fall's
 * Inheritance rolls bonus loot by chance; Whale Fall leaves a healing patch after a substantial kill. Numbers in
 * {@link MarrowplumeRules}.
 */
public class MarrowplumeMorphlingItem extends HouseMorphlingItem {
	private static final String PLUME_COOLDOWN = "PlumeCloud";
	private static final String PLUME_PULSE = "marrowplume.plume_pulse";
	private static final String WHALE_FALL = "WhaleFall";
	private static final int PULSE_TICKS = 10;

	public MarrowplumeMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.MARROWPLUME;
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		boolean networkFoundSomeone = false;
		if (stage >= 3 && proc(player, MarrowplumeRules.ROOT_NETWORK_TICKS)) {
			for (LivingEntity ally : MorphlingAllies.allies(player, MorphlingHomeBonus.radius(
					MarrowplumeRules.ROOT_NETWORK_RANGE, home(player)))) {
				if (ally.getHealth() < ally.getMaxHealth()) {
					ally.heal(MarrowplumeRules.ROOT_NETWORK_HEAL);
					networkFoundSomeone = true;
				}
			}
			if (!networkFoundSomeone) MorphlingSession.put(player, "marrowplume.alone", 1L);
			else MorphlingSession.remove(player, "marrowplume.alone");
		}
		if (proc(player, MarrowplumeRules.PROC_TICKS)) {
			float heal = MarrowplumeRules.feeding(stage, home(player));
			if (stage >= 3 && MorphlingSession.get(player, "marrowplume.alone") > 0L) {
				heal += MarrowplumeRules.ALONE_FEEDING_BONUS;
			}
			MorphlingHealing.healHost(player, heal);
		}
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount) {
		onEquippedHurt(player, stack, source, amount, true);
	}

	@Override
	public void onEquippedHurt(Player player, ItemStack stack, DamageSource source, float amount,
			boolean allowReactions) {
		int stage = stage(stack);
		if (!allowReactions || stage < 2 || player.getHealth() <= 0.0F
				|| !ready(player, stack, PLUME_COOLDOWN, MarrowplumeRules.PLUME_COOLDOWN)) return;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, source)) {
			spend(player, stack, PLUME_COOLDOWN);
			MorphlingSession.open(player, PLUME_PULSE, PULSE_TICKS);
			double radius = MorphlingHomeBonus.radius(MarrowplumeRules.plumeRadius(stage), home(player));
			int caught = 0;
			for (LivingEntity hostile : MorphlingAllies.hostiles(player, radius)) {
				hostile.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MarrowplumeRules.PLUME_WEAKNESS_TICKS, 0,
						false, true, true), player);
				hostile.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, MarrowplumeRules.PLUME_SLOWNESS_TICKS,
						0, false, true, true), player);
				caught++;
			}
			MorphlingHealing.healHost(player, MarrowplumeRules.plumeHeal(caught));
		}
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim) {
		onEquippedKill(player, stack, victim, true);
	}

	@Override
	public void onEquippedKill(Player player, ItemStack stack, LivingEntity victim, boolean allowReactions) {
		if (!(player.level() instanceof ServerLevel level)) return;
		int stage = stage(stack);
		if (stage >= 4 && MarrowplumeRules.mayInherit(victim instanceof Player,
				ManipulationReactiveEvents.isBoss(victim), isSpawnerMob(victim))) {
			if (level.random.nextFloat() < MarrowplumeRules.inheritanceChance(stage)) {
				rollBonusLoot(level, player, victim);
			}
			level.addFreshEntity(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(),
					new ItemStack(Items.BONE)));
			MorphlingItem.addHusbandryProgress(stack, 1);
		}
		if (MorphlingItem.isPrimal(stack) && victim.getMaxHealth() >= MarrowplumeRules.WHALE_FALL_MIN_HEALTH
				&& !(victim instanceof Player) && ready(player, stack, WHALE_FALL, MarrowplumeRules.WHALE_FALL_COOLDOWN)) {
			spend(player, stack, WHALE_FALL);
			boolean home = home(player);
			MorphlingItem.applyMorphicStrain(player, PrimalMorphlingRules.morphicStrainDuration(
					MarrowplumeRules.WHALE_FALL_STRAIN, SkillPointHelper.getPrimalMorphogenesisLevel(player)), 0);
			MorphlingFieldEntity.spawn(level, player, MorphlingFieldEntity.Kind.WHALE_FALL, victim.getX(), victim.getY(),
					victim.getZ(), (float) MorphlingHomeBonus.radius(MarrowplumeRules.WHALE_FALL_RADIUS, home),
					MorphlingHomeBonus.duration(MarrowplumeRules.WHALE_FALL_TICKS, home));
			if (level.random.nextFloat() < MarrowplumeRules.sporeSacChance(SkillPointHelper.getHyphalCultivationLevel(player))) {
				level.addFreshEntity(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(),
						new ItemStack(ItemInit.spore_sac.get())));
			}
		}
	}

	private static boolean isSpawnerMob(LivingEntity victim) {
		return victim instanceof Mob mob && mob.getSpawnType() == MobSpawnType.SPAWNER;
	}

	private static void rollBonusLoot(ServerLevel level, Player player, LivingEntity victim) {
		var table = level.getServer().reloadableRegistries().getLootTable(victim.getLootTable());
		LootParams params = new LootParams.Builder(level)
				.withParameter(LootContextParams.THIS_ENTITY, victim)
				.withParameter(LootContextParams.ORIGIN, victim.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
				.withParameter(LootContextParams.ATTACKING_ENTITY, player)
				.withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, player)
				.withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
				.create(LootContextParamSets.ENTITY);
		for (ItemStack drop : table.getRandomItems(params)) {
			level.addFreshEntity(new ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), drop));
		}
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.PLUME_CLOUD, MorphlingSession.isOpen(player, PLUME_PULSE));
		boolean rooted = MorphlingAnchorState.isAnchored(player, 1) && !player.level().getEntitiesOfClass(
				MorphlingFieldEntity.class, player.getBoundingBox().inflate(MarrowplumeRules.WHALE_FALL_RADIUS * 1.25D),
				field -> field.kind() == MorphlingFieldEntity.Kind.WHALE_FALL && player.getUUID().equals(field.owner())
						&& field.distanceToSqr(player) <= field.radius() * field.radius()).isEmpty();
		return MorphlingActiveState.set(flags, MorphlingActiveState.WHALE_FALL_ROOTED, rooted);
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "plume_cloud", "root_network", "falls_inheritance", "whale_fall");
	}
}
