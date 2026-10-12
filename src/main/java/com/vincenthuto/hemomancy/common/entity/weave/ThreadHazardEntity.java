package com.vincenthuto.hemomancy.common.entity.weave;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/** A lingering patch a thread leaves on the ground: Flammeus embers burn, Mortem miasma sickens. */
public class ThreadHazardEntity extends Entity {
	private static final EntityDataAccessor<Byte> TENDENCY = SynchedEntityData.defineId(ThreadHazardEntity.class, EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(ThreadHazardEntity.class, EntityDataSerializers.FLOAT);
	private int life;
	private float power = 1.0F;
	@Nullable private UUID owner;

	public ThreadHazardEntity(EntityType<? extends ThreadHazardEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
	}

	public static ThreadHazardEntity spawn(ServerLevel level, FrayedThreadEntity owner, Vec3 at, EnumBloodTendency tendency,
			float radius, int ticks, float power) {
		ThreadHazardEntity hazard = new ThreadHazardEntity(EntityInit.thread_hazard.get(), level);
		hazard.entityData.set(TENDENCY, (byte) tendency.ordinal());
		hazard.entityData.set(RADIUS, radius);
		hazard.life = ticks;
		hazard.power = power;
		hazard.owner = owner.getUUID();
		hazard.moveTo(at.x, at.y, at.z);
		level.addFreshEntity(hazard);
		return hazard;
	}

	public EnumBloodTendency tendency() { return EnumBloodTendency.values()[entityData.get(TENDENCY)]; }
	public float radius() { return entityData.get(RADIUS); }

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TENDENCY, (byte) 0);
		builder.define(RADIUS, 1.5F);
	}

	@Override
	public void tick() {
		super.tick();
		float r = radius();
		if (level().isClientSide) {
			for (int i = 0; i < 2; i++) {
				double a = random.nextDouble() * Math.PI * 2, d = random.nextDouble() * r;
				level().addParticle(tendency() == EnumBloodTendency.MORTEM ? HemoParticleData.darkGlow(tendency().getColor())
								: HemoParticleData.ember(tendency().getColor(), 0.08F, 0.8F, 18),
						getX() + Math.cos(a) * d, getY() + 0.1, getZ() + Math.sin(a) * d, 0, 0.03, 0);
			}
			return;
		}
		if (--life <= 0) {
			discard();
			return;
		}
		if (tickCount % 20 != 0 || !(level() instanceof ServerLevel server)) return;
		FrayedThreadEntity thread = owner != null && server.getEntity(owner) instanceof FrayedThreadEntity t ? t : null;
		// A patch harms only the weaver its thread hunts; once that thread is gone it only fades.
		if (thread == null) return;
		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(r, 1.0, r),
				e -> e.isAlive() && thread.hunts(e) && e.distanceToSqr(this) <= r * r + 1)) {
			ThreadAttacks.markWeaveHarm(victim, thread);
			if (tendency() == EnumBloodTendency.MORTEM) {
				victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
				victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, power >= 2.0F ? 1 : 0));
			} else {
				victim.hurt(damageSources().mobAttack(thread), power);
				victim.igniteForSeconds(1.0F);
			}
		}
	}

	@Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
	@Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
