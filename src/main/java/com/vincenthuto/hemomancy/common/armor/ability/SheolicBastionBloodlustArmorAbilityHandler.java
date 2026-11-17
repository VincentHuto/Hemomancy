package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.util.FlightArbiter;
import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.armor.ArmorSetBonusRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoText;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
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
	private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
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
			boolean enabled = ArmorSetBonusRules.nextSearingContactState(
					player.getPersistentData().getBoolean(SEARING_CONTACT_KEY));
			player.getPersistentData().putBoolean(SEARING_CONTACT_KEY, enabled);
			player.displayClientMessage(HemoText.translatable("message.hemomancy.sheolic_bastion_bloodlust_armor_ability_handler.searing_contact",
					(enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)).withStyle(enabled ? ChatFormatting.GOLD : ChatFormatting.GRAY), true);
			return;
		}
		if (isBastionActive(player)) {
			clearBastion(player);
			player.displayClientMessage(Component.translatable("message.hemomancy.sheolic_bastion_bloodlust_armor_ability_handler.bastion_stance_released").withStyle(ChatFormatting.DARK_RED), true);
			return;
		}
		long until = player.level().getGameTime() + BASTION_DURATION_TICKS;
		player.getPersistentData().putLong(BASTION_UNTIL_KEY, until);
		player.getPersistentData().putDouble(BASTION_X_KEY, player.getX());
		player.getPersistentData().putDouble(BASTION_Y_KEY, player.getY());
		player.getPersistentData().putDouble(BASTION_Z_KEY, player.getZ());
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0.0F;
		player.displayClientMessage(Component.translatable("message.hemomancy.sheolic_bastion_bloodlust_armor_ability_handler.bastion_stance").withStyle(ChatFormatting.DARK_RED), true);
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
		if (ArmorSetBonusRules.searingContactActive(
				player.getPersistentData().getBoolean(SEARING_CONTACT_KEY),
				ArmorSetHelper.hasFullSheolicBloodlust(player))) {
			target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(),
					ArmorSetBonusRules.SEARING_CONTACT_FIRE_TICKS));
		}
	}

	/** Defender stage of {@link com.vincenthuto.hemomancy.common.damage.DamageStages}. */
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
		FlightArbiter.ground(player);
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 255, false, false, false));
	}

	private static void clearBastion(ServerPlayer player) {
		player.getPersistentData().remove(BASTION_UNTIL_KEY);
		player.getPersistentData().remove(BASTION_X_KEY);
		player.getPersistentData().remove(BASTION_Y_KEY);
		player.getPersistentData().remove(BASTION_Z_KEY);
	}

	private static void triggerCrimsonRetribution(ServerPlayer player, LivingEntity attacker) {
		if (!ManipulationCombatHelper.canHarm(player, attacker)) {
			return;
		}
		attacker.hurt(player.damageSources().indirectMagic(player, player), 4.0F);
		attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(), 80));
		BlockPos pos = attacker.blockPosition();
		BlockState state = player.level().getBlockState(pos);
		if (state.canBeReplaced() || state.isAir()) {
			PlayerBlockEdits.place(player, pos, BlockInit.crimson_flames.get().defaultBlockState());
		}
		player.serverLevel().sendParticles(HemoParticleData.emberSpark(EnumBloodTemper.FLAMMEUS.getColor(), 0.05F, 1.0F, 20),
				attacker.getX(), attacker.getY() + 0.8D, attacker.getZ(),
				20, 0.35D, 0.65D, 0.35D, 0.02D);
		player.serverLevel().sendParticles(HemoParticleData.bloodCell(BLOOD),
				attacker.getX(), attacker.getY() + 0.8D, attacker.getZ(),
				8, 0.35D, 0.65D, 0.35D, 0.02D);
	}
}
