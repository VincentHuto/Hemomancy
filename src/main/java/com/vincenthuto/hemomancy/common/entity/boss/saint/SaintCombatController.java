package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The one owner of a kit Saint's locomotion, facing (turn-rate limited) and move timeline, on the Mycophant
 * controller's model: roam, approach or hold; pick; wind up; commit; active; recover; chain. A stagger, a forced
 * {@link Cut#QUEUE} move or an ordinary phase change never cuts an active move: it waits for the recovery boundary
 * (the recovery's first tick, or no move at all). Only the ending and a {@link Cut#IMMEDIATE} phase change call
 * {@link #cancel}. While a move is active its footprint strikes through {@link SaintMoves#struck} and the move's
 * {@code onHit}; its {@code onTick} runs every tick.
 */
public final class SaintCombatController {
	public enum Cut { QUEUE, IMMEDIATE }

	private static final BossMoveTimeline IDLE = new BossMoveTimeline(0, 0, 0);
	private static final int RETRY_DELAY = 5;

	private final SaintTrialEntity saint;
	private final SaintMotor motor;
	private final SaintPickRules.History history = new SaintPickRules.History();
	private final SaintPickRules.Pressure pressure = new SaintPickRules.Pressure();
	private final Map<Integer, Integer> cooldowns = new HashMap<>();
	private final Map<Long, Integer> hitLog = new HashMap<>();

	@Nullable
	private SaintMove move;
	private BossMoveTimeline timeline = IDLE;
	private BossMoveTimeline.Stage stage = BossMoveTimeline.Stage.COMPLETE;
	private int moveTick;
	private SaintMoveInputs inputs = SaintMoveInputs.NONE;
	private int serial;
	@Nullable
	private LivingEntity target;
	private Vec3 origin = Vec3.ZERO;
	private Vec3 aim = Vec3.ZERO;
	private Vec3 lockedAim = Vec3.ZERO;
	private Vec3 sentAim = Vec3.ZERO;
	private boolean committed;
	private boolean recoveryOpened;
	private List<Vec3> points = List.of();
	private LongSet cells = new LongOpenHashSet();
	private final List<Vec3> anchors = new ArrayList<>();
	private boolean hitLanded;
	private int chainDepth;
	private int decisionTimer = 20;
	@Nullable
	private SaintFollowUp link;
	private int linkTimer;
	@Nullable
	private SaintMove forced;
	private int queuedStagger;
	@Nullable
	private Vec3 queuedFace;
	private int staggerTicks;
	@Nullable
	private Vec3 staggerFace;

	SaintCombatController(SaintTrialEntity saint) {
		this.saint = saint;
		this.motor = new SaintMotor(saint);
	}

	public SaintMotor motor() {
		return motor;
	}

	// ── the tick ─────────────────────────────────────────────────────

	/** Roam, pick, wind up, commit, active, recover, chain. */
	public void tick() {
		if (!(saint.level() instanceof ServerLevel level)) return;
		SaintArena arena = saint.arena();
		SaintKit kit = saint.kit();
		if (arena == null || kit == null) return;
		SaintLocomotion locomotion = kit.locomotion();
		cooldowns.replaceAll((id, left) -> Math.max(0, left - 1));
		motor.tick();
		List<Player> challengers = arena.challengers();
		if (move == null || stage == BossMoveTimeline.Stage.WINDUP && !committed) {
			LivingEntity chosen = locomotion.chooseTarget(saint, challengers, target);
			if (chosen != target) {
				target = chosen;
				if (move != null) saint.syncMoveTarget(target == null ? -1 : target.getId());
			}
		}
		if (target != null && (!target.isAlive() || target.isRemoved())) target = null;
		samplePressure(locomotion);

		if (staggerTicks > 0) {
			staggerTicks--;
			motor.stop();
			if (staggerFace != null) motor.face(staggerFace, locomotion.turnRate(saint, false));
			return;
		}
		if (move != null && interruptible() && interruptQueued()) endMove();
		if (move == null) {
			if (applyQueued(level, arena, locomotion)) return;
			if (link != null) {
				if (--linkTimer <= 0) {
					SaintFollowUp next = link;
					link = null;
					chainDepth++;
					startMove(next.next(), pickContext(locomotion, arena), level, arena, locomotion);
				} else if (target != null) {
					motor.face(target.position(), locomotion.turnRate(saint, false));
				}
				return;
			}
			locomotion.roam(saint, motor, target);
			if (target != null) motor.face(target.position(), locomotion.turnRate(saint, false));
			if (decisionTimer > 0) {
				decisionTimer--;
				return;
			}
			if (target == null || !locomotion.mayPick(saint)) return;
			SaintPickContext ctx = pickContext(locomotion, arena);
			SaintMove pick = SaintPickRules.choose(kit.moves(), ctx, this::cooldownLeft, history,
					bound -> saint.getRandom().nextInt(bound));
			if (pick == null) {
				decisionTimer = RETRY_DELAY;
				return;
			}
			cooldowns.put(pick.id(), Math.max(0, pick.cooldown(ctx)));
			chainDepth = 0;
			startMove(pick, ctx, level, arena, locomotion);
			return;
		}
		runMove(level, arena, locomotion);
	}

	private boolean interruptQueued() {
		return forced != null || queuedStagger > 0;
	}

	/** At the boundary: a queued stagger first, then a forced move. True when one was applied. */
	private boolean applyQueued(ServerLevel level, SaintArena arena, SaintLocomotion locomotion) {
		if (queuedStagger > 0) {
			staggerTicks = queuedStagger;
			staggerFace = queuedFace;
			queuedStagger = 0;
			queuedFace = null;
			link = null;
			chainDepth = 0;
			motor.stop();
			return true;
		}
		if (forced != null) {
			SaintMove next = forced;
			forced = null;
			link = null;
			chainDepth = 0;
			startMove(next, pickContext(locomotion, arena), level, arena, locomotion);
			return true;
		}
		return false;
	}

	private void startMove(SaintMove next, SaintPickContext ctx, ServerLevel level, SaintArena arena,
			SaintLocomotion locomotion) {
		move = next;
		int variant = next.rollVariant(ctx, bound -> saint.getRandom().nextInt(Math.max(1, bound)));
		inputs = new SaintMoveInputs(saint.phases().phase(), variant, saint.deeds().mask(), arena.challengerCount());
		timeline = SaintControllerRules.effective(next.kind(), next.timeline(inputs));
		moveTick = -1;
		stage = BossMoveTimeline.Stage.WINDUP;
		committed = false;
		recoveryOpened = false;
		hitLanded = false;
		hitLog.clear();
		points = List.of();
		cells = new LongOpenHashSet();
		anchors.clear();
		origin = saint.position();
		Vec3 targetPos = targetPosition();
		lockedAim = SaintControllerRules.aim(next.aim(), false, targetPos, targetPos, targetMotion(), next.leadTicks());
		aim = lockedAim;
		sentAim = aim;
		serial++;
		history.record(next);
		saint.syncMoveStart(next.id(), serial, variant, origin, aim, target == null ? -1 : target.getId(), frame().save());
		runMove(level, arena, locomotion);
	}

	private void runMove(ServerLevel level, SaintArena arena, SaintLocomotion locomotion) {
		SaintMove current = move;
		if (current == null) return;
		moveTick++;
		stage = timeline.stageAt(moveTick);
		if (stage == BossMoveTimeline.Stage.COMPLETE) {
			finish(locomotion);
			return;
		}
		boolean committedNow = !committed && moveTick >= timeline.commitTick();
		if (!committed) {
			aim = SaintControllerRules.aim(current.aim(), false, lockedAim, targetPosition(), targetMotion(),
					current.leadTicks());
			if (current.aim() != SaintMove.Aim.LOCK_AT_START) lockedAim = aim;
			if (committedNow) {
				committed = true;
				lockedAim = aim;
				origin = saint.position();
				saint.syncMoveCommit(origin, aim, frame().save());
				sentAim = aim;
			} else if (SaintControllerRules.aimMoved(sentAim, aim)) {
				saint.syncMoveAim(aim);
				sentAim = aim;
			}
		}
		boolean tracking = !committed && current.aim() != SaintMove.Aim.LOCK_AT_START;
		motor.face(aim, locomotion.turnRate(saint, tracking));
		SaintMoveContext ctx = new SaintMoveContext(saint, level, current, inputs, moveTick, stage, committedNow, frame(),
				target, arena);
		if (stage == BossMoveTimeline.Stage.RECOVERY && !recoveryOpened) {
			recoveryOpened = true;
			SaintOpening.Spec opening = current.recoveryOpening(ctx);
			if (opening != null && saint.openings().open(opening, opening.ticks())) {
				timeline = SaintControllerRules.withOpening(timeline, opening.ticks());
			}
		}
		current.onTick(ctx);
		if (move != current) return;
		locomotion.inMove(saint, motor, ctx);
		if (stage == BossMoveTimeline.Stage.ACTIVE) {
			for (LivingEntity struck : SaintMoves.struck(ctx)) {
				current.onHit(ctx, struck);
				if (move != current) return;
			}
		}
		saint.syncMoveTick(moveTick);
	}

	private void finish(SaintLocomotion locomotion) {
		SaintMove done = move;
		boolean landed = hitLanded;
		endMove();
		if (done == null) return;
		boolean interrupt = interruptQueued() || saint.phases().pending();
		SaintFollowContext follow = new SaintFollowContext(saint, saint.phases().phase(), landed, band(locomotion),
				sector());
		SaintFollowUp next = SaintPickRules.followUpLink(done, follow, chainDepth, interrupt,
				() -> saint.getRandom().nextDouble());
		if (next != null) {
			link = next;
			linkTimer = Math.max(1, next.linkGap());
		} else {
			chainDepth = 0;
			decisionTimer = Math.max(decisionTimer, locomotion.decisionDelay(saint));
		}
	}

	private void endMove() {
		move = null;
		timeline = IDLE;
		stage = BossMoveTimeline.Stage.COMPLETE;
		moveTick = 0;
		committed = false;
		saint.syncMoveEnd();
	}

	// ── what the Saint calls ─────────────────────────────────────────

	@Nullable
	public SaintMove move() {
		return move;
	}

	public int moveTick() {
		return Math.max(0, moveTick);
	}

	public BossMoveTimeline.Stage stage() {
		return stage;
	}

	/** The running move's timeline, its recovery stretched by an opening; an empty one between moves. */
	public BossMoveTimeline timeline() {
		return timeline;
	}

	public SaintMoveInputs inputs() {
		return inputs;
	}

	@Nullable
	public LivingEntity target() {
		return target;
	}

	/** The running move's frame, or null between moves. */
	@Nullable
	public FootprintFrame frame() {
		if (move == null) return null;
		SaintArena arena = saint.arena();
		double floor = arena == null ? origin.y : arena.floorY();
		var floorBox = arena == null ? saint.getBoundingBox().inflate(16.0D) : arena.bossFloor();
		var chamberBox = arena == null ? floorBox : arena.chamber();
		return new FootprintFrame(origin, aim, targetPosition(), floor, points, cells, anchors, floorBox, chamberBox);
	}

	/** A forced move (Ignition, the Last Thirst, the Radiance Flood): at the next recovery boundary, or now. */
	public void start(SaintMove next, Cut cut) {
		if (cut == Cut.IMMEDIATE) {
			if (move != null) endMove();
			link = null;
			chainDepth = 0;
		}
		forced = next;
	}

	/** Queued to the recovery boundary; {@code face} is where the stagger turns the body. */
	public void requestStagger(int ticks, @Nullable Vec3 face) {
		if (ticks <= 0) return;
		queuedStagger = Math.max(queuedStagger, ticks);
		queuedFace = face;
	}

	public boolean staggered() {
		return staggerTicks > 0;
	}

	/** Ends the wind-up and active span now: the next tick is the recovery's first. */
	public void skipToRecovery() {
		if (move == null || stage == BossMoveTimeline.Stage.RECOVERY) return;
		if (!committed) {
			committed = true;
			lockedAim = aim;
			saint.syncMoveCommit(origin, aim, frame().save());
		}
		moveTick = timeline.windup() + timeline.active() - 1;
	}

	/** Only SaintEnding and Cut.IMMEDIATE phase changes call this (guardrail). */
	public void cancel() {
		if (move != null) endMove();
		forced = null;
		link = null;
		chainDepth = 0;
		queuedStagger = 0;
		queuedFace = null;
		staggerTicks = 0;
		motor.stop();
	}

	/** Discs and Path points: synced at start and commit, and whenever set. */
	public void setPoints(List<Vec3> value) {
		points = List.copyOf(value);
		resendFrame();
	}

	/** Region exclusions, as {@code BossFootprint.cell} keys. */
	public void setCells(LongSet value) {
		cells = new LongOpenHashSet(value);
		resendFrame();
	}

	/** Tether ANCHOR ends, re-synced when they move. */
	public void setAnchor(int index, Vec3 point) {
		while (anchors.size() <= index) anchors.add(point);
		anchors.set(index, point);
		resendFrame();
	}

	public void markHit() {
		hitLanded = true;
	}

	public boolean hitLanded() {
		return hitLanded;
	}

	public int chainDepth() {
		return chainDepth;
	}

	public int ticksSinceOpening() {
		return saint.openings().ticksSinceClosed();
	}

	/** Holds the next pick (and a pending follow-up) back by {@code ticks}. */
	public void delay(int ticks) {
		decisionTimer += Math.max(0, ticks);
		if (link != null) linkTimer += Math.max(0, ticks);
	}

	public SaintPickRules.History history() {
		return history;
	}

	public SaintPickRules.Pressure pressure() {
		return pressure;
	}

	/** No move, or a move in its recovery: where staggers, forced moves and phase changes land. */
	public boolean interruptible() {
		return SaintControllerRules.interruptible(move != null, stage);
	}

	public int cooldownLeft(SaintMove candidate) {
		return cooldowns.getOrDefault(candidate.id(), 0);
	}

	/** Whether part {@code part} may strike {@code entity} on {@code tick}: once per move, or once per hitEvery. */
	boolean mayStrike(int part, LivingEntity entity, int tick, int hitEvery) {
		long key = ((long) part << 32) | (entity.getId() & 0xFFFFFFFFL);
		Integer last = hitLog.get(key);
		if (last != null && (hitEvery <= 0 || tick - last < hitEvery)) return false;
		hitLog.put(key, tick);
		return true;
	}

	/** The challenger who hurt it in the last 100 ticks, else the nearest one in sight, else the nearest. */
	@Nullable
	public static LivingEntity defaultTarget(SaintTrialEntity saint, List<Player> challengers) {
		if (challengers.isEmpty()) return null;
		LivingEntity hurtBy = saint.getLastHurtByMob();
		if (hurtBy instanceof Player player && challengers.contains(player)
				&& saint.tickCount - saint.getLastHurtByMobTimestamp() < 100) return player;
		Player nearestSeen = null, nearest = null;
		double seenDistance = Double.MAX_VALUE, distance = Double.MAX_VALUE;
		for (Player player : challengers) {
			double d = saint.distanceToSqr(player);
			if (d < distance) {
				distance = d;
				nearest = player;
			}
			if (d < seenDistance && saint.hasLineOfSight(player)) {
				seenDistance = d;
				nearestSeen = player;
			}
		}
		return nearestSeen != null ? nearestSeen : nearest;
	}

	// ── helpers ──────────────────────────────────────────────────────

	private void resendFrame() {
		FootprintFrame frame = frame();
		if (frame != null) saint.syncMoveFrame(frame.save());
	}

	private Vec3 targetPosition() {
		if (target != null) return target.position();
		return move == null ? saint.position() : lockedAim;
	}

	private Vec3 targetMotion() {
		if (target == null) return Vec3.ZERO;
		return new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
	}

	private void samplePressure(SaintLocomotion locomotion) {
		if (target == null) return;
		pressure.sample(band(locomotion), saint.hasLineOfSight(target));
	}

	private BossBands.Band band(SaintLocomotion locomotion) {
		if (target == null) return BossBands.Band.FAR;
		double dx = target.getX() - saint.getX(), dz = target.getZ() - saint.getZ();
		return locomotion.bands().band(Math.sqrt(dx * dx + dz * dz));
	}

	private BossBands.Sector sector() {
		if (target == null) return BossBands.Sector.FRONT;
		return BossBands.sector(BossBands.degreesFromFacing(saint.getX(), saint.getZ(), saint.getYRot(), target.getX(),
				target.getZ()));
	}

	private SaintPickContext pickContext(SaintLocomotion locomotion, SaintArena arena) {
		double distance = target == null ? 0.0D : Math.hypot(target.getX() - saint.getX(), target.getZ() - saint.getZ());
		float targetMeter = 0.0F, targetSecondary = 0.0F;
		SaintMeter meter = saint.meter();
		if (target instanceof Player player) {
			SaintMeter.PlayerState state = meter.existing(player.getUUID());
			if (state != null) {
				targetMeter = state.value() / meter.playerMax();
				targetSecondary = state.secondary() / state.secondaryMax();
			}
		}
		return new SaintPickContext(saint, saint.phases().phase(), target, band(locomotion), sector(), distance,
				pressure.kiteTicks(), pressure.hugTicks(), pressure.unseenTicks(), ticksSinceOpening(),
				saint.phases().ticksInPhase(), meter.fraction(), targetMeter, targetSecondary, saint.deeds().mask(),
				saint.awakening(), arena.challengerCount());
	}

	// ── save ─────────────────────────────────────────────────────────

	/** Cooldowns and the replay serial are kept; a move under way is dropped and the Saint picks again on load. */
	public void save(CompoundTag tag) {
		CompoundTag controller = new CompoundTag();
		ListTag list = new ListTag();
		cooldowns.forEach((id, left) -> {
			if (left <= 0) return;
			CompoundTag entry = new CompoundTag();
			entry.putInt("id", id);
			entry.putInt("left", left);
			list.add(entry);
		});
		controller.put("cooldowns", list);
		controller.putInt("serial", serial);
		tag.put("SaintController", controller);
	}

	public void load(CompoundTag tag) {
		CompoundTag controller = tag.getCompound("SaintController");
		cooldowns.clear();
		ListTag list = controller.getList("cooldowns", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			cooldowns.put(entry.getInt("id"), entry.getInt("left"));
		}
		serial = controller.getInt("serial");
	}
}
