package com.vincenthuto.hemomancy.entity.blood;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.network.NetworkHooks;

public class EntityBloodOrbTracking extends ThrowableProjectile {
	public static EntityType<EntityBloodOrbTracking> TYPE = EntityInit.tracking_blood_orb.get();

	private static final String TAG_TIME = "time";
	private static final EntityDataAccessor<Boolean> EVIL = SynchedEntityData.defineId(EntityBloodOrbTracking.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(EntityBloodOrbTracking.class,
			EntityDataSerializers.INT);

	double lockX, lockY = -1, lockZ;
	int time = 0;

	public EntityBloodOrbTracking(EntityType<EntityBloodOrbTracking> type, Level world) {
		super(type, world);
	}

	public EntityBloodOrbTracking(Level world) {
		this(TYPE, world);
	}

	public EntityBloodOrbTracking(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, thrower.level);
		setEvil(evil);
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(EVIL, false);
		entityData.define(TARGET, 0);
	}

	@Nonnull
	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void setEvil(boolean evil) {
		entityData.set(EVIL, evil);
	}

	public boolean isEvil() {
		return entityData.get(EVIL);
	}

	public void setTarget(LivingEntity e) {
		entityData.set(TARGET, e == null ? -1 : e.getId());
	}

	public LivingEntity getTargetEntity() {
		int id = entityData.get(TARGET);
		Entity e = level.getEntity(id);
		if (e != null && e instanceof LivingEntity)
			return (LivingEntity) e;

		return null;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide) {
			level.addParticle(GlowParticleFactory.createData(new ParticleColor(200, 0, 0)),
					getX() + HLParticleUtils.inRange(-0.1, 0.1), getY() + HLParticleUtils.inRange(-0.1, 0.1),
					getZ() + HLParticleUtils.inRange(-0.1, 0.1), 0, 0.005, 0);
			level.addParticle(ParticleTypes.ASH, this.getX() + (Math.random() - 0.5) * 0.4,
					this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);
			level.addParticle(DustParticleOptions.REDSTONE, this.getX() + (Math.random() - 0.5) * 0.4,
					this.getY() + (Math.random() - 0.5) * 0.4, this.getZ() + (Math.random() - 0.5) * 0.4, 0, 0, 0);
		}
		LivingEntity target = getTargetEntity();
		if (!level.isClientSide && (!findTarget() || time > 40)) {
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
			Vector3 motionVec = diffVec.normalize().multiply(evil ? 0.5 : 0.6);
			setDeltaMovement(motionVec.toVec3());
			if (time < 10)
				setDeltaMovement(getDeltaMovement().x(), Math.abs(getDeltaMovement().y()), getDeltaMovement().z());
			List<LivingEntity> targetList = level.getEntitiesOfClass(LivingEntity.class,
					new AABB(getX() - 0.5, getY() - 0.5, getZ() - 0.5, getX() + 0.5, getY() + 0.5, getZ() + 0.5));
			if (targetList.contains(target)) {
				LivingEntity thrower = (LivingEntity) getOwner();
				if (thrower != null) {
					Player player = thrower instanceof Player ? (Player) thrower : null;
					target.hurt(player == null ? DamageSource.mobAttack(thrower) : DamageSource.playerAttack(player),
							evil ? 12 : 7);
				} else
					target.hurt(DamageSource.GENERIC, evil ? 12 : 7);

				this.remove(RemovalReason.KILLED);
			}

			if (evil && diffVec.mag() < 0)
				this.remove(RemovalReason.KILLED);
		}

		time++;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		cmp.putInt(TAG_TIME, time);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		time = cmp.getInt(TAG_TIME);
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
			entities = level.getEntitiesOfClass(Player.class, bounds);
		} else {
			entities = level.getEntitiesOfClass(Entity.class, bounds, (Predicates.instanceOf(LivingEntity.class)));
			if (entities.contains(this.getOwner())) {
				entities.remove(this.getOwner());
			}
		}
		while (entities.size() > 0) {
			Entity e = (Entity) entities.get(level.random.nextInt(entities.size()));
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

	@Override
	protected void onHit(@Nonnull HitResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			Block block = level.getBlockState(((BlockHitResult) pos).getBlockPos()).getBlock();
			if (!(block instanceof BushBlock) && !(block instanceof LeavesBlock))
				this.remove(RemovalReason.KILLED);
			break;
		}
		case ENTITY: {
			if (((EntityHitResult) pos).getEntity() == getTargetEntity())
				this.remove(RemovalReason.KILLED);
			break;
		}
		default: {
			this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

}