package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** The gift each facsimile body lends the will, called through the Thread of Will. Hand-coded per form. */
public final class PossessionAbilities {
	private static final ParticleColor PALE = new ParticleColor(226, 206, 200);
	public static final double TELEPORT_RANGE = 24.0;
	public static final double SLAM_RADIUS = 4.0;
	public static final float SLAM_DAMAGE = 4f;

	private PossessionAbilities() {}

	/** Uses {@code form}'s gift; returns whether it took (and so starts the cooldown). */
	public static boolean use(ServerPlayer player, SurrogateForm form) {
		ServerLevel level = player.serverLevel();
		switch (form) {
			case BAT -> {
				for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16),
						entity -> entity != player)) {
					near.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
				}
				level.playSound(null, player.blockPosition(), SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 1f, 1.4f);
				return true;
			}
			case ENDERMAN -> {
				HitResult hit = player.pick(TELEPORT_RANGE, 0f, false);
				if (!(hit instanceof BlockHitResult block) || hit.getType() == HitResult.Type.MISS) return false;
				Vec3 to = Vec3.atBottomCenterOf(block.getBlockPos().relative(block.getDirection()));
				if (!player.randomTeleport(to.x, to.y, to.z, true)) return false;
				level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 0.8f);
				return true;
			}
			case BLAZE -> {
				Vec3 look = player.getLookAngle();
				SmallFireball fireball = new SmallFireball(level, player, look.scale(0.6));
				fireball.setPos(player.getX() + look.x, player.getEyeY() - 0.1, player.getZ() + look.z);
				level.addFreshEntity(fireball);
				level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1f, 1f);
				return true;
			}
			case GOLEM -> {
				for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class,
						player.getBoundingBox().inflate(SLAM_RADIUS),
						entity -> entity != player && ManipulationCombatHelper.canHarm(player, entity))) {
					near.hurt(player.damageSources().playerAttack(player), SLAM_DAMAGE);
					near.knockback(1.2, player.getX() - near.getX(), player.getZ() - near.getZ());
				}
				level.sendParticles(HemoParticleData.sporiticSpore(PALE), player.getX(), player.getY() + 0.1,
						player.getZ(), 30, 1.5, 0.05, 1.5, 0.05);
				level.playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1f, 0.7f);
				return true;
			}
			case HORSE -> {
				Vec3 look = player.getLookAngle();
				player.setDeltaMovement(look.x * 1.2, 0.6, look.z * 1.2);
				player.hurtMarked = true;
				level.playSound(null, player.blockPosition(), SoundEvents.HORSE_JUMP, SoundSource.PLAYERS, 1f, 1f);
				return true;
			}
			case WOLF -> {
				Vec3 look = player.getLookAngle();
				player.setDeltaMovement(look.x * 1.6, 0.2, look.z * 1.6);
				player.hurtMarked = true;
				level.playSound(null, player.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.PLAYERS, 1f, 1f);
				return true;
			}
			default -> {
				return false;
			}
		}
	}
}
