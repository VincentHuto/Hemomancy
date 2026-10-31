package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A Brined Court house strain. Identity (tendencies, Latin, passive effect) comes from its
 * {@link MorphlingStrain} row; each strain's class adds its power ladder (Brined Court Track 2).
 */
public abstract class HouseMorphlingItem extends MorphlingItem {
	protected HouseMorphlingItem(Properties properties) {
		super(properties);
	}

	public abstract MorphlingStrain strain();

	@Override
	public EnumBloodTendency getPreferredTendency() {
		return strain().preferred();
	}

	@Override
	public EnumBloodTendency getSecondaryTendency() {
		return strain().secondary();
	}

	@Override
	protected String binomialKey() {
		return strain().binomialKey();
	}

	/** The Order's description, the organism at its current stage and, once the Court is known, the house line. */
	@Override
	protected void appendStrainText(ItemStack stack, java.util.List<net.minecraft.network.chat.Component> tooltip) {
		MorphlingStrain strain = strain();
		tooltip.add(net.minecraft.network.chat.Component.translatable(strain.descriptionKey())
				.withStyle(net.minecraft.ChatFormatting.GRAY));
		tooltip.add(net.minecraft.network.chat.Component.translatable(strain.stageKey(MorphlingItem.getMaturityLevel(stack)))
				.withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
		if (com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtKnowledge.isKnown(
				com.vincenthuto.hemomancy.common.util.ClientBridge.get().localPlayer())) {
			tooltip.add(net.minecraft.network.chat.Component.translatable(strain.courtKey())
					.withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
		}
	}

	@Override
	public void onEquippedTick(Player player, ItemStack stack) {
		try (var schoolAbility = MorphlingCombat.scope(this, player, stack, null)) {
			applyStrainPassive(player, stack);
		}
	}

	/** The shared preamble for a Primal active: refuses while action-locked and ends a Marionette channel. */
	protected static boolean mayAct(Player player) {
		if (com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis.blocksActions(player)) return false;
		if (player instanceof net.minecraft.server.level.ServerPlayer server
				&& com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.isMarionetteChannel(server)) {
			com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager.stop(server, false);
		}
		return true;
	}

	/** Strips harmful effects, as Fold Shut's unfolding and Arbor's cleanse do; {@code limit} caps how many. */
	protected static int cleanseHarmful(net.minecraft.world.entity.LivingEntity entity, int limit) {
		int removed = 0;
		for (var instance : java.util.List.copyOf(entity.getActiveEffects())) {
			if (removed >= limit) break;
			var effect = instance.getEffect();
			if (effect.value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL
					&& !effect.is(com.vincenthuto.hemomancy.common.init.EffectInit.morphic_strain)
					&& !effect.is(com.vincenthuto.hemomancy.common.init.EffectInit.pinned)) {
				entity.removeEffect(effect);
				removed++;
			}
		}
		return removed;
	}

	protected static int stage(ItemStack stack) {
		return MorphlingItem.getMaturityLevel(stack);
	}

	protected boolean home(Player player) {
		return MorphlingHomeConditions.isHome(player, strain());
	}

	/** A stack cooldown: ready when never used or {@code ticks} have passed since. */
	protected static boolean ready(Player player, ItemStack stack, String key, int ticks) {
		long last = MorphlingItem.getLastAbilityTick(stack, key);
		return last <= 0L || player.level().getGameTime() - last >= ticks;
	}

	protected static void spend(Player player, ItemStack stack, String key) {
		MorphlingItem.setLastAbilityTick(stack, key, Math.max(1L, player.level().getGameTime()));
	}

	/** True on the server tick a {@code period}-tick proc falls on. */
	protected static boolean proc(Player player, int period) {
		return !player.level().isClientSide && player.level().getGameTime() % period == 0;
	}

	/** A maturity-bonus line's text: {@code message.hemomancy.<strain id>_morphling_item.<power>}. */
	protected net.minecraft.network.chat.Component power(String power) {
		return net.minecraft.network.chat.Component.translatable(
				"message.hemomancy." + strain().id() + "_morphling_item." + power);
	}

	/** The ladder's bonus lines: the passive from Unfed, then Developing to Primal, in that order. */
	protected java.util.List<net.minecraft.network.chat.Component> ladder(int currentMaturity, String developing,
			String mature, String apex, String primal) {
		return java.util.List.of(
				MorphlingItem.maturityBonusLine(power("passive"), 0, currentMaturity),
				MorphlingItem.maturityBonusLine(power(developing), 2, currentMaturity),
				MorphlingItem.maturityBonusLine(power(mature), 3, currentMaturity),
				MorphlingItem.maturityBonusLine(power(apex), 4, currentMaturity),
				MorphlingItem.maturityBonusLine(power(primal), 5, currentMaturity));
	}

	/** Keeps the strain's passive effect up at an amplifier that follows maturity. */
	protected void applyStrainPassive(Player player, ItemStack stack) {
		int maturity = MorphlingItem.getMaturityLevel(stack);
		MorphlingItem.applyPassiveEffect(player, stack, strain().passiveEffect(),
				MorphlingItem.passiveAmplifier(player, stack, maturity));
	}
}
