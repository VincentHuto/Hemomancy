package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.data.SporiticSporeParticleData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class SporiticThuribleEvents {
	private static final Map<UUID, Float> LAST_YAW = new ConcurrentHashMap<>();

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		ItemStack offhand = player.getItemBySlot(EquipmentSlot.OFFHAND);
		if (!(offhand.getItem() instanceof SporiticThuribleItem) || !SporiticThuribleItem.isLit(offhand)) {
			LAST_YAW.remove(player.getUUID());
			return;
		}
		Optional<SporiticThuribleSpore> spore = SporiticThuribleItem.getStoredSpore(offhand);
		if (spore.isEmpty() || !hasValidHarbingerState(player)) {
			SporiticThuribleItem.extinguish(offhand);
			LAST_YAW.remove(player.getUUID());
			return;
		}
		long gameTime = player.level().getGameTime();
		if (!SporiticThuribleItem.tickBurn(offhand, gameTime)) {
			LAST_YAW.remove(player.getUUID());
			return;
		}

		double intensity = getSwingIntensity(player);
		if (gameTime % SporiticThuribleRules.UPKEEP_INTERVAL_TICKS == 0
				&& !drainUpkeep(serverPlayer, offhand, intensity)) {
			LAST_YAW.remove(player.getUUID());
			return;
		}
		if (gameTime % SporiticThuribleRules.AMBIENT_PARTICLE_INTERVAL_TICKS == 0) {
			spawnAmbientCloud(serverPlayer, spore.orElseThrow(), intensity);
		}
		if (gameTime % SporiticThuribleRules.AURA_INTERVAL_TICKS == 0) {
			applyAura(serverPlayer, spore.orElseThrow(), intensity);
		}
	}

	private static boolean hasValidHarbingerState(Player player) {
		boolean bloodActive = HemoCapabilityAccess.getBloodVolume(player)
				.map(IBloodVolume::isActive)
				.orElse(false);
		return bloodActive && HemoCapabilityAccess.getPlayerDegreeNumber(player) >= SporiticThuribleRules.REQUIRED_DEGREE;
	}

	private static boolean drainUpkeep(ServerPlayer player, ItemStack stack, double intensity) {
		double drain = SporiticThuribleRules.bloodDrainPerSecond(intensity,
				SkillPointHelper.getSporiticAttunementLevel(player));
		Optional<IBloodVolume> volumeOpt = HemoCapabilityAccess.getBloodVolume(player);
		if (volumeOpt.isEmpty()) {
			SporiticThuribleItem.extinguish(stack);
			return false;
		}
		IBloodVolume volume = volumeOpt.orElseThrow();
		boolean drained = volume.drain(drain);
		if (drained) {
			volume.addBloodSpend(drain);
		} else {
			SporiticThuribleItem.extinguish(stack);
		}
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		return drained;
	}

	private static void spawnAmbientCloud(ServerPlayer owner, SporiticThuribleSpore spore, double intensity) {
		ServerLevel level = owner.serverLevel();
		double clampedIntensity = Mth.clamp(intensity, 0.0, 1.0);
		double spread = 0.45 + 0.8 * clampedIntensity;
		int closeCount = 6 + (int) (10 * clampedIntensity);
		int driftCount = 3 + (int) (7 * clampedIntensity);
		double x = owner.getX();
		double y = owner.getY() + 0.95;
		double z = owner.getZ();
		SporiticSporeParticleData particle = new SporiticSporeParticleData(spore.red(), spore.green(), spore.blue());

		level.sendParticles(particle, x, y, z, closeCount, spread, 0.35, spread, 0.015);
		level.sendParticles(particle, x, y + 0.25, z, driftCount, spread * 0.95, 0.45, spread * 0.95, 0.004);
	}

	private static void applyAura(ServerPlayer owner, SporiticThuribleSpore spore, double intensity) {
		ServerLevel level = owner.serverLevel();
		double radius = SporiticThuribleRules.auraRadius(intensity,
				SkillPointHelper.getSporiticAttunementLevel(owner));
		AABB area = owner.getBoundingBox().inflate(radius);
		long expires = level.getGameTime() + SporiticThuribleRules.RESONANCE_DURATION_TICKS;

		for (Player nearby : level.getEntitiesOfClass(Player.class, area, Player::isAlive)) {
			boolean bloodActive = HemoCapabilityAccess.getBloodVolume(nearby)
					.map(IBloodVolume::isActive)
					.orElse(false);
			if (!bloodActive || HemoCapabilityAccess.getPlayerDegreeNumber(nearby) <= 0) {
				continue;
			}
			nearby.addEffect(new MobEffectInstance(EffectInit.sporitic_resonance,
					SporiticThuribleRules.RESONANCE_DURATION_TICKS, 0, true, true, true));
			SporiticThuribleResonanceState.grant(nearby, spore.tendency(), expires);
		}

		for (Monster mob : level.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
			mob.addEffect(new MobEffectInstance(EffectInit.blood_loss,
					SporiticThuribleRules.BLOOD_LOSS_DURATION_TICKS, 0, true, true, true));
			mob.addEffect(new MobEffectInstance(MobEffects.WITHER,
					SporiticThuribleRules.WITHER_DURATION_TICKS, 0, true, true, true));
			spore.applySecondary(mob);
		}

		int particles = 12 + (int) (24 * Mth.clamp(intensity, 0.0, 1.0));
		level.sendParticles(new SporiticSporeParticleData(spore.red(), spore.green(), spore.blue()),
				owner.getX(), owner.getY() + 0.9, owner.getZ(), particles,
				radius * 0.35, 0.35, radius * 0.35, 0.025);
	}

	private static double getSwingIntensity(Player player) {
		float previousYaw = LAST_YAW.getOrDefault(player.getUUID(), player.getYRot());
		float yawDelta = Math.abs(Mth.wrapDegrees(player.getYRot() - previousYaw));
		LAST_YAW.put(player.getUUID(), player.getYRot());

		double movement = player.getDeltaMovement().horizontalDistance();
		double movementIntensity = movement * 4.0;
		double turnIntensity = yawDelta / 90.0;
		double attackIntensity = player.swingTime > 0 ? 0.35 : 0.0;
		return Mth.clamp(movementIntensity + turnIntensity + attackIntensity, 0.0, 1.0);
	}
}
