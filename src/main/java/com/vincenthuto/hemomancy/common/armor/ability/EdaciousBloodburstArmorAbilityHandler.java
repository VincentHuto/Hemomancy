package com.vincenthuto.hemomancy.common.armor.ability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class EdaciousBloodburstArmorAbilityHandler {
	private static final String EDACIOUS_FLIGHT_KEY = "hemomancy:edacious_flight_enabled";
	private static final int BLOODBURST_NEEDLE_COUNT = 36;
	private static final double BLOODBURST_DAMAGE = 4.0D;

	private EdaciousBloodburstArmorAbilityHandler() {
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

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) {
			return;
		}
		updateEdaciousFlight(player);
	}
}
