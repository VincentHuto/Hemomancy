package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class CircusKnifeProjectileEntity extends BloodNeedleEntity {
	public CircusKnifeProjectileEntity(EntityType<? extends CircusKnifeProjectileEntity> type, Level level) {
		super(type, level);
	}

	public CircusKnifeProjectileEntity(Level level, LivingEntity owner, float damage) {
		super(EntityInit.circus_knife.get(), level, owner, null);
		setBaseDamage(damage);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity != getOwner() && !(entity instanceof CircusPerformerEntity)
				&& !(entity instanceof EnthralledDollEntity doll && doll.isOwnedByCircusPerformer());
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target) {
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
		}
	}

	@Override
	protected double getDefaultGravity() {
		return 0.03D;
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && tickCount >= 30) discard();
	}
}
