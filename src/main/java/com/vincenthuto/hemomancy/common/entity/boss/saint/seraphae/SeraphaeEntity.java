package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossGroundHazardEntity;
import com.vincenthuto.hemomancy.common.entity.boss.BossHold;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintArena;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChamberLayout;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintChannelRouter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCombatController;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintHit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMeter;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMotor;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintRoot;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.util.AttributeModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntUnaryOperator;

/**
 * Saint Seraphae, the Chain Saint, on the Saint combat kit: a containment fight (design
 * {@code docs/superpowers/specs/2026-11-05-saint-seraphae-overhaul-design.md}). Her bar is containment integrity, not
 * health; her health never changes, and only her openings let a blow tighten the binding. Her presence mends the
 * wounded and grows the whole; her reach is her chain, its length set by integrity and by her anchors; her most
 * dangerous move is her kindest. She is contained at full integrity, never killed.
 */
public class SeraphaeEntity extends SaintTrialEntity {
	private static final EntityDataAccessor<Integer> DATA_RESTRAINTS =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_RESTRAINT_PROGRESS =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_LEASH =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Vector3f> DATA_LEASH_CENTRE =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Integer> DATA_GLIDE =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SEIZED =
			SynchedEntityData.defineId(SeraphaeEntity.class, EntityDataSerializers.INT);

	/** {@code DATA_GLIDE}: what her body is doing between moves, for the pose. */
	public static final int GLIDE_IDLE = 0, GLIDE_MOVING = 1, GLIDE_STRAIN = 2, GLIDE_REELED = 3;

	public static final float WIDTH = 1.2F;
	public static final float HEIGHT = 3.2F;
	public static final double HOVER = 0.35D;
	public static final double HOVER_EASE = 0.2D;
	public static final double GLIDE_SPEED = 0.11D;
	public static final double GLIDE_ACCEL = 0.01D;
	public static final double GLIDE_HUG_SPEED = 0.06D;
	public static final double BASE_SPEED = 0.22D;
	public static final double CHEST = 2.0D;
	public static final double FRAGMENT_RING = 2.5D;
	public static final int FRAGMENTS_TWO = 3;
	public static final int FRAGMENTS_THREE = 4;
	public static final int FRAGMENTS_MAX = 8;
	public static final int PAD_LIFE = 100;
	public static final int PAD_PULSE = 20;
	public static final int PAD_KIND = 1;
	public static final int HOLD_TICKS = 60;
	public static final int HOLD_ROOT = 30;
	public static final float HOLD_LASH_CHANCE = 0.7F;
	public static final int HOLD_BREAK_ABSORB = 20;
	public static final int HOLD_BREAK_HITS = 2;
	public static final double CLOSE_STRIKER = 4.0D;
	public static final double THREAD_PULL = 0.6D;
	public static final int THREAD_UNSEEN = 4;
	public static final double GRASP_THROW = 3.0D;
	public static final int GRASP_ROOT = 10;
	public static final int KNOT_ROOT = 10;
	public static final int PAD_ROOT = 30;
	public static final double WRENCH_KNOCKBACK = 0.8D;
	public static final double WRENCH_NEAR_ANCHOR = 5.0D;
	public static final int WRENCH_STRAIN_TICKS = 40;
	public static final int SEIZED_DRAG_TICKS = 6;
	public static final double SEIZED_DRAG = 2.0D;
	public static final int FLOOD_GROWTH = 4;
	public static final int OPENING_QUEUE_LIMIT = 100;
	public static final double BODY_AIM_CONE = 30.0D;
	public static final double FRAGMENT_AIM_CONE = 24.0D;
	public static final int FEEDING_FLAG_TICKS = 3;
	private static final ResourceLocation KNOTTING = Hemomancy.rloc("seraphae_knotting");
	private static final ResourceLocation ROOT = Hemomancy.rloc("seraphae_root");

	// ── server state ─────────────────────────────────────────────────
	private final Map<SeraphaeRestraint, UUID> bindingAnchors = new EnumMap<>(SeraphaeRestraint.class);
	private final Map<SeraphaeRestraint, ContainmentAnchorEntity.State> savedStates = new EnumMap<>(SeraphaeRestraint.class);
	private final List<BlockPos> reserveHomes = new ArrayList<>();
	private final UUID[] reserveAnchors = new UUID[SeraphaeAnchorRules.RESERVE_COUNT];
	private final boolean[] reserveWoken = new boolean[SeraphaeAnchorRules.RESERVE_COUNT];
	private final float[] reserveProgress = new float[SeraphaeAnchorRules.RESERVE_COUNT];
	private final int[] reserveSinceFed = {Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2};
	private final Set<UUID> fragments = new HashSet<>();
	private final Map<UUID, Float> lastHealth = new HashMap<>();
	private final Map<UUID, Float> woundCarry = new HashMap<>();
	private final Map<UUID, Integer> fedTicks = new HashMap<>();
	private final Map<UUID, Integer> feedingFor = new HashMap<>();
	private final Map<UUID, Float> feedEarned = new HashMap<>();
	private final Set<UUID> knotted = new HashSet<>();
	private boolean anchorsWoken;
	private Vec3 leashCentre = Vec3.ZERO;
	private boolean leashCentreSet;
	private double leash = SeraphaeLeashRules.length(SeraphaeIntegrityRules.START, 0, 0);
	private double glideSpeed;
	private boolean firstReachQueued;
	private boolean reachStoppedShort;
	private boolean reachCaught;
	private double reachChain;
	private int lastLashSide;
	private int lastLanceSide;
	private boolean wingCaught;
	@Nullable
	private SeraphaeRestraint wrenchRestraint;
	private int wrenchReserve = -1;
	private boolean wrenchSeized;
	@Nullable
	private Vec3 lastHealPos;
	private int turtleTicks;
	private int strainTicks;
	private int threadUnseen;
	private boolean containedPending;
	private boolean floodArmed = true;
	private boolean fractureOpen;
	private int spreadTurned;
	@Nullable
	private SaintOpening.Spec pendingOpening;
	private int pendingFor;
	@Nullable
	private Vec3 seizedToward;
	private int holdAbsorb;
	private int holdAllyHits;
	private boolean holdStilling;
	@Nullable
	private Vec3 endingFrom;

	public SeraphaeEntity(EntityType<? extends SeraphaeEntity> type, Level level) {
		super(type, level, 120, new BossBarBinding("entity.hemomancy.seraphae", BossPresentationIds.FRAME_SERAPHAE,
				BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10));
		this.setNoGravity(true);
	}

	@Override
	public EnumSaintType saintType() {
		return EnumSaintType.SERAPHAE;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 500.0D)
				.add(Attributes.MOVEMENT_SPEED, BASE_SPEED)
				.add(Attributes.ATTACK_DAMAGE, 4.0D)
				.add(Attributes.FOLLOW_RANGE, 48.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
				.add(Attributes.ARMOR, 20.0D);
	}

	/** Her own hitbox (resolution B-12): about 3.2 blocks tall at render scale 2.0. */
	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return EntityDimensions.scalable(WIDTH, HEIGHT);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_RESTRAINTS, 0);
		builder.define(DATA_RESTRAINT_PROGRESS, 0);
		builder.define(DATA_LEASH, (float) leash);
		builder.define(DATA_LEASH_CENTRE, new Vector3f());
		builder.define(DATA_GLIDE, GLIDE_IDLE);
		builder.define(DATA_SEIZED, 0);
	}

	// ── the kit ──────────────────────────────────────────────────────

	@Override
	protected SaintKit createKit() {
		SaintMeter meter = new SaintMeter(SaintMeter.Owner.SAINT, new IntegrityRule());
		return new SaintKit(List.of(SeraphaeMove.values()), new SeraphaeGlide(), meter, new SeraphaePresence(),
				SeraphaePhases.table(), SaintDamageTypes.SERAPHAE, SaintKit.CHALLENGER_SCALING, SeraphaeEndingRules.SPEC);
	}

	/** Containment integrity: 0 to 100, from 30, decaying by challengers and living fragments. */
	private final class IntegrityRule implements SaintMeter.Rule {
		@Override
		public float max(int challengers) {
			return SeraphaeIntegrityRules.MAX;
		}

		@Override
		public float start(@Nullable SaintTrialEntity saint) {
			return SeraphaeIntegrityRules.START;
		}

		@Override
		public float drift(@Nullable SaintTrialEntity saint, float value, float max) {
			SaintArena arena = arena();
			if (arena == null) return 0.0F;
			return -SeraphaeIntegrityRules.decay(arena.challengerCount(), livingFragments(null));
		}

		@Override
		public float[] thresholds() {
			return SeraphaeIntegrityRules.THRESHOLDS;
		}

		@Override
		public void crossed(@Nullable SaintTrialEntity saint, @Nullable Player player, int threshold, boolean rising) {
			if (threshold == SeraphaeIntegrityRules.CONTAINED_THRESHOLD && rising) containedPending = true;
		}
	}

	/** Her bar is the meter (user ruling). */
	@Override
	protected float barProgress() {
		return meter().fraction();
	}

	/** Rising out of her tomb she takes no touch at all; after that every touch goes to the kit's pipeline. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (rising()) return false;
		return super.hurt(source, amount);
	}

	/** Her health is never the bar: a blow changes nothing on her body, only the binding through an opening. */
	@Override
	protected float damageTakenFactor(SaintHit hit) {
		return 0.0F;
	}

	// ── synced reads ─────────────────────────────────────────────────

	public int syncedRestraints() {
		return entityData.get(DATA_RESTRAINTS);
	}

	public int syncedRestraintProgress() {
		return entityData.get(DATA_RESTRAINT_PROGRESS);
	}

	public float syncedLeash() {
		return entityData.get(DATA_LEASH);
	}

	public Vec3 syncedLeashCentre() {
		Vector3f v = entityData.get(DATA_LEASH_CENTRE);
		return new Vec3(v.x(), v.y(), v.z());
	}

	public int syncedGlide() {
		return entityData.get(DATA_GLIDE);
	}

	/** The restraint a Seized opening drags toward, or null. */
	@Nullable
	public SeraphaeRestraint syncedSeized() {
		int id = entityData.get(DATA_SEIZED);
		return id <= 0 ? null : SeraphaeRestraint.byId(id - 1);
	}

	// ── arena: anchors and reserves ──────────────────────────────────

	@Override
	protected void onArenaResolved(SaintArena arena) {
		reserveHomes.clear();
		BlockPos centre = arena.chamberFound() ? arena.tomb() : blockPosition();
		List<BlockPos> dormant = arena.chamberFound()
				? arena.find("seraphae_anchors", ContainmentAnchorEntity::dormantLower) : List.of();
		if (arena.chamberFound()) reserveHomes.addAll(SeraphaeAnchorRules.reserves(centre, dormant));
		if (!anchorsWoken) {
			anchorsWoken = true;
			wakeBindingAnchors(arena, centre, dormant, Map.of());
		}
	}

	/** A load restored the chamber and discarded her anchors: stand them up again as they were. */
	@Override
	protected void onArenaReloaded() {
		SaintArena arena = arena();
		if (arena == null) return;
		bindingAnchors.clear();
		fragments.clear();
		BlockPos centre = arena.chamberFound() ? arena.tomb() : blockPosition();
		List<BlockPos> dormant = arena.chamberFound()
				? arena.find("seraphae_anchors", ContainmentAnchorEntity::dormantLower) : List.of();
		wakeBindingAnchors(arena, centre, dormant, new EnumMap<>(savedStates));
		for (int i = 0; i < reserveAnchors.length; i++) {
			reserveAnchors[i] = null;
			if (reserveWoken[i] && i < reserveHomes.size()) wakeReserve(arena, i, false);
		}
		int turned = spreadTurned;
		spreadTurned = 0;
		spread(turned);
	}

	private void wakeBindingAnchors(SaintArena arena, BlockPos centre, List<BlockPos> dormant,
			Map<SeraphaeRestraint, ContainmentAnchorEntity.State> states) {
		if (!(level() instanceof ServerLevel server)) return;
		List<SeraphaeAnchorRules.Placement> placements = SeraphaeAnchorRules.placements(centre, position(),
				dormant.stream().filter(pos -> SeraphaeAnchorRules.inReach(centre, pos)).toList());
		List<Vec3> homes = new ArrayList<>();
		for (int i = 0; i < placements.size(); i++) {
			SeraphaeAnchorRules.Placement placement = placements.get(i);
			SeraphaeRestraint restraint = restraintFor(arena, placement, i);
			ContainmentAnchorEntity anchor = EntityInit.containment_anchor.get().create(server);
			if (anchor == null) continue;
			Vec3 at = placement.position();
			anchor.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
			anchor.setup(getUUID(), ContainmentAnchorEntity.Kind.BINDING, restraint, placement.home());
			if (!server.addFreshEntity(anchor)) continue;
			arena.own(anchor, placement.home());
			if (placement.home() != null) arena.clearColumn(placement.home(), 2);
			bindingAnchors.put(restraint, anchor.getUUID());
			ContainmentAnchorEntity.State state = states.get(restraint);
			if (state != null && state != ContainmentAnchorEntity.State.HELD) anchor.slip();
			homes.add(at);
			SeraphaeEffects.cue(server, position().add(0.0D, CHEST, 0.0D), anchor.crown(), SeraphaeVisualRules.WAKE, 0.8F);
		}
		if (!leashCentreSet) {
			leashCentreSet = true;
			leashCentre = homes.isEmpty() || !arena.chamberFound() ? position() : centroid(homes);
		}
		SeraphaeEffects.sound(server, position(), SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.2F, 1.3F);
	}

	/** Which iron an anchor holds: in her chamber by where it stands; on the fallback ring by slot. */
	private static SeraphaeRestraint restraintFor(SaintArena arena, SeraphaeAnchorRules.Placement placement, int slot) {
		if (!arena.chamberFound() || placement.home() == null) {
			return SeraphaeRestraint.BINDING[Math.floorMod(slot, SeraphaeRestraint.BINDING.length)];
		}
		Vec3 local = arena.toLocal(placement.position());
		return SeraphaeRestraint.forAnchor(local.x > SaintChamberLayout.TOMB_X, local.z < SaintChamberLayout.ZC + 0.5D);
	}

	private static Vec3 centroid(List<Vec3> points) {
		double x = 0, y = 0, z = 0;
		for (Vec3 p : points) {
			x += p.x;
			y += p.y;
			z += p.z;
		}
		return new Vec3(x / points.size(), y / points.size(), z / points.size());
	}

	private void wakeReserve(SaintArena arena, int index, boolean first) {
		if (!(level() instanceof ServerLevel server) || index >= reserveHomes.size()) return;
		BlockPos home = reserveHomes.get(index);
		ContainmentAnchorEntity anchor = EntityInit.containment_anchor.get().create(server);
		if (anchor == null) return;
		Vec3 at = Vec3.atBottomCenterOf(home);
		anchor.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
		anchor.setup(getUUID(), ContainmentAnchorEntity.Kind.RESERVE, SeraphaeRestraint.WAIST, home);
		if (!server.addFreshEntity(anchor)) return;
		arena.own(anchor, home);
		arena.clearColumn(home, 2);
		reserveAnchors[index] = anchor.getUUID();
		reserveWoken[index] = true;
		reserveProgress[index] = 0.0F;
		anchor.flare(ContainmentAnchorEntity.FLARE_TICKS);
		if (first) {
			SeraphaeEffects.cue(server, anchor.crown(), position().add(0.0D, 1.2D, 0.0D), SeraphaeVisualRules.WAKE, 0.9F);
			SeraphaeEffects.sound(server, at, SoundInit.RITE_SIGIL_AWAKEN, 1.0F, 1.0F);
			SeraphaeEffects.sound(server, at, SoundEvents.RESPAWN_ANCHOR_CHARGE, 1.0F, 1.3F);
		}
	}

	@Nullable
	private ContainmentAnchorEntity anchor(@Nullable UUID id) {
		if (id == null || !(level() instanceof ServerLevel server)) return null;
		return server.getEntity(id) instanceof ContainmentAnchorEntity anchor && anchor.isAlive() ? anchor : null;
	}

	/** Every anchor of hers that stands now: binding anchors first, then woken reserves. */
	public List<ContainmentAnchorEntity> anchors() {
		List<ContainmentAnchorEntity> list = new ArrayList<>();
		for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
			ContainmentAnchorEntity anchor = anchor(bindingAnchors.get(restraint));
			if (anchor != null) list.add(anchor);
		}
		for (UUID id : reserveAnchors) {
			ContainmentAnchorEntity anchor = anchor(id);
			if (anchor != null) list.add(anchor);
		}
		return list;
	}

	private List<ContainmentAnchorEntity> heldAnchors() {
		List<ContainmentAnchorEntity> held = new ArrayList<>();
		for (ContainmentAnchorEntity anchor : anchors()) if (anchor.isHeld()) held.add(anchor);
		return held;
	}

	/** Whether an anchor or fragment is one of the ones she knows: a stale copy that loaded late discards itself. */
	public boolean knows(Entity entity) {
		UUID id = entity.getUUID();
		if (fragments.contains(id) || bindingAnchors.containsValue(id)) return true;
		for (UUID reserve : reserveAnchors) if (id.equals(reserve)) return true;
		return false;
	}

	// ── leash ────────────────────────────────────────────────────────

	private void updateLeash() {
		int packed = restraintsPacked();
		leash = SeraphaeLeashRules.length(meter().value(), SeraphaeRestraintState.slipped(packed),
				SeraphaeRestraintState.reservesHeld(packed));
		entityData.set(DATA_LEASH, (float) leash);
		entityData.set(DATA_LEASH_CENTRE, new Vector3f((float) leashCentre.x, (float) leashCentre.y, (float) leashCentre.z));
	}

	private double fromCentre(Vec3 point) {
		double dx = point.x - leashCentre.x, dz = point.z - leashCentre.z;
		return Math.sqrt(dx * dx + dz * dz);
	}

	private boolean reeling() {
		return SeraphaeLeashRules.reeling(fromCentre(position()), leash);
	}

	// ── locomotion (SeraphaeGlide) ───────────────────────────────────

	void glideRoam(SaintMotor motor, @Nullable LivingEntity target) {
		if (bossHold().holding() || ending()) {
			hoverTo(position(), 0.0D);
			entityData.set(DATA_GLIDE, GLIDE_IDLE);
			return;
		}
		if (seizedDrag()) return;
		if (openings().isOpen()) {
			hoverTo(position(), 0.0D);
			return;
		}
		if (reeling()) {
			reel();
			return;
		}
		if (target == null) {
			hoverTo(leashCentre, GLIDE_HUG_SPEED);
			entityData.set(DATA_GLIDE, GLIDE_IDLE);
			return;
		}
		double[] want = SeraphaeLeashRules.desired(getX(), getZ(), target.getX(), target.getZ(), leashCentre.x,
				leashCentre.z, leash);
		boolean straining = SeraphaeLeashRules.straining(fromCentre(target.position()), leash);
		double gap = Math.hypot(target.getX() - getX(), target.getZ() - getZ());
		double top = (gap < BossBands.STANDARD.hug() ? GLIDE_HUG_SPEED : GLIDE_SPEED) * speedFactor();
		Vec3 goal = new Vec3(want[0], getY(), want[1]);
		boolean moving = goal.distanceToSqr(position().x, goal.y, position().z) > 0.04D;
		glideSpeed = moving ? Math.min(top, glideSpeed + GLIDE_ACCEL) : Math.max(0.0D, glideSpeed - GLIDE_ACCEL * 2.0D);
		hoverTo(goal, glideSpeed);
		entityData.set(DATA_GLIDE, straining ? GLIDE_STRAIN : moving ? GLIDE_MOVING : GLIDE_IDLE);
	}

	void glideInMove(SaintMotor motor, SaintMoveContext ctx) {
		if (reeling() && !motor.lunging()) {
			reel();
			return;
		}
		if (motor.lunging()) {
			hoverVertical();
			return;
		}
		hoverTo(position(), 0.0D);
		entityData.set(DATA_GLIDE, GLIDE_IDLE);
	}

	boolean mayPick() {
		if (bossHold().holding() || openings().isOpen() || ending()) return false;
		return SeraphaeLeashRules.mayPickWhileReeled(fromCentre(position()), leash);
	}

	private void reel() {
		Vec3 toward = new Vec3(leashCentre.x - getX(), 0.0D, leashCentre.z - getZ());
		double length = toward.length();
		Vec3 step = length < 1.0E-4D ? Vec3.ZERO : toward.scale(SeraphaeLeashRules.REEL_SPEED / length);
		setDeltaMovement(step.x, hoverDelta(), step.z);
		glideSpeed = 0.0D;
		entityData.set(DATA_GLIDE, GLIDE_REELED);
	}

	private boolean seizedDrag() {
		if (seizedToward == null || !openings().isOpen(SeraphaeOpenings.SEIZED)) return false;
		if (openings().age() < SEIZED_DRAG_TICKS) {
			Vec3 toward = new Vec3(seizedToward.x - getX(), 0.0D, seizedToward.z - getZ());
			double length = toward.length();
			Vec3 step = length < 1.0E-4D ? Vec3.ZERO : toward.scale(SEIZED_DRAG / SEIZED_DRAG_TICKS / length);
			setDeltaMovement(step.x, hoverDelta(), step.z);
		} else {
			hoverTo(position(), 0.0D);
		}
		return true;
	}

	/** Glides horizontally toward {@code goal} at {@code speed} and eases onto her hover height. */
	private void hoverTo(Vec3 goal, double speed) {
		Vec3 flat = new Vec3(goal.x - getX(), 0.0D, goal.z - getZ());
		double length = flat.length();
		Vec3 step = length < 1.0E-4D || speed <= 0.0D ? Vec3.ZERO : flat.scale(Math.min(speed, length) / length);
		setDeltaMovement(step.x, hoverDelta(), step.z);
	}

	private void hoverVertical() {
		Vec3 motion = getDeltaMovement();
		setDeltaMovement(motion.x, hoverDelta(), motion.z);
	}

	/** The vertical step that eases her onto 0.35 blocks over the floor beneath her. */
	private double hoverDelta() {
		return (floorBelow() + HOVER - getY()) * HOVER_EASE;
	}

	private double floorBelow() {
		BlockPos.MutableBlockPos pos = BlockPos.containing(getX(), getY() + 0.5D, getZ()).mutable();
		for (int i = 0; i < 6; i++) {
			BlockState state = level().getBlockState(pos);
			VoxelShape shape = state.getCollisionShape(level(), pos);
			if (!shape.isEmpty()) return pos.getY() + shape.max(Direction.Axis.Y);
			pos.move(Direction.DOWN);
		}
		return getY() - HOVER;
	}

	private double speedFactor() {
		AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
		return speed == null ? 1.0D : speed.getValue() / BASE_SPEED;
	}

	/** Her target: the most hurt challenger in sight within 24 blocks, because she means to heal. */
	@Nullable
	LivingEntity chooseHealTarget(List<Player> challengers) {
		List<SeraphaeSelectionRules.Candidate> candidates = new ArrayList<>();
		for (Player player : challengers) {
			float missing = player.getMaxHealth() <= 0.0F ? 0.0F : 1.0F - player.getHealth() / player.getMaxHealth();
			candidates.add(new SeraphaeSelectionRules.Candidate(player.getId(), missing, distanceTo(player),
					hasLineOfSight(player)));
		}
		int id = SeraphaeSelectionRules.target(candidates);
		for (Player player : challengers) if (player.getId() == id) return player;
		return null;
	}

	@Nullable
	Player nearestChallenger(Vec3 from) {
		SaintArena arena = arena();
		if (arena == null) return null;
		Player best = null;
		double bestDistance = Double.MAX_VALUE;
		for (Player player : arena.challengers()) {
			double d = player.position().distanceToSqr(from);
			if (d < bestDistance && ManipulationCombatHelper.canHarmFor(this, player)) {
				bestDistance = d;
				best = player;
			}
		}
		return best;
	}

	// ── the picker's questions (SeraphaeMove) ────────────────────────

	int adjustWeight(SeraphaeMove move, SaintPickContext ctx, int base) {
		LivingEntity target = ctx.target();
		return switch (move) {
			case LAYING_ON_OF_HANDS -> ctx.band() == BossBands.Band.FAR && target != null
					&& fromCentre(target.position()) > leash + SeraphaeLeashRules.REACH_FROM_FAR ? 0 : base;
			case TRANSFUSION_THREAD -> base + SeraphaeSelectionRules.kiteBonus(ctx.kiteTicks());
			case STRAINING_REACH -> base + SeraphaeSelectionRules.kiteBonus(ctx.kiteTicks());
			case CONVULSION -> base > 0 ? base + SeraphaeSelectionRules.hugBonus(ctx.hugTicks()) : 0;
			case REGROWTH_KNOT, HEALED_GROUND -> base > 0 ? base + SeraphaeSelectionRules.turtleBonus(turtleTicks) : 0;
			case CHAIN_WRENCH -> base > 0 && anyFedRecently() ? 60 : base;
			default -> base;
		};
	}

	boolean eligible(SeraphaeMove move, SaintPickContext ctx) {
		LivingEntity target = ctx.target();
		return switch (move) {
			case STRAINING_REACH -> target != null && strainingReachable(target);
			case CHAIN_WRENCH -> !heldAnchors().isEmpty() && (challengerNearAnchor() || strainTicks >= WRENCH_STRAIN_TICKS);
			case FRACTURE -> livingFragments(null) == 0 && !fractureOpen && ctx.ticksSinceOpening() >= 200;
			case EXTRACTION -> !heldAnchors().isEmpty();
			default -> true;
		};
	}

	private boolean strainingReachable(LivingEntity target) {
		double d = fromCentre(target.position());
		return d > leash + SeraphaeLeashRules.HAND_REACH && d <= leash + SeraphaeLeashRules.STRAINING_REACH;
	}

	private boolean challengerNearAnchor() {
		SaintArena arena = arena();
		if (arena == null) return false;
		for (ContainmentAnchorEntity anchor : heldAnchors()) {
			for (Player player : arena.challengers()) {
				if (player.distanceToSqr(anchor) <= WRENCH_NEAR_ANCHOR * WRENCH_NEAR_ANCHOR) return true;
			}
		}
		return false;
	}

	private boolean anyFedRecently() {
		for (ContainmentAnchorEntity anchor : anchors()) if (anchor.ticksSinceFed() <= 10) return true;
		return false;
	}

	int variantFor(SeraphaeMove move, SaintPickContext ctx, IntUnaryOperator roll) {
		LivingEntity target = ctx.target();
		return switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> {
				boolean hasty = move == SeraphaeMove.LAYING_ON_OF_HANDS && wingCaught;
				wingCaught = false;
				reachChain = leash + (move == SeraphaeMove.STRAINING_REACH ? SeraphaeLeashRules.STRAINING_EXTRA : 0.0D);
				Vec3 to = target == null ? position().add(getLookAngle().scale(4.0D)) : target.position();
				double dx = to.x - getX(), dz = to.z - getZ();
				double distance = Math.sqrt(dx * dx + dz * dz);
				double stop = distance < 1.0E-4D ? 0.0D : SeraphaeLeashRules.chainStop(getX(), getZ(), dx / distance,
						dz / distance, leashCentre.x, leashCentre.z, reachChain);
				yield SeraphaeMove.laneVariant(SeraphaeLeashRules.reachLength(distance, stop), hasty);
			}
			case NERVE_LASH -> {
				lastLashSide = target == null ? 0 : sideOf(target.position());
				yield lastLashSide;
			}
			case COUNTER_LASH -> 1 - lastLashSide;
			case AXON_LANCE -> {
				lastLanceSide = roll.applyAsInt(2);
				yield lastLanceSide;
			}
			case AXON_MIRROR -> 1 - lastLanceSide;
			case CHAIN_WRENCH -> chooseWrench(roll);
			case UNCLOSING_EYE -> roll.applyAsInt(2);
			default -> 0;
		};
	}

	/** 0 when {@code point} lies on her left as she faces, 1 on her right. */
	private int sideOf(Vec3 point) {
		double yaw = Math.toRadians(getYRot());
		double fx = -Math.sin(yaw), fz = Math.cos(yaw);
		double tx = point.x - getX(), tz = point.z - getZ();
		return fx * tz - fz * tx > 0.0D ? 1 : 0;
	}

	/** The anchor a Chain Wrench reaches for: a woken reserve first, then one a challenger stands by, then any. */
	private int chooseWrench(IntUnaryOperator roll) {
		wrenchRestraint = null;
		wrenchReserve = -1;
		for (int i = 0; i < reserveAnchors.length; i++) {
			ContainmentAnchorEntity reserve = anchor(reserveAnchors[i]);
			if (reserve != null && reserve.isHeld()) {
				wrenchReserve = i;
				return SeraphaeRestraintState.RESERVE_SLOT + i;
			}
		}
		List<SeraphaeRestraint> held = new ArrayList<>();
		SaintArena arena = arena();
		for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
			ContainmentAnchorEntity anchor = anchor(bindingAnchors.get(restraint));
			if (anchor == null || !anchor.isHeld()) continue;
			if (arena != null) {
				for (Player player : arena.challengers()) {
					if (player.distanceToSqr(anchor) <= WRENCH_NEAR_ANCHOR * WRENCH_NEAR_ANCHOR) {
						wrenchRestraint = restraint;
						return restraint.ordinal();
					}
				}
			}
			held.add(restraint);
		}
		if (held.isEmpty()) return 0;
		wrenchRestraint = held.get(Math.floorMod(roll.applyAsInt(held.size()), held.size()));
		return wrenchRestraint.ordinal();
	}

	@Nullable
	private ContainmentAnchorEntity wrenchAnchor() {
		if (wrenchReserve >= 0) return anchor(reserveAnchors[wrenchReserve]);
		return wrenchRestraint == null ? null : anchor(bindingAnchors.get(wrenchRestraint));
	}

	@Nullable
	SaintOpening.Spec recoveryOpening(SeraphaeMove move, SaintMoveContext ctx) {
		boolean noRest = SeraphaeDeeds.noRest(ctx.inputs().deedMask());
		return switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> reachStoppedShort && !reachCaught
					? SeraphaeOpenings.recoil(move == SeraphaeMove.STRAINING_REACH, noRest) : null;
			case CHAIN_WRENCH -> {
				if (!wrenchSeized) yield null;
				ContainmentAnchorEntity anchor = wrenchAnchor();
				seize(anchor);
				yield SeraphaeOpenings.SEIZED;
			}
			case UNCLOSING_EYE -> SeraphaeOpenings.LAPSE;
			default -> null;
		};
	}

	private void seize(@Nullable ContainmentAnchorEntity anchor) {
		seizedToward = anchor == null ? null : anchor.position();
		entityData.set(DATA_SEIZED, anchor == null ? 0 : (anchor.kind() == ContainmentAnchorEntity.Kind.RESERVE
				? SeraphaeRestraint.WAIST : anchor.restraint()).ordinal() + 1);
	}

	// ── moves (SeraphaeMove) ─────────────────────────────────────────

	void moveTick(SeraphaeMove move, SaintMoveContext ctx) {
		ServerLevel level = ctx.level();
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		int tick = ctx.tick();
		boolean recoveryStart = ctx.stage() == BossMoveTimeline.Stage.RECOVERY
				&& tick == ctx.saint().controller().timeline().windup() + ctx.saint().controller().timeline().active();
		switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> {
				if (tick == 0) {
					reachStoppedShort = false;
					reachCaught = false;
					SeraphaeLines.voice(this, SeraphaeLines.reach(syncedMoveSerial()));
				}
				if (ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick % 6 == 0) {
					float rise = tick / (float) Math.max(1, ctx.saint().controller().timeline().windup());
					SeraphaeEffects.sound(level, chest, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.8F, 0.6F + rise * 0.6F);
				}
				if (ctx.committedNow()) commitReach(move, ctx);
				if (recoveryStart && reachStoppedShort && !reachCaught) recoil();
			}
			case NERVE_LASH, COUNTER_LASH -> {
				if (tick == 0) SeraphaeEffects.sound(level, chest, SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.4F);
				if (ctx.committedNow()) {
					SeraphaeEffects.sound(level, chest, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.3F);
					SeraphaeEffects.sound(level, chest, SoundInit.ENTITY_STAINED_PRIESTESS_FLARE, 0.7F, 1.8F);
					SeraphaeEffects.spark(level, chest.add(getLookAngle().scale(0.4D)), 3);
				}
			}
			case AXON_LANCE, AXON_MIRROR -> {
				if (tick == 0) SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_POWER_SELECT, 0.8F, 1.6F);
				if (ctx.committedNow()) SeraphaeEffects.sound(level, chest, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.2F, 1.4F);
			}
			case REGROWTH_KNOT -> {
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE && ctx.age() % 20 == 0) {
					Vec3 at = ctx.frame().aim();
					SeraphaeEffects.sound(level, at, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.0F, 0.8F);
					SeraphaeEffects.cue(level, at.add(0.0D, 0.1D, 0.0D), SeraphaeVisualRules.PAD,
							(float) SeraphaeMove.KNOT_RADIUS);
				}
			}
			case TRANSFUSION_THREAD -> tickThread(ctx, chest);
			case WING_GRASP -> {
				if (ctx.committedNow()) SeraphaeEffects.sound(level, ctx.frame().aim(), SoundEvents.CHAIN_PLACE, 1.0F, 1.2F);
			}
			case CONVULSION, CONVULSION_WIDE -> {
				for (int at : SeraphaeNerveFlashRules.STUTTERS) {
					if (tick == at && tick < ctx.saint().controller().timeline().windup()) {
						SeraphaeEffects.sound(level, chest, SoundEvents.SCULK_CLICKING, 1.0F, 1.8F);
						SeraphaeEffects.spark(level, chest, 4);
					}
				}
				if (ctx.committedNow()) {
					SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_DEACTIVATE, 1.0F, 1.6F);
					SeraphaeEffects.sound(level, chest, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 0.8F);
				}
			}
			case CHAIN_WRENCH -> tickWrench(ctx);
			case FRACTURE -> {
				if (ctx.committedNow()) fracture(level);
			}
			case HEALED_GROUND -> {
				if (tick == 0) ctx.saint().controller().setPoints(padPoints());
				if (ctx.committedNow()) {
					layPads(level, ctx.frame());
					SeraphaeEffects.sound(level, chest, SoundEvents.BONE_BLOCK_PLACE, 1.0F, 0.6F);
				}
			}
			case UNCLOSING_EYE -> {
				if (tick == 0) SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_ACTIVATE, 1.2F, 0.7F);
				if (ctx.stage() == BossMoveTimeline.Stage.ACTIVE && ctx.age() % 20 == 0) {
					SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_AMBIENT, 1.2F, 1.0F);
				}
				if (recoveryStart) SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_DEACTIVATE, 1.0F, 0.6F);
			}
			case EXTRACTION -> {
				if (tick == 0) {
					List<ContainmentAnchorEntity> held = heldAnchors();
					for (int i = 0; i < Math.min(held.size(), SeraphaeMove.EXTRACTION_CHAINS); i++) {
						ctx.saint().controller().setAnchor(i, held.get(i).crown());
					}
				}
				if (ctx.committedNow() || ctx.stage() == BossMoveTimeline.Stage.ACTIVE && ctx.age() == 10) {
					SeraphaeEffects.sound(level, chest, SoundInit.RITE_ANCHOR_FILL, 1.0F, 0.9F);
				}
			}
			case RADIANCE_FLOOD -> {
				if (tick == 0) SeraphaeLines.voice(this, SeraphaeLines.FLOOD);
				if (ctx.stage() == BossMoveTimeline.Stage.WINDUP && tick % 10 == 0) {
					SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_POWER_SELECT, 1.2F, 0.5F);
				}
				if (ctx.committedNow()) {
					meter().set(SeraphaeIntegrityRules.FLOOD_RESET);
					spread(SeraphaeRadianceSpread.FLOOD);
					SeraphaeEffects.sound(level, chest, SoundInit.RITE_BACKLASH, 2.0F, 0.8F);
					SeraphaeEffects.cue(level, chest, SeraphaeVisualRules.HIT, 6.0F);
				}
			}
		}
	}

	private void commitReach(SeraphaeMove move, SaintMoveContext ctx) {
		ServerLevel level = ctx.level();
		if (move == SeraphaeMove.STRAINING_REACH) tearForStraining(ctx);
		Vec3 aim = ctx.frame().aim();
		double dx = aim.x - getX(), dz = aim.z - getZ();
		double length = Math.sqrt(dx * dx + dz * dz);
		LivingEntity target = ctx.target();
		double targetDistance = target == null ? length : Math.hypot(target.getX() - getX(), target.getZ() - getZ());
		double stop = length < 1.0E-4D ? 0.0D : SeraphaeLeashRules.chainStop(getX(), getZ(), dx / length, dz / length,
				leashCentre.x, leashCentre.z, reachChain);
		reachStoppedShort = SeraphaeLeashRules.stoppedShort(targetDistance, stop);
		double glide = SeraphaeLeashRules.glide(targetDistance, stop);
		if (glide > 0.05D && length > 1.0E-4D) {
			ctx.saint().controller().motor().lunge(new Vec3(dx, 0.0D, dz), glide, SeraphaeLeashRules.REACH_GLIDE_TICKS);
		}
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		SeraphaeEffects.sound(level, chest, SoundInit.ENTITY_STAINED_PRIESTESS_FLARE, 1.0F, 1.6F);
		SeraphaeEffects.spark(level, chest, 5);
	}

	/** The Straining Reach tears one held binding anchor loose first: the one on the target's side, if it holds. */
	private void tearForStraining(SaintMoveContext ctx) {
		LivingEntity target = ctx.target();
		int side = target == null ? 0 : sideOf(target.position());
		SeraphaeRestraint wanted = side == 0 ? SeraphaeRestraint.LEFT_WRIST : SeraphaeRestraint.RIGHT_WRIST;
		ContainmentAnchorEntity anchor = anchor(bindingAnchors.get(wanted));
		if (anchor == null || !anchor.isHeld()) {
			for (ContainmentAnchorEntity candidate : heldAnchors()) {
				if (candidate.kind() == ContainmentAnchorEntity.Kind.BINDING) {
					anchor = candidate;
					break;
				}
			}
		}
		if (anchor != null && anchor.isHeld()) slip(anchor);
	}

	private void recoil() {
		if (!(level() instanceof ServerLevel server)) return;
		SaintMotor motor = controller().motor();
		motor.lunge(getLookAngle().scale(-1.0D), 0.5D, 4);
		SeraphaeEffects.sound(server, position(), SoundEvents.CHAIN_HIT, 1.2F, 0.6F);
		SeraphaeEffects.sound(server, position(), SoundInit.RITE_ANCHOR_FILL, 1.0F, 0.8F);
		for (ContainmentAnchorEntity anchor : heldAnchors()) {
			SeraphaeEffects.cue(server, anchor.crown(), position().add(0.0D, 1.6D, 0.0D), SeraphaeVisualRules.RECOIL, 0.6F);
		}
	}

	private void tickThread(SaintMoveContext ctx, Vec3 chest) {
		ServerLevel level = ctx.level();
		LivingEntity target = ctx.target();
		if (ctx.committedNow()) threadUnseen = 0;
		if (ctx.stage() != BossMoveTimeline.Stage.ACTIVE || target == null) return;
		if (ctx.age() % 20 == 0) SeraphaeEffects.sound(level, chest, SoundEvents.BEACON_AMBIENT, 0.8F, 1.8F);
		boolean seen = SaintMoves.clearSight(ctx, target);
		threadUnseen = seen ? 0 : threadUnseen + 1;
		boolean far = target.distanceTo(this) > SeraphaeMove.THREAD_RANGE;
		if (threadUnseen >= THREAD_UNSEEN || far) {
			SeraphaeEffects.sound(level, target.position().add(0.0D, 1.0D, 0.0D), SoundEvents.AMETHYST_BLOCK_BREAK, 1.0F, 1.5F);
			ctx.saint().controller().skipToRecovery();
		}
	}

	private void tickWrench(SaintMoveContext ctx) {
		ServerLevel level = ctx.level();
		ContainmentAnchorEntity anchor = wrenchAnchor();
		if (ctx.tick() == 0) {
			wrenchSeized = false;
			if (anchor != null) {
				ctx.saint().controller().setAnchor(0, anchor.crown());
				ctx.saint().controller().setPoints(List.of(anchor.position()));
			}
			SeraphaeEffects.sound(level, position(), SoundEvents.CHAIN_PLACE, 1.0F, 0.5F);
		}
		if (ctx.committedNow()) {
			SeraphaeEffects.sound(level, position(), SoundEvents.CHAIN_BREAK, 1.2F, 0.6F);
			if (anchor == null) return;
			if (SeraphaeAnchorBindingRules.heldAtCommit(anchor.ticksSinceFed())) {
				wrenchSeized = true;
			} else if (anchor.isHeld()) {
				slip(anchor);
			}
		}
	}

	/** An anchor slips: her iron goes free, her chain lengthens, the binding loosens. */
	private void slip(ContainmentAnchorEntity anchor) {
		if (!(level() instanceof ServerLevel server)) return;
		anchor.slip();
		meter().add(SeraphaeIntegrityRules.SLIP, (Player) null);
		SeraphaeEffects.sound(server, anchor.position(), SoundInit.RITE_ANCHOR_BREAK, 1.2F, 1.0F);
		SeraphaeEffects.cue(server, anchor.crown(), position().add(0.0D, 1.6D, 0.0D), SeraphaeVisualRules.SLIP, 0.8F);
		SeraphaeLines.teachAll(this, SeraphaeLines.TEACH_SLIP);
	}

	private void fracture(ServerLevel level) {
		SaintArena arena = arena();
		if (arena == null) return;
		int phase = phases().phase();
		int count = Math.min(FRAGMENTS_MAX, (phase >= 3 ? FRAGMENTS_THREE : FRAGMENTS_TWO)
				+ (arena.challengerCount() - 1) + SeraphaeDeeds.fragmentBonus(deeds().mask()));
		for (int i = 0; i < count; i++) {
			SeraphaeFragmentEntity fragment = EntityInit.seraphae_fragment.get().create(level);
			if (fragment == null) continue;
			double angle = i * Mth.TWO_PI / count + getRandom().nextFloat() * 0.5F;
			fragment.moveTo(getX() + Math.cos(angle) * FRAGMENT_RING, getY() + 1.0D, getZ() + Math.sin(angle) * FRAGMENT_RING,
					getRandom().nextFloat() * 360.0F, 0.0F);
			fragment.setParentId(getUUID());
			fragment.setPersistenceRequired();
			if (!level.addFreshEntity(fragment)) continue;
			arena.own(fragment, null);
			fragments.add(fragment.getUUID());
		}
		fractureOpen = !fragments.isEmpty();
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		SeraphaeEffects.sound(level, chest, SoundEvents.GLASS_BREAK, 2.0F, 0.6F);
		SeraphaeEffects.sound(level, chest, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.6F, 0.6F);
		SeraphaeEffects.cue(level, chest, SeraphaeVisualRules.FRACTURE_BURST, 2.4F);
		SeraphaeLines.teachAll(this, SeraphaeLines.TEACH_FRACTURE);
	}

	/** Healed Ground's pads: under the most wounded challenger, where she last healed, and one more per extra challenger. */
	private List<Vec3> padPoints() {
		List<Vec3> points = new ArrayList<>();
		SaintArena arena = arena();
		if (arena == null) return points;
		List<Player> byWound = new ArrayList<>(arena.challengers());
		byWound.sort((a, b) -> Float.compare(b.getMaxHealth() - b.getHealth(), a.getMaxHealth() - a.getHealth()));
		if (!byWound.isEmpty()) points.add(byWound.get(0).position());
		if (lastHealPos != null) points.add(lastHealPos);
		for (int i = 1; i < Math.min(byWound.size(), arena.challengerCount()); i++) points.add(byWound.get(i).position());
		if (points.isEmpty()) points.add(position().add(getLookAngle().scale(5.0D)));
		return points;
	}

	private void layPads(ServerLevel level, FootprintFrame frame) {
		SaintArena arena = arena();
		if (arena == null) return;
		for (Vec3 point : frame.points()) {
			BossGroundHazardEntity pad = BossGroundHazardEntity.spawn(level, this, PAD_KIND,
					new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, SeraphaeMove.PAD_RADIUS, BossFootprint.Height.STANDING),
					FootprintFrame.simple(point, point, point, frame.floorY()), PAD_LIFE, PAD_PULSE);
			arena.own(pad, null);
			SeraphaeEffects.cue(level, point.add(0.0D, 0.1D, 0.0D), SeraphaeVisualRules.PAD, (float) SeraphaeMove.PAD_RADIUS);
		}
	}

	void moveHit(SeraphaeMove move, SaintMoveContext ctx, LivingEntity target) {
		ServerLevel level = ctx.level();
		switch (move) {
			case LAYING_ON_OF_HANDS, STRAINING_REACH -> {
				if (reachCaught || !(target instanceof ServerPlayer player)) return;
				if (startHold(player)) {
					reachCaught = true;
					ctx.saint().controller().motor().stop();
					ctx.saint().controller().skipToRecovery();
				}
			}
			case NERVE_LASH, COUNTER_LASH -> {
				if (move == SeraphaeMove.COUNTER_LASH && target.isCrouching()) return;
				if (SaintMoves.hurt(ctx, target, move.baseDamage())) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 1, false, false, true));
					if (move == SeraphaeMove.NERVE_LASH && target instanceof Player player) addGrowth(player, 1);
				}
			}
			case AXON_LANCE, AXON_MIRROR -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage())) {
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, false, true));
					if (target instanceof Player player) addGrowth(player, 1);
				}
			}
			case REGROWTH_KNOT -> {
				if (target instanceof Player player) {
					if (SeraphaeGrowthRules.wounded(player.getHealth(), player.getMaxHealth())) {
						heal(player, 1.0F);
					} else if (SaintMoves.hurt(ctx, target, move.baseDamage())) {
						addGrowth(player, 2);
					}
					SaintRoot.root(player, KNOT_ROOT, ROOT, false);
				}
			}
			case TRANSFUSION_THREAD -> {
				if (!(target instanceof Player player)) return;
				if (SeraphaeGrowthRules.wounded(player.getHealth(), player.getMaxHealth())) heal(player, 1.0F);
				else addGrowth(player, 1);
				Vec3 toward = position().subtract(player.position());
				Vec3 flat = new Vec3(toward.x, 0.0D, toward.z);
				if (flat.lengthSqr() > 1.0E-4D) {
					Vec3 pull = flat.normalize().scale(THREAD_PULL);
					player.push(pull.x, 0.05D, pull.z);
					player.hurtMarked = true;
				}
				SeraphaeEffects.spark(level, player.position().add(0.0D, 1.2D, 0.0D), 2);
			}
			case WING_GRASP -> {
				if (!SaintMoves.hurt(ctx, target, move.baseDamage())) return;
				Vec3 front = position().add(getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize().scale(GRASP_THROW));
				target.teleportTo(front.x, getY() - HOVER, front.z);
				target.setDeltaMovement(Vec3.ZERO);
				target.hurtMarked = true;
				if (target instanceof Player player) SaintRoot.root(player, GRASP_ROOT, ROOT, true);
				wingCaught = true;
				SeraphaeEffects.spark(level, target.position().add(0.0D, 1.2D, 0.0D), 4);
			}
			case CONVULSION, CONVULSION_WIDE -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage())) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3, false, false, true));
					if (target instanceof Player player) addGrowth(player, 1);
				}
			}
			case CHAIN_WRENCH -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage())) {
					Vec3 away = target.position().subtract(position());
					Vec3 flat = new Vec3(away.x, 0.0D, away.z);
					if (flat.lengthSqr() > 1.0E-4D) {
						Vec3 push = flat.normalize().scale(WRENCH_KNOCKBACK);
						target.push(push.x, 0.2D, push.z);
						target.hurtMarked = true;
					}
				}
			}
			case HEALED_GROUND -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage()) && target instanceof Player player) {
					SaintRoot.root(player, PAD_ROOT, ROOT, true);
				}
			}
			case UNCLOSING_EYE, EXTRACTION -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage()) && target instanceof Player player) addGrowth(player, 1);
			}
			case RADIANCE_FLOOD -> {
				if (SaintMoves.hurt(ctx, target, move.baseDamage()) && target instanceof Player player) {
					addGrowth(player, FLOOD_GROWTH);
				}
			}
			default -> SaintMoves.hurt(ctx, target, move.baseDamage());
		}
	}

	// ── the hold ─────────────────────────────────────────────────────

	private boolean startHold(ServerPlayer player) {
		holdAbsorb = 0;
		holdAllyHits = 0;
		holdStilling = false;
		BossHold.Spec spec = new BossHold.Spec(HOLD_TICKS, new Vec3(1.1D, -HOVER, 0.0D), 0.6D, true);
		if (!bossHold().tryHold(player, spec, (held, ticks) -> holdAbsorb >= HOLD_BREAK_ABSORB || holdStilling
				|| holdAllyHits >= HOLD_BREAK_HITS)) return false;
		SeraphaeLines.teach(player, SeraphaeLines.TEACH_HOLD);
		if (level() instanceof ServerLevel server) {
			SeraphaeEffects.sound(server, player.position(), SoundInit.RITE_AMBIENT_HEARTBEAT, 1.0F, 1.4F);
		}
		return true;
	}

	private void tickHold() {
		UUID held = bossHold().held();
		if (held == null || !(level() instanceof ServerLevel server)) return;
		Player player = server.getPlayerByUUID(held);
		if (player == null) return;
		meter().flags(player, meter().player(player).flags() | SeraphaeGrowthRules.FLAG_HELD);
		int ticks = bossHold().heldTicks();
		if (ticks <= 0 || ticks % SeraphaeGrowthRules.HOLD_PULSE != 0) return;
		float pace = 1.4F + ticks / (float) HOLD_TICKS * 0.4F;
		SeraphaeEffects.sound(server, player.position(), SoundInit.RITE_AMBIENT_HEARTBEAT, 1.0F, pace);
		SeraphaeEffects.spark(server, player.position().add(0.0D, 1.2D, 0.0D), 3);
		if (SeraphaeGrowthRules.wounded(player.getHealth(), player.getMaxHealth())) {
			heal(player, SeraphaeGrowthRules.HOLD_HEAL);
			if (!SeraphaeGrowthRules.wounded(player.getHealth(), player.getMaxHealth())) {
				SeraphaeLines.voice(this, SeraphaeLines.HOLD);
			}
		} else {
			addGrowth(player, SeraphaeGrowthRules.holdGrowth(phases().phase()));
		}
	}

	@Override
	protected void onHoldReleased(BossHold.Reason reason) {
		if (!(level() instanceof ServerLevel server)) return;
		SaintArena arena = arena();
		Player released = null;
		if (arena != null) {
			for (Player player : arena.challengers()) {
				SaintMeter.PlayerState state = meter().existing(player.getUUID());
				if (state != null && (state.flags() & SeraphaeGrowthRules.FLAG_HELD) != 0) {
					meter().flags(player, state.flags() & ~SeraphaeGrowthRules.FLAG_HELD);
					released = player;
				}
			}
		}
		if (released != null) SeraphaeEffects.sound(server, released.position(), SoundInit.ENTITY_VESPER_GRAB_RELEASE, 1.0F, 1.5F);
		switch (reason) {
			case BROKEN -> queueOpening(SeraphaeOpenings.BROKEN_HOLD);
			case DONE -> {
				if (released != null) {
					SaintRoot.root(released, HOLD_ROOT, ROOT, true);
					if (getRandom().nextFloat() < HOLD_LASH_CHANCE) {
						lastLashSide = sideOf(released.position());
						controller().start(SeraphaeMove.NERVE_LASH, SaintCombatController.Cut.QUEUE);
					}
				}
			}
			default -> {
			}
		}
		holdAbsorb = 0;
		holdAllyHits = 0;
		holdStilling = false;
	}

	// ── presence and growth ──────────────────────────────────────────

	void presencePulse(Player player) {
		if (!(level() instanceof ServerLevel server)) return;
		boolean wounded = SeraphaeGrowthRules.wounded(player.getHealth(), player.getMaxHealth());
		Vec3 from = position().add(0.0D, 0.2D, 0.0D);
		Vec3 to = player.position().add(0.0D, 0.2D, 0.0D);
		SeraphaeEffects.mote(server, from, to.subtract(from).scale(1.0D / 20.0D));
		if (wounded) {
			heal(player, SeraphaeGrowthRules.healing(player.getHealth(), player.getMaxHealth()));
			addGrowth(player, -1);
			return;
		}
		int gained = SeraphaeGrowthRules.pulse(false, Math.hypot(player.getX() - getX(), player.getZ() - getZ()));
		addGrowth(player, gained);
		SeraphaeEffects.soundTo(player, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.4F, 1.6F);
	}

	private void heal(Player player, float amount) {
		if (amount <= 0.0F) return;
		player.heal(amount);
		lastHealth.put(player.getUUID(), player.getHealth());
		lastHealPos = player.position();
	}

	public int growth(Player player) {
		SaintMeter.PlayerState state = meter().existing(player.getUUID());
		return state == null ? 0 : Math.round(state.secondary());
	}

	/** Adds growth (negative sheds it); at 10 it bursts. */
	public void addGrowth(Player player, int amount) {
		if (amount == 0 || player.isSpectator() || player.isCreative()) return;
		int before = growth(player);
		int after = SeraphaeGrowthRules.clamp(before + amount);
		if (after >= SeraphaeGrowthRules.MAX) {
			burst(player);
			return;
		}
		setGrowth(player, after);
		if (before < SeraphaeGrowthRules.SWELLING && after >= SeraphaeGrowthRules.SWELLING) {
			SeraphaeLines.teach(player, SeraphaeLines.TEACH_GROWTH);
		}
	}

	private void setGrowth(Player player, int growth) {
		meter().secondary(player, growth, SeraphaeGrowthRules.MAX);
		SaintMeter.PlayerState state = meter().player(player);
		boolean held = (state.flags() & SeraphaeGrowthRules.FLAG_HELD) != 0;
		boolean feeding = feedingFor.getOrDefault(player.getUUID(), 0) > 0;
		meter().flags(player, SeraphaeGrowthRules.flags(growth, held, feeding));
	}

	private void burst(Player player) {
		if (!(level() instanceof ServerLevel server)) return;
		setGrowth(player, 0);
		boolean held = player.getUUID().equals(bossHold().held());
		SaintMoves.hurt(this, player, SeraphaeGrowthRules.burstDamage(phases().phase()), null);
		lastHealth.put(player.getUUID(), player.getHealth());
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SeraphaeGrowthRules.BURST_WEAKNESS, 0, false, false, true));
		SaintRoot.root(player, SeraphaeGrowthRules.BURST_ROOT, ROOT, true);
		meter().add(SeraphaeIntegrityRules.BURST, (Player) null);
		Vec3 chest = player.position().add(0.0D, 1.2D, 0.0D);
		SeraphaeEffects.cue(server, chest, SeraphaeVisualRules.GROWTH_BURST, 1.0F);
		SeraphaeEffects.scatter(server, chest, 6);
		SeraphaeEffects.spark(server, chest, 4);
		SeraphaeEffects.sound(server, chest, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 0.5F);
		SeraphaeEffects.sound(server, chest, SoundEvents.BONE_BLOCK_BREAK, 1.0F, 0.7F);
		player.displayClientMessage(Component.translatable(SeraphaeLines.EXCESS_GROWTH), true);
		if (held) SeraphaeLines.voice(this, SeraphaeLines.HELD_BURST);
	}

	/** Each tick for each challenger: wounds shed growth, the stage's effect holds, growth ebbs outside her radius. */
	private void tickChallengers() {
		SaintArena arena = arena();
		if (arena == null) return;
		Set<UUID> present = new HashSet<>();
		for (Player player : arena.challengers()) {
			UUID id = player.getUUID();
			present.add(id);
			float health = player.getHealth();
			Float before = lastHealth.put(id, health);
			int growth = growth(player);
			if (before != null && before > health && growth > 0) {
				float[] shed = SeraphaeGrowthRules.shedByDamage(growth, woundCarry.getOrDefault(id, 0.0F), before - health);
				woundCarry.put(id, shed[1]);
				if ((int) shed[0] != growth) setGrowth(player, (int) shed[0]);
				growth = (int) shed[0];
			}
			int feeding = feedingFor.getOrDefault(id, 0);
			if (feeding > 0) {
				feedingFor.put(id, feeding - 1);
				if (feeding == 1) setGrowth(player, growth);
			}
			double distance = Math.hypot(player.getX() - getX(), player.getZ() - getZ());
			if (distance > SeraphaeGrowthRules.RADIUS && growth > 0 && tickCount % SeraphaeGrowthRules.EBB_TICKS == 0) {
				setGrowth(player, growth - 1);
				growth--;
			}
			SeraphaeGrowthRules.Stage stage = SeraphaeGrowthRules.stage(growth);
			if (tickCount % 20 == 0 && stage != SeraphaeGrowthRules.Stage.NONE) {
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SeraphaeGrowthRules.STAGE_EFFECT_TICKS,
						stage == SeraphaeGrowthRules.Stage.SWELLING ? 0 : 1, false, false, true));
			}
			knot(player, stage == SeraphaeGrowthRules.Stage.KNOTTING);
		}
		knotted.removeIf(id -> {
			if (present.contains(id)) return false;
			Player gone = level().getPlayerByUUID(id);
			if (gone != null) unknot(gone);
			return true;
		});
		lastHealth.keySet().retainAll(present);
		woundCarry.keySet().retainAll(present);
	}

	/** Knotting holds jump strength at zero through a held modifier; released when growth falls or they leave. */
	private void knot(Player player, boolean knotting) {
		AttributeModifiers.sync(player.getAttribute(Attributes.JUMP_STRENGTH), KNOTTING, -1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, knotting);
		if (knotting) knotted.add(player.getUUID());
		else knotted.remove(player.getUUID());
	}

	private void unknot(Player player) {
		AttributeModifiers.sync(player.getAttribute(Attributes.JUMP_STRENGTH), KNOTTING, -1.0D,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, false);
	}

	/** Every challenger's growth leaves them ("the light leaves you"), and every knot with it. */
	private void clearGrowth() {
		SaintArena arena = arena();
		if (arena != null) {
			for (Player player : arena.challengers()) {
				setGrowth(player, 0);
				unknot(player);
			}
		}
		for (UUID id : knotted) {
			Player player = level().getPlayerByUUID(id);
			if (player != null) unknot(player);
		}
		knotted.clear();
	}

	// ── hazards: her healed pads ─────────────────────────────────────

	@Override
	public boolean hazardTouches(LivingEntity entity) {
		SaintArena arena = arena();
		return entity instanceof Player player && arena != null && arena.challengers().contains(player)
				&& ManipulationCombatHelper.canHarmFor(this, player);
	}

	@Override
	public void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside) {
		if (hazard.kind() != PAD_KIND) return;
		for (LivingEntity entity : inside) if (entity instanceof Player player) addGrowth(player, 1);
	}

	// ── fragments ────────────────────────────────────────────────────

	/** Living fragments of hers, leaving out {@code except} (one that is resolving now). */
	int livingFragments(@Nullable Entity except) {
		if (!(level() instanceof ServerLevel server)) return 0;
		int count = 0;
		for (UUID id : fragments) {
			Entity entity = server.getEntity(id);
			if (entity != null && entity != except && entity.isAlive()) count++;
		}
		return count;
	}

	void fragmentResolved(SeraphaeFragmentEntity fragment, SeraphaeIntegrityRules.FragmentEnd end, Vec3 toward) {
		if (!(level() instanceof ServerLevel server)) return;
		Vec3 at = fragment.position().add(0.0D, 0.5D, 0.0D);
		Player nearest = nearestChallenger(at);
		float change = SeraphaeIntegrityRules.fragment(end);
		if (change != 0.0F) meter().add(change, change > 0.0F ? nearest : null);
		switch (end) {
			case ABSORBED -> {
				SeraphaeEffects.cue(server, at, toward, SeraphaeVisualRules.CAPTURE, 0.5F);
				SeraphaeEffects.sound(server, at, SoundInit.RITE_SEGMENT_SEAL, 1.0F, 1.4F);
			}
			case CAPTURED, RECALLED -> {
				SeraphaeEffects.cue(server, at, toward, SeraphaeVisualRules.CAPTURE, 0.7F);
				SeraphaeEffects.sound(server, at, SoundInit.RITE_SEGMENT_SEAL, 1.0F, 1.4F);
			}
			case STRUCK -> {
				SeraphaeEffects.scatter(server, at, 5);
				SeraphaeEffects.sound(server, at, SoundEvents.AMETHYST_BLOCK_BREAK, 1.0F, 1.4F);
			}
			case GRAFTED -> SeraphaeEffects.cue(server, at, SeraphaeVisualRules.GROWTH_BURST, 0.6F);
			case RETURNED -> SeraphaeEffects.cue(server, at, toward, SeraphaeVisualRules.CAPTURE, 0.6F);
		}
		fragments.remove(fragment.getUUID());
		if (fractureOpen && livingFragments(fragment) == 0 && end != SeraphaeIntegrityRules.FragmentEnd.RECALLED) {
			fractureOpen = false;
			queueOpening(SeraphaeOpenings.CONDENSING);
		}
	}

	void fragmentGrafted(Player player) {
		addGrowth(player, 2);
	}

	// ── openings ─────────────────────────────────────────────────────

	/** Opens now if no move is mid-strike and no opening is open; otherwise at the next boundary. */
	private void queueOpening(SaintOpening.Spec spec) {
		pendingOpening = spec;
		pendingFor = 0;
		tryPendingOpening();
	}

	private void tryPendingOpening() {
		if (pendingOpening == null) return;
		if (++pendingFor > OPENING_QUEUE_LIMIT) {
			pendingOpening = null;
			return;
		}
		if (!controller().interruptible() || openings().isOpen()) return;
		SaintOpening.Spec spec = pendingOpening;
		pendingOpening = null;
		openings().open(spec, spec.ticks());
	}

	@Override
	protected void onOpeningChanged(@Nullable SaintOpening.Spec from, @Nullable SaintOpening.Spec to) {
		if (!(level() instanceof ServerLevel server)) return;
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		if (to == null) {
			entityData.set(DATA_SEIZED, 0);
			seizedToward = null;
			return;
		}
		switch (to.id()) {
			case SeraphaeOpenings.RECOIL_ID -> {
				SeraphaeLines.voice(this, SeraphaeLines.RECOIL);
				SeraphaeLines.teachAll(this, SeraphaeLines.TEACH_RECOIL);
			}
			case SeraphaeOpenings.SEIZED_ID -> SeraphaeEffects.sound(server, chest, SoundEvents.CHAIN_HIT, 1.2F, 0.8F);
			case SeraphaeOpenings.CONDENSING_ID -> {
				feedEarned.clear();
				SeraphaeLines.teachAll(this, SeraphaeLines.TEACH_CONDENSING);
				SeraphaeEffects.sound(server, chest, SoundEvents.BEACON_DEACTIVATE, 2.0F, 0.8F);
			}
			case SeraphaeOpenings.LAPSE_ID -> SeraphaeLines.teachAll(this, SeraphaeLines.TEACH_LAPSE);
			default -> {
			}
		}
	}

	@Override
	protected void onHitTaken(SaintHit hit, float applied, SaintOpening.Admit admit) {
		Player attacker = hit.attacker();
		if (attacker == null || !(level() instanceof ServerLevel server)) return;
		Vec3 point = hitPoint(attacker);
		SaintOpening.Spec open = openings().current();
		if (admit == SaintOpening.Admit.COUNTED && open != null) {
			meter().add(SeraphaeIntegrityRules.hitGain(open.id(), hit.delivery(), hit.school()), attacker);
			ContainmentAnchorEntity nearest = nearestHeldAnchor(point);
			SeraphaeEffects.cue(server, point, nearest == null ? point : nearest.crown(), SeraphaeVisualRules.KNOT, 0.4F);
			SeraphaeEffects.sound(server, point, SoundEvents.AMETHYST_CLUSTER_BREAK, 1.0F, 1.0F);
		} else {
			SeraphaeEffects.cue(server, point, SeraphaeVisualRules.CLOSE, 0.3F);
			SeraphaeEffects.sound(server, point, SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.4F);
			SeraphaeLines.teach(attacker, SeraphaeLines.TEACH_CLOSES);
			if (attacker.distanceTo(this) <= CLOSE_STRIKER
					&& !SeraphaeGrowthRules.wounded(attacker.getHealth(), attacker.getMaxHealth())) {
				addGrowth(attacker, 1);
			}
		}
		if (hit.delivery() == SaintOpening.Delivery.SCHOOL_CAST) addGrowth(attacker, -SeraphaeGrowthRules.CAST_SHED);
		if (bossHold().holding()) {
			if (SeraphaeIntegrityRules.breaksHold(hit.school())) holdStilling = true;
			if (!attacker.getUUID().equals(bossHold().held())) holdAllyHits++;
		}
	}

	private Vec3 hitPoint(Player attacker) {
		Vec3 chest = position().add(0.0D, CHEST - 0.3D, 0.0D);
		Vec3 toward = attacker.getEyePosition().subtract(chest);
		return toward.lengthSqr() < 1.0E-4D ? chest : chest.add(toward.normalize().scale(WIDTH * 0.5D));
	}

	@Nullable
	private ContainmentAnchorEntity nearestHeldAnchor(Vec3 from) {
		ContainmentAnchorEntity best = null;
		double bestDistance = Double.MAX_VALUE;
		for (ContainmentAnchorEntity anchor : heldAnchors()) {
			double d = anchor.position().distanceToSqr(from);
			if (d < bestDistance) {
				bestDistance = d;
				best = anchor;
			}
		}
		return best;
	}

	// ── held channels (SaintChannelRouter) ───────────────────────────

	@Override
	protected SaintChannelRouter.Result onChannel(SaintChannelRouter.Request request) {
		return request.kind() == SaintChannelRouter.Kind.PROJECT ? project(request) : absorb(request);
	}

	private SaintChannelRouter.Result project(SaintChannelRouter.Request request) {
		ContainmentAnchorEntity anchor = aimedAnchor(request);
		if (anchor != null) return feedAnchor(request, anchor);
		int reserve = aimedReserve(request);
		if (reserve >= 0) return feedReserve(request, reserve);
		return SaintChannelRouter.Result.PASS;
	}

	private SaintChannelRouter.Result absorb(SaintChannelRouter.Request request) {
		SeraphaeFragmentEntity fragment = aimedFragment(request);
		ServerPlayer player = request.player();
		if (fragment != null) {
			fragment.draw(player);
			if (level() instanceof ServerLevel server && player.tickCount % 3 == 0) {
				Vec3 from = fragment.position().add(0.0D, 0.4D, 0.0D);
				SeraphaeEffects.mote(server, from, request.eye().subtract(from).scale(0.08D));
			}
			return SaintChannelRouter.Result.taken(0.0D);
		}
		if (SaintChannelRouter.aimsAt(request, position().add(0.0D, CHEST, 0.0D), request.reach() + WIDTH,
				BODY_AIM_CONE, true)) {
			if (bossHold().holding()) {
				holdAbsorb++;
				return SaintChannelRouter.Result.taken(0.0D);
			}
			return SaintChannelRouter.Result.refused(Component.translatable(SeraphaeLines.REFUSED_DRAIN));
		}
		return SaintChannelRouter.Result.PASS;
	}

	@Nullable
	private ContainmentAnchorEntity aimedAnchor(SaintChannelRouter.Request request) {
		ContainmentAnchorEntity best = null;
		double bestDot = -2.0D;
		for (ContainmentAnchorEntity anchor : anchors()) {
			Vec3 body = anchor.position().add(0.0D, 0.8D, 0.0D);
			if (!SaintChannelRouter.aimsAt(request, body, request.reach() + 1.0D, SeraphaeAnchorBindingRules.AIM_CONE, true)
					&& !SaintChannelRouter.aimsAt(request, anchor.crown(), request.reach() + 1.0D,
					SeraphaeAnchorBindingRules.AIM_CONE, true)) continue;
			double dot = body.subtract(request.eye()).normalize().dot(request.look().normalize());
			if (dot > bestDot) {
				bestDot = dot;
				best = anchor;
			}
		}
		return best;
	}

	private int aimedReserve(SaintChannelRouter.Request request) {
		for (int i = 0; i < reserveHomes.size(); i++) {
			if (reserveWoken[i]) continue;
			BlockPos home = reserveHomes.get(i);
			if (request.block() != null) {
				BlockPos hit = request.block().getBlockPos();
				if (hit.equals(home) || hit.equals(home.above())) return i;
			}
			Vec3 body = Vec3.atBottomCenterOf(home).add(0.0D, 1.0D, 0.0D);
			if (SaintChannelRouter.aimsAt(request, body, request.reach() + 1.0D, SeraphaeAnchorBindingRules.AIM_CONE, false)) {
				return i;
			}
		}
		return -1;
	}

	@Nullable
	private SeraphaeFragmentEntity aimedFragment(SaintChannelRouter.Request request) {
		if (!(level() instanceof ServerLevel server)) return null;
		SeraphaeFragmentEntity best = null;
		double bestDistance = Double.MAX_VALUE;
		for (UUID id : fragments) {
			if (!(server.getEntity(id) instanceof SeraphaeFragmentEntity fragment) || !fragment.isAlive()) continue;
			Vec3 body = fragment.position().add(0.0D, 0.4D, 0.0D);
			if (!SaintChannelRouter.aimsAt(request, body, request.reach() + 0.5D, FRAGMENT_AIM_CONE, true)) continue;
			double d = body.distanceToSqr(request.eye());
			if (d < bestDistance) {
				bestDistance = d;
				best = fragment;
			}
		}
		return best;
	}

	private SaintChannelRouter.Result feedAnchor(SaintChannelRouter.Request request, ContainmentAnchorEntity anchor) {
		boolean condensing = anchor.isHeld() && openings().isOpen(SeraphaeOpenings.CONDENSING);
		BloodAccounting.Source source = condensing ? SeraphaeAnchorBindingRules.CONDENSING_SOURCE
				: SeraphaeAnchorBindingRules.SOURCE;
		double take = SeraphaeAnchorBindingRules.take(request.ratePerTick());
		BloodAccounting.Payment payment = SaintChannelRouter.pay(request, take, source);
		if (payment.paid() <= 0.0D) {
			return SaintChannelRouter.Result.refused(Component.translatable(SeraphaeLines.NO_BLOOD));
		}
		ServerPlayer player = request.player();
		boolean wasHeld = anchor.isHeld();
		boolean rebound = anchor.feed(payment.paid());
		if (condensing) {
			float earned = feedEarned.getOrDefault(player.getUUID(), 0.0F);
			float gain = SeraphaeIntegrityRules.feed(payment.paid(), earned);
			if (gain > 0.0F) {
				meter().add(gain, player);
				feedEarned.put(player.getUUID(), earned + gain);
			}
		}
		if (rebound && !wasHeld) rebound(anchor, player);
		fed(player, anchor.crown());
		return SaintChannelRouter.Result.taken(payment.paid());
	}

	private SaintChannelRouter.Result feedReserve(SaintChannelRouter.Request request, int index) {
		double take = SeraphaeAnchorBindingRules.take(request.ratePerTick());
		BloodAccounting.Payment payment = SaintChannelRouter.pay(request, take, SeraphaeAnchorBindingRules.RESERVE_SOURCE);
		if (payment.paid() <= 0.0D) {
			return SaintChannelRouter.Result.refused(Component.translatable(SeraphaeLines.NO_BLOOD));
		}
		ServerPlayer player = request.player();
		reserveSinceFed[index] = 0;
		reserveProgress[index] = SeraphaeAnchorBindingRules.feed(reserveProgress[index], payment.paid(),
				SeraphaeAnchorBindingRules.RESERVE_BLOOD);
		BlockPos home = reserveHomes.get(index);
		if (SeraphaeAnchorBindingRules.complete(reserveProgress[index]) && arena() != null) {
			wakeReserve(arena(), index, true);
			meter().add(SeraphaeIntegrityRules.reserveWoken(true), player);
		}
		fed(player, Vec3.atBottomCenterOf(home).add(0.0D, 1.5D, 0.0D));
		return SaintChannelRouter.Result.taken(payment.paid());
	}

	/** A slipped anchor holds again: her iron is bound, the binding tightens, she is Seized toward it. */
	private void rebound(ContainmentAnchorEntity anchor, Player player) {
		if (!(level() instanceof ServerLevel server)) return;
		boolean reserve = anchor.kind() == ContainmentAnchorEntity.Kind.RESERVE;
		meter().add(reserve ? SeraphaeIntegrityRules.reserveWoken(false) : SeraphaeIntegrityRules.REBIND, player);
		SeraphaeEffects.sound(server, anchor.position(), SoundInit.RITE_ANCHOR_REPAIR, 1.0F, 1.0F);
		SeraphaeEffects.sound(server, anchor.position(), SoundEvents.CHAIN_PLACE, 1.0F, 0.8F);
		SeraphaeEffects.cue(server, anchor.crown(), position().add(0.0D, 1.6D, 0.0D), SeraphaeVisualRules.CAPTURE, 0.6F);
		seize(anchor);
		queueOpening(SeraphaeOpenings.SEIZED);
	}

	/** Bookkeeping for one fed tick: the feeding flag, the shed, the anchor tick and a trace into the anchor. */
	private void fed(ServerPlayer player, Vec3 into) {
		UUID id = player.getUUID();
		boolean wasFeeding = feedingFor.getOrDefault(id, 0) > 0;
		feedingFor.put(id, FEEDING_FLAG_TICKS);
		if (!wasFeeding) setGrowth(player, growth(player));
		int ticks = fedTicks.merge(id, 1, Integer::sum);
		if (ticks % SeraphaeGrowthRules.FED_TICKS_PER_SHED == 0) addGrowth(player, -1);
		if (!(level() instanceof ServerLevel server)) return;
		if (ticks % 10 == 0) SeraphaeEffects.sound(server, into, SoundInit.MAUSOLEUM_SERAPHAE_ANCHOR_TICK, 0.8F, 1.0F);
		if (ticks % 4 == 0) {
			Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.6D)).add(0.0D, -0.4D, 0.0D);
			SeraphaeEffects.mote(server, hand, into.subtract(hand).scale(0.1D));
		}
	}

	// ── the server tick ──────────────────────────────────────────────

	@Override
	protected void tickSaint() {
		if (!(level() instanceof ServerLevel server) || arena() == null) return;
		updateLeash();
		syncRestraints();
		if (ending() || hasTrialEnded() || rising()) return;
		if (!firstReachQueued && !arena().challengers().isEmpty()) {
			firstReachQueued = true;
			SeraphaeLines.voice(this, SeraphaeLines.RISE);
			controller().start(SeraphaeMove.LAYING_ON_OF_HANDS, SaintCombatController.Cut.QUEUE);
		}
		tickChallengers();
		tickHold();
		tickReserves();
		tryPendingOpening();
		tickPressure();
		if (SeraphaeIntegrityRules.rearms(meter().value())) floodArmed = true;
		if (SeraphaeIntegrityRules.floods(meter().value(), floodArmed)) {
			floodArmed = false;
			controller().start(SeraphaeMove.RADIANCE_FLOOD, SaintCombatController.Cut.QUEUE);
		}
		if (containedPending) beginContainment(server);
	}

	private void tickReserves() {
		for (int i = 0; i < reserveProgress.length; i++) {
			if (reserveWoken[i] || reserveProgress[i] <= 0.0F) continue;
			if (reserveSinceFed[i] < Integer.MAX_VALUE / 2) reserveSinceFed[i]++;
			reserveProgress[i] = SeraphaeAnchorBindingRules.bleed(reserveProgress[i], reserveSinceFed[i],
					SeraphaeAnchorBindingRules.RESERVE_BLOOD);
		}
	}

	/** Strain at the chain's end (for the Chain Wrench) and turtling past her reach (for the knot and pads). */
	private void tickPressure() {
		LivingEntity target = controller().target();
		if (target == null) {
			turtleTicks = 0;
			strainTicks = 0;
			return;
		}
		boolean past = SeraphaeLeashRules.straining(fromCentre(target.position()), leash);
		strainTicks = past && syncedGlide() == GLIDE_STRAIN ? strainTicks + 1 : 0;
		turtleTicks = past && distanceTo(target) > SeraphaeGrowthRules.RADIUS ? turtleTicks + 1 : 0;
	}

	private int restraintsPacked() {
		int[] states = new int[SeraphaeRestraintState.SLOTS];
		for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
			ContainmentAnchorEntity anchor = anchor(bindingAnchors.get(restraint));
			states[restraint.ordinal()] = anchor == null ? SeraphaeRestraintState.ABSENT : code(anchor.state());
		}
		for (int i = 0; i < reserveAnchors.length; i++) {
			ContainmentAnchorEntity anchor = anchor(reserveAnchors[i]);
			int slot = SeraphaeRestraintState.RESERVE_SLOT + i;
			if (anchor != null) states[slot] = code(anchor.state());
			else states[slot] = i < reserveHomes.size() && !reserveWoken[i] ? SeraphaeRestraintState.DORMANT
					: SeraphaeRestraintState.ABSENT;
		}
		return SeraphaeRestraintState.pack(states);
	}

	private static int code(ContainmentAnchorEntity.State state) {
		return switch (state) {
			case HELD -> SeraphaeRestraintState.HELD;
			case SLIPPED -> SeraphaeRestraintState.SLIPPED;
			case BINDING -> SeraphaeRestraintState.BINDING;
		};
	}

	private void syncRestraints() {
		entityData.set(DATA_RESTRAINTS, restraintsPacked());
		float[] progress = new float[SeraphaeRestraintState.SLOTS];
		for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
			ContainmentAnchorEntity anchor = anchor(bindingAnchors.get(restraint));
			if (anchor != null) {
				progress[restraint.ordinal()] = anchor.progress();
				savedStates.put(restraint, anchor.state());
			}
		}
		for (int i = 0; i < reserveAnchors.length; i++) {
			ContainmentAnchorEntity anchor = anchor(reserveAnchors[i]);
			progress[SeraphaeRestraintState.RESERVE_SLOT + i] = anchor != null ? anchor.progress() : reserveProgress[i];
		}
		entityData.set(DATA_RESTRAINT_PROGRESS, SeraphaeRestraintState.packProgress(progress));
	}

	private void spread(int wanted) {
		SaintArena arena = arena();
		if (arena == null) return;
		spreadTurned += SeraphaeRadianceSpread.spread(arena, position(), spreadTurned, wanted);
	}

	@Override
	protected void onPhaseChanged(int from, int to) {
		if (!(level() instanceof ServerLevel server)) return;
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		if (to == SeraphaePhases.RESTRAINT) {
			spread(SeraphaeRadianceSpread.PHASE_TWO);
			SeraphaeLines.voice(this, SeraphaeLines.RESTRAINT);
			SeraphaeEffects.sound(server, chest, SoundInit.RITE_ESCALATE, 1.5F, 0.9F);
		} else if (to == SeraphaePhases.ESCAPING_LIGHT) {
			spread(SeraphaeRadianceSpread.PHASE_THREE);
			SeraphaeLines.voice(this, SeraphaeLines.ESCAPING);
			SeraphaeEffects.sound(server, chest, SoundInit.RITE_CULMINATION, 1.5F, 0.8F);
		}
		SeraphaeEffects.spark(server, chest, 6);
	}

	// ── the ending ───────────────────────────────────────────────────

	private void beginContainment(ServerLevel server) {
		containedPending = false;
		UUID contributor = meter().lastContributor();
		Player victor = contributor == null ? null : server.getPlayerByUUID(contributor);
		endingFrom = position();
		beginEnding(SeraphaeEndingRules.SPEC, victor, () -> false);
		BossScenes.play(this, BossPresentationIds.SCENE_SERAPHAE_CONTAINED);
	}

	@Override
	protected void onEndingTick(int tick) {
		if (!(level() instanceof ServerLevel server)) return;
		if (endingFrom == null) endingFrom = position();
		Vec3 chest = position().add(0.0D, CHEST, 0.0D);
		List<ContainmentAnchorEntity> anchors = anchors();
		if (tick == SeraphaeEndingRules.FIRST_LINE) {
			SeraphaeLines.voice(this, SeraphaeLines.ENDING_FIRST);
			recallFragments(server);
		}
		for (int i = 0; i < anchors.size(); i++) {
			if (!SeraphaeEndingRules.flaresAt(tick, i)) continue;
			anchors.get(i).flare(SeraphaeEndingRules.END_TRIAL);
			SeraphaeEffects.sound(server, anchors.get(i).position(), SoundInit.RITE_ANCHOR_FILL, 1.2F, 0.8F + i * 0.1F);
			SeraphaeEffects.cue(server, anchors.get(i).crown(), chest, SeraphaeVisualRules.FLARE, 0.8F);
		}
		if (tick == SeraphaeEndingRules.GROWTH_CLEARED) clearGrowth();
		Vec3 home = tombTop();
		if (tick < SeraphaeEndingRules.END_TRIAL) {
			double[] point = SeraphaeEndingRules.reelPoint(endingFrom.x, endingFrom.y, endingFrom.z, home.x, home.y,
					home.z, tick + 1);
			setDeltaMovement(point[0] - getX(), point[1] - getY(), point[2] - getZ());
		} else {
			setDeltaMovement(Vec3.ZERO);
		}
		if (tick == SeraphaeEndingRules.SECOND_LINE) SeraphaeLines.voice(this, SeraphaeLines.ENDING_SECOND);
		if (tick == SeraphaeEndingRules.LAST_FLASH) SeraphaeEffects.spark(server, chest, 6);
		if (tick == SeraphaeEndingRules.END_TRIAL) {
			// her anchors let go first, so the arena stands their blocks again as it restores the chamber
			SaintArena arena = arena();
			if (arena != null) {
				for (Entity owned : arena.owned()) {
					if (owned instanceof ContainmentAnchorEntity || owned instanceof SeraphaeFragmentEntity) {
						arena.disown(owned);
						owned.discard();
					}
				}
			}
			SeraphaeEffects.sound(server, chest, SoundInit.RITE_SEAL, 1.5F, 0.9F);
			SeraphaeEffects.cue(server, chest, SeraphaeVisualRules.SEALED, 3.0F);
		}
		if (tick == SeraphaeEndingRules.LENGTH - 1) SeraphaeEffects.sound(server, chest, SoundEvents.BEACON_DEACTIVATE, 1.5F, 0.5F);
	}

	private void recallFragments(ServerLevel server) {
		for (UUID id : new ArrayList<>(fragments)) {
			if (!(server.getEntity(id) instanceof SeraphaeFragmentEntity fragment)) continue;
			ContainmentAnchorEntity anchor = nearestHeldAnchor(fragment.position());
			fragment.resolve(SeraphaeIntegrityRules.FragmentEnd.RECALLED,
					anchor == null ? position().add(0.0D, CHEST, 0.0D) : anchor.crown());
		}
		fragments.clear();
	}

	/** Where the chains reel her: over the tomb, a little above it; her spawn with no tomb. */
	private Vec3 tombTop() {
		BlockPos tomb = getTombPos();
		Vec3 base = tomb == null ? leashCentre : Vec3.atBottomCenterOf(tomb);
		return new Vec3(base.x, base.y + 1.0D, base.z);
	}

	@Override
	protected void announceTrialEnd(@Nullable Player victor) {
		clearGrowth();
	}

	@Override
	protected boolean rewardsGoToVictor() {
		return true;
	}

	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && level() instanceof ServerLevel) clearGrowth();
		super.remove(reason);
	}

	// ── save ─────────────────────────────────────────────────────────

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		CompoundTag own = new CompoundTag();
		own.putBoolean("AnchorsWoken", anchorsWoken);
		own.putBoolean("LeashSet", leashCentreSet);
		own.putDouble("LeashX", leashCentre.x);
		own.putDouble("LeashY", leashCentre.y);
		own.putDouble("LeashZ", leashCentre.z);
		own.putBoolean("FirstReach", firstReachQueued);
		own.putBoolean("FloodArmed", floodArmed);
		own.putBoolean("FractureOpen", fractureOpen);
		own.putInt("Spread", spreadTurned);
		CompoundTag states = new CompoundTag();
		savedStates.forEach((restraint, state) -> states.putString(restraint.name(), state.name()));
		own.put("Restraints", states);
		ListTag reserves = new ListTag();
		for (int i = 0; i < reserveWoken.length; i++) {
			CompoundTag entry = new CompoundTag();
			entry.putBoolean("Woken", reserveWoken[i]);
			entry.putFloat("Progress", reserveProgress[i]);
			reserves.add(entry);
		}
		own.put("Reserves", reserves);
		if (lastHealPos != null) {
			own.putDouble("HealX", lastHealPos.x);
			own.putDouble("HealY", lastHealPos.y);
			own.putDouble("HealZ", lastHealPos.z);
		}
		if (endingFrom != null) {
			own.putDouble("EndX", endingFrom.x);
			own.putDouble("EndY", endingFrom.y);
			own.putDouble("EndZ", endingFrom.z);
		}
		tag.put("Seraphae", own);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		CompoundTag own = tag.getCompound("Seraphae");
		anchorsWoken = own.getBoolean("AnchorsWoken");
		leashCentreSet = own.getBoolean("LeashSet");
		leashCentre = new Vec3(own.getDouble("LeashX"), own.getDouble("LeashY"), own.getDouble("LeashZ"));
		firstReachQueued = own.getBoolean("FirstReach");
		floodArmed = !own.contains("FloodArmed") || own.getBoolean("FloodArmed");
		// a reload restores the chamber and discards her fragments, so a Fracture under way ends with them
		fractureOpen = false;
		spreadTurned = own.getInt("Spread");
		savedStates.clear();
		CompoundTag states = own.getCompound("Restraints");
		for (SeraphaeRestraint restraint : SeraphaeRestraint.BINDING) {
			if (!states.contains(restraint.name())) continue;
			try {
				savedStates.put(restraint, ContainmentAnchorEntity.State.valueOf(states.getString(restraint.name())));
			} catch (IllegalArgumentException unknown) {
				// an unknown state wakes held
			}
		}
		ListTag reserves = own.getList("Reserves", Tag.TAG_COMPOUND);
		for (int i = 0; i < Math.min(reserves.size(), reserveWoken.length); i++) {
			reserveWoken[i] = reserves.getCompound(i).getBoolean("Woken");
			reserveProgress[i] = reserves.getCompound(i).getFloat("Progress");
		}
		lastHealPos = own.contains("HealX") ? new Vec3(own.getDouble("HealX"), own.getDouble("HealY"), own.getDouble("HealZ")) : null;
		endingFrom = own.contains("EndX") ? new Vec3(own.getDouble("EndX"), own.getDouble("EndY"), own.getDouble("EndZ")) : null;
	}

	// ── misc ─────────────────────────────────────────────────────────

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	public float getVoicePitch() {
		return 0.7F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.AMETHYST_BLOCK_CHIME;
	}

	/** An opening hit breaks light out of her; a hit that closes only rings. */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return openings().isOpen() ? SoundEvents.AMETHYST_CLUSTER_BREAK : SoundEvents.AMETHYST_BLOCK_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BEACON_DEACTIVATE;
	}
}
