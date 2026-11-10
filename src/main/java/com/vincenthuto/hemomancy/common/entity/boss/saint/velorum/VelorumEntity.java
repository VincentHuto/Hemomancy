package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnrageRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintLines;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Velorum, Saint of the Endless Hour (CONGEATIO + TENEBRIS), on the Saint combat kit (2026-11-05 overhaul design).
 * <p>
 * He fights from stillness and asks the player for the opposite: every challenger carries a stillness meter
 * ({@link VelorumStillness}) that climbs while they stand and falls only while they travel, and at full they are
 * preserved, shut in clear rime for a moment and hurt. Outside his three openings his still body takes 30% of a blow
 * and his health holds at 60% and 25% ({@link VelorumGuardRules}); the Guttering Lantern, the Overheld Invocation and the
 * Bow ({@link VelorumOpeningRules}) are how the fight is won. The frozen congregation round the floor is the arena
 * ({@link VelorumCongregation}): cover from his gaze, woken by warm blood to fight beside a proper challenger, risen
 * against a forced one. When his body fails the hood bows, the frost falls out of his lanterns' light, and the faithful
 * walk back to their places and kneel ({@link VelorumEndingRules}).
 * <p>
 * The 2026-11-06 revision makes every piece of his ice Frozen Cruor and gives the Tenebris half of him work: the liquid
 * dark in pools his lantern holds back ({@link VelorumDark}), Arrested Pulse's mark, Cruor Javelins, Veins of Frost
 * drawn out of the congregation, the Shadow Step, Gloam Cuts and the Eclipse, which the fight's state starts beside the
 * Endless Hour and held Projection relights. The stain record (where his hits drew blood) feeds the Splinter Crown.
 */
public class VelorumEntity extends SaintTrialEntity {
	public static final ResourceLocation STILL_FLOOR_ROOT = Hemomancy.rloc("velorum_still_floor");
	public static final float MAX_HEALTH = 640.0F;
	public static final float WIDTH = 0.9F;
	public static final float HEIGHT = 2.6F;
	/** Ground hazard kinds: the cruor field the Release's trenches leave standing (Slowness II inside). */
	public static final int HAZARD_CRUOR_FIELD = 1;
	/** A pool or strip of liquid dark ({@link VelorumDark}); the Eclipse's chamber-wide dark is one too. */
	public static final int HAZARD_DARK_POOL = 2;
	/** An Eclipse shape standing up out of a frozen or stilled figure, its {@code Disc} the reach it grips within. */
	public static final int HAZARD_ECLIPSE_SHAPE = 3;
	/** A pinned figure's Eclipse shape: it rises, but the hyphae hold its arms. */
	public static final int HAZARD_ECLIPSE_SHAPE_HELD = 4;
	/**
	 * A reach of the Eclipse's shapes. An arm the congregation throws along the floor together is a {@code Union} of one
	 * {@code Lane} part, revealed 20 ticks before it runs out, and roots what it touches; a shape's own reach and grip is
	 * a {@code Tether} from the figure's foot to the challenger, standing its tell and grip, which strikes nothing (the
	 * congregation's tick runs the grip) and from which the renderer poses the shape's arm.
	 */
	public static final int HAZARD_ECLIPSE_REACH = 5;
	public static final int CRUOR_FIELD_TICKS = 60;
	public static final int CRUOR_FIELD_PULSE = 10;
	public static final int CRUOR_FIELD_SLOW = 15;
	public static final ResourceLocation ECLIPSE_ROOT = Hemomancy.rloc("velorum_eclipse_reach");
	/** Where his hits drew blood is kept 200 ticks, and the stains are cued every 20. */
	public static final int SPILL_KEEP = 200;
	public static final int STAIN_EVERY = 20;
	public static final int STAINS_SHOWN = 4;
	/** A stain's depth is its spilled blood over this. */
	public static final float STAIN_FULL = 20.0F;
	/** The Draw of Silence: blood a tick, up to 120 a player, or 1 damage every 5 ticks for the blood-inactive. */
	public static final float SILENCE_RATE = 6.0F;
	public static final float SILENCE_CAP = 120.0F;
	public static final int SILENCE_HURT_EVERY = 5;
	public static final float SILENCE_HURT = 1.0F;
	/** The Endless Hour: never within 400 ticks of the last one's end, and the 6 damage the unpreserved take. */
	public static final int HOUR_REST = 400;
	public static final float HOUR_CLOSE_DAMAGE = 6.0F;
	public static final float HOUR_PRESERVES_AT = 50.0F;
	/** Target choice: kept at least 60 ticks unless out of sight and range. */
	public static final int TARGET_KEEP = 60;
	public static final double TARGET_RANGE = 24.0D;
	private static final int AMBIENT_MIN = 200;
	private static final int AMBIENT_SPREAD = 200;
	private static final int GLIDE_STEP_TICKS = 12;

	private static final String LINE_RISE = "message.hemomancy.velorum.line.rise";
	private static final String LINE_PRESERVED = "message.hemomancy.velorum.line.first_preservation";
	private static final String LINE_CONGREGATION = "message.hemomancy.velorum.line.congregation";
	private static final String LINE_HOUR = "message.hemomancy.velorum.line.endless_hour";

	@Nullable
	private VelorumStillness stillness;
	@Nullable
	private VelorumCongregation congregation;
	private int passed;
	private int floorBowAt = -1;
	private int bowedFloors;
	private boolean hourHolding;
	private boolean hourBroken;
	private boolean hourArmed;
	private boolean lastTenthReached;
	private float hourDamage;
	private long hourEndedAt = Long.MIN_VALUE / 2;
	private int invocationHits;
	private float invocationDamage;
	private boolean overheld;
	private float windowDamage;
	private int gutterFed;
	private boolean gutterSearing;
	private long seamAt = Long.MIN_VALUE / 2;
	private final Set<UUID> gazed = new LinkedHashSet<>();
	private final Map<UUID, Float> silenceDrawn = new HashMap<>();
	@Nullable
	private UUID recallTarget;
	@Nullable
	private SaintMove lastMove;
	private int settleLeft;
	private int lessonLeft = -1;
	private int farTicks;
	private int targetHeld;
	private int glideTicks;
	private int ambientIn = AMBIENT_MIN;
	private boolean spokeRise;
	private boolean spokePreserved;
	private boolean spokeCongregation;
	private List<Vec3> scattered = List.of();
	@Nullable
	private Vec3 ring;
	@Nullable
	private VelorumDark dark;
	/** Per move: which of a move's parts (shafts, cuts, the veins) have struck each body. */
	private final Map<UUID, Integer> strikes = new HashMap<>();
	private final ArrayDeque<Spill> spills = new ArrayDeque<>();
	private final boolean[] javelinCut = new boolean[5];
	@Nullable
	private Vec3 stepFrom;
	@Nullable
	private Vec3 stepTo;
	private int cardStep = -1;
	private boolean hourEverEnded;
	/** The move his state last queued for the next boundary (the Hour, the Long Night, the Eclipse), until it starts. */
	@Nullable
	private SaintMove queued;
	private boolean eclipseHolding;
	private boolean eclipseRelit;
	private float eclipseFed;
	private int eclipseIdle;
	private long eclipseEndedAt = Long.MIN_VALUE / 2;
	private double coverage;
	/** The fight's own tick count: the stains' and the coverage's cadence. */
	private int fightTicks;
	private final Map<UUID, Set<UUID>> reachStruck = new HashMap<>();

	/** One blow of his that drew blood: where, how much, when. */
	private record Spill(long cell, Vec3 at, float amount, long time) {
	}

	public VelorumEntity(EntityType<? extends VelorumEntity> type, Level level) {
		super(type, level, 120, new BossBarBinding("entity.hemomancy.velorum", BossPresentationIds.FRAME_VELORUM,
				BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10));
		refreshDimensions();
	}

	@Override
	public EnumSaintType saintType() {
		return EnumSaintType.VELORUM;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.ATTACK_DAMAGE, 9.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 14.0D);
	}

	/** His hitbox (resolution B-12): a 2.3-block figure to the hood, 2.6 to the halo. */
	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return EntityDimensions.scalable(WIDTH, HEIGHT);
	}

	// ── the kit ──────────────────────────────────────────────────────

	@Override
	protected SaintKit createKit() {
		return new SaintKit(List.of(VelorumMove.values()), new VelorumLocomotion(),
				new SaintMeter(SaintMeter.Owner.PLAYER, stillness()), new VelorumPresence(), VelorumPhases.table(),
				SaintDamageTypes.VELORUM, SaintKit.CHALLENGER_SCALING, VelorumEndingRules.SPEC);
	}

	public VelorumStillness stillness() {
		if (stillness == null) stillness = new VelorumStillness();
		return stillness;
	}

	public VelorumCongregation congregation() {
		if (congregation == null) congregation = new VelorumCongregation();
		return congregation;
	}

	public VelorumDark dark() {
		if (dark == null) dark = new VelorumDark();
		return dark;
	}

	/** The phase the passed floors ask for: the phase table reads it. */
	public int wantedPhase() {
		return VelorumGuardRules.phase(passed);
	}

	public boolean forced() {
		return SaintEnrageRules.enraged(awakening());
	}

	@Override
	protected void onArenaResolved(SaintArena arena) {
		congregation().resolve(arena);
		ring = arena.chamberFound()
				? arena.toWorld(VelorumLocomotionRules.RING_X + 0.5D, SaintChamberLayout.FLOOR_Y, VelorumLocomotionRules.RING_Z + 0.5D)
				: arena.bossFloor().getCenter();
	}

	@Override
	protected void onArenaReloaded() {
		// the chamber came back whole: the congregation is frozen again and the lights burn
		hourHolding = false;
		eclipseHolding = false;
		queued = null;
		cardStep = -1;
		dark().clear();
	}

	// ── the server tick ──────────────────────────────────────────────

	@Override
	protected void tickSaint() {
		if (!(level() instanceof ServerLevel server) || ending() || rising()) return;
		SaintArena arena = arena();
		if (arena == null) return;
		List<Player> challengers = arena.challengers();
		fightTicks++;
		if (!spokeRise) {
			spokeRise = true;
			lessonLeft = VelorumLocomotionRules.LESSON_TICKS;
			SaintLines.voice(this, LINE_RISE);
		}
		if (lessonLeft > 0) lessonLeft--;
		SaintMove move = controller().move();
		if (lastMove != null && move == null) settleLeft = VelorumLocomotionRules.settle(VelorumDeedRules.repose(deeds().mask()));
		else if (settleLeft > 0 && move == null) settleLeft--;
		if (move != null && move != lastMove) moveStarted();
		lastMove = move;
		LivingEntity target = controller().target();
		if (target != null && BossBands.STANDARD.band(Math.hypot(target.getX() - getX(), target.getZ() - getZ())) == BossBands.Band.FAR) farTicks++;
		else farTicks = 0;
		congregation().tick(this);
		dark().tick(this, challengers);
		stillness().tick(this, challengers);
		tickCardStep();
		tickStains(server, challengers);
		// he is the cold: a Congeatio state on him melts away
		if (hasEffect(SchoolState.RIME.effect())) removeEffect(SchoolState.RIME.effect());
		tickHour(server, challengers);
		tickEclipseGate(server);
		tickOpeningPressure();
		if (--ambientIn <= 0) {
			ambientIn = AMBIENT_MIN + random.nextInt(AMBIENT_SPREAD);
			VelorumMoves.sound(this, position(), SoundInit.MAUSOLEUM_VELORUM_CREAK.get(), 0.6F, 0.8F);
		}
		for (Player player : challengers) {
			if (player.distanceTo(this) <= 6.0D) {
				for (VelorumCongregation.Figure figure : congregation().figures()) {
					if (figure.state() == VelorumCongregationRules.State.FROZEN
							&& player.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(figure.base())) < 3.0D) {
						SaintLines.teach(player, forced() ? "message.hemomancy.velorum.hint.forced_figure"
								: "message.hemomancy.velorum.hint.proper_figure");
						break;
					}
				}
			}
		}
	}

	private void moveStarted() {
		invocationHits = 0;
		invocationDamage = 0.0F;
		overheld = false;
		gazed.clear();
		silenceDrawn.clear();
		strikes.clear();
		java.util.Arrays.fill(javelinCut, false);
		if (controller().move() == queued) queued = null;
	}

	/** The Endless Hour's state trigger (resolution D-7): on entering phase 3, then when the fight calls for it. */
	private void tickHour(ServerLevel server, List<Player> challengers) {
		if (phases().phase() < 3 || hourHolding || controller().move() == VelorumMove.ENDLESS_HOUR) return;
		if (queued != null || eclipseHolding || controller().move() == VelorumMove.ECLIPSE) return;
		long now = server.getGameTime();
		// D-7's mirror (revision R.7): no Hour within 400 ticks of an Eclipse's end
		if (!VelorumEclipseRules.hourAllowed(now - eclipseEndedAt)) return;
		if (!hourArmed && now - hourEndedAt >= HOUR_REST) {
			boolean many = arena() != null && arena().challengerCount() >= 2;
			boolean still = false;
			for (Player player : challengers) still |= stillness().value(this, player) > HOUR_PRESERVES_AT;
			if (many || still || lastTenthReached) {
				hourArmed = true;
				lastTenthReached = false;
			}
		}
		if (hourArmed && phases().cardTicks() <= 0 && planted() && !openings().isOpen()) {
			hourArmed = false;
			queue(VelorumMove.ENDLESS_HOUR);
		}
	}

	/**
	 * The opening pressure's guarantee (design section 4: the first opening inside the first minute, and one within 600
	 * ticks from any state): past {@link VelorumDeedRules#openingDue} ticks without one, the Long Night is called at the
	 * next boundary. State, not a clock: any opening, the Overheld or the Bow included, resets it.
	 */
	private void tickOpeningPressure() {
		if (controller().move() != null || openings().isOpen() || hourArmed || queued != null || lessonLeft > 0) return;
		if (phases().cardTicks() > 0 || controller().cooldownLeft(VelorumMove.LONG_NIGHT) > 0) return;
		if (controller().ticksSinceOpening() < VelorumDeedRules.openingDue(VelorumDeedRules.litany(deeds().mask()))) return;
		queue(VelorumMove.LONG_NIGHT);
	}

	@Override
	protected void onPhaseChanged(int from, int to) {
		if (to >= 2 && from < 2) {
			congregation().floorRise(this, 0);
			if (!forced() && !spokeCongregation) {
				spokeCongregation = true;
				SaintLines.voice(this, LINE_CONGREGATION);
			}
		}
		if (to >= 3 && from < 3) {
			congregation().floorRise(this, 1);
			hourArmed = true;
			// at the card he sinks into the dark and rises on the ring, then is planted (revision R.6)
			if (ring != null && !planted()) cardStep = 0;
		}
		VelorumMoves.sound(this, position(), SoundEvents.BELL_RESONATE, 1.0F, 0.4F);
	}

	// ── openings ─────────────────────────────────────────────────────

	@Override
	protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
		if (!(level() instanceof ServerLevel)) return;
		if (to != null) {
			windowDamage = 0.0F;
			if (to.id() == VelorumOpeningRules.GUTTER_ID) {
				gutterFed = 0;
				gutterSearing = false;
				VelorumEffects.gutter(this);
				VelorumMoves.sound(this, position(), SoundEvents.LANTERN_PLACE, 0.4F, 0.5F);
			} else if (to.id() == VelorumOpeningRules.OVERHELD_ID) {
				VelorumEffects.overheld(this);
				VelorumMoves.sound(this, position(), SoundInit.RITE_CANCEL_DRAW.get(), 1.0F, 0.8F);
			} else if (to.id() == VelorumOpeningRules.BOW_ID) {
				VelorumEffects.bow(this);
				VelorumMoves.sound(this, position(), SoundInit.RITE_SUBJECT_THUD.get(), 1.0F, 0.6F);
			}
			if (arena() != null) {
				for (Player player : arena().challengers()) SaintLines.teach(player, "message.hemomancy.velorum.hint.opening");
			}
		}
		if (from != null && from.id() == VelorumOpeningRules.BOW_ID && floorBowAt >= 0
				&& (to == null || to.id() != VelorumOpeningRules.BOW_ID)) {
			// a phase floor passes when the Bow made at it ends
			passed = Math.max(passed, floorBowAt + 1);
			floorBowAt = -1;
		}
		if (from != null && to == null && VelorumDeedRules.litany(deeds().mask())) litanyToll();
	}

	/** The Litany deed: each opening ends with a toll that breaks every held channel within 12 blocks. */
	private void litanyToll() {
		SaintArena arena = arena();
		if (arena == null) return;
		VelorumMoves.sound(this, position(), SoundEvents.BELL_BLOCK, 1.0F, 0.5F);
		VelorumEffects.litanyToll(this);
		for (Player player : arena.challengers()) {
			if (player.distanceTo(this) > VelorumDeedRules.TOLL_RADIUS || !player.isUsingItem()) continue;
			SchoolStates.apply(this, player, SchoolState.DISRUPTED, VelorumDeedRules.TOLL_DISRUPTED, 1);
			player.stopUsingItem();
		}
	}

	private boolean bowOpen() {
		SaintOpening.Spec open = openings().current();
		return open != null && open.id() == VelorumOpeningRules.BOW_ID;
	}

	/** Opens or chains into the Bow: 70 ticks (40 under the Repose deed, 100 after a broken Hour). */
	private void bow(boolean brokenHour) {
		boolean repose = VelorumDeedRules.repose(deeds().mask());
		SaintOpening.Spec bow = VelorumOpeningRules.bow(VelorumOpeningRules.bowTicks(repose, brokenHour), repose);
		if (openings().isOpen()) openings().chainTo(bow, bow.ticks());
		else openings().open(bow, bow.ticks());
	}

	public boolean bowing() {
		return bowOpen();
	}

	public boolean seamDrawn() {
		return level().getGameTime() - seamAt <= 1;
	}

	// ── the damage pipeline's hooks ──────────────────────────────────

	private boolean overheldCast(SaintHit hit) {
		SaintOpening.Spec open = openings().current();
		return open != null && open.id() == VelorumOpeningRules.OVERHELD_ID
				&& hit.delivery() == SaintOpening.Delivery.SCHOOL_CAST;
	}

	@Override
	protected float damageTakenFactor(SaintHit hit) {
		// under the dark in the Shadow Step there is nothing of him to strike
		if (stepUnder()) return 0.0F;
		return VelorumGuardRules.factor(openings().isOpen(), hourHolding, overheldCast(hit));
	}

	/** His presence is off while he is under the dark. */
	@Override
	protected boolean presencePaused() {
		return stepUnder();
	}

	@Override
	protected float healthFloor(SaintHit hit) {
		float max = getMaxHealth();
		float health = max <= 0.0F ? 1.0F : getHealth() / max;
		return VelorumGuardRules.floor(health, passed, openings().isOpen() || hourHolding) * max;
	}

	@Override
	protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) {
		if (!(level() instanceof ServerLevel) || stepUnder()) return;
		float max = getMaxHealth();
		boolean open = openings().isOpen();
		Vec3 at = position().add(0.0D, 1.5D, 0.0D);
		if (open || hourHolding) {
			if (applied > 0.0F) VelorumMoves.sound(this, at, SoundEvents.GLASS_BREAK, 0.5F, 0.7F);
		} else {
			VelorumMoves.sound(this, at, SoundEvents.GLASS_HIT, 0.6F, 0.6F);
			VelorumEffects.guarded(this, at);
		}
		boolean searing = hit.fire() || hit.school() == EnumBloodTemper.FLAMMEUS;
		SaintOpening.Spec current = openings().current();
		if (current != null) {
			windowDamage += applied;
			if (current.id() == VelorumOpeningRules.GUTTER_ID && searing && !gutterSearing
					&& !VelorumDeedRules.litany(deeds().mask())) {
				gutterSearing = true;
				openings().extend(VelorumOpeningRules.SEARING_EXTENSION, VelorumOpeningRules.SEARING_EXTENSION
						+ VelorumOpeningRules.PROJECTION_EXTENSION_CAP);
			}
			if (VelorumOpeningRules.chainsToBow(current.id(), windowDamage, max)) bow(false);
		}
		int before = passed;
		passed = VelorumGuardRules.passedBelow(getHealth() / Math.max(1.0F, max), passed);
		if (passed > before && !bowOpen() && (bowedFloors & (1 << (passed - 1))) == 0) {
			bowedFloors |= 1 << (passed - 1);
			bow(false);
		}
		SaintMove move = controller().move();
		boolean invocation = move instanceof VelorumMove velorumMove && VelorumMove.INVOCATIONS.contains(velorumMove)
				&& controller().stage() == BossMoveTimeline.Stage.ACTIVE;
		// Veins of Frost overholds over its wind-up and active span both: struck, the veins snap dead (revision R.8)
		boolean veins = move == VelorumMove.VEINS_OF_FROST && controller().stage() != BossMoveTimeline.Stage.RECOVERY;
		if ((invocation || veins) && hit.attacker() != null && !overheld) {
			invocationHits++;
			invocationDamage += applied;
			if (VelorumOpeningRules.overheld(invocationHits, invocationDamage, max)) overhold();
		}
		if (hourHolding && hit.attacker() != null) {
			hourDamage += VelorumOpeningRules.hourWeight(applied, searing);
			if (VelorumOpeningRules.hourBreaks(hourDamage, max)) breakHour();
		}
	}

	@Override
	protected void onFloorReached(SaintHit hit) {
		float max = getMaxHealth();
		float fraction = getHealth() / Math.max(1.0F, max);
		if (getHealth() <= 1.0F + 1.0E-3F && (openings().isOpen() || hourHolding)) {
			fail(hit.attacker());
			return;
		}
		int floor = VelorumGuardRules.phaseFloor(fraction);
		if (floor >= 0 && (bowedFloors & (1 << floor)) == 0) {
			bowedFloors |= 1 << floor;
			floorBowAt = floor;
			bow(false);
			return;
		}
		if (VelorumGuardRules.lastTenth(fraction)) lastTenthReached = true;
	}

	/** Struck three times or for 4% in an invocation's hold: the move ends at once and the Overheld Invocation opens. */
	private void overhold() {
		overheld = true;
		controller().skipToRecovery();
		if (!openings().isOpen()) openings().open(VelorumOpeningRules.OVERHELD, VelorumOpeningRules.OVERHELD.ticks());
	}

	public boolean overheldThisMove() {
		return overheld;
	}

	// ── the Endless Hour ─────────────────────────────────────────────

	public void beginHour() {
		hourHolding = true;
		hourBroken = false;
		hourDamage = 0.0F;
		congregation().darkenAll(this, VelorumMove.ENDLESS_HOUR.authored().active());
		SaintLines.voice(this, LINE_HOUR);
	}

	/** The hold ran out unbroken: every challenger at 50 or more is preserved, the rest take 6. */
	public void closeHour(SaintMoveContext ctx) {
		if (!hourHolding) return;
		hourHolding = false;
		hourEverEnded = true;
		hourEndedAt = level().getGameTime();
		if (arena() == null) return;
		for (Player player : arena().challengers()) {
			if (stillness().value(this, player) >= HOUR_PRESERVES_AT) {
				stillness().add(this, player, VelorumStillnessRules.MAX);
			} else {
				SaintMoves.hurt(this, player, HOUR_CLOSE_DAMAGE, null);
			}
		}
	}

	/** 8% of his maximum in the hold: the invocation breaks into the long Bow. */
	private void breakHour() {
		hourHolding = false;
		hourBroken = true;
		hourEverEnded = true;
		hourEndedAt = level().getGameTime();
		controller().skipToRecovery();
		congregation().relight(this);
		VelorumEffects.hourBroken(this);
		VelorumMoves.sound(this, position(), SoundInit.RITE_COLLAPSE.get(), 1.0F, 0.8F);
		bow(true);
	}

	public boolean hourHolding() {
		return hourHolding;
	}

	public boolean hourBroken() {
		return hourBroken;
	}

	public boolean hourPending() {
		return hourArmed;
	}

	// ── what the moves call ──────────────────────────────────────────

	/** The Release's filled trenches stand 60 ticks as the cruor field. */
	public void standCruorField(BossFootprint.Lane lane, FootprintFrame frame) {
		if (!(level() instanceof ServerLevel server) || arena() == null) return;
		BossGroundHazardEntity hazard = BossGroundHazardEntity.spawn(server, this, HAZARD_CRUOR_FIELD, lane, frame,
				CRUOR_FIELD_TICKS, CRUOR_FIELD_PULSE);
		arena().own(hazard, null);
		VelorumEffects.cruorRelease(this, frame.origin(), frame.aim());
	}

	@Override
	public void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside) {
		if (hazard.kind() == HAZARD_CRUOR_FIELD) {
			for (LivingEntity entity : inside) {
				if (!com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(this, entity)) continue;
				VelorumMoves.slow(entity, CRUOR_FIELD_SLOW, VelorumMoves.SLOWNESS_II);
			}
		} else if (hazard.kind() == HAZARD_ECLIPSE_REACH && !(hazard.shape() instanceof BossFootprint.Tether)) {
			Set<UUID> struck = reachStruck.computeIfAbsent(hazard.getUUID(), ignored -> new java.util.HashSet<>());
			for (LivingEntity entity : inside) {
				if (!(entity instanceof Player player) || !struck.add(player.getUUID())) continue;
				if (!VelorumMoves.hurt(this, player, VelorumEclipseRules.ARM_DAMAGE)) continue;
				SaintRoot.root(player, VelorumEclipseRules.ARM_ROOT, ECLIPSE_ROOT, true);
				stillness().add(this, player, VelorumEclipseRules.ARM_STILLNESS);
			}
		}
	}

	// ── the revision's moves (2026-11-06) ────────────────────────────

	/** Records a blow of his that drew blood: the Splinter Crown finds where they bled. */
	public void spilled(LivingEntity target, float amount) {
		long now = level().getGameTime();
		double floor = arena() == null ? target.getY() : arena().floorY();
		Vec3 at = new Vec3(Math.floor(target.getX()) + 0.5D, floor, Math.floor(target.getZ()) + 0.5D);
		spills.addLast(new Spill(BossFootprint.cellAt(at), at, amount, now));
		while (!spills.isEmpty() && now - spills.peekFirst().time() > SPILL_KEEP) spills.removeFirst();
	}

	/** The cells where his hits spilled most blood in the last 200 ticks, most first, with how much. */
	private List<Map.Entry<Vec3, Float>> spillSpots() {
		long now = level().getGameTime();
		Map<Long, Vec3> where = new HashMap<>();
		Map<Long, Float> how = new HashMap<>();
		for (Spill spill : spills) {
			if (now - spill.time() > SPILL_KEEP) continue;
			where.putIfAbsent(spill.cell(), spill.at());
			how.merge(spill.cell(), spill.amount(), Float::sum);
		}
		List<Map.Entry<Vec3, Float>> spots = new ArrayList<>();
		for (Map.Entry<Long, Float> entry : how.entrySet()) spots.add(Map.entry(where.get(entry.getKey()), entry.getValue()));
		spots.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));
		return spots;
	}

	/** The Splinter Crown's two extra points: where his hits spilled most blood, else scattered points on the floor. */
	public List<Vec3> crownSpills(int count, boolean reroll) {
		List<Vec3> points = new ArrayList<>();
		for (Map.Entry<Vec3, Float> spot : spillSpots()) {
			if (points.size() >= count) break;
			points.add(spot.getKey());
		}
		if (points.size() < count) points.addAll(scatteredPoints(count - points.size(), reroll));
		return points;
	}

	/** Every 20 ticks the stains show: each challenger's stillest spot and where his hits drew blood. */
	private void tickStains(ServerLevel server, List<Player> challengers) {
		if (fightTicks % STAIN_EVERY != 0 || phases().phase() < 2) return;
		// a stain soaks into the floor: a spot taken mid-jump or from a flying challenger is laid down on it (polish:
		// the review's hovering camera hung a stain in the air)
		SaintArena arena = arena();
		for (Player player : challengers) {
			float depth = stillness().stillDepth(player);
			if (depth > 0.0F) VelorumEffects.stain(this, onFloor(arena, stillness().stillest(player)), depth);
		}
		List<Map.Entry<Vec3, Float>> spots = spillSpots();
		for (int i = 0; i < Math.min(STAINS_SHOWN, spots.size()); i++) {
			VelorumEffects.stain(this, onFloor(arena, spots.get(i).getKey()), Math.min(1.0F, spots.get(i).getValue() / STAIN_FULL));
		}
	}

	private static Vec3 onFloor(@Nullable SaintArena arena, Vec3 at) {
		return arena == null ? at : new Vec3(at.x, arena.floorY(), at.z);
	}

	/** Whether a move's part {@code part} may strike {@code target} now: once a part, at most {@code cap} parts a move. */
	public boolean strikeOnce(LivingEntity target, int part, int cap) {
		int mask = strikes.getOrDefault(target.getUUID(), 0);
		int bit = 1 << part;
		if ((mask & bit) != 0 || Integer.bitCount(mask) >= cap) return false;
		strikes.put(target.getUUID(), mask | bit);
		return true;
	}

	/** The Long Night's commit: four pools in the lantern-shadow of the four rank pillars nearest the target. */
	public void seedNightPools(SaintMoveContext ctx) {
		SaintArena arena = arena();
		if (arena == null) return;
		Vec3 toward = ctx.target() == null ? ctx.frame().aim() : ctx.target().position();
		List<Vec3> pillars = new ArrayList<>();
		for (int x : SaintChamberLayout.PILLARS) {
			for (int z : new int[]{SaintChamberLayout.RANK_NORTH_Z, SaintChamberLayout.RANK_SOUTH_Z}) {
				pillars.add(arena.toWorld(x + 0.5D, SaintChamberLayout.FLOOR_Y, z + 0.5D));
			}
		}
		pillars.sort(java.util.Comparator.comparingDouble(p -> Math.hypot(p.x - toward.x, p.z - toward.z)));
		int phase = ctx.inputs().phase();
		double cap = VelorumDarkRules.cap(VelorumDarkRules.Source.LONG_NIGHT, phase);
		int life = VelorumDarkRules.life(VelorumDarkRules.Source.LONG_NIGHT, phase);
		Vec3 light = position();
		for (int i = 0; i < Math.min(VelorumDarkRules.LONG_NIGHT_POOLS, pillars.size()); i++) {
			Vec3 pillar = pillars.get(i);
			Vec3 away = new Vec3(pillar.x - light.x, 0.0D, pillar.z - light.z);
			Vec3 shadow = away.lengthSqr() < 1.0E-6D ? pillar : pillar.add(away.normalize());
			dark().pool(this, shadow, cap, life);
		}
	}

	/** Arrested Pulse's commit: the target is marked; in phase 3 with two or more challengers, a second too. */
	public void arrestPulse(SaintMoveContext ctx) {
		SaintArena arena = arena();
		if (arena == null || !(ctx.target() instanceof Player target)) return;
		stillness().mark(this, target);
		if (VelorumPulseRules.marks(ctx.inputs().phase(), arena.challengerCount()) < 2) return;
		Player second = stillestOther(arena.challengers(), target, true);
		if (second != null) stillness().mark(this, second);
	}

	/** The stillest challenger other than {@code not}, unmarked when asked. */
	@Nullable
	private Player stillestOther(List<Player> challengers, Player not, boolean unmarked) {
		Player best = null;
		float bestStill = -1.0F;
		for (Player player : challengers) {
			if (player == not || unmarked && stillness().marked(player)) continue;
			float still = stillness().value(this, player);
			if (still > bestStill) {
				bestStill = still;
				best = player;
			}
		}
		return best;
	}

	/**
	 * Cruor Javelins' aims, re-read through the wind-up and locked at the commit: the target, its lead (where it will be
	 * in 10 ticks) and its mirrored lead; in phase 3 two side shafts 2 off the target, or a second target's own two.
	 * Each shaft's end is cut at the first pillar, figure or wall on its line and becomes the tether's anchor.
	 */
	public void aimJavelins(SaintMoveContext ctx, int count) {
		Vec3 origin = ctx.frame().origin();
		LivingEntity main = ctx.target();
		List<Vec3> aims = new ArrayList<>();
		if (main != null) {
			Vec3 at = main.position();
			Vec3 lead = new Vec3(main.getX() - main.xo, 0.0D, main.getZ() - main.zo).scale(VelorumMove.JAVELIN_LEAD);
			aims.add(at);
			aims.add(at.add(lead));
			aims.add(at.subtract(lead));
			if (count > 3) {
				Player second = arena() != null && arena().challengerCount() >= 2 && main instanceof Player player
						? stillestOther(arena().challengers(), player, false) : null;
				if (second != null) {
					Vec3 other = second.position();
					aims.add(other);
					aims.add(other.add(new Vec3(second.getX() - second.xo, 0.0D, second.getZ() - second.zo)
							.scale(VelorumMove.JAVELIN_LEAD)));
				} else {
					Vec3 line = new Vec3(at.x - origin.x, 0.0D, at.z - origin.z);
					Vec3 side = line.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D)
							: new Vec3(-line.z, 0.0D, line.x).normalize().scale(VelorumMove.JAVELIN_SIDE);
					aims.add(at.add(side));
					aims.add(at.subtract(side));
				}
			}
		}
		while (aims.size() < count) aims.add(ctx.frame().aim());
		double floor = arena() == null ? origin.y : arena().floorY();
		for (int i = 0; i < count; i++) {
			Vec3 to = aims.get(i);
			Vec3 line = new Vec3(to.x - origin.x, 0.0D, to.z - origin.z);
			Vec3 direction = line.lengthSqr() < 1.0E-6D ? getLookAngle().multiply(1.0D, 0.0D, 1.0D) : line;
			if (direction.lengthSqr() < 1.0E-6D) direction = new Vec3(0.0D, 0.0D, 1.0D);
			direction = direction.normalize();
			double reach = shaftReach(origin, direction);
			javelinCut[i] = reach < VelorumMove.JAVELIN_REACH - 1.0E-3D;
			Vec3 end = origin.add(direction.scale(reach));
			controller().setAnchor(i, new Vec3(end.x, floor, end.z));
		}
	}

	/** How far a shaft flies from {@code origin} along {@code direction} before the first pillar, figure or wall. */
	public double shaftReach(Vec3 origin, Vec3 direction) {
		Vec3 from = origin.add(0.0D, SHAFT_HEIGHT, 0.0D);
		Vec3 to = from.add(direction.scale(VelorumMove.JAVELIN_REACH));
		var hit = level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
		if (hit.getType() == HitResult.Type.MISS) return VelorumMove.JAVELIN_REACH;
		return Math.max(0.0D, Math.hypot(hit.getLocation().x - from.x, hit.getLocation().z - from.z));
	}

	/** The shaft's line runs at a kneeling figure's chest, so a frozen figure stops it. */
	private static final double SHAFT_HEIGHT = 0.8D;

	/** Ticks after its loose that shaft {@code index} reaches its end. */
	public int javelinLands(FootprintFrame frame, int index) {
		if (index >= frame.anchors().size()) return -1;
		double length = BossFootprint.Shapes.planar(frame.origin(), frame.anchors().get(index));
		return (int) Math.ceil(length / VelorumMove.JAVELIN_SPEED);
	}

	/** A shaft at its end: lodged in the floor, or shattered harmlessly on rime and stone. */
	public void javelinLanded(FootprintFrame frame, int index) {
		if (index >= frame.anchors().size()) return;
		Vec3 end = frame.anchors().get(index);
		if (index < javelinCut.length && javelinCut[index]) VelorumMoves.sound(this, end, SoundEvents.GLASS_HIT, 1.0F, 0.8F);
		else VelorumMoves.sound(this, end, SoundEvents.GLASS_BREAK, 0.6F, 0.6F);
	}

	public boolean javelinCut(int index) {
		return index >= 0 && index < javelinCut.length && javelinCut[index];
	}

	/**
	 * Veins of Frost's veins, re-laid every 3 ticks until tick 18: {@code points[0]} is where they meet (the target),
	 * then each vein from there out to its figure along the block seams ({@link VelorumVisualRules#veinPath}) and back,
	 * the last not retraced; {@code anchors} are the feeding figures.
	 */
	public void layVeins(SaintMoveContext ctx, boolean flare) {
		SaintArena arena = arena();
		if (arena == null) return;
		Vec3 toward = ctx.target() == null ? ctx.frame().aim() : ctx.target().position();
		List<Vec3> feeding = congregation().feeding(this, toward, VelorumSelectionRules.VEIN_REACH);
		if (flare) congregation().flareVeins(this, feeding);
		double floor = arena.floorY();
		Vec3 meet = new Vec3(toward.x, floor, toward.z);
		List<Vec3> points = new ArrayList<>();
		points.add(meet);
		for (int i = 0; i < feeding.size(); i++) {
			Vec3 figure = feeding.get(i);
			double[] path = VelorumVisualRules.veinPath(figure.x, figure.z, meet.x, meet.z, i);
			List<Vec3> out = new ArrayList<>();
			for (int k = path.length - 2; k >= 0; k -= 2) out.add(new Vec3(path[k], floor, path[k + 1]));
			for (int k = 1; k < out.size(); k++) points.add(out.get(k));
			if (i + 1 < feeding.size()) for (int k = out.size() - 2; k >= 0; k--) points.add(out.get(k));
			controller().setAnchor(i, new Vec3(figure.x, floor, figure.z));
		}
		controller().setPoints(points);
	}

	/**
	 * Where the Shadow Step rises for {@code target}: a leash cell 4 to 6 from them, beside or behind them, within 1.5
	 * of a frozen or stilled figure; if none qualifies, any such cell; null when none at all.
	 */
	@Nullable
	public Vec3 stepDestination(LivingEntity target) {
		SaintArena arena = arena();
		if (arena == null) return null;
		AABB leash = VelorumDark.leash(arena);
		List<Vec3> figures = congregation().standingFigures(this);
		for (boolean nearFigure : new boolean[]{true, false}) {
			List<Vec3> cells = new ArrayList<>();
			for (double x = Math.floor(leash.minX) + 0.5D; x < leash.maxX; x += 1.0D) {
				for (double z = Math.floor(leash.minZ) + 0.5D; z < leash.maxZ; z += 1.0D) {
					double figure = Double.MAX_VALUE;
					for (Vec3 at : figures) figure = Math.min(figure, Math.hypot(at.x - x, at.z - z));
					if (VelorumLocomotionRules.stepCell(x, z, target.getX(), target.getZ(), getX(), getZ(), leash.minX,
							leash.minZ, leash.maxX, leash.maxZ, figure, nearFigure)) {
						cells.add(new Vec3(x, arena.floorY(), z));
					}
				}
			}
			if (!cells.isEmpty()) return cells.get(random.nextInt(cells.size()));
		}
		return null;
	}

	/** The Shadow Step's beats: the destination at once, a pool at each end, the sink, the move, the rise. */
	public void tickStep(SaintMoveContext ctx) {
		int tick = ctx.tick();
		int commit = controller().timeline().commitTick();
		if (tick == 0) {
			stepFrom = position();
			stepTo = ctx.target() == null ? null : stepDestination(ctx.target());
			if (stepTo == null) stepTo = position();
			controller().setPoints(List.of(stepTo));
		}
		int phase = ctx.inputs().phase();
		double cap = VelorumDarkRules.cap(VelorumDarkRules.Source.STEP, phase);
		int life = VelorumDarkRules.life(VelorumDarkRules.Source.STEP, phase);
		if (tick == VelorumLocomotionRules.STEP_POOL_DEPART && stepFrom != null) dark().pool(this, stepFrom, cap, life);
		if (tick == VelorumLocomotionRules.STEP_POOL_ARRIVE && stepTo != null) dark().pool(this, stepTo, cap, life);
		if (tick == VelorumLocomotionRules.STEP_SINK && stepFrom != null) {
			VelorumEffects.stepSink(this, stepFrom);
			VelorumMoves.sound(ctx, SoundInit.ENTITY_WILL_DISSOLVE.get(), 1.0F, 0.5F);
		}
		// moved on the tick before the commit, so the commit's frame stands where he rose
		if (tick == commit - 1 && stepTo != null) teleportTo(stepTo.x, stepTo.y, stepTo.z);
		if (ctx.committedNow()) {
			VelorumEffects.stepRise(this, position());
			VelorumMoves.sound(ctx, SoundInit.ENTITY_WILL_MATERIALIZE.get(), 1.0F, 0.5F);
			VelorumMoves.sound(ctx, SoundInit.RITE_SUBJECT_RISE.get(), 1.0F, 0.6F);
		}
		if (ctx.age() == 1) VelorumMoves.sound(ctx, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 0.5F);
	}

	/** Under the dark: in the Shadow Step from tick 14 to four past its commit, or through the phase-3 card's step. */
	public boolean stepUnder() {
		if (controller().move() == VelorumMove.UMBRAL_STEP
				&& VelorumLocomotionRules.under(controller().moveTick(), controller().timeline().commitTick())) return true;
		return cardStep >= 0 && VelorumLocomotionRules.underInCard(cardStep);
	}

	public boolean cardStepping() {
		return cardStep >= 0;
	}

	/** The phase-3 card's step: he sinks at the end of the Bow and rises on the ring, with no arrival ring. */
	private void tickCardStep() {
		if (cardStep < 0) return;
		if (cardStep == 0) stepFrom = position();
		int phase = phases().phase();
		double cap = VelorumDarkRules.cap(VelorumDarkRules.Source.STEP, phase);
		int life = VelorumDarkRules.life(VelorumDarkRules.Source.STEP, phase);
		if (cardStep == VelorumLocomotionRules.STEP_POOL_DEPART && stepFrom != null) dark().pool(this, stepFrom, cap, life);
		if (cardStep == VelorumLocomotionRules.STEP_SINK && stepFrom != null) {
			VelorumEffects.stepSink(this, stepFrom);
			VelorumMoves.sound(this, stepFrom, SoundInit.ENTITY_WILL_DISSOLVE.get(), 1.0F, 0.5F);
		}
		if (cardStep == VelorumLocomotionRules.CARD_MOVE && ring != null) {
			teleportTo(ring.x, ring.y, ring.z);
			dark().pool(this, ring, cap, life);
			VelorumEffects.stepRise(this, ring);
			VelorumMoves.sound(this, ring, SoundInit.ENTITY_WILL_MATERIALIZE.get(), 1.0F, 0.5F);
			VelorumMoves.sound(this, ring, SoundInit.RITE_SUBJECT_RISE.get(), 1.0F, 0.6F);
		}
		if (++cardStep > VelorumLocomotionRules.CARD_SURFACED) cardStep = -1;
	}

	// ── the Eclipse ──────────────────────────────────────────────────

	/** The Eclipse's state trigger (revision R.7), beside the Hour's: started at the next boundary once the gate holds. */
	private void tickEclipseGate(ServerLevel server) {
		long now = server.getGameTime();
		if (fightTicks % 10 == 0) coverage = dark().coverage(this);
		if (phases().phase() < 3 || queued != null || eclipseHolding || hourHolding || hourArmed) return;
		SaintMove move = controller().move();
		if (move == VelorumMove.ECLIPSE || move == VelorumMove.ENDLESS_HOUR) return;
		float health = getHealth() / Math.max(1.0F, getMaxHealth());
		if (!VelorumEclipseRules.gate(phases().phase(), hourEverEnded, now - hourEndedAt, now - eclipseEndedAt, coverage,
				health)) return;
		if (phases().cardTicks() > 0 || cardStep >= 0 || !planted() || openings().isOpen()) return;
		queue(VelorumMove.ECLIPSE);
	}

	/** One forced start at a time: the controller keeps a single queued move, so a second would replace the first. */
	private void queue(SaintMove move) {
		queued = move;
		controller().start(move, com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController.Cut.QUEUE);
	}

	/** The fingers begin to close: every lantern in the chamber goes out for the wind-up and the hold. */
	public void eclipseOnset(SaintMoveContext ctx) {
		eclipseRelit = false;
		eclipseFed = 0.0F;
		eclipseIdle = 0;
		BossMoveTimeline timeline = controller().timeline();
		congregation().darkenEverything(this, timeline.windup() + timeline.active() + 1);
		VelorumEffects.eclipseOut(this);
		VelorumMoves.sound(ctx, SoundInit.RITE_SUBJECT_SINK.get(), 1.0F, 0.5F);
		for (Player player : ctx.arena().challengers()) SaintLines.teach(player, "message.hemomancy.velorum.hint.eclipse");
	}

	/** The hold begins: the chamber's dark, and a shape out of every frozen, stilled and pinned figure. */
	public void beginEclipse(SaintMoveContext ctx) {
		eclipseHolding = true;
		int hold = controller().timeline().active();
		dark().eclipse(this, hold);
		congregation().beginEclipse(this, hold);
	}

	/** One tick of the hold: the shapes reach, and an unfed relight bleeds back at half speed after its grace. */
	public void tickEclipse(int holdAge) {
		congregation().tickEclipse(this, holdAge);
		if (eclipseFed > 0.0F) {
			eclipseIdle++;
			eclipseFed = VelorumCongregationRules.feed(eclipseFed, false, eclipseIdle);
		}
	}

	/** Unbroken, the light seeps back: everyone at 60 or more is preserved, the rest take 6, the Gutter is half. */
	public void closeEclipse() {
		if (!eclipseHolding) return;
		endEclipse(VelorumEclipseRules.ARM_HOLD * 2);
		VelorumMoves.sound(this, position(), SoundInit.RITE_COLLAPSE.get(), 1.0F, 0.6F);
		if (arena() == null) return;
		for (Player player : arena().challengers()) {
			if (stillness().value(this, player) >= VelorumEclipseRules.PRESERVES_AT) {
				stillness().add(this, player, VelorumStillnessRules.MAX);
			} else {
				VelorumMoves.hurt(this, player, VelorumEclipseRules.UNBROKEN_DAMAGE);
			}
		}
	}

	/** Relit by held Projection: the light bursts through his fingers and the dark tears back to the walls. */
	private void relightEclipse() {
		eclipseRelit = true;
		endEclipse(VelorumEclipseRules.ARM_HOLD);
		dark().tearBack(this, VelorumEclipseRules.ARM_HOLD);
		VelorumEffects.eclipseRelit(this);
		VelorumMoves.sound(this, position(), SoundInit.RITE_SIGIL_AWAKEN.get(), 1.0F, 0.7F);
		controller().skipToRecovery();
	}

	private void endEclipse(int fade) {
		eclipseHolding = false;
		eclipseEndedAt = level().getGameTime();
		congregation().endEclipse(this, fade);
		congregation().relightEverything(this);
		dark().endEclipse(this, fade);
		reachStruck.clear();
	}

	public boolean eclipseHolding() {
		return eclipseHolding;
	}

	public boolean eclipseRelit() {
		return eclipseRelit;
	}

	public boolean eclipsePending() {
		return queued == VelorumMove.ECLIPSE;
	}

	/** Held Projection into his closed claw in the Eclipse's hold: the lesser of 3 and the item's rate, a fed tick. */
	private SaintChannelRouter.Result kindle(SaintChannelRouter.Request request) {
		double amount = Math.min(VelorumEclipseRules.KINDLE_RATE, request.ratePerTick());
		double paid = SaintChannelRouter.pay(request, amount, VelorumMoves.KINDLING).paid();
		if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
		eclipseFed = VelorumCongregationRules.feed(eclipseFed, true, 0);
		eclipseIdle = 0;
		if ((int) eclipseFed % 4 == 1) VelorumMoves.sound(this, lanternPoint(), SoundInit.RITE_ANCHOR_FILL.get(), 0.8F, 0.6F);
		if (VelorumEclipseRules.relit(eclipseFed, VelorumDeedRules.litany(deeds().mask()))) relightEclipse();
		return SaintChannelRouter.Result.taken(paid);
	}

	public void gaze(Player player) {
		gazed.add(player.getUUID());
	}

	/** The challengers the gaze held this move. */
	public List<Player> gazed() {
		List<Player> players = new ArrayList<>();
		if (arena() == null) return players;
		for (Player player : arena().challengers()) if (gazed.contains(player.getUUID())) players.add(player);
		return players;
	}

	/** One tick of the Draw of Silence on a player: blood from the blood-active, a little health from the rest. */
	public void drawSilence(SaintMoveContext ctx, Player player) {
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume.isActive()) {
			float drawn = silenceDrawn.getOrDefault(player.getUUID(), 0.0F);
			float amount = Math.min(SILENCE_RATE, SILENCE_CAP - drawn);
			if (amount <= 0.0F) return;
			BloodAccounting.Payment paid = BloodAccounting.pay(player, amount, VelorumMoves.SILENCE);
			silenceDrawn.put(player.getUUID(), drawn + (float) paid.paid());
		} else if (VelorumMoves.activeTick(ctx) % SILENCE_HURT_EVERY == 0) {
			SaintMoves.hurt(this, player, SILENCE_HURT, null);
		}
	}

	public void beginRecall() {
		VelorumDevoteeEntity devotee = congregation().recallable(this);
		recallTarget = devotee == null ? null : devotee.getUUID();
	}

	@Nullable
	private VelorumDevoteeEntity recalled() {
		if (recallTarget == null || !(level() instanceof ServerLevel server)) return null;
		return server.getEntity(recallTarget) instanceof VelorumDevoteeEntity devotee && devotee.isAlive() ? devotee : null;
	}

	/** Whether Recall's line still holds: the ally awake, in range and in his sight. */
	public boolean holdRecall() {
		VelorumDevoteeEntity devotee = recalled();
		return devotee != null && devotee.awake() && devotee.distanceTo(this) <= VelorumCongregationRules.RECALL_RANGE
				&& hasLineOfSight(devotee);
	}

	public void finishRecall() {
		VelorumDevoteeEntity devotee = recalled();
		if (devotee != null) congregation().recall(this, devotee);
		recallTarget = null;
	}

	/** The point his lantern burns at, held high: the Long Night's sight test reads it. */
	public Vec3 lanternPoint() {
		return position().add(0.0D, HEIGHT + 0.4D, 0.0D);
	}

	/** Whether a block ray from {@code from} reaches the player's eyes. */
	public boolean sees(Vec3 from, Player player) {
		return level().clip(new ClipContext(from, player.getEyePosition(), ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
	}

	/** Two points on the boss floor away from the challengers, rerolled at the move's start and kept at its lock. */
	public List<Vec3> scatteredPoints(int count, boolean reroll) {
		if (reroll || scattered.size() != count) {
			List<Vec3> points = new ArrayList<>();
			AABB floor = arena() == null ? getBoundingBox().inflate(7.0D) : arena().bossFloor();
			for (int i = 0; i < count; i++) {
				double x = floor.minX + 1.0D + random.nextDouble() * Math.max(0.0D, floor.getXsize() - 2.0D);
				double z = floor.minZ + 1.0D + random.nextDouble() * Math.max(0.0D, floor.getZsize() - 2.0D);
				points.add(new Vec3(x, arena() == null ? getY() : arena().floorY(), z));
			}
			scattered = List.copyOf(points);
		}
		return scattered;
	}

	/** The nearest challenger to a point, for a hostile devotee's hunt. */
	@Nullable
	public Player nearestChallenger(Vec3 from) {
		if (arena() == null) return null;
		Player best = null;
		double distance = Double.MAX_VALUE;
		for (Player player : arena().challengers()) {
			double d = player.position().distanceToSqr(from);
			if (d < distance) {
				distance = d;
				best = player;
			}
		}
		return best;
	}

	/** He keeps his target 60 ticks unless it leaves sight and range; otherwise the stillest challenger in sight. */
	@Nullable
	public LivingEntity chooseTarget(List<Player> challengers, @Nullable LivingEntity current) {
		if (current instanceof Player player && challengers.contains(player) && player.isAlive()) {
			targetHeld++;
			boolean sight = hasLineOfSight(player) || distanceTo(player) <= TARGET_RANGE;
			if (targetHeld < TARGET_KEEP || sight) return player;
		}
		targetHeld = 0;
		Player best = null;
		float bestStill = -1.0F;
		double bestDistance = Double.MAX_VALUE;
		for (Player player : challengers) {
			if (!hasLineOfSight(player)) continue;
			float still = stillness().value(this, player);
			double distance = distanceToSqr(player);
			if (still > bestStill + 1.0E-3F || Math.abs(still - bestStill) <= 1.0E-3F && distance < bestDistance) {
				best = player;
				bestStill = still;
				bestDistance = distance;
			}
		}
		if (best != null) return best;
		return com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController.defaultTarget(this, challengers);
	}

	public int lessonLeft() {
		return lessonLeft;
	}

	public int settleLeft() {
		return settleLeft;
	}

	public int farTicks() {
		return farTicks;
	}

	public boolean gripped() {
		return congregation().gripping();
	}

	@Nullable
	public Vec3 ringCentre() {
		return ring;
	}

	public boolean planted() {
		return ring == null || Math.hypot(getX() - ring.x, getZ() - ring.z) <= VelorumLocomotionRules.PLANTED_REACH + 0.2D;
	}

	/** On the boss floor or the block round it (not yet on the dais). */
	public boolean onFloor() {
		if (arena() == null) return true;
		AABB floor = arena().bossFloor().inflate(1.0D, 0.0D, 1.0D);
		return getX() >= floor.minX && getX() <= floor.maxX && getZ() >= floor.minZ && getZ() <= floor.maxZ
				&& getY() <= arena().floorY() + 0.5D;
	}

	/** A tick of the glide: the hem's frost whispers every 12 ticks of travel. */
	public void glided() {
		if (++glideTicks % GLIDE_STEP_TICKS == 0) {
			VelorumMoves.sound(this, position(), SoundEvents.POWDER_SNOW_STEP, 0.3F, 0.6F);
		}
	}

	public void firstPreservation() {
		if (spokePreserved) return;
		spokePreserved = true;
		SaintLines.voice(this, LINE_PRESERVED);
	}

	public void firstWoken() {
		if (phases().phase() >= 2 && !spokeCongregation) {
			spokeCongregation = true;
			SaintLines.voice(this, LINE_CONGREGATION);
		}
	}

	// ── held channels ────────────────────────────────────────────────

	@Override
	protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) {
		if (request.kind() == SaintChannelRouter.Kind.PROJECT) {
			Player warmed = aimedPreserved(request);
			if (warmed != null) return thaw(request, warmed);
			Player marked = aimedMarked(request);
			if (marked != null) return warmMark(request, marked);
			if (aimsAtBody(request)) return eclipseHolding ? kindle(request) : warm(request);
			return congregation().project(this, request);
		}
		if (aimsAtBody(request)) return seam(request);
		return SaintChannelRouter.Result.PASS;
	}

	private boolean aimsAtBody(SaintChannelRouter.Request request) {
		if (request.entity() != null && request.entity().getEntity() == this) return true;
		return SaintChannelRouter.aimsAt(request, getBoundingBox().getCenter(), request.reach() + WIDTH, 20.0D, true);
	}

	@Nullable
	private Player aimedPreserved(SaintChannelRouter.Request request) {
		if (request.entity() == null || !(request.entity().getEntity() instanceof Player target)) return null;
		if (target == request.player() || !stillness().preserved(target)) return null;
		return target;
	}

	@Nullable
	private Player aimedMarked(SaintChannelRouter.Request request) {
		if (request.entity() == null || !(request.entity().getEntity() instanceof Player target)) return null;
		if (target == request.player() || !stillness().marked(target)) return null;
		return target;
	}

	/** Another challenger's held Projection on a marked one: 10 fed ticks warm Arrested Pulse out of them. */
	private SaintChannelRouter.Result warmMark(SaintChannelRouter.Request request, Player target) {
		double amount = Math.min(VelorumStillnessRules.WARM_RATE, request.ratePerTick());
		double paid = SaintChannelRouter.pay(request, amount, VelorumMoves.THAWING).paid();
		if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
		stillness().warmMark(this, target);
		return SaintChannelRouter.Result.taken(paid);
	}

	/** Held Projection into him in the Guttering Lantern: 4 blood, 1 guard-free damage, a tick added per four fed. */
	private SaintChannelRouter.Result warm(SaintChannelRouter.Request request) {
		SaintOpening.Spec open = openings().current();
		if (open == null || open.id() != VelorumOpeningRules.GUTTER_ID) {
			return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.cold_refuses"));
		}
		double amount = Math.min(VelorumOpeningRules.WARMING_RATE, request.ratePerTick());
		double paid = SaintChannelRouter.pay(request, amount, VelorumMoves.WARMING).paid();
		if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
		gutterFed++;
		if (!VelorumDeedRules.litany(deeds().mask()) && gutterFed % VelorumOpeningRules.FED_TICKS_PER_EXTENSION == 0) {
			openings().extend(1, VelorumOpeningRules.PROJECTION_EXTENSION_CAP + VelorumOpeningRules.SEARING_EXTENSION);
		}
		DamageSource source = damageSources().source(SaintDamageTypes.VELORUM, request.player());
		// a held channel lands every tick, so the blow before it grants no grace
		invulnerableTime = 0;
		hurtByChannel(source, VelorumOpeningRules.WARMING_DAMAGE);
		return SaintChannelRouter.Result.taken(paid);
	}

	/** Held Absorption on the Bow's seam: the cold blood drawn out, 1.5 damage a tick, 2 blood to the absorber. */
	private SaintChannelRouter.Result seam(SaintChannelRouter.Request request) {
		if (!bowOpen()) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.seam_closed"));
		seamAt = level().getGameTime();
		DamageSource source = damageSources().source(SaintDamageTypes.VELORUM, request.player());
		// a held channel lands every tick, so the blow before it grants no grace
		invulnerableTime = 0;
		hurtByChannel(source, VelorumOpeningRules.SEAM_DAMAGE);
		double credited = SaintChannelRouter.credit(request.player(),
				Math.min(VelorumOpeningRules.SEAM_CREDIT, request.ratePerTick()));
		return SaintChannelRouter.Result.taken(credited);
	}

	/** Another challenger's held Projection warming a preserved one out of the ice. */
	private SaintChannelRouter.Result thaw(SaintChannelRouter.Request request, Player target) {
		double amount = Math.min(VelorumStillnessRules.WARM_RATE, request.ratePerTick());
		double paid = SaintChannelRouter.pay(request, amount, VelorumMoves.THAWING).paid();
		if (paid <= 0.0D) return SaintChannelRouter.Result.refused(Component.translatable("message.hemomancy.velorum.no_blood"));
		stillness().warm(target);
		return SaintChannelRouter.Result.taken(paid);
	}

	// ── the ending ───────────────────────────────────────────────────

	/** His body fails: only ever from a blow inside an opening (or the Hour's hold). */
	private void fail(@Nullable Player victor) {
		beginEnding(VelorumEndingRules.SPEC, victor,
				() -> VelorumEndingRules.done(endingTick(), congregation().everyoneHome(this)));
	}

	@Override
	protected boolean endingDoneAfterLoad() {
		return VelorumEndingRules.done(endingTick(), congregation().everyoneHome(this));
	}

	@Override
	protected void onEndingTick(int tick) {
		if (!(level() instanceof ServerLevel)) return;
		SaintArena arena = arena();
		List<Player> challengers = arena == null ? List.of() : arena.challengers();
		if (tick == 0) {
			stillness().releaseAll(this, challengers);
			hourHolding = false;
			cardStep = -1;
			if (eclipseHolding) endEclipse(VelorumEclipseRules.ARM_HOLD);
			dark().fadeAll(this, VelorumDarkRules.EXPIRE_TICKS);
		}
		if (tick == VelorumEndingRules.HAND_LOWERED) VelorumMoves.sound(this, position(), SoundInit.RITE_COLLAPSE.get(), 1.0F, 0.6F);
		for (int chime : VelorumEndingRules.CHIMES) {
			if (tick == chime) VelorumMoves.sound(this, position(), SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.6F);
		}
		if (tick == VelorumEndingRules.METERS_DRAIN_FROM) {
			VelorumEffects.rest(this);
			congregation().relight(this);
			for (Player player : challengers) {
				SaintRoot.release(player, STILL_FLOOR_ROOT);
				SaintRoot.release(player, ECLIPSE_ROOT);
			}
		}
		if (tick >= VelorumEndingRules.METERS_DRAIN_FROM && tick <= VelorumEndingRules.METERS_DRAIN_TO) {
			stillness().drainForEnding(this, challengers, tick);
		}
		if (tick == VelorumEndingRules.DEVOTEES_TURN) {
			congregation().sendHome(this);
			broadcast(Component.translatable("message.hemomancy.velorum.congregation_returns")
					.withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
		}
		if (tick == VelorumEndingRules.LENGTH - 1) congregation().burstStragglers(this);
	}

	/** Felled, he goes still again; the tomb closes over him after. */
	@Override
	protected void announceTrialEnd(@Nullable Player victor) {
		broadcastMessage("message.hemomancy.velorum.returns_to_stillness", ChatFormatting.DARK_AQUA);
	}

	// ── sounds ───────────────────────────────────────────────────────

	/** He is silent; his creak is his own tick's, rare and low. */
	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return openings().isOpen() ? SoundEvents.GLASS_BREAK : SoundEvents.GLASS_HIT;
	}

	@Override
	public float getVoicePitch() {
		return 0.65F;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GLASS_BREAK;
	}

	// ── save ─────────────────────────────────────────────────────────

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		CompoundTag own = new CompoundTag();
		own.putInt("Passed", passed);
		own.putInt("FloorBow", floorBowAt);
		own.putInt("Bowed", bowedFloors);
		own.putBoolean("HourArmed", hourArmed);
		own.putBoolean("SpokeRise", spokeRise);
		own.putBoolean("SpokePreserved", spokePreserved);
		own.putBoolean("SpokeCongregation", spokeCongregation);
		tag.put("Velorum", own);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		CompoundTag own = tag.getCompound("Velorum");
		passed = own.getInt("Passed");
		floorBowAt = own.contains("FloorBow") ? own.getInt("FloorBow") : -1;
		bowedFloors = own.getInt("Bowed");
		hourArmed = own.getBoolean("HourArmed");
		spokeRise = own.getBoolean("SpokeRise");
		spokePreserved = own.getBoolean("SpokePreserved");
		spokeCongregation = own.getBoolean("SpokeCongregation");
	}
}
