package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import java.util.UUID;

/**
 * Containment Anchor — a static arena element placed by the encounter.
 * <p>
 * When a {@link SeraphaeFragmentEntity} comes within capture range the anchor
 * activates, locks the fragment briefly, then converts it into a containment
 * integrity gain for the parent {@link SeraphaeEntity}.
 * <p>
 * In her chamber an anchor wakes from a dormant {@link ContainmentAnchorBlock}: the block clears while the anchor
 * stands and is built again where it stood when the anchor goes for good (discarded or killed, not unloaded).
 */
public class ContainmentAnchorEntity extends Entity {

	private static final EntityDataAccessor<Boolean> DATA_ACTIVE =
			SynchedEntityData.defineId(ContainmentAnchorEntity.class, EntityDataSerializers.BOOLEAN);

	/** Radius within which fragments are captured. */
	private static final double CAPTURE_RADIUS = 3.5;
	/** Ticks between capture scans. */
	private static final int SCAN_INTERVAL = 10;
	/** How many ticks the anchor remains lit after capturing a fragment. */
	private static final int ACTIVE_DISPLAY_TICKS = 30;

	/** UUID of the parent Seraphae that spawned this anchor. */
	private static final SyncedOwner PARENT = SyncedOwner.define(ContainmentAnchorEntity.class);
	private int activeTimer;

	/** Ticks between checks that the parent Seraphae still exists. */
	private static final int ORPHAN_CHECK_INTERVAL = 100;
	/** Consecutive failed checks, with a player watching, before an orphaned anchor lets go. */
	private static final int ORPHAN_CHECKS = 3;
	/** A player this close keeps the arena loaded, so a missing parent is gone rather than unloaded. */
	private static final double ORPHAN_WITNESS_RANGE = 16.0;
	private static final String TAG_HOME = "HomeAnchor";

	/** The lower half of the block anchor this anchor woke from; {@code null} for a ring anchor. */
	@Nullable
	private BlockPos home;
	private int orphanChecks;

	public ContainmentAnchorEntity(EntityType<?> type, Level level) {
		super(type, level);
		this.noPhysics = true;
	}

	/* ---- sync data ---- */

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ACTIVE, false);
		PARENT.defineDefault(builder);
	}

	public boolean isActive() {
		return this.entityData.get(DATA_ACTIVE);
	}

	/* ---- parent tracking ---- */

	public void setParentId(@Nullable UUID id) {
		PARENT.set(this, id);
	}

	@Nullable
	public UUID getParentId() {
		return PARENT.get(this);
	}

	/* ---- home block ---- */

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

	/**
	 * Wakes this anchor from the block anchor at {@code pos}: both storeys clear, without drops or a shape-update
	 * cascade, and the anchor remembers the cell so it can stand the block back up when it goes.
	 */
	public void wakeFrom(BlockPos pos) {
		this.home = pos.immutable();
		int quiet = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
		this.level().setBlock(pos.above(), Blocks.AIR.defaultBlockState(), quiet);
		this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), quiet);
	}

	@Nullable
	public BlockPos getHome() {
		return home;
	}

	/** Gone for good (discarded or killed): the dormant block anchor stands again where it woke, if its cells are free. */
	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && home != null && this.level() instanceof ServerLevel server) {
			restoreHome(server);
		}
		super.remove(reason);
	}

	private void restoreHome(ServerLevel server) {
		BlockPos lower = home, upper = home.above();
		home = null;
		if (!server.isLoaded(lower) || !server.isLoaded(upper)
				|| !server.getBlockState(lower).isAir() || !server.getBlockState(upper).isAir()) return;
		BlockState dormant = BlockInit.containment_anchor_block.get().defaultBlockState()
				.setValue(ContainmentAnchorBlock.SNAPPED, false);
		server.setBlock(lower, dormant.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
		server.setBlock(upper, dormant.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
	}

	/* ---- tick ---- */

	@Override
	public void tick() {
		super.tick();

		if (this.level().isClientSide) return;

		// Server-side active-display countdown
		if (activeTimer > 0) {
			activeTimer--;
			if (activeTimer == 0) {
				this.entityData.set(DATA_ACTIVE, false);
			}
		}

		if (this.tickCount % ORPHAN_CHECK_INTERVAL == 0 && tickOrphaned((ServerLevel) this.level())) return;

		// Scan for fragments
		if (this.tickCount % SCAN_INTERVAL != 0) return;

		List<SeraphaeFragmentEntity> fragments = this.level().getEntitiesOfClass(
				SeraphaeFragmentEntity.class,
				new AABB(this.blockPosition()).inflate(CAPTURE_RADIUS),
				f -> f.isAlive() && !f.isContained());

		if (fragments.isEmpty()) return;

		// Capture the nearest fragment
		SeraphaeFragmentEntity nearest = null;
		double nearestDist = Double.MAX_VALUE;
		for (SeraphaeFragmentEntity f : fragments) {
			double d = this.distanceToSqr(f);
			if (d < nearestDist) {
				nearestDist = d;
				nearest = f;
			}
		}
		if (nearest == null) return;

		captureFragment(nearest);
	}

	/**
	 * An anchor whose Seraphae was removed while it lay unloaded is an orphan; once a player has stood near it through
	 * a few checks with her nowhere in the level, it lets go (and its block anchor returns).
	 */
	private boolean tickOrphaned(ServerLevel server) {
		if (!isOrphan(server) || server.getNearestPlayer(this, ORPHAN_WITNESS_RANGE) == null) {
			orphanChecks = 0;
			return false;
		}
		if (++orphanChecks < ORPHAN_CHECKS) return false;
		this.discard();
		return true;
	}

	/** Whether this anchor's Seraphae is nowhere alive in the level (an anchor with no parent is never an orphan). */
	public boolean isOrphan(ServerLevel server) {
		UUID parent = getParentId();
		return parent != null && !(server.getEntity(parent) instanceof SeraphaeEntity seraphae && seraphae.isAlive());
	}

	/** Its home is a position in this dimension, so an anchor never travels. */
	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	/**
	 * Pulls the fragment to the anchor, kills it (marking it contained),
	 * and notifies the parent Seraphae for an integrity boost.
	 */
	private void captureFragment(SeraphaeFragmentEntity fragment) {
		Vec3 capturedFrom = fragment.position().add(0, .5, 0);
		// Pull fragment to anchor position
		Vec3 anchorPos = this.position();
		fragment.teleportTo(anchorPos.x, anchorPos.y, anchorPos.z);

		// Mark as anchor-captured before killing so die() doesn't double-notify
		fragment.setAnchorCaptured(true);

		// Kill fragment, which triggers SeraphaeFragmentEntity.die() → sets contained flag
		fragment.hurt(fragment.damageSources().magic(), fragment.getMaxHealth() + 1.0F);

		// Visual/audio feedback
		if (this.level() instanceof ServerLevel server) {
			server.playSound(null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.5F, 1.4F);
			SeraphaeEffects.bind(server, capturedFrom, anchorPos.add(0, .8, 0));
		}

		// Activate visual
		this.entityData.set(DATA_ACTIVE, true);
		activeTimer = ACTIVE_DISPLAY_TICKS;

		// Find parent Seraphae and award integrity
		if (getParentId() != null && this.level() instanceof ServerLevel server) {
			for (SeraphaeEntity seraphae : server.getEntitiesOfClass(
					SeraphaeEntity.class,
					new AABB(this.blockPosition()).inflate(64.0))) {
				if (seraphae.getUUID().equals(getParentId()) && seraphae.isAlive()) {
					seraphae.onFragmentContained(true); // anchor-assisted = bonus
					break;
				}
			}
		}
	}

	/* ---- serialisation ---- */

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID("ParentSeraphae")) {
			setParentId(tag.getUUID("ParentSeraphae"));
		}
		activeTimer = tag.getInt("ActiveTimer");
		this.entityData.set(DATA_ACTIVE, activeTimer > 0);
		home = tag.contains(TAG_HOME) ? BlockPos.of(tag.getLong(TAG_HOME)) : null;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (getParentId() != null) {
			tag.putUUID("ParentSeraphae", getParentId());
		}
		tag.putInt("ActiveTimer", activeTimer);
		if (home != null) {
			tag.putLong(TAG_HOME, home.asLong());
		}
	}
}
