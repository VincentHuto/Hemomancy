package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** A knot of a thread's tendency flung at the weaver; its hit applies that tendency's harm. */
public class ThreadBoltEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(ThreadBoltEntity.class, EntityDataSerializers.BYTE);
	private float power = 1.0F;

	public ThreadBoltEntity(EntityType<? extends ThreadBoltEntity> type, Level level) {
		super(type, level);
	}

	public ThreadBoltEntity(Level level, FrayedThreadEntity owner, EnumBloodTendency tendency, float power) {
		super(EntityInit.thread_bolt.get(), owner, level);
		this.entityData.set(TENDENCY, (byte) tendency.ordinal());
		this.power = power;
	}

	public EnumBloodTendency tendency() {
		return EnumBloodTendency.values()[entityData.get(TENDENCY)];
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TENDENCY, (byte) 0);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.0D;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide)
			level().addParticle(HemoParticleData.glow(tendency().getColor()), getX(), getY(), getZ(), 0, 0, 0);
		else if (tickCount > 60) discard();
	}

	/** Bolts pass through helpers, bystanders and mobs: they strike only the weaver their thread hunts. */
	public boolean mayStrike(net.minecraft.world.entity.Entity target) {
		return getOwner() instanceof FrayedThreadEntity thread && thread.hunts(target);
	}

	@Override
	protected boolean canHitEntity(net.minecraft.world.entity.Entity target) {
		return super.canHitEntity(target) && mayStrike(target);
	}

	@Override
	protected void onHitEntity(EntityHitResult hit) {
		if (!level().isClientSide && hit.getEntity() instanceof LivingEntity target && getOwner() instanceof FrayedThreadEntity thread
				&& !(target instanceof FrayedThreadEntity))
			ThreadAttacks.onBoltHit(tendency(), thread, target, power);
		discard();
	}

	@Override
	protected void onHitBlock(BlockHitResult hit) {
		super.onHitBlock(hit);
		if (!level().isClientSide && tendency() == EnumBloodTendency.FLAMMEUS && getOwner() instanceof FrayedThreadEntity thread
				&& level() instanceof net.minecraft.server.level.ServerLevel server)
			ThreadHazardEntity.spawn(server, thread, hit.getLocation(), EnumBloodTendency.FLAMMEUS, 1.5F, 60, power);
		discard();
	}
}
