package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class VeinwingFeatherEntity extends ThrowableProjectile {
	private static final float DAMAGE = 1.25F;
	private static final int FLIGHT_LIFETIME_TICKS = 30;
	private static final int EMBEDDED_DURATION_TICKS = 100;
	private static final EntityDataAccessor<Integer> EMBEDDED_TARGET_ID =
			SynchedEntityData.defineId(VeinwingFeatherEntity.class, EntityDataSerializers.INT);
	private UUID embeddedTargetUuid;
	private int embeddedTicks;

	public VeinwingFeatherEntity(EntityType<? extends VeinwingFeatherEntity> type, Level level) {
		super(type, level);
	}

	public VeinwingFeatherEntity(Level level, LivingEntity owner) {
		super(EntityInit.veinwing_feather.get(), owner, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(EMBEDDED_TARGET_ID, -1);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (isEmbedded() || !super.canHitEntity(entity) || !(entity instanceof LivingEntity target)
				|| !(getOwner() instanceof Mob owner) || !(owner instanceof BoundPuppeteerSummon summon)) return false;
		return BoundSummonBehavior.canAttack(owner, summon, target);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target
				&& getOwner() instanceof LivingEntity owner) {
			target.hurt(target.damageSources().mobProjectile(this, owner), DAMAGE);
			embedIn(target);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		discard();
	}

	@Override
	protected double getDefaultGravity() {
		return 0.015D;
	}

	@Override
	public void tick() {
		if (isEmbedded()) {
			noPhysics = true;
			setNoGravity(true);
			setDeltaMovement(Vec3.ZERO);
		}
		super.tick();
		if (isEmbedded()) {
			tickEmbedded();
		} else if (!level().isClientSide && tickCount >= FLIGHT_LIFETIME_TICKS) {
			discard();
		}
	}

	public boolean isEmbedded() {
		return entityData.get(EMBEDDED_TARGET_ID) >= 0 || embeddedTargetUuid != null;
	}

	private void embedIn(LivingEntity target) {
		entityData.set(EMBEDDED_TARGET_ID, target.getId());
		embeddedTargetUuid = target.getUUID();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(Vec3.ZERO);
		applyMaxHealthPenalty(target);
	}

	private void tickEmbedded() {
		LivingEntity target = embeddedTarget();
		if (target == null) {
			if (!level().isClientSide) discard();
			return;
		}
		if (!level().isClientSide) {
			if (!target.isAlive() || ++embeddedTicks >= EMBEDDED_DURATION_TICKS) {
				discard();
				return;
			}
			applyMaxHealthPenalty(target);
		}
		long seed = getUUID().getLeastSignificantBits();
		double angle = (seed & 1023L) * (Math.PI * 2.0D / 1024.0D);
		double radius = target.getBbWidth() * 0.38D;
		double y = target.getY() + target.getBbHeight() * (0.3D + ((seed >>> 10) & 255L) / 640.0D);
		setPos(target.getX() + Math.cos(angle) * radius, y, target.getZ() + Math.sin(angle) * radius);
	}

	private LivingEntity embeddedTarget() {
		Entity target = level().getEntity(entityData.get(EMBEDDED_TARGET_ID));
		if (target instanceof LivingEntity living) return living;
		if (embeddedTargetUuid != null && level() instanceof ServerLevel serverLevel) {
			target = serverLevel.getEntity(embeddedTargetUuid);
			if (target instanceof LivingEntity living) {
				entityData.set(EMBEDDED_TARGET_ID, living.getId());
				return living;
			}
		}
		return null;
	}

	private void applyMaxHealthPenalty(LivingEntity target) {
		AttributeInstance maxHealth = target.getAttribute(Attributes.MAX_HEALTH);
		ResourceLocation id = modifierId();
		if (maxHealth != null && maxHealth.getModifier(id) == null) {
			maxHealth.addTransientModifier(new AttributeModifier(id, -1.0D, AttributeModifier.Operation.ADD_VALUE));
			target.setHealth(Math.min(target.getHealth(), target.getMaxHealth()));
		}
	}

	private void removeMaxHealthPenalty() {
		LivingEntity target = embeddedTarget();
		AttributeInstance maxHealth = target == null ? null : target.getAttribute(Attributes.MAX_HEALTH);
		if (maxHealth != null) maxHealth.removeModifier(modifierId());
	}

	private ResourceLocation modifierId() {
		return Hemomancy.rloc("veinwing_feather/" + getStringUUID());
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide) removeMaxHealthPenalty();
		super.remove(reason);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (embeddedTargetUuid != null) tag.putUUID("EmbeddedTarget", embeddedTargetUuid);
		tag.putInt("EmbeddedTicks", embeddedTicks);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		embeddedTargetUuid = tag.hasUUID("EmbeddedTarget") ? tag.getUUID("EmbeddedTarget") : null;
		embeddedTicks = tag.getInt("EmbeddedTicks");
	}
}
