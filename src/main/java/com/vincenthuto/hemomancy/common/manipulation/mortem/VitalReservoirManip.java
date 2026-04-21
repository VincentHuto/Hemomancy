package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Vital Reservoir — a T2 (MEDIOCRITAS) quick manipulation that converts
 * the player's experience into blood volume.
 * <p>
 * Consumes 10 XP levels and restores 1000 blood volume.
 * Will not activate if the player has fewer than 10 levels or if
 * blood volume is already full.
 * <p>
 * A mid-game resource conversion utility: trade excess XP for blood.
 */
public class VitalReservoirManip extends BloodManipulation {

	private static final int XP_LEVELS_COST = 10;
	private static final double BLOOD_RESTORE = 1000.0;

	public VitalReservoirManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (player.experienceLevel < XP_LEVELS_COST) {
			player.displayClientMessage(
					Component.literal("§cNot enough experience! (Need " + XP_LEVELS_COST + " levels)"), true);
			return;
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			return;
		}

		if (volume.isFull()) {
			player.displayClientMessage(
					Component.literal("§cBlood volume is already full!"), true);
			return;
		}

		// Consume XP levels
		player.giveExperienceLevels(-XP_LEVELS_COST);

		// Restore blood (capped at max)
		double toRestore = Math.min(BLOOD_RESTORE, volume.getMaxBloodVolume() - volume.getBloodVolume());
		volume.addBloodVolume(toRestore);

		// Sync blood volume to client
		if (player instanceof ServerPlayer serverPlayer) {
			PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(volume));
		}

		world.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 0.6f);
		world.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.5f, 1.3f);

		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;

			// Green XP → dark red blood conversion particles
			for (int i = 0; i < 20; i++) {
				// Green particles rising (XP leaving)
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(0, 180 + random.nextFloat() * 75, 0)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						pos.getY() + 0.5 + random.nextDouble() * 0.8,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
						1, 0f, 0.2f, 0f, 0.02f);
			}
			for (int i = 0; i < 15; i++) {
				// Dark red particles converging (blood gained)
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								120 + random.nextFloat() * 80, 0, random.nextFloat() * 30)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.2,
						pos.getY() + 1.5 + (random.nextDouble() - 0.5) * 0.4,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.2,
						1, 0f, -0.1f, 0f, 0.01f);
			}
		}
	}
}
