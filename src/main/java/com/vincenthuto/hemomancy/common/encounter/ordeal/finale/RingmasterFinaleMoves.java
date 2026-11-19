package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHazardKinds;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealKit;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirector.Finale;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusEffects;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Ringmaster's moves in his finale (2026-11-18). Up in the rafters (phase 1) he barely moves himself: he blinks
 * between perches throwing knives with two fakes beside him, and turns the tent on the fighter, a string of lights, a
 * burning spotlight, a stampede of the phantom audience, and the carousel come alive ({@link CarouselAlive}). Every
 * move is a {@link BossMoveTimeline}, telegraphed on the floor from its first tick, its blow landing fast; the move and
 * its tick are synced on his copy for the client's poses and camera. He hunts only his finale's own player, and every
 * hit goes through {@link OrdealKit#strike} in his name. Every number is the builder's starting value for the owner.
 */
public final class RingmasterFinaleMoves {
	public static final int KIND_VOLLEY = OrdealHazardKinds.RINGMASTER, KIND_LIGHTS = KIND_VOLLEY + 1,
			KIND_SPOTLIGHT = KIND_VOLLEY + 2, KIND_RUSH = KIND_VOLLEY + 3;
	/** The beat between his moves' starts: 70 ticks at full health, quickening to 45 at half. */
	public static final int BEAT_FULL = 70, BEAT_HALF = 45;
	/** The least rest after a move, and the wait before his first once the rafters open. */
	public static final int REST = 12, FIRST_MOVE = 24;

	public static final float VOLLEY_DAMAGE = 5.0F;
	/** The knives fly this long from the hand to the floor; he drops to a low perch this long after the throw. */
	public static final int VOLLEY_FLIGHT = 3, VOLLEY_DROP = 6, VOLLEY_WINDOW = 40;
	/** A fake's knife lands this far to the side of where his lands: standing in a fake's lane is safe. */
	public static final double FAKE_SIDE = 2.4D;

	public static final float LIGHTS_DAMAGE = 3.0F;
	public static final double LIGHTS_HALF = 0.7D, LIGHTS_BEND = 3.5D, LIGHTS_OVERSHOOT = 3.0D;
	public static final int LIGHTS_SEGMENTS = 12, LIGHTS_ROOT = 30;

	public static final float SPOTLIGHT_DAMAGE = 6.0F;
	public static final double SPOTLIGHT_RADIUS = 2.5D, SPOTLIGHT_FOLLOW = 0.3D;
	public static final int SPOTLIGHT_TRACK = 20, SPOTLIGHT_BURN = 30, SPOTLIGHT_REDRAW = 2, SPOTLIGHT_FIRE_SECONDS = 3,
			SPOTLIGHT_BLIND = 40;
	/** The pool's floor stays scorched this long after the burn. */
	public static final int SPOTLIGHT_SCORCH = 8;

	public static final float RUSH_DAMAGE = 3.0F;
	public static final double RUSH_LANE = 18.0D, RUSH_HALF = 1.2D, RUSH_SPACING = 3.0D, RUSH_SPEED = 3.0D;
	public static final int RUSH_GATHER = 20, RUSH_STAGGER = 6, RUSH_LANES = 3, RUSH_DOLLS = 2, RUSH_MAX_DOLLS = 4;
	/** Where his dolls come in: the ring's curb plus a step. */
	public static final double RING_EDGE = 7.5D;

	/** The reveal: he crouches on his perch, then leaps to the ring floor, landing on this tick of the phase. */
	public static final int LEAP_CROUCH = 8, LEAP_FLIGHT = 10, LEAP_LAND = LEAP_CROUCH + LEAP_FLIGHT;
	public static final double LEAP_HEIGHT = 4.0D, LEAP_FROM_HUB = 4.5D;
	public static final int LANDED_REST = 30;
	/** On foot he closes on the fighter at this pace, to this distance. */
	public static final double WALK_SPEED = 1.15D, WALK_CLOSE = 2.6D;

	public static final int STAFF_WINDUP = 14;
	public static final double STAFF_REACH = 3.2D, STAFF_DEGREES = 100.0D;
	public static final float STAFF_DAMAGE = 7.0F;

	public static final int SWING_WINDUP = 12, SWING_TICKS = 20, SWING_SEGMENTS = 20;
	public static final double SWING_RADIUS = 3.5D, SWING_BAND = 1.2D, SWING_DEGREES = 300.0D, SWING_LEAD = 150.0D;
	public static final float SWING_DAMAGE = 6.0F;

	public static final int CASCADE_WINDUP = 12, CASCADE_GAP = 6, CASCADE_DROPS = 6;
	public static final double CASCADE_RADIUS = 1.6D, CASCADE_STEP = 1.4D;
	public static final float CASCADE_DAMAGE = 5.0F;

	public static final int PULL_WINDUP = 20;
	public static final double PULL_HALF = 0.8D, PULL_DISTANCE = 5.0D, PULL_NEAREST = 1.8D;
	/** After a pull lands the staff strike follows with only this much of its wind-up left. */
	public static final int PULL_STRIKE_LEAD = 5;

	/** His lines through the finale, said to its player (Task 12; his sheet in docs/NPC_VOICES.md). */
	public static final String LINE_OPENING = "hemomancy.circus.finale.line.opening", LINE_SEAM = "hemomancy.circus.finale.line.seam",
			LINE_REVEAL = "hemomancy.circus.finale.line.reveal", LINE_CASCADE = "hemomancy.circus.finale.line.cascade",
			LINE_PULL = "hemomancy.circus.finale.line.pull", LINE_KNEEL = "hemomancy.circus.finale.line.kneel",
			LINE_REFUSED = "hemomancy.circus.finale.line.refused", LINE_KILLED = "hemomancy.circus.finale.line.killed";
	/** The first-time prompt at the kneel stays up this long; the flag that it has been seen, in the circus progress state. */
	public static final int PROMPT_TICKS = 140;
	public static final String PROMPT_SEEN = "finale.kneel_prompt_seen";

	public static final int KIND_STAFF = KIND_VOLLEY + 7, KIND_SWING = KIND_VOLLEY + 8, KIND_CASCADE = KIND_VOLLEY + 9,
			KIND_PULL = KIND_VOLLEY + 10;

	/** His moves and the carousel's, in one list: the synced id is the ordinal. */
	public enum Move {
		NONE("none", null, 0, 0, CarouselAlive.Move.NONE, false),
		RAFTER_VOLLEY("rafter_volley", new BossMoveTimeline(16, VOLLEY_FLIGHT + VOLLEY_DROP, VOLLEY_WINDOW), 3, 0,
				CarouselAlive.Move.NONE, false),
		STRING_LIGHTS("string_lights", new BossMoveTimeline(20, 4, 12), 2, 1, CarouselAlive.Move.NONE, false),
		BURNING_SPOTLIGHT("burning_spotlight", new BossMoveTimeline(SPOTLIGHT_BURN, 4, 10), 2, 1, CarouselAlive.Move.NONE, false),
		AUDIENCE_RUSH("audience_rush", new BossMoveTimeline(RUSH_GATHER, RUSH_STAGGER * RUSH_LANES + 6, 8), 2, 1,
				CarouselAlive.Move.NONE, false),
		HORSE_VOLLEY("horse_volley", CarouselAlive.Move.HORSE_VOLLEY.timeline(), 2, 1, CarouselAlive.Move.HORSE_VOLLEY, false),
		HORSE_SPIRAL("horse_spiral", CarouselAlive.Move.HORSE_SPIRAL.timeline(), 2, 1, CarouselAlive.Move.HORSE_SPIRAL, false),
		POLE_SLAM("pole_slam", CarouselAlive.Move.POLE_SLAM.timeline(), 2, 1, CarouselAlive.Move.POLE_SLAM, false),
		// Phase 2, on the ring floor in his true self (2026-11-18).
		STAFF_STRIKE("staff_strike", new BossMoveTimeline(STAFF_WINDUP, 3, 12), 0, 4, CarouselAlive.Move.NONE, false),
		CAROUSEL_SWING("carousel_swing", new BossMoveTimeline(SWING_WINDUP, SWING_TICKS, 12), 0, 3, CarouselAlive.Move.NONE, false),
		ECHO_CUE("echo_cue", new BossMoveTimeline(FinaleEchoCue.STEP_IN, 1, 10), 0, 3, CarouselAlive.Move.NONE, false),
		CRIMSON_CASCADE("crimson_cascade", new BossMoveTimeline(CASCADE_WINDUP, CASCADE_GAP * CASCADE_DROPS, 10), 0, 4,
				CarouselAlive.Move.NONE, true),
		MARIONETTE_PULL("marionette_pull", new BossMoveTimeline(PULL_WINDUP, 2, 8), 0, 4, CarouselAlive.Move.NONE, true);

		private final String id;
		@Nullable
		private final BossMoveTimeline timeline;
		/** How likely he picks it up in the rafters, and down on the ring. */
		private final int weight, descentWeight;
		private final CarouselAlive.Move carousel;
		/** Paid for with the blood his bats bring him. */
		private final boolean empowered;

		Move(String id, @Nullable BossMoveTimeline timeline, int weight, int descentWeight, CarouselAlive.Move carousel,
				boolean empowered) {
			this.id = id;
			this.timeline = timeline;
			this.weight = weight;
			this.descentWeight = descentWeight;
			this.carousel = carousel;
			this.empowered = empowered;
		}

		public boolean empowered() {
			return empowered;
		}

		public String id() {
			return id;
		}

		public BossMoveTimeline timeline() {
			return timeline;
		}

		/** The carousel's move this is, or NONE for his own. */
		public CarouselAlive.Move carousel() {
			return carousel;
		}

		public static Move byId(int id) {
			Move[] all = values();
			return id >= 0 && id < all.length ? all[id] : NONE;
		}

		public static Optional<Move> byName(String id) {
			for (Move move : values()) if (move.id.equals(id) && move != NONE) return Optional.of(move);
			return Optional.empty();
		}
	}

	/** One finale's moves as they run, held on its {@link Finale}. */
	public static final class State {
		Move move = Move.NONE, last = Move.NONE;
		@Nullable
		Move forced;
		int tick, cooldown = FIRST_MOVE;
		/** The perch he stands on (index into {@link FinaleStage#HIGH_PERCHES}), and whether he has dropped low. */
		int perch = -1;
		boolean low;
		/** The move's marks: lanes' starts and ends, the curve, the spotlight's pool. */
		Vec3[] froms = new Vec3[0], tos = new Vec3[0], spots = new Vec3[0];
		List<Vec3> curve = List.of();
		Vec3 aim = Vec3.ZERO, floor = Vec3.ZERO, facing = Vec3.ZERO;
		boolean landed;
		int hitMask;
		/** The phase the moves last ran in: a change drops a running move. */
		FinalePhase seen = FinalePhase.CROSSING;
		/** The reveal's leap, from his perch to the ring. */
		@Nullable
		Vec3 leapFrom, leapTo;
		/** The swing's start round the pole, degrees. */
		double swingFrom;
		/** The echo he has called onto the ring, and which passed professor he calls next. */
		@Nullable
		FinaleEchoCue.Echo echo;
		int nextEcho;

		public Move move() {
			return move;
		}

		/** Whether one of his echoes is out on the ring. */
		public boolean echoing() {
			return echo != null;
		}
	}

	private RingmasterFinaleMoves() {
	}

	/** The beat between his moves at {@code health} (a fraction): 70 at full, 45 at half and below. */
	public static int beat(float health) {
		float t = Mth.clamp((health - FinaleRules.DESCENT_AT) / (1.0F - FinaleRules.DESCENT_AT), 0.0F, 1.0F);
		return Math.round(HemoEasing.lerp((float) BEAT_HALF, (float) BEAT_FULL, t));
	}

	/** A review or test hook: his next move is {@code id} (a move's id), once the running one is over. */
	public static boolean forceNext(UUID player, String id) {
		Optional<Move> move = Move.byName(id);
		Optional<Finale> finale = RingmasterFinaleDirector.finale(player);
		if (move.isEmpty() || finale.isEmpty()) return false;
		finale.get().moves().forced = move.get();
		if (finale.get().moves().move == Move.NONE) finale.get().moves().cooldown = Math.min(finale.get().moves().cooldown, 1);
		return true;
	}

	/** A review or test hook: no move of his own choosing starts for at least {@code ticks} more ticks. */
	public static void hold(UUID player, int ticks) {
		RingmasterFinaleDirector.finale(player).ifPresent(finale -> {
			State state = finale.moves();
			if (state.move == Move.NONE && state.forced == null) state.cooldown = Math.max(state.cooldown, ticks);
		});
	}

	/** His copy's tick on its stage: the finale's phase decides what he does; in the rafters he runs his moves. */
	public static void tick(ServerLevel chamber, CircusRingmasterEntity copy) {
		Optional<Map.Entry<UUID, Finale>> owned = RingmasterFinaleDirector.ofCopy(copy);
		if (owned.isEmpty()) return;
		Finale finale = owned.get().getValue();
		State state = finale.moves();
		ServerPlayer player = chamber.getServer().getPlayerList().getPlayer(owned.get().getKey());
		CircusCarouselEntity wheel = chamber.getEntity(finale.carousel()) instanceof CircusCarouselEntity found ? found : null;
		FinalePhase phase = finale.phase();
		boolean present = player != null && player.isAlive() && player.level() == chamber && wheel != null;
		if (phase != state.seen) {
			// Coming down drops whatever he ran in the rafters; the reveal runs on into the descent.
			if (state.move != Move.NONE && !(state.seen == FinalePhase.REVEAL && phase == FinalePhase.DESCENT)) stop(copy, wheel, state);
			state.seen = phase;
		}
		boolean down = phase == FinalePhase.REVEAL || phase == FinalePhase.DESCENT;
		if (present && (down || phase == FinalePhase.KNEEL)) {
			FinaleSwarm.tick(chamber, player, copy, finale, down);
			if (state.echo != null && (!down || FinaleEchoCue.tick(chamber, player, copy, state.echo, finale.floor()))) {
				if (!down) FinaleEchoCue.dismiss(chamber, state.echo);
				state.echo = null;
			}
		}
		if (present && phase == FinalePhase.KNEEL) {
			if (state.move != Move.NONE) stop(copy, wheel, state);
			kneel(chamber, copy, wheel, player, finale);
			return;
		}
		if (!present || !(phase == FinalePhase.RAFTERS || down)) {
			if (state.move != Move.NONE) stop(copy, wheel, state);
			copy.getNavigation().stop();
			return;
		}
		state.floor = finale.floor();
		if (phase == FinalePhase.RAFTERS && finale.phaseTicks() == 1) say(player, LINE_OPENING);
		if (phase == FinalePhase.RAFTERS && finale.phaseTicks() == FinaleRules.VEIL_OPENING_TICKS) say(player, LINE_SEAM);
		if (phase == FinalePhase.REVEAL && !reveal(chamber, copy, player, finale, state)) return;
		if (down) onFoot(chamber, copy, player, finale, state);
		else {
			copy.getNavigation().stop();
			OrdealKit.face(copy, player.position().subtract(copy.position()));
		}
		if (state.perch < 0) state.perch = nearestHigh(finale, copy.position());
		if (state.move == Move.NONE) {
			if (--state.cooldown > 0) return;
			Move next = pick(copy.getRandom(), state, down, finale, player);
			if (next.empowered() && !FinaleSwarm.spend(finale)) next = Move.STAFF_STRIKE;
			start(copy, wheel, player, state, next);
		}
		boolean over = run(chamber, copy, wheel, player, finale, state);
		state.tick++;
		copy.syncFinaleMove(state.move.ordinal(), state.tick);
		if (state.move.carousel() != CarouselAlive.Move.NONE) wheel.tickAlive(state.tick);
		if (over || state.tick >= state.move.timeline().total()) finish(chamber, copy, wheel, finale, state);
	}

	private static Move pick(RandomSource random, State state, boolean down, Finale finale, ServerPlayer player) {
		if (state.forced != null) {
			Move forced = state.forced;
			state.forced = null;
			return forced;
		}
		int total = 0;
		for (Move move : Move.values()) total += weight(move, state, down, finale, player);
		if (total <= 0) return down ? Move.STAFF_STRIKE : Move.RAFTER_VOLLEY;
		int roll = random.nextInt(total);
		for (Move move : Move.values()) {
			roll -= weight(move, state, down, finale, player);
			if (roll < 0) return move;
		}
		return down ? Move.STAFF_STRIKE : Move.RAFTER_VOLLEY;
	}

	/** How likely {@code move} is next: by phase, never twice running, an echo only with none out, empowered only charged. */
	private static int weight(Move move, State state, boolean down, Finale finale, ServerPlayer player) {
		if (move == Move.NONE || move == state.last) return 0;
		if (move == Move.ECHO_CUE && (state.echo != null || FinaleEchoes.passed(player).isEmpty())) return 0;
		if (move.empowered && !FinaleRules.mayEmpower(finale.swarm().charge())) return 0;
		return down ? move.descentWeight : move.weight;
	}

	private static void start(CircusRingmasterEntity copy, CircusCarouselEntity wheel, ServerPlayer player, State state, Move move) {
		state.move = move;
		state.tick = 0;
		state.landed = false;
		state.hitMask = 0;
		if (move.carousel() == CarouselAlive.Move.HORSE_VOLLEY)
			wheel.beginAlive(move.carousel(), RingmasterFinaleGeometry.yawToward(wheel.position(), player.position()));
		else if (move.carousel() == CarouselAlive.Move.HORSE_SPIRAL)
			wheel.beginAlive(move.carousel(), CarouselAlive.spiralFire(wheel.aliveYaw(wheel.aliveTime(0.0F))));
		else if (move.carousel() != CarouselAlive.Move.NONE) wheel.beginAlive(move.carousel(), 0.0F);
		Hemomancy.LOGGER.debug("Ringmaster finale move {}", move.id());
	}

	private static boolean run(ServerLevel chamber, CircusRingmasterEntity copy, CircusCarouselEntity wheel, ServerPlayer player,
			Finale finale, State state) {
		int t = state.tick;
		return switch (state.move) {
			case RAFTER_VOLLEY -> volley(chamber, copy, player, finale, state, t);
			case STRING_LIGHTS -> lights(chamber, copy, player, finale, state, t);
			case BURNING_SPOTLIGHT -> spotlight(chamber, copy, player, state, t);
			case AUDIENCE_RUSH -> rush(chamber, copy, player, finale, state, t);
			case STAFF_STRIKE -> staff(chamber, copy, player, state, t);
			case CAROUSEL_SWING -> swing(chamber, copy, wheel, player, state, t);
			case ECHO_CUE -> cue(chamber, copy, player, finale, state, t);
			case CRIMSON_CASCADE -> cascade(chamber, copy, player, state, t);
			case MARIONETTE_PULL -> pull(chamber, copy, player, state, t);
			case HORSE_VOLLEY, HORSE_SPIRAL, POLE_SLAM -> {
				// He conducts the carousel from his perch: the cue's raised staff through its wind-up.
				copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
				yield CarouselAlive.run(chamber, player, copy, wheel, state.move.carousel(), t, state);
			}
			case NONE -> true;
		};
	}

	private static void finish(ServerLevel chamber, CircusRingmasterEntity copy, CircusCarouselEntity wheel, Finale finale, State state) {
		if (state.move == Move.RAFTER_VOLLEY && state.low) {
			// The melee window shuts: he vaults back up into the rigging.
			state.perch = otherHigh(copy.getRandom(), state.perch, -1);
			blink(chamber, copy, perch(finale, FinaleStage.HIGH_PERCHES.get(state.perch)));
			state.low = false;
		}
		if (state.move.carousel() != CarouselAlive.Move.NONE) wheel.endAlive();
		float health = copy.getHealth() / copy.getMaxHealth();
		state.cooldown = Math.max(REST, beat(health) - state.move.timeline().total());
		state.last = state.move;
		state.move = Move.NONE;
		state.tick = 0;
		state.facing = Vec3.ZERO;
		copy.syncFinaleMove(0, 0);
		copy.posePlay(CircusRingmasterMoves.Move.NONE, 0);
	}

	/** The phase moved on under a running move: it is dropped where it stands. */
	private static void stop(CircusRingmasterEntity copy, @Nullable CircusCarouselEntity wheel, State state) {
		copy.clearFakes(true);
		if (wheel != null) wheel.endAlive();
		state.move = Move.NONE;
		state.low = false;
		state.tick = 0;
		state.facing = Vec3.ZERO;
		copy.syncFinaleMove(0, 0);
		copy.posePlay(CircusRingmasterMoves.Move.NONE, 0);
	}

	// --- rafter_volley ----------------------------------------------------------------------------------------------

	/**
	 * He blinks to a high perch and two fakes stand on two others; all three draw knives through a 16-tick gather and
	 * throw down three lanes. Only his lands. His real lane runs through the fighter; each fake's runs a little to one
	 * side, so a fighter who reads the fakes (no shadow, no thread from the rigging) can stand in a fake's lane. A shot
	 * through a fake bursts it. Then he drops to a low perch on the curb for a melee window.
	 */
	private static boolean volley(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state,
			int t) {
		int windup = Move.RAFTER_VOLLEY.timeline().windup();
		int land = windup + VOLLEY_FLIGHT, drop = windup + VOLLEY_DROP;
		Vec3 floor = state.floor;
		if (t == 0) {
			RandomSource random = copy.getRandom();
			int real = otherHigh(random, state.perch, -1);
			int fakeA = otherHigh(random, real, state.perch), fakeB = otherHigh(random, real, fakeA);
			if (fakeB == fakeA) fakeB = otherHigh(random, fakeA, real);
			Vec3 at = perch(finale, FinaleStage.HIGH_PERCHES.get(real));
			blink(chamber, copy, at);
			state.perch = real;
			Vec3 a = perch(finale, FinaleStage.HIGH_PERCHES.get(fakeA)), b = perch(finale, FinaleStage.HIGH_PERCHES.get(fakeB));
			copy.showFakes(a, b);
			CircusEffects.vault(chamber, a);
			CircusEffects.vault(chamber, b);
			Vec3 feet = onFloor(player.position(), floor);
			Vec3[] throwers = {at, a, b};
			state.froms = new Vec3[3];
			state.tos = new Vec3[3];
			state.spots = new Vec3[3];
			for (int k = 0; k < 3; k++) {
				Vec3 base = onFloor(throwers[k], floor);
				Vec3 dir = OrdealKit.flat(feet.subtract(base));
				if (dir.lengthSqr() < 1.0E-6D) dir = new Vec3(0.0D, 0.0D, 1.0D);
				Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
				Vec3 target = k == 0 ? feet : feet.add(side.scale(k == 1 ? FAKE_SIDE : -FAKE_SIDE));
				double before = Math.min(10.0D, OrdealKit.horizontal(base, target));
				state.spots[k] = target;
				state.froms[k] = target.subtract(dir.scale(before));
				state.tos[k] = state.froms[k].add(dir.scale(RingmasterFinaleGeometry.VOLLEY_LANE));
				CarouselAlive.lane(chamber, copy, KIND_VOLLEY, state.froms[k], state.tos[k], RingmasterFinaleGeometry.VOLLEY_HALF,
						windup);
			}
			chamber.playSound(null, at.x, at.y, at.z, SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 2.5F, 0.8F);
			chamber.playSound(null, at.x, at.y, at.z, SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE, 2.5F, 1.2F);
		}
		// The throw's pose, its release on the knives' release (the old throw commits at 12).
		int release = CircusRingmasterMoves.Move.KNIFE_THROW.timeline().commitTick();
		copy.posePlay(t >= windup - release && t < drop ? CircusRingmasterMoves.Move.KNIFE_THROW : CircusRingmasterMoves.Move.NONE,
				t - (windup - release));
		if (t < windup) {
			float charge = t / (float) windup;
			gatherAt(chamber, copy.position(), charge);
			for (int k = 0; k < 2; k++) {
				Vec3 fake = copy.fakeAt(k);
				if (fake == null) continue;
				gatherAt(chamber, fake, charge);
				if (shotThrough(chamber, player, fake)) copy.burstFake(k);
			}
			return false;
		}
		if (t == windup) {
			chamber.playSound(null, copy.getX(), copy.getY() + 2.0D, copy.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE,
					3.0F, 1.1F);
			for (int k = 0; k < 3; k++) {
				Vec3 hand = thrower(copy, k);
				if (hand == null) continue;
				CarouselAlive.lane(chamber, copy, KIND_VOLLEY, state.froms[k], state.tos[k], RingmasterFinaleGeometry.VOLLEY_HALF,
						VOLLEY_FLIGHT + 1);
			}
		}
		if (t >= windup && t <= land) {
			// The knives in flight from each hand to where its lane meets the fighter: his in blood, the fakes' in bone.
			for (int k = 0; k < 3; k++) {
				Vec3 hand = thrower(copy, k);
				if (hand == null) continue;
				Vec3 spot = landing(state, k);
				float from = (t - windup) / (float) (VOLLEY_FLIGHT + 1), to = (t - windup + 1) / (float) (VOLLEY_FLIGHT + 1);
				streak(chamber, hand.add(0.0D, 2.0D, 0.0D), spot.add(0.0D, 0.6D, 0.0D), from, to, k == 0);
			}
		}
		if (t == land) {
			Vec3 feet = player.position();
			for (int k = 0; k < 3; k++) {
				if (thrower(copy, k) == null) continue;
				Vec3 spot = landing(state, k);
				if (k == 0) {
					chamber.playSound(null, spot.x, spot.y, spot.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.0F, 1.1F);
					CircusEffects.impact(chamber, spot.add(0.0D, 0.4D, 0.0D), 0.8D);
					CircusEffects.lineFootprint(chamber, state.froms[0], state.tos[0], CircusEffects.CRIMSON);
					if (RingmasterFinaleGeometry.laneHits(state.froms[0], state.tos[0], RingmasterFinaleGeometry.VOLLEY_HALF, feet)) {
						state.landed = true;
						OrdealKit.strike(chamber, player, copy, VOLLEY_DAMAGE, state.tos[0].subtract(state.froms[0]), 0.5D, 0.2D);
					}
				} else {
					// A fake's knife is nothing: it comes apart in bone where it would have landed.
					BossTelegraphs.spores(chamber, spot.add(0.0D, 0.5D, 0.0D), CircusEffects.BONE, 10, 0.4D, 0.03D);
				}
			}
			copy.clearFakes(true);
		}
		if (t == drop) {
			// The melee window: down onto the curb nearest the fighter.
			state.low = true;
			blink(chamber, copy, perch(finale, nearestLow(finale, player.position())));
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 2.5F, 1.0F);
			CircusEffects.dust(chamber, copy.position(), 14);
		}
		return false;
	}

	/** The thrower {@code k} (0 is he, 1 and 2 his fakes) where he stands, or null for a fake that has burst. */
	@Nullable
	private static Vec3 thrower(CircusRingmasterEntity copy, int k) {
		return k == 0 ? copy.position() : copy.fakeAt(k - 1);
	}

	/** Where thrower {@code k}'s knife lands: the point on its lane it was aimed at. */
	private static Vec3 landing(State state, int k) {
		return state.spots[k];
	}

	/** Whether one of the fighter's own shots is passing through a fake. */
	private static boolean shotThrough(ServerLevel chamber, ServerPlayer player, Vec3 fake) {
		AABB body = new AABB(fake.x - 0.5D, fake.y, fake.z - 0.5D, fake.x + 0.5D, fake.y + 2.3D, fake.z + 0.5D);
		return !chamber.getEntitiesOfClass(Projectile.class, body.inflate(0.3D), shot -> shot.getOwner() == player).isEmpty();
	}

	private static void gatherAt(ServerLevel chamber, Vec3 feet, float charge) {
		CircusEffects.gather(chamber, feet.add(0.0D, 2.0D, 0.0D), CircusEffects.BONE, charge);
	}

	/** One stretch of a knife's flight, {@code from} to {@code to} of the way: a bright streak, his in blood. */
	private static void streak(ServerLevel chamber, Vec3 hand, Vec3 spot, float from, float to, boolean real) {
		Vec3 path = spot.subtract(hand);
		int points = 8;
		for (int i = 0; i <= points; i++) {
			Vec3 at = hand.add(path.scale(Mth.lerp(i / (float) points, from, to)));
			chamber.sendParticles(HemoParticleData.glow(real ? CircusEffects.CRIMSON : CircusEffects.BONE), at.x, at.y, at.z, 1,
					0.02D, 0.02D, 0.02D, 0.0D);
		}
		if (real) {
			Vec3 tip = hand.add(path.scale(to));
			chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), tip.x, tip.y, tip.z, 2, 0.05D, 0.05D, 0.05D, 0.02D);
		}
	}

	// --- string_lights ----------------------------------------------------------------------------------------------

	/**
	 * A living string of stage lights lowers from the rigging and snakes across the floor toward the fighter along a
	 * bowed curve, lit bulb by bulb through its 20-tick gather; then it snaps taut. Standing on it then roots the fighter.
	 */
	private static boolean lights(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state,
			int t) {
		int windup = Move.STRING_LIGHTS.timeline().windup();
		if (t == 0) {
			BlockPos cell = FinaleStage.HIGH_PERCHES.get(otherHigh(copy.getRandom(), state.perch, -1));
			Vec3 rig = perch(finale, cell);
			Vec3 base = onFloor(rig, state.floor);
			Vec3 feet = onFloor(player.position(), state.floor);
			Vec3 dir = OrdealKit.flat(feet.subtract(base));
			Vec3 end = feet.add(dir.scale(LIGHTS_OVERSHOOT));
			double bend = (copy.getRandom().nextBoolean() ? 1.0D : -1.0D) * LIGHTS_BEND;
			state.curve = RingmasterFinaleGeometry.curve(base, end, bend, LIGHTS_SEGMENTS);
			state.aim = rig;
			for (int i = 1; i < state.curve.size(); i++)
				CarouselAlive.lane(chamber, copy, KIND_LIGHTS, state.curve.get(i - 1), state.curve.get(i), LIGHTS_HALF, windup);
			BossTelegraphs.tendril(chamber, rig.add(0.0D, 1.0D, 0.0D), base.add(0.0D, 0.3D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
					CircusEffects.THREAD_GLOW_ARGB, copy.getId() * 53L + copy.tickCount);
			chamber.playSound(null, rig.x, rig.y, rig.z, SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 2.5F, 1.3F);
		}
		copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
		int segments = state.curve.size() - 1;
		if (t < windup) {
			// The bulbs light along the string one after another, the head of it crawling toward the fighter.
			int lit = Math.min(segments, Math.round((t + 1) / (float) windup * segments));
			for (int i = 0; i <= lit; i++) {
				Vec3 bulb = state.curve.get(i).add(0.0D, 0.15D + 0.08D * Math.sin(t * 0.7D + i), 0.0D);
				chamber.sendParticles(HemoParticleData.glow(i % 2 == 0 ? CircusEffects.FLAME_CORE : CircusEffects.BONE), bulb.x, bulb.y,
						bulb.z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
			}
			if (t % 4 == 0) chamber.playSound(null, state.curve.get(lit).x, state.curve.get(lit).y, state.curve.get(lit).z,
					SoundEvents.CHAIN_STEP, SoundSource.HOSTILE, 1.5F, 1.4F + 0.3F * t / windup);
			return false;
		}
		if (t == windup) {
			for (int i = 1; i < state.curve.size(); i++)
				CarouselAlive.lane(chamber, copy, KIND_LIGHTS, state.curve.get(i - 1), state.curve.get(i), LIGHTS_HALF, 4);
			for (int i = 3; i < state.curve.size(); i += 3)
				BossTelegraphs.tendril(chamber, state.curve.get(i - 3).add(0.0D, 0.2D, 0.0D), state.curve.get(i).add(0.0D, 0.2D, 0.0D),
						CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB, copy.getId() * 59L + i);
			Vec3 mid = state.curve.get(segments / 2);
			chamber.playSound(null, mid.x, mid.y, mid.z, SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 3.0F, 0.8F);
			chamber.playSound(null, mid.x, mid.y, mid.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 1.3F);
			Vec3 feet = player.position();
			if (RingmasterFinaleGeometry.curveHits(state.curve, LIGHTS_HALF, feet)) {
				state.landed = true;
				OrdealKit.strike(chamber, player, copy, LIGHTS_DAMAGE, Vec3.ZERO, 0.0D, 0.0D);
				MorphlingLock.pin(player, LIGHTS_ROOT);
				BossTelegraphs.tendril(chamber, feet.add(0.0D, 0.1D, 0.0D), feet.add(0.0D, 1.6D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
						CircusEffects.THREAD_GLOW_ARGB, copy.getId() * 61L + t);
			}
		}
		if (t > windup && t < windup + 8 && t % 2 == 0)
			for (Vec3 bulb : state.curve)
				chamber.sendParticles(HemoParticleData.glow(CircusEffects.FLAME), bulb.x, bulb.y + 0.2D, bulb.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
		return false;
	}

	// --- burning_spotlight ------------------------------------------------------------------------------------------

	/** A spotlight's pool hunts the fighter for 20 ticks, stops, and ten ticks later burns: fire and blindness. */
	private static boolean spotlight(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, State state, int t) {
		Vec3 feet = onFloor(player.position(), state.floor);
		if (t == 0) {
			state.aim = feet;
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(),
					SoundSource.HOSTILE, 2.5F, 0.7F);
		}
		copy.posePlay(CircusRingmasterMoves.Move.SPOTLIGHT, t);
		if (t < SPOTLIGHT_TRACK) {
			state.aim = state.aim.add(feet.subtract(state.aim).scale(SPOTLIGHT_FOLLOW));
			if (t % SPOTLIGHT_REDRAW == 0) {
				OrdealKit.hazard(chamber, copy, KIND_SPOTLIGHT, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, SPOTLIGHT_RADIUS,
						BossFootprint.Height.STANDING), state.aim, state.aim, SPOTLIGHT_REDRAW + 1);
				CircusEffects.spotlight(chamber, state.aim, SPOTLIGHT_RADIUS, 1.0F);
			}
			return false;
		}
		if (t == SPOTLIGHT_TRACK) {
			OrdealKit.hazard(chamber, copy, KIND_SPOTLIGHT, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, SPOTLIGHT_RADIUS,
					BossFootprint.Height.STANDING), state.aim, state.aim, SPOTLIGHT_BURN - SPOTLIGHT_TRACK + 1);
			chamber.playSound(null, state.aim.x, state.aim.y, state.aim.z, SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 2.5F, 0.6F);
		}
		if (t < SPOTLIGHT_BURN) {
			// The beam narrows its heat onto the pool: embers rising off the floor, faster toward the burn.
			if (t % 2 == 0) {
				CircusEffects.spotlight(chamber, state.aim, SPOTLIGHT_RADIUS, 2.0F);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.25F, 0.8F, 12), state.aim.x, state.aim.y + 0.2D,
						state.aim.z, 2 + (t - SPOTLIGHT_TRACK), SPOTLIGHT_RADIUS * 0.5D, 0.1D, SPOTLIGHT_RADIUS * 0.5D, 0.03D);
			}
			return false;
		}
		if (t == SPOTLIGHT_BURN) {
			chamber.playSound(null, state.aim.x, state.aim.y, state.aim.z, SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 3.0F, 0.6F);
			chamber.playSound(null, state.aim.x, state.aim.y, state.aim.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 3.0F, 0.7F);
			CircusEffects.flameStream(chamber, state.aim.add(0.0D, 6.0D, 0.0D), new Vec3(0.0D, -1.0D, 0.0D), 10, 0.6D, 3, 0.6D);
			for (int i = 0; i < 12; i++) {
				double angle = Math.PI * 2.0D * i / 12;
				Vec3 at = state.aim.add(Math.cos(angle) * SPOTLIGHT_RADIUS * 0.8D, 0.2D, Math.sin(angle) * SPOTLIGHT_RADIUS * 0.8D);
				chamber.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.35F, 0.9F, 16), at.x, at.y, at.z, 3, 0.2D,
						0.3D, 0.2D, 0.06D);
			}
			CircusEffects.impact(chamber, state.aim.add(0.0D, 0.4D, 0.0D), SPOTLIGHT_RADIUS);
			OrdealKit.hazard(chamber, copy, KIND_SPOTLIGHT, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, SPOTLIGHT_RADIUS,
					BossFootprint.Height.STANDING), state.aim, state.aim, SPOTLIGHT_SCORCH);
			if (RingmasterFinaleGeometry.discHits(state.aim, SPOTLIGHT_RADIUS, player.position())) {
				state.landed = true;
				OrdealKit.strike(chamber, player, copy, SPOTLIGHT_DAMAGE, player.position().subtract(state.aim), 0.3D, 0.2D);
				player.igniteForSeconds(SPOTLIGHT_FIRE_SECONDS);
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, SPOTLIGHT_BLIND), copy);
			}
		}
		return false;
	}

	// --- audience_rush ----------------------------------------------------------------------------------------------

	/**
	 * Three lanes of the phantom audience dash across the ring one after another, six ticks apart, the middle lane
	 * through the fighter; each catches the fighter in its way, and two of his dolls come in at the ring's edge.
	 */
	private static boolean rush(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state,
			int t) {
		if (t == 0) {
			RandomSource random = copy.getRandom();
			Vec3 feet = onFloor(player.position(), state.floor);
			double angle = random.nextDouble() * Math.PI * 2.0D;
			Vec3 dir = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
			Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
			// The sweep runs from one side to the other; which side first is a coin.
			double sweep = random.nextBoolean() ? 1.0D : -1.0D;
			state.froms = new Vec3[RUSH_LANES];
			state.tos = new Vec3[RUSH_LANES];
			for (int k = 0; k < RUSH_LANES; k++) {
				Vec3 mid = feet.add(side.scale((k - 1) * RUSH_SPACING * sweep));
				state.froms[k] = mid.subtract(dir.scale(RUSH_LANE / 2.0D));
				state.tos[k] = mid.add(dir.scale(RUSH_LANE / 2.0D));
				CarouselAlive.lane(chamber, copy, KIND_RUSH, state.froms[k], state.tos[k], RUSH_HALF, dashAt(k) + 1);
			}
			// The client's phantoms run from these: the middle lane's centre, and the heading with the sweep's side in y.
			copy.setFinaleMarks(feet, new Vec3(dir.x, sweep, dir.z));
			chamber.playSound(null, feet.x, feet.y, feet.z, SoundInit.ENTITY_ENTHRALLED_DOLL_AMBIENT.get(), SoundSource.HOSTILE, 3.0F, 0.6F);
			callDolls(chamber, copy, player, finale);
		}
		copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
		Vec3 feet = player.position();
		for (int k = 0; k < RUSH_LANES; k++) {
			int run = t - dashAt(k);
			if (run < 0) continue;
			Vec3 dir = state.tos[k].subtract(state.froms[k]).normalize();
			if (run == 0 || run == 3) CarouselAlive.lane(chamber, copy, KIND_RUSH, state.froms[k], state.tos[k], RUSH_HALF, 4);
			if (run == 0) chamber.playSound(null, state.froms[k].x, state.froms[k].y, state.froms[k].z, SoundEvents.HORSE_GALLOP,
					SoundSource.HOSTILE, 3.0F, 1.4F);
			double front = run * RUSH_SPEED;
			if (front > RUSH_LANE + RUSH_SPEED) continue;
			Vec3 at = state.froms[k].add(dir.scale(Math.min(front, RUSH_LANE)));
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), at.x, at.y + 0.8D, at.z, 6, 0.6D, 0.6D, 0.6D, 0.02D);
			CircusEffects.dust(chamber, at, 4);
			if ((state.hitMask & (1 << k)) != 0) continue;
			Vec3 back = state.froms[k].add(dir.scale(Math.max(0.0D, front - RUSH_SPEED - 1.0D)));
			Vec3 ahead = state.froms[k].add(dir.scale(Math.min(RUSH_LANE, front + 0.5D)));
			if (RingmasterFinaleGeometry.laneHits(back, ahead, RUSH_HALF, feet)) {
				state.hitMask |= 1 << k;
				OrdealKit.strike(chamber, player, copy, RUSH_DAMAGE, dir, 0.9D, 0.35D);
			}
		}
		return false;
	}

	/** The tick lane {@code k} of the rush dashes on. */
	public static int dashAt(int k) {
		return RUSH_GATHER + k * RUSH_STAGGER;
	}

	/** Two of his dolls come in at the ring's edge, never more than four of them about. */
	private static void callDolls(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale) {
		Vec3 floor = finale.floor();
		AABB stage = new AABB(floor, floor).inflate(40.0D, 16.0D, 40.0D);
		int alive = chamber.getEntitiesOfClass(EnthralledDollEntity.class, stage,
				doll -> doll.isAlive() && doll.getTags().contains(RingmasterFinaleDirector.COPY_TAG)).size();
		RandomSource random = copy.getRandom();
		for (int i = 0; i < Math.min(RUSH_DOLLS, RUSH_MAX_DOLLS - alive); i++) {
			EnthralledDollEntity doll = EntityInit.enthralled_doll.get().create(chamber);
			if (doll == null) return;
			double angle = random.nextDouble() * Math.PI * 2.0D;
			Vec3 at = floor.add(Math.cos(angle) * RING_EDGE, 0.0D, Math.sin(angle) * RING_EDGE);
			doll.moveTo(at.x, at.y, at.z, (float) Math.toDegrees(angle) + 90.0F, 0.0F);
			doll.addTag(RingmasterFinaleDirector.COPY_TAG);
			doll.setPersistenceRequired();
			chamber.addFreshEntity(doll);
			doll.setTarget(player);
			CircusEffects.vault(chamber, at);
		}
	}

	// --- the reveal and the descent ---------------------------------------------------------------------------------

	/**
	 * The reveal: the phase card, a crouch on his perch with his landing marked on the ring, then the leap down in an
	 * arc and a landing that shakes the floor. False until he is down, so no move starts in the air.
	 */
	private static boolean reveal(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state) {
		int t = finale.phaseTicks();
		Vec3 floor = state.floor;
		if (state.leapFrom == null) {
			state.leapFrom = copy.position();
			Vec3 toward = OrdealKit.flat(player.position().subtract(floor));
			if (toward.lengthSqr() < 1.0E-6D) toward = new Vec3(0.0D, 0.0D, -1.0D);
			state.leapTo = floor.add(toward.scale(LEAP_FROM_HUB));
			com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes.playTo(player, copy,
					com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds.SCENE_CIRCUS_DESCENT_CARD, 0);
			say(player, LINE_REVEAL);
			OrdealKit.hazard(chamber, copy, KIND_CASCADE, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 2.0D,
					BossFootprint.Height.STANDING), state.leapTo, state.leapTo, LEAP_LAND + 1);
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(),
					SoundSource.HOSTILE, 2.5F, 0.8F);
		}
		copy.noPhysics = true;
		copy.setNoGravity(true);
		copy.getNavigation().stop();
		if (t >= LEAP_LAND + 6) return true;
		OrdealKit.face(copy, state.leapTo.subtract(copy.position()));
		if (t < LEAP_CROUCH) {
			copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
			if (t % 2 == 0) CircusEffects.gather(chamber, copy.position().add(0.0D, 1.2D, 0.0D), CircusEffects.CRIMSON, t / (float) LEAP_CROUCH);
			return false;
		}
		// Out of the crouch his pose is let go: a pose left held is snapped back on the client every few ticks.
		if (t == LEAP_CROUCH) copy.posePlay(CircusRingmasterMoves.Move.NONE, 0);
		if (t < LEAP_LAND) {
			// The leap: fast and falling, the arc topping out early.
			float s = (t - LEAP_CROUCH + 1) / (float) LEAP_FLIGHT;
			Vec3 at = state.leapFrom.lerp(state.leapTo, HemoEasing.easeInCubic(s) * 0.6D + s * 0.4D)
					.add(0.0D, LEAP_HEIGHT * Math.sin(Math.PI * Math.min(1.0D, s * 1.15D)) * (1.0D - s), 0.0D);
			// Moved, not teleported: a teleport each tick reaches the client unsmoothed.
			copy.setPos(at.x, at.y, at.z);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), at.x, at.y + 1.0D, at.z, 3, 0.3D, 0.5D, 0.3D, 0.0D);
			return false;
		}
		if (t == LEAP_LAND) {
			copy.setPos(state.leapTo.x, state.leapTo.y, state.leapTo.z);
			copy.fallDistance = 0.0F;
			chamber.playSound(null, state.leapTo.x, state.leapTo.y, state.leapTo.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE,
					4.0F, 0.6F);
			chamber.playSound(null, state.leapTo.x, state.leapTo.y, state.leapTo.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE,
					3.0F, 0.7F);
			CircusEffects.impact(chamber, state.leapTo.add(0.0D, 0.3D, 0.0D), 2.5D);
			CircusEffects.dust(chamber, state.leapTo, 30);
			BossTelegraphs.ring(chamber, state.leapTo, 3.0D, CircusEffects.CRIMSON, 28);
			// A breath on the ring before his first blow down here.
			state.cooldown = Math.max(state.cooldown, LANDED_REST);
		}
		return false;
	}

	/** Down on the ring he walks the fighter down between moves, glowing with what his bats have brought him. */
	private static void onFoot(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state) {
		boolean airborne = state.move == Move.CAROUSEL_SWING;
		copy.noPhysics = airborne;
		copy.setNoGravity(airborne);
		if (FinaleRules.mayEmpower(finale.swarm().charge()) && copy.tickCount % 3 == 0) {
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.CRIMSON), copy.getX(), copy.getY() + 1.5D, copy.getZ(), 4, 0.5D, 0.9D,
					0.5D, 0.01D);
			chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), copy.getX(), copy.getY() + 2.6D, copy.getZ(), 1, 0.3D,
					0.2D, 0.3D, 0.0D);
		}
		if (state.move != Move.NONE) {
			copy.getNavigation().stop();
			return;
		}
		OrdealKit.face(copy, player.position().subtract(copy.position()));
		if (copy.distanceTo(player) > WALK_CLOSE) copy.getNavigation().moveTo(player, WALK_SPEED);
		else copy.getNavigation().stop();
	}

	/**
	 * The staff strike: he hauls the topper back over his shoulder through a 14-tick gather, the wedge before him burning
	 * in, then cracks it down: 7, and a shove.
	 */
	private static boolean staff(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, State state, int t) {
		if (t == 0 || state.facing == Vec3.ZERO) aimStaff(chamber, copy, player, state, STAFF_WINDUP);
		int release = CircusRingmasterMoves.Move.STAFF_STRIKE.timeline().commitTick();
		copy.posePlay(CircusRingmasterMoves.Move.STAFF_STRIKE, t - (STAFF_WINDUP - release));
		Vec3 at = state.aim;
		OrdealKit.face(copy, state.facing);
		if (t < STAFF_WINDUP) {
			CircusEffects.gather(chamber, copy.position().add(0.0D, 3.0D, 0.0D), CircusEffects.CRIMSON, t / (float) STAFF_WINDUP);
			return false;
		}
		if (t == STAFF_WINDUP) {
			OrdealKit.hazard(chamber, copy, KIND_STAFF, staffWedge(), at, at.add(state.facing), 4);
			Vec3 point = at.add(state.facing.scale(STAFF_REACH * 0.6D));
			chamber.playSound(null, point.x, point.y, point.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 3.5F, 0.7F);
			chamber.playSound(null, point.x, point.y, point.z, SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F, 0.9F);
			CircusEffects.impact(chamber, point.add(0.0D, 0.3D, 0.0D), 1.6D);
			CircusEffects.dust(chamber, point, 18);
			Vec3 feet = player.position();
			if (RingmasterFinaleGeometry.wedgeHits(at, state.facing, STAFF_REACH, STAFF_DEGREES, feet)) {
				state.landed = true;
				OrdealKit.strike(chamber, player, copy, STAFF_DAMAGE, state.facing, 0.8D, 0.35D);
			}
		}
		return false;
	}

	/** Locks the strike on the fighter: his feet, the way to them, and the wedge on the floor for {@code life} ticks. */
	private static void aimStaff(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, State state, int life) {
		state.aim = new Vec3(copy.getX(), state.floor.y, copy.getZ());
		state.facing = OrdealKit.flat(player.position().subtract(copy.position()));
		if (state.facing.lengthSqr() < 1.0E-6D) state.facing = new Vec3(0.0D, 0.0D, 1.0D);
		OrdealKit.hazard(chamber, copy, KIND_STAFF, staffWedge(), state.aim, state.aim.add(state.facing), life);
		chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_PERFORMER_WINDUP.get(), SoundSource.HOSTILE,
				2.5F, 0.75F);
	}

	private static BossFootprint staffWedge() {
		return new BossFootprint.Cone(0.0D, STAFF_REACH, STAFF_DEGREES, 0.0D, BossFootprint.Height.STANDING);
	}

	/**
	 * The carousel swing: he grips the Procession's pole and swings round it at 3.5 blocks, 300 degrees in 20 ticks,
	 * the arc he will sweep laid on the floor from the first tick and burning as he passes: 6, and thrown out.
	 */
	private static boolean swing(ServerLevel chamber, CircusRingmasterEntity copy, CircusCarouselEntity wheel, ServerPlayer player,
			State state, int t) {
		Vec3 pivot = new Vec3(wheel.getX(), state.floor.y, wheel.getZ());
		double step = SWING_DEGREES / SWING_SEGMENTS;
		if (t == 0) {
			Vec3 to = player.position().subtract(pivot);
			state.swingFrom = Math.toDegrees(Math.atan2(to.z, to.x)) - SWING_LEAD;
			for (int i = 0; i < SWING_SEGMENTS; i++) {
				Vec3 a = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, state.swingFrom + i * step);
				Vec3 b = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, state.swingFrom + (i + 1) * step);
				CarouselAlive.lane(chamber, copy, KIND_SWING, a, b, SWING_BAND / 2.0D, SWING_WINDUP + i * SWING_TICKS / SWING_SEGMENTS + 1);
			}
			CircusEffects.vault(chamber, copy.position());
			Vec3 grip = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, state.swingFrom);
			copy.teleportTo(grip.x, grip.y, grip.z);
			CircusEffects.vault(chamber, grip);
			chamber.playSound(null, pivot.x, pivot.y + 2.0D, pivot.z, SoundEvents.CHAIN_HIT, SoundSource.HOSTILE, 2.5F, 0.6F);
		}
		copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
		if (t < SWING_WINDUP) {
			Vec3 grip = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, state.swingFrom);
			copy.teleportTo(grip.x, state.floor.y + 0.3D * t / SWING_WINDUP, grip.z);
			OrdealKit.face(copy, pivot.subtract(grip));
			BossTelegraphs.tendril(chamber, grip.add(0.0D, 1.6D, 0.0D), pivot.add(0.0D, 3.5D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
					CircusEffects.THREAD_GLOW_ARGB, copy.getId() * 67L + t / 4);
			return false;
		}
		int run = t - SWING_WINDUP;
		if (run > SWING_TICKS) {
			copy.teleportTo(copy.getX(), state.floor.y, copy.getZ());
			return false;
		}
		double angle = state.swingFrom + SWING_DEGREES * run / SWING_TICKS;
		Vec3 at = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, angle);
		copy.teleportTo(at.x, state.floor.y + 0.8D, at.z);
		Vec3 tangent = new Vec3(-Math.sin(Math.toRadians(angle)), 0.0D, Math.cos(Math.toRadians(angle)));
		OrdealKit.face(copy, tangent);
		if (run % 2 == 0) {
			Vec3 next = RingmasterFinaleGeometry.onCircle(pivot, SWING_RADIUS, angle + step * 2.0D);
			CarouselAlive.lane(chamber, copy, KIND_SWING, at, next, SWING_BAND / 2.0D, 4);
			CircusEffects.dust(chamber, at, 4);
		}
		if (run % 5 == 0) chamber.playSound(null, at.x, at.y, at.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.6F);
		if (!state.landed && RingmasterFinaleGeometry.arcHits(pivot, SWING_RADIUS, SWING_BAND, (float) state.swingFrom, (float) angle,
				player.position())) {
			state.landed = true;
			Vec3 out = player.position().subtract(pivot);
			OrdealKit.strike(chamber, player, copy, SWING_DAMAGE, out, 1.3D, 0.45D);
			chamber.playSound(null, player.getX(), player.getY(), player.getZ(), SoundInit.RITE_SUBJECT_THUD.get(), SoundSource.HOSTILE, 3.0F,
					0.8F);
		}
		return false;
	}

	/** His cue: the staff raised to the dark, and the next professor this fighter passed steps in to perform for him. */
	private static boolean cue(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, Finale finale, State state, int t) {
		copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
		if (t == 0 && state.echo == null) {
			List<OrdealProfessor> passed = FinaleEchoes.passed(player);
			if (passed.isEmpty()) return true;
			OrdealProfessor professor = passed.get(Math.floorMod(state.nextEcho++, passed.size()));
			state.echo = FinaleEchoCue.call(chamber, player, copy, professor, finale.floor());
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(),
					SoundSource.HOSTILE, 2.0F, 1.3F);
		}
		if (t % 3 == 0) CircusEffects.gather(chamber, copy.position().add(0.0D, 3.2D, 0.0D), CircusEffects.BONE, t / (float) FinaleEchoCue.STEP_IN);
		return false;
	}

	/**
	 * Empowered: the blood his bats brought falls back as rain, six marks down the fighter's path one after another,
	 * each closing for its fall: 5 apiece.
	 */
	private static boolean cascade(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, State state, int t) {
		if (t == 0) {
			Vec3 feet = new Vec3(player.getX(), state.floor.y, player.getZ());
			Vec3 path = OrdealKit.flat(player.getDeltaMovement());
			if (path.lengthSqr() < 1.0E-6D || player.getDeltaMovement().horizontalDistanceSqr() < 0.0025D)
				path = OrdealKit.flat(feet.subtract(copy.position()));
			if (path.lengthSqr() < 1.0E-6D) path = new Vec3(1.0D, 0.0D, 0.0D);
			say(player, LINE_CASCADE);
			state.spots = new Vec3[CASCADE_DROPS];
			for (int i = 0; i < CASCADE_DROPS; i++) {
				// The first falls just behind the fighter, the rest walk on down their path.
				state.spots[i] = feet.add(path.scale((i - 1) * CASCADE_STEP));
				OrdealKit.hazard(chamber, copy, KIND_CASCADE, new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, CASCADE_RADIUS,
						BossFootprint.Height.STANDING), state.spots[i], state.spots[i], cascadeAt(i) + 1);
			}
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_RINGMASTER_PHASE_CHANGE.get(),
					SoundSource.HOSTILE, 2.5F, 0.6F);
		}
		copy.posePlay(CircusRingmasterMoves.Move.CUE, t);
		for (int i = 0; i < CASCADE_DROPS; i++) {
			int land = cascadeAt(i);
			Vec3 spot = state.spots[i];
			if (t < land && t % 2 == 0) {
				// The blood gathering overhead and starting to fall.
				double height = 7.0D * (1.0D - HemoEasing.clamp01((t - (land - 8)) / 8.0D));
				chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), spot.x, spot.y + Math.max(0.5D, height), spot.z, 3,
						0.4D, 0.2D, 0.4D, 0.0D);
			}
			if (t != land) continue;
			chamber.playSound(null, spot.x, spot.y, spot.z, SoundInit.RITE_STAFF_IMPACT.get(), SoundSource.HOSTILE, 2.5F, 0.9F + 0.05F * i);
			BossTelegraphs.bloodCells(chamber, spot.add(0.0D, 0.4D, 0.0D), CircusEffects.CRIMSON, 24, CASCADE_RADIUS * 0.5D, 0.12D);
			CircusEffects.impact(chamber, spot.add(0.0D, 0.3D, 0.0D), CASCADE_RADIUS);
			if ((state.hitMask & (1 << i)) == 0 && RingmasterFinaleGeometry.discHits(spot, CASCADE_RADIUS, player.position())) {
				state.hitMask |= 1 << i;
				OrdealKit.strike(chamber, player, copy, CASCADE_DAMAGE, player.position().subtract(spot), 0.4D, 0.3D);
			}
		}
		return false;
	}

	/** The tick drop {@code i} of the cascade lands. */
	public static int cascadeAt(int i) {
		return CASCADE_WINDUP + i * CASCADE_GAP;
	}

	/**
	 * Empowered: threads run from his hands down a lane to the fighter through a 20-tick gather; caught in it, the
	 * fighter is hauled five blocks toward him and the staff strike follows at once.
	 */
	private static boolean pull(ServerLevel chamber, CircusRingmasterEntity copy, ServerPlayer player, State state, int t) {
		Vec3 hands = copy.position().add(0.0D, 1.6D, 0.0D);
		if (t == 0) {
			Vec3 from = new Vec3(copy.getX(), state.floor.y, copy.getZ());
			Vec3 dir = OrdealKit.flat(player.position().subtract(from));
			if (dir.lengthSqr() < 1.0E-6D) dir = new Vec3(0.0D, 0.0D, 1.0D);
			state.aim = from;
			state.facing = dir;
			state.spots = new Vec3[]{from.add(dir.scale(OrdealKit.horizontal(from, player.position()) + 2.0D))};
			say(player, LINE_PULL);
			CarouselAlive.lane(chamber, copy, KIND_PULL, from, state.spots[0], PULL_HALF, PULL_WINDUP);
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundEvents.CHAIN_FALL, SoundSource.HOSTILE, 2.5F, 0.7F);
		}
		copy.posePlay(CircusRingmasterMoves.Move.STRINGS_CUT, t);
		OrdealKit.face(copy, state.facing);
		if (t < PULL_WINDUP) {
			if (t % 4 == 0) {
				Vec3 reach = hands.lerp(state.spots[0].add(0.0D, 1.0D, 0.0D), (t + 4) / (double) PULL_WINDUP);
				BossTelegraphs.tendril(chamber, hands, reach, CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB,
						copy.getId() * 71L + t);
			}
			return false;
		}
		if (t == PULL_WINDUP) {
			CarouselAlive.lane(chamber, copy, KIND_PULL, state.aim, state.spots[0], PULL_HALF, 4);
			Vec3 feet = player.position();
			if (!RingmasterFinaleGeometry.laneHits(state.aim, state.spots[0], PULL_HALF, feet)) return true;
			Vec3 toHim = OrdealKit.flat(copy.position().subtract(feet));
			double haul = Math.min(PULL_DISTANCE, Math.max(0.0D, OrdealKit.horizontal(copy.position(), feet) - PULL_NEAREST));
			Vec3 to = feet.add(toHim.scale(haul));
			BossTelegraphs.tendril(chamber, hands, player.getEyePosition(), CircusEffects.THREAD_CORE_ARGB, CircusEffects.THREAD_GLOW_ARGB,
					copy.getId() * 73L);
			player.teleportTo(to.x, Math.max(to.y, state.floor.y), to.z);
			player.hurtMarked = true;
			chamber.playSound(null, to.x, to.y, to.z, SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 3.0F, 0.6F);
			CircusEffects.impact(chamber, to.add(0.0D, 1.0D, 0.0D), 0.8D);
			// The staff comes down on the fighter he has hauled in, its wind-up all but spent.
			state.move = Move.STAFF_STRIKE;
			state.tick = STAFF_WINDUP - PULL_STRIKE_LEAD - 1;
			state.landed = false;
			aimStaff(chamber, copy, player, state, PULL_STRIKE_LEAD + 1);
		}
		return false;
	}

	// --- the kneel ---------------------------------------------------------------------------------------------------

	/**
	 * He kneels, held at one health: he names both ways out; the captives' threads on the carousel glow taut (the kill
	 * frees them); once the kneel settles his blood rises toward a player fit to take his place, and the first time a
	 * player kneels him a prompt names the two actions.
	 */
	private static void kneel(ServerLevel chamber, CircusRingmasterEntity copy, CircusCarouselEntity wheel, ServerPlayer player,
			Finale finale) {
		int t = finale.phaseTicks();
		if (copy.getHealth() > 1.0F) copy.setHealth(1.0F);
		copy.getNavigation().stop();
		copy.noPhysics = false;
		copy.setNoGravity(false);
		copy.posePlay(CircusRingmasterMoves.Move.NONE, 0);
		copy.syncFinaleMove(0, 0);
		OrdealKit.face(copy, player.position().subtract(copy.position()));
		if (t == 1) {
			say(player, LINE_KNEEL);
			chamber.playSound(null, copy.getX(), copy.getY(), copy.getZ(), SoundInit.ENTITY_CIRCUS_RINGMASTER_FINALE_COLLAPSE.get(),
					SoundSource.HOSTILE, 2.0F, 0.8F);
			BossTelegraphs.bloodCells(chamber, copy.position().add(0.0D, 1.0D, 0.0D), CircusEffects.CRIMSON, 30, 0.6D, 0.1D);
		}
		// The captives' threads drawn taut and glowing: cut him down and they go free.
		if (t % 8 == 0) for (int horse = 0; horse < 3; horse++) {
			com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules.HorsePose pose =
					com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules.horsePose(wheel.getYRot(), horse);
			Vec3 rider = new Vec3(wheel.getX() + pose.x(), wheel.getY() + 3.2D, wheel.getZ() + pose.z());
			BossTelegraphs.tendril(chamber, rider, rider.add(0.0D, 6.0D, 0.0D), CircusEffects.THREAD_CORE_ARGB,
					CircusEffects.THREAD_GLOW_ARGB, copy.getId() * 79L + horse + t);
			chamber.sendParticles(HemoParticleData.glow(CircusEffects.BONE), rider.x, rider.y, rider.z, 4, 0.2D, 0.6D, 0.2D, 0.0D);
		}
		boolean settled = t >= FinaleRules.KNEEL_SETTLE_TICKS;
		boolean fit = RingmasterAbsorption.fit(player);
		if (t == FinaleRules.KNEEL_SETTLE_TICKS) {
			var progress = com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress.state(player);
			if (!progress.getBoolean(PROMPT_SEEN)) {
				progress.putBoolean(PROMPT_SEEN, true);
				copy.setKneelPrompt(fit ? 1 : 2);
			}
		}
		if (t == FinaleRules.KNEEL_SETTLE_TICKS + PROMPT_TICKS) copy.setKneelPrompt(0);
		// His blood offered: rising off him toward a fit player, before anyone draws it.
		if (settled && fit && t % 2 == 0) {
			Vec3 from = copy.position().add(0.0D, copy.getBbHeight() * 0.6D, 0.0D);
			Vec3 to = player.getEyePosition().add(0.0D, -0.5D, 0.0D);
			double reach = Math.min(1.0D, (t - FinaleRules.KNEEL_SETTLE_TICKS) / 40.0D) * 0.6D;
			for (int i = 0; i < 4; i++) {
				Vec3 at = from.lerp(to, reach * (i + 1) / 4.0D).add(0.0D, 0.25D * Math.sin((t + i * 5) * 0.3D), 0.0D);
				chamber.sendParticles(HemoParticleData.bloodCell(CircusEffects.CRIMSON), at.x, at.y, at.z, 1, 0.05D, 0.05D, 0.05D, 0.01D);
			}
		}
	}

	/** One of his lines, to the finale's player, as he speaks everywhere in the troupe: his name and the line in quotes. */
	public static void say(ServerPlayer player, String key) {
		player.displayClientMessage(net.minecraft.network.chat.Component.translatable("hemomancy.circus.ringmaster.says",
				net.minecraft.network.chat.Component.translatable(key)).withStyle(net.minecraft.ChatFormatting.GRAY), false);
	}

	// --- shared -----------------------------------------------------------------------------------------------------

	/** A blink: gone in a column of dark glow, there in another. */
	private static void blink(ServerLevel chamber, CircusRingmasterEntity copy, Vec3 to) {
		CircusEffects.vault(chamber, copy.position());
		copy.teleportTo(to.x, to.y, to.z);
		CircusEffects.vault(chamber, to);
	}

	static Vec3 perch(Finale finale, BlockPos cell) {
		return FinaleStage.perch(finale.stage(), finale.rotation(), cell);
	}

	private static Vec3 onFloor(Vec3 point, Vec3 floor) {
		return new Vec3(point.x, floor.y, point.z);
	}

	/** A high perch other than {@code a} and {@code b}. */
	private static int otherHigh(RandomSource random, int a, int b) {
		List<Integer> free = new ArrayList<>();
		for (int i = 0; i < FinaleStage.HIGH_PERCHES.size(); i++) if (i != a && i != b) free.add(i);
		return free.get(random.nextInt(free.size()));
	}

	private static int nearestHigh(Finale finale, Vec3 at) {
		int best = 0;
		for (int i = 1; i < FinaleStage.HIGH_PERCHES.size(); i++)
			if (perch(finale, FinaleStage.HIGH_PERCHES.get(i)).distanceToSqr(at)
					< perch(finale, FinaleStage.HIGH_PERCHES.get(best)).distanceToSqr(at)) best = i;
		return best;
	}

	private static BlockPos nearestLow(Finale finale, Vec3 at) {
		BlockPos best = FinaleStage.LOW_PERCHES.getFirst();
		for (BlockPos cell : FinaleStage.LOW_PERCHES)
			if (perch(finale, cell).distanceToSqr(at) < perch(finale, best).distanceToSqr(at)) best = cell;
		return best;
	}
}
