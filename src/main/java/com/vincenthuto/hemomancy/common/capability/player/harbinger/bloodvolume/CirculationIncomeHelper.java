package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.CirculationIncomeRules;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Minecraft-facing adapter for {@link CirculationIncomeRules}: all passive
 * blood-income sources (armor set regen, mask trickles, morphling siphons,
 * cradle redistribution) route their fills through {@link #grant} so they
 * share one bandwidth window instead of stacking additively.
 *
 * <p>Callers keep their own capability lookups and sync calls — this helper
 * only decides how much of the requested income fits through the pipe this
 * window, fills that much, and records the usage on the player's persistent
 * data (matching the persistent-data-tag pattern used by ArmorSetBonusHandler
 * cooldowns).</p>
 */
public final class CirculationIncomeHelper {

	/** Which system is asking; recorded for future debug readouts. */
	public enum IncomeChannel {
		ARMOR, MORPHLING, CRADLE, SCAR, OTHER
	}

	private static final String WINDOW_START_TAG = "hemomancy:circulation_window_start";
	private static final String WINDOW_USED_TAG = "hemomancy:circulation_window_used";

	private CirculationIncomeHelper() {
	}

	/**
	 * Request passive income for the player. Fills the granted amount into the
	 * supplied volume and returns it; returns 0 when the pipe is saturated,
	 * the volume is inactive/full, or the request is non-positive. With the
	 * governor disabled in config this is a pure pass-through fill.
	 */
	public static double grant(Player player, IBloodVolume volume, double requested, IncomeChannel channel) {
		if (player == null || volume == null || requested <= 0.0D) {
			return 0.0D;
		}
		if (player.level().isClientSide()) {
			return 0.0D;
		}
		if (!volume.isActive() || volume.isFull()) {
			return 0.0D;
		}
		if (!HemoServerConfig.CIRCULATION_ENABLED.get()) {
			double before = volume.getBloodVolume();
			volume.fill(requested);
			return Math.max(0.0D, volume.getBloodVolume() - before);
		}

		long now = player.level().getGameTime();
		CompoundTag data = player.getPersistentData();
		double bandwidth = CirculationIncomeRules.bandwidthPerWindow(
				HemoServerConfig.CIRCULATION_BASE_BANDWIDTH.get(),
				HemoServerConfig.CIRCULATION_BANDWIDTH_PER_DEGREE.get(),
				HemoCapabilityAccess.getPlayerDegreeNumber(player),
				HemoServerConfig.CIRCULATION_BANDWIDTH_PER_CAPACITY_POINT.get(),
				SkillPointHelper.getCapacityBonus(player));
		CirculationIncomeRules.Grant grant = CirculationIncomeRules.grant(requested, now,
				HemoServerConfig.CIRCULATION_WINDOW_TICKS.get(), data.getLong(WINDOW_START_TAG),
				data.getDouble(WINDOW_USED_TAG), bandwidth);

		double actual = 0.0D;
		if (grant.granted() > 0.0D) {
			double before = volume.getBloodVolume();
			volume.fill(grant.granted());
			actual = Math.max(0.0D, volume.getBloodVolume() - before);
		}
		// Record what actually moved — fill may clamp at max volume, and the
		// window must never charge for blood that was not delivered (callers
		// like the cradle decrement their buffers by the returned amount).
		data.putLong(WINDOW_START_TAG, grant.windowStart());
		data.putDouble(WINDOW_USED_TAG, grant.usedInWindow() - (grant.granted() - actual));
		return actual;
	}
}
