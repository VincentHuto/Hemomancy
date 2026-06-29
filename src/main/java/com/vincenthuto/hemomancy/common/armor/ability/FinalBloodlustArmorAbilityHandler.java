package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FinalBloodlustArmorAbilityHandler {
	private static final String EDACIOUS_FLIGHT_KEY = "hemomancy:edacious_flight_enabled";
	private static final String BASTION_UNTIL_KEY = "hemomancy:sheolic_bastion_until";
	private static final String BASTION_X_KEY = "hemomancy:sheolic_bastion_x";
	private static final String BASTION_Y_KEY = "hemomancy:sheolic_bastion_y";
	private static final String BASTION_Z_KEY = "hemomancy:sheolic_bastion_z";
	private static final String PHANTASMAL_REACTIVE_COOLDOWN_KEY = "hemomancy:phantasmal_reactive_until";
	private static final int BASTION_DURATION_TICKS = 120;
	private static final int BLOODBURST_NEEDLE_COUNT = 36;
	private static final double BLOODBURST_DAMAGE = 4.0D;
	private static final double PHANTASMAL_STEP_RANGE = 32.0D;
	private static final double PHANTASMAL_DISPLACEMENT = 8.0D;
	private static final int PHANTASMAL_REACTIVE_COOLDOWN_TICKS = 40;

	private FinalBloodlustArmorAbilityHandler() {
	}

	public static void activateBloodburst(ServerPlayer player) {
		Vec3 origin = player.position().add(0.0D, 1.0D, 0.0D);
		for (int i = 0; i < BLOODBURST_NEEDLE_COUNT; i++) {
			double yaw = (Math.PI * 2.0D * i) / BLOODBURST_NEEDLE_COUNT;
			double pitch = (player.getRandom().nextDouble() - 0.5D) * 0.45D;
			Vec3 direction = new Vec3(Math.cos(yaw), pitch, Math.sin(yaw)).normalize();
			BloodNeedleEntity needle = new BloodNeedleEntity(player.level(), player);
			needle.setPos(origin.x, origin.y, origin.z);
			needle.setBloodburstNeedle(true);
			needle.setBaseDamage(BLOODBURST_DAMAGE);
			needle.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			needle.shoot(direction.x, direction.y, direction.z, 1.65F, 3.0F);
			player.level().addFreshEntity(needle);
		}
		player.serverLevel().sendParticles(ParticleTypes.CRIMSON_SPORE,
				player.getX(), player.getY() + 1.0D, player.getZ(),
				80, 1.0D, 0.55D, 1.0D, 0.05D);
		player.level().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_3.value(),
				SoundSource.PLAYERS, 0.9F, 0.6F);
	}

	public static void activateBastionStance(ServerPlayer player) {
		if (isBastionActive(player)) {
			clearBastion(player);
			player.displayClientMessage(Component.literal("Bastion Stance released.").withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		long until = player.level().getGameTime() + BASTION_DURATION_TICKS;
		player.getPersistentData().putLong(BASTION_UNTIL_KEY, until);
		player.getPersistentData().putDouble(BASTION_X_KEY, player.getX());
		player.getPersistentData().putDouble(BASTION_Y_KEY, player.getY());
		player.getPersistentData().putDouble(BASTION_Z_KEY, player.getZ());
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		player.displayClientMessage(Component.literal("Bastion Stance.").withStyle(ChatFormatting.DARK_RED), true);
	}

	public static void activatePhantasmalStep(ServerPlayer player) {
		BlockPos landingPos = findLookLandingPosition(player, PHANTASMAL_STEP_RANGE);
		if (landingPos == null) {
			player.displayClientMessage(Component.literal("No visible landing place.").withStyle(ChatFormatting.DARK_PURPLE), true);
			return;
		}
		Vec3 oldPos = player.position();
		double destX = landingPos.getX() + 0.5D;
		double destY = landingPos.getY();
		double destZ = landingPos.getZ() + 0.5D;
		player.teleportTo(destX, destY, destZ);
		player.fallDistance = 0.0F;
		player.resetFallDistance();
		player.level().playSound(null, landingPos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.15F);
		player.serverLevel().sendParticles(ParticleTypes.PORTAL, oldPos.x, oldPos.y + 1.0D, oldPos.z,
				35, 0.45D, 0.75D, 0.45D, 0.08D);
		player.serverLevel().sendParticles(ParticleTypes.PORTAL, destX, destY + 1.0D, destZ,
				35, 0.45D, 0.75D, 0.45D, 0.08D);
	}

	public static void updateEdaciousFlight(ServerPlayer player) {
		boolean hasSet = ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST);
		boolean marked = player.getPersistentData().getBoolean(EDACIOUS_FLIGHT_KEY);
		if (hasSet) {
			if (!player.getAbilities().mayfly || !marked) {
				player.getAbilities().mayfly = true;
				player.getPersistentData().putBoolean(EDACIOUS_FLIGHT_KEY, true);
			}
			player.getAbilities().setFlyingSpeed(0.025F);
			player.onUpdateAbilities();
			player.fallDistance = 0.0F;
		} else if (marked) {
			player.getPersistentData().remove(EDACIOUS_FLIGHT_KEY);
			if (!player.isCreative() && !player.isSpectator()) {
				player.getAbilities().mayfly = false;
				player.getAbilities().flying = false;
			}
			player.getAbilities().setFlyingSpeed(0.05F);
			player.onUpdateAbilities();
		}
	}

	public static boolean isBastionActive(ServerPlayer player) {
		long until = player.getPersistentData().getLong(BASTION_UNTIL_KEY);
		return until > player.level().getGameTime();
	}

	public static void triggerDistortedPresence(ServerPlayer player) {
		if (player.getLastHurtByMob() instanceof LivingEntity attacker) {
			triggerDistortedPresence(player, attacker);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		updateEdaciousFlight(player);
		updateSheolicPassives(player);
		updateBastionRoot(player);
	}

	@SubscribeEvent
	public static void onPlayerDamage(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		if (isBastionActive(player)) {
			event.setNewDamage(0.0F);
			player.setDeltaMovement(Vec3.ZERO);
			player.fallDistance = 0.0F;
			return;
		}
		if (ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.SHEOLIC_BASTION_STANCE)) {
			if (event.getSource().is(DamageTypeTags.IS_FIRE)
					|| event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LAVA)
					|| event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
				event.setNewDamage(0.0F);
				player.clearFire();
			}
			if (event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player) {
				triggerCrimsonRetribution(player, attacker);
			}
		}
		if (ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.PHANTASMAL_STEP)
				&& event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player) {
			triggerDistortedPresence(player, attacker);
		}
	}

	private static void updateSheolicPassives(ServerPlayer player) {
		if (!ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.SHEOLIC_BASTION_STANCE)) {
			return;
		}
		player.fallDistance = 0.0F;
		player.clearFire();
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, true));
	}

	private static void updateBastionRoot(ServerPlayer player) {
		if (!isBastionActive(player)) {
			if (player.getPersistentData().contains(BASTION_UNTIL_KEY)
					&& player.getPersistentData().getLong(BASTION_UNTIL_KEY) <= player.level().getGameTime()) {
				clearBastion(player);
			}
			return;
		}
		if (!ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.SHEOLIC_BASTION_STANCE)) {
			clearBastion(player);
			return;
		}
		double x = player.getPersistentData().getDouble(BASTION_X_KEY);
		double y = player.getPersistentData().getDouble(BASTION_Y_KEY);
		double z = player.getPersistentData().getDouble(BASTION_Z_KEY);
		player.teleportTo(x, y, z);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		player.stopFallFlying();
		player.getAbilities().flying = false;
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 255, false, false, false));
	}

	private static void clearBastion(ServerPlayer player) {
		player.getPersistentData().remove(BASTION_UNTIL_KEY);
		player.getPersistentData().remove(BASTION_X_KEY);
		player.getPersistentData().remove(BASTION_Y_KEY);
		player.getPersistentData().remove(BASTION_Z_KEY);
	}

	private static void triggerCrimsonRetribution(ServerPlayer player, LivingEntity attacker) {
		attacker.hurt(player.damageSources().magic(), 4.0F);
		attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), 80));
		BlockPos pos = attacker.blockPosition();
		BlockState state = player.level().getBlockState(pos);
		if (state.canBeReplaced() || state.isAir()) {
			player.level().setBlock(pos, BlockInit.crimson_flames.get().defaultBlockState(), 3);
		}
		player.serverLevel().sendParticles(ParticleTypes.CRIMSON_SPORE,
				attacker.getX(), attacker.getY() + 0.8D, attacker.getZ(),
				28, 0.35D, 0.65D, 0.35D, 0.04D);
	}

	private static void triggerDistortedPresence(ServerPlayer player, LivingEntity attacker) {
		long now = player.level().getGameTime();
		if (player.getPersistentData().getLong(PHANTASMAL_REACTIVE_COOLDOWN_KEY) > now) {
			return;
		}
		if (player.getRandom().nextFloat() > 0.75F) {
			return;
		}
		player.getPersistentData().putLong(PHANTASMAL_REACTIVE_COOLDOWN_KEY,
				now + PHANTASMAL_REACTIVE_COOLDOWN_TICKS);
		attacker.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, true, true));
		attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING, 120, 0, false, true, true));
		BlockPos destination = findAttackerDisplacement(player, attacker);
		if (destination != null) {
			attacker.teleportTo(destination.getX() + 0.5D, destination.getY(), destination.getZ() + 0.5D);
			player.level().playSound(null, destination, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7F, 0.8F);
		}
		player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,
				attacker.getX(), attacker.getY() + 1.0D, attacker.getZ(),
				32, 0.45D, 0.75D, 0.45D, 0.04D);
	}

	private static BlockPos findLookLandingPosition(ServerPlayer player, double range) {
		Vec3 eyePos = player.getEyePosition(1.0F);
		Vec3 lookVec = player.getViewVector(1.0F);
		Vec3 endPos = eyePos.add(lookVec.scale(range));
		BlockHitResult hitResult = player.level().clip(new ClipContext(
				eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		if (hitResult.getType() == HitResult.Type.MISS) {
			return null;
		}
		BlockPos targetBlock = hitResult.getBlockPos();
		BlockPos landingPos = targetBlock.above();
		if (isSafeLanding(player, landingPos)) {
			return landingPos;
		}
		landingPos = targetBlock.relative(hitResult.getDirection());
		return isSafeLanding(player, landingPos) ? landingPos : null;
	}

	private static BlockPos findAttackerDisplacement(ServerPlayer player, LivingEntity attacker) {
		Vec3 away = attacker.position().subtract(player.position());
		if (away.lengthSqr() < 0.001D) {
			away = player.getLookAngle().reverse();
		}
		away = away.normalize().scale(PHANTASMAL_DISPLACEMENT);
		BlockPos center = BlockPos.containing(attacker.position().add(away));
		for (int y = 1; y >= -2; y--) {
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					BlockPos candidate = center.offset(x, y, z);
					if (isSafeLanding(player, candidate)) {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private static boolean isSafeLanding(ServerPlayer player, BlockPos pos) {
		BlockState feet = player.level().getBlockState(pos);
		BlockState head = player.level().getBlockState(pos.above());
		BlockState below = player.level().getBlockState(pos.below());
		return (feet.isAir() || feet.getCollisionShape(player.level(), pos).isEmpty())
				&& (head.isAir() || head.getCollisionShape(player.level(), pos.above()).isEmpty())
				&& !below.isAir()
				&& below.getBlock() != Blocks.LAVA
				&& !below.getCollisionShape(player.level(), pos.below()).isEmpty();
	}
}
