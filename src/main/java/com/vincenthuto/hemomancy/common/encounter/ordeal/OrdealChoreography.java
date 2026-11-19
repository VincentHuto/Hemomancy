package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * One professor's part in their ordeal (2026-11-17): where their true self keeps watch from outside the ring and what
 * each of their interventions does. {@link OrdealInterventionsDriver} owns the rest for every professor alike: the
 * ramps and their lines, which move opens next and when, how many run at once. A choreography only places its
 * professor, marks a move's target when it starts, and runs the move tick by tick until it is over.
 */
public interface OrdealChoreography {
	/** Where the professor's true self appears when the ordeal begins. */
	Vec3 entrance(BlockPos stage);

	/**
	 * Places and turns the professor's true self this tick. {@code floor} is the top of the ring's plate at its centre.
	 * The professor never stands in the ring: out of reach, invulnerable, watching.
	 */
	void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now);

	/** The true self's gesture kind for {@code move}, or -1 when the professor has none for it. */
	default int gestureFor(String move) {
		return -1;
	}

	/** Where a move aims when it starts: a mark on the floor (at the plate's height). Defaults to the ring's centre. */
	default Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		return floor;
	}

	/**
	 * One tick of a running move; true when it is over. {@code active.age(now)} counts from its first wind-up tick and
	 * the move lands on {@code active.move.windupTicks()}.
	 */
	boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now);
}
