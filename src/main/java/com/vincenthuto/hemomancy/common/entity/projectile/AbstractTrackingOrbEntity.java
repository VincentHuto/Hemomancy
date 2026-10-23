package com.vincenthuto.hemomancy.common.entity.projectile;

import com.google.common.base.Predicates;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * O-2: an orb that picks a living target nearby (players only when evil), homes in on it and strikes it on contact.
 * The kinds differ only in their trail, speed, early lift, damage and what an entity hit does.
 */
public abstract class AbstractTrackingOrbEntity extends ThrowableProjectile {

	private static final String TAG_TIME = "time";
	private static final EntityDataAccessor<Boolean> EVIL = SynchedEntityData.defineId(AbstractTrackingOrbEntity.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(AbstractTrackingOrbEntity.class,
			EntityDataSerializers.INT);

	double lockX, lockY = -1, lockZ;
	int time = 0;

	protected AbstractTrackingOrbEntity(EntityType<? extends AbstractTrackingOrbEntity> type, Level world) {
		super(type, world);
	}

	protected AbstractTrackingOrbEntity(EntityType<? extends AbstractTrackingOrbEntity> type, LivingEntity thrower,
			boolean evil) {
		super(type, thrower, thrower.level());
		setEvil(evil);
	}

	/** Client trail particles, drawn every tick. */
	protected abstract void spawnTrail();

	protected double speed(boolean evil) {
		return evil ? 0.5 : 0.6;
	}

	/** The motion for the first ten ticks: the orb rises before it dives. */
	protected void liftEarly() {
		setDeltaMovement(getDeltaMovement().x(), Math.abs(getDeltaMovement().y()), getDeltaMovement().z());
	}

	protected float damage(boolean evil) {
		return evil ? 12 : 7;
	}

	protected void onEntityHit(EntityHitResult hit) {
		if (hit.getEntity() == getTargetEntity())
			this.remove(RemovalReason.KILLED);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		cmp.putInt(TAG_TIME, time);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(EVIL, false);
		builder.define(TARGET, 0);
	}

	public boolean findTarget() {
		LivingEntity target = getTargetEntity();
		if (target != null && target.isAlive())
			return true;
		if (target != null)
			setTarget(null);

		double range = 22;
		AABB bounds = new AABB(getX() - range, getY() - range, getZ() - range, getX() + range, getY() + range,
				getZ() + range);
		@SuppressWarnings("rawtypes")
		List entities;
		if (isEvil()) {
			entities = level().getEntitiesOfClass(Player.class, bounds);
		} else {
			entities = level().getEntitiesOfClass(Entity.class, bounds, (Predicates.instanceOf(LivingEntity.class)));
			if (entities.contains(this.getOwner())) {
				entities.remove(this.getOwner());
			}
		}
		while (entities.size() > 0) {
			Entity e = (Entity) entities.get(level().random.nextInt(entities.size()));
			if (!(e instanceof LivingEntity) || !e.isAlive()) { // Just in case...
				entities.remove(e);
				continue;
			}

			target = (LivingEntity) e;
			setTarget(target);
			break;
		}

		return target != null;
	}

	@Nonnull

	public LivingEntity getTargetEntity() {
		int id = entityData.get(TARGET);
		Entity e = level().getEntity(id);
		if (e != null && e instanceof LivingEntity)
			return (LivingEntity) e;

		return null;
	}

	public boolean isEvil() {
		return entityData.get(EVIL);
	}

	@Override
	protected void onHit(@Nonnull HitResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			Block block = level().getBlockState(((BlockHitResult) pos).getBlockPos()).getBlock();
			if (!(block instanceof BushBlock) && !(block instanceof LeavesBlock))
				this.remove(RemovalReason.KILLED);
			break;
		}
		case ENTITY: {
			onEntityHit((EntityHitResult) pos);
			break;
		}
		default: {
			this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		time = cmp.getInt(TAG_TIME);
	}

	public void setEvil(boolean evil) {
		entityData.set(EVIL, evil);
	}

	public void setTarget(LivingEntity e) {
		entityData.set(TARGET, e == null ? -1 : e.getId());
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			spawnTrail();
		}
		LivingEntity target = getTargetEntity();
		if (!level().isClientSide && (!findTarget() || time > 40)) {
			this.remove(RemovalReason.KILLED);
			return;
		}

		boolean evil = isEvil();
		Vector3 thisVec = Vector3.fromEntityCenter(this);
		if (target != null) {
			if (lockY == -1) {
				lockX = target.getX();
				lockY = target.getY();
				lockZ = target.getZ();
			}

			Vector3 targetVec = evil ? new Vector3(lockX, lockY, lockZ) : Vector3.fromEntityCenter(target);
			Vector3 diffVec = targetVec.subtract(thisVec);
			Vector3 motionVec = diffVec.normalize().multiply(speed(evil));
			setDeltaMovement(motionVec.toVec3());
			if (time < 10)
				liftEarly();
			List<LivingEntity> targetList = level().getEntitiesOfClass(LivingEntity.class,
					new AABB(getX() - 0.5, getY() - 0.5, getZ() - 0.5, getX() + 0.5, getY() + 0.5, getZ() + 0.5));
			if (targetList.contains(target)) {
				LivingEntity thrower = (LivingEntity) getOwner();
				if (thrower != null) {
					Player player = thrower instanceof Player ? (Player) thrower : null;
					
					if (com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(thrower, target))
                    target.hurt(com.vincenthuto.hemomancy.common.damage.SchoolDamage.projectileSource(
                            player == null ? this.damageSources().mobAttack(thrower) : this.damageSources().playerAttack(player), this, thrower),
							damage(evil));
				} else
					target.hurt(this.damageSources().generic(), damage(evil));

				this.remove(RemovalReason.KILLED);
			}

			if (evil && diffVec.mag() < 0)
				this.remove(RemovalReason.KILLED);
		}

		time++;
	}

}
