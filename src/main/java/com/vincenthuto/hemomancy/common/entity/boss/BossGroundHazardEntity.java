package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.entity.SyncedOwner;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A lingering ground hazard a boss leaves (Hemorath's standing iron and tide, Seraphae's healed pads, Putriciel's
 * colonies, fire patches and breath cloud, Velorum's black ice): a footprint and its frame, a kind its owner reads, a
 * life and a pulse. On each pulse its owner gets the living bodies inside and applies the effect. It draws nothing of
 * its own: the telegraph pass paints it from the synced shape. One that loads with no live owner discards itself.
 */
public final class BossGroundHazardEntity extends Entity {
	/** The boss behind a hazard. */
	public interface Owner {
		/** The effect, on its pulse. */
		void hazardTick(BossGroundHazardEntity hazard, List<LivingEntity> inside);

		default boolean hazardTouches(LivingEntity entity) {
			return true;
		}
	}

	private static final EntityDataAccessor<Integer> DATA_OWNER =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_KIND =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_LIFE =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_FADE =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_FADE_AT =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<CompoundTag> DATA_SHAPE =
			SynchedEntityData.defineId(BossGroundHazardEntity.class, EntityDataSerializers.COMPOUND_TAG);
	private static final SyncedOwner OWNER = SyncedOwner.define(BossGroundHazardEntity.class);

	/** How far round its frame's origin a hazard looks for bodies; shapes reach no further. */
	private static final double SEARCH = 24.0D;
	/** Ticks a hazard waits for its owner to load before it gives up. */
	private static final int OWNER_GRACE = 40;

	private int pulseEvery = 10;
	private int fadeLeft = -1;
	@Nullable
	private BossFootprint shape;
	@Nullable
	private FootprintFrame frame;

	public BossGroundHazardEntity(EntityType<? extends BossGroundHazardEntity> type, Level level) {
		super(type, level);
		this.noPhysics = true;
		this.setNoGravity(true);
	}

	public static BossGroundHazardEntity spawn(ServerLevel level, Entity owner, int kind, BossFootprint shape,
			FootprintFrame frame, int lifeTicks, int pulseEvery) {
		BossGroundHazardEntity hazard = new BossGroundHazardEntity(EntityInit.boss_ground_hazard.get(), level);
		OWNER.set(hazard, owner.getUUID());
		hazard.pulseEvery = Math.max(1, pulseEvery);
		hazard.shape = shape;
		hazard.frame = frame;
		hazard.setPos(frame.origin());
		hazard.entityData.set(DATA_OWNER, owner.getId());
		hazard.entityData.set(DATA_KIND, kind);
		hazard.entityData.set(DATA_LIFE, Math.max(1, lifeTicks));
		hazard.entityData.set(DATA_SHAPE, pack(shape, frame));
		level.addFreshEntity(hazard);
		return hazard;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_OWNER, -1);
		builder.define(DATA_KIND, 0);
		builder.define(DATA_LIFE, 1);
		builder.define(DATA_FADE, -1);
		builder.define(DATA_FADE_AT, 0);
		builder.define(DATA_SHAPE, new CompoundTag());
		OWNER.defineDefault(builder);
	}

	public int kind() {
		return entityData.get(DATA_KIND);
	}

	/** Its whole life in ticks; its age is its own tick count. */
	public int life() {
		return entityData.get(DATA_LIFE);
	}

	/** The fade's length once {@link #expire} has been called, else -1. */
	public int fade() {
		return entityData.get(DATA_FADE);
	}

	public BossFootprint shape() {
		if (shape == null) unpack();
		return shape == null ? BossFootprint.None.INSTANCE : shape;
	}

	public FootprintFrame frame() {
		if (frame == null) unpack();
		return frame == null ? FootprintFrame.simple(position(), position(), position(), getY()) : frame;
	}

	@Nullable
	public Entity owner() {
		UUID ownerId = OWNER.get(this);
		if (level() instanceof ServerLevel server && ownerId != null) return server.getEntity(ownerId);
		int id = entityData.get(DATA_OWNER);
		return id < 0 ? null : level().getEntity(id);
	}

	/** The tick count at which {@link #expire} was called; the fade runs {@link #fade()} ticks from it. */
	public int fadeStart() {
		return entityData.get(DATA_FADE_AT);
	}

	/** Ends the hazard after {@code fadeTicks}; it touches nothing while it fades. */
	public void expire(int fadeTicks) {
		if (fadeLeft >= 0) return;
		fadeLeft = Math.max(0, fadeTicks);
		entityData.set(DATA_FADE_AT, tickCount);
		entityData.set(DATA_FADE, fadeLeft);
	}

	public boolean fading() {
		return fadeLeft >= 0 || entityData.get(DATA_FADE) >= 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel)) return;
		Entity owner = owner();
		if (owner == null || owner.isRemoved()) {
			if (tickCount > OWNER_GRACE || owner != null) discard();
			return;
		}
		if (fadeLeft >= 0) {
			if (fadeLeft-- <= 0) discard();
			return;
		}
		if (tickCount >= life()) {
			expire(10);
			return;
		}
		if (tickCount % pulseEvery == 0 && owner instanceof Owner boss) boss.hazardTick(this, inside(boss));
	}

	/** The living bodies inside the footprint now, height band included, that the owner says it touches. */
	public List<LivingEntity> inside(Owner boss) {
		FootprintFrame current = frame();
		BossFootprint footprint = shape();
		AABB search = new AABB(current.origin(), current.origin()).inflate(SEARCH, 6.0D, SEARCH);
		List<LivingEntity> inside = new ArrayList<>();
		for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, search, LivingEntity::isAlive)) {
			if (entity == owner()) continue;
			if (footprint.hits(current, entity.position(), entity.onGround(), tickCount) && boss.hazardTouches(entity)) {
				inside.add(entity);
			}
		}
		return inside;
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
	public boolean isAttackable() {
		return false;
	}

	@Override
	public boolean shouldBeSaved() {
		return super.shouldBeSaved() && OWNER.get(this) != null;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		OWNER.load(this, tag);
		pulseEvery = Math.max(1, tag.getInt("PulseEvery"));
		fadeLeft = tag.contains("Fade") ? tag.getInt("Fade") : -1;
		entityData.set(DATA_KIND, tag.getInt("Kind"));
		entityData.set(DATA_LIFE, Math.max(1, tag.getInt("Life")));
		entityData.set(DATA_FADE, fadeLeft);
		entityData.set(DATA_FADE_AT, tag.getInt("FadeAt"));
		entityData.set(DATA_SHAPE, tag.getCompound("Shape"));
		shape = null;
		frame = null;
		this.tickCount = tag.getInt("Age");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		OWNER.save(this, tag);
		tag.putInt("PulseEvery", pulseEvery);
		tag.putInt("Fade", fadeLeft);
		tag.putInt("FadeAt", fadeStart());
		tag.putInt("Kind", kind());
		tag.putInt("Life", life());
		tag.put("Shape", entityData.get(DATA_SHAPE));
		tag.putInt("Age", tickCount);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DATA_SHAPE.equals(key)) {
			shape = null;
			frame = null;
		}
	}

	private static CompoundTag pack(BossFootprint shape, FootprintFrame frame) {
		CompoundTag tag = new CompoundTag();
		tag.put("shape", BossFootprints.save(shape));
		tag.put("frame", frame.save());
		return tag;
	}

	private void unpack() {
		CompoundTag tag = entityData.get(DATA_SHAPE);
		if (!tag.contains("shape")) return;
		shape = BossFootprints.load(tag.getCompound("shape"));
		frame = FootprintFrame.load(tag.getCompound("frame"));
	}

	/** A hazard sits on the ground at its frame's origin. */
	public Vec3 anchor() {
		return frame().origin();
	}
}
