package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * A puppet's own moves in its ordeal (2026-11-17): the true self fights, not just its summon's ordinary attack. Run
 * by the director every tick of the ordeal after the professor's interventions; its session state is keyed by the
 * puppet and cleared with the rest of the ordeal's.
 */
public interface OrdealPuppetMoves {
	/** One tick of the fight; {@code floor} is the top of the ring's plate at its centre. */
	void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor);

	void forget(UUID puppet);

	void clearSessionState();

	/** Whether the puppet has the fighter held right now (a grab, a pin): the review scene leaves them be. */
	default boolean holding(UUID puppet, long now) {
		return false;
	}

	/** No moves of its own: the summon's ordinary attack carries the fight. */
	OrdealPuppetMoves NONE = new OrdealPuppetMoves() {
		@Override
		public void tick(ServerLevel chamber, ServerPlayer caster, Mob puppet, Vec3 floor) {
		}

		@Override
		public void forget(UUID puppet) {
		}

		@Override
		public void clearSessionState() {
		}
	};
}
