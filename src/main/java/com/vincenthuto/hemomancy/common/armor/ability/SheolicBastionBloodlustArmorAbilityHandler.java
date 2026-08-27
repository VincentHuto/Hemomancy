package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.armor.BodyIdiomArmorRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SheolicBastionBloodlustArmorAbilityHandler {
	private static final String BASTION_UNTIL_KEY = "hemomancy:sheolic_bastion_until";
	private static final String BASTION_X_KEY = "hemomancy:sheolic_bastion_x";
	private static final String BASTION_Y_KEY = "hemomancy:sheolic_bastion_y";
	private static final String BASTION_Z_KEY = "hemomancy:sheolic_bastion_z";
	private static final String SEARING_CONTACT_KEY = "hemomancy:searing_contact";
	private static final int BASTION_DURATION_TICKS = 120;

	private SheolicBastionBloodlustArmorAbilityHandler() {
	}

	public static void activateBastionStance(ServerPlayer player) {
		if (player.isShiftKeyDown()) {
			boolean enabled = BodyIdiomArmorRules.nextSearingContactState(
					player.getPersistentData().getBoolean(SEARING_CONTACT_KEY));
			player.getPersistentData().putBoolean(SEARING_CONTACT_KEY, enabled);
			player.displayClientMessage(Component.literal("Searing Contact: " + (enabled ? "ON" : "OFF"))
					.withStyle(enabled ? ChatFormatting.GOLD : ChatFormatting.GRAY), true);
			return;
		}
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

	public static boolean isBastionActive(ServerPlayer player) {
		long until = player.getPersistentData().getLong(BASTION_UNTIL_KEY);
		return until > player.level().getGameTime();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		updateSheolicPassives(player);
		updateBastionRoot(player);
		if (!ArmorSetHelper.hasFullSheolicBloodlust(player)) {
			player.getPersistentData().remove(SEARING_CONTACT_KEY);
		}
	}

	@SubscribeEvent
	public static void onPlayerAttack(AttackEntityEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| !(event.getTarget() instanceof LivingEntity target)) return;
		if (BodyIdiomArmorRules.searingContactActive(
				player.getPersistentData().getBoolean(SEARING_CONTACT_KEY),
				ArmorSetHelper.hasFullSheolicBloodlust(player))) {
			target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(),
					BodyIdiomArmorRules.SEARING_CONTACT_FIRE_TICKS));
		}
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
}
