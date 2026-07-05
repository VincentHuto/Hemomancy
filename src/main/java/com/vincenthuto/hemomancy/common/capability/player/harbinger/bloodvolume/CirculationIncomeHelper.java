package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.event.CirculationIncomeRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Compatibility adapter for the shared passive blood-income bandwidth.
 * Prefer {@link BloodFlowLedger#applyCirculationIncome} for new call sites so
 * diagnostics can use a specific source id and label.
 */
public final class CirculationIncomeHelper {

	public enum IncomeChannel {
		ARMOR, MORPHLING, CRADLE, SCAR, OTHER
	}

	private CirculationIncomeHelper() {
	}

	/**
	 * Request passive income for the player. Fills the granted amount into the
	 * supplied volume and returns what actually moved. The math is delegated to the
	 * unified blood-flow ledger so legacy sources still appear in diagnostics.
	 */
	public static double grant(Player player, IBloodVolume volume, double requested, IncomeChannel channel) {
		if (!(player instanceof ServerPlayer serverPlayer) || volume == null || requested <= 0.0D) {
			return 0.0D;
		}
		return BloodFlowLedger.applyCirculationIncome(serverPlayer, volume,
				"circulation_" + channelName(channel), titleCase(channelName(channel)), category(channel), requested,
				CirculationIncomeRules.DEFAULT_WINDOW_TICKS, channel);
	}

	private static Category category(IncomeChannel channel) {
		if (channel == null) {
			return Category.OTHER;
		}
		return switch (channel) {
			case ARMOR -> Category.ARMOR;
			case MORPHLING, CRADLE -> Category.MORPHLING;
			case SCAR -> Category.SCAR;
			case OTHER -> Category.OTHER;
		};
	}

	private static String channelName(IncomeChannel channel) {
		return channel == null ? "other" : channel.name().toLowerCase();
	}

	private static String titleCase(String value) {
		if (value == null || value.isEmpty()) {
			return "Other";
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
