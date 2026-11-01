package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;

/**
 * Who a card is open to. Every field is optional; a card that fails one is hidden, or drawn locked with
 * {@link #firstFailure}'s reason when it sets {@code show_locked}. Degree and stage are read through
 * {@link ProgressionGates}; {@code bloodActive} is implied by a blood price.
 */
public record NpcTradeRequirements(Path path, int degreeAtLeast, int stageAtLeast, int rapportAtLeast,
		Optional<ResourceLocation> advancement, boolean bloodActive) {
	public static final NpcTradeRequirements NONE = new NpcTradeRequirements(Path.ANY, 0, 0, 0, Optional.empty(), false);

	public static final String LOCKED_PATH = "hemomancy.trade.locked.path";
	public static final String LOCKED_DEGREE = "hemomancy.trade.locked.degree";
	public static final String LOCKED_STAGE = "hemomancy.trade.locked.stage";
	public static final String LOCKED_BLOOD = "hemomancy.trade.locked.blood";
	public static final String LOCKED_ADVANCEMENT = "hemomancy.trade.locked.advancement";
	public static final String LOCKED_RAPPORT = "hemomancy.trade.locked.rapport";
	/** Every lock reason key, for the lang resource test. */
	public static final List<String> LOCK_KEYS = List.of(LOCKED_PATH, LOCKED_DEGREE, LOCKED_STAGE, LOCKED_BLOOD,
			LOCKED_ADVANCEMENT, LOCKED_RAPPORT);

	public enum Path {
		ANY, HARBINGER, UNSTAINED;

		boolean holds(ServerPlayer player) {
			return switch (this) {
				case ANY -> true;
				case HARBINGER -> ProgressionGates.hasDegree(player, 1);
				case UNSTAINED -> ProgressionGates.isUnstained(player);
			};
		}
	}

	public NpcTradeRequirements withBloodActive() {
		return new NpcTradeRequirements(path, degreeAtLeast, stageAtLeast, rapportAtLeast, advancement, true);
	}

	/** The lock reason key of the first requirement {@code player} fails, given their rapport with the speaker. */
	public Optional<String> firstFailure(ServerPlayer player, int rapport) {
		if (!path.holds(player)) return Optional.of(LOCKED_PATH);
		if (!ProgressionGates.hasDegree(player, degreeAtLeast)) return Optional.of(LOCKED_DEGREE);
		if (ProgressionGates.unstainedStage(player) < stageAtLeast) return Optional.of(LOCKED_STAGE);
		if (bloodActive && !ProgressionGates.isBloodActive(player)) return Optional.of(LOCKED_BLOOD);
		if (advancement.isPresent() && !hasAdvancement(player, advancement.get())) return Optional.of(LOCKED_ADVANCEMENT);
		if (rapport < rapportAtLeast) return Optional.of(LOCKED_RAPPORT);
		return Optional.empty();
	}

	private static boolean hasAdvancement(ServerPlayer player, ResourceLocation id) {
		AdvancementHolder holder = player.server.getAdvancements().get(id);
		return holder != null && player.getAdvancements().getOrStartProgress(holder).isDone();
	}
}
