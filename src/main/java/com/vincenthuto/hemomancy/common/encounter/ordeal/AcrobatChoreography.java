package com.vincenthuto.hemomancy.common.encounter.ordeal;

import static com.vincenthuto.hemomancy.common.encounter.ordeal.AcrobatOrdealHits.*;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Acrobat's part in the Veinwing Vulture's ordeal (2026-11-17): hanging by the knees from a trapeze in the rigging
 * over the net apron ({@link AcrobatRigging}), body square to the ring, head on the fighter, they reach into the ring
 * with their rigging made of their own blood: a trapeze sent swinging through the fighter's spot, their own double
 * dropped from the dark, a snare line cast round the fighter's feet and hauled, and a net flung over them. The hits are
 * {@link AcrobatOrdealHits}.
 */
public final class AcrobatChoreography implements OrdealChoreography {
	public static final AcrobatChoreography INSTANCE = new AcrobatChoreography();
	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_acrobat", "veinwing_vulture",
			"ordeal/acrobat", List.of(
					new Intervention("trapeze_swing", Ramp.OPENING, SWING_WINDUP, 110),
					new Intervention("the_drop", Ramp.OPENING, DROP_WINDUP, 100),
					new Intervention("snare_line", Ramp.PRESSED, SNARE_WINDUP, 150),
					new Intervention("falling_net", Ramp.DESPERATE, NET_WINDUP, 220)),
			INSTANCE, VultureOrdealMoves.MOVES);
	/** How long the net lies on the netted. */
	private static final int NET_HOLD = 50;
	/** The furthest the head tips toward the ring and turns off the squared body, in degrees. */
	private static final float HEAD_TILT = 60.0F, HEAD_TURN = 70.0F;

	private AcrobatChoreography() {
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return AcrobatRigging.position(stage, 0.0F);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Vec3 at = AcrobatRigging.position(BlockPos.containing(floor.x, floor.y - 1.0D, floor.z), professor.tickCount);
		professor.setPos(at.x, at.y, at.z);
		face(professor, floor, caster.getEyePosition());
		// Where the trapeze's lines run up into the dark: a thread of blood light now and then.
		if (professor.tickCount % 6 == 0)
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), at.x, at.y + 2.0D + chamber.getRandom().nextDouble() * 6.0D,
					at.z, 1, 0.6D, 0.4D, 0.6D, 0.0D);
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		Vec3 feet = new Vec3(caster.getX(), floor.y, caster.getZ());
		if (move.id().equals("the_drop")) return lead(caster.position(), caster.getDeltaMovement(), 6.0D, floor.y);
		return feet;
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int windup = active.move.windupTicks();
		int age = active.age(now);
		float charge = Math.min(1.0F, age / (float) windup);
		Vec3 feet = caster.position();
		Vec3 mark = active.mark;
		if (age == 0 && professor instanceof CircusAcrobatEntity acrobat) {
			acrobat.gesture(gesture(active.move.id()), mark);
			chamber.playSound(null, acrobat.getX(), acrobat.getY() - 3.0D, acrobat.getZ(),
					SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE, 2.5F, 1.25F);
		}
		switch (active.move.id()) {
			case "trapeze_swing" -> {
				Vec3 d = swingDirection(mark, floor, professor);
				Vec3 start = mark.add(d.scale(-SWING_HALF_LENGTH));
				if (age == 0)
					OrdealKit.hazard(chamber, professor, KIND_SWING, new BossFootprint.Lane(2.0D * SWING_HALF_LENGTH, SWING_HALF_WIDTH,
							0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), start, start.add(d), windup);
				if (age < windup) {
					// The spectral trapeze hauled back over the lane's near end, trembling as it is held.
					Vec3 held = start.add(0.0D, barHeight(-SWING_HALF_LENGTH) + 0.5D, 0.0D);
					CircusEffects.gather(chamber, held, CircusEffects.CRIMSON, charge);
					return false;
				}
				int swung = age - windup;
				if (swung == 0)
					chamber.playSound(null, mark.x, mark.y + 2.0D, mark.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 3.0F, .55F);
				double along = swingAlong(swung);
				Vec3 bar = mark.add(d.scale(along));
				if (swung % 2 == 0) {
					// The bar burning on the floor where it passes, and its light in the air at its height.
					Vec3 across = new Vec3(-d.z, 0.0D, d.x);
					OrdealKit.hazard(chamber, professor, KIND_SWING, new BossFootprint.Lane(2.0D * SWING_HALF_WIDTH, .45D, 0, 0.0D, 0.0D,
							0.0D, 0, BossFootprint.Height.STANDING), bar.subtract(across.scale(SWING_HALF_WIDTH)),
							bar.add(across), 3);
					chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), bar.x, bar.y + barHeight(along), bar.z, 6,
							Math.abs(across.x) * .8D + .05D, .05D, Math.abs(across.z) * .8D + .05D, 0.0D);
				}
				if (!active.landed && swingHits(mark, d, along, floor.y, feet)) {
					active.landed = true;
					landed(chamber, feet.add(0, 1, 0), 1.0D);
					OrdealKit.strike(chamber, caster, professor, SWING_DAMAGE, d, 1.3D, .55D);
				}
				return swung >= SWING_TICKS;
			}
			case "the_drop" -> {
				if (age == 0)
					OrdealKit.hazard(chamber, professor, KIND_DROP, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, DROP_RADIUS,
							BossFootprint.Height.STANDING), mark, mark, windup + 6);
				if (age < windup) {
					// The double coming down out of the dark: a falling knot of bone and blood light over the mark.
					double height = dropHeight(age);
					chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), mark.x, mark.y + 1.0D + height, mark.z, 4, .3D, .5D,
							.3D, 0.0D);
					if (age > windup - 5)
						chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), mark.x, mark.y + 1.0D + height, mark.z, 6,
								.2D, 1.2D, .2D, 0.0D);
					return false;
				}
				landed(chamber, mark.add(0, .4D, 0), DROP_RADIUS + .5D);
				chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .7F);
				CircusEffects.dust(chamber, mark.add(0, .3, 0), 36);
				if (discHits(mark, DROP_RADIUS, feet))
					OrdealKit.strike(chamber, caster, professor, DROP_DAMAGE, feet.subtract(mark), .8D, .6D);
				return true;
			}
			case "snare_line" -> {
				if (age == 0)
					OrdealKit.hazard(chamber, professor, KIND_SNARE, new BossFootprint.Ring(SNARE_RADIUS, SNARE_RADIUS, .5D, 1, 0, windup,
							BossFootprint.Height.STANDING), mark, mark, windup);
				Vec3 hand = professor == null ? mark.add(0, 10, 0) : AcrobatRigging.eyes(professor.position());
				if (age < windup) {
					// The line paying down from the Acrobat's hand to the loop.
					if (age % 4 == 0) BossTelegraphs.tendril(chamber, hand, mark.add(0, .1D, 0), CircusEffects.THREAD_CORE_ARGB,
							CircusEffects.THREAD_GLOW_ARGB, age * 31L);
					return false;
				}
				if (age == windup) {
					chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.ENTITY_IMPALED_PROCESSION_THREAD_SEVER.get(),
							SoundSource.HOSTILE, 3.0F, .8F);
					BossTelegraphs.tendril(chamber, hand, mark.add(0, .6D, 0), CircusEffects.THREAD_CORE_ARGB,
							CircusEffects.THREAD_GLOW_ARGB, 977L);
					if (discHits(mark, SNARE_RADIUS, feet)) {
						active.landed = true;
						Vec3 yank = snareYank(feet, hand);
						OrdealKit.strike(chamber, caster, professor, SNARE_DAMAGE, yank, 1.1D, 1.15D);
						landed(chamber, feet.add(0, .5D, 0), .8D);
					}
				}
				// The line held taut for a beat after the haul.
				if (active.landed && age < windup + 8 && age % 2 == 0)
					BossTelegraphs.tendril(chamber, hand, caster.position().add(0, .8D, 0), CircusEffects.THREAD_CORE_ARGB,
							CircusEffects.THREAD_GLOW_ARGB, age * 17L);
				return age >= windup + 10;
			}
			case "falling_net" -> {
				if (age == 0)
					OrdealKit.hazard(chamber, professor, KIND_NET, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, NET_RADIUS,
							BossFootprint.Height.STANDING), mark, mark, windup + 4);
				if (age < windup) {
					// The net's knots coming down: slow, then fast.
					float fall = charge * charge;
					double height = 1.0D + NET_HEIGHT * (1.0D - fall);
					if (age % 2 == 0)
						for (int i = -3; i <= 3; i += 2)
							for (int j = -3; j <= 3; j += 2)
								chamber.sendParticles(HemoParticleData.glow((i + j) % 4 == 0 ? CircusEffects.BONE : CircusEffects.CRIMSON),
										mark.x + i * NET_RADIUS / 3.0D, mark.y + height, mark.z + j * NET_RADIUS / 3.0D, 1, .05D, .05D, .05D, 0.0D);
					return false;
				}
				if (age == windup) {
					chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .9F);
					BossTelegraphs.ring(chamber, mark.add(0, .15D, 0), NET_RADIUS, CircusEffects.CRIMSON, 36);
					CircusEffects.dust(chamber, mark.add(0, .3D, 0), 30);
					if (discHits(mark, NET_RADIUS, feet)) {
						active.landed = true;
						OrdealKit.strike(chamber, caster, professor, NET_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
						caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, NET_HOLD, 3, false, true));
					}
				}
				// The netted fighter tangled: the net's light clinging to them while it holds.
				if (active.landed && age % 5 == 0)
					BossTelegraphs.ring(chamber, caster.position().add(0, .2D, 0), 1.0D, CircusEffects.CRIMSON, 14);
				return age >= windup + (active.landed ? NET_HOLD : 4);
			}
			default -> {
				return true;
			}
		}
	}

	/**
	 * The swing's line: from the ring's centre out through the mark, so the trapeze crosses the fighter's spot; when the
	 * fighter stands at the centre, from the Acrobat's side across it.
	 */
	public static Vec3 swingDirection(Vec3 mark, Vec3 floor, Entity professor) {
		Vec3 out = flat(mark.subtract(floor));
		if (horizontal(mark, floor) >= 1.5D || professor == null) return out.lengthSqr() < 1.0E-6D ? new Vec3(0, 0, 1) : out;
		Vec3 across = flat(floor.subtract(professor.position()));
		return across.lengthSqr() < 1.0E-6D ? new Vec3(0, 0, 1) : across;
	}

	/** A blow landing: a thud and a burst of blood off the plate. The camera's jolt is the client's, from the gesture. */
	private static void landed(ServerLevel chamber, Vec3 at, double size) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.5F, .7F);
		CircusEffects.impact(chamber, at, size);
	}

	@Override
	public int gestureFor(String move) {
		return gesture(move);
	}

	private static int gesture(String move) {
		return switch (move) {
			case "trapeze_swing" -> CircusAcrobatEntity.GESTURE_SWING;
			case "the_drop" -> CircusAcrobatEntity.GESTURE_DROP;
			case "snare_line" -> CircusAcrobatEntity.GESTURE_SNARE;
			case "falling_net" -> CircusAcrobatEntity.GESTURE_NET;
			default -> -1;
		};
	}

	/**
	 * Squares the hanging body to the ring's centre while the head follows the fighter, turning at most
	 * {@link #HEAD_TURN} degrees off the body and tipping at most {@link #HEAD_TILT}.
	 */
	private static void face(Entity professor, Vec3 centre, Vec3 fighter) {
		Vec3 eye = AcrobatRigging.eyes(professor.position());
		float body = OrdealKit.yaw(eye, centre);
		float head = body + Math.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - body), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(body);
		professor.setYBodyRot(body);
		professor.setYHeadRot(head);
		professor.setXRot(Math.clamp(pitch, -HEAD_TILT, HEAD_TILT));
	}
}
