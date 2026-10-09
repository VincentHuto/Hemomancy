package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class MarrowSpitterEntity extends Skeleton implements SyncedBoundSummon {
	private static final int SHOT_INTERVAL_TICKS = 35;
	private static final double ORBIT_RADIUS = 3.25;
	private static final double ORBIT_HEIGHT = 1.8;
	private static final BoundSummonSync SYNC = BoundSummonSync.define(MarrowSpitterEntity.class);
	private int shotCooldown = 0;

	public MarrowSpitterEntity(EntityType<? extends Skeleton> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return BoundSummonBehavior.definitionAttributes(Skeleton.createAttributes(),
				PuppeteerSummonDefinitions.MARROW_SPITTER);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		SYNC.defineDefaults(builder, "marrow_spitter");
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
										MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		setNoGravity(true);
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
		setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		return data;
	}

	@Override
	public void tick() {
		super.tick();
		setNoGravity(true);
		if (level().isClientSide) {
			return;
		}
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			LivingEntity target = getTarget();
			if (target != null) tickHoverAround(target, 6.0, 2.2);
			tickSentryFire();
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
			getNavigation().stop();
			if (owner.isPresent()
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)) {
				tickBoundOrbit(owner.get());
			}
			tickSentryFire();
		}
	}

	private void tickBoundOrbit(Player owner) {
		tickHoverAround(owner, ORBIT_RADIUS, ORBIT_HEIGHT);
	}

	private void tickHoverAround(LivingEntity anchor, double radius, double height) {
		double phase = tickCount * 0.025 + Math.floorMod(getUUID().hashCode(), 360) * (Math.PI / 180.0);
		double bob = Math.sin(tickCount * 0.055 + phase) * 0.22;
		Vec3 desired = anchor.position().add(Math.cos(phase) * radius, height + bob, Math.sin(phase) * radius);
		Vec3 correction = desired.subtract(position());
		Vec3 velocity = getDeltaMovement().scale(0.78).add(correction.scale(0.035));
		double speed = velocity.length();
		if (speed > 0.18) velocity = velocity.scale(0.18 / speed);
		setDeltaMovement(velocity);
		hurtMarked = true;
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (isAlive()) move(MoverType.SELF, getDeltaMovement());
		else super.travel(travelVector);
	}

    private void tickSentryFire() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || !canAttack(target) || !hasLineOfSight(target)) { shotCooldown = 0; return; }
        getLookControl().setLookAt(target, 30, 30);
        if (distanceToSqr(target) > 24 * 24) { shotCooldown = 0; return; }
        if (com.vincenthuto.hemomancy.common.circus.MarrowJugglerRules.throwTick(shotCooldown)) performRangedAttack(target, 1);
        shotCooldown = (shotCooldown + 1) % SHOT_INTERVAL_TICKS;
    }
    @Override public void performRangedAttack(LivingEntity target, float distanceFactor) {
        var shot = new com.vincenthuto.hemomancy.common.entity.projectile.CircusKnifeProjectileEntity(level(), this,
                com.vincenthuto.hemomancy.common.circus.MarrowJugglerRules.daggerDamage(getAttributeValue(Attributes.ATTACK_DAMAGE)));
        shot.setPuppetDagger();
        double dx = target.getX() - getX(), dz = target.getZ() - getZ();
        shot.shoot(dx, target.getY(.55) - shot.getY() + Math.sqrt(dx * dx + dz * dz) * .03, dz, 1.7F, 1);
        level().addFreshEntity(shot);
        level().playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, .5F, 1.4F);
    }

	@Override
	protected boolean isSunBurnTick() {
		return false;
	}

	@Override
	public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
		return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return PuppeteerSummonRules.shouldDespawnInPeaceful(
				hemomancy$isTrialSummon(), hemomancy$getOwnerUUID());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		BoundSummonBehavior.save(this, tag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		BoundSummonBehavior.load(this, tag);
		setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
	}

	@Override public BoundSummonSync hemomancy$sync() { return SYNC; }
}
