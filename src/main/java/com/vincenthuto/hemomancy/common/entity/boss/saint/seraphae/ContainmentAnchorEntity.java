package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

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
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (getParentId() != null) {
			tag.putUUID("ParentSeraphae", getParentId());
		}
		tag.putInt("ActiveTimer", activeTimer);
	}
}
