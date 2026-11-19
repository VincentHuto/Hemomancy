package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * The Fire Breather's part in the Cinder Bellows' ordeal (2026-11-17): their true self paces the pulpit, a crescent
 * gallery past the north rim of the Scorch Ring, body square to the ring and head on the fighter, and breathes into
 * the ring from there. What reaches in is their breath given a body: a tongue of flame poured from the mouth over the
 * curb ({@code CircusTrueFireEaterModel}'s breath). They loose a long breath down a lane through the fighter, spit three
 * embers on and round them, light a ring of fire at the rim that closes in on the Bellows, and spew a cinder rain whose
 * marks burn on as pools. The hits are {@link FireEaterOrdealGeometry}.
 */
public final class FireEaterChoreography implements OrdealChoreography {
	public static final FireEaterChoreography INSTANCE = new FireEaterChoreography();
	public static final OrdealProfessor PROFESSOR = new OrdealProfessor("circus_fire_eater", "cinder_bellows",
			"ordeal/fire_eater", List.of(
					new Intervention("long_breath", Ramp.OPENING, FireEaterChoreography.BREATH_IMPACT, 120),
					new Intervention("spat_embers", Ramp.OPENING, FireEaterChoreography.SPIT_IMPACT, 100),
					new Intervention("ring_of_fire", Ramp.PRESSED, FireEaterChoreography.RING_IMPACT, 180),
					new Intervention("cinder_rain", Ramp.DESPERATE, FireEaterChoreography.RAIN_IMPACT, 220)),
			INSTANCE, CinderBellowsOrdealMoves.MOVES);

	/** The Fire Breather's footprints (their block in {@link OrdealHazardKinds}); the Bellows' are 4054 to 4057. */
	public static final int KIND_BREATH = OrdealHazardKinds.FIRE_EATER, KIND_EMBER = KIND_BREATH + 1, KIND_RING = KIND_BREATH + 2,
			KIND_RAIN = KIND_BREATH + 3, KIND_POOL = KIND_BREATH + 8;
	/**
	 * Each move's beats, in ticks from its wind-up's start: the breath is loosed at {@link #BREATH_IMPACT}; the first
	 * ember lands at {@link #SPIT_IMPACT} and the next two {@link #SPIT_GAP} apart, each spat {@link #SPIT_FLIGHT}
	 * before it lands; the ring is lit at {@link #RING_IMPACT}; the rain erupts at {@link #RAIN_IMPACT} and its marks land
	 * {@link #RAIN_FALL} later, {@link #RAIN_GAP} apart.
	 */
	public static final int BREATH_IMPACT = 24, SPIT_IMPACT = 20, SPIT_GAP = 6, SPIT_FLIGHT = 6, RING_IMPACT = 30,
			RAIN_IMPACT = 30, RAIN_FALL = 8, RAIN_GAP = 4;
	/** How long a passed breath and a fallen cinder burn on the floor, and how often that fire bites. */
	public static final int BREATH_LINGER = 40, POOL_TICKS = 60, BURN_EVERY = 10, RING_HOLD = 30;
	private static final int BREATH_RUN = (int) Math.ceil(FireEaterOrdealGeometry.BREATH_LENGTH / FireEaterOrdealGeometry.BREATH_SPEED);
	private static final int REDRAW = OrdealInterventionsDriver.BAR_REDRAW;
	public static final float BREATH_DAMAGE = 6.0F, EMBER_DAMAGE = 4.0F, RING_DAMAGE = 5.0F, RING_TOUCH_DAMAGE = 3.0F,
			RAIN_DAMAGE = 4.0F, BURN_DAMAGE = 2.0F;

	/** The pulpit: the true self paces this far from the ring's centre, this far over its floor, swinging round north. */
	public static final double RADIUS = 15.0D, LIFT = 2.0D, SWING = 40.0D;
	/** Ticks one walk along the pulpit and back takes. */
	public static final double PERIOD = 2400.0D;
	/** How many times over the true self is drawn, and its mouth over its feet in model blocks. */
	public static final float SCALE = 3.0F;
	public static final double MOUTH = 2.45D;
	private static final float HEAD_TURN = 70.0F, HEAD_TILT = 40.0F;

	private FireEaterChoreography() {
	}

	/** Where the true self stands at {@code time}: on the pulpit, {@link #SWING} degrees either side of north. */
	public static Vec3 position(Vec3 floor, double time) {
		double bearing = Math.toRadians(SWING * Math.sin(time / PERIOD * Math.PI * 2.0D));
		return new Vec3(floor.x + Math.sin(bearing) * RADIUS, floor.y + LIFT, floor.z - Math.cos(bearing) * RADIUS);
	}

	/** The drawn mouth of a true self standing at {@code feet} and facing {@code yaw}. */
	public static Vec3 mouth(Vec3 feet, float yaw) {
		Vec3 ahead = Vec3.directionFromRotation(0.0F, yaw).scale(SCALE * 0.3D);
		return feet.add(ahead.x, SCALE * MOUTH, ahead.z);
	}

	@Override
	public Vec3 entrance(BlockPos stage) {
		return position(Vec3.atBottomCenterOf(stage).add(0, 1, 0), 0.0D);
	}

	@Override
	public void present(ServerLevel chamber, Entity professor, Vec3 floor, ServerPlayer caster, long now) {
		Vec3 at = position(floor, now);
		professor.setPos(at.x, at.y, at.z);
		face(professor, floor, caster.getEyePosition());
		// The figure smoulders where it stands: embers lifting off the coat and the flues.
		if (now % 4 == 0)
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.25F, 0.8F, 30), at.x, at.y + SCALE * 1.6D,
					at.z, 2, 0.6D, 1.2D, 0.6D, 0.02D);
	}

	@Override
	public Vec3 mark(Intervention move, ServerPlayer caster, Mob puppet, Vec3 floor) {
		Vec3 feet = new Vec3(caster.getX(), floor.y, caster.getZ());
		return switch (move.id()) {
			case "long_breath" -> feet;
			case "spat_embers" -> FireEaterOrdealGeometry.emberLead(feet, caster.getDeltaMovement(), floor);
			default -> floor;
		};
	}

	@Override
	public boolean run(ServerLevel chamber, ServerPlayer caster, Mob puppet, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, long now) {
		int age = active.age(now);
		Vec3 feet = caster.position();
		// Where the true self stood when the move began: the path is a pure function of time.
		Vec3 from = position(floor, active.startedAt);
		Vec3 mouth = professor == null ? from.add(0, SCALE * MOUTH, 0) : mouth(professor.position(), professor.getYHeadRot());
		if (age == 0) {
			if (professor instanceof CircusFireEaterEntity breather)
				breather.gesture(gesture(active.move.id()), reach(active, floor, from));
			// The breath drawn in: a long rasp as they fill.
			chamber.playSound(null, mouth.x, mouth.y, mouth.z, SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE,
					3.0F, .7F);
		}
		return switch (active.move.id()) {
			case "long_breath" -> longBreath(chamber, caster, professor, floor, active, age, feet, from, mouth);
			case "spat_embers" -> spatEmbers(chamber, caster, professor, floor, active, age, feet, from, mouth);
			case "ring_of_fire" -> ringOfFire(chamber, caster, professor, floor, active, age, feet, mouth);
			case "cinder_rain" -> cinderRain(chamber, caster, professor, floor, active, age, feet, mouth);
			default -> true;
		};
	}

	private boolean longBreath(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, Vec3 feet, Vec3 from, Vec3 mouth) {
		Vec3 start = FireEaterOrdealGeometry.breathStart(floor, active.mark, from);
		Vec3 direction = FireEaterOrdealGeometry.flat(active.mark.subtract(from));
		if (age == 0) lane(chamber, professor, start, direction, FireEaterOrdealGeometry.BREATH_LENGTH, BREATH_IMPACT);
		if (age < BREATH_IMPACT) {
			// Filling: the throat brightening, embers drawn up into the mouth, the far end of the lane smoking.
			CircusEffects.gather(chamber, mouth, CircusEffects.FLAME, age / (float) BREATH_IMPACT);
			if (age % 3 == 0) chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .35F, .7F, 16), start.x,
					start.y + .2D, start.z, 3, .5D, .1D, .5D, .01D);
			return false;
		}
		int since = age - BREATH_IMPACT;
		double front = FireEaterOrdealGeometry.breathFront(since + 1);
		if (since == 0) {
			chamber.playSound(null, start.x, start.y, start.z, SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.HOSTILE,
					4.0F, .55F);
			cast(chamber, start, .7F);
			CircusEffects.impact(chamber, start.add(0, .4, 0), 1.5D);
		}
		if (since % REDRAW == 0) lane(chamber, professor, start, direction, Math.max(.5D, front), REDRAW + 1);
		if (since < BREATH_RUN + 6) {
			// The tongue of flame poured from the mouth onto the rim, and the fire running down the lane.
			pour(chamber, mouth, start, 2);
			Vec3 tip = start.add(direction.scale(front));
			pour(chamber, start.add(0, .3, 0), tip.add(0, .3, 0), 1);
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, .9F, 1.0F, 10), tip.x, tip.y + .5D, tip.z,
					6, .6D, .4D, .6D, .04D);
		} else if (since % 2 == 0) {
			embersAlong(chamber, start, direction, FireEaterOrdealGeometry.BREATH_LENGTH);
		}
		if (!active.landed && FireEaterOrdealGeometry.inBreath(start, direction, front, feet)) {
			active.landed = true;
			strike(chamber, caster, professor, BREATH_DAMAGE, direction, .6D, .3D);
		} else if (since >= BREATH_RUN && since % BURN_EVERY == 0
				&& FireEaterOrdealGeometry.inBreath(start, direction, FireEaterOrdealGeometry.BREATH_LENGTH, feet)) {
			strike(chamber, caster, professor, BURN_DAMAGE, Vec3.ZERO, 0, 0);
		}
		return since >= BREATH_RUN + BREATH_LINGER;
	}

	private boolean spatEmbers(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, Vec3 feet, Vec3 from, Vec3 mouth) {
		List<Vec3> marks = FireEaterOrdealGeometry.emberMarks(active.mark, active.mark.subtract(from), floor);
		for (int i = 0; i < marks.size(); i++) {
			Vec3 mark = marks.get(i);
			int lands = SPIT_IMPACT + i * SPIT_GAP;
			if (age == 0)
				OrdealKit.hazard(chamber, professor, KIND_EMBER, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
						FireEaterOrdealGeometry.EMBER_RADIUS, BossFootprint.Height.STANDING), mark, mark, lands + 6);
			int flight = age - (lands - SPIT_FLIGHT);
			if (flight == 0) chamber.playSound(null, mouth.x, mouth.y, mouth.z, castSound(), SoundSource.HOSTILE, 2.0F, 1.3F);
			if (flight >= 0 && flight < SPIT_FLIGHT) {
				// The gob of burning spit in its arc: up out of the mouth, dropping hard onto the mark.
				float t = (flight + 1) / (float) SPIT_FLIGHT;
				Vec3 at = mouth.lerp(mark, t).add(0, 3.0D * 4.0D * t * (1.0F - t), 0);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, 1.2F, 1.0F, 8), at.x, at.y, at.z, 3, .1D,
						.1D, .1D, 0.0D);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .6F, .9F, 14), at.x, at.y, at.z, 4, .25D, .25D,
						.25D, .01D);
			}
			if (age == lands) {
				chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.5F, .9F);
				burst(chamber, mark, FireEaterOrdealGeometry.EMBER_RADIUS);
				if (FireEaterOrdealGeometry.emberHits(mark, feet))
					strike(chamber, caster, professor, EMBER_DAMAGE, feet.subtract(mark), .5D, .35D);
			}
		}
		return age >= SPIT_IMPACT + 2 * SPIT_GAP + 10;
	}

	private boolean ringOfFire(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, Vec3 feet, Vec3 mouth) {
		double open = FireEaterOrdealGeometry.RING_OPEN, closed = FireEaterOrdealGeometry.RING_CLOSED,
				band = FireEaterOrdealGeometry.RING_BAND;
		if (age == 0)
			OrdealKit.hazard(chamber, professor, KIND_RING, new BossFootprint.Ring(open, open, band, 1, 0, RING_IMPACT,
					BossFootprint.Height.STANDING), floor, floor, RING_IMPACT);
		if (age < RING_IMPACT) {
			// The rim catching: embers running round the curb, thicker as the breath is drawn.
			if (age % 2 == 0) flameRing(chamber, floor, open, age / (float) RING_IMPACT * .6F);
			CircusEffects.gather(chamber, mouth, CircusEffects.FLAME, age / (float) RING_IMPACT);
			return false;
		}
		int since = age - RING_IMPACT;
		if (since == 0) {
			OrdealKit.hazard(chamber, professor, KIND_RING, new BossFootprint.Ring(open, closed, band, 1, 0,
					FireEaterOrdealGeometry.RING_CLOSE_TICKS, BossFootprint.Height.STANDING), floor, floor,
					FireEaterOrdealGeometry.RING_CLOSE_TICKS + 1);
			chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.HOSTILE,
					5.0F, .45F);
			chamber.playSound(null, floor.x, floor.y, floor.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 4.0F, .5F);
			Vec3 near = floor.add(FireEaterOrdealGeometry.flat(mouth.subtract(floor)).scale(open));
			pour(chamber, mouth, near, 3);
		}
		if (since == FireEaterOrdealGeometry.RING_CLOSE_TICKS)
			OrdealKit.hazard(chamber, professor, KIND_RING, new BossFootprint.Ring(closed, closed - .01D, band, 1, 0, RING_HOLD,
					BossFootprint.Height.STANDING), floor, floor, RING_HOLD);
		if (since < 8) pour(chamber, mouth, floor.add(FireEaterOrdealGeometry.flat(mouth.subtract(floor)).scale(open)), 2);
		double radius = FireEaterOrdealGeometry.ringRadius(since);
		flameRing(chamber, floor, radius, 1.0F);
		if (FireEaterOrdealGeometry.ringHits(floor, radius, feet)) {
			Vec3 in = FireEaterOrdealGeometry.ringThrow(floor, feet);
			if (!active.landed) {
				active.landed = true;
				strike(chamber, caster, professor, RING_DAMAGE, in, 1.0D, .35D);
			} else if (since % BURN_EVERY == 0) {
				strike(chamber, caster, professor, RING_TOUCH_DAMAGE, in, .7D, .25D);
			}
		}
		return since >= FireEaterOrdealGeometry.RING_CLOSE_TICKS + RING_HOLD;
	}

	private boolean cinderRain(ServerLevel chamber, ServerPlayer caster, Entity professor, Vec3 floor,
			OrdealInterventionsDriver.Active active, int age, Vec3 feet, Vec3 mouth) {
		List<Vec3> marks = FireEaterOrdealGeometry.rainMarks(floor, active.startedAt);
		if (age == 0)
			for (int i = 0; i < marks.size(); i++)
				OrdealKit.hazard(chamber, professor, KIND_RAIN, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
						FireEaterOrdealGeometry.RAIN_RADIUS, BossFootprint.Height.STANDING), marks.get(i), marks.get(i),
						lands(i) + 6);
		if (age < RAIN_IMPACT) {
			CircusEffects.gather(chamber, mouth, CircusEffects.FLAME, age / (float) RAIN_IMPACT);
			CircusEffects.gather(chamber, mouth.add(0, .5, 0), CircusEffects.FLAME_CORE, age / (float) RAIN_IMPACT);
			return false;
		}
		if (age == RAIN_IMPACT) {
			chamber.playSound(null, mouth.x, mouth.y, mouth.z, SoundInit.ITEM_LIVING_TORCH_BREATH_LOOP.get(), SoundSource.HOSTILE,
					5.0F, .4F);
			cast(chamber, mouth, .5F);
		}
		if (age < RAIN_IMPACT + 10) {
			// The fountain: flame spewed straight up out of the mouth into the dark over the ring.
			CircusEffects.flameStream(chamber, mouth, new Vec3(0, 1, 0), 14, .6D, 2, .5D);
		}
		boolean burning = false;
		for (int i = 0; i < marks.size(); i++) {
			Vec3 mark = marks.get(i);
			int fall = age - (lands(i) - RAIN_FALL);
			if (fall >= 0 && fall < RAIN_FALL) {
				double height = 12.0D * (1.0D - (fall + 1) / (double) RAIN_FALL);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, 1.0F, 1.0F, 6), mark.x,
						mark.y + height, mark.z, 2, .15D, .3D, .15D, 0.0D);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .5F, .9F, 12), mark.x, mark.y + height + .8D,
						mark.z, 3, .25D, .6D, .25D, .0D);
			}
			if (age == lands(i)) {
				chamber.playSound(null, mark.x, mark.y, mark.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, .8F);
				burst(chamber, mark, FireEaterOrdealGeometry.RAIN_RADIUS);
				OrdealKit.hazard(chamber, professor, KIND_POOL, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN,
						FireEaterOrdealGeometry.RAIN_RADIUS, BossFootprint.Height.STANDING), mark, mark, POOL_TICKS);
				if (FireEaterOrdealGeometry.rainHits(mark, feet))
					strike(chamber, caster, professor, RAIN_DAMAGE, feet.subtract(mark), .5D, .3D);
			}
			if (age > lands(i) && age < lands(i) + POOL_TICKS) {
				if (age % 5 == i % 5) chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .45F, .8F, 18), mark.x,
						mark.y + .15D, mark.z, 2, .5D, .05D, .5D, .015D);
				burning |= FireEaterOrdealGeometry.rainHits(mark, feet);
			}
		}
		if (burning && age % BURN_EVERY == 0) strike(chamber, caster, professor, BURN_DAMAGE, Vec3.ZERO, 0, 0);
		return age >= lands(marks.size() - 1) + POOL_TICKS;
	}

	/** When cinder {@code i} lands, in ticks from the rain's wind-up. */
	public static int lands(int i) {
		return RAIN_IMPACT + RAIN_FALL + i * RAIN_GAP;
	}

	/** The breath's lane, laid from where it first touches the rim across the ring. */
	private static void lane(ServerLevel chamber, Entity professor, Vec3 start, Vec3 direction, double length, int life) {
		OrdealKit.hazard(chamber, professor, KIND_BREATH, new BossFootprint.Lane(length,
				FireEaterOrdealGeometry.BREATH_HALF_WIDTH, 0, 0.0D, 0.0D, 0.0D, 0, BossFootprint.Height.STANDING), start,
				start.add(direction), life);
	}

	/** A pour of flame from one point to another: the breath's body between the mouth and where it lands. */
	private static void pour(ServerLevel chamber, Vec3 from, Vec3 to, int density) {
		Vec3 span = to.subtract(from);
		int steps = Math.max(2, (int) (span.length() / .8D));
		CircusEffects.flameStream(chamber, from, span, steps, span.length() / steps, density, .35D);
	}

	private static void embersAlong(ServerLevel chamber, Vec3 start, Vec3 direction, double length) {
		for (double d = .5D; d < length; d += 1.5D) {
			Vec3 at = start.add(direction.scale(d));
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .4F, .85F, 16), at.x, at.y + .15D, at.z, 1,
					.6D, .05D, .6D, .02D);
		}
	}

	/** A ring of fire standing at {@code radius}: flame along the wall, its core where it is hottest. */
	private static void flameRing(ServerLevel chamber, Vec3 floor, double radius, float heat) {
		int points = Math.max(16, (int) (radius * 5.0D));
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2.0D * i / points;
			double x = floor.x + Math.cos(angle) * radius, z = floor.z + Math.sin(angle) * radius;
			chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .5F + .4F * heat, .9F, 14), x, floor.y + .3D, z,
					1, .15D, .35D * heat, .15D, .02D);
			if (heat > .8F && i % 3 == 0)
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, .7F, 1.0F, 8), x, floor.y + .8D, z, 1,
						.1D, .4D, .1D, .03D);
		}
	}

	/** Fire landing: a hot core thrown up, flame rolling out over the mark, blood cells in it. */
	private static void burst(ServerLevel chamber, Vec3 mark, double radius) {
		chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, 1.4F, 1.0F, 10), mark.x, mark.y + .5D, mark.z, 8,
				.3D, .3D, .3D, .08D);
		chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, .8F, .9F, 18), mark.x, mark.y + .3D, mark.z, 24,
				radius * .5D, .2D, radius * .5D, .06D);
		CircusEffects.impact(chamber, mark.add(0, .4, 0), radius);
	}

	private static void cast(ServerLevel chamber, Vec3 at, float pitch) {
		chamber.playSound(null, at.x, at.y, at.z, castSound(), SoundSource.HOSTILE, 3.0F, pitch);
	}

	private static SoundEvent castSound() {
		var cast = SoundInit.MANIPULATION_CAST.get("crimson_flame_conjuration");
		return cast == null ? SoundInit.RITE_STAFF_IMPACT.get() : cast.get();
	}

	@Override
	public int gestureFor(String move) {
		return gesture(move);
	}

	private static int gesture(String move) {
		return switch (move) {
			case "long_breath" -> CircusFireEaterEntity.GESTURE_BREATH;
			case "spat_embers" -> CircusFireEaterEntity.GESTURE_SPIT;
			case "ring_of_fire" -> CircusFireEaterEntity.GESTURE_RING;
			case "cinder_rain" -> CircusFireEaterEntity.GESTURE_RAIN;
			default -> -1;
		};
	}

	/** Where the gesture reaches: the breath's first touch on the rim, the first ember's mark, else the ring's centre. */
	private static Vec3 reach(OrdealInterventionsDriver.Active active, Vec3 floor, Vec3 from) {
		return switch (active.move.id()) {
			case "long_breath" -> FireEaterOrdealGeometry.breathStart(floor, active.mark, from);
			case "spat_embers" -> active.mark;
			default -> floor;
		};
	}

	/** Body square to the ring's centre, head on the fighter (at most {@link #HEAD_TURN} off the body). */
	private static void face(Entity professor, Vec3 centre, Vec3 fighter) {
		Vec3 eye = professor.position().add(0, SCALE * MOUTH, 0);
		float body = OrdealKit.yaw(eye, centre);
		float head = body + Math.clamp(Mth.wrapDegrees(OrdealKit.yaw(eye, fighter) - body), -HEAD_TURN, HEAD_TURN);
		double dx = fighter.x - eye.x, dy = fighter.y - eye.y, dz = fighter.z - eye.z;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		professor.setYRot(body);
		professor.setYBodyRot(body);
		professor.setYHeadRot(head);
		professor.setXRot(Math.clamp(pitch, -HEAD_TILT, HEAD_TILT));
	}

	private static void strike(ServerLevel chamber, ServerPlayer caster, Entity professor, float damage, Vec3 away, double push,
			double lift) {
		OrdealKit.strike(chamber, caster, professor, damage, away, push, lift);
	}
}
