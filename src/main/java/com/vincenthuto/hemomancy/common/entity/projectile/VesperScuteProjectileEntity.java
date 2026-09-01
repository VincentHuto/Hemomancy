package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperEncounterPuppetEvents;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class VesperScuteProjectileEntity extends ThrowableProjectile {
	private Vec3 origin = Vec3.ZERO;

	public VesperScuteProjectileEntity(EntityType<? extends VesperScuteProjectileEntity> type, Level level) {
		super(type, level);
	}

	public VesperScuteProjectileEntity(Level level, VesperTheCrownedRefusalEntity owner) {
		super(EntityInit.vesper_scute_projectile.get(), owner, level);
		origin = owner.position();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		Entity owner = getOwner();
		if (!(entity instanceof LivingEntity target) || !(owner instanceof LivingEntity livingOwner)) return false;
		boolean ownedPuppet = entity.getPersistentData().hasUUID(VesperEncounterPuppetEvents.BOSS_KEY)
				&& owner != null && owner.getUUID().equals(entity.getPersistentData().getUUID(VesperEncounterPuppetEvents.BOSS_KEY));
		return super.canHitEntity(entity) && VesperScuteProjectileRules.mayHit(entity == owner,
				entity instanceof VesperTheCrownedRefusalEntity || entity instanceof VesperTheEveningStarEntity,
				ownedPuppet, entity.isAlive(),
				livingOwner.canAttack(target));
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target
				&& getOwner() instanceof VesperTheCrownedRefusalEntity boss
				&& target.hurt(HemoDamageTypes.vesperScute(level(), this, boss), 6.0F)) {
			Vec3 away = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() > 1.0E-4D) {
				away = away.normalize().scale(0.45D);
				target.push(away.x, 0.16D, away.z);
			}
		}
		discard();
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		discard();
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && VesperScuteProjectileRules.shouldExpire(tickCount, position().distanceToSqr(origin))) discard();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putDouble("ScuteOriginX", origin.x);
		tag.putDouble("ScuteOriginY", origin.y);
		tag.putDouble("ScuteOriginZ", origin.z);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		origin = new Vec3(tag.getDouble("ScuteOriginX"), tag.getDouble("ScuteOriginY"), tag.getDouble("ScuteOriginZ"));
	}
}
