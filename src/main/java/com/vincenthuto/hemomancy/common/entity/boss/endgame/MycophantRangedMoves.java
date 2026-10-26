package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/** The vents and the siphon at range, and the brood: the Vent Bloom, the Siphon Spit, the Spore Mortar and the Nursing. */
final class MycophantRangedMoves {
	/** The mortar's landing points stay where they were read at commit, so the rings and the eggs agree. */
	private static final Map<MycophantEntity, Volley> VOLLEYS = new WeakHashMap<>();
	private static final double[][] VENTS = { { -2.3D, 0.8D, -3.0D }, { 2.3D, 0.8D, -3.0D } };

	private MycophantRangedMoves() {
	}

	/**
	 * One tick of the ranged and brood moves. {@code tick} counts from the start of the move (the wind-up), as the controller's attack clock
	 * does; the active stage starts at {@code MycophantControllerRules.timeline(attack, phase, variant).windup()}.
	 */
	static void tick(ServerLevel level, MycophantEntity boss, MycophantController controller, MycophantAttack attack, int tick,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, boss.getPhase(), controller.variant());
		int active = tick - timeline.windup();
		if (stage == BossMoveTimeline.Stage.RECOVERY && active == timeline.active()) boss.setGlow(0.6F);
		switch (attack) {
			case VENT_BLOOM -> ventBloom(level, boss, tick, stage, commit);
			case SIPHON_SPIT -> spit(level, boss, tick, active, stage, quarry);
			case SPORE_MORTAR -> mortar(level, boss, tick, active, timeline, stage, commit, quarry);
			case NURSING -> nursing(level, boss, controller, tick, active, timeline, stage, commit);
			default -> { }
		}
	}

	// ------------------------------------------------------------------ the Vent Bloom

	private static void ventBloom(ServerLevel level, MycophantEntity boss, int tick, BossMoveTimeline.Stage stage, boolean commit) {
		Vec3 cloud = boss.bodyPoint(0.0D, 0.4D, -4.5D);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.7F + tick / 24.0F);
			if (tick % 3 == 0) {
				for (double[] vent : VENTS) {
					Vec3 at = boss.bodyPoint(vent[0], vent[1] + 0.6D, vent[2]);
					BossTelegraphs.glow(level, at, MycophantMoves.YOLK, 1 + tick / 5, 0.25D);
					BossTelegraphs.spores(level, at, MycophantMoves.SPORE, 2, 0.2D, 0.02D);
				}
			}
			if (tick % 4 == 0) BossTelegraphs.ring(level, cloud, MycophantHazardRules.CLOUD_RADIUS, MycophantMoves.SPORE, 22);
			return;
		}
		if (!commit) return;
		MycophantHazardEntity.cloud(level, cloud, boss);
		for (double[] vent : VENTS)
			BossTelegraphs.spores(level, boss.bodyPoint(vent[0], vent[1] + 0.6D, vent[2]), MycophantMoves.SPORE, 24, 0.8D, 0.08D);
		BossTelegraphs.spores(level, cloud.add(0.0D, 0.6D, 0.0D), MycophantMoves.SPORE, 30, 1.6D, 0.03D);
		BossTelegraphs.ring(level, cloud, MycophantHazardRules.CLOUD_RADIUS, MycophantMoves.YOLK, 32);
		level.playSound(null, boss.blockPosition(), SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.HOSTILE, 1.8F, 0.5F);
		level.playSound(null, boss.blockPosition(), SoundEvents.FUNGUS_BREAK, SoundSource.HOSTILE, 1.4F, 0.6F);
	}

	// ------------------------------------------------------------------ the Siphon Spit

	private static void spit(ServerLevel level, MycophantEntity boss, int tick, int active, BossMoveTimeline.Stage stage,
			@Nullable LivingEntity quarry) {
		Vec3 mouth = siphonMouth(boss);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.7F + tick / 30.0F);
			if (tick % 2 == 0) BossTelegraphs.glow(level, mouth, MycophantMoves.YOLK, 1 + tick / 6, 0.2D);
			if (tick == 4) level.playSound(null, boss.blockPosition(), SoundEvents.HONEY_DRINK, SoundSource.HOSTILE, 1.6F, 0.5F);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		int[] releases = MycophantStrikeRules.globTicks(boss.getPhase());
		for (int i = 0; i < releases.length; i++) {
			if (releases[i] != active) continue;
			// Each glob comes down on the quarry as they stand now, or on its own seeded spot around them.
			Vec3 target = aimPoint(boss, quarry);
			double[] at = MycophantStrikeRules.spread(target.x, target.z, releases.length, moveSeed(level, boss, tick), 1.5D, 3.5D)[i];
			Vec3 landing = new Vec3(at[0], target.y, at[1]);
			int flight = MycophantStrikeRules.mortarFlightTicks(horizontal(mouth, landing));
			MycophantGlobEntity.launch(level, boss, mouth, landing, MycophantGlobEntity.Kind.GLOB, false, flight);
			BossTelegraphs.ring(level, landing, MycophantHazardRules.POOL_RADIUS, MycophantMoves.AMBER, 14);
			BossTelegraphs.bloodCells(level, mouth, MycophantMoves.AMBER, 6, 0.2D, 0.08D);
			BossTelegraphs.glow(level, mouth, MycophantMoves.YOLK, 4, 0.25D);
			level.playSound(null, mouth.x, mouth.y, mouth.z, SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 1.6F, 0.45F);
		}
	}

	// ------------------------------------------------------------------ the Spore Mortar

	private static void mortar(ServerLevel level, MycophantEntity boss, int tick, int active, BossMoveTimeline timeline,
			BossMoveTimeline.Stage stage, boolean commit, @Nullable LivingEntity quarry) {
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			boss.setGlow(0.7F + tick / 28.0F);
			// the abdomen pumps: the vents beat brighter on every fourth tick
			if (tick % 4 == 0) {
				for (double[] vent : VENTS) {
					Vec3 at = boss.bodyPoint(vent[0] * 0.5D, 2.4D, vent[2]);
					BossTelegraphs.glow(level, at, MycophantMoves.AMBER, 2 + tick / 6, 0.3D);
					BossTelegraphs.spores(level, at, MycophantMoves.SPORE, 3, 0.3D, 0.03D);
				}
				level.playSound(null, boss.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 1.2F, 0.4F);
			}
			return;
		}
		long start = level.getGameTime() - tick;
		if (commit) {
			VOLLEYS.put(boss, Volley.plan(boss, quarry, start, moveSeed(level, boss, tick), timeline.active()));
			level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.6F, 0.6F);
		}
		Volley volley = VOLLEYS.get(boss);
		if (volley == null || volley.start() != start) return;
		for (int i = 0; i < volley.landings().length; i++) {
			Vec3 landing = volley.landings()[i];
			int launch = volley.launches()[i];
			if (active == launch) {
				Vec3 from = volley.vents()[i];
				MycophantGlobEntity.launch(level, boss, from, landing, MycophantGlobEntity.Kind.EGG, i == volley.tumor(), volley.flights()[i]);
				BossTelegraphs.spores(level, from, MycophantMoves.SPORE, 10, 0.4D, 0.06D);
				BossTelegraphs.glow(level, from, MycophantMoves.YOLK, 4, 0.3D);
				level.playSound(null, from.x, from.y, from.z, SoundEvents.SNIFFER_EGG_PLOP, SoundSource.HOSTILE, 1.6F, 0.5F);
			}
			// the landing rings go up at commit and stay until the egg comes down (every flight is at least 14 ticks)
			int impact = launch + volley.flights()[i];
			if (active < impact && active % 3 == 0) {
				BossTelegraphs.ring(level, landing, MycophantHazardRules.EGG_RADIUS, MycophantMoves.YOLK, 26);
				if (impact - active <= 6) BossTelegraphs.glow(level, landing.add(0.0D, 0.1D, 0.0D), MycophantMoves.AMBER, 3, 0.4D);
			}
		}
		if (stage == BossMoveTimeline.Stage.RECOVERY && active >= volley.lastImpact()) VOLLEYS.remove(boss);
	}

	/** One mortar volley as planned at commit: where each egg lands, when it leaves which vent, and which one roots. */
	private record Volley(long start, Vec3[] landings, Vec3[] vents, int[] launches, int[] flights, int tumor) {
		static Volley plan(MycophantEntity boss, @Nullable LivingEntity quarry, long start, long seed, int active) {
			int count = mortarEggs(boss.getPhase(), boss.getHealth(), boss.getMaxHealth());
			Vec3 target = aimPoint(boss, quarry);
			double[][] spread = MycophantStrikeRules.spread(target.x, target.z, count, seed, 2.0D, 4.5D);
			Vec3[] landings = new Vec3[count];
			Vec3[] vents = new Vec3[count];
			int[] launches = new int[count];
			int[] flights = new int[count];
			for (int i = 0; i < count; i++) {
				double[] vent = VENTS[i % 2];
				landings[i] = new Vec3(spread[i][0], target.y, spread[i][1]);
				vents[i] = boss.bodyPoint(vent[0] * 0.5D, 2.4D, vent[2]);
				launches[i] = mortarLaunch(i, count, active);
				flights[i] = MycophantStrikeRules.mortarFlightTicks(horizontal(vents[i], landings[i]));
			}
			// one egg per volley, on the ring rather than on the quarry, lands whole as a brood tumor
			boolean roots = MycophantBroodRules.mayPlantTumor(boss.getEncounterOwner() != null, boss.broodTumors().standing());
			return new Volley(start, landings, vents, launches, flights, roots && count > 1 ? count - 1 : -1);
		}

		int lastImpact() {
			int last = 0;
			for (int i = 0; i < launches.length; i++) last = Math.max(last, launches[i] + flights[i]);
			return last;
		}
	}

	/** Eggs in a volley: three, four in phase 2, five once she is below 35% health. */
	static int mortarEggs(int phase, float health, float maxHealth) {
		if (health < maxHealth * 0.35F) return 5;
		return phase >= 2 ? 4 : 3;
	}

	/** The active-relative tick egg {@code index} of {@code count} leaves her, spread evenly across the active stage. */
	static int mortarLaunch(int index, int count, int active) {
		if (count <= 1) return 0;
		return index * ((active - 1) / (count - 1));
	}

	// ------------------------------------------------------------------ the Nursing

	private static void nursing(ServerLevel level, MycophantEntity boss, MycophantController controller, int tick, int active,
			BossMoveTimeline timeline, BossMoveTimeline.Stage stage, boolean commit) {
		Vec3 mouth = siphonMouth(boss);
		if (stage == BossMoveTimeline.Stage.WINDUP) {
			FunglingEntity young = controller.nursingTarget();
			if (young == null) return;
			// she turns from the quarry to her young (the controller turns her toward the quarry first)
			controller.face(young.position(), MycophantControllerRules.turnRateDegrees(boss.getPhase(), true) * 2.0F);
			if (tick % 3 == 0) BossTelegraphs.line(level, boss.bodyPoint(0.0D, 0.0D, 2.0D), young.position(), MycophantMoves.YOLK);
			BossTelegraphs.glow(level, mouth, MycophantMoves.YOLK, 1, 0.15D);
			if (tick == 2) level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.4F, 1.5F);
			return;
		}
		if (stage != BossMoveTimeline.Stage.ACTIVE) return;
		if (commit) {
			FunglingEntity young = controller.nursingTarget();
			if (young == null) {
				controller.skipToRecovery();
				return;
			}
			boss.nurse(young);
			BossTelegraphs.glow(level, young.position().add(0.0D, 0.4D, 0.0D), MycophantMoves.AMBER, 6, 0.3D);
			level.playSound(null, young.blockPosition(), SoundEvents.HONEY_DRINK, SoundSource.HOSTILE, 1.4F, 0.6F);
			return;
		}
		Entity latched = boss.getSiphonTargetId() < 0 ? null : level.getEntity(boss.getSiphonTargetId());
		if (!(latched instanceof FunglingEntity young) || !young.isAlive()) {
			boss.stopNursing();
			controller.skipToRecovery();
			return;
		}
		if (controller.nursingDamageTaken() >= MycophantBroodRules.NURSE_BREAK_DAMAGE) {
			boss.stopNursing();
			controller.skipToRecovery();
			BossTelegraphs.bloodCells(level, mouth, MycophantMoves.CRIMSON, 14, 0.4D, 0.12D);
			BossTelegraphs.spores(level, young.position().add(0.0D, 0.4D, 0.0D), MycophantMoves.SPORE, 10, 0.4D, 0.04D);
			level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_HURT.get(), SoundSource.HOSTILE, 1.6F, 0.8F);
			return;
		}
		// it sits still to be fed, which makes it a target
		young.getNavigation().stop();
		young.setDeltaMovement(0.0D, young.getDeltaMovement().y, 0.0D);
		controller.face(young.position(), MycophantControllerRules.turnRateDegrees(boss.getPhase(), false));
		float progress = Math.min(1.0F, active / (float) timeline.active());
		boss.setGlow(0.8F + progress * 0.5F);
		// nectar runs down the siphon in beats, mouth to young
		Vec3 body = young.position().add(0.0D, young.getBbHeight() * 0.5D, 0.0D);
		double along = (active % 10) / 10.0D;
		BossTelegraphs.glow(level, mouth.lerp(body, along), MycophantMoves.YOLK, 2, 0.08D);
		if (active % 10 == 0) {
			BossTelegraphs.glow(level, body, MycophantMoves.AMBER, 2 + Math.round(progress * 6.0F), 0.3D * (1.0D + progress));
			level.playSound(null, young.blockPosition(), SoundEvents.HONEY_DRINK, SoundSource.HOSTILE, 0.8F, 0.5F + progress * 0.4F);
		}
		if (active < timeline.active() - 1) return;
		young.setGrown(true);
		boss.stopNursing();
		BossTelegraphs.spores(level, body, MycophantMoves.SPORE, 30, 0.9D, 0.06D);
		BossTelegraphs.glow(level, body, MycophantMoves.YOLK, 12, 0.6D);
		BossTelegraphs.ring(level, young.position(), 1.8D, MycophantMoves.AMBER, 18);
		level.playSound(null, young.blockPosition(), SoundEvents.SNIFFER_EGG_HATCH, SoundSource.HOSTILE, 1.6F, 0.5F);
		level.playSound(null, boss.blockPosition(), SoundInit.ENTITY_MYCOPHANT_AMBIENT.get(), SoundSource.HOSTILE, 1.6F, 1.2F);
	}

	// ------------------------------------------------------------------ shared

	private static Vec3 siphonMouth(MycophantEntity boss) {
		double[] at = MycophantVisualRules.siphonMouth();
		return boss.bodyPoint(at[0], at[1], at[2]);
	}

	/** Where a lob is aimed: the quarry's feet, or eight blocks ahead of her when she has none. */
	private static Vec3 aimPoint(MycophantEntity boss, @Nullable LivingEntity quarry) {
		return quarry != null ? quarry.position() : boss.position().add(boss.forward().scale(8.0D));
	}

	/** A seed fixed for one run of a move: her id and the game tick the move started. */
	private static long moveSeed(ServerLevel level, MycophantEntity boss, int tick) {
		return boss.getId() * 0x9E3779B97F4A7C15L ^ (level.getGameTime() - tick);
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		double dx = b.x - a.x;
		double dz = b.z - a.z;
		return Math.sqrt(dx * dx + dz * dz);
	}
}
