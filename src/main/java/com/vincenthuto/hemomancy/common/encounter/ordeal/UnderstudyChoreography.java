package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Retired Understudy's part in the Mnemonist Puppet's ordeal (2026-11-17): they keep to the prompt corner in the
 * wings of their own stage, standing straight, the mask down, and play the house from there. Every intervention is
 * stagecraft or memory: a spotlight that hunts the fighter and burns where it settles, the house curtain swept across
 * the ring with one parting to stand in, a reprise that learns the fighter's next steps and plays them back as bursts
 * on the very marks, and the stage-hands' trapdoors, knocked for three times and dropped. What reaches into the ring
 * is their double, a life-sized copy of the Understudy in red light that steps out onto the boards to play the
 * fighter's part back at them. The hits are {@link UnderstudyOrdealGeometry}.
 */
public final class UnderstudyChoreography implements OrdealChoreography {
	public static final UnderstudyChoreography INSTANCE = new UnderstudyChoreography();
	/** Their footprints and the puppet's, the Understudy's block in {@link OrdealHazardKinds}. */
	public static final int KIND_SPOT = OrdealHazardKinds.UNDERSTUDY, KIND_CURTAIN = KIND_SPOT + 1, KIND_REPRISE = KIND_SPOT + 2,
			KIND_TRAP = KIND_SPOT + 3, KIND_RECITAL = KIND_SPOT + 4, KIND_THREAD = KIND_SPOT + 5, KIND_ECHO = KIND_SPOT + 6,
			KIND_LUNGE = KIND_SPOT + 7;
	/**
	 * Their gestures ({@link CircusPerformerEntity#gesture}): the cue light raised on the staff, the lock (the staff held
	 * on the settled light, thrust at the burn), the fly rope hauled for the curtain, the mask drawn down for the reprise,
	 * each beat of the reprise (their double at the mark), and the staff knocked on the boards for the trapdoors.
	 */
	public static final int GESTURE_CUE = 0, GESTURE_LOCK = 1, GESTURE_CURTAIN = 2, GESTURE_REPRISE = 3, GESTURE_BEAT = 4,
			GESTURE_KNOCK = 5;
	/** The spotlight hunts for this many ticks, then settles; it burns {@link #LOCK} ticks after settling. */
	public static final int HUNT = 20, LOCK = 10;
	/** How often the hunting light and the sweeping curtain are redrawn where they stand, in ticks. */
	public static final int REDRAW = 2;
	/** The reprise learns a mark every {@link #BEAT} ticks through its wind-up, then bursts them in order, as far apart. */
	public static final int BEAT = 6, MARKS = 6, LEAD = 4;
	/** The stage-hands' three knocks, in ticks from the trapdoors' wind-up. */
	public static final int[] KNOCKS = {6, 13, 20};
	/** The prompt corner: in the wings past the apron, the stage's north, pacing a little either way. */
	public static final double WINGS = 14.5D, PLATFORM = 2.0D, PACE = 0.42D;
	public static final float PACE_PERIOD = 480.0F;
	/** How many times over the true self is drawn, and its eyes above its feet then, in blocks. */
	public static final float SCALE = 1.65F;
	public static final double EYES = 3.35D * SCALE;
	private static final float SPOT_DAMAGE = 6.0F, CURTAIN_DAMAGE = 5.0F, REPRISE_DAMAGE = 4.0F, TRAP_DAMAGE = 6.0F;
	private static final float HEAD_TILT = 35.0F, HEAD_TURN = 70.0F;

	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_understudy", "mnemonist_puppet",
			"ordeal/understudy", List.of(
					new Intervention("spotlight", Ramp.OPENING, HUNT + LOCK, 100),
					new Intervention("curtain", Ramp.OPENING, 24, 130),
					new Intervention("reprise", Ramp.PRESSED, BEAT * MARKS, 150),
					new Intervention("trapdoors", Ramp.DESPERATE, 22, 170)),
			INSTANCE, OrdealMnemonistMoves.MOVES);

	/** Each running move's own state, dropped with the move (weak keys: a move dropped with its ordeal goes too). */
	private static final Map<OrdealInterventionsDriver.Active, Move> RUNNING = new WeakHashMap<>();

	private UnderstudyChoreography() {
	}

	private static final class Move {
		Vec3 light;
		double parting;
		final List<Vec3> marks = new ArrayList<>();
		List<Vec3> doors = List.of();
		int hits;
	}

	/** Where they stand in the wings at {@code time}: pacing the prompt corner, up on its platform. */
	public static Vec3 wings(Vec3 floor, float time) {
		double phi = PACE * Math.sin(time / PACE_PERIOD * Math.PI * 2.0D);
		return floor.add(Math.sin(phi) * WINGS, PLATFORM, -Math.cos(phi) * WINGS);
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return wings(new Vec3(stage.getX() + 0.5D, stage.getY() + 1.0D, stage.getZ() + 0.5D), 0.0F);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Vec3 at = wings(floor, now);
		professor.setPos(at.x, at.y, at.z);
		face(professor, floor, caster.getEyePosition());
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		return new Vec3(caster.getX(), floor.y, caster.getZ());
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int windup = active.move.windupTicks();
		int age = active.age(now);
		Move move = RUNNING.computeIfAbsent(active, ignored -> new Move());
		Vec3 feet = caster.position();
		CircusPerformerEntity understudy = professor instanceof CircusPerformerEntity performer ? performer : null;
		boolean over = switch (active.move.id()) {
			case "spotlight" -> spotlight(chamber, caster, understudy, floor, active, move, age, feet);
			case "curtain" -> curtain(chamber, caster, understudy, floor, active, move, age, windup, feet);
			case "reprise" -> reprise(chamber, caster, understudy, floor, move, age, windup, feet);
			case "trapdoors" -> trapdoors(chamber, caster, understudy, floor, active, move, age, windup, feet);
			default -> true;
		};
		if (over) RUNNING.remove(active);
		return over;
	}

	/** The cue light: raised on the staff, it hunts the fighter for {@link #HUNT} ticks, settles, and burns. */
	private static boolean spotlight(ServerLevel chamber, ServerPlayer caster, CircusPerformerEntity understudy, Vec3 floor,
			OrdealInterventionsDriver.Active active, Move move, int age, Vec3 feet) {
		if (move.light == null) move.light = active.mark;
		if (age == 0) {
			gesture(understudy, GESTURE_CUE, move.light);
			sound(chamber, understudy, SoundInit.RITE_SIGIL_AWAKEN.get(), 2.0F, .7F);
		}
		if (age < HUNT) {
			move.light = UnderstudyOrdealGeometry.spotlightStep(move.light, new Vec3(feet.x, floor.y, feet.z), floor);
			if (age % REDRAW == 0) pool(chamber, understudy, move.light, REDRAW + 1);
			beam(chamber, understudy, move.light, 0.5F);
			return false;
		}
		if (age == HUNT) {
			// It settles: the pool fixed where it stopped, its rim closing to the burn.
			gesture(understudy, GESTURE_LOCK, move.light);
			OrdealKit.hazard(chamber, understudy, KIND_SPOT, disc(UnderstudyOrdealGeometry.SPOT_RADIUS), move.light, move.light,
					LOCK + 6);
			sound(chamber, understudy, SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), 2.4F, .8F);
		}
		if (age < HUNT + LOCK) {
			beam(chamber, understudy, move.light, 1.0F);
			return false;
		}
		Vec3 at = move.light;
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, .75F);
		chamber.playSound(null, at.x, at.y, at.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .9F);
		// The light burning down: a column of it on the pool, then blood and bone off the boards.
		for (int k = 0; k < 6; k++)
			chamber.sendParticles(HemoParticleData.glow(k % 2 == 0 ? CircusEffects.BONE : CircusEffects.FLAME), at.x, at.y + .5D + k * .6D,
					at.z, 6, .45D, .15D, .45D, 0.0D);
		CircusEffects.impact(chamber, at.add(0, .5D, 0), UnderstudyOrdealGeometry.SPOT_RADIUS + .4D);
		CircusEffects.dust(chamber, at.add(0, .3D, 0), 24);
		if (UnderstudyOrdealGeometry.spotlightHits(at, feet))
			OrdealKit.strike(chamber, caster, understudy, SPOT_DAMAGE, feet.subtract(at), .4D, .35D);
		return true;
	}

	/** The house curtain: brought down at the wings' rim through the wind-up, then swept across the ring to the far rim. */
	private static boolean curtain(ServerLevel chamber, ServerPlayer caster, CircusPerformerEntity understudy, Vec3 floor,
			OrdealInterventionsDriver.Active active, Move move, int age, int windup, Vec3 feet) {
		if (age == 0) {
			move.parting = UnderstudyOrdealGeometry.parting(unit(active.startedAt, caster));
			gesture(understudy, GESTURE_CURTAIN, floor.add(move.parting, 0.0D, -UnderstudyOrdealGeometry.CURTAIN_REACH));
			sound(chamber, understudy, SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), 2.4F, .6F);
			curtainHalves(chamber, understudy, floor, move.parting, UnderstudyOrdealGeometry.curtainOffset(0), windup);
		}
		double along = UnderstudyOrdealGeometry.curtainOffset(age - windup);
		if (age < windup) {
			if (age % 3 == 0) folds(chamber, floor, move.parting, along, age / (float) windup);
			return false;
		}
		if (age == windup) {
			chamber.playSound(null, floor.x, floor.y, floor.z - UnderstudyOrdealGeometry.CURTAIN_REACH, SoundInit.RITE_WAVE_START.get(),
					SoundSource.HOSTILE, 3.0F, .6F);
			chamber.playSound(null, floor.x, floor.y, floor.z - UnderstudyOrdealGeometry.CURTAIN_REACH,
					SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .55F);
		}
		if ((age - windup) % REDRAW == 0) {
			curtainHalves(chamber, understudy, floor, move.parting, along, REDRAW + 2);
			folds(chamber, floor, move.parting, along, 1.0F);
		}
		if (!active.landed && UnderstudyOrdealGeometry.curtainHits(floor, along, move.parting, feet)) {
			active.landed = true;
			CircusEffects.impact(chamber, feet.add(0, 1.0D, 0), 1.0D);
			OrdealKit.strike(chamber, caster, understudy, CURTAIN_DAMAGE, new Vec3(0, 0, 1), 1.1D, .3D);
		}
		return age - windup >= UnderstudyOrdealGeometry.CURTAIN_TICKS;
	}

	/**
	 * The reprise: through the wind-up a mark is laid where the fighter stands every {@link #BEAT} ticks (the Understudy
	 * learning their part); then the marks burst in the order they were laid, the double stepping onto each to play it.
	 */
	private static boolean reprise(ServerLevel chamber, ServerPlayer caster, CircusPerformerEntity understudy, Vec3 floor,
			Move move, int age, int windup, Vec3 feet) {
		if (age == 0) {
			gesture(understudy, GESTURE_REPRISE, floor);
			sound(chamber, understudy, SoundInit.RITE_SIGIL_AWAKEN.get(), 2.0F, .55F);
		}
		if (age < windup && age % BEAT == 0) {
			Vec3 mark = new Vec3(feet.x, floor.y, feet.z);
			int index = move.marks.size();
			move.marks.add(mark);
			OrdealKit.hazard(chamber, understudy, KIND_REPRISE, disc(UnderstudyOrdealGeometry.REPRISE_RADIUS), mark, mark,
					windup - age + index * BEAT + 6);
			chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SEGMENT_SEAL.get(), SoundSource.HOSTILE, 1.4F,
					1.0F + index * .08F);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), mark.x, mark.y + .2D, mark.z, 8, .5D, .05D, .5D, 0.0D);
			return false;
		}
		if (age < windup - LEAD) return false;
		for (int index = 0; index < move.marks.size(); index++) {
			int burst = windup + index * BEAT;
			Vec3 mark = move.marks.get(index);
			// The double steps onto the mark a moment before it bursts.
			if (age == burst - LEAD) gesture(understudy, GESTURE_BEAT, mark);
			if (age != burst) continue;
			chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.4F,
					.8F + index * .06F);
			CircusEffects.impact(chamber, mark.add(0, .4D, 0), UnderstudyOrdealGeometry.REPRISE_RADIUS);
			CircusEffects.dust(chamber, mark.add(0, .2D, 0), 12);
			if (move.hits < 2 && UnderstudyOrdealGeometry.repriseHits(mark, feet)) {
				move.hits++;
				OrdealKit.strike(chamber, caster, understudy, REPRISE_DAMAGE, feet.subtract(mark), .5D, .35D);
			}
		}
		return age >= windup + (move.marks.size() - 1) * BEAT + 2;
	}

	/** The stage-hands' trapdoors: three knocks on the boards, then four doors drop, one under the fighter. */
	private static boolean trapdoors(ServerLevel chamber, ServerPlayer caster, CircusPerformerEntity understudy, Vec3 floor,
			OrdealInterventionsDriver.Active active, Move move, int age, int windup, Vec3 feet) {
		if (age == 0) {
			move.doors = UnderstudyOrdealGeometry.trapdoors(feet, floor, unit(active.startedAt, caster) * Math.PI * 2.0D);
			gesture(understudy, GESTURE_KNOCK, move.doors.get(0));
			for (Vec3 door : move.doors)
				OrdealKit.hazard(chamber, understudy, KIND_TRAP, disc(UnderstudyOrdealGeometry.TRAP_RADIUS), door, door, windup + 6);
		}
		for (int knock : KNOCKS) {
			if (age != knock) continue;
			// The staff's heel on the boards, and every door answering it from below.
			sound(chamber, understudy, SoundInit.RITE_SUBJECT_THUD.get(), 2.6F, 1.1F);
			for (Vec3 door : move.doors) {
				chamber.playSound(null, door.x, door.y, door.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 1.2F, 1.3F);
				CircusEffects.dust(chamber, door.add(0, .2D, 0), 6);
			}
		}
		if (age < windup) return false;
		for (Vec3 door : move.doors) {
			chamber.playSound(null, door.x, door.y, door.z, SoundInit.RITE_FLOOR_FORM.get(), SoundSource.HOSTILE, 2.0F, .6F);
			CircusEffects.impact(chamber, door.add(0, .3D, 0), UnderstudyOrdealGeometry.TRAP_RADIUS);
			CircusEffects.dust(chamber, door.add(0, .2D, 0), 18);
		}
		chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, .5F);
		for (Vec3 door : move.doors) {
			if (!UnderstudyOrdealGeometry.trapdoorHits(door, feet)) continue;
			// Dropped to the knees: struck, pulled down and slowed while they climb out.
			OrdealKit.strike(chamber, caster, understudy, TRAP_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
			caster.setDeltaMovement(caster.getDeltaMovement().multiply(.2D, 0.0D, .2D).add(0, -.6D, 0));
			caster.hurtMarked = true;
			caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
			break;
		}
		return true;
	}

	/** A disc on the boards at standing height. */
	private static BossFootprint disc(double radius) {
		return new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, radius, BossFootprint.Height.STANDING);
	}

	/** The hunting light's pool, drawn where it stands for a moment ({@code OrdealTelegraphPass} draws it as a spotlight). */
	private static void pool(ServerLevel chamber, Entity owner, Vec3 light, int life) {
		OrdealKit.hazard(chamber, owner, KIND_SPOT, disc(UnderstudyOrdealGeometry.SPOT_RADIUS), light, light, life);
	}

	/** The beam from the cue light on their staff to the pool: fine motes of bone light, thicker when it settles. */
	private static void beam(ServerLevel chamber, CircusPerformerEntity understudy, Vec3 light, float strength) {
		if (understudy == null || chamber.getGameTime() % 2 != 0) return;
		Vec3 lamp = lamp(understudy);
		Vec3 span = light.add(0, .2D, 0).subtract(lamp);
		int steps = (int) (span.length() / .7D);
		for (int i = 1; i < steps; i++) {
			if (chamber.getRandom().nextFloat() > .35F + .4F * strength) continue;
			Vec3 at = lamp.add(span.scale(i / (double) steps));
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), at.x, at.y, at.z, 1, .04D, .04D, .04D, 0.0D);
		}
		chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), light.x, light.y + .1D, light.z, (int) (3 + 4 * strength),
				UnderstudyOrdealGeometry.SPOT_RADIUS * .5D, .02D, UnderstudyOrdealGeometry.SPOT_RADIUS * .5D, 0.0D);
	}

	/** The cue light at the head of their staff, raised: over their right shoulder, toward the ring. */
	public static Vec3 lamp(Entity understudy) {
		Vec3 inward = Vec3.directionFromRotation(0.0F, understudy.getYRot());
		return understudy.position().add(0.0D, 5.2D * SCALE, 0.0D).add(inward.scale(2.0D));
	}

	/** The curtain's two halves either side of the parting, at {@code along} across the ring. */
	private static void curtainHalves(ServerLevel chamber, Entity owner, Vec3 floor, double parting, double along, int life) {
		for (double[] half : UnderstudyOrdealGeometry.curtainHalves(parting)) {
			// Clipped to the ring's chord where the curtain stands.
			double chord = Math.sqrt(Math.max(0.0D, UnderstudyOrdealGeometry.RING * UnderstudyOrdealGeometry.RING - along * along));
			double from = Math.max(half[0], -chord), to = Math.min(half[1], chord);
			if (to - from < .2D) continue;
			Vec3 start = floor.add(from, 0.0D, along);
			OrdealKit.hazard(chamber, owner, KIND_CURTAIN, new BossFootprint.Lane(to - from, UnderstudyOrdealGeometry.CURTAIN_HALF,
					0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), start, start.add(1, 0, 0), life);
		}
	}

	/** The curtain's folds standing up off the boards: a wall of crimson motes, with the parting left open. */
	private static void folds(ServerLevel chamber, Vec3 floor, double parting, double along, float strength) {
		double chord = Math.sqrt(Math.max(0.0D, UnderstudyOrdealGeometry.RING * UnderstudyOrdealGeometry.RING - along * along));
		for (double x = -chord; x <= chord; x += 1.1D) {
			if (Math.abs(x - parting) <= UnderstudyOrdealGeometry.PARTING_HALF) continue;
			for (int k = 0; k < 3; k++) {
				if (chamber.getRandom().nextFloat() > .3F + .6F * strength) continue;
				chamber.sendParticles(HemoParticleData.glow(k == 2 ? CircusEffects.CLOT : CircusEffects.CRIMSON), floor.x + x,
						floor.y + .4D + k * 1.2D * strength, floor.z + along, 1, .25D, .3D, .08D, 0.0D);
			}
		}
		// The parting's edges in bone, so the gap reads from anywhere.
		for (int side = -1; side <= 1; side += 2)
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), floor.x + parting + side * UnderstudyOrdealGeometry.PARTING_HALF,
					floor.y + 1.0D, floor.z + along, 2, .05D, .8D * strength, .05D, 0.0D);
	}

	/** A seeded unit for a move's choice (the curtain's parting, the doors' turn), the same for the same start. */
	private static double unit(long startedAt, ServerPlayer caster) {
		long seed = startedAt * 0x9E3779B97F4A7C15L ^ caster.getUUID().getLeastSignificantBits();
		seed ^= seed >>> 31;
		return Math.floorMod(seed, 1000L) / 1000.0D;
	}

	private static void gesture(CircusPerformerEntity understudy, int kind, Vec3 at) {
		if (understudy != null) understudy.gesture(kind, at);
	}

	private static void sound(ServerLevel chamber, CircusPerformerEntity understudy, net.minecraft.sounds.SoundEvent sound,
			float volume, float pitch) {
		if (understudy == null) return;
		chamber.playSound(null, understudy.getX(), understudy.getY() + 3.0D, understudy.getZ(), sound, SoundSource.HOSTILE,
				volume, pitch);
	}

	/**
	 * Squares their body to the ring's centre while their head follows the fighter, turning at most {@link #HEAD_TURN}
	 * degrees off the body and tipping at most {@link #HEAD_TILT}. The eyes are the drawn true self's.
	 */
	private static void face(Entity professor, Vec3 centre, Vec3 fighter) {
		Vec3 eye = professor.position().add(0.0D, EYES, 0.0D);
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
