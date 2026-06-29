package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.summon.PhantasmalEchoEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
	private static final int MASQUERADE_ECHO_COUNT = 8;
	private static final int MASQUERADE_ECHO_DURATION_TICKS = 200;
	private static final double MASQUERADE_MIN_SPAWN_RADIUS = 1.5D;
	private static final double MASQUERADE_MAX_SPAWN_RADIUS = 3.0D;
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

	public static void activateMasqueradeOfTheForgotten(ServerPlayer player) {
		spawnMasqueradeEchoes(player);
		player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,
				player.getX(), player.getY() + 1.0D, player.getZ(),
				90, 1.0D, 0.75D, 1.0D, 0.08D);
		player.serverLevel().sendParticles(ParticleTypes.SMOKE,
				player.getX(), player.getY() + 0.8D, player.getZ(),
				45, 0.9D, 0.45D, 0.9D, 0.02D);
		player.level().playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE,
				SoundSource.PLAYERS, 0.9F, 0.85F);
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
		if (ArmorSetAbilityRegistry.isAbilityAvailable(player, ArmorSetAbilityRegistry.MASQUERADE_OF_THE_FORGOTTEN)
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

	private static void spawnMasqueradeEchoes(ServerPlayer player) {
		boolean hasNearbyHostiles = !player.level().getEntitiesOfClass(Monster.class,
				player.getBoundingBox().inflate(18.0D), Monster::isAlive).isEmpty();
		for (int i = 0; i < MASQUERADE_ECHO_COUNT; i++) {
			double angle = (Math.PI * 2.0D * i) / MASQUERADE_ECHO_COUNT
					+ player.getRandom().nextDouble() * 0.35D;
			double radius = MASQUERADE_MIN_SPAWN_RADIUS
					+ player.getRandom().nextDouble() * (MASQUERADE_MAX_SPAWN_RADIUS - MASQUERADE_MIN_SPAWN_RADIUS);
			BlockPos spawnPos = findEchoSpawnPosition(player, angle, radius);
			PhantasmalEchoEntity echo = new PhantasmalEchoEntity(EntityInit.phantasmal_echo.get(), player.level());
			echo.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
					player.getYRot(), player.getXRot());
			Vec3 outward = new Vec3(Math.cos(angle), 0.08D, Math.sin(angle))
					.normalize()
					.scale(0.35D + player.getRandom().nextDouble() * 0.25D);
			echo.configureFromOwner(player, chooseMasqueradeMode(i, hasNearbyHostiles),
					MASQUERADE_ECHO_DURATION_TICKS, outward);
			player.level().addFreshEntity(echo);
		}
	}

	private static PhantasmalEchoEntity.BehaviorMode chooseMasqueradeMode(int index, boolean hasNearbyHostiles) {
		if (hasNearbyHostiles && index < 2) {
			return PhantasmalEchoEntity.BehaviorMode.AGGRESSIVE;
		}
		return switch (index % 4) {
			case 0 -> PhantasmalEchoEntity.BehaviorMode.MIRRORING;
			case 1 -> PhantasmalEchoEntity.BehaviorMode.FLEEING;
			case 2 -> PhantasmalEchoEntity.BehaviorMode.CIRCLING;
			default -> PhantasmalEchoEntity.BehaviorMode.AGGRESSIVE;
		};
	}

	private static BlockPos findEchoSpawnPosition(ServerPlayer player, double angle, double radius) {
		BlockPos center = BlockPos.containing(player.getX() + Math.cos(angle) * radius,
				player.getY(), player.getZ() + Math.sin(angle) * radius);
		for (int y = 1; y >= -2; y--) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos candidate = center.offset(x, y, z);
					if (isSafeLanding(player, candidate)) {
						return candidate;
					}
				}
			}
		}
		return player.blockPosition();
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
