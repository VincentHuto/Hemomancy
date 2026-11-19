package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class CircusKnifeProjectileEntity extends BloodNeedleEntity {
	private boolean harmless;
	private boolean puppetDagger;
	private boolean pinDagger;
	public void setPuppetDagger() { puppetDagger = true; }
	/** The Juggler's third dagger of a volley: it pins what it strikes. */
	public void setPinDagger() { puppetDagger = true; pinDagger = true; }
	public CircusKnifeProjectileEntity(EntityType<? extends CircusKnifeProjectileEntity> type, Level level) {
		super(type, level);
	}

	public CircusKnifeProjectileEntity(Level level, LivingEntity owner, float damage) {
		super(EntityInit.circus_knife.get(), level, owner, null);
		setBaseDamage(damage);
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return !harmless && (!(getOwner() instanceof net.minecraft.world.entity.Mob mob && mob instanceof com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon bound)
                || entity instanceof LivingEntity target && com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.canAttack(mob, bound, target)) && super.canHitEntity(entity) && entity != getOwner() && !(entity instanceof CircusPerformerEntity)
				&& !(entity instanceof EnthralledDollEntity doll && doll.isOwnedByCircusPerformer())
				&& (!(entity instanceof LivingEntity target) || troupeMayStrike(target));
	}

	/** D-34: a troupe knife thrown in an act or finale lands only on its owner and the bodies they hold. */
	private boolean troupeMayStrike(LivingEntity target) {
		if (level().isClientSide) return true;
		if (getOwner() instanceof CircusPerformerEntity performer) return performer.canStrike(target);
		if (getOwner() instanceof CircusRingmasterEntity ringmaster) return com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.canHarmFor(ringmaster, target);
		return true;
	}

	/** A knife thrown by the troupe itself, not a dagger from a player's Juggler; read on both sides. */
	public boolean isTroupeKnife() {
		return getOwner() instanceof CircusPerformerEntity || getOwner() instanceof CircusRingmasterEntity;
	}

	public void setHarmless() {
		harmless = true;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("Harmless", harmless);
		tag.putBoolean("PuppetDagger", puppetDagger);
		tag.putBoolean("PinDagger", pinDagger);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		harmless = tag.getBoolean("Harmless");
		puppetDagger = tag.getBoolean("PuppetDagger");
		pinDagger = tag.getBoolean("PinDagger");
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
        if (puppetDagger && !level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            var type = registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                            com.vincenthuto.hemomancy.Hemomancy.rloc("marrow_dagger")));
            float damage = com.vincenthuto.hemomancy.common.entity.summon.MarrowJugglerRules.impactDamage(getBaseDamage(), getDeltaMovement().length());
            if (target.hurt(new net.minecraft.world.damagesource.DamageSource(type, this, getOwner()), damage) && pinDagger)
                com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingLock.pin(target,
                        com.vincenthuto.hemomancy.common.entity.summon.MarrowJugglerRules.PIN_TICKS);
            discard(); return;
        }
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
