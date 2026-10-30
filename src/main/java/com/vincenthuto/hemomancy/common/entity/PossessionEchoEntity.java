package com.vincenthuto.hemomancy.common.entity;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSequences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
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
 * Its manifest (synced, 0..1) is how much of the player it still is: 1 is their own skin and stance, 0 the shadow; the
 * possession's cinematics move it both ways.
 */
public class PossessionEchoEntity extends Entity {
	private static final SyncedOwner OWNER = SyncedOwner.define(PossessionEchoEntity.class);
	private static final EntityDataAccessor<Float> MANIFEST = SynchedEntityData.defineId(PossessionEchoEntity.class,
			EntityDataSerializers.FLOAT);
	private float manifestO;
	private float manifestNow;

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

	public void setManifest(float manifest) {
		entityData.set(MANIFEST, Math.max(0.0F, Math.min(1.0F, manifest)));
	}

	public float manifest() {
		return entityData.get(MANIFEST);
	}

	/** The manifest between the last two ticks, for the renderer's blend. */
	public float manifest(float partialTick) {
		return manifestO + (manifestNow - manifestO) * partialTick;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		OWNER.defineDefault(builder);
		builder.define(MANIFEST, 0.0F);
	}

	@Override
	public void tick() {
		super.tick();
		manifestO = tickCount <= 1 ? manifest() : manifestNow;
		manifestNow = manifest();
		if (level().isClientSide || tickCount % 100 != 0) return;
		// An echo outlives its possession only by mistake: an owner who is offline or whose will is home dissolves it.
		ServerPlayer owner = owner().map(id -> ((ServerLevel) level()).getServer().getPlayerList().getPlayer(id))
				.orElse(null);
		if (owner == null || !getUUID().equals(HemoCapabilityAccess.getPossession(owner).echo())
				&& !PossessionSequences.holdsEcho(owner, getUUID())) discard();
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
		if (tag.contains("Manifest")) setManifest(tag.getFloat("Manifest"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		OWNER.save(this, tag);
		tag.putFloat("Manifest", manifest());
	}
}
