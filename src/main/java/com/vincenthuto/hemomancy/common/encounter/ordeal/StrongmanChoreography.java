package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Strongman's part in the Gorebound Hulk's ordeal (the pilot, 2026-11-17): a waist-up colossus wading round his
 * Iron Yard ({@link OrdealFlight}), body square to the ring, head on the fighter, whose spectral arms land a dropped
 * weight, a ground heave, a bar sweep and holding the line. The hits are {@link OrdealMoves}.
 */
public final class StrongmanChoreography implements OrdealChoreography {
	public static final StrongmanChoreography INSTANCE = new StrongmanChoreography();
	private static final int HEAVE_TICKS = (int) Math.ceil(OrdealMoves.HEAVE_START / OrdealMoves.HEAVE_SPEED);
	private static final int HOLD_TICKS = 60;
	/** The furthest his head tips toward the ring, and turns off his squared body, in degrees. */
	private static final float HEAD_TILT = 30.0F, HEAD_TURN = 60.0F;

	private StrongmanChoreography() {
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return OrdealFlight.position(stage, 0.0F);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Vec3 at = OrdealFlight.position(BlockPos.containing(floor.x, floor.y - 1.0D, floor.z), now);
		professor.setPos(at.x, at.y, at.z);
		face(professor, floor, caster.getEyePosition());
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		if (!move.id().equals("dropped_weight")) return floor;
		Vec3 ahead = caster.position().add(caster.getDeltaMovement().scale(8.0D));
		return new Vec3(ahead.x, floor.y, ahead.z);
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int windup = active.move.windupTicks();
		int age = active.age(now);
		float charge = Math.min(1.0F, age / (float) windup);
		Vec3 feet = caster.position();
		if (age == 0 && professor instanceof CircusFacultyEntity faculty) {
			faculty.gesture(gesture(active.move.id()), reach(active, floor, feet));
			// A low groan of strain as he gathers the blow.
			chamber.playSound(null, faculty.getX(), faculty.getY() + 6.0D, faculty.getZ(),
					SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE, 3.0F, .55F);
		}
		if (age == windup) landed(chamber, active.move.id(), active.mark, floor);
		switch (active.move.id()) {
			case "dropped_weight" -> {
				if (age == 0)
					OrdealKit.hazard(chamber, professor, OrdealInterventionsDriver.KIND_WEIGHT, new BossFootprint.Disc(
							BossFootprint.Anchor.ORIGIN, OrdealMoves.WEIGHT_RADIUS, BossFootprint.Height.STANDING), active.mark,
							active.mark, windup + 6);
				if (age < windup) {
					// The weight coming down out of the dark: a falling knot of iron light over the mark.
					double drop = 14.0D * (1.0F - charge);
					chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), active.mark.x, active.mark.y + 1.0D + drop,
							active.mark.z, 6, 0.3D, 0.3D, 0.3D, 0.0D);
					return false;
				}
				CircusEffects.impact(chamber, active.mark.add(0, 1, 0), OrdealMoves.WEIGHT_RADIUS + 0.5D);
				CircusEffects.impact(chamber, active.mark.add(0, 0.4, 0), OrdealMoves.WEIGHT_RADIUS);
				if (OrdealMoves.droppedWeightHits(active.mark, feet)) strike(chamber, caster, professor, OrdealMoves.WEIGHT_DAMAGE);
				return true;
			}
			case "ground_heave" -> {
				if (age == 0)
					OrdealKit.hazard(chamber, professor, OrdealInterventionsDriver.KIND_HEAVE, new BossFootprint.Ring(
							OrdealMoves.HEAVE_START, OrdealMoves.HEAVE_START, 0.8D, 1, 0, windup, BossFootprint.Height.JUMPABLE),
							floor, floor, windup);
				if (age < windup) return false;
				if (age == windup) {
					OrdealKit.hazard(chamber, professor, OrdealInterventionsDriver.KIND_HEAVE, new BossFootprint.Ring(
							OrdealMoves.HEAVE_START, 0.0D, 2.0D * OrdealMoves.HEAVE_WIDTH, 1, 0, HEAVE_TICKS,
							BossFootprint.Height.JUMPABLE), floor, floor, HEAVE_TICKS + 2);
				}
				double wave = OrdealMoves.heaveRadius(age - windup);
				if (!active.landed && OrdealMoves.groundHeaveHits(floor, wave, feet)) {
					active.landed = true;
					strike(chamber, caster, professor, OrdealMoves.HEAVE_DAMAGE);
				}
				return age - windup >= HEAVE_TICKS;
			}
			case "bar_sweep" -> {
				double along = OrdealMoves.sweepOffset(Math.max(0, age - windup));
				// The bar redrawn where it stands every few ticks, so it reads as one bar crossing the ring.
				if (age == 0) bar(chamber, professor, floor, along, windup);
				else if (age >= windup && (age - windup) % OrdealInterventionsDriver.BAR_REDRAW == 0)
					bar(chamber, professor, floor, along, OrdealInterventionsDriver.BAR_REDRAW + 1);
				if (age < windup) return false;
				if (!active.landed && OrdealMoves.barSweepHits(floor, along, feet)) {
					active.landed = true;
					strike(chamber, caster, professor, OrdealMoves.SWEEP_DAMAGE);
				}
				return age - windup >= OrdealMoves.SWEEP_TICKS;
			}
			case "hold_the_line" -> {
				if (age == 0) {
					// The floor tilting toward the puppet: a wide lane from the Hulk out through where the fighter stands.
					Vec3 from = new Vec3(puppet.getX(), floor.y, puppet.getZ()), to = new Vec3(feet.x, floor.y, feet.z);
					double reach = Math.max(3.0D, from.distanceTo(to) + 2.0D);
					OrdealKit.hazard(chamber, professor, OrdealInterventionsDriver.KIND_HOLD, new BossFootprint.Lane(reach, 1.5D, 0,
							0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), from, to, windup + HOLD_TICKS);
				}
				if (age < windup) {
					CircusEffects.gather(chamber, puppet.position().add(0, 1, 0), CircusEffects.CRIMSON, charge);
					return false;
				}
				Vec3 pull = OrdealMoves.holdTheLinePull(feet, puppet.position());
				caster.push(pull.x, 0.0D, pull.z);
				caster.hurtMarked = true;
				return age - windup >= HOLD_TICKS;
			}
			default -> {
				return true;
			}
		}
	}

	/**
	 * The blow landing: a deep thud through the floor where it falls and a burst of blood and bone off the plate (the
	 * heave all round the rim it starts from). The camera's jolt is the client's, read from the same gesture.
	 */
	private static void landed(ServerLevel chamber, String move, Vec3 mark, Vec3 floor) {
		switch (move) {
			case "dropped_weight" -> {
				chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 4.0F, .5F);
				chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .6F);
				CircusEffects.dust(chamber, mark.add(0, .3, 0), 40);
			}
			case "ground_heave" -> {
				chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 5.0F, .4F);
				for (int angle = 0; angle < 360; angle += 30) {
					double a = Math.toRadians(angle);
					Vec3 at = floor.add(Math.cos(a) * OrdealMoves.HEAVE_START, .4D, Math.sin(a) * OrdealMoves.HEAVE_START);
					CircusEffects.impact(chamber, at, 1.0D);
					CircusEffects.dust(chamber, at, 8);
				}
			}
			case "bar_sweep" -> chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.RITE_STAFF_IMPACT.get(),
					SoundSource.HOSTILE, 3.0F, .45F);
			case "hold_the_line" -> chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.RITE_SUBJECT_THUD.get(),
					SoundSource.HOSTILE, 3.0F, .7F);
			default -> {
			}
		}
	}

	/** The true self's gesture for a move: the weight a fist driven down, the heave both, the bar a sweep, the line a haul. */
	@Override
	public int gestureFor(String move) {
		return gesture(move);
	}

	private static int gesture(String move) {
		return switch (move) {
			case "dropped_weight" -> CircusFacultyEntity.GESTURE_DROP;
			case "ground_heave" -> CircusFacultyEntity.GESTURE_SLAM;
			case "bar_sweep" -> CircusFacultyEntity.GESTURE_SWEEP;
			case "hold_the_line" -> CircusFacultyEntity.GESTURE_PULL;
			default -> -1;
		};
	}

	/** Where his gesture reaches: the weight's mark, the fighter for the line, else the ring's centre. */
	private static Vec3 reach(OrdealInterventionsDriver.Active active, Vec3 floor, Vec3 feet) {
		return switch (active.move.id()) {
			case "dropped_weight" -> active.mark;
			case "hold_the_line" -> feet;
			default -> floor;
		};
	}

	/** The bar across the ring at {@code along}: a lane the ring's width at knee height, laid along z. */
	private static void bar(ServerLevel chamber, Entity professor, Vec3 floor, double along, int life) {
		Vec3 start = floor.add(along, 0.0D, -OrdealMoves.SWEEP_REACH);
		OrdealKit.hazard(chamber, professor, OrdealInterventionsDriver.KIND_BAR, new BossFootprint.Lane(2.0D * OrdealMoves.SWEEP_REACH,
				OrdealMoves.BAR_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.JUMPABLE), start, start.add(0, 0, 1), life);
	}

	/**
	 * Squares the professor's body to the ring's centre while his head follows the fighter, turning at most
	 * {@link #HEAD_TURN} degrees off his body and tipping at most {@link #HEAD_TILT}. The eyes are the drawn colossus's,
	 * not the small entity's sunk below the apron.
	 */
	private static void face(Entity professor, Vec3 centre, Vec3 fighter) {
		Vec3 eye = OrdealFlight.eyes(professor.position());
		float body = OrdealKit.yaw(eye, centre);
		float head = body + Math.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - body), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(body);
		professor.setYBodyRot(body);
		professor.setYHeadRot(head);
		professor.setXRot(Math.clamp(pitch, -HEAD_TILT, HEAD_TILT));
	}

	private static void strike(ServerLevel chamber, ServerPlayer caster, Entity professor, float damage) {
		OrdealKit.strike(chamber, caster, professor, damage, Vec3.ZERO, 0.0D, 0.0D);
	}
}
