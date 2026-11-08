package com.vincenthuto.hemomancy.common.entity.npc.circus;

import net.minecraft.core.BlockPos;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CircusAcrobatEntity extends CircusPerformerEntity {
	private static final EntityDataAccessor<Integer> FLIP_TICKS = SynchedEntityData.defineId(
			CircusAcrobatEntity.class, EntityDataSerializers.INT);
	private int attackCooldown;
	/** Where a wound-up vault will land, locked when the wind-up starts; null for a plain lunge. */
	private Vec3 vaultTarget;

	public CircusAcrobatEntity(EntityType<? extends CircusAcrobatEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 26.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.30D).add(Attributes.ARMOR, 1.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(FLIP_TICKS, 0);
	}

	@Override protected String roleId() { return "circus_acrobat"; }
	@Override protected String texturePath() { return "textures/entity/npc/harbinger/circus/acrobat_0.png"; }
	@Override protected int performanceDuration() { return 100; }

	@Override
	protected void tickPerformance(int actTick) {
		int flipTicks = entityData.get(FLIP_TICKS);
		if (flipTicks > 0) {
			if (onGround() && flipTicks > 3) {
				entityData.set(FLIP_TICKS, 0);
				if (level() instanceof ServerLevel server) CircusEffects.dust(server, position(), 12);
			} else {
				entityData.set(FLIP_TICKS, Math.min(16, flipTicks + 1));
			}
		}
		if (actTick == 30 || actTick == 70) {
			double side = actTick == 30 ? -2.5D : 2.5D;
			jumpTo(new Vec3(getHome().getX() + 0.5D + side, getHome().getY(), getHome().getZ() + 0.5D));
		}
	}

	public float getFlipProgress(float partialTick) {
		int ticks = entityData.get(FLIP_TICKS);
		return ticks == 0 ? 0.0F : Math.min(1.0F, (ticks + partialTick) / 16.0F);
	}

	private void jumpTo(Vec3 destination) {
		if (!canLandAt(destination)) return;
		Vec3 horizontal = destination.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		if (horizontal.lengthSqr() > 0.01D) horizontal = horizontal.normalize().scale(0.28D);
		setDeltaMovement(horizontal.x, 0.65D, horizontal.z);
		hasImpulse = true;
		entityData.set(FLIP_TICKS, 1);
		if (level() instanceof ServerLevel server) CircusEffects.dust(server, position(), 10);
	}

	/**
	 * Every 50 ticks the Acrobat picks a spot two blocks behind its target and crouches for the melee wind-up while the
	 * spot is marked; on the commit tick it vaults there and strikes if the target is still within 2.5 blocks. With
	 * no safe spot it closes in and winds up a plain lunge instead (2 blocks).
	 */
	@Override
	protected void tickDefense(LivingEntity target) {
		if (attackCooldown-- > 0) return;
		attackCooldown = 50 - defenseMove().total();
		Vec3 look = target.getLookAngle();
		Vec3 destination = target.position().subtract(look.x * 2.0D, 0.0D, look.z * 2.0D);
		if (canLandAt(destination)) {
			vaultTarget = destination;
			getNavigation().stop();
			beginDefenseMove(target);
		} else {
			getNavigation().moveTo(target, 1.15D);
			if (distanceToSqr(target) <= 4.0D) {
				vaultTarget = null;
				beginDefenseMove(target);
			}
		}
	}

	@Override
	protected void telegraphDefense(ServerLevel server, LivingEntity target, int tick, BossMoveTimeline move) {
		float charge = tick / (float) move.windup();
		CircusEffects.gather(server, position().add(0.0D, 0.9D, 0.0D), CircusEffects.BONE, charge);
		if (tick % 3 != 0) return;
		if (vaultTarget != null) {
			BossTelegraphs.ring(server, vaultTarget, 1.0D, CircusEffects.CRIMSON, 16);
			BossTelegraphs.ring(server, vaultTarget, 2.5D * charge + 0.3D, CircusEffects.BONE, 20);
		} else {
			BossTelegraphs.line(server, position(), target.position(), CircusEffects.CRIMSON);
		}
	}

	@Override
	protected void strikeDefense(LivingEntity target) {
		boolean vaulted = vaultTarget != null && vaultTo(vaultTarget);
		vaultTarget = null;
		if (distanceToSqr(target) > (vaulted ? 6.25D : 4.0D)) return;
		if (target.hurt(damageSources().mobAttack(this), 4.0F) && level() instanceof ServerLevel server)
			CircusEffects.impact(server, target.position().add(0.0D, 1.0D, 0.0D), 0.8D);
	}

	private boolean vaultTo(Vec3 destination) {
		if (!canLandAt(destination)) return false;
		if (level() instanceof ServerLevel server) CircusEffects.vault(server, position());
		teleportTo(destination.x, destination.y, destination.z);
		if (level() instanceof ServerLevel server) CircusEffects.vault(server, position());
		level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.45F, 1.5F);
		return true;
	}

	private boolean canLandAt(Vec3 destination) {
		BlockPos feet = BlockPos.containing(destination);
		AABB moved = getBoundingBox().move(destination.subtract(position()));
		return CircusPerformerRules.isSafeVault(level().hasChunkAt(feet),
				level().getWorldBorder().isWithinBounds(feet),
				!level().getBlockState(feet.below()).getCollisionShape(level(), feet.below()).isEmpty(),
				level().noCollision(this, moved));
	}
}
