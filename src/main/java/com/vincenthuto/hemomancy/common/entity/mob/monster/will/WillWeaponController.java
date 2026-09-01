package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WillWeaponController {
	private WillWeaponController() {
	}

	public static void equip(WillEntity will) {
		ItemStack weapon = WillEquipmentRules.createWeaponStack(will.getOrigin(), will.getSchool(), will.getTier());
		will.setItemSlot(EquipmentSlot.MAINHAND, weapon);
		will.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
	}

	public static void tick(WillEntity will) {
		if (will.level().isClientSide || will.getPhase() != WillPhase.MATERIALIZED) return;
		if (will.tickCount % 80 == 0 && !isExpectedWeapon(will)) {
			equip(will);
		}
		LivingEntity target = will.getTarget();
		if (target == null || !target.isAlive()) return;
		if (will.getSchool() == com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.DUCTILIS
				&& will.distanceToSqr(target) > 6.0D * 6.0D
				&& will.distanceToSqr(target) < 28.0D * 28.0D
				&& will.tickCount % rangedInterval(will) == 0) {
			fireBloodBolt(will, target);
		}
		if (will.getSchool() == com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.FERRIC
				&& will.getHealth() < will.getMaxHealth() * 0.5F
				&& will.tickCount % 140 == 0) {
			will.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 0, false, true));
		}
	}

	public static void onMeleeHit(WillEntity will, LivingEntity target) {
		switch (will.getSchool()) {
		case ANIMUS -> {
			target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 160, 0));
			target.hurt(will.damageSources().mobAttack(will), 2.5F);
			will.heal(1.5F);
		}
		case FLAMMEUS -> CrimsonFireHelper.igniteCrimson(target, 4);
		case DUCTILIS -> {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
		}
		case LUX -> {
			target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 180, 0, false, true));
			target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, true));
		}
		case MORTEM -> {
			target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
			will.heal(2.0F);
		}
		case CONGEATIO -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, true));
		case FERRIC -> {
			Vec3 away = target.position().subtract(will.position());
			if (away.lengthSqr() > 1.0E-4D) {
				away = away.normalize();
				target.push(away.x * 0.8D, 0.25D, away.z * 0.8D);
			}
			will.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, true));
		}
		case TENEBRIS -> {
			target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 120, 1));
			target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0, false, true));
			will.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 45, 0, false, false));
		}
		}
	}

	private static boolean isExpectedWeapon(WillEntity will) {
		return will.getMainHandItem().is(WillEquipmentRules.weaponItem(will.getOrigin(), will.getSchool(), will.getTier()));
	}

	private static int rangedInterval(WillEntity will) {
		return Math.max(35, 70 - will.getTier() * 8);
	}

	private static void fireBloodBolt(WillEntity will, LivingEntity target) {
		Level level = will.level();
		BloodBoltEntity bolt = new BloodBoltEntity(level, will, will.getMainHandItem());
		Vec3 aim = target.getEyePosition().subtract(will.getEyePosition());
		if (aim.lengthSqr() <= 1.0E-5D) return;
		aim = aim.normalize();
		bolt.setBaseDamage(Math.max(2.5D, will.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75D));
		bolt.shoot(aim.x, aim.y, aim.z, 3.2F, 1.0F);
		level.addFreshEntity(bolt);
		level.playSound(null, will.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.HOSTILE, 0.9F,
				0.9F + level.random.nextFloat() * 0.25F);
	}
}
