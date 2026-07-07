package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRecipeUnlockEvents;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.SynapticJoltManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMagnetismManip;
import com.vincenthuto.hemomancy.common.manipulation.tenebris.BlackVeilCovenantManager;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.util.CrimsonFireHelper;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public final class EntityManipulationEffects {
	private static final Set<String> SUPPORTED_CASTS = Set.of(
			"blood_shot", "blood_needle", "blood_aneurysm", "blood_cloud",
			"crimson_flame_conjuration", "sanguine_ignition", "scalding_updraft", "vitric_combustion",
			"deadly_gaze", "hemolymphal_pulse", "activation_potential", "synaptic_jolt", "conductive_mark",
			"hematic_flare", "crimson_sight", "prismatic_reproof", "unclosing_eye",
			"hemorrhage", "exsanguinate", "insatiable_hunger", "grave_debt", "bloom_of_rot",
			"cryogenic_pulse", "glacial_grasp", "glacial_bastion", "glacial_rampart",
			"pyretic_forge", "iron_retort", "sanguine_magnetism",
			"void_shroud", "gloam_laceration", "umbral_step", "blood_eclipse",
			"blood_eclipse_mantle");

	private EntityManipulationEffects() {
	}

	public static boolean isSupported(BloodManipulation manipulation) {
		return manipulation != null && SUPPORTED_CASTS.contains(manipulation.getName());
	}

	public static boolean cast(BloodManipulation manipulation, ManipulationCastContext context) {
		if (manipulation == null || context == null || context.level().isClientSide) return false;
		return switch (manipulation.getName()) {
		case "blood_shot" -> bloodShot(context);
		case "blood_needle" -> bloodNeedle(context);
		case "blood_aneurysm" -> bloodAneurysm(context);
		case "blood_cloud" -> bloodCloud(context);
		case "crimson_flame_conjuration" -> crimsonFlameConjuration(context);
		case "sanguine_ignition" -> sanguineIgnition(context);
		case "scalding_updraft" -> scaldingUpdraft(context);
		case "vitric_combustion" -> vitricCombustion(context);
		case "deadly_gaze" -> deadlyGaze(context);
		case "hemolymphal_pulse" -> hemolymphalPulse(context);
		case "activation_potential" -> activationPotential(context);
		case "synaptic_jolt" -> synapticJolt(context);
		case "conductive_mark" -> conductiveMark(context);
		case "hematic_flare" -> hematicFlare(context);
		case "crimson_sight" -> crimsonSight(context);
		case "prismatic_reproof" -> prismaticReproof(context);
		case "unclosing_eye" -> unclosingEye(context);
		case "hemorrhage" -> hemorrhage(context);
		case "exsanguinate" -> exsanguinate(context);
		case "insatiable_hunger" -> insatiableHunger(context);
		case "grave_debt" -> graveDebt(context);
		case "bloom_of_rot" -> bloomOfRot(context);
		case "cryogenic_pulse" -> cryogenicPulse(context);
		case "glacial_grasp" -> glacialGrasp(context);
		case "glacial_bastion" -> glacialBastion(context);
		case "glacial_rampart" -> glacialRampart(context);
		case "pyretic_forge" -> pyreticForge(context);
		case "iron_retort" -> ironRetort(context);
		case "sanguine_magnetism" -> sanguineMagnetism(context);
		case "void_shroud" -> voidShroud(context);
		case "gloam_laceration" -> gloamLaceration(context);
		case "umbral_step" -> umbralStep(context);
		case "blood_eclipse" -> bloodEclipse(context);
		case "blood_eclipse_mantle" -> bloodEclipseMantle(context);
		default -> false;
		};
	}

	private static boolean bloodShot(ManipulationCastContext context) {
		BloodShotEntity shot = new BloodShotEntity(context.level(), context.caster(), context.heldItem());
		shootArrow(context, shot, 4.5F, 1.0F, 3.0D);
		return true;
	}

	private static boolean bloodNeedle(ManipulationCastContext context) {
		int count = 8 + context.level().random.nextInt(6);
		for (int i = 0; i < count; i++) {
			BloodNeedleEntity needle = new BloodNeedleEntity(context.level(), context.caster(), context.heldItem());
			shootArrow(context, needle, 3.8F + context.level().random.nextFloat() * 1.2F, 8.0F, 1.25D);
		}
		return true;
	}

	private static boolean bloodAneurysm(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(12.0D));
		if (target == null) return false;
		float directDamage = context.scaleDamage(8.0F);
		target.hurt(context.level().damageSources().magic(), directDamage);
		target.setDeltaMovement(target.getDeltaMovement().x, 1.15D, target.getDeltaMovement().z);
		target.hurtMarked = true;
		Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
		for (LivingEntity splash : livingNear(context.level(), center, 4.0D, context.caster())) {
			if (splash != target && canAffect(context.caster(), splash)) {
				splash.hurt(context.level().damageSources().magic(), context.scaleDamage(3.0F));
			}
		}
		sendParticles(context.level(), center, 34, new ParticleColor(210, 0, 0), 1.8D);
		context.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE,
				0.5F, 1.8F);
		return true;
	}

	private static boolean bloodCloud(ManipulationCastContext context) {
		Vec3 aim = context.aim().normalize();
		BloodCloudCarrierEntity carrier = new BloodCloudCarrierEntity(context.level(), context.caster(),
				context.caster().getX(), context.caster().getEyeY() - 0.15D, context.caster().getZ());
		carrier.setDeltaMovement(aim.scale(0.45D));
		carrier.xPower = carrier.getDeltaMovement().x * 0.01D;
		carrier.yPower = carrier.getDeltaMovement().y * 0.01D;
		carrier.zPower = carrier.getDeltaMovement().z * 0.01D;
		context.level().addFreshEntity(carrier);
		context.level().levelEvent(null, 1016, context.caster().blockPosition(), 0);
		return true;
	}

	private static boolean crimsonFlameConjuration(ManipulationCastContext context) {
		BlockPos firePos = aimedBlock(context, context.scaleRange(16.0D))
				.map(hit -> hit.getBlockPos().relative(hit.getDirection()))
				.orElseGet(() -> context.hasTarget() ? context.target().blockPosition() : context.origin());
		if (!context.level().getBlockState(firePos).canBeReplaced()) {
			firePos = firePos.above();
		}
		BlockState existing = context.level().getBlockState(firePos);
		if (!existing.canBeReplaced()) return false;
		context.level().setBlock(firePos, BlockInit.crimson_flames.get().defaultBlockState(), 3);
		context.level().playSound(null, firePos, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.8F, 0.9F);
		sendParticles(context.level(), Vec3.atCenterOf(firePos), 18, new ParticleColor(255, 40, 20), 0.35D);
		return true;
	}

	private static boolean sanguineIgnition(ManipulationCastContext context) {
		int hits = 0;
		for (LivingEntity target : targetsAroundCaster(context, 5.0D)) {
			CrimsonFireHelper.igniteCrimson(target, 4);
			target.hurt(context.level().damageSources().onFire(), context.scaleDamage(2.0F));
			hits++;
		}
		context.level().playSound(null, context.origin(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9F, 0.8F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.8D, 0.0D), 32,
				new ParticleColor(245, 95, 10), 3.0D);
		if (hits > 0) {
			LivingWeaponGraftRecipeUnlockEvents.onTorchAlignedManipulation(context.caster());
		}
		return hits > 0;
	}

	private static boolean scaldingUpdraft(ManipulationCastContext context) {
		Vec3 flatAim = new Vec3(context.aim().x, 0.0D, context.aim().z);
		if (flatAim.lengthSqr() > 1.0E-4D) {
			flatAim = flatAim.normalize().scale(0.4D);
		}
		context.caster().push(flatAim.x, 1.05D, flatAim.z);
		context.caster().hurtMarked = true;
		context.caster().fallDistance = 0.0F;
		context.caster().resetFallDistance();
		context.caster().addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));
		for (LivingEntity target : targetsAroundCaster(context, 4.0D)) {
			CrimsonFireHelper.igniteCrimson(target, 3);
			target.hurt(context.level().damageSources().onFire(), context.scaleDamage(1.5F));
		}
		context.level().playSound(null, context.origin(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.9F, 0.6F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.7D, 0.0D), 40,
				new ParticleColor(255, 140, 20), 1.4D);
		LivingWeaponGraftRecipeUnlockEvents.onTorchAlignedManipulation(context.caster());
		return true;
	}

	private static boolean vitricCombustion(ManipulationCastContext context) {
		Vec3 center = context.hasTarget()
				? context.target().position().add(0.0D, context.target().getBbHeight() * 0.5D, 0.0D)
				: context.caster().getEyePosition().add(context.aim().scale(context.scaleRange(18.0D)));
		int hits = 0;
		for (LivingEntity target : livingNear(context.level(), center, 4.0D, context.caster())) {
			if (!canAffect(context.caster(), target)) continue;
			CrimsonFireHelper.igniteCrimson(target, 8);
			target.hurt(context.level().damageSources().magic(), context.scaleDamage(8.0F));
			Vec3 knockback = target.position().subtract(center);
			if (knockback.lengthSqr() > 1.0E-4D) {
				knockback = knockback.normalize().scale(1.1D);
				target.push(knockback.x, 0.4D, knockback.z);
			}
			hits++;
		}
		context.level().playSound(null, BlockPos.containing(center), SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.HOSTILE, 1.1F, 0.7F);
		sendParticles(context.level(), center, 56, new ParticleColor(235, 80, 10), 3.0D);
		return hits > 0;
	}

	private static boolean deadlyGaze(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(24.0D));
		if (target == null) return false;
		target.push(0.0D, 0.85D, 0.0D);
		target.hurtMarked = true;
		target.hurt(context.level().damageSources().magic(), context.scaleDamage(2.5F));
		target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0, false, true));
		sendParticles(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.7D, 0.0D), 16,
				new ParticleColor(255, 0, 0), 0.8D);
		return true;
	}

	private static boolean hemolymphalPulse(ManipulationCastContext context) {
		int tagged = 0;
		for (LivingEntity target : targetsAroundCaster(context, 24.0D)) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 300, 0, false, false));
			tagged++;
		}
		if (context.level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 32; i++) {
				double angle = i * Math.PI * 2.0D / 32.0D;
				serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
						context.caster().getX() + Math.cos(angle) * 7.0D, context.caster().getY() + 1.0D,
						context.caster().getZ() + Math.sin(angle) * 7.0D, 1, 0.0D, 0.0D, 0.0D, 0.1D);
			}
		}
		context.level().playSound(null, context.origin(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.8F, 0.6F);
		if (tagged > 0) {
			LivingWeaponGraftRecipeUnlockEvents.onSpearAlignedManipulation(context.caster());
		}
		return tagged > 0;
	}

	private static boolean activationPotential(ManipulationCastContext context) {
		int hits = 0;
		for (LivingEntity target : targetsAroundCaster(context, 5.0D)) {
			float damage = context.scaleDamage(5.0F);
			target.hurt(context.level().damageSources().mobAttack(context.caster()), damage);
			SchoolHitHelper.tryTriggerConductiveArc(context.caster(), target, EnumBloodTendency.DUCTILIS, null, damage);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, true));
			hits++;
		}
		context.caster().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0, false, true));
		context.level().playSound(null, context.origin(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE,
				0.55F, 1.5F);
		return hits > 0;
	}

	private static boolean synapticJolt(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(7.0D));
		if (target == null) return false;
		SynapticJoltManip.staggerTarget(target);
		DuctilisLightningEffects.synapticJolt(context.caster(), target);
		float damage = context.scaleDamage(3.0F);
		target.hurt(context.level().damageSources().magic(), damage);
		SchoolHitHelper.tryTriggerConductiveArc(context.caster(), target, EnumBloodTendency.DUCTILIS, null, damage);
		context.level().playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE,
				0.45F, 1.75F);
		return true;
	}

	private static boolean conductiveMark(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(14.0D));
		if (target == null) return false;
		SchoolHitHelper.markConductive(target, ManipulationStatusRules.CONDUCTIVE_MARK_TICKS);
		target.addEffect(new MobEffectInstance(MobEffects.GLOWING, ManipulationStatusRules.CONDUCTIVE_MARK_TICKS,
				0, false, true, true));
		DuctilisLightningEffects.conductiveMark(context.caster(), target);
		context.level().playSound(null, target.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE,
				0.35F, 2.0F);
		return true;
	}

	private static boolean hematicFlare(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(16.0D));
		if (target == null) return false;
		boolean concealed = target.hasEffect(MobEffects.INVISIBILITY);
		if (concealed) {
			target.removeEffect(MobEffects.INVISIBILITY);
		}
		target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0, false, true));
		float damage = context.scaleDamage(concealed ? 5.0F : 3.0F);
		target.hurt(context.level().damageSources().magic(), damage);
		SchoolHitHelper.tryTriggerConductiveArc(context.caster(), target, EnumBloodTendency.LUX,
				EnumBloodTendency.FLAMMEUS, damage);
		context.level().playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE,
				0.75F, concealed ? 1.75F : 1.45F);
		sendParticles(context.level(), target.getEyePosition(), 20, new ParticleColor(255, 235, 175), 0.8D);
		LivingWeaponGraftRecipeUnlockEvents.onSpearAlignedManipulation(context.caster());
		return true;
	}

	private static boolean crimsonSight(ManipulationCastContext context) {
		context.caster().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, true));
		int glowed = 0;
		for (LivingEntity target : targetsAroundCaster(context, 32.0D)) {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false, true));
			glowed++;
		}
		context.level().playSound(null, context.origin(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 0.5F, 1.8F);
		if (glowed > 0) {
			LivingWeaponGraftRecipeUnlockEvents.onSpearAlignedManipulation(context.caster());
		}
		return glowed > 0 || !context.playerCast();
	}

	private static boolean prismaticReproof(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(14.0D));
		if (target == null) return false;
		target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, true));
		target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0, false, true));
		if (target.hasEffect(MobEffects.GLOWING)) {
			float damage = context.scaleDamage(4.0F);
			target.hurt(context.level().damageSources().magic(), damage);
			SchoolHitHelper.tryTriggerConductiveArc(context.caster(), target, EnumBloodTendency.LUX,
					EnumBloodTendency.DUCTILIS, damage);
		} else {
			float damage = context.scaleDamage(2.0F);
			target.hurt(context.level().damageSources().magic(), damage);
			SchoolHitHelper.tryTriggerConductiveArc(context.caster(), target, EnumBloodTendency.LUX,
					EnumBloodTendency.DUCTILIS, damage);
		}
		context.level().playSound(null, context.origin(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE,
				0.8F, 1.4F);
		sendParticles(context.level(), target.getEyePosition(), 24, new ParticleColor(220, 200, 255), 0.9D);
		return true;
	}

	private static boolean unclosingEye(ManipulationCastContext context) {
		int revealed = 0;
		for (LivingEntity target : livingNear(context.level(), context.caster().position(), 32.0D, null)) {
			if (!target.isAlive()) continue;
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false));
			if (target.hasEffect(MobEffects.INVISIBILITY)) {
				target.removeEffect(MobEffects.INVISIBILITY);
			}
			revealed++;
		}
		context.level().playSound(null, context.origin(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.0F, 1.5F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 1.4D, 0.0D), 44,
				new ParticleColor(255, 240, 180), 2.4D);
		return revealed > 0;
	}

	private static boolean hemorrhage(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(8.0D));
		if (target == null) return false;
		target.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 1, false, true));
		context.level().playSound(null, context.origin(), SoundEvents.WITHER_HURT, SoundSource.HOSTILE, 0.7F, 1.6F);
		sendParticles(context.level(), midpoint(context.caster(), target), 20, new ParticleColor(0, 75, 0), 0.9D);
		LivingWeaponGraftRecipeUnlockEvents.onAxeAlignedManipulation(context.caster());
		return true;
	}

	private static boolean exsanguinate(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(10.0D));
		if (target == null || target.getHealth() > target.getMaxHealth() * 0.35F) return false;
		float damage = Math.max(4.0F, target.getHealth() * 1.5F);
		target.hurt(context.level().damageSources().magic(), context.scaleDamage(damage));
		context.caster().heal(Math.min(6.0F, damage * 0.35F));
		context.level().playSound(null, context.origin(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 0.7F, 1.8F);
		sendParticles(context.level(), midpoint(target, context.caster()), 28, new ParticleColor(145, 0, 20), 1.1D);
		return true;
	}

	private static boolean insatiableHunger(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(10.0D));
		if (target == null) return false;
		target.addEffect(new MobEffectInstance(EffectInit.insatiable_hunger, 220, 0, false, true, true));
		context.level().playSound(null, target.blockPosition(), SoundEvents.HUSK_AMBIENT, SoundSource.HOSTILE,
				0.65F, 0.7F);
		sendParticles(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
				22, new ParticleColor(45, 105, 30), 0.9D);
		return true;
	}

	private static boolean graveDebt(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(12.0D));
		if (target == null) return false;
		if (context.caster() instanceof net.minecraft.world.entity.player.Player player) {
			SchoolHitHelper.markGraveDebt(target, player, ManipulationStatusRules.GRAVE_DEBT_TICKS);
		} else {
			target.addEffect(new MobEffectInstance(EffectInit.grave_debt,
					ManipulationStatusRules.GRAVE_DEBT_TICKS, 0, false, true, true));
			target.getPersistentData().putBoolean("hemomancy:grave_debt_burst_used", false);
		}
		context.level().playSound(null, target.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE,
				0.45F, 0.55F);
		sendParticles(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
				24, new ParticleColor(70, 105, 35), 1.0D);
		return true;
	}

	private static boolean bloomOfRot(ManipulationCastContext context) {
		int hits = 0;
		for (LivingEntity target : targetsAroundCaster(context, 8.0D)) {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true));
			hits++;
		}
		context.caster().addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
		context.level().playSound(null, context.origin(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.HOSTILE, 1.0F, 0.7F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.7D, 0.0D), 48,
				new ParticleColor(45, 125, 25), 4.0D);
		return hits > 0;
	}

	private static boolean cryogenicPulse(ManipulationCastContext context) {
		int hits = 0;
		for (LivingEntity target : targetsAroundCaster(context, 5.0D)) {
			applyColdControl(context, target, 60, 2);
			target.hurt(context.level().damageSources().freeze(), context.scaleDamage(3.0F));
			hits++;
		}
		context.level().playSound(null, context.origin(), SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 1.0F, 0.7F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.7D, 0.0D), 36,
				new ParticleColor(145, 215, 255), 3.0D);
		if (hits > 0) {
			LivingWeaponGraftRecipeUnlockEvents.onFlailAlignedManipulation(context.caster());
		}
		return hits > 0;
	}

	private static boolean glacialGrasp(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(10.0D));
		boolean controlled = false;
		if (target != null) {
			applyColdControl(context, target, 120, 3);
			target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 100));
			controlled = true;
		}
		if (context.level() instanceof ServerLevel serverLevel) {
			BlockPos center = target != null ? target.blockPosition() : context.caster().blockPosition();
			int placed = 0;
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					for (int dy = -1; dy <= 0; dy++) {
						BlockPos pos = center.offset(dx, dy, dz);
						BlockState state = context.level().getBlockState(pos);
						if (state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource()
								&& context.level().getBlockState(pos.above()).isAir()) {
							context.level().setBlock(pos, Blocks.FROSTED_ICE.defaultBlockState(), 3);
							context.level().scheduleTick(pos, Blocks.FROSTED_ICE, 60 + context.level().random.nextInt(40));
							placed++;
						}
					}
				}
			}
			if (placed > 0) {
				sendParticles(serverLevel, Vec3.atCenterOf(center), 20, new ParticleColor(175, 230, 255), 2.2D);
				controlled = true;
			}
		}
		context.level().playSound(null, context.origin(), SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 0.8F, 1.3F);
		if (controlled) {
			LivingWeaponGraftRecipeUnlockEvents.onFlailAlignedManipulation(context.caster());
		}
		return controlled;
	}

	private static boolean glacialBastion(ManipulationCastContext context) {
		if (!(context.level() instanceof ServerLevel serverLevel)) return false;
		BlockPos base = context.caster().blockPosition();
		int placed = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				int dist = Math.abs(dx) + Math.abs(dz);
				if (dist < 2 || dist > 3) continue;
				for (int dy = 0; dy < 3; dy++) {
					BlockPos pos = base.offset(dx, dy, dz);
					if (pos.equals(base) || pos.equals(base.above())) continue;
					if (TemporaryIceManager.place(serverLevel, pos, Blocks.PACKED_ICE.defaultBlockState(),
							420 + context.level().random.nextInt(120))) {
						placed++;
					}
				}
			}
		}
		context.level().playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 1.0F, 0.5F);
		return placed > 0;
	}

	private static boolean glacialRampart(ManipulationCastContext context) {
		if (!(context.level() instanceof ServerLevel serverLevel)) return false;
		Vec3 anchor = context.hasTarget()
				? context.target().position().subtract(context.aim().scale(1.8D))
				: context.caster().position().add(context.aim().scale(6.0D));
		BlockPos base = BlockPos.containing(anchor);
		Direction lateral = context.caster().getDirection().getClockWise();
		int placed = 0;
		for (int dLat = -1; dLat <= 1; dLat++) {
			for (int dUp = 0; dUp < 3; dUp++) {
				BlockPos pos = base.relative(lateral, dLat).above(dUp);
				if (TemporaryIceManager.place(serverLevel, pos, Blocks.PACKED_ICE.defaultBlockState(),
						420 + context.level().random.nextInt(120))) {
					placed++;
				}
			}
		}
		context.level().playSound(null, base, SoundEvents.GLASS_PLACE, SoundSource.HOSTILE, 1.0F, 0.5F);
		return placed > 0;
	}

	private static boolean pyreticForge(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(14.0D));
		if (target == null) return false;
		CrimsonFireHelper.igniteCrimson(target, 5);
		target.hurt(context.level().damageSources().onFire(), context.scaleDamage(4.0F));
		context.caster().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0, false, true));
		context.level().playSound(null, context.origin(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.8F, 1.0F);
		sendParticles(context.level(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), 26,
				new ParticleColor(230, 105, 20), 1.1D);
		LivingWeaponGraftRecipeUnlockEvents.onTorchAlignedManipulation(context.caster());
		return true;
	}

	private static boolean ironRetort(ManipulationCastContext context) {
		context.caster().addEffect(new MobEffectInstance(EffectInit.iron_retort,
				ManipulationStatusRules.IRON_RETORT_TICKS, 0, false, true, true));
		context.level().playSound(null, context.origin(), SoundEvents.ANVIL_USE, SoundSource.HOSTILE,
				0.55F, 1.8F);
		sendParticles(context.level(), context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.58D, 0.0D),
				28, new ParticleColor(155, 155, 150), 1.1D);
		return true;
	}

	private static boolean sanguineMagnetism(ManipulationCastContext context) {
		if (!(context.level() instanceof ServerLevel serverLevel)) return false;
		Vec3 center = context.hasTarget()
				? context.target().position()
				: context.caster().position().add(context.aim().normalize().scale(8.0D));
		SanguineMagnetismManip.spawnMagneticPillar(context.caster(), serverLevel, center,
				ManipulationStatusRules.SANGUINE_MAGNETISM_TICKS);
		return true;
	}

	private static boolean voidShroud(ManipulationCastContext context) {
		context.caster().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0, false, false));
		context.caster().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false));
		context.caster().addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100, 0, false, false));
		context.level().playSound(null, context.origin(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.5F, 1.8F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.9D, 0.0D), 24,
				new ParticleColor(70, 0, 120), 1.0D);
		return true;
	}

	private static boolean gloamLaceration(ManipulationCastContext context) {
		LivingEntity target = preferredTarget(context, context.scaleRange(7.0D));
		if (target == null) return false;
		boolean ambush = context.caster().hasEffect(MobEffects.INVISIBILITY)
				|| BlackVeilCovenantManager.isDarkEnough(context.level(), context.caster().blockPosition(), 7);
		target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 140, 0, false, true));
		target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, false, true));
		target.hurt(context.level().damageSources().magic(), context.scaleDamage(ambush ? 6.0F : 3.5F));
		context.level().playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE,
				0.75F, ambush ? 0.65F : 0.85F);
		if (context.level() instanceof ServerLevel serverLevel) {
			PacketHandler.sendClawSlash(target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D),
					context.aim(), new ParticleColor(ambush ? 55 : 85, 0, ambush ? 135 : 100),
					ambush, ambush ? 1.05F : 0.92F, 64.0D, serverLevel);
		}
		return true;
	}

	private static boolean umbralStep(ManipulationCastContext context) {
		if (!context.hasTarget()) return false;
		BlockPos landing = findLandingNearTarget(context.caster(), context.target());
		if (landing == null) return false;
		Vec3 oldPos = context.caster().position();
		context.caster().teleportTo(landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D);
		context.caster().fallDistance = 0.0F;
		context.caster().resetFallDistance();
		context.level().playSound(null, landing, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8F, 1.2F);
		sendParticles(context.level(), oldPos.add(0.0D, 0.9D, 0.0D), 20, new ParticleColor(70, 0, 125), 0.8D);
		sendParticles(context.level(), Vec3.atBottomCenterOf(landing).add(0.0D, 0.9D, 0.0D), 20,
				new ParticleColor(70, 0, 125), 0.8D);
		return true;
	}

	private static boolean bloodEclipse(ManipulationCastContext context) {
		int hits = 0;
		Vec3 eye = context.caster().getEyePosition();
		Vec3 look = context.aim().normalize();
		double range = context.scaleRange(18.0D);
		double coneDot = Math.cos(Math.toRadians(35.0D));
		for (LivingEntity target : targetsAroundCaster(context, range)) {
			Vec3 toTarget = target.getEyePosition().subtract(eye);
			if (toTarget.lengthSqr() <= 1.0E-4D || look.dot(toTarget.normalize()) < coneDot) continue;
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, false, true));
			target.hurt(context.level().damageSources().magic(), context.scaleDamage(3.0F));
			hits++;
		}
		context.level().playSound(null, context.origin(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.6F, 2.0F);
		return hits > 0;
	}

	private static boolean bloodEclipseMantle(ManipulationCastContext context) {
		context.caster().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 360, 1, false, true));
		context.caster().addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 360, 0, false, true));
		context.caster().addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 0, false, true));
		context.level().playSound(null, context.origin(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.75F, 0.6F);
		sendParticles(context.level(), context.caster().position().add(0.0D, 0.9D, 0.0D), 36,
				new ParticleColor(70, 0, 120), 1.6D);
		return true;
	}

	private static void shootArrow(ManipulationCastContext context, AbstractArrow arrow, float velocity, float inaccuracy,
			double baseDamage) {
		Vec3 aim = context.aim().normalize();
		arrow.setBaseDamage(baseDamage * context.damageScale());
		arrow.shoot(aim.x, aim.y, aim.z, velocity, inaccuracy);
		context.level().addFreshEntity(arrow);
	}

	@Nullable
	private static LivingEntity preferredTarget(ManipulationCastContext context, double range) {
		if (context.hasTarget() && context.caster().distanceTo(context.target()) <= range
				&& canAffect(context.caster(), context.target())) {
			return context.target();
		}
		return targetsAroundCaster(context, range).stream()
				.min((a, b) -> Double.compare(context.caster().distanceToSqr(a), context.caster().distanceToSqr(b)))
				.orElse(null);
	}

	private static List<LivingEntity> targetsAroundCaster(ManipulationCastContext context, double radius) {
		return context.level().getEntitiesOfClass(LivingEntity.class, context.caster().getBoundingBox().inflate(radius),
				target -> target.distanceTo(context.caster()) <= radius && canAffect(context.caster(), target));
	}

	private static List<LivingEntity> livingNear(Level level, Vec3 center, double radius, @Nullable LivingEntity excluded) {
		AABB box = new AABB(BlockPos.containing(center)).inflate(radius);
		return level.getEntitiesOfClass(LivingEntity.class, box,
				target -> target != excluded && target.isAlive() && target.position().distanceTo(center) <= radius);
	}

	private static boolean canAffect(LivingEntity caster, @Nullable LivingEntity target) {
		if (target == null || target == caster || !target.isAlive()) return false;
		if (target.isAlliedTo(caster) || caster.isAlliedTo(target)) return false;
		return !(caster instanceof Mob mob) || mob.getTarget() == target || mob.canAttack(target);
	}

	private static void applyColdControl(ManipulationCastContext context, LivingEntity target, int duration, int amplifier) {
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, true));
		target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration + 20, 0, false, true));
	}

	private static java.util.Optional<BlockHitResult> aimedBlock(ManipulationCastContext context, double range) {
		Vec3 eye = context.caster().getEyePosition();
		Vec3 end = eye.add(context.aim().scale(range));
		BlockHitResult hit = context.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
				ClipContext.Fluid.NONE, context.caster()));
		return hit.getType() == HitResult.Type.MISS ? java.util.Optional.empty() : java.util.Optional.of(hit);
	}

	@Nullable
	private static BlockPos findLandingNearTarget(LivingEntity caster, LivingEntity target) {
		Vec3 away = caster.position().subtract(target.position());
		if (away.lengthSqr() < 1.0E-4D) {
			away = caster.getLookAngle().reverse();
		}
		away = away.normalize();
		BlockPos preferred = BlockPos.containing(target.position().add(away.scale(2.0D)));
		BlockPos safe = firstSafeLanding(caster.level(), preferred);
		if (safe != null) return safe;
		for (int radius = 1; radius <= 3; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					safe = firstSafeLanding(caster.level(), target.blockPosition().offset(dx, 0, dz));
					if (safe != null) return safe;
				}
			}
		}
		return null;
	}

	@Nullable
	private static BlockPos firstSafeLanding(Level level, BlockPos pos) {
		for (int dy = 2; dy >= -2; dy--) {
			BlockPos feet = pos.offset(0, dy, 0);
			if (level.getBlockState(feet).isAir()
					&& level.getBlockState(feet.above()).isAir()
					&& !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty()) {
				return feet;
			}
		}
		return null;
	}

	private static Vec3 midpoint(LivingEntity a, LivingEntity b) {
		return a.getEyePosition().add(b.getEyePosition()).scale(0.5D);
	}

	private static void sendParticles(Level level, Vec3 center, int count, ParticleColor color, double spread) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		for (int i = 0; i < count; i++) {
			serverLevel.sendParticles(GlowParticleFactory.createData(color),
					center.x + (level.random.nextDouble() - 0.5D) * spread,
					center.y + (level.random.nextDouble() - 0.5D) * spread,
					center.z + (level.random.nextDouble() - 0.5D) * spread,
					1, 0.0D, 0.04D, 0.0D, 0.01D);
		}
	}
}
