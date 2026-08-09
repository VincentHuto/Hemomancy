package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSickleCombatRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class LivingSickleHookEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Float> ATTACK_DAMAGE = SynchedEntityData.defineId(
			LivingSickleHookEntity.class, EntityDataSerializers.FLOAT);
	private static final int MAX_LIFETIME_TICKS = 14;
	private static final ParticleColor BLOOD = new ParticleColor(220, 0, 20);

	public LivingSickleHookEntity(EntityType<? extends LivingSickleHookEntity> type, Level level) {
		super(type, level);
	}

	public LivingSickleHookEntity(Level level, LivingEntity owner) {
		super(EntityInit.living_sickle_hook.get(), owner, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ATTACK_DAMAGE, 7.0F);
	}

	public void setAttackDamage(float damage) {
		entityData.set(ATTACK_DAMAGE, Math.max(0.0F, damage));
	}

	public void spawnBloodTendril() {
		if (!(level() instanceof ServerLevel server) || !(getOwner() instanceof LivingEntity owner)) return;
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(0xF8DC0014, 0xC8750018)
				.withRange((float) (LivingSickleCombatRules.HOOK_RANGE + 4.0D))
				.withLifecycle(2, 11, 4)
				.withShape(22, 2, 0.085F, 0.065F)
				.withBranching(3, 1, 0.22F, 0.72F)
				.withWrithe(0.1F, 0.055F, 0.55F, 0.04F)
				.withBlendColors(false)
				.withFixedSeed(true, owner.getUUID().getLeastSignificantBits() ^ getId());
		TendrilEffectSpawner.spawn(server,
				new TendrilAnchor.Entity(owner.getId(), TendrilAnchor.AnchorPoint.CENTER, new Vec3(0.0D, 0.2D, 0.0D)),
				new TendrilAnchor.Entity(getId(), TendrilAnchor.AnchorPoint.CENTER, Vec3.ZERO), config);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!level().isClientSide && result.getEntity() instanceof LivingEntity target
				&& getOwner() instanceof LivingEntity owner && target != owner
				&& !target.isAlliedTo(owner) && owner.canAttack(target)) {
			target.hurt(TendencyWeaponHelper.createWeaponDamageSource(target, owner), entityData.get(ATTACK_DAMAGE));
			pullTowardOwner(target, owner);
			emitImpact(target);
			owner.playSound(SoundEvents.CHAIN_HIT, 1.0F, 0.72F);
		}
		discard();
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		if (!super.canHitEntity(entity) || !(entity instanceof LivingEntity target)
				|| !(getOwner() instanceof LivingEntity owner)) return false;
		return target != owner && target.isAlive() && !target.isAlliedTo(owner) && owner.canAttack(target);
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		discard();
	}

	@Override
	public void tick() {
		super.tick();
		Entity owner = getOwner();
		if (!level().isClientSide && (owner == null || owner.isRemoved() || owner.level() != level()
				|| tickCount >= MAX_LIFETIME_TICKS
				|| distanceToSqr(owner) > LivingSickleCombatRules.HOOK_RANGE * LivingSickleCombatRules.HOOK_RANGE)) {
			discard();
		}
	}

	private static void pullTowardOwner(LivingEntity target, LivingEntity owner) {
		double strength = LivingSickleCombatRules.pullStrength(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
		Vec3 direction = owner.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
		if (strength <= 0.0D || direction.lengthSqr() < 1.0E-4D) return;
		Vec3 existing = target.getDeltaMovement().scale(0.25D);
		target.setDeltaMovement(existing.add(direction.normalize().scale(strength)).add(0.0D, 0.25D, 0.0D));
		target.hasImpulse = true;
		target.hurtMarked = true;
	}

	private void emitImpact(LivingEntity target) {
		if (!(level() instanceof ServerLevel server)) return;
		Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.52D, 0.0D);
		server.sendParticles(BloodCellParticleFactory.createData(BLOOD), center.x, center.y, center.z,
				18, 0.55D, 0.65D, 0.55D, 0.08D);
		server.sendParticles(DarkGlowParticleFactory.createData(new ParticleColor(18, 0, 5)),
				center.x, center.y, center.z, 10, 0.65D, 0.7D, 0.65D, 0.04D);
	}
}
