package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberDiscoveryEvents;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossIntroWindow;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossHold;
import com.vincenthuto.hemomancy.common.entity.boss.BossHoldCarrier;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * O-1: the four entombed Saints fought in a trial (Hemorath, Seraphae, Putriciel, Velorum). They share a persistent
 * body, a boss bar, the rise out of the sarcophagus and the chat lines sent to everyone in the fight. Each Saint keeps
 * its own mechanics and bar progress: Seraphae's bar is her containment integrity, not her health.
 * <p>
 * A Saint cannot be killed. Every ending (Hemorath's overload or fall, Putriciel's fifth absolution, Seraphae's
 * containment, Velorum's fall) goes through {@link #endTrial} once: it rolls the Saint's loot table
 * {@code entities/<id>} and closes the Saint back into the sarcophagus it rose from.
 * <p>
 * The Saint combat kit: every Saint returns its kit from {@link #createKit()} and registers no goals; the kit owns its
 * server tick (rise, arena, phases, meter, presence, openings, controller, hold, ending, sync, then
 * {@link #tickSaint}), its damage pipeline ({@code actuallyHurt}: classify, admit, factor, floor) and its ending
 * ({@link #beginEnding}).
 */
public abstract class SaintTrialEntity extends Monster implements RisingSaint, BossHoldCarrier,
		BossGroundHazardEntity.Owner {
	private static final EntityDataAccessor<Integer> DATA_RISE_TICK =
			SynchedEntityData.defineId(SaintTrialEntity.class, EntityDataSerializers.INT);

	/** How far from the Saint a player can stand and still carry its reward when no one struck the last blow. */
	private static final double REWARD_RANGE = 32.0D;
	private static final String TAG_TOMB_POS = "SaintTombPos";
	private static final String TAG_TOMB_STATE = "SaintTombState";
	private static final String TAG_TRIAL_ENDED = "SaintTrialEnded";

	protected final BossBarBinding bossBar;
	private final BossIntroWindow rise = new BossIntroWindow(SaintRise.TICKS);
	private final BossReplayClock riseClock = new BossReplayClock();
	@Nullable
	private BlockPos tombPos;
	@Nullable
	private BlockState tombState;
	private boolean trialEnded;

	// ── the kit's synced block ───────────────────────────────────────
	private static final EntityDataAccessor<Integer> DATA_MOVE = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_MOVE_SERIAL = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_MOVE_TICK = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_MOVE_VARIANT = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Vector3f> DATA_MOVE_ORIGIN = define(EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Vector3f> DATA_MOVE_AIM = define(EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Integer> DATA_MOVE_TARGET = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<CompoundTag> DATA_MOVE_FRAME = define(EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<Integer> DATA_MOVE_FLAGS = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_PHASE = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_OPENING = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_OPENING_TICK = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_OPENING_LENGTH = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_METER = define(EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_METER_MAX = define(EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_PRESENCE_RADIUS = define(EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_DEEDS = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<CompoundTag> DATA_CHALLENGERS = define(EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<Integer> DATA_CHALLENGER_COUNT = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_HOLD_PLAYER = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Vector3f> DATA_HOLD_OFFSET = define(EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Float> DATA_HOLD_SLACK = define(EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_ENDING_TICK = define(EntityDataSerializers.INT);

	/** {@code DATA_MOVE_FLAGS} bits. */
	public static final int MOVE_COMMITTED = 1;
	public static final int MOVE_AIM_LOCKED = 1 << 1;
	/** {@code DATA_CHALLENGERS} resends at most this often. */
	public static final int CHALLENGERS_SYNC_GAP = 5;
	private static final String TAG_KIT = "SaintKit";

	private static <T> EntityDataAccessor<T> define(net.minecraft.network.syncher.EntityDataSerializer<T> serializer) {
		return SynchedEntityData.defineId(SaintTrialEntity.class, serializer);
	}

	@Nullable
	private SaintKit kit;
	private boolean kitResolved;
	private final SaintCombatController controller = new SaintCombatController(this);
	private SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, SaintMeter.fixed(1.0F));
	private final SaintOpenings openings = new SaintOpenings(this::openingChanged);
	@Nullable
	private SaintArena arena;
	private SaintPhaseTable phases = SaintPhaseTable.single("", BossEvent.BossBarColor.RED);
	private SaintDeeds deeds = SaintDeeds.NONE;
	private final BossHold bossHold = new BossHold(this, this::syncHold, this::holdReleased);
	private final SaintMoveView view = new SaintMoveView(this);
	private final SaintMeterSync meterSync = new SaintMeterSync();
	@Nullable
	private SaintEnding.Clock ending;
	@Nullable
	private UUID endingVictor;
	@Nullable
	private BooleanSupplier endingDone;
	@Nullable
	private CompoundTag loadedKit;
	@Nullable
	private SchoolHitContext stagedHit;
	private boolean channelHit;
	private float lastFloorReached = -1.0F;
	private int presenceTicks;
	private int challengersSince = CHALLENGERS_SYNC_GAP;
	private int scaledFor;

	protected SaintTrialEntity(EntityType<? extends SaintTrialEntity> type, Level level, int xpReward,
			BossBarBinding bossBar) {
		super(type, level);
		this.bossBar = bossBar;
		this.setPersistenceRequired();
		this.xpReward = xpReward;
	}

	/** Which Saint this body is: the one its sarcophagus holds again when the trial ends. */
	public abstract EnumSaintType saintType();

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_RISE_TICK, -1);
		builder.define(DATA_MOVE, 0);
		builder.define(DATA_MOVE_SERIAL, 0);
		builder.define(DATA_MOVE_TICK, 0);
		builder.define(DATA_MOVE_VARIANT, 0);
		builder.define(DATA_MOVE_ORIGIN, new Vector3f());
		builder.define(DATA_MOVE_AIM, new Vector3f());
		builder.define(DATA_MOVE_TARGET, -1);
		builder.define(DATA_MOVE_FRAME, new CompoundTag());
		builder.define(DATA_MOVE_FLAGS, 0);
		builder.define(DATA_PHASE, 1);
		builder.define(DATA_OPENING, 0);
		builder.define(DATA_OPENING_TICK, 0);
		builder.define(DATA_OPENING_LENGTH, 0);
		builder.define(DATA_METER, 0.0F);
		builder.define(DATA_METER_MAX, 1.0F);
		builder.define(DATA_PRESENCE_RADIUS, 0.0F);
		builder.define(DATA_DEEDS, 0);
		builder.define(DATA_CHALLENGERS, new CompoundTag());
		builder.define(DATA_CHALLENGER_COUNT, 1);
		builder.define(DATA_HOLD_PLAYER, -1);
		builder.define(DATA_HOLD_OFFSET, new Vector3f());
		builder.define(DATA_HOLD_SLACK, -1.0F);
		builder.define(DATA_ENDING_TICK, -1);
	}

	// ── the kit: what a Saint provides ───────────────────────────────

	/** The Saint's kit, made once on first read. */
	protected abstract SaintKit createKit();

	/** The kit, made on first read. */
	public final SaintKit kit() {
		if (!kitResolved) {
			kitResolved = true;
			kit = createKit();
			meter = kit.meter();
			phases = kit.phases();
			meter.bind(this);
		}
		return kit;
	}

	public SaintCombatController controller() {
		return controller;
	}

	public SaintMeter meter() {
		kit();
		return meter;
	}

	public SaintOpenings openings() {
		return openings;
	}

	/** The chamber, resolved at the rise (or the first kit tick); null before that. */
	@Nullable
	public SaintArena arena() {
		return arena;
	}

	public SaintPhaseTable phases() {
		kit();
		return phases;
	}

	public SaintDeeds deeds() {
		return deeds;
	}

	@Override
	public BossHold bossHold() {
		return bossHold;
	}

	@Override
	public net.minecraft.world.entity.Entity holdBody() {
		return this;
	}

	/** The move with this id in the kit's table, or null. */
	@Nullable
	public SaintMove move(int id) {
		SaintKit current = kit();
		if (current == null || id == 0) return null;
		for (SaintMove candidate : current.moves()) if (candidate.id() == id) return candidate;
		return null;
	}

	public SaintMoveView view() {
		return view;
	}

	/** Who has been sent this Saint's {@code SaintMeterPacket}: join, change, keep-alive and CLEAR. */
	public SaintMeterSync meterSync() {
		return meterSync;
	}

	/** The frame id that draws this Saint's bar; the client finds the bar its meter sits under by it. */
	public net.minecraft.resources.ResourceLocation barFrame() {
		return bossBar.frame();
	}

	/** The Saint's own damage type: the kit's, else the one {@link SaintDamageTypes} names for it. */
	public ResourceKey<DamageType> damageType() {
		SaintKit current = kit();
		return current != null ? current.damageType() : SaintDamageTypes.of(saintType());
	}

	// ── hooks ────────────────────────────────────────────────────────

	/** Multiplies the damage of a hit, on top of the open opening's factor. */
	protected float damageTakenFactor(SaintHit hit) {
		return 1.0F;
	}

	/** The health a hit may not take it under; never below 1: a Saint is never killed. */
	protected float healthFloor(SaintHit hit) {
		return 1.0F;
	}

	protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) {
	}

	/** The floor reached, once per floor value; usually {@link #beginEnding}. */
	protected void onFloorReached(SaintHit hit) {
	}

	/** The bar's fill: health by default, the meter for Seraphae, the absolutions for Putriciel. */
	protected float barProgress() {
		return getHealth() / getMaxHealth();
	}

	/** A held Blood Absorption or Projection aimed at this fight; PASS lets the next Saint and the item answer. */
	protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) {
		return SaintChannelRouter.Result.PASS;
	}

	protected boolean presencePaused() {
		return false;
	}

	protected void onArenaResolved(SaintArena arena) {
	}

	/** After a load restored the chamber mid-fight: put back what the phase needs (Putriciel's pyre table). */
	protected void onArenaReloaded() {
	}

	protected void onPhaseChanged(int from, int to) {
	}

	protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
	}

	/** After the kit's tick, on the server. */
	protected void tickSaint() {
	}

	/** A reloaded ending's finishing condition, where the live {@code done} supplier was lost with the save. */
	protected boolean endingDoneAfterLoad() {
		return false;
	}

	@Override
	public void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside) {
	}

	/** The hold ended; the reason says why. */
	protected void onHoldReleased(BossHold.Reason reason) {
	}

	// ── the ending ───────────────────────────────────────────────────

	/**
	 * Starts the ending: cancels the active move, releases the hold, expires the hazards, stops the presence and makes
	 * the Saint untouchable. {@link #endTrial} runs once at the spec's tick (or when {@code done} holds and the spec
	 * finishes early); the arena restores after it; the Saint is discarded at the spec's length.
	 */
	protected final void beginEnding(SaintEnding.Spec spec, @Nullable Player victor, BooleanSupplier done) {
		if (ending != null || trialEnded) return;
		controller.cancel();
		bossHold.release(BossHold.Reason.CANCELLED);
		openings.close();
		if (arena != null) {
			for (net.minecraft.world.entity.Entity owned : arena.owned()) {
				if (owned instanceof BossGroundHazardEntity hazard) hazard.expire(10);
			}
		}
		ending = new SaintEnding.Clock(spec);
		endingVictor = victor == null ? null : victor.getUUID();
		endingDone = done;
		entityData.set(DATA_ENDING_TICK, 0);
	}

	public boolean ending() {
		return ending != null;
	}

	/** Ticks into the ending, -1 while none runs. */
	public int endingTick() {
		return ending == null ? -1 : Math.max(0, ending.tick());
	}

	protected void onEndingTick(int tick) {
	}

	/** True when the Saint was discarded this tick. */
	private boolean tickEnding() {
		SaintEnding.Clock clock = ending;
		if (clock == null) return false;
		boolean done = endingDone != null ? endingDone.getAsBoolean() : endingDoneAfterLoad();
		SaintEnding.Step step = clock.advance(done);
		entityData.set(DATA_ENDING_TICK, Math.max(0, clock.tick()));
		onEndingTick(clock.tick());
		if (step == SaintEnding.Step.END_TRIAL || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
			Player victor = endingVictor == null ? null : level().getPlayerByUUID(endingVictor);
			endTrial(victor, null);
			if (arena != null) arena.restore(clock.spec().restore(), clock.spec().restoreTicks());
			releaseChallengers();
		}
		if (step == SaintEnding.Step.DISCARD || step == SaintEnding.Step.END_TRIAL_AND_DISCARD) {
			if (!trialEnded) endTrial(null, null);
			if (arena != null) arena.restoreNow();
			releaseChallengers();
			discard();
			return true;
		}
		return false;
	}

	/**
	 * Every record back now and every owned entity discarded, resolving the arena first when a load left it pending:
	 * removal, and the server stopping while the chunks are still loaded.
	 */
	public void restoreArenaNow() {
		if (!(this.level() instanceof ServerLevel)) return;
		if (arena == null && loadedKit != null) {
			arena = SaintArena.resolve(this);
			arena.load(loadedKit);
			loadedKit = null;
		}
		if (arena != null) arena.restoreNow();
	}

	/** The leash: an empty chamber for 600 ticks, or an operator: the chamber back, the tomb closed, no loot. */
	public void resetTrial() {
		if (!(this.level() instanceof ServerLevel server)) return;
		if (arena != null) arena.restoreNow();
		bossHold.release(BossHold.Reason.CANCELLED);
		releaseChallengers();
		if (!trialEnded) {
			trialEnded = true;
			returnToTomb(server);
		}
		discard();
	}

	// ── the kit's server tick ────────────────────────────────────────

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level() instanceof ServerLevel && !isRemoved()) kitTick();
	}

	private void kitTick() {
		if (arena == null) {
			arena = SaintArena.resolve(this);
			if (loadedKit != null) {
				arena.load(loadedKit);
				arena.restoreNow();
				loadedKit = null;
				if (trialEnded) {
					discard();
					return;
				}
				onArenaResolved(arena);
				// a Saint loaded mid-fight carries on with a clean chamber; its hook puts back what its phase needs
				if (ending == null) onArenaReloaded();
			} else {
				onArenaResolved(arena);
			}
		}
		stagedHit = null;
		boolean rising = tickRise();
		boolean fighting = !rising && ending == null && isAlive();
		if (fighting) {
			arena.countChallengers();
			scaleForChallengers();
		}
		if (arena.tick(fighting)) {
			resetTrial();
			return;
		}
		if (fighting) {
			phases.tick(this, controller);
			meter.tick(this);
			tickPresence();
			openings.tick();
			controller.tick();
			bossHold.tick();
		}
		if (!rising && !trialEnded) meterSync.tick(this);
		if (ending != null && tickEnding()) return;
		syncKit();
		tickSaint();
	}

	private void tickPresence() {
		SaintPresence presence = kit().presence();
		double radius = presence.radius(this);
		entityData.set(DATA_PRESENCE_RADIUS, (float) radius);
		if (radius <= 0.0D || presencePaused() || phases.cardTicks() > 0 || arena == null) return;
		presenceTicks++;
		boolean pulse = presenceTicks % Math.max(1, presence.cadence(this)) == 0;
		double vertical = presence.verticalReach(this);
		for (Player player : arena.challengers()) {
			double dx = player.getX() - getX(), dz = player.getZ() - getZ();
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (distance > radius || Math.abs(player.getY() - getY()) > vertical) continue;
			if (!ManipulationCombatHelper.canHarmFor(this, player)) continue;
			if (presence.needsSight(this) && !hasLineOfSight(player)) continue;
			float strength = presence.falloff(this, distance) * presence.playerFactor(this, player);
			presence.everyTick(this, player, strength);
			if (pulse) presence.pulse(this, player, strength);
		}
	}

	/** Max health 1 + h (n - 1) of itself whenever the ratcheted challenger count rises, keeping the fraction. */
	private void scaleForChallengers() {
		if (arena == null) return;
		int count = arena.challengerCount();
		if (count == scaledFor) return;
		scaledFor = count;
		float fraction = getMaxHealth() <= 0.0F ? 1.0F : getHealth() / getMaxHealth();
		double amount = kit().healthPerExtraChallenger() * (count - 1);
		AttributeModifiers.syncPermanent(getAttribute(Attributes.MAX_HEALTH), Hemomancy.rloc("saint_challengers"),
				amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, amount > 0.0D);
		setHealth(fraction * getMaxHealth());
	}

	private void syncKit() {
		entityData.set(DATA_PHASE, phases.phase());
		SaintOpening.Spec open = openings.current();
		entityData.set(DATA_OPENING, open == null ? 0 : open.id());
		entityData.set(DATA_OPENING_TICK, open == null ? 0 : openings.age());
		entityData.set(DATA_OPENING_LENGTH, open == null ? 0 : openings.length());
		if (meter.dataDue()) {
			entityData.set(DATA_METER, meter.value());
			entityData.set(DATA_METER_MAX, meter.max());
			meter.markDataSent();
		}
		entityData.set(DATA_DEEDS, deeds.mask());
		if (arena != null) entityData.set(DATA_CHALLENGER_COUNT, arena.challengerCount());
		if (++challengersSince >= CHALLENGERS_SYNC_GAP) {
			CompoundTag roster = challengerRoster();
			if (!roster.equals(entityData.get(DATA_CHALLENGERS))) {
				entityData.set(DATA_CHALLENGERS, roster);
				challengersSince = 0;
			}
		}
		if (ending == null) bossBar.setProgress(Math.max(0.0F, Math.min(1.0F, barProgress())));
	}

	/** Entity id to (value 0..255 of the maximum, flags), as {@code DATA_CHALLENGERS} carries it. */
	private CompoundTag challengerRoster() {
		CompoundTag roster = new CompoundTag();
		if (arena == null) return roster;
		for (Player player : arena.challengers()) {
			SaintMeter.PlayerState state = meter.existing(player.getUUID());
			int coarse = state == null ? 0 : meter.coarse(state);
			int flags = state == null ? 0 : state.flags() & 0xFFFF;
			roster.putInt(Integer.toString(player.getId()), coarse << 16 | flags);
		}
		return roster;
	}

	/** The phase table entered a phase: synced, the bar re-coloured and renamed, the card played once. */
	void phaseEntered(int from, SaintPhaseTable.Phase phase, boolean card) {
		entityData.set(DATA_PHASE, phase.number());
		bossBar.setColor(phase.color());
		if (!phase.nameKey().isEmpty()) bossBar.setName(Component.translatable(phase.nameKey()));
		if (card && phase.card() != null) BossScenes.play(this, phase.card());
		onPhaseChanged(from, phase.number());
	}

	private void openingChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
		onOpeningChanged(from, to);
	}

	private void syncHold(int playerId, Vec3 offset, float slack) {
		entityData.set(DATA_HOLD_PLAYER, playerId);
		entityData.set(DATA_HOLD_OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
		entityData.set(DATA_HOLD_SLACK, slack);
	}

	private void holdReleased(BossHold.Reason reason) {
		onHoldReleased(reason);
	}

	/** The trial ended or reset: roots, presence heals and lessons end for everyone inside. */
	private void releaseChallengers() {
		meterSync.clearAll(this);
		if (arena == null) return;
		List<Player> challengers = arena.challengers();
		for (Player player : challengers) {
			SaintRoot.releaseAll(player);
			SaintHealing.end(player);
		}
		SaintLines.reset(this, challengers);
	}

	/** The router asks: a rising or ending Saint passes. */
	SaintChannelRouter.Result channel(SaintChannelRouter.Request request) {
		if (ending != null || rising()) return SaintChannelRouter.Result.PASS;
		SaintChannelRouter.Result result = onChannel(request);
		return result == null ? SaintChannelRouter.Result.PASS : result;
	}

	/** SaintHitEvents stages the incoming hit's school context for {@code actuallyHurt}. */
	void stageHit(@Nullable SchoolHitContext context) {
		stagedHit = context;
	}

	/** A hit a Saint's channel handler deals (a held Projection struck home): classified as CHANNEL. */
	public boolean hurtByChannel(DamageSource source, float amount) {
		channelHit = true;
		try {
			return hurt(source, amount);
		} finally {
			channelHit = false;
		}
	}

	// ── the kit's damage pipeline ────────────────────────────────────

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		if (isInvulnerableTo(source)) return;
		// a /kill is the one way a Saint is taken: no factor and no floor, so die() sees it (and sends it to its tomb)
		if (source.is(DamageTypes.GENERIC_KILL)) {
			super.actuallyHurt(source, amount);
			return;
		}
		SaintHit hit = classify(source, amount);
		stagedHit = null;
		SaintOpening.Admit admit = openings.admit(hit, arena == null ? 1 : arena.challengerCount());
		float factor = SaintDamageRules.factor(damageTakenFactor(hit), openings.damageTaken());
		float before = getHealth();
		// the floor is read before the blow lands: a Saint's floor may depend on the health it stood at
		float floor = SaintDamageRules.floor(healthFloor(hit));
		// NeoForge's actuallyHurt ignores its amount and spends the damage container's new damage, so the factor
		// goes on the container
		if (!damageContainers.isEmpty()) damageContainers.peek().setNewDamage(damageContainers.peek().getNewDamage() * factor);
		super.actuallyHurt(source, amount * factor);
		float kept = SaintDamageRules.kept(before, getHealth(), floor);
		if (kept != getHealth()) setHealth(kept);
		onHitTaken(hit, before - getHealth(), admit);
		if (SaintDamageRules.reached(getHealth(), floor, lastFloorReached)) {
			lastFloorReached = floor;
			onFloorReached(hit);
		}
	}

	private SaintHit classify(DamageSource source, float amount) {
		Player attacker = source.getEntity() instanceof Player player ? player : null;
		SchoolHitContext context = stagedHit != null ? stagedHit
				: source instanceof SchoolDamageSource school ? school.context() : null;
		boolean projectile = source.getDirectEntity() instanceof Projectile || source.is(DamageTypeTags.IS_PROJECTILE);
		boolean direct = attacker != null && source.getDirectEntity() == attacker;
		SaintOpening.Delivery delivery = SaintDamageRules.delivery(channelHit, context != null, projectile, direct);
		return new SaintHit(attacker, source, amount, delivery, context == null ? null : context.primary(),
				source.is(DamageTypeTags.IS_FIRE), context);
	}

	// ── what the controller and view sync ────────────────────────────

	void syncMoveStart(int id, int serial, int variant, Vec3 origin, Vec3 aim, int target, CompoundTag frame) {
		entityData.set(DATA_MOVE_SERIAL, serial);
		entityData.set(DATA_MOVE_VARIANT, variant);
		entityData.set(DATA_MOVE_ORIGIN, vector(origin));
		entityData.set(DATA_MOVE_AIM, vector(aim));
		entityData.set(DATA_MOVE_TARGET, target);
		entityData.set(DATA_MOVE_FRAME, frame);
		entityData.set(DATA_MOVE_FLAGS, 0);
		entityData.set(DATA_MOVE_TICK, 0);
		entityData.set(DATA_MOVE, id);
	}

	void syncMoveCommit(Vec3 origin, Vec3 aim, CompoundTag frame) {
		entityData.set(DATA_MOVE_ORIGIN, vector(origin));
		entityData.set(DATA_MOVE_AIM, vector(aim));
		entityData.set(DATA_MOVE_FRAME, frame);
		entityData.set(DATA_MOVE_FLAGS, MOVE_COMMITTED | MOVE_AIM_LOCKED);
	}

	void syncMoveAim(Vec3 aim) {
		entityData.set(DATA_MOVE_AIM, vector(aim));
	}

	void syncMoveTarget(int target) {
		entityData.set(DATA_MOVE_TARGET, target);
	}

	void syncMoveFrame(CompoundTag frame) {
		entityData.set(DATA_MOVE_FRAME, frame);
	}

	void syncMoveTick(int tick) {
		entityData.set(DATA_MOVE_TICK, tick);
	}

	void syncMoveEnd() {
		entityData.set(DATA_MOVE, 0);
		entityData.set(DATA_MOVE_FLAGS, 0);
	}

	private static Vector3f vector(Vec3 value) {
		return new Vector3f((float) value.x, (float) value.y, (float) value.z);
	}

	private static Vec3 vec(Vector3f value) {
		return new Vec3(value.x(), value.y(), value.z());
	}

	public int syncedMoveId() {
		return entityData.get(DATA_MOVE);
	}

	public int syncedMoveSerial() {
		return entityData.get(DATA_MOVE_SERIAL);
	}

	public int syncedMoveTick() {
		return entityData.get(DATA_MOVE_TICK);
	}

	public int syncedMoveVariant() {
		return entityData.get(DATA_MOVE_VARIANT);
	}

	public Vec3 syncedMoveOrigin() {
		return vec(entityData.get(DATA_MOVE_ORIGIN));
	}

	public Vec3 syncedMoveAim() {
		return vec(entityData.get(DATA_MOVE_AIM));
	}

	public int syncedMoveTarget() {
		return entityData.get(DATA_MOVE_TARGET);
	}

	public CompoundTag syncedMoveFrame() {
		return entityData.get(DATA_MOVE_FRAME);
	}

	public int syncedMoveFlags() {
		return entityData.get(DATA_MOVE_FLAGS);
	}

	public int syncedPhase() {
		return entityData.get(DATA_PHASE);
	}

	public int syncedOpening() {
		return entityData.get(DATA_OPENING);
	}

	public int syncedOpeningTick() {
		return entityData.get(DATA_OPENING_TICK);
	}

	public int syncedOpeningLength() {
		return entityData.get(DATA_OPENING_LENGTH);
	}

	public float syncedMeter() {
		return entityData.get(DATA_METER);
	}

	public float syncedMeterMax() {
		return entityData.get(DATA_METER_MAX);
	}

	public float syncedPresenceRadius() {
		return entityData.get(DATA_PRESENCE_RADIUS);
	}

	public int syncedDeeds() {
		return entityData.get(DATA_DEEDS);
	}

	public int syncedChallengerCount() {
		return entityData.get(DATA_CHALLENGER_COUNT);
	}

	/** Another player's coarse value (0..255 of the maximum) from {@code DATA_CHALLENGERS}, or -1 when absent. */
	public int syncedChallengerValue(int entityId) {
		CompoundTag roster = entityData.get(DATA_CHALLENGERS);
		String key = Integer.toString(entityId);
		return roster.contains(key) ? roster.getInt(key) >>> 16 : -1;
	}

	/** Another player's Saint-defined flags (bits 0..15) from {@code DATA_CHALLENGERS}, 0 when absent. */
	public int syncedChallengerFlags(int entityId) {
		return entityData.get(DATA_CHALLENGERS).getInt(Integer.toString(entityId)) & 0xFFFF;
	}

	public int syncedHoldPlayer() {
		return entityData.get(DATA_HOLD_PLAYER);
	}

	public Vec3 syncedHoldOffset() {
		return vec(entityData.get(DATA_HOLD_OFFSET));
	}

	/** Negative when the hold leaves input unlocked. */
	public float syncedHoldSlack() {
		return entityData.get(DATA_HOLD_SLACK);
	}

	public int syncedEndingTick() {
		return entityData.get(DATA_ENDING_TICK);
	}

	@Override
	public void beginRise() {
		rise.begin();
		this.entityData.set(DATA_RISE_TICK, 0);
		bossBar.setVisible(false);
	}

	@Override
	public float riseTime(float partialTick) {
		return SaintRise.time(riseClock, this.entityData.get(DATA_RISE_TICK), this.tickCount, partialTick);
	}

	/**
	 * Called first in the kit's server tick: true while the Saint is still rising out of its sarcophagus, so it skips
	 * its fight. Shows the bar on the tick the rise ends.
	 */
	private boolean tickRise() {
		if (!rise.active()) return false;
		if (this.level() instanceof ServerLevel server) SaintRise.tickEffects(server, this, rise.elapsed());
		boolean ended = rise.tick();
		this.entityData.set(DATA_RISE_TICK, rise.elapsed());
		if (ended) bossBar.setVisible(true);
		return true;
	}

	/** Still coming out of the sarcophagus: a Saint's own {@code hurt} rules wait for it. */
	protected boolean rising() {
		return rise.active();
	}

	/** Rising, it neither moves nor runs its goals, but it still settles onto the floor. */
	@Override
	protected boolean isImmobile() {
		return super.isImmobile() || rise.active();
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return (rise.active() || ending != null) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				|| super.isInvulnerableTo(source);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		rise.save(tag, "SaintRise");
		tag.putBoolean(TAG_TRIAL_ENDED, trialEnded);
		tag.putString("SaintAwakening", awakening);
		tag.putString("SaintTombRecord", tombRecord);
		net.minecraft.nbt.ListTag skipped = new net.minecraft.nbt.ListTag();
		skippedChapels.forEach(kind -> skipped.add(net.minecraft.nbt.StringTag.valueOf(kind)));
		tag.put("SaintSkippedChapels", skipped);
		if (tombPos != null && tombState != null) {
			tag.putLong(TAG_TOMB_POS, tombPos.asLong());
			tag.put(TAG_TOMB_STATE, NbtUtils.writeBlockState(tombState));
		}
		tag.put(TAG_KIT, saveKit());
	}

	private CompoundTag saveKit() {
		// the meter and phase table are the kit's own objects, so resolve it before writing them
		kit();
		CompoundTag kitTag = new CompoundTag();
		controller.save(kitTag);
		meter.save(kitTag);
		openings.save(kitTag);
		phases.save(kitTag);
		if (arena != null) arena.save(kitTag);
		else if (loadedKit != null && loadedKit.contains("SaintArena")) kitTag.put("SaintArena", loadedKit.getCompound("SaintArena"));
		if (ending != null) {
			kitTag.put("Ending", ending.save());
			if (endingVictor != null) kitTag.putUUID("EndingVictor", endingVictor);
		}
		kitTag.putFloat("FloorReached", lastFloorReached);
		return kitTag;
	}

	private void loadKit(CompoundTag kitTag) {
		// resolve the kit first: before it, meter and phases are placeholders that kit() would replace, losing the load
		kit();
		controller.load(kitTag);
		meter.load(kitTag);
		openings.load(kitTag);
		phases.load(kitTag);
		SaintPhaseTable.Phase current = phases.current();
		entityData.set(DATA_PHASE, current.number());
		bossBar.setColor(current.color());
		if (!current.nameKey().isEmpty()) bossBar.setName(Component.translatable(current.nameKey()));
		ending = kitTag.contains("Ending") ? SaintEnding.Clock.load(kitTag.getCompound("Ending")) : null;
		endingVictor = kitTag.hasUUID("EndingVictor") ? kitTag.getUUID("EndingVictor") : null;
		endingDone = null;
		lastFloorReached = kitTag.contains("FloorReached") ? kitTag.getFloat("FloorReached") : -1.0F;
		// the chamber is restored on the first server tick, when the arena resolves against the loaded level
		loadedKit = kitTag.copy();
		arena = null;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		rise.load(tag, "SaintRise");
		this.entityData.set(DATA_RISE_TICK, rise.elapsed());
		if (rise.active()) bossBar.setVisible(false);
		trialEnded = tag.getBoolean(TAG_TRIAL_ENDED);
		// The enrage's modifiers and effects were saved with the entity; only the record is read back.
		if (tag.contains("SaintAwakening")) awakening = tag.getString("SaintAwakening");
		if (tag.contains("SaintTombRecord")) tombRecord = tag.getString("SaintTombRecord");
		skippedChapels.clear();
		for (net.minecraft.nbt.Tag kind : tag.getList("SaintSkippedChapels", net.minecraft.nbt.Tag.TAG_STRING)) {
			skippedChapels.add(kind.getAsString());
		}
		if (tag.contains(TAG_TOMB_POS) && tag.contains(TAG_TOMB_STATE)) {
			tombPos = BlockPos.of(tag.getLong(TAG_TOMB_POS));
			tombState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK),
					tag.getCompound(TAG_TOMB_STATE));
		}
		deeds = SaintDeeds.of(awakening, skippedChapels);
		this.entityData.set(DATA_DEEDS, deeds.mask());
		if (tag.contains(TAG_KIT)) loadKit(tag.getCompound(TAG_KIT));
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossBar.show(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.hide(player);
	}

	/** Sends a translated chat line to everyone in the fight. */
	protected void broadcastMessage(String key, ChatFormatting color) {
		broadcast(Component.translatable(key).withStyle(color));
	}

	/** Sends a chat line to everyone in the fight: by default, whoever sees the bar. */
	protected void broadcast(Component message) {
		bossBar.broadcast(message);
	}

	// ── the ending ────────────────────────────────────────────────────

	/**
	 * Remembers the sarcophagus this Saint rose from (its position and block state, before the encounter clears it),
	 * so that the ending can close the Saint back into it.
	 */
	/** How this Saint was woken: through a ring door opened properly, shattered (enraged), or neither. */
	private String awakening = "NONE";
	/** The tomb's own door record when the Saint rose (the awakening may be inferred from who woke it). */
	private String tombRecord = "NONE";
	private final java.util.List<String> skippedChapels = new java.util.ArrayList<>();

	public String awakening() {
		return awakening;
	}

	/**
	 * Wakes the Saint as its ring door was opened. A forced awakening enrages it once: harder and faster, with the
	 * deed of every skipped Chapel.
	 */
	public void setAwakening(String mode, java.util.List<String> skipped) {
		setAwakening(mode, skipped, mode);
	}

	public void setAwakening(String mode, java.util.List<String> skipped, String rawRecord) {
		awakening = mode;
		tombRecord = rawRecord;
		skippedChapels.clear();
		skippedChapels.addAll(skipped);
		deeds = SaintDeeds.of(mode, skipped);
		this.entityData.set(DATA_DEEDS, deeds.mask());
		if (!SaintEnrageRules.enraged(mode)) return;
		java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind> kinds = new java.util.ArrayList<>();
		for (String name : skipped) {
			try {
				kinds.add(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.valueOf(name));
			} catch (IllegalArgumentException ignored) {
				// an unknown Chapel lends nothing
			}
		}
		var add = net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE), Hemomancy.rloc("saint_enrage_damage"),
				SaintEnrageRules.damageBonus(), add, true);
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED), Hemomancy.rloc("saint_enrage_speed"),
				SaintEnrageRules.speedBonus(kinds), add, true);
		double health = SaintEnrageRules.healthBonus(kinds);
		com.vincenthuto.hemomancy.common.util.AttributeModifiers.syncPermanent(getAttribute(
				net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH), Hemomancy.rloc("saint_enrage_fall"),
				health, add, health > 0);
		setHealth(getMaxHealth());
		if (SaintEnrageRules.resists(kinds)) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
		if (SaintEnrageRules.regenerates(kinds)) {
			addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,
					net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION, 0, false, false));
		}
	}

	public void bindTomb(BlockPos pos, BlockState sarcophagus) {
		this.tombPos = pos.immutable();
		this.tombState = sarcophagus;
	}

	@Nullable
	public BlockPos getTombPos() {
		return tombPos;
	}

	/** The sarcophagus the Saint rose from, as it stood (its facing turns the chamber round it); {@code null} with no tomb. */
	@Nullable
	public BlockState getTombState() {
		return tombState;
	}

	/** True once {@link #endTrial} has run: the rewards are given and the tomb is closed. */
	public boolean hasTrialEnded() {
		return trialEnded;
	}

	/**
	 * Ends the trial, once per encounter: rolls the Saint's loot table for {@code victor} (or the nearest living player
	 * when no one is named), lets the Saint have its say, then restores its sarcophagus where it stood. Later calls,
	 * and calls on the client, do nothing. A removed or unloaded Saint never calls it.
	 */
	protected final void endTrial(@Nullable Player victor, @Nullable DamageSource cause) {
		if (trialEnded || !(this.level() instanceof ServerLevel server)) return;
		trialEnded = true;
		Player recipient = victor != null && victor.isAlive() ? victor
				: server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), REWARD_RANGE,
						entity -> entity.isAlive() && !entity.isSpectator());
		rollTrialRewards(server, recipient, cause);
		announceTrialEnd(recipient);
		returnToTomb(server);
	}

	/** The Saint's own word on its ending, said before the tomb closes; {@code victor} carries the reward. */
	protected void announceTrialEnd(@Nullable Player victor) {
	}

	/** True when the rewards go into the victor's inventory (overflow drops at their feet), not onto the floor. */
	protected boolean rewardsGoToVictor() {
		return false;
	}

	/** A Saint is never killed: only a {@code /kill} takes it, and that sends it back to its tomb ({@link #remove}). */
	@Override
	public void die(DamageSource source) {
		if (!source.is(DamageTypes.GENERIC_KILL)) {
			setHealth(Math.max(1.0F, getHealth()));
			return;
		}
		super.die(source);
	}

	/** The loot table rolls once, in {@link #endTrial}, whatever the ending; never again as vanilla death loot. */
	@Override
	protected void dropFromLootTable(DamageSource damageSource, boolean hitByPlayer) {
	}

	private void rollTrialRewards(ServerLevel server, @Nullable Player recipient, @Nullable DamageSource cause) {
		DamageSource source = cause != null ? cause
				: recipient != null ? this.damageSources().playerAttack(recipient) : this.damageSources().generic();
		LootParams.Builder params = new LootParams.Builder(server)
				.withParameter(LootContextParams.THIS_ENTITY, this)
				.withParameter(LootContextParams.ORIGIN, this.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, source)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
		if (recipient != null) {
			params = params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, recipient).withLuck(recipient.getLuck());
		}
		LootTable table = server.getServer().reloadableRegistries().getLootTable(this.getType().getDefaultLootTable());
		for (ItemStack rolled : table.getRandomItems(params.create(LootContextParamSets.ENTITY))) {
			ItemStack reward = SaintEnrageRules.fragmentOnly(awakening) ? fragmentOf(rolled) : rolled;
			// a reward handed into the inventory is never picked up, so it unlocks its Liber pages here
			if (recipient instanceof ServerPlayer player) LiberDiscoveryEvents.unlockForItem(player, reward);
			if (recipient != null && rewardsGoToVictor()) {
				if (!recipient.getInventory().add(reward)) {
					recipient.drop(reward, false);
				}
			} else {
				this.spawnAtLocation(reward);
			}
		}
	}

	/** A Saint taken out of the world without an ending (a {@code /kill}, a discard) still goes back into its tomb, unrewarded. */
	@Override
	public void remove(RemovalReason reason) {
		if (kitResolved && reason.shouldDestroy() && this.level() instanceof ServerLevel) {
			restoreArenaNow();
			bossHold.release(BossHold.Reason.CANCELLED);
			releaseChallengers();
		}
		if (!trialEnded && reason.shouldDestroy() && this.isAddedToLevel() && this.level() instanceof ServerLevel server) {
			trialEnded = true;
			returnToTomb(server);
		}
		super.remove(reason);
	}

	/** Peaceful never clears a Saint away: its tomb is already open. */
	@Override
	public boolean shouldDespawnInPeaceful() {
		return false;
	}

	/** The tomb is remembered by position alone, so the Saint stays in its own dimension. */
	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	/** A forced fight leaves only a fragment where a whole Hallowed Residuum would have been. */
	private static ItemStack fragmentOf(ItemStack rolled) {
		var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(rolled.getItem());
		if (!id.getNamespace().equals(Hemomancy.MOD_ID) || !id.getPath().startsWith("hallowed_residuum_")
				|| id.getPath().startsWith("hallowed_residuum_fragment_")) return rolled;
		var fragment = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(Hemomancy.rloc(
				id.getPath().replace("hallowed_residuum_", "hallowed_residuum_fragment_")));
		return fragment == net.minecraft.world.item.Items.AIR ? rolled : new ItemStack(fragment, rolled.getCount());
	}

	private void returnToTomb(ServerLevel server) {
		if (tombPos == null || tombState == null) return;
		if (!SaintSarcophagusBlock.restoreDormant(server, tombPos, tombState, saintType())) {
			Hemomancy.LOGGER.warn("Saint {} could not return to its tomb at {}: the space is occupied", saintType(), tombPos);
			return;
		}
		// The door stays as it was opened: a tomb behind a shattered door wakes enraged again.
		if (server.getBlockEntity(tombPos) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity tomb
				&& SaintEnrageRules.recordToKeep(tombRecord) != null) {
			java.util.List<com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind> kinds = new java.util.ArrayList<>();
			for (String name : skippedChapels) {
				try {
					kinds.add(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.valueOf(name));
				} catch (IllegalArgumentException ignored) {
					// unknown names are dropped
				}
			}
			try {
				tomb.recordDoor(com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity.DoorMode.valueOf(tombRecord), kinds);
			} catch (IllegalArgumentException unknown) {
				// an unreadable record is not written back
			}
		}
		broadcast(Component.translatable("message.hemomancy.saint.returns_to_tomb", saintType().getDisplayName())
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
	}
}
