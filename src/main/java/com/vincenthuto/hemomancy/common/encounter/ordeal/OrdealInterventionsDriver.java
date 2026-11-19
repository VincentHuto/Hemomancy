package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Runs a professor's interventions from outside the ring (2026-11-17), the same for every professor: keeps their true
 * self where their {@link OrdealChoreography} says, opens up and quickens their moves as the puppet weakens (speaking
 * at each ramp), and runs each move through the choreography until it is over. Every footprint is drawn boldly on the
 * floor by the client's ordeal pass ({@link OrdealKit#hazard}).
 */
public final class OrdealInterventionsDriver {
	private static final Map<UUID, State> STATES = new HashMap<>();
	/**
	 * The Strongman's footprints among the ground hazards (his block in {@link OrdealHazardKinds}), one kind per move so
	 * the client's ordeal pass knows what it is drawing.
	 */
	public static final int KIND_WEIGHT = 4001, KIND_HEAVE = 4002, KIND_BAR = 4003, KIND_HOLD = 4004;
	/** The Hulk's own moves in the ordeal ({@link OrdealHulkMoves}), drawn by the same pass. */
	public static final int KIND_HAMMER = 4005, KIND_CHARGE = 4006, KIND_GRAB = 4007, KIND_STOMP = 4008;
	/** How often the sweeping bar is redrawn where it stands, in ticks. */
	public static final int BAR_REDRAW = 3;
	/** Every Strongman move's wind-up, for the client's telegraph pulse. */
	public static final int WINDUP = CircusFacultyEntity.GESTURE_IMPACT;

	private OrdealInterventionsDriver() {
	}

	private static final class State {
		Ramp ramp = Ramp.OPENING;
		long nextAt;
		final List<Active> active = new ArrayList<>();
	}

	/** One move under way: which, when its wind-up began, where it aims, and whether it has landed on the fighter. */
	public static final class Active {
		public final Intervention move;
		public final long startedAt;
		public final Vec3 mark;
		public boolean landed;

		/** Public so the Ringmaster's finale can run one professor's intervention through its echo (FinaleEchoCue). */
		public Active(Intervention move, long startedAt, Vec3 mark) {
			this.move = move;
			this.startedAt = startedAt;
			this.mark = mark;
		}

		/** Ticks since the wind-up began; the move lands at {@code move.windupTicks()}. */
		public int age(long now) {
			return (int) (now - startedAt);
		}
	}

	static void tick(ServerLevel chamber, ServerPlayer caster, OrdealDirector.Ordeal ordeal, Mob puppet) {
		State state = STATES.computeIfAbsent(caster.getUUID(), ignored -> {
			State fresh = new State();
			fresh.nextAt = chamber.getGameTime() + 60;
			return fresh;
		});
		long now = chamber.getGameTime();
		OrdealChoreography choreography = ordeal.professor().choreography();
		// The top of the ring's plate at its centre: the stage block is the ring's bed, the plate is laid on it.
		Vec3 floor = new Vec3(ordeal.stage().getX() + 0.5D, ordeal.stage().getY() + 1.0D, ordeal.stage().getZ() + 0.5D);
		Entity professor = ordeal.professorEntity == null ? null : chamber.getEntity(ordeal.professorEntity);
		if (professor != null) choreography.present(chamber, professor, floor, caster, now);
		Ramp ramp = OrdealRules.ramp(puppet.getHealth() / puppet.getMaxHealth());
		if (ramp != state.ramp) {
			state.ramp = ramp;
			OrdealDirector.rampLine(caster, ordeal.professor(), ramp);
		}
		if (now >= state.nextAt && state.active.size() < OrdealRules.concurrent(ramp)) {
			Set<String> running = new HashSet<>();
			for (Active active : state.active) running.add(active.move.id());
			for (int attempt = 0; attempt < 4; attempt++) {
				Intervention move = OrdealRules.next(ordeal.professor(), ramp, now / 20 + attempt, caster.getUUID().getLeastSignificantBits());
				if (running.contains(move.id())) continue;
				state.active.add(new Active(move, now, choreography.mark(move, caster, puppet, floor)));
				state.nextAt = now + (long) (move.cooldownTicks() * OrdealRules.cadence(ramp) / OrdealRules.concurrent(ramp));
				break;
			}
		}
		state.active.removeIf(active -> choreography.run(chamber, caster, puppet, professor, floor, active, now));
	}

	static void forget(UUID caster) {
		STATES.remove(caster);
	}

	public static void clearSessionState() {
		STATES.clear();
	}
}
