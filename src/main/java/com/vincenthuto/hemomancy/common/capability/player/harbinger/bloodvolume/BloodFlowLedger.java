package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.event.CirculationIncomeRules;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class BloodFlowLedger {
	private static final int EXPIRY_PADDING_TICKS = 4;
	private static final ConcurrentMap<UUID, ConcurrentMap<String, TrackedContribution>> RECENT =
			new ConcurrentHashMap<>();

	private BloodFlowLedger() {
	}

	public record DrainResult(double requested, double actual, boolean satisfied) {
	}

	public static double applyDirectIncome(ServerPlayer player, IBloodVolume volume, String sourceId, String label,
			Category category, double amount, int intervalTicks) {
		if (player == null || volume == null || amount <= 0.0D) {
			return 0.0D;
		}
		int interval = normalizedInterval(intervalTicks);
		double actual = 0.0D;
		String condition = "";
		if (!volume.isActive()) {
			condition = "Dormant";
		} else if (volume.isFull()) {
			condition = "Full reserve";
		} else {
			double before = volume.getBloodVolume();
			volume.fill(amount);
			actual = Math.max(0.0D, volume.getBloodVolume() - before);
			if (actual + 0.000001D < amount) {
				condition = "Full reserve";
			}
		}
		record(player, BloodFlowContribution.direct(sourceId, label, category, amount / interval,
				actual / interval, condition), interval);
		if (actual > 0.0D) {
			BloodVolumeEvents.syncVolume(player, volume);
		}
		return actual;
	}

	public static double applyCirculationIncome(ServerPlayer player, IBloodVolume volume, String sourceId,
			String label, Category category, double requested, int intervalTicks,
			CirculationIncomeHelper.IncomeChannel channel) {
		if (player == null || volume == null || requested <= 0.0D) {
			return 0.0D;
		}
		int interval = normalizedInterval(intervalTicks);
		double actual = 0.0D;
		String condition = "";
		if (!volume.isActive()) {
			condition = "Dormant";
		} else if (volume.isFull()) {
			condition = "Full reserve";
		} else if (!circulationEnabled()) {
			double before = volume.getBloodVolume();
			volume.fill(requested);
			actual = Math.max(0.0D, volume.getBloodVolume() - before);
			condition = "Circulation disabled";
		} else {
			long now = player.level().getGameTime();
			PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
			double bandwidth = bandwidthPerWindow(player);
			CirculationIncomeRules.Grant grant = CirculationIncomeRules.grant(requested, now,
					circulationWindowTicks(), state.getCirculationWindowStart(), state.getCirculationWindowUsed(),
					bandwidth);
			if (grant.granted() > 0.0D) {
				double before = volume.getBloodVolume();
				volume.fill(grant.granted());
				actual = Math.max(0.0D, volume.getBloodVolume() - before);
			}
			state.setCirculationWindowStart(grant.windowStart());
			state.setCirculationWindowUsed(grant.usedInWindow() - (grant.granted() - actual));
			if (actual + 0.000001D < requested) {
				condition = actual <= 0.0D ? "Circulation saturated" : "Circulation limited";
			}
		}
		record(player, new BloodFlowContribution(sourceId, label, category, requested / interval,
				actual / interval, true, condition), interval);
		if (actual > 0.0D) {
			BloodVolumeEvents.syncVolume(player, volume);
		}
		return actual;
	}

	public static DrainResult applyDrain(ServerPlayer player, IBloodVolume volume, String sourceId, String label,
			Category category, double amount, int intervalTicks, boolean requireFullAmount) {
		if (player == null || volume == null || amount <= 0.0D) {
			return new DrainResult(0.0D, 0.0D, false);
		}
		int interval = normalizedInterval(intervalTicks);
		double actual = 0.0D;
		boolean satisfied = false;
		String condition = "";
		if (!volume.isActive()) {
			condition = "Dormant";
		} else if (requireFullAmount && volume.getBloodVolume() < amount) {
			condition = "Insufficient blood";
		} else {
			double before = volume.getBloodVolume();
			boolean drained = volume.drain(amount);
			actual = Math.max(0.0D, before - volume.getBloodVolume());
			satisfied = drained && actual + 0.000001D >= amount;
			if (!satisfied && actual + 0.000001D < amount) {
				condition = "Blood exhausted";
			}
		}
		record(player, BloodFlowContribution.direct(sourceId, label, category, -amount / interval,
				-actual / interval, condition), interval);
		if (actual > 0.0D) {
			BloodVolumeEvents.syncVolume(player, volume);
		}
		return new DrainResult(amount, actual, satisfied);
	}

	public static double transferFromVessel(ServerPlayer player, IBloodVolume playerVolume, IBloodVolume vesselVolume,
			String sourceId, String label, double requested, int intervalTicks) {
		if (player == null || playerVolume == null || vesselVolume == null || requested <= 0.0D) {
			return 0.0D;
		}
		int interval = normalizedInterval(intervalTicks);
		double actual = 0.0D;
		String condition = "";
		if (!playerVolume.isActive()) {
			condition = "Dormant";
		} else if (playerVolume.isFull()) {
			condition = "Full reserve";
		} else if (vesselVolume.getBloodVolume() <= 0.0D) {
			condition = "Vessel empty";
		} else {
			double transfer = Math.min(requested, vesselVolume.getBloodVolume());
			transfer = Math.min(transfer, playerVolume.getMaxBloodVolume() - playerVolume.getBloodVolume());
			double before = playerVolume.getBloodVolume();
			vesselVolume.drain(transfer);
			playerVolume.fill(transfer);
			actual = Math.max(0.0D, playerVolume.getBloodVolume() - before);
			if (actual + 0.000001D < requested) {
				condition = playerVolume.isFull() ? "Full reserve" : "Vessel limited";
			}
		}
		record(player, BloodFlowContribution.direct(sourceId, label, Category.VESSEL, requested / interval,
				actual / interval, condition), interval);
		if (actual > 0.0D) {
			BloodVolumeEvents.syncVolume(player, playerVolume);
		}
		return actual;
	}

	public static BloodFlowSnapshot collect(ServerPlayer player, IBloodVolume volume) {
		if (player == null) {
			return BloodFlowSnapshot.EMPTY;
		}
		prune(player);
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		ConcurrentMap<String, TrackedContribution> entries = RECENT.get(player.getUUID());
		List<BloodFlowContribution> contributions = entries == null ? List.of()
				: entries.values().stream()
						.map(TrackedContribution::contribution)
						.sorted(Comparator
								.comparing((BloodFlowContribution contribution) -> contribution.category().ordinal())
								.thenComparing(BloodFlowContribution::label)
								.thenComparing(BloodFlowContribution::sourceId))
						.toList();
		return BloodFlowRules.appliedSnapshot(contributions,
				bandwidthPerWindow(player), state.getCirculationWindowUsed(), circulationWindowTicks(),
				circulationEnabled());
	}

	public static void recordApplied(ServerPlayer player, String sourceId, String label, Category category,
			double requestedAmount, double actualAmount, int intervalTicks, boolean circulationLimited,
			String condition) {
		if (player == null) {
			return;
		}
		int interval = normalizedInterval(intervalTicks);
		record(player, new BloodFlowContribution(sourceId, label, category, requestedAmount / interval,
				actualAmount / interval, circulationLimited, condition), interval);
	}

	public static void forget(ServerPlayer player, String sourceId) {
		if (player == null || sourceId == null) {
			return;
		}
		ConcurrentMap<String, TrackedContribution> entries = RECENT.get(player.getUUID());
		if (entries != null) {
			entries.remove(sourceId);
		}
	}

	private static void record(ServerPlayer player, BloodFlowContribution contribution, int intervalTicks) {
		long now = player.level().getGameTime();
		long expiresAt = now + normalizedInterval(intervalTicks) + EXPIRY_PADDING_TICKS;
		RECENT.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>())
				.put(contribution.sourceId(), new TrackedContribution(contribution, expiresAt));
	}

	private static void prune(ServerPlayer player) {
		ConcurrentMap<String, TrackedContribution> entries = RECENT.get(player.getUUID());
		if (entries == null) {
			return;
		}
		long now = player.level().getGameTime();
		ArrayList<String> stale = new ArrayList<>();
		for (Map.Entry<String, TrackedContribution> entry : entries.entrySet()) {
			if (entry.getValue().expiresAt() < now) {
				stale.add(entry.getKey());
			}
		}
		for (String key : stale) {
			entries.remove(key);
		}
	}

	private static double bandwidthPerWindow(ServerPlayer player) {
		return CirculationIncomeRules.bandwidthPerWindow(HemoServerConfig.CIRCULATION_BASE_BANDWIDTH.get(),
				HemoServerConfig.CIRCULATION_BANDWIDTH_PER_DEGREE.get(),
				HemoCapabilityAccess.getPlayerDegreeNumber(player),
				HemoServerConfig.CIRCULATION_BANDWIDTH_PER_CAPACITY_POINT.get(),
				SkillPointHelper.getCapacityBonus(player));
	}

	private static boolean circulationEnabled() {
		return HemoServerConfig.CIRCULATION_ENABLED == null || HemoServerConfig.CIRCULATION_ENABLED.get();
	}

	private static int circulationWindowTicks() {
		return HemoServerConfig.CIRCULATION_WINDOW_TICKS == null
				? CirculationIncomeRules.DEFAULT_WINDOW_TICKS : HemoServerConfig.CIRCULATION_WINDOW_TICKS.get();
	}

	private static int normalizedInterval(int intervalTicks) {
		return Math.max(1, intervalTicks);
	}

	private record TrackedContribution(BloodFlowContribution contribution, long expiresAt) {
	}
}
