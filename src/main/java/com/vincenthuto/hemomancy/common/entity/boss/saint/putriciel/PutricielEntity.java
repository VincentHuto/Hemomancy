package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.RotPyreBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.SporeShelfBlock;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossReplayClock;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHealing;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Act;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielAbsolutionRules.Glyph;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielChamberLightingRules.Light;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Catch;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Patch;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Placement;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielSludgeRules.Schedule;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BogRevenantEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Putriciel, The Befouled Saint (they; he in life), on the Saint combat kit: <i>Putriciel purified</i>. Tendencies
 * MORTEM + FLAMMEUS. They cannot be beaten down, only absolved, and only while the purifier is awake in them
 * ({@code docs/superpowers/specs/2026-11-05-saint-putriciel-overhaul-design.md}).
 * <p>
 * The rot phases never burn: censers trail rot, the sacs swell, shelves are flung and the presence colonises. Every
 * blow, and above all every Flammeus cast (fire fed to the rot is drunk), stokes the Kindling; when it fills, the
 * interval starts at the next recovery boundary: the Ignition, the fire moves, two Offerings at 66% and 33% (the palm
 * takes one hit of at least 4, or 100 blood of held Projection), the burn-down and the Embering. Five absolutions run
 * the 100-tick ending and {@code endTrial} once. Health floors at 30% and regrows at each interval's end; the bar is
 * the absolutions.
 */
public class PutricielEntity extends SaintTrialEntity {
	/** The one blood Putriciel takes: projected into the open palm (resolution D-5). */
	public static final BloodAccounting.Source OFFERING_SOURCE = BloodAccounting.Source.of("putriciel_offering",
			Category.RITE, false, false);

	private static final EntityDataAccessor<Integer> DATA_ABSOLUTIONS = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STAGE = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STAGE_SERIAL = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STAGE_AGE = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_GLYPH = define(EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_INTERVALS = define(EntityDataSerializers.INT);

	/** Health never falls under this fraction; the win is the absolutions. */
	public static final float HEALTH_FLOOR = 0.30F;
	/** Knockback resistance added while the fire burns (to 1.0 from 0.9). */
	public static final double FIRE_FOOTING = 0.1D;
	/** The narration's pale cream. */
	public static final int CREAM = 0xE8D8A0;
	/** How long a cleanse marks a player as burning for the client's geometry. */
	public static final int BURNING_MARK = 20;
	/** The Communion breaks at this much damage per challenger (or any Flammeus hit). */
	public static final float COMMUNION_BREAK_DAMAGE = 14.0F;
	public static final int COMMUNION_STAGGER = 40;
	/**
	 * The palm sits about here in front of the kneeling body: where the Offering pose holds the hand up at the brow
	 * (measured off the posed model in the review, 2026-11-06), so the absolution's burst and the pour's aim meet the
	 * palm the player sees.
	 */
	public static final double PALM_FORWARD = 0.7D, PALM_UP = 2.45D;
	public static final double SACS_CONE = 40.0D;
	public static final int SAC_FEED_EVERY = 10;
	public static final float SAC_FEED_COLONISATION = 1.0F;
	public static final float FAITHFUL_HIT_COLONISATION = 2.0F;

	public static final ResourceLocation ROOTED_KEY = Hemomancy.rloc("putriciel_rooted");
	public static final ResourceLocation OVERGROWN_KEY = Hemomancy.rloc("putriciel_overgrown");
	public static final ResourceLocation FOOTING_KEY = Hemomancy.rloc("putriciel_fire_footing");
	/** Wading in rot sludge: movement speed x0.65 (revision 2026-11-06, R2.3). */
	public static final ResourceLocation SLUDGE_KEY = Hemomancy.rloc("putriciel_sludge");
	/** How high over the floor a player still wades (a hop does not leave the pool). */
	public static final double WADE_HEIGHT = 1.5D;
	/** The body fire's crackle, every this many ticks while it burns. */
	public static final int BODY_FIRE_SOUND_EVERY = 20;

	private static final String PYRES = "putriciel_pyres", SHELVES = "putriciel_shelves", BOG_BODIES = "putriciel_bog_bodies";
	private static final String LINE = "message.hemomancy.putriciel.";

	private PutricielStage stage = PutricielStage.ROT;
	private int stageAge;
	private int stageSerial;
	private final BossReplayClock stageClock = new BossReplayClock();
	private boolean roomTaken;
	private boolean ignited;
	private boolean ignitionRunning;
	private boolean purgeRunning;
	private boolean emberingRunning;
	private boolean offeringDue;
	private int offeringsOpened;
	private boolean purgedThisInterval;
	private boolean absolvedThisOffering;
	private Glyph glyphLast = Glyph.NONE, glyphBefore = Glyph.NONE;
	private int bloomUsedPhase = -1;
	private boolean tremorSounded;
	private float communionDamage;
	private boolean communionBroken;
	private float absorbGiven;
	private final Map<UUID, Double> poured = new HashMap<>();
	private final Map<UUID, Integer> burningUntil = new HashMap<>();
	private final Map<UUID, Integer> tiers = new HashMap<>();
	private final Set<UUID> modded = new HashSet<>();
	private final LongLinkedOpenHashSet ashCells = new LongLinkedOpenHashSet();
	private final Map<UUID, BlockPos> faithful = new HashMap<>();
	private final Map<UUID, Integer> faithfulStamps = new HashMap<>();
	private int faithfulRaised;
	// the rot sludge (revision 2026-11-06, R2): the fight's own clock, who wades, and the fire's schedule
	private int sludgeTicks;
	private final Map<UUID, Integer> wadingAt = new HashMap<>();
	private final Map<UUID, Integer> sludgeFedAt = new HashMap<>();
	/** Ticks since the Ignition's commit while its schedule runs, else -1; saved, so a load can recompute it. */
	private int catchClock = -1;
	@Nullable
	private Schedule catchSchedule;
	private final List<UUID> scheduled = new ArrayList<>();
	private final Set<UUID> ashed = new HashSet<>();

	public PutricielEntity(EntityType<? extends PutricielEntity> type, Level level) {
		super(type, level, 120, new BossBarBinding("entity.hemomancy.putriciel", BossPresentationIds.FRAME_PUTRICIEL,
				BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10));
	}

	private static <T> EntityDataAccessor<T> define(net.minecraft.network.syncher.EntityDataSerializer<T> serializer) {
		return SynchedEntityData.defineId(PutricielEntity.class, serializer);
	}

	@Override
	public EnumSaintType saintType() {
		return EnumSaintType.PUTRICIEL;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 400.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.20D)
				.add(Attributes.ATTACK_DAMAGE, 8.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
				.add(Attributes.ARMOR, 12.0D);
	}

	@Override
	protected SaintKit createKit() {
		return new SaintKit(List.of(PutricielMove.values()), new PutricielLocomotion(),
				new SaintMeter(SaintMeter.Owner.SAINT, new PutricielKindling()), new PutricielPresence(),
				SaintPhaseTable.bySaint(saint -> saint instanceof PutricielEntity putriciel ? putriciel.wantedPhase() : 1,
						PutricielPhase.rows()),
				SaintDamageTypes.PUTRICIEL, SaintKit.CHALLENGER_SCALING, PutricielEndingRules.SPEC);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ABSOLUTIONS, 0);
		builder.define(DATA_STAGE, PutricielStage.ROT.ordinal());
		builder.define(DATA_STAGE_SERIAL, 0);
		builder.define(DATA_STAGE_AGE, 0);
		builder.define(DATA_GLYPH, Glyph.NONE.ordinal());
		builder.define(DATA_INTERVALS, 0);
	}

	// ── what both sides read ─────────────────────────────────────────

	public int absolutions() {
		return entityData.get(DATA_ABSOLUTIONS);
	}

	public PutricielStage stage() {
		return PutricielStage.byId(entityData.get(DATA_STAGE));
	}

	/** Ticks since the stage began, smooth across frames on the client. */
	public float stageTime(float partial) {
		return stageClock.sample(entityData.get(DATA_STAGE_SERIAL), entityData.get(DATA_STAGE_AGE), tickCount, partial, 1);
	}

	public Glyph glyph() {
		return Glyph.byId(entityData.get(DATA_GLYPH));
	}

	public int intervalsEnded() {
		return entityData.get(DATA_INTERVALS);
	}

	/** The phase the bar shows: the kit table's on the server, the synced one on the client. */
	public PutricielPhase currentPhase() {
		return PutricielPhase.byNumber(level().isClientSide ? syncedPhase() : phases().phase());
	}

	/** The phase the fire cycle asks the table for; the table applies it at the recovery boundary. */
	public int wantedPhase() {
		return stage.interval() ? PutricielPhase.INTERVAL.number() : PutricielPhase.rotAfter(intervalsEnded()).number();
	}

	/** A challenger's colonisation (the meter state's secondary), 0 for anyone else. */
	public float colonisation(Player player) {
		SaintMeter.PlayerState state = meter().existing(player.getUUID());
		return state == null ? 0.0F : state.secondary();
	}

	// ── the kit's hooks ──────────────────────────────────────────────

	@Override
	protected float barProgress() {
		return PutricielAbsolutionRules.progress(absolutions());
	}

	@Override
	protected float healthFloor(SaintHit hit) {
		return getMaxHealth() * HEALTH_FLOOR;
	}

	@Override
	public boolean fireImmune() {
		return true;
	}

	/** Fire fed to the rot is drunk: it deals nothing and stokes the Kindling. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (rising()) return false;
		if (!level().isClientSide && source.is(DamageTypeTags.IS_FIRE)) {
			if (!ending()) stoke(amount, true, source.getEntity() instanceof Player player ? player : null);
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) {
		if (ending()) return;
		Player attacker = hit.attacker() != null && isChallenger(hit.attacker()) ? hit.attacker() : null;
		boolean flammeus = hit.fire() || hit.school() == EnumBloodTendency.FLAMMEUS;
		stoke(hit.amount(), flammeus, attacker);
		if (attacker != null && hit.school() == EnumBloodTendency.FLAMMEUS) {
			colonise(attacker, -PutricielColonisationRules.FLAMMEUS_CAUTERY);
		}
		if (isOfferingOpen() && admit == SaintOpening.Admit.COUNTED && attacker != null
				&& PutricielAbsolutionRules.hitQualifies(hit.amount())) {
			if (glyph().accepts(Act.STRIKE)) absolve(attacker, Act.STRIKE);
			else attacker.displayClientMessage(Component.translatable(LINE + "palm_refuses_blow"), true);
		}
		if (controller().move() == PutricielMove.MOULD_COMMUNION
				&& controller().stage() == com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline.Stage.ACTIVE
				&& !communionBroken) {
			communionDamage += hit.amount();
			int challengers = arena() == null ? 1 : arena().challengerCount();
			boolean breaks = flammeus || communionDamage >= COMMUNION_BREAK_DAMAGE * challengers;
			if (breaks && PutricielDeeds.communionBreaks(deeds().mask(), flammeus)) breakCommunion(attacker);
		}
	}

	@Override
	protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) {
		boolean atBody = request.entity() != null && request.entity().getEntity() == this;
		if (request.kind() == SaintChannelRouter.Kind.PROJECT) {
			boolean atPalm = atBody || SaintChannelRouter.aimsAt(request, palmPoint(), request.reach(),
					PutricielAbsolutionRules.PALM_CONE, true);
			if (!atPalm) return SaintChannelRouter.Result.PASS;
			if (!isOfferingOpen()) return SaintChannelRouter.Result.refused(Component.translatable(LINE + "palm_only"));
			if (!glyph().accepts(Act.POUR)) return SaintChannelRouter.Result.refused(Component.translatable(LINE + "palm_refuses_blood"));
			UUID id = request.player().getUUID();
			double already = poured.getOrDefault(id, 0.0D);
			double step = PutricielAbsolutionRules.pourStep(request.ratePerTick(), already);
			if (step <= 0.0D) return SaintChannelRouter.Result.taken(0.0D);
			double paid = SaintChannelRouter.pay(request, step, OFFERING_SOURCE).paid();
			poured.put(id, already + paid);
			if (PutricielAbsolutionRules.pourComplete(already + paid)) absolve(request.player(), Act.POUR);
			return SaintChannelRouter.Result.taken(paid);
		}
		boolean atSacs = atBody || SaintChannelRouter.aimsAt(request, getBoundingBox().getCenter(), request.reach(), SACS_CONE, true);
		if (!atSacs) return SaintChannelRouter.Result.PASS;
		if (!openings().isOpen(PutricielOpenings.SPENT_SACS)) {
			return SaintChannelRouter.Result.refused(Component.translatable(LINE + "rot_holds"));
		}
		float add = PutricielKindlingRules.absorb(absorbGiven);
		if (add > 0.0F) {
			meter().add(add, request.player());
			absorbGiven += add;
		}
		if (request.useTicks() % SAC_FEED_EVERY == 0) colonise(request.player(), SAC_FEED_COLONISATION);
		// the absorber takes the rot into their own blood, not blood out of the Saint
		return SaintChannelRouter.Result.taken(0.0D);
	}

	@Override
	protected boolean presencePaused() {
		return stage == PutricielStage.EMBERING || stage == PutricielStage.IGNITING && !ignited;
	}

	@Override
	protected void onArenaResolved(SaintArena arena) {
		pyres();
		arena.find(SHELVES, state -> state.is(BlockInit.spore_shelf.get()) && state.hasProperty(SporeShelfBlock.BLOOM));
		arena.find(BOG_BODIES, state -> state.is(BlockInit.bog_body.get()));
	}

	/** A load restored the chamber as built: put back what the stage needs and re-queue the beat the save dropped. */
	@Override
	protected void onArenaReloaded() {
		ashCells.clear();
		faithful.clear();
		faithfulStamps.clear();
		// the load restored the chamber clean: every pool went with it; the schedule is recomputed from what stands
		catchSchedule = null;
		scheduled.clear();
		ashed.clear();
		wadingAt.clear();
		sludgeFedAt.clear();
		if (roomTaken) applyLighting(PutricielChamberLightingRules.Phase.of(stage), false, 0);
		switch (stage) {
			case IGNITING -> {
				ignitionRunning = false;
				controller().start(PutricielMove.IGNITION, SaintCombatController.Cut.QUEUE);
			}
			case BURNING -> {
				purgeRunning = false;
				if (isOfferingOpen()) {
					if (!absolvedThisOffering) offeringsOpened = Math.max(0, offeringsOpened - 1);
					absolvedThisOffering = true;
					openings().close();
				}
				offeringDue = false;
			}
			case EMBERING -> endInterval(false);
			case ROT -> {
			}
		}
	}

	@Override
	protected void onPhaseChanged(int from, int to) {
		if (to == PutricielPhase.INTERVAL.number()) {
			controller().start(PutricielMove.IGNITION, SaintCombatController.Cut.QUEUE);
		}
	}

	@Override
	protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
		if (to != null && to.id() == PutricielOpenings.OFFERING_ID) {
			sound(SoundInit.RITE_CONSECRATE.get(), 1.0F, 0.8F);
			teachAll(LINE + "palm_opens");
			if (glyph() != Glyph.NONE) narrate(Component.translatable(LINE + "glyph." + glyph().name().toLowerCase(java.util.Locale.ROOT)));
		}
		if (from != null && from.id() == PutricielOpenings.OFFERING_ID && to == null && !absolvedThisOffering) {
			sound(SoundInit.RITE_COLLAPSE.get(), 0.8F, 0.7F);
			narrate(Component.translatable(LINE + "offering_lapses"));
		}
		if (to != null && to.id() == PutricielOpenings.SPENT_SACS_ID) absorbGiven = 0.0F;
		if (to == null && from != null && from.id() == PutricielOpenings.OFFERING_ID) {
			entityData.set(DATA_GLYPH, Glyph.NONE.ordinal());
			poured.clear();
		}
	}

	@Override
	public void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside) {
		PutricielHazard kind = PutricielHazard.byId(hazard.kind());
		if (kind == null || ending() || kind.harmless(hazard.tickCount)) return;
		if (kind == PutricielHazard.ROT_SLUDGE) {
			feedSludge(inside);
			return;
		}
		if (kind == PutricielHazard.PURGING_FIRE || kind == PutricielHazard.FIRE_WALL) {
			fireReachesSludge(hazard.shape(), hazard.frame(), hazard.tickCount);
		}
		for (LivingEntity entity : inside) {
			if (!(entity instanceof Player player)) continue;
			SaintMoves.hurt(this, player, kind.damage(), kind.fire() ? SaintDamageTypes.PUTRICIEL_CLEANSING : SaintDamageTypes.PUTRICIEL);
			colonise(player, kind.colonisation());
			if (kind.igniteSeconds() > 0) CrimsonFireHelper.igniteCrimson(player, kind.igniteSeconds());
		}
	}

	@Override
	public boolean hazardTouches(LivingEntity entity) {
		return entity instanceof Player player && isChallenger(player)
				&& com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(this, player);
	}

	// ── the server tick ──────────────────────────────────────────────

	@Override
	protected void tickSaint() {
		if (!(level() instanceof ServerLevel) || ending() || hasTrialEnded() || rising()) return;
		SaintArena arena = arena();
		if (arena == null) return;
		if (!roomTaken) {
			roomTaken = true;
			takeTheRoom();
		}
		stageAge++;
		entityData.set(DATA_STAGE_AGE, stageAge);
		tickCycle();
		tickColonisation(arena);
		tickSludge(arena);
		tickFaithful(arena);
		if (stage.fire() && (stage != PutricielStage.IGNITING || ignited) && stageAge % BODY_FIRE_SOUND_EVERY == 0) {
			sound(SoundEvents.FIRE_AMBIENT, 1.0F, 0.7F);
		}
		if (stage == PutricielStage.ROT && !tremorSounded && meter().fraction() >= PutricielKindlingRules.TREMOR) {
			tremorSounded = true;
			sound(SoundInit.MAUSOLEUM_PUTRICIEL_CREAK.get(), 1.0F, 0.7F);
			teachAll(LINE + "heat_builds");
		}
		AttributeModifiers.sync(getAttribute(Attributes.KNOCKBACK_RESISTANCE), FOOTING_KEY, FIRE_FOOTING,
				AttributeModifier.Operation.ADD_VALUE, stage.fire());
	}

	/** The fire cycle: the interval when the Kindling fills, the Offerings as it burns, the Embering when it dies. */
	private void tickCycle() {
		PutricielMove running = controller().move() instanceof PutricielMove move ? move : null;
		switch (stage) {
			case ROT -> {
				if (PutricielKindlingRules.full(meter().value(), meter().max()) && !openings().isOpen()) beginInterval();
			}
			case IGNITING -> {
				if (running == PutricielMove.IGNITION) ignitionRunning = true;
				else if (ignitionRunning) {
					ignitionRunning = false;
					setStage(PutricielStage.BURNING);
				}
			}
			case BURNING -> {
				if (running == PutricielMove.FULL_PURGE) purgeRunning = true;
				else if (purgeRunning) {
					purgeRunning = false;
					controller().start(PutricielMove.OFFERING, SaintCombatController.Cut.QUEUE);
				}
				boolean busy = offeringDue || running == PutricielMove.OFFERING || running == PutricielMove.FULL_PURGE
						|| isOfferingOpen();
				if (!busy && offeringsOpened < PutricielKindlingRules.armed(meter().fraction())) {
					offeringDue = true;
					boolean purge = PutricielAbsolutionRules.fullPurgeDue(absolutions(), purgedThisInterval);
					controller().start(purge ? PutricielMove.FULL_PURGE : PutricielMove.OFFERING, SaintCombatController.Cut.QUEUE);
				} else if (!busy && meter().value() <= 1.0E-3F) {
					fireDies();
				}
			}
			case EMBERING -> {
				if (running == PutricielMove.EMBERING) emberingRunning = true;
				else if (emberingRunning) {
					emberingRunning = false;
					endInterval(false);
				}
			}
		}
	}

	private void beginInterval() {
		setStage(PutricielStage.IGNITING);
		ignited = false;
		ignitionRunning = false;
		offeringsOpened = 0;
		offeringDue = false;
		purgedThisInterval = false;
		purgeRunning = false;
	}

	private void fireDies() {
		if (PutricielDeeds.repose(deeds().mask())) {
			endInterval(true);
			return;
		}
		setStage(PutricielStage.EMBERING);
		emberingRunning = false;
		controller().start(PutricielMove.EMBERING, SaintCombatController.Cut.QUEUE);
	}

	/** The rot comes back stronger: the next rot phase, health regrown, the pyres out (Repose: with its head start). */
	private void endInterval(boolean repose) {
		entityData.set(DATA_INTERVALS, intervalsEnded() + 1);
		setStage(PutricielStage.ROT);
		offeringsOpened = 0;
		offeringDue = false;
		purgedThisInterval = false;
		ignited = false;
		tremorSounded = false;
		bloomUsedPhase = -1;
		endSchedule();
		entityData.set(DATA_GLYPH, Glyph.NONE.ordinal());
		setHealth(getMaxHealth());
		if (repose) {
			applyLighting(PutricielChamberLightingRules.Phase.ROT, true, PutricielChamberLightingRules.PUT_OUT_GAP);
			int challengers = arena() == null ? 1 : arena().challengerCount();
			meter().set(PutricielKindlingRules.reposeHeadStart(challengers));
		}
	}

	private void setStage(PutricielStage next) {
		stage = next;
		stageAge = 0;
		stageSerial++;
		entityData.set(DATA_STAGE, next.ordinal());
		entityData.set(DATA_STAGE_SERIAL, stageSerial);
		entityData.set(DATA_STAGE_AGE, 0);
	}

	/** The meter's drift a tick ({@link PutricielKindling}). */
	float kindlingDrift() {
		int challengers = arena() == null ? 1 : arena().challengerCount();
		PutricielMove running = controller().move() instanceof PutricielMove move ? move : null;
		boolean paused = isOfferingOpen() || running == PutricielMove.OFFERING || running == PutricielMove.FULL_PURGE;
		boolean communion = running == PutricielMove.MOULD_COMMUNION
				&& controller().stage() == com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline.Stage.ACTIVE
				&& !communionBroken;
		return PutricielKindlingRules.drift(stage, currentPhase(), challengers, paused, communion);
	}

	private void stoke(float rawDamage, boolean flammeus, @Nullable Player contributor) {
		SaintOpening.Spec open = openings().current();
		float multiplier = PutricielOpenings.kindlingMultiplier(open == null ? 0 : open.id());
		float points = PutricielKindlingRules.fromDamage(rawDamage, flammeus, multiplier, stage);
		if (points > 0.0F) meter().add(points, contributor);
	}

	// ── the presence and colonisation ────────────────────────────────

	double presenceRadius() {
		if (stage == PutricielStage.ROT) return currentPhase().rot() ? currentPhase().presenceRadius() : 0.0D;
		if (stage == PutricielStage.BURNING || stage == PutricielStage.IGNITING && ignited) {
			return PutricielPhase.INTERVAL.presenceRadius();
		}
		return 0.0D;
	}

	void presencePulse(Player player) {
		if (stage.fire()) {
			colonise(player, -PutricielColonisationRules.HEAT_CLEANSE);
			if ((tickCount / PutricielColonisationRules.HEAT_EVERY) % 2 == 0) {
				SaintMoves.hurt(this, player, PutricielColonisationRules.HEAT_DAMAGE, SaintDamageTypes.PUTRICIEL_CLEANSING);
			}
		} else if (stage == PutricielStage.ROT) {
			colonise(player, PutricielColonisationRules.gain(currentPhase()));
		}
	}

	/** Changes a challenger's colonisation by {@code delta}; a cleanse marks them burning for a moment. */
	void colonise(Player player, float delta) {
		if (ending() || delta == 0.0F) return;
		float before = colonisation(player);
		float after = PutricielColonisationRules.bounded(before + delta);
		meter().secondary(player, after, PutricielColonisationRules.MAX);
		if (after < before) {
			burningUntil.put(player.getUUID(), tickCount + BURNING_MARK);
			SaintLines.teach(player, LINE + "fire_cleanses");
		}
	}

	private void tickColonisation(SaintArena arena) {
		List<Player> challengers = arena.challengers();
		Set<UUID> present = new HashSet<>();
		for (Player player : challengers) {
			UUID id = player.getUUID();
			present.add(id);
			float value = colonisation(player);
			if (stage == PutricielStage.ROT && tickCount % PutricielColonisationRules.DECAY_EVERY == 0
					&& horizontal(player) > presenceRadius()) {
				value = PutricielColonisationRules.bounded(value - PutricielColonisationRules.DECAY);
			}
			if (PutricielColonisationRules.fruiting(value) && tickCount % PutricielColonisationRules.FRUIT_EVERY == 0) {
				SaintMoves.hurt(this, player, PutricielColonisationRules.FRUIT_SELF_DAMAGE, SaintDamageTypes.PUTRICIEL);
				for (Player other : challengers) {
					if (other != player && other.distanceTo(player) <= PutricielColonisationRules.FRUIT_RADIUS) {
						colonise(other, PutricielColonisationRules.FRUIT_SPREAD);
					}
				}
			}
			SaintMeter.PlayerState state = meter().player(player);
			if (PutricielColonisationRules.overgrows(value)) {
				SaintRoot.root(player, PutricielColonisationRules.OVERGROWN_ROOT, OVERGROWN_KEY, true);
				SaintMoves.hurt(this, player, PutricielColonisationRules.OVERGROWN_DAMAGE, SaintDamageTypes.PUTRICIEL);
				value = PutricielColonisationRules.OVERGROWN_FALL_TO;
				meter().stateTicks(player, PutricielColonisationRules.OVERGROWN_ROOT);
				soundAt(player.position(), SoundEvents.ROOTED_DIRT_PLACE, 1.0F, 0.5F);
				SaintLines.teach(player, LINE + "overgrown");
			}
			boolean rooted = PutricielColonisationRules.rooted(value);
			AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), ROOTED_KEY,
					PutricielColonisationRules.ROOTED_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, rooted);
			if (rooted) modded.add(id);
			if (PutricielColonisationRules.shelved(value) && tickCount % 20 == 0) {
				SaintHealing.set(player, PutricielColonisationRules.SHELVED_HEAL, PutricielColonisationRules.SHELVED_HOLD);
			}
			int tier = PutricielColonisationRules.tier(value);
			int was = tiers.getOrDefault(id, 0);
			if (tier > was) player.playNotifySound(SoundEvents.FUNGUS_PLACE, SoundSource.HOSTILE, 0.6F, 0.9F + 0.1F * tier);
			tiers.put(id, tier);
			boolean burning = burningUntil.getOrDefault(id, 0) > tickCount;
			meter().secondary(player, value, PutricielColonisationRules.MAX);
			meter().flags(player, PutricielColonisationRules.flags(value, state.stateTicks() > 0, burning));
		}
		releaseAbsent(present);
	}

	/** Everything colonisation applies leaves a player who left the chamber. */
	private void releaseAbsent(Set<UUID> present) {
		Iterator<UUID> it = modded.iterator();
		while (it.hasNext()) {
			UUID id = it.next();
			if (present.contains(id)) continue;
			Player player = level().getPlayerByUUID(id);
			if (player != null) releaseColonisation(player);
			it.remove();
		}
		tiers.keySet().retainAll(present);
		burningUntil.keySet().retainAll(present);
		wadingAt.keySet().retainAll(present);
		sludgeFedAt.keySet().retainAll(present);
	}

	private void releaseColonisation(Player player) {
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), ROOTED_KEY, 0.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
		AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), SLUDGE_KEY, 0.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
		wadingAt.remove(player.getUUID());
		SaintRoot.release(player, OVERGROWN_KEY);
		SaintHealing.end(player);
	}

	/** The trial ends, resets or unloads: every challenger's colonisation and what it applied goes. */
	private void clearColonisation() {
		for (UUID id : new ArrayList<>(modded)) {
			Player player = level().getPlayerByUUID(id);
			if (player != null) releaseColonisation(player);
		}
		modded.clear();
		SaintArena arena = arena();
		if (arena != null) {
			for (Player player : arena.challengers()) {
				releaseColonisation(player);
				meter().secondary(player, 0.0F, PutricielColonisationRules.MAX);
				meter().flags(player, 0);
			}
		}
	}

	// ── absolution ───────────────────────────────────────────────────

	boolean isOfferingOpen() {
		SaintOpening.Spec open = openings().current();
		return open != null && open.id() == PutricielOpenings.OFFERING_ID;
	}

	/** The Offering's length: 60 after the Full Purge and under the Litany deed, else 50. */
	int offeringTicks() {
		boolean afterPurge = purgedThisInterval && absolutions() == PutricielAbsolutionRules.NEEDED - 1;
		return PutricielAbsolutionRules.offeringTicks(PutricielDeeds.litany(deeds().mask()), afterPurge);
	}

	private void absolve(@Nullable Player player, Act act) {
		if (absolvedThisOffering || ending()) return;
		absolvedThisOffering = true;
		int count = absolutions() + 1;
		entityData.set(DATA_ABSOLUTIONS, count);
		poured.clear();
		openings().close();
		sound(SoundInit.RITE_SEAL.get(), 1.3F, 1.0F);
		burst(palmPoint(), PutricielVisualRules.ABSOLVE, 1.6F);
		narrate(Component.translatable(LINE + "absolution_accepted", count, PutricielAbsolutionRules.NEEDED));
		if (player != null && act == Act.POUR) colonise(player, -PutricielAbsolutionRules.PROJECTOR_CLEANSE);
		if (PutricielAbsolutionRules.done(count)) {
			beginEnding(PutricielEndingRules.SPEC, player, () -> false);
			return;
		}
		controller().requestStagger(PutricielAbsolutionRules.RECOIL_TICKS, null);
	}

	private void breakCommunion(@Nullable Player attacker) {
		communionBroken = true;
		controller().skipToRecovery();
		controller().requestStagger(COMMUNION_STAGGER, null);
		openings().open(PutricielOpenings.BROKEN_COMMUNION, COMMUNION_STAGGER);
		meter().add(PutricielKindlingRules.COMMUNION_BREAK, attacker);
		sound(SoundInit.RITE_CANCEL.get(), 1.0F, 0.8F);
		burst(position().add(0.0D, 1.0D, 0.0D), PutricielVisualRules.COMMUNION_BREAK, 2.0F);
		narrate(Component.translatable(LINE + "communion_broken"));
	}

	/** Where the open palm sits, in front of the kneeling body. */
	public Vec3 palmPoint() {
		float yaw = getYRot() * Mth.DEG_TO_RAD;
		return position().add(-Mth.sin(yaw) * PALM_FORWARD, PALM_UP, Mth.cos(yaw) * PALM_FORWARD);
	}

	// ── beats the moves call ─────────────────────────────────────────

	void ignitionBegins() {
		sound(SoundInit.MAUSOLEUM_PUTRICIEL_CRACKLE.get(), 1.4F, 0.8F);
		if (intervalsEnded() == 0) narrate(Component.translatable(LINE + "the_man_shows"));
	}

	void ignite() {
		ignited = true;
		sound(SoundEvents.FIRECHARGE_USE, 1.2F, 0.6F);
		burst(position().add(0.0D, 0.2D, 0.0D), PutricielVisualRules.IGNITE, 6.0F);
		// the pyres relight in this order, RELIGHT_GAP apart: each sends a fire runner to the pooled rot
		List<Vec3> relit = new ArrayList<>();
		for (BlockPos pos : pyres()) {
			BlockState state = level().getBlockState(pos);
			if (state.is(BlockInit.rot_pyre.get()) && state.hasProperty(RotPyreBlock.LIT) && !state.getValue(RotPyreBlock.LIT)) {
				relit.add(Vec3.atBottomCenterOf(pos));
			}
		}
		applyLighting(PutricielChamberLightingRules.Phase.CLEANSING, true, PutricielChamberLightingRules.RELIGHT_GAP);
		sound(SoundInit.MAUSOLEUM_PUTRICIEL_FLARE.get(), 1.0F, 1.1F);
		burnRotHazards(relit);
	}

	void offeringBegins() {
		offeringDue = false;
		offeringsOpened++;
		absolvedThisOffering = false;
		poured.clear();
		if (PutricielDeeds.litany(deeds().mask())) {
			Glyph next = PutricielAbsolutionRules.nextGlyph(glyphLast, glyphBefore, getRandom().nextInt());
			glyphBefore = glyphLast;
			glyphLast = next;
			entityData.set(DATA_GLYPH, next.ordinal());
		}
	}

	void purgeBegins() {
		purgedThisInterval = true;
		narrate(Component.translatable(LINE + "full_purge"));
	}

	void emberingBegins() {
		sound(SoundEvents.FIRE_EXTINGUISH, 1.2F, 0.6F);
		sound(SoundInit.MAUSOLEUM_PUTRICIEL_CREAK.get(), 1.0F, 0.6F);
		if (level() instanceof ServerLevel server) PutricielEffects.soot(server, position().add(0.0D, 1.5D, 0.0D), 10);
		applyLighting(PutricielChamberLightingRules.Phase.ROT, true, PutricielChamberLightingRules.PUT_OUT_GAP);
		narrate(Component.translatable(LINE + "embering"));
	}

	void bloomBegins() {
		bloomUsedPhase = phases().phase();
		SaintArena arena = arena();
		if (arena == null) return;
		List<Vec3> pyres = new ArrayList<>();
		for (BlockPos pos : pyres()) pyres.add(Vec3.atBottomCenterOf(pos));
		LongSet safe = PutricielArenaRules.safeCells(standingAsh(), pyres, position(), position(),
				PutricielArenaRules.BLOOM_RADIUS, pooledCells(arena));
		controller().setCells(safe);
	}

	boolean bloomUsedThisPhase() {
		return bloomUsedPhase == phases().phase();
	}

	/** Nobody within NEAR: the Communion may kneel. */
	boolean nobodyClose() {
		SaintArena arena = arena();
		if (arena == null) return true;
		for (Player player : arena.challengers()) if (horizontal(player) < BossBands.STANDARD.near()) return false;
		return true;
	}

	/** The scorched cells still standing as packed ash. */
	private LongSet standingAsh() {
		LongSet standing = new LongOpenHashSet();
		for (long cell : ashCells) if (level().getBlockState(ashPos(cell)).is(BlockInit.packed_ash.get())) standing.add(cell);
		return standing;
	}

	/** Scorches floor cells to packed ash through the arena's budget: open above, on the boss floor, not a pyre. */
	public void scorch(LongList cells) {
		SaintArena arena = arena();
		if (arena == null || cells.isEmpty()) return;
		scorch(arena, arena.budget(PutricielArenaRules.SCORCH_BUDGET, PutricielArenaRules.SCORCH_PER_INTERVAL,
				PutricielArenaRules.SCORCH_WINDOW, PutricielArenaRules.SCORCH_RECORDED), cells, true);
	}

	/** True when any cell turned to ash; {@code mark} sends the scorch burst at the first. */
	private boolean scorch(SaintArena arena, SaintArena.EditBudget budget, LongList cells, boolean mark) {
		if (cells.isEmpty()) return false;
		AABB floor = arena.bossFloor();
		BlockState ash = BlockInit.packed_ash.get().defaultBlockState();
		boolean any = false;
		for (long cell : cells) {
			BlockPos pos = ashPos(cell);
			if (pos.getX() < floor.minX || pos.getX() >= floor.maxX || pos.getZ() < floor.minZ || pos.getZ() >= floor.maxZ) continue;
			BlockState here = level().getBlockState(pos);
			if (here.isAir() || here.is(BlockInit.packed_ash.get()) || here.is(BlockInit.rot_pyre.get())) continue;
			if (!level().getBlockState(pos.above()).isAir()) continue;
			if (arena.edit(budget, pos, ash)) {
				ashCells.add(cell);
				any = true;
			}
		}
		if (any && mark) {
			BlockPos first = ashPos(cells.getLong(0));
			burst(Vec3.atCenterOf(first.above()), PutricielVisualRules.SCORCH, 1.0F);
		}
		return any;
	}

	/** After the Bloom's last pulse the rot takes back every ash cell nobody stands on. */
	void reclaimAsh() {
		SaintArena arena = arena();
		if (arena == null) return;
		LongSet occupied = new LongOpenHashSet();
		for (Player player : arena.challengers()) occupied.add(BossFootprint.cellAt(player.position()));
		for (long cell : PutricielArenaRules.reclaim(new ArrayList<>(standingAsh()), occupied)) {
			arena.revert(ashPos(cell));
			ashCells.remove(cell);
		}
	}

	private BlockPos ashPos(long cell) {
		BlockPos column = BlockPos.of(cell);
		SaintArena arena = arena();
		int y = arena == null ? blockPosition().getY() - 1 : Mth.floor(arena.floorY()) - 1;
		return new BlockPos(column.getX(), y, column.getZ());
	}

	void spawnHazard(PutricielHazard kind, BossFootprint shape, FootprintFrame frame) {
		spawnHazard(kind, shape, frame, kind.life(), kind.pulse());
	}

	@Nullable
	private BossGroundHazardEntity spawnHazard(PutricielHazard kind, BossFootprint shape, FootprintFrame frame, int life,
			int pulse) {
		SaintArena arena = arena();
		if (arena == null || !(level() instanceof ServerLevel server)) return null;
		return arena.own(BossGroundHazardEntity.spawn(server, this, kind.id(), shape, frame, life, pulse), null);
	}

	/**
	 * The Ignition's commit: the breath clouds flash off over 10 ticks, and the fire's schedule over every live pool
	 * and colony patch starts ({@link PutricielSludgeRules#catchSchedule}): nothing vanishes at once.
	 */
	private void burnRotHazards(List<Vec3> relit) {
		SaintArena arena = arena();
		if (arena == null) return;
		for (Entity entity : arena.owned()) {
			if (entity instanceof BossGroundHazardEntity hazard && hazard.kind() == PutricielHazard.ROT_CLOUD.id()) {
				hazard.expire(10);
			}
		}
		catchClock = 0;
		buildSchedule(arena, relit);
	}

	// ── the rot sludge (revision 2026-11-06, R2) ─────────────────────

	/**
	 * Lays a pool of rot sludge where a rot move landed, through the placement rules (the boss floor, the pyre
	 * circles, this fight's ash, the overlap with older pools) and the cap, which settles the oldest pools first. A
	 * pool laid over a colony patch absorbs it. Only in the rot.
	 */
	public Outcome laySludge(Patch wanted) {
		SaintArena arena = arena();
		if (arena == null || !(level() instanceof ServerLevel) || stage != PutricielStage.ROT || ending()) return Outcome.SKIP;
		double floorY = arena.floorY();
		Patch onFloor = new Patch(new Vec3(wanted.from().x, floorY, wanted.from().z), new Vec3(wanted.to().x, floorY, wanted.to().z),
				wanted.radius(), false);
		List<BossGroundHazardEntity> pools = livePools(arena);
		List<Patch> patches = patchesOf(pools, false);
		Placement placement = PutricielSludgeRules.place(onFloor, arena.bossFloor(), pyrePoints(), standingAsh(), patches);
		if (placement.outcome() == Outcome.SKIP) return Outcome.SKIP;
		if (placement.outcome() == Outcome.SPLASH) {
			Vec3 at = patches.get(placement.onto()).nearestEdge(onFloor.centre());
			burst(at.add(0.0D, 0.1D, 0.0D), PutricielVisualRules.ROT_IMPACT, 1.0F);
			soundAt(at, SoundEvents.HONEY_BLOCK_PLACE, 0.8F, 0.8F);
			return Outcome.SPLASH;
		}
		Patch pool = placement.patch();
		int settle = PutricielSludgeRules.settleCount(patches, pool, arena.challengerCount());
		for (int i = 0; i < settle; i++) settle(pools.get(i));
		for (Entity entity : arena.owned()) {
			if (entity instanceof BossGroundHazardEntity colony && colony.kind() == PutricielHazard.ROT_COLONY.id() && !colony.fading()
					&& PutricielSludgeRules.absorbs(pool, colony.anchor(), PutricielHazard.COLONY_RADIUS)) {
				colony.expire(PutricielSludgeRules.ABSORB);
			}
		}
		spawnHazard(PutricielHazard.ROT_SLUDGE, PutricielSludgeRules.shape(pool), PutricielSludgeRules.frame(pool, floorY),
				PutricielSludgeRules.POOL_LIFE, PutricielSludgeRules.POOL_PULSE);
		Vec3 centre = pool.centre();
		soundAt(centre, SoundEvents.HONEY_BLOCK_PLACE, 1.0F, 0.6F);
		soundAt(centre, SoundEvents.MUD_PLACE, 0.8F, 0.7F);
		teachAll(LINE + "sludge_taught");
		return Outcome.LAY;
	}

	/** The Bloom's field drains into two pools at its last pulse, on cells clear of safe ground and of challengers. */
	void drainBloom(FootprintFrame frame) {
		SaintArena arena = arena();
		if (arena == null) return;
		List<Vec3> players = new ArrayList<>();
		for (Player player : arena.challengers()) players.add(player.position());
		long seed = syncedMoveSerial() * 31L + getUUID().getLeastSignificantBits();
		for (Vec3 spot : PutricielSludgeRules.bloomDrains(arena.bossFloor(), frame.origin(), PutricielArenaRules.BLOOM_RADIUS,
				frame.cells(), players, arena.floorY(), seed)) {
			laySludge(Patch.disc(spot, PutricielSludgeRules.BLOOM_RADIUS));
		}
	}

	/** The live pools, oldest first: not burning, settling or fading. */
	private List<BossGroundHazardEntity> livePools(SaintArena arena) {
		List<BossGroundHazardEntity> pools = new ArrayList<>();
		for (Entity entity : arena.owned()) {
			if (entity instanceof BossGroundHazardEntity hazard && hazard.kind() == PutricielHazard.ROT_SLUDGE.id() && !hazard.fading()) {
				pools.add(hazard);
			}
		}
		return pools;
	}

	/** For GameTests and the review: the live pools, oldest first. */
	public List<BossGroundHazardEntity> sludgePools() {
		SaintArena arena = arena();
		return arena == null ? List.of() : livePools(arena);
	}

	/** Ticks since the Ignition's commit while the fire's schedule runs, else -1. */
	public int catchClock() {
		return catchClock;
	}

	private static List<Patch> patchesOf(List<BossGroundHazardEntity> hazards, boolean colony) {
		List<Patch> patches = new ArrayList<>();
		for (BossGroundHazardEntity hazard : hazards) {
			Patch patch = PutricielSludgeRules.patchOf(hazard.shape(), hazard.frame(), colony);
			if (patch != null) patches.add(patch);
		}
		return patches;
	}

	private List<Vec3> pyrePoints() {
		List<Vec3> points = new ArrayList<>();
		for (BlockPos pos : pyres()) points.add(Vec3.atBottomCenterOf(pos));
		return points;
	}

	/** The cells under the live pools, which the Bloom never counts as safe. */
	private LongSet pooledCells(SaintArena arena) {
		LongSet cells = new LongOpenHashSet();
		for (BossGroundHazardEntity pool : livePools(arena)) {
			Patch patch = PutricielSludgeRules.patchOf(pool.shape(), pool.frame(), false);
			if (patch == null) continue;
			cells.addAll(patch.runnel() ? PutricielArenaRules.lineCells(patch.from(), patch.to(), patch.radius(), 64)
					: PutricielArenaRules.discCells(patch.from(), patch.radius(), 64));
		}
		return cells;
	}

	/** A pool sinks into the moss over {@link PutricielSludgeRules#SETTLE} ticks and touches nothing while it does. */
	private void settle(BossGroundHazardEntity pool) {
		if (pool.fading()) return;
		pool.expire(PutricielSludgeRules.SETTLE);
		soundAt(pool.anchor(), SoundEvents.MUD_BREAK, 0.6F, 0.6F);
	}

	/** Where Putriciel likes to stand: the nearest live pool or colony patch within {@code within} of {@code near}. */
	@Nullable
	Vec3 restingRot(Vec3 near, double within) {
		SaintArena arena = arena();
		if (arena == null) return null;
		Vec3 best = null;
		double distance = within;
		for (Entity entity : arena.owned()) {
			if (!(entity instanceof BossGroundHazardEntity hazard) || hazard.fading()) continue;
			PutricielHazard kind = PutricielHazard.byId(hazard.kind());
			if (kind == null || !kind.catches()) continue;
			Vec3 at = hazard.anchor();
			double d = BossFootprint.Shapes.planar(at, near);
			if (d <= distance) {
				distance = d;
				best = at;
			}
		}
		return best == null ? null : new Vec3(best.x, getY(), best.z);
	}

	/** A pool's pulse: +0.5 colonisation to whoever wades, at most once a pulse however many pools they stand in. */
	private void feedSludge(List<LivingEntity> inside) {
		// the Bloom's field passes over the pools: its pulses do not stack on them
		if (controller().move() == PutricielMove.BLOOM_OF_ROT
				&& controller().stage() == com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline.Stage.ACTIVE) return;
		for (LivingEntity entity : inside) {
			if (!(entity instanceof Player player)) continue;
			Integer last = sludgeFedAt.get(player.getUUID());
			if (last != null && sludgeTicks - last < PutricielSludgeRules.POOL_PULSE) continue;
			sludgeFedAt.put(player.getUUID(), sludgeTicks);
			colonise(player, PutricielSludgeRules.POOL_COLONISATION);
		}
	}

	/** The sludge's tick: the safety life, the fire's schedule and live sources, the slow, the burning pools' ash. */
	private void tickSludge(SaintArena arena) {
		sludgeTicks++;
		for (BossGroundHazardEntity pool : livePools(arena)) {
			if (pool.tickCount >= PutricielSludgeRules.POOL_LIFE - 2) settle(pool);
		}
		tickCatch(arena);
		tickWading(arena);
		tickBurningPools(arena);
	}

	/** Wading slows (x0.65), released 6 ticks after the last pool; the honey step every 8 ticks to the wader. */
	private void tickWading(SaintArena arena) {
		List<Patch> pools = patchesOf(livePools(arena), false);
		for (Player player : arena.challengers()) {
			UUID id = player.getUUID();
			boolean in = false;
			if (!pools.isEmpty() && player.getY() - arena.floorY() <= WADE_HEIGHT && hazardTouches(player)) {
				for (Patch pool : pools) {
					if (pool.contains(player.position())) {
						in = true;
						break;
					}
				}
			}
			if (in) {
				wadingAt.put(id, sludgeTicks);
				if (sludgeTicks % PutricielSludgeRules.WADE_SOUND_EVERY == 0) {
					player.playNotifySound(SoundEvents.HONEY_BLOCK_STEP, SoundSource.HOSTILE, 0.6F, 0.6F);
				}
			}
			Integer last = wadingAt.get(id);
			boolean slowed = last != null && sludgeTicks - last <= PutricielSludgeRules.SLOW_RELEASE;
			if (!slowed) wadingAt.remove(id);
			AttributeModifiers.sync(player.getAttribute(Attributes.MOVEMENT_SPEED), SLUDGE_KEY, PutricielSludgeRules.SLOW,
					AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, slowed);
			if (slowed) modded.add(id);
		}
	}

	/** The schedule over every live pool and colony patch, from the commit (tick 0 of {@link #catchClock}). */
	private void buildSchedule(SaintArena arena, List<Vec3> pyres) {
		List<Patch> patches = new ArrayList<>();
		scheduled.clear();
		for (Entity entity : arena.owned()) {
			if (!(entity instanceof BossGroundHazardEntity hazard) || hazard.fading()) continue;
			PutricielHazard kind = PutricielHazard.byId(hazard.kind());
			if (kind == null || !kind.catches()) continue;
			Patch patch = PutricielSludgeRules.patchOf(hazard.shape(), hazard.frame(), kind == PutricielHazard.ROT_COLONY);
			if (patch == null) continue;
			patches.add(patch);
			scheduled.add(hazard.getUUID());
		}
		catchSchedule = PutricielSludgeRules.catchSchedule(patches, position(), pyres, PutricielChamberLightingRules.RELIGHT_GAP);
	}

	private void endSchedule() {
		catchClock = -1;
		catchSchedule = null;
		scheduled.clear();
	}

	/** The fire comes for the rot: runners set out, due catches land, and the burning body and players light pools. */
	private void tickCatch(SaintArena arena) {
		if (catchClock < 0) return;
		// a load drops the schedule: recompute it from the saved commit tick (late catches land at once)
		if (catchSchedule == null) buildSchedule(arena, pyrePoints());
		Schedule schedule = catchSchedule;
		for (int index : schedule.departing(catchClock)) spawnRunner(arena, schedule.entry(index));
		for (int index : schedule.due(catchClock)) {
			Catch due = schedule.entry(index);
			lightPatch(index, due.point(), due.from(), due.source());
		}
		if (stage == PutricielStage.BURNING || stage == PutricielStage.IGNITING && ignited) {
			double body = getBbWidth() / 2.0D;
			for (int index = 0; index < schedule.size(); index++) {
				if (schedule.caught(index)) continue;
				Patch patch = schedule.patch(index);
				if (PutricielSludgeRules.bodyTouches(patch, position(), body)) {
					lightPatch(index, patch.nearestEdge(position()), position(), PutricielSludgeRules.Source.BODY);
					continue;
				}
				for (Player player : arena.challengers()) {
					if (player.isOnFire() && patch.contains(player.position())) {
						Vec3 feet = new Vec3(player.getX(), arena.floorY(), player.getZ());
						lightPatch(index, feet, feet, PutricielSludgeRules.Source.PLAYER);
						break;
					}
				}
			}
		}
		catchClock++;
	}

	/** A fire move or fire hazard reaching the rot lights every patch its footprint strikes, at the first point hit. */
	void fireReachesSludge(BossFootprint shape, FootprintFrame frame, double age) {
		Schedule schedule = catchSchedule;
		if (schedule == null || catchClock < 0 || !(stage == PutricielStage.BURNING || stage == PutricielStage.IGNITING && ignited)) return;
		for (int index = 0; index < schedule.size(); index++) {
			if (schedule.caught(index)) continue;
			Vec3 hit = PutricielSludgeRules.touched(schedule.patch(index), shape, frame, age);
			if (hit != null) lightPatch(index, hit, frame.origin(), PutricielSludgeRules.Source.FIRE);
		}
	}

	/**
	 * A patch catches: a pool starts its 50-tick burn and a {@code SLUDGE_FIRE} on its footprint (aim the catch point,
	 * target the source) takes its place; a colony patch chars and crumbles over 30 ticks. The slow ends now.
	 */
	private void lightPatch(int index, Vec3 point, Vec3 from, PutricielSludgeRules.Source source) {
		Schedule schedule = catchSchedule;
		if (schedule == null || !schedule.light(index, Math.max(0, catchClock), point, from, source)) return;
		if (!(level() instanceof ServerLevel server) || index >= scheduled.size()) return;
		if (!(server.getEntity(scheduled.get(index)) instanceof BossGroundHazardEntity hazard) || hazard.isRemoved() || hazard.fading()) return;
		Patch patch = schedule.patch(index);
		if (patch.colony()) {
			hazard.expire(PutricielSludgeRules.COLONY_BURN);
			return;
		}
		hazard.expire(PutricielSludgeRules.BURN);
		spawnHazard(PutricielHazard.SLUDGE_FIRE, hazard.shape(), hazard.frame().withAim(point).withTarget(from),
				PutricielSludgeRules.FIRE_LIFE, PutricielSludgeRules.FIRE_PULSE);
		soundAt(point, SoundEvents.FIRECHARGE_USE, 0.6F, 1.2F);
		SaintArena arena = arena();
		if (arena != null) {
			for (Player player : arena.challengers()) if (patch.contains(player.position())) wadingAt.remove(player.getUUID());
		}
		teachAll(LINE + "sludge_burns");
	}

	/** A fire runner: a burning fuse from its source to the catch point, harmless, crawling at 0.5 a tick. */
	private void spawnRunner(SaintArena arena, Catch leg) {
		double length = BossFootprint.Shapes.planar(leg.from(), leg.point());
		if (length < 0.25D) return;
		int life = PutricielSludgeRules.runnerLife(length);
		spawnHazard(PutricielHazard.FIRE_RUNNER, PutricielSludgeRules.runnerShape(length),
				FootprintFrame.simple(leg.from(), leg.point(), leg.point(), arena.floorY()), life, life);
		soundAt(leg.from(), SoundEvents.FIRE_AMBIENT, 0.8F, 1.1F);
	}

	/** A burning pool boils (a lava pop now and then) and at age 44 turns the floor under it to ash. */
	private void tickBurningPools(SaintArena arena) {
		Set<UUID> burning = new HashSet<>();
		for (Entity entity : arena.owned()) {
			if (!(entity instanceof BossGroundHazardEntity fire) || fire.kind() != PutricielHazard.SLUDGE_FIRE.id()) continue;
			burning.add(fire.getUUID());
			int age = fire.tickCount;
			if (fire.life() == PutricielSludgeRules.FIRE_LIFE && age >= PutricielSludgeRules.ASH_AT && ashed.add(fire.getUUID())) {
				Patch patch = PutricielSludgeRules.patchOf(fire.shape(), fire.frame(), false);
				if (patch != null) {
					scorch(arena, arena.budget(PutricielArenaRules.SLUDGE_ASH_BUDGET, PutricielArenaRules.SLUDGE_ASH_PER_WINDOW,
									PutricielArenaRules.SLUDGE_ASH_WINDOW, PutricielArenaRules.SLUDGE_ASH_RECORDED),
							PutricielSludgeRules.ashCells(patch, PutricielArenaRules.SLUDGE_ASH_PER_POOL), false);
				}
				soundAt(fire.anchor(), SoundEvents.FIRE_EXTINGUISH, 0.5F, 0.8F);
			}
			if (age >= PutricielSludgeRules.CATCH && age < PutricielSludgeRules.BOIL_END && age % 4 == 0
					&& getRandom().nextFloat() < 0.6F) {
				Vec3 at = fire.anchor();
				soundAt(at.add(getRandom().nextGaussian() * 0.6D, 0.1D, getRandom().nextGaussian() * 0.6D),
						SoundEvents.LAVA_POP, 0.7F, 0.7F);
			}
		}
		ashed.retainAll(burning);
	}

	/** At the ending every unlit pool catches at once from its edge nearest the body; its fire lives 10 ticks, no ash. */
	private void burnSludgeAtTheEnding() {
		endSchedule();
		SaintArena arena = arena();
		if (arena == null) return;
		for (Entity entity : arena.owned()) {
			if (!(entity instanceof BossGroundHazardEntity pool) || pool.kind() != PutricielHazard.ROT_SLUDGE.id()) continue;
			if (pool.fade() == PutricielSludgeRules.BURN || pool.fade() == PutricielSludgeRules.SETTLE) continue;
			Patch patch = PutricielSludgeRules.patchOf(pool.shape(), pool.frame(), false);
			if (patch == null) continue;
			Vec3 point = PutricielSludgeRules.endingCatch(patch, position());
			spawnHazard(PutricielHazard.SLUDGE_FIRE, pool.shape(), pool.frame().withAim(point).withTarget(position()),
					PutricielSludgeRules.ENDING_BURN, PutricielSludgeRules.ENDING_BURN);
		}
	}

	// ── the chamber ──────────────────────────────────────────────────

	/** The chamber's pyres, the dais's first (nearest the tomb). */
	private List<BlockPos> pyres() {
		SaintArena arena = arena();
		if (arena == null) return List.of();
		List<BlockPos> found = new ArrayList<>(arena.find(PYRES,
				state -> state.is(BlockInit.rot_pyre.get()) && state.hasProperty(RotPyreBlock.LIT)));
		BlockPos tomb = arena.tomb();
		found.sort(Comparator.comparingDouble(pos -> pos.distSqr(tomb)));
		return found;
	}

	private List<BlockPos> litPyres() {
		List<BlockPos> lit = new ArrayList<>();
		for (BlockPos pos : pyres()) {
			BlockState state = level().getBlockState(pos);
			if (state.is(BlockInit.rot_pyre.get()) && state.hasProperty(RotPyreBlock.LIT) && state.getValue(RotPyreBlock.LIT)
					&& Math.sqrt(pos.distToCenterSqr(position())) <= PutricielArenaRules.PYRE_REACH) {
				lit.add(pos);
			}
		}
		lit.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(position())));
		return lit;
	}

	int litPyreCount() {
		return litPyres().size();
	}

	/** How many pyres Pyre Lines flares: every lit one within 20, at most 10. */
	int pyreLineCount() {
		return Math.min(PutricielArenaRules.MAX_PYRE_LINES, litPyreCount());
	}

	List<Vec3> flaredPyres(int count) {
		List<Vec3> points = new ArrayList<>();
		for (BlockPos pos : litPyres()) {
			if (points.size() >= count) break;
			points.add(Vec3.atBottomCenterOf(pos));
		}
		return points;
	}

	/** The lit pyre nearest the quarry, which the Cauterising Lane runs from. */
	@Nullable
	Vec3 cauterisingPyre() {
		LivingEntity target = controller().target();
		Vec3 toward = target == null ? position() : target.position();
		BlockPos best = null;
		double distance = Double.MAX_VALUE;
		for (BlockPos pos : litPyres()) {
			double d = pos.distToCenterSqr(toward);
			if (d < distance) {
				distance = d;
				best = pos;
			}
		}
		return best == null ? null : Vec3.atBottomCenterOf(best);
	}

	/**
	 * Sets the chamber's lights for {@code phase} through the arena (which records each first change): pyres one
	 * every {@code gap} ticks, dais first; shelves at once.
	 */
	private void applyLighting(PutricielChamberLightingRules.Phase phase, boolean effects, int gap) {
		SaintArena arena = arena();
		if (arena == null || !(level() instanceof ServerLevel server)) return;
		int index = 0;
		boolean pyreOn = PutricielChamberLightingRules.on(Light.PYRE, phase);
		for (BlockPos pos : pyres()) {
			BlockState state = level().getBlockState(pos);
			if (!state.is(BlockInit.rot_pyre.get()) || !state.hasProperty(RotPyreBlock.LIT) || state.getValue(RotPyreBlock.LIT) == pyreOn) continue;
			BlockState next = state.setValue(RotPyreBlock.LIT, pyreOn);
			if (gap > 0 && index > 0) arena.schedule(pos, next, index * gap);
			else arena.set(pos, next);
			if (effects) PutricielEffects.lightShift(server, pos, Light.PYRE, pyreOn);
			index++;
		}
		boolean shelfOn = PutricielChamberLightingRules.on(Light.SHELF, phase);
		for (BlockPos pos : arena.find(SHELVES, state -> state.is(BlockInit.spore_shelf.get()) && state.hasProperty(SporeShelfBlock.BLOOM))) {
			BlockState state = level().getBlockState(pos);
			if (!state.is(BlockInit.spore_shelf.get()) || state.getValue(SporeShelfBlock.BLOOM) == shelfOn) continue;
			arena.set(pos, state.setValue(SporeShelfBlock.BLOOM, shelfOn));
			if (effects) PutricielEffects.lightShift(server, pos, Light.SHELF, shelfOn);
		}
	}

	/** At the bar reveal the rot takes the room: the pyres gutter out one every 3 ticks, dais first; the shelves bloom. */
	private void takeTheRoom() {
		applyLighting(PutricielChamberLightingRules.Phase.ROT, true, PutricielChamberLightingRules.PUT_OUT_GAP);
		sound(SoundInit.MAUSOLEUM_PUTRICIEL_FLARE.get(), 1.0F, 0.6F);
		teachAll(LINE + "kindling_taught");
	}

	// ── the Downfall deed's faithful ─────────────────────────────────

	void raiseFaithful(Vec3 near) {
		SaintArena arena = arena();
		if (arena == null || !PutricielDeeds.downfall(deeds().mask())) return;
		if (!PutricielArenaRules.faithfulMayRise(faithful.size(), faithfulRaised)) return;
		BlockPos best = null;
		double distance = Double.MAX_VALUE;
		for (BlockPos pos : arena.find(BOG_BODIES, state -> state.is(BlockInit.bog_body.get()))) {
			if (!level().getBlockState(pos).is(BlockInit.bog_body.get())) continue;
			double d = pos.distToCenterSqr(near);
			if (d < distance) {
				distance = d;
				best = pos;
			}
		}
		if (best == null) return;
		BogRevenantEntity revenant = EntityInit.bog_revenant.get().create(level());
		if (revenant == null) return;
		revenant.moveTo(best.getX() + 0.5D, best.getY(), best.getZ() + 0.5D, getYRot(), 0.0F);
		revenant.addTag(PutricielFaithfulEvents.TAG);
		revenant.setPersistenceRequired();
		arena.set(best, Blocks.AIR.defaultBlockState(), SaintArena.RESTORE_FLAGS);
		level().addFreshEntity(revenant);
		arena.own(revenant, best);
		if (controller().target() != null) revenant.setTarget(controller().target());
		faithful.put(revenant.getUUID(), best);
		faithfulRaised++;
		soundAt(Vec3.atCenterOf(best), SoundEvents.ROOTED_DIRT_BREAK, 1.0F, 0.6F);
		narrate(Component.translatable(LINE + "faithful_rise"));
	}

	private void tickFaithful(SaintArena arena) {
		if (faithful.isEmpty() || !(level() instanceof ServerLevel server)) return;
		Iterator<Map.Entry<UUID, BlockPos>> it = faithful.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, BlockPos> entry = it.next();
			Entity entity = server.getEntity(entry.getKey());
			if (entity == null || entity.isRemoved() || !entity.isAlive()) {
				arena.revert(entry.getValue());
				faithfulStamps.remove(entry.getKey());
				it.remove();
				continue;
			}
			if (entity instanceof BogRevenantEntity revenant) {
				LivingEntity struck = revenant.getLastHurtMob();
				int stamp = revenant.getLastHurtMobTimestamp();
				Integer seen = faithfulStamps.put(entry.getKey(), stamp);
				if (struck instanceof Player player && seen != null && seen != stamp && isChallenger(player)) {
					colonise(player, FAITHFUL_HIT_COLONISATION);
				}
				if (revenant.getTarget() == null && controller().target() != null) revenant.setTarget(controller().target());
			}
		}
	}

	/** The fire takes the faithful back: any inside a fire move's footprint burns to ash. */
	void burnFaithful(BossFootprint shape, FootprintFrame frame, double age) {
		if (faithful.isEmpty() || !(level() instanceof ServerLevel server)) return;
		for (UUID id : new ArrayList<>(faithful.keySet())) {
			Entity entity = server.getEntity(id);
			if (entity == null || entity.isRemoved()) continue;
			if (shape.hits(frame, entity.position(), entity.onGround(), age)) {
				burst(entity.position().add(0.0D, 1.0D, 0.0D), PutricielVisualRules.EMBER, 1.0F);
				entity.discard();
			}
		}
	}

	private void discardFaithful() {
		if (!(level() instanceof ServerLevel server)) return;
		for (UUID id : new ArrayList<>(faithful.keySet())) {
			Entity entity = server.getEntity(id);
			if (entity != null && !entity.isRemoved()) {
				burst(entity.position().add(0.0D, 1.0D, 0.0D), PutricielVisualRules.EMBER, 1.0F);
				entity.discard();
			}
		}
	}

	// ── the ending ───────────────────────────────────────────────────

	@Override
	protected void onEndingTick(int tick) {
		if (tick == 0) {
			clearColonisation();
			discardFaithful();
			burnSludgeAtTheEnding();
			bossBar.setProgress(1.0F);
			sound(SoundInit.RITE_SUBJECT_SINK.get(), 1.0F, 0.7F);
			burst(palmPoint(), PutricielVisualRules.ABSOLVE, 3.0F);
		} else if (tick == PutricielEndingRules.BURN_START) {
			sound(SoundInit.MAUSOLEUM_PUTRICIEL_CRACKLE.get(), 1.4F, 0.9F);
			applyLighting(PutricielChamberLightingRules.Phase.REST, true, 0);
		} else if (tick == PutricielEndingRules.BURN_END) {
			sound(SoundEvents.FIRE_EXTINGUISH, 1.0F, 0.7F);
			if (level() instanceof ServerLevel server) PutricielEffects.soot(server, position().add(0.0D, 1.2D, 0.0D), 12);
		} else if (tick == PutricielEndingRules.LINE_TICK) {
			sound(SoundInit.RITE_COMPLETE.get(), 1.2F, 0.8F);
			narrate(Component.translatable(LINE + "the_saint_finds_rest"));
			burst(position().add(0.0D, 1.0D, 0.0D), PutricielVisualRules.REST, 3.0F);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide) clearColonisation();
		super.remove(reason);
	}

	// ── sounds and lines ─────────────────────────────────────────────

	@Override
	protected SoundEvent getAmbientSound() {
		return stage().fire() ? SoundEvents.FIRE_AMBIENT : SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundInit.ENTITY_FUNGLING_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.RITE_SUBJECT_SINK.get();
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.MUD_STEP, 0.7F, 0.8F);
		playSound(SoundEvents.ROOTED_DIRT_STEP, 0.7F, 0.8F);
	}

	void sound(SoundEvent event, float volume, float pitch) {
		level().playSound(null, getX(), getY(), getZ(), event, SoundSource.HOSTILE, volume, pitch);
	}

	void soundAt(Vec3 at, SoundEvent event, float volume, float pitch) {
		level().playSound(null, at.x, at.y, at.z, event, SoundSource.HOSTILE, volume, pitch);
	}

	void burst(Vec3 at, int kind, float radius) {
		if (level() instanceof ServerLevel server) PutricielEffects.burst(server, at, kind, radius);
	}

	/** Narration in pale cream italics to everyone in the fight: Putriciel never speaks. */
	private void narrate(Component line) {
		broadcast(line.copy().withStyle(style -> style.withColor(CREAM).withItalic(true)));
	}

	private void teachAll(String key) {
		SaintArena arena = arena();
		if (arena == null) return;
		for (Player player : arena.challengers()) SaintLines.teach(player, key);
	}

	private boolean isChallenger(Player player) {
		SaintArena arena = arena();
		return arena != null && arena.challengers().contains(player);
	}

	private double horizontal(Entity entity) {
		return Math.hypot(entity.getX() - getX(), entity.getZ() - getZ());
	}

	// ── save ─────────────────────────────────────────────────────────

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		CompoundTag own = new CompoundTag();
		own.putInt("stage", stage.ordinal());
		own.putInt("stage_age", stageAge);
		own.putInt("absolutions", absolutions());
		own.putInt("intervals", intervalsEnded());
		own.putInt("offerings", offeringsOpened);
		own.putBoolean("offering_due", offeringDue);
		own.putBoolean("purged", purgedThisInterval);
		own.putBoolean("absolved", absolvedThisOffering);
		own.putBoolean("room_taken", roomTaken);
		own.putBoolean("ignited", ignited);
		own.putInt("glyph", entityData.get(DATA_GLYPH));
		own.putInt("glyph_last", glyphLast.ordinal());
		own.putInt("glyph_before", glyphBefore.ordinal());
		own.putInt("bloom_phase", bloomUsedPhase);
		own.putBoolean("tremor", tremorSounded);
		own.putInt("faithful_raised", faithfulRaised);
		own.putLongArray("ash", ashCells.toLongArray());
		own.putInt("catch_clock", catchClock);
		ListTag risen = new ListTag();
		faithful.forEach((id, pos) -> {
			CompoundTag entry = new CompoundTag();
			entry.putUUID("id", id);
			entry.putLong("pos", pos.asLong());
			risen.add(entry);
		});
		own.put("faithful", risen);
		tag.put("Putriciel", own);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		CompoundTag own = tag.getCompound("Putriciel");
		stage = PutricielStage.byId(own.getInt("stage"));
		stageAge = own.getInt("stage_age");
		entityData.set(DATA_STAGE, stage.ordinal());
		entityData.set(DATA_STAGE_AGE, stageAge);
		entityData.set(DATA_ABSOLUTIONS, own.getInt("absolutions"));
		entityData.set(DATA_INTERVALS, own.getInt("intervals"));
		entityData.set(DATA_GLYPH, own.getInt("glyph"));
		offeringsOpened = own.getInt("offerings");
		offeringDue = own.getBoolean("offering_due");
		purgedThisInterval = own.getBoolean("purged");
		absolvedThisOffering = own.getBoolean("absolved");
		roomTaken = own.getBoolean("room_taken");
		ignited = own.getBoolean("ignited");
		glyphLast = Glyph.byId(own.getInt("glyph_last"));
		glyphBefore = Glyph.byId(own.getInt("glyph_before"));
		bloomUsedPhase = own.contains("bloom_phase") ? own.getInt("bloom_phase") : -1;
		tremorSounded = own.getBoolean("tremor");
		faithfulRaised = own.getInt("faithful_raised");
		ashCells.clear();
		for (long cell : own.getLongArray("ash")) ashCells.add(cell);
		catchClock = own.contains("catch_clock") ? own.getInt("catch_clock") : -1;
		catchSchedule = null;
		faithful.clear();
		ListTag risen = own.getList("faithful", Tag.TAG_COMPOUND);
		for (int i = 0; i < risen.size(); i++) {
			CompoundTag entry = risen.getCompound(i);
			if (entry.hasUUID("id")) faithful.put(entry.getUUID("id"), BlockPos.of(entry.getLong("pos")));
		}
	}

	/** For GameTests: the fire cycle's counters. */
	public int offeringsOpened() {
		return offeringsOpened;
	}

	public float absorbedThisOpening() {
		return absorbGiven;
	}

	public int faithfulRaised() {
		return faithfulRaised;
	}

	public LongList scorchedCells() {
		return new LongArrayList(ashCells);
	}
}
