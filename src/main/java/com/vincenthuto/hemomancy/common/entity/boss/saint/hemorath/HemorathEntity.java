package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Saint Hemorath, the Hollow Vessel, on the Saint combat kit: <em>Hemorath consumed</em>. His body drinks every
 * challenger near him into a meter that leaks all the time, faster the fuller he is, down the gutters into four wells
 * he can call back. Hollow he is quick, hungry and iron-hard; full he is slow, strong and soft. Both roads to the
 * ending want him fed: overload him (the first full meter empties him into phase 2; the next bursts him), or wear his
 * body down while he is swollen and keep him from his wells. Design: {@code docs/superpowers/specs/
 * 2026-11-05-saint-hemorath-overhaul-design.md}.
 * <p>
 * The kit owns his tick order, damage pipeline, phases, presence and ending; this class supplies the kit and the hooks,
 * and holds what is his alone: the wells, the patches, the Tithe ledger, the Last Thirst and the tide. He cannot be
 * killed: every ending goes through {@code endTrial} once, and the marrow labours on in the closed tomb.
 */
public class HemorathEntity extends SaintTrialEntity {
	public static final String NAME_KEY = "entity.hemomancy.hemorath";
	public static final String EMPTIES_KEY = "message.hemomancy.hemorath.saint_hemorath_the_vessel_empties";
	public static final String THIRST_KEY = "message.hemomancy.hemorath.saint_hemorath_the_last_thirst";
	public static final String LESSON_BRIMMING = "message.hemomancy.hemorath.lesson_brimming";
	public static final String LESSON_GORGED = "message.hemomancy.hemorath.lesson_gorged";
	public static final String LESSON_TITHE = "message.hemomancy.hemorath.lesson_tithe";
	public static final String RIBS_SHUT = "message.hemomancy.hemorath.ribs_shut";
	public static final String BLOOD_ANSWERS_HIM = "message.hemomancy.hemorath.blood_answers_him";
	public static final String MARROW_LABOURS = "message.hemomancy.hemorath.the_marrow_labours_on";
	public static final String VICTOR_LINE = "message.hemomancy.hemorath.the_vessel_quiets_you_carry_the";

	/** The solo meter maximum (the old overload threshold). */
	public static final float OVERLOAD_THRESHOLD = HemorathMeterRules.BASE_MAX;
	public static final double MAX_HEALTH = 600.0D;
	public static final double ARMOR = 4.0D;
	public static final double ATTACK = 8.0D;
	public static final double KNOCKBACK_RESISTANCE = 0.8D;
	public static final double FOLLOW_RANGE = 48.0D;
	/** The chest the drain and the projections aim at, above his feet. */
	public static final double CHEST_HEIGHT = 2.0D;
	public static final double CHEST_CONE = 30.0D;
	public static final double WELL_CONE = 25.0D;
	public static final double CHAIN_CONE = 20.0D;
	public static final int CHAIN_SAMPLES = 8;
	/** Animus Pressure pressed into him fills the meter this much a level. */
	public static final float PRESSURE_FEED = 60.0F;
	/** The Last Thirst's running gutters take this a tick from whoever stands in one. */
	public static final float THIRST_GUTTER_DRAIN = 3.0F;
	public static final float THIRST_PER_HEALTH = 40.0F;
	/** Chained Debt's reel: up to 0.4 a tick for at most 20 ticks, until 2 blocks off. */
	public static final double REEL_SPEED = 0.4D;
	public static final int REEL_TICKS = 20;
	public static final double REEL_STOP = 2.0D;
	/** Standing iron and the tide, as ground hazards he owns. */
	public static final int HAZARD_THICKET = 1, HAZARD_TIDE = 2;
	public static final int THICKET_LIFE = 160, THICKET_PULSE = 10;
	public static final float THICKET_DAMAGE = 2.0F;
	public static final int TIDE_PULSE = 10;
	public static final float TIDE_DRAIN = 15.0F;
	public static final float TIDE_DAMAGE = 1.0F;
	/** Where the challengers' blood fell, kept this long for the Iron Thicket (resolution A-2). */
	public static final int BLOOD_MARK_TICKS = 100;
	public static final int BLOOD_MARKS = 16;
	/** The well streams' visual refresh during a draw. */
	public static final int STREAM_EVERY = 10;

	private static final EntityDataAccessor<Integer> DATA_PATCHES =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_WELLS =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TARGET_DEBT =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_THIRST =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<CompoundTag> DATA_CHAMBER =
			SynchedEntityData.defineId(HemorathEntity.class, EntityDataSerializers.COMPOUND_TAG);

	private final HemorathTitheLedger tithe = new HemorathTitheLedger();
	private final HemorathWells wells = new HemorathWells();
	private final HemorathPatches patches = new HemorathPatches();
	private final HemorathEnding endingBeats = new HemorathEnding();
	private final Deque<BloodMark> bloodMarks = new ArrayDeque<>();
	private final Set<UUID> gorgeContacts = new HashSet<>();
	private final float[] denied = new float[HemorathWellRules.WELLS];
	@Nullable
	private HemorathArena chamber;
	private float leakThisTick;
	private boolean pendingOverload;
	private boolean emptiedByOverload;
	private boolean emptying;
	private boolean thirstUsed;
	private int thirstTicks = -1;
	private float tideHeld;
	private int tideTicks;
	private int fightTicks;
	private boolean downfallApplied;
	private boolean firstSiphonDone;
	private float siphonTook;
	private float wellsDrawn;
	private boolean lastFeedFromWell;
	private int lastPressure;
	private int lastLodestone;
	private boolean lastRime;
	@Nullable
	private UUID hooked;
	private int hookTicks;
	private float bodyWidth = HemorathVisualRules.WIDTH_SUNKEN;

	/** One place a challenger's blood fell, for the Iron Thicket. */
	private record BloodMark(Vec3 at, int tick) {
	}

	public HemorathEntity(EntityType<? extends HemorathEntity> type, Level level) {
		super(type, level, 80, new BossBarBinding("entity.hemomancy.hemorath", BossPresentationIds.FRAME_HEMORATH, BossEvent.BossBarColor.RED,
				BossEvent.BossBarOverlay.NOTCHED_10));
	}

	@Override
	public EnumSaintType saintType() {
		return EnumSaintType.HEMORATH;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, HemorathStalk.BASE_SPEED)
				.add(Attributes.ATTACK_DAMAGE, ATTACK)
				.add(Attributes.ATTACK_KNOCKBACK, 1.0D)
				.add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE)
				.add(Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RESISTANCE)
				.add(Attributes.ARMOR, ARMOR);
	}

	// ── the kit ──────────────────────────────────────────────────────

	@Override
	protected SaintKit createKit() {
		// runs on both sides: the telegraph pass reads moves through it, so nothing here may touch server state
		SaintPhaseTable phases = SaintPhaseTable.byHealth(
				new SaintPhaseTable.Phase(1, NAME_KEY, BossEvent.BossBarColor.RED, 1.0F, null),
				new SaintPhaseTable.Phase(2, EMPTIES_KEY, BossEvent.BossBarColor.PURPLE, HemorathPhaseRules.PHASE_2_HEALTH,
						BossPresentationIds.SCENE_HEMORATH_EMPTIES_CARD));
		return new SaintKit(List.of(HemorathMove.values()), new HemorathStalk(this),
				new SaintMeter(SaintMeter.Owner.SAINT, new HemorathMeter(this)), new HemorathPresence(this), phases,
				SaintDamageTypes.HEMORATH, SaintKit.CHALLENGER_SCALING, HemorathEndingRules.SPEC);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PATCHES, 0);
		builder.define(DATA_WELLS, HemorathWellRules.pack(new float[HemorathWellRules.WELLS]));
		builder.define(DATA_TARGET_DEBT, 0);
		builder.define(DATA_THIRST, -1);
		builder.define(DATA_CHAMBER, new CompoundTag());
	}

	// ── what the rest of his package reads ───────────────────────────

	public HemorathTitheLedger tithe() {
		return tithe;
	}

	public HemorathWells wells() {
		return wells;
	}

	@Nullable
	public HemorathArena chamber() {
		return chamber;
	}

	public int patchCount() {
		return patches.count();
	}

	public boolean emptying() {
		return emptying;
	}

	/** Sets the standing patches outright: for the fight's GameTests and review fixtures, never the fight itself. */
	public void forcePatches(int count) {
		int now = patches.count();
		if (count < now) patches.tear(now - count, tickCount);
		else if (count > now) patches.leak((count - now) * HemorathIronRules.LEAK_PER_PATCH, tickCount);
		syncExtras();
	}

	public boolean thirstUsed() {
		return thirstUsed;
	}

	/** During the Last Thirst: its drag to the pit and its 200 rooted ticks. */
	public boolean thirsting() {
		if (level().isClientSide) return syncedThirst() >= 0 || syncedMoveId() == HemorathMove.LAST_THIRST.id();
		return thirstTicks >= 0 || controller().move() == HemorathMove.LAST_THIRST;
	}

	public float fraction() {
		return level().isClientSide ? syncedFraction() : meter().fraction();
	}

	public HemorathMeterRules.State state() {
		return HemorathMeterRules.state(fraction());
	}

	public Vec3 chest() {
		return position().add(0.0D, CHEST_HEIGHT, 0.0D);
	}

	public float tideHeld() {
		return tideHeld;
	}

	public float wellsDrawn() {
		return wellsDrawn;
	}

	/** The Downfall deed's half leak still runs. */
	private boolean fresh() {
		return HemorathMeterRules.fresh(deeds().skipped(ChapelRules.Kind.DOWNFALL), fightTicks);
	}

	private boolean litany() {
		return deeds().skipped(ChapelRules.Kind.LITANY);
	}

	public HemorathPickRules.Situation situation(SaintPickContext ctx) {
		SaintArena arena = arena();
		List<UUID> roster = arena == null ? List.of() : arena.challengers().stream().map(Player::getUUID).toList();
		float pair = litany() ? sum(wells.contents()) : wells.bestPairContent(level().getGameTime());
		boolean pit = chamber != null && chamber.inPit(position());
		return new HemorathPickRules.Situation(ctx.phase(), ctx.band(), ctx.sector(), meter().fraction(), patches.count(),
				tithe.max(roster), pit, pair, ctx.kiteTicks(), ctx.hugTicks(), deeds().skipped(ChapelRules.Kind.DOWNFALL),
				thirsting());
	}

	/** He walks to the pit when hungry for a pair he may draw, or a pair runs high. */
	public boolean wantsPit() {
		if (chamber == null || wells.count() == 0) return false;
		if (controller().cooldownLeft(HemorathMove.WELLS_ANSWER) > 0) return false;
		float pair = litany() ? sum(wells.contents()) : wells.bestPairContent(level().getGameTime());
		if (pair < HemorathWellRules.MIN_PAIR) return false;
		return meter().fraction() < HemorathMeterRules.FILLING_FROM || pair >= HemorathWellRules.HUNGRY_PAIR;
	}

	/** The wells a draw takes now: all four under the Litany deed, else the fullest pair off its cooldown. */
	public int chooseWells() {
		if (litany()) return wells.allMask();
		int pair = wells.bestPair(level().getGameTime());
		return pair < 0 ? 0 : HemorathWells.pairMask(pair);
	}

	// ── the meter ────────────────────────────────────────────────────

	/** Feeds the meter (raised in phase 2); returns what went in. Refused while he empties or once the fight is over. */
	public float feed(float amount, @Nullable Player contributor, boolean fromWell) {
		if (amount <= 0.0F || kit() == null || ending() || hasTrialEnded() || emptying || rising()) return 0.0F;
		float moved = meter().add(HemorathMeterRules.intake(amount, phases().phase() >= 2), contributor);
		if (moved > 0.0F) {
			lastFeedFromWell = fromWell;
			if (thirsting()) heal(moved / THIRST_PER_HEALTH);
		}
		return moved;
	}

	/** The old public door, kept for the fight's own tests: blood a player gave him. */
	public void addAbsorbedBlood(Player source, double amount) {
		feed((float) amount, source, false);
	}

	public float getAbsorbedBlood() {
		return level().isClientSide ? syncedMeter() : meter().value();
	}

	public boolean isInPhase2() {
		return (level().isClientSide ? syncedPhase() : phases().phase()) >= 2;
	}

	/** The leak the meter rule asks for this tick. */
	float leakNow(float value, float max) {
		if (emptying || thirsting() || openings().isOpen(HemorathOpenings.GORGED) || ending()) return 0.0F;
		return HemorathMeterRules.leak(max, max <= 0.0F ? 0.0F : value / max, phases().phase() >= 2,
				SchoolStates.has(this, SchoolState.SEARING), false, fresh());
	}

	void leaked(float amount) {
		leakThisTick += Math.max(0.0F, amount);
	}

	void thresholdCrossed(int threshold, boolean rising) {
		if (!rising) return;
		switch (threshold) {
			case HemorathMeterRules.THRESHOLD_FILLING -> HemorathEffects.sound(this, SoundInit.RITE_ANCHOR_FILL, 0.8F, 0.6F);
			case HemorathMeterRules.THRESHOLD_BRIMMING -> {
				HemorathEffects.sound(this, SoundInit.RITE_ANCHOR_FILL, 0.8F, 0.8F);
				teachAll(LESSON_BRIMMING);
			}
			case HemorathMeterRules.THRESHOLD_FULL -> pendingOverload = true;
			default -> {
			}
		}
	}

	@Nullable
	private Player overloadVictor() {
		SaintArena arena = arena();
		if (lastFeedFromWell || meter().lastContributor() == null) {
			if (arena == null || arena.challengers().isEmpty()) return null;
			Player nearest = null;
			for (Player player : arena.challengers()) {
				if (nearest == null || distanceToSqr(player) < distanceToSqr(nearest)) nearest = player;
			}
			return nearest;
		}
		return level().getPlayerByUUID(meter().lastContributor());
	}

	private void handleOverload() {
		if (!pendingOverload) return;
		pendingOverload = false;
		if (HemorathPhaseRules.overload(phases().phase()) == HemorathPhaseRules.Overload.EMPTIES && !emptiedByOverload) {
			emptiedByOverload = true;
			emptying = true;
			phases().advance(2, SaintCombatController.Cut.IMMEDIATE);
			return;
		}
		beginEnding(HemorathEndingRules.SPEC, overloadVictor(), () -> false);
	}

	// ── kit hooks ────────────────────────────────────────────────────

	@Override
	protected float damageTakenFactor(SaintHit hit) {
		boolean dragging = controller().move() == HemorathMove.LAST_THIRST
				&& controller().stage() == BossMoveTimeline.Stage.WINDUP;
		return HemorathDamageRules.factor(meter().fraction(), patches.count(), dragging);
	}

	@Override
	protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) {
		if (openings().isOpen(HemorathOpenings.STRAINED) && hit.amount() >= HemorathDamageRules.RIB_CRACK) {
			controller().requestStagger(HemorathDamageRules.RIB_CRACK_FLINCH, hit.attacker() == null ? null
					: hit.attacker().position());
			HemorathEffects.sound(this, SoundEvents.BONE_BLOCK_BREAK, 1.0F, 0.6F);
		}
		if (controller().move() == HemorathMove.MARROW_SIPHON && controller().stage() == BossMoveTimeline.Stage.ACTIVE
				&& hit.amount() >= HemorathDamageRules.SIPHON_CHOKE) {
			controller().skipToRecovery();
		}
		if (state() == HemorathMeterRules.State.BRIMMING && applied > 0.0F) {
			HemorathEffects.sound(this, SoundEvents.GENERIC_DRINK, 0.5F, 0.6F);
		}
	}

	@Override
	protected void onFloorReached(SaintHit hit) {
		beginEnding(HemorathEndingRules.SPEC, hit.attacker(), () -> false);
	}

	@Override
	protected void onArenaResolved(SaintArena arena) {
		chamber = HemorathArena.scan(arena, position());
		wells.count(chamber.wells().size());
		CompoundTag points = new CompoundTag();
		long[] positions = new long[chamber.wells().size()];
		for (int i = 0; i < positions.length; i++) positions[i] = chamber.wells().get(i).asLong();
		points.putLongArray("wells", positions);
		points.putDouble("pit_x", chamber.pit().x);
		points.putDouble("pit_y", chamber.pit().y);
		points.putDouble("pit_z", chamber.pit().z);
		BlockPos tomb = arena.tomb();
		points.putLong("tomb", tomb.asLong());
		entityData.set(DATA_CHAMBER, points);
	}

	@Override
	protected void onPhaseChanged(int from, int to) {
		if (to < 2 || from >= 2) return;
		if (!(level() instanceof ServerLevel server)) return;
		HemorathEffects.sound(this, SoundInit.RITE_BACKLASH, 2.0F, 0.6F);
		HemorathEffects.burst(server, chest().add(0.0D, 0.6D, 0.0D), HemorathVisualRules.PHASE_BREAK, 2.5F);
		if (!emptying) return;
		// the first full meter: he empties across the floor instead of bursting
		setHealth(HemorathPhaseRules.tornHealth(getHealth(), getMaxHealth()));
		tearPatches(patches.count(), true);
		controller().start(HemorathMove.EXSANGUINATING_TIDE, SaintCombatController.Cut.IMMEDIATE);
	}

	@Override
	protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
		if (to == null) return;
		if (to.id() == HemorathOpenings.GORGED.id()) {
			HemorathEffects.sound(this, SoundEvents.GENERIC_DRINK, 1.2F, 0.4F);
			teachAll(LESSON_GORGED);
		} else if (to.id() == HemorathOpenings.STRAINED.id()) {
			HemorathEffects.sound(this, SoundEvents.BONE_BLOCK_BREAK, 1.2F, 0.5F);
		}
	}

	@Override
	protected void onEndingTick(int tick) {
		if (level() instanceof ServerLevel server) endingBeats.tick(this, server, tick);
	}

	@Override
	protected boolean endingDoneAfterLoad() {
		return false;
	}

	@Override
	public void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside) {
		for (LivingEntity body : inside) {
			if (hazard.kind() == HAZARD_THICKET) {
				HemorathMoves.thicketPulse(this, hazard, body);
			} else if (hazard.kind() == HAZARD_TIDE) {
				float paid = HemorathMoves.drain(body, TIDE_DRAIN, HemorathSources.TIDE);
				if (paid > 0.0F) {
					tideHeld += paid;
					bloodFell(body);
				} else {
					SaintMoves.hurt(this, body, TIDE_DAMAGE, null);
				}
			}
		}
	}

	@Override
	public boolean hazardTouches(LivingEntity entity) {
		return com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(this, entity);
	}

	// ── held channels ────────────────────────────────────────────────

	@Override
	protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) {
		ServerPlayer player = request.player();
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		boolean atChest = request.entity() != null && request.entity().getEntity() == this
				|| SaintChannelRouter.aimsAt(request, chest(), request.reach(), CHEST_CONE, true);
		int well = aimedWell(request);
		if (request.kind() == SaintChannelRouter.Kind.PROJECT) {
			if (openings().isOpen(HemorathOpenings.TITHE) && controller().target() == player && aimsAtChain(request)) {
				float rate = (float) Math.min(HemorathOpenings.FEED_RATE, request.ratePerTick());
				float owed = tithe.debt(player.getUUID());
				if (owed > 0.0F) {
					float paid = (float) SaintChannelRouter.pay(request, Math.min(rate, owed), HemorathSources.TITHE_PAYMENT).paid();
					tithe.pay(player.getUUID(), paid);
					feed(paid * HemorathOpenings.TITHE_FEED, player, false);
					if (tithe.debt(player.getUUID()) <= 0.0F) HemorathEffects.sound(this, SoundInit.RITE_OFFERING_DRAWN, 1.0F, 0.8F);
					return SaintChannelRouter.Result.taken(paid);
				}
			}
			float conversion = HemorathOpenings.feed(syncedOpeningId(), thirsting());
			if (atChest && conversion > 0.0F) {
				float rate = (float) Math.min(HemorathOpenings.FEED_RATE, request.ratePerTick());
				float paid = (float) SaintChannelRouter.pay(request, rate, HemorathSources.VESSEL_OFFERING).paid();
				feed(paid * conversion, player, false);
				bloodFell(player);
				return SaintChannelRouter.Result.taken(paid);
			}
			if (well >= 0) {
				float put = HemorathWellRules.project(wells.content(well), request.ratePerTick(), volume.getBloodVolume());
				if (put <= 0.0F) return SaintChannelRouter.Result.taken(0.0D);
				float paid = (float) SaintChannelRouter.pay(request, put, HemorathSources.WELL_OFFERING).paid();
				wells.credit(well, paid);
				return SaintChannelRouter.Result.taken(paid);
			}
			if (atChest) return SaintChannelRouter.Result.refused(Component.translatable(RIBS_SHUT)
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			return SaintChannelRouter.Result.PASS;
		}
		if (well >= 0) {
			double room = Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume());
			float wanted = HemorathWellRules.absorb(wells.content(well), request.ratePerTick(), room);
			float taken = wells.take(well, wanted);
			double credited = SaintChannelRouter.credit(player, taken);
			if ((wells.drawMask() & (1 << well)) != 0) denied[well] += taken;
			return SaintChannelRouter.Result.taken(credited);
		}
		if (atChest) return SaintChannelRouter.Result.refused(Component.translatable(BLOOD_ANSWERS_HIM)
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		return SaintChannelRouter.Result.PASS;
	}

	private int syncedOpeningId() {
		SaintOpening.Spec open = openings().current();
		return open == null ? 0 : open.id();
	}

	private int aimedWell(SaintChannelRouter.Request request) {
		if (chamber == null) return -1;
		for (int i = 0; i < chamber.wells().size(); i++) {
			BlockPos pos = chamber.wells().get(i);
			if (request.block() != null && request.block().getBlockPos().equals(pos)) return i;
			if (SaintChannelRouter.aimsAt(request, Vec3.atCenterOf(pos), request.reach(), WELL_CONE, true)) return i;
		}
		return -1;
	}

	/** Whether a Tithe payer aims along his chain: at any point of it within the item's reach. */
	private boolean aimsAtChain(SaintChannelRouter.Request request) {
		Vec3 from = clawPoint();
		Vec3 to = request.player().position().add(0.0D, request.player().getBbHeight() * 0.55D, 0.0D);
		for (int i = 0; i <= CHAIN_SAMPLES; i++) {
			Vec3 point = from.lerp(to, i / (double) CHAIN_SAMPLES);
			if (SaintChannelRouter.aimsAt(request, point, request.reach(), CHAIN_CONE, false)) return true;
		}
		return false;
	}

	/** About where his claw hangs: a little forward and to his right of the chest. */
	public Vec3 clawPoint() {
		double yaw = Math.toRadians(getYRot());
		Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
		Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
		return chest().add(forward.scale(0.6D)).add(right.scale(-0.7D)).add(0.0D, 0.4D, 0.0D);
	}

	// ── the move hooks ───────────────────────────────────────────────

	/** A move's blow, of his damage type; where it lands, a challenger's blood falls (the Thicket reads it). */
	boolean strike(SaintMoveContext ctx, LivingEntity target, float damage) {
		boolean hurt = SaintMoves.hurt(ctx, target, damage);
		if (hurt) bloodFell(target);
		return hurt;
	}

	void bloodFell(LivingEntity body) {
		Vec3 at = new Vec3(body.getX(), body.getY(), body.getZ());
		BloodMark newest = bloodMarks.peekFirst();
		if (newest != null && newest.at().distanceToSqr(at) < 1.0D && tickCount - newest.tick() < 10) return;
		bloodMarks.addFirst(new BloodMark(at, tickCount));
		while (bloodMarks.size() > BLOOD_MARKS) bloodMarks.removeLast();
	}

	/** Where the Thicket lands: where their blood fell lately, newest first and spaced, else round the target. */
	List<Vec3> thicketPoints(int discs, @Nullable LivingEntity target, FootprintFrame frame) {
		List<Vec3> points = new ArrayList<>();
		double floor = frame.floorY();
		for (BloodMark mark : bloodMarks) {
			if (points.size() >= discs) break;
			if (tickCount - mark.tick() > BLOOD_MARK_TICKS) continue;
			Vec3 at = new Vec3(mark.at().x, floor, mark.at().z);
			boolean spaced = true;
			for (Vec3 other : points) spaced &= other.distanceToSqr(at) >= HemorathMoves.THICKET_SPACING * HemorathMoves.THICKET_SPACING;
			if (spaced) points.add(at);
		}
		Vec3 centre = target != null ? target.position() : frame.target();
		for (int i = 0; points.size() < discs; i++) {
			double angle = i * Math.PI * 2.0D / Math.max(1, discs) + tickCount * 0.1D;
			points.add(new Vec3(centre.x + Math.cos(angle) * HemorathMoves.THICKET_RING, floor,
					centre.z + Math.sin(angle) * HemorathMoves.THICKET_RING));
		}
		return points;
	}

	void startSiphon() {
		siphonTook = 0.0F;
	}

	void siphoned(float fed) {
		siphonTook += Math.max(0.0F, fed);
	}

	/** The fight's first siphon always ends Gorged (it teaches the overload door); later ones when they took 10%. */
	boolean siphonEndsGorged(float max) {
		boolean gorged = !firstSiphonDone || siphonTook >= HemorathMoves.SIPHON_GORGED * max;
		firstSiphonDone = true;
		return gorged;
	}

	void beginTithe() {
		openings().open(HemorathOpenings.TITHE, HemorathOpenings.TITHE.ticks());
		LivingEntity target = controller().target();
		if (target instanceof Player player) SaintLines.teach(player, LESSON_TITHE);
	}

	public float targetDebt() {
		LivingEntity target = controller().target();
		return target instanceof Player player ? tithe.debt(player.getUUID()) : 0.0F;
	}

	void collectTithe(SaintMoveContext ctx, LivingEntity target) {
		if (!(target instanceof Player player)) return;
		float damage = HemorathTitheRules.damage(tithe.debt(player.getUUID()));
		tithe.clear(player.getUUID());
		if (damage <= 0.0F) return;
		if (strike(ctx, player, damage)) {
			HemorathEffects.burst(ctx.level(), player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D),
					HemorathVisualRules.DEBT_HIT, 1.5F);
		}
	}

	void hook(LivingEntity target) {
		hooked = target.getUUID();
		hookTicks = 0;
	}

	void startGorge() {
		gorgeContacts.clear();
	}

	boolean gorgeContact(LivingEntity target) {
		return gorgeContacts.add(target.getUUID());
	}

	boolean gorgeReachedNoOne() {
		return gorgeContacts.isEmpty();
	}

	/** A wet front runs down each drawn well's spoke toward the pit over the wind-up. */
	void wellsFront(int mask, int windup) {
		if (chamber == null || !(level() instanceof ServerLevel server)) return;
		for (int i = 0; i < chamber.wells().size(); i++) {
			if ((mask & (1 << i)) == 0) continue;
			double longest = Math.max(1.0D, chamber.longestSpoke(i));
			chamber.front(server, chamber.spoke(i), 3, cell -> (int) Math.round((longest - cell.fromPit()) / longest * windup));
		}
	}

	void startDraw(int mask) {
		wells.startDraw(mask, level().getGameTime());
		wellsDrawn = 0.0F;
		java.util.Arrays.fill(denied, 0.0F);
	}

	/** One tick of Wells Answer's draw: each drawn well's flow, less what a player took from it, into him. */
	void drawTick() {
		if (!wells.drawing()) return;
		for (int i = 0; i < wells.count(); i++) {
			float flow = wells.flow(i);
			if (flow <= 0.0F) continue;
			float share = HemorathWellRules.afterDenial(flow, denied[i]);
			float taken = wells.take(i, share);
			feed(taken, null, true);
			heal(HemorathWellRules.heal(taken));
			wellsDrawn += taken;
			if (chamber != null && level() instanceof ServerLevel server && wells.drawing() && tickCount % STREAM_EVERY == 0) {
				HemorathEffects.flow(server, chamber.well(i), chest(), HemorathVisualRules.WELL_STREAM,
						HemorathVisualRules.STREAM_TICKS / 2, 0.06D);
			}
		}
		java.util.Arrays.fill(denied, 0.0F);
		wells.tickDraw();
	}

	/** Tears out the newest patches (a Ferric hit, the Thicket, the overload, the ending). */
	void tearPatches(int n, boolean burst) {
		int before = patches.count();
		int torn = patches.tear(n, tickCount);
		if (torn <= 0 || !(level() instanceof ServerLevel server)) return;
		HemorathEffects.sound(this, SoundEvents.CHAIN_BREAK, 1.0F, 0.6F);
		for (int i = 0; i < torn; i++) {
			HemorathEffects.flakes(server, chest().add(0.0D, -0.4D * (before - i) / 9.0D, 0.0D), burst ? 6 : 3);
		}
	}

	void spawnHazard(int kind, BossFootprint shape, FootprintFrame frame, int life) {
		if (!(level() instanceof ServerLevel server) || arena() == null) return;
		int pulse = kind == HAZARD_TIDE ? TIDE_PULSE : THICKET_PULSE;
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, this, kind, shape, frame, life, pulse);
		arena().own(hazard, null);
	}

	/** The tide's flood: he dumps part of his meter onto the boss floor, which stands there and drains to the wells. */
	void flood(FootprintFrame frame, boolean forced, int active) {
		float max = meter().max(), value = meter().value();
		float dump = forced ? Math.max(0.0F, value - HemorathMeterRules.AFTER_EMPTIES * max)
				: value * HemorathMeterRules.TIDE_DUMP;
		emptying = false;
		meter().set(value - dump);
		tideHeld = dump;
		tideTicks = active;
		openings().open(HemorathOpenings.WRUNG, HemorathOpenings.WRUNG.ticks());
		spawnHazard(HAZARD_TIDE, new BossFootprint.Region(BossFootprint.Area.BOSS_FLOOR, 0.0D, HemorathMove.TIDE_HEIGHT),
				frame, active);
	}

	/** Rib Burst's vent: 12% of the meter out through his ribs, which leaks like any other. */
	void ventRibs() {
		float vent = Math.min(meter().value(), HemorathMeterRules.RIB_BURST_VENT * meter().max());
		meter().set(meter().value() - vent);
		leaked(vent);
		if (level() instanceof ServerLevel server) {
			HemorathEffects.burst(server, chest(), HemorathVisualRules.RIB_JETS, (float) HemorathMove.RIB_LENGTH);
			HemorathEffects.sound(this, SoundEvents.GENERIC_EXPLODE, 1.0F, 0.8F);
		}
	}

	void beginThirstDrag() {
		bossBar.setName(Component.translatable(THIRST_KEY));
		bossBar.setColor(BossEvent.BossBarColor.RED);
		HemorathEffects.sound(this, SoundInit.RITE_AMBIENT_HEARTBEAT, 1.2F, 0.6F);
	}

	void startThirst() {
		thirstTicks = 0;
	}

	private void endThirst() {
		thirstTicks = -1;
		bossBar.setName(Component.translatable(EMPTIES_KEY));
		bossBar.setColor(BossEvent.BossBarColor.PURPLE);
	}

	/** The ending's first tick: every running piece of the fight stops. */
	void endFightState() {
		wells.stopDraw();
		thirstTicks = -1;
		hooked = null;
		tideTicks = 0;
		tideHeld = 0.0F;
		tithe.clearAll();
	}

	/** The ending's pour: every gutter floods in fronts running out from where he stands. */
	void floodAll() {
		if (chamber == null || !(level() instanceof ServerLevel server)) return;
		Vec3 from = position();
		chamber.front(server, chamber.all(), 3, cell -> (int) Math.round(Math.sqrt(Vec3.atCenterOf(cell.pos())
				.distanceToSqr(from)) * HemorathEndingRules.FLOOD_TICKS_PER_BLOCK));
	}

	Vec3 tombPoint() {
		SaintArena arena = arena();
		return arena != null && arena.chamberFound() ? Vec3.atCenterOf(arena.tomb()) : position();
	}

	// ── the tick ─────────────────────────────────────────────────────

	@Override
	public void tick() {
		super.tick();
		float width = HemorathVisualRules.width(fraction());
		if (Math.abs(width - bodyWidth) >= 0.05F) {
			bodyWidth = width;
			refreshDimensions();
		}
	}

	@Override
	protected void tickSaint() {
		if (!(level() instanceof ServerLevel server)) return;
		if (ending() || hasTrialEnded() || rising()) {
			leakThisTick = 0.0F;
			syncExtras();
			return;
		}
		fightTicks++;
		if (!downfallApplied) {
			downfallApplied = true;
			if (deeds().skipped(ChapelRules.Kind.DOWNFALL)) {
				// he rises having drunk his wells (resolution D-1)
				wells.takeAll();
				meter().set(HemorathMeterRules.DOWNFALL_START * meter().max());
			}
		}
		handleOverload();
		if (ending()) return;
		distributeLeak(server);
		pollSchools();
		if (litany()) wells.refill();
		tickThirst(server);
		tickTide();
		tickHook(server);
		if (fightTicks % HemorathPresenceRules.heartbeat(meter().fraction()) == 0) {
			HemorathEffects.sound(this, SoundInit.MAUSOLEUM_HEMORATH_HEARTBEAT, 1.0F, 0.7F);
		}
		if (tickCount % HemorathArena.UPDATE_EVERY == 0) updateGutters(server);
		while (!bloodMarks.isEmpty() && tickCount - bloodMarks.peekLast().tick() > BLOOD_MARK_TICKS) bloodMarks.removeLast();
		if (!thirstUsed && thirstTicks < 0 && controller().interruptible() && controller().move() == null
				&& HemorathPhaseRules.thirstDue(phases().phase(), getHealth() / getMaxHealth(), thirstUsed)) {
			thirstUsed = true;
			controller().start(HemorathMove.LAST_THIRST, SaintCombatController.Cut.QUEUE);
		}
		syncExtras();
	}

	private void distributeLeak(ServerLevel server) {
		float leak = leakThisTick;
		leakThisTick = 0.0F;
		if (leak <= 0.0F) return;
		if (chamber != null && wells.count() > 0) {
			float share = leak * HemorathMeterRules.wellShare(phases().phase() >= 2);
			int[] nearest = chamber.nearestWells(position(), 2);
			for (int well : nearest) wells.credit(well, share / nearest.length);
		}
		int erupted = patches.leak(leak, tickCount);
		if (erupted > 0) {
			HemorathEffects.sound(this, SoundEvents.POINTED_DRIPSTONE_PLACE, 0.8F, 0.7F);
			HemorathEffects.burst(server, chest(), HemorathVisualRules.PATCH_BLOOM, 0.6F);
		}
	}

	/** The school answers on him (design section 7): read off his own states each tick. */
	private void pollSchools() {
		int pressure = SchoolStates.levels(this, SchoolState.PRESSURE);
		if (pressure > lastPressure) {
			Player presser = getLastHurtByMob() instanceof Player player ? player : null;
			feed(PRESSURE_FEED * (pressure - lastPressure), presser, false);
		}
		lastPressure = pressure;
		int lodestone = SchoolStates.levels(this, SchoolState.LODESTONE);
		if (lodestone > lastLodestone) tearPatches(lodestone - lastLodestone, false);
		lastLodestone = lodestone;
		boolean rime = SchoolStates.has(this, SchoolState.RIME);
		if (rime && !lastRime && controller().move() == HemorathMove.WELLS_ANSWER
				&& controller().stage() != BossMoveTimeline.Stage.RECOVERY) {
			wells.stopDraw();
			controller().skipToRecovery();
			openings().open(HemorathOpenings.CLOTTED, HemorathOpenings.CLOTTED.ticks());
			HemorathEffects.sound(this, SoundEvents.BONE_BLOCK_HIT, 1.0F, 0.5F);
		}
		lastRime = rime;
	}

	private void tickThirst(ServerLevel server) {
		if (thirstTicks < 0) return;
		thirstTicks++;
		int left = Math.max(1, HemorathPhaseRules.THIRST_TICKS - thirstTicks + 1);
		for (int i = 0; i < wells.count(); i++) {
			float taken = wells.take(i, wells.content(i) / left);
			feed(taken, null, true);
		}
		SaintArena arena = arena();
		if (arena != null) {
			for (Player player : arena.challengers()) {
				if (!standsInGutter(server, player)) continue;
				float paid = HemorathMoves.drain(player, THIRST_GUTTER_DRAIN, HemorathSources.THIRST);
				if (paid > 0.0F) feed(paid, player, false);
			}
		}
		if (thirstTicks % 20 == 0) HemorathEffects.sound(this, SoundInit.MAUSOLEUM_HEMORATH_DRIP, 1.0F, 0.9F);
		if (HemorathPhaseRules.thirstExit(thirstTicks, false, false) == HemorathPhaseRules.ThirstExit.TIMEOUT) {
			endThirst();
			controller().start(HemorathMove.EXSANGUINATING_TIDE, SaintCombatController.Cut.QUEUE);
		}
	}

	private boolean standsInGutter(ServerLevel level, Player player) {
		BlockPos feet = player.blockPosition();
		for (BlockPos pos : new BlockPos[]{feet, feet.below()}) {
			BlockState state = level.getBlockState(pos);
			if (state.is(BlockInit.blood_gutter.get()) && state.getValue(BloodGutterBlock.FILL) > 0) return true;
		}
		return false;
	}

	private void tickTide() {
		if (tideTicks <= 0) return;
		if (--tideTicks > 0) return;
		float each = HemorathWellRules.tideReturnEach(tideHeld);
		for (int i = 0; i < wells.count(); i++) wells.credit(i, each);
		tideHeld = 0.0F;
		HemorathEffects.sound(this, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0F, 0.6F);
	}

	private void tickHook(ServerLevel server) {
		if (hooked == null) return;
		Entity entity = server.getEntity(hooked);
		if (!(entity instanceof LivingEntity body) || !body.isAlive() || ++hookTicks > REEL_TICKS
				|| body.distanceTo(this) <= REEL_STOP) {
			hooked = null;
			return;
		}
		Vec3 to = position().subtract(body.position());
		Vec3 flat = new Vec3(to.x, 0.0D, to.z).normalize().scale(REEL_SPEED);
		body.setDeltaMovement(flat.x, body.getDeltaMovement().y, flat.z);
		body.hurtMarked = true;
		if (hookTicks % 5 == 0) HemorathEffects.sound(this, SoundEvents.CHAIN_STEP, 0.8F, 0.5F);
	}

	/** The chamber shows his circulation: the pit ring his meter, each spoke its well, everything full in the Thirst. */
	private void updateGutters(ServerLevel server) {
		if (chamber == null) return;
		boolean thirst = thirsting();
		int pit = thirst ? 3 : HemorathWellRules.pitFill(meter().fraction());
		boolean answering = controller().move() == HemorathMove.WELLS_ANSWER;
		int drawing = answering ? HemorathMove.wells(syncedMoveVariant()) : 0;
		int basin = thirstTicks >= 0 ? 3 - Math.min(3, 3 * thirstTicks / HemorathPhaseRules.THIRST_TICKS)
				: HemorathArena.REVERT;
		chamber.apply(server, cell -> switch (cell.run()) {
			case PIT, SUMP -> pit;
			case SPOKE -> {
				if (cell.well() < 0) yield thirst ? 3 : HemorathArena.LEAVE;
				if ((drawing & (1 << cell.well())) != 0) yield HemorathArena.LEAVE;
				yield thirst ? 3 : HemorathWellRules.spokeFill(wells.content(cell.well()));
			}
			case EAST -> thirst ? 3 : HemorathArena.REVERT;
			default -> HemorathArena.LEAVE;
		}, basin);
	}

	private void syncExtras() {
		entityData.set(DATA_PATCHES, patches.count());
		entityData.set(DATA_WELLS, HemorathWellRules.pack(wells.contents()));
		entityData.set(DATA_TARGET_DEBT, Math.round(targetDebt()));
		entityData.set(DATA_THIRST, thirstTicks);
		SaintArena arena = arena();
		if (arena == null || kit() == null) return;
		for (Player player : arena.challengers()) {
			meter().secondary(player, tithe.debt(player.getUUID()), HemorathTitheRules.DEBT_SHOWN);
		}
	}

	private void teachAll(String key) {
		SaintArena arena = arena();
		if (arena == null) return;
		for (Player player : arena.challengers()) SaintLines.teach(player, key);
	}

	private static float sum(float[] values) {
		float total = 0.0F;
		for (float value : values) total += value;
		return total;
	}

	// ── what the client reads ────────────────────────────────────────

	public float syncedFraction() {
		float max = syncedMeterMax();
		return max <= 0.0F ? 0.0F : Math.max(0.0F, Math.min(1.0F, syncedMeter() / max));
	}

	public int syncedPatches() {
		return entityData.get(DATA_PATCHES);
	}

	public float syncedWell(int well) {
		return HemorathWellRules.unpack(entityData.get(DATA_WELLS), well);
	}

	public int syncedTargetDebt() {
		return entityData.get(DATA_TARGET_DEBT);
	}

	/** Ticks into the Last Thirst's rooted draw; -1 outside it. */
	public int syncedThirst() {
		return entityData.get(DATA_THIRST);
	}

	public List<BlockPos> syncedWellPositions() {
		long[] positions = entityData.get(DATA_CHAMBER).getLongArray("wells");
		List<BlockPos> out = new ArrayList<>(positions.length);
		for (long pos : positions) out.add(BlockPos.of(pos));
		return out;
	}

	@Nullable
	public Vec3 syncedPit() {
		CompoundTag tag = entityData.get(DATA_CHAMBER);
		if (!tag.contains("pit_x")) return null;
		return new Vec3(tag.getDouble("pit_x"), tag.getDouble("pit_y"), tag.getDouble("pit_z"));
	}

	@Nullable
	public BlockPos syncedTomb() {
		CompoundTag tag = entityData.get(DATA_CHAMBER);
		return tag.contains("tomb") ? BlockPos.of(tag.getLong("tomb")) : null;
	}

	// ── body ─────────────────────────────────────────────────────────

	/** His hitbox swells with the meter: 0.9 wide hollow to 1.4 brimming, 3 tall (resolution B-12). */
	@Override
	protected EntityDimensions getDefaultDimensions(Pose pose) {
		float width = bodyWidth > 0.0F ? bodyWidth : HemorathVisualRules.WIDTH_SUNKEN;
		return EntityDimensions.scalable(width, HemorathVisualRules.HEIGHT);
	}

	/** The thrown halo flies 10 blocks out: keep him drawn while it does. */
	@Override
	public AABB getBoundingBoxForCulling() {
		AABB box = super.getBoundingBoxForCulling();
		return syncedMoveId() == HemorathMove.SPIKED_HALO.id() ? box.inflate(HemorathVisualRules.HALO_CULL) : box;
	}

	// ── save ─────────────────────────────────────────────────────────

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		CompoundTag own = new CompoundTag();
		tithe.save(own);
		wells.save(own);
		patches.save(own);
		own.putBoolean("emptied", emptiedByOverload);
		own.putBoolean("emptying", emptying);
		own.putBoolean("thirst_used", thirstUsed);
		own.putInt("thirst", thirstTicks);
		own.putFloat("tide_held", tideHeld);
		own.putInt("tide_ticks", tideTicks);
		own.putInt("fight_ticks", fightTicks);
		own.putBoolean("downfall", downfallApplied);
		own.putBoolean("first_siphon", firstSiphonDone);
		tag.put("Hemorath", own);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		CompoundTag own = tag.getCompound("Hemorath");
		tithe.load(own);
		wells.load(own);
		patches.load(own);
		emptiedByOverload = own.getBoolean("emptied");
		emptying = own.getBoolean("emptying");
		thirstUsed = own.getBoolean("thirst_used");
		thirstTicks = own.contains("thirst") ? own.getInt("thirst") : -1;
		tideHeld = own.getFloat("tide_held");
		tideTicks = own.getInt("tide_ticks");
		fightTicks = own.getInt("fight_ticks");
		downfallApplied = own.getBoolean("downfall");
		firstSiphonDone = own.getBoolean("first_siphon");
		// a reload drops the move under way, so an emptying that lost its tide is forced again
		if (emptying) controller().start(HemorathMove.EXSANGUINATING_TIDE, SaintCombatController.Cut.QUEUE);
	}

	// ── the trial's end ──────────────────────────────────────────────

	/** His residuum goes straight into the victor's hands. */
	@Override
	protected boolean rewardsGoToVictor() {
		return true;
	}

	@Override
	protected void announceTrialEnd(@Nullable Player victor) {
		if (victor != null) {
			victor.displayClientMessage(Component.translatable(VICTOR_LINE).withStyle(ChatFormatting.GOLD,
					ChatFormatting.ITALIC), false);
		}
		SaintArena arena = arena();
		if (arena != null) {
			for (Player player : arena.challengers()) {
				player.displayClientMessage(Component.translatable(MARROW_LABOURS).withStyle(ChatFormatting.GRAY,
						ChatFormatting.ITALIC), false);
			}
		}
		HemorathEffects.sound(this, SoundInit.MAUSOLEUM_HEMORATH_HEARTBEAT, 1.0F, 0.5F);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		player.displayClientMessage(Component.translatable("message.hemomancy.hemorath.hemorath_stirs_blood_will")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	@Nullable
	protected SoundEvent getDeathSound() {
		return null;
	}

	@Override
	public float getVoicePitch() {
		return 0.5F + (getRandom().nextFloat() - 0.5F) * 0.1F;
	}

	/**
	 * Called from {@code BloodManipulation.performAction} and {@code EarthenVeinTravelManager} with the blood a player
	 * spent: a cast or a vein feed within 24 blocks of him feeds his meter and adds to their Tithe debt.
	 */
	public static void onPlayerBloodSpend(Player player, double amount) {
		if (player.level().isClientSide || amount <= 0.0D) return;
		for (HemorathEntity vessel : player.level().getEntitiesOfClass(HemorathEntity.class,
				player.getBoundingBox().inflate(HemorathTitheRules.RANGE))) {
			if (!vessel.isAlive() || vessel.kit() == null || vessel.arena() == null
					|| !vessel.arena().challengers().contains(player)) continue;
			if (vessel.distanceTo(player) > HemorathTitheRules.RANGE) continue;
			vessel.feed((float) amount, player, false);
			vessel.tithe.accrue(player.getUUID(), (float) amount, vessel.litany());
			vessel.bloodFell(player);
			return;
		}
	}
}
