package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.entity.utility.MorphlingFieldEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Sunburst, the Giving Light's strain (Heliophyta donans): sustenance, rescue of others, light. Sun-Fed waives upkeep
 * and feeds the host at home; Shared Table gives health to a failing ally (or food to a failing host alone); Lantern
 * Fronds light the deep for a still host's company; Bloom seeds a feeding patch; Arbor anchors the host as a healing
 * tree. Sunburst has no self death-save: its rescue is aimed at others. Numbers in {@link SunburstRules}.
 */
public class SunburstMorphlingItem extends HouseMorphlingItem {
	private static final String TABLE_COOLDOWN = "SharedTable";
	private static final String BLOOM_COOLDOWN = "Bloom";
	private static final String LANTERN_WINDOW = "sunburst.lantern_until";
	private static final String BLOOM_WINDOW = "sunburst.bloom_until";
	private static final String ARBOR_WINDOW = "sunburst.arbor_until";
	private static final String ARBOR_START = "sunburst.arbor_start";
	private static final int LANTERN_REFRESH = 10;
	private static final int LANTERN_EFFECT_TICKS = 80;

	public SunburstMorphlingItem(Properties properties) {
		super(properties);
	}

	@Override
	public MorphlingStrain strain() {
		return MorphlingStrains.SUNBURST;
	}

	@Override
	public boolean waivesUpkeep(Player player, ItemStack stack, boolean puppetInterference) {
		return SunburstRules.waivesUpkeep(home(player), puppetInterference);
	}

	@Override
	public void onEquippedServerTick(Player player, ItemStack stack) {
		int stage = stage(stack);
		boolean home = home(player);
		if (home && proc(player, SunburstRules.PROC_TICKS)) sunFed(player, stage);
		if (stage >= 2 && proc(player, 10)) sharedTable(player, stack, stage, home);
		if (stage >= 3) lanternFronds(player, home);
		if (stage >= 4 && home && MorphlingAnchorState.isStill(player, SunburstRules.BLOOM_STILL_TICKS)
				&& ready(player, stack, BLOOM_COOLDOWN, SunburstRules.BLOOM_COOLDOWN)
				&& player.level() instanceof ServerLevel level) {
			spend(player, stack, BLOOM_COOLDOWN);
			MorphlingFieldEntity.spawn(level, player, MorphlingFieldEntity.Kind.BLOOM, player.getX(), player.getY(),
					player.getZ(), (float) SunburstRules.BLOOM_RADIUS, SunburstRules.BLOOM_LIFETIME);
			MorphlingSession.open(player, BLOOM_WINDOW, SunburstRules.BLOOM_LIFETIME);
		}
		if (MorphlingSession.get(player, ARBOR_WINDOW) > 0L) arbor(player, home);
	}

	private static void sunFed(Player player, int stage) {
		var food = player.getFoodData();
		if (food.getFoodLevel() < 20) {
			MorphlingFeeding.feed(player, SunburstRules.sunFedHunger(stage), true);
		} else if (SunburstRules.surplusBecomesBlood(food.getFoodLevel(), player.level().isDay())
				&& player instanceof ServerPlayer server) {
			BloodFlowLedger.applyCirculationIncome(server, HemoCapabilityAccess.getBloodVolume(player), "sun_fed",
					"blood_source.hemomancy.sun_fed", Category.MORPHLING, SunburstRules.sunFedBlood(stage),
					SunburstRules.PROC_TICKS, CirculationIncomeHelper.IncomeChannel.MORPHLING);
		}
	}

	private static void sharedTable(Player player, ItemStack stack, int stage, boolean home) {
		if (!ready(player, stack, TABLE_COOLDOWN, home ? SunburstRules.SHARED_TABLE_HOME_COOLDOWN
				: SunburstRules.SHARED_TABLE_COOLDOWN)) return;
		List<LivingEntity> allies = MorphlingAllies.allies(player, SunburstRules.SHARED_TABLE_RANGE);
		for (LivingEntity ally : allies) {
			if (ally.getHealth() > ally.getMaxHealth() * SunburstRules.SHARED_TABLE_TRIGGER) continue;
			float gift = MorphlingBalanceBands.givable(player.getHealth(), player.getMaxHealth(),
					MorphlingBalanceBands.SHARED_TABLE_GIVER_FLOOR, SunburstRules.sharedTableGift(stage));
			if (gift <= 0.0F) return;
			player.setHealth(player.getHealth() - gift);
			ally.heal(gift);
			spend(player, stack, TABLE_COOLDOWN);
			return;
		}
		if (allies.isEmpty() && player.getHealth() <= player.getMaxHealth() * SunburstRules.SHARED_TABLE_TRIGGER) {
			var food = player.getFoodData();
			food.eat(SunburstRules.SHARED_TABLE_ALONE_FOOD, 0.0F);
			food.setSaturation(food.getFoodLevel());
			spend(player, stack, TABLE_COOLDOWN);
		}
	}

	private static void lanternFronds(Player player, boolean home) {
		if (MorphlingAnchorState.isStill(player, SunburstRules.LANTERN_STILL_TICKS)) {
			MorphlingSession.open(player, LANTERN_WINDOW, SunburstRules.LANTERN_LINGER_TICKS);
		}
		if (!MorphlingSession.isOpen(player, LANTERN_WINDOW) || !proc(player, LANTERN_REFRESH)) return;
		List<LivingEntity> lit = new ArrayList<>(MorphlingAllies.allies(player,
				home ? SunburstRules.LANTERN_HOME_RANGE : SunburstRules.LANTERN_RANGE));
		lit.add(player);
		// Spec: the light lasts 3 seconds after the host moves. Each refresh runs only to the end of the linger
		// window (plus one refresh), so nobody carries the fronds' breathing much past it.
		int ticks = (int) Math.min(LANTERN_EFFECT_TICKS,
				MorphlingSession.remaining(player, LANTERN_WINDOW) + LANTERN_REFRESH);
		for (LivingEntity living : lit) {
			living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, ticks, 0, true, false, true));
			living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, ticks, 0, true, false, true));
		}
	}

	@Override
	public boolean tryUse(Player player, InteractionHand hand, ItemStack stack, Level level) {
		if (!mayAct(player)) return false;
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			if (!MorphlingItem.tryBeginPrimalAbility(player, stack, "Arbor", SunburstRules.ARBOR_COST,
					SunburstRules.ARBOR_COOLDOWN, SunburstRules.ARBOR_STRAIN, 0,
					MorphlingItem.primalSource("arbor"))) return false;
			MorphlingAnchorState.settle(player, player.isShiftKeyDown(), 0);
			MorphlingSession.open(player, ARBOR_WINDOW, SunburstRules.ARBOR_MAX_TICKS);
			MorphlingSession.put(player, ARBOR_START, player.level().getGameTime());
			return true;
		}
	}

	/** Arbor while anchored: regeneration and saturation around the host, a cleanse every 4 s, blood at home. */
	private static void arbor(Player player, boolean home) {
		long now = player.level().getGameTime();
		boolean uprooted = now - MorphlingSession.get(player, ARBOR_START) > 2
				&& MorphlingAnchorState.stillTicks(player) == 0;
		if (!MorphlingSession.isOpen(player, ARBOR_WINDOW) || uprooted) {
			MorphlingSession.remove(player, ARBOR_WINDOW);
			MorphlingSession.remove(player, ARBOR_START);
			return;
		}
		long elapsed = now - MorphlingSession.get(player, ARBOR_START);
		if (elapsed % 20 != 0) return;
		List<LivingEntity> sheltered = new ArrayList<>(MorphlingAllies.allies(player, SunburstRules.ARBOR_RANGE));
		sheltered.add(player);
		boolean cleanse = elapsed > 0 && elapsed % SunburstRules.ARBOR_CLEANSE_TICKS == 0;
		for (LivingEntity living : sheltered) {
			living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 1, true, false, true));
			if (living instanceof Player sheltered1) {
				var food = sheltered1.getFoodData();
				food.setSaturation(Math.min(food.getFoodLevel(), food.getSaturationLevel() + 1.0F));
			}
			if (cleanse) cleanseHarmful(living, 1);
		}
		if (home && player instanceof ServerPlayer server) {
			BloodFlowLedger.applyCirculationIncome(server, HemoCapabilityAccess.getBloodVolume(player), "arbor",
					"blood_source.hemomancy.arbor", Category.MORPHLING, SunburstRules.ARBOR_HOME_BLOOD_PER_SECOND, 20,
					CirculationIncomeHelper.IncomeChannel.MORPHLING);
		}
	}

	@Override
	public int activeFlags(Player player, ItemStack stack) {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.LANTERN_FRONDS,
				stage(stack) >= 3 && MorphlingSession.isOpen(player, LANTERN_WINDOW));
		flags = MorphlingActiveState.set(flags, MorphlingActiveState.BLOOM, MorphlingSession.isOpen(player, BLOOM_WINDOW));
		return MorphlingActiveState.set(flags, MorphlingActiveState.ARBOR, MorphlingSession.isOpen(player, ARBOR_WINDOW));
	}

	@Override
	public List<Component> getMaturityBonusDescriptions(int currentMaturity) {
		return ladder(currentMaturity, "shared_table", "lantern_fronds", "bloom", "arbor");
	}
}
