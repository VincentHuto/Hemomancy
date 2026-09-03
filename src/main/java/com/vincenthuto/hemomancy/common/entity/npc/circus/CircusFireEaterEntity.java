package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;

public final class CircusFireEaterEntity extends CircusPerformerEntity {
	private int attackCooldown;

	public CircusFireEaterEntity(EntityType<? extends CircusFireEaterEntity> type, Level level) {
		super(type, level);
		setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemInit.living_torch.get()));
		setDropChance(EquipmentSlot.MAINHAND, 0.0F);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.22D).add(Attributes.ARMOR, 2.0D)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	@Override protected String roleId() { return "circus_fire_eater"; }
	@Override protected String texturePath() { return "textures/entity/circus/fire_eater_0.png"; }
	@Override protected int performanceDuration() { return 80; }

	@Override
	protected void tickPerformance(int actTick) {
		if (actTick == 48 && level() instanceof ServerLevel server) {
			Vec3 look = getLookAngle();
			server.sendParticles(ParticleTypes.FLAME, getX() + look.x, getEyeY() - 0.2D,
					getZ() + look.z, 18, 0.2D, 0.15D, 0.2D, 0.04D);
			level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.7F, 0.85F);
		}
	}

	@Override
	protected void tickDefense(LivingEntity target) {
		getNavigation().moveTo(target, 0.9D);
		if (attackCooldown-- > 0 || distanceToSqr(target) > 25.0D) return;
		attackCooldown = 80;
		Vec3 facing = getLookAngle();
		if (level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.FLAME, getX() + facing.x, getEyeY() - 0.2D,
					getZ() + facing.z, 35, 0.45D, 0.25D, 0.45D, 0.06D);
		}
		level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0F, 0.7F);
		for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5.0D))) {
			Vec3 offset = victim.position().subtract(position());
			if (victim == this || isTroupeMember(victim)
					|| !CircusPerformerRules.insideCone(facing.x, facing.z, offset.x, offset.z)) continue;
			if (victim.hurt(damageSources().mobAttack(this), 4.0F)) {
				victim.igniteForSeconds(3.0F);
				victim.push(facing.x * 0.7D, 0.2D, facing.z * 0.7D);
			}
		}
	}
}
