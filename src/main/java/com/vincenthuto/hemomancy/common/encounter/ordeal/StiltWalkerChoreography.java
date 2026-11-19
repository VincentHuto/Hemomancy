package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
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
 * The Stilt Walker's part in the Scarlet Mummer's ordeal (2026-11-17): a true self about twelve blocks tall walking the
 * promenade round the ring one long stride at a time ({@link OrdealStiltWalk}), who stops, plants and reaches in with
 * height. What reaches in is spectral stilt (their own leg, which is stilt from the knee down, drawn in red light): one
 * planted in the floor like a spear, one walking four footfalls across the ring, two laid across the ring's whole width
 * and spun half a circle, and at the last their whole shadow standing up at the rim and falling across the ring. The
 * hits are {@link StiltWalkerMoves}.
 */
public final class StiltWalkerChoreography implements OrdealChoreography {
	public static final StiltWalkerChoreography INSTANCE = new StiltWalkerChoreography();
	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_stilt_walker", "scarlet_mummer",
			"ordeal/stilt_walker", List.of(
					new Intervention("planted_stilt", Ramp.OPENING, StiltWalkerMoves.PLANT_WINDUP, 90),
					new Intervention("stride", Ramp.OPENING, StiltWalkerMoves.STRIDE_WINDUP, 120),
					new Intervention("spin", Ramp.PRESSED, StiltWalkerMoves.SPIN_WINDUP, 150),
					new Intervention("long_shadow", Ramp.DESPERATE, StiltWalkerMoves.SHADOW_WINDUP, 200)),
			INSTANCE, ScarletMummerOrdealMoves.MOVES);
	/** How far the body turns a tick, in degrees: a tall thing turns slowly. */
	private static final float TURN = 9.0F;
	/** The head's reach off the body, and its tilt down onto the ring, in degrees. */
	private static final float HEAD_TURN = 75.0F, HEAD_TILT = 55.0F;

	private StiltWalkerChoreography() {
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return OrdealStiltWalk.position(Vec3.atBottomCenterOf(stage).add(0.0D, 1.0D, 0.0D), 0.0D);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		float stride = professor instanceof CircusStiltWalkerEntity walker ? walker.walk(now) : now / (float) OrdealStiltWalk.STRIDE_TICKS;
		boolean held = professor instanceof CircusStiltWalkerEntity walker && walker.walkHeld(now);
		Vec3 at = OrdealStiltWalk.position(floor, stride);
		professor.setPos(at.x, at.y, at.z);
		// Walking, they face along the walk; making a move, they turn square to the ring. The head follows the fighter.
		float body = held ? OrdealKit.yaw(at, floor) : OrdealKit.yaw(Vec3.ZERO, OrdealStiltWalk.heading(stride));
		float turned = professor.getYRot() + Mth.clamp(Mth.wrapDegrees(body - professor.getYRot()), -TURN, TURN);
		Vec3 eye = OrdealStiltWalk.eyes(at), fighter = caster.getEyePosition();
		float head = turned + Mth.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - turned), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(turned);
		professor.setYBodyRot(turned);
		professor.setYHeadRot(head);
		professor.setXRot(Mth.clamp(pitch, -15.0F, HEAD_TILT));
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		Vec3 feet = caster.position();
		return switch (move.id()) {
			// The planted stilt leads a moving fighter: it lands where they are going.
			case "planted_stilt" -> StiltWalkerMoves.inside(floor, feet.add(caster.getDeltaMovement().scale(6.0D)), 0.8D);
			// The spin's start, kept as a point a block from the centre in its direction.
			case "spin" -> {
				double start = StiltWalkerMoves.spinStart(floor, feet);
				yield floor.add(Math.cos(start), 0.0D, Math.sin(start));
			}
			default -> StiltWalkerMoves.inside(floor, feet, 0.5D);
		};
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int windup = active.move.windupTicks();
		int age = active.age(now);
		Vec3 feet = caster.position();
		Vec3 standing = professor == null ? floor.add(0.0D, 0.0D, OrdealStiltWalk.RADIUS) : professor.position();
		Vec3 rim = StiltWalkerMoves.rimToward(floor, standing);
		CircusStiltWalkerEntity walker = professor instanceof CircusStiltWalkerEntity w ? w : null;
		// The walk stands still while any move is under way: the stilts stay planted.
		if (walker != null) walker.holdWalk(now + 1);
		switch (active.move.id()) {
			case "planted_stilt" -> {
				if (age == 0) {
					begin(chamber, walker, CircusStiltWalkerEntity.GESTURE_PLANT, active.mark, active.mark.subtract(rim));
					OrdealKit.hazard(chamber, professor, StiltWalkerMoves.KIND_PLANT, new BossFootprint.Disc(
							BossFootprint.Anchor.ORIGIN, StiltWalkerMoves.PLANT_RADIUS, BossFootprint.Height.STANDING),
							active.mark, active.mark, windup + 6);
				}
				if (age < windup) {
					if (age % 2 == 0) descending(chamber, active.mark, age / (float) windup);
					return false;
				}
				if (age == windup) {
					thud(chamber, active.mark, 4.0F, .55F);
					CircusEffects.impact(chamber, active.mark.add(0, 1, 0), StiltWalkerMoves.PLANT_RADIUS + .5D);
					CircusEffects.impact(chamber, active.mark.add(0, .4, 0), StiltWalkerMoves.PLANT_RADIUS);
					CircusEffects.dust(chamber, active.mark.add(0, .3, 0), 40);
					if (StiltWalkerMoves.plantHits(active.mark, feet))
						OrdealKit.strike(chamber, caster, professor, StiltWalkerMoves.PLANT_DAMAGE, feet.subtract(active.mark), .8D, .5D);
				}
				// The stilt stands in the floor a moment, bleeding into the plate.
				if (age % 4 == 0)
					chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), active.mark.x, active.mark.y + .2D,
							active.mark.z, 3, .2D, .05D, .2D, .02D);
				return age >= windup + StiltWalkerMoves.PLANT_HOLD;
			}
			case "stride" -> {
				Vec3[] chord = StiltWalkerMoves.strideChord(floor, rim, active.mark);
				if (age == 0) {
					begin(chamber, walker, CircusStiltWalkerEntity.GESTURE_STRIDE, chord[0], chord[1]);
					for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++) {
						Vec3 step = StiltWalkerMoves.strideStep(chord[0], chord[1], k);
						OrdealKit.hazard(chamber, professor, StiltWalkerMoves.KIND_STRIDE, new BossFootprint.Disc(
								BossFootprint.Anchor.ORIGIN, StiltWalkerMoves.STRIDE_RADIUS, BossFootprint.Height.STANDING), step,
								step, StiltWalkerMoves.strideLands(k) + 6);
					}
				}
				for (int k = 0; k < StiltWalkerMoves.STRIDE_STEPS; k++) {
					if (age != StiltWalkerMoves.strideLands(k)) continue;
					Vec3 step = StiltWalkerMoves.strideStep(chord[0], chord[1], k);
					thud(chamber, step, 3.0F, .7F + .05F * k);
					CircusEffects.impact(chamber, step.add(0, .4, 0), StiltWalkerMoves.STRIDE_RADIUS);
					CircusEffects.dust(chamber, step.add(0, .2, 0), 24);
					if (!active.landed && StiltWalkerMoves.strideHits(step, feet)) {
						active.landed = true;
						OrdealKit.strike(chamber, caster, professor, StiltWalkerMoves.STRIDE_DAMAGE, feet.subtract(step), .7D, .4D);
					}
				}
				return age >= StiltWalkerMoves.strideLands(StiltWalkerMoves.STRIDE_STEPS - 1) + 10;
			}
			case "spin" -> {
				double start = Math.atan2(active.mark.z - floor.z, active.mark.x - floor.x);
				if (age == 0) {
					begin(chamber, walker, CircusStiltWalkerEntity.GESTURE_SPIN, floor, new Vec3(Math.cos(start), 0.0D, Math.sin(start)));
					spoke(chamber, professor, floor, start, windup);
				}
				if (age < windup) return false;
				int turning = age - windup;
				double angle = StiltWalkerMoves.spinAngle(start, turning);
				spoke(chamber, professor, floor, angle, 2);
				if (turning == 0)
					chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .5F);
				if (turning % 3 == 0) {
					// The stilts' tips scuffing the sawdust at the rim as they come round.
					for (int end = -1; end <= 1; end += 2)
						CircusEffects.dust(chamber, floor.add(Math.cos(angle) * end * 9.0D, .2D, Math.sin(angle) * end * 9.0D), 6);
				}
				if (!active.landed && StiltWalkerMoves.spinHits(floor, angle, feet)) {
					active.landed = true;
					Vec3 sweep = new Vec3(-Math.sin(angle), 0.0D, Math.cos(angle));
					double side = Math.signum((feet.x - floor.x) * Math.cos(angle) + (feet.z - floor.z) * Math.sin(angle));
					OrdealKit.strike(chamber, caster, professor, StiltWalkerMoves.SPIN_DAMAGE, sweep.scale(side == 0 ? 1 : side), .9D, .4D);
				}
				return turning >= StiltWalkerMoves.SPIN_TICKS;
			}
			case "long_shadow" -> {
				Vec3 aim = StiltWalkerMoves.shadowAim(rim, active.mark, floor);
				Vec3 foot = new Vec3(rim.x, floor.y, rim.z);
				if (age == 0) {
					begin(chamber, walker, CircusStiltWalkerEntity.GESTURE_FALL, foot, aim.scale(StiltWalkerMoves.SHADOW_LENGTH));
					shadow(chamber, professor, foot, aim, windup);
				}
				if (age < windup) {
					// The shadow lengthening over the floor as it leans: dark motes crawling out along its lane.
					if (age % 2 == 0) {
						double reach = StiltWalkerMoves.SHADOW_LENGTH * Math.sin(StiltWalkerMoves.shadowFall(age));
						Vec3 at = foot.add(aim.scale(reach));
						chamber.sendParticles(HemoParticleData.darkGlow(CircusEffects.CLOT), at.x, at.y + .3D, at.z, 6, .6D, .1D, .6D,
								.01D);
					}
					return false;
				}
				if (age == windup) {
					shadow(chamber, professor, foot, aim, 6);
					Vec3 middle = foot.add(aim.scale(StiltWalkerMoves.SHADOW_LENGTH * .5D));
					thud(chamber, middle, 5.0F, .4F);
					for (int i = 1; i <= 4; i++) {
						Vec3 at = foot.add(aim.scale(StiltWalkerMoves.SHADOW_LENGTH * i / 4.5D));
						CircusEffects.impact(chamber, at.add(0, .4, 0), StiltWalkerMoves.SHADOW_HALF_WIDTH);
						CircusEffects.dust(chamber, at.add(0, .2, 0), 16);
					}
					if (StiltWalkerMoves.shadowHits(foot, aim, feet)) {
						double side = (feet.x - foot.x) * aim.z - (feet.z - foot.z) * aim.x;
						Vec3 away = new Vec3(aim.z, 0.0D, -aim.x).scale(side >= 0 ? 1.0D : -1.0D);
						OrdealKit.strike(chamber, caster, professor, StiltWalkerMoves.SHADOW_DAMAGE, away, 1.0D, .45D);
					}
				}
				return age >= windup + StiltWalkerMoves.SHADOW_REST;
			}
			default -> {
				return true;
			}
		}
	}

	/** A move's first tick: the gesture on every client, and a creak of strain from the stilts as they gather. */
	private static void begin(ServerLevel chamber, CircusStiltWalkerEntity walker, int gesture, Vec3 at, Vec3 aim) {
		if (walker == null) return;
		walker.gesture(gesture, at, aim);
		chamber.playSound(null, walker.getX(), walker.getY() + 9.0D, walker.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
				SoundSource.HOSTILE, 3.0F, .5F);
	}

	/** A spectral stilt coming down out of the dark over its mark: a falling knot of red light. */
	private static void descending(ServerLevel chamber, Vec3 mark, float charge) {
		double drop = 12.0D * (1.0F - charge * charge);
		chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), mark.x, mark.y + 1.0D + drop, mark.z, 5, .15D, .5D,
				.15D, 0.0D);
	}

	/** A heavy landing: a deep thud through the floor and the crack of wood on plate. */
	private static void thud(ServerLevel chamber, Vec3 at, float volume, float pitch) {
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, volume, pitch);
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, volume * .75F, pitch + .1F);
	}

	/** The spun stilts where they lie: one bar through the centre, the ring's whole width, at knee height. */
	private static void spoke(ServerLevel chamber, Entity professor, Vec3 floor, double angle, int life) {
		Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
		Vec3 start = floor.subtract(dir.scale(StiltWalkerMoves.RIM));
		OrdealKit.hazard(chamber, professor, StiltWalkerMoves.KIND_SPIN, new BossFootprint.Lane(2.0D * StiltWalkerMoves.RIM,
				StiltWalkerMoves.SPIN_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.JUMPABLE), start, start.add(dir), life);
	}

	/** Where the long shadow falls: a wide lane from the rim on their side toward the fighter. */
	private static void shadow(ServerLevel chamber, Entity professor, Vec3 foot, Vec3 aim, int life) {
		OrdealKit.hazard(chamber, professor, StiltWalkerMoves.KIND_SHADOW, new BossFootprint.Lane(StiltWalkerMoves.SHADOW_LENGTH,
				StiltWalkerMoves.SHADOW_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), foot, foot.add(aim), life);
	}
}
