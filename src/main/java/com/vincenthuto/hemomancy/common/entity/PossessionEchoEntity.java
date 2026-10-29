package com.vincenthuto.hemomancy.common.entity;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;

import java.util.Optional;
import java.util.UUID;

/**
 * The shadow a possessing Silentium leaves at the rite: semi-corporeal, drawn in their skin, visible to everyone. It is
 * not the player. It has no hitbox and no collision, cannot be hurt, and dissolves once its owner's will has returned.
 */
public class PossessionEchoEntity extends Entity {
	private static final SyncedOwner OWNER = SyncedOwner.define(PossessionEchoEntity.class);

	public PossessionEchoEntity(EntityType<? extends PossessionEchoEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
		setInvulnerable(true);
	}

	public void setOwner(UUID owner) {
		OWNER.set(this, owner);
	}

	public Optional<UUID> owner() {
		return Optional.ofNullable(OWNER.get(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		OWNER.defineDefault(builder);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide || tickCount % 100 != 0) return;
		// An echo outlives its possession only by mistake: an owner who is offline or whose will is home dissolves it.
		ServerPlayer owner = owner().map(id -> ((ServerLevel) level()).getServer().getPlayerList().getPlayer(id))
				.orElse(null);
		if (owner == null || !getUUID().equals(HemoCapabilityAccess.getPossession(owner).echo())) discard();
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		OWNER.load(this, tag);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		OWNER.save(this, tag);
	}
}
