package com.vincenthuto.hemomancy.entity.projectile;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.PotionInit;
import com.vincenthuto.hemomancy.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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

public class EntityTrackingSerpent extends ThrowableProjectile {
	public static EntityType<EntityTrackingSerpent> TYPE = EntityInit.tracking_snake.get();

	private static final String TAG_TIME = "time";
	private static final EntityDataAccessor<Boolean> EVIL = SynchedEntityData.defineId(EntityTrackingSerpent.class,
			EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(EntityTrackingSerpent.class,
			EntityDataSerializers.INT);

	double lockX, lockY = -1, lockZ;
	int time = 0;

	public EntityTrackingSerpent(EntityType<EntityTrackingSerpent> type, Level world) {
		super(type, world);
	}

	public EntityTrackingSerpent(Level world) {
		this(TYPE, world);
	}

	public EntityTrackingSerpent(LivingEntity thrower, boolean evil) {
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
			level.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 100, 0)),
					this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
					this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			level.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 100, 0)),
					this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
					this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			for (int i = 0; i < 10; i++) {
				level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
						this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
				level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
						this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
				level.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getX() + (Math.random() - 0.5) * 0.1, this.getY() + (Math.random() - 0.5) * 0.1,
						this.getZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			}

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
			// This normalize part changes how angular the beam gets
			Vector3 motionVec = diffVec.normalize().multiply(evil ? 0.15 : 0.6);
			setDeltaMovement(motionVec.toVec3());
			if (time < 10)
				setDeltaMovement(getDeltaMovement().x() + HLParticleUtils.inRange(-0.15, 0.15),
						Math.abs(getDeltaMovement().y()), getDeltaMovement().z() + HLParticleUtils.inRange(-0.15, 0.15));
			List<LivingEntity> targetList = level.getEntitiesOfClass(LivingEntity.class,
					new AABB(getX() - 0.5, getY() - 0.5, getZ() - 0.5, getX() + 0.5, getY() + 0.5, getZ() + 0.5));
			if (targetList.contains(target)) {
				LivingEntity thrower = (LivingEntity) getOwner();
				if (thrower != null) {
					Player player = thrower instanceof Player ? (Player) thrower : null;
					target.hurt(player == null ? DamageSource.mobAttack(thrower) : DamageSource.playerAttack(player),
							evil ? 2 : 2);
				} else
					target.hurt(DamageSource.GENERIC, evil ? 2 : 2);

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
			if (!(pos instanceof BlockHitResult)) {
				if (((EntityHitResult) pos).getEntity() == getTargetEntity())
					((LivingEntity) ((EntityHitResult) pos).getEntity())
							.addEffect(new MobEffectInstance(PotionInit.blood_binding.get(), 300));
				this.remove(RemovalReason.KILLED);
			}
			break;
		}
		default: {
			this.remove(RemovalReason.KILLED);
			break;
		}
		}
	}

}