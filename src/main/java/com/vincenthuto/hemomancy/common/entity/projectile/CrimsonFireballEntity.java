package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

/**
 * A facsimile blaze's shot: the hive's copy of a blaze fireball, burning crimson. It sets what it hits alight with
 * crimson fire. Only a blaze facsimile allowed to grief leaves flames on the block it strikes; a possessed player's
 * shot burns creatures and never the world, since a possessed body does not build.
 */
public class CrimsonFireballEntity extends AbstractHurtingProjectile {
	public static final ParticleColor CRIMSON = new ParticleColor(196, 22, 34);
	public static final ParticleColor CORE = new ParticleColor(255, 92, 70);
	public static final float DAMAGE = 5f;
	public static final int BURN_SECONDS = 5;

	public CrimsonFireballEntity(EntityType<? extends CrimsonFireballEntity> type, Level level) {
		super(type, level);
	}

	public CrimsonFireballEntity(Level level, LivingEntity owner, Vec3 movement) {
		super(EntityInit.crimson_fireball.get(), owner, movement, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide && isAlive()) {
			level().addParticle(HemoParticleData.glow(CORE), getX(), getY() + getBbHeight() / 2, getZ(), 0, 0, 0);
		}
	}

	@Override
	protected boolean shouldBurn() {
		return false;
	}

	@Override
	protected ParticleOptions getTrailParticle() {
		return HemoParticleData.emberSpark(CRIMSON, 0.18f, 0.8f, 14);
	}

	@Override
	protected boolean canHitEntity(Entity target) {
		if (!super.canHitEntity(target)) return false;
		return !(target instanceof LivingEntity living) || !(getOwner() instanceof LivingEntity owner)
				|| ManipulationCombatHelper.canHarmFor(owner, living);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!(level() instanceof ServerLevel)) return;
		Entity target = result.getEntity();
		int fireBefore = target.getRemainingFireTicks();
		target.igniteForSeconds(BURN_SECONDS);
		Entity owner = getOwner();
		DamageSource source = owner == null ? damageSources().source(DamageTypes.UNATTRIBUTED_FIREBALL, this)
				: damageSources().source(DamageTypes.FIREBALL, this, owner);
		if (target.hurt(source, DAMAGE)) {
			CrimsonFireHelper.markCrimsonForRemainingFire(target, BURN_SECONDS * 20);
		} else {
			target.setRemainingFireTicks(fireBefore);
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		if (level().isClientSide || !(getOwner() instanceof Mob mob) || !EventHooks.canEntityGrief(level(), mob)) return;
		BlockPos at = result.getBlockPos().relative(result.getDirection());
		var flames = BlockInit.crimson_flames.get().defaultBlockState();
		if (level().isEmptyBlock(at) && flames.canSurvive(level(), at)) level().setBlockAndUpdate(at, flames);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!level().isClientSide) {
			((ServerLevel) level()).sendParticles(HemoParticleData.emberSpark(CRIMSON, 0.3f, 0.9f, 18),
					getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.04);
			discard();
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return false;
	}
}
