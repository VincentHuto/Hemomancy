package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * The four Saint gifts (Canon Memories) and the Saint each is earned from. A gift is woven and learned only by a player
 * who ended that Saint's trial (D-1, NQ-08-2): the record is written in {@code SaintTrialEntity.endTrial}.
 */
public final class CanonGifts {
	private static final Map<String, EnumSaintType> SAINT_OF = Map.of(
			"crimson_tithe", EnumSaintType.HEMORATH,
			"unclosing_eye", EnumSaintType.SERAPHAE,
			"bloom_of_rot", EnumSaintType.PUTRICIEL,
			"endless_hour", EnumSaintType.VELORUM);

	private CanonGifts() {
	}

	/** The Saint whose gift this is, or null for every other manipulation. */
	@Nullable
	public static EnumSaintType saintOf(@Nullable String manipulationName) {
		return manipulationName == null ? null : SAINT_OF.get(manipulationName);
	}

	@Nullable
	public static EnumSaintType saintOf(@Nullable BloodManipulation manipulation) {
		return manipulation == null ? null : saintOf(manipulation.getName());
	}

	/** How far a Saint's fight is looked for round a gift's caster. */
	private static final double FIGHT_REACH = 48.0D;

	/**
	 * The live Saint of {@code type} whose fight has {@code player} as a challenger, or null: a Saint answers its own
	 * gift only in its own trial (D-1 spec 2.6, NQ-08-11).
	 */
	@Nullable
	public static <T extends SaintTrialEntity> T fightOf(Player player, Class<T> type) {
		for (T saint : player.level().getEntitiesOfClass(type, player.getBoundingBox().inflate(FIGHT_REACH))) {
			if (!saint.isAlive() || saint.ending() || saint.hasTrialEnded() || saint.arena() == null) continue;
			if (saint.arena().challengers().contains(player)) return saint;
		}
		return null;
	}

	/** True for an ordinary manipulation, or for a gift whose Saint's trial this player has ended. */
	public static boolean trialAllows(Player player, @Nullable BloodManipulation manipulation) {
		EnumSaintType saint = saintOf(manipulation);
		return saint == null || HemoCapabilityAccess.getInitiatoryDegree(player).hasEndedSaintTrial(saint);
	}
}
