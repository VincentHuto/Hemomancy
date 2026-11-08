package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
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
	/** The breath's flat facing, locked when the wind-up starts so the drawn cone is the burned cone. */
	private Vec3 breathFacing = Vec3.ZERO;

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
	@Override protected String texturePath() { return "textures/entity/npc/harbinger/circus/fire_eater_0.png"; }
	@Override protected int performanceDuration() { return 80; }

	@Override
	protected void tickPerformance(int actTick) {
		if (actTick == 48 && level() instanceof ServerLevel server) {
			Vec3 look = getLookAngle();
			server.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME, 0.3F, 0.9F, 12), getX() + look.x,
					getEyeY() - 0.2D, getZ() + look.z, 18, 0.2D, 0.15D, 0.2D, 0.04D);
			level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.7F, 0.85F);
		}
		if (actTick >= 52 && actTick <= 68 && level() instanceof ServerLevel server) {
			Vec3 look = getLookAngle();
			Vec3 stream = new Vec3(look.x, 0.55D, look.z).normalize();
			Vec3 mouth = new Vec3(getX() + look.x * 0.65D, getEyeY() - 0.2D,
					getZ() + look.z * 0.65D);
			CircusEffects.flameStream(server, mouth, stream, 8, 0.55D, 2, 0.07D);
		}
	}

	/** The cone is an area move: 20 ticks of wind-up with the 5-block, 55-degree wedge drawn on the floor. */
	@Override
	public BossMoveTimeline defenseMove() {
		return CircusPerformerRules.AREA_MOVE;
	}

	@Override
	protected boolean tracksDuringWindup() {
		return false;
	}

	@Override
	protected void tickDefense(LivingEntity target) {
		getNavigation().moveTo(target, 0.9D);
		if (attackCooldown-- > 0 || distanceToSqr(target) > 25.0D) return;
		attackCooldown = 80 - defenseMove().total();
		getNavigation().stop();
		Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
		breathFacing = toward.lengthSqr() > 1.0E-4D ? toward.normalize() : getLookAngle().multiply(1.0D, 0.0D, 1.0D);
		beginDefenseMove(target);
	}

	@Override
	protected void telegraphDefense(ServerLevel server, LivingEntity target, int tick, BossMoveTimeline move) {
		Vec3 mouth = new Vec3(getX() + breathFacing.x * 0.5D, getEyeY() - 0.2D, getZ() + breathFacing.z * 0.5D);
		getLookControl().setLookAt(mouth.x + breathFacing.x * 4.0D, mouth.y, mouth.z + breathFacing.z * 4.0D);
		float charge = tick / (float) move.windup();
		// The torch is drawn in: embers rise toward the mouth as the breath is held.
		CircusEffects.gather(server, mouth, CircusEffects.FLAME, charge);
		server.sendParticles(HemoParticleData.emberSpark(CircusEffects.FLAME_CORE, 0.18F, 0.9F, 8), mouth.x,
				mouth.y, mouth.z, 1 + Math.round(charge * 3.0F), 0.1D, 0.1D, 0.1D, 0.0D);
		if (tick % 4 == 0) BossTelegraphs.cone(server, position(), breathFacing, 5.0D, 55.0D, CircusEffects.FLAME);
	}

	@Override
	protected void strikeDefense(LivingEntity target) {
		Vec3 facing = breathFacing;
		if (level() instanceof ServerLevel server) {
			Vec3 mouth = new Vec3(getX() + facing.x * 0.5D, getEyeY() - 0.2D, getZ() + facing.z * 0.5D);
			for (double yaw : new double[] { -22.0D, -11.0D, 0.0D, 11.0D, 22.0D }) {
				Vec3 tongue = facing.yRot((float) Math.toRadians(yaw)).add(0.0D, -0.06D, 0.0D);
				CircusEffects.flameStream(server, mouth, tongue, 9, 0.55D, 2, 0.18D);
			}
			BossTelegraphs.cone(server, position(), facing, 5.0D, 55.0D, CircusEffects.FLAME_CORE);
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
