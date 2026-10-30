package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.projectile.CrimsonFireballEntity;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * What the possession wheel does on the server: the visit's controls (the return hold, Still) and the gift each
 * facsimile body lends the will, hand-coded per action. Every request is checked by {@link PossessionWheelRules}
 * first: possessing, offered by the form, cooled.
 */
public final class PossessionAbilities {
	private static final ParticleColor PALE = new ParticleColor(226, 206, 200);
	public static final double TELEPORT_RANGE = 24.0;
	public static final double SLAM_RADIUS = 4.0;
	public static final float SLAM_DAMAGE = 4f;

	private PossessionAbilities() {}

	/** Carries out a wheel choice if the rules allow it; anything else is ignored. */
	public static void act(ServerPlayer player, PossessionAction action) {
		// A body still arriving, or already falling away, takes no orders.
		if (PossessionSequences.isStaged(player)) return;
		Possession possession = HemoCapabilityAccess.getPossession(player);
		SurrogateForm form = possession.form();
		long now = player.level().getGameTime();
		if (PossessionWheelRules.check(form, action, now, possession.readyAt(action)) != PossessionWheelRules.Verdict.USE)
			return;
		switch (action) {
			case RETURN -> {
				if (possession.returning()) return;
				possession.beginReturn(now + PossessionRules.RETURN_HOLD_TICKS, player.position());
				player.displayClientMessage(Component.translatable("message.hemomancy.possession.drawing_home")
						.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			}
			case STILL, CLIMB, HOVER -> {
				boolean on = possession.toggle(action);
				if (action == PossessionAction.HOVER) PossessionService.applyFlight(player, form, true);
				player.displayClientMessage(Component.translatable(on ? "message.hemomancy.possession.toggled_on"
						: "message.hemomancy.possession.toggled_off",
						Component.translatable("possession_action.hemomancy." + action.id())), true);
			}
			default -> {
				if (use(player, action)) {
					possession.setReadyAt(action, now + PossessionWheelRules.cooldownTicks(form, action));
				}
			}
		}
	}

	/** Uses a body's gift; returns whether it took (and so starts the cooldown). */
	static boolean use(ServerPlayer player, PossessionAction action) {
		ServerLevel level = player.serverLevel();
		switch (action) {
			case ECHOLOCATE -> {
				for (LivingEntity near : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16),
						entity -> entity != player)) {
					near.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
				}
				level.playSound(null, player.blockPosition(), SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 1f, 1.4f);
				return true;
			}
			case STEP -> {
				HitResult hit = player.pick(TELEPORT_RANGE, 0f, false);
				if (!(hit instanceof BlockHitResult block) || hit.getType() == HitResult.Type.MISS) return false;
				Vec3 to = Vec3.atBottomCenterOf(block.getBlockPos().relative(block.getDirection()));
				if (!player.randomTeleport(to.x, to.y, to.z, true)) return false;
				level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1f, 0.8f);
				return true;
			}
			case SPIT -> {
				Vec3 look = player.getLookAngle();
				CrimsonFireballEntity fireball = new CrimsonFireballEntity(level, player, look.scale(0.6));
				fireball.setPos(player.getX() + look.x, player.getEyeY() - 0.1, player.getZ() + look.z);
				level.addFreshEntity(fireball);
				level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1f, 1f);
				return true;
			}
			case SLAM -> {
				player.swing(InteractionHand.MAIN_HAND, true);
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
			case LEAP -> {
				Vec3 look = player.getLookAngle();
				player.setDeltaMovement(look.x * 1.2, 0.6, look.z * 1.2);
				player.hurtMarked = true;
				level.playSound(null, player.blockPosition(), SoundEvents.HORSE_JUMP, SoundSource.PLAYERS, 1f, 1f);
				return true;
			}
			case LUNGE -> {
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
