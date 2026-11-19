package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealInterventionsDriver;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealTrueSelves;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * His cue to a professor's echo (2026-11-18): one professor this player passed steps in out of his dark as their true
 * self, where their own ordeal has them stand, and runs their signature intervention at the fighter through their own
 * {@code OrdealChoreography}, with him as the puppet they work for; then fades. One echo at a time; it lands
 * {@link #STEP_IN} ticks after his cue plus its own wind-up.
 */
public final class FinaleEchoCue {
	/** Each professor's signature intervention: an id in their {@link OrdealProfessor} row. */
	public static final Map<String, String> SIGNATURE = Map.of(
			"circus_strongman", "dropped_weight",
			"circus_acrobat", "trapeze_swing",
			"circus_knife_thrower", "knife_fan",
			"circus_stilt_walker", "planted_stilt",
			"circus_beast_tamer", "whipcrack",
			"circus_fire_eater", "long_breath",
			"circus_understudy", "spotlight");
	/** Ticks the echo takes to step in out of the dark before its move begins. */
	public static final int STEP_IN = 24;
	/** An echo whose move never reports over is let go after this long. */
	public static final int LONGEST = 200;

	/** One echo on his stage. */
	public static final class Echo {
		final UUID entity;
		final OrdealProfessor professor;
		final Intervention move;
		final long calledAt;
		@Nullable
		OrdealInterventionsDriver.Active active;

		Echo(UUID entity, OrdealProfessor professor, Intervention move, long calledAt) {
			this.entity = entity;
			this.professor = professor;
			this.move = move;
			this.calledAt = calledAt;
		}

		public OrdealProfessor professor() {
			return professor;
		}
	}

	private FinaleEchoCue() {
	}

	/** The professor's signature intervention, if their row has it. */
	public static Optional<Intervention> signature(OrdealProfessor professor) {
		String id = SIGNATURE.get(professor.role());
		return professor.interventions().stream().filter(move -> move.id().equals(id)).findFirst();
	}

	/**
	 * Calls {@code professor}'s echo onto his stage: their true self raised where their choreography has them enter,
	 * translated to the copy ({@code floor} is the copy's ring floor), tagged as one of the finale's copies.
	 */
	@Nullable
	public static Echo call(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity ringmaster, OrdealProfessor professor,
			Vec3 floor) {
		Optional<Intervention> move = signature(professor);
		if (move.isEmpty()) return null;
		CircusPerformerEntity echo = OrdealTrueSelves.spawnProfessor(chamber, professor, player, stageUnder(floor));
		if (echo == null) return null;
		echo.addTag(RingmasterFinaleDirector.COPY_TAG);
		echo.setInvulnerable(true);
		// Out at their own station in his dark, as their stand-in stood, never on the ordeal's orbit: that orbit is
		// laid round its ring's centre, which here is his carousel.
		Vec3 station = FinaleEchoes.station(floor, OrdealProfessor.all().indexOf(professor));
		echo.moveTo(station.x, station.y, station.z, yawToward(station, floor), 0.0F);
		CircusEffects.vault(chamber, echo.position());
		chamber.playSound(null, echo.getX(), echo.getY(), echo.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE,
				2.5F, 0.8F);
		return new Echo(echo.getUUID(), professor, move.get(), chamber.getGameTime());
	}

	/** One tick of a called echo; true once it has performed and faded (or lost its body). */
	public static boolean tick(ServerLevel chamber, ServerPlayer player, CircusRingmasterEntity ringmaster, Echo echo, Vec3 floor) {
		Entity body = chamber.getEntity(echo.entity);
		long now = chamber.getGameTime();
		if (body == null || !body.isAlive()) return true;
		hold(body, FinaleEchoes.station(floor, OrdealProfessor.all().indexOf(echo.professor)), player.position());
		int age = (int) (now - echo.calledAt);
		if (age < STEP_IN) {
			// Stepping in out of his dark: the threads that work them drawn taut from above.
			if (age % 4 == 0) BossTelegraphs.tendril(chamber, body.position().add(0.0D, 6.0D, 0.0D), body.position().add(0.0D, 1.8D, 0.0D),
					CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, body.getId() * 37L + age);
			return false;
		}
		if (echo.active == null)
			echo.active = new OrdealInterventionsDriver.Active(echo.move, now,
					echo.professor.choreography().mark(echo.move, player, ringmaster, floor));
		boolean over = echo.professor.choreography().run(chamber, player, ringmaster, body, floor, echo.active, now)
				|| age > STEP_IN + LONGEST;
		if (over) dismiss(chamber, echo);
		return over;
	}

	/** Keeps a called echo still at its station, facing the player it performs at. */
	private static void hold(Entity body, Vec3 station, Vec3 toward) {
		float yaw = yawToward(station, toward);
		body.setPos(station.x, station.y, station.z);
		body.setDeltaMovement(Vec3.ZERO);
		body.setYRot(yaw);
		body.setYHeadRot(yaw);
		if (body instanceof net.minecraft.world.entity.LivingEntity living) living.setYBodyRot(yaw);
	}

	private static float yawToward(Vec3 from, Vec3 to) {
		return (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
	}

	/** The echo fades back into his dark. */
	public static void dismiss(ServerLevel chamber, Echo echo) {
		Entity body = chamber.getEntity(echo.entity);
		if (body == null) return;
		CircusEffects.vault(chamber, body.position());
		BossTelegraphs.spores(chamber, body.position().add(0.0D, 1.5D, 0.0D), CircusEffects.BONE, 16, 0.6D, 0.04D);
		body.discard();
	}

	/** The ordeal stage block whose plate top is {@code floor}: the ordeals measure their floor one above their stage. */
	private static BlockPos stageUnder(Vec3 floor) {
		return BlockPos.containing(floor.x, floor.y - 1.0D, floor.z);
	}
}
