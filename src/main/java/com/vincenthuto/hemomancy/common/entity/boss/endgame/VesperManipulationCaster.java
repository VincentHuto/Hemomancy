package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronSpike;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.summon.PuppetStrings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Lets the Evening Star cast any fight manipulation. Ones with a shared entity cast go through it; the rest are
 * Vesper-side boss versions that fire the manipulation's own authored visual form with a matching fight effect.
 * Wills and the shared entity cast list are untouched.
 */
public final class VesperManipulationCaster {
	private VesperManipulationCaster() {
	}

	public static boolean cast(VesperTheEveningStarEntity boss, String manipulation, LivingEntity target) {
		VesperManipulationCoverage.Entry entry = VesperManipulationCoverage.entry(manipulation);
		if (entry == null || !(boss.level() instanceof ServerLevel server) || !target.isAlive()) return false;
		return switch (entry.coverage()) {
			case CASTABLE -> VesperPhaseTwoCombat.cast(boss, target, manipulation, 0.65D);
			case BOSS_VARIANT -> variant(server, boss, target, entry.spec());
			case EXCLUDED -> false;
		};
	}

	private static boolean variant(ServerLevel server, VesperTheEveningStarEntity boss, LivingEntity target,
			VesperManipulationCoverage.Spec spec) {
		ManipulationVisuals.Form form = spec.form();
		Vec3 eye = boss.getEyePosition();
		Vec3 at = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
		float power = spec.power();
		switch (spec.kind()) {
			case STRIKE -> {
				ManipulationVisuals.burst(server, form, at, at, 1.5D, 20);
				hurt(boss, target, power);
			}
			case LINE -> {
				ManipulationVisuals.burst(server, form, eye, at, 1.0D, 16);
				if (boss.hasLineOfSight(target)) hurt(boss, target, power);
			}
			case AREA -> {
				ManipulationVisuals.burst(server, form, at, at, 3.0D, 24);
				for (LivingEntity victim : around(boss, target.position(), 3.0D)) hurt(boss, victim, power);
			}
			case SELF_BUFF -> {
				ManipulationVisuals.attached(boss, form, 1.5D, 100, 1);
				selfBuff(boss, form);
			}
			case DEBUFF -> {
				ManipulationVisuals.attached(target, form, 1.2D, 60, 1);
				if (form == ManipulationVisuals.Form.HOUR) {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
					target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
				} else {
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 3));
					target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
				}
			}
			case PULL -> {
				ManipulationVisuals.burst(server, form, at, eye, 1.5D, 18);
				Vec3 toward = boss.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
				if (toward.lengthSqr() > 0.01D) push(target, toward.normalize().scale(0.9D), 0.25D);
				hurt(boss, target, power);
			}
			case PUSH -> {
				ManipulationVisuals.burst(server, form, boss.position(), at, 3.0D, 18);
				Vec3 away = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
				if (away.lengthSqr() > 0.01D) push(target, away.normalize().scale(1.2D), 0.4D);
				hurt(boss, target, power);
			}
			case BLINK -> {
				Vec3 from = boss.position();
				VesperPhaseTwoCombat.safeFlank(boss, target);
				ManipulationVisuals.burst(server, form, from, boss.position(), 1.2D, 16);
			}
			case SWAP -> {
				Vec3 bossAt = boss.position();
				Vec3 targetAt = target.position();
				ManipulationVisuals.burst(server, form, bossAt, targetAt, 1.2D, 16);
				boss.teleportTo(targetAt.x, targetAt.y, targetAt.z);
				target.teleportTo(bossAt.x, bossAt.y, bossAt.z);
			}
			case SPIKES -> {
				ManipulationVisuals.burst(server, form, at, at, 2.5D, 20);
				for (int i = 0; i < 5; i++) {
					double angle = Mth.TWO_PI * i / 5.0D;
					EntityIronSpike spike = new EntityIronSpike(EntityInit.iron_spike.get(), server, boss);
					spike.moveTo(target.getX() + Math.cos(angle) * 2.2D, target.getY(),
							target.getZ() + Math.sin(angle) * 2.2D, (float) Math.toDegrees(angle), 0.0F);
					server.addFreshEntity(spike);
				}
				VesperPhaseTwoCombat.markOwnedIronPillars(boss);
			}
			case WALL -> {
				ManipulationVisuals.attached(boss, form, 2.0D, 60, 1);
				boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 1));
			}
			case LAUNCH -> {
				ManipulationVisuals.burst(server, form, target.position(), at.add(0.0D, 2.0D, 0.0D), 1.5D, 20);
				push(target, Vec3.ZERO, power >= 4.0F ? 1.0D : 0.6D);
				if (power < 4.0F) target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
				hurt(boss, target, power);
			}
			case FREEZE -> {
				ManipulationVisuals.attached(target, form, 1.2D, 40, 1);
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 9));
				if (power > 0.0F) hurt(boss, target, power);
			}
			case BURN -> {
				ManipulationVisuals.burst(server, form, at, at, 1.5D, 18);
				target.setRemainingFireTicks(80);
				Vec3 away = target.position().subtract(boss.position()).multiply(1.0D, 0.0D, 1.0D);
				if (away.lengthSqr() > 0.01D) push(target, away.normalize().scale(0.6D), 0.2D);
				hurt(boss, target, power);
			}
			case POISON -> {
				ManipulationVisuals.burst(server, form, at, at, 2.0D, 24);
				target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
				hurt(boss, target, power);
			}
			case UNRAVEL -> {
				// Thread Ripper turned on the player's own puppets: the nearest one is hauled away on its strings.
				Mob puppet = server.getEntitiesOfClass(Mob.class, new AABB(target.blockPosition()).inflate(16.0D),
						mob -> mob instanceof BoundPuppeteerSummon bound && !bound.hemomancy$isTrialSummon()
								&& bound.hemomancy$getOwnerUUID() != null
								&& server.getPlayerByUUID(bound.hemomancy$getOwnerUUID()) != null
								&& !PuppetStrings.isUnspooling(mob))
						.stream().min(java.util.Comparator.comparingDouble(target::distanceToSqr)).orElse(null);
				if (puppet == null) return false;
				ManipulationVisuals.burst(server, form, eye, puppet.position(), 1.0D, 18);
				PuppetStrings.unspool(puppet);
			}
			case DRAIN -> {
				ManipulationVisuals.burst(server, form, at, eye, 1.2D, 20);
				target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 100, 0));
				hurt(boss, target, power);
			}
			case PROJECTILE_FAN -> {
				ManipulationVisuals.attached(boss, form, 1.0D, 10, 1);
				Vec3 base = at.subtract(eye).normalize();
				for (int i = -2; i <= 2; i++) {
					double angle = Math.toRadians(i * 9.0D);
					Vec3 dir = new Vec3(base.x * Math.cos(angle) - base.z * Math.sin(angle), base.y,
							base.x * Math.sin(angle) + base.z * Math.cos(angle)).normalize();
					BloodNeedleEntity needle = new BloodNeedleEntity(server, boss);
					needle.setPos(eye.x, eye.y - 0.2D, eye.z);
					needle.setBaseDamage(power);
					needle.shoot(dir.x, dir.y, dir.z, 1.6F, 0.0F);
					server.addFreshEntity(needle);
				}
			}
		}
		return true;
	}

	private static void selfBuff(VesperTheEveningStarEntity boss, ManipulationVisuals.Form form) {
		switch (form) {
			case RUSH -> {
				boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
				boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0));
			}
			case WARD -> boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1));
			case CROWN -> boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
			case FURNACE -> {
				boss.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 160, 0));
				for (LivingEntity victim : around(boss, boss.position(), 3.0D)) victim.setRemainingFireTicks(60);
			}
			default -> boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
		}
	}

	private static java.util.List<LivingEntity> around(VesperTheEveningStarEntity boss, Vec3 center, double radius) {
		return boss.level().getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius),
				entity -> entity instanceof Player player && player.isAlive() && !player.isCreative()
						&& !player.isSpectator());
	}

	private static void hurt(VesperTheEveningStarEntity boss, LivingEntity target, float power) {
		if (power > 0.0F) target.hurt(boss.damageSources().indirectMagic(boss, boss), power);
	}

	private static void push(LivingEntity target, Vec3 horizontal, double up) {
		target.push(horizontal.x, up, horizontal.z);
		target.hurtMarked = true;
	}
}
