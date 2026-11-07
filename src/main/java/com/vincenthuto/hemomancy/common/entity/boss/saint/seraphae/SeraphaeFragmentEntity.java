package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A fragment of Seraphae's light, broken loose at a Fracture. It glides toward the nearest challenger and grafts onto
 * them if it reaches them; left alone it returns to her after 300 ticks. A challenger draws it in with held Blood
 * Absorption, leads it into a held anchor, or strikes it apart; each way it ends tells her (design section 9). It has
 * no goals: its glide is its own tick. It is never killed: a blow ends it as {@code STRUCK}.
 */
public class SeraphaeFragmentEntity extends Monster implements SaintCompanion {
	private static final EntityDataAccessor<Float> DATA_DRAWN =
			SynchedEntityData.defineId(SeraphaeFragmentEntity.class, EntityDataSerializers.FLOAT);

	public static final int RETURN_TICKS = 300;
	public static final double SEEK_SPEED = 0.12D;
	public static final double HOVER = 1.0D;
	public static final double GRAFT_REACH = 0.9D;
	/** Held absorption ticks that draw it in. */
	public static final int DRAW_TICKS = 20;
	/** Ticks without absorption before the draw starts to fade. */
	public static final int DRAW_GRACE = 6;
	private static final int OWNER_GRACE = 40;

	@Nullable
	private UUID parentId;
	private boolean resolved;
	private int drawnTicks;
	private int sinceDrawn = Integer.MAX_VALUE / 2;

	public SeraphaeFragmentEntity(EntityType<? extends SeraphaeFragmentEntity> type, Level level) {
		super(type, level);
		this.xpReward = 0;
		this.setNoGravity(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_DRAWN, 0.0F);
	}

	/** No goals: the glide below is the whole of its will. */
	@Override
	protected void registerGoals() {
	}

	public void setParentId(@Nullable UUID id) {
		this.parentId = id;
	}

	@Nullable
	public UUID getParentId() {
		return parentId;
	}

	@Override
	public UUID saintId() {
		return parentId == null ? new UUID(0L, 0L) : parentId;
	}

	@Override
	public Allegiance allegiance() {
		return Allegiance.SAINT;
	}

	/** 0..1: how far a challenger's held absorption has drawn it in. */
	public float drawn() {
		return entityData.get(DATA_DRAWN);
	}

	@Nullable
	private SeraphaeEntity parent() {
		if (parentId == null || !(level() instanceof ServerLevel server)) return null;
		return server.getEntity(parentId) instanceof SeraphaeEntity seraphae && seraphae.isAlive() ? seraphae : null;
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel server) || resolved) return;
		SeraphaeEntity seraphae = parent();
		if (seraphae == null || !seraphae.knows(this)) {
			if (tickCount > OWNER_GRACE || seraphae != null) discard();
			return;
		}
		if (sinceDrawn < Integer.MAX_VALUE / 2) sinceDrawn++;
		if (sinceDrawn > DRAW_GRACE && drawnTicks > 0) drawnTicks--;
		entityData.set(DATA_DRAWN, Math.min(1.0F, drawnTicks / (float) DRAW_TICKS));
		if (tickCount >= RETURN_TICKS) {
			resolve(SeraphaeIntegrityRules.FragmentEnd.RETURNED, seraphae.position().add(0.0D, 1.6D, 0.0D));
			return;
		}
		Player quarry = seraphae.nearestChallenger(position());
		Vec3 goal = quarry == null ? seraphae.position().add(0.0D, 1.6D, 0.0D)
				: quarry.position().add(0.0D, HOVER, 0.0D);
		Vec3 delta = goal.subtract(position());
		double length = delta.length();
		// held absorption pins it while it is drawn in
		double speed = sinceDrawn <= DRAW_GRACE ? 0.0D : SEEK_SPEED;
		setDeltaMovement(length < 1.0E-4D ? Vec3.ZERO : delta.scale(Math.min(speed, length) / length));
		if (quarry != null && quarry.position().add(0.0D, 1.0D, 0.0D).distanceTo(position()) <= GRAFT_REACH + 0.6D
				&& ManipulationCombatHelper.canHarmFor(this, quarry)) {
			seraphae.fragmentGrafted(quarry);
			resolve(SeraphaeIntegrityRules.FragmentEnd.GRAFTED, quarry.position().add(0.0D, 1.0D, 0.0D));
		}
	}

	/** One tick of a challenger's held absorption on it; true when this tick drew it in. */
	public boolean draw(Player player) {
		if (resolved) return false;
		sinceDrawn = 0;
		drawnTicks++;
		entityData.set(DATA_DRAWN, Math.min(1.0F, drawnTicks / (float) DRAW_TICKS));
		if (drawnTicks < DRAW_TICKS) return false;
		resolve(SeraphaeIntegrityRules.FragmentEnd.ABSORBED, player.position().add(0.0D, 1.2D, 0.0D));
		return true;
	}

	/** Ends its run one way, tells her once, and goes. */
	public void resolve(SeraphaeIntegrityRules.FragmentEnd end, Vec3 toward) {
		if (resolved) return;
		resolved = true;
		SeraphaeEntity seraphae = parent();
		if (seraphae != null) seraphae.fragmentResolved(this, end, toward);
		discard();
	}

	/** A challenger's blow strikes it apart; nothing else touches it. */
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide || resolved) return false;
		if (!(source.getEntity() instanceof Player player)) return false;
		resolve(SeraphaeIntegrityRules.FragmentEnd.STRUCK, player.position().add(0.0D, 1.0D, 0.0D));
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean canUsePortal(boolean allowPassengers) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (parentId != null) tag.putUUID("ParentSeraphae", parentId);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		parentId = tag.hasUUID("ParentSeraphae") ? tag.getUUID("ParentSeraphae") : null;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.AMETHYST_BLOCK_CHIME;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.AMETHYST_BLOCK_HIT;
	}
}
