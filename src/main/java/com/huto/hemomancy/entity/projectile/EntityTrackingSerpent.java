package com.huto.hemomancy.entity.projectile;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.huto.hemomancy.init.EntityInit;
import com.huto.hemomancy.init.PotionInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.huto.hemomancy.particle.util.ParticleUtil;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityTrackingSerpent extends ThrowableEntity {
	public static EntityType<EntityTrackingSerpent> TYPE = EntityInit.tracking_snake.get();

	private static final String TAG_TIME = "time";
	private static final DataParameter<Boolean> EVIL = EntityDataManager.createKey(EntityTrackingSerpent.class,
			DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> TARGET = EntityDataManager.createKey(EntityTrackingSerpent.class,
			DataSerializers.VARINT);

	double lockX, lockY = -1, lockZ;
	int time = 0;

	public EntityTrackingSerpent(EntityType<EntityTrackingSerpent> type, World world) {
		super(type, world);
	}

	public EntityTrackingSerpent(World world) {
		this(TYPE, world);
	}

	public EntityTrackingSerpent(LivingEntity thrower, boolean evil) {
		super(TYPE, thrower, thrower.world);
		setEvil(evil);
	}

	@Override
	protected void registerData() {
		dataManager.register(EVIL, false);
		dataManager.register(TARGET, 0);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public void setEvil(boolean evil) {
		dataManager.set(EVIL, evil);
	}

	public boolean isEvil() {
		return dataManager.get(EVIL);
	}

	public void setTarget(LivingEntity e) {
		dataManager.set(TARGET, e == null ? -1 : e.getEntityId());
	}

	public LivingEntity getTargetEntity() {
		int id = dataManager.get(TARGET);
		Entity e = world.getEntityByID(id);
		if (e != null && e instanceof LivingEntity)
			return (LivingEntity) e;

		return null;
	}

	@Override
	public void tick() {
		super.tick();
		if (world.isRemote) {
			world.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 100, 0)),
					this.getPosX() + (Math.random() - 0.5) * 0.1, this.getPosY() + (Math.random() - 0.5) * 0.1,
					this.getPosZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			world.addParticle(SerpentParticleFactory.createData(new ParticleColor(100, 100, 0)),
					this.getPosX() + (Math.random() - 0.5) * 0.1, this.getPosY() + (Math.random() - 0.5) * 0.1,
					this.getPosZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			for (int i = 0; i < 10; i++) {
				world.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getPosX() + (Math.random() - 0.5) * 0.1, this.getPosY() + (Math.random() - 0.5) * 0.1,
						this.getPosZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
				world.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getPosX() + (Math.random() - 0.5) * 0.1, this.getPosY() + (Math.random() - 0.5) * 0.1,
						this.getPosZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
				world.addParticle(SerpentParticleFactory.createData(new ParticleColor(255, 50, 50)),
						this.getPosX() + (Math.random() - 0.5) * 0.1, this.getPosY() + (Math.random() - 0.5) * 0.1,
						this.getPosZ() + (Math.random() - 0.5) * 0.1, 0, 0, 0);
			}

		}
		LivingEntity target = getTargetEntity();
		if (!world.isRemote && (!findTarget() || time > 40)) {
			remove();
			return;
		}

		boolean evil = isEvil();
		Vector3 thisVec = Vector3.fromEntityCenter(this);
		if (target != null) {
			if (lockY == -1) {
				lockX = target.getPosX();
				lockY = target.getPosY();
				lockZ = target.getPosZ();
			}

			Vector3 targetVec = evil ? new Vector3(lockX, lockY, lockZ) : Vector3.fromEntityCenter(target);
			Vector3 diffVec = targetVec.subtract(thisVec);
			// This normalize part changes how angular the beam gets
			Vector3 motionVec = diffVec.normalize().multiply(evil ? 0.15 : 0.6);
			setMotion(motionVec.toVector3d());
			if (time < 10)
				setMotion(getMotion().getX() + ParticleUtil.inRange(-0.15, 0.15), Math.abs(getMotion().getY()),
						getMotion().getZ() + ParticleUtil.inRange(-0.15, 0.15));
			List<LivingEntity> targetList = world.getEntitiesWithinAABB(LivingEntity.class,
					new AxisAlignedBB(getPosX() - 0.5, getPosY() - 0.5, getPosZ() - 0.5, getPosX() + 0.5,
							getPosY() + 0.5, getPosZ() + 0.5));
			if (targetList.contains(target)) {
				LivingEntity thrower = (LivingEntity) func_234616_v_();
				if (thrower != null) {
					PlayerEntity player = thrower instanceof PlayerEntity ? (PlayerEntity) thrower : null;
					target.attackEntityFrom(player == null ? DamageSource.causeMobDamage(thrower)
							: DamageSource.causePlayerDamage(player), evil ? 2 : 2);
				} else
					target.attackEntityFrom(DamageSource.GENERIC, evil ? 2 : 2);

				remove();
			}

			if (evil && diffVec.mag() < 0)
				remove();
		}

		time++;
	}

	@Override
	public void writeAdditional(CompoundNBT cmp) {
		super.writeAdditional(cmp);
		cmp.putInt(TAG_TIME, time);
	}

	@Override
	public void readAdditional(CompoundNBT cmp) {
		super.readAdditional(cmp);
		time = cmp.getInt(TAG_TIME);
	}

	public boolean findTarget() {
		LivingEntity target = getTargetEntity();
		if (target != null && target.isAlive())
			return true;
		if (target != null)
			setTarget(null);

		double range = 22;
		AxisAlignedBB bounds = new AxisAlignedBB(getPosX() - range, getPosY() - range, getPosZ() - range,
				getPosX() + range, getPosY() + range, getPosZ() + range);
		@SuppressWarnings("rawtypes")
		List entities;
		if (isEvil()) {
			entities = world.getEntitiesWithinAABB(PlayerEntity.class, bounds);
		} else {
			entities = world.getEntitiesWithinAABB(Entity.class, bounds, (Predicates.instanceOf(LivingEntity.class)));
			if (entities.contains(this.func_234616_v_())) {
				entities.remove(this.func_234616_v_());
			}
		}
		while (entities.size() > 0) {
			Entity e = (Entity) entities.get(world.rand.nextInt(entities.size()));
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
	protected void onImpact(@Nonnull RayTraceResult pos) {
		switch (pos.getType()) {
		case BLOCK: {
			Block block = world.getBlockState(((BlockRayTraceResult) pos).getPos()).getBlock();
			if (!(block instanceof BushBlock) && !(block instanceof LeavesBlock))
				remove();
			break;
		}
		case ENTITY: {
			if (!(pos instanceof BlockRayTraceResult)) {
				if (((EntityRayTraceResult) pos).getEntity() == getTargetEntity())
					((LivingEntity) ((EntityRayTraceResult) pos).getEntity())
							.addPotionEffect(new EffectInstance(PotionInit.blood_binding.get(), 300));
				remove();
			}
			break;
		}
		default: {
			remove();
			break;
		}
		}
	}

}