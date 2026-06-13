package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.effect.ConstrictorCordRules;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ConstrictorCordEntity extends ThrowableItemProjectile {
	public ConstrictorCordEntity(EntityType<? extends ConstrictorCordEntity> type, Level level) {
		super(type, level);
	}

	public ConstrictorCordEntity(Level level, LivingEntity owner) {
		super(EntityInit.constrictor_cord_projectile.get(), owner, level);
	}

	@Override
	protected Item getDefaultItem() {
		return ItemInit.constrictor_cord.get();
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target && target != getOwner()) {
			int duration = isBossTarget(target)
					? ConstrictorCordRules.BOSS_DURATION_TICKS
					: ConstrictorCordRules.DURATION_TICKS;
			target.setDeltaMovement(0.0D, 0.0D, 0.0D);
			target.hurtMarked = true;
			target.addEffect(new MobEffectInstance(EffectInit.blood_binding, duration,
					ConstrictorCordRules.BLOOD_BINDING_AMPLIFIER, false, true, true), getOwner());
			target.addEffect(new MobEffectInstance(EffectInit.constricted, duration, 0, false, true, true), getOwner());
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!level().isClientSide) {
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 12,
						0.2D, 0.2D, 0.2D, 0.03D);
			}
			level().playSound(null, blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.7F, 0.75F);
		}
		this.remove(RemovalReason.KILLED);
	}

	private static boolean isBossTarget(LivingEntity target) {
		return target instanceof EnderDragon
				|| target instanceof WitherBoss
				|| target.getClass().getName().startsWith("com.vincenthuto.hemomancy.common.entity.boss.");
	}
}
