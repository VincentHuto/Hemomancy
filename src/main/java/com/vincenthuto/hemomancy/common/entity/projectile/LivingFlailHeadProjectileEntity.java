package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailDeployment;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingFlailRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public final class LivingFlailHeadProjectileEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Float> CHARGE = SynchedEntityData.defineId(
			LivingFlailHeadProjectileEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<String> PRIMARY_TEMPER = SynchedEntityData.defineId(
			LivingFlailHeadProjectileEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> SECONDARY_TEMPER = SynchedEntityData.defineId(
			LivingFlailHeadProjectileEntity.class, EntityDataSerializers.STRING);
	private UUID deploymentId;
	private UUID ownerId;
	private InteractionHand originalHand = InteractionHand.MAIN_HAND;
	private boolean impacted;
	private Vec3 lastSafePosition = Vec3.ZERO;

	public LivingFlailHeadProjectileEntity(EntityType<? extends LivingFlailHeadProjectileEntity> type, Level level) {
		super(type, level);
	}

	public LivingFlailHeadProjectileEntity(Level level, LivingEntity owner) {
		super(EntityInit.living_flail_head.get(), owner, level);
		ownerId = owner.getUUID();
		deploymentId = getUUID();
		lastSafePosition = position();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(CHARGE, 0.0F);
		builder.define(PRIMARY_TEMPER, EnumBloodTemper.CONGEATIO.name());
		builder.define(SECONDARY_TEMPER, "");
	}

	public void configure(float charge, InteractionHand hand, EnumBloodTemper primary,
			@Nullable EnumBloodTemper secondary) {
		entityData.set(CHARGE, Mth.clamp(charge, 0.0F, 1.0F));
		originalHand = hand;
		entityData.set(PRIMARY_TEMPER, (primary == null ? EnumBloodTemper.CONGEATIO : primary).name());
		entityData.set(SECONDARY_TEMPER, secondary == null ? "" : secondary.name());
        com.vincenthuto.hemomancy.common.damage.SchoolDamage.capture(this,
                com.vincenthuto.hemomancy.common.damage.SchoolHitContext.direct(
                        com.vincenthuto.hemomancy.Hemomancy.rloc("living_flail"),
                        primary == null ? EnumBloodTemper.CONGEATIO : primary, secondary, getOwner())
                        .withCharge(charge).withApplication(LivingFlailRules.slownessTicks(charge), 1));
	}

	public float getCharge() {
		return entityData.get(CHARGE);
	}

	public UUID getDeploymentId() {
		return deploymentId == null ? getUUID() : deploymentId;
	}

	@Nullable
	public UUID getOwnerUuid() {
		Entity owner = getOwner();
		return owner != null ? owner.getUUID() : ownerId;
	}

	public EnumBloodTemper getPrimaryTemper() {
		return parseTemper(entityData.get(PRIMARY_TEMPER), EnumBloodTemper.CONGEATIO);
	}

	@Nullable
	public EnumBloodTemper getSecondaryTemper() {
		return parseTemper(entityData.get(SECONDARY_TEMPER), null);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity target)
				|| !(getOwner() instanceof LivingEntity owner)) return false;
		return LivingFlailImpactRules.isValidTarget(target == owner, target.isAlliedTo(owner), target.isAlive(),
				owner.canAttack(target));
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		impactOnce(result.getLocation(), false);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		impactOnce(result.getLocation(), false);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		ServerPlayer owner = resolveOwner();
		if (owner == null || owner.level() != level()) {
			if (owner != null) LivingFlailDeployment.restoreHead(owner, getDeploymentId());
			discard();
			return;
		}
		if (!LivingFlailDeployment.hasDeployment(owner, getDeploymentId())) {
			discard();
			return;
		}
		if (level().noCollision(getBoundingBox())) lastSafePosition = position();
		LivingFlailImpactEffects.emitProjectileTrail((ServerLevel) level(), this);
		if (tickCount >= LivingFlailRules.lifetimeTicks(getCharge())) impactOnce(lastSafePosition, true);
	}

	private void impactOnce(Vec3 center, boolean timeout) {
		if (level().isClientSide || !LivingFlailImpactRules.mayImpact(impacted)) return;
		impacted = true;
		LivingFlailImpactEffects.impact((ServerLevel) level(), this, center, timeout);
		ServerPlayer owner = resolveOwner();
		if (owner != null) LivingFlailDeployment.restoreHead(owner, getDeploymentId());
		discard();
	}

	@Nullable
	private ServerPlayer resolveOwner() {
		if (getOwner() instanceof ServerPlayer player) return player;
		if (!(level() instanceof ServerLevel server) || ownerId == null || server.getServer() == null) return null;
		return server.getServer().getPlayerList().getPlayer(ownerId);
	}

	@Override
	protected double getDefaultGravity() {
		return 0.035D;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		UUID savedOwner = getOwnerUuid();
		if (savedOwner != null) new LivingFlailProjectileState(getDeploymentId(), savedOwner, getCharge(),
				originalHand, getPrimaryTemper(), getSecondaryTemper(), impacted,
				lastSafePosition.x, lastSafePosition.y, lastSafePosition.z).write(tag);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		LivingFlailProjectileState state = LivingFlailProjectileState.read(tag);
		deploymentId = tag.hasUUID("DeploymentId") ? state.deploymentId() : getUUID();
		ownerId = tag.hasUUID("FlailOwner") ? state.ownerId() : null;
		entityData.set(CHARGE, Mth.clamp(state.charge(), 0.0F, 1.0F));
		originalHand = state.originalHand();
		entityData.set(PRIMARY_TEMPER, state.primaryTemper().name());
		entityData.set(SECONDARY_TEMPER,
				state.secondaryTemper() == null ? "" : state.secondaryTemper().name());
		impacted = state.impacted();
		lastSafePosition = new Vec3(state.lastSafeX(), state.lastSafeY(), state.lastSafeZ());
	}

	@Nullable
	private static EnumBloodTemper parseTemper(String name, @Nullable EnumBloodTemper fallback) {
		try {
			return EnumBloodTemper.valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return fallback;
		}
	}
}
