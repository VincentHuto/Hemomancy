package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock;
import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * One of Seraphae's containment anchors, woken from a dormant {@link ContainmentAnchorBlock} round her tomb (a binding
 * anchor, holding one of her four irons) or in the aisles by a challenger's held Projection (a reserve, chaining her
 * waist). It holds, slips when she wrenches it loose, and re-binds while a challenger feeds it blood; a held anchor
 * captures her loose fragments. Its block cells belong to her arena, which clears them when it wakes and stands them
 * again when the trial ends; the anchor itself never edits a block.
 */
public class ContainmentAnchorEntity extends Entity implements SaintCompanion {
	public enum Kind { BINDING, RESERVE }

	public enum State { HELD, SLIPPED, BINDING }

	private static final EntityDataAccessor<Integer> DATA_KIND =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_RESTRAINT =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_STATE =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_PROGRESS =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_FLARE =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.INT);
	private static final SyncedOwner PARENT = SyncedOwner.define(ContainmentAnchorEntity.class);

	/** Radius within which a held anchor captures a fragment. */
	public static final double CAPTURE_RADIUS = 3.5D;
	private static final int SCAN_INTERVAL = 10;
	public static final int FLARE_TICKS = 30;
	/** Where her chain meets the anchor: its crown, above its feet. */
	public static final double CROWN = 1.55D;
	private static final int ORPHAN_CHECK_INTERVAL = 100;
	private static final int ORPHAN_CHECKS = 3;
	private static final double ORPHAN_WITNESS_RANGE = 16.0D;
	private static final String TAG_HOME = "HomeAnchor";

	@Nullable
	private BlockPos home;
	private int orphanChecks;
	private int sinceFed = Integer.MAX_VALUE / 2;
	private boolean tornOnce;

	public ContainmentAnchorEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_KIND, Kind.BINDING.ordinal());
		builder.define(DATA_RESTRAINT, SeraphaeRestraint.LEFT_WING.ordinal());
		builder.define(DATA_STATE, State.HELD.ordinal());
		builder.define(DATA_PROGRESS, 0.0F);
		builder.define(DATA_FLARE, 0);
		PARENT.defineDefault(builder);
	}

	// ── what it is ───────────────────────────────────────────────────

	public void setup(UUID parent, Kind kind, SeraphaeRestraint restraint, @Nullable BlockPos home) {
		PARENT.set(this, parent);
		entityData.set(DATA_KIND, kind.ordinal());
		entityData.set(DATA_RESTRAINT, restraint.ordinal());
		entityData.set(DATA_STATE, State.HELD.ordinal());
		this.home = home == null ? null : home.immutable();
	}

	@Nullable
	public UUID getParentId() {
		return PARENT.get(this);
	}

	@Override
	public UUID saintId() {
		UUID parent = getParentId();
		return parent == null ? new UUID(0L, 0L) : parent;
	}

	@Override
	public Allegiance allegiance() {
		return Allegiance.SAINT;
	}

	public Kind kind() {
		return Kind.values()[Math.floorMod(entityData.get(DATA_KIND), Kind.values().length)];
	}

	public SeraphaeRestraint restraint() {
		return SeraphaeRestraint.byId(entityData.get(DATA_RESTRAINT));
	}

	public State state() {
		return State.values()[Math.floorMod(entityData.get(DATA_STATE), State.values().length)];
	}

	public boolean isHeld() {
		return state() == State.HELD;
	}

	/** 0..1 toward re-binding while slipped or binding. */
	public float progress() {
		return entityData.get(DATA_PROGRESS);
	}

	/** Ticks left of a flare (a capture, a re-bind, the ending's braid). */
	public int flare() {
		return entityData.get(DATA_FLARE);
	}

	public void flare(int ticks) {
		entityData.set(DATA_FLARE, Math.max(flare(), ticks));
	}

	@Nullable
	public BlockPos getHome() {
		return home;
	}

	/** Where her chain meets it. */
	public Vec3 crown() {
		return position().add(0.0D, CROWN, 0.0D);
	}

	/** Whether a torn reserve has been torn before (its re-wake pays less). */
	public boolean tornOnce() {
		return tornOnce;
	}

	// ── what the fight does to it ────────────────────────────────────

	/** Wrenched loose: the iron it held goes free and her chain lengthens. */
	public void slip() {
		entityData.set(DATA_STATE, State.SLIPPED.ordinal());
		entityData.set(DATA_PROGRESS, 0.0F);
		if (kind() == Kind.RESERVE) tornOnce = true;
	}

	/** Fed blood toward re-binding; true when this feed completed it. Fed blood toward a held anchor only marks it fed. */
	public boolean feed(double blood) {
		sinceFed = 0;
		if (isHeld()) return false;
		float next = SeraphaeAnchorBindingRules.feed(progress(), blood, SeraphaeAnchorBindingRules.REBIND_BLOOD);
		entityData.set(DATA_PROGRESS, next);
		entityData.set(DATA_STATE, State.BINDING.ordinal());
		if (!SeraphaeAnchorBindingRules.complete(next)) return false;
		entityData.set(DATA_STATE, State.HELD.ordinal());
		entityData.set(DATA_PROGRESS, 0.0F);
		flare(FLARE_TICKS);
		return true;
	}

	/** Ticks since a challenger last fed it, for the Chain Wrench's held-at-commit rule. */
	public int ticksSinceFed() {
		return sinceFed;
	}

	// ── tick ─────────────────────────────────────────────────────────

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel server)) return;
		if (flare() > 0) entityData.set(DATA_FLARE, flare() - 1);
		if (sinceFed < Integer.MAX_VALUE / 2) sinceFed++;
		if (state() != State.HELD && progress() > 0.0F) {
			float bled = SeraphaeAnchorBindingRules.bleed(progress(), sinceFed, SeraphaeAnchorBindingRules.REBIND_BLOOD);
			entityData.set(DATA_PROGRESS, bled);
			if (bled <= 0.0F) entityData.set(DATA_STATE, State.SLIPPED.ordinal());
		}
		if (tickCount % ORPHAN_CHECK_INTERVAL == 0 && tickOrphaned(server)) return;
		// a copy that loaded after her reload had already stood her anchors up again is not one of hers
		if (tickCount % 20 == 0 && getParentId() != null && server.getEntity(getParentId()) instanceof SeraphaeEntity seraphae
				&& seraphae.isAlive() && !seraphae.knows(this)) {
			discard();
			return;
		}
		if (isHeld() && tickCount % SCAN_INTERVAL == 0) capture(server);
	}

	/** A held anchor draws in the nearest loose fragment of hers within reach. */
	private void capture(ServerLevel server) {
		UUID parent = getParentId();
		if (parent == null) return;
		List<SeraphaeFragmentEntity> fragments = server.getEntitiesOfClass(SeraphaeFragmentEntity.class,
				new AABB(blockPosition()).inflate(CAPTURE_RADIUS),
				fragment -> fragment.isAlive() && parent.equals(fragment.getParentId())
						&& fragment.distanceToSqr(this) <= CAPTURE_RADIUS * CAPTURE_RADIUS);
		SeraphaeFragmentEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (SeraphaeFragmentEntity fragment : fragments) {
			double d = fragment.distanceToSqr(this);
			if (d < best) {
				best = d;
				nearest = fragment;
			}
		}
		if (nearest == null) return;
		nearest.resolve(SeraphaeIntegrityRules.FragmentEnd.CAPTURED, crown());
		flare(FLARE_TICKS);
	}

	/**
	 * An anchor whose Seraphae is nowhere alive in the level, with a player standing by through a few checks, lets go:
	 * its Saint was removed while it lay unloaded, so its arena already stood the block back.
	 */
	private boolean tickOrphaned(ServerLevel server) {
		if (!isOrphan(server) || server.getNearestPlayer(this, ORPHAN_WITNESS_RANGE) == null) {
			orphanChecks = 0;
			return false;
		}
		if (++orphanChecks < ORPHAN_CHECKS) return false;
		discard();
		return true;
	}

	public boolean isOrphan(ServerLevel server) {
		UUID parent = getParentId();
		return parent == null || !(server.getEntity(parent) instanceof SeraphaeEntity seraphae && seraphae.isAlive());
	}

	/** True when {@code pos} holds the lower half of a whole, unsnapped block anchor that can wake. */
	public static boolean isDormantAnchor(Level level, BlockPos pos) {
		BlockState lower = level.getBlockState(pos);
		BlockState upper = level.getBlockState(pos.above());
		return lower.is(BlockInit.containment_anchor_block.get())
				&& lower.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.LOWER
				&& !lower.getValue(ContainmentAnchorBlock.SNAPPED)
				&& upper.is(BlockInit.containment_anchor_block.get())
				&& upper.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.UPPER;
	}

	/** The lower half of a whole, unsnapped block anchor: what the arena scans the chamber for. */
	public static boolean dormantLower(BlockState state) {
		return state.is(BlockInit.containment_anchor_block.get())
				&& state.getValue(ContainmentAnchorBlock.HALF) == DoubleBlockHalf.LOWER
				&& !state.getValue(ContainmentAnchorBlock.SNAPPED);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		PARENT.load(this, tag);
		entityData.set(DATA_KIND, tag.getInt("AnchorKind"));
		entityData.set(DATA_RESTRAINT, tag.getInt("Restraint"));
		entityData.set(DATA_STATE, tag.getInt("AnchorState"));
		entityData.set(DATA_PROGRESS, tag.getFloat("Progress"));
		tornOnce = tag.getBoolean("TornOnce");
		home = tag.contains(TAG_HOME) ? BlockPos.of(tag.getLong(TAG_HOME)) : null;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		PARENT.save(this, tag);
		tag.putInt("AnchorKind", entityData.get(DATA_KIND));
		tag.putInt("Restraint", entityData.get(DATA_RESTRAINT));
		tag.putInt("AnchorState", entityData.get(DATA_STATE));
		tag.putFloat("Progress", progress());
		tag.putBoolean("TornOnce", tornOnce);
		if (home != null) tag.putLong(TAG_HOME, home.asLong());
	}
}
