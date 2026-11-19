package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Beast Tamer's part in the Sanguine Hound's ordeal (2026-11-17): their true self walks the dark apron outside the
 * cage of the Empty Menagerie, body square to the ring and eyes on the fighter, and stops to work. Their lash, a living
 * strand of their own blood in here, reaches across the ring for a whipcrack that snares, to light a hoop of fire round
 * the fighter, and to goad the Hound into a frenzy; and from the dark beyond the bars they call the beasts they once
 * caged. The hits are {@link BeastTamerMoves}.
 */
public final class BeastTamerChoreography implements OrdealChoreography {
	public static final BeastTamerChoreography INSTANCE = new BeastTamerChoreography();
	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_beast_tamer", "sanguine_hound",
			"ordeal/beast_tamer", List.of(
					new Intervention("whipcrack", Ramp.OPENING, BeastTamerMoves.CRACK_WINDUP, 90),
					new Intervention("hoop_of_fire", Ramp.OPENING, BeastTamerMoves.HOOP_WINDUP, 130),
					new Intervention("goad", Ramp.PRESSED, BeastTamerMoves.GOAD_WINDUP, 150),
					new Intervention("beasts_in_the_dark", Ramp.DESPERATE, BeastTamerMoves.BEASTS_WINDUP, 170)),
			INSTANCE, OrdealHoundMoves.MOVES);
	/** They walk the apron outside the bars at this radius. */
	public static final double RADIUS = 13.5D;
	/** The drawn eyes above their feet, in blocks. */
	private static final double EYES = BeastTamerMoves.EYES * BeastTamerMoves.SCALE;
	/** How far in toward the ring a mark is kept, and how far their head turns off their squared body, tips, in degrees. */
	private static final double MARK_LIMIT = OrdealStage.RING_RADIUS - 1.0D;
	private static final float HEAD_TURN = 70.0F, HEAD_TILT = 35.0F;

	private BeastTamerChoreography() {
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return Walk.position(Vec3.atBottomCenterOf(stage).add(0.0D, 1.0D, 0.0D), 0.0D);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Walk.Pace pace = Walk.step(professor.getUUID(), floor, now);
		Vec3 at = Walk.position(floor, pace.angle);
		professor.setPos(at.x, at.y, at.z);
		// Squared to the ring as they walk, turned to their work while a move is under way.
		face(professor, now < pace.still && pace.aim != null ? pace.aim : floor, caster.getEyePosition());
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		Vec3 feet = new Vec3(caster.getX(), floor.y, caster.getZ());
		Vec3 mark = switch (move.id()) {
			case "hoop_of_fire" -> feet.add(caster.getDeltaMovement().x * BeastTamerMoves.HOOP_LEAD, 0.0D,
					caster.getDeltaMovement().z * BeastTamerMoves.HOOP_LEAD);
			case "goad" -> new Vec3(puppet.getX(), floor.y, puppet.getZ());
			default -> feet;
		};
		// As the client receives it, so the beasts' lanes and the drawn beasts agree.
		return new Vec3(BeastTamerMoves.inRing(mark, floor, MARK_LIMIT).toVector3f());
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int windup = active.move.windupTicks();
		int age = active.age(now);
		Vec3 feet = caster.position();
		Vec3 tamer = professor == null ? floor.add(RADIUS, -1.0D, 0.0D) : professor.position();
		if (age == 0) begin(chamber, active, professor, floor, now);
		switch (active.move.id()) {
			case "whipcrack" -> {
				Vec3 from = BeastTamerMoves.crackFrom(floor, tamer, active.mark, OrdealStage.RING_RADIUS);
				Vec3 dir = BeastTamerMoves.flat(active.mark.subtract(tamer));
				double length = BeastTamerMoves.crackLength(from, active.mark);
				if (age == 0) OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_CRACK, lane(length,
						BeastTamerMoves.CRACK_HALF_WIDTH), from, from.add(dir), windup);
				if (age < windup) {
					// The lash drawn back, a drip of blood off its tip over their shoulder.
					if (age % 3 == 0) chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), tamer.x,
							tamer.y + EYES + 0.8D, tamer.z, 2, 0.4D, 0.3D, 0.4D, 0.01D);
					return false;
				}
				if (age == windup) {
					OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_CRACK, lane(length,
							BeastTamerMoves.CRACK_HALF_WIDTH), from, from.add(dir), OrdealInterventionsDriver.BAR_REDRAW + 1);
					crack(chamber, from, dir, length);
				}
				int unroll = age - windup;
				if (!active.landed && unroll < BeastTamerMoves.CRACK_UNROLL && BeastTamerMoves.laneHits(from, dir,
						length * (unroll + 1) / BeastTamerMoves.CRACK_UNROLL, BeastTamerMoves.CRACK_HALF_WIDTH, feet)) {
					active.landed = true;
					// Snared: the lash wraps them and yanks them toward the Tamer's side of the ring.
					chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F,
							1.2F);
					OrdealKit.strike(chamber, caster, professor, BeastTamerMoves.CRACK_DAMAGE, tamer.subtract(feet),
							BeastTamerMoves.SNARE_PULL, BeastTamerMoves.SNARE_LIFT);
				}
				return unroll >= BeastTamerMoves.CRACK_UNROLL + 2;
			}
			case "hoop_of_fire" -> {
				Vec3 mark = active.mark;
				if (age == 0) OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_HOOP, new BossFootprint.Ring(
						BeastTamerMoves.HOOP_RADIUS, BeastTamerMoves.HOOP_RADIUS, BeastTamerMoves.HOOP_BAND, 1, 0, windup,
						BossFootprint.Height.JUMPABLE), mark, mark, windup);
				if (age < windup) {
					// Flame licking up round the hoop, more of it as it nears the light.
					if (age % 2 == 0) hoopFlames(chamber, mark, 4 + age / 4, 0.18F);
					return false;
				}
				if (age == windup) {
					OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_HOOP, new BossFootprint.Ring(
							BeastTamerMoves.HOOP_RADIUS - 0.05D, BeastTamerMoves.HOOP_RADIUS + 0.05D, BeastTamerMoves.HOOP_BAND, 1, 0, 6,
							BossFootprint.Height.JUMPABLE), mark, mark, 6);
					OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_HOOP, new BossFootprint.Disc(
							BossFootprint.Anchor.ORIGIN, BeastTamerMoves.HOOP_RADIUS - BeastTamerMoves.HOOP_BAND / 2.0D,
							BossFootprint.Height.STANDING), mark, mark, BeastTamerMoves.HOOP_BURN + 6);
					chamber.playSound(null, mark.x, mark.y, mark.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 3.0F, .6F);
					chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.5F, .8F);
					hoopFlames(chamber, mark, 36, 0.4F);
					CircusEffects.impact(chamber, mark.add(0, .4, 0), BeastTamerMoves.HOOP_RADIUS);
					if (BeastTamerMoves.hoopBandHits(mark, feet)) burn(chamber, caster, professor, BeastTamerMoves.HOOP_DAMAGE);
				}
				int burning = age - windup;
				if (burning > 0 && burning % 3 == 0) {
					double r = BeastTamerMoves.HOOP_RADIUS * 0.6D;
					chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.3F, 0.9F, 14), mark.x, mark.y + .3D,
							mark.z, 10, r, 0.15D, r, 0.03D);
					hoopFlames(chamber, mark, 6, 0.25F);
				}
				if (burning > 0 && burning % BeastTamerMoves.HOOP_BURN_EVERY == 0 && BeastTamerMoves.hoopInsideHits(mark, feet))
					burn(chamber, caster, professor, BeastTamerMoves.HOOP_BURN_DAMAGE);
				return burning >= BeastTamerMoves.HOOP_BURN;
			}
			case "goad" -> {
				Vec3 at = new Vec3(puppet.getX(), floor.y, puppet.getZ());
				if (age == 0) OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_GOAD, new BossFootprint.Disc(
						BossFootprint.Anchor.ORIGIN, 1.6D, BossFootprint.Height.STANDING), at, at, windup + 4);
				if (age < windup) {
					CircusEffects.gather(chamber, puppet.position().add(0, 2.2, 0), CircusEffects.CRIMSON, age / (float) windup);
					return false;
				}
				if (age == windup) {
					// The crack in the air over the Hound, and the Hound goes wild.
					Vec3 over = puppet.position().add(0.0D, 2.2D, 0.0D);
					chamber.playSound(null, over.x, over.y, over.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3.0F, 1.6F);
					chamber.playSound(null, over.x, over.y, over.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 2.5F, .7F);
					chamber.playSound(null, puppet.getX(), puppet.getY(), puppet.getZ(), SoundEvents.WOLF_GROWL, SoundSource.HOSTILE,
							3.0F, .55F);
					CircusEffects.impact(chamber, over, 1.6D);
					chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), puppet.getX(), puppet.getY() + 0.8D,
							puppet.getZ(), 30, 0.6D, 0.4D, 0.6D, 0.08D);
					OrdealHoundMoves.goad(puppet.getUUID(), now + BeastTamerMoves.FRENZY_TICKS, now);
				}
				return age >= windup + 4;
			}
			case "beasts_in_the_dark" -> {
				Vec3 mark = active.mark;
				if (age == 0) {
					for (int i = 0; i < BeastTamerMoves.BEASTS; i++)
						OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_BEAST, lane(BeastTamerMoves.BEAST_FROM
								+ BeastTamerMoves.BEAST_PAST, BeastTamerMoves.BEAST_HALF_WIDTH), BeastTamerMoves.beastFrom(mark, i),
								BeastTamerMoves.beastFrom(mark, i).add(BeastTamerMoves.beastDir(mark, i)), BeastTamerMoves.beastLands(i));
				}
				for (int i = 0; i < BeastTamerMoves.BEASTS; i++) {
					int lands = BeastTamerMoves.beastLands(i), since = age - lands;
					Vec3 from = BeastTamerMoves.beastFrom(mark, i), dir = BeastTamerMoves.beastDir(mark, i);
					if (since < 0) {
						// Eyes in the dark past the bars, a pair to a beast, brighter as it gathers to spring.
						if (age % 3 == i % 3) eyes(chamber, from, dir, age / (float) lands);
						continue;
					}
					if (since == 0) {
						active.landed = false;
						OrdealKit.hazard(chamber, professor, BeastTamerMoves.KIND_BEAST, lane(BeastTamerMoves.BEAST_FROM
								+ BeastTamerMoves.BEAST_PAST, BeastTamerMoves.BEAST_HALF_WIDTH), from, from.add(dir),
								OrdealInterventionsDriver.BAR_REDRAW + 1);
						chamber.playSound(null, from.x, from.y, from.z, SoundEvents.WOLF_GROWL, SoundSource.HOSTILE, 3.5F, .45F);
						chamber.playSound(null, from.x, from.y, from.z, SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
								SoundSource.HOSTILE, 2.0F, 1.3F);
					}
					if (since >= 0 && since <= BeastTamerMoves.BEAST_RUN) {
						Vec3 head = BeastTamerMoves.beastAt(mark, i, since).add(0.0D, 1.0D, 0.0D);
						chamber.sendParticles(HemoParticleData.bloodClaw(CircusEffects.CRIMSON), head.x, head.y, head.z, 2, 0.3D,
								0.3D, 0.3D, 0.0D);
						if (!active.landed && BeastTamerMoves.beastHits(mark, i, since, feet)) {
							active.landed = true;
							chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE,
									3.0F, .9F);
							OrdealKit.strike(chamber, caster, professor, BeastTamerMoves.BEAST_DAMAGE, dir, .9D, .4D);
						}
						if (since == BeastTamerMoves.BEAST_RUN) {
							Vec3 end = BeastTamerMoves.beastAt(mark, i, since);
							CircusEffects.impact(chamber, end.add(0, .4, 0), 1.0D);
							CircusEffects.dust(chamber, end, 12);
						}
					}
				}
				return age >= BeastTamerMoves.beastLands(BeastTamerMoves.BEASTS - 1) + BeastTamerMoves.BEAST_RUN + 2;
			}
			default -> {
				return true;
			}
		}
	}

	/** A move's first tick: the gesture on every client, the breath they draw for it, and their walk held until it is done. */
	private static void begin(ServerLevel chamber, OrdealInterventionsDriver.Active active, Entity professor, Vec3 floor,
			long now) {
		if (!(professor instanceof CircusPerformerEntity tamer)) return;
		int gesture = switch (active.move.id()) {
			case "whipcrack" -> BeastTamerMoves.GESTURE_CRACK;
			case "hoop_of_fire" -> BeastTamerMoves.GESTURE_HOOP;
			case "goad" -> BeastTamerMoves.GESTURE_GOAD;
			default -> BeastTamerMoves.GESTURE_BEASTS;
		};
		tamer.gesture(gesture, active.mark);
		int windup = active.move.windupTicks();
		int length = switch (active.move.id()) {
			case "hoop_of_fire" -> windup + BeastTamerMoves.HOOP_BURN;
			case "beasts_in_the_dark" -> BeastTamerMoves.beastLands(BeastTamerMoves.BEASTS - 1) + BeastTamerMoves.BEAST_RUN
					+ BeastTamerMoves.RECOVER;
			default -> windup + BeastTamerMoves.HELD + BeastTamerMoves.RECOVER;
		};
		Walk.hold(tamer.getUUID(), now + length, gesture == BeastTamerMoves.GESTURE_BEASTS ? floor : active.mark);
		chamber.playSound(null, tamer.getX(), tamer.getY() + EYES, tamer.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 2.5F, gesture == BeastTamerMoves.GESTURE_BEASTS ? .6F : .95F);
	}

	/** The crack: the lash laid out down its lane, a burst of blood along it and the sound of it breaking the air. */
	private static void crack(ServerLevel chamber, Vec3 from, Vec3 dir, double length) {
		for (double d = 0.0D; d <= length; d += 0.6D) {
			Vec3 at = from.add(dir.scale(d));
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), at.x, at.y + .35D, at.z, 1, .05D, .05D, .05D, 0.0D);
			if ((int) (d / 0.6D) % 4 == 0)
				chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), at.x, at.y + .3D, at.z, 2, .1D, .1D, .1D, .03D);
		}
		Vec3 tip = from.add(dir.scale(length));
		CircusEffects.impact(chamber, tip.add(0, .4, 0), 0.8D);
		chamber.playSound(null, tip.x, tip.y, tip.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3.0F, 1.7F);
		chamber.playSound(null, tip.x, tip.y, tip.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 2.5F, .8F);
		chamber.playSound(null, from.x, from.y, from.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.0F, 1.4F);
	}

	/** Flame round the hoop's band, rising. */
	private static void hoopFlames(ServerLevel chamber, Vec3 mark, int points, float rise) {
		for (int i = 0; i < points; i++) {
			double a = Math.PI * 2.0D * (i + (chamber.getGameTime() % 7) / 7.0D) / points;
			double x = mark.x + Math.cos(a) * BeastTamerMoves.HOOP_RADIUS, z = mark.z + Math.sin(a) * BeastTamerMoves.HOOP_RADIUS;
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.34F, 0.9F, 14), x, mark.y + .15D, z, 1, .08D,
					rise, .08D, 0.02D);
			if (i % 3 == 0) chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, 0.22F, 1.0F, 10), x,
					mark.y + .1D, z, 1, .05D, rise * .5D, .05D, 0.01D);
		}
	}

	/** A pair of eyes in the dark where a beast waits, lit brighter as it gathers. */
	private static void eyes(ServerLevel chamber, Vec3 from, Vec3 dir, float charge) {
		Vec3 side = new Vec3(-dir.z, 0.0D, dir.x).scale(0.22D);
		Vec3 at = from.add(0.0D, 1.1D, 0.0D);
		for (Vec3 eye : new Vec3[]{at.add(side), at.subtract(side)})
			chamber.sendParticles(charge > .6F ? HemoParticleData.hitGlow(CircusEffects.CRIMSON) : HemoParticleData.glow(CircusEffects.CRIMSON),
					eye.x, eye.y, eye.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	private static void burn(ServerLevel chamber, ServerPlayer caster, Entity professor, float damage) {
		OrdealKit.strike(chamber, caster, professor, damage, Vec3.ZERO, 0.0D, 0.0D);
		caster.setRemainingFireTicks(Math.max(caster.getRemainingFireTicks(), BeastTamerMoves.HOOP_FIRE_TICKS));
	}

	private static BossFootprint.Lane lane(double length, double halfWidth) {
		return new BossFootprint.Lane(length, halfWidth, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING);
	}

	/**
	 * Squares the body toward {@code toward} while the head follows the fighter, turning at most {@link #HEAD_TURN}
	 * degrees off the body and tipping at most {@link #HEAD_TILT}. The eyes are the drawn true self's.
	 */
	private static void face(Entity professor, Vec3 toward, Vec3 fighter) {
		Vec3 eye = professor.position().add(0.0D, EYES, 0.0D);
		float body = OrdealKit.yaw(eye, toward);
		float head = body + Math.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - body), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(body);
		professor.setYBodyRot(body);
		professor.setYHeadRot(head);
		professor.setXRot(Math.clamp(pitch, -HEAD_TILT, HEAD_TILT));
	}

	/**
	 * Their walk round the apron, session state: an angle that advances at a strolling pace that slows to a standstill
	 * every twenty seconds, and holds while a move is under way. A holder of its own, so clearing it never loads the
	 * choreography (whose professor row would then be built before {@link OrdealProfessor}'s own table).
	 */
	public static final class Walk {
		private static final Map<UUID, Pace> PACES = new HashMap<>();
		/** A circuit every 80 seconds on average; the pace swells and falls to nothing over 20 seconds. */
		private static final double PERIOD = 1600.0D, SWAY = 400.0D;
		/** A pace not stepped for this long belongs to an ordeal that has ended. */
		private static final long STALE = 400L;

		private Walk() {
		}

		static final class Pace {
			double angle;
			long last = Long.MIN_VALUE;
			long still;
			Vec3 aim;
		}

		static Pace step(UUID professor, Vec3 floor, long now) {
			PACES.entrySet().removeIf(entry -> !entry.getKey().equals(professor) && now - entry.getValue().last > STALE);
			Pace pace = PACES.computeIfAbsent(professor, ignored -> new Pace());
			if (pace.last != Long.MIN_VALUE && now >= pace.still)
				pace.angle += Math.PI * 2.0D / PERIOD * (1.0D - Math.cos(now * Math.PI * 2.0D / SWAY));
			pace.last = now;
			return pace;
		}

		static void hold(UUID professor, long until, Vec3 aim) {
			Pace pace = PACES.computeIfAbsent(professor, ignored -> new Pace());
			pace.still = Math.max(pace.still, until);
			pace.aim = aim;
		}

		/** Their feet on the apron, a block below the ring's plate. */
		static Vec3 position(Vec3 floor, double angle) {
			return new Vec3(floor.x + Math.cos(angle) * RADIUS, floor.y - 1.0D, floor.z + Math.sin(angle) * RADIUS);
		}

		public static void clearSessionState() {
			PACES.clear();
		}
	}
}
