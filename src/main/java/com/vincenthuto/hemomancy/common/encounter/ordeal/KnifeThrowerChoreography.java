package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowJugglerRules;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock;
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
 * The Dagger Thrower's part in the Marrow Juggler's ordeal (2026-11-17). Their true self paces a throwing gallery
 * over the north side of the ring, body square to it, head on the fighter, and throws: a fan of five knives down
 * lanes, a ring of blades that closes, a heavy blade that pins a foot, and the board, where six blades outline the
 * fighter and the seventh goes in the bullseye. Their real arm throws where they stand; what crosses the ring are
 * spectral blades their true self's model draws from the same gesture. The hits are {@link KnifeOrdealMoves}.
 */
public final class KnifeThrowerChoreography implements OrdealChoreography {
	public static final KnifeThrowerChoreography INSTANCE = new KnifeThrowerChoreography();
	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_knife_thrower", "marrow_spitter",
			"ordeal/knife_thrower", List.of(
					new Intervention("knife_fan", Ramp.OPENING, KnifeOrdealMoves.FAN_WINDUP, 110),
					new Intervention("closing_ring", Ramp.OPENING, KnifeOrdealMoves.RING_WINDUP, 130),
					new Intervention("pinning_blade", Ramp.PRESSED, KnifeOrdealMoves.PIN_WINDUP, 120),
					new Intervention("the_board", Ramp.DESPERATE, KnifeOrdealMoves.BOARD_WINDUP, 220)),
			INSTANCE, MarrowJugglerOrdealMoves.MOVES);
	/** The furthest their head tips toward the ring, and turns off their squared body, in degrees. */
	private static final float HEAD_TILT = 35.0F, HEAD_TURN = 70.0F;

	private KnifeThrowerChoreography() {
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return KnifeOrdealMoves.professorAt(Vec3.atBottomCenterOf(stage).add(0, 1, 0), 0.0D);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Vec3 at = KnifeOrdealMoves.professorAt(floor, now);
		professor.setPos(at.x, at.y, at.z);
		Vec3 eye = at.add(0.0D, KnifeOrdealMoves.SCALE * KnifeOrdealMoves.EYES, 0.0D);
		Vec3 fighter = caster.getEyePosition();
		float body = OrdealKit.yaw(eye, floor);
		float head = body + Math.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - body), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(body);
		professor.setYBodyRot(body);
		professor.setYHeadRot(head);
		professor.setXRot(Math.clamp(pitch, -HEAD_TILT, HEAD_TILT));
	}

	/** Each move aims at the fighter: the fan and the pin where they are heading, the ring and the board where they stand. */
	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		Vec3 feet = caster.position();
		if (move.id().equals("knife_fan") || move.id().equals("pinning_blade")) {
			Vec3 ahead = feet.add(caster.getDeltaMovement().multiply(1, 0, 1).scale(move.id().equals("pinning_blade") ? 8.0D : 4.0D));
			// A lead never past the curb.
			Vec3 out = new Vec3(ahead.x - floor.x, 0.0D, ahead.z - floor.z);
			double limit = OrdealStage.RING_RADIUS - 1.0D;
			if (out.length() > limit) ahead = floor.add(out.normalize().scale(limit));
			return new Vec3(ahead.x, floor.y, ahead.z);
		}
		return new Vec3(feet.x, floor.y, feet.z);
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int age = active.age(now);
		int windup = active.move.windupTicks();
		if (age == 0 && professor instanceof CircusPerformerEntity performer) {
			performer.gesture(gesture(active.move.id()), active.mark);
			chamber.playSound(null, performer.getX(), performer.getY() + 4.0D, performer.getZ(),
					SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE, 2.5F, 1.15F);
		}
		Vec3 feet = caster.position();
		return switch (active.move.id()) {
			case "knife_fan" -> fan(chamber, caster, professor, floor, active, age, windup, feet);
			case "closing_ring" -> ring(chamber, caster, professor, floor, active, age, windup, feet);
			case "pinning_blade" -> pin(chamber, caster, puppet, professor, active, age, windup, feet, now);
			case "the_board" -> board(chamber, caster, professor, active, age, feet);
			default -> true;
		};
	}

	/** Where the fan's lanes start: where the thrower stands when the blades leave the hand, on the floor's plane. */
	private static Vec3 throwPoint(Vec3 floor, OrdealInterventionsDriver.Active active) {
		Vec3 at = KnifeOrdealMoves.professorAt(floor, active.startedAt + active.move.windupTicks());
		return new Vec3(at.x, floor.y, at.z);
	}

	private static boolean fan(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, int windup, Vec3 feet) {
		Vec3 origin = throwPoint(floor, active);
		if (age == 0) lanes(chamber, professor, origin, active.mark, floor, windup);
		if (age < windup) return false;
		if (age == windup) {
			lanes(chamber, professor, origin, active.mark, floor, 4);
			chamber.playSound(null, professor.getX(), professor.getY() + 4.0D, professor.getZ(), SoundEvents.TRIDENT_THROW.value(),
					SoundSource.HOSTILE, 3.0F, 1.2F);
		}
		if (age == windup + KnifeOrdealMoves.FLIGHT) {
			chamber.playSound(null, active.mark.x, active.mark.y, active.mark.z, SoundInit.RITE_STAFF_IMPACT.get(),
					SoundSource.HOSTILE, 3.0F, 1.1F);
			for (int lane = 0; lane < KnifeOrdealMoves.FAN_LANES; lane++) {
				Vec3 dir = KnifeOrdealMoves.fanDirection(origin, active.mark, lane);
				double[] span = KnifeOrdealMoves.span(origin, dir, floor, OrdealStage.RING_RADIUS);
				if (span == null) continue;
				Vec3 far = origin.add(dir.scale(span[1]));
				CircusEffects.lineFootprint(chamber, origin.add(dir.scale(span[0])), far, CircusEffects.BONE);
				CircusEffects.impact(chamber, far.add(0, .4, 0), 0.6D);
			}
			if (KnifeOrdealMoves.fanHits(origin, active.mark, feet, floor.y))
				OrdealKit.strike(chamber, caster, professor, KnifeOrdealMoves.FAN_DAMAGE, feet.subtract(origin), .5D, .2D);
		}
		return age >= windup + KnifeOrdealMoves.FLIGHT + 2;
	}

	/** The fan's five lanes across the ring, from curb to curb. */
	private static void lanes(ServerLevel chamber, Entity professor, Vec3 origin, Vec3 aim, Vec3 floor, int life) {
		for (int lane = 0; lane < KnifeOrdealMoves.FAN_LANES; lane++) {
			Vec3 dir = KnifeOrdealMoves.fanDirection(origin, aim, lane);
			double[] span = KnifeOrdealMoves.span(origin, dir, floor, OrdealStage.RING_RADIUS);
			if (span == null) continue;
			Vec3 start = origin.add(dir.scale(span[0]));
			OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_FAN, new BossFootprint.Lane(span[1] - span[0],
					KnifeOrdealMoves.FAN_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), start, start.add(dir), life);
		}
	}

	private static boolean ring(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, int windup, Vec3 feet) {
		Vec3 centre = active.mark;
		if (age == 0)
			OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_RING, new BossFootprint.Ring(KnifeOrdealMoves.RING_RADIUS,
					KnifeOrdealMoves.RING_RADIUS, 0.8D, 1, 0, windup, BossFootprint.Height.JUMPABLE), centre, centre, windup);
		if (age == KnifeOrdealMoves.FLIGHT + 4) {
			// The ring of blades lands point-down round the fighter.
			chamber.playSound(null, centre.x, centre.y, centre.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.3F);
			for (int k = 0; k < KnifeOrdealMoves.RING_BLADES; k++)
				CircusEffects.dust(chamber, KnifeOrdealMoves.ringBlade(centre, k, KnifeOrdealMoves.RING_RADIUS), 3);
		}
		if (age < windup) return false;
		int sliding = age - windup;
		if (sliding == 0) {
			OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_RING, new BossFootprint.Ring(KnifeOrdealMoves.RING_RADIUS, 0.0D,
					KnifeOrdealMoves.RING_BAND, 1, 0, KnifeOrdealMoves.RING_CLOSE, BossFootprint.Height.JUMPABLE), centre, centre,
					KnifeOrdealMoves.RING_CLOSE + 2);
			chamber.playSound(null, centre.x, centre.y, centre.z, SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 2.5F, .8F);
		}
		double wave = KnifeOrdealMoves.ringRadius(sliding);
		if (sliding % 2 == 0)
			for (int k = 0; k < KnifeOrdealMoves.RING_BLADES; k++) {
				Vec3 at = KnifeOrdealMoves.ringBlade(centre, k, wave);
				chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), at.x, at.y + .3D, at.z, 1, .05D, .05D, .05D, 0.0D);
			}
		if (!active.landed && KnifeOrdealMoves.ringHits(centre, wave, feet)) {
			active.landed = true;
			OrdealKit.strike(chamber, caster, professor, KnifeOrdealMoves.RING_DAMAGE, feet.subtract(centre), .3D, .25D);
		}
		if (sliding == KnifeOrdealMoves.RING_CLOSE) {
			// The blades meet in the middle with a clash.
			chamber.playSound(null, centre.x, centre.y, centre.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .9F);
			CircusEffects.impact(chamber, centre.add(0, .4, 0), 1.0D);
		}
		return sliding >= KnifeOrdealMoves.RING_CLOSE + 4;
	}

	private static boolean pin(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor,
			OrdealInterventionsDriver.Active active, int age, int windup, Vec3 feet, long now) {
		Vec3 mark = active.mark;
		if (age == 0)
			OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_PIN, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					KnifeOrdealMoves.PIN_RADIUS, BossFootprint.Height.STANDING), mark, mark, windup + 6);
		if (age == windup - KnifeOrdealMoves.PIN_FLIGHT)
			chamber.playSound(null, professor.getX(), professor.getY() + 4.0D, professor.getZ(), SoundEvents.TRIDENT_THROW.value(),
					SoundSource.HOSTILE, 3.0F, .7F);
		if (age < windup) return false;
		if (age == windup) {
			chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.5F, .8F);
			chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
			CircusEffects.impact(chamber, mark.add(0, .4, 0), KnifeOrdealMoves.PIN_RADIUS + .4D);
			CircusEffects.dust(chamber, mark.add(0, .2, 0), 24);
			if (KnifeOrdealMoves.spotHits(mark, KnifeOrdealMoves.PIN_RADIUS, feet)) {
				OrdealKit.strike(chamber, caster, professor, KnifeOrdealMoves.PIN_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
				MorphlingLock.pin(caster, MarrowJugglerRules.PIN_TICKS);
				MarrowJugglerOrdealMoves.notePinned(puppet.getUUID(), now + MarrowJugglerRules.PIN_TICKS);
			}
		}
		return age >= windup + 4;
	}

	private static boolean board(ServerLevel chamber, ServerPlayer caster, Entity professor,
			OrdealInterventionsDriver.Active active, int age, Vec3 feet) {
		Vec3 centre = active.mark;
		if (age == 0) {
			int end = KnifeOrdealMoves.BULLSEYE_AT + 6;
			// The board: its edge and its inner ring smouldering, then each blade's mark closing in turn.
			for (double radius : new double[]{KnifeOrdealMoves.BOARD_EDGE, KnifeOrdealMoves.BOARD_OUTLINE})
				OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_BOARD, new BossFootprint.Ring(radius, radius,
						radius == KnifeOrdealMoves.BOARD_EDGE ? .5D : .25D, 1, 0, end, BossFootprint.Height.STANDING), centre, centre, end);
			for (int k = 0; k < KnifeOrdealMoves.BOARD_KNIVES; k++) {
				Vec3 spot = KnifeOrdealMoves.boardSpot(centre, k);
				OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_BOARD, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
						KnifeOrdealMoves.BOARD_SPOT, BossFootprint.Height.STANDING), spot, spot, KnifeOrdealMoves.boardLanding(k) + 6);
			}
			OrdealKit.hazard(chamber, professor, KnifeOrdealMoves.KIND_BOARD, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
					KnifeOrdealMoves.BULLSEYE, BossFootprint.Height.STANDING), centre, centre, end);
		}
		for (int k = 0; k < KnifeOrdealMoves.BOARD_KNIVES; k++) {
			if (age == KnifeOrdealMoves.boardLanding(k) - KnifeOrdealMoves.FLIGHT)
				chamber.playSound(null, professor.getX(), professor.getY() + 4.0D, professor.getZ(), SoundEvents.TRIDENT_THROW.value(),
						SoundSource.HOSTILE, 2.5F, 1.4F + .05F * k);
			if (age != KnifeOrdealMoves.boardLanding(k)) continue;
			Vec3 spot = KnifeOrdealMoves.boardSpot(centre, k);
			chamber.playSound(null, spot.x, spot.y, spot.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.2F + .05F * k);
			CircusEffects.impact(chamber, spot.add(0, .3, 0), KnifeOrdealMoves.BOARD_SPOT);
			if (KnifeOrdealMoves.spotHits(spot, KnifeOrdealMoves.BOARD_SPOT, feet))
				OrdealKit.strike(chamber, caster, professor, KnifeOrdealMoves.OUTLINE_DAMAGE, feet.subtract(centre), .3D, .15D);
		}
		if (age == KnifeOrdealMoves.BULLSEYE_AT - KnifeOrdealMoves.FLIGHT)
			chamber.playSound(null, professor.getX(), professor.getY() + 4.0D, professor.getZ(), SoundEvents.TRIDENT_THROW.value(),
					SoundSource.HOSTILE, 3.0F, .9F);
		if (age == KnifeOrdealMoves.BULLSEYE_AT) {
			chamber.playSound(null, centre.x, centre.y, centre.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 4.0F, .7F);
			chamber.playSound(null, centre.x, centre.y, centre.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .8F);
			CircusEffects.impact(chamber, centre.add(0, .4, 0), KnifeOrdealMoves.BOARD_EDGE);
			CircusEffects.dust(chamber, centre.add(0, .2, 0), 30);
			if (KnifeOrdealMoves.spotHits(centre, KnifeOrdealMoves.BULLSEYE, feet))
				OrdealKit.strike(chamber, caster, professor, KnifeOrdealMoves.BULLSEYE_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
		}
		return age >= KnifeOrdealMoves.BULLSEYE_AT + 4;
	}

	/** The true self's gesture for a move. */
	@Override
	public int gestureFor(String move) {
		return gesture(move);
	}

	private static int gesture(String move) {
		return switch (move) {
			case "knife_fan" -> KnifeOrdealMoves.GESTURE_FAN;
			case "closing_ring" -> KnifeOrdealMoves.GESTURE_RING;
			case "pinning_blade" -> KnifeOrdealMoves.GESTURE_PIN;
			case "the_board" -> KnifeOrdealMoves.GESTURE_BOARD;
			default -> -1;
		};
	}
}
